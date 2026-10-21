import { createApp, ref } from 'vue'
import ElementPlus from 'element-plus-secondary'
import TicketEdit from '@/views/share/share/TicketEdit.vue'

createApp({
  components: { TicketEdit },
  setup() {
    const editor = ref()
    const saved = ref(0)
    const open = () => editor.value.edit({ ticket: 'old-ticket', exp: 30, args: '{"region":["saved"]}' }, 'https://example.invalid/share')
    const create = () => editor.value.edit(null, 'https://example.invalid/share')
    return { editor, saved, open, create }
  },
  template: '<button @click="open">编辑已有 Ticket</button><button @click="create">新建 Ticket</button><span id="saved">{{ saved }}</span><ticket-edit ref="editor" uuid="share-uuid" @saved="saved++" />'
}).use(ElementPlus).component('Icon', { template: '<span><slot /></span>' }).mount('#app')
