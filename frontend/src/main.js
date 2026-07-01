import { createApp } from 'vue'
import { createRouter, createWebHashHistory } from 'vue-router'

// Bootstrap
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap-icons/font/bootstrap-icons.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'

// Global styles
import '@/assets/main.css'

// Root component
import App from './App.vue'

// Pages — Client
import HomePage        from '@/pages/HomePage.vue'
import ProductsPage    from '@/pages/ProductsPage.vue'
import ProductDetail   from '@/pages/ProductDetail.vue'
import WishlistPage    from '@/pages/WishlistPage.vue'
import ProfilePage     from '@/pages/ProfilePage.vue'
import LoginPage       from '@/pages/LoginPage.vue'
import AboutPage       from '@/pages/AboutPage.vue'
import CheckoutPage    from '@/pages/CheckoutPage.vue'
import PaymentResultPage from '@/pages/PaymentResultPage.vue'
import QRPaymentPage from '@/pages/QRPaymentPage.vue'

// Pages — Admin
import AdminDashboard  from '@/pages/admin/AdminDashboard.vue'
import AdminProducts   from '@/pages/admin/AdminProducts.vue'
import AdminOrders     from '@/pages/admin/AdminOrders.vue'
import AdminCustomers  from '@/pages/admin/AdminCustomers.vue'
import AdminEmployees  from '@/pages/admin/AdminEmployees.vue'
import AdminVouchers   from '@/pages/admin/AdminVouchers.vue'
import AdminSettings   from '@/pages/admin/AdminSettings.vue'
import AdminPOS        from '@/pages/admin/AdminPOS.vue'
import AdminSchedule   from '@/pages/admin/AdminSchedule.vue'
import AdminStatisticalDashboard from '@/pages/admin/AdminStatisticalDashboard.vue'
import AdminNotifications from '@/pages/admin/AdminNotifications.vue'

const routes = [
  { path: '/',               component: HomePage,       name: 'home' },
  { path: '/collections',    component: ProductsPage,   name: 'products' },
  { path: '/product/:id',    component: ProductDetail,  name: 'product-detail' },
  { path: '/wishlist',       component: WishlistPage,   name: 'wishlist' },
  { path: '/profile',        component: ProfilePage,    name: 'profile' },
  { path: '/login',          component: LoginPage,      name: 'login' },
  { path: '/about',          component: AboutPage,      name: 'about' },
  { path: '/checkout',       component: CheckoutPage,   name: 'checkout' },
  { path: '/payment-result', component: PaymentResultPage, name: 'payment-result' },
  { path: '/qr-payment',    component: QRPaymentPage,     name: 'qr-payment' },

  { path: '/admin',           component: AdminDashboard, name: 'admin-dashboard' },
  { path: '/admin/thong-ke',  component: AdminStatisticalDashboard, name: 'admin-thong-ke' },
  { path: '/admin/products',  component: AdminProducts,  name: 'admin-products' },
  { path: '/admin/orders',    component: AdminOrders,    name: 'admin-orders' },
  { path: '/admin/customers', component: AdminCustomers, name: 'admin-customers' },
  { path: '/admin/employees', component: AdminEmployees, name: 'admin-employees' },
  { path: '/admin/schedule',  component: AdminSchedule,  name: 'admin-schedule' },
  { path: '/admin/vouchers',  component: AdminVouchers,  name: 'admin-vouchers' },
  { path: '/admin/notifications', component: AdminNotifications, name: 'admin-notifications' },
  { path: '/admin/settings',  component: AdminSettings,  name: 'admin-settings' },
  { path: '/admin/pos',       component: AdminPOS,       name: 'admin-pos' },
  { path: '/tracking', name: 'Tracking', component: () => import('@/pages/OrderTrackingPage.vue') },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

import { useAuth } from '@/composables/useApi'

router.beforeEach((to, from, next) => {
  const { isLoggedIn, getUser } = useAuth()
  const isAdminRoute = to.path.startsWith('/admin')
  const isProfileRoute = to.path.startsWith('/profile')

  if (isAdminRoute || isProfileRoute) {
    if (!isLoggedIn()) {
      return next({ name: 'login', query: { redirect: to.fullPath } })
    }
    const user = getUser()
    if (!user) {
      return next({ name: 'login', query: { redirect: to.fullPath } })
    }
    if (isAdminRoute) {
      if (user.role === 'KhachHang') {
        return next({ name: 'home' })
      }
    }
  }
  next()
})

createApp(App).use(router).mount('#app')
