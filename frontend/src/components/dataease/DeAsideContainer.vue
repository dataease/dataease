<template>
  <el-aside
    :width="width"
    class="ms-aside-container"
    :style="{'margin-left': !asideHidden ? 0 : '-' + width}"
  >
    <!--<div v-if="enableAsideHidden" class="hiddenBottom" @click="asideHidden = !asideHidden">-->
    <!--<i v-if="!asideHidden" class="el-icon-arrow-left"/>-->
    <!--<i v-if="asideHidden" class="el-icon-arrow-right"/>-->
    <!--</div>-->
    <slot />
    <de-horizontal-drag-bar v-if="showDragBar" />
  </el-aside>
</template>

<script>
import DeHorizontalDragBar from './dragbar/DeLeft2RightDragBar'
export default {
  name: 'DeAsideContainer',
  components: { DeHorizontalDragBar },
  props: {
    width: {
      type: String,
      default: '260px'
    },
    enableAsideHidden: {
      type: Boolean,
      default: true
    },
    showDragBar: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      asideHidden: false
    }
  }
}
</script>

<style scoped>

  .ms-aside-container {
    border: 1px solid #E6E6E6;
    padding: 10px;
    border-radius: 2px;
    box-sizing: border-box;
    background-color: #FFF;
    height: calc(100vh - 56px);
    border-right: 0px;
    position: relative;
  }

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
