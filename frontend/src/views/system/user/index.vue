<template>
  <layout-content v-loading="$store.getters.loadingMap[$store.getters.currentPath]">
    <complex-table
      :data="data"
      :columns="columns"
      local-key="userGrid"
      :search-config="searchConfig"
      :pagination-config="paginationConfig"
      @select="select"
      @search="search"
      @sort-change="sortChange"
    >
      <template #toolbar>
        <el-button v-permission="['user:add']" icon="el-icon-circle-plus-outline" @click="create">{{ $t('user.create') }}</el-button>
        <!-- <fu-table-button v-permission="['user:add']" icon="el-icon-circle-plus-outline" :label="$t('user.create')" @click="create" /> -->
      </template>

      <el-table-column prop="username" label="ID" />
      <el-table-column prop="nickName" sortable="custom" :label="$t('commons.nick_name')" />
      <el-table-column prop="gender" :label="$t('commons.gender')" />

      <el-table-column :show-overflow-tooltip="true" prop="phone" :label="$t('commons.phone')" />
      <el-table-column :show-overflow-tooltip="true" prop="email" :label="$t('commons.email')" />
      <el-table-column :show-overflow-tooltip="true" prop="dept" sortable="custom" :label="$t('commons.organization')">
        <template slot-scope="scope">
          <div>{{ scope.row.dept && scope.row.dept.deptName }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="roles" :label="$t('commons.role')">
        <template slot-scope="scope">
          <div v-if="scope.row.roles && scope.row.roles.length <= 2">
            <div v-for="role in scope.row.roles" :key="role.roleId">{{ role.roleName }}</div>
          </div>
          <div v-if="scope.row.roles && scope.row.roles.length > 2">
            <el-tooltip placement="top">
              <div slot="content">
                <div v-for="role in scope.row.roles" :key="role.roleId">{{ role.roleName }}</div>
              </div>
              <div>
                <div v-for="(role,index) in scope.row.roles" v-if="index < 2" :key="role.roleId">{{ role.roleName }}</div>
                <div>...</div>
              </div>
            </el-tooltip>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="status" sortable="custom" :label="$t('commons.status')">
        <template v-slot:default="scope">
          <el-switch v-model="scope.row.enabled" :active-value="1" :inactive-value="0" :disabled="!checkPermission(['user:edit']) || scope.row.isAdmin" inactive-color="#DCDFE6" @change="changeSwitch(scope.row)" />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" sortable="custom" :label="$t('commons.create_time')">
        <template v-slot:default="scope">
          <span>{{ scope.row.createTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>
      <fu-table-operations :buttons="buttons" :label="$t('commons.operating')" fix />
    </complex-table>

    <el-dialog
      append-to-body
      :close-on-click-modal="false"
      :title="formType=='add' ? $t('user.create') : $t('user.modify')"
      :visible.sync="dialogVisible"
      width="570px"
      :destroy-on-close="true"
      @closed="handleClose"
    >
      <el-form ref="createUserForm" :inline="true" :model="form" :rules="rule" size="small" label-width="66px">

        <el-form-item :label="$t('commons.name')" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item :label="$t('commons.phone')" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item :label="$t('commons.nick_name')" prop="nickName">
          <el-input v-model="form.nickName" />
        </el-form-item>
        <el-form-item :label="$t('commons.email')" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>

        <el-form-item :label="$t('commons.gender')">
          <el-radio-group v-model="form.gender" style="width: 178px">
            <el-radio :label="$t('commons.man')">{{ $t('commons.man') }}</el-radio>
            <el-radio :label="$t('commons.woman')">{{ $t('commons.woman') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('commons.status')">
          <el-radio-group v-model="form.enabled" style="width: 140px">
            <el-radio :label="1">{{ $t('commons.enable') }} </el-radio>
            <el-radio :label="0"> {{ $t('commons.disable') }} </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('commons.organization')" prop="dept">
          <treeselect
            v-model="form.deptId"
            :options="depts"
            :load-options="loadDepts"
            style="width: 430px"
            :placeholder="$t('user.choose_org')"
          />
        </el-form-item>
        <el-form-item style="margin-bottom: 0;" :label="$t('commons.role')" prop="roleIds">
          <el-select
            v-model="form.roleIds"
            style="width: 430px"
            multiple
            required="true"
            :placeholder="$t('commons.please_select')"
            @remove-tag="deleteTag"
            @change="changeRole"
          >
            <el-option
              v-for="item in roles"
              :key="item.name"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="text" @click="dialogVisible = false">{{ $t('commons.cancel') }}</el-button>
        <el-button type="primary" @click="createUser('createUserForm')">{{ $t('commons.confirm') }}</el-button>
      </div>
    </el-dialog>

    <el-dialog
      :close-on-click-modal="false"
      :title="$t('member.edit_password')"
      :visible.sync="editPasswordVisible"
      width="30%"
      :destroy-on-close="true"
      left
      @close="handleClose"
    >
      <el-form
        ref="editPasswordForm"
        :model="ruleForm"
        label-position="right"
        label-width="120px"
        :rules="rule"
        class="demo-ruleForm"
        @keyup.enter.native="editUserPassword('editPasswordForm')"
      >
        <el-form-item :label="$t('member.new_password')" prop="newPassword">
          <el-input v-model="ruleForm.newPassword" type="password" autocomplete="off" show-password />
        </el-form-item>
        <el-form-item>
          <el-input v-model="ruleForm.userId" autocomplete="off" :disabled="true" style="display:none" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="text" @click="editPasswordVisible = false">{{ $t('commons.cancel') }}</el-button>
        <el-button type="primary" @click="editUserPassword('editPasswordForm')">{{ $t('commons.confirm') }}</el-button>
      </div>
    </el-dialog>
  </layout-content>
</template>

<script>
import LayoutContent from '@/components/business/LayoutContent'
import ComplexTable from '@/components/business/complex-table'
// import { checkPermission } from '@/utils/permission'
import { formatCondition, formatQuickCondition, addOrder, formatOrders } from '@/utils/index'
import { PHONE_REGEX } from '@/utils/validate'
import { LOAD_CHILDREN_OPTIONS, LOAD_ROOT_OPTIONS } from '@riophae/vue-treeselect'
import Treeselect from '@riophae/vue-treeselect'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'

import { userLists, addUser, editUser, delUser, editPassword, editStatus } from '@/api/system/user'
import { allRoles } from '@/api/system/role'
import { getDeptTree, treeByDeptId } from '@/api/system/dept'

export default {

  components: { ComplexTable, LayoutContent, Treeselect },
  data() {
    return {
      header: '',
      columns: [],
      buttons: [
        {
          label: this.$t('commons.edit'), icon: 'el-icon-edit', type: 'primary', click: this.edit,
          show: this.checkPermission(['user:edit'])
        }, {
          label: this.$t('commons.delete'), icon: 'el-icon-delete', type: 'danger', click: this.del,
          disabled: this.btnDisabled,
          show: this.checkPermission(['user:del'])
        }, {
          label: this.$t('member.edit_password'), icon: 'el-icon-s-tools', type: 'success', click: this.editPassword,
          show: this.checkPermission(['user:editPwd'])
        }
      ],
      searchConfig: {
        useQuickSearch: true,
        useComplexSearch: true,
        quickPlaceholder: this.$t('user.search_by_name'),
        components: [
          { field: 'nick_name', label: this.$t('commons.nick_name'), component: 'DeComplexInput' },
          {
            field: 'u.enabled',
            label: this.$t('commons.status'),
            component: 'FuComplexSelect',
            options: [
              { label: this.$t('commons.enable'), value: '1' },
              { label: this.$t('commons.disable'), value: '0' }
            ],
            multiple: false
          }
        //   { field: 'deptId', label: '组织', component: conditionTable }
        ]
      },
      paginationConfig: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
      data: [],

      dialogVisible: false,
      editPasswordVisible: false,
      form: {
        roles: [{
          id: ''
        }]
      },
      checkPasswordForm: {},
      ruleForm: {},
      rule: {
        username: [
          { required: true, message: this.$t('user.input_id'), trigger: 'blur' },
          { min: 1, max: 50, message: this.$t('commons.input_limit', [1, 50]), trigger: 'blur' },
          {
            required: true,
            pattern: '^[^\u4e00-\u9fa5]+$',
            message: this.$t('user.special_characters_are_not_supported'),
            trigger: 'blur'
          }
        ],
        nickName: [
          { required: true, message: this.$t('user.input_name'), trigger: 'blur' },
          { min: 2, max: 50, message: this.$t('commons.input_limit', [2, 50]), trigger: 'blur' },
          {
            required: true,
            message: this.$t('user.special_characters_are_not_supported'),
            trigger: 'blur'
          }
        ],
        phone: [
          {
            pattern: PHONE_REGEX,
            message: this.$t('user.mobile_number_format_is_incorrect'),
            trigger: 'blur'
          }
        ],
        email: [
          { required: true, message: this.$t('user.input_email'), trigger: 'blur' },
          {
            required: true,
            pattern: /^[a-zA-Z0-9_._-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/,
            message: this.$t('user.email_format_is_incorrect'),
            trigger: 'blur'
          }
        ],
        password: [
          { required: true, message: this.$t('user.input_password'), trigger: 'blur' },
          {
            required: true,
            pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[^]{8,30}$/,
            message: this.$t('member.password_format_is_incorrect'),
            trigger: 'blur'
          }
        ],
        newPassword: [
          { required: true, message: this.$t('user.input_password'), trigger: 'blur' },
          {
            required: true,
            pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[^]{8,30}$/,
            message: this.$t('member.password_format_is_incorrect'),
            trigger: 'blur'
          }
        ],
        roleIds: [{ required: true, message: this.$t('user.input_roles'), trigger: 'blur' }]

      },
      defaultForm: { id: null, username: null, nickName: null, gender: '男', email: null, enabled: 1, deptId: null, phone: null },
      depts: null,
      roles: [],
      roleDatas: [],
      userRoles: [],
      formType: 'add',
      permission: {
        add: ['user:add'],
        edit: ['user:edit'],
        del: ['user:del'],
        editPwd: ['user:editPwd']
      },
      orderConditions: [],
      last_condition: null
    }
  },
  mounted() {
    // this.form = Object.assign({}, this.defaultForm);
    this.allRoles()
    this.search()
  },

  methods: {
    sortChange({ column, prop, order }) {
      this.orderConditions = []
      if (!order) {
        this.search(this.last_condition)
        return
      }
      if (prop === 'dept') {
        prop = 'u.deptId'
      }
      if (prop === 'status') {
        prop = 'u.enabled'
      }
      this.orderConditions = []
      addOrder({ field: prop, value: order }, this.orderConditions)
      this.search(this.last_condition)
    },
    select(selection) {
    },

    search(condition) {
      this.last_condition = condition
      condition = formatQuickCondition(condition, 'nick_name')
      const temp = formatCondition(condition)
      const param = temp || {}
      param['orders'] = formatOrders(this.orderConditions)
      const { currentPage, pageSize } = this.paginationConfig
      userLists(currentPage, pageSize, param).then(response => {
        this.data = response.data.listObject
        this.paginationConfig.total = response.data.itemCount
      })
    },
    create() {
      this.$router.push({ name: 'system-user-form' })
    },
    // create() {
    //   this.depts = null
    //   this.formType = 'add'
    //   this.form = Object.assign({}, this.defaultForm)
    //   this.dialogVisible = true
    // },
    edit(row) {
      this.$router.push({ name: 'system-user-form', params: row })
    },
    // edit(row) {
    //   this.depts = null
    //   this.formType = 'modify'
    //   this.dialogVisible = true
    //   this.form = Object.assign({}, row)
    //   if (this.form.deptId === 0) {
    //     this.form.deptId = null
    //   }
    //   this.initDeptTree()
    // },
    editPassword(row) {
      this.editPasswordVisible = true
      const tempForm = Object.assign({}, row)
      this.ruleForm = { userId: tempForm.userId }
    },
    del(row) {
      this.$confirm(this.$t('user.delete_confirm'), '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        delUser(encodeURIComponent(row.userId)).then(res => {
          this.$success(this.$t('commons.delete_success'))
          this.search()
        })
      }).catch(() => {
        this.$info(this.$t('commons.delete_cancel'))
      })
    },
    createUser(createUserForm) {
      this.$refs[createUserForm].validate(valid => {
        if (valid) {
          const method = this.formType === 'add' ? addUser : editUser
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

    editUserPassword(editPasswordForm) {
      this.$refs[editPasswordForm].validate(valid => {
        if (valid) {
          editPassword(this.ruleForm).then(res => {
            this.$success(this.$t('commons.modify_success'))
            this.editPasswordVisible = false
            this.search()
            window.location.reload()
          })
        } else {
          return false
        }
      })
    },
    handleClose() {
      this.depts = null
      this.formType = 'add'
      this.form = {}
      this.editPasswordVisible = false
      this.dialogVisible = false
    },
    changeSwitch(row) {
      const { userId, enabled } = row
      const param = { userId: userId, enabled: enabled }
      editStatus(param).then(res => {
        this.$success(this.$t('commons.modify_success'))
      })
    },

    initDeptTree() {
      treeByDeptId(this.form.deptId || 0).then(res => {
        const results = res.data.map(node => {
          if (node.hasChildren && !node.children) {
            node.children = null
          }
          return node
        })
        this.depts = results
      })
    },
    // 获取弹窗内部门数据
    loadDepts({ action, parentNode, callback }) {
      if (action === LOAD_ROOT_OPTIONS && !this.form.deptId) {
        const _self = this
        treeByDeptId(0).then(res => {
          const results = res.data.map(node => {
            if (node.hasChildren && !node.children) {
              node.children = null
            }
            return node
          })
          _self.depts = results
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
    deleteTag(value) {
      this.userRoles.forEach(function(data, index) {
        if (data.id === value) {
          this.userRoles.splice(index, value)
        }
      }.bind(this))
    },
    changeRole(value) {
      this.userRoles = []
      value.forEach(function(data, index) {
        const role = { id: data }
        this.userRoles.push(role)
      }.bind(this))
    },
    allRoles() {
      allRoles().then(res => {
        this.roles = res.data
      })
    },
    btnDisabled(row) {
      return row.userId === 1
    }
  }
}
</script>

<style scoped>

</style>
