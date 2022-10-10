<template>
  <div ref="chartContainer" style="padding: 0;width: 100%;height: 100%;overflow: hidden;">
    <view-track-bar
      ref="viewTrack"
      :track-menu="trackMenu"
      class="track-bar"
      :style="trackBarStyleTime"
      @trackClick="trackClick"
    />
    <span v-if="chart.type" v-show="title_show" ref="title" :style="title_class" style="cursor: default;display: block;">
      {{ chart.title }}
    </span>
    <div v-if="chart.data && show_Prog" :id="chartId" style="width: 100%;overflow: hidden;" :style="{ borderRadius: borderRadius,'height': title_show? 'calc(100% - 30px);' : '100%;'}">
      <el-row class="prog_box" :style="{width:progStyle.inside? '100%' : '97%'}">
          <el-col :span="progStyle.position === 'top'? 24 : 6" :style="{fontSize: progStyle.fontSize,color: progStyle.color,fontFamily: progStyle.fontFamily}">
            <p style="text-align: center;" >{{progressData.name}}</p>
            <!-- <el-progress :text-inside="progStyle.inside" :color="customColor" :stroke-width="progStyle.strokeWidth" :percentage="progressData.value"></el-progress> -->
          </el-col>
          <el-col :span="progStyle.position === 'top'? 24 : 18">
            <el-progress :text-inside="progStyle.inside" :color="customColor" :stroke-width="progStyle.strokeWidth" :percentage="progressData.value"></el-progress>
          </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import {
  uuid
} from 'vue-uuid'
import { hexColorToRGBA } from '@/views/chart/chart/util'
import ViewTrackBar from '@/components/canvas/components/Editor/ViewTrackBar'

export default {
  name: 'progressBar',
  components: {
    ViewTrackBar
  },
  props: {
    chart: {
      type: Object,
      required: true
    },
    filter: {
      type: Object,
      required: false,
      default: function() {
        return {}
      }
    },
    trackMenu: {
      type: Array,
      required: false,
      default: function() {
        return ['drill']
      }
    },
    searchCount: {
      type: Number,
      required: false,
      default: 0
    },
    terminalType: {
      type: String,
      default: 'pc'
    }
  },
  data() {
    return {
      chartId: uuid.v1(),
      cleWidth: 80,
      mgHeight: '0px',
      trackBarStyle: {
        position: 'absolute',
        left: '0px',
        top: '0px'
      },
      borderRadius: '0px',
      title_show: true,
      title_class: {
        margin: '0 0',
        width: '100%',
        fontSize: '18px',
        color: '#303133',
        textAlign: 'left',
        fontStyle: 'normal',
        fontWeight: 'normal',
        background: hexColorToRGBA('#ffffff', 0),
        fontFamily:  ''
      },
      chartHeight: '100%',
      show_Prog: false,
      progressData: {
        name: '',
        value: '',
      },
      customColor: '#409eff',
      progStyle: {
        fontSize: '14px',
        color: '#000000',
        fontFamily: '',
        inside: true,
        strokeWidth: 20,
        position: 'top'
      }
    }
  },
  computed: {
    trackBarStyleTime() {
      return this.trackBarStyle
    },
    ...mapState([
      'canvasStyleData',
    ])
  },
  watch: {
    chart: {
      handler(newVal,oldVal){
        this.preDraw()
      },
      deep: true
    }
  },
  mounted() {
    this.preDraw()
  },
  methods: {
    chartResize() {
      this.calcHeightDelay()
    },
    trackClick(trackAction) {
      const param = this.pointParam
      if (!param || !param.data || !param.data.dimensionList) {
        // 地图提示没有关联字段 其他没有维度信息的 直接返回
        if (this.chart.type === 'map') {
          this.$warning(this.$t('panel.no_drill_field'))
        }
        return
      }
      const linkageParam = {
        option: 'linkage',
        viewId: this.chart.id,
        dimensionList: this.pointParam.data.dimensionList,
        quotaList: this.pointParam.data.quotaList
      }
      const jumpParam = {
        option: 'jump',
        viewId: this.chart.id,
        dimensionList: this.pointParam.data.dimensionList,
        quotaList: this.pointParam.data.quotaList
      }
      switch (trackAction) {
        case 'drill':
          this.$emit('onChartClick', this.pointParam)
          break
        case 'linkage':
          this.$store.commit('addViewTrackFilter', linkageParam)
          break
        case 'jump':
          this.$emit('onJumpClick', jumpParam)
          break
        default:
          break
      }
    },
    preDraw() {
      this.initTitle()
      this.calcHeightDelay()
      new Promise((resolve) => { resolve() }).then(() => {
        this.drawView()
      })
    },

    drawView() {
      const chart = this.chart
      if(chart.data) {
        if(chart.data.fields.length && chart.data.series.length) {
          this.show_Prog = true
        }else {
          this.show_Prog = false
          return
        }
        if(chart.data.series) {
          const data = chart.data.series[0].data
          this.progressData.name = data[0].dimensionList[0].value
          this.progressData.value = data[0].value
          console.log(this.progressData)
        }
      }
    },

    // title and bg
    initTitle() {
      if (this.chart.customStyle) {
        const customStyle = JSON.parse(this.chart.customStyle)
        // console.log('customStyle,progress',customStyle)
        if (customStyle.text) {
          this.title_show = customStyle.text.show
          // this.title_class.fontSize = customStyle.text.fontSize + 'px'
          this.title_class.color = customStyle.text.color
          this.title_class.textAlign = customStyle.text.hPosition
          this.title_class.fontStyle = customStyle.text.isItalic ? 'italic' : 'normal'
          this.title_class.fontWeight = customStyle.text.isBolder ? 'bold' : 'normal'
          this.title_class.fontFamily = customStyle.text.fontFamily? customStyle.text.fontFamily 
            : this.canvasStyleData.fontFamily? this.canvasStyleData.fontFamily : ''

          if (this.$refs.title) {
            this.$refs.title.style.fontSize = customStyle.text.fontSize + 'px'
          }
        }
        if (customStyle.background) {
          this.title_class.background = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
          this.borderRadius = (customStyle.background.borderRadius || 0) + 'px'
        }
      }

      if (this.chart.customAttr) {
        const customAttr = JSON.parse(this.chart.customAttr)
        // console.log('customAttr,progress',customAttr)

        this.progStyle.fontSize = customAttr.label.progressFontSize + 'px'
        this.progStyle.color = customAttr.label.progressFontColor
        this.progStyle.fontFamily =  this.canvasStyleData.fontFamily
        this.progStyle.inside = customAttr.label.progressInside !== undefined? customAttr.label.progressInside : true
        this.progStyle.strokeWidth = customAttr.label.strokeWidth !== undefined? customAttr.label.strokeWidth : 20
        this.progStyle.position = customAttr.label.progressPosition !== undefined? customAttr.label.progressPosition : 'top'
        this.customColor = customAttr.color.colors[0]
      }
    },

    calcHeightRightNow() {
      this.$nextTick(() => {
        if (this.$refs.chartContainer) {
          const currentHeight = this.$refs.chartContainer.offsetHeight
          if (this.$refs.title) {
            const titleHeight = this.$refs.title.offsetHeight
            this.chartHeight = (currentHeight - titleHeight) + 'px'
          }
        }
      })
    },
    calcHeightDelay() {
      this.calcHeightRightNow()
      setTimeout(() => {
        this.calcHeightRightNow()
      }, 100)
    },
  }
}
</script>
<style lang="scss" scoped>
.prog_box {
  margin: 0px;
  width: 100%;
  height: 100%;
  padding-top: 5px;
  // overflow: hidden;
}
</style>