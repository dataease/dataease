<template>

  <el-form
    v-if="element.options!== null && element.options.attrs!==null"
    ref="form"
    :model="form"
    style="width: 100%;"
    :rules="rules"
  >
    <div
      class="de-number-range-container"
      :class="{'show-required-tips': showRequiredTips}"
    >
      <el-form-item
        prop="min"
        style="padding-left: 0px;"
      >
        <el-input
          ref="de-number-range-min"
          v-model="form.min"
          :placeholder="showRequiredTips ? $t('panel.required_tips') : $t(element.options.attrs.placeholder_min)"
          :size="size"
          @input="inputChange"
          @change="handleMinChange"
        />
      </el-form-item>
      <span>{{ $t('denumberrange.split_placeholder') }}</span>
      <el-form-item
        prop="max"
        style="padding-right: 0px;width: calc(50% - 4px) !important;"
      >
        <el-input
          ref="de-number-range-max"
          v-model="form.max"
          :placeholder="showRequiredTips ? $t('panel.required_tips') : $t(element.options.attrs.placeholder_max)"
          :size="size"
          @input="inputChange"
          @change="handleMaxChange"
        />
      </el-form-item>
    </div>
  </el-form>

</template>

<script>
const MIN_NUMBER = Number.MIN_SAFE_INTEGER
const MAX_NUMBER = Number.MAX_SAFE_INTEGER
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
      operator: 'between',
      values: null,
      canEdit: false,

      form: { min: '', max: '' },
      rules: {
        min: [
        //   { required: true, message: this.$t('denumberrange.please_key_min'), trigger: 'blur' },
          { validator: this.validateCom, trigger: 'blur' },
          { validator: this.validateMin, trigger: 'blur' }
        ],
        max: [
        //   { required: true, message: this.$t('denumberrange.please_key_max'), trigger: 'blur' },
          { validator: this.validateCom, trigger: 'blur' },
          { validator: this.validateMax, trigger: 'blur' }
        ]
      },
      changeIndex: 0,
      timeMachine: null
    }
  },
  computed: {
    defaultvalues() {
      if (!this.element.options.value) {
        return JSON.stringify([])
      }
      return JSON.stringify(this.element.options.value)
    },
    viewIds() {
      if (!this.element || !this.element.options || !this.element.options.attrs.viewIds) return ''
      return this.element.options.attrs.viewIds.toString()
    },
    manualModify() {
      return !!this.element.options.manualModify
    },
    showRequiredTips() {
      return this.inDraw && this.element.options.attrs.required && !this.form.min && !this.form.max
    }
  },
  watch: {
    'form.min': function(val, old) {
      if (!this.inDraw) {
        this.$emit('widget-value-changed', val)
      }
    },
    'form.max': function(val, old) {
      if (!this.inDraw) {
        this.$emit('widget-value-changed', val)
      }
    },
    'viewIds': function(value, old) {
      if (typeof value === 'undefined' || value === old) return
      this.setCondition()
    },
    'defaultvalues': function(value, old) {
      if (value === old) return
      const values = this.fillValueDerfault()
      this.form.min = values[0]
      if (values.length > 1) {
        this.form.max = values[1]
      }
      this.search()
    },
    form: {
      handler(value) {
        this.destroyTimeMachine()
        this.changeIndex++
        this.searchWithKey(this.changeIndex)
      },
      deep: true
    }
  },
  created() {
    const existLastValidFilters = this.$store.state.lastValidFilters && this.$store.state.lastValidFilters[this.element.id]
    if ((this.element.options.value && this.element.options.value.length > 0) || existLastValidFilters) {
      const values = this.fillValueDerfault()
      this.form.min = values[0]
      if (values.length > 1) {
        this.form.max = values[1]
      }
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
      this.form.min = null
      this.form.max = null
    },
    resetDefaultValue(ele) {
      const id = ele.id
      const eleVal = ele.options.value.toString()
      if (this.inDraw && this.manualModify && this.element.id === id && this.defaultValueStr === eleVal) {
        if (!this.element.options.value) {
          this.form.min = null
          this.form.max = null
        } else {
          const values = this.fillValueDerfault()
          this.form.min = values[0]
          if (values.length > 1) {
            this.form.max = values[1]
          }
        }
        this.search()
      }
    },
    searchWithKey(index) {
      this.timeMachine = setTimeout(() => {
        if (index === this.changeIndex) {
          this.search()
        }
        this.destroyTimeMachine()
      }, 1000)
    },
    destroyTimeMachine() {
      this.timeMachine && clearTimeout(this.timeMachine)
      this.timeMachine = null
    },
    getFormData() {
      const ret = {}
      this.$refs.form.validate((valid) => {
        ret.valid = valid
        ret.form = this.form
      })
      return ret
    },
    resetForm() {
      this.$refs.form.resetFields()
    },
    handleMinChange() {
      this.$refs.form.validateField('max')
    },
    handleMaxChange() {
      this.$refs.form.validateField('min')
    },
    validateCom(rule, value, callback) {
      if (!value) return callback()
      const one = Number(value)
      if (!Number.isNaN(one)) {
        if (one < MIN_NUMBER) {
          return callback(new Error(this.$t('denumberrange.out_of_min')))
        } else if (one > MAX_NUMBER) {
          return callback(new Error(this.$t('denumberrange.out_of_max')))
        }
        return callback()
      }
      return callback(new Error(this.$t('denumberrange.must_int')))
    },
    validateMin(rule, value, callback) {
      if (!value) return callback()
      const one = Number(value)
      const max = Number(this.form.max)
      if (!max || one < max) {
        return callback()
      }
      return callback(new Error(this.$t('denumberrange.min_out_max')))
    },
    validateMax(rule, value, callback) {
      if (!value) return callback()
      const one = Number(value)
      const min = Number(this.form.min)
      if (!min || one > min) {
        return callback()
      }
      return callback(new Error(this.$t('denumberrange.max_out_min')))
    },

    search() {
      this.$nextTick(() => {
        this.$refs.form.validate(valid => {
          if (!valid) {
            return false
          }
          if (!this.showRequiredTips) {
            const values = [this.form.min, this.form.max]
            this.$store.commit('setLastValidFilters', {
              componentId: this.element.id,
              val: (values && Array.isArray(values)) ? values.join(',') : values
            })
          }
          this.setCondition()
        })
      })
    },
    getCondition() {
      const param = {
        canvasId: this.canvasId,
        component: this.element,
        value: [this.form.min, this.form.max],
        operator: this.operator
      }
      if (this.form.min && this.form.max) {
        return param
      }
      if (!this.form.min && !this.form.max) {
        param.value = []
        return param
      }
      if (this.form.min) {
        param.value = [this.form.min]
        param.operator = 'ge'
        return param
      }
      if (this.form.max) {
        param.value = [this.form.max]
        param.operator = 'le'
        return param
      }
      return param
    },
    setCondition() {
      if (this.showRequiredTips) {
        return
      }
      const param = this.getCondition()

      if (this.form.min && this.form.max) {
        !this.isRelation && this.inDraw && this.$store.commit('addViewFilter', param)
        return
      }
      if (!this.form.min && !this.form.max) {
        param.value = []
        !this.isRelation && this.inDraw && this.$store.commit('addViewFilter', param)
        return
      }
      if (this.form.min) {
        param.value = [this.form.min]
        param.operator = 'ge'
        !this.isRelation && this.inDraw && this.$store.commit('addViewFilter', param)
        return
      }
      if (this.form.max) {
        param.value = [this.form.max]
        param.operator = 'le'
        !this.isRelation && this.inDraw && this.$store.commit('addViewFilter', param)
        return
      }
    },

    inputChange(val) {
      if (!this.inDraw) {
        const values = [this.form.min, this.form.max]
        this.element.options.value = values
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
      if (defaultV === null || typeof defaultV === 'undefined' || defaultV === '' || defaultV === '[object Object]') return []
      return defaultV.split(',')
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
.de-number-range-container {
  display: inline;
  max-height: 40px;
  ::v-deep div.el-form-item {
    width: calc(50% - 10px) !important;
    display: inline-block;
    padding: 0 5px;
    margin-bottom: 0;
  }
}
</style>
