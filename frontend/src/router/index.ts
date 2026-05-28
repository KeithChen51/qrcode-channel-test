import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { getAuthToken } from '@/auth/session'

declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    icon?: string
    public?: boolean
  }
}

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/',
    component: () => import('@/layouts/DashboardLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页', icon: 'home' }
      },
      {
        path: 'config',
        name: 'Config',
        component: () => import('@/views/Config.vue'),
        meta: { title: '配置管理', icon: 'settings' }
      },
      {
        path: 'campaigns',
        name: 'Campaigns',
        component: () => import('@/views/Campaigns.vue'),
        meta: { title: '活动管理', icon: 'flag' }
      },
      {
        path: 'qrcode',
        name: 'Qrcode',
        component: () => import('@/views/Qrcode.vue'),
        meta: { title: '二维码管理', icon: 'qr-code' }
      },
      {
        path: 'scan-dashboard',
        name: 'ScanDashboard',
        component: () => import('@/views/ScanDashboard.vue'),
        meta: { title: '扫码看板', icon: 'chart' }
      },
      {
        path: 'guide',
        name: 'Guide',
        component: () => import('@/views/Guide.vue'),
        meta: { title: '使用指南', icon: 'book' }
      }
    ]
  },
  {
    path: '/jump',
    name: 'JumpPage',
    component: () => import('@/views/JumpPage.vue'),
    meta: { title: 'H5 跳转页', public: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '页面未找到', public: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  document.title = `${to.meta.title || '页面'} - JCYC 活码系统`

  const hasToken = Boolean(getAuthToken())
  if (!to.meta.public && !hasToken) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  if (to.path === '/login' && hasToken) {
    const redirect = typeof to.query.redirect === 'string' ? to.query.redirect : '/'
    next(redirect)
    return
  }

  next()
})

export default router
