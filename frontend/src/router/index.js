import { createRouter, createWebHistory } from 'vue-router'
import ClientLayout from '@/layouts/ClientLayout.vue'
import DeveloperLayout from '@/layouts/DeveloperLayout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import LoginView from '@/views/LoginView.vue'
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
import AdminLoginView from '@/views/admin/AdminLoginView.vue'
import AdminDashboardView from '@/views/admin/AdminDashboardView.vue'
import AdminDemandAuditView from '@/views/admin/AdminDemandAuditView.vue'
import AdminOrderListView from '@/views/admin/AdminOrderListView.vue'
import AdminDisputeListView from '@/views/admin/AdminDisputeListView.vue'
import AdminUserListView from '@/views/admin/users/AdminUserListView.vue'
import { useAdminAuthStore } from '@/stores/adminAuth'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    component: LoginView
  },
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
      { path: 'market', component: DeveloperMarketView },
      { path: 'quotes', component: DeveloperQuoteListView },
      { path: 'orders', component: DeveloperOrderListView },
      { path: 'disputes', component: DeveloperDisputeListView }
    ]
  },
  {
    path: '/admin/login',
    component: AdminLoginView
  },
  {
    path: '/admin',
    component: AdminLayout,
    children: [
      { path: 'dashboard', component: AdminDashboardView },
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
  if (to.path.startsWith('/admin') && to.path !== '/admin/login') {
    const adminAuthStore = useAdminAuthStore()
    if (!adminAuthStore.token) {
      return '/admin/login'
    }
  }
  return true
})

export default router
