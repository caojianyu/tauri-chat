import { createRouter, createWebHashHistory } from 'vue-router'

// 导入组件
import Login from '../view/Login.vue'
import ChatView from '../view/ChatView.vue'

// 定义一些路由
// 每个路由都需要映射到一个组件。
const routes = [
    {
        path: '/',
        component: Login,
    },
    {
        path: '/chatView',
        component: ChatView,
    }
]

// 暂时保持简单
const router = createRouter({
    history: createWebHashHistory(),
    routes,
})

// 导出路由
export default router
