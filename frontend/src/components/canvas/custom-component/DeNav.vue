<template>
  <div>
    <div class="nav_calss" :style="navStyleSet">
      <div v-for="(item,index) in navList" :key="index" class="nav_info" :style="setStyle(item)">
        <span class="title_class" :style="{color:heightlight(item)}" @mousedown="baseMoseDownEven" @click.stop="toggleNav(item)">{{ item.name }}</span>
      </div>
    </div>
  </div>
</template>
<script>
import { mapState } from 'vuex'
import { deepCopy } from '@/components/canvas/utils/utils'
export default {
  props: {
    element: {
      type: Object,
      default: () => {},
      require: true
    }
  },

  data() {
    return {
      // isShow: true,
      heightKey: ''
    }
  },
  computed: {
    ...mapState([
      'curComponent',
      'componentData',
      'canvasStyleData',
      'previewCanvasScale'
    ]),
    navStyleSet() {
      const style = {}
      style.fontSize = (this.element.options.fontSize * this.previewCanvasScale.scalePointWidth) + 'px'
      style.color = this.element.options.color
      style.textAlign = this.element.options.horizontal
      style.alignItems = this.element.options.vertical
      style.height = this.element.style.height + 'px'
      style.fontFamily = this.canvasStyleData.fontFamily
      return style
    },
    heightlight() {
      return function(value) {
        if (this.canvasStyleData.navShowKey === value.name) {
          return this.element.options.highlight
          // return this.element.options.color
        } else {
          return this.element.options.color
        }
      }
    },
    setStyle() {
      return function(value) {
        const style = {}
        console.log('this.element.options', this.element.options)
        if (this.canvasStyleData.navShowKey === value.name) {
          // return this.element.options.highlight
          // return this.element.options.color
          style.backgroundColor = this.element.options.highlightBg
          style.backgroundImage = `url(${this.element.options.heightBgImg})`
        } else {
          // return this.element.options.color
          style.backgroundColor = ''
          style.backgroundImage = null
        }
        style.lineHeight = this.element.style.height + 'px'
        style.backgroundRepeat = 'no-repeat'
        style.backgroundSize = '100% 100%'
        return style
      }
    },
    navList() {
      return this.element.options.navTabList
    }
  },
  watch: {

  },
  created() {
    // console.log('轮播图片组件', this.element)
  },
  mounted() {
    // this.changeSlidesPerView()
  },
  methods: {
    baseMoseDownEven(e) {
      e.stopPropagation()
    },
    commitStyle() {
      const canvasStyleData = deepCopy(this.canvasStyleData)
      console.log('const canvasStyleData', canvasStyleData)
      // canvasStyleData.panel = this.panel
      this.$store.commit('setCanvasStyle', canvasStyleData)
      this.$store.commit('recordSnapshot', 'commitStyle')
    },
    toggleNav(key) {
      // 切换导航
      console.log('previewCanvasScale', this.previewCanvasScale)
      console.log('切换导航------ ', this.componentData, this.canvasStyleData)
      const iframeArr = []
      this.canvasStyleData.navShowKey = key.name
      this.commitStyle()
      this.componentData.forEach((ele, index) => {
        if (ele.type === 'de-frame') {
          iframeArr.push(ele)
        }
      })
      this.heightKey = key.name

      console.log('key---')
      if (JSON.stringify(iframeArr) !== '[]') {
        iframeArr.forEach(ele => {
          document.getElementById('iframe' + ele.id).contentWindow.postMessage(key, '*')
          console.log('网页插件', ele)
        })
      }
    }
  }
}
</script>
<style >
.nav_calss{
  display:flex;
  height: 100%;
}
.nav_info{
  flex:1;
  height:100%;
}
.title_class{
  cursor: pointer;
  height:100%;
}
  /* .el-carousel__item h3 {
    color: #475669;
    font-size: 14px;
    opacity: 0.75;
    line-height: 200px;
    margin: 0;
  }

  .el-carousel__item:nth-child(2n) {
    background-color: #99a9bf;
  }

  .el-carousel__item:nth-child(2n+1) {
    background-color: #d3dce6;
  } */
  .recommendPage{
    /* padding:10px; */
  }
  .recommendPage .swiper-container {
  position: relative;
  width: 100%;
  /* height: 200px; */
}
.recommendPage .swiper-container .swiper-slide {
  width: 100%;
  /* line-height: 200px; */
  /* border:1px solid green; */
  color: #000;
  font-size: 16px;
  text-align: center;
  vertical-align:middle;
  height:100%;
  display:flex;
  align-items:flex-end;
  justify-content:center;
}
.recommendPage .swiper-container .swiper-slide-active img{
  /* height:100% !important;
   */
   height:100% !important;
   /* transform:scaleX(1.5) */
}
</style>

