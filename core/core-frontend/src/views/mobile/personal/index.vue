<script lang="ts" setup>
import icon_right_outlined from '@/assets/svg/icon_right_outlined.svg'
import { useUserStoreWithOut } from '@/store/modules/user'
import userImg from '@/assets/img/user.png'
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router_2'
import { logoutApi } from '@/api/login'
import { logoutHandler } from '@/utils/logout'
import UpdatePwd from '@/views/system/modify-pwd/UpdatePwd.vue'
import VanPopup from 'vant/es/popup'
import VanNavBar from 'vant/es/nav-bar'
import VanImage from 'vant/es/image'
import 'vant/es/image/style'
import 'vant/es/nav-bar/style'
import 'vant/es/popup/style'

interface OrgTreeNode {
  id: string | number
  name: string
  readOnly: boolean
  children?: OrgTreeNode[]
}
const userStore = useUserStoreWithOut()
const { push } = useRouter()
const logout = async () => {
  await logoutApi()
  logoutHandler()
  push('/login')
}

const showPwd = ref(false)
const success = () => {
  showPwd.value = false
  logout()
}
</script>

<template>
  <div class="de-mobile-user">
    <div class="logout flex-center" style="padding-top: 8px; margin: 0">{{ $t('user.my') }}</div>
    <div class="mobile-user-top">
      <van-image round width="48" height="48" :src="userImg" />
      <div class="user-name">
        {{ userStore.name }}
      </div>
    </div>
    <div class="logout flex-center" @click="showPwd = true">{{ $t('user.change_password') }}</div>
    <div class="logout flex-center danger" @click="logout">{{ $t('user.logout') }}</div>
    <van-popup teleport="body" position="bottom" v-model:show="showPwd">
      <div style="padding: 0 24px 24px">
        <update-pwd @success="success" />
      </div>
    </van-popup>
  </div>
</template>

<style lang="less" scoped>
.de-mobile-user {
  height: calc(100% - 50px);
  width: 100vw;
  background: #f5f6f7;

  .cell-org_scroll {
    height: calc(100% - 144px);
    overflow-y: auto;
  }

  .mobile-user-top {
    padding: 16px;
    display: flex;
    align-items: center;
    margin-bottom: 8px;
    height: 80px;
    background: #fff;

    .user-name {
      font-size: 17px;
      font-weight: 500;
      line-height: 28px;
      margin-left: 12px;
    }
  }

  .logout {
    margin-top: 8px;
    height: 48px;
    font-size: 17px;
    font-weight: 500;
    line-height: 24px;
    background: #fff;
    &.danger {
      color: var(--ed-color-danger);
      font-size: 16px;
      font-weight: 400;
      line-height: 22px;
    }
  }

  .grey {
    height: 44px;
    padding: 16px;
    width: 100%;
    color: #646a73;
    font-size: 14px;
    font-weight: 400;
    line-height: 20px;
    display: flex;
    align-items: center;

    & > div {
      white-space: nowrap;
    }

    .ellipsis {
      max-width: 250px;
    }

    .active {
      color: var(--ed-color-primary);
    }

    .ed-icon {
      font-size: 12px;
      margin: 0 4px;
      color: #8f959e;
    }
  }
}
</style>
