/* eslint-disable @typescript-eslint/ban-ts-comment */
// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** POST /app/add */
export async function addApp(body: API.AppAddRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseLong>('/app/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** POST /app/update */
export async function updateAppByUser(body: API.AppUserUpdateRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/app/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** POST /app/delete */
export async function deleteAppByUser(body: API.DeleteRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/app/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** GET /app/get/vo */
export async function getAppVOById(params: API.getAppVOByIdParams, options?: { [key: string]: any }) {
  return request<API.BaseResponseAppVO>('/app/get/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** POST /app/my/list/page/vo */
export async function listMyAppVOByPage(body: API.AppUserQueryRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponsePageAppVO>('/app/my/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** POST /app/featured/list/page/vo */
export async function listFeaturedAppVOByPage(
  body: API.AppFeaturedQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageAppVO>('/app/featured/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** POST /app/admin/delete */
export async function deleteAppByAdmin(body: API.DeleteRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/app/admin/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** POST /app/admin/update */
export async function updateAppByAdmin(body: API.AppAdminUpdateRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/app/admin/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** POST /app/admin/list/page */
export async function listAppByPageForAdmin(body: API.AppAdminQueryRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponsePageApp>('/app/admin/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** GET /app/admin/get */
export async function getAppByIdForAdmin(params: API.getAppByIdForAdminParams, options?: { [key: string]: any }) {
  return request<API.BaseResponseApp>('/app/admin/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** POST /app/deploy */
export async function deployApp(body: API.AppDeployRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseString>('/app/deploy', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
