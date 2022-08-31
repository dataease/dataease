<template>

  <div v-if="element.options!== null && element.options.attrs!==null && show" class="de-select-grid-class">
    <div class="de-select-grid-search">
      <el-input
        ref="de-select-grid"
        v-model="keyWord"
        :placeholder="$t('deinputsearch.placeholder')"
        :size="size"
        prefix-icon="el-icon-search"
        clearable
      />
    </div>
    <div class="list">

      <div v-if="element.options.attrs.multiple" class="checkbox-group-container">
        <el-checkbox v-model="checkAll" :indeterminate="isIndeterminate" @change="handleCheckAllChange">
          {{ $t('commons.all') }}</el-checkbox>

        <el-checkbox-group v-model="value" @change="handleCheckedChange">
          <el-checkbox v-for="item in datas.filter(node => !keyWord || (node.id && node.id.includes(keyWord)))" :key="item.id" :label="item.id">{{ item.id }}</el-checkbox>
        </el-checkbox-group>
      </div>

      <div v-else class="radio-group-container">
        <el-radio-group v-model="value" @change="changeRadioBox">
          <el-radio v-for="(item, index) in datas.filter(node => !keyWord || (node.id && node.id.includes(keyWord)))" :key="index" :label="item.id" @click.native.prevent="testChange(item)">
            <span>{{ item.id }}</span>
          </el-radio>
        </el-radio-group>
      </div>

    </div>

  </div>

</template>

<script>
import { multFieldValues, linkMultFieldValues } from '@/api/dataset/dataset'
import { getLinkToken, getToken } from '@/utils/auth'
import bus from '@/utils/bus'
import { isSameVueObj } from '@/utils'
import { attrsMap, styleAttrs, textSelectGridWidget } from '@/components/widget/DeWidget/serviceNameFn.js'

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
    size: String,
    isRelation: {
      type: Boolean,
      default: false
    }
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
      checkAll: false
    }
  },
  computed: {
    operator() {
      return this.element.options.attrs.multiple ? 'in' : 'eq'
    },
    defaultValueStr() {
      if (!this.element || !this.element.options || !this.element.options.value) return ''
      return this.element.options.value.toString()
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
    cssArr() {
      const { brColor, wordColor, innerBgColor } = this.element.style
      return { brColor, wordColor, innerBgColor }
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
      const param = { fieldIds: this.element.options.attrs.fieldId.split(','), sort: this.element.options.attrs.sort }
      if (this.panelInfo.proxy) {
        param.userId = this.panelInfo.proxy
      }
      this.element.options.attrs.fieldId &&
          this.element.options.attrs.fieldId.length > 0 &&
      method(param).then(res => {
        this.datas = this.optionDatas(res.data)
        this.changeInputStyle()
        if (this.element.options.attrs.multiple) {
          this.checkAll = this.value.length === this.datas.length
          this.isIndeterminate = this.value.length > 0 && this.value.length < this.datas.length
        }
      }) || (this.element.options.value = '')
    },
    'element.options.attrs.multiple': function(value, old) {
      if (typeof old === 'undefined' || value === old || isSameVueObj(value, old)) return
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
        this.changeInputStyle()
      })
    },
    'element.options.attrs.sort': function(value, old) {
      if (typeof value === 'undefined' || value === old) return
      this.datas = []
      let method = multFieldValues
      const token = this.$store.getters.token || getToken()
      const linkToken = this.$store.getters.linkToken || getLinkToken()
      if (!token && linkToken) {
        method = linkMultFieldValues
      }
      const param = { fieldIds: this.element.options.attrs.fieldId.split(','), sort: this.element.options.attrs.sort }
      if (this.panelInfo.proxy) {
        param.userId = this.panelInfo.proxy
      }
      this.element.options.attrs.fieldId &&
          this.element.options.attrs.fieldId.length > 0 &&
      method(param).then(res => {
        this.datas = this.optionDatas(res.data)
        this.changeInputStyle()
        if (this.element.options.attrs.multiple) {
          this.checkAll = this.value.length === this.datas.length
          this.isIndeterminate = this.value.length > 0 && this.value.length < this.datas.length
        }
      }) || (this.element.options.value = '')
    },
    cssArr: {
      handler: 'changeInputStyle',
      deep: true
    },
    keyWord: 'changeInputStyle'
  },
  created() {
    if (!this.element.options.attrs.sort) {
      this.element.options.attrs.sort = {}
    }
    this.initLoad()
  },
  mounted() {
    if (this.inDraw) {
      bus.$on('reset-default-value', this.resetDefaultValue)
    }
  },
  beforeDestroy() {
    bus.$off('reset-default-value', this.resetDefaultValue)
  },
  methods: {
    clearHandler() {
      this.value = this.element.options.attrs.multiple ? [] : null
      this.checkAll = false
      this.isIndeterminate = false
    },
    resetDefaultValue(id) {
      if (this.inDraw && this.manualModify && this.element.id === id) {
        this.value = this.fillValueDerfault()
        this.changeValue(this.value)

        if (this.element.options.attrs.multiple) {
          this.checkAll = this.value.length === this.datas.length
          this.isIndeterminate = this.value.length > 0 && this.value.length < this.datas.length
        }
      }
    },
    changeInputStyle() {
      if (!this.$parent.handlerInputStyle) return
      this.$nextTick(() => {
        this.handlerInputStyle(this.element.style)
      })
    },
    handlerInputStyle(newValue) {
      let nodeCache = ''
      if (!this.$refs['de-select-grid']) return
      styleAttrs.forEach(ele => {
        if (!nodeCache) {
          nodeCache = this.$refs['de-select-grid'].$el.querySelector('.el-input__inner')
        }
        nodeCache.style[attrsMap[ele]] = newValue[ele]
        this.textSelectGridWidget(this.$el, ele, newValue[ele])
      })
    },
    textSelectGridWidget: textSelectGridWidget,
    initLoad() {
      this.value = this.element.options.attrs.multiple ? [] : null
      if (this.element.options.attrs.fieldId) {
        let method = multFieldValues
        const token = this.$store.getters.token || getToken()
        const linkToken = this.$store.getters.linkToken || getLinkToken()
        if (!token && linkToken) {
          method = linkMultFieldValues
        }
        method({ fieldIds: this.element.options.attrs.fieldId.split(','), sort: this.element.options.attrs.sort }).then(res => {
          this.datas = this.optionDatas(res.data)
          this.changeInputStyle()
          if (this.element.options.attrs.multiple) {
            this.checkAll = this.value.length === this.datas.length
            this.isIndeterminate = this.value.length > 0 && this.value.length < this.datas.length
          }
        })
      }
      if (this.element.options.value) {
        this.value = this.fillValueDerfault()
        this.changeValue(this.value)
      }
    },
    changeValue(value) {
      if (!this.inDraw) {
        if (value === null) {
          this.element.options.value = ''
        } else {
          this.element.options.value = Array.isArray(value) ? value.join() : value
        }
        this.element.options.manualModify = false
      } else {
        this.element.options.manualModify = true
      }
      this.setCondition()
    },
    getCondition() {
      const param = {
        component: this.element,
        value: this.formatFilterValue(),
        operator: this.operator
      }
      return param
    },
    setCondition() {
      const param = this.getCondition()
      !this.isRelation && this.inDraw && this.$store.commit('addViewFilter', param)
    },
    formatFilterValue() {
      if (this.value === null) return []
      if (Array.isArray(this.value)) return this.value
      return this.value.split(',')
    },
    fillValueDerfault() {
      const defaultV = this.element.options.value === null ? '' : this.element.options.value.toString()
      if (this.element.options.attrs.multiple) {
        if (defaultV === null || typeof defaultV === 'undefined' || defaultV === '' || defaultV === '[object Object]') { return [] }
        return defaultV.split(',')
      } else {
        if (defaultV === null || typeof defaultV === 'undefined' || defaultV === '' || defaultV === '[object Object]') { return null }
        return defaultV.split(',')[0]
      }
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
  .de-select-grid-search {
    ::v-deep input {
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
