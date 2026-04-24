<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import AppSidebar from '@/components/AppSidebar.vue'
import { siteTitle } from '@/config/site'
import { useUserStore } from '@/stores/user'

const year = new Date().getFullYear()
const userStore = useUserStore()
const { loginUser, isLogin } = storeToRefs(userStore)
const route = useRoute()

onMounted(() => {
  userStore.fetchLoginUser()
})

const showTopbar = computed(() => route.meta.hideTopbar !== true)

const topbarTitle = computed(() => {
  const t = route.meta.pageTitle as string | undefined
  if (t) return t
  if (isLogin.value && loginUser.value) {
    const n = loginUser.value.userName || loginUser.value.userAccount || '用户'
    return `你好，${n}`
  }
  return '欢迎使用'
})

const topbarSub = computed(() => {
  if (route.meta.pageTitle) return siteTitle
  return '用对话生成网站，实时预览与部署'
})
</script>

<template>
  <div class="shell">
    <AppSidebar />

    <div class="shell__main">
      <header v-if="showTopbar" class="shell__top">
        <div>
          <h1 class="shell__title">{{ topbarTitle }}</h1>
          <p class="shell__sub">{{ topbarSub }}</p>
        </div>
      </header>

      <main class="shell__body" :class="{ 'shell__body--wide': route.meta.fullWidth === true }">
        <RouterView />
      </main>

      <footer class="shell__foot">© {{ year }} {{ siteTitle }}</footer>
    </div>
  </div>
</template>

<style scoped>
.shell {
  display: flex;
  min-height: 100vh;
  background: var(--ds-page-bg);
}

.shell__main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.shell__top {
  padding: 22px 28px 8px;
  flex-shrink: 0;
}

.shell__title {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--ds-ink);
}

.shell__sub {
  margin: 6px 0 0;
  font-size: 14px;
  color: var(--ds-text-muted);
}

.shell__body {
  flex: 1;
  padding: 8px 28px 32px;
}

.shell__body--wide {
  padding: 12px 20px 20px;
}

.shell__foot {
  flex-shrink: 0;
  text-align: center;
  padding: 14px 16px 20px;
  font-size: 12px;
  color: var(--ds-text-muted);
}

@media (max-width: 991px) {
  .shell {
    flex-direction: column;
  }

  .shell__top {
    padding: 16px 16px 4px;
  }

  .shell__title {
    font-size: 22px;
  }

  .shell__body {
    padding: 8px 16px 24px;
  }

  .shell__body--wide {
    padding: 8px 12px 16px;
  }
}
</style>
