<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import { SearchOutlined } from '@ant-design/icons-vue'
import { storeToRefs } from 'pinia'
import { siteTitle } from '@/config/site'
import { useUserStore } from '@/stores/user'
import AppEditModal from '@/components/AppEditModal.vue'
import {
  addApp,
  deleteAppByUser,
  getAppVoById,
  listFeaturedAppVoByPage,
  listMyAppVoByPage,
} from '@/api/appController'

const router = useRouter()
const userStore = useUserStore()
const { isLogin, loginUser } = storeToRefs(userStore)

const initPrompt = ref('')
const creating = ref(false)

const myPage = ref(1)
const myPageSize = ref(10)
const myTotal = ref(0)
const myName = ref('')
const myLoading = ref(false)
const myRecords = ref<API.AppVO[]>([])

const featPage = ref(1)
const featPageSize = ref(10)
const featTotal = ref(0)
const featName = ref('')
const featLoading = ref(false)
const featRecords = ref<API.AppVO[]>([])

const detailOpen = ref(false)
const detailApp = ref<API.AppVO | null>(null)

const editOpen = ref(false)
const editAppId = ref<number | null>(null)

function isOwner(row: API.AppVO) {
  return row.userId != null && row.userId === loginUser.value?.id
}

async function loadMyApps() {
  if (!isLogin.value) {
    myRecords.value = []
    myTotal.value = 0
    return
  }
  myLoading.value = true
  try {
    const res = await listMyAppVoByPage({
      pageNum: myPage.value,
      pageSize: Math.min(myPageSize.value, 20),
      appName: myName.value.trim() || undefined,
    })
    const { code, data, message: msg } = res.data
    if (code !== 0 || !data) {
      message.error(msg || '加载我的应用失败')
      return
    }
    myRecords.value = data.records ?? []
    myTotal.value = data.totalRow ?? 0
  } finally {
    myLoading.value = false
  }
}

async function loadFeatured() {
  if (!isLogin.value) {
    featRecords.value = []
    featTotal.value = 0
    return
  }
  featLoading.value = true
  try {
    const res = await listFeaturedAppVoByPage({
      pageNum: featPage.value,
      pageSize: Math.min(featPageSize.value, 20),
      appName: featName.value.trim() || undefined,
    })
    const { code, data, message: msg } = res.data
    if (code !== 0 || !data) {
      message.error(msg || '加载精选应用失败')
      return
    }
    featRecords.value = data.records ?? []
    featTotal.value = data.totalRow ?? 0
  } finally {
    featLoading.value = false
  }
}

function searchMy() {
  myPage.value = 1
  void loadMyApps()
}

function searchFeat() {
  featPage.value = 1
  void loadFeatured()
}

onMounted(async () => {
  await userStore.fetchLoginUser()
  await Promise.all([loadMyApps(), loadFeatured()])
})

watch(isLogin, async () => {
  myPage.value = 1
  featPage.value = 1
  await Promise.all([loadMyApps(), loadFeatured()])
})

watch([myPage, myPageSize], () => {
  void loadMyApps()
})

watch([featPage, featPageSize], () => {
  void loadFeatured()
})

async function waitAppVoAfterCreate(id: number, maxAttempts = 8): Promise<boolean> {
  for (let attempt = 0; attempt < maxAttempts; attempt++) {
    const voRes = await getAppVoById({ id })
    const { code, data, message: msg } = voRes.data
    if (code === 0 && data) {
      return true
    }
    if (attempt === maxAttempts - 1) {
      message.error(msg || '应用已创建，但加载详情失败，请稍后在「我的应用」中打开')
      return false
    }
    await new Promise((r) => setTimeout(r, 80 * (attempt + 1)))
  }
  return false
}

async function onCreateApp() {
  if (!isLogin.value) {
    message.warning('请先登录')
    await router.push({ path: '/user/login', query: { redirect: '/' } })
    return
  }
  const text = initPrompt.value.trim()
  if (!text) {
    message.warning('请输入提示词')
    return
  }
  creating.value = true
  try {
    const res = await addApp({ initPrompt: text })
    const { code, data, message: msg } = res.data
    if (code !== 0 || data == null) {
      message.error(msg || '创建失败')
      return
    }
    const newId = Number(data)
    if (!Number.isFinite(newId) || newId <= 0) {
      message.error('创建返回的应用 ID 无效')
      return
    }
    const voReady = await waitAppVoAfterCreate(newId)
    if (!voReady) {
      await loadMyApps()
      return
    }
    message.success('已创建应用，正在进入对话页')
    initPrompt.value = ''
    await router.push({ path: `/app/gen/${newId}` })
  } finally {
    creating.value = false
  }
}

function goChat(id: number) {
  void router.push(`/app/gen/${id}`)
}

function goEdit(id: number) {
  editAppId.value = id
  editOpen.value = true
}

async function onEditSuccess() {
  await Promise.all([loadMyApps(), loadFeatured()])
}

function openDetail(row: API.AppVO) {
  detailApp.value = row
  detailOpen.value = true
}

function onDeleteMy(row: API.AppVO) {
  Modal.confirm({
    title: '确认删除该应用？',
    content: `将删除「${row.appName ?? row.id}」，此操作不可恢复。`,
    okText: '删除',
    okType: 'danger',
    async onOk() {
      const res = await deleteAppByUser({ id: row.id })
      const { code, message: msg } = res.data
      if (code !== 0) {
        message.error(msg || '删除失败')
        throw new Error(msg)
      }
      message.success('已删除')
      await loadMyApps()
    },
  })
}

function appInitial(name?: string) {
  const s = (name ?? 'A').trim()
  return s.slice(0, 1).toUpperCase()
}
</script>

<template>
  <div class="home">
    <section class="home__intro">
      <p class="home__eyebrow">{{ siteTitle }}</p>
      <h1 class="home__headline">用一句话，开始你的网站</h1>
      <p class="home__lede">描述需求，与 AI 对话生成页面，并在一侧实时预览效果，可随时部署上线。</p>
    </section>

    <section class="ds-surface home__create">
      <div class="home__create-head">
        <span class="home__create-label">创建应用</span>
        <span v-if="!isLogin" class="home__pill">需登录</span>
      </div>
      <a-textarea
        v-model:value="initPrompt"
        class="home__textarea"
        :rows="5"
        :maxlength="2000"
        show-count
        :bordered="false"
        placeholder="例如：做一个深色风格的作品集首页，含导航、项目卡片与页脚联系方式…"
      />
      <div class="home__create-foot">
        <a-button
          type="primary"
          size="large"
          class="home__cta"
          :loading="creating"
          :disabled="creating"
          @click="onCreateApp"
        >
          创建并开始生成
        </a-button>
      </div>
    </section>

    <section class="ds-surface home__block">
      <div class="home__block-head">
        <h2 class="home__h2">我的应用</h2>
        <a-input
          v-if="isLogin"
          v-model:value="myName"
          allow-clear
          class="home__search"
          placeholder="按名称搜索"
          @press-enter="searchMy"
        >
          <template #suffix>
            <SearchOutlined class="home__search-ico" @click="searchMy" />
          </template>
        </a-input>
      </div>

      <template v-if="!isLogin">
        <a-empty class="home__empty" description="登录后查看与管理你的应用" />
      </template>
      <template v-else>
        <a-spin :spinning="myLoading">
          <div v-if="!myRecords.length" class="home__empty-wrap">
            <a-empty description="暂无应用，先在上方输入提示词创建吧" />
          </div>
          <ul v-else class="home__list">
            <li v-for="row in myRecords" :key="row.id" class="home__row">
              <div class="home__row-ava" :class="{ 'home__row-ava--img': !!row.cover }">
                <img v-if="row.cover" :src="row.cover" alt="" />
                <span v-else>{{ appInitial(row.appName) }}</span>
              </div>
              <div class="home__row-main">
                <div class="home__row-title">{{ row.appName }}</div>
                <div class="home__row-meta">#{{ row.id }} · {{ row.createTime ?? '—' }}</div>
              </div>
              <div class="home__row-actions">
                <a-button type="text" class="home__linkish" @click="goChat(row.id!)">对话</a-button>
                <a-button type="text" class="home__linkish" @click="goEdit(row.id!)">编辑</a-button>
                <a-button type="text" danger @click="onDeleteMy(row)">删除</a-button>
              </div>
            </li>
          </ul>
        </a-spin>
        <div v-if="myTotal > 0" class="home__pager">
          <a-pagination
            v-model:current="myPage"
            v-model:page-size="myPageSize"
            :total="myTotal"
            :show-size-changer="true"
            :page-size-options="['10', '20']"
          />
        </div>
      </template>
    </section>

    <section class="home__block home__block--feat">
      <div class="home__block-head">
        <h2 class="home__h2">精选应用</h2>
        <a-input
          v-if="isLogin"
          v-model:value="featName"
          allow-clear
          class="home__search"
          placeholder="按名称搜索"
          @press-enter="searchFeat"
        >
          <template #suffix>
            <SearchOutlined class="home__search-ico" @click="searchFeat" />
          </template>
        </a-input>
      </div>

      <template v-if="!isLogin">
        <a-empty class="home__empty" description="登录后查看精选应用" />
      </template>
      <template v-else>
        <a-spin :spinning="featLoading">
          <div v-if="!featRecords.length" class="home__empty-wrap">
            <a-empty description="暂无精选应用" />
          </div>
          <div v-else class="feat-grid">
            <article v-for="row in featRecords" :key="row.id" class="feat-card">
              <div class="feat-card__media">
                <img v-if="row.cover" :src="row.cover" alt="" />
                <div v-else class="feat-card__placeholder">{{ appInitial(row.appName) }}</div>
              </div>
              <div class="feat-card__body">
                <h3 class="feat-card__title">{{ row.appName }}</h3>
                <p class="feat-card__sub">优先级 {{ row.priority ?? '—' }} · {{ row.createTime ?? '' }}</p>
                <div class="feat-card__actions">
                  <a-button size="small" type="primary" ghost @click="openDetail(row)">详情</a-button>
                  <a-button v-if="isOwner(row)" size="small" @click="goChat(row.id!)">对话</a-button>
                  <a-button v-if="isOwner(row)" size="small" @click="goEdit(row.id!)">编辑</a-button>
                </div>
              </div>
            </article>
          </div>
        </a-spin>
        <div v-if="featTotal > 0" class="home__pager">
          <a-pagination
            v-model:current="featPage"
            v-model:page-size="featPageSize"
            :total="featTotal"
            :show-size-changer="true"
            :page-size-options="['10', '20']"
          />
        </div>
      </template>
    </section>

    <AppEditModal v-model:open="editOpen" :app-id="editAppId" @success="onEditSuccess" />

    <a-modal
      v-model:open="detailOpen"
      title="应用详情"
      :footer="null"
      width="520px"
      destroy-on-close
      wrap-class-name="ds-modal"
    >
      <template v-if="detailApp">
        <a-descriptions bordered size="small" :column="1">
          <a-descriptions-item label="ID">{{ detailApp.id }}</a-descriptions-item>
          <a-descriptions-item label="名称">{{ detailApp.appName }}</a-descriptions-item>
          <a-descriptions-item label="类型">{{ detailApp.codeGenType }}</a-descriptions-item>
          <a-descriptions-item label="优先级">{{ detailApp.priority }}</a-descriptions-item>
          <a-descriptions-item label="创建时间">{{ detailApp.createTime }}</a-descriptions-item>
          <a-descriptions-item v-if="detailApp.initPrompt" label="初始提示词">
            {{ detailApp.initPrompt }}
          </a-descriptions-item>
        </a-descriptions>
      </template>
    </a-modal>
  </div>
</template>

<style scoped>
.home {
  max-width: 1120px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 22px;
  padding-top: 8px;
}

.ds-surface {
  background: var(--ds-surface);
  border-radius: var(--ds-radius-xl);
  box-shadow: var(--ds-shadow);
  border: 1px solid var(--ds-border);
}

.home__intro {
  padding: 4px 4px 8px;
}

.home__eyebrow {
  margin: 0 0 8px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--ds-text-muted);
}

.home__headline {
  margin: 0;
  font-size: 32px;
  font-weight: 800;
  letter-spacing: -0.03em;
  line-height: 1.15;
  color: var(--ds-ink);
}

.home__lede {
  margin: 12px 0 0;
  max-width: 640px;
  font-size: 15px;
  line-height: 1.6;
  color: var(--ds-text-muted);
}

.home__create {
  padding: 22px 24px 20px;
}

.home__create-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.home__create-label {
  font-weight: 700;
  font-size: 15px;
  color: var(--ds-ink);
}

.home__pill {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  background: var(--ds-accent-soft);
  color: var(--ds-accent);
  font-weight: 600;
}

.home__textarea :deep(textarea) {
  font-size: 15px;
  line-height: 1.55;
  padding: 8px 0;
  resize: none;
}

.home__create-foot {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.home__cta {
  min-width: 200px;
  height: 48px !important;
  border-radius: 14px !important;
  font-weight: 700 !important;
  font-size: 15px !important;
  box-shadow: 0 8px 24px rgba(255, 107, 0, 0.28);
}

.home__block {
  padding: 20px 22px 18px;
}

.home__block--feat {
  background: transparent;
  border: none;
  box-shadow: none;
  padding-left: 0;
  padding-right: 0;
}

.home__block-head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.home__h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--ds-ink);
}

.home__search {
  max-width: 280px;
  border-radius: 999px !important;
}

.home__search :deep(.ant-input) {
  border-radius: 999px;
}

.home__search-ico {
  color: var(--ds-text-muted);
  cursor: pointer;
}

.home__empty {
  padding: 24px 0;
}

.home__empty-wrap {
  padding: 20px 0;
}

.home__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.home__row {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border-radius: var(--ds-radius-lg);
  border: 1px solid var(--ds-border);
  background: #fafbfc;
  transition: box-shadow 0.2s ease, border-color 0.2s ease;
}

.home__row:hover {
  border-color: rgba(255, 107, 0, 0.35);
  box-shadow: var(--ds-shadow-hover);
}

.home__row-ava {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: linear-gradient(135deg, #ffe7d6, #ffd0bf);
  color: var(--ds-accent);
  font-weight: 800;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
}

.home__row-ava--img {
  padding: 0;
  background: none;
}

.home__row-ava img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.home__row-main {
  flex: 1;
  min-width: 0;
}

.home__row-title {
  font-weight: 600;
  font-size: 15px;
  color: var(--ds-ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.home__row-meta {
  font-size: 12px;
  color: var(--ds-text-muted);
  margin-top: 4px;
}

.home__row-actions {
  display: flex;
  flex-shrink: 0;
  gap: 4px;
}

.home__linkish {
  color: var(--ds-accent) !important;
  font-weight: 600;
}

.home__pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.feat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 18px;
}

.feat-card {
  background: var(--ds-surface);
  border-radius: var(--ds-radius-xl);
  box-shadow: var(--ds-shadow);
  border: 1px solid var(--ds-border);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.feat-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--ds-shadow-hover);
}

.feat-card__media {
  aspect-ratio: 16 / 10;
  background: linear-gradient(145deg, #eceef2, #dfe3ea);
  overflow: hidden;
}

.feat-card__media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.feat-card__placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 42px;
  font-weight: 800;
  color: rgba(26, 26, 26, 0.2);
}

.feat-card__body {
  padding: 16px 18px 18px;
}

.feat-card__title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--ds-ink);
}

.feat-card__sub {
  margin: 8px 0 14px;
  font-size: 12px;
  color: var(--ds-text-muted);
}

.feat-card__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

@media (max-width: 640px) {
  .home__headline {
    font-size: 26px;
  }

  .home__row {
    flex-wrap: wrap;
  }

  .home__row-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
