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

export interface DemandOrderTag {
  label: string
  type?: 'warning' | 'success' | 'info' | 'danger'
}

export interface DemandOrderItem {
  id: number
  title: string
  category: string
  summary: string
  detail: string
  coverImage: string
  budget: string
  deadline: string
  location: string
  publisher: string
  postedAt: string
  tags: DemandOrderTag[]
  highlights: string[]
  deliverables: string[]
  contactHint: string
}
