<script setup lang="ts">
import { computed } from 'vue'
import { renderAssistantMarkdown } from '@/utils/markdownRender'

const props = defineProps<{
  content: string
}>()

const html = computed(() => renderAssistantMarkdown(props.content))
</script>

<template>
  <div class="chat-md" v-html="html" />
</template>

<style scoped>
.chat-md {
  font-size: 14px;
  line-height: 1.65;
  color: #1a1a1a;
  word-break: break-word;
}

.chat-md :deep(h1),
.chat-md :deep(h2),
.chat-md :deep(h3),
.chat-md :deep(h4) {
  margin: 1.1em 0 0.45em;
  font-weight: 700;
  line-height: 1.3;
  letter-spacing: -0.02em;
}

.chat-md :deep(h1) {
  font-size: 1.35em;
}
.chat-md :deep(h2) {
  font-size: 1.2em;
}
.chat-md :deep(h3) {
  font-size: 1.08em;
}

.chat-md :deep(p) {
  margin: 0.55em 0;
}

.chat-md :deep(p:first-child) {
  margin-top: 0;
}

.chat-md :deep(p:last-child) {
  margin-bottom: 0;
}

.chat-md :deep(ul),
.chat-md :deep(ol) {
  margin: 0.5em 0;
  padding-left: 1.35em;
}

.chat-md :deep(li) {
  margin: 0.25em 0;
}

.chat-md :deep(blockquote) {
  margin: 0.65em 0;
  padding: 0.35em 0 0.35em 0.85em;
  border-left: 3px solid rgba(255, 107, 0, 0.55);
  background: rgba(0, 0, 0, 0.03);
  border-radius: 0 8px 8px 0;
  color: rgba(0, 0, 0, 0.72);
}

.chat-md :deep(a) {
  color: #ff6b00;
  font-weight: 600;
  text-decoration: none;
}

.chat-md :deep(a:hover) {
  text-decoration: underline;
}

.chat-md :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 0.75em 0;
  font-size: 13px;
}

.chat-md :deep(th),
.chat-md :deep(td) {
  border: 1px solid rgba(0, 0, 0, 0.1);
  padding: 6px 10px;
  text-align: left;
}

.chat-md :deep(th) {
  background: rgba(0, 0, 0, 0.04);
  font-weight: 600;
}

.chat-md :deep(hr) {
  border: none;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
  margin: 1em 0;
}

.chat-md :deep(code) {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
  font-size: 0.9em;
}

.chat-md :deep(p code),
.chat-md :deep(li code),
.chat-md :deep(td code) {
  padding: 0.12em 0.4em;
  border-radius: 6px;
  background: rgba(0, 0, 0, 0.06);
  font-size: 0.88em;
}

.chat-md :deep(.md-fence) {
  margin: 0.65em 0;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(0, 0, 0, 0.08);
  background: #f6f8fa;
}

.chat-md :deep(.md-fence pre) {
  margin: 0;
  padding: 12px 14px;
  overflow: auto;
  max-height: 360px;
  font-size: 12.5px;
  line-height: 1.5;
}

.chat-md :deep(.md-fence code) {
  display: block;
  padding: 0;
  background: transparent !important;
  white-space: pre;
  font-size: 12.5px;
}
</style>
