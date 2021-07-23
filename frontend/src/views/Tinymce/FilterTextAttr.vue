<template>
  <el-card class="el-card-main" :style="mainStyle">
    <div style="position: relative;">
      <el-tooltip :content="$t('panel.fontSize')">

        <i style="float: left;margin-top: 3px;margin-left: 2px;" class="iconfont icon-font_size" />
      </el-tooltip>

      <div style="width: 70px;float: left;margin-top: 2px;margin-left: 2px;">
        <el-input v-model="styleInfo.fontSize" type="number" size="mini" min="12" max="128" @change="styleChange"/>
      </div>

      <el-tooltip :content="$t('panel.fontWeight')">
        <i style="float: left;margin-top: 3px;margin-left: 2px;" class="icon iconfont icon-font-weight-bold" />
      </el-tooltip>

      <div style="width: 70px;float: left;margin-top: 2px;margin-left: 2px;">
        <el-input v-model="styleInfo.fontWeight" type="number" size="mini" min="100" step="100" max="900" @change="styleChange"/>
      </div>

      <el-tooltip :content="$t('panel.letterSpacing')">
        <i style="float: left;margin-top: 3px;margin-left: 2px;" class="icon iconfont icon-letter_spacing" />
      </el-tooltip>

      <div style="width: 70px;float: left;margin-top: 2px;margin-left: 2px;">
        <el-input v-model="styleInfo.letterSpacing" type="number" size="mini" min="0" max="99" @change="styleChange"/>
      </div>

      <div style="width: 20px;float: left;margin-top: 2px;margin-left: 10px;">
        <div style="width: 16px;height: 18px">
          <el-tooltip :content="$t('panel.color')">
            <i class="icon iconfont icon-zimua" @click="goColor" />
          </el-tooltip>
          <div :style="letterDivColor" />
          <el-color-picker ref="colorPicker" v-model="styleInfo.color" style="margin-top: 7px;height: 0px" size="mini" @change="styleChange"/>
        </div>
      </div>

      <div style="width: 20px;float: left;margin-top: 2px;margin-left: 10px;">
        <div style="width: 16px;height: 18px">
          <el-tooltip content="背景颜色">
            <i class="iconfont icon-beijingse1" @click="goBackgroundColor" />
          </el-tooltip>
          <div :style="backgroundDivColor" />
          <el-color-picker ref="backgroundColorPicker" v-model="styleInfo.backgroundColor" style="margin-top: 7px;height: 0px" size="mini" @change="styleChange"/>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script>
import { mapState } from 'vuex'
export default {
  props: {
    scrollLeft: {
      type: Number,
      default: 0
    },
    scrollTop: {
      type: Number,
      default: 0
    }
  },
  data() {
    return {
      textAlignOptions: [
        {
          icon: 'iconfont icon-juzuo',
          tooltip: this.$t('panel.text_align_left'),
          label: 'left'
        },
        {
          icon: 'iconfont icon-align-center',
          tooltip: this.$t('panel.text_align_center'),
          label: 'center'
        },
        {
          icon: 'iconfont icon-juyou',
          tooltip: this.$t('panel.text_align_right'),
          label: 'right'
        }
      ] }
  },
  computed: {

    letterDivColor() {
      const style = {
        height: '2px',
        background: this.styleInfo.color
      }
      return style
    },
    backgroundDivColor() {
      const style = {
        height: '2px',
        background: this.styleInfo.backgroundColor
      }
      return style
    },

    mainStyle() {
      const style = {
        left: (this.getPositionX(this.curComponent.style.left) - this.scrollLeft) + 'px',
        top: (this.getPositionY(this.curComponent.style.top) - this.scrollTop - 3) + 'px'
      }
      return style
    },
    styleInfo() {
      return this.$store.state.curComponent.style
    },
    ...mapState([
      'curComponent',
      'curCanvasScale',
      'canvasStyleData'
    ])

  },
  methods: {
    goColor() {
      this.$refs.colorPicker.handleTrigger()
    },
    goBackgroundColor() {
      this.$refs.backgroundColorPicker.handleTrigger()
    },
    getPositionX(x) {
      if (this.canvasStyleData.selfAdaption) {
        return (x * this.curCanvasScale.scaleWidth / 100) + 60
      } else {
        return x + 60
      }
    },
    getPositionY(y) {
      if (this.canvasStyleData.selfAdaption) {
        return y * this.curCanvasScale.scaleHeight / 100
      } else {
        return y
      }
    },
    styleChange() {
      debugger
      this.$store.state.styleChangeTimes++
    }
  }
}
</script>

<style lang="scss" scoped>
  .attr-list {
    overflow: auto;
    padding: 20px;
    padding-top: 0;
    height: 100%;
  }
  .el-card-main {
    height: 34px;
    z-index: 10;
    width: 350px;
    position: absolute;

  }
  .el-card-main ::v-deep .el-card__body {
    padding: 0px!important;

  }

  ::v-deep .el-radio-button__inner{
    padding: 5px!important;
    width: 30px!important;
  }
</style>
