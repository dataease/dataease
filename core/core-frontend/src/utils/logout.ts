import { useUserStoreWithOut } from '@/store/modules/user'
import router from '@/router'
import { usePermissionStoreWithOut } from '@/store/modules/permission'
import { interactiveStoreWithOut } from '@/store/modules/interactive'
import { useCache } from '@/hooks/web/useCache'
const { wsCache } = useCache()
const permissionStore = usePermissionStoreWithOut()
const userStore = useUserStoreWithOut()
const interactiveStore = interactiveStoreWithOut()

export const logoutHandler = () => {
  userStore.clear()
  userStore.$reset()
  permissionStore.clear()
  permissionStore.$reset()
  interactiveStore.clear()
  interactiveStore.$reset()
  removeCache()
  let queryRedirectPath = '/workbranch/index'
  // 如果redirect参数中有值
  if (router.currentRoute.value.fullPath) {
    queryRedirectPath = router.currentRoute.value.fullPath as string
  }
  router.push(`/login?redirect=${queryRedirectPath}`)
}

const removeCache = () => {
  const keys = Object.keys(wsCache['storage'])
  keys.forEach(key => {
    if (key.startsWith('de-plugin-')) {
      wsCache.delete(key)
    }
  })
}
