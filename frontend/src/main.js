import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'

// Bootstrap
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap-icons/font/bootstrap-icons.css'

// Global styles
import '@/assets/main.css'

// Root component
import App from './App.vue'
import { i18n } from '@/i18n'
import { installDomTranslator } from '@/i18n/domTranslator'

// Lazy-loaded pages keep the initial bundle smaller.
const HomePage = () => import('@/pages/HomePage.vue')
const ProductsPage = () => import('@/pages/ProductsPage.vue')
const ProductDetail = () => import('@/pages/ProductDetail.vue')
const WishlistPage = () => import('@/pages/WishlistPage.vue')
const ProfilePage = () => import('@/pages/ProfilePage.vue')
const LoginPage = () => import('@/pages/LoginPage.vue')
const AboutPage = () => import('@/pages/AboutPage.vue')
const CheckoutPage = () => import('@/pages/CheckoutPage.vue')
const PaymentResultPage = () => import('@/pages/PaymentResultPage.vue')
const QRPaymentPage = () => import('@/pages/QRPaymentPage.vue')
const NotificationsPage = () => import('@/pages/NotificationsPage.vue')
const MyOrdersPage = () => import('@/pages/MyOrdersPage.vue')
const OrderTrackingPage = () => import('@/pages/OrderTrackingPage.vue')
const LookbookPage = () => import('@/pages/LookbookPage.vue')
const PoliciesPage = () => import('@/pages/PoliciesPage.vue')
const ReviewsPage = () => import('@/pages/ReviewsPage.vue')
const CompareProductsPage = () => import('@/pages/CompareProductsPage.vue')
const LuckyWheelPage = () => import('@/pages/LuckyWheelPage.vue')

const AdminDashboard = () => import('@/pages/admin/AdminDashboard.vue')
const AdminProducts = () => import('@/pages/admin/AdminProducts.vue')
const AdminOrders = () => import('@/pages/admin/AdminOrders.vue')
const AdminCustomers = () => import('@/pages/admin/AdminCustomers.vue')
const AdminEmployees = () => import('@/pages/admin/AdminEmployees.vue')
const AdminVouchers = () => import('@/pages/admin/AdminVouchers.vue')
const AdminSettings = () => import('@/pages/admin/AdminSettings.vue')
const AdminPOS = () => import('@/pages/admin/AdminPOS.vue')
const AdminSchedule = () => import('@/pages/admin/AdminSchedule.vue')
const AdminStatisticalDashboard = () => import('@/pages/admin/AdminStatisticalDashboard.vue')
const AdminNotifications = () => import('@/pages/admin/AdminNotifications.vue')
const AdminSupportChat = () => import('@/pages/admin/AdminSupportChat.vue')
const AdminPromotions = () => import('@/pages/admin/AdminPromotions.vue')
const AdminReturns = () => import('@/pages/admin/AdminReturns.vue')
const AdminLuckyWheel = () => import('@/pages/admin/AdminLuckyWheel.vue')

const routes = [
  { path: '/',               component: HomePage,       name: 'home' },
  { path: '/collections',    alias: '/products', component: ProductsPage, name: 'products' },
  { path: '/product/:id',    component: ProductDetail,  name: 'product-detail' },
  { path: '/wishlist',       component: WishlistPage,   name: 'wishlist' },
  { path: '/profile',        component: ProfilePage,    name: 'profile' },
  { path: '/login',          component: LoginPage,      name: 'login' },
  { path: '/about',          component: AboutPage,      name: 'about' },
  { path: '/checkout',       component: CheckoutPage,   name: 'checkout' },
  { path: '/payment-result', component: PaymentResultPage, name: 'payment-result' },
  { path: '/qr-payment',    component: QRPaymentPage,     name: 'qr-payment' },
  { path: '/notifications',  component: NotificationsPage, name: 'notifications' },
  { path: '/my-orders',      component: MyOrdersPage,      name: 'my-orders' },
  { path: '/lookbook',       component: LookbookPage,      name: 'lookbook' },
  { path: '/policies',       component: PoliciesPage,      name: 'policies' },
  { path: '/reviews',        component: ReviewsPage,       name: 'reviews' },
  { path: '/compare',        component: CompareProductsPage, name: 'compare' },
  { path: '/lucky-wheel',    component: LuckyWheelPage, name: 'lucky-wheel' },

  { path: '/admin',           component: AdminDashboard, name: 'admin-dashboard' },
  { path: '/admin/thong-ke',  component: AdminStatisticalDashboard, name: 'admin-thong-ke' },
  { path: '/admin/products',  component: AdminProducts,  name: 'admin-products' },
  { path: '/admin/orders',    component: AdminOrders,    name: 'admin-orders' },
  { path: '/admin/customers', component: AdminCustomers, name: 'admin-customers' },
  { path: '/admin/employees', component: AdminEmployees, name: 'admin-employees' },
  { path: '/admin/schedule',  component: AdminSchedule,  name: 'admin-schedule' },
  { path: '/admin/vouchers',  component: AdminVouchers,  name: 'admin-vouchers' },
  { path: '/admin/promotions', component: AdminPromotions, name: 'admin-promotions' },
  { path: '/admin/lucky-wheel', component: AdminLuckyWheel, name: 'admin-lucky-wheel' },
  { path: '/admin/returns', component: AdminReturns, name: 'admin-returns' },
  { path: '/admin/notifications', component: AdminNotifications, name: 'admin-notifications' },
  { path: '/admin/support-chat', component: AdminSupportChat, name: 'admin-support-chat' },
  { path: '/admin/settings',  component: AdminSettings,  name: 'admin-settings' },
  { path: '/admin/pos',       component: AdminPOS,       name: 'admin-pos' },
  { path: '/tracking', name: 'Tracking', component: OrderTrackingPage },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: (to) => to.hash ? { el: to.hash, behavior: 'smooth' } : { top: 0 },
})

import { api, useAuth } from '@/composables/useApi'

const adminOnlyRouteNames = new Set([
  'admin-thong-ke',
  'admin-customers',
  'admin-employees',
  'admin-vouchers',
  'admin-promotions',
  'admin-lucky-wheel',
  'admin-notifications',
  'admin-settings',
])

function isAdminRole(role) {
  return role === 'Admin'
}

function isStaffRole(role) {
  return isAdminRole(role) || role === 'NhanVien' || role === 'Nhân viên'
}

function isEmployeeRole(role) {
  return role === 'NhanVien' || role === 'Nhân viên'
}

router.beforeEach(async (to, from, next) => {
  const { isLoggedIn, getUser } = useAuth()
  const isAdminRoute = to.path.startsWith('/admin')
  const isProfileRoute = to.path.startsWith('/profile') || to.path.startsWith('/my-orders')
  const requiresLogin = isAdminRoute || isProfileRoute
  const user = isLoggedIn() ? getUser() : null

  if (user && isStaffRole(user.role) && !isAdminRoute) {
    return next({ name: 'admin-dashboard' })
  }

  if (requiresLogin) {
    if (!isLoggedIn()) {
      return next({ name: 'login', query: { redirect: to.fullPath } })
    }
    if (!user) {
      return next({ name: 'login', query: { redirect: to.fullPath } })
    }
    if (isAdminRoute) {
      if (!isStaffRole(user.role)) {
        return next({ name: 'home' })
      }
      if (isEmployeeRole(user.role) && to.name !== 'admin-schedule') {
        try {
          const shiftStatus = await api().getWorkShiftStatus()
          if (!shiftStatus?.canOperate) return next({ name: 'admin-schedule' })
        } catch (e) {
          return next({ name: 'admin-schedule' })
        }
      }
      if (!isAdminRole(user.role) && adminOnlyRouteNames.has(to.name)) {
        return next({ name: 'admin-pos' })
      }
      if (!isAdminRole(user.role) && to.name === 'admin-products') {
        return next({ name: 'admin-pos' })
      }
    }
  }
  next()
})

const app = createApp(App)
app.use(router)
app.use(i18n)
installDomTranslator(app)
router.isReady().then(() => app.mount('#app'))

