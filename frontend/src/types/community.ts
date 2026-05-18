export type CommunityPostItem = {
  id: number
  creatorId?: number
  title: string
  authorName: string
  forumName: string
  summary: string
  content: string
  replyCount: number
  likeCount: number
  favoriteCount: number
  liked?: boolean
  favorited?: boolean
  status?: number
  createdAt?: string
  updatedAt?: string
}

export type CommunityReplyItem = {
  id: number
  postId: number
  creatorId?: number
  parentReplyId?: number | null
  authorName: string
  replyToAuthorName?: string | null
  content: string
  likeCount?: number
  status?: number
  createdAt?: string
  updatedAt?: string
}

export type CommunityReplyNode = CommunityReplyItem & {
  children: CommunityReplyNode[]
}
