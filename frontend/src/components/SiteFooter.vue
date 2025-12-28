<template>
  <footer class="site-footer">
    <div class="container inner">
      <div class="left">
        <div class="title">高校图书馆 · 中文门户</div>
        <div class="meta">用于课程设计学习 · UI结构参考高校官网风格</div>
      </div>
      <div class="right">
        <div class="meta">
          <RouterLink class="admin-link" to="/admin/login">后台管理</RouterLink>
          <span class="dot">·</span>
          <span>© {{ new Date().getFullYear() }} Library Portal</span>
        </div>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.site-footer {
  border-top: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.72);
}

.inner {
  padding: 18px 0;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
}

.title {
  font-family: var(--font-serif);
  font-weight: 700;
  letter-spacing: 0.4px;
  color: var(--ink);
}

.meta {
  margin-top: 4px;
  font-size: 12px;
  color: var(--muted-2);
  letter-spacing: 0.2px;
}

.admin-link {
  color: rgba(11, 43, 91, 0.86);
  border-bottom: 1px dashed rgba(11, 43, 91, 0.28);
  padding-bottom: 1px;
}

.admin-link:hover {
  border-bottom-color: rgba(11, 43, 91, 0.5);
}

.dot {
  margin: 0 8px;
  opacity: 0.6;
}
</style>
