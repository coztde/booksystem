<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="section">
    <div class="container nf">
      <div class="h1">页面不存在</div>
      <div class="muted">你访问的地址不存在或已被移动。</div>
      <div class="actions">
        <button class="btn btn-primary" type="button" @click="router.push('/')">返回首页</button>
        <button class="btn" type="button" @click="router.push('/resources')">馆藏资源</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.nf {
  padding: 48px 0;
  text-align: center;
}

.actions {
  margin-top: 16px;
  display: flex;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
}
</style>

