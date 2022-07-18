<template>
  <div>
    <div class="recommendPage swiper-no-swiping">
      <swiper v-if="isShow&&bannerImgList.length>0" ref="mySwiper" :options="swiperOption" class="swiper-wrapper" :style="bannerStyle">
        <swiper-slide v-for="(item,index) in bannerImgList" :key="index">
          <div style="width: 100%;height: 100%;">
            <div style="width: 100%;height: 70%;">
              <img :src="item.url" style="width:100%;height:100%;">
            </div>
            <div
              class="img_box"
              :style="{
                'background-color': item.imgBackgroundColor && item.imgOpacity? hexToRgba(item.imgBackgroundColor,item.imgOpacity) : 'none',
                'fontFamily': canvasStyleData.fontFamily ? canvasStyleData.fontFamily : '',
              }"
            >
              <div
                :style="{
                  'fontSize': itemFontSize(item.imgFontSize),
                  'color': item.imgFontColor
                }"
              >{{ item.imgTitle }}</div>
              <div
                :style="{
                  'fontSize': itemFontSize(item.imgFontSize),
                  'color': item.imgFontColor
                }"
              >{{ item.imgContent }}</div>
            </div>
          </div>
        </swiper-slide>
        <div slot="pagination" class="swiper-pagination" />
        <!-- <div slot="button-prev" class="swiper-button-prev" /> -->
        <!-- <div slot="button-next" class="swiper-button-next" /> -->
      </swiper>
    </div>
  </div>
</template>
<script>
import { mapState } from 'vuex'
import { swiper, swiperSlide } from 'vue-awesome-swiper'
import 'swiper/swiper-bundle.css'
export default {
  components: { swiper, swiperSlide },
  props: {
    // eslint-disable-next-line vue/require-default-prop
    element: {
      type: Object,
      require: true
    }
  },

  data() {
    return {
      isShow: true,
      swiperOption: {
        slidesPerView: 1,
        spaceBetween: 5,
        loop: true,
        autoplay: { delay: 2000, disableOnInteraction: false },
        centeredSlides: true,
        pagination: {
          el: '.swiper-pagina',
          clickable: true
        },
        effect: 'coverflow',
        coverflowEffect: {
          rotate: 0, // 滑动时旋转角度
          stretch: 0, // 聚合宽度
          depth: 10, // 深
          modifier: 2, // 覆盖叠加层数
          slideShadows: true // 开启slide阴影。默认 true。
        }
      }
    }
  },
  computed: {
    ...mapState([
      'curComponent',
      'componentData',
      'canvasStyleData',
      'previewCanvasScale'
    ]),
    swiperOptionNew() {
      const options = {
        slidesPerView: 3,
        loop: true,
        spaceBetween: 10,
        autoplay: { delay: 2000, disableOnInteraction: false },
        centeredSlides: true
      }
      options.slidesPerView = this.element.options.slidesPerView
      options.autoplay.delay = this.element.options.rotationTime * 1000
      return options
    },
    bannerStyle() {
      const style = {}
      style.height = this.element.style.height + 'px'
      return style
    },
    imgStyle() {
      const style = {}
      console.log('触发高度比---')
      if (this.element.options.bannerImgList <= 1) {
        style.height = '90%'
      } else {
        style.height = '100%'
      }
      return style
    },
    bannerImgList() {
      console.log('this.curComponent---------------', this.curComponent, this.canvasStyleData)

      return this.element.options.bannerImgList
    },
    rotationTime() {
      return this.element.options.rotationTime
    },
    slidesPerView() {
      return this.element.options.slidesPerView
    },
    pictureGap() {
      return this.element.options.pictureGap
    }
  },
  watch: {
    slidesPerView: {
      handler: function(val1, val2) {
        console.log('监听视图层变化=============slidesPerView', val1, val2)
        this.changeSlidesPerView()
      },
      deep: true

    },
    rotationTime: {
      handler: function(val1, val2) {
        console.log('轮播时间控制----------------------', val1, val2)
        this.changeSlidesPerView()
      },
      deep: true
    },
    pictureGap: {
      handler: function(val1, val2) {
        console.log('设置图片间隔----------------------', val1, val2)
        this.changeSlidesPerView()
      },
      deep: true
    },
    bannerImgList: {
      handler: function(val1, val2) {
        console.log('设置图片数量----------------------', val1, val2)
        this.changeSlidesPerView()
      },
      deep: true
    }
  },
  created() {
    // console.log('轮播图片组件', this.element)
  },
  mounted() {
    this.changeSlidesPerView()
  },
  methods: {
    itemFontSize(size) {
      return Math.ceil(size * this.previewCanvasScale.scalePointWidth) + 'px'
    },
    changeSlidesPerView() {
      if (this.element.options.bannerImgList.length <= 1) {
        this.swiperOption.slidesPerView = 1
      } else {
        this.swiperOption.slidesPerView = this.element.options.slidesPerView
      }

      // this.swiperOption.slidesPerView = this.element.options.slidesPerView
      this.swiperOption.spaceBetween = this.element.options.pictureGap

      this.swiperOption.autoplay = {
        delay: this.element.options.rotationTime * 1000, disableOnInteraction: false
      }
      this.isShow = false
      this.$nextTick(() => {
        this.isShow = true
      })
    },
    // hex -> rgba
    hexToRgba(hex, opacity) {
      // console.log('转化',hex,opacity,typeof hex)
      if (typeof (hex) !== 'string') {
        return 'none'
      }
      return 'rgba(' + parseInt('0x' + hex.slice(1, 3)) + ',' + parseInt('0x' + hex.slice(3, 5)) + ',' +
            parseInt('0x' + hex.slice(5, 7)) + ',' + opacity + ')'
    }
  }
}
</script>
<style >
  .img_box {
    width: 100%;
    height: 30%;
  }
  .img_box div {
    width: 100%;
    word-wrap: break-word;
	  word-break: break-all;
    white-space: break-spaces;
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

