import { createApp, h } from 'vue'
import { createMemoryHistory, createRouter, RouterView } from 'vue-router_2'
import ElementPlus from 'element-plus-secondary'
import 'element-plus-secondary/dist/index.css'
import 'element-plus-secondary/theme-chalk/dark/css-vars.css'
import AiService from 'ai-service-page'
import AiServiceGroup from '@/views/pages/system/ai-service-group/index.vue'
import AiRecords from '@/views/pages/system/ai-records/index.vue'

const query = new URLSearchParams(location.search)
if (query.get('view') === 'conversation') {
  const { default: Conversation } = await import('@/views/demo/ai-assistant/index.vue')
  createApp(Conversation).use(ElementPlus).mount('#app')
} else if (query.get('view') === 'ai-menu') {
  const recordsOnly = query.get('access') === 'records'
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      {
        path: '/sys-setting/ai-service',
        name: 'ai-service-group',
        component: AiServiceGroup,
        children: [
          ...(!recordsOnly ? [{ path: 'models', name: 'ai-models', component: AiService }] : []),
          { path: 'records', name: 'ai-records', component: AiRecords }
        ]
      }
    ]
  })
  window.aiNavigate = (name) => router.push({ name })
  window.aiRoute = () => router.currentRoute.value.path
  const app = createApp({ render: () => h(RouterView) })
    .use(router)
    .use(ElementPlus)
  router.push('/sys-setting/ai-service')
  router.isReady().then(() => app.mount('#app'))
} else {
  createApp(AiService).use(ElementPlus).mount('#app')
}
