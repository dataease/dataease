<template>

  <div
    :class="containerClass"
  >

    <div
      v-if="isPublicLink"
      class="function-div"
    >
      <el-button-group size="mini">
        <el-button
          v-if="!isNewBlank"
          size="mini"
          @click="back2Last"
        ><i class="icon iconfont el-icon-back" />{{ $t('chart.back') }}</el-button>
        <el-button
          v-if="existLinkage"
          size="mini"
          @click="clearAllLinkage"
        ><i class="icon iconfont icon-quxiaoliandong" />{{ $t('panel.remove_all_linkage') }}</el-button>
        <el-button
          size="mini"
          @click="exportPDF"
        ><i class="icon iconfont el-icon-download" />下载</el-button>
      </el-button-group>
    </div>

    <!-- <div
      v-show="isPublicLink && !isNewBlank"
      class="bar-main-left"
    >
      <el-button
        size="mini"
        @click="back2Last"
      ><i class="icon iconfont el-icon-back" />{{ $t('chart.back') }}</el-button>
    </div> -->
    <div
      v-else-if="existLinkage"
      class="bar-main-right"
    >
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
    containerClass() {
      return this.isPublicLink ? 'trans-pc' : 'bar-main'
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
    },
    exportPDF() {
      this.$emit('link-export-pdf')
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

  .trans-pc {
    position: absolute;
    width: 60px;
    right: 0;
    top: 0;
    border-top: 60px solid rgba(245, 74, 69, 0.2);
    border-left: 60px solid transparent;
    cursor: pointer;
    z-index: 999;
    .function-div {
      display: none;
      position: absolute;
      right: 10px;
      top: -50px;
      width: max-content;
      text-align: end;
      z-index: 999;

    }
    &:hover {
      border-top: 60px solid rgba(245, 74, 69, 0.8);;
      .function-div {
        display: block;
      }
    }
  }

</style>
