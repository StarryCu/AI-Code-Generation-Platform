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
    <a-card class="card" title="用户注册">
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
    </a-card>
  </div>
</template>

<style scoped>
.page {
  display: flex;
  justify-content: center;
  padding: 24px 16px;
}

.card {
  width: 100%;
  max-width: 400px;
}

.extra {
  text-align: center;
  color: rgba(0, 0, 0, 0.45);
  font-size: 14px;
}

.extra a {
  margin-left: 4px;
}
</style>
