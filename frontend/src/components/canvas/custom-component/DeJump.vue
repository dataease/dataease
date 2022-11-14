<template>
  <div >
    <div class="nav_calss">
      <el-select 
        class="jump_sel"
        v-model="linkOpen" 
        @change="linkChange" 
        style="width: 100%;"
        :placeholder="getText"
        :popper-append-to-body="inScreen"
        :style="jumpStyle"
        popper-class="jump_sel_pop"
      >
        <el-option v-for="(item,index) in jumpArr" 
          :key="index" :value="item.jumpLink" 
          :label="item.jumpName" :style="fontStyle">
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
    getText() {
      return this.element.options.placeholder !== undefined? this.element.options.placeholder : ''
    },
    fontStyle() {
      let style= {}
      if(this.element.options.nameType === 'back') {
        if(this.element.options.nameBgImg !== '') {
          style.backgroundImage = `url(${this.element.options.nameBgImg})`
        }
        style.backgroundRepeat = 'no-repeat'
        style.backgroundSize = '100% 100%'
      } else {
        style.backgroundColor = this.element.options.nameBgColor
      }
      style.color = this.element.options.color
      return style
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
      // console.log('options',this.element.options,style)
      style.marginTop = (this.element.style.height - 36) + 'px'
      // style.lineHeight = this.element.style.height + 'px'
      return style
    },
  },
  data() {
    return {
      linkOpen: null,
      isClick: false,
    }
  },
  mounted() {
    console.log('de-jump',this.curComponent,this.element)
  },
  methods: {
    linkChange(value) {
      // console.log('跳转',this.linkOpen)
      if(value !== '' && value !== null) {
        window.location.href = value
      }
    }
  },
}
</script>
<style lang="less" scoped>
.first-element {
  position: relative;
  display: table-cell;
  vertical-align: middle;
  margin: 0px;
  padding: 0px;
  height: 100%;
}

.first-element-contaner {
    width: calc(100% - 10px);
    background: initial;
    position: absolute;
    bottom: 0px; // 原值为5px
    margin: 0 4px;

    div {
      width: 100%;
    }

    display: flex;
    align-items: flex-end;
  }

.nav_calss{
  display:flex;
  height: 100%;
}

.jump_sel_pop {
  background-color: transparent;
  border-color: transparent;
}

.jump_sel /deep/ .el-input--medium .el-input__inner {
  background-color: transparent;
  border-color: transparent;
  color: white;
}
.jump_sel /deep/ .el-input__inner::placeholder{
  color: white;
}

/deep/ .el-select__caret::before {
  color: white;
}
/deep/ .el-select-dropdown {
  background-color: transparent !important;
  border-color: transparent !important;
}

</style>