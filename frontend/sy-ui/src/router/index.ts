import {createRouter, createWebHistory} from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { setupRouterGuard } from './guard'

const routes: RouteRecordRaw[] = [
    {
      path: '/',
      redirect: '/reim/list',
    },
    {
      path: '/reim/list',
      name: 'ReimList',
      component: () => import('@/views/ReimList.vue'),
      meta: {
        title: '报销单列表',
      },
    },
    {
      path: '/reim/detail/:id',
      name: 'ReimDetail',
      component: () => import('@/views/ReimDetail.vue'),
      meta: {
        title: '差旅费用报销单',
      },
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/reim/list',
    },
  ]

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes,
    scrollBehavior(){
        return {top : 0, left : 0}
    },
})

setupRouterGuard(router)

export default router
