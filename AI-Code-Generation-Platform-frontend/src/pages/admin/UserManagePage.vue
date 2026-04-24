<script setup lang="ts">
// 用户管理页占位：后续可接入用户分页等能力
</script>

<template>
  <div class="placeholder">
    <div class="placeholder__card ds-surface">
      <h2 class="placeholder__title">用户管理</h2>
      <a-empty description="功能开发中" />
    </div>
  </div>
</template>

<style scoped>
.ds-surface {
  background: var(--ds-surface);
  border-radius: var(--ds-radius-xl);
  box-shadow: var(--ds-shadow);
  border: 1px solid var(--ds-border);
  padding: 28px 24px 36px;
}

.placeholder {
  max-width: 1400px;
  margin: 0 auto;
}

.placeholder__title {
  margin: 0 0 20px;
  font-size: 20px;
  font-weight: 800;
  color: var(--ds-ink);
}
</style>
