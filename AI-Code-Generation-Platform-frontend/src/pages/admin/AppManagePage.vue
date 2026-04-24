<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import AppEditModal from '@/components/AppEditModal.vue'
import {
  deleteAppByAdmin,
  listAppByPageForAdmin,
  updateAppByAdmin,
} from '@/api/appController'

const loading = ref(false)
const dataSource = ref<API.App[]>([])
const total = ref(0)

const editOpen = ref(false)
const editAppId = ref<number | null>(null)

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  id: undefined as number | undefined,
  appName: '',
  userId: undefined as number | undefined,
  codeGenType: '',
  deployKey: '',
  priority: undefined as number | undefined,
})

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 100 },
  { title: '名称', dataIndex: 'appName', key: 'appName', ellipsis: true },
  { title: '类型', dataIndex: 'codeGenType', key: 'codeGenType', width: 110 },
  { title: '优先级', dataIndex: 'priority', key: 'priority', width: 90 },
  { title: '用户 ID', dataIndex: 'userId', key: 'userId', width: 100 },
  { title: 'deployKey', dataIndex: 'deployKey', key: 'deployKey', ellipsis: true, width: 120 },
  { title: '更新时间', dataIndex: 'updateTime', key: 'updateTime', width: 180 },
  { title: '操作', key: 'action', width: 220, fixed: 'right' as const },
]

async function fetchList() {
  loading.value = true
  try {
    const res = await listAppByPageForAdmin({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      id: query.id,
      appName: query.appName.trim() || undefined,
      userId: query.userId,
      codeGenType: query.codeGenType.trim() || undefined,
      deployKey: query.deployKey.trim() || undefined,
      priority: query.priority,
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
  query.id = undefined
  query.appName = ''
  query.userId = undefined
  query.codeGenType = ''
  query.deployKey = ''
  query.priority = undefined
  query.pageNum = 1
  void fetchList()
}

function goEdit(record: API.App) {
  if (record.id == null) return
  editAppId.value = record.id
  editOpen.value = true
}

function onDelete(record: API.App) {
  Modal.confirm({
    title: '确认删除该应用？',
    content: `将删除 ID=${record.id} 的应用，不可恢复。`,
    okText: '删除',
    okType: 'danger',
    async onOk() {
      const res = await deleteAppByAdmin({ id: record.id })
      const { code, message: msg } = res.data
      if (code !== 0) {
        message.error(msg || '删除失败')
        throw new Error(msg)
      }
      message.success('已删除')
      await fetchList()
    },
  })
}

function onFeatured(record: API.App) {
  Modal.confirm({
    title: '设为精选（优先级 99）',
    content: `将应用「${record.appName ?? record.id}」优先级设为 99。`,
    async onOk() {
      const res = await updateAppByAdmin({ id: record.id, priority: 99 })
      const { code, message: msg } = res.data
      if (code !== 0) {
        message.error(msg || '操作失败')
        throw new Error(msg)
      }
      message.success('已更新')
      await fetchList()
    },
  })
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
    <AppEditModal v-model:open="editOpen" :app-id="editAppId" @success="fetchList" />

    <div class="manage__card ds-surface">
      <h2 class="manage__title">应用管理</h2>
      <a-form class="manage__query" layout="inline" :model="query" @submit.prevent="onSearch">
        <a-form-item label="ID">
          <a-input-number v-model:value="query.id" :min="1" placeholder="精确" style="width: 140px" />
        </a-form-item>
        <a-form-item label="名称">
          <a-input v-model:value="query.appName" allow-clear placeholder="模糊" style="width: 160px" />
        </a-form-item>
        <a-form-item label="用户 ID">
          <a-input-number v-model:value="query.userId" :min="1" placeholder="精确" style="width: 140px" />
        </a-form-item>
        <a-form-item label="类型">
          <a-input v-model:value="query.codeGenType" allow-clear placeholder="codeGenType" style="width: 140px" />
        </a-form-item>
        <a-form-item label="deployKey">
          <a-input v-model:value="query.deployKey" allow-clear style="width: 140px" />
        </a-form-item>
        <a-form-item label="优先级">
          <a-input-number v-model:value="query.priority" :min="0" style="width: 120px" />
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
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="goEdit(record)">编辑</a-button>
              <a-button type="link" size="small" @click="onFeatured(record)">精选</a-button>
              <a-button type="link" danger size="small" @click="onDelete(record)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
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
