<template>
  <div ref="tableContainer" :style="bg_class">
    <p v-show="title_show" ref="title" :style="title_class">{{ chart.title }}</p>
    <div
      v-if="chart.data && chart.data.x && chart.data.x.length > 0 && chart.data.series && chart.data.series.length > 0 && chart.data.series[0].data && chart.data.series[0].data.length > 0"
      id="label-content"
      :style="content_class"
    >
      <p :style="label_class">
        {{ chart.data.x[0] }}
      </p>
      <span :style="label_space">
        <p v-for="item in chart.data.series" :key="item.name" :style="label_content_class">
          {{ item.data[0] }}
        </p>
      </span>
    </div>
  </div>
</template>

<script>
import { hexColorToRGBA } from '../../chart/util'

export default {
  name: 'LabelNormal',
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
      title_class: {
        margin: '0 0',
        width: '100%',
        fontSize: '18px',
        color: '#303133',
        textAlign: 'left',
        fontStyle: 'normal'
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
      title_show: true
    }
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
  },
  methods: {
    init() {
      const that = this
      this.initStyle()
      window.onresize = function() {
        that.calcHeight()
      }
    },
    calcHeight() {
      const that = this
      setTimeout(function() {
        // const currentHeight = document.documentElement.clientHeight
        // const tableMaxHeight = currentHeight - 56 - 40 - 84 - that.$refs.title.offsetHeight - 20
        const currentHeight = that.$refs.tableContainer.offsetHeight
        const contentHeight = currentHeight - that.$refs.title.offsetHeight
        that.height = contentHeight + 'px'
        that.content_class.height = that.height
      }, 10)
    },
    initStyle() {
      if (this.chart.customAttr) {
        const customAttr = JSON.parse(this.chart.customAttr)
        if (customAttr.color) {
          this.label_class.color = customAttr.color.dimensionColor
          this.label_content_class.color = customAttr.color.quotaColor
        }
        if (customAttr.size) {
          this.label_class.fontSize = customAttr.size.dimensionFontSize + 'px'
          this.label_content_class.fontSize = customAttr.size.quotaFontSize + 'px'
          this.label_space.marginTop = customAttr.size.spaceSplit + 'px'
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
        }
        if (customStyle.background) {
          this.bg_class.background = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
        }
      }
    }
  }
}
</script>

<style scoped>
  .table-class>>>.body--wrapper{
    background: rgba(1,1,1,0);
  }
</style>
