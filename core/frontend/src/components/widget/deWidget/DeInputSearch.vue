<template>

  <el-input
    v-if="element.options!== null && element.options.attrs!==null"
    ref="de-input-search"
    v-model="value"
    resize="vertical"
    :placeholder="showRequiredTips ? $t('panel.required_tips') : $t(element.options.attrs.placeholder)"
    :size="size"
    class="de-range-tag"
    :class="{'show-required-tips': showRequiredTips}"
    @input="valueChange"
    @keypress.enter.native="search"
    @dblclick="setEdit"
  >

    <el-button
      v-if="!isRelation"
      slot="append"
      icon="el-icon-search"
      @click="search"
    />
  </el-input>

</template>

<script>
import bus from '@/utils/bus'
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
    size: String,
    isRelation: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      operator: 'like',
      value: null,
      canEdit: false
    }
  },
  computed: {
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
    showRequiredTips() {
      return this.inDraw && this.element.options.attrs.required && !this.value
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
      this.search()
    }
  },
  created() {
    const existLastValidFilters = this.$store.state.lastValidFilters && this.$store.state.lastValidFilters[this.element.id]
    if (this.element.options.value || existLastValidFilters) {
      this.value = this.fillValueDerfault()
      this.search()
    }
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
      this.value = null
    },
    resetDefaultValue(ele) {
      const id = ele.id
      const eleVal = ele.options.value.toString()
      if (this.inDraw && this.manualModify && this.element.id === id && this.value.toString() !== eleVal && this.defaultValueStr === eleVal) {
        this.value = this.fillValueDerfault()
        this.search()
      }
    },
    search() {
      if (!this.inDraw) {
        this.element.options.value = this.value
      } else if (!this.showRequiredTips) {
        this.$store.commit('setLastValidFilters', {
          componentId: this.element.id,
          val: (this.value && Array.isArray(this.value)) ? this.value.join(',') : this.value
        })
      }
      this.setCondition()
    },
    getCondition() {
      const param = {
        canvasId: this.canvasId,
        component: this.element,
        value: !this.value ? [] : Array.isArray(this.value) ? this.value : [this.value],
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
    setEdit() {
      this.canEdit = true
    },
    valueChange(val) {
      if (!this.inDraw) {
        this.element.options.value = val
        this.element.options.manualModify = false
      } else {
        this.element.options.manualModify = true
      }
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
      if (defaultV === null || typeof defaultV === 'undefined' || defaultV === '' || defaultV === '[object Object]') return null
      return defaultV.split(',')[0]
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
</style>
