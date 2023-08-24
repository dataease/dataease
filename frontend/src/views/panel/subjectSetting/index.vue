<template>
  <el-row class="slider-container">
    <div class="theme-slider-main">
      {{ $t('panel.dashboard_theme') }}
    </div>
    <div class="theme-slider-position" />
    <div>
      <slider
        v-if="sliderShow"
        @reload="sliderReload"
      />
    </div>
    <!--折叠面板-->
    <div
      v-if="collapseShow"
      style="margin: 12px;overflow-y: auto"
    >
      <div>
        <el-collapse
          v-model="activeNames"
          @change="handleChange"
        >
          <el-collapse-item
            :title="$t('panel.overall_setting')"
            name="panel"
          >
            <el-row class="selector-div">
              <overall-setting />
            </el-row>
          </el-collapse-item>
          <el-collapse-item
            :title="$t('panel.panel_background')"
            name="panelBackground"
          >
            <background-selector />
          </el-collapse-item>
          <el-collapse-item
            :title="$t('panel.view_style')"
            name="componentStyle"
          >
            <background-overall />
          </el-collapse-item>
          <el-collapse-item
            :title="$t('panel.view_color_setting')"
            name="graphical"
          >
            <panel-color-selector @onColorChange="onColorChange" />
          </el-collapse-item>
          <el-collapse-item
            :title="$t('panel.chart_title')"
            name="table"
          >
            <view-title @onTextChange="onTextChange" />
          </el-collapse-item>
          <el-collapse-item
            :title="$t('panel.filter_component')"
            name="filterComponent"
          >
            <FilterStyleSelector />
          </el-collapse-item>
        </el-collapse>
      </div>
    </div>
  </el-row>
</template>

<script>
import slider from './preSubject/Slider'
import BackgroundSelector from './panelStyle/BackgroundSelector'
import PanelColorSelector from './panelStyle/PanelColorSelector'
import { mapState } from 'vuex'
import bus from '@/utils/bus'
import OverallSetting from '@/views/panel/subjectSetting/panelStyle/OverallSetting'
import ViewTitle from '@/views/panel/subjectSetting/panelStyle/ViewTitle'
import ComponentStyle from '@/views/panel/subjectSetting/panelStyle/ComponentStyle'
import { adaptCurThemeCommonStyleAll } from '@/components/canvas/utils/style'
import FilterStyleSelector from '@/views/panel/subjectSetting/panelStyle/FilterStyleSelector'
import Background from '@/views/background'
import BackgroundOverall from '@/views/background/BackgroundOverall'

export default {
  components: {
    BackgroundOverall,
    // eslint-disable-next-line
    Background,
    FilterStyleSelector,
    // eslint-disable-next-line
    ComponentStyle,
    ViewTitle,
    slider,
    BackgroundSelector,
    PanelColorSelector,
    OverallSetting
  },
  data() {
    return {
      sliderShow: true,
      panelInfo: this.$store.state.panel.panelInfo,
      activeNames: ['panel'],
      collapseShow: true
    }
  },
  computed: mapState([
    'canvasStyleData',
    'componentData'
  ]),

  watch: {},

  mounted() {
    bus.$on('onSubjectChange', this.onSubjectChange)
  },
  beforeDestroy() {
    bus.$off('onSubjectChange', this.onSubjectChange)
  },
  created() {
    this.init()
  },

  methods: {
    onSubjectChange() {
      this.collapseShow = false
      this.$nextTick(() => {
        this.init()
        this.dataMerge()
        this.collapseShow = true
      })
    },
    sliderReload() {
      this.sliderShow = false
      this.$nextTick(() => {
        this.sliderShow = true
      })
    },
    dataMerge() {
      adaptCurThemeCommonStyleAll()
      this.$store.commit('recordSnapshot')
    },
    init() {
      // 初始化赋值
    },
    handleChange(val) {
    },
    onChangePanelStyle(parma) {
    },
    onColorChange(val) {
      this.themeAttrChange('customAttr', 'color', val)
    },
    onTextChange(val) {
      this.themeAttrChange('customStyle', 'text', val)
    },
    styleChange() {
      this.$store.commit('canvasChange')
    },
    themeAttrChange(custom, property, value) {
      bus.$emit('onThemeAttrChange', {
        'custom': custom,
        'property': property,
        'value': value
      })
      this.$store.commit('recordSnapshot', 'save')
    }
  }
}
</script>

<style lang="scss" scoped>
.slider-container {
  width: 100%;
  min-height: 24px;
  padding-top: 0px;
  padding-bottom: 0px;
  position: relative;
  max-height: 976px;
  color: #3d4d66;
  font-size: 12px;
}

.attr-selector {
  background-color: white;
  height: 32px;
  margin: 5px 5px 5px 5px;
  padding: 0 4px;
  display: flex;
  align-items: center;
  z-index: 10001;
}

.blackTheme .attr-selector {
  background-color: var(--MainBG)
}

.selector-div {
  background-color: var(--MainBG);
}

.padding-lr {
  padding: 0 6px;
}

.theme-slider-main {
  height: 40px;
  padding-left: 15px;
  text-align: left;
  white-space: pre;
  text-overflow: ellipsis;
  left: 0px;
  right: 0px;
  top: 0px;
  font-weight: 700
}

.theme-slider-position {
  height: 1px;
  position: absolute;
  left: 15px;
  right: 15px;
  top: 40px;
  box-sizing: border-box;
  border-bottom: 1px solid #e8eaed
}

::v-deep .el-collapse-item__header {
  font-weight: 500 !important;
  font-size: 14px !important;
  color: var(--TextPrimary, #1F2329);
  padding: 0 !important;
}

</style>
