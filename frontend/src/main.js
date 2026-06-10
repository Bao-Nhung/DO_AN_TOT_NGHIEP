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

// Pages
import HomePage        from '@/pages/HomePage.vue'
import ProductsPage    from '@/pages/ProductsPage.vue'
import ProductDetail   from '@/pages/ProductDetail.vue'
import WishlistPage    from '@/pages/WishlistPage.vue'
import ProfilePage     from '@/pages/ProfilePage.vue'
import LoginPage       from '@/pages/LoginPage.vue'

const routes = [
  { path: '/',               component: HomePage,      name: 'home' },
  { path: '/collections',    component: ProductsPage,  name: 'products' },
  { path: '/product/:id',    component: ProductDetail, name: 'product-detail' },
  { path: '/wishlist',       component: WishlistPage,  name: 'wishlist' },
  { path: '/profile',        component: ProfilePage,   name: 'profile' },
  { path: '/login',          component: LoginPage,     name: 'login' },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

createApp(App).use(router).mount('#app')
