
<template>
  <div
    class="el-tree-select"
    :class="selectClass"
  >
    <el-select
      :id="'el-tree-select-' + guid"
      ref="select"
      v-model="labels"
      v-popover:popover
      :style="styles"
      :collapse-tags="showNumber"
      class="el-tree-select-input"
      :disabled="disabled"
      popper-class="de-select-option"
      v-bind="selectParams"
      :popper-append-to-body="popperAppendToBody"
      :filterable="false"
      :multiple="selectParams.multiple"
      :title="labels"
      @remove-tag="_selectRemoveTag"
      @clear="_selectClearFun"
      @focus="_popoverShowFun"
    />
    <el-popover
      ref="popover"
      v-model="visible"
      :append-to-body="popperAppendToBody"
      :placement="placement"
      :transition="transition"
      :popper-class="popperClass"
      :width="width"
      trigger="click"
      @show="showPopover"
    >
      <el-input
        v-if="treeParams.filterable"
        ref="input"
        v-model="keywords"
        size="mini"
        class="input-with-select mb10"
      >
        <el-button
          slot="append"
          icon="el-icon-search"
          @click="_searchFun"
        />
      </el-input>
      <p
        v-if="selectParams.multiple"
        class="tree-select-all"
      ><el-checkbox
        v-model="selectAll"
        v-customStyle="customStyle"
        :indeterminate="isIndeterminate"
        @change="selectAllChange"
      >{{ $t('dataset.check_all') }}</el-checkbox></p>
      <el-scrollbar
        tag="div"
        wrap-class="el-select-dropdown__wrap"
        view-class="el-select-dropdown__list"
        class="is-empty"
      >
        <el-tree
          v-show="data.length > 0"
          ref="tree"
          :popper-append-to-body="popperAppendToBody"
          v-bind="treeParams"
          :data="data"
          :node-key="propsValue"
          :draggable="false"
          :current-node-key="ids.length > 0 ? ids[0] : ''"
          :show-checkbox="selectParams.multiple"
          :filter-node-method="filterNodeMethod ? filterNodeMethod : _filterFun"
          :render-content="treeRenderFun"
          @node-click="_treeNodeClickFun"
          @check="_treeCheckFun"
          @check-change="_treeCheckChange"
        />
        <div
          v-if="data.length === 0"
          class="no-data"
        >暂无数据</div>
      </el-scrollbar>
    </el-popover>
  </div>
</template>

<script>
import { on, off } from './dom'
import { each, guid } from './utils'
export default {
  name: 'ElTreeSelect',
  components: {},
  props: {
    value: {
      type: [String, Array, Number],
      default() {
        return ''
      }
    },
    popperAppendToBody: {
      type: Boolean,
      default: false
    },
    styles: {
      type: Object,
      default() {
        return {
          width: '100%'
        }
      }
    },
    selectClass: {
      type: String,
      default() {
        return ''
      }
    },
    popoverClass: {
      type: String,
      default() {
        return ''
      }
    },
    disabled: {
      type: Boolean,
      default() {
        return false
      }
    },
    placement: {
      type: String,
      default() {
        return 'bottom'
      }
    },
    transition: {
      type: String,
      default() {
        return 'el-zoom-in-top'
      }
    },
    // eslint-disable-next-line vue/require-default-prop
    treeRenderFun: Function,
    // eslint-disable-next-line vue/require-default-prop
    filterNodeMethod: Function,

    selectParams: {
      type: Object,

      default() {
        return {
          clearable: true,
          disabled: false,
          placeholder: '请选择'
        }
      }
    },

    treeParams: {
      type: Object,

      default() {
        return {
          clickParent: false,
          filterable: false,
          leafOnly: false,
          includeHalfChecked: false,
          data: [],
          showParent: false,
          props: {
            children: 'children',
            label: 'name',
            code: 'code',
            value: 'flowId',
            disabled: 'disabled'
          }
        }
      }
    },
    customStyle: {
      type: Object,
      default: () => {}
    }
  },
  data() {
    return {
      selectAll: false,
      guid: guid(),
      propsValue: 'flowId',
      propsLabel: 'name',
      propsCode: null,
      propsDisabled: 'disabled',
      propsChildren: 'children',
      leafOnly: false,
      includeHalfChecked: false,
      data: [],
      keywords: '',
      labels: '',
      ids: [],
      visible: false,
      width: 150,
      showParent: false,
      showNumber: false
    }
  },
  computed: {
    popperClass() {
      const _c = 'el-tree-select-popper ' + this.popoverClass
      return this.disabled ? _c + ' disabled ' : _c
    },
    isIndeterminate() {
      if (!this.selectParams.multiple) return
      return this.ids.length > 0 && this.ids.length !== this._checkSum().length
    }
  },
  watch: {
    ids: function(val) {
      if (val !== undefined) {
        this.$nextTick(() => {
          this._setSelectNodeFun(val)
        })
      }
    },
    value: function(val) {
      if (this.ids !== val) {
        this._setMultipleFun()
        if (this.selectParams.multiple) {
          this.ids = [...val]
        } else {
          this.ids = val === '' ? [] : [val]
        }
      }
    },
    labels: function() {
      this.setShowNumber()
    }
  },
  created() {
    const { props, data, leafOnly, includeHalfChecked, showParent } = this.treeParams
    this._setMultipleFun()
    this.propsValue = props.value
    this.propsLabel = props.label
    this.propsCode = props.code || null
    this.propsDisabled = props.disabled
    this.propsChildren = props.children
    this.leafOnly = leafOnly
    this.includeHalfChecked = includeHalfChecked
    this.data = data.length > 0 ? [...data] : []
    if (this.selectParams.multiple) {
      this.labels = []
      this.ids = this.value
    } else {
      this.labels = ''
      this.ids = this.value instanceof Array ? this.value : [this.value]
    }
    this.showParent = showParent
  },
  mounted() {
    this._updateH()
    this.$nextTick(() => {
      on(document, 'mouseup', this._popoverHideFun)
      this.bindScroll()
    })
  },
  beforeDestroy() {
    off(document, 'mouseup', this._popoverHideFun)
    this.unbindScroll()
  },
  methods: {
    bindScroll() {
      window.onmousewheel = this._popoverHideFun
      on(document, 'DOMMouseScroll', this._popoverHideFun)
    },
    unbindScroll() {
      window.onmousewheel = null
      off(document, 'DOMMouseScroll', this._popoverHideFun)
    },
    showPopover() {
      this.$nextTick(() => {
        this.$refs.input.focus()
      })
    },
    resetSelectAll() {
      this.selectAll = false
    },
    selectAllChange(val) {
      if (val) {
        this.ids = this._checkSum()
        this._emitFun()
        this.$emit('check', null, this.ids, null)
        return
      }
      this._selectClearFun()
    },
    _checkSum() {
      let arr = [];
      (this.data || []).forEach(ele => {
        arr = [...this.allKidIds(ele), ...arr]
      })
      return arr
    },
    _treeCheckChange() {
      this.$emit('treeCheckChange')
    },
    _setMultipleFun() {
      let multiple = false
      if (this.value instanceof Array) {
        multiple = true
      }
      this.$set(this.selectParams, 'multiple', multiple)
    },
    _searchFun() {
      this.$emit('searchFun', this.keywords)
    },
    _setSelectNodeFun(ids) {
      const el = this.$refs.tree
      if (!el) {
        throw new Error('找不到tree dom')
      }
      const { multiple } = this.selectParams
      if (ids.length === 0 || this.data.length === 0) {
        this.labels = multiple ? [] : ''
        if (multiple) {
          el.setCheckedKeys([])
        } else {
          el.setCurrentKey(null)
        }
        return
      }
      if (multiple) {
        el.getCheckedNodes(this.leafOnly, this.includeHalfChecked).forEach(item => {
          el.setChecked(item, false)
        })
        ids.forEach(id => {
          el.setChecked(id, true)
        })
        const nodes = el.getCheckedNodes(this.leafOnly, this.includeHalfChecked)
        if (!this.showParent) {
          if (this.propsCode) {
            this.labels = nodes.map(item => (item[this.propsCode] ? item[this.propsLabel] + '(' + item[this.propsCode] + ')' : item[this.propsLabel])) || []
          } else {
            this.labels = nodes.map(item => item[this.propsLabel]) || []
          }
        } else {
          this.labels = nodes.map(item => this.cascadeLabels(item)) || []
        }
      } else {
        el.setCurrentKey(ids[0])
        const node = el.getCurrentNode()
        if (node) {
          if (!this.showParent) {
            if (this.propsCode) {
              this.labels = node[this.propsCode] ? node[this.propsLabel] + '(' + node[this.propsCode] + ')' : node[this.propsLabel]
            } else {
              this.labels = node[this.propsLabel]
            }
          } else {
            this.labels = this.cascadeLabels(node)
          }
        } else {
          this.labels = ''
        }
      }
      this._updatePopoverLocationFun()
    },

    parentNodes(node) {
      const results = []
      let currentNode = node
      while (currentNode && currentNode.data && !(currentNode.data instanceof Array)) {
        results.push(currentNode)
        currentNode = currentNode.parent
      }
      return results
    },

    cascadeLabels(data) {
      const cNode = this.$refs.tree.getNode(data)
      const linkedNodes = this.parentNodes(cNode)
      const labels = linkedNodes.map(item => item.data[this.propsLabel]).reverse().join(':')
      return labels
    },
    _updatePopoverLocationFun() {
      setTimeout(() => {
        this.$refs.popover.updatePopper()
      }, 50)
    },
    _getEventPath(evt) {
      const path = (evt.composedPath && evt.composedPath()) || evt.path
      const target = evt.target
      if (path != null) {
        return path.indexOf(window) < 0 ? path.concat(window) : path
      }
      if (target === window) {
        return [window]
      }
      function getParents(node, memo) {
        memo = memo || []
        const parentNode = node.parentNode
        if (!parentNode) {
          return memo
        } else {
          return getParents(parentNode, memo.concat(parentNode))
        }
      }
      return [target].concat(getParents(target), window)
    },
    _filterFun(value, data, node) {
      if (!value) return true
      return data[this.propsLabel].indexOf(value) !== -1
    },
    _treeNodeClickFun(data, node, vm) {
      const { multiple } = this.selectParams
      if (multiple) return
      const { clickParent } = this.treeParams
      const checkStrictly = this.treeParams['check-strictly']
      const { propsValue, propsChildren, propsDisabled } = this
      const children = data[propsChildren] || []
      if (data[propsDisabled]) {
        return
      }
      if (node.checked) {
        const value = data[propsValue]
        this.ids = this.ids.filter(id => id !== value)
        if (!checkStrictly && children.length) {
          children.forEach(item => {
            this.ids = this.ids.filter(id => id !== item[propsValue])
          })
        }
      } else {
        if (!multiple) {
          if (!clickParent) {
            if (children.length === 0) {
              this.ids = [data[propsValue]]
              this.visible = false
            } else {
              return false
            }
          } else {
            this.ids = [data[propsValue]]
            this.visible = false
          }
        } else {
          if (!clickParent && children.length === 0) {
            this.ids.push(data[propsValue])
          } else if (clickParent) {
            this.ids.push(data[propsValue])
            if (!checkStrictly && children.length) {
              children.forEach(item => {
                this.ids.push(item[propsValue])
              })
            }
          }
        }
      }
      this._emitFun()

      this.$emit('node-click', data, node, vm)
    },
    _treeCheckFun(data, node, vm) {
      this.ids = []
      const { propsValue } = this
      const checkKeys = this.$refs.tree.getCheckedKeys()
      checkKeys.forEach((i, n) => {
        const node = this.$refs.tree.getNode(i)
        if (!node.visible && node.checked) {
          this.$refs.tree.setChecked(i, false)
        }
      })

      const checkedNodes = this.$refs.tree.getCheckedNodes()

      checkedNodes.forEach(item => {
        this.ids.push(item[propsValue])
      })

      node.checkedKeys = checkedNodes.map(node => node.id)
      this.selectAll = this._checkSum().length === this.ids.length
      this.$emit('check', data, node, vm)
      this._emitFun()
    },
    allKidIds(node, ids) {
      ids = ids || []
      if (!node) {
        return
      }
      const stack = []
      stack.push(node)
      let tmpNode
      while (stack.length > 0) {
        tmpNode = stack.pop()
        ids.push(tmpNode.id)
        if (tmpNode.children && tmpNode.children.length > 0) {
          var i = tmpNode.children.length - 1
          for (i = tmpNode.children.length - 1; i >= 0; i--) {
            stack.push(tmpNode.children[i])
          }
        }
      }
      return ids
    },
    _selectRemoveTag(tag) {
      const { data, propsValue, propsLabel, propsChildren } = this
      const { multiple } = this.selectParams
      each(
        data,
        item => {
          const labels = this.showParent ? this.cascadeLabels(item) : item[propsLabel]
          if (labels === tag) {
            if (multiple && item.children && item.children.length) {
              const needCancelIds = this.allKidIds(item) || []
              this.ids = this.ids.filter(id => !needCancelIds.includes(id))
            } else {
              const value = item[propsValue]
              this.ids = this.ids.filter(id => id !== value)
            }
          }
        },
        propsChildren
      )
      this.$refs.tree.setCheckedKeys(this.ids)
      this.$emit('removeTag', this.ids, tag)
      this._emitFun()
    },
    _selectClearFun() {
      this.ids = []
      const { multiple } = this.selectParams
      this.$emit('input', multiple ? [] : '')
      this.$emit('select-clear')
      this.selectAll = false
      this._updatePopoverLocationFun()
    },
    _emitFun() {
      const { multiple } = this.selectParams
      this.$emit('input', multiple ? this.ids : this.ids.length > 0 ? this.ids[0] : '')
      this._updatePopoverLocationFun()
    },
    _updateH() {
      this.$nextTick(() => {
        this.width = this.$refs.select.$el.getBoundingClientRect().width
      })
    },
    _popoverShowFun(val) {
      this._updateH()
      this.$emit('onFocus')
    },
    _popoverHideFun(e) {
      const path = this._getEventPath(e)
      const isInside = path.some(list => {
        return list.className && typeof list.className === 'string' && list.className.indexOf('el-tree-select') !== -1
      })
      if (!isInside) {
        this.visible = false
      }
    },

    treeDataUpdateFun(data) {
      this.data = data
      if (data.length > 0) {
        setTimeout(() => {
          this._setSelectNodeFun(this.ids)
        }, 300)
      }
    },

    filterFun(val) {
      this.$refs.tree.filter(val)
    },

    setShowNumber() {
      this.showNumber = false

      this.$nextTick(() => {
        if (!this.selectParams.multiple || !this.$refs.select || !this.$refs.select.$refs.tags) {
          return
        }
        const kids = this.$refs.select.$refs.tags.children[0].children
        let contentWidth = 0
        kids.forEach(kid => {
          contentWidth += kid.offsetWidth
        })
        this.showNumber = contentWidth > ((this.$refs.select.$refs.tags.clientWidth - 35) * 0.9)
      })
    }
  }
}
</script>
<style>
.el-tree-select .de-select-option {
    display: none !important;
}
.tree-select-all {
    padding: 10px 20px 0 24px;
  }
[aria-disabled='true'] > .el-tree-node__content {
    color: inherit !important;
    background: transparent !important;
    cursor: no-drop !important;
}

.el-tree-select-popper {
    max-height: 400px;
    overflow: auto;
}
.el-tree-select-popper.disabled {
    display: none !important;
}
.el-tree-select-popper .el-button--small {
    width: 25px !important;
    min-width: 25px !important;
}

.el-tree-select-popper[x-placement^='bottom'] {
    margin-top: 5px;
}

.mb10 {
    margin-bottom: 10px;
}

.no-data {
    height: 32px;
    line-height: 32px;
    font-size: 14px;
    color: #cccccc;
    text-align: center;
}
</style>
