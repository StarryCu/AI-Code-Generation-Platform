<script setup lang="ts">
import { ConfigProvider, theme } from 'ant-design-vue'
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import BasicLayout from '@/layouts/BasicLayout.vue'

const antTheme = {
  algorithm: theme.defaultAlgorithm,
  token: {
    colorPrimary: '#ff6b00',
    borderRadiusLG: 14,
    borderRadius: 10,
    fontFamily: `'Inter', system-ui, -apple-system, 'Segoe UI', sans-serif`,
  },
}
</script>

<template>
  <ConfigProvider :locale="zhCN" :theme="antTheme">
    <BasicLayout />
  </ConfigProvider>
</template>
