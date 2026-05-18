import { createRouter, createWebHistory } from 'vue-router'
import { useAdminAuthStore } from '@/stores/adminAuth'
import { useAuthStore } from '@/stores/auth'
import { fetchCurrentUser } from '@/api/modules/auth'
import { emitAuthExpired } from '@/utils/authEvents'

const ClientLayout = () => import('@/layouts/ClientLayout.vue')
const DeveloperLayout = () => import('@/layouts/DeveloperLayout.vue')
const AdminLayout = () => import('@/layouts/AdminLayout.vue')
const HomePageView = () => import('@/views/public/HomePageView.vue')
const HomeNoticeDetailView = () => import('@/views/public/HomeNoticeDetailView.vue')
const MarketView = () => import('@/views/public/MarketView.vue')
const PublishEntryView = () => import('@/views/public/PublishEntryView.vue')
const AdminEntryView = () => import('@/views/public/AdminEntryView.vue')
const DemandDetailView = () => import('@/views/public/DemandDetailView.vue')
const PersonalCenterView = () => import('@/views/public/PersonalCenterView.vue')
const ChatCenterView = () => import('@/views/shared/ChatCenterView.vue')
const InboxView = () => import('@/views/shared/InboxView.vue')
const ResourceShareView = () => import('@/views/public/content/ResourceShareView.vue')
const CommunityView = () => import('@/views/public/content/CommunityView.vue')
const CommunityPostDetailView = () => import('@/views/public/content/CommunityPostDetailView.vue')
const RoadmapView = () => import('@/views/public/content/RoadmapView.vue')
const ArticleListView = () => import('@/views/public/content/ArticleListView.vue')
const CourseListView = () => import('@/views/public/content/CourseListView.vue')
const KnowledgeBaseView = () => import('@/views/public/content/KnowledgeBaseView.vue')
const ClientHomeView = () => import('@/views/client/ClientHomeView.vue')
const ClientDemandListView = () => import('@/views/client/ClientDemandListView.vue')
const ClientDemandCreateView = () => import('@/views/client/ClientDemandCreateView.vue')
const ClientOrderListView = () => import('@/views/client/ClientOrderListView.vue')
const ClientDisputeListView = () => import('@/views/client/ClientDisputeListView.vue')
const DeveloperHomeView = () => import('@/views/developer/DeveloperHomeView.vue')
const DeveloperMarketView = () => import('@/views/developer/DeveloperMarketView.vue')
const DeveloperQuoteListView = () => import('@/views/developer/DeveloperQuoteListView.vue')
const DeveloperOrderListView = () => import('@/views/developer/DeveloperOrderListView.vue')
const DeveloperDisputeListView = () => import('@/views/developer/DeveloperDisputeListView.vue')
const DeveloperProfileView = () => import('@/views/developer/DeveloperProfileView.vue')
const AdminLoginView = () => import('@/views/admin/AdminLoginView.vue')
const AdminDashboardView = () => import('@/views/admin/AdminDashboardView.vue')
const BannerManageView = () => import('@/views/admin/BannerManageView.vue')
const ResourceManageView = () => import('@/views/admin/ResourceManageView.vue')
const CommunityManageView = () => import('@/views/admin/CommunityManageView.vue')
const SkillTagManageView = () => import('@/views/admin/SkillTagManageView.vue')
const DemandCategoryManageView = () => import('@/views/admin/DemandCategoryManageView.vue')
const KnowledgeBaseManageView = () => import('@/views/admin/KnowledgeBaseManageView.vue')
const AdminDemandAuditView = () => import('@/views/admin/AdminDemandAuditView.vue')
const AdminOrderListView = () => import('@/views/admin/AdminOrderListView.vue')
const AdminDisputeListView = () => import('@/views/admin/AdminDisputeListView.vue')
const AdminChatAuditView = () => import('@/views/admin/AdminChatAuditView.vue')
const AdminUserListView = () => import('@/views/admin/users/AdminUserListView.vue')

const routes = [
  {
    path: '/',
    component: HomePageView,
    props: {
      previewCount: 3,
      showMoreAction: true,
      compact: true
    }
  },
  { path: '/notices/:id', component: HomeNoticeDetailView },
  { path: '/market', component: MarketView },
  { path: '/market/demand/:id', component: DemandDetailView },
  { path: '/publish', component: PublishEntryView },
  { path: '/resources', component: ResourceShareView },
  { path: '/community', component: CommunityView },
  { path: '/community/posts/:id', component: CommunityPostDetailView },
  { path: '/roadmap', component: RoadmapView },
  { path: '/articles', component: ArticleListView },
  { path: '/courses', component: CourseListView },
  { path: '/knowledge-base', component: KnowledgeBaseView },
  { path: '/me', component: PersonalCenterView },
  { path: '/messages', component: ChatCenterView },
  { path: '/admin-entry', component: AdminEntryView },
  {
    path: '/client',
    component: ClientLayout,
    children: [
      { path: 'home', component: ClientHomeView },
      { path: 'demands', component: ClientDemandListView },
      { path: 'demands/create', component: ClientDemandCreateView },
      { path: 'orders', component: ClientOrderListView },
      { path: 'disputes', component: ClientDisputeListView },
      { path: 'inbox', component: InboxView },
      { path: 'messages', component: ChatCenterView }
    ]
  },
  {
    path: '/developer',
    component: DeveloperLayout,
    children: [
      { path: 'home', component: DeveloperHomeView },
      { path: 'profile', component: DeveloperProfileView },
      { path: 'market', component: DeveloperMarketView },
      { path: 'quotes', component: DeveloperQuoteListView },
      { path: 'orders', component: DeveloperOrderListView },
      { path: 'disputes', component: DeveloperDisputeListView },
      { path: 'inbox', component: InboxView },
      { path: 'messages', component: ChatCenterView }
    ]
  },
  { path: '/admin/login', component: AdminLoginView },
  {
    path: '/admin',
    component: AdminLayout,
    children: [
      { path: 'dashboard', component: AdminDashboardView },
      { path: 'banners', component: BannerManageView },
      { path: 'resources', component: ResourceManageView },
      { path: 'community', component: CommunityManageView },
      { path: 'skill-tags', component: SkillTagManageView },
      { path: 'categories', component: DemandCategoryManageView },
      { path: 'knowledge-bases', component: KnowledgeBaseManageView },
      { path: 'users', component: AdminUserListView },
      { path: 'demands', component: AdminDemandAuditView },
      { path: 'orders', component: AdminOrderListView },
      { path: 'disputes', component: AdminDisputeListView },
      { path: 'chats', component: AdminChatAuditView }
    ]
  }
]

function resolveWorkspaceRedirect(path, fullPath) {
  if (path === '/client') {
    return '/client/home'
  }
  if (path === '/developer') {
    return '/developer/home'
  }
  if (path === '/me') {
    return '/me'
  }
  return fullPath
}

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore()

  if (to.path === '/login') {
    if (authStore.token) {
      return authStore.userInfo?.redirectPath || '/me'
    }

    emitAuthExpired({
      scope: 'user',
      message: '请先登录后继续。',
      redirectPath: ''
    })
    return '/market'
  }

  if (to.path === '/me' || to.path === '/messages' || to.path.startsWith('/client') || to.path.startsWith('/developer')) {
    if (!authStore.token) {
      emitAuthExpired({
        scope: 'user',
        message: '请先登录后继续。',
        redirectPath: resolveWorkspaceRedirect(to.path, to.fullPath)
      })
      return '/market'
    }
  }

  if (to.path.startsWith('/developer')) {
    if (authStore.userInfo?.developerStatus !== 2) {
      try {
        const response = await fetchCurrentUser()
        const latest = response.data || {}
        authStore.userInfo = {
          ...authStore.userInfo,
          developerStatus: latest.developerStatus ?? authStore.userInfo?.developerStatus ?? 0,
          idVerifyStatus: latest.idVerifyStatus ?? authStore.userInfo?.idVerifyStatus ?? 0,
          skillAuditStatus: latest.skillAuditStatus ?? authStore.userInfo?.skillAuditStatus ?? 0,
          roles: latest.roles || authStore.userInfo?.roles || [],
          redirectPath: latest.redirectPath || authStore.userInfo?.redirectPath || '/me'
        }
        window.localStorage.setItem('user_info', JSON.stringify(authStore.userInfo))
        if (authStore.userInfo?.developerStatus !== 2) {
          return '/me'
        }
      } catch {
        return '/market'
      }
    }
  }

  if ((to.path === '/admin' || to.path.startsWith('/admin/')) && to.path !== '/admin/login') {
    const adminAuthStore = useAdminAuthStore()
    if (!adminAuthStore.token) {
      if (to.path === '/admin') return '/admin-entry'
      return '/admin/login'
    }
    if (to.path === '/admin') return '/admin/dashboard'
  }

  return true
})

export default router
