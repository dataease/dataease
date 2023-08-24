<template>
  <div class="page-line-container">
    <div
      v-for="(line, index) in lineLocations"
      :key="index"
      :ref="baseLineKey + index"
      class="page-line-item"
      :style="{'top': line + 'px', 'background': panelBg}"
      @mousedown="handleMouseDown"
    >
      <span class="top-span" />
      <span class="bottom-span" />
    </div>
  </div>
</template>

<script>
import { reverseColor } from '@/views/chart/chart/common/common'
export default {
  name: 'PageLineEditor',
  props: {

    canvasStyleData: {
      type: Object,
      require: true
    }
  },
  data() {
    return {
      baseLineKey: 'page-line-',
      lineLocations: [],
      curLineHeight: 0,
      clientStartY: 0,
      startTop: 0,
      movingLineHeight: 0,
      scrollHeight: 0,
      baseLineColor: '#1F2329'
    }
  },
  computed: {
    windowHeight() {
      return window.innerHeight - 56
    },
    panelBg() {
      if (this.canvasStyleData.panel.backgroundType === 'color') {
        return reverseColor(this.canvasStyleData.panel.color)
      }
      return this.baseLineColor
    },
    pdfPageLine() {
      return this.$store.state.canvasStyleData.pdfPageLine
    }
  },

  mounted() {
    this.$nextTick(() => {
      this.init()
    })
  },
  created() {
  },

  methods: {
    resize() {
      this.$nextTick(() => {
        this.init()
      })
    },
    init(scrollHeight) {
      this.lineLocations = []
      this.scrollHeight = scrollHeight || document.getElementById('canvas-id-canvas-main').scrollHeight

      if (this.pdfPageLine?.proportion) {
        this.curLineHeight = this.pdfPageLine.proportion * this.scrollHeight
      } else {
        this.curLineHeight = this.windowHeight
        this.saveLineHeight()
      }
      let curLineLocation = this.curLineHeight
      while (curLineLocation < this.scrollHeight) {
        this.lineLocations.push(curLineLocation)
        curLineLocation += this.curLineHeight
      }
    },
    handleMouseDown(e) {
      this.clientStartY = e.clientY
      this.startTop = parseInt(e.target.style.top)
      document.onmousemove = ev => this.handleMouseMove(e, ev)
      document.onmouseup = () => {
        this.curLineHeight = this.movingLineHeight
        this.saveLineHeight()
        this.init()
        document.onmousemove = document.onmouseup = null
      }
    },
    handleMouseMove(e, ev) {
      const moveInstance = ev.clientY - this.clientStartY
      this.movingLineHeight = this.curLineHeight + moveInstance
      e.target.style.top = this.startTop + moveInstance + 'px'
    },
    saveLineHeight() {
      this.$store.commit('canvasChange')
      this.canvasStyleData.pdfPageLine.proportion = this.curLineHeight / this.scrollHeight
      this.pdfPageLine.proportion = this.curLineHeight / this.scrollHeight
    }
  }
}
</script>

<style lang="scss" scoped>
  .page-line-container {
    /* width: 100%;
    height: 100%; */
  }
  .page-line-item {
    height: 1px;
    width: 100%;
    margin: 2px 0;
    position: absolute;
    z-index: 999;
    cursor: row-resize;
    span {
      width: 20px;
      background: #bbb;
      left: calc(50% - 5px);
      height: 2px;
      position: absolute;
    }
    .top-span {
      top: -3px !important;
    }
    .bottom-span {
      top: 3px !important;
    }
  }
</style>
