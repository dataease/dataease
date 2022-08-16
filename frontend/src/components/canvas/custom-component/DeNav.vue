<template>
  <div>
    <div v-show="element.options.pattern !=='scroll'" class="nav_calss" :style="navStyleSet">
      <div v-for="(item,index) in navList" :key="index" class="nav_info" :style="boxStyle">
        <div :style="setStyle(item)">
          <span class="title_class" :style="{color:heightlight(item)}" @mousedown="baseMoseDownEven" @click.stop="toggleNav(item)">{{ item.name }}</span>
        </div>
      </div>
    </div>

    <!-- <div v-show="element.options.pattern=='scroll'&&element.options.vertical=='elementKey'" class="scroll_box">
      <span class="left_btn" @click.stop="scrollBtn('lt')">
        <i class="el-icon-arrow-left" />
      </span>
      <span class="right_btn" @click.stop="scrollBtn('rt')">
        <i class="el-icon-arrow-right" />
      </span>
      <div class="scroll_nav_calss" :style="box_width">
        <div v-for="(item,index) in navList" :key="index" :style="boxStyle">
          <div :style="setStyle(item)">
            <span class="title_class" :style="{color:heightlight(item)}" @mousedown="baseMoseDownEven" @click.stop="toggleNav(item)">{{ item.name }}</span>
          </div>
        </div>
      </div>
    </div> -->
  </div>
</template>
<script>
// import { swiper, swiperSlide } from 'vue-awesome-swiper'
import 'swiper/swiper-bundle.css'
import { mapState } from 'vuex'
import { deepCopy } from '@/components/canvas/utils/utils'
export default {
  // components: { swiper, swiperSlide },
  props: {
    element: {
      type: Object,
      default: () => {},
      require: true
    }
  },

  data() {
    return {
      isShow: true,
      move: 0,
      heightKey: '',
      oldName: '',
      swiperOption: {
        slidesPerView: 2,
        spaceBetween: 5,
        loop: true,
        autoplay: { delay: 3000, disableOnInteraction: false },
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
        if (this.element.options.vertical !== 'elementKey') {
          if (this.canvasStyleData.navShowKey === value.name) {
            return this.element.options.highlight
          // return this.element.options.color
          } else {
            return this.element.options.color
          }
        } else {
          if (this.element.options.heightTabs === value.name) {
            return this.element.options.highlight
          } else {
            return this.element.options.color
          }
        }
      }
    },
    boxStyle() {
      const style = {}
      style.paddingLeft = this.element.options.spacing + 'px'
      style.paddingRight = this.element.options.spacing + 'px'
      style.width = this.boxWidth + 'px'
      return style
    },
    setStyle() {
      return function(value) {
        const style = {}
        console.log('this.element.options', this.element.options)
        if (this.element.options.vertical !== 'elementKey') {
          if (this.canvasStyleData.navShowKey === value.name) {
          // return this.element.options.highlight
          // return this.element.options.color
            style.backgroundColor = this.element.options.highlightBg
            if (this.element.options.heightBgImg) {
              style.backgroundImage = `url(${this.element.options.heightBgImg})`
            }
          } else {
          // return this.element.options.color
            style.backgroundColor = ''
            if (this.element.options.defaultBg) {
              style.backgroundImage = `url(${this.element.options.defaultBg})`
            }
          }
        } else {
          if (this.element.options.heightTabs === value.name) {
            style.backgroundColor = this.element.options.highlightBg
            if (this.element.options.heightBgImg) {
              style.backgroundImage = `url(${this.element.options.heightBgImg})`
            }
          } else {
            style.backgroundColor = ''
            if (this.element.options.defaultBg) {
              style.backgroundImage = `url(${this.element.options.defaultBg})`
            }
          }
        }

        style.lineHeight = this.element.style.height + 'px'
        style.backgroundRepeat = 'no-repeat'
        style.backgroundSize = '100% 100%'
        style.textAlign = this.element.options.horizontal
        return style
      }
    },
    navList() {
      return this.element.options.navTabList
    },
    rotationTime() {
      return this.element.options.autoTime
    },
    slidesPerView() {
      return this.element.options.scrollPage
    },
    boxWidth() {
      const b_width = this.element.style.width / this.element.options.scrollPage
      console.log('b_width', b_width)
      return Math.floor(b_width)
    },
    box_width() {
      const style = {}
      style.width = (this.boxWidth * this.navList.length) + 'px'
      return style
    }
    // pictureGap() {
    //   return this.element.options.pictureGap
    // }
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
    // pictureGap: {
    //   handler: function(val1, val2) {
    //     console.log('设置图片间隔----------------------', val1, val2)
    //     this.changeSlidesPerView()
    //   },
    //   deep: true
    // },
    navList: {
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
    this.oldName = this.element.options.heightTabs
    if (this.element.options.pattern === 'scroll' && this.element.options.vertical === 'elementKey') {
      this.changeSlidesPerView()
    }
  },
  methods: {
    scrollBtn(key) {
      console.log('123123', key)
      if (key === 'lt') {
        const datas = this.navList[this.navList.length - 1]
        this.navList.splice(this.navList.length - 1, 1)
        this.navList.unshift(datas)

        setTimeout(() => {
          // this.move = this.move + 200
        }, 500)
      } else {
        // this.move = this.move - 200
        setTimeout(() => {

        }, 500)
        const datas = this.navList[0]
        this.navList.splice(0, 1)
        this.navList.push(datas)
      }
      this.element.options.heightTabs = this.navList[0].name
      console.log('this.move', this.move)
    },
    baseMoseDownEven(e) {
      e.stopPropagation()
    },
    changeSlidesPerView() {
      if (this.element.options.navTabList.length <= 1) {
        this.swiperOption.slidesPerView = 1
      } else {
        this.swiperOption.slidesPerView = this.element.options.scrollPage
      }

      // this.swiperOption.slidesPerView = this.element.options.slidesPerView
      this.swiperOption.spaceBetween = this.element.options.spacing

      this.swiperOption.autoplay = {
        delay: this.element.options.autoTime * 1000, disableOnInteraction: false
      }
      this.isShow = false
      this.$nextTick(() => {
        this.isShow = true
      })
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
      if (this.canvasStyleData.showArr) {
        const newArr = this.canvasStyleData.showArr
        newArr.forEach((keys, index) => {
          console.log('keys', keys)
          if (this.oldName === keys) {
            newArr.splice(index, 1)
            // delete keys
          }
        })
        console.log('newArrnewArrnewArrnewArrnewArrnewArrnewArr', this.oldName, 'this.oldName', newArr)
        this.canvasStyleData.showArr = newArr
      }

      // console.log('previewCanvasScale', this.previewCanvasScale)
      // console.log('切换导航------ ', this.componentData, this.canvasStyleData)
      const iframeArr = []
      if (this.element.options.vertical !== 'elementKey') {
        // this.canvasStyleData.navModel = 'defult'
        this.canvasStyleData.navShowKey = key.name
      } else {
        // this.canvasStyleData.navModel = 'independent'
        console.warn('chi-------111111111-------chu', this.canvasStyleData.showArr)
        if (this.canvasStyleData.showArr) {
          console.warn('chi-------22222222-------chu', this.canvasStyleData.showArr)
          if (this.canvasStyleData.showArr.indexOf(key.name) === -1) {
            this.canvasStyleData.showArr.push(key.name)
          }
        } else {
          this.canvasStyleData.showArr = []
          if (this.canvasStyleData.showArr.indexOf(key.name) === -1) {
            this.canvasStyleData.showArr.push(key.name)
          }
        }
        // this.canvasStyleData.showArr.push(key.name)
        this.element.options.heightTabs = key.name
      }
      this.oldName = key.name

      // 处理主tab切换后，子tab对应的元素组件也隐藏
      console.warn('---处理主tab切换后，子tab对应的元素组件也隐藏---')
      console.warn('数据源', this.componentData, this.canvasStyleData)
      if (this.element.options.vertical !== 'elementKey') {
        let chengkey = true
        this.componentData.forEach(res => {
          console.log('res', res)
          if (res.showName === key.name) {
            console.warn(res)
            // res.options.heightTabs = res.options.navTabList[0].name
            this.canvasStyleData.showArr = []
            this.canvasStyleData.showArr.push(res.options.heightTabs)
            chengkey = false
          }
        })
        if (chengkey) {
          this.canvasStyleData.showArr = []
        }
      }

      console.warn('--end---')
      // --end

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
.scroll_nav_calss{
  display:flex;

}
.left_btn{
  position:absolute;
  left:-4px;
  top:50%;
  transform:translateY(-50%);
  z-index:999;
  font-size:24px;
  cursor:pointer;
}
.right_btn{
  position:absolute;
  right:-4px;
  top:50%;
  transform:translateY(-50%);
  z-index:999;
  font-size:24px;
   cursor:pointer;
}
.scroll_box{
  position:relative;
  /* padding:0 20px; */
  overflow-x:scroll
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

