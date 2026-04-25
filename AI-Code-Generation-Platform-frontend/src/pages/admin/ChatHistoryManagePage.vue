<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { listChatHistoryAdminByPage } from '@/api/chatHistoryController'

const loading = ref(false)
const dataSource = ref<API.ChatHistoryAdminVO[]>([])
const total = ref(0)

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  appId: undefined as number | undefined,
  userId: undefined as number | undefined,
  messageType: '',
})

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 100 },
  { title: '应用', dataIndex: 'appName', key: 'appName', ellipsis: true, width: 180 },
  { title: 'appId', dataIndex: 'appId', key: 'appId', width: 110 },
  { title: 'userId', dataIndex: 'userId', key: 'userId', width: 110 },
  { title: '类型', dataIndex: 'messageType', key: 'messageType', width: 120 },
  { title: '内容', dataIndex: 'message', key: 'message', ellipsis: true },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
]

async function fetchList() {
  loading.value = true
  try {
    const res = await listChatHistoryAdminByPage({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      appId: query.appId,
      userId: query.userId,
      messageType: query.messageType.trim() || undefined,
    })
    const { code, data, message: msg } = res.data
    if (code !== 0 || !data) {
      message.error(msg || '查询失败')
      return
    }
    dataSource.value = data.records ?? []
    total.value = data.totalRow ?? 0
  } finally {
    loading.value = false
  }
}

function onSearch() {
  query.pageNum = 1
  void fetchList()
}

function onReset() {
  query.appId = undefined
  query.userId = undefined
  query.messageType = ''
  query.pageNum = 1
  void fetchList()
}

function handleTableChange(pag: { current?: number; pageSize?: number } | false) {
  if (!pag) return
  query.pageNum = pag.current ?? 1
  query.pageSize = pag.pageSize ?? 10
  void fetchList()
}

onMounted(() => {
  void fetchList()
})
</script>

<template>
  <div class="manage">
    <div class="manage__card ds-surface">
      <h2 class="manage__title">对话管理</h2>
      <a-form class="manage__query" layout="inline" :model="query" @submit.prevent="onSearch">
        <a-form-item label="appId">
          <a-input-number v-model:value="query.appId" :min="1" placeholder="精确" style="width: 140px" />
        </a-form-item>
        <a-form-item label="userId">
          <a-input-number v-model:value="query.userId" :min="1" placeholder="精确" style="width: 140px" />
        </a-form-item>
        <a-form-item label="messageType">
          <a-input v-model:value="query.messageType" allow-clear placeholder="模糊" style="width: 160px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit">查询</a-button>
            <a-button @click="onReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <a-table
        row-key="id"
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :scroll="{ x: 1200 }"
        :pagination="{
          current: query.pageNum,
          pageSize: query.pageSize,
          total,
          showSizeChanger: true,
          showTotal: (t: number) => `共 ${t} 条`,
          pageSizeOptions: ['10', '20', '50', '100'],
        }"
        @change="handleTableChange"
      />
    </div>
  </div>
</template>

<style scoped>
.ds-surface {
  background: var(--ds-surface);
  border-radius: var(--ds-radius-xl);
  box-shadow: var(--ds-shadow);
  border: 1px solid var(--ds-border);
  padding: 22px 24px 26px;
}

.manage {
  max-width: 1400px;
  margin: 0 auto;
}

.manage__title {
  margin: 0 0 18px;
  font-size: 20px;
  font-weight: 800;
  color: var(--ds-ink);
  letter-spacing: -0.02em;
}

.manage__query {
  margin-bottom: 16px;
  row-gap: 12px;
}

.manage :deep(.ant-input),
.manage :deep(.ant-input-number) {
  border-radius: 10px;
}
</style>

