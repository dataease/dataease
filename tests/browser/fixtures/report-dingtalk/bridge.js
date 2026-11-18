import { defineComponent, h } from 'vue'

export const useI18n = () => ({ t: key => key })

export const Icon = defineComponent({
  name: 'IconStub',
  setup(_, { slots }) {
    return () => h('span', slots.default?.())
  }
})

export const queryCategoryStatusApi = () =>
  Promise.resolve({ data: [{ name: 'dingtalk', enable: true }] })

export const dingtalkGroupOptionApi = () =>
  Promise.resolve({
    data: {
      valid: true,
      groupList: [{ chat_id: 'chat-1', name: '测试群消息-1' }]
    }
  })

export const larkGroupOptionApi = () => Promise.resolve({ data: { valid: false, groupList: [] } })

export const larksuiteGroupOptionApi = () =>
  Promise.resolve({ data: { valid: false, groupList: [] } })

export default defineComponent({
  name: 'ReciSelectStub',
  props: {
    modelValue: { type: Array, default: () => [] },
    resourceId: { type: String, default: '' },
    resourceFlag: { type: Number, default: 0 }
  },
  setup(_, { expose }) {
    expose({ setHeight() {} })
    return () => h('div', { class: 'reci-select-stub' })
  }
})
