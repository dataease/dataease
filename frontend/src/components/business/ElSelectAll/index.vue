<template>
  <el-select v-model="selected" multiple v-bind="$attrsAll" v-on="$listenserAll" @change="onChange">
    <el-option v-for="item in mdoptionsList" :key="item.key" :label="item.label" :value="item.value" />
    <slot name="default" />
  </el-select>
</template>

<script>
export default {
  name: 'ElSelectAll',
  props: {
    value: {
      type: Array,
      default: () => {
        return []
      }
    },
    options: {
      type: Array,
      default: () => {
        return []
      }
    }
  },
  data() {
    const selected = this.value || []
    return {
      selected,
      mdoptionsValue: [],
      oldMdoptionsValue: [],
      mdoptionsList: []
    }
  },
  computed: {
    $attrsAll() {
      // const val = this.$vnode.data.model && this.$vnode.data.model.value;
      const result = {
        // value: val,
        ...this.$attrs
      }
      return result
    },
    $listenserAll() {
      const _this = this
      return Object.assign({}, this.$listeners, {
        change: () => {
          this.$emit('change', (_this.selected || []).filter(v => {
            return v !== 'all'
          }))
        },
        input: () => {
          this.$emit('input', (_this.selected || []).filter(v => {
            return v !== 'all'
          }))
        }
      })
    }
  },
  watch: {
    selected: {
      immediate: true,
      deep: true,
      handler(val) {
        this.$emit('input', (val || []).filter(v => {
          return v !== 'all'
        }))
      }
    },
    options: {
      immediate: true,
      deep: true,
      handler(val) {
        if ((!val || val.length === 0) && !this.$slots) {
          this.mdoptionsList = []
        } else {
          this.mdoptionsList = [{
            key: 'all',
            value: 'all',
            label: '全部'
          }, ...val]
        }
      }
    }
  },
  mounted() {
  },
  methods: {
    onChange(val) {
      // eslint-disable-next-line no-debugger
      const allValues = []
      // 保留所有值
      for (const item of this.mdoptionsList) {
        allValues.push(item.value)
      }
      // 用来储存上一次的值，可以进行对比
      const oldVal = this.oldMdoptionsValue.length === 1 ? [] : this.oldMdoptionsValue[1] || []
      // 若是全部选择
      if (val.includes('all')) this.selected = allValues
      // 取消全部选中  上次有 当前没有 表示取消全选
      if (oldVal.includes('all') && !val.includes('all')) this.selected = []
      // 点击非全部选中  需要排除全部选中 以及 当前点击的选项
      // 新老数据都有全部选中
      if (oldVal.includes('all') && val.includes('all')) {
        const index = val.indexOf('all')
        val.splice(index, 1) // 排除全选选项
        this.selected = val
      }
      // 全选未选 但是其他选项全部选上 则全选选上 上次和当前 都没有全选
      if (!oldVal.includes('all') && !val.includes('all')) {
        if (val.length === allValues.length - 1) this.selected = ['all'].concat(val)
      }
      // 储存当前最后的结果 作为下次的老数据
      this.oldMdoptionsValue[1] = this.selected
    }
  }
}
</script>
