#!/bin/bash

############################################################
# CT-Tibet-WMS 全面安全扫描脚本
# 功能: 自动化执行多种安全扫描工具
# 作者: Security Team
# 日期: 2025-11-24
############################################################

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 配置
REPORT_DIR="security-reports"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 打印带颜色的信息
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_section() {
    echo ""
    echo "=========================================="
    echo "$1"
    echo "=========================================="
}

# 创建报告目录
mkdir -p "$REPORT_DIR/$TIMESTAMP"
cd "$PROJECT_DIR"

print_section "CT-Tibet-WMS Security Scan - $TIMESTAMP"

# ==================== 1. 依赖漏洞扫描 (OWASP Dependency-Check) ====================
print_section "1. OWASP Dependency-Check - 依赖漏洞扫描"

if command -v mvn &> /dev/null; then
    print_info "运行OWASP Dependency-Check..."
    cd backend

    # 首次运行会下载漏洞数据库,耗时较长
    mvn dependency-check:check \
        -DfailBuildOnCVSS=7 \
        -DoutputDirectory="../$REPORT_DIR/$TIMESTAMP/dependency-check" \
        -Dformat=ALL \
        || print_warn "发现高危漏洞 (CVSS >= 7.0)"

    cd ..

    # 复制HTML报告到总结目录
    if [ -f "$REPORT_DIR/$TIMESTAMP/dependency-check/dependency-check-report.html" ]; then
        print_info "✅ 依赖扫描报告: $REPORT_DIR/$TIMESTAMP/dependency-check/dependency-check-report.html"
    fi
else
    print_error "❌ Maven未安装,跳过依赖扫描"
fi


# ==================== 2. 容器镜像扫描 (Trivy) ====================
print_section "2. Trivy - 容器镜像漏洞扫描"

if command -v trivy &> /dev/null; then
    print_info "扫描Docker镜像..."

    # 扫描基础镜像
    trivy image --severity HIGH,CRITICAL \
        --format json \
        --output "$REPORT_DIR/$TIMESTAMP/trivy-openjdk.json" \
        openjdk:11-jre-slim \
        || true

    trivy image --severity HIGH,CRITICAL \
        --format table \
        --output "$REPORT_DIR/$TIMESTAMP/trivy-openjdk.txt" \
        openjdk:11-jre-slim \
        || true

    # 如果本地有构建的镜像,也扫描
    if docker images | grep -q "ct-tibet-wms"; then
        trivy image --severity HIGH,CRITICAL \
            --format json \
            --output "$REPORT_DIR/$TIMESTAMP/trivy-wms.json" \
            ct-tibet-wms:latest \
            || true
    fi

    print_info "✅ Trivy扫描报告: $REPORT_DIR/$TIMESTAMP/trivy-*.json"
else
    print_warn "❌ Trivy未安装,跳过容器扫描"
    print_info "安装命令: https://aquasecurity.github.io/trivy/latest/getting-started/installation/"
fi


# ==================== 3. 敏感信息扫描 (Gitleaks) ====================
print_section "3. Gitleaks - 敏感信息泄露扫描"

if command -v gitleaks &> /dev/null; then
    print_info "扫描代码中的密钥/密码..."

    gitleaks detect \
        --source . \
        --report-path "$REPORT_DIR/$TIMESTAMP/gitleaks-report.json" \
        --verbose \
        || print_warn "发现敏感信息泄露"

    # 生成SARIF格式 (用于GitHub Security)
    gitleaks detect \
        --source . \
        --report-format sarif \
        --report-path "$REPORT_DIR/$TIMESTAMP/gitleaks-report.sarif" \
        || true

    print_info "✅ Gitleaks报告: $REPORT_DIR/$TIMESTAMP/gitleaks-report.json"
else
    print_warn "❌ Gitleaks未安装,跳过敏感信息扫描"
    print_info "安装命令: https://github.com/gitleaks/gitleaks#installing"
fi


# ==================== 4. 代码静态分析 (SpotBugs) ====================
print_section "4. SpotBugs - 静态代码分析"

if command -v mvn &> /dev/null; then
    print_info "运行SpotBugs + FindSecBugs..."
    cd backend

    mvn spotbugs:check \
        -Dspotbugs.xmlOutput=true \
        -Dspotbugs.htmlOutput=true \
        || print_warn "发现潜在安全问题"

    # 复制报告
    if [ -f "target/spotbugsXml.xml" ]; then
        cp target/spotbugsXml.xml "../$REPORT_DIR/$TIMESTAMP/spotbugs.xml"
    fi
    if [ -f "target/site/spotbugs.html" ]; then
        cp target/site/spotbugs.html "../$REPORT_DIR/$TIMESTAMP/spotbugs.html"
    fi

    cd ..
    print_info "✅ SpotBugs报告: $REPORT_DIR/$TIMESTAMP/spotbugs.html"
else
    print_error "❌ Maven未安装,跳过静态分析"
fi


# ==================== 5. 端口和服务扫描 (Nmap) ====================
print_section "5. Nmap - 端口和服务扫描"

if command -v nmap &> /dev/null; then
    print_info "扫描本地服务端口..."

    # 扫描常见端口
    nmap -sV -p 3306,6379,5672,48888,80,443 \
        -oN "$REPORT_DIR/$TIMESTAMP/nmap-scan.txt" \
        localhost \
        || true

    print_info "✅ Nmap报告: $REPORT_DIR/$TIMESTAMP/nmap-scan.txt"
else
    print_warn "❌ Nmap未安装,跳过端口扫描"
fi


# ==================== 6. SQL注入测试 (SQLMap) ====================
print_section "6. SQL注入测试 (手动执行)"

print_info "SQL注入测试需手动执行:"
echo "sqlmap -u 'http://localhost:48888/api/users?pageNum=1&keyword=test' \\"
echo "  --cookie='Authorization: Bearer YOUR_TOKEN' \\"
echo "  --batch --level=3 --risk=2 \\"
echo "  --output-dir=$REPORT_DIR/$TIMESTAMP/sqlmap"


# ==================== 7. Web应用漏洞扫描 (OWASP ZAP) ====================
print_section "7. OWASP ZAP - Web应用漏洞扫描"

if command -v zap-cli &> /dev/null; then
    print_info "运行OWASP ZAP扫描..."

    # 启动ZAP守护进程
    zap-cli start --start-options '-config api.disablekey=true' || true
    sleep 5

    # 快速扫描
    zap-cli quick-scan \
        --self-contained \
        --spider \
        --ajax-spider \
        http://localhost:48888 \
        || true

    # 生成报告
    zap-cli report \
        -o "$REPORT_DIR/$TIMESTAMP/zap-report.html" \
        -f html \
        || true

    # 停止ZAP
    zap-cli shutdown || true

    print_info "✅ ZAP报告: $REPORT_DIR/$TIMESTAMP/zap-report.html"
else
    print_warn "❌ OWASP ZAP未安装,跳过Web扫描"
    print_info "安装命令: pip install zapcli"
    print_info "手动扫描: 访问 http://localhost:8080 (ZAP代理)"
fi


# ==================== 8. 配置文件安全检查 ====================
print_section "8. 配置文件安全检查"

print_info "检查敏感配置..."

# 检查默认密码
print_info "检查默认密码..."
grep -r "password.*root" backend/src/main/resources/ \
    | grep -v ".git" \
    | tee "$REPORT_DIR/$TIMESTAMP/default-passwords.txt" \
    || print_info "✅ 未发现默认密码"

# 检查硬编码密钥
print_info "检查硬编码密钥..."
grep -r -E "(secret|key|password).*=.*['\"][a-zA-Z0-9]{8,}" backend/src/main/resources/ \
    | grep -v ".git" \
    | tee "$REPORT_DIR/$TIMESTAMP/hardcoded-secrets.txt" \
    || print_info "✅ 未发现硬编码密钥"

# 检查调试配置
print_info "检查调试配置..."
grep -r "debug.*true" backend/src/main/resources/ \
    | grep -v ".git" \
    | tee "$REPORT_DIR/$TIMESTAMP/debug-configs.txt" \
    || print_info "✅ 未发现调试配置"


# ==================== 9. 依赖许可证检查 ====================
print_section "9. 依赖许可证检查"

if command -v mvn &> /dev/null; then
    print_info "检查依赖许可证..."
    cd backend

    mvn license:add-third-party \
        -Dlicense.outputDirectory="../$REPORT_DIR/$TIMESTAMP" \
        || true

    cd ..
    print_info "✅ 许可证报告: $REPORT_DIR/$TIMESTAMP/THIRD-PARTY.txt"
else
    print_error "❌ Maven未安装,跳过许可证检查"
fi


# ==================== 10. 生成总结报告 ====================
print_section "10. 生成安全扫描总结"

SUMMARY_FILE="$REPORT_DIR/$TIMESTAMP/SECURITY_SCAN_SUMMARY.md"

cat > "$SUMMARY_FILE" << EOF
# CT-Tibet-WMS 安全扫描总结

**扫描时间**: $(date)
**扫描目录**: $PROJECT_DIR
**报告目录**: $REPORT_DIR/$TIMESTAMP

---

## 扫描项目

| 扫描工具 | 状态 | 报告文件 |
|---------|------|----------|
| OWASP Dependency-Check | ✅ | dependency-check/dependency-check-report.html |
| Trivy | ✅ | trivy-*.json |
| Gitleaks | ✅ | gitleaks-report.json |
| SpotBugs | ✅ | spotbugs.html |
| Nmap | ✅ | nmap-scan.txt |
| OWASP ZAP | ⚠️ | zap-report.html (需手动运行) |
| SQLMap | ⚠️ | 需手动运行 |

---

## 关键发现

### 高危漏洞
EOF

# 统计高危漏洞数量
if [ -f "$REPORT_DIR/$TIMESTAMP/dependency-check/dependency-check-report.json" ]; then
    CRITICAL_COUNT=$(jq '[.dependencies[].vulnerabilities[] | select(.severity == "CRITICAL")] | length' \
        "$REPORT_DIR/$TIMESTAMP/dependency-check/dependency-check-report.json" 2>/dev/null || echo "0")
    HIGH_COUNT=$(jq '[.dependencies[].vulnerabilities[] | select(.severity == "HIGH")] | length' \
        "$REPORT_DIR/$TIMESTAMP/dependency-check/dependency-check-report.json" 2>/dev/null || echo "0")

    echo "- 严重 (CRITICAL): $CRITICAL_COUNT" >> "$SUMMARY_FILE"
    echo "- 高危 (HIGH): $HIGH_COUNT" >> "$SUMMARY_FILE"
fi

cat >> "$SUMMARY_FILE" << EOF

### 敏感信息泄露
EOF

if [ -f "$REPORT_DIR/$TIMESTAMP/gitleaks-report.json" ]; then
    LEAK_COUNT=$(jq '. | length' "$REPORT_DIR/$TIMESTAMP/gitleaks-report.json" 2>/dev/null || echo "0")
    echo "- 发现 $LEAK_COUNT 个潜在泄露" >> "$SUMMARY_FILE"
fi

cat >> "$SUMMARY_FILE" << EOF

---

## 建议措施

1. **立即修复高危漏洞**: 查看 dependency-check-report.html
2. **更新依赖版本**: 运行 \`mvn versions:display-dependency-updates\`
3. **移除敏感信息**: 查看 gitleaks-report.json
4. **修复代码问题**: 查看 spotbugs.html

---

## 下一步行动

- [ ] 审查所有高危和严重漏洞
- [ ] 更新有漏洞的依赖
- [ ] 移除代码中的敏感信息
- [ ] 修复SpotBugs发现的问题
- [ ] 手动执行SQLMap和ZAP扫描
- [ ] 定期重新扫描 (每周一次)

EOF

print_info "✅ 总结报告已生成: $SUMMARY_FILE"


# ==================== 完成 ====================
print_section "扫描完成"

print_info "所有报告已保存到: $REPORT_DIR/$TIMESTAMP"
print_info ""
print_info "查看总结报告:"
echo "  cat $SUMMARY_FILE"
print_info ""
print_info "查看详细报告:"
echo "  xdg-open $REPORT_DIR/$TIMESTAMP/dependency-check/dependency-check-report.html"
echo "  xdg-open $REPORT_DIR/$TIMESTAMP/spotbugs.html"

# 如果发现高危漏洞,退出码为1
if [ -f "$REPORT_DIR/$TIMESTAMP/dependency-check/dependency-check-report.json" ]; then
    CRITICAL_COUNT=$(jq '[.dependencies[].vulnerabilities[] | select(.severity == "CRITICAL")] | length' \
        "$REPORT_DIR/$TIMESTAMP/dependency-check/dependency-check-report.json" 2>/dev/null || echo "0")

    if [ "$CRITICAL_COUNT" -gt 0 ]; then
        print_error "❌ 发现 $CRITICAL_COUNT 个严重漏洞,请立即修复!"
        exit 1
    fi
fi

print_info "✅ 安全扫描完成,无严重漏洞"
exit 0
