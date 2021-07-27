<template>
  <layout-content v-loading="$store.getters.loadingMap[$store.getters.currentPath]">
    <tree-table
      :columns="columns"
      :search-config="searchConfig"
      @search="search"
    >
      <template #toolbar>
        <el-button v-permission="['dept:add']" icon="el-icon-circle-plus-outline" @click="create">{{ $t('organization.create') }}</el-button>
      </template>
      <el-table
        ref="table"
        :data="tableData"
        lazy
        :load="loadExpandDatas"
        style="width: 100%"
        :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
        row-key="deptId"
      >
        <el-table-column :label="$t('organization.name')" prop="name" />
        <el-table-column :label="$t('organization.sub_organizations')" prop="subCount" />
        <el-table-column prop="createTime" :label="$t('organization.create_time')">
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>

        <fu-table-operations :buttons="buttons" :label="$t('commons.operating')" fix />
      </el-table>
    </tree-table>
    <!-- </complex-table> -->

    <!-- add organization form -->
    <el-dialog
      :close-on-click-modal="false"
      :title="formType=='add' ? $t('organization.create') : $t('organization.modify')"
      :visible.sync="dialogOrgAddVisible"
      width="500px"
      :destroy-on-close="true"
      @closed="closeFunc"
    >
      <el-form ref="createOrganization" inline :model="form" :rules="rule" size="small" label-width="80px">

        <el-form-item :label="$t('organization.name')" prop="name">
          <el-input v-model="form.name" style="width: 370px;" />
        </el-form-item>
        <el-form-item :label="$t('organization.sort')" prop="deptSort">
          <el-input-number
            v-model.number="form.deptSort"
            :min="0"
            :max="999"
            controls-position="right"
            style="width: 370px;"
          />
        </el-form-item>

        <el-form-item :label="$t('organization.top_org')" prop="top">
          <el-radio-group v-model="form.top" style="width: 140px" @change="topChange">
            <el-radio :label="true">{{ $t('commons.yes') }}</el-radio>
            <el-radio :label="false">{{ $t('commons.no') }}</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="$t('commons.status')" prop="enabled">
          <el-radio-group v-model="form.enabled" style="width: 140px" disabled>
            <el-radio :label="true">{{ $t('commons.enable') }}</el-radio>
            <el-radio :label="false">{{ $t('commons.disable') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="!form.top" style="margin-bottom: 0;" :label="$t('organization.parent_org')" prop="pid">
          <treeselect
            v-model="form.pid"
            :auto-load-root-options="false"
            :load-options="loadDepts"
            :options="depts"
            style="width: 370px;"
            :placeholder="$t('organization.select_parent_org')"
          />
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button type="text" @click="dialogOrgAddVisible = false">{{ $t('commons.cancel') }}</el-button>
        <el-button type="primary" @click="createDept('createOrganization')">{{ $t('commons.confirm') }}</el-button>
      </div>

    </el-dialog>

  </layout-content>
</template>

<script>
import LayoutContent from '@/components/business/LayoutContent'
import TreeTable from '@/components/business/tree-table'
import { formatCondition, formatQuickCondition } from '@/utils/index'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'
import { checkPermission } from '@/utils/permission'
import { getDeptTree, addDept, editDept, delDept, loadTable } from '@/api/system/dept'

export default {
  name: 'MsOrganization',
  components: {
    LayoutContent,
    TreeTable
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
      },
      header: '',
      columns: [],
      buttons: [
        {
          label: this.$t('commons.edit'), icon: 'el-icon-edit', type: 'primary', click: this.edit,
          show: checkPermission(['dept:edit'])
        }, {
          label: this.$t('commons.delete'), icon: 'el-icon-delete', type: 'danger', click: this._handleDelete,
          show: checkPermission(['dept:del'])
        }
      ],
      searchConfig: {
        useQuickSearch: true,
        quickPlaceholder: this.$t('role.search_by_name'),
        components: [
          { field: 'name', label: this.$t('organization.name'), component: 'FuComplexInput' }
        ]
      },

      defaultCondition: {
        field: 'pid',
        operator: 'eq',
        value: 0
      },
      defaultForm: { deptId: null, top: true, enabled: true, pid: null },
      isTableExpand: false,
      isLazy: true
    }
  },
  mounted() {
    this.form = Object.assign({}, this.defaultForm)
    this.search()
  },
  methods: {
    // create() {
    //   this.form = Object.assign({}, this.defaultForm)
    //   this.dialogOrgAddVisible = true
    //   this.formType = 'add'
    // },
    // edit(row) {
    //   this.dialogOrgAddVisible = true
    //   this.formType = 'modify'
    //   this.oldPid = row.pid
    //   this.form = Object.assign({}, row)
    //   this.treeByRow(row)
    // },
    create() {
      this.$router.push({ name: 'system-dept-form' })
    },
    edit(row) {
      this.$router.push({ name: 'system-dept-form', params: row })
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

    quick_condition(condition) {
      const result = {}
      if (condition && condition.quick) {
        for (const [key, value] of Object.entries(condition)) {
          // console.log(`${key}`)
          if (`${key}` === 'quick') {
            const v_new = Object.assign({}, value)
            v_new['field'] = 'name'
            result['name'] = v_new
          } else {
            result[`${key}`] = value
          }
        }
        return result
      }
      return Object.assign({}, condition)
    },

    setTableAttr(isSearch) {
      if (isSearch) {
        this.lazy = false
        this.isTableExpand = true
      } else {
        this.lazy = true
        this.isTableExpand = false
      }
    },
    // 加载表格数据
    search(condition) {
      condition = formatQuickCondition(condition, 'name')
      let conditionExist = false
      const temp = formatCondition(condition)
      this.tableData = []
      let param = {}
      if (temp && temp.conditions && temp.conditions.length !== 0) {
        conditionExist = true
        param = temp
      } else {
        param = { conditions: [this.defaultCondition] }
      }

      loadTable(param).then(res => {
        let data = res.data
        data = data.map(obj => {
          if (obj.subCount > 0) {
            obj.hasChildren = true
          }
          return obj
        })

        if (conditionExist) {
          data = this.buildTree(data)
        }
        this.tableData = data
        this.$nextTick(() => {
          this.tableData.forEach(node => {
            this.$refs.table.toggleRowExpansion(node, conditionExist)
          })
        })
        this.depts = null
      })
    },

    buildTree(arrs) {
      const idMapping = arrs.reduce((acc, el, i) => {
        acc[el.deptId] = i
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

    // 加载下一级子节点数据
    loadExpandDatas(row, treeNode, resolve) {
      getDeptTree(row.deptId).then(res => {
        let data = res.data
        data = data.map(obj => {
          if (obj.subCount > 0) {
            obj.hasChildren = true
          }
          return obj
        })
        this.maps.set(row.deptId, { row, treeNode, resolve })
        resolve && resolve(data)
      })
    },

    // initTableData(row, treeNode, resolve) {
    //   const _self = this
    //   const pid = (row && row.deptId) ? row.deptId : '0'
    //   getDeptTree(pid).then(response => {
    //     let data = response.data
    //     data = data.map(obj => {
    //       if (obj.subCount > 0) {
    //         obj.hasChildren = true
    //       }
    //       return obj
    //     })
    //     if (!row) {
    //       data.some(node => {
    //         node.children = null
    //       })
    //       _self.tableData = data
    //       _self.depts = null
    //     } else {
    //       this.maps.set(row.deptId, { row, treeNode, resolve })
    //       resolve && resolve(data)
    //     }
    //   })
    // },
    closeFunc() {
      this.search()
      this.form = {}
      this.oldPid = null
      this.depts = null
      this.dialogOrgAddVisible = false
    },

    // 获取弹窗内部门数据
    loadDepts({ action, parentNode, callback }) {
      if (action === 'LOAD_ROOT_OPTIONS') {
        const _self = this
        !this.depts && getDeptTree('0').then(res => {
          _self.depts = res.data.map(node => _self.normalizer(node))
          callback()
        })
      }

      if (action === 'LOAD_CHILDREN_OPTIONS') {
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
        children: node.children || null
      }
    },
    // 改变状态
    changeEnabled(data, val) {
      this.$confirm('此操作将 "停用" ' + data.name + '组织, 是否继续？', '提示', {
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
              this.search()
              this.oldPid && this.reloadByPid(this.oldPid)
              this.reloadByPid(this.form['pid'])
              this.dialogOrgAddVisible = false
            })
          } else {
            editDept(this.form).then(res => {
              this.$success(this.$t('commons.save_success'))
              this.search()
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

    _handleDelete(organization) {
      this.$confirm(this.$t('organization.delete_confirm'), '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        const requests = [{ deptId: organization.deptId, pid: organization.pid }]
        delDept(requests).then(res => {
          this.$success(this.$t('commons.delete_success'))
          this.search()
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
        this.loadExpandDatas(row, treeNode, resolve)
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
    },
    topChange(value) {
    //   console.log(value)
      !value && (this.depts = null)
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

.dialog-css >>> .el-dialog__header {
  padding: 0;
}
 ::v-deep .el-input-number .el-input__inner {
    text-align: left;
  }

</style>
