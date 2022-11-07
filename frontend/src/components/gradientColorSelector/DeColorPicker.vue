<template>
  <div class="c__color-picker">

    <el-popover
      v-model="visible"
      placement="bottom"
      title=""
      width="314"
      poper-class=""
      trigger="click"
      @hide="whenHide"
    >

      <!-- 颜色面板 -->
      <div
        v-show="visible"
        class=""
      >
        <div class="gradient-div">

          <div
            class="gradient-check"
            :class="activeIndex === 0 ? 'active' : ''"
            @click="checkGradient(0)"
          >
            <div :style="{background: startColor}" />
          </div>
          <span
            class="gradient-div-span"
            :style="{background: gradientColor}"
          />
          <div
            class="gradient-check"
            :class="activeIndex === 1 ? 'active' : ''"
            @click="checkGradient(1)"
          >
            <div :style="{background: endColor}" />
          </div>
        </div>
        <!-- 画布 -->
        <div class="c__canvas">
          <canvas
            :id="canvasId"
            :width="width"
            :height="height"
            @click="onCanvasClick"
            @mousedown="onCanvasMousedown"
          />
          <!-- 当前选中小块 -->
          <em
            :id="curId"
            class="c__cur"
            @mousedown="function(e){onCanvasMousedown(e,'cur')}"
          />
          <!-- 左侧色条选中小块 -->
          <em
            :id="barId"
            class="c__bar"
            @mousedown="function(e){onCanvasMousedown(e,'bar')}"
          />
          <!-- 透明度控制条 -->
          <div
            v-if="showAlpha"
            class="c__alpha-silder"
          >
            <div
              class="c__alpha-silder-bar"
              :style="alphaBarStyle"
              @click="onAlphaClick"
              @mousedown="onAlphaMousedown"
            />
            <em
              :id="alphaId"
              class="c__alpha"
              :style="{left:alpha*100 + '%'}"
              @mousedown="onAlphaMousedown"
            />
          </div>
        </div>
        <!-- 底部按钮栏 -->
        <div class="c__control">
          <input
            class="c__input"
            :value="innerValues[0]"
          >
          <input
            class="c__input"
            :value="innerValues[1]"
          >
          <div class="c__btns">
            <div
              class="c__btn-clear"
              @click="curColor=''"
            >清空</div>
            <div
              class="c__btn-confirm"
              @click="confirm"
            >确定</div>
          </div>
        </div>
      </div>
      <!-- 颜色显示方块 -->
      <div
        slot="reference"
        class="c__block"
        :style="{background: formatBgColor(value)}"
      />
    </el-popover>
  </div>
</template>

<script>
export default {
  name: 'DeColorPicker',
  props: {
    /* 绑定的色值 */
    value: {
      type: Array,
      default: () => {
        return ['rgba(144,202,249,1)', 'rgba(1,87,155,1)']
      }
    },
    /* 是否禁用 */
    disabled: {
      type: Boolean,
      default: false
    },
    /* 是否使用透明度 */
    showAlpha: {
      type: Boolean,
      default: false
    },
    /* 颜色类型，可选值：hex，rgb */
    colorFormat: {
      type: String,
      default: 'hex'
    },
    baseId: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      canvasId: this.baseId + 'canvas-' + Date.now(), // 画布 id，防止多组件时冲突
      curId: this.baseId + 'cur-' + Date.now(), // 当前选中小块 id，防止多组件时冲突
      barId: this.baseId + 'bar-' + Date.now(), // 左侧色条选中小块 id，防止多组件时冲突
      alphaId: this.baseId + 'alpha-' + Date.now(), // 透明度选中小块 id，防止多组件时冲突
      canvas: null, // 画布
      ctx: null, // 画布实例
      curColor: '', // 当前选中颜色
      activeColor: '', // bar选中颜色
      alpha: 1, // 透明度
      width: 286, // canvas宽度
      height: 200, // canvas高度
      activeIndex: 0,
      innerValues: {
        0: this.value[0],
        1: this.value[1]
      },
      visible: false
    }
  },
  computed: {
    alphaBarStyle() {
      return 'background: linear-gradient(to right, rgba(255, 69, 0, 0) 0%, ' + this.activeColor + ' 100%);'
    },
    startColor() {
      return this.innerValues[0]
    },
    endColor() {
      return this.innerValues[1]
    },
    gradientColor() {
      return 'linear-gradient(90.0deg,' + this.innerValues[0] + ' 0.0,' + this.innerValues[1] + ' 100.0%)'
    },
    valueJson() {
      return JSON.stringify(this.value)
    }
  },
  watch: {
    innerValues: {
      handler(old, val) {
        if (old === val) return

        this.curColor = this.innerValues[this.activeIndex]
        this.resetCurColor()
      }
    }

  },
  created() {
    JSON.parse(JSON.stringify(this.value)).forEach((item, index) => {
      this.innerValues[index] = item
    })
    this.setAlpha()
    this.handler = function(e) {
      // _this.visible = false
    }
    document.body.addEventListener('click', this.handler)
    this.$nextTick(() => {
      this.curColor = this.innerValues[this.activeIndex]
      this.activeColor = this.innerValues[this.activeIndex]
      this.resetCurColor()
      this.init()
    })
  },
  beforeDestroy() {
    document.body.removeEventListener('click', this.handler)
  },
  methods: {
    setAlpha() {
      let curColor = this.innerValues[this.activeIndex]
      var reg = /^(rgb|RGB)/
      if (!reg.test(curColor)) {
        curColor = this.hex2rgb(curColor.slice(0, 7))
      }
      var colorArr = curColor.replace(/(?:rgba|rgb|RGBA|RGB|\(|\))*/g, '').split(',')
      if (colorArr.length === 4) {
        this.alpha = colorArr[3]
        return
      }
      this.alpha = 1
    },
    formatBgColor(color) {
      return 'linear-gradient(0.0deg,' + color[0] + ' 0.0,' + color[1] + ' 100.0%)'
    },
    triggerHide() {
      this.visible = false
    },
    whenHide() {
      JSON.parse(JSON.stringify(this.value)).forEach((item, index) => {
        this.innerValues[index] = item
      })
      this.$nextTick(() => {
        this.curColor = this.innerValues[this.activeIndex]
        this.activeColor = this.innerValues[this.activeIndex]
        this.resetCurColor()
        this.init()
      })
    },
    /* 初始化 */
    init() {
      this.canvas = document.getElementById(this.canvasId)
      this.$nextTick(() => {
        this.ctx = this.canvas.getContext('2d')
        this.makeColorBar()
        this.makeColorBox(this.innerValues[this.activeIndex])
      })
    },
    /* 绘制左侧面板颜色选择条 */
    makeColorBar() {
      var gradientBar = this.ctx.createLinearGradient(0, 0, 0, this.height)
      gradientBar.addColorStop(0, '#f00')
      gradientBar.addColorStop(1 / 6, '#f0f')
      gradientBar.addColorStop(2 / 6, '#00f')
      gradientBar.addColorStop(3 / 6, '#0ff')
      gradientBar.addColorStop(4 / 6, '#0f0')
      gradientBar.addColorStop(5 / 6, '#ff0')
      gradientBar.addColorStop(1, '#f00')

      this.ctx.fillStyle = gradientBar
      this.ctx.fillRect(0, 0, 20, this.height)
    },
    /* 绘制颜色选择区域 */
    makeColorBox(color) {
      var gradientBase = this.ctx.createLinearGradient(30, 0, this.width, 0)
      gradientBase.addColorStop(1, color)
      gradientBase.addColorStop(0, 'rgba(255,255,255,1)')
      this.ctx.fillStyle = gradientBase
      this.ctx.fillRect(30, 0, this.width, this.height)

      var my_gradient1 = this.ctx.createLinearGradient(0, 0, 0, this.height)
      my_gradient1.addColorStop(0, 'rgba(0,0,0,0)')
      my_gradient1.addColorStop(1, 'rgba(0,0,0,1)')
      this.ctx.fillStyle = my_gradient1
      this.ctx.fillRect(30, 0, this.width, this.height)
    },
    /* canvas点击 */
    onCanvasClick(e) {
      var ePos = {
        x: e.offsetX || e.layerX,
        y: e.offsetY || e.layerY
      }
      var rgbaStr = '#000'
      if (ePos.x >= 0 && ePos.x < 20) {
        // in
        rgbaStr = this.getRgbaAtPoint(ePos, 'bar')
        var barBlock = document.getElementById(this.barId)
        barBlock.style.top = ePos.y + 'px'
        this.makeColorBox('rgb(' + rgbaStr.slice(0, 3).join() + ')')
        this.innerValues[this.activeIndex] = 'rgba(' + rgbaStr.slice(0, 3).join() + ',' + this.alpha + ')'

        this.$emit('active-change', this.innerValues)
      } else if (ePos.x >= 30) {
        rgbaStr = this.getRgbaAtPoint(ePos, 'box')
        var cur = document.getElementById(this.curId)
        cur.style.left = ePos.x + 'px'
        cur.style.top = ePos.y + 'px'
      } else {
        return
      }
      this.setCurColor(rgbaStr)
    },
    /* canvas鼠标按下，绑定鼠标拖动函数 */
    onCanvasMousedown(ev, type) {
      var self = this
      var ePos = {
        x: ev.layerX || ev.offsetX,
        y: ev.layerY || ev.offsetY
      }
      var offsetTop = parseInt(ev.target.offsetTop)
      var offsetLeft = parseInt(ev.target.offsetLeft)
      if (type === 'cur' || (ePos.x >= 30 && ePos.x < 30 + this.width && ePos.y >= 0 && ePos.y < this.height)) {
        var cur = document.getElementById(this.curId)
        document.onmouseup = function() {
          document.onmouseup = document.onmousemove = null
        }
        document.onmousemove = function(e) {
          try {
            var pos = {
              x: e.clientX - ev.clientX + ev.offsetX + offsetLeft,
              y: e.clientY - ev.clientY + ev.offsetY + offsetTop
            }

            pos.x = pos.x <= 30 ? 30 : pos.x && (pos.x > self.width - 1 ? self.width - 1 : pos.x)
            pos.y = pos.y <= 0 ? 0 : pos.y && (pos.y > self.height - 1 ? self.height - 1 : pos.y)

            var rgbaStr = self.getRgbaAtPoint(pos, 'box')
            self.setCurColor(rgbaStr)
            cur.style.left = pos.x + 'px'
            cur.style.top = pos.y + 'px'
          } catch (e) {
            console.error(e)
          }
        }
      } else if (ePos.x <= 20 && ePos.y <= this.height) {
        var bar = document.getElementById(this.barId)
        document.onmouseup = function() {
          document.onmouseup = document.onmousemove = null
        }
        document.onmousemove = function(e) {
          try {
            var pos = {
              x: 0,
              y: e.clientY - ev.clientY + ev.offsetY + offsetTop
            }

            pos.y = pos.y <= 0 ? 0 : pos.y && (pos.y > self.height - 1 ? self.height - 1 : pos
              .y)

            var rgbaStr = self.getRgbaAtPoint(pos, 'box')
            bar.style.top = pos.y + 'px'
            self.activeColor = 'rgb(' + rgbaStr.slice(0, 3).join() + ')'
            self.makeColorBox('rgb(' + rgbaStr.slice(0, 3).join() + ')')
            self.innerValues[self.activeIndex] = self.activeColor
            self.$emit('active-change', self.innerValues)
          } catch (e) {
            console.error(e)
          }
        }
      }
    },
    /* 透明度控制条点击 */
    onAlphaClick(e) {
      var x = e.offsetX || e.layerX
      var bar = document.getElementById(this.alphaId)
      var parentNode = bar.parentNode
      this.alpha = parseFloat(x / parentNode.clientWidth).toFixed(2)
      this.resetCurColor()
    },
    /* 透明度控制 */
    onAlphaMousedown(ev) {
      var self = this

      var offsetLeft = parseInt(ev.target.offsetLeft)
      var bar = document.getElementById(this.alphaId)
      var parentNode = bar.parentNode
      document.onmouseup = function() {
        document.onmouseup = document.onmousemove = null
      }
      document.onmousemove = function(e) {
        try {
          var pos = {
            x: e.clientX - ev.clientX + ev.offsetX + offsetLeft,
            y: 0
          }

          pos.x = pos.x <= 0 ? 0 : pos.x && (pos.x > parentNode.clientWidth ? parentNode.clientWidth : pos
            .x)

          self.alpha = parseFloat(pos.x / parentNode.clientWidth).toFixed(2)
          self.resetCurColor()
        } catch (e) {
          console.error(e)
        }
      }
    },
    /* 透明度变化重新计算当前颜色值 */
    resetCurColor() {
      var curColor = this.curColor
      var alpha = this.alpha
      var reg = /^(rgb|RGB)/
      if (!reg.test(curColor)) {
        curColor = this.hex2rgb(curColor.slice(0, 7))
      }
      var colorArr = curColor.replace(/(?:rgba|rgb|RGBA|RGB|\(|\))*/g, '').split(',')
      colorArr[3] = alpha
      this.setCurColor(colorArr)
    },
    /* 设置当前颜色值 */
    setCurColor(rgbaStr) {
      var txt = 'a'
      if (rgbaStr.length === 4 && parseFloat(rgbaStr[3]) === 1 || !this.showAlpha) {
        rgbaStr = rgbaStr.slice(0, 3)
        txt = ''
      }
      if (this.colorFormat === 'hex') {
        this.curColor = this.rgb2hex('rgb' + txt + '(' + rgbaStr.join() + ')')
      } else if (this.colorFormat === 'rgb') {
        this.curColor = 'rgb' + txt + '(' + rgbaStr.join() + ')'
      }
      this.innerValues[this.activeIndex] = this.curColor
      this.activeColor = this.innerValues[this.activeIndex]

      this.sendChange()
    },
    /* 确认按钮点击 */
    confirm() {
      const values = []
      Object.keys(this.innerValues).forEach(key => {
        values.push(this.innerValues[key])
      })
      this.$emit('input', values)
      this.visible = false
      this.sendChange()
    },
    /* 触发change回调 */
    sendChange() {
      const values = []
      Object.keys(this.innerValues).forEach(key => {
        values.push(this.innerValues[key])
      })
      this.$emit('change', values)
    },
    /* 获取rgb */
    getRgbaAtPoint(pos, area) {
      let imgData = null
      if (area === 'bar') {
        imgData = this.ctx.getImageData(0, 0, 20, this.height)
      } else {
        imgData = this.ctx.getImageData(0, 0, this.width, this.height)
      }
      var data = imgData.data
      var dataIndex = (pos.y * imgData.width + pos.x) * 4
      /* 开始消除误差 */
      if (pos.x >= 30 && pos.y > this.height - 3) {
        return [0, 0, 0, this.alpha]
      }
      if (pos.x >= 30 && pos.y <= 1) {
        data[dataIndex] = 255
      }
      if (pos.x >= 30 && pos.x <= 31) {
        return [
          data[dataIndex],
          data[dataIndex],
          data[dataIndex],
          this.alpha
        ]
      }
      if (pos.x >= this.width - 1) {
        return [
          data[dataIndex],
          0,
          0,
          this.alpha
        ]
      }
      /* 消除误差结束 */
      return [
        data[dataIndex],
        data[dataIndex + 1],
        data[dataIndex + 2],
        this.alpha
      ]
    },
    /* rgb/rgba色值转16进制 */
    rgb2hex(rgb) {
      var reg = /^(rgb|RGB)/
      var a
      if (reg.test(rgb)) {
        var colorArr = rgb.replace(/(?:rgba|rgb|RGBA|RGB|\(|\))*/g, '').split(',')
        var alpha = (colorArr && colorArr[3] || '').trim()
        var hex = '#' + ((1 << 24) + (parseInt(colorArr[0]) << 16) + (parseInt(colorArr[1]) << 8) + parseInt(
          colorArr[2])).toString(16).slice(1)
        if (alpha !== '' && alpha !== '1') {
          a = ((alpha * 255) | 1 << 8).toString(16).slice(1)
          hex = hex + a
        }
        return hex
      } else {
        return rgb
      }
    },
    /* 16进制色值转rgb */
    hex2rgb(hex) {
      var reg = /^#([0-9a-fA-f]{3}|[0-9a-fA-f]{6})$/
      var color = hex.toLowerCase()
      if (reg.test(color)) {
        if (color.length === 4) {
          var colorNew = '#'
          for (var i = 1; i < color.length; i += 1) {
            colorNew += color.slice(i, i + 1).concat(color.slice(i, i + 1))
          }
          color = colorNew
        }
        var colorChange = []
        for (let i = 1; i < color.length; i += 2) {
          colorChange.push(parseInt('0x' + color.slice(i, i + 2)))
        }
        return 'rgb(' + colorChange.join(',') + ')'
      } else {
        return color
      }
    },
    checkGradient(index) {
      this.activeIndex = index
      this.curColor = this.innerValues[this.activeIndex]
      this.activeColor = this.innerValues[this.activeIndex]
      this.setAlpha()
      this.$nextTick(() => {
        this.resetCurColor()
        this.init()
      })
    }
  }
}
</script>

<style lang="scss">
	.c__color-picker {
		position: relative;
		padding: 3px;
		border: 1px solid #e6e6e6;
		border-radius: 4px;
		display: inline-block;
		margin-right: 0px;
	}

	.c__block {
		width: 18px;
		height: 18px;
		border-radius: 2px;
	}

	.c__panel {
		// position: absolute;
		top: 30px;
		left: -137px;
		z-index: 2001;
		transform-origin: center top;
		padding: 6px;
		box-sizing: content-box;
		background-color: #FFF;
		border: 1px solid #EBEEF5;
		border-radius: 4px;
		box-shadow: 0 2px 12px 0 rgb(0 0 0 / 10%);
	}

	.c__canvas {
		position: relative;
	}

	.c__canvas canvas {
		left: 0;
		top: 0;
	}

	.c__cur {
		width: 3px;
		height: 3px;
		outline: 2px solid #fff;
		margin-left: -1px;
		margin-top: -1px;
		position: absolute;
		border-radius: 50%;
		box-shadow: 0 0 0 1.5px #fff, inset 0 0 1px 1px rgb(0 0 0 / 30%), 0 0 1px 2px rgb(0 0 0 / 40%);
	}

	.c__bar,.c__alpha{
		background-color: #fff;
		position: absolute;
		cursor: pointer;
		border-radius: 1px;
		border: 1px solid #f0f0f0;
		box-shadow: 0 0 2px rgb(0 0 0 / 60%);
	}

	.c__bar {
		width: 20px;
		height: 3px;
		left: 0;
		top: 0;
	}

	.c__alpha{
		width: 5px;
		height: 20px;
		left: 100%;
		top: 0;
		margin-left: -3px;
		margin-top: -1px;
	}

	.c__alpha-silder {
		position: relative;
		box-sizing: border-box;
		width: 100%;
		height: 18px;
		background: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAwAAAAMCAIAAADZF8uwAAAAGUlEQVQYV2M4gwH+YwCGIasIUwhT25BVBADtzYNYrHvv4gAAAABJRU5ErkJggg==);
	}

	.c__alpha-silder-bar {
		position: relative;
		background: linear-gradient(to right, rgba(255, 69, 0, 0) 0%, rgb(255, 69, 0) 100%);
		height: 100%;
	}

	.c__control {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-top: 5px;
	}

	.c__input {
		background-color: #FFF;
		background-image: none;
		border-radius: 4px;
		border: 1px solid #DCDFE6;
		box-sizing: border-box;
		color: #606266;
		display: inline-block;
		outline: 0;
		padding: 0 5px;
		transition: border-color .2s cubic-bezier(.645, .045, .355, 1);
		width: 130px;
		font-size: 12px;
		height: 28px;
		line-height: 28px;
	}

	.c__input:focus {
		border-color: #409EFF;
	}

	.c__btns {
		display: flex;
		justify-content: space-between;
		align-items: center;
	}

	.c__btn-clear {
		font-size: 12px;
		color: #409EFF;
		cursor: pointer;
		display: none;
	}

	.c__btn-confirm {
		padding: 7px 7px;
		margin-left: 0px;
		font-size: 12px;
		border-radius: 3px;
		display: inline-block;
		line-height: 1;
		white-space: nowrap;
		cursor: pointer;
		background: #FFF;
		border: 1px solid #DCDFE6;
		color: #606266;
		text-align: center;
		box-sizing: border-box;
		outline: 0;
		font-weight: 500;
		&:hover {
			border-color: var(--primary, #3370ff);
			color: var(--primary, #3370ff);
		}
	}
	.gradient-div {
		width: 100%;
		height: 30px;
		padding-bottom: 5px;
		display: flex;
		.active {
			border: 1px dashed var(--primary,#3370ff);
		}
	}
	.gradient-div-span {
		width: calc(100% - 60px);
		height: 20px;
		display: inline-block;
		margin: 0 10px;
	}
	.gradient-check {
		width: 20px;
    height: 20px;
    border-radius: 2px;
    border: 1px solid #e6e6e6;
		cursor: pointer;
		div{
			width: 16px;
			height: 16px;
			border-radius: 2px;
			border: 1px solid #e6e6e6;
			margin: 1px 1px;
		}
	}
</style>
