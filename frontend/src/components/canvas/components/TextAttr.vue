<template>
  <el-card class="el-card-main" :style="mainStyle">
    <div id="main-attr" style="position: relative;">
      <div v-if="attrShow('textAlign')" style="width: 100px;float: left;margin-top: 2px;margin-left: 2px;">
        <el-radio-group v-model="styleInfo.textAlign" size="mini" @change="styleChange">
          <el-radio-button
            v-for="item in textAlignOptions"
            :key="item.label"
            :label="item.label"
          >
            <el-tooltip :content="item.tooltip">
              <span style="float: left;">
                <i :class="item.icon" />
              </span>
            </el-tooltip>
          </el-radio-button>
        </el-radio-group>
      </div>
      <div v-if="attrShow('borderStyle')" style="width: 80px;margin-top: 2px;margin-left: 2px;float: left">
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

      <div v-if="attrShow('borderWidth')" style="width: 60px;float: left;margin-top: 2px;margin-left: 2px;">
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
      <el-tooltip v-if="attrShow('fontSize')" :content="$t('panel.fontSize')">
        <i style="float: left;margin-top: 3px;margin-left: 2px;" class="iconfont icon-font_size" />
      </el-tooltip>

      <div v-if="attrShow('fontSize')" style="width: 70px;float: left;margin-top: 2px;margin-left: 2px;">
        <el-input v-model="initFontSize" type="number" size="mini" :min="miniFontSize" :max="maxFontSize" @change="styleChange" />
      </div>

      <el-tooltip v-if="attrShow('fontWeight')" :content="$t('panel.fontWeight')">
        <i style="float: left;margin-top: 3px;margin-left: 2px;" class="icon iconfont icon-font-weight-bold" />
      </el-tooltip>

      <div v-if="attrShow('fontWeight')" style="width: 70px;float: left;margin-top: 2px;margin-left: 2px;">
        <el-input v-model="styleInfo.fontWeight" type="number" size="mini" min="100" step="100" max="900" @change="styleChange" />
      </div>

      <el-tooltip v-if="attrShow('letterSpacing')" :content="$t('panel.letterSpacing')">
        <i style="float: left;margin-top: 3px;margin-left: 2px;" class="icon iconfont icon-letter_spacing" />
      </el-tooltip>

      <div v-if="attrShow('letterSpacing')" style="width: 70px;float: left;margin-top: 2px;margin-left: 2px;">
        <el-input v-model="styleInfo.letterSpacing" type="number" size="mini" min="0" max="99" @change="styleChange" />
      </div>

      <el-tooltip v-if="attrShow('margin')" :content="$t('panel.margin')">
        <i style="float: left;margin-top: 3px;margin-left: 2px;" class="icon iconfont icon-margin" />
      </el-tooltip>

      <div v-if="attrShow('margin')" style="width: 70px;float: left;margin-top: 2px;margin-left: 2px;">
        <el-input v-model="styleInfo.margin" type="number" size="mini" min="0" max="99" @change="styleChange" />
      </div>

      <el-tooltip v-if="attrShow('opacity')" :content="$t('panel.opacity')">
        <i style="float: left;margin-top: 3px;margin-left: 2px;" class="icon iconfont icon-touming" />
      </el-tooltip>

      <div v-if="attrShow('opacity')" style="width: 70px;float: left;margin-top: 2px;margin-left: 2px;">
        <el-input v-model="innerOpacity" type="number" size="mini" min="0" max="100" step="10" @change="styleChange" />
      </div>

      <el-tooltip v-if="attrShow('borderRadius')" :content="$t('panel.borderRadius')">
        <i style="float: left;margin-top: 3px;margin-left: 2px;" class="icon iconfont icon-fangxing-" />
      </el-tooltip>

      <div v-if="attrShow('borderRadius')" style="width: 70px;float: left;margin-top: 2px;margin-left: 2px;">
        <el-input v-model="styleInfo.borderRadius" type="number" size="mini" min="0" max="100" step="1" @change="styleChange" />
      </div>

      <div v-if="attrShow('color')" style="width: 20px;float: left;margin-top: 2px;margin-left: 10px;">
        <div style="width: 16px;height: 18px">
          <el-tooltip :content="$t('panel.color')">
            <i class="icon iconfont icon-zimua" @click="goColor" />
          </el-tooltip>
          <div :style="letterDivColor" />
          <el-color-picker ref="colorPicker" v-model="styleInfo.color" style="margin-top: 7px;height: 0px" size="mini" :predefine="predefineColors" @change="styleChange" />
        </div>
      </div>
      <div v-if="attrShow('borderColor')" style="width: 20px;float: left;margin-top: 2px;margin-left: 10px;">
        <div style="width: 16px;height: 18px">
          <el-tooltip content="边框颜色">
            <i class="iconfont icon-huabi" @click="goBoardColor" />
          </el-tooltip>
          <div :style="boardDivColor" />
          <el-color-picker ref="boardColorPicker" v-model="styleInfo.borderColor" style="margin-top: 7px;height: 0px" size="mini" :predefine="predefineColors" @change="styleChange" />
        </div>
      </div>

      <div v-if="attrShow('backgroundColor')" style="width: 20px;float: left;margin-top: 2px;margin-left: 10px;">
        <div style="width: 16px;height: 18px">
          <el-tooltip content="背景颜色">
            <i class="iconfont icon-beijingse1" @click="goBackgroundColor" />
          </el-tooltip>
          <div :style="backgroundDivColor" />
          <el-color-picker ref="backgroundColorPicker" v-model="styleInfo.backgroundColor" style="margin-top: 7px;height: 0px" :predefine="predefineColors" size="mini" @change="styleChange" />
        </div>
      </div>

      <div v-if="attrShow('hyperlinks')" style="width: 20px;float: left;margin-top: 2px;margin-left: 2px;">
        <el-tooltip v-if="curComponent.hyperlinks" content="超链接">
          <Hyperlinks :link-info="curComponent.hyperlinks" />
        </el-tooltip>
      </div>
      <div v-if="attrShow('videoLinks')" style="width: 20px;float: left;margin-top: 2px;margin-left: 2px;">
        <el-tooltip content="视频信息">
          <VideoLinks :link-info="curComponent.videoLinks" />
        </el-tooltip>
      </div>

      <div v-if="attrShow('streamMediaLinks')" style="width: 20px;float: left;margin-top: 2px;margin-left: 2px;">
        <el-tooltip content="流媒体信息">
          <StreamMediaLinks :link-info="curComponent.streamMediaLinks" />
        </el-tooltip>
      </div>

      <div v-if="attrShow('frameLinks')" style="width: 20px;float: left;margin-top: 2px;margin-left: 2px;">
        <el-tooltip content="网页地址">
          <FrameLinks :link-info="curComponent.frameLinks" />
        </el-tooltip>
      </div>
      <div v-if="attrShow('date-format')" style="width: 20px;float: left;margin-top: 2px;margin-left: 10px;">
        <el-tooltip content="日期格式">
          <date-format :format-info="curComponent.formatInfo" />
        </el-tooltip>
      </div>

      <div v-if="attrShow('deTabStyle')" style="width: 20px;float: left;margin-top: 2px;margin-left: 10px;">
        <el-tooltip content="tab内部样式">
          <tab-style :style-info="styleInfo" />
        </el-tooltip>
      </div>

    </div>
  </el-card>
</template>

<script>
import { mapState } from 'vuex'
import Hyperlinks from '@/components/canvas/components/Editor/Hyperlinks'
import VideoLinks from '@/components/canvas/components/Editor/VideoLinks'
import StreamMediaLinks from '@/components/canvas/components/Editor/StreamMediaLinks'
import DateFormat from '@/components/canvas/components/Editor/DateFormat'
import { COLOR_PANEL } from '@/views/chart/chart/chart'
import FrameLinks from '@/components/canvas/components/Editor/FrameLinks'

export default {
  components: { FrameLinks, Hyperlinks, DateFormat, VideoLinks, StreamMediaLinks },
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
      predefineColors: COLOR_PANEL,
      showMain: true,
      innerOpacity: 0,
      mainWidthOffset: 600,
      initFontSize: 12,
      miniFontSize: 12,
      maxFontSize: 128,
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
      ],
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
      // 矩形组件显示的属性
      'picture-add': [
        'borderStyle',
        'borderWidth',
        'borderColor',
        'hyperlinks'
      ],
      // 过滤组件显示的属性
      'custom': [
        'fontSize',
        'fontWeight',
        'letterSpacing',
        'color'
      ],
      // tab组件显示的属性
      'de-tabs': [
        'borderStyle',
        'borderWidth',
        'borderColor',
        'deTabStyle'
      ],
      // 矩形组件显示的属性
      'rect-shape': [
        'borderStyle',
        'borderWidth',
        'borderColor'
      ],
      // 时间组件显示的属性
      'de-show-date': [
        'textAlign',
        'fontSize',
        'fontWeight',
        'color',
        'date-format',
        'time_margin',
        'padding'
        /* 'margin' */
      ],
      // 文本组件显示的属性
      'v-text': [
        'textAlign',
        'fontSize',
        'fontWeight',
        'letterSpacing',
        'color',
        'hyperlinks'
      ],
      'de-video': [
        'opacity',
        'videoLinks'
      ],
      'de-stream-media': [
        'opacity',
        'streamMediaLinks'
      ],
      'de-frame': [
        'opacity',
        'frameLinks'
      ]
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
    canvasWidth() {
      return this.canvasStyleData.width * this.curCanvasScale.scalePointWidth
    },
    ...mapState([
      'curComponent',
      'curCanvasScale',
      'canvasStyleData'
    ])

  },
  watch: {
    styleInfo: {
      handler(newVal, oldVla) {
        if (newVal.fontSize) {
          this.initFontSize = newVal.fontSize
        }
      },
      deep: true
    },
    innerOpacity: {
      handler(oldVal, newVal) {
        this.styleInfo['opacity'] = this.innerOpacity / 100
      }
    },
    initFontSize: {
      handler(newVal) {
        if (newVal < this.miniFontSize) {
          this.styleInfo.fontSize = this.miniFontSize
        } else if (newVal > this.maxFontSize) {
          this.styleInfo.fontSize = this.maxFontSize
        } else {
          this.styleInfo.fontSize = newVal
        }
      }
    },
    curComponent: {
      handler(oldVal, newVal) {
        this.$nextTick(() => {
          this.init()
        })
        // console.log('curComponent change')
      }
    }
  },
  mounted() {
    this.init()
    if (this.attrShow('fontSize')) {
      this.initFontSize = this.styleInfo.fontSize
    }
  },

  methods: {
    init() {
      if (this.styleInfo['opacity']) {
        this.innerOpacity = this.styleInfo['opacity'] * 100
      }
      if (this.curComponent.type === 'v-text') {
        this.mainWidthOffset = 400
      } else if (this.curComponent.type === 'de-show-date') {
        this.mainWidthOffset = 350
      } else {
        this.mainWidthOffset = document.getElementById('main-attr').offsetWidth - 50
      }
      // console.log('mainWidthOffset:' + this.mainWidthOffset)
    },
    attrShow(attr) {
      // console.log('attr:' + attr + this[this.curComponent.type].includes(attr))
      return this[this.curComponent.type].includes(attr)
    },
    goColor() {
      this.$refs.colorPicker.handleTrigger()
    },
    goBoardColor() {
      this.$refs.boardColorPicker.handleTrigger()
    },
    goBackgroundColor() {
      this.$refs.backgroundColorPicker.handleTrigger()
    },
    getPositionX(x) {
      let ps = 0
      ps = (x * this.curCanvasScale.scalePointWidth) + 60
      // 防止toolbar超出边界
      const xGap = ps + this.mainWidthOffset - this.canvasWidth
      if (xGap > 0) {
        return ps - xGap
      } else {
        return ps
      }
    },
    getPositionY(y) {
      return y * this.curCanvasScale.scalePointHeight
    },
    styleChange() {
      this.$store.commit('recordStyleChange')
    },
    goHeadFontColor() {
      this.$refs.headFontColorPicker.handleTrigger()
    },
    goHeadFontActiveColor() {
      this.$refs.headFontActiveColorPicker.handleTrigger()
    },
    goHeadBorderColor() {
      this.$refs.headBorderColorPicker.handleTrigger()
    },
    goHeadBorderActiveColor() {
      this.$refs.headBorderActiveColorPicker.handleTrigger()
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
    position: absolute;

  }
  .el-card-main ::v-deep .el-card__body {
    padding: 0px!important;

  }

  ::v-deep .el-radio-button__inner{
    padding: 5px!important;
    width: 30px!important;
  }

  ::v-deep .el-color-dropdown__link-btn {
    display: inline!important;
  }

  ::v-deep input::-webkit-outer-spin-button,
  ::v-deep input::-webkit-inner-spin-button {
    -webkit-appearance: none !important;
  }
  ::v-deep input[type='number'] {
    -moz-appearance: textfield !important;
  }

</style>
