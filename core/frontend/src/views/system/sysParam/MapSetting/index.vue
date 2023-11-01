<template>
  <el-container class="map-setting-container">
    <el-aside class="map-setting-left">
      <div class="left-container">

        <div
          v-for="(item, index) in leftOptions"
          :key="item.id"
          class="left-menu-item"
          :class="{'active': activeIndex === item.id}"
          @click="selectHandler(index)"
        >
          <span>{{ $t(item.name) }}</span>
        </div>
      </div>
    </el-aside>
    <el-main class="map-setting-right">
      <div class="right-container">
        <OnlineMap v-if="activeIndex" />
        <geometry v-else />
      </div>
    </el-main>
  </el-container>
</template>

<script>
import Geometry from './Geometry'
import OnlineMap from './OnlineMap'
export default {
  name: 'MapSetting',
  components: { Geometry, OnlineMap },
  data() {
    return {
      leftOptions: [
        { id: 0, name: 'online_map.geometry' },
        { id: 1, name: 'online_map.onlinemap' }
      ],
      activeIndex: 0
    }
  },
  methods: {
    selectHandler(index) {
      this.activeIndex = index
    }
  }
}
</script>
<style lang="scss" scoped>
.map-setting-container {
  width: 100%;
  height: 100%;
  padding-bottom: 0px !important;
  .map-setting-left {
    width: 200px !important;
    height: calc(100% + 20px);
    border-right: 1px solid #1f232926;
    .left-container {
      padding: 16px 16px 16px 16px;
      width: 100%;
      height: 100%;
      .left-menu-item {
        width: 168px;
        height: 40px;
        padding: 9px 8px;
        line-height: 22px;
        border-radius: 4px;
        font-size: 14px;
        font-weight: 400;
        cursor: pointer;
        &:hover {
          background: #1f232926;
        }
      }
      .active {
        background: #3370FF1A;
        color: #3370FF;
        font-weight: 500;
      }
    }
  }
  .map-setting-right {
    padding-bottom: 0 !important;
    .right-container {
      height: 100%;
    }
  }
}
</style>
