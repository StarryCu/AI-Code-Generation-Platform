<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import { storeToRefs } from 'pinia'
import { API_BASE } from '@/config/apiBase'
import { deployApp, getAppVOById } from '@/api/appController'
import { ChatSseHttpError, streamAppChatGenCode } from '@/utils/chatSse'
import { useUserStore } from '@/stores/user'
import { RobotOutlined } from '@ant-design/icons-vue'
import ChatMarkdown from '@/components/ChatMarkdown.vue'

type ChatRole = 'user' | 'assistant'

interface ChatMessage {
  role: ChatRole
  content: string
}

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { loginUser } = storeToRefs(userStore)

const appId = ref(0)
const appDetail = ref<API.AppVO | null>(null)
const messages = ref<ChatMessage[]>([])
const input = ref('')
const sending = ref(false)
const previewUrl = ref<string | null>(null)
const listRef = ref<HTMLElement | null>(null)
const abortCtl = ref<AbortController | null>(null)
const autoStarted = ref(false)

const userDisplayName = computed(
  () => loginUser.value?.userName || loginUser.value?.userAccount || '我',
)
const userInitial = computed(() => userDisplayName.value.slice(0, 1).toUpperCase())

function buildPreviewUrl(vo: API.AppVO) {
  const type = vo.codeGenType ?? 'multi_file'
  const id = vo.id
  if (!id) return null
  const base = API_BASE.replace(/\/$/, '')
  return `${base}/static/${type}_${id}/`
}

async function scrollToBottom() {
  await nextTick()
  const el = listRef.value
  if (!el) return
  el.scrollTop = el.scrollHeight
}

async function loadApp(): Promise<boolean> {
  const raw = Number(route.params.appId)
  if (!Number.isFinite(raw) || raw <= 0) {
    message.error('无效的应用 ID')
    await router.replace('/')
    return false
  }
  appId.value = raw
  const res = await getAppVOById({ id: raw })
  const { code, data, message: msg } = res.data
  if (code !== 0 || !data) {
    message.error(msg || '加载应用失败')
    await router.replace('/')
    return false
  }
  appDetail.value = data
  if (data.userId !== loginUser.value?.id) {
    message.warning('仅应用所有者可以在此对话生成')
    await router.replace('/')
    return false
  }
  return true
}

async function runStream(userText: string) {
  sending.value = true
  previewUrl.value = null
  messages.value.push({ role: 'user', content: userText })
  messages.value.push({ role: 'assistant', content: '' })
  await scrollToBottom()

  const ac = new AbortController()
  abortCtl.value = ac
  const assistantIndex = messages.value.length - 1

  try {
    await streamAppChatGenCode(
      appId.value,
      userText,
      (chunk) => {
        const cur = messages.value[assistantIndex]
        if (cur) cur.content += chunk
        void scrollToBottom()
      },
      { signal: ac.signal },
    )
    if (appDetail.value) {
      previewUrl.value = buildPreviewUrl(appDetail.value)
    }
  } catch (e) {
    if ((e as Error).name === 'AbortError') return
    if (e instanceof ChatSseHttpError && e.status === 401) {
      message.warning('请先登录')
      await router.push({ path: '/user/login', query: { redirect: route.fullPath } })
    } else {
      message.error((e as Error).message || '生成失败')
    }
    const last = messages.value[messages.value.length - 1]
    if (last?.role === 'assistant' && !last.content) {
      messages.value.pop()
    } else if (last?.role === 'assistant') {
      last.content += '\n\n（流式输出异常中断）'
    }
  } finally {
    sending.value = false
    abortCtl.value = null
    await scrollToBottom()
  }
}

async function onSend() {
  const text = input.value.trim()
  if (!text || sending.value) return
  input.value = ''
  await runStream(text)
}

async function onDeploy() {
  if (!appDetail.value?.id) return
  Modal.confirm({
    title: '部署应用',
    content: '将把已生成的代码发布到可访问地址（若已部署则可能覆盖）。是否继续？',
    okText: '部署',
    async onOk() {
      const res = await deployApp({ appId: appDetail.value!.id })
      const { code, data, message: msg } = res.data
      if (code !== 0 || !data) {
        message.error(msg || '部署失败')
        throw new Error(msg)
      }
      message.success('部署成功')
      Modal.info({
        title: '部署地址',
        content: `请访问：${data}`,
      })
    },
  })
}

onMounted(async () => {
  await userStore.fetchLoginUser()
  const ok = await loadApp()
  if (!ok) return
  if (route.query.auto === '1' && appDetail.value?.initPrompt && !autoStarted.value) {
    autoStarted.value = true
    const p = appDetail.value.initPrompt.trim()
    await router.replace({ path: route.path })
    if (p) await runStream(p)
  }
})

onUnmounted(() => {
  abortCtl.value?.abort()
})

watch(
  () => route.params.appId,
  async () => {
    autoStarted.value = false
    messages.value = []
    previewUrl.value = null
    input.value = ''
    await loadApp()
  },
)
</script>

<template>
  <div class="gen">
    <header class="gen__bar ds-surface">
      <div class="gen__bar-left">
        <div class="gen__bar-avatars">
          <a-tooltip :title="appDetail?.appName || '应用'">
            <a-avatar v-if="appDetail?.cover" class="gen__bar-ava" :size="44" :src="appDetail.cover" />
            <a-avatar v-else class="gen__bar-ava gen__ava--ai" :size="44">
              <template #icon>
                <RobotOutlined />
              </template>
            </a-avatar>
          </a-tooltip>
          <span class="gen__bar-vs" aria-hidden="true">·</span>
          <a-tooltip :title="userDisplayName">
            <a-avatar class="gen__bar-ava gen__bar-ava--user" :size="44" :src="loginUser?.userAvatar">
              {{ userInitial }}
            </a-avatar>
          </a-tooltip>
        </div>
        <div class="gen__bar-text">
          <span class="gen__bar-label">当前应用</span>
          <h2 class="gen__bar-title">{{ appDetail?.appName || `应用 #${appId}` }}</h2>
        </div>
      </div>
      <a-button type="primary" size="large" class="gen__deploy" :disabled="!appDetail?.id" @click="onDeploy">
        部署
      </a-button>
    </header>

    <div class="gen__grid">
      <section class="gen__panel ds-surface">
        <div class="gen__panel-head">对话</div>
        <div ref="listRef" class="gen__messages">
          <div v-for="(m, idx) in messages" :key="idx" class="gen__msg" :class="`gen__msg--${m.role}`">
            <template v-if="m.role === 'assistant'">
              <div class="gen__msg-side gen__msg-side--assistant">
                <a-avatar v-if="appDetail?.cover" class="gen__ava" :size="40" :src="appDetail.cover" />
                <a-avatar v-else class="gen__ava gen__ava--ai" :size="40">
                  <template #icon>
                    <RobotOutlined />
                  </template>
                </a-avatar>
                <span class="gen__ava-label">{{ appDetail?.appName || '应用' }}</span>
              </div>
              <div class="gen__msg-main">
                <div class="gen__bubble gen__bubble--md">
                  <template v-if="!m.content && sending">…</template>
                  <ChatMarkdown v-else-if="m.content" :content="m.content" />
                </div>
              </div>
            </template>
            <template v-else>
              <div class="gen__msg-main">
                <div class="gen__bubble">{{ m.content }}</div>
              </div>
              <div class="gen__msg-side gen__msg-side--user">
                <a-avatar class="gen__ava" :size="40" :src="loginUser?.userAvatar">
                  {{ userInitial }}
                </a-avatar>
                <span class="gen__ava-label">{{ userDisplayName }}</span>
              </div>
            </template>
          </div>
          <a-empty v-if="!messages.length" class="gen__empty" description="发送消息开始生成代码" />
        </div>
        <div class="gen__input-wrap">
          <div class="gen__input-ava" aria-hidden="true">
            <a-tooltip :title="userDisplayName">
              <a-avatar :size="36" :src="loginUser?.userAvatar">{{ userInitial }}</a-avatar>
            </a-tooltip>
          </div>
          <a-textarea
            v-model:value="input"
            class="gen__input"
            :auto-size="{ minRows: 2, maxRows: 6 }"
            :disabled="sending"
            :bordered="false"
            placeholder="描述你想要的修改或页面…"
          />
          <a-button type="primary" class="gen__send" :loading="sending" @click="onSend">发送</a-button>
        </div>
      </section>

      <section class="gen__panel gen__panel--preview ds-surface">
        <div class="gen__panel-head gen__panel-head--row">
          <span>网页预览</span>
          <span v-if="previewUrl" class="gen__badge">已就绪</span>
        </div>
        <div class="gen__preview-body">
          <iframe v-if="previewUrl" class="gen__iframe" :src="previewUrl" title="preview" />
          <div v-else class="gen__preview-placeholder">
            <p>完成一次完整流式生成后，将在此处展示生成的站点。</p>
          </div>
        </div>
      </section>
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

.gen {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: calc(100vh - 140px);
  max-width: 1600px;
  margin: 0 auto;
}

.gen__bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 22px;
}

.gen__bar-left {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 14px;
}

.gen__bar-avatars {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.gen__bar-vs {
  color: var(--ds-text-muted);
  font-weight: 700;
  user-select: none;
}

.gen__bar-text {
  min-width: 0;
}

.gen__bar-ava {
  flex-shrink: 0;
  border: 2px solid #fff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
}

.gen__bar-ava--user {
  background: linear-gradient(145deg, #e8eaef, #d4d8e0);
  color: var(--ds-ink);
  font-weight: 700;
}

.gen__ava--ai {
  background: linear-gradient(135deg, #ff9a56, #ff6b00) !important;
  color: #fff !important;
}

.gen__bar-label {
  display: block;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--ds-text-muted);
  margin-bottom: 4px;
}

.gen__bar-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--ds-ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.gen__deploy {
  flex-shrink: 0;
  height: 44px !important;
  padding-inline: 28px !important;
  border-radius: 14px !important;
  font-weight: 700 !important;
  box-shadow: 0 8px 22px rgba(255, 107, 0, 0.25);
}

.gen__grid {
  display: grid;
  grid-template-columns: minmax(320px, 1fr) minmax(360px, 1.25fr);
  gap: 18px;
  flex: 1;
  min-height: 0;
}

.gen__panel {
  display: flex;
  flex-direction: column;
  min-height: 560px;
  overflow: hidden;
}

.gen__panel-head {
  padding: 14px 20px;
  font-weight: 700;
  font-size: 14px;
  border-bottom: 1px solid var(--ds-border);
  color: var(--ds-ink);
}

.gen__panel-head--row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.gen__badge {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(34, 197, 94, 0.12);
  color: #16a34a;
}

.gen__messages {
  flex: 1;
  overflow: auto;
  padding: 18px 16px;
  background: linear-gradient(180deg, #fafbfc 0%, #f3f4f6 100%);
}

.gen__msg {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  margin-bottom: 16px;
}

.gen__msg--user {
  justify-content: flex-end;
}

.gen__msg--assistant {
  justify-content: flex-start;
}

.gen__msg-side {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
  width: 56px;
}

.gen__msg-side--user {
  order: 1;
}

.gen__msg--user .gen__msg-main {
  order: 0;
}

.gen__ava-label {
  font-size: 11px;
  color: var(--ds-text-muted);
  max-width: 56px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-align: center;
  line-height: 1.2;
}

.gen__msg-main {
  max-width: min(100% - 72px, 640px);
  min-width: 0;
}

.gen__bubble {
  max-width: 100%;
  padding: 12px 16px;
  border-radius: 20px;
  font-size: 14px;
  line-height: 1.55;
  white-space: pre-wrap;
  word-break: break-word;
}

.gen__msg--user .gen__bubble {
  background: var(--ds-ink);
  color: #fff;
  border-bottom-right-radius: 6px;
}

.gen__msg--assistant .gen__bubble {
  background: #fff;
  border: 1px solid var(--ds-border);
  border-bottom-left-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.gen__bubble--md {
  white-space: normal;
  max-width: 100%;
}

.gen__empty {
  margin-top: 48px;
}

.gen__input-wrap {
  display: flex;
  gap: 10px;
  align-items: flex-end;
  padding: 14px 16px;
  border-top: 1px solid var(--ds-border);
  background: #fff;
}

.gen__input-ava {
  flex-shrink: 0;
  padding-bottom: 4px;
}

.gen__input-ava :deep(.ant-avatar) {
  border: 2px solid #f0f1f3;
}

.gen__input {
  flex: 1;
  border-radius: 16px !important;
  background: #f4f5f7 !important;
  padding: 8px 12px !important;
}

.gen__send {
  height: 44px !important;
  border-radius: 14px !important;
  font-weight: 700 !important;
  padding-inline: 22px !important;
}

.gen__preview-body {
  flex: 1;
  min-height: 0;
  background: #eceef2;
  display: flex;
}

.gen__iframe {
  flex: 1;
  width: 100%;
  min-height: 420px;
  border: 0;
  background: #fff;
}

.gen__preview-placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
  text-align: center;
  color: var(--ds-text-muted);
  font-size: 14px;
  line-height: 1.6;
}

@media (max-width: 991px) {
  .gen__grid {
    grid-template-columns: 1fr;
  }

  .gen__panel {
    min-height: 420px;
  }
}
</style>
