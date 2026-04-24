/// <reference types="vite/client" />

import 'vue-router'

declare module 'vue-router' {
  interface RouteMeta {
    requiresAdmin?: boolean
    requiresAuth?: boolean
    fullWidth?: boolean
    /** 主区域顶栏标题；不填则显示问候语 */
    pageTitle?: string
    /** 不渲染顶栏（首页、对话页等自行排版） */
    hideTopbar?: boolean
  }
}
