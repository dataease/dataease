<template>

  <div v-if="options!== null && options.attrs!==null" class="de-select-grid-class">
    <div class="de-select-grid-search">
      <el-input v-model="keyWord" :placeholder="$t('deinputsearch.placeholder')" size="mini" prefix-icon="el-icon-search" clearable />
    </div>
    <div>
      <el-tree
        v-if="options!== null && options.attrs!==null"
        ref="deSelectGrid"
        :data="options.attrs.multiple ? [allNode, ...options.attrs.datas] : options.attrs.datas"
        :props="defaultProp"
        :indent="0"
        :filter-node-method="filterNode"
        class="de-filter-tree"
        default-expand-all
      >
        <span slot-scope="{ node, data }" class="custom-tree-node-list father">
          <span style="display: flex;flex: 1;width: 0;">
            <el-radio v-if="!options.attrs.multiple" v-model="options.value" :label="data.id" @change="changeRadioBox"><span> {{ node.label }} </span></el-radio>
            <el-checkbox v-if="options.attrs.multiple && data.id !== allNode.id" v-model="data.checked" :label="data.id" @change="changeCheckBox(data)"><span> {{ node.label }} </span></el-checkbox>
            <el-checkbox v-if="inDraw && options.attrs.multiple && data.id === allNode.id" v-model="data.checked" :indeterminate="data.indeterminate" :label="data.id" @change="allCheckChange(data)"><span> {{ node.label }} </span></el-checkbox>
          </span>
          <span v-if="!options.attrs.multiple && options.value && options.value === data.id" class="child">
            <span style="margin-left: 12px;" @click.stop>
              <span class="el-dropdown-link">
                <el-button
                  icon="el-icon-circle-close"
                  type="text"
                  size="small"
                  @click="cancelRadio(data)"
                />
              </span>
            </span>
          </span>
        </span>
      </el-tree>

    </div>

  </div>

</template>

<script>

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
    }
  },
  data() {
    return {
      options: null,
      // value: null,
      checked: null,
      defaultProp: {
        id: 'id',
        label: 'text',
        children: 'children'
      },
      keyWord: null,
      allNode: {
        id: (-2 << 16) + '',
        text: this.$t('commons.all'),
        checked: false,
        indeterminate: false
      }
    }
  },
  computed: {
    operator() {
      return this.options.attrs.multiple ? 'in' : 'eq'
    }
  },
  watch: {
    'options.attrs.multiple': function(value) {
      const datas = JSON.parse(JSON.stringify(this.options.attrs.datas))
      this.options.attrs.datas = []
      this.options.attrs.datas = datas
      const sourceValue = this.options.value
      const sourceValid = !!sourceValue && Object.keys(sourceValue).length > 0
      if (value) {
        !sourceValid && (this.options.value = [])
        sourceValid && !Array.isArray(sourceValue) && (this.options.value = sourceValue.split(','))
        !this.inDraw && (this.options.value = [])
        if (!this.inDraw) {
          this.options.value = []
          this.allNode.indeterminate = false
          this.allNode.checked = false
        }
        this.setMutiBox()
      } else {
        !sourceValid && (this.options.value = null)
        sourceValid && Array.isArray(sourceValue) && (this.options.value = sourceValue[0])
        !this.inDraw && (this.options.value = null)
      }
    },
    keyWord(val) {
      this.$refs.deSelectGrid.filter(val)
    }
  },
  created() {
    this.options = this.element.options
    this.setMutiBox()
    this.setRadioBox()
    // this.setCondition()
  },
  mounted() {
    // this.$nextTick(() => {
    //   this.options && this.options.value && this.changeValue(this.options.value)
    // })
    this.options && this.options.value && Object.keys(this.options.value).length > 0 && this.initValue(this.options.value)
  },
  methods: {
    initValue(value) {
      // this.options.value = [value]
      this.setCondition()
    },
    setMutiBox() {
      if (this.options && this.options.attrs.multiple) {
        this.options.attrs.datas.forEach(data => {
          data.checked = (this.options.value && this.options.value.includes(data.id))
        })
        this.setAllNodeStatus()
      }
    },
    setRadioBox() {
      if (this.options && !this.options.attrs.multiple) {
        if (Array.isArray(this.options.value) && this.options.value.length > 0) {
          // this.value = this.options.value.length[0]
        }
      }
    },

    setCondition() {
      const param = {
        component: this.element,
        value: Array.isArray(this.options.value) ? this.options.value : [this.options.value],
        operator: this.operator
      }
      this.inDraw && this.$store.commit('addViewFilter', param)
    },
    changeCheckBox(data) {
      const values = Array.isArray(this.options.value) ? this.options.value : this.options.value ? [this.options.value] : []
      const index = values.indexOf(data.id)
      if (index < 0 && data.checked) {
        values.push(data.id)
      }
      if (index >= 0 && !data.checked) {
        values.splice(index, 1)
      }

      this.setAllNodeStatus()

      this.options.value = values
      this.setCondition()
      this.styleChange()
    },
    // 勾选数据项 会影响全选节点的状态
    setAllNodeStatus() {
      const nodeSize = this.options.attrs.datas.length
      const checkedSize = this.options.attrs.datas.filter(item => item.checked).length
      if (nodeSize === checkedSize) {
        this.allNode.checked = true
        this.allNode.indeterminate = false
      } else if (checkedSize === 0) {
        this.allNode.checked = false
        this.allNode.indeterminate = false
      } else {
        this.allNode.checked = false
        this.allNode.indeterminate = true
      }
    },
    allCheckChange(data) {
      data.indeterminate = false
      const values = []
      // this.options.value = []
      this.options.attrs.datas.forEach(item => {
        item.checked = data.checked
        // data.checked && this.options.value.push(item.id)
        data.checked && values.push(item.id)
      })

      this.options.value = values
      this.setCondition()
    },
    changeRadioBox(value) {
      // this.options.value = []
      // if (this.value) this.options.value = [this.value]
      this.setCondition()
    },
    cancelRadio(data) {
      this.options.value = null
      this.changeRadioBox()
    },
    filterNode(value, data) {
      if (!value) return true
      return data[this.defaultProp.label].indexOf(value) !== -1
    },
    styleChange() {
      this.$store.state.styleChangeTimes++
    }

  }
}
</script>

<style lang="scss" scoped>
.custom-tree-node-list {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    padding:0 8px;
  }
  .father .child {
    /*display: none;*/
    visibility: hidden;
  }
  .father:hover .child {
    /*display: inline;*/
    visibility: visible;
  }
.de-filter-tree {
  >>>span.is-leaf {
    width: 5px !important;
    padding: 6px 0 !important;
  }
}
.de-select-grid-search {
  >>>input {
    border-radius: 0px;
  }
}
</style>
