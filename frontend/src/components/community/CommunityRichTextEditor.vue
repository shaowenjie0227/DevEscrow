<script setup lang="ts">
import { computed } from 'vue'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'

const props = withDefaults(
  defineProps<{
    modelValue?: string
    placeholder?: string
  }>(),
  {
    modelValue: '',
    placeholder: '写清楚背景、目标、已尝试方案和你希望得到的帮助'
  }
)

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const editorValue = computed({
  get: () => props.modelValue,
  set: (value: string) => emit('update:modelValue', value)
})

const toolbar = [
  [{ header: [1, 2, 3, false] }],
  ['bold', 'italic', 'underline', 'strike'],
  [{ list: 'ordered' }, { list: 'bullet' }],
  ['blockquote', 'code-block'],
  ['link', 'clean']
]
</script>

<template>
  <div class="community-editor">
    <div class="community-editor__head">
      <span>技术贴支持标题、列表、引用、链接和代码块</span>
    </div>
    <QuillEditor
      v-model:content="editorValue"
      content-type="html"
      theme="snow"
      :toolbar="toolbar"
      :placeholder="placeholder"
      class="community-editor__surface"
    />
  </div>
</template>

<style scoped>
.community-editor {
  border: 1px solid rgba(17, 19, 34, 0.08);
  border-radius: 24px;
  background:
    radial-gradient(circle at top right, rgba(243, 190, 37, 0.14), transparent 28%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(255, 255, 255, 0.9));
  overflow: hidden;
}

.community-editor__head {
  padding: 12px 16px;
  border-bottom: 1px solid rgba(17, 19, 34, 0.08);
  color: rgba(17, 19, 34, 0.56);
  font-size: 12px;
  line-height: 1.6;
}

.community-editor__surface {
  min-height: 320px;
}

.community-editor :deep(.ql-toolbar.ql-snow) {
  border: 0;
  border-bottom: 1px solid rgba(17, 19, 34, 0.08);
  background: rgba(255, 255, 255, 0.72);
  padding: 10px 12px;
}

.community-editor :deep(.ql-container.ql-snow) {
  border: 0;
  font-family: inherit;
}

.community-editor :deep(.ql-editor) {
  min-height: 260px;
  padding: 20px 22px;
  color: #111322;
  font-size: 15px;
  line-height: 1.85;
}

.community-editor :deep(.ql-editor.ql-blank::before) {
  left: 22px;
  right: 22px;
  color: rgba(17, 19, 34, 0.36);
  font-style: normal;
}

.community-editor :deep(.ql-editor blockquote) {
  border-left: 3px solid rgba(243, 190, 37, 0.58);
  margin: 10px 0;
  padding-left: 14px;
  color: rgba(17, 19, 34, 0.72);
}

.community-editor :deep(.ql-editor pre) {
  border-radius: 18px;
  background: #111322;
  color: #f7f5ef;
  padding: 16px;
}
</style>
