<template>
  <el-row class="component-wait">
    <el-tabs
      v-model="activeName"
      class="wait-tab"
      style="padding-left: 10px"
    >
      <el-tab-pane
        :label="$t('panel.component_hidden')"
        name="component-area"
      />
      <el-tab-pane
        :label="$t('panel.mobile_style_setting')"
        name="style-area"
      />
    </el-tabs>
    <el-row
      v-show="activeName === 'component-area'"
      class="component-wait-main"
      :style="mobileCanvasStyle"
    >
      <el-col
        v-for="(config) in pcComponentData"
        v-if="!config.mobileSelected && config.canvasId === 'canvas-main'"
        :id="'wait' + config.id"
        :key="config.id"
        :span="8"
      >
        <component-wait-item
          :config="config"
        />
      </el-col>

    </el-row>
    <el-row
      v-show="activeName === 'style-area'"
      class="component-wait-main"
      style="padding:10px"
    >
      <mobile-background-selector />
    </el-row>
  </el-row>
</template>

<script>
import { mapState } from 'vuex'
import ComponentWaitItem from '@/views/panel/edit/ComponentWaitItem'
import MobileBackgroundSelector from '@/views/panel/subjectSetting/panelStyle/MobileBackgroundSelector'
import { imgUrlTrans } from '@/components/canvas/utils/utils'

export default {
  name: 'ComponentWait',
  components: { MobileBackgroundSelector, ComponentWaitItem },
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
      activeName: 'component-area',
      itemWidth: 280,
      itemHeight: 200,
      outStyle: {
        width: this.itemWidth,
        height: this.itemHeight
      }
    }
  },
  computed: {
    mobileCanvasStyle() {
      let style
      if (this.canvasStyleData.openCommonStyle) {
        const styleInfo = this.canvasStyleData.panel.mobileSetting && this.canvasStyleData.panel.mobileSetting.customSetting
          ? this.canvasStyleData.panel.mobileSetting : this.canvasStyleData.panel
        if (styleInfo.backgroundType === 'image' && typeof (styleInfo.imageUrl) === 'string') {
          style = {
            background: `url(${imgUrlTrans(styleInfo.imageUrl)}) no-repeat`
          }
        } else if (styleInfo.backgroundType === 'color') {
          style = {
            background: styleInfo.color
          }
        } else {
          style = {
            background: '#f7f8fa'
          }
        }
      }
      return style
    },
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
      'canvasStyleData',
      'pcComponentData'
    ])
  },
  methods: {}
}
</script>

<style scoped>
.component-wait {
  width: 100%;
  height: 100%;
}

.component-wait-title {
  width: 100%;
  height: 30px;
  color: #FFFFFF;
  vertical-align: center;
  background-color: #9ea6b2;
  border-bottom: 1px black;
}

.component-wait-main {
  width: 100%;
  height: calc(100% - 40px);
  float: left;
  overflow-y: auto;
}

.component-custom {
  outline: none;
  width: 100% !important;
  height: 100%;
}

.wait-tab {
  height: 40px !important;
  background-color: #9ea6b2;
}
</style>
