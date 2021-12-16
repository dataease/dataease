<template>

  <el-form v-if="element.options!== null && element.options.attrs!==null" ref="form" :model="form" :rules="rules">
    <div class="de-number-range-container">
      <el-form-item prop="min">
        <el-input v-model="form.min" :placeholder="$t(element.options.attrs.placeholder_min)" @input="inputChange" @change="handleMinChange" />
      </el-form-item>
      <span>{{ $t('denumberrange.split_placeholder') }}</span>
      <el-form-item prop="max">
        <el-input v-model="form.max" :placeholder="$t(element.options.attrs.placeholder_max)" @input="inputChange" @change="handleMaxChange" />
      </el-form-item>
    </div>
  </el-form>

</template>

<script>
const MIN_NUMBER = -2147483648
const MAX_NUMBER = 2147483647
export default {

  props: {
    element: {
      type: Object,
      default: null
    },
    inDraw: {
      type: Boolean,
      default: true
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
    }
  },
  watch: {
    'defaultvalues': function(value, old) {
      if (value === old) return
      const values = this.element.options.value
      this.form.min = values[0]
      if (values.length > 1) {
        this.form.max = values[1]
      }
      this.search()
    },
    form: {
      handler(value) {
        this.destryTimeMachine()
        this.changeIndex++
        this.searchWithKey(this.changeIndex)
      },
      deep: true
    }
  },
  created() {
    if (this.element.options.value && this.element.options.value.length > 0) {
      const values = this.element.options.value
      this.form.min = values[0]
      if (values.length > 1) {
        this.form.max = values[1]
      }
      this.search()
    }
  },
  methods: {
    searchWithKey(index) {
      this.timeMachine = setTimeout(() => {
        if (index === this.changeIndex) {
          this.search()
        }
        this.destryTimeMachine()
      }, 1000)
    },
    destryTimeMachine() {
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
      if (Number.isInteger(one)) {
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

          this.setCondition()
          this.$store.commit('recordStyleChange')
        })
      })
    },
    setCondition() {
      const param = {
        component: this.element,
        // value: !this.values ? [] : Array.isArray(this.values) ? this.values : [this.values],
        value: [this.form.min, this.form.max],
        operator: this.operator
      }

      if (this.form.min && this.form.max) {
        this.inDraw && this.$store.commit('addViewFilter', param)
        return
      }
      if (!this.form.min && !this.form.max) {
        param.value = []
        this.inDraw && this.$store.commit('addViewFilter', param)
        return
      }
      if (this.form.min) {
        param.value = [this.form.min]
        param.operator = 'ge'
        this.inDraw && this.$store.commit('addViewFilter', param)
        return
      }
      if (this.form.max) {
        param.value = [this.form.max]
        param.operator = 'le'
        this.inDraw && this.$store.commit('addViewFilter', param)
        return
      }
    },
    styleChange() {
      this.$store.commit('recordStyleChange')
    },

    inputChange(val) {
      if (!this.inDraw) {
        const values = [this.form.min, this.form.max]
        this.element.options.value = values
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.de-number-range-container {
  display: inline;
  >>>div.el-form-item {
    width: calc(50% - 10px) !important;
    display: inline-block;
    padding: 0 5px;
  }
}
</style>
