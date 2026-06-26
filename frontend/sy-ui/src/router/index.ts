import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/list',
    },
    {
      path: '/list',
      name: 'ReimbursementList',
      component: () => import('@/views/list/ReimbursementList.vue'),
      meta: { title: '差旅报销单' },
    },
    {
      path: '/detail/:id?',
      name: 'ReimbursementDetail',
      component: () => import('@/views/detail/ReimbursementDetail.vue'),
      meta: { title: '报销单' },
      props: true,
    },
  ],
})

export default router
