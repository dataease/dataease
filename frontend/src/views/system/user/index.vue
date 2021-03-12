<template>
  <layout-content v-loading="$store.getters.loadingMap[$store.getters.currentPath]">
    <complex-table
      :data="data"
      :columns="columns"
      :buttons="buttons"
      :header="header"
      :search-config="searchConfig"
      :pagination-config="paginationConfig"
      @select="select"
      @search="search"
    >
      <template #buttons>
        <fu-table-button icon="el-icon-circle-plus-outline" :label="$t('user.create')" @click="create" />
      </template>

      <el-table-column type="selection" fix />
      <el-table-column prop="username" label="ID" width="80" />
      <el-table-column prop="nickName" :label="$t('commons.name')" width="140" />
      <el-table-column prop="gender" label="性别" width="50" />

      <el-table-column :show-overflow-tooltip="true" prop="phone" width="200" label="电话" />
      <el-table-column :show-overflow-tooltip="true" width="200" prop="email" :label="$t('commons.email')" />
      <el-table-column :show-overflow-tooltip="true" prop="dept" :label="$t('commons.organization')">
        <template slot-scope="scope">
          <div>{{ scope.row.dept.deptName }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="status" :label="$t('commons.status')" width="60">
        <template v-slot:default="scope">
          <el-switch v-model="scope.row.enabled" :active-value="1" :inactive-value="0" inactive-color="#DCDFE6" @change="changeSwitch(scope.row)" />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" :label="$t('commons.create_time')" width="160">
        <template v-slot:default="scope">
          <span>{{ scope.row.createTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>
      <fu-table-operations :buttons="buttons" label="操作" fix />
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

        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickName">
          <el-input v-model="form.nickName" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>

        <el-form-item label="性别">
          <el-radio-group v-model="form.gender" style="width: 178px">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.enabled" style="width: 140px">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="部门" prop="dept">
          <treeselect
            v-model="form.deptId"
            :options="depts"
            :load-options="loadDepts"
            style="width: 430px"
            placeholder="选择部门"
          />
        </el-form-item>
        <el-form-item style="margin-bottom: 0;" label="角色" prop="roleIds">
          <el-select
            v-model="form.roleIds"
            style="width: 430px"
            multiple
            required="true"
            placeholder="请选择"
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
        <el-button type="primary" @click="createUser('createUserForm')">确认</el-button>
      </div>
    </el-dialog>

    <!--Changing user password in system settings-->
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
      >
        <el-form-item :label="$t('member.new_password')" prop="newpassword">
          <el-input v-model="ruleForm.newPassword" type="password" autocomplete="off" show-password />
        </el-form-item>
        <el-form-item>
          <el-input v-model="ruleForm.userId" autocomplete="off" :disabled="true" style="display:none" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="text" @click="editPasswordVisible = false">{{ $t('commons.cancel') }}</el-button>
        <el-button type="primary" @click="editUserPassword('editPasswordForm')">确认</el-button>
      </div>
    </el-dialog>
  </layout-content>
</template>

<script>
import LayoutContent from '@/components/business/LayoutContent'
import ComplexTable from '@/components/business/complex-table'
// import conditionTable from '@/components/business/condition-table'
// import CustomCondition from './CustomCondtion'
// import { GridButton } from '@/components/GridButton'
import { checkPermission } from '@/utils/permission'
import { formatCondition } from '@/utils/index'
import { PHONE_REGEX } from '@/utils/validate'
import { LOAD_CHILDREN_OPTIONS, LOAD_ROOT_OPTIONS } from '@riophae/vue-treeselect'
import Treeselect from '@riophae/vue-treeselect'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'

import { userLists, addUser, editUser, delUser, editPassword, editStatus } from '@/api/system/user'
import { allRoles } from '@/api/system/role'
import { getDeptTree } from '@/api/system/dept'

export default {

  components: { ComplexTable, LayoutContent, Treeselect },
  data() {
    return {
      header: '',
      columns: [],
      buttons: [
        {
          label: this.$t('commons.edit'), icon: 'el-icon-edit', click: this.edit
        }, {
          label: this.$t('commons.delete'), icon: 'el-icon-delete', type: 'danger', click: this.del
        }, {
          label: this.$t('member.edit_password'), icon: 'el-icon-s-tools', type: 'danger', click: this.editPassword,
          show: checkPermission(['user:editPwd'])
        }
      ],
      searchConfig: {
        useQuickSearch: false,
        quickPlaceholder: '按姓名搜索',
        components: [
        //   { field: 'name', label: '姓名', component: 'FuComplexInput', defaultOperator: 'eq' },
          { field: 'nick_name', label: '姓名', component: 'FuComplexInput' },

          {
            field: 'u.enabled',
            label: '状态',
            component: 'FuComplexSelect',
            options: [
              { label: '启用', value: '1' },
              { label: '禁用', value: '0' }
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
      }
    }
  },
  activated() {
    // this.form = Object.assign({}, this.defaultForm);
    this.allRoles()
    this.search()
  },

  methods: {
    select(selection) {
      console.log(selection)
    },

    search(condition) {
      console.log(condition) // demo只查看搜索条件，没有搜索的实现
      const temp = formatCondition(condition)
      const param = temp || {}
      const { currentPage, pageSize } = this.paginationConfig
      userLists(currentPage, pageSize, param).then(response => {
        this.data = response.data.listObject
        this.paginationConfig.total = response.data.itemCount
      })
    },

    create() {
      this.formType = 'add'
      this.form = Object.assign({}, this.defaultForm)
      this.dialogVisible = true
    },
    edit(row) {
      this.formType = 'modify'
      this.dialogVisible = true
      this.form = Object.assign({}, row)
    },
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
    }
  }
}
</script>

<style scoped>

</style>
