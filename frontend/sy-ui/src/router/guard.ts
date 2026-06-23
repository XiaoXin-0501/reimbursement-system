// 全局守卫
import type {Router} from 'vue-router'

export function setupRouterGuard(router : Router){
    router.beforeEach((to, _from, next) => {
        document.title = (to.meta.title as string)
        next()
    })
}