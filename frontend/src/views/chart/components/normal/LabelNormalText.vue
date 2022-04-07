<template>
  <div ref="tableContainer" :style="bg_class" style="padding: 8px;width: 100%;height: 100%;overflow: hidden;">
    <p v-show="title_show" ref="title" :style="title_class">{{ chart.title }}</p>
    <div
      v-if="chart.data && chart.data.series && chart.data.series.length > 0"
      id="label-content"
      :style="content_class"
    >
      <span :style="label_class">
        <p v-if="chart.data.series[0].data && chart.data.series[0].data.length > 0" :style="label_content_class">
          {{ chart.data.series[0].data[0] }}
        </p>
      </span>
      <span v-if="dimensionShow" :style="label_space">
        <p :style="label_class">
          {{ chart.data.series[0].name }}
        </p>
      </span>
    </div>
  </div>
</template>

<script>
import { hexColorToRGBA } from '../../chart/util'
import eventBus from '@/components/canvas/utils/eventBus'

export default {
  name: 'LabelNormalText',
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
    }
  },
  data() {
    return {
      height: 'auto',
      splitHeight: '10px',
      dimensionShow: true,
      quotaShow: true,
      title_class: {
        margin: '0 0',
        width: '100%',
        fontSize: '18px',
        color: '#303133',
        textAlign: 'left',
        fontStyle: 'normal',
        fontWeight: 'normal'
      },
      content_class: {
        display: 'flex',
        flex: 1,
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        height: 'auto'
      },
      label_class: {
        margin: 0
      },
      label_content_class: {
        margin: 0
      },
      label_space: {
        marginTop: '10px',
        textAlign: 'center'
      },
      bg_class: {
        background: hexColorToRGBA('#ffffff', 0)
      },
      title_show: true,
      borderRadius: '0px'
    }
  },
  computed: {
    // bg_class() {
    //   return {
    //     background: hexColorToRGBA('#ffffff', 0),
    //     borderRadius: this.borderRadius
    //   }
    // }
  },
  watch: {
    chart() {
      this.init()
      this.calcHeight()
    }
  },
  mounted() {
    this.init()
    this.calcHeight()
    // 监听元素变动事件
    eventBus.$on('resizing', (componentId) => {
      this.chartResize()
    })
  },
  methods: {
    init() {
      const that = this
      this.initStyle()
      window.onresize = function() {
        that.calcHeight()
      }
      this.setBackGroundBorder()
    },
    setBackGroundBorder() {
      if (this.chart.customStyle) {
        const customStyle = JSON.parse(this.chart.customStyle)
        if (customStyle.background) {
          this.borderRadius = (customStyle.background.borderRadius || 0) + 'px'
          this.bg_class.borderRadius = this.borderRadius
        }
      }
    },
    calcHeight() {
      const that = this
      this.$nextTick(function() {
        if (that.$refs.tableContainer) {
          const currentHeight = that.$refs.tableContainer.offsetHeight
          const contentHeight = currentHeight - that.$refs.title.offsetHeight - 16
          that.height = contentHeight + 'px'
          that.content_class.height = that.height
        }
      })
    },
    initStyle() {
      if (this.chart.customAttr) {
        const customAttr = JSON.parse(this.chart.customAttr)
        if (customAttr.color) {
          this.label_class.color = customAttr.color.dimensionColor
          this.label_content_class.color = customAttr.color.quotaColor
        }
        if (customAttr.size) {
          this.dimensionShow = customAttr.size.dimensionShow
          this.quotaShow = customAttr.size.quotaShow
          this.label_class.fontSize = customAttr.size.dimensionFontSize + 'px'
          this.label_content_class.fontSize = customAttr.size.quotaFontSize + 'px'
          if (!this.dimensionShow) {
            this.label_space.marginTop = '0px'
          } else {
            this.label_space.marginTop = customAttr.size.spaceSplit + 'px'
          }
        }
      }
      if (this.chart.customStyle) {
        const customStyle = JSON.parse(this.chart.customStyle)
        if (customStyle.text) {
          this.title_show = customStyle.text.show
          this.title_class.fontSize = customStyle.text.fontSize + 'px'
          this.title_class.color = customStyle.text.color
          this.title_class.textAlign = customStyle.text.hPosition
          this.title_class.fontStyle = customStyle.text.isItalic ? 'italic' : 'normal'
          this.title_class.fontWeight = customStyle.text.isBolder ? 'bold' : 'normal'
        }
        if (customStyle.background) {
          this.bg_class.background = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
        }
      }
    },
    chartResize() {
      // 指定图表的配置项和数据
      this.calcHeight()
    }
  }
}
</script>

<style scoped>
  .table-class>>>.body--wrapper{
    background: rgba(1,1,1,0);
  }
</style>
