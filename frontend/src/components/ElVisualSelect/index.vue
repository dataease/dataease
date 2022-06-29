<template>
  <el-select
    ref="visualSelect"
    v-model="selectValue"
    :class="classId"
    popper-class="VisualSelects"
    no-match-text=" "
    v-bind="$attrs"
    v-on="$listeners"
    @change="visualChange"
    @visible-change="popChange"
  >
    <el-option v-for="item in options" :key="item.id" :label="item.text" :value="item.id" />
  </el-select>
</template>

<script>
import { uuid } from 'vue-uuid'
export default {
  name: 'ElVisualSelect',
  model: {
    prop: 'value', // 绑定的值，通过父组件传递
    event: 'update' // 自定义名
  },
  props: {
    classId: {
      type: String,
      require: true,
      default: uuid.v1()
    },
    list: {
      type: Array,
      default: () => {
        return []
      }
    },
    value: {
      type: [String, Number, Array],
      default: ''
    }
  },
  data() {
    return {
      newList: [],
      selectValue: this.value,
      options: [],
      domList: null,
      slectBoxDom: null,
      scrollbar: null,
      startIndex: 0,
      endIndex: 0,
      maxLength: 9, // 弹出框最大支持9个条目
      itemHeight: 34, // select组件选项高度
      maxHeightDom: null,
      defaultFirst: false
    }
  },
  watch: {
    value(val) {
      this.selectValue = val
    },
    selectValue(val) {
      this.$emit('update', val)
      if (!val) {
        this.resetList()
        this.maxHeightDom.style.height = this.newList.length * 34 + 'px'
        this.domList.style.paddingTop = 0 + 'px'
      }
    },
    list() {
      this.resetList()
      this.$nextTick(() => {
        this.init()
      })
    }
  },
  mounted() {
    this.resetList()
    this.$nextTick(() => {
      this.init()
    })
  },
  methods: {
    addScrollDiv(selectDom) {
      this.maxHeightDom = document.createElement('div')
      this.maxHeightDom.className = 'el-select-height'
      selectDom.insertBefore(this.maxHeightDom, this.domList)
    },
    reCacularHeight() {
      this.maxHeightDom.style.height = this.newList.length * this.itemHeight + 'px'
    },
    resetList(arrys) {
      if (Array.isArray(arrys)) {
        this.newList = arrys.slice()
        this.domList.style.paddingTop = 0 + 'px'
        this.scrollbar.scrollTop = 0
        this.callback()
      } else {
        this.newList = this.list.slice()
      }
      this.options = this.newList.slice(0, this.maxLength)
    },

    init() {
      if (this.defaultFirst && this.list.length > 0) {
        this.selectValue = this.list[0].value
      }
      if (!this.list || !this.list.length) return

      const selectDom = document.querySelector(
        `.${this.classId} .el-select-dropdown .el-select-dropdown__wrap`
      )
      this.scrollbar = document.querySelector(`.${this.classId} .el-select-dropdown .el-scrollbar`)
      this.slectBoxDom = document.querySelector(`.${this.classId} .el-select-dropdown__wrap`)
      this.slectBoxDom.style.display = 'flex'
      this.slectBoxDom.style.flexDirection = 'row'
      this.domList = selectDom.querySelector(
        `.${this.classId} .el-select-dropdown__wrap .el-select-dropdown__list`
      )
      this.addScrollDiv(this.slectBoxDom)

      this.scrollFn()
    },

    scrollFn() {
      this.scrollbar.addEventListener('scroll', this.callback, false)
    },

    callback() {
      if (!this.scrollbar) return
      const scrollTop = this.scrollbar.scrollTop
      this.startIndex = parseInt(scrollTop / this.itemHeight)
      this.endIndex = this.startIndex + this.maxLength

      this.options = this.newList.slice(this.startIndex, this.endIndex)
      this.domList.style.paddingTop = scrollTop - (scrollTop % this.itemHeight) + 'px'
    },
    popChange() {
      this.domList.style.paddingTop = 0 + 'px'

      this.resetList()
      this.reCacularHeight()
    },
    visualChange(val) {
      this.$emit('visual-change', val)
    }
  }

}
</script>

<style lang="scss">
.VisualSelects {
  .el-scrollbar {
    position: relative;
    height: 251px;
    overflow: inherit;
    overflow-x: hidden;
    content-visibility: auto;
  }
  ::-webkit-scrollbar {
    background: #ffffff !important;
  }
  .el-select-height {
    width: 1px;
    position: absolute;
    top: 0;
    left: 0;
  }
  .el-select-dropdown__list {
    width: 100%;
    position: absolute;
    top: 0;
    left: 0;
  }
  .el-select-dropdown__wrap {
    height: 0;
  }
}
</style>
