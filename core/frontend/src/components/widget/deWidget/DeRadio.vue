<template>
  <el-radio-group
    v-model="selectValue"
    style="height: 40px; align-items: center; display: flex"
    @change="visualChange"
  >
    <template v-for="item in options">
      <el-radio
        v-if="radioStyle.showStyle === 'single'"
        :key="item.id + 'radio'"
        :label="item.id"
        class="is-custom-select"
        :disabled="itemDisabled"
      >
        {{ item.text }}
      </el-radio>
      <el-radio-button
        v-else
        :key="item.id + 'tab'"
        :disabled="itemDisabled"
        class="is-custom-select"
        :label="item.id"
      >{{ item.text }}
      </el-radio-button>
    </template>
  </el-radio-group>
</template>

<script>
import { uuid } from 'vue-uuid'

export default {
  name: 'DeRadio',
  model: {
    prop: 'value', // 绑定的值，通过父组件传递
    event: 'update' // 自定义名
  },
  props: {
    id: {
      type: String,
      require: true,
      default: uuid.v1()
    },
    radioStyle: {
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
      type: [String, Number],
      default: ''
    },
    isConfig: {
      type: Boolean,
      default: false
    },
    itemDisabled: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      newList: [],
      selectValue: this.value,
      options: [],
      defaultFirst: false
    }
  },
  computed: {
    maxLength() {
      return this.radioStyle.showNum || 5
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
      }
    },
    maxLength() {
      this.resetList()
    },
    list() {
      this.resetList()
      this.$nextTick(() => {
        this.init()
      })
    },
    radioStyle: {
      handler() {
        this.setPlaceholderColor()
      },
      deep: true
    }
  },
  mounted() {
    this.resetList()
    this.$nextTick(() => {
      this.init()
      this.setPlaceholderColor()
    })
  },
  beforeDestroy() {
    const styleEle = document.querySelector(`#radio-style${this.id}`)
    if (styleEle) {
      styleEle.parentElement.removeChild(styleEle)
    }
  },
  methods: {
    setPlaceholderColor() {
      let styleEle = document.querySelector(`#radio-style${this.id}`)
      if (!styleEle) {
        styleEle = document.createElement('style')
        styleEle.id = `radio-style${this.id}`
        document.querySelector('head').appendChild(styleEle)
      }
      styleEle.innerHTML = `#component${this.id} .el-radio-button:not(.is-active) .el-radio-button__inner {\n  color: ${this.radioStyle.wordColor}; \n border-color: ${this.radioStyle.brColor}; \n background-color: ${this.radioStyle.innerBgColor}; \n  } #component${this.id} .el-radio:not(.is-check) .el-radio__label {\n  color: ${this.radioStyle.wordColor}; \n }`
    },
    resetList(arrays) {
      if (Array.isArray(arrays)) {
        this.newList = arrays.slice()
      } else {
        this.newList = this.list.slice()
      }
      this.options = this.newList.slice(0, this.maxLength)
    },
    init() {
      if (this.defaultFirst && this.list.length > 0) {
        this.selectValue = this.list[0].value
      }
    },
    visualChange(val) {
      this.$emit('change', val)
    }
  }
}
</script>

<style lang="scss">
.VisualSelects {
  .el-scrollbar {
    position: relative;
    height: 245px;
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
