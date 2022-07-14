<template>
  <div>
    <img v-if="iconData!==''" :src="iconData" style="width: 100%;height: 100%;">
    <!-- <div class="icon_class"> -->
    <!-- <i v-if="iconData.type==='system'" :class="iconData.icon" :style="navStyleSet" />
      <svg-icon v-else :icon-class="iconData.icon" :style="navStyleSet" /> -->
    <!-- 字体图标 -->
    <!-- </div> -->
    <!-- <div >

    </div> -->
    <!-- <div class="nav_calss" :style="navStyleSet">
      <div v-for="(item,index) in navList" :key="index" class="nav_info">
        <span class="title_class" :style="{color:heightlight(item)}" @mousedown="baseMoseDownEven" @click.stop="toggleNav(item)">{{ item.name }}</span>
      </div>
    </div> -->
  </div>
</template>
<script>
import { mapState } from 'vuex'
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
      style.fontSize = (this.element.iconData.fontSize * this.previewCanvasScale.scalePointWidth) + 'px'
      style.color = this.element.iconData.color
      // style.textAlign = this.element.options.horizontal
      // style.alignItems = this.element.options.vertical
      // style.height = this.element.style.height + 'px'
      // style.fontFamily = this.canvasStyleData.fontFamily
      return style
    },
    boxStyle() {
      const style = {}

      // style.textAlign = this.element.options.horizontal
      // style.alignItems = this.element.options.vertical
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
    iconData() {
      return this.element.picData
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
    toggleNav(key) {
      // 切换导航
      console.log('previewCanvasScale', this.previewCanvasScale)
      console.log('切换导航------ ', this.componentData, this.canvasStyleData)
      const iframeArr = []
      this.canvasStyleData.navShowKey = key.name
      this.componentData.forEach((ele, index) => {
        if (ele.type === 'de-frame') {
          iframeArr.push(ele)
        }
      })
      this.heightKey = key.name

      console.log('key---')
      iframeArr.forEach(ele => {
        document.getElementById('iframe' + ele.id).contentWindow.postMessage(key, '*')
        console.log('网页插件', ele)
      })
    }
  }
}
</script>
<style >
.nav_calss{
  display:flex;
  height: 100%;
}
.icon_class{
  width:100%;
  height:100%;
  display:flex;
  align-items:center;
  justify-content:center;
}
.nav_info{
  flex:1;
}
.title_class{
  cursor: pointer;
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

