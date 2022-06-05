<template>
  <div>
    <!-- <el-carousel :interval="4000" type="card" height="200px">
      <el-carousel-item v-for="item in 6" :key="item">
        <h3 class="medium">{{ item }}</h3>
      </el-carousel-item>
    </el-carousel> -->
    <!-- <div>设置</div> -->
    <div class="recommendPage swiper-no-swiping">
      <swiper ref="mySwiper" :options="swiperOption" class="swiper-wrapper" :style="bannerStyle">
        <swiper-slide v-for="(item,index) in bannerImgList" :key="index">
          <img :src="item" style="width:100%;height:100%">
        </swiper-slide>
        <!-- <swiper-slide>I'm Slide 2</swiper-slide>
        <swiper-slide>I'm Slide 3</swiper-slide>
        <swiper-slide>I'm Slide 4</swiper-slide>
        <swiper-slide>I'm Slide 5</swiper-slide>
        <swiper-slide>I'm Slide 6</swiper-slide> -->
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
      swiperOption: {
        slidesPerView: 3,
        spaceBetween: 10,
        loop: true,
        autoplay: { delay: 2000, disableOnInteraction: false },
        centeredSlides: true,
        pagination: {
          el: '.swiper-pagina',
          clickable: true
        }
        // loop: true, // 是否循环轮播
        // // 自动轮播
        // // autoplay: true,
        // autoplay: {
        //   delay: 1000,
        //   // 当用户滑动图片后继续自动轮播
        //   disableOnInteraction: false
        // },
        // slidesPerView: 2, // 可是区域内可展示多少个块
        // spaceBetween: 30, // 块之间间隔距离
        // initialSlide: 1, // 默认初始显示块
        // freeMode: false,
        // // 显示分页
        // // pagination: {
        // //   el: '.swiper-pagination',
        // //   clickable: true
        // // },
        // // 设置点击箭头
        // navigation: {
        //   nextEl: '.swiper-button-next',
        //   prevEl: '.swiper-button-prev'
        // }
      }
    }
  },
  computed: {
    ...mapState([
      'curComponent',
      'componentData'
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
      const style = {

      }
      style.height = this.element.style.height + 'px'
      // style.color = '#f96'
      return style
    },
    bannerImgList() {
      console.log('this.curComponent---------------', this.curComponent)
      return this.element.options.bannerImgList
    },
    slidesPerView() {
      if (this.element.options.slidesPerView < this.element.options.bannerImgList.length) {
        return this.element.options.slidesPerView
      } else {
        return this.element.options.bannerImgList.length
      }
    }
  },
  watch: {
    slidesPerView: {
      handler: function(val1, val2) {
        console.log('监听视图层变化=============slidesPerView', val1, val2)
        this.$nextTick(() => {
          this.swiperOption.slidesPerView = val1
        })
      },
      deep: true

    }
  },
  created() {
    console.log('轮播图片组件', this.element)
  },
  mounted() {
    this.$nextTick(() => {
      this.swiperOption = {
        slidesPerView: this.element.options.slidesPerView,
        loop: true,
        spaceBetween: 10,
        autoplay: { delay: this.element.options.rotationTime * 1000, disableOnInteraction: false },
        centeredSlides: true
      }
    })
  }
}
</script>
<style>
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
}
</style>

