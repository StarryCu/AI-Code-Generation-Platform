<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MenuOutlined } from '@ant-design/icons-vue'
import { navMenuItems, type NavMenuItem } from '@/config/navMenu'
import { siteTitle as defaultSiteTitle } from '@/config/site'

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
</script>

<template>
  <div class="global-header">
    <div class="global-header__brand">
      <img class="global-header__logo" src="/laoda.png" alt="" width="36" height="36" />
      <span class="global-header__title">{{ siteTitle }}</span>
    </div>

    <template v-if="!isMobile">
      <a-menu
        class="global-header__menu"
        mode="horizontal"
        :selected-keys="selectedKeys"
        :items="menuItems.map((i) => ({ key: i.key, label: i.label }))"
        @click="onMenuClick"
      />
    </template>
    <template v-else>
      <div class="global-header__right">
        <a-button type="text" @click="drawerOpen = true">
          <template #icon>
            <MenuOutlined />
          </template>
        </a-button>
        <a-drawer v-model:open="drawerOpen" placement="left" title="菜单" :width="260">
          <a-menu mode="inline" :selected-keys="selectedKeys" @click="onMenuClick">
            <a-menu-item v-for="item in menuItems" :key="item.key">
              {{ item.label }}
            </a-menu-item>
          </a-menu>
        </a-drawer>
        <a-button type="primary">登录</a-button>
      </div>
    </template>

    <div v-if="!isMobile" class="global-header__extra">
      <a-button type="primary">登录</a-button>
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

.global-header__extra {
  flex-shrink: 0;
}

.global-header__right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

@media (max-width: 767px) {
  .global-header__title {
    max-width: 46vw;
    font-size: 14px;
  }
}
</style>
