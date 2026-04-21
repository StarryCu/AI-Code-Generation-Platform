/** 顶部导航菜单项（可按需增删，与路由 path 对应） */
export interface NavMenuItem {
  /** 与路由 path 一致，用于 Menu 的 key 与跳转 */
  key: string
  label: string
}

export const navMenuItems: NavMenuItem[] = [
  { key: '/', label: '首页' },
  { key: '/about', label: '关于' },
]
