<template>
  <el-select
    v-if="show"
    ref="visualSelect"
    v-model="selectValue"
    :class="classId"
    popper-class="VisualSelects coustom-de-select"
    no-match-text=" "
    clearable
    v-bind="$attrs"
    v-on="$listeners"
    @change="visualChange"
    @visible-change="popChange"
  >
    <p v-if="startIndex === 0 && $attrs.multiple" class="select-all"><el-checkbox v-model="selectAll" v-customStyle="customStyle" :indeterminate="isIndeterminate" @change="selectAllChane">{{ $t('dataset.check_all') }}</el-checkbox></p>
    <el-option v-for="item in options" :key="item.id" :label="item.text" :value="item.id" :class="setSelect(item.id)" />
  </el-select>
</template>

<script>
import { handlerInputStyle } from '@/components/widget/DeWidget/serviceNameFn.js'

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
    customStyle: {
      type: Object,
      default: () => {}
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
    },
    keyWord: {
      type: String,
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
      defaultFirst: false,
      show: true,
      selectAll: false
    }
  },
  computed: {
    isIndeterminate() {
      return Array.isArray(this.selectValue) && this.selectValue.length > 0 && this.selectValue.length !== this.list.length
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
      this.show = false
      this.$nextTick(() => {
        this.show = true
        this.$nextTick(() => {
          this.init()
        })
      })
    },
    keyWord(val, old) {
      if (val === old) return
      const results = val ? this.list.filter(item => item.text.includes(val)) : null
      this.resetList(results)
      this.reCacularHeight()
      this.$nextTick(() => {
        this.callback()
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
    resetSelectAll() {
      this.selectAll = false
    },
    setSelect(id) {
      if (Array.isArray(this.selectValue)) {
        return this.selectValue.map(ele => ele.id).includes(id) && 'selected'
      }
      return this.selectValue === id && 'selected'
    },
    selectAllChane(val) {
      const vals = val ? [...this.list.map(ele => ele.id)] : []
      this.visualChange(vals)
      this.selectValue = vals
      this.$emit('change', vals)
      this.$emit('handleShowNumber')
    },
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
    customInputStyle() {
      if (!this.$parent.$parent.handlerInputStyle) return
      handlerInputStyle(this.$refs.visualSelect.$el.querySelector('.el-input__inner'), this.$parent.element.style)
      handlerInputStyle(this.$refs.visualSelect.$el.querySelector('.el-select__input'), { wordColor: this.$parent.element.style.wordColor })
    },
    init() {
      if (this.defaultFirst && this.list.length > 0) {
        this.selectValue = this.list[0].value
      }
      if (!this.list || !this.list.length) {
        this.customInputStyle()
        return
      }

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
      this.customInputStyle()
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
      if (this.$attrs.multiple) {
        this.selectAll = val.length === this.list.length
      }
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
.select-all {
    padding: 10px 20px 0 20px;
  }
</style>
