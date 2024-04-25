<template>
  <el-aside
    :width="currentWidth"
    class="ms-aside-container"
    :class="{ retract: !sideTreeStatus }"
    :style="{'margin-left': !asideHidden ? 0 : '-' + currentWidth}"
    @mouseenter.native="mouseenter"
    @mouseleave.native="mouseleave"
  >
    <DeArrowSide
      v-if="!close"
      :style="sideStyle"
      :is-inside="!sideTreeStatus"
      @changeSideTreeStatus="changeSideTreeStatus"
    />
    <slot />
    <de-horizontal-drag-bar
      v-if="isSystem"
      :type="type"
    />
  </el-aside>
</template>

<script>
import DeHorizontalDragBar from './dragbar/DeLeft2RightDragBar'
import { getLayout } from '@/utils/LayoutUtil'
import DeArrowSide from '@/components/dataease/DeArrowSide.vue'
export default {
  name: 'DeAsideContainer',
  components: { DeHorizontalDragBar, DeArrowSide },
  props: {
    width: {
      type: String,
      default: '260px'
    },
    close: {
      type: Boolean,
      default: false
    },
    isCollapseWidth: {
      type: String,
      default: ''
    },
    enableAsideHidden: {
      type: Boolean,
      default: true
    },
    showDragBar: {
      type: Boolean,
      default: true
    },
    isTemplate: {
      type: Boolean,
      default: false
    },
    type: {
      type: String,
      default: null
    }
  },
  data() {
    return {
      asideHidden: false,
      currentWidth: '',
      sideTreeStatus: true
    }
  },
  computed: {
    isSystem() {
      // 系统管理不需要拖拽菜单
      return this.isTemplate || (!this.$route.fullPath.includes('system') && this.showDragBar)
    },
    sideStyle() {
      return this.sideTreeStatus ? { right: '-12px' } : { left: 0 }
    }
  },
  mounted() {
    this.setCurrentWidth()
  },
  beforeUpdate() {
    this.setCurrentWidth()
  },
  methods: {
    setCurrentWidth() {
      this.currentWidth = this.isCollapseWidth || this.type && getLayout(this.type) || this.width
    },
    mouseenter() {
      if (!this.sideTreeStatus) return
      this.$store.dispatch('app/setArrowSide', true)
    },
    mouseleave() {
      if (!this.sideTreeStatus) return
      this.$store.dispatch('app/setArrowSide', false)
    },
    changeSideTreeStatus(val) {
      this.sideTreeStatus = val
    }
  }
}
</script>

<style lang="less" scoped>

  .ms-aside-container {
    /* border: 1px solid #E6E6E6; */
    border-radius: 2px;
    box-sizing: border-box;
    background-color: var(--SiderBG, #FFF);
    height: calc(100vh - 56px);
    border-right: 0px;
    position: relative;
    padding-bottom: 50px;
    overflow: visible;

    &.retract {
      width: 0 !important;
      min-width: 0 !important;

      ::v-deep.de-dataset-search, ::v-deep.tree-style {
        width: 0 !important;
        min-width: 0 !important;
        padding: 0;
      }
    }
  }

  /* .collapse-style {
    height: calc(100vh - 56px);
  } */

  .hiddenBottom {
    width: 8px;
    height: 50px;
    top: calc((100vh - 80px)/3);
    right: -10px;
    /*top: 0;*/
    line-height: 50px;
    border-radius: 0 15px 15px 0;
    background-color: #acb7c1;
    display: inline-block;
    position: absolute;
    cursor: pointer;
    opacity: 0.2;
    font-size: 2px;
    margin-left: 1px;
  }

  .hiddenBottom i {
    margin-left: -2px;
  }

  .hiddenBottom:hover {
    background-color: #783887;
    opacity: 0.8;
    width: 12px;
  }

  .hiddenBottom:hover i {
    margin-left: 0;
    color: white;
  }

</style>
