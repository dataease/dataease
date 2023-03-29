<script lang="ts" setup>
import { ref } from 'vue'
import { Icon } from '@/components/icon-custom'
import { useCache } from '@/hooks/web/useCache'
import { useAppStoreWithOut } from '@/store/modules/app'
import router from '@/router'
const { wsCache } = useCache()
const appStore = useAppStoreWithOut()
const name = ref('admin')

const logout = () => {
  wsCache.delete(appStore.getToken)
  router.push({ path: '/login' })
}
</script>

<template>
  <el-dropdown ref="my-drop" class="top-dropdown" trigger="click">
    <div class="el-dropdown-link">
      <span>{{ name }}</span>
      <span>
        <el-icon>
          <Icon name="icon_right_outlined" />
        </el-icon>
      </span>
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
.operator-container {
  display: flex;
  align-items: center;
  width: 100px;
}
.operator-name {
  display: flex;
  color: var(--TopTextColor);
  font-size: 14px;
  width: 100%;
  .name-span {
    max-width: 80px;
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
  }
}
.logout {
  display: block;
}
.top-dropdown-link {
  text-decoration: none;
}
</style>
