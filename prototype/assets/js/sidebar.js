// 侧边栏折叠功能
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    if (!sidebar) return;

    sidebar.classList.toggle('collapsed');

    // 保存状态到 localStorage
    const isCollapsed = sidebar.classList.contains('collapsed');
    localStorage.setItem('sidebarCollapsed', isCollapsed);
}

// 恢复侧边栏状态
function restoreSidebarState() {
    const sidebar = document.getElementById('sidebar');
    if (!sidebar) return;

    const isCollapsed = localStorage.getItem('sidebarCollapsed') === 'true';
    if (isCollapsed) {
        sidebar.classList.add('collapsed');
    }
}

// 页面加载时恢复侧边栏状态
document.addEventListener('DOMContentLoaded', restoreSidebarState);
