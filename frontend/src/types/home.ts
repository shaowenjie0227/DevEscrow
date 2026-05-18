export interface TechCategoryItem {
  label: string
  count: string
}

export interface HeroMetric {
  label: string
  value: string
  hint: string
}

export interface StatItem {
  label: string
  value: string
  note: string
}

export interface HomeNoticeItem {
  id: number
  noticeType?: number
  typeLabel?: string
  title: string
  summary: string
  targetUrl?: string
  coverUrl?: string
  isPinned?: number
}

export interface HomeAdminContact {
  title: string
  message: string
  qrImageUrl?: string
}

export interface HomeInsightItem {
  label: string
  value: string
  note: string
}

export interface HomeOverviewPayload {
  adminContact?: HomeAdminContact
  heroMetrics?: HeroMetric[]
  stats?: StatItem[]
  marketSignals?: HomeInsightItem[]
  tradingRules?: string[]
}

export interface DemandOrderTag {
  label: string
  type?: 'warning' | 'success' | 'info' | 'danger'
}

export interface DemandOrderItem {
  id: number
  demandNo?: string
  title: string
  category: string
  summary: string
  detail: string
  coverImage: string
  budgetMin?: number
  budgetMax?: number
  budget: string
  deadline: string
  location: string
  publisher: string
  postedAt: string
  tags: DemandOrderTag[]
  highlights: string[]
  deliverables: string[]
  contactHint: string
  orderType?: number
  isUrgent?: boolean
  urgentBonus?: number
  deliveryType?: number
  expectedDays?: number
  reviewStatus?: number
  status?: number
}
