<template>
  <div
    v-show="existLinkage"
    class="bar-main"
  >
    <div
      v-show="isPublicLink && !isNewBlank"
      class="bar-main-left"
    >
      <el-button
        size="mini"
        @click="back2Last"
      ><i class="icon iconfont el-icon-back" />{{ $t('chart.back') }}</el-button>
    </div>
    <div class="bar-main-right">
      <el-button
        size="mini"
        type="warning"
        @click="clearAllLinkage"
      ><i class="icon iconfont icon-quxiaoliandong" />{{ $t('panel.remove_all_linkage') }}</el-button>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import bus from '@/utils/bus'

export default {
  computed: {
    existLinkage() {
      let linkageFiltersCount = 0
      this.componentData.forEach(item => {
        if (item.linkageFilters && item.linkageFilters.length > 0) {
          linkageFiltersCount++
        }
      })
      return linkageFiltersCount
    },
    isPublicLink() {
      return this.$router.currentRoute.path === '/delink'
    },
    isNewBlank() {
      return window.history.length === 1
    },
    ...mapState([
      'componentData'
    ])
  },
  methods: {
    clearAllLinkage() {
      this.$store.commit('clearPanelLinkageInfo')
      bus.$emit('clear_panel_linkage', { viewId: 'all' })
    },
    back2Last() {
      this.$router.back(-1)
    }
  }
}
</script>

<style lang="scss" scoped>
  .bar-main {
    display: flex;
    div {
      position: absolute;
      z-index: 10;
      height: 20px;
      border-radius:2px;
      padding-left: 3px;
      padding-right: 0px;
      cursor:pointer!important;
    }
  }
  .bar-main-right{
    right: 0px;
    opacity: 0.8;
  }

  .bar-main-left {
    left: 0px;
    opacity: 0;
    height: fit-content;
    &:hover {
      opacity: 0.8;
    }
  }

</style>
