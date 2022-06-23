<!--
 * @moduleName: 下拉树组件
 * @Author: dawdler
 * @Date: 2018-12-19 14:03:03
 * @LastModifiedBy: dawdler
 * @LastEditTime: 2020-12-26 14:51:20
 -->
<template>
  <div class="el-tree-select" :class="selectClass">
    <!-- 下拉文本 -->
    <el-select
      :id="'el-tree-select-' + guid"
      ref="select"
      v-model="labels"
      v-popover:popover
      :style="styles"
      :collapse-tags="showNumber"
      class="el-tree-select-input"
      :disabled="disabled"
      popper-class="select-option"
      v-bind="selectParams"
      :popper-append-to-body="false"
      :filterable="false"
      :multiple="selectParams.multiple"
      :title="labels"
      @remove-tag="_selectRemoveTag"
      @clear="_selectClearFun"
      @focus="_popoverShowFun"
    />
    <!-- 弹出框 -->
    <el-popover ref="popover" v-model="visible" :placement="placement" :transition="transition" :popper-class="popperClass" :width="width" trigger="click">
      <!-- 是否显示搜索框 -->
      <el-input v-if="treeParams.filterable" v-model="keywords" size="mini" class="input-with-select mb10">
        <el-button slot="append" icon="el-icon-search" @click="_searchFun" />
      </el-input>
      <el-scrollbar tag="div" wrap-class="el-select-dropdown__wrap" view-class="el-select-dropdown__list" class="is-empty">
        <!-- 树列表 -->
        <el-tree
          v-show="data.length > 0"
          ref="tree"
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
        <!-- 暂无数据 -->
        <div v-if="data.length === 0" class="no-data">暂无数据</div>
      </el-scrollbar>
    </el-popover>
  </div>
</template>

<script>
import { on, off } from './dom'
import { each, guid } from './utils'
// @group api
export default {
  name: 'ElTreeSelect',
  components: {},
  props: {
    // v-model,存储的是treeParams.data里面的id
    value: {
      // `String` / `Array` / `Number`
      type: [String, Array, Number],
      // `''`
      default() {
        return ''
      }
    },
    // el-select样式
    styles: {
      type: Object,
      // {}
      default() {
        return {
          width: '100%'
        }
      }
    },
    // 下拉框 挂类
    selectClass: {
      type: String,
      default() {
        return ''
      }
    },
    // popover 挂类
    popoverClass: {
      type: String,
      default() {
        return ''
      }
    },
    // 是否禁用文本框
    disabled: {
      type: Boolean,
      // false
      default() {
        return false
      }
    },
    // 弹出框位置
    placement: {
      type: String,
      //  bottom
      default() {
        return 'bottom'
      }
    },
    // 弹出框过渡动画
    transition: {
      type: String,
      //  el-zoom-in-top
      default() {
        return 'el-zoom-in-top'
      }
    },
    // 树渲染方法，具体参考el-tree Function(h, { node, data, store }) {}
    // eslint-disable-next-line vue/require-default-prop
    treeRenderFun: Function,
    // 搜索过滤方法，具体参考el-tree Function(h, { value, data, node }) {}
    // eslint-disable-next-line vue/require-default-prop
    filterNodeMethod: Function,
    /*
        文本框参数，几乎支持el-select所有的API<br>
        取消参数：<br>
        设定下拉框的弹出框隐藏：<br>
        `:popper-append-to-body="false"` <br>
        搜索从弹出框里面执行： <br>
        `filterable="false"`
        */
    selectParams: {
      type: Object,
      /*
            Object默认参数：<br><br>
            是否可以清空选项：<br>
            `clearable: true,`<br><br>
            是否禁用：<br>
            `disabled: false,`<br><br>
            搜索框placeholder文字：<br>
            `placeholder: '请选择',`<br><br>
            */
      default() {
        return {
          clearable: true,
          disabled: false,
          placeholder: '请选择'
        }
      }
    },
    /*
        下拉树参数，几乎支持el-tree所有的API<br>
         取消参数:<br>
        `:show-checkbox="selectParams.multiple"`<br>
        使用下拉框参数multiple判断是否对树进行多选<br>
        取消对el-tree的人为传参show-checkbox<br>
        `:node-key="propsValue"`     自动获取treeParams.props.value<br>
        `:draggable="false"`         屏蔽拖动
        */
    treeParams: {
      type: Object,
      /*
            Object默认参数：<br><br>
            在有子级的情况下是否点击父级关闭弹出框,false 只能点击子级关闭弹出框：<br><br>
            `clickParent: false`<br><br>
            是否显示搜索框：<br><br>
            `filterable: false`<br><br>
            是否只是叶子节点：<br><br>
            `leafOnly: false`<br><br>
            是否包含半选节点：<br><br>
            `includeHalfChecked: false`<br><br>
            下拉树的数据：<br><br>
            `data:[]`<br><br>
            下拉树的props：<br><br>
            `props: {`<br>
                `children: 'children',`<br>
                `label: 'name',`<br>
                `value: 'flowId',`<br>
                `disabled: 'disabled'`<br>
            `}`
            */
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
    }
  },
  data() {
    return {
      guid: guid(),
      propsValue: 'flowId',
      propsLabel: 'name',
      propsCode: null, // 可能有空的情况
      propsDisabled: 'disabled',
      propsChildren: 'children',
      leafOnly: false,
      includeHalfChecked: false,
      data: [],
      keywords: '',
      labels: '', // 存储名称，用于下拉框显示内容
      ids: [], // 存储id
      visible: false, // popover v-model
      width: 150,
      showParent: false,
      showNumber: false
    }
  },
  computed: {
    popperClass() {
      const _c = 'el-tree-select-popper ' + this.popoverClass
      return this.disabled ? _c + ' disabled ' : _c
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
    this.propsCode = props.code || null // 可能为空
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
    })
  },
  beforeDestroy() {
    off(document, 'mouseup', this._popoverHideFun)
  },
  methods: {
    _treeCheckChange() {
      this.$emit("treeCheckChange")
    },
    // 根据类型判断单选，多选
    _setMultipleFun() {
      let multiple = false
      if (this.value instanceof Array) {
        multiple = true
      }
      this.$set(this.selectParams, 'multiple', multiple)
    },
    // 输入文本框输入内容抛出
    _searchFun() {
      /*
            对外抛出搜索方法，自行判断是走后台查询，还是前端过滤<br>
            前端过滤：this.$refs.treeSelect.$refs.tree.filter(value);<br>
            后台查询：this.$refs.treeSelect.treeDataUpdateFun(data);
            */
      this.$emit('searchFun', this.keywords)
    },
    //  根据id筛选当前树名称，以及选中树列表
    _setSelectNodeFun(ids) {
      const el = this.$refs.tree
      if (!el) {
        throw new Error('找不到tree dom')
      }
      const { multiple } = this.selectParams
      // 长度为0，清空选择
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
        // element-ui bug. 如果是父子节点全选 el.setCheckedKeys([非全量id]);之后el.getCheckedNodes()还是全量
        el.getCheckedNodes(this.leafOnly, this.includeHalfChecked).forEach(item => {
          el.setChecked(item, false)
        })
        ids.forEach(id => {
          el.setChecked(id, true)
        })
        const nodes = el.getCheckedNodes(this.leafOnly, this.includeHalfChecked)
        if (!this.showParent) {
          if (this.propsCode) {
          // 如果有code   labels=code(name)
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
            // 如果有code   labels=code(name)
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
    // 更新popover位置
    _updatePopoverLocationFun() {
      // dom高度还没有更新，做一个延迟
      setTimeout(() => {
        this.$refs.popover.updatePopper()
      }, 50)
    },
    // 获取MouseEvent.path 针对浏览器兼容性兼容ie11,edge,chrome,firefox,safari
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
    // 树过滤
    _filterFun(value, data, node) {
      if (!value) return true
      return data[this.propsLabel].indexOf(value) !== -1
    },
    // 树点击
    _treeNodeClickFun(data, node, vm) {
      const { multiple } = this.selectParams
      if (multiple) return // 多选 不允许点击节点
      const { clickParent } = this.treeParams
      const checkStrictly = this.treeParams['check-strictly']
      const { propsValue, propsChildren, propsDisabled } = this
      const children = data[propsChildren] || []
      if (data[propsDisabled]) {
        // 禁用
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
          // 多选，不关闭，单选，判断是否允许点击父级关闭弹出框
          if (!clickParent) {
            // 如果不允许点击父级,自身为末级，允许点击之后关闭
            if (children.length === 0) {
              this.ids = [data[propsValue]]
              this.visible = false
            } else {
              // 不允许父级，阻止继续派发
              return false
            }
          } else {
            this.ids = [data[propsValue]]
            this.visible = false
          }
        } else {
          if (!clickParent && children.length === 0) {
            // 如果不能点击父级
            this.ids.push(data[propsValue])
          } else if (clickParent) {
            // 允许点击父级
            this.ids.push(data[propsValue])
            // 如果父子关联，将子节点push进勾选项
            if (!checkStrictly && children.length) {
              children.forEach(item => {
                this.ids.push(item[propsValue])
              })
            }
          }
        }
      }
      this._emitFun()
      /*
            点击节点，对外抛出   `data, node, vm`<br>
            `data:` 当前点击的节点数据<br>
            `node:` 当前点击的node<br>
            `vm:` 当前组件的vm
            */
      this.$emit('node-click', data, node, vm)
    },
    // 树勾选
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
      /*
        点击复选框，对外抛出   `data, node, vm`<br>
        `data:` 当前点击的节点数据<br>
        `node:` 当前点击的node<br>
        `vm:` 当前组件的vm
        */
      node.checkedKeys = checkedNodes.map(node => node.id)
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
    // 下拉框移除tag时触发
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
    // 下拉框清空数据
    _selectClearFun() {
      this.ids = []
      const { multiple } = this.selectParams
      // 下拉框清空，对外抛出``this.$emit('input', multiple ? [] : '');`
      this.$emit('input', multiple ? [] : '')
      // 下拉框清空，对外抛出``this.$emit('select-clear');`
      this.$emit('select-clear')
      this._updatePopoverLocationFun()
    },
    // 判断类型，抛出当前选中id
    _emitFun() {
      const { multiple } = this.selectParams
      this.$emit('input', multiple ? this.ids : this.ids.length > 0 ? this.ids[0] : '')
      this._updatePopoverLocationFun()
    },
    // 更新宽度
    _updateH() {
      this.$nextTick(() => {
        this.width = this.$refs.select.$el.getBoundingClientRect().width
      })
    },
    // 显示弹出框的时候容错，查看是否和el宽度一致
    _popoverShowFun(val) {
      this._updateH()
      this.$emit('onFoucs')
    },
    // 判断是否隐藏弹出框
    _popoverHideFun(e) {
      const path = this._getEventPath(e)
      const isInside = path.some(list => {
        // 鼠标在弹出框内部，阻止隐藏弹出框
        return list.className && typeof list.className === 'string' && list.className.indexOf('el-tree-select') !== -1
      })
      if (!isInside) {
        this.visible = false
      }
    },
    /**
         * @vuese
         * 树列表更新数据
         * @arg Array
         */
    treeDataUpdateFun(data) {
      this.data = data
      // 数据更新完成之后，判断是否回显内容
      if (data.length > 0) {
        setTimeout(() => {
          this._setSelectNodeFun(this.ids)
        }, 300)
      }
    },

    /**
         * @vuese
         * 本地过滤方法
         * @arg String
         */
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
.el-tree-select .select-option {
    display: none !important;
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
