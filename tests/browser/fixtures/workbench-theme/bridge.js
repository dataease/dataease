import { defineComponent, h } from 'vue'

const Stub = defineComponent({
  name: 'WorkbenchStub',
  setup(_, { slots }) {
    return () => h('div', { class: 'workbench-stub' }, slots.default?.())
  }
})

const card = count => ({ menuAuth: true, anyManage: true, leafNodeCount: count })

export default Stub

export const useI18n = () => ({ t: key => key })

export const usePermissionStoreWithOut = () => ({ currentPath: '/workbranch' })

export const useRequestStoreWithOut = () => ({ loadingMap: {} })

export const interactiveStoreWithOut = () => ({
  getData: {
    0: card(3),
    1: card(2),
    2: card(1),
    3: card(4)
  }
})

export const useUserStoreWithOut = () => ({ getName: 'Test user', getUid: 1 })

export const useRouter = () => ({
  push: () => {},
  resolve: () => ({ href: '#/dataset-form' })
})

export const useCache = () => ({ wsCache: { get: () => null, set: () => {} } })

export const useEmbedded = () => ({ baseUrl: '' })

export const useAppStoreWithOut = () => ({ getIsDataEaseBi: false })

export const useShareStoreWithOut = () => ({ setData: () => {} })

export const searchMarketRecommend = () => Promise.resolve({ data: { baseUrl: '', contents: [] } })

export const queryShareBaseApi = () => Promise.resolve({ data: {} })

export const Base64 = { encode: value => btoa(value) }
