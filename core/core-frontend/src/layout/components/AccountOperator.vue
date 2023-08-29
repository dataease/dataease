<script lang="ts" setup>
import { computed } from 'vue'
import { Icon } from '@/components/icon-custom'
import { useUserStoreWithOut } from '@/store/modules/user'
import { logoutApi } from '@/api/login'
import { logoutHandler } from '@/utils/logout'
import { XpackComponent } from '@/components/plugin'
const userStore = useUserStoreWithOut()

const logout = async () => {
  await logoutApi()
  logoutHandler()
}

const name = computed(() => userStore.getName)
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
        <XpackComponent jsname="dWNlbnRlci1oYW5kbGVy" />

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
