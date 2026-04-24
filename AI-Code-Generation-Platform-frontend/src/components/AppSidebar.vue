<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { message } from 'ant-design-vue'
import {
  AppstoreOutlined,
  HomeOutlined,
  InfoCircleOutlined,
  MenuOutlined,
} from '@ant-design/icons-vue'
import { getNavMenuItems, type NavMenuItem } from '@/config/navMenu'
import { siteTitle } from '@/config/site'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { loginUser, isLogin } = storeToRefs(userStore)

const drawerOpen = ref(false)
const isMobile = ref(false)

const navWithIcons = computed(() => {
  const admin = loginUser.value?.userRole === 'admin'
  const items = getNavMenuItems(!!admin)
  const iconMap: Record<string, typeof HomeOutlined> = {
    '/': HomeOutlined,
    '/about': InfoCircleOutlined,
    '/admin/appManage': AppstoreOutlined,
  }
  return items.map((i: NavMenuItem) => ({
    ...i,
    icon: iconMap[i.key] ?? HomeOutlined,
  }))
})

const activeKey = computed(() => {
  const path = route.path
  if (path === '/') return '/'
  const keys = navWithIcons.value
    .map((i) => i.key)
    .filter((k) => k !== '/')
    .sort((a, b) => b.length - a.length)
  const hit = keys.find((k) => path === k || path.startsWith(`${k}/`))
  return hit ?? ''
})

const displayName = computed(
  () => loginUser.value?.userName || loginUser.value?.userAccount || '访客',
)

const emailHint = computed(() => loginUser.value?.userAccount ?? '')

function updateBp() {
  isMobile.value = window.matchMedia('(max-width: 991px)').matches
}

onMounted(() => {
  updateBp()
  window.addEventListener('resize', updateBp)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateBp)
})

function go(key: string) {
  void router.push(key)
  drawerOpen.value = false
}

function goLogin() {
  void router.push({ path: '/user/login', query: { redirect: route.fullPath } })
}

function goRegister() {
  void router.push('/user/register')
}

async function logout() {
  try {
    await userStore.logout()
    message.success('已退出登录')
    await router.push('/')
  } catch {
    message.error('退出失败')
  }
}
</script>

<template>
  <!-- 移动端顶条 + 抽屉（单根节点便于外层 flex 布局） -->
  <div v-if="isMobile" class="sidebar-mob">
    <div class="mob-bar">
      <button type="button" class="mob-bar__btn" aria-label="菜单" @click="drawerOpen = true">
        <MenuOutlined />
      </button>
      <span class="mob-bar__title">{{ siteTitle }}</span>
    </div>
    <a-drawer v-model:open="drawerOpen" placement="left" :width="280" title="导航" root-class-name="ds-drawer">
      <nav class="side-nav">
        <button
          v-for="item in navWithIcons"
          :key="item.key"
          type="button"
          class="side-nav__item"
          :class="{ 'side-nav__item--active': activeKey === item.key }"
          @click="go(item.key)"
        >
          <span class="side-nav__ico"><component :is="item.icon" /></span>
          {{ item.label }}
        </button>
      </nav>
      <div class="side-foot">
        <template v-if="isLogin">
          <div class="side-user">
            <a-avatar :size="40" :src="loginUser?.userAvatar">{{ displayName.slice(0, 1) }}</a-avatar>
            <div class="side-user__meta">
              <div class="side-user__name">{{ displayName }}</div>
              <div class="side-user__mail">{{ emailHint }}</div>
            </div>
          </div>
          <a-button block class="side-logout" @click="logout">退出登录</a-button>
        </template>
        <template v-else>
          <a-button type="primary" block class="side-login" @click="goLogin">登录</a-button>
          <a-button block class="side-reg" @click="goRegister">注册</a-button>
        </template>
      </div>
    </a-drawer>
  </div>

  <aside v-else class="sidebar">
    <div class="sidebar__brand" @click="go('/')">
      <img src="/laoda.png" alt="" class="sidebar__logo" width="40" height="40" />
      <div class="sidebar__titles">
        <div class="sidebar__name">{{ siteTitle }}</div>
        <div class="sidebar__tag">AI 建站</div>
      </div>
    </div>

    <nav class="sidebar__nav">
      <button
        v-for="item in navWithIcons"
        :key="item.key"
        type="button"
        class="nav-btn"
        :class="{ 'nav-btn--active': activeKey === item.key }"
        @click="go(item.key)"
      >
        <span class="nav-btn__ico"><component :is="item.icon" /></span>
        <span class="nav-btn__txt">{{ item.label }}</span>
      </button>
    </nav>

    <div class="sidebar__spacer" />

    <div class="sidebar__foot">
      <template v-if="isLogin">
        <div class="sidebar-user">
          <a-avatar :size="44" :src="loginUser?.userAvatar">{{ displayName.slice(0, 1) }}</a-avatar>
          <div class="sidebar-user__text">
            <div class="sidebar-user__name">{{ displayName }}</div>
            <div class="sidebar-user__sub">{{ emailHint }}</div>
          </div>
        </div>
        <a-button block class="btn-outline" @click="logout">退出登录</a-button>
      </template>
      <template v-else>
        <a-button type="primary" block class="btn-accent" @click="goLogin">登录</a-button>
        <a-button block class="btn-outline btn-mt" @click="goRegister">注册</a-button>
      </template>
    </div>
  </aside>
</template>

<style scoped>
.sidebar {
  width: 260px;
  flex-shrink: 0;
  min-height: 100vh;
  background: var(--ds-sidebar-bg);
  border-right: 1px solid var(--ds-border);
  display: flex;
  flex-direction: column;
  padding: 22px 16px 20px;
  position: sticky;
  top: 0;
  align-self: flex-start;
  z-index: 50;
}

.sidebar__brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 8px 20px;
  cursor: pointer;
  border-radius: var(--ds-radius);
}

.sidebar__brand:hover {
  background: rgba(0, 0, 0, 0.03);
}

.sidebar__logo {
  border-radius: 12px;
  object-fit: cover;
}

.sidebar__titles {
  min-width: 0;
}

.sidebar__name {
  font-weight: 700;
  font-size: 14px;
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.sidebar__tag {
  font-size: 11px;
  color: var(--ds-text-muted);
  margin-top: 2px;
  letter-spacing: 0.04em;
}

.sidebar__nav {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.nav-btn {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  border: none;
  background: transparent;
  cursor: pointer;
  text-align: left;
  padding: 12px 14px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  color: var(--ds-text);
  transition: background 0.15s ease, color 0.15s ease;
}

.nav-btn:hover {
  background: rgba(0, 0, 0, 0.04);
}

.nav-btn--active {
  background: var(--ds-ink);
  color: #fff;
}

.nav-btn--active:hover {
  background: #000;
  color: #fff;
}

.nav-btn__ico {
  display: flex;
  width: 22px;
  justify-content: center;
  font-size: 16px;
}

.sidebar__spacer {
  flex: 1;
  min-height: 16px;
}

.sidebar__foot {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-top: 12px;
  border-top: 1px solid var(--ds-border);
}

.sidebar-user {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 4px;
}

.sidebar-user__text {
  min-width: 0;
}

.sidebar-user__name {
  font-weight: 600;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sidebar-user__sub {
  font-size: 12px;
  color: var(--ds-text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.btn-accent {
  height: 42px !important;
  border-radius: 12px !important;
  font-weight: 600 !important;
  background: var(--ds-accent) !important;
  border: none !important;
}

.btn-outline {
  height: 40px !important;
  border-radius: 12px !important;
  font-weight: 500 !important;
  border-color: var(--ds-border) !important;
  color: var(--ds-ink) !important;
  background: #fff !important;
}

.btn-mt {
  margin-top: 0;
}

.sidebar-mob {
  display: none;
}

.mob-bar {
  display: none;
}

@media (max-width: 991px) {
  .sidebar-mob {
    display: block;
  }

  .mob-bar {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 16px;
    background: var(--ds-sidebar-bg);
    border-bottom: 1px solid var(--ds-border);
    position: sticky;
    top: 0;
    z-index: 40;
  }

  .mob-bar__btn {
    border: none;
    background: rgba(0, 0, 0, 0.05);
    width: 40px;
    height: 40px;
    border-radius: 12px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
  }

  .mob-bar__title {
    font-weight: 700;
    font-size: 15px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .sidebar {
    display: none;
  }
}

.side-nav {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.side-nav__item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  border: none;
  background: rgba(0, 0, 0, 0.04);
  padding: 12px 14px;
  border-radius: 12px;
  cursor: pointer;
  font-size: 14px;
  text-align: left;
}

.side-nav__item--active {
  background: var(--ds-ink);
  color: #fff;
}

.side-nav__ico {
  width: 22px;
  display: flex;
  justify-content: center;
}

.side-foot {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid var(--ds-border);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.side-user {
  display: flex;
  gap: 10px;
  align-items: center;
}

.side-user__meta {
  min-width: 0;
}

.side-user__name {
  font-weight: 600;
  font-size: 14px;
}

.side-user__mail {
  font-size: 12px;
  color: var(--ds-text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
}

.side-logout,
.side-login,
.side-reg {
  border-radius: 12px !important;
  height: 40px !important;
}
</style>
