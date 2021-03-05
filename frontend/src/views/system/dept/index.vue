<template>
  <div v-loading="result.loading">

    <el-card class="table-card">
      <template v-slot:header>
        <ms-table-header
          :permission="permission"
          :condition.sync="condition"
          :create-tip="$t('organization.create')"
          :title="$t('commons.organization')"
          @search="initTableData"
          @create="create"
        />
      </template>
      <!-- system menu organization table-->
      <el-table
        ref="table"
        border
        class="adjust-table"
        :data="tableData"
        lazy
        :load="initTableData"
        style="width: 100%"
        :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
        row-key="deptId"
      >
        <!-- <el-table-column :selectable="checkboxT" type="selection" width="55" /> -->
        <el-table-column label="名称" prop="name" />
        <el-table-column label="下属部门数" prop="subCount" />
        <el-table-column label="状态" align="center" prop="enabled">
          <template slot-scope="scope">
            <el-switch
              v-model="scope.row.enabled"
              :disabled="scope.row.id === 1"
              active-color="#409EFF"
              inactive-color="#F56C6C"
              @change="changeEnabled(scope.row, scope.row.enabled,)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建日期">
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>

        <el-table-column :label="$t('commons.operating')">
          <template v-slot:default="scope">
            <ms-table-operator :permission="permission" @editClick="edit(scope.row)" @deleteClick="handleDelete(scope.row)" />
          </template>
        </el-table-column> -->
      </el-table>

    </el-card>

    <!-- add organization form -->
    <el-dialog
      :close-on-click-modal="false"
      :title="formType=='add' ? $t('organization.create') : $t('organization.modify')"
      :visible.sync="dialogOrgAddVisible"
      width="30%"
      :destroy-on-close="true"
      @closed="closeFunc"
    >
      <el-form ref="createOrganization" inline :model="form" :rules="rule" size="small" label-width="80px">

        <el-form-item label="部门名称" prop="name">
          <el-input v-model="form.name" style="width: 370px;" />
        </el-form-item>
        <el-form-item label="部门排序" prop="deptSort">
          <el-input-number
            v-model.number="form.deptSort"
            :min="0"
            :max="999"
            controls-position="right"
            style="width: 370px;"
          />
        </el-form-item>

        <el-form-item label="顶级部门" prop="top">
          <el-radio-group v-model="form.top" style="width: 140px">
            <el-radio :label="true">是</el-radio>
            <el-radio :label="false">否</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="状态" prop="enabled">
          <el-radio-group v-model="form.enabled" style="width: 140px">
            <el-radio :label="true">启用</el-radio>
            <el-radio :label="false">停用</el-radio>
          </el-radio-group>

          <!-- <el-radio v-for="item in dict.dept_status" :key="item.id" v-model="form.enabled" :label="item.value">{{ item.label }}</el-radio> -->
        </el-form-item>
        <el-form-item v-if="!form.top" style="margin-bottom: 0;" label="上级部门" prop="pid">
          <treeselect
            v-model="form.pid"
            :auto-load-root-options="false"
            :load-options="loadDepts"
            :options="depts"
            style="width: 370px;"
            placeholder="选择上级类目"
          />
        </el-form-item>
      </el-form>
      <!-- <div slot="footer" class="dialog-footer">
        <el-button type="text" @click="crud.cancelCU">取消</el-button>
        <el-button :loading="crud.status.cu === 2" type="primary" @click="crud.submitCU">确认</el-button>
      </div> -->
      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="dialogOrgAddVisible = false"
          @confirm="createDept('createOrganization')"
        />
      </template>
    </el-dialog>

    <ms-delete-confirm ref="deleteConfirm" :title="$t('organization.delete')" @delete="_handleDelete" />

  </div>
</template>

<script>
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
import { getDeptTree, addDept, editDept, delDept } from '@/api/system/dept'

export default {
  name: 'MsOrganization',
  components: {
    MsDeleteConfirm,
    MsTableHeader,
    MsTableOperator,
    MsDialogFooter,
    Treeselect
  },
  data() {
    return {
      rootNodeLoaded: false,
      depts: null,
      formType: 'add',
      queryPath: '/api/dept/childNodes',
      deletePath: '/api/dept/delete',
      createPath: '/api/dept/create',
      updatePath: '/api/dept/update',
      changeStatusPath: '/api/dept/updateStatus',
      result: {},
      dialogOrgAddVisible: false,
      condition: {},
      tableData: [],
      maps: new Map(),
      oldPid: null,
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
        add: ['dept:add'],
        edit: ['dept:edit'],
        del: ['dept:del']
      }

    }
  },
  activated() {
    this.initTableData()
  },
  methods: {
    create() {
      this.dialogOrgAddVisible = true
      this.formType = 'add'
      listenGoBack(this.closeFunc)
    },
    search(condition) {
      console.log(condition)
    },

    edit(row) {
      this.dialogOrgAddVisible = true
      this.formType = 'modify'
      this.oldPid = row.pid
      this.form = Object.assign({}, row)
      this.treeByRow(row)
      listenGoBack(this.closeFunc)
    },

    treeByRow(row) {
      !this.depts && (this.depts = [])
      if (!this.depts.some(node => node.id === row.pid) && row.pid !== 0) {
        const arr = this.mapToArray()
        const ids = arr.map(item => item.id)
        const tempTreeNodes = this.treeByArr(arr)
        const pnodes = this.tableData.filter(node => (node.pid === 0) && (ids.indexOf(node.deptId) === -1)).map(node => this.normalizer(node))
        const showNodes = tempTreeNodes.concat(pnodes)
        this.depts = this.depts.concat(showNodes)
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
      const pid = row ? row.deptId : '0'
      getDeptTree(pid).then(response => {
        let data = response.data
        data = data.map(obj => {
          if (obj.subCount > 0) {
            obj.hasChildren = true
          }
          return obj
        })
        if (!row) {
          data.some(node => {
            node.children = null
          })
          _self.tableData = data
          _self.depts = null
        } else {
          this.maps.set(row.deptId, { row, treeNode, resolve })
          resolve && resolve(data)
        }
      })
    },
    closeFunc() {
      this.initTableData()
      this.form = {}
      this.oldPid = null
      this.depts = null
      removeGoBackListener(this.closeFunc)
      this.dialogOrgAddVisible = false
    },

    // 获取弹窗内部门数据
    loadDepts({ action, parentNode, callback }) {
      if (action === LOAD_ROOT_OPTIONS) {
        const _self = this
        !this.depts && getDeptTree('0').then(res => {
          _self.depts = res.data.map(node => _self.normalizer(node))
          callback()
        })
      }

      if (action === LOAD_CHILDREN_OPTIONS) {
        const _self = this
        getDeptTree(parentNode.id).then(res => {
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
        id: node.deptId,
        label: node.name,
        children: node.children
      }
    },
    editNormalizer(node) {
      return {
        id: node.deptId,
        pid: node.pid,
        label: node.name,
        children: node.children
      }
    },
    // 改变状态
    changeEnabled(data, val) {
      this.$confirm('此操作将 "停用" ' + data.name + '部门, 是否继续？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const param = { deptId: data.deptId, status: data.enabled }
        this.$post(this.changeStatusPath, param, () => {
          this.$success(this.$t('commons.modify_success'))
        })
      }).catch(() => {
        data.enabled = !data.enabled
      })
    },
    // checkboxT(row, rowIndex) {
    //   return row.depId !== 1
    // },
    createDept(createOrganizationForm) {
      this.$refs[createOrganizationForm].validate(valid => {
        if (valid) {
          if (this.formType !== 'modify') {
            addDept(this.form).then(res => {
              this.$success(this.$t('commons.save_success'))
              this.initTableData()
              this.oldPid && this.reloadByPid(this.oldPid)
              this.reloadByPid(this.form['pid'])
              this.dialogOrgAddVisible = false
            })
          } else {
            editDept(this.form).then(res => {
              this.$success(this.$t('commons.save_success'))
              this.initTableData()
              this.oldPid && this.reloadByPid(this.oldPid)
              this.reloadByPid(this.form['pid'])
              this.dialogOrgAddVisible = false
            })
          }
        } else {
          return false
        }
      })
    },
    handleDelete(organization) {
      this.$refs.deleteConfirm.open(organization)
    },
    _handleDelete(organization) {
      this.$confirm(this.$t('organization.delete_confirm'), '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        const requests = [{ deptId: organization.deptId, pid: organization.pid }]
        delDept(requests).then(res => {
          this.$success(this.$t('commons.delete_success'))
          this.initTableData()
          this.reloadByPid(organization.pid)
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
    array2Tree(arr) {
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

</style>
