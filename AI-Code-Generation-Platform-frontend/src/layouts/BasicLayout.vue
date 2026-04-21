<script setup lang="ts">
import { RouterView } from 'vue-router'
import GlobalHeader from '@/components/GlobalHeader.vue'
import { siteTitle } from '@/config/site'

const year = new Date().getFullYear()
</script>

<template>
  <a-layout class="basic-layout">
    <a-layout-header class="basic-layout__header">
      <GlobalHeader />
    </a-layout-header>

    <a-layout-content class="basic-layout__content">
      <div class="basic-layout__inner">
        <RouterView />
      </div>
    </a-layout-content>

    <a-layout-footer class="basic-layout__footer">
      © {{ year }} {{ siteTitle }}
    </a-layout-footer>
  </a-layout>
</template>

<style scoped>
.basic-layout {
  min-height: 100vh;
}

.basic-layout__header {
  position: sticky;
  top: 0;
  z-index: 100;
  height: 64px;
  padding: 0;
  line-height: 64px;
  background: #fff;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.basic-layout__content {
  flex: 1;
  padding: 16px;
}

.basic-layout__inner {
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}

.basic-layout__footer {
  text-align: center;
  padding: 16px 24px 24px;
  color: rgba(0, 0, 0, 0.45);
  background: #fafafa;
}

@media (min-width: 768px) {
  .basic-layout__content {
    padding: 24px;
  }
}
</style>

<style>
html,
body,
#app {
  height: 100%;
  margin: 0;
}
</style>
