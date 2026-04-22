<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { message } from 'ant-design-vue'
import { MenuOutlined } from '@ant-design/icons-vue'
import { navMenuItems, type NavMenuItem } from '@/config/navMenu'
import { siteTitle as defaultSiteTitle } from '@/config/site'
import { useUserStore } from '@/stores/user'

const props = withDefaults(
  defineProps<{
    siteTitle?: string
    menuItems?: NavMenuItem[]
  }>(),
  {
    siteTitle: defaultSiteTitle,
    menuItems: () => navMenuItems,
  },
)

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { loginUser, isLogin } = storeToRefs(userStore)

const displayName = computed(
  () => loginUser.value?.userName || loginUser.value?.userAccount || '用户',
)

const selectedKeys = computed(() => {
  const path = route.path
  const match = props.menuItems
    .map((i) => i.key)
    .filter((k) => k !== '/')
    .sort((a, b) => b.length - a.length)
    .find((k) => path === k || path.startsWith(`${k}/`))
  if (match) return [match]
  return [path === '/' ? '/' : path]
})

const drawerOpen = ref(false)
const isMobile = ref(false)

function updateBreakpoint() {
  isMobile.value = window.matchMedia('(max-width: 767px)').matches
}

onMounted(() => {
  updateBreakpoint()
  window.addEventListener('resize', updateBreakpoint)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateBreakpoint)
})

function onMenuClick({ key }: { key: string | number }) {
  router.push(String(key))
  drawerOpen.value = false
}

function goLogin() {
  if (route.path === '/user/login' || route.path === '/user/register') {
    router.push('/user/login')
    return
  }
  router.push({ path: '/user/login', query: { redirect: route.fullPath } })
}

function goRegister() {
  router.push('/user/register')
}

async function onUserMenuClick({ key }: { key: string | number }) {
  if (key !== 'logout') return
  try {
    await userStore.logout()
    message.success('已退出登录')
    await router.push('/')
  } catch {
    message.error('退出失败，请稍后重试')
  }
}
</script>

<template>
  <div class="global-header">
    <div class="global-header__brand">
      <img class="global-header__logo" src="/laoda.png" alt="" width="36" height="36" />
      <span class="global-header__title">{{ siteTitle }}</span>
    </div>

    <a-menu
      v-if="!isMobile"
      class="global-header__menu"
      mode="horizontal"
      :selected-keys="selectedKeys"
      :items="menuItems.map((i) => ({ key: i.key, label: i.label }))"
      @click="onMenuClick"
    />

    <div class="global-header__tail">
      <a-button v-if="isMobile" type="text" aria-label="打开菜单" @click="drawerOpen = true">
        <template #icon>
          <MenuOutlined />
        </template>
      </a-button>
      <a-drawer v-if="isMobile" v-model:open="drawerOpen" placement="left" title="菜单" :width="260">
        <a-menu mode="inline" :selected-keys="selectedKeys" @click="onMenuClick">
          <a-menu-item v-for="item in menuItems" :key="item.key">
            {{ item.label }}
          </a-menu-item>
        </a-menu>
      </a-drawer>

      <template v-if="isLogin">
        <a-dropdown>
          <div class="global-header__user-trigger" role="button" tabindex="0">
            <a-avatar :size="36" :src="loginUser?.userAvatar">
              {{ displayName.slice(0, 1) }}
            </a-avatar>
            <span v-if="!isMobile" class="global-header__nickname">{{ displayName }}</span>
          </div>
          <template #overlay>
            <a-menu @click="onUserMenuClick">
              <a-menu-item key="logout">退出登录</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </template>
      <template v-else>
        <a-button type="primary" @click="goLogin">登录</a-button>
        <a-button @click="goRegister">注册</a-button>
      </template>
    </div>
  </div>
</template>

<style scoped>
.global-header {
  display: flex;
  align-items: center;
  gap: 16px;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 16px;
  height: 100%;
}

.global-header__brand {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
  min-width: 0;
}

.global-header__logo {
  display: block;
  border-radius: 8px;
  object-fit: cover;
}

.global-header__title {
  font-weight: 600;
  font-size: 16px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.global-header__menu {
  flex: 1;
  min-width: 0;
  border-bottom: none !important;
  justify-content: flex-start;
  line-height: 62px;
}

.global-header__tail {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.global-header__user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 4px 4px 0;
  border-radius: 8px;
}

.global-header__user-trigger:hover {
  background: rgba(0, 0, 0, 0.04);
}

.global-header__nickname {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
}

@media (max-width: 767px) {
  .global-header__title {
    max-width: 36vw;
    font-size: 14px;
  }
}
</style>
