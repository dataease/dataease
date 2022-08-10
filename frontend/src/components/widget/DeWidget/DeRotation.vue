<template>
  <div>
    <div class="block">
      <div class="scroll_box" :style="big_box">
        <span class="left_btn" @click.stop="scrollBtn('lt')">
          <i class="el-icon-arrow-left" />
        </span>
        <span class="right_btn" @click.stop="scrollBtn('rt')">
          <i class="el-icon-arrow-right" />
        </span>
        <div class="scroll_nav_calss" :style="box_width">
          <div v-for="(item,index) in datas" :key="index" :style="boxStyle">
            <div :style="setStyle(item)">
              <span class="title_class" :style="{color:heightlight(item)}" @mousedown="baseMoseDownEven" @click.stop="toggleNav(item)">{{ item.text }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { multFieldValues, linkMultFieldValues } from '@/api/dataset/dataset'
import { getLinkToken, getToken } from '@/utils/auth'
import { mapState } from 'vuex'
import bus from '@/utils/bus'
export default {

  props: {
    element: {
      type: Object,
      default: null
    },
    inDraw: {
      type: Boolean,
      default: true
    },
    inScreen: {
      type: Boolean,
      required: false,
      default: true
    },
    size: String
  },
  data() {
    return {
      value: null,
      checked: null,
      defaultProp: {
        id: 'id',
        label: 'text',
        children: 'children'
      },
      keyWord: '',
      allNode: {
        id: (-2 << 16) + '',
        text: this.$t('commons.all'),
        checked: false,
        indeterminate: false
      },
      show: true,
      datas: [],
      isIndeterminate: false,
      checkAll: false,
      timer: null

    }
  },
  computed: {
    ...mapState([
      'curComponent',
      'componentData',
      'canvasStyleData',
      'previewCanvasScale'
    ]),
    operator() {
      console.log('修改组件问题=====', this.element)
      return this.element.options.attrs.multiple ? 'in' : 'eq'
    },
    setStyle() {
      return function(value) {
        const style = {}
        // console.log('this.element.options', this.element.options)
        if (this.element.options.vertical === 'elementKey') {
          if (this.canvasStyleData.navShowKey === value.text) {
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
          if (this.element.options.heightTabs === value.text) {
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
    heightlight() {
      return function(value) {
        if (this.element.options.vertical === 'elementKey') {
          if (this.canvasStyleData.navShowKey === value.text) {
            return this.element.options.highlight
          // return this.element.options.color
          } else {
            return this.element.options.color
          }
        } else {
          if (this.element.options.heightTabs === value.text) {
            return this.element.options.highlight
          } else {
            return this.element.options.color
          }
        }
      }
    },
    big_box() {
      const style = {}
      console.log('this.element', this.element)
      style.height = this.element.style.height + 'px'
      style.fontWeight = this.element.style.fontWeight
      style.fontSize = this.element.style.fontSize + 'px'
      return style
    },
    defaultValueStr() {
      if (!this.element || !this.element.options || !this.element.options.value) return ''
      return this.element.options.value.toString()
    },
    boxStyle() {
      const style = {}
      style.fontSize = (this.element.options.fontSize * this.previewCanvasScale.scalePointWidth) + 'px'
      style.paddingLeft = this.element.options.spacing + 'px'
      style.paddingRight = this.element.options.spacing + 'px'
      style.width = this.boxWidth + 'px'
      return style
    },
    viewIds() {
      if (!this.element || !this.element.options || !this.element.options.attrs.viewIds) return ''
      return this.element.options.attrs.viewIds.toString()
    },
    manualModify() {
      return !!this.element.options.manualModify
    },
    panelInfo() {
      return this.$store.state.panel.panelInfo
    },
    boxWidth() {
      const b_width = (this.element.style.width - 12) / this.element.options.scrollPage
      console.log('b_width', b_width)
      return Math.floor(b_width)
    },
    box_width() {
      const style = {}
      style.width = (this.boxWidth * this.datas.length) + 'px'
      return style
    },
    bannerStyle() {
      const style = {

      }
      style.lineHeight = this.element.style.height + 'px'
      style.fontWeight = this.element.style.fontWeight
      style.fontSize = this.element.style.fontSize + 'px'
      return style
    },
    scrollTimes() {
      return this.element.options.autoTime * 1000
    }
  },
  watch: {
    'viewIds': function(value, old) {
      if (typeof value === 'undefined' || value === old) return
      this.setCondition()
    },
    'defaultValueStr': function(value, old) {
      if (value === old) return
      this.value = this.fillValueDerfault()
      this.changeValue(value)

      if (this.element.options.attrs.multiple) {
        this.checkAll = this.value.length === this.datas.length
        this.isIndeterminate = this.value.length > 0 && this.value.length < this.datas.length
      }
    },
    'element.options.attrs.fieldId': function(value, old) {
      if (typeof value === 'undefined' || value === old) return
      this.datas = []
      let method = multFieldValues
      const token = this.$store.getters.token || getToken()
      const linkToken = this.$store.getters.linkToken || getLinkToken()
      if (!token && linkToken) {
        method = linkMultFieldValues
      }
      const param = { fieldIds: this.element.options.attrs.fieldId.split(',') }
      if (this.panelInfo.proxy) {
        param.userId = this.panelInfo.proxy
      }
      this.element.options.attrs.fieldId &&
          this.element.options.attrs.fieldId.length > 0 &&
      method(param).then(res => {
        this.datas = this.optionDatas(res.data)
        console.log('this.datas22222222', this.datas)
      }) || (this.element.options.value = '')
    },
    'element.options.autoplay': function(value, old) {
      console.log('判断是否启用轮播模式-------')
      if (this.element.options.autoplay) {
        this.scrollEven()
      } else {
        clearInterval(this.timer)
      }
    },
    'element.options.attrs.multiple': function(value, old) {
      if (typeof old === 'undefined' || value === old) return
      if (!this.inDraw) {
        this.value = value ? [] : null
        this.element.options.value = ''
      } else {
        this.value = this.fillValueDerfault()
      }

      this.show = false
      this.$nextTick(() => {
        this.show = true
        if (value) {
          this.checkAll = this.value.length === this.datas.length
          this.isIndeterminate = this.value.length > 0 && this.value.length < this.datas.length
        }
      })
    }
  },
  created() {
    this.initLoad()
    console.log(':size="size"', this.size)
  },
  mounted() {
    bus.$on('reset-default-value', id => {
      if (this.inDraw && this.manualModify && this.element.id === id) {
        this.value = this.fillValueDerfault()
        this.changeValue(this.value)

        if (this.element.options.attrs.multiple) {
          this.checkAll = this.value.length === this.datas.length
          this.isIndeterminate = this.value.length > 0 && this.value.length < this.datas.length
        }
      }
    })
    setTimeout(() => {
      console.log('数据来源------=========>>>>', this.datas)
    })
  },
  destroyed() {
    clearInterval(this.timer)
  },

  methods: {
    scrollEven() {
      // console.log('123123', key)

      this.timer = setInterval(() => {
        this.scrollBtn('rt')
      }, this.scrollTimes)
    },
    baseMoseDownEven(e) {
      e.stopPropagation()
    },
    scrollBtn(key) {
      console.log('key', key)
      // clearInterval(this.timer)
      if (key === 'lt') {
        const datasll = this.datas[this.datas.length - 1]
        this.datas.splice(this.datas.length - 1, 1)
        this.datas.unshift(datasll)

        // setTimeout(() => {
        // }, 500)
      } else {
        // setTimeout(() => {

        // }, 500)
        const datasll = this.datas[0]
        this.datas.splice(0, 1)
        this.datas.push(datasll)
      }
      this.element.options.heightTabs = this.datas[0].text
      this.changeValue(this.element.options.heightTabs)
      // this.scrollEven()
    },
    toggleNav(key) {
      console.log('keysss', key)
      this.element.options.heightTabs = key.text
      this.changeValue(key.id)
    },
    changeCarousel(e) {
      console.log('element:', this.element)
      if (JSON.stringify(this.datas) !== '[]') {
        console.log('this.datas', this.datas)
        this.datas.forEach((item, index) => {
          if (e === index) {
            console.log('对数据进行解析----', item, index, e)
            this.changeValue(item.id)
          }
          if (e === this.datas.length - 1) {
            this.value = this.datas[0].id
          } else {
            if ((e + 1) === index) {
              this.value = item.id
            }
          }

          // console.log('对数据进行解析----', item, index)
        })
      }
      // this.changeValue(e)
      // JSON.str
      // console.log('轮播图事件触发------', e)
    },
    initLoad() {
      console.log('this.element=======', this.element)
      this.value = this.element.options.attrs.multiple ? [] : null
      if (this.element.options.attrs.fieldId) {
        let method = multFieldValues
        const token = this.$store.getters.token || getToken()
        const linkToken = this.$store.getters.linkToken || getLinkToken()
        if (!token && linkToken) {
          method = linkMultFieldValues
        }
        method({ fieldIds: this.element.options.attrs.fieldId.split(',') }).then(res => {
          this.datas = this.optionDatas(res.data)
          console.log('this.datas1111111111------------->', this.datas)
          if (this.element.options.autoplay) {
            this.scrollEven()
          }

          // setTimeout(() => {
          //   this.changeValue(res.data[0].id)
          // }, 1000)
          if (this.element.options.attrs.multiple) {
            this.checkAll = this.value.length === this.datas.length
            this.isIndeterminate = this.value.length > 0 && this.value.length < this.datas.length
          }
        })
      }
      if (this.element.options.value) {
        console.log('---------------------------------------首次触发--->')
        this.value = this.fillValueDerfault()
        this.changeValue(this.value)
      }
    },
    changeValue(value) {
      console.log('轮播框的值', value, this.inDraw)
      if (!this.inDraw) {
        this.element.options.value = value
      }
      this.element.options.value = Array.isArray(value) ? value.join() : value
      this.setCondition()
    },

    setCondition() {
      const param = {
        component: this.element,
        value: this.formatFilterValue(),
        operator: this.operator
      }
      console.log(' this.inDraw ', this.inDraw)
      this.inDraw && this.$store.commit('addViewFilter', param)
    },
    formatFilterValue() {
      if (this.value === null) return []
      if (Array.isArray(this.value)) return this.value
      return this.value.split(',')
    },
    fillValueDerfault() {
      const defaultV = this.element.options.value === null ? '' : this.element.options.value.toString()
      if (defaultV === null || typeof defaultV === 'undefined' || defaultV === '' || defaultV === '[object Object]') return null
      return defaultV.split(',')[0]
    },
    optionDatas(datas) {
      if (!datas) return null
      return datas.filter(item => !!item).map(item => {
        return {
          id: item,
          text: item
        }
      })
    },
    changeRadioBox(value) {
      this.changeValue(value)
    },
    handleCheckAllChange(val) {
      this.value = val ? this.datas.map(item => item.id) : []
      this.isIndeterminate = false
      this.changeValue(this.value)
    },
    handleCheckedChange(values) {
      const checkedCount = values.length
      this.checkAll = checkedCount === this.datas.length
      this.isIndeterminate = checkedCount > 0 && checkedCount < this.datas.length
      this.changeValue(values)
    },
    testChange(item) {
      this.value = this.value === item.id ? null : item.id
      this.changeRadioBox(this.value)
    }

  }
}

</script>

<style lang="scss" scoped>
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
  // overflow-x:scroll
  overflow:hidden;
}
.nav_info{
  flex:1;
  height:100%;
}
.title_class{
  cursor: pointer;
  height:100%;
}
.banner_class{
  text-align:center;

}
  .de-select-grid-search {
    >>>input {
      border-radius: 0px;

    }

    .el-input {
      display: block !important;
    }
  }

  .de-select-grid-class {
    height: 100%;

    .list {
      overflow-y: auto;
      width: 100%;
      position: relative;
      bottom: 0;
      height: calc(100% - 40px);
      text-align: left;
    }
  }

  .radio-group-container>.el-radio-group>label {
    display: block !important;
    margin: 10px !important;
  }

  .checkbox-group-container {
    label.el-checkbox {
      display: block !important;
      margin: 10px !important;
    }

    .el-checkbox-group>label {
      display: block !important;
      margin: 10px !important;
    }

  }

</style>
