<template>
  <div v-loading="result.loading">

    <el-card class="table-card">
      <template v-slot:header>
        <ms-table-header
          :condition.sync="condition"
          :create-tip="$t('menu.create')"
          :title="$t('commons.menu')"
          @search="initTableData"
          @create="create"
        />
      </template>
      <el-table
        ref="table"
        border
        class="adjust-table"
        :data="tableData"
        lazy
        :load="initTableData"
        style="width: 100%"
        :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
        row-key="menuId"
      >

        <el-table-column :show-overflow-tooltip="true" label="菜单标题" width="150px" prop="title" />
        <el-table-column prop="icon" label="图标" align="center" width="60px">
          <template slot-scope="scope">
            <svg-icon :icon-class="scope.row.icon ? scope.row.icon : ''" />
          </template>
        </el-table-column>

        <el-table-column :show-overflow-tooltip="true" prop="permission" label="权限标识" />
        <el-table-column :show-overflow-tooltip="true" prop="component" label="组件路径" />
        <el-table-column prop="iframe" label="外链" width="75px">
          <template slot-scope="scope">
            <span v-if="scope.row.iframe">是</span>
            <span v-else>否</span>
          </template>
        </el-table-column>
        <el-table-column prop="cache" label="缓存" width="75px">
          <template slot-scope="scope">
            <span v-if="scope.row.cache">是</span>
            <span v-else>否</span>
          </template>
        </el-table-column>
        <el-table-column prop="hidden" label="可见" width="75px">
          <template slot-scope="scope">
            <span v-if="scope.row.hidden">否</span>
            <span v-else>是</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建日期" width="160px">
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>

        <el-table-column :label="$t('commons.operating')">
          <template v-slot:default="scope">
            <ms-table-operator @editClick="edit(scope.row)" @deleteClick="handleDelete(scope.row)" />
          </template>
        </el-table-column> -->
      </el-table>

    </el-card>

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
          <el-radio-group v-model="form.type" size="mini" style="width: 179px">
            <el-radio-button label="0">目录</el-radio-button>
            <el-radio-button label="1">菜单</el-radio-button>
            <el-radio-button label="2">按钮</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-show="form.type!== '2'" label="菜单图标" prop="icon">
          <el-popover
            placement="bottom-start"
            width="425"
            trigger="click"
            @show="$refs['iconSelect'].reset()"
          >
            <IconSelect ref="iconSelect" @selected="selected" />
            <el-input slot="reference" v-model="form.icon" style="width: 450px;" placeholder="点击选择图标" readonly>
              <svg-icon v-if="form.icon" slot="prefix" :icon-class="form.icon" class="el-input__icon" style="height: 32px;width: 16px;" />
              <i v-else slot="prefix" class="el-icon-search el-input__icon" />
            </el-input>
          </el-popover>
        </el-form-item>
        <el-form-item v-show="form.type !== '2'" label="外链菜单" prop="iframe">
          <el-radio-group v-model="form.iframe" size="mini">
            <el-radio-button label="true">是</el-radio-button>
            <el-radio-button label="false">否</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-show="form.type=== '1'" label="菜单缓存" prop="cache">
          <el-radio-group v-model="form.cache" size="mini">
            <el-radio-button label="true">是</el-radio-button>
            <el-radio-button label="false">否</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-show="form.type !== '2'" label="菜单可见" prop="hidden">
          <el-radio-group v-model="form.hidden" size="mini">
            <el-radio-button label="false">是</el-radio-button>
            <el-radio-button label="true">否</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.type !== '2'" label="菜单标题" prop="title">
          <el-input v-model="form.title" :style=" form.type === '0' ? 'width: 450px' : 'width: 179px'" placeholder="菜单标题" />
        </el-form-item>
        <el-form-item v-if="form.type === '2'" label="按钮名称" prop="title">
          <el-input v-model="form.title" placeholder="按钮名称" style="width: 179px;" />
        </el-form-item>
        <el-form-item v-show="form.type !== '0'" label="权限标识" prop="permission">
          <el-input v-model="form.permission" :disabled="form.iframe" placeholder="权限标识" style="width: 179px;" />
        </el-form-item>
        <el-form-item v-if="form.type !== '2'" label="路由地址" prop="path">
          <el-input v-model="form.path" placeholder="路由地址" style="width: 179px;" />
        </el-form-item>
        <el-form-item label="菜单排序" prop="menuSort">
          <el-input-number v-model.number="form.menuSort" :min="0" :max="999" controls-position="right" style="width: 179px;" />
        </el-form-item>
        <el-form-item v-show="!form.iframe && form.type === '1'" label="组件名称" prop="componentName">
          <el-input v-model="form.componentName" style="width: 179px;" placeholder="匹配组件内Name字段" />
        </el-form-item>
        <el-form-item v-show="!form.iframe && form.type === '1'" label="组件路径" prop="component">
          <el-input v-model="form.component" style="width: 179px;" placeholder="组件路径" />
        </el-form-item>
        <el-form-item label="上级类目" prop="pid">
          <treeselect
            v-model="form.pid"
            :options="menus"
            :load-options="loadMenus"
            style="width: 450px;"
            placeholder="选择上级类目"
          />
        </el-form-item>
      </el-form>

      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="dialogVisible = false"
          @confirm="createMenu('menuForm')"
        />
      </template>
    </el-dialog>

    <ms-delete-confirm ref="deleteConfirm" :title="$t('menu.delete')" @delete="_handleDelete" />

  </div>
</template>

<script>
import IconSelect from '@/components/IconSelect'
import Treeselect from '@riophae/vue-treeselect'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'
import { LOAD_CHILDREN_OPTIONS, LOAD_ROOT_OPTIONS } from '@riophae/vue-treeselect'
import MsTableHeader from '@/metersphere/common/components/MsTableHeader'
import MsTableOperator from '@/metersphere/common/components/MsTableOperator'
import MsDialogFooter from '@/metersphere/common/components/MsDialogFooter'
import {
  listenGoBack,
  removeGoBackListener
} from '@/metersphere/common/js/utils'
import MsDeleteConfirm from '@/metersphere/common/components/MsDeleteConfirm'

import { addMenu, editMenu, delMenu, getMenusTree } from '@/api/system/menu'

export default {
  name: 'MsMenu',
  components: {
    MsDeleteConfirm,
    MsTableHeader,
    MsTableOperator,
    MsDialogFooter,
    Treeselect,
    IconSelect
  },
  data() {
    return {
      menus: [],
      topMunu: { id: 0, label: '顶级类目', children: null },
      formType: 'add',
      queryPath: '/api/menu/childNodes/',
      deletePath: '/api/menu/delete',
      createPath: '/api/menu/create',
      updatePath: '/api/menu/update',
      result: {},
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
      }

    }
  },
  activated() {
    this.form = Object.assign({}, this.defaultForm)
    this.initTableData()
  },
  methods: {
    create() {
      this.dialogVisible = true
      this.formType = 'add'
      listenGoBack(this.closeFunc)
    },
    search(condition) {
      console.log(condition)
    },

    edit(row) {
      this.dialogVisible = true
      this.formType = 'modify'
      this.oldPid = row.pid
      this.form = Object.assign({}, row)
      this.treeByRow(row)
      listenGoBack(this.closeFunc)
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
      const pid = row ? row.menuId : '0'

      getMenusTree(pid).then(response => {
        let data = response.data.data
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
      removeGoBackListener(this.closeFunc)
      this.dialogVisible = false
    },

    // 获取弹窗内部门数据
    loadMenus({ action, parentNode, callback }) {
      if (action === LOAD_ROOT_OPTIONS) {
        const _self = this
        !this.menus && getMenusTree('0').then(res => {
          _self.menus = res.data.data.map(node => _self.normalizer(node))
          callback()
        })
      }

      if (action === LOAD_CHILDREN_OPTIONS) {
        const _self = this
        getMenusTree(parentNode.id).then(res => {
          parentNode.children = res.data.data.map(function(obj) {
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
            this.initTableData()
            this.oldPid && this.reloadByPid(this.oldPid)
            this.reloadByPid(this.form['pid'])
            this.dialogVisible = false
          })
        } else {
          return false
        }
      })
    },
    handleDelete(menu) {
      this.$refs.deleteConfirm.open(menu)
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
        this.$set(this.$refs.table.store.states.lazyTreeNodeMap, pid, [])
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
@import "~@/metersphere/common/css/index.css";
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

.dialog-css >>> .el-dialog__header {
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
