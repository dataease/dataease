<template>
  <div v-loading="$store.getters.loadingMap[$store.getters.currentPath]">

    <el-card class="table-card">
      <template v-slot:header>
        <ms-table-header
          :permission="permission"
          :condition.sync="condition"
          :create-tip="$t('user.create')"
          :title="$t('commons.user')"
          @search="search"
          @create="create"
        />
      </template>

      <el-table border class="adjust-table" :data="tableData" style="width: 100%">
        <el-table-column prop="username" label="ID" />
        <el-table-column prop="nickName" :label="$t('commons.name')" width="200" />
        <el-table-column prop="gender" label="性别" />

        <el-table-column :show-overflow-tooltip="true" prop="phone" width="100" label="电话" />
        <el-table-column :show-overflow-tooltip="true" width="135" prop="email" :label="$t('commons.email')" />
        <el-table-column :show-overflow-tooltip="true" prop="dept" :label="$t('commons.organization')">
          <template slot-scope="scope">
            <div>{{ scope.row.dept.deptName }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('commons.status')" width="120">
          <template v-slot:default="scope">
            <el-switch v-model="scope.row.enabled" :active-value="1" :inactive-value="0" inactive-color="#DCDFE6" @change="changeSwitch(scope.row)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="$t('commons.create_time')">
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>

        <!-- <el-table-column prop="source" :label="$t('user.source')"/> -->
        <el-table-column :label="$t('commons.operating')" min-width="120px">
          <template v-slot:default="scope">
            <ms-table-operator :permission="permission" @editClick="edit(scope.row)" @deleteClick="del(scope.row)">
              <template v-slot:behind>
                <ms-table-operator-button
                  v-if="scope.row.isLocalUser"
                  :tip="$t('member.edit_password')"
                  icon="el-icon-s-tools"
                  type="success"
                  @exec="editPassword(scope.row)"
                />
              </template>
            </ms-table-operator>
          </template>
        </el-table-column>
      </el-table>

      <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize" :total="total" />

    </el-card>
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
          <el-input v-model.number="form.phone" />
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
        <el-form-item style="margin-bottom: 0;" label="角色" prop="roles">
          <el-select
            v-model="form.roleIds"
            style="width: 430px"
            multiple
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
      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="dialogVisible = false"
          @confirm="createUser('createUserForm')"
        />
      </template>
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
        size="small"
        :rules="rule"
        class="demo-ruleForm"
      >
        <el-form-item :label="$t('member.new_password')" prop="newpassword">
          <el-input v-model="ruleForm.newpassword" type="password" autocomplete="off" show-password />
        </el-form-item>
        <el-form-item>
          <el-input v-model="ruleForm.id" autocomplete="off" :disabled="true" style="display:none" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <ms-dialog-footer
          @cancel="editPasswordVisible = false"
          @confirm="editUserPassword('editPasswordForm')"
        />
      </span>
    </el-dialog>

  </div>
</template>

<script>
// import MsCreateBox from '@/metersphere/common/components/CreateBox'
import MsTablePagination from '@/metersphere/common/pagination/TablePagination'
import MsTableHeader from '@/metersphere/common/components/MsTableHeader'
import MsTableOperator from '@/metersphere/common/components/MsTableOperator'
import MsDialogFooter from '@/metersphere/common/components/MsDialogFooter'
import MsTableOperatorButton from '@/metersphere/common/components/MsTableOperatorButton'
import { listenGoBack, removeGoBackListener } from '@/metersphere/common/js/utils'
import { PHONE_REGEX } from '@/metersphere/common/js/regex'
import { LOAD_CHILDREN_OPTIONS, LOAD_ROOT_OPTIONS } from '@riophae/vue-treeselect'
import Treeselect from '@riophae/vue-treeselect'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'
import { userLists, addUser, editUser, delUser, editPassword, editStatus } from '@/api/system/user'
import { allRoles } from '@/api/system/role'
import { getDeptTree } from '@/api/system/dept'
export default {
  name: 'MsUser',

  components: {
    // MsCreateBox,
    MsTablePagination,
    MsTableHeader,
    MsTableOperator,
    MsDialogFooter,
    MsTableOperatorButton,
    Treeselect
  },
  data() {
    return {
      queryPath: '/api/user/userGrid',
      deletePath: '/api/user/delete/',
      createPath: '/api/user/create',
      updatePath: '/api/user/update',
      editPasswordPath: '/api/user/password',
      result: {},
      dialogVisible: false,
      editPasswordVisible: false,
      multipleSelection: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      condition: {},
      tableData: [],
      form: {
        roles: [{
          id: ''
        }]
      },
      checkPasswordForm: {},
      ruleForm: {},
      rule: {
        id: [
          { required: true, message: this.$t('user.input_id'), trigger: 'blur' },
          { min: 1, max: 50, message: this.$t('commons.input_limit', [1, 50]), trigger: 'blur' },
          {
            required: true,
            pattern: '^[^\u4e00-\u9fa5]+$',
            message: this.$t('user.special_characters_are_not_supported'),
            trigger: 'blur'
          }
        ],
        name: [
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
        newpassword: [
          { required: true, message: this.$t('user.input_password'), trigger: 'blur' },
          {
            required: true,
            pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[^]{8,30}$/,
            message: this.$t('member.password_format_is_incorrect'),
            trigger: 'blur'
          }
        ]
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
        del: ['user:del']
      }
    }
  },

  activated() {
    // this.form = Object.assign({}, this.defaultForm);
    this.allRoles()
    this.search()
  },

  methods: {
    create() {
      this.formType = 'add'
      this.form = Object.assign({}, this.defaultForm)
      this.dialogVisible = true

      listenGoBack(this.handleClose)
    },
    edit(row) {
      this.formType = 'modify'
      this.dialogVisible = true
      this.form = Object.assign({}, row)

      listenGoBack(this.handleClose)
    },
    editPassword(row) {
      this.editPasswordVisible = true
      this.ruleForm = Object.assign({}, row)
      listenGoBack(this.handleClose)
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
    search() {
      userLists(this.currentPage, this.pageSize, this.condition).then(response => {
        const data = response.data
        this.total = data.itemCount
        this.tableData = data.listObject
      })
      //   this.result = this.$post(this.buildPagePath(this.queryPath), this.condition, response => {
      //     const data = response.data
      //     this.total = data.itemCount
      //     this.tableData = data.listObject

    //   })
    },
    handleClose() {
      this.formType = 'add'
      this.form = {}
      removeGoBackListener(this.handleClose)
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

    handleSelectionChange(val) {
      this.multipleSelection = val
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
@import "~@/metersphere/common/css/index.css";
</style>
