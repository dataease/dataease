<template>
  <div v-loading="result.loading">

    <el-card class="table-card">
      <template v-slot:header>
        <ms-table-header :condition.sync="condition" @search="search" @create="create"
                         :create-tip="$t('user.create')" :title="$t('commons.user')"/>
      </template>

      <el-table border class="adjust-table" :data="tableData" style="width: 100%">
        <el-table-column prop="username" label="ID"/>
        <el-table-column prop="nickName" :label="$t('commons.name')" width="200"/>
        <el-table-column prop="gender" label="性别" />

        <el-table-column :show-overflow-tooltip="true" prop="phone" width="100" label="电话" />
        <el-table-column :show-overflow-tooltip="true" width="135" prop="email" :label="$t('commons.email')" />
        <el-table-column :show-overflow-tooltip="true" prop="dept" :label="$t('commons.organization')">
            <template slot-scope="scope">
                <div>{{ scope.row.dept.name }}</div>
            </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('commons.status')" width="120">
          <template v-slot:default="scope">
            <el-switch v-model="scope.row.enabled" inactive-color="#DCDFE6" @change="changeSwitch(scope.row)" />                                                                             
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
            <ms-table-operator @editClick="edit(scope.row)" @deleteClick="del(scope.row)">
              <template v-slot:behind>
                <ms-table-operator-button :tip="$t('member.edit_password')" icon="el-icon-s-tools"
                                          type="success" @exec="editPassword(scope.row)" v-if="scope.row.isLocalUser"/>
              </template>
            </ms-table-operator>
          </template>
        </el-table-column>
      </el-table>

      <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize" :total="total"/>
                           
    </el-card>
    <el-dialog append-to-body :close-on-click-modal="false" :title="$t('user.create')" :visible.sync="dialogVisible"  width="570px" @closed="handleClose"
               :destroy-on-close="true">
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
            
            <!-- <el-form-item :label="$t('commons.password')" prop="password" >
              <el-input v-model="form.password" autocomplete="new-password" show-password
                    :placeholder="$t('user.input_password')" style="width: 175px;"/>
            </el-form-item> -->
            <el-form-item label="性别">
              <el-radio-group v-model="form.gender" style="width: 178px">
                <el-radio label="男">男</el-radio>
                <el-radio label="女">女</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="状态">
              <el-radio-group v-model="form.enabled" style="width: 140px">
                <el-radio label='1'>启用</el-radio>
                <el-radio label='0'>停用</el-radio>
            </el-radio-group>
            </el-form-item>
            <el-form-item  label="部门" prop="dept">
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
          @confirm="createUser('createUserForm')"/>
      </template>
    </el-dialog>

    
    <!--Changing user password in system settings-->
    <el-dialog :close-on-click-modal="false" :title="$t('member.edit_password')" :visible.sync="editPasswordVisible"
               width="30%"
               :destroy-on-close="true" @close="handleClose" left>
      <el-form :model="ruleForm" label-position="right" label-width="120px" size="small" :rules="rule"
               ref="editPasswordForm" class="demo-ruleForm">
        <el-form-item :label="$t('member.new_password')" prop="newpassword">
          <el-input type="password" v-model="ruleForm.newpassword" autocomplete="off" show-password></el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="ruleForm.id" autocomplete="off" :disabled="true" style="display:none"/>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <ms-dialog-footer
          @cancel="editPasswordVisible = false"
          @confirm="editUserPassword('editPasswordForm')"/>
      </span>
    </el-dialog>

  </div>
</template>

<script>
import MsCreateBox from "../CreateBox";
import MsTablePagination from "../../common/pagination/TablePagination";
import MsTableHeader from "../../common/components/MsTableHeader";
import MsTableOperator from "../../common/components/MsTableOperator";
import MsDialogFooter from "../../common/components/MsDialogFooter";
import MsTableOperatorButton from "../../common/components/MsTableOperatorButton";
import {hasRole, listenGoBack, removeGoBackListener} from "@/common/js/utils";
import {ROLE_ADMIN} from "@/common/js/constants";
import {getCurrentUser} from "../../../../common/js/utils";
import {PHONE_REGEX} from "@/common/js/regex";
import { LOAD_CHILDREN_OPTIONS, LOAD_ROOT_OPTIONS } from '@riophae/vue-treeselect'
import Treeselect from '@riophae/vue-treeselect'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'
export default {
  name: "MsUser",
  components: {
    MsCreateBox,
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
          {required: true, message: this.$t('user.input_id'), trigger: 'blur'},
          {min: 1, max: 50, message: this.$t('commons.input_limit', [1, 50]), trigger: 'blur'},
          {
            required: true,
            pattern: '^[^\u4e00-\u9fa5]+$',
            message: this.$t('user.special_characters_are_not_supported'),
            trigger: 'blur'
          }
        ],
        name: [
          {required: true, message: this.$t('user.input_name'), trigger: 'blur'},
          {min: 2, max: 50, message: this.$t('commons.input_limit', [2, 50]), trigger: 'blur'},
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
          {required: true, message: this.$t('user.input_email'), trigger: 'blur'},
          {
            required: true,
            pattern: /^[a-zA-Z0-9_._-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/,
            message: this.$t('user.email_format_is_incorrect'),
            trigger: 'blur'
          }
        ],
        password: [
          {required: true, message: this.$t('user.input_password'), trigger: 'blur'},
          {
            required: true,
            pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[^]{8,30}$/,
            message: this.$t('member.password_format_is_incorrect'),
            trigger: 'blur'
          }
        ],
        newpassword: [
          {required: true, message: this.$t('user.input_password'), trigger: 'blur'},
          {
            required: true,
            pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[^]{8,30}$/,
            message: this.$t('member.password_format_is_incorrect'),
            trigger: 'blur'
          }
        ]
      },
      defaultForm: { id: null, username: null, nickName: null, gender: '男', email: null, enabled: '1',  deptId: null, phone: null },
      depts: null,
      roles: [],
      roleDatas: [],
      userRoles: []
    }
  },
  activated() {
    //this.form = Object.assign({}, this.defaultForm);
    this.allRoles()
    this.search()
    
  },
  methods: {
    create() {
      this.form = Object.assign({}, this.defaultForm);
      this.dialogVisible = true;
      
      listenGoBack(this.handleClose);
    },
    edit(row) {
      this.dialogVisible = true;
      this.form = Object.assign({}, row);
      
      listenGoBack(this.handleClose);
    },
    editPassword(row) {
      this.editPasswordVisible = true;
      this.ruleForm = Object.assign({}, row);
      listenGoBack(this.handleClose);
    },
    del(row) {
      this.$confirm(this.$t('user.delete_confirm'), '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        this.result = this.$get(this.deletePath + encodeURIComponent(row.userId), () => {
          this.$success(this.$t('commons.delete_success'));
          this.search();
        });
      }).catch(() => {
        this.$info(this.$t('commons.delete_cancel'));
      });
    },
    createUser(createUserForm) {
      this.$refs[createUserForm].validate(valid => {
        if (valid) {
          this.result = this.$post(this.createPath, this.form, () => {
            this.$success(this.$t('commons.save_success'));
            this.search();
            this.dialogVisible = false;
          });
        } else {
          return false;
        }
      })
    },
    updateUser(updateUserForm) {
      this.$refs[updateUserForm].validate(valid => {
        if (valid) {
          this.result = this.$post(this.updatePath, this.form, () => {
            this.$success(this.$t('commons.modify_success'));
            this.dialogVisible = false;
            this.search();
          });
        } else {
          return false;
        }
      })
    },
    editUserPassword(editPasswordForm) {
      this.$refs[editPasswordForm].validate(valid => {
        if (valid) {
          this.result = this.$post(this.editPasswordPath, this.ruleForm, () => {
            this.$success(this.$t('commons.modify_success'));
            this.editPasswordVisible = false;
            this.search();
            window.location.reload();
          });
        } else {
          return false;
        }
      })
    },
    search() {
      
      this.result = this.$post(this.buildPagePath(this.queryPath), this.condition, response => {
        let data = response.data;
        this.total = data.itemCount;
        this.tableData = data.listObject;
        // let url = "/user/special/user/role";
        // for (let i = 0; i < this.tableData.length; i++) {
        //   if (this.tableData[i].id) {
        //     this.$get(url + '/' + encodeURIComponent(this.tableData[i].id), result => {
        //       let data = result.data;
        //       let roles = data.roles;
        //       // let userRoles = result.userRoles;
        //       this.$set(this.tableData[i], "roles", roles);
        //       this.$set(this.tableData[i], "isLocalUser", this.tableData[i].source === 'LOCAL');
        //     });
        //   }
        // }
      })
    },
    handleClose() {
      this.form = {};
      removeGoBackListener(this.handleClose);
      this.editPasswordVisible = false;
      this.dialogVisible = false;
    },
    changeSwitch(row) {
      this.$post('/api/user/update_status', row, () => {
        this.$success(this.$t('commons.modify_success'));
      })
    },
    buildPagePath(path) {
      return path + "/" + this.currentPage + "/" + this.pageSize;
    },
    handleSelectionChange(val) {
      this.multipleSelection = val;
    },
    // 获取弹窗内部门数据
    loadDepts({ action, parentNode, callback }) {
      if (action === LOAD_ROOT_OPTIONS) {
        let _self = this;
        !this.depts && this.$post("api/dept/childNodes/0", null, (res) => {
              _self.depts = res.data.map(node => _self.normalizer(node))
              callback()
        })
      }

      if (action === LOAD_CHILDREN_OPTIONS) {
        let _self = this;
        this.$post("api/dept/childNodes/"+parentNode.id, null, (res) => {
            parentNode.children = res.data.map(function(obj) {
                return _self.normalizer(obj)
            })
            callback()
        })
      }

    },
    normalizer(node) {
      if(node.hasChildren){
        node.children = null
      }
      return {
        id: node.deptId,
        label:node.name,
        children:node.children
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
    allRoles(){
        this.$post("/api/role/all", null, res => {            
            this.roles = res.data
        })
    }
    
  }
}
</script>

<style scoped>
</style>
