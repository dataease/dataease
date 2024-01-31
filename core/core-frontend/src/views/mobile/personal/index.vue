<script lang="ts" setup>
import { useUserStoreWithOut } from '@/store/modules/user'
import userImg from '@/assets/img/user.png'
import { useRouter } from 'vue-router'
import { logoutApi } from '@/api/login'
import { logoutHandler } from '@/utils/logout'
import VanButton from 'vant/es/button'
import VanCell from 'vant/es/cell'
import VanIcon from 'vant/es/icon'
import VanImage from 'vant/es/image'
import 'vant/es/image/style'
import 'vant/es/icon/style'
import 'vant/es/cell/style'
import 'vant/es/button/style'
const userStore = useUserStoreWithOut()
const { push } = useRouter()

const logout = async () => {
  await logoutApi()
  logoutHandler()
  push('/login')
}
</script>

<template>
  <div class="de-mobile-user">
    <div class="mobile-user-top">
      <van-image width="75" height="75" :src="userImg" />
      <div class="user-name">
        {{ userStore.name }}
      </div>
    </div>
    <van-cell value="系统信息" is-link>
      <template #title>
        <van-icon name="user-o" class="search-icon" />
        <span class="custom-title">关于</span>
      </template>
    </van-cell>
    <div style="margin: 16px">
      <van-button round block type="primary" @click="logout"> 注销 </van-button>
    </div>
  </div>
</template>

<style lang="less" scoped>
.de-mobile-user {
  height: 100vh;
  width: 100vw;

  .mobile-user-top {
    padding: 24px 24px 16px 24px;
    display: flex;

    .user-name {
      height: 45px;
      text-align: left;
      padding-left: 10px;
      padding-top: 20px;
    }
  }

  :deep(.van-cell__title) {
    display: flex;
    align-items: center;
  }

  .custom-title {
    margin-right: 4px;
    vertical-align: middle;
    margin-left: 12px;
  }

  .search-icon {
    font-size: 26px;
    line-height: inherit;
  }
}
</style>
