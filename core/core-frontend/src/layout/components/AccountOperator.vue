<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { Icon } from '@/components/icon-custom'
import { useUserStoreWithOut } from '@/store/modules/user'
import router from '@/router'
import { usePermissionStoreWithOut } from '@/store/modules/permission'
import { logoutApi } from '@/api/login'

const permissionStore = usePermissionStoreWithOut()
const userStore = useUserStoreWithOut()
const name = ref('')

const logout = async () => {
  const res = await logoutApi()
  console.log(res)
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
onMounted(() => {
  name.value = userStore.getName
})
</script>

<template>
  <el-dropdown ref="my-drop" class="top-dropdown" trigger="click">
    <div class="my-drop-link">
      {{ name }}
      <el-icon class="el-icon-animate">
        <Icon name="icon_expand-down_filled" />
      </el-icon>
    </div>
    <template #dropdown>
      <el-dropdown-menu>
        <router-link to="/person-info/index" class="top-dropdown-link">
          <el-dropdown-item>{{ $t('common.personal_info') }}</el-dropdown-item>
        </router-link>

        <!-- <router-link
            v-if="$store.getters.validate"
            to="/ukey/index"
          >
            <el-dropdown-item>{{ $t('commons.ukey_title') }}</el-dropdown-item>
          </router-link> -->

        <router-link to="/person-pwd/index" class="top-dropdown-link">
          <el-dropdown-item>{{ $t('user.change_password') }}</el-dropdown-item>
        </router-link>

        <router-link to="/about/index" class="top-dropdown-link">
          <el-dropdown-item>{{ $t('common.about') }}</el-dropdown-item>
        </router-link>
        <el-dropdown-item divided @click="logout">
          <span>{{ $t('common.exit_system') }}</span>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<style lang="less">
.logout {
  display: block;
}
.top-dropdown-link {
  text-decoration: none;
}

.my-drop-link {
  cursor: pointer;
  margin-left: 11px;
  color: rgba(255, 255, 255, 0.8);
  .ed-icon {
    margin: 0 5.25px !important;
    font-size: 9.5px !important;
  }
}
@keyframes iconSelect {
  from {
    transform: rotateZ(0);
  }
  to {
    transform: rotateZ(-180deg);
  }
}
[aria-expanded='true'] > .ed-icon-animate {
  animation: iconSelect 0.6s;
  transform: rotateZ(-180deg);
}
</style>
