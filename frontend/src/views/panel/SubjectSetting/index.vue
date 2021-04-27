<template>
  <el-row class="slider-container">
    <div style="height: 40px; line-height: 40px; padding-left: 15px; text-align: left; white-space: pre; text-overflow: ellipsis; left: 0px; right: 0px; top: 0px; font-weight: 700">仪表盘主题</div>
    <div style="height: 1px; position: absolute; left: 15px; right: 15px; top: 40px; box-sizing:border-box;border-bottom: 1px solid #e8eaed" />
    <div>
      <slider />
    </div>
    <!--折叠面板-->
    <div style="margin: 10px;overflow-y: auto">
      <el-collapse v-model="activeNames" @change="handleChange">
        <el-collapse-item title="仪表盘" name="panel">
          <el-row style="background-color: #f7f8fa; margin: 5px">
            <background-selector class="attr-selector" @changeBackgroundStyle="changeBackgroundStyle" />
            <background-selector class="attr-selector" @changeBackgroundStyle="changeBackgroundStyle" />
            <background-selector class="attr-selector" @changeBackgroundStyle="changeBackgroundStyle" />
            <background-selector class="attr-selector" @changeBackgroundStyle="changeBackgroundStyle" />
          </el-row>
        </el-collapse-item>
        <el-collapse-item title="视图" name="view">
          <div>控制反馈：通过界面样式和交互动效让用户可以清晰的感知自己的操作；</div>
          <div>页面反馈：操作后，通过页面元素的变化清晰地展现当前状态。</div>
        </el-collapse-item>
        <el-collapse-item title="表格" name="table">
          <div>简化流程：设计简洁直观的操作流程；</div>
          <div>清晰明确：语言表达清晰且表意明确，让用户快速理解进而作出决策；</div>
          <div>帮助用户识别：界面简单直白，让用户快速识别而非回忆，减少用户记忆负担。</div>
        </el-collapse-item>
        <el-collapse-item title="过滤组件" name="4">
          <div>用户决策：根据场景可给予用户操作建议或安全提示，但不能代替用户进行决策；</div>
          <div>结果可控：用户可以自由的进行操作，包括撤销、回退和终止当前操作等。</div>
        </el-collapse-item>
      </el-collapse>
    </div>
  </el-row>
</template>

<script>
import slider from './Slider'
import BackgroundSelector from './BackgroundSelector'
export default {
  components: {
    slider,
    BackgroundSelector
  },
  data() {
    return {
      panelInfo: this.$store.state.panel.panelInfo,
      activeNames: ['1'],
      backgroundType: 'color'
    }
  },
  created() {

  },

  methods: {

    handleChange(val) {
      console.log(val)
    },
    changeBackgroundStyle() {
      console.log('changeBackgroundStyle')
    }
  }
}
</script>

<style lang="scss" scoped>
  .slider-container {
    width: 100%;
    overflow: hidden auto;
    min-height: 24px;
    padding-top: 0px;
    padding-bottom: 0px;
    position: relative;
    max-height: 976px;
    color: #3d4d66;
    font-size: 12px;
  }
  .attr-selector{
    background-color: white;
    height: 32px;
    margin: 5px 5px 5px 5px;
    padding:0 4px;
    display: flex;
    align-items: center;
    z-index: 10001;
  }

</style>
