const orderStatusMap = {
  0: { label: '待报价', type: 'info' },
  1: { label: '已选报价', type: 'warning' },
  2: { label: '已托管', type: 'primary' },
  3: { label: '开发中', type: 'success' },
  4: { label: '待验收', type: 'warning' },
  5: { label: '已完成', type: 'success' },
  6: { label: '已取消', type: 'info' },
  7: { label: '纠纷中', type: 'danger' }
}

const payStatusMap = {
  0: { label: '未支付', type: 'info' },
  1: { label: '已托管', type: 'primary' },
  2: { label: '已放款', type: 'success' },
  3: { label: '已退款', type: 'warning' },
  4: { label: '已分账', type: 'success' }
}

const demandReviewStatusMap = {
  0: { label: '待审核', type: 'warning' },
  1: { label: '已通过', type: 'success' },
  2: { label: '已驳回', type: 'danger' }
}

const demandStatusMap = {
  0: { label: '草稿', type: 'info' },
  1: { label: '待审核', type: 'warning' },
  2: { label: '待报价', type: 'primary' },
  3: { label: '报价中', type: 'success' },
  4: { label: '已选开发者', type: 'warning' },
  5: { label: '进行中', type: 'primary' },
  6: { label: '已完成', type: 'success' },
  7: { label: '已取消', type: 'info' },
  8: { label: '已驳回', type: 'danger' }
}

const quoteStatusMap = {
  0: { label: '有效', type: 'primary' },
  1: { label: '已中标', type: 'success' },
  2: { label: '已落选', type: 'info' },
  3: { label: '已撤回', type: 'warning' },
  4: { label: '已过期', type: 'danger' }
}

const disputeStatusMap = {
  0: { label: '待处理', type: 'warning' },
  1: { label: '处理中', type: 'primary' },
  2: { label: '待举证', type: 'warning' },
  3: { label: '已裁决', type: 'success' },
  4: { label: '已驳回', type: 'danger' },
  5: { label: '已关闭', type: 'info' }
}

const disputeTypeMap = {
  1: { label: '延期', type: 'warning' },
  2: { label: '质量问题', type: 'danger' },
  3: { label: '未交付', type: 'danger' },
  4: { label: '付款争议', type: 'primary' },
  5: { label: '违规沟通', type: 'info' }
}

function resolveStatus(map, code) {
  return map[code] || { label: String(code ?? '-'), type: 'info' }
}

export function getOrderStatus(code) {
  return resolveStatus(orderStatusMap, code)
}

export function getPayStatus(code) {
  return resolveStatus(payStatusMap, code)
}

export function getDemandReviewStatus(code) {
  return resolveStatus(demandReviewStatusMap, code)
}

export function getDemandStatus(code) {
  return resolveStatus(demandStatusMap, code)
}

export function getQuoteStatus(code) {
  return resolveStatus(quoteStatusMap, code)
}

export function getDisputeStatus(code) {
  return resolveStatus(disputeStatusMap, code)
}

export function getDisputeType(code) {
  return resolveStatus(disputeTypeMap, code)
}
