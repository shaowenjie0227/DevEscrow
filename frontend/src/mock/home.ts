import type { DemandOrderItem, HeroMetric, StatItem, TechCategoryItem } from '@/types/home'

export const techCategories: TechCategoryItem[] = [
  { label: '全部需求', count: '218' },
  { label: 'Java', count: '63' },
  { label: 'Vue 3', count: '41' },
  { label: 'Spring Boot', count: '38' },
  { label: '小程序', count: '34' },
  { label: 'UniApp', count: '19' },
  { label: 'AI 应用', count: '27' },
  { label: '管理后台', count: '52' },
  { label: '官网落地页', count: '15' },
  { label: '接盘续做', count: '11' }
]

export const heroMetrics: HeroMetric[] = [
  {
    label: '今日新增需求',
    value: '128',
    hint: 'Demo 验证、企业后台和微信生态需求仍是高频。'
  },
  {
    label: '等待响应中',
    value: '43',
    hint: '多数需求会在 10 分钟内收到第一条沟通消息。'
  },
  {
    label: '本周高频组合',
    value: 'Vue 3 + Spring Boot',
    hint: 'SaaS MVP、运营后台和接盘改造最常见。'
  }
]

export const statsBar: StatItem[] = [
  {
    label: '累计公开需求',
    value: '12000+',
    note: '从官网到企业系统，需求描述越来越具体，报价效率也更高。'
  },
  {
    label: '认证开发者',
    value: '5000+',
    note: '覆盖前后端、小程序、AI、运维与数据方向。'
  },
  {
    label: '高意向响应率',
    value: '98%',
    note: '信息完整的需求，通常当天就能开始沟通。'
  }
]

export const demandOrders: DemandOrderItem[] = [
  {
    id: 1,
    title: '做一个 Vue 3 + Spring Boot 的设备巡检后台 Demo',
    category: '管理后台 / 企业系统',
    summary: '需要先做一版可演示的巡检台账、工单流转和报表首页，用于下周给客户看。',
    detail:
      '现有原型和字段文档已经整理好，希望开发者先做出巡检首页、点位管理、工单列表和基础权限。优先要求是可演示、风格清爽，接口可以先 mock，第二阶段再接真实后端。',
    coverImage:
      'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=900&q=80',
    budget: '¥ 8,000 - ¥ 12,000',
    deadline: '7 天内交首版',
    location: '上海',
    publisher: '某工业 SaaS 团队',
    postedAt: '15 分钟前',
    tags: [
      { label: 'Vue 3', type: 'success' },
      { label: 'Spring Boot' },
      { label: '管理后台', type: 'warning' },
      { label: 'Demo 急单', type: 'danger' }
    ],
    highlights: ['已有原型', '接口可先 mock', '适合全栈接单'],
    deliverables: ['PC 演示后台', '角色权限骨架', '可演示数据看板'],
    contactHint: '希望今晚先语音确认范围'
  },
  {
    id: 2,
    title: '微信小程序预约下单 MVP，先跑通核心流程',
    category: '小程序 / 本地生活',
    summary: '做家政预约下单和订单跟踪，先不接支付，重点是预约流程和状态流转。',
    detail:
      '用户侧需要首页、服务选择、时间选择、预约提交和订单查询。后台只要简单的订单看板和状态更新即可。我们想先验证业务，有小程序交付经验的开发者优先。',
    coverImage:
      'https://images.unsplash.com/photo-1556740749-887f6717d7e4?auto=format&fit=crop&w=900&q=80',
    budget: '¥ 6,000 - ¥ 9,500',
    deadline: '10 天内上线测试',
    location: '深圳',
    publisher: '本地生活创业项目',
    postedAt: '28 分钟前',
    tags: [
      { label: '小程序', type: 'success' },
      { label: '预约系统', type: 'warning' },
      { label: 'MVP', type: 'info' },
      { label: '轻后台' }
    ],
    highlights: ['先不做支付', '流程优先', '后续可长期合作'],
    deliverables: ['用户预约小程序', '订单状态页', '轻量后台'],
    contactHint: '可以先看你做过的小程序案例'
  },
  {
    id: 3,
    title: 'AI 文档问答 Demo，用于企业知识库场景演示',
    category: 'AI 应用 / Python',
    summary: '需要一个上传文档后可问答的 Demo，重点给客户展示检索质量与引用来源。',
    detail:
      '需求包含文档上传、知识库列表、会话问答和答案引用来源展示。优先考虑 Python + FastAPI，界面不需要太重，核心是问答链路可跑通、演示可信。',
    coverImage:
      'https://images.unsplash.com/photo-1677442136019-21780ecad995?auto=format&fit=crop&w=900&q=80',
    budget: '¥ 10,000 - ¥ 12,000',
    deadline: '2 周内可演示',
    location: '杭州',
    publisher: 'ToB AI 咨询团队',
    postedAt: '42 分钟前',
    tags: [
      { label: 'AI', type: 'danger' },
      { label: 'RAG', type: 'warning' },
      { label: 'FastAPI', type: 'success' },
      { label: '知识库', type: 'info' }
    ],
    highlights: ['客户演示用', '可用开源方案', '重视交互体验'],
    deliverables: ['上传与解析页', '问答界面', '引用来源展示'],
    contactHint: '最好能先给出一版技术路线建议'
  },
  {
    id: 4,
    title: '接手一个半成品 CRM 项目，修 bug 并补 3 个页面',
    category: '接盘续做 / Vue',
    summary: '前任外包留下的代码能跑，但结构很乱，希望有人能快速接手梳理。',
    detail:
      '主要问题是客户列表筛选异常、详情页接口耦合和移动端适配不完整。还需要补一个跟进记录页、任务提醒页和导出功能。希望先 code review 再报价。',
    coverImage:
      'https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?auto=format&fit=crop&w=900&q=80',
    budget: '¥ 15,000 - ¥ 25,000',
    deadline: '按范围评估',
    location: '北京',
    publisher: '教育行业客户',
    postedAt: '1 小时前',
    tags: [
      { label: '接盘', type: 'danger' },
      { label: 'Vue', type: 'success' },
      { label: 'Bug 修复', type: 'warning' },
      { label: 'CRM', type: 'info' }
    ],
    highlights: ['需要先评估', '有现成代码', '适合经验型开发者'],
    deliverables: ['bug 修复清单', '新增 3 个页面', '代码结构梳理说明'],
    contactHint: '优先找能快速接手的老手'
  }
]

export function getDemandOrderById(id: number) {
  return demandOrders.find((item) => item.id === id)
}
