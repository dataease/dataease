<script lang="ts" setup>
import { ref } from 'vue'
import Home from './home/index.vue'
import Directory from './directory/index.vue'
import Personal from './personal/index.vue'
import VanTabbar from 'vant/es/tabbar'
import VanTabbarItem from 'vant/es/tabbar-item'
import VanOverlay from 'vant/es/overlay'
import VanLoading from 'vant/es/loading'
import 'vant/es/tabbar-item/style'
import 'vant/es/tabbar/style'
import 'vant/es/overlay/style'
import 'vant/es/loading/style'

const activeTabbar = ref('home')
const showLoading = ref(false)
const hiddenTabbar = ref(false)
</script>

<template>
  <div class="mobile-index">
    <Home v-if="activeTabbar === 'home'" @setLoading="val => (showLoading = val)"></Home>
    <Directory
      v-else-if="activeTabbar === 'direct'"
      @setLoading="val => (showLoading = val)"
      @hiddenTabbar="val => (hiddenTabbar = val)"
    ></Directory>
    <Personal v-else-if="activeTabbar === 'user'"> </Personal>
    <van-tabbar safe-area-inset-bottom v-if="!hiddenTabbar" v-model="activeTabbar">
      <van-tabbar-item name="home" icon="wap-home-o">首页</van-tabbar-item>
      <van-tabbar-item name="direct" icon="bars">目录</van-tabbar-item>
      <van-tabbar-item name="user" icon="user-o">我的</van-tabbar-item>
    </van-tabbar>
    <van-overlay v-model:show="showLoading">
      <div style="display: flex; align-items: center; justify-content: center; height: 100%">
        <van-loading type="spinner" />
      </div>
    </van-overlay>
  </div>
</template>

<style lang="less">
.mobile-index {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}
</style>
