import { createRouter, createWebHashHistory } from 'vue-router'

import Login from '../view/Login.vue'
import ChatView from '../view/ChatView.vue'

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

const router = createRouter({
    history: createWebHashHistory(),
    routes,
})

export default router
