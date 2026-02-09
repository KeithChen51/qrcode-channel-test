import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
    {
        path: '/',
        component: () => import('@/layouts/DashboardLayout.vue'),
        children: [
            {
                path: '',
                name: 'Home',
                component: () => import('@/views/Home.vue'),
                meta: { title: '首页', icon: 'solar:home-2-bold-duotone' }
            },
            {
                path: 'config',
                name: 'Config',
                component: () => import('@/views/Config.vue'),
                meta: { title: '配置管理', icon: 'solar:settings-bold-duotone' }
            },
            {
                path: 'campaigns',
                name: 'Campaigns',
                component: () => import('@/views/Campaigns.vue'),
                meta: { title: '活动管理', icon: 'solar:flag-bold-duotone' }
            },
            {
                path: 'qrcode',
                name: 'Qrcode',
                component: () => import('@/views/Qrcode.vue'),
                meta: { title: '二维码管理', icon: 'solar:qr-code-bold-duotone' }
            },
            {
                path: 'scan-dashboard',
                name: 'ScanDashboard',
                component: () => import('@/views/ScanDashboard.vue'),
                meta: { title: '扫码看板', icon: 'solar:chart-2-bold-duotone' }
            },
            {
                path: 'guide',
                name: 'Guide',
                component: () => import('@/views/Guide.vue'),
                meta: { title: '使用指南', icon: 'solar:book-bold-duotone' }
            }
        ]
    },
    {
        path: '/jump',
        name: 'JumpPage',
        component: () => import('@/views/JumpPage.vue'),
        meta: { title: 'H5跳转页' }
    },
    {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: () => import('@/views/NotFound.vue'),
        meta: { title: '页面未找到' }
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 路由标题
router.beforeEach((to, _from, next) => {
    document.title = `${to.meta.title || '页面'} - JCYC 活码系统`
    next()
})

export default router
