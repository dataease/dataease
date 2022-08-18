<template>
  <div>

    <div class="space_box" :style="box_style">
      <div class="loacdClass" :style="fontStyle">
        <i class="el-icon-location-information" />
        {{ watherData.city }}
      </div>
      <div class="iconClass" :style="iconStyle">
        <svg-icon :icon-class="newIcon(watherData.type)" />
      </div>
      <div class="statusClass" :style="fontStyle">
        <span>{{ watherData.wendu }}℃ </span> <span> {{ watherData.fengxiang }}</span>
      </div>
    </div>
    <!-- <div class="icon_class" :style="boxStyle">
      <i v-if="iconData.type==='system'" :class="iconData.icon" :style="navStyleSet" />
      <svg-icon v-else :icon-class="iconData.icon" :style="navStyleSet" />

    </div> -->
    <!-- 天气组件 -->
  </div>
</template>
<script>
import axios from 'axios'
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
      heightKey: '',
      timer: null,
      watherData: {
        wendu: '',
        city: '',
        fengxiang: '',
        type: '',
        fengli: '',
        icon: ''
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
    fontStyle() {
      const style = {}
      style.fontSize = (this.element.weatherStyle.fontSize * this.previewCanvasScale.scalePointWidth) + 'px'
      style.color = this.element.weatherStyle.color
      return style
    },
    iconStyle() {
      const style = {}
      style.fontSize = (this.element.weatherStyle.iocnSize * this.previewCanvasScale.scalePointWidth) + 'px'
      return style
    },
    box_style() {
      const style = {}
      style.height = this.element.style.height + 'px'
      return style
    },
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
      return this.element.iconData
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
    // console.log('getLocation', this.getLocation())
    this.getSunType()
    // this.getLocation()
    this.timer = setInterval(() => {
      this.getSunType()
    }, 360000)
  },
  destroyed() {
    clearInterval(this.timer)
  },
  methods: {
    newIcon(key) {
      let name = ''
      switch (key) {
        case '多云':
          name = 'duoyun'
          break
        case '晴':
          name = 'qing'
          break
        case '阴天':
          name = 'yingtian'
          break
        case '小雨':
          name = 'xiaoyu'
          break
        case '雷阵雨':
          name = 'leizhanyu'
          break
        case '大雨':
          name = 'dayu'
          break
        default:
          name = ''
      }
      console.log('name', name)
      return name
    },
    getSunType() {
      console.log('chu00000000000000')
      axios({
        methods: 'get',
        url: 'http://wthrcdn.etouch.cn/weather_mini?city=姑苏区'
        // url: 'http://i.tianqi.com/index.php'
      }).then(res => {
        console.log('天气信息-----', res.data)
        const dataInfo = res.data.data
        this.watherData.wendu = dataInfo.wendu
        this.watherData.city = dataInfo.city
        this.watherData.fengxiang = dataInfo.forecast[0].fengxiang
        this.watherData.type = dataInfo.forecast[0].type
        this.watherData.fengli = dataInfo.forecast[0].fengli
      })
    },
    baseMoseDownEven(e) {
      e.stopPropagation()
    },
    getLocation() {
      if (navigator.geolocation) {
        // navigator.geolocation.getCurrentPosition这个方法里面有三个参数
        // 这个会在界面拉出一个消息框，让用户确认是否允许获取位置,不过pc端我试了都是err，
        // 参1，成功后执行的函数
        // 参2，失败时执行的函数
        // 参3，选项配置，下面是在3000毫秒内结束请求
        navigator.geolocation.getCurrentPosition(
          function(position) {
            var latitude = position.coords.latitude// 纬度
            var longitude = position.coords.longitude// 经度
            console.log('Latitude : ' + latitude + ' Longitude: ' + longitude)
            console.log(position)
          },
          // function(err) {
          //   console.log('您的浏览器不支持此项技术')
          // }, { timeout: 3000 }
        )
      }
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
.space_box{
  padding-left:5px;
  display:flex;
  height:100%;
  width:100%;
  align-items:center;
}
.loacdClass{
   /* flex:1; */
}
.iconClass{
  /* flex:1; */
  font-size:40px;
  padding-left:10px;
  padding-right:10px;
}
.statusClass{
 flex:1;
}
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

