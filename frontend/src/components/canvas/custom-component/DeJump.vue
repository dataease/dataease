<template>
  <div>
    <div class="nav_calss">
      <el-select 
        v-model="linkOpen" 
        @change="linkChange" 
        style="width: 100%;"
        :popper-append-to-body="inScreen"
      >
      <!-- :style="jumpStyle" -->
        <el-option v-for="(item,index) in jumpArr" 
          :key="index" :value="item.jumpLink" 
          :label="item.jumpName" :style="{color: fontColor}">
        </el-option>
      </el-select>
      <!-- <div style="width: 100%;height: 100%">
        <div class="j_select">
          <div class="j_inner">
            <div class="j_inputWarp">
              <el-input size="small" readonly placeholder="请选择" :suffix-icon="isClick? 'el-icon-arrow-down' : 'el-icon-arrow-left'"></el-input>
            </div>
            <ul class="j_ul">
              <li v-for="(item,index) in jumpArr" :key="index" 
                class="j_li" @click="linkChange(item.jumpLink)">
                {{item.jumpName}}
              </li>
            </ul>
          </div>
        </div>
      </div> -->
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
      if(this.element.options.jumpBgImg !== '') {
        style.backgroundImage = `url(${this.element.options.jumpBgImg})`
      }
      style.lineHeight = this.element.style.height + 'px'
      style.backgroundRepeat = 'no-repeat'
      style.backgroundSize = '100% 100%'
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
<style lang="scss">
.nav_calss{
  display:flex;
  height: 100%;
}
</style>