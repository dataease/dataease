<template>
  <layout-content v-loading="$store.getters.loadingMap[$store.getters.currentPath]">
    <tree-table
      :columns="columns"
      :search-config="searchConfig"
      @search="search"
    >
      <template #toolbar>
        <fu-table-button v-permission="['menu:add']" icon="el-icon-circle-plus-outline" :label="$t('menu.create')" @click="create" />
      </template>

      <el-table
        ref="table"
        :data="tableData"
        lazy
        :load="initTableData"
        :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
        row-key="menuId"
      >

        <!-- <el-table-column type="selection" fix /> -->
        <el-table-column :show-overflow-tooltip="true" :label="$t('menu.tile')" prop="title" />
        <el-table-column prop="icon" :label="$t('commons.icon')" align="center">
          <template slot-scope="scope">
            <svg-icon :icon-class="scope.row.icon ? scope.row.icon : ''" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="$t('menu.create_time')">
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>

        <fu-table-operations :buttons="buttons" :label="$t('commons.operating')" fix />
      </el-table>
    </tree-table>

    <el-dialog
      :close-on-click-modal="false"
      :title="formType=='add' ? $t('menu.create') : $t('menu.modify')"
      :visible.sync="dialogVisible"
      width="580px"
      :destroy-on-close="true"
      @closed="closeFunc"
    >
      <el-form ref="menuForm" inline :model="form" :rules="rule" size="small" label-width="80px">

        <el-form-item label="菜单类型" prop="type">
          <el-radio-group v-model="form.type" size="mini" :disabled="formType!=='add'" style="width: 179px">
            <el-radio-button label="0">{{ $t('commons.catalogue') }} </el-radio-button>
            <el-radio-button label="1">{{ $t('commons.menu') }} </el-radio-button>
            <el-radio-button label="2">{{ $t('commons.button') }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-show="form.type!== 2" :label="$t('commons.icon')" prop="icon">
          <el-popover
            placement="bottom-start"
            width="425"
            trigger="click"
            @show="$refs['iconSelect'].reset()"
          >
            <IconSelect ref="iconSelect" @selected="selected" />
            <el-input slot="reference" v-model="form.icon" style="width: 450px;" :placeholder="$t('menu.select_icon')" readonly>
              <svg-icon v-if="form.icon" slot="prefix" :icon-class="form.icon" class="el-input__icon" style="height: 32px;width: 16px;" />
              <i v-else slot="prefix" class="el-icon-search el-input__icon" />
            </el-input>
          </el-popover>
        </el-form-item>

        <el-form-item v-if="form.type !== 2" :label="$t('menu.tile')" prop="title">
          <el-input v-model="form.title" :style=" form.type === '0' ? 'width: 450px' : 'width: 179px'" :placeholder="$t('menu.tile')" />
        </el-form-item>
        <el-form-item v-if="form.type === 2" :label="$t('menu.button_name')" prop="title">
          <el-input v-model="form.title" :placeholder="$t('menu.button_name')" style="width: 179px;" />
        </el-form-item>
        <el-form-item v-show="form.type !== 0" :label="$t('menu.authority_identification')" prop="permission">
          <el-input v-model="form.permission" :disabled="form.iframe || formType!=='add'" :placeholder="$t('menu.authority_identification')" style="width: 179px;" />
        </el-form-item>
        <el-form-item v-if="form.type !== 2" label="$t('menu.route_addr')" prop="path">
          <el-input v-model="form.path" :placeholder="$t('menu.route_addr')" :disabled="formType!=='add'" style="width: 179px;" />
        </el-form-item>
        <el-form-item :label="$t('menu.menu_sort')" prop="menuSort">
          <el-input-number v-model.number="form.menuSort" :min="0" :max="999" controls-position="right" style="width: 179px;" />
        </el-form-item>
        <el-form-item v-show="!form.iframe && form.type === 1" :label="$t('menu.module_name')" prop="componentName">
          <el-input v-model="form.componentName" :disabled="formType!=='add'" style="width: 179px;" placeholder="匹配组件内Name字段" />
        </el-form-item>
        <el-form-item v-show="!form.iframe && form.type === 1" label="$t('menu.path')" prop="component">
          <el-input v-model="form.component" :disabled="formType!=='add'" style="width: 179px;" :placeholder="$t('menu.path')" />
        </el-form-item>
        <el-form-item :label="$t('menu.parent_category')" prop="pid">
          <treeselect
            v-model="form.pid"
            :disabled="formType!=='add'"
            :options="menus"
            :load-options="loadMenus"
            style="width: 450px;"
            :placeholder="$t('menu.parent_category')"
            :noChildrenText="$t('commons.treeselect.no_children_text')"
            :noOptionsText="$t('commons.treeselect.no_options_text')"
            :noResultsText="$t('commons.treeselect.no_results_text')"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="text" @click="dialogVisible = false">{{ $t('commons.cancel') }}</el-button>
        <el-button type="primary" @click="createMenu('menuForm')">{{ $t('commons.confirm') }}</el-button>
      </div>

    </el-dialog>

  </layout-content>
</template>

<script>
import LayoutContent from '@/components/business/LayoutContent'
import TreeTable from '@/components/business/tree-table'
// import { checkPermission } from '@/utils/permission'
import IconSelect from '@/components/IconSelect'
import Treeselect from '@riophae/vue-treeselect'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'
import { LOAD_CHILDREN_OPTIONS, LOAD_ROOT_OPTIONS } from '@riophae/vue-treeselect'
import { checkPermission } from '@/utils/permission'
import { addMenu, editMenu, delMenu, getMenusTree, queryCondition } from '@/api/system/menu'
import { formatCondition, formatQuickCondition } from '@/utils/index'

export default {
  components: {
    TreeTable,
    LayoutContent,
    Treeselect,
    IconSelect
  },
  data() {
    return {
      menus: [],
      topMunu: { id: 0, label: '顶级目录', children: null },
      formType: 'add',
      dialogVisible: false,
      condition: {},
      tableData: [],
      maps: new Map(),
      oldPid: null,
      defaultForm: { menuId: null, title: null, menuSort: 999, path: null, component: null, componentName: null, iframe: false, pid: 0, icon: null, cache: false, hidden: false, type: 0, permission: null },
      form: {},
      rule: {
        name: [
          { required: true, message: this.$t('organization.input_name'), trigger: 'blur' },
          { min: 2, max: 25, message: this.$t('commons.input_limit', [2, 25]), trigger: 'blur' }
        ],
        description: [
          { max: 50, message: this.$t('commons.input_limit', [0, 50]), trigger: 'blur' }
        ]
      },
      permission: {
        add: ['menu:add'],
        edit: ['menu:edit'],
        del: ['menu:del']
      },

      header: '',
      columns: [],
      buttons: [
        {
          label: this.$t('commons.edit'), icon: 'el-icon-edit', type: 'primary', click: this.edit,
          show: checkPermission(['menu:edit'])
        }, {
          label: this.$t('commons.delete'), icon: 'el-icon-delete', type: 'danger', click: this._handleDelete,
          show: checkPermission(['menu:del'])
        }
      ],
      searchConfig: {
        useQuickSearch: true,
        quickPlaceholder: '按标题搜索',
        components: [

          { field: 'title', label: this.$t('menu.tile'), component: 'FuComplexInput' }

        //   {
        //     field: 'enabled',
        //     label: '状态',
        //     component: 'FuComplexSelect',
        //     options: [
        //       { label: '启用', value: '1' },
        //       { label: '禁用', value: '0' }
        //     ],
        //     multiple: false
        //   }
        ]
      }

    }
  },
  mounted() {
    this.form = Object.assign({}, this.defaultForm)
    this.initTableData()
  },
  methods: {
    // create() {
    //   this.form = Object.assign({}, this.defaultForm)
    //   this.dialogVisible = true
    //   this.formType = 'add'
    // },
    create() {
      this.$router.push({ name: 'system-menu-form' })
    },
    search(condition) {
      condition = formatQuickCondition(condition, 'title')
      const temp = formatCondition(condition)
      if (!temp || !temp.conditions || temp.conditions.length === 0) {
        this.initTableData()
        this.$nextTick(() => {
          this.tableData.forEach(node => {
            this.$refs.table.toggleRowExpansion(node, false)
          })
        })
        return
      }
      const param = temp || {}
      queryCondition(param).then(res => {
        let data = res.data
        data = data.map(obj => {
          if (obj.subCount > 0) {
            obj.hasChildren = true
          }
          return obj
        })

        if (condition) {
          data = data.map(node => {
            delete (node.hasChildren)
            return node
          })
          this.tableData = this.buildTree(data)
          this.$nextTick(() => {
            data.forEach(node => {
              this.$refs.table.toggleRowExpansion(node, true)
            })
          })
        } else {
          this.tableData = data
        }
      })
    },
    buildTree(arrs) {
      const idMapping = arrs.reduce((acc, el, i) => {
        acc[el.menuId] = i
        return acc
      }, {})
      const roots = []
      arrs.forEach(el => {
        // 判断根节点
        if (el.pid === null || el.pid === 0) {
          roots.push(el)
          return
        }
        // 用映射表找到父元素
        const parentEl = arrs[idMapping[el.pid]]
        // 把当前元素添加到父元素的`children`数组中
        parentEl.children = [...(parentEl.children || []), el]
      })
      return roots
    },

    // edit(row) {
    //   this.dialogVisible = true
    //   this.formType = 'modify'
    //   this.oldPid = row.pid
    //   this.form = Object.assign({}, row)
    //   this.treeByRow(row)
    // },
    edit(row) {
      this.$router.push({ name: 'system-menu-form', params: row })
    },

    treeByRow(row) {
      !this.menus && (this.menus = [])
      if (!this.menus.some(node => node.id === row.pid) && row.pid !== 0) {
        const arr = this.mapToArray()
        const ids = arr.map(item => item.id)
        const tempTreeNodes = this.treeByArr(arr)
        const pnodes = this.tableData.filter(node => (node.pid === 0) && (ids.indexOf(node.menuId) === -1)).map(node => this.normalizer(node))
        const showNodes = tempTreeNodes.concat(pnodes)
        this.menus = this.menus.concat(showNodes)
      }
    },

    mapToArray() {
      const result = []

      this.maps.forEach((value, key) => {
        if (value.hasOwnProperty('row')) {
          result.push(this.editNormalizer(value.row))
        }
      })
      return result
    },
    treeByArr(arr) {
      if (!Array.isArray(arr) || !arr.length) return
      const map = {}

      arr.forEach(item => {
        map[item.id] = item
      })

      const roots = []
      arr.forEach(item => {
        const parent = map[item.pid]
        if (parent) {
          (parent.children || (parent.children = [])).push(item)
        } else {
          roots.push(item)
        }
      })
      return roots
    },

    initTableData(row, treeNode, resolve) {
      const _self = this
      const pid = (row && row.menuId) ? row.menuId : '0'

      getMenusTree(pid).then(response => {
        let data = response.data
        data = data.map(obj => {
          if (obj.subCount > 0) {
            obj.hasChildren = true
          }
          return obj
        })
        if (!row) {
        //   data.some(node => node.children = null)
          _self.tableData = data
          _self.menus = []
          _self.menus.push(_self.topMunu)
        } else {
          this.maps.set(row.menuId, { row, treeNode, resolve })
          resolve && resolve(data)
        }
      })
    },
    closeFunc() {
      this.initTableData()
      this.form = this.defaultForm
      this.oldPid = null
      this.menus = null
      this.dialogVisible = false
    },

    // 获取弹窗内部门数据
    loadMenus({ action, parentNode, callback }) {
      if (action === LOAD_ROOT_OPTIONS) {
        const _self = this
        !this.menus && getMenusTree('0').then(res => {
          _self.menus = res.data.map(node => _self.normalizer(node))
          callback()
        })
      }

      if (action === LOAD_CHILDREN_OPTIONS) {
        const _self = this
        getMenusTree(parentNode.id).then(res => {
          parentNode.children = res.data.map(function(obj) {
            return _self.normalizer(obj)
          })
          callback()
        })
      }
    },
    normalizer(node) {
      if (node.hasChildren) {
        node.children = null
      }
      return {
        id: node.menuId,
        label: node.title,
        children: node.children
      }
    },
    editNormalizer(node) {
      return {
        id: node.menuId,
        pid: node.pid,
        label: node.title,
        children: node.children
      }
    },

    createMenu(menuForm) {
      this.$refs[menuForm].validate(valid => {
        if (valid) {
          let method = addMenu
          this.formType === 'modify' && this.form['menuId'] && (method = editMenu)
          method(this.form).then(res => {
            this.$success(this.$t('commons.save_success'))
            // this.initTableData()
            this.oldPid && this.reloadByPid(this.oldPid)
            this.reloadByPid(this.form['pid'])
            this.dialogVisible = false
          })
        } else {
          return false
        }
      })
    },

    _handleDelete(menu) {
      this.$confirm(this.$t('menu.delete_confirm'), '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        const request = { menuId: menu.menuId, pid: menu.pid }
        delMenu(request).then(res => {
          this.$success(this.$t('commons.delete_success'))
          this.initTableData()
          this.reloadByPid(menu.pid)
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: this.$t('commons.delete_cancelled')
        })
      })
    },
    reloadByPid(pid) {
      if (pid !== 0 && this.maps.get(pid)) {
        const { row, treeNode, resolve } = this.maps.get(pid)
        const sto = this.$refs.table['store']
        this.$set(sto.states.lazyTreeNodeMap, pid, [])
        this.initTableData(row, treeNode, resolve)
      }
    },
    // 选中图标
    selected(name) {
      this.form.icon = name
    }

  }

}
</script>

<style scoped>
.member-size {
  text-decoration: underline;
}

.org-member-id {
  float: left;
}

.org-member-email {
  float: right;
  color: #8492a6;
  font-size: 13px;
}

.select-width {
  width: 100%;
}

.dialog-css ::v-deep .el-dialog__header {
  padding: 0;
}
::v-deep .el-input-number .el-input__inner {
    text-align: left;
}
::v-deep .vue-treeselect__control,::v-deep .vue-treeselect__placeholder,::v-deep .vue-treeselect__single-value {
    height: 30px;
    line-height: 30px;
}

</style>
