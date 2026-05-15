<template>
  <section class="page-card">
    <div class="toolbar">
      <div>
        <h2>需求审核</h2>
        <p>先核对需求内容，再决定是否进入报价阶段；已处理记录支持回看审核结果。</p>
      </div>

      <div class="toolbar-actions">
        <el-button @click="loadDemands">刷新</el-button>
      </div>
    </div>

    <el-table :data="demands" v-loading="loading" empty-text="暂无需求记录">
      <el-table-column prop="demandNo" label="需求编号" min-width="190" />
      <el-table-column prop="title" label="需求标题" min-width="220" />
      <el-table-column prop="category" label="分类" width="150" />
      <el-table-column label="预算区间" width="180">
        <template #default="{ row }">
          <span>{{ formatBudget(row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="审核状态" width="140">
        <template #default="{ row }">
          <el-tag :type="getDemandReviewStatus(row.reviewStatus).type">
            {{ getDemandReviewStatus(row.reviewStatus).label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="业务状态" width="150">
        <template #default="{ row }">
          <el-tag :type="getDemandStatus(row.status).type">
            {{ getDemandStatus(row.status).label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="170" fixed="right">
        <template #default="{ row }">
          <div class="row-actions">
            <el-button link type="primary" @click="openReview(row)">
              {{ canAudit(row) ? '去审核' : '查看详情' }}
            </el-button>
            <span v-if="!canAudit(row)" class="row-actions__hint">
              {{ row.reviewStatus === 2 ? '可查看原因' : '已审核' }}
            </span>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="reviewVisible"
      append-to-body
      class="review-dialog"
      width="880px"
      :title="currentDemandCanAudit ? '需求审核' : '需求详情'"
    >
      <template v-if="currentDemand">
        <div class="review-dialog__body">
          <div class="review-grid">
            <article class="review-item">
              <span>需求编号</span>
              <strong>{{ currentDemand.demandNo }}</strong>
            </article>
            <article class="review-item">
              <span>需求标题</span>
              <strong>{{ currentDemand.title }}</strong>
            </article>
            <article class="review-item">
              <span>分类</span>
              <strong>{{ currentDemand.category || '-' }}</strong>
            </article>
            <article class="review-item">
              <span>预算区间</span>
              <strong>{{ formatBudget(currentDemand) }}</strong>
            </article>
            <article class="review-item">
              <span>预计工期</span>
              <strong>{{ formatExpectedDays(currentDemand.expectedDays) }}</strong>
            </article>
            <article class="review-item">
              <span>交付方式</span>
              <strong>{{ getDeliveryTypeLabel(currentDemand.deliveryType) }}</strong>
            </article>
            <article class="review-item">
              <span>审核状态</span>
              <el-tag :type="getDemandReviewStatus(currentDemand.reviewStatus).type">
                {{ getDemandReviewStatus(currentDemand.reviewStatus).label }}
              </el-tag>
            </article>
            <article class="review-item">
              <span>业务状态</span>
              <el-tag :type="getDemandStatus(currentDemand.status).type">
                {{ getDemandStatus(currentDemand.status).label }}
              </el-tag>
            </article>
            <article class="review-item">
              <span>收到报价</span>
              <strong>{{ currentDemand.quoteCount ?? 0 }} 份</strong>
            </article>
          </div>

          <section class="review-card">
            <div class="review-card__head">
              <strong>需求摘要</strong>
              <span>{{ currentDemand.summary ? '已填写' : '未填写' }}</span>
            </div>
            <p>{{ currentDemand.summary || '暂无摘要说明。' }}</p>
          </section>

          <section class="review-card">
            <div class="review-card__head">
              <strong>需求详情</strong>
              <span>{{ currentDemand.stagePlans?.length ? `${currentDemand.stagePlans.length} 个阶段` : '单阶段交付' }}</span>
            </div>
            <p class="review-card__detail">{{ currentDemand.detail || '暂无详细描述。' }}</p>

            <div v-if="currentDemand.stagePlans?.length" class="stage-list">
              <article v-for="(item, index) in currentDemand.stagePlans" :key="`${index}-${item.stageDesc}`" class="stage-item">
                <strong>阶段 {{ index + 1 }} · {{ item.stageDesc }}</strong>
                <span>{{ formatCurrency(item.stageAmount) }}</span>
              </article>
            </div>
          </section>

          <div class="review-meta">
            <span>{{ currentDemand.images?.length || 0 }} 张图片</span>
            <span>{{ currentDemand.attachments?.length || 0 }} 个附件</span>
            <span>{{ currentDemand.quoteCount ?? 0 }} 份报价</span>
          </div>

          <section v-if="!currentDemandCanAudit" class="audit-result" :class="{ 'is-rejected': currentDemand.reviewStatus === 2 }">
            <strong>{{ currentDemand.reviewStatus === 2 ? '本次审核已驳回' : '本次审核已通过' }}</strong>
            <p>
              {{
                currentDemand.reviewStatus === 2
                  ? (currentDemand.rejectReason || '未记录驳回原因。')
                  : '该需求已进入后续业务流程，无需重复审核。'
              }}
            </p>
          </section>

          <section v-else class="audit-form">
            <div class="audit-form__head">
              <strong>审核备注</strong>
              <span>驳回时必须写清修改要求；通过时可留空，系统会补默认备注。</span>
            </div>
            <el-input
              v-model="auditRemark"
              type="textarea"
              :rows="4"
              placeholder="例如：需求描述完整，可进入报价阶段；若驳回，请明确指出需补充的资料或修改项。"
            />
          </section>
        </div>
      </template>

      <template #footer>
        <div class="review-dialog__footer">
          <el-button @click="reviewVisible = false">关闭</el-button>
          <template v-if="currentDemandCanAudit">
            <el-button type="danger" plain :loading="auditing" @click="submitAudit('reject')">驳回需求</el-button>
            <el-button type="primary" :loading="auditing" @click="submitAudit('approve')">审核通过</el-button>
          </template>
        </div>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { approveDemand, fetchAdminDemands, rejectDemand } from '@/api/modules/admin'
import { getDemandReviewStatus, getDemandStatus } from '@/utils/status'

const loading = ref(false)
const auditing = ref(false)
const reviewVisible = ref(false)
const demands = ref([])
const currentDemand = ref(null)
const auditRemark = ref('')

const currentDemandCanAudit = computed(() => canAudit(currentDemand.value))

function canAudit(row) {
  return Number(row?.reviewStatus) === 0
}

async function loadDemands() {
  loading.value = true
  try {
    const response = await fetchAdminDemands()
    const list = response.data || []
    demands.value = [...list].sort((left, right) => Number(left.reviewStatus !== 0) - Number(right.reviewStatus !== 0))
  } catch (error) {
    ElMessage.error(error.message || '加载需求列表失败')
  } finally {
    loading.value = false
  }
}

function openReview(row) {
  currentDemand.value = row
  auditRemark.value = ''
  reviewVisible.value = true
}

async function submitAudit(action) {
  if (!currentDemand.value?.id || !currentDemandCanAudit.value) {
    return
  }

  const remark = action === 'approve'
    ? (auditRemark.value.trim() || '审核通过，允许进入报价阶段。')
    : auditRemark.value.trim()

  if (action === 'reject' && !remark) {
    ElMessage.warning('驳回需求时请填写审核原因')
    return
  }

  const actionText = action === 'approve' ? '通过' : '驳回'

  try {
    await ElMessageBox.confirm(
      `确认${actionText}需求 ${currentDemand.value.demandNo} 吗？`,
      '审核确认',
      {
        type: action === 'approve' ? 'success' : 'warning',
        confirmButtonText: action === 'approve' ? '确认通过' : '确认驳回',
        cancelButtonText: '取消'
      }
    )
  } catch {
    return
  }

  auditing.value = true
  try {
    if (action === 'approve') {
      await approveDemand(currentDemand.value.id, { remark })
    } else {
      await rejectDemand(currentDemand.value.id, { remark })
    }
    ElMessage.success(action === 'approve' ? '需求已审核通过' : '需求已驳回')
    reviewVisible.value = false
    await loadDemands()
  } catch (error) {
    ElMessage.error(error.message || '审核操作失败')
  } finally {
    auditing.value = false
  }
}

function formatBudget(demand) {
  const minValue = Number(demand?.budgetMin || 0)
  const maxValue = Number(demand?.budgetMax || 0)
  if (!minValue && !maxValue) {
    return '待补充'
  }
  if (!maxValue || minValue === maxValue) {
    return `￥${minValue.toLocaleString()}`
  }
  return `￥${minValue.toLocaleString()} - ￥${maxValue.toLocaleString()}`
}

function formatExpectedDays(days) {
  if (!days) {
    return '待补充'
  }
  return `${days} 天`
}

function getDeliveryTypeLabel(type) {
  return type === 2 ? '分阶段交付' : '一次性交付'
}

function formatCurrency(value) {
  const amount = Number(value || 0)
  return `￥${amount.toLocaleString()}`
}

onMounted(loadDemands)
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.toolbar p {
  margin: 6px 0 0;
  color: #64748b;
}

.toolbar-actions {
  display: flex;
  gap: 12px;
}

.row-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
}

.row-actions__hint {
  font-size: 12px;
  color: #94a3b8;
}

.review-dialog__body {
  display: grid;
  gap: 18px;
}

.review-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.review-item {
  display: grid;
  gap: 6px;
  min-height: 86px;
  padding: 14px 16px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 16px;
  background: #f8fafc;
}

.review-item span {
  font-size: 12px;
  color: #64748b;
}

.review-item strong {
  font-size: 15px;
  color: #0f172a;
  line-height: 1.5;
}

.review-card {
  display: grid;
  gap: 12px;
  padding: 18px 20px;
  border: 1px solid rgba(148, 163, 184, 0.16);
  border-radius: 18px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(248, 250, 252, 0.94));
}

.review-card__head,
.audit-form__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.review-card__head span,
.audit-form__head span {
  font-size: 12px;
  color: #64748b;
}

.review-card p,
.audit-result p {
  margin: 0;
  color: #334155;
  line-height: 1.75;
}

.review-card__detail {
  white-space: pre-wrap;
}

.stage-list {
  display: grid;
  gap: 10px;
}

.stage-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(248, 250, 252, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.14);
}

.stage-item strong {
  color: #0f172a;
  font-size: 14px;
}

.stage-item span {
  color: var(--brand-primary);
  font-weight: 700;
}

.review-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.review-meta span {
  display: inline-flex;
  align-items: center;
  min-height: 34px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.05);
  color: #475569;
  font-size: 12px;
  font-weight: 600;
}

.audit-result,
.audit-form {
  display: grid;
  gap: 10px;
  padding: 18px 20px;
  border-radius: 18px;
  border: 1px solid rgba(148, 163, 184, 0.16);
}

.audit-result {
  background: rgba(236, 253, 245, 0.72);
  border-color: rgba(34, 197, 94, 0.18);
}

.audit-result.is-rejected {
  background: rgba(254, 242, 242, 0.84);
  border-color: rgba(239, 68, 68, 0.16);
}

.audit-result strong,
.audit-form__head strong {
  color: #0f172a;
}

.audit-form {
  background: rgba(248, 250, 252, 0.82);
}

.review-dialog__footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 900px) {
  .review-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .toolbar-actions {
    justify-content: flex-end;
  }

  .review-grid {
    grid-template-columns: 1fr;
  }

  .review-card__head,
  .audit-form__head,
  .stage-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .review-dialog__footer {
    flex-wrap: wrap;
  }
}
</style>
