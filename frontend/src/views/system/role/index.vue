<template>
  <layout-content v-loading="$store.getters.loadingMap[$store.getters.currentPath]">
    <!-- <div v-loading="result.loading" style="height: 100%"> -->
    <el-container style="width: 100%; height: 100%;">
      <el-aside width="70%">
        <complex-table
          highlight-current-row
          :data="tableData"
          :columns="columns"

          :search-config="searchConfig"
          :pagination-config="paginationConfig"
          @search="search"
          @row-click="rowClick"
        >
          <template #toolbar>
            <!-- <fu-table-button icon="el-icon-circle-plus-outline" :label="$t('role.add')" @click="create" /> -->
            <el-button v-permission="['role:add']" icon="el-icon-circle-plus-outline" @click="create">{{ $t('role.add') }}</el-button>
          </template>

          <el-table-column prop="name" label="名称" />
          <!-- <el-table-column prop="code" label="代码" /> -->
          <el-table-column :show-overflow-tooltip="true" prop="createTime" label="创建日期">
            <template v-slot:default="scope">
              <span>{{ scope.row.createTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <fu-table-operations :buttons="buttons" label="操作" fix />
        </complex-table>
      </el-aside>
      <el-main style="padding: 8px 20px;">
        <el-tabs v-model="activeName" @tab-click="handleClick">
          <el-tab-pane label="菜单授权" name="first">
            <el-tree
              ref="menu"
              lazy
              :data="menus"
              :default-checked-keys="menuIds"
              :load="getMenuDatas"
              :props="defaultProps"
              check-strictly
              show-checkbox
              node-key="id"
              @check="menuChange"
            />
          </el-tab-pane>
          <el-tab-pane label="数据授权" name="second">玩命开发中...</el-tab-pane>
        </el-tabs>
      </el-main>
    </el-container>
    <el-dialog
      :close-on-click-modal="false"
      :title="formType=='add' ? $t('role.add') : $t('role.modify')"
      :visible.sync="dialogVisible"
      width="580px"
      :destroy-on-close="true"
      @closed="closeFunc"
    >
      <el-form ref="roleForm" inline :model="form" :rules="rule" size="small" label-width="80px">

        <el-form-item label="角色名称" prop="name">
          <el-input v-model="form.name" style="width: 380px;" />
        </el-form-item>

        <!-- <el-form-item label="角色代码" prop="code">
          <el-input v-model="form.code" style="width: 380px;" />
        </el-form-item> -->

        <el-form-item label="描述信息" prop="description">
          <el-input v-model="form.description" style="width: 380px;" rows="5" type="textarea" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="text" @click="dialogVisible = false">{{ $t('commons.cancel') }}</el-button>
        <el-button type="primary" @click="saveRole('roleForm')">确认</el-button>
      </div>
    </el-dialog>

  </layout-content>

</template>
<script>

import LayoutContent from '@/components/business/LayoutContent'
import ComplexTable from '@/components/business/complex-table'
import { formatCondition } from '@/utils/index'
import { addRole, editRole, delRole, roleGrid, addRoleMenus, menuIds } from '@/api/system/role'

import { getMenusTree, getChild } from '@/api/system/menu'
export default {
  name: 'Role',
  components: {
    LayoutContent,
    ComplexTable
  },
  data() {
    return {

      tableData: [],
      menus: [],
      menuIds: [],
      defaultProps: { children: 'children', label: 'label', isLeaf: 'isLeaf' },
      activeName: 'first',
      dialogVisible: false,
      formType: 'add',
      form: {},
      rule: {
        name: [
          { required: true, message: '请输入名称', trigger: 'blur' }
        ],
        code: [{ required: true, message: '请输入代码', trigger: 'blur' }]
      },
      currentRow: null,
      permission: {
        add: ['role:add'],
        edit: ['role:edit'],
        del: ['role:del']
      },
      header: '',
      columns: [],
      buttons: [
        {
          label: this.$t('commons.edit'), icon: 'el-icon-edit', click: this.edit
        }, {
          label: this.$t('commons.delete'), icon: 'el-icon-delete', type: 'danger', click: this.handleDelete
        }
      ],
      searchConfig: {
        useQuickSearch: false,
        quickPlaceholder: '按名称搜索',
        components: [
        //   { field: 'name', label: '姓名', component: 'FuComplexInput', defaultOperator: 'eq' },
          { field: 'name', label: '角色名称', component: 'FuComplexInput' },

          { field: 'code', label: '角色代码', component: 'FuComplexInput', defaultOperator: 'eq' }
        //   { field: 'deptId', label: '组织', component: conditionTable }
        ]
      },
      paginationConfig: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      }
    }
  },
  watch: {
    currentRow: 'currentRowChange'
  },
  mounted() {
    this.search()
  },
  methods: {
    handleClick(tab, event) {
      console.log(tab, event)
    },
    // create() {
    //   this.form = {}
    //   this.formType = 'add'
    //   this.dialogVisible = true
    // },
    create() {
      this.$router.push({ name: '角色表单' })
    },
    search(condition) {
      const temp = formatCondition(condition)
      const param = temp || {}
      roleGrid(this.paginationConfig.currentPage, this.paginationConfig.pageSize, param).then(response => {
        const data = response.data
        this.total = data.itemCount
        this.tableData = data.listObject
      })
    },

    // edit(row) {
    //   this.formType = 'modify'
    //   this.dialogVisible = true
    //   this.form = Object.assign({}, row)
    // },
    edit(row) {
      this.$router.push({ name: '角色表单', params: row })
    },

    saveRole(roleForm) {
      this.$refs[roleForm].validate(valid => {
        if (valid) {
          const method = this.formType === 'add' ? addRole : editRole
          method(this.form).then(res => {
            this.$success(this.$t('commons.save_success'))
            this.search()
            this.dialogVisible = false
          })
        } else {
          return false
        }
      })
    },

    closeFunc() {
      this.dialogVisible = false
    },

    getMenuDatas(node, resolve) {
      const pid = node.data.id ? node.data.id : '0'
      getMenusTree(pid).then(res => {
        const datas = res.data
        const nodes = datas.map(data => this.formatNode(data))
        resolve && resolve(nodes)
      })
    },
    formatNode(node) {
      const result = {
        id: node.menuId,
        label: node.title,
        isLeaf: !node.hasChildren,
        children: node.children
      }
      return result
    },
    menuChange(menu) {
      getChild(menu.id).then(res => {
        const childIds = res.data
        if (this.menuIds.indexOf(menu.id) !== -1) {
          for (let i = 0; i < childIds.length; i++) {
            const index = this.menuIds.indexOf(childIds[i])
            if (index !== -1) {
              this.menuIds.splice(index, 1)
            }
          }
        } else {
          for (let i = 0; i < childIds.length; i++) {
            const index = this.menuIds.indexOf(childIds[i])
            if (index === -1) {
              this.menuIds.push(childIds[i])
            }
          }
        }
        console.log(this.menuIds)
        this.$refs.menu.setCheckedKeys(this.menuIds)
        this.saveMenus()
      })
    },
    saveMenus() {
      if (!this.currentRow) {
        return
      }
      const param = { roleId: this.currentRow.roleId, menuIds: this.menuIds }
      addRoleMenus(param).then(res => {
        this.search()
      })
    },
    rowClick(row, column, event) {
      menuIds(row.roleId).then(res => {
        const menuIds = res.data
        row.menuIds = menuIds
        this.currentRow = row
      })
    },
    currentRowChange(newVal, oldVal) {
      if (newVal === oldVal) {
        return
      }
      if (!newVal) {
        this.menuIds = []
        return
      }

      this.menuIds = newVal.menuIds
      this.$refs.menu.setCheckedKeys(this.menuIds)
    },
    handleDelete(row) {
      this.$confirm('确认删除角色[' + row.name + ']？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delRole(row.roleId).then(res => {
          this.$success(this.$t('commons.modify_success'))
          this.search()
        })
      }).catch(() => {

      })
    }
  }
}
</script>

<style scoped>
</style>
