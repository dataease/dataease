<template>
  <el-card class="el-card-main" :style="mainStyle">
    <div style="position: relative;">
      <div style="width: 80px;margin-top: 2px;margin-left: 2px;float: left">
        <el-tooltip content="边框风格">
          <el-select v-model="styleInfo.borderStyle" size="mini" @change="styleChange">
            <el-option
              v-for="item in lineStyle"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            >
              <span style="float: left;">
                <i :class="item.icon" />
              </span>
              <span style="float: right; color: #8492a6; font-size: 12px">{{ item.label }}</span>
            </el-option>
          </el-select>
        </el-tooltip>
      </div>

      <div style="width: 55px;float: left;margin-top: 2px;margin-left: 2px;">
        <el-tooltip content="边框宽度">
          <el-select v-model="styleInfo.borderWidth" size="mini" placeholder="" @change="styleChange">
            <el-option
              v-for="item in lineFont"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-tooltip>
      </div>

      <el-tooltip :content="$t('panel.borderRadius')">
        <i style="float: left;margin-top: 3px;margin-left: 2px;" class="icon iconfont icon-fangxing-" />
      </el-tooltip>

      <div style="width: 70px;float: left;margin-top: 2px;margin-left: 2px;">
        <el-input v-model="styleInfo.borderRadius" type="number" size="mini" min="0" max="100" step="1" @change="styleChange" />
      </div>

      <el-tooltip :content="$t('panel.opacity')">
        <i style="float: left;margin-top: 3px;margin-left: 2px;" class="icon iconfont icon-touming" />
      </el-tooltip>

      <div style="width: 70px;float: left;margin-top: 2px;margin-left: 2px;">
        <el-input v-model="innerOpacity" type="number" size="mini" min="0" max="100" step="10" @change="styleChange" />
      </div>

      <div style="width: 20px;float: left;margin-top: 2px;margin-left: 10px;">
        <div style="width: 16px;height: 18px">
          <el-tooltip content="边框颜色">
            <i class="iconfont icon-huabi" @click="goBoardColor" />
          </el-tooltip>
          <div :style="boardDivColor" />
          <el-color-picker ref="boardColorPicker" v-model="styleInfo.borderColor" style="margin-top: 7px;height: 0px" size="mini" @change="styleChange" />
        </div>
      </div>

      <div style="width: 20px;float: left;margin-top: 2px;margin-left: 10px;">
        <div style="width: 16px;height: 18px">
          <el-tooltip content="背景颜色">
            <i class="iconfont icon-beijingse1" @click="goBackgroundColor" />
          </el-tooltip>
          <div :style="backgroundDivColor" />
          <el-color-picker ref="backgroundColorPicker" v-model="styleInfo.backgroundColor" style="margin-top: 7px;height: 0px" size="mini" @change="styleChange" />
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
      lineStyle: [{
        icon: 'iconfont icon-solid_line',
        value: 'solid',
        label: '实线'
      }, {
        icon: 'iconfont icon-xuxian',
        value: 'dashed',
        label: '虚线'
      }, {
        icon: 'iconfont icon-dianxian',
        value: 'dotted',
        label: '点线'
      }],
      lineFont: [{
        value: '0',
        label: '0'
      }, {
        value: '1',
        label: '1'
      }, {
        value: '2',
        label: '2'
      }, {
        value: '3',
        label: '3'
      }, {
        value: '4',
        label: '4'
      }, {
        value: '5',
        label: '5'
      }],
      innerOpacity: 0
    }
  },
  watch: {
    innerOpacity: {
      handler(oldVal, newVal) {
        this.styleInfo['opacity'] = this.innerOpacity / 100
      }
    }
  },
  mounted() {
    if (this.styleInfo['opacity']) {
      this.innerOpacity = this.styleInfo['opacity'] * 100
    }
  },
  computed: {

    boardDivColor() {
      const style = {
        height: '2px',
        background: this.styleInfo.borderColor
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
        left: this.getPositionX(this.curComponent.style.left - this.scrollLeft) + 'px',
        top: (this.getPositionY(this.curComponent.style.top) - this.scrollTop - 3) + 'px'
      }
      return style
    },
    styleInfo() {
      return this.$store.state.curComponent.style
    },
    canvasWidth() {
      let scaleWidth = 1
      if (this.canvasStyleData.selfAdaption) {
        scaleWidth = this.curCanvasScale.scaleWidth / 100
      }
      return this.canvasStyleData.width * scaleWidth
    },
    ...mapState([
      'curComponent',
      'curCanvasScale',
      'canvasStyleData'
    ])

  },
  methods: {
    goBoardColor() {
      this.$refs.boardColorPicker.handleTrigger()
    },
    goBackgroundColor() {
      this.$refs.backgroundColorPicker.handleTrigger()
    },
    getPositionX(x) {
      let ps = 0
      if (this.canvasStyleData.selfAdaption) {
        ps = (x * this.curCanvasScale.scaleWidth / 100) + 60
      } else {
        ps = x + 60
      }
      // 防止toolbar超出边界
      const xGap = ps + 345 - this.canvasWidth
      if (xGap > 0) {
        return ps - xGap
      } else {
        return ps
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
      this.$store.commit('canvasChange')
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
    width: 400px;
    position: absolute;

  }
  .el-card-main ::v-deep .el-card__body {
    padding: 0px!important;

  }
</style>
