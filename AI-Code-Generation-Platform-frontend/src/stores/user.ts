import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getLoginUser, userLogout } from '@/api/userController'

export const useUserStore = defineStore('user', () => {
  const loginUser = ref<API.LoginUserVO | null>(null)

  const isLogin = computed(() => !!loginUser.value?.id)

  function setLoginUser(user: API.LoginUserVO | null) {
    loginUser.value = user
  }

  /** 根据 Cookie 会话拉取当前登录用户；未登录时静默清空 */
  async function fetchLoginUser() {
    try {
      const res = await getLoginUser()
      if (res.data.code === 0) {
        loginUser.value = res.data.data ?? null
      } else {
        loginUser.value = null
      }
    } catch {
      loginUser.value = null
    }
  }

  async function logout() {
    try {
      await userLogout()
    } finally {
      loginUser.value = null
    }
  }

  return {
    loginUser,
    isLogin,
    setLoginUser,
    fetchLoginUser,
    logout,
  }
})
