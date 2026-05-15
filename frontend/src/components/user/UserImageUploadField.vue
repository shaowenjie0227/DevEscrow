<template>
  <div class="image-upload-field">
    <div v-if="modelValue" class="image-upload-field__preview">
      <img :src="modelValue" alt="uploaded preview" />
    </div>
    <div v-else class="image-upload-field__empty">上传后会在这里显示图片预览</div>

    <div class="image-upload-field__actions">
      <el-upload
        :show-file-list="false"
        :http-request="handleUpload"
        accept="image/*"
        class="image-upload-field__upload"
      >
        <el-button type="primary" :loading="uploading">上传图片</el-button>
      </el-upload>
      <el-button v-if="modelValue" @click="emit('update:modelValue', '')">移除图片</el-button>
    </div>
    <p class="image-upload-field__hint">{{ hint }}</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadUserImage } from '@/api/modules/auth'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  hint: {
    type: String,
    default: '支持 JPG、PNG、WebP，单张不超过 5MB。'
  }
})

const emit = defineEmits(['update:modelValue'])
const uploading = ref(false)

async function handleUpload(options) {
  const file = options.file
  if (!file?.type?.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 5MB')
    return
  }
  uploading.value = true
  try {
    const response = await uploadUserImage(file)
    emit('update:modelValue', response.data?.url || '')
    ElMessage.success('图片上传成功')
    options.onSuccess?.(response)
  } catch (error) {
    ElMessage.error(error.message || '图片上传失败')
    options.onError?.(error)
  } finally {
    uploading.value = false
  }
}
</script>

<style scoped>
.image-upload-field {
  display: grid;
  gap: 12px;
}

.image-upload-field__preview {
  overflow: hidden;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 18px;
  background: #f8fafc;
}

.image-upload-field__preview img {
  display: block;
  width: 100%;
  max-height: 220px;
  object-fit: cover;
}

.image-upload-field__empty {
  display: grid;
  place-items: center;
  min-height: 160px;
  border: 1px dashed rgba(148, 163, 184, 0.5);
  border-radius: 18px;
  color: rgba(71, 85, 105, 0.92);
  background: #f8fafc;
}

.image-upload-field__actions {
  display: flex;
  gap: 12px;
}

.image-upload-field__hint {
  margin: 0;
  color: rgba(71, 85, 105, 0.88);
  font-size: 13px;
}
</style>
