<template>

  <el-input
    v-if="element.options!== null && element.options.attrs!==null"
    ref="de-input-search"
    v-model="value"
    resize="vertical"
    :placeholder="$t(element.options.attrs.placeholder)"
    :size="size"
    class="de-range-tag"
    @input="valueChange"
    @keypress.enter.native="search"
    @dblclick="setEdit"
  >

    <el-button
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
      this.search()
    }
  },
  created() {
    if (this.element.options.value) {
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
    resetDefaultValue(id) {
      if (this.inDraw && this.manualModify && this.element.id === id) {
        this.value = this.fillValueDerfault()
        this.search()
      }
    },
    search() {
      if (!this.inDraw) {
        this.element.options.value = this.value
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
      const defaultV = this.element.options.value === null ? '' : this.element.options.value.toString()
      if (defaultV === null || typeof defaultV === 'undefined' || defaultV === '' || defaultV === '[object Object]') return null
      return defaultV.split(',')[0]
    }
  }
}
</script>

<style lang="scss">
// .de-range-tag {
//   input::placeholder {
//     color: var(--CustomColor, #909399) !important;
//   }
// }
</style>
