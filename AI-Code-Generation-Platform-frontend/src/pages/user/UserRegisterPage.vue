<script setup lang="ts">
import { reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'
import { userRegister } from '@/api/userController'

const router = useRouter()
const submitting = ref(false)

const formState = reactive({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
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
  checkPassword: [
    { required: true, message: '请再次输入密码' },
    {
      validator: async (_rule, value: string) => {
        if (value && value !== formState.userPassword) {
          return Promise.reject('两次输入的密码不一致')
        }
        return Promise.resolve()
      },
      trigger: 'change',
    },
  ],
}

async function onSubmit() {
  submitting.value = true
  try {
    const res = await userRegister({
      userAccount: formState.userAccount.trim(),
      userPassword: formState.userPassword,
      checkPassword: formState.checkPassword,
    })
    const { code, message: msg } = res.data
    if (code === 0) {
      message.success('注册成功，请登录')
      await router.push('/user/login')
    } else {
      message.error(msg || '注册失败')
    }
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="page">
    <div class="card ds-surface">
      <h2 class="card__title">用户注册</h2>
      <a-form :model="formState" :rules="rules" layout="vertical" @finish="onSubmit">
        <a-form-item label="账号" name="userAccount">
          <a-input v-model:value="formState.userAccount" autocomplete="username" placeholder="账号" />
        </a-form-item>
        <a-form-item label="密码" name="userPassword">
          <a-input-password
            v-model:value="formState.userPassword"
            autocomplete="new-password"
            placeholder="至少 8 位"
          />
        </a-form-item>
        <a-form-item label="确认密码" name="checkPassword">
          <a-input-password
            v-model:value="formState.checkPassword"
            autocomplete="new-password"
            placeholder="再次输入密码"
          />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" block :loading="submitting">注册</a-button>
        </a-form-item>
      </a-form>
      <div class="extra">
        已有账号？
        <RouterLink to="/user/login">去登录</RouterLink>
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
