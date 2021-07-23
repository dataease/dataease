<template>

  <el-form v-if="options!== null && options.attrs!==null" ref="form" :model="form" :rules="rules">
    <div class="de-number-range-container">
      <el-form-item prop="min">
        <el-input v-model="form.min" :placeholder="options.attrs.placeholder" @change="handleMinChange" />
      </el-form-item>
      <span>~</span>
      <el-form-item prop="max">
        <el-input v-model="form.max" :placeholder="options.attrs.placeholder" @change="handleMaxChange" />
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
      options: null,
      operator: 'between',
      values: null,
      canEdit: false,

      form: { min: '', max: '' },
      rules: {
        min: [
          { required: true, message: this.$t('denumberrange.please_key_min'), trigger: 'blur' },
          { validator: this.validateCom, trigger: 'blur' },
          { validator: this.validateMin, trigger: 'blur' }
        ],
        max: [
          { required: true, message: this.$t('denumberrange.please_key_max'), trigger: 'blur' },
          { validator: this.validateCom, trigger: 'blur' },
          { validator: this.validateMax, trigger: 'blur' }
        ]
      },
      changeIndex: 0,
      timeMachine: null
    }
  },
  watch: {
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
    this.options = this.element.options
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
      const one = Number(value)
      const max = Number(this.form.max)
      if (!max || one < max) {
        return callback()
      }
      return callback(new Error(this.$t('denumberrange.min_out_max')))
    },
    validateMax(rule, value, callback) {
      const one = Number(value)
      const min = Number(this.form.min)
      if (!min || one > min) {
        return callback()
      }
      return callback(new Error(this.$t('denumberrange.max_out_min')))
    },

    search() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return false
        }
        this.setCondition()
      })
    },
    setCondition() {
      const param = {
        component: this.element,
        // value: !this.values ? [] : Array.isArray(this.values) ? this.values : [this.values],
        value: [this.form.min, this.form.max],
        operator: this.operator
      }
      this.inDraw && this.$store.commit('addViewFilter', param)
    }
  }
}
</script>

<style lang="scss" scoped>
.de-number-range-container {
  display: inline;
  >>>div.el-form-item {
    width: calc(50% - 5px) !important;
    display: inline-block;
    padding: 0 5px;
  }
}
</style>
