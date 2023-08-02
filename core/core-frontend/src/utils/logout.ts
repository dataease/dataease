import { useUserStoreWithOut } from '@/store/modules/user'
import router from '@/router'
import { usePermissionStoreWithOut } from '@/store/modules/permission'

const permissionStore = usePermissionStoreWithOut()
const userStore = useUserStoreWithOut()

export const logoutHandler = () => {
  userStore.clear()
  userStore.$reset()
  permissionStore.$reset()
  let queryRedirectPath = '/workbranch/index'
  // 如果redirect参数中有值
  if (router.currentRoute.value.fullPath) {
    queryRedirectPath = router.currentRoute.value.fullPath as string
  }
  router.push(`/login?redirect=${queryRedirectPath}`)
}
