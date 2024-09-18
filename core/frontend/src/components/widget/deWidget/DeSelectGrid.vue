<template>

  <div
    v-if="element.options!== null && element.options.attrs!==null && show"
    class="de-select-grid-class"
  >
    <div
      class="de-select-grid-search"
      :class="{'show-required-tips': showRequiredTips}"
    >
      <el-input
        ref="de-select-grid"
        v-model="keyWord"
        :placeholder="showRequiredTips ? $t('panel.required_tips') : $t('deinputsearch.placeholder')"
        :size="size"
        prefix-icon="el-icon-search"
        clearable
        @input="filterMethod"
        @clear="filterMethod"
      />
    </div>
    <div class="list">

      <div
        v-if="element.options.attrs.multiple"
        class="checkbox-group-container"
      >
        <el-checkbox
          v-model="checkAll"
          class="is-tree-select"
          :indeterminate="isIndeterminate"
          @change="handleCheckAllChange"
        >
          {{ $t('commons.all') }}
        </el-checkbox>
        <el-checkbox-group
          v-model="value"
          @change="handleCheckedChange"
        >
          <template v-for="item in dataWithEmpty">
            <el-checkbox
              :key="item.id"
              class="is-tree-select"
              :label="item.id"
            >{{ item.label || item.id }}
            </el-checkbox>
            <br :key="item.id">
          </template>
        </el-checkbox-group>
      </div>

      <div
        v-else
        class="radio-group-container"
      >
        <el-radio-group
          v-model="value"
          @change="changeRadioBox"
        >
          <el-radio
            v-for="item in dataWithEmpty"
            :key="item.id"
            :label="item.id"
            @click.native.prevent="testChange(item)"
          >
            <span>{{ item.label || item.id }}</span>
          </el-radio>
        </el-radio-group>
      </div>

    </div>

  </div>

</template>

<script>
import { linkMultFieldValues, multFieldValues } from '@/api/dataset/dataset'
import { getLinkToken, getToken } from '@/utils/auth'
import bus from '@/utils/bus'
import { isSameVueObj, mergeCustomSortOption } from '@/utils'
import { attrsMap, styleAttrs, textSelectGridWidget } from '@/components/widget/deWidget/serviceNameFn.js'

export default {
  props: {
    canvasId: {
      type: String,
      required: true
    },
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
      data: [],
      isIndeterminate: false,
      checkAll: false,
      timeMachine: null,
      changeIndex: 0
    }
  },
  computed: {
    dataWithEmpty() {
      return this.element.options.attrs.showEmpty ? [{ label: '空数据', id: '_empty_$' }, ...this.data] : this.data
    },
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
    },
    isCustomSortWidget() {
      return this.element.serviceName === 'textSelectGridWidget'
    },
    showRequiredTips() {
      return this.inDraw && this.element.options.attrs.required && (!this.value || !this.value.length)
    }
  },
  watch: {
    'value': function(val, old) {
      if (!this.inDraw) {
        this.$emit('widget-value-changed', val)
      }
    },
    'viewIds': function(value, old) {
      if (typeof value === 'undefined' || value === old) return
      this.setCondition()
    },
    'defaultValueStr': function(value, old) {
      if (value === old) return
      this.value = this.fillValueDerfault()
      this.changeValue(value)

      if (this.element.options.attrs.multiple) {
        this.checkAll = this.value.length === this.dataWithEmpty.length
        this.isIndeterminate = this.value.length > 0 && this.value.length < this.dataWithEmpty.length
      }
    },
    'element.options.attrs.fieldId': function(value, old) {
      if (typeof value === 'undefined' || value === old) return
      this.data = []
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
        this.data = this.optionData(res.data)
        this.clearDefault(this.data)
        this.changeInputStyle()
        if (this.element.options.attrs.multiple) {
          this.checkAll = this.value.length === this.dataWithEmpty.length
          this.isIndeterminate = this.value.length > 0 && this.value.length < this.dataWithEmpty.length
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
          this.checkAll = this.value.length === this.dataWithEmpty.length
          this.isIndeterminate = this.value.length > 0 && this.value.length < this.dataWithEmpty.length
        }
        this.changeInputStyle()
      })
    },
    'element.options.attrs.sort': function(value, old) {
      if (typeof value === 'undefined' || value === old) return
      this.data = []
      let method = multFieldValues
      const token = this.$store.getters.token || getToken()
      const linkToken = this.$store.getters.linkToken || getLinkToken()
      if (!token && linkToken) {
        method = linkMultFieldValues
      }
      const param = { fieldIds: this.element.options.attrs.fieldId.split(','), sort: this.element.options.attrs.sort, keyword: this.keyWord }
      if (this.panelInfo.proxy) {
        param.userId = this.panelInfo.proxy
      }
      this.element.options.attrs.fieldId &&
      this.element.options.attrs.fieldId.length > 0 &&
      method(param).then(res => {
        this.data = this.optionData(res.data)
        this.changeInputStyle()
        if (this.element.options.attrs.multiple) {
          this.checkAll = this.value.length === this.dataWithEmpty.length
          this.isIndeterminate = this.value.length > 0 && this.value.length < this.dataWithEmpty.length
        }
      }) || (this.element.options.value = '')
    },
    cssArr: {
      handler: 'changeInputStyle',
      deep: true
    }
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
    searchWithKey(index) {
      this.timeMachine = setTimeout(() => {
        if (index === this.changeIndex) {
          this.initOptions()
        }
        this.destroyTimeMachine()
      }, 1500)
    },
    destroyTimeMachine() {
      this.timeMachine && clearTimeout(this.timeMachine)
      this.timeMachine = null
    },
    filterMethod() {
      this.destroyTimeMachine()
      this.changeIndex++
      this.searchWithKey(this.changeIndex)
    },
    clearDefault(optionList) {
      const emptyOption = !optionList?.length

      if (!this.inDraw && this.element.options.value) {
        if (Array.isArray(this.element.options.value)) {
          if (emptyOption) {
            this.element.options.value = []
            return
          }
          const tempValueArray = JSON.parse(JSON.stringify(this.element.options.value))
          this.element.options.value = tempValueArray.filter(item => optionList.some(option => option === item))
        } else {
          if (emptyOption) {
            this.element.options.value = ''
            return
          }
          const tempValueArray = JSON.parse(JSON.stringify(this.element.options.value.split(',')))
          this.element.options.value = tempValueArray.filter(item => optionList.some(option => option === item)).join(',')
        }
      }
    },
    clearHandler() {
      this.value = this.element.options.attrs.multiple ? [] : null
      this.checkAll = false
      this.isIndeterminate = false
    },
    resetDefaultValue(ele) {
      const id = ele.id
      const eleVal = ele.options.value.toString()
      if (this.inDraw && this.manualModify && this.element.id === id && this.value.toString() !== eleVal && this.defaultValueStr === eleVal) {
        this.value = this.fillValueDerfault()
        this.changeValue(this.value)

        if (this.element.options.attrs.multiple) {
          this.checkAll = this.value.length === this.dataWithEmpty.length
          this.isIndeterminate = this.value.length > 0 && this.value.length < this.dataWithEmpty.length
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
      this.initOptions()
      const existLastValidFilters = this.$store.state.lastValidFilters && this.$store.state.lastValidFilters[this.element.id]
      if (this.element.options.value || existLastValidFilters) {
        this.value = this.fillValueDerfault()
        this.changeValue(this.value)
      }
    },
    refreshLoad() {
      this.initOptions()
    },
    initOptions() {
      if (this.element.options.attrs.fieldId) {
        let method = multFieldValues
        const token = this.$store.getters.token || getToken()
        const linkToken = this.$store.getters.linkToken || getLinkToken()
        if (!token && linkToken) {
          method = linkMultFieldValues
        }
        const param = {
          fieldIds: this.element.options.attrs.fieldId.split(','),
          sort: this.element.options.attrs.sort, keyword: this.keyWord
        }
        if (this.panelInfo.proxy) {
          param.userId = this.panelInfo.proxy
        }
        method(param).then(res => {
          this.data = this.optionData(res.data)
          this.changeInputStyle()
          if (this.element.options.attrs.multiple) {
            this.checkAll = this.value.length === this.dataWithEmpty.length
            this.isIndeterminate = this.value.length > 0 && this.value.length < this.dataWithEmpty.length
          }
        })
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
        if (!this.showRequiredTips) {
          this.$store.commit('setLastValidFilters', {
            componentId: this.element.id,
            val: (this.value && Array.isArray(this.value)) ? this.value.join(',') : this.value
          })
        }
      }
      this.setCondition()
    },
    getCondition() {
      const param = {
        canvasId: this.canvasId,
        component: this.element,
        value: this.formatFilterValue(),
        operator: this.operator
      }
      return param
    },
    setCondition() {
      if (this.showRequiredTips) {
        return
      }
      const param = this.getCondition()
      !this.isRelation && this.inDraw && this.$store.commit('addViewFilter', param)
    },
    formatFilterValue() {
      if (this.value === null) return []
      if (Array.isArray(this.value)) return this.value
      return this.value.split(',')
    },
    fillValueDerfault() {
      let defaultV = this.element.options.value === null ? '' : this.element.options.value.toString()
      if (this.inDraw) {
        let lastFilters = null
        if (this.$store.state.lastValidFilters) {
          lastFilters = this.$store.state.lastValidFilters[this.element.id]
          if (lastFilters) {
            defaultV = lastFilters.val === null ? '' : lastFilters.val.toString()
          }
        }
      }
      if (this.element.options.attrs.multiple) {
        if (defaultV === null || typeof defaultV === 'undefined' || defaultV === '' || defaultV === '[object Object]') {
          return []
        }
        return defaultV.split(',')
      } else {
        if (defaultV === null || typeof defaultV === 'undefined' || defaultV === '' || defaultV === '[object Object]') {
          return null
        }
        return defaultV.split(',')[0]
      }
    },
    optionData(data) {
      if (!data) return null
      let tempData = data.filter(item => !!item)
      if (this.isCustomSortWidget && this.element.options.attrs?.sort?.sort === 'custom') {
        tempData = mergeCustomSortOption(this.element.options.attrs.sort.list, tempData)
      }
      return tempData.map(item => {
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
      this.value = val ? this.dataWithEmpty.map(item => item.id) : []
      this.isIndeterminate = false
      this.changeValue(this.value)
    },
    handleCheckedChange(values) {
      const checkedCount = values.length
      this.checkAll = checkedCount === this.dataWithEmpty.length
      this.isIndeterminate = checkedCount > 0 && checkedCount < this.dataWithEmpty.length
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

.show-required-tips ::v-deep .el-input__inner {
  border-color: #ff0000 !important;
}
.show-required-tips ::v-deep .el-input__inner::placeholder {
  color: #ff0000 !important;
}
.show-required-tips ::v-deep i {
  color: #ff0000 !important;
}
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

.radio-group-container > .el-radio-group > label {
  display: block !important;
  margin: 10px !important;
}

.checkbox-group-container {
  label.el-checkbox {
    margin: 10px 10px 0 10px !important;
  }

  .el-checkbox-group > label {
    margin: 10px 10px 0 10px !important;
  }

}

</style>
