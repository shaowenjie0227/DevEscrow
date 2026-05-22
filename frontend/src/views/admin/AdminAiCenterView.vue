<template>
  <section class="page-card">
    <div class="toolbar">
      <div>
        <h2>AI 配置中心</h2>
        <p class="page-subtitle">统一管理运行时 AI 配置、Prompt 模板，以及每次调用的日志。</p>
      </div>
      <div class="toolbar-actions">
        <el-button @click="loadAll">刷新全部</el-button>
        <el-button type="primary" @click="openCreate">新建 Prompt 模板</el-button>
      </div>
    </div>

    <div class="section-card">
      <div class="section-head">
        <div>
          <strong>运行时 AI 配置</strong>
          <p>这里的配置会覆盖本地默认配置。API Key 只会在后端加密保存，页面只展示掩码。</p>
        </div>
      </div>

      <el-form :model="runtimeForm" label-position="top" class="runtime-grid">
        <el-form-item label="启用 AI">
          <el-switch v-model="runtimeForm.enabled" />
        </el-form-item>
        <el-form-item label="允许失败回退">
          <el-switch v-model="runtimeForm.fallbackEnabled" />
        </el-form-item>
        <el-form-item label="Provider">
          <el-input v-model="runtimeForm.provider" placeholder="例如：deepseek" />
        </el-form-item>
        <el-form-item label="Base URL">
          <el-input v-model="runtimeForm.baseUrl" placeholder="例如：https://api.deepseek.com" />
        </el-form-item>
        <el-form-item label="Chat Path">
          <el-input v-model="runtimeForm.chatPath" placeholder="例如：/v1/chat/completions" />
        </el-form-item>
        <el-form-item label="模型">
          <el-input v-model="runtimeForm.model" placeholder="例如：deepseek-v4-flash" />
        </el-form-item>
        <el-form-item label="Temperature">
          <el-input-number v-model="runtimeForm.temperature" :min="0" :max="2" :step="0.1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="TopK">
          <el-input-number v-model="runtimeForm.topK" :min="1" :max="20" style="width: 100%" />
        </el-form-item>
        <el-form-item label="缓存秒数">
          <el-input-number v-model="runtimeForm.cacheTtlSeconds" :min="0" :step="300" style="width: 100%" />
        </el-form-item>
        <el-form-item label="连接超时(ms)">
          <el-input-number v-model="runtimeForm.connectTimeoutMs" :min="1000" :step="1000" style="width: 100%" />
        </el-form-item>
        <el-form-item label="读取超时(ms)">
          <el-input-number v-model="runtimeForm.readTimeoutMs" :min="1000" :step="1000" style="width: 100%" />
        </el-form-item>
        <el-form-item class="runtime-grid__full" label="当前密钥">
          <div class="masked-key-row">
            <span>{{ runtimeForm.maskedApiKey || '未配置' }}</span>
            <el-tag :type="runtimeForm.hasApiKey ? 'success' : 'info'">
              {{ runtimeForm.hasApiKey ? '已保存' : '未保存' }}
            </el-tag>
          </div>
        </el-form-item>
        <el-form-item class="runtime-grid__full" label="更新 API Key">
          <el-input
            v-model="runtimeForm.apiKey"
            show-password
            type="password"
            placeholder="留空表示保留当前密钥，输入新值表示覆盖"
          />
        </el-form-item>
        <el-form-item class="runtime-grid__full">
          <el-checkbox v-model="runtimeForm.clearApiKey">清空已保存的 API Key</el-checkbox>
        </el-form-item>
      </el-form>

      <div class="runtime-actions">
        <el-button type="primary" :loading="runtimeSaving" @click="saveRuntimeConfig">保存运行时配置</el-button>
      </div>
    </div>

    <div class="section-card">
      <div class="section-head">
        <div>
          <strong>Prompt 模板</strong>
          <p>当前先管理需求发布场景，后续可以扩展到客服、知识问答、内容生成等场景。</p>
        </div>
      </div>

      <el-table :data="promptTemplates" v-loading="promptLoading">
        <el-table-column prop="sceneCode" label="场景编码" width="160" />
        <el-table-column prop="sceneName" label="场景名称" width="180" />
        <el-table-column prop="model" label="模型" width="150">
          <template #default="{ row }">
            {{ row.model || '沿用运行时模型' }}
          </template>
        </el-table-column>
        <el-table-column prop="temperature" label="Temperature" width="120" />
        <el-table-column prop="topK" label="TopK" width="100" />
        <el-table-column label="系统 Prompt" min-width="260">
          <template #default="{ row }">
            <div class="preview-text">{{ row.systemPrompt || '未单独配置，使用内置系统 Prompt' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="用户 Prompt 模板" min-width="260">
          <template #default="{ row }">
            <div class="preview-text">{{ row.userPromptTemplate || '未单独配置，使用内置用户 Prompt 模板' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <div class="table-row-actions">
              <el-button size="small" @click="editItem(row)">编辑</el-button>
              <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
                {{ row.status === 1 ? '停用' : '启用' }}
              </el-button>
              <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="section-card">
      <div class="section-head">
        <div>
          <strong>AI 调用日志</strong>
          <p>观察 Prompt 版本、模型、上下文数量、缓存命中率和回退情况。</p>
        </div>
        <div class="toolbar-actions toolbar-actions--wrap">
          <el-select v-model="logQuery.sceneCode" clearable placeholder="按场景筛选" style="width: 180px">
            <el-option label="需求发布 AI 初稿" value="DEMAND_DRAFT" />
          </el-select>
          <el-select v-model="logQuery.status" clearable placeholder="按状态筛选" style="width: 140px">
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="2" />
          </el-select>
          <el-input-number v-model="logQuery.limit" :min="10" :max="200" :step="10" style="width: 140px" />
          <el-button @click="loadLogs">刷新日志</el-button>
        </div>
      </div>

      <el-table :data="logs" v-loading="logLoading">
        <el-table-column prop="sceneCode" label="场景" width="140" />
        <el-table-column prop="promptVersion" label="Prompt 版本" min-width="220" />
        <el-table-column prop="model" label="模型" width="140" />
        <el-table-column label="结果" width="220">
          <template #default="{ row }">
            <div class="log-tag-row">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '成功' : '失败' }}</el-tag>
              <el-tag :type="row.cacheHit === 1 ? 'warning' : 'info'">{{ row.cacheHit === 1 ? '命中缓存' : '实时生成' }}</el-tag>
              <el-tag :type="row.fallbackUsed === 1 ? 'warning' : 'success'">{{ row.fallbackUsed === 1 ? '规则回退' : 'LLM' }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="contextCount" label="上下文数" width="100" />
        <el-table-column prop="latencyMs" label="耗时(ms)" width="110" />
        <el-table-column prop="requestPreview" label="请求摘要" min-width="240">
          <template #default="{ row }">
            <div class="preview-text">{{ row.requestPreview }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="resultPreview" label="结果摘要" min-width="240">
          <template #default="{ row }">
            <div class="preview-text">{{ row.resultPreview || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="errorMessage" label="错误信息" min-width="220">
          <template #default="{ row }">
            <div class="preview-text preview-text--error">{{ row.errorMessage || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="180" />
      </el-table>
    </div>

    <el-dialog
      v-model="dialogVisible"
      append-to-body
      :title="dialogMode === 'create' ? '新建 Prompt 模板' : '编辑 Prompt 模板'"
      width="860px"
    >
      <el-form :model="form" label-position="top">
        <div class="dialog-grid">
          <el-form-item label="场景编码">
            <el-input v-model="form.sceneCode" placeholder="例如：DEMAND_DRAFT" />
          </el-form-item>
          <el-form-item label="场景名称">
            <el-input v-model="form.sceneName" placeholder="例如：需求发布 AI 初稿" />
          </el-form-item>
          <el-form-item label="模型">
            <el-input v-model="form.model" placeholder="留空则沿用运行时模型" />
          </el-form-item>
          <el-form-item label="Temperature">
            <el-input-number v-model="form.temperature" :min="0" :max="2" :step="0.1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="TopK">
            <el-input-number v-model="form.topK" :min="1" :max="20" style="width: 100%" />
          </el-form-item>
        </div>

        <el-alert
          title="可用占位符：{{requirement}}、{{existing_draft}}、{{categories}}、{{references}}、{{rules}}"
          type="info"
          :closable="false"
          class="prompt-placeholder-tip"
        />

        <el-form-item label="系统 Prompt">
          <el-input v-model="form.systemPrompt" type="textarea" :rows="8" placeholder="留空则使用系统内置 Prompt" />
        </el-form-item>
        <el-form-item label="用户 Prompt 模板">
          <el-input
            v-model="form.userPromptTemplate"
            type="textarea"
            :rows="10"
            placeholder="留空则使用系统内置用户 Prompt 模板"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">
          {{ dialogMode === 'create' ? '创建模板' : '保存修改' }}
        </el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createAdminAiPromptTemplate,
  deleteAdminAiPromptTemplate,
  fetchAdminAiCallLogs,
  fetchAdminAiPromptTemplates,
  fetchAdminAiRuntimeConfig,
  toggleAdminAiPromptTemplateStatus,
  updateAdminAiPromptTemplate,
  updateAdminAiRuntimeConfig
} from '@/api/modules/admin'

const promptLoading = ref(false)
const logLoading = ref(false)
const runtimeLoading = ref(false)
const submitting = ref(false)
const runtimeSaving = ref(false)
const promptTemplates = ref([])
const logs = ref([])
const dialogVisible = ref(false)
const dialogMode = ref('create')
const editingId = ref(null)

const logQuery = reactive({
  sceneCode: 'DEMAND_DRAFT',
  status: null,
  limit: 50
})

const runtimeForm = reactive({
  enabled: true,
  provider: 'deepseek',
  baseUrl: 'https://api.deepseek.com',
  chatPath: '/v1/chat/completions',
  apiKey: '',
  clearApiKey: false,
  maskedApiKey: '',
  hasApiKey: false,
  model: 'deepseek-v4-flash',
  temperature: 0.2,
  topK: 6,
  fallbackEnabled: true,
  cacheTtlSeconds: 1800,
  connectTimeoutMs: 3000,
  readTimeoutMs: 20000
})

const form = reactive({
  sceneCode: 'DEMAND_DRAFT',
  sceneName: '需求发布 AI 初稿',
  model: '',
  temperature: 0.2,
  topK: 6,
  systemPrompt: '',
  userPromptTemplate: ''
})

async function loadRuntimeConfig() {
  runtimeLoading.value = true
  try {
    const response = await fetchAdminAiRuntimeConfig()
    const data = response.data || {}
    runtimeForm.enabled = Boolean(data.enabled)
    runtimeForm.provider = data.provider || 'deepseek'
    runtimeForm.baseUrl = data.baseUrl || 'https://api.deepseek.com'
    runtimeForm.chatPath = data.chatPath || '/v1/chat/completions'
    runtimeForm.apiKey = ''
    runtimeForm.clearApiKey = false
    runtimeForm.maskedApiKey = data.maskedApiKey || ''
    runtimeForm.hasApiKey = Boolean(data.hasApiKey)
    runtimeForm.model = data.model || 'deepseek-v4-flash'
    runtimeForm.temperature = Number(data.temperature ?? 0.2)
    runtimeForm.topK = Number(data.topK ?? 6)
    runtimeForm.fallbackEnabled = Boolean(data.fallbackEnabled)
    runtimeForm.cacheTtlSeconds = Number(data.cacheTtlSeconds ?? 1800)
    runtimeForm.connectTimeoutMs = Number(data.connectTimeoutMs ?? 3000)
    runtimeForm.readTimeoutMs = Number(data.readTimeoutMs ?? 20000)
  } catch (error) {
    ElMessage.error(error.message || '加载运行时 AI 配置失败')
  } finally {
    runtimeLoading.value = false
  }
}

async function saveRuntimeConfig() {
  runtimeSaving.value = true
  try {
    const response = await updateAdminAiRuntimeConfig({
      enabled: runtimeForm.enabled,
      provider: runtimeForm.provider,
      baseUrl: runtimeForm.baseUrl,
      chatPath: runtimeForm.chatPath,
      apiKey: runtimeForm.apiKey,
      clearApiKey: runtimeForm.clearApiKey,
      model: runtimeForm.model,
      temperature: Number(runtimeForm.temperature),
      topK: Number(runtimeForm.topK),
      fallbackEnabled: runtimeForm.fallbackEnabled,
      cacheTtlSeconds: Number(runtimeForm.cacheTtlSeconds),
      connectTimeoutMs: Number(runtimeForm.connectTimeoutMs),
      readTimeoutMs: Number(runtimeForm.readTimeoutMs)
    })
    const data = response.data || {}
    runtimeForm.apiKey = ''
    runtimeForm.clearApiKey = false
    runtimeForm.maskedApiKey = data.maskedApiKey || ''
    runtimeForm.hasApiKey = Boolean(data.hasApiKey)
    ElMessage.success('运行时 AI 配置已保存')
  } catch (error) {
    ElMessage.error(error.message || '保存运行时 AI 配置失败')
  } finally {
    runtimeSaving.value = false
  }
}

async function loadPromptTemplates() {
  promptLoading.value = true
  try {
    const response = await fetchAdminAiPromptTemplates()
    promptTemplates.value = response.data || []
  } catch (error) {
    ElMessage.error(error.message || '加载 Prompt 模板失败')
  } finally {
    promptLoading.value = false
  }
}

async function loadLogs() {
  logLoading.value = true
  try {
    const response = await fetchAdminAiCallLogs({
      sceneCode: logQuery.sceneCode || undefined,
      status: logQuery.status ?? undefined,
      limit: logQuery.limit
    })
    logs.value = response.data || []
  } catch (error) {
    ElMessage.error(error.message || '加载 AI 调用日志失败')
  } finally {
    logLoading.value = false
  }
}

async function loadAll() {
  await Promise.all([loadRuntimeConfig(), loadPromptTemplates(), loadLogs()])
}

function resetForm() {
  form.sceneCode = 'DEMAND_DRAFT'
  form.sceneName = '需求发布 AI 初稿'
  form.model = ''
  form.temperature = 0.2
  form.topK = 6
  form.systemPrompt = ''
  form.userPromptTemplate = ''
  editingId.value = null
}

function openCreate() {
  dialogMode.value = 'create'
  resetForm()
  dialogVisible.value = true
}

function editItem(row) {
  dialogMode.value = 'edit'
  editingId.value = row.id
  form.sceneCode = row.sceneCode
  form.sceneName = row.sceneName
  form.model = row.model || ''
  form.temperature = Number(row.temperature ?? 0.2)
  form.topK = Number(row.topK ?? 6)
  form.systemPrompt = row.systemPrompt || ''
  form.userPromptTemplate = row.userPromptTemplate || ''
  dialogVisible.value = true
}

async function submitForm() {
  submitting.value = true
  try {
    const payload = {
      sceneCode: form.sceneCode,
      sceneName: form.sceneName,
      model: form.model,
      temperature: Number(form.temperature),
      topK: Number(form.topK),
      systemPrompt: form.systemPrompt,
      userPromptTemplate: form.userPromptTemplate
    }
    if (dialogMode.value === 'create') {
      await createAdminAiPromptTemplate(payload)
      ElMessage.success('Prompt 模板创建成功')
    } else {
      await updateAdminAiPromptTemplate(editingId.value, payload)
      ElMessage.success('Prompt 模板保存成功')
    }
    dialogVisible.value = false
    await loadPromptTemplates()
  } catch (error) {
    ElMessage.error(error.message || '保存 Prompt 模板失败')
  } finally {
    submitting.value = false
  }
}

async function toggleStatus(row) {
  try {
    await toggleAdminAiPromptTemplateStatus(row.id, { status: row.status === 1 ? 2 : 1 })
    ElMessage.success('模板状态已更新')
    await loadPromptTemplates()
  } catch (error) {
    ElMessage.error(error.message || '更新模板状态失败')
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除 Prompt 模板“${row.sceneName}”吗？删除后无法恢复。`, '删除 Prompt 模板', {
      type: 'warning'
    })
    await deleteAdminAiPromptTemplate(row.id)
    ElMessage.success('Prompt 模板已删除')
    await loadPromptTemplates()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除 Prompt 模板失败')
    }
  }
}

onMounted(loadAll)
</script>

<style scoped>
.page-subtitle {
  margin: 8px 0 0;
  color: #64748b;
  line-height: 1.7;
}

.section-card + .section-card {
  margin-top: 24px;
}

.toolbar-actions--wrap {
  flex-wrap: wrap;
}

.runtime-grid,
.dialog-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.runtime-grid__full {
  grid-column: 1 / -1;
}

.runtime-actions {
  margin-top: 12px;
}

.masked-key-row {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 40px;
  color: #0f172a;
}

.preview-text {
  white-space: pre-wrap;
  line-height: 1.6;
  color: #475569;
}

.preview-text--error {
  color: #b91c1c;
}

.log-tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.prompt-placeholder-tip {
  margin-bottom: 16px;
}

@media (max-width: 900px) {
  .runtime-grid,
  .dialog-grid {
    grid-template-columns: 1fr;
  }
}
</style>
