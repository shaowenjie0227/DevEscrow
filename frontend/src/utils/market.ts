const DEFAULT_DEMAND_COVER =
  'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=900&q=80'

function toText(value: unknown, fallback = '') {
  if (value == null) return fallback
  const normalized = String(value).trim()
  return normalized || fallback
}

function getOrderTypeLabel(orderType: number) {
  return orderType === 2 ? '文档单' : '开发单'
}

function getDeliveryTypeLabel(deliveryType: number) {
  return deliveryType === 2 ? '分阶段交付' : '整单交付'
}

function getUrgentLabel(isUrgent: boolean, urgentBonus: number) {
  if (!isUrgent) return '常规节奏'
  if (urgentBonus > 0) return `加急 +${urgentBonus.toLocaleString()}`
  return '加急需求'
}

export function formatBudget(min: unknown, max: unknown) {
  const minValue = Number(min || 0)
  const maxValue = Number(max || 0)

  if (!minValue && !maxValue) return '预算待沟通'
  if (!maxValue || minValue === maxValue) return `￥${minValue.toLocaleString()}`
  return `￥${minValue.toLocaleString()} - ￥${maxValue.toLocaleString()}`
}

export function normalizeMarketDemand(item: any) {
  const deliverables = Array.isArray(item?.stagePlans)
    ? item.stagePlans
        .map((stage: any) => toText(stage?.stageName || stage?.stageDesc))
        .filter(Boolean)
    : []

  const category = toText(item?.category, '未分类')
  const title = toText(item?.title, '未命名需求')
  const summary = toText(item?.summary || item?.detail, '暂无简介')
  const detail = toText(item?.detail || item?.summary, summary)
  const budgetMin = Number(item?.budgetMin || 0)
  const budgetMax = Number(item?.budgetMax || 0)
  const expectedDays = Number(item?.expectedDays || 0)
  const deliveryType = Number(item?.deliveryType || 1)
  const orderType = Number(item?.orderType || 1)
  const isUrgent = Boolean(item?.isUrgent)
  const urgentBonus = Number(item?.urgentBonus || 0)
  const demandNo = toText(item?.demandNo, `NO.${String(item?.id || '').padStart(3, '0')}`)
  const categoryId = item?.categoryId == null ? null : String(item.categoryId)
  const deliveryLabel = getDeliveryTypeLabel(deliveryType)
  const orderTypeLabel = getOrderTypeLabel(orderType)
  const urgentLabel = getUrgentLabel(isUrgent, urgentBonus)

  return {
    id: Number(item?.id || 0),
    demandNo,
    categoryId,
    category,
    title,
    summary,
    detail,
    coverImage: toText(item?.coverImage, DEFAULT_DEMAND_COVER),
    budget: formatBudget(budgetMin, budgetMax),
    budgetMin,
    budgetMax,
    deadline: expectedDays > 0 ? `${expectedDays} 天内` : '工期待沟通',
    expectedDays,
    location: '线上协作',
    publisher: demandNo,
    postedAt: '最新',
    tags: [
      { label: category },
      { label: deliveryLabel },
      { label: orderTypeLabel }
    ],
    highlights: [
      '已审核公开',
      deliveryLabel,
      orderTypeLabel,
      urgentLabel,
      expectedDays > 0 ? `${expectedDays} 天交付预期` : '工期可协商'
    ],
    deliverables: deliverables.length ? deliverables : ['交付范围可沟通拆分'],
    contactHint: isUrgent ? '可优先沟通' : '站内快速沟通',
    orderType,
    isUrgent,
    urgentBonus,
    deliveryType,
    reviewStatus: Number(item?.reviewStatus || 0),
    status: Number(item?.status || 0),
    searchText: [
      title,
      category,
      summary,
      detail,
      demandNo,
      deliveryLabel,
      orderTypeLabel,
      urgentLabel,
      ...deliverables
    ]
      .join(' ')
      .toLowerCase()
  }
}
