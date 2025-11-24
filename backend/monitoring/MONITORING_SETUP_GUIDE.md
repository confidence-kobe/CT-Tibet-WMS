# CT-Tibet-WMS 监控系统部署指南

## 概述

本指南描述如何为CT-Tibet-WMS系统部署完整的监控解决方案,包括:
- **Prometheus**: 指标采集和存储
- **Grafana**: 数据可视化和仪表板
- **Alertmanager**: 告警管理
- **Exporters**: 各类指标导出器

## 架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                         监控架构                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌────────────┐      ┌────────────┐      ┌────────────┐       │
│  │ WMS App    │─────▶│ Prometheus │─────▶│  Grafana   │       │
│  │ (Actuator) │      │  (Metrics) │      │(Dashboard) │       │
│  └────────────┘      └────────────┘      └────────────┘       │
│         │                   │                    │             │
│         │                   ▼                    │             │
│  ┌──────▼──────┐   ┌────────────┐      ┌────────▼───┐        │
│  │MySQL/Redis  │   │Alertmanager│      │Email/Slack │        │
│  │  Exporters  │   │  (Alerts)  │      │  Webhooks  │        │
│  └─────────────┘   └────────────┘      └────────────┘        │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## 前提条件

### 1. 系统要求
- **操作系统**: Linux/Windows/macOS
- **Docker**: 20.10+ (推荐使用Docker方式部署)
- **内存**: 至少4GB可用内存
- **磁盘**: 至少20GB可用空间 (用于指标存储)

### 2. 应用配置

#### 添加Micrometer依赖 (已配置)
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

#### 启用Prometheus端点 (application.yml)
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
    prometheus:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
```

## 部署方式

### 方式1: Docker Compose (推荐)

#### 步骤1: 准备配置文件
```bash
cd backend/monitoring

# 目录结构
monitoring/
├── docker-compose-monitoring.yml
├── prometheus.yml
├── alerts/
│   └── wms-alerts.yml
├── alertmanager.yml
└── grafana/
    ├── provisioning/
    │   ├── datasources/
    │   │   └── prometheus.yml
    │   └── dashboards/
    │       └── default.yml
    └── dashboards/
        ├── wms-overview.json
        ├── wms-jvm.json
        └── wms-database.json
```

#### 步骤2: 配置MySQL Exporter
创建MySQL监控用户:
```sql
CREATE USER 'exporter'@'%' IDENTIFIED BY 'exporter_password';
GRANT PROCESS, REPLICATION CLIENT ON *.* TO 'exporter'@'%';
GRANT SELECT ON performance_schema.* TO 'exporter'@'%';
FLUSH PRIVILEGES;
```

#### 步骤3: 启动监控栈
```bash
# 启动所有服务
docker-compose -f docker-compose-monitoring.yml up -d

# 检查服务状态
docker-compose -f docker-compose-monitoring.yml ps

# 查看日志
docker-compose -f docker-compose-monitoring.yml logs -f
```

#### 步骤4: 验证部署
```bash
# 验证Prometheus
curl http://localhost:9090/-/healthy

# 验证Grafana
curl http://localhost:3000/api/health

# 验证Exporters
curl http://localhost:9100/metrics  # Node Exporter
curl http://localhost:9104/metrics  # MySQL Exporter
curl http://localhost:9121/metrics  # Redis Exporter
```

### 方式2: 手动部署

#### Prometheus
```bash
# 下载Prometheus
wget https://github.com/prometheus/prometheus/releases/download/v2.48.0/prometheus-2.48.0.linux-amd64.tar.gz
tar -xzf prometheus-2.48.0.linux-amd64.tar.gz
cd prometheus-2.48.0.linux-amd64

# 复制配置文件
cp ../monitoring/prometheus.yml .
cp -r ../monitoring/alerts .

# 启动Prometheus
./prometheus \
  --config.file=prometheus.yml \
  --storage.tsdb.path=./data \
  --storage.tsdb.retention.time=30d \
  --web.listen-address=:9090

# 创建systemd服务(可选)
sudo tee /etc/systemd/system/prometheus.service > /dev/null <<EOF
[Unit]
Description=Prometheus Monitoring System
After=network.target

[Service]
Type=simple
User=prometheus
ExecStart=/opt/prometheus/prometheus \
  --config.file=/opt/prometheus/prometheus.yml \
  --storage.tsdb.path=/var/lib/prometheus \
  --storage.tsdb.retention.time=30d
Restart=on-failure

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable prometheus
sudo systemctl start prometheus
```

#### Grafana
```bash
# 下载Grafana
wget https://dl.grafana.com/oss/release/grafana-10.2.2.linux-amd64.tar.gz
tar -xzf grafana-10.2.2.linux-amd64.tar.gz
cd grafana-10.2.2

# 启动Grafana
./bin/grafana-server web

# 或使用包管理器安装
# Debian/Ubuntu
sudo apt-get install -y adduser libfontconfig1
wget https://dl.grafana.com/oss/release/grafana_10.2.2_amd64.deb
sudo dpkg -i grafana_10.2.2_amd64.deb
sudo systemctl enable grafana-server
sudo systemctl start grafana-server
```

## 配置Grafana

### 1. 首次登录
```
URL: http://localhost:3000
默认用户名: admin
默认密码: admin123 (首次登录需修改)
```

### 2. 添加数据源
1. 左侧菜单 → Configuration → Data Sources
2. 点击 "Add data source"
3. 选择 "Prometheus"
4. 配置:
   - Name: Prometheus
   - URL: http://prometheus:9090 (Docker) 或 http://localhost:9090
   - Access: Server (default)
5. 点击 "Save & Test"

### 3. 导入仪表板

#### 方式1: 使用社区仪表板
1. 左侧菜单 → Dashboards → Import
2. 输入仪表板ID:
   - **JVM (Micrometer)**: 4701
   - **Spring Boot**: 12900
   - **MySQL Overview**: 7362
   - **Redis Dashboard**: 11835
3. 选择Prometheus数据源
4. 点击 "Import"

#### 方式2: 导入自定义仪表板
```bash
# 仪表板文件位于
backend/monitoring/grafana/dashboards/

# 在Grafana UI中:
# Dashboards → Import → Upload JSON file
```

### 4. 创建自定义仪表板

#### CT-Tibet-WMS 业务概览仪表板

**核心指标面板:**

1. **系统健康状态**
   - 查询: `up{application="ct-tibet-wms"}`
   - 类型: Stat (显示在线/离线)

2. **API请求速率**
   - 查询: `rate(http_server_requests_seconds_count{application="ct-tibet-wms"}[5m])`
   - 类型: Graph (时间序列)

3. **API响应时间 (P95)**
   - 查询: `histogram_quantile(0.95, rate(http_server_requests_seconds_bucket{application="ct-tibet-wms"}[5m]))`
   - 类型: Graph

4. **错误率**
   - 查询: `rate(http_server_requests_seconds_count{application="ct-tibet-wms",status=~"5.."}[5m]) / rate(http_server_requests_seconds_count{application="ct-tibet-wms"}[5m])`
   - 类型: Graph

5. **JVM堆内存使用**
   - 查询: `jvm_memory_used_bytes{application="ct-tibet-wms",area="heap"}`
   - 类型: Graph

6. **数据库连接池**
   - 查询: `hikaricp_connections_active{application="ct-tibet-wms"}`
   - 类型: Graph

7. **Redis缓存命中率**
   - 查询: `rate(redis_keyspace_hits_total[5m]) / (rate(redis_keyspace_hits_total[5m]) + rate(redis_keyspace_misses_total[5m]))`
   - 类型: Gauge

8. **活跃用户数** (自定义指标)
   - 查询: `wms_active_users_total`
   - 类型: Stat

9. **待审批申请数** (自定义指标)
   - 查询: `wms_apply_pending_count`
   - 类型: Stat

10. **低库存预警数** (自定义指标)
    - 查询: `wms_inventory_low_stock_alerts_total`
    - 类型: Stat

## 配置告警

### 1. 配置Alertmanager

编辑 `alertmanager.yml`:
```yaml
global:
  resolve_timeout: 5m
  smtp_smarthost: 'smtp.example.com:587'
  smtp_from: 'alertmanager@example.com'
  smtp_auth_username: 'alertmanager@example.com'
  smtp_auth_password: 'password'

route:
  group_by: ['alertname', 'cluster', 'service']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 12h
  receiver: 'team-email'
  routes:
    - match:
        severity: critical
      receiver: 'team-email-critical'
    - match:
        severity: warning
      receiver: 'team-email-warning'

receivers:
  - name: 'team-email'
    email_configs:
      - to: 'team@example.com'
        headers:
          Subject: '[WMS Alert] {{ .GroupLabels.alertname }}'

  - name: 'team-email-critical'
    email_configs:
      - to: 'team@example.com'
        headers:
          Subject: '[WMS Critical] {{ .GroupLabels.alertname }}'

  - name: 'team-email-warning'
    email_configs:
      - to: 'team@example.com'
        headers:
          Subject: '[WMS Warning] {{ .GroupLabels.alertname }}'
```

### 2. 测试告警
```bash
# 手动触发告警测试
curl -X POST http://localhost:9093/api/v1/alerts \
  -H 'Content-Type: application/json' \
  -d '[
    {
      "labels": {
        "alertname": "TestAlert",
        "severity": "warning"
      },
      "annotations": {
        "summary": "This is a test alert"
      }
    }
  ]'
```

## 自定义业务指标

### 1. 在代码中添加自定义指标

```java
@Service
@RequiredArgsConstructor
public class MetricsService {

    private final MeterRegistry meterRegistry;

    // 计数器: 低库存预警数量
    public void recordLowStockAlert() {
        meterRegistry.counter("wms.inventory.low_stock.alerts").increment();
    }

    // 计量器: 待审批申请数量
    public void updatePendingApplies(int count) {
        meterRegistry.gauge("wms.apply.pending.count", count);
    }

    // 计时器: 自定义操作耗时
    public void recordOperationTime(String operation, long duration) {
        meterRegistry.timer("wms.operation.duration",
            Tags.of("operation", operation))
            .record(duration, TimeUnit.MILLISECONDS);
    }

    // 分布汇总: 库存数量分布
    public void recordInventoryQuantity(BigDecimal quantity) {
        meterRegistry.summary("wms.inventory.quantity")
            .record(quantity.doubleValue());
    }
}
```

### 2. 在Prometheus中查询自定义指标

```promql
# 低库存预警总数
wms_inventory_low_stock_alerts_total

# 低库存预警增长率
rate(wms_inventory_low_stock_alerts_total[5m])

# 待审批申请数量
wms_apply_pending_count

# 操作平均耗时
rate(wms_operation_duration_sum[5m]) / rate(wms_operation_duration_count[5m])
```

## 常见问题排查

### 1. Prometheus无法抓取指标
```bash
# 检查应用Actuator端点
curl http://localhost:48888/actuator/prometheus

# 检查Prometheus配置
docker exec wms-prometheus promtool check config /etc/prometheus/prometheus.yml

# 查看Prometheus日志
docker logs wms-prometheus
```

### 2. Grafana无法连接Prometheus
```bash
# 在Grafana容器中测试连接
docker exec wms-grafana curl http://prometheus:9090/-/healthy

# 检查网络连接
docker network inspect monitoring
```

### 3. 告警未触发
```bash
# 检查告警规则
curl http://localhost:9090/api/v1/rules

# 检查告警状态
curl http://localhost:9090/api/v1/alerts

# 查看Alertmanager日志
docker logs wms-alertmanager
```

## 性能调优

### Prometheus存储优化
```yaml
# prometheus.yml
global:
  scrape_interval: 15s  # 降低抓取频率可减少存储
  evaluation_interval: 15s

storage:
  tsdb:
    retention.time: 30d  # 根据需要调整保留时间
    retention.size: 10GB # 限制存储大小
```

### Grafana查询优化
- 使用查询缓存
- 限制查询时间范围
- 使用合适的分辨率
- 避免过于复杂的计算

## 监控最佳实践

### 1. 黄金指标 (Golden Signals)
- **延迟**: API响应时间
- **流量**: 请求速率 (TPS)
- **错误**: 错误率
- **饱和度**: CPU/内存/连接池使用率

### 2. RED方法 (适用于服务监控)
- **Rate**: 请求速率
- **Errors**: 错误率
- **Duration**: 响应时间

### 3. USE方法 (适用于资源监控)
- **Utilization**: 资源使用率
- **Saturation**: 资源饱和度
- **Errors**: 错误数量

### 4. 告警规则设计原则
- **可操作性**: 告警必须是可操作的
- **相关性**: 告警应该关注业务影响
- **上下文**: 提供足够的上下文信息
- **阈值合理**: 基于历史数据设置合理阈值

## 维护和备份

### 定期备份Grafana配置
```bash
# 导出所有仪表板
curl -H "Authorization: Bearer <API_KEY>" \
  http://localhost:3000/api/search?type=dash-db \
  | jq -r '.[].uri' \
  | while read uri; do
      curl -H "Authorization: Bearer <API_KEY>" \
        "http://localhost:3000/api/dashboards/${uri}" \
        > "backup_${uri}.json"
    done
```

### 定期清理Prometheus数据
```bash
# 手动清理旧数据
docker exec wms-prometheus promtool tsdb prune \
  --retention.time=30d /prometheus
```

## 扩展监控

### 集成更多监控组件
- **Loki**: 日志聚合
- **Jaeger**: 分布式追踪
- **Blackbox Exporter**: 黑盒监控
- **Pushgateway**: 短生命周期任务监控

### 集成第三方告警渠道
- 企业微信
- 钉钉
- Slack
- PagerDuty

## 参考资源

- [Prometheus文档](https://prometheus.io/docs/)
- [Grafana文档](https://grafana.com/docs/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer文档](https://micrometer.io/docs/)
