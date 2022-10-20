<template>
  <div>
    <div class="nav_calss">
      <el-select 
        class="jump_sel"
        v-model="linkOpen" 
        @change="linkChange" 
        style="width: 100%;"
        :popper-append-to-body="inScreen"
        :style="jumpStyle"
      >
        <el-option v-for="(item,index) in jumpArr" 
          :key="index" :value="item.jumpLink" 
          :label="item.jumpName" :style="{color: fontColor}">
        </el-option>
      </el-select>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'

export default {
  name: 'DeJump',
  props: {
    propValue: {
      type: String,
      require: true
    },
    element: {
      type: Object
    },
    inScreen: {
      type: Boolean,
      required: false,
      default: true
    },
  },
  computed: {
    ...mapState([
      'curComponent',
      'componentData',
      'canvasStyleData',
      'previewCanvasScale'
    ]),
    jumpArr() {
      return this.element.options.jumpList
    },
    fontColor() {
      return this.element.options.color
    },
    jumpStyle() {
      const style = {}
      if(this.element.options.bgType === 'back') {
        if(this.element.options.jumpBgImg !== '') {
          style.backgroundImage = `url(${this.element.options.jumpBgImg})`
        }
        style.backgroundRepeat = 'no-repeat'
        style.backgroundSize = '100% 100%'
      } else {
        style.backgroundColor = this.element.options.jumpBgColor
      }
      style.marginTop = (this.element.style.height - 40) + 'px'
      // style.lineHeight = this.element.style.height + 'px'
      return style
    },
  },
  data() {
    return {
      linkOpen: '',
      isClick: false,
    }
  },
  mounted() {
    console.log('de-jump',this.curComponent,this.element)
  },
  methods: {
    linkChange(value) {
      // console.log('跳转',this.linkOpen)
      if(value !== '') {
        window.location.href = value
      }
    }
  },
}
</script>
<style lang="less" scoped>
.nav_calss{
  display:flex;
  height: 100%;
}

.jump_sel /deep/ .el-input--medium .el-input__inner {
  background-color: transparent;
  border-color: transparent;
}
.jump_sel /deep/ .el-input__inner {
  background-color: transparent;
  border-color: transparent;
}
</style>