import type { CommunityReplyItem, CommunityReplyNode } from '@/types/community'

export function htmlToPlainText(html = '') {
  if (!html) return ''

  if (typeof window === 'undefined') {
    return html
      .replace(/<[^>]*>/g, ' ')
      .replace(/&nbsp;/g, ' ')
      .replace(/\s+/g, ' ')
      .trim()
  }

  const doc = new window.DOMParser().parseFromString(html, 'text/html')
  return (doc.body.textContent || '').replace(/\u00a0/g, ' ').replace(/\s+/g, ' ').trim()
}

export function buildReplyTree(replies: CommunityReplyItem[]) {
  const nodeMap = new Map<number, CommunityReplyNode>()
  const roots: CommunityReplyNode[] = []

  replies.forEach((reply) => {
    nodeMap.set(reply.id, {
      ...reply,
      children: []
    })
  })

  nodeMap.forEach((node) => {
    if (node.parentReplyId && nodeMap.has(node.parentReplyId)) {
      nodeMap.get(node.parentReplyId)?.children.push(node)
      return
    }
    roots.push(node)
  })

  const sortNodes = (items: CommunityReplyNode[]) => {
    items.sort((left, right) => {
      const leftTime = left.createdAt ? new Date(left.createdAt).getTime() : 0
      const rightTime = right.createdAt ? new Date(right.createdAt).getTime() : 0
      return leftTime - rightTime || left.id - right.id
    })
    items.forEach((item) => sortNodes(item.children))
  }

  sortNodes(roots)
  return roots
}
