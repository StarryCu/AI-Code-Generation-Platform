<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { storeToRefs } from 'pinia'
import { useUserStore } from '@/stores/user'
import {
  getAppByIdForAdmin,
  getAppVOById,
  updateAppByAdmin,
  updateAppByUser,
} from '@/api/appController'

const open = defineModel<boolean>('open', { required: true })

const props = defineProps<{
  /** 要编辑的应用 id；弹窗打开且 id 有效时拉取详情 */
  appId: number | null
}>()

const emit = defineEmits<{
  /** 保存成功后通知父级刷新列表 */
  success: []
}>()

const userStore = useUserStore()
const { loginUser } = storeToRefs(userStore)

const loading = ref(false)
const submitting = ref(false)
const isAdmin = computed(() => loginUser.value?.userRole === 'admin')

const appName = ref('')
const cover = ref('')
const priority = ref<number | null>(null)
const editingId = ref(0)
const ownerUserId = ref<number | null>(null)

function resetForm() {
  editingId.value = 0
  ownerUserId.value = null
  appName.value = ''
  cover.value = ''
  priority.value = null
}

function close() {
  open.value = false
}

async function load(id: number) {
  loading.value = true
  resetForm()
  editingId.value = id
  try {
    await userStore.fetchLoginUser()

    if (isAdmin.value) {
      const res = await getAppByIdForAdmin({ id })
      const { code, data, message: msg } = res.data
      if (code !== 0 || !data) {
        message.error(msg || '加载失败')
        close()
        return
      }
      ownerUserId.value = data.userId ?? null
      appName.value = data.appName ?? ''
      cover.value = data.cover ?? ''
      priority.value = data.priority ?? 0
      return
    }

    const res = await getAppVOById({ id })
    const { code, data, message: msg } = res.data
    if (code !== 0 || !data) {
      message.error(msg || '加载失败')
      close()
      return
    }
    ownerUserId.value = data.userId ?? null
    if (data.userId !== loginUser.value?.id) {
      message.warning('只能编辑自己的应用')
      close()
      return
    }
    appName.value = data.appName ?? ''
    cover.value = data.cover ?? ''
    priority.value = data.priority ?? 0
  } finally {
    loading.value = false
  }
}

watch(
  () => [open.value, props.appId] as const,
  ([isOpen, id]) => {
    if (isOpen && id != null && id > 0) {
      void load(id)
    }
    if (!isOpen) {
      resetForm()
    }
  },
)

async function onSubmit() {
  const name = appName.value.trim()
  if (!name) {
    message.warning('应用名称不能为空')
    return
  }
  if (!editingId.value) return
  submitting.value = true
  try {
    if (isAdmin.value) {
      const res = await updateAppByAdmin({
        id: editingId.value,
        appName: name,
        cover: cover.value.trim() || undefined,
        priority: priority.value ?? undefined,
      })
      const { code, message: msg } = res.data
      if (code !== 0) {
        message.error(msg || '保存失败')
        return
      }
    } else {
      const res = await updateAppByUser({
        id: editingId.value,
        appName: name,
      })
      const { code, message: msg } = res.data
      if (code !== 0) {
        message.error(msg || '保存失败')
        return
      }
    }
    message.success('已保存')
    emit('success')
    close()
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <a-modal
    v-model:open="open"
    title="修改应用信息"
    width="520px"
    :footer="null"
    destroy-on-close
    wrap-class-name="app-edit-modal"
  >
    <a-spin :spinning="loading">
      <a-form layout="vertical" class="edit-form" @submit.prevent="onSubmit">
        <a-form-item label="应用名称" required>
          <a-input v-model:value="appName" placeholder="应用名称" allow-clear />
        </a-form-item>
        <template v-if="isAdmin">
          <a-form-item label="封面 URL">
            <a-input v-model:value="cover" placeholder="可选，封面图片地址" allow-clear />
          </a-form-item>
          <a-form-item label="优先级">
            <a-input-number v-model:value="priority" :min="0" :max="999" style="width: 100%" />
            <div class="edit-hint">大于 0 会在精选列表中展示；设为 99 可作为高优先级精选。</div>
          </a-form-item>
          <a-form-item v-if="ownerUserId != null" label="所有者用户 ID">
            <a-input :value="String(ownerUserId)" disabled />
          </a-form-item>
        </template>
        <a-form-item class="edit-actions">
          <a-space>
            <a-button @click="close">取消</a-button>
            <a-button type="primary" html-type="submit" :loading="submitting">保存</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-spin>
  </a-modal>
</template>

<style scoped>
.edit-form :deep(.ant-input),
.edit-form :deep(.ant-input-number) {
  border-radius: 12px;
}

.edit-hint {
  margin-top: 6px;
  color: var(--ds-text-muted);
  font-size: 13px;
}

.edit-actions {
  margin-bottom: 0;
}
</style>
