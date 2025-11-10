// 公共JS函数

// 消息提示
const Message = {
    success(text, duration = 3000) {
        this.show(text, 'success', duration);
    },
    error(text, duration = 3000) {
        this.show(text, 'error', duration);
    },
    warning(text, duration = 3000) {
        this.show(text, 'warning', duration);
    },
    info(text, duration = 3000) {
        this.show(text, 'info', duration);
    },
    show(text, type = 'info', duration = 3000) {
        const msg = document.createElement('div');
        msg.className = `message message-${type}`;
        msg.innerHTML = `
            <span style="font-size: 16px">${this.getIcon(type)}</span>
            <span>${text}</span>
        `;
        document.body.appendChild(msg);

        setTimeout(() => {
            msg.style.opacity = '0';
            setTimeout(() => msg.remove(), 300);
        }, duration);
    },
    getIcon(type) {
        const icons = {
            success: '✓',
            error: '✗',
            warning: '⚠',
            info: 'ℹ'
        };
        return icons[type] || icons.info;
    }
};

// 移动端Toast
const Toast = {
    show(text, duration = 2000) {
        const toast = document.createElement('div');
        toast.className = 'toast';
        toast.textContent = text;
        document.body.appendChild(toast);

        setTimeout(() => {
            toast.style.opacity = '0';
            setTimeout(() => toast.remove(), 300);
        }, duration);
    }
};

// 确认对话框
function confirm(title, content, onConfirm, onCancel) {
    const overlay = document.createElement('div');
    overlay.className = 'modal-overlay';
    overlay.innerHTML = `
        <div class="modal" style="max-width: 400px;">
            <div class="modal-header">
                <div class="modal-title">${title}</div>
            </div>
            <div class="modal-body">
                <p>${content}</p>
            </div>
            <div class="modal-footer">
                <button class="btn btn-default" onclick="this.closest('.modal-overlay').remove(); ${onCancel ? 'onCancel()' : ''}">取消</button>
                <button class="btn btn-primary confirm-btn">确定</button>
            </div>
        </div>
    `;
    document.body.appendChild(overlay);

    overlay.querySelector('.confirm-btn').onclick = function() {
        overlay.remove();
        if (onConfirm) onConfirm();
    };

    overlay.onclick = function(e) {
        if (e.target === overlay) {
            overlay.remove();
            if (onCancel) onCancel();
        }
    };
}

// 显示详情弹窗
function showDetailModal(title, content) {
    const overlay = document.createElement('div');
    overlay.className = 'modal-overlay';
    overlay.innerHTML = `
        <div class="modal">
            <div class="modal-header">
                <div class="modal-title">${title}</div>
                <span class="modal-close">×</span>
            </div>
            <div class="modal-body">
                ${content}
            </div>
            <div class="modal-footer">
                <button class="btn btn-default" onclick="this.closest('.modal-overlay').remove()">关闭</button>
            </div>
        </div>
    `;
    document.body.appendChild(overlay);

    overlay.querySelector('.modal-close').onclick = function() {
        overlay.remove();
    };

    overlay.onclick = function(e) {
        if (e.target === overlay) {
            overlay.remove();
        }
    };
}

// 格式化日期时间
function formatDateTime(dateStr) {
    if (!dateStr) return '-';
    return dateStr.substring(0, 16).replace('T', ' ');
}

// 格式化日期
function formatDate(dateStr) {
    if (!dateStr) return '-';
    return dateStr.substring(0, 10);
}

// 格式化金额
function formatMoney(amount) {
    if (amount === null || amount === undefined) return '-';
    return '¥' + amount.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
}

// 获取状态标签HTML
function getStatusTag(status, statusText, type = 'apply') {
    const statusClass = mockUtils.getStatusClass(status, type);
    return `<span class="tag tag-${statusClass}">${statusText}</span>`;
}

// 分页
class Pagination {
    constructor(container, total, pageSize = 10, onChange) {
        this.container = container;
        this.total = total;
        this.pageSize = pageSize;
        this.currentPage = 1;
        this.onChange = onChange;
        this.render();
    }

    get totalPages() {
        return Math.ceil(this.total / this.pageSize);
    }

    goToPage(page) {
        if (page < 1 || page > this.totalPages) return;
        this.currentPage = page;
        this.render();
        if (this.onChange) {
            this.onChange(page, this.pageSize);
        }
    }

    render() {
        const html = `
            <div class="pagination">
                <button class="pagination-btn ${this.currentPage === 1 ? 'disabled' : ''}"
                        onclick="pagination.goToPage(${this.currentPage - 1})"
                        ${this.currentPage === 1 ? 'disabled' : ''}>
                    上一页
                </button>
                ${this.getPageButtons()}
                <button class="pagination-btn ${this.currentPage === this.totalPages ? 'disabled' : ''}"
                        onclick="pagination.goToPage(${this.currentPage + 1})"
                        ${this.currentPage === this.totalPages ? 'disabled' : ''}>
                    下一页
                </button>
                <span style="margin-left: 16px; color: #8c8c8c;">
                    共 ${this.total} 条，第 ${this.currentPage}/${this.totalPages} 页
                </span>
            </div>
        `;
        this.container.innerHTML = html;
    }

    getPageButtons() {
        let buttons = '';
        const maxButtons = 5;
        let startPage = Math.max(1, this.currentPage - Math.floor(maxButtons / 2));
        let endPage = Math.min(this.totalPages, startPage + maxButtons - 1);

        if (endPage - startPage < maxButtons - 1) {
            startPage = Math.max(1, endPage - maxButtons + 1);
        }

        for (let i = startPage; i <= endPage; i++) {
            buttons += `
                <button class="pagination-btn ${i === this.currentPage ? 'active' : ''}"
                        onclick="pagination.goToPage(${i})">
                    ${i}
                </button>
            `;
        }
        return buttons;
    }
}

// 表格排序
function sortTable(table, column, order = 'asc') {
    const tbody = table.querySelector('tbody');
    const rows = Array.from(tbody.querySelectorAll('tr'));

    rows.sort((a, b) => {
        const aVal = a.children[column].textContent.trim();
        const bVal = b.children[column].textContent.trim();

        if (order === 'asc') {
            return aVal.localeCompare(bVal, 'zh-CN');
        } else {
            return bVal.localeCompare(aVal, 'zh-CN');
        }
    });

    rows.forEach(row => tbody.appendChild(row));
}

// 表格筛选
function filterTable(table, keyword, columns = []) {
    const tbody = table.querySelector('tbody');
    const rows = tbody.querySelectorAll('tr');

    keyword = keyword.toLowerCase();

    rows.forEach(row => {
        let found = false;

        if (columns.length === 0) {
            // 搜索所有列
            found = row.textContent.toLowerCase().includes(keyword);
        } else {
            // 搜索指定列
            for (let col of columns) {
                if (row.children[col].textContent.toLowerCase().includes(keyword)) {
                    found = true;
                    break;
                }
            }
        }

        row.style.display = found ? '' : 'none';
    });
}

// 导出Excel（模拟）
function exportToExcel(data, filename) {
    // 实际项目中使用 xlsx.js 或后端导出
    Message.info('Excel文件导出功能需要后端支持');
    console.log('导出数据：', data, '文件名：', filename);
}

// 加载中动画
const Loading = {
    show(text = '加载中...') {
        const loading = document.createElement('div');
        loading.id = 'global-loading';
        loading.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(0,0,0,0.5);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 9999;
        `;
        loading.innerHTML = `
            <div style="background: white; padding: 20px 40px; border-radius: 8px; text-align: center;">
                <div style="font-size: 24px; margin-bottom: 12px;">⏳</div>
                <div>${text}</div>
            </div>
        `;
        document.body.appendChild(loading);
    },
    hide() {
        const loading = document.getElementById('global-loading');
        if (loading) loading.remove();
    }
};

// URL参数处理
const URLParams = {
    get(name) {
        const params = new URLSearchParams(window.location.search);
        return params.get(name);
    },
    set(name, value) {
        const url = new URL(window.location);
        url.searchParams.set(name, value);
        window.history.pushState({}, '', url);
    },
    getAll() {
        const params = new URLSearchParams(window.location.search);
        const obj = {};
        for (let [key, value] of params) {
            obj[key] = value;
        }
        return obj;
    }
};

// 本地存储
const Storage = {
    set(key, value) {
        localStorage.setItem(key, JSON.stringify(value));
    },
    get(key, defaultValue = null) {
        const value = localStorage.getItem(key);
        if (value === null) return defaultValue;
        try {
            return JSON.parse(value);
        } catch {
            return value;
        }
    },
    remove(key) {
        localStorage.removeItem(key);
    },
    clear() {
        localStorage.clear();
    }
};

// 用户会话
const Session = {
    getCurrentUser() {
        return Storage.get('currentUser', {
            id: 2,
            username: 'wl_warehouse',
            name: '张三',
            role: 'WAREHOUSE',
            roleName: '仓库管理员',
            deptId: 1,
            deptName: '网络运维部'
        });
    },
    setCurrentUser(user) {
        Storage.set('currentUser', user);
    },
    logout() {
        Storage.remove('currentUser');
        window.location.href = 'login.html';
    }
};

// 防抖
function debounce(fn, delay = 300) {
    let timer = null;
    return function(...args) {
        if (timer) clearTimeout(timer);
        timer = setTimeout(() => {
            fn.apply(this, args);
        }, delay);
    };
}

// 节流
function throttle(fn, delay = 300) {
    let lastTime = 0;
    return function(...args) {
        const now = Date.now();
        if (now - lastTime >= delay) {
            fn.apply(this, args);
            lastTime = now;
        }
    };
}

// 复制到剪贴板
async function copyToClipboard(text) {
    try {
        await navigator.clipboard.writeText(text);
        Message.success('复制成功');
    } catch {
        Message.error('复制失败');
    }
}

// 生成唯一ID
function generateId() {
    return Date.now().toString(36) + Math.random().toString(36).substring(2);
}

// 返回上一页
function goBack() {
    window.history.back();
}

// 跳转页面
function navigateTo(url) {
    window.location.href = url;
}
