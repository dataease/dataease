<template>
  <el-row class="component-wait">
    <el-row class="component-wait-title">
      隐藏的组件
    </el-row>
    <el-row class="component-wait-main">
      <component-wait-item
        v-for="(config, index) in pcComponentData"
        v-if="!config.mobileSelected"
        :id="'wait' + config.id"
        :key="index"
        :config="config"
      />
    </el-row>

  </el-row>

</template>

<script>
import { mapState } from 'vuex'
import ComponentWaitItem from '@/views/panel/edit/ComponentWaitItem'

export default {
  name: 'ComponentWait',
  components: { ComponentWaitItem },
  props: {
    template: {
      type: Object,
      default() {
        return {}
      }
    }
  },
  data() {
    return {
      itemWidth: 280,
      itemHeight: 200,
      outStyle: {
        width: this.itemWidth,
        height: this.itemHeight
      }
    }
  },
  computed: {
    // 移动端编辑组件选择按钮显示
    mobileCheckBarShow() {
      // 显示条件：1.当前是移动端画布编辑状态
      return this.mobileLayoutStatus
    },
    componentItemStyle() {
      return {
        padding: '5px',
        display: 'inline-block',
        width: '33.3333%'
      }
    },
    ...mapState([
      'mobileLayoutStatus',
      'pcComponentData'
    ])
  },
  methods: {
  }
}
</script>

<style scoped>
  .component-wait{
    width: 100%;
    height: 100%;
  }
  .component-wait-title {
    width: 100%;
    height: 30px;
    background-color: #9ea6b2;
    border-bottom: 1px black;
  }
  .component-wait-main {
    width: 100%;
    height: calc(100% - 30px);
    overflow-y: auto;
    background-color: lightgray;
  }
  .component-custom {
    outline: none;
    width: 100% !important;
    height: 100%;
  }
</style>
