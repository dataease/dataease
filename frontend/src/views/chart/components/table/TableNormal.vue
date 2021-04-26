<template>
  <div :style="bg_class">
    <p v-show="title_show" ref="title" :style="title_class">{{ chart.title }}</p>
    <ux-grid
      ref="plxTable"
      size="mini"
      style="width: 100%;"
      :height="height"
      :checkbox-config="{highlight: true}"
      :width-resize="true"
    >
      <ux-table-column
        v-for="field in fields"
        :key="field.originName"
        min-width="200px"
        :field="field.originName"
        :resizable="true"
        sortable
        :title="field.name"
      >
        <!--        <template slot="header">-->
        <!--          <span>{{ field.name }}</span>-->
        <!--        </template>-->
      </ux-table-column>
    </ux-grid>
  </div>
</template>

<script>
import { hexColorToRGBA } from '../../chart/util'

export default {
  name: 'TableNormal',
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
      fields: [],
      height: 500,
      title_class: {
        margin: '8px 0',
        width: '100%',
        fontSize: '18px',
        color: '#303133',
        textAlign: 'left',
        fontStyle: 'normal'
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
      this.fields = JSON.parse(JSON.stringify(this.chart.data.fields))
      const datas = JSON.parse(JSON.stringify(this.chart.data.tableRow))
      this.$refs.plxTable.reloadData(datas)
      this.initStyle()
      window.onresize = function() {
        that.calcHeight()
      }
    },
    calcHeight() {
      const that = this
      setTimeout(function() {
        const currentHeight = document.documentElement.clientHeight
        that.height = currentHeight - 56 - 40 - 84 - that.$refs.title.offsetHeight - 8 * 2 - 20
      }, 100)
    },
    initStyle() {
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

</style>
