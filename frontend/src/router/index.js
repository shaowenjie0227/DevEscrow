import { createRouter, createWebHistory } from 'vue-router'
import ClientLayout from '@/layouts/ClientLayout.vue'
import DeveloperLayout from '@/layouts/DeveloperLayout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import LoginView from '@/views/LoginView.vue'
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

const routes = [
  { path: '/', redirect: '/market' },
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
  { path: '/login', component: LoginView },
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

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  if (to.path.startsWith('/client') || to.path.startsWith('/developer')) {
    const authStore = useAuthStore()
    if (!authStore.token) {
      return '/login'
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
