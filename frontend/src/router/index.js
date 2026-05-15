import { createRouter, createWebHistory } from 'vue-router'
import ClientLayout from '@/layouts/ClientLayout.vue'
import DeveloperLayout from '@/layouts/DeveloperLayout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import HomePageView from '@/views/public/HomePageView.vue'
import MarketView from '@/views/public/MarketView.vue'
import PublishEntryView from '@/views/public/PublishEntryView.vue'
import AdminEntryView from '@/views/public/AdminEntryView.vue'
import DemandDetailView from '@/views/public/DemandDetailView.vue'
import PersonalCenterView from '@/views/public/PersonalCenterView.vue'
import ResourceShareView from '@/views/public/content/ResourceShareView.vue'
import CommunityView from '@/views/public/content/CommunityView.vue'
import RoadmapView from '@/views/public/content/RoadmapView.vue'
import ArticleListView from '@/views/public/content/ArticleListView.vue'
import CourseListView from '@/views/public/content/CourseListView.vue'
import KnowledgeBaseView from '@/views/public/content/KnowledgeBaseView.vue'
import ClientHomeView from '@/views/client/ClientHomeView.vue'
import ClientDemandListView from '@/views/client/ClientDemandListView.vue'
import ClientDemandCreateView from '@/views/client/ClientDemandCreateView.vue'
import ClientOrderListView from '@/views/client/ClientOrderListView.vue'
import ClientDisputeListView from '@/views/client/ClientDisputeListView.vue'
import DeveloperHomeView from '@/views/developer/DeveloperHomeView.vue'
import DeveloperMarketView from '@/views/developer/DeveloperMarketView.vue'
import DeveloperQuoteListView from '@/views/developer/DeveloperQuoteListView.vue'
import DeveloperOrderListView from '@/views/developer/DeveloperOrderListView.vue'
import DeveloperDisputeListView from '@/views/developer/DeveloperDisputeListView.vue'
import DeveloperProfileView from '@/views/developer/DeveloperProfileView.vue'
import AdminLoginView from '@/views/admin/AdminLoginView.vue'
import AdminDashboardView from '@/views/admin/AdminDashboardView.vue'
import BannerManageView from '@/views/admin/BannerManageView.vue'
import ResourceManageView from '@/views/admin/ResourceManageView.vue'
import SkillTagManageView from '@/views/admin/SkillTagManageView.vue'
import DemandCategoryManageView from '@/views/admin/DemandCategoryManageView.vue'
import KnowledgeBaseManageView from '@/views/admin/KnowledgeBaseManageView.vue'
import AdminDemandAuditView from '@/views/admin/AdminDemandAuditView.vue'
import AdminOrderListView from '@/views/admin/AdminOrderListView.vue'
import AdminDisputeListView from '@/views/admin/AdminDisputeListView.vue'
import AdminUserListView from '@/views/admin/users/AdminUserListView.vue'
import { useAdminAuthStore } from '@/stores/adminAuth'
import { useAuthStore } from '@/stores/auth'
import { fetchCurrentUser } from '@/api/modules/auth'
import { emitAuthExpired } from '@/utils/authEvents'

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
  { path: '/market', component: MarketView },
  { path: '/market/demand/:id', component: DemandDetailView },
  { path: '/publish', component: PublishEntryView },
  { path: '/resources', component: ResourceShareView },
  { path: '/community', component: CommunityView },
  { path: '/roadmap', component: RoadmapView },
  { path: '/articles', component: ArticleListView },
  { path: '/courses', component: CourseListView },
  { path: '/knowledge-base', component: KnowledgeBaseView },
  { path: '/me', component: PersonalCenterView },
  { path: '/admin-entry', component: AdminEntryView },
  {
    path: '/client',
    component: ClientLayout,
    children: [
      { path: 'home', component: ClientHomeView },
      { path: 'demands', component: ClientDemandListView },
      { path: 'demands/create', component: ClientDemandCreateView },
      { path: 'orders', component: ClientOrderListView },
      { path: 'disputes', component: ClientDisputeListView }
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
      { path: 'disputes', component: DeveloperDisputeListView }
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
      { path: 'skill-tags', component: SkillTagManageView },
      { path: 'categories', component: DemandCategoryManageView },
      { path: 'knowledge-bases', component: KnowledgeBaseManageView },
      { path: 'users', component: AdminUserListView },
      { path: 'demands', component: AdminDemandAuditView },
      { path: 'orders', component: AdminOrderListView },
      { path: 'disputes', component: AdminDisputeListView }
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

  if (to.path === '/me' || to.path.startsWith('/client') || to.path.startsWith('/developer')) {
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
