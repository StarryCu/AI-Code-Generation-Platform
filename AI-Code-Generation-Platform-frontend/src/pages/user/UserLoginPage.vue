<script setup lang="ts">
import { reactive, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'
import { userLogin } from '@/api/userController'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const submitting = ref(false)

const formState = reactive({
  userAccount: '',
  userPassword: '',
})

const rules: Record<string, Rule[]> = {
  userAccount: [
    { required: true, message: '请输入账号' },
    { min: 4, message: '账号至少 4 位' },
  ],
  userPassword: [
    { required: true, message: '请输入密码' },
    { min: 8, message: '密码至少 8 位' },
  ],
}

function getSafeRedirect(raw: unknown): string | null {
  if (typeof raw !== 'string' || !raw) return null
  try {
    const u = new URL(raw, window.location.origin)
    if (u.origin !== window.location.origin) return null
    return `${u.pathname}${u.search}${u.hash}`
  } catch {
    return null
  }
}

async function onSubmit() {
  submitting.value = true
  try {
    const res = await userLogin({
      userAccount: formState.userAccount.trim(),
      userPassword: formState.userPassword,
    })
    const { code, data, message: msg } = res.data
    if (code === 0 && data) {
      userStore.setLoginUser(data)
      message.success('登录成功')
      const target = getSafeRedirect(route.query.redirect)
      if (target) {
        await router.replace(target)
      } else {
        await router.replace('/')
      }
    } else {
      message.error(msg || '登录失败')
    }
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="page">
    <div class="card ds-surface">
      <h2 class="card__title">用户登录</h2>
      <a-form :model="formState" :rules="rules" layout="vertical" @finish="onSubmit">
        <a-form-item label="账号" name="userAccount">
          <a-input v-model:value="formState.userAccount" autocomplete="username" placeholder="账号" />
        </a-form-item>
        <a-form-item label="密码" name="userPassword">
          <a-input-password
            v-model:value="formState.userPassword"
            autocomplete="current-password"
            placeholder="密码"
          />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" block :loading="submitting">登录</a-button>
        </a-form-item>
      </a-form>
      <div class="extra">
        还没有账号？
        <RouterLink to="/user/register">去注册</RouterLink>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ds-surface {
  background: var(--ds-surface);
  border-radius: var(--ds-radius-xl);
  box-shadow: var(--ds-shadow);
  border: 1px solid var(--ds-border);
}

.page {
  display: flex;
  justify-content: center;
  padding: 32px 16px 48px;
}

.card {
  width: 100%;
  max-width: 420px;
  padding: 28px 28px 24px;
}

.card__title {
  margin: 0 0 22px;
  font-size: 22px;
  font-weight: 800;
  color: var(--ds-ink);
  letter-spacing: -0.02em;
}

.card :deep(.ant-input-affix-wrapper),
.card :deep(.ant-input) {
  border-radius: 12px;
}

.card :deep(.ant-btn-primary) {
  height: 46px !important;
  border-radius: 14px !important;
  font-weight: 700 !important;
}

.extra {
  text-align: center;
  color: var(--ds-text-muted);
  font-size: 14px;
  margin-top: 8px;
}

.extra a {
  margin-left: 4px;
  color: var(--ds-accent);
  font-weight: 600;
}
</style>
