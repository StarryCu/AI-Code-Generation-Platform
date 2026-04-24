import { createRouter, createWebHistory } from 'vue-router'
import { message } from 'ant-design-vue'
import HomePage from '@/pages/HomePage.vue'
import AboutPage from '@/pages/AboutPage.vue'
import UserManagePage from '@/pages/admin/UserManagePage.vue'
import AppManagePage from '@/pages/admin/AppManagePage.vue'
import UserRegisterPage from '@/pages/user/UserRegisterPage.vue'
import UserLoginPage from '@/pages/user/UserLoginPage.vue'
import AppGenChatPage from '@/pages/app/AppGenChatPage.vue'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '主页',
      component: HomePage,
      meta: { hideTopbar: true },
    },
    {
      path: '/about',
      name: '关于',
      component: AboutPage,
      meta: { pageTitle: '关于' },
    },
    {
      path: '/user/login',
      name: '用户登录',
      component: UserLoginPage,
      meta: { pageTitle: '登录' },
    },
    {
      path: '/user/register',
      name: '用户注册',
      component: UserRegisterPage,
      meta: { pageTitle: '注册' },
    },
    {
      path: '/app/gen/:appId',
      name: '应用生成对话',
      component: AppGenChatPage,
      meta: { requiresAuth: true, fullWidth: true, hideTopbar: true },
    },
    {
      path: '/admin/userManage',
      name: '用户管理',
      component: UserManagePage,
      meta: { pageTitle: '用户管理' },
    },
    {
      path: '/admin/appManage',
      name: '应用管理',
      component: AppManagePage,
      meta: { requiresAdmin: true, pageTitle: '应用管理' },
    },
  ],
})

router.beforeEach(async (to) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth || to.meta.requiresAdmin) {
    if (!userStore.loginUser?.id) {
      await userStore.fetchLoginUser()
    }
  }
  if (to.meta.requiresAuth && !userStore.isLogin) {
    message.warning('请先登录')
    return { path: '/user/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.requiresAdmin) {
    if (userStore.loginUser?.userRole !== 'admin') {
      message.warning('需要管理员权限')
      return { path: '/' }
    }
  }
  return true
})

export default router
