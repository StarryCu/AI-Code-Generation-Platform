// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 POST /chatHistory/admin/list/page/vo */
export async function listChatHistoryAdminByPage(
  body: API.ChatHistoryAdminQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageChatHistoryAdminVO>('/chatHistory/admin/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /chatHistory/app/list/page/vo */
export async function listAppChatHistoryByCursor(
  body: API.ChatHistoryAppCursorRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseChatHistoryAppPageVO>('/chatHistory/app/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
