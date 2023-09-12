<script lang="ts" setup>
import { computed, ref } from 'vue'
import { Icon } from '@/components/icon-custom'
import { useUserStoreWithOut } from '@/store/modules/user'
import { logoutApi } from '@/api/login'
import { logoutHandler } from '@/utils/logout'
import { XpackComponent } from '@/components/plugin'
import { useI18n } from '@/hooks/web/useI18n'
import { useEmitt } from '@/hooks/web/useEmitt'
import AboutPage from '@/views/about/index.vue'
const userStore = useUserStoreWithOut()
const { t } = useI18n()
const dropLinkList = ref([])

const logout = async () => {
  await logoutApi()
  logoutHandler()
}

const ucenterLoaded = items => {
  items.forEach(item => dropLinkList.value.push(item))
}

const toAbout = () => {
  useEmitt().emitter.emit('open-about-dialog')
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
        <el-dropdown-item @click="toAbout">
          <span>{{ t('common.about') }}</span>
        </el-dropdown-item>

        <router-link
          v-for="(item, index) in dropLinkList"
          :key="index"
          :to="item.link"
          class="top-dropdown-link"
        >
          <el-dropdown-item>{{ item.label }}</el-dropdown-item>
        </router-link>
        <el-dropdown-item divided @click="logout">
          <span>{{ t('common.exit_system') }}</span>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
  <AboutPage />
  <XpackComponent jsname="dWNlbnRlci1oYW5kbGVy" @loaded="ucenterLoaded" />
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
