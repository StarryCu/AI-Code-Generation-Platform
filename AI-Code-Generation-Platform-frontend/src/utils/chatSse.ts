import { API_BASE } from '@/config/apiBase'

export class ChatSseHttpError extends Error {
  constructor(public readonly status: number) {
    super(`请求失败：HTTP ${status}`)
    this.name = 'ChatSseHttpError'
  }
}

/**
 * 消费后端 `GET /app/chat/gen/code` 的 SSE（data 为 JSON：`{"d":"片段"}`）
 */
export async function streamAppChatGenCode(
  appId: number,
  message: string,
  onChunk: (text: string) => void,
  options?: { signal?: AbortSignal },
): Promise<void> {
  const base = API_BASE.replace(/\/$/, '')
  const url = `${base}/app/chat/gen/code?appId=${appId}&message=${encodeURIComponent(message)}`
  const res = await fetch(url, {
    method: 'GET',
    credentials: 'include',
    signal: options?.signal,
  })
  if (!res.ok) {
    throw new ChatSseHttpError(res.status)
  }
  const reader = res.body?.getReader()
  if (!reader) {
    throw new Error('响应体不可读')
  }
  const decoder = new TextDecoder()
  let buffer = ''
  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    buffer = consumeSseBuffer(buffer, onChunk)
  }
  buffer += decoder.decode()
  flushTrailingSse(buffer, onChunk)
}

function consumeSseBuffer(buffer: string, onChunk: (text: string) => void): string {
  const parts = buffer.split(/\n\n/)
  const rest = parts.pop() ?? ''
  for (const rawEvent of parts) {
    const lines = rawEvent.split(/\n/)
    for (const line of lines) {
      if (!line.startsWith('data:')) continue
      const payload = line.slice(5).trimStart()
      if (!payload || payload === '[DONE]') continue
      try {
        const obj = JSON.parse(payload) as { d?: string }
        if (obj.d) onChunk(obj.d)
      } catch {
        // 忽略非 JSON 行
      }
    }
  }
  return rest
}

function flushTrailingSse(buffer: string, onChunk: (text: string) => void) {
  if (!buffer.trim()) return
  for (const line of buffer.split(/\n/)) {
    if (!line.startsWith('data:')) continue
    const payload = line.slice(5).trimStart()
    if (!payload || payload === '[DONE]') continue
    try {
      const obj = JSON.parse(payload) as { d?: string }
      if (obj.d) onChunk(obj.d)
    } catch {
      // ignore
    }
  }
}
