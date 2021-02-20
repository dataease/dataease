<template>
  <div v-loading="result.loading">

    <el-card class="table-card">
      <template v-slot:header>
        <ms-table-header :condition.sync="condition" @search="search" @create="create"
                         :create-tip="$t('user.create')" :title="$t('commons.user')"/>
      </template>

      <el-table border class="adjust-table" :data="tableData" style="width: 100%">
        <el-table-column prop="id" label="ID"/>
        <el-table-column prop="name" :label="$t('commons.name')" width="200"/>
        <el-table-column :label="$t('commons.role')" width="120">
          <template v-slot:default="scope">
            <ms-roles-tag :roles="scope.row.roles"/>
          </template>
        </el-table-column>
        <el-table-column prop="email" :label="$t('commons.email')"/>
        <el-table-column prop="status" :label="$t('commons.status')" width="120">
          <template v-slot:default="scope">
            <el-switch :disabled="currentUserId === scope.row.id" v-model="scope.row.status"
                       inactive-color="#DCDFE6"
                       active-value="1"
                       inactive-value="0"
                       @change="changeSwitch(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="$t('commons.create_time')">
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="source" :label="$t('user.source')"/>
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

      <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </el-card>

    <!--Create user-->
    <el-dialog :close-on-click-modal="false" :title="$t('user.create')" :visible.sync="createVisible" width="35%"
               @closed="handleClose"
               :destroy-on-close="true">
      <el-form :model="form" label-position="right" label-width="120px" size="small" :rules="rule" ref="createUserForm">
        <el-form-item label="ID" prop="id">
          <el-input v-model="form.id" autocomplete="off" :placeholder="$t('user.input_id_placeholder')"/>
        </el-form-item>
        <el-form-item :label="$t('commons.username')" prop="name">
          <el-input v-model="form.name" autocomplete="off" :placeholder="$t('user.input_name')"/>
        </el-form-item>
        <el-form-item :label="$t('commons.email')" prop="email">
          <el-input v-model="form.email" autocomplete="off" :placeholder="$t('user.input_email')"/>
        </el-form-item>
        <el-form-item :label="$t('commons.phone')" prop="phone">
          <el-input v-model="form.phone" autocomplete="off" :placeholder="$t('user.input_phone')"/>
        </el-form-item>
        <el-form-item :label="$t('commons.password')" prop="password" style="margin-bottom: 29px">
          <el-input v-model="form.password" autocomplete="new-password" show-password
                    :placeholder="$t('user.input_password')"/>
        </el-form-item>
        <div v-for="(role, index) in form.roles" :key="index">
          <el-form-item :label="$t('commons.role')+index"
                        :prop="'roles.' + index + '.id'"
                        :rules="{required: true, message: $t('role.please_choose_role'), trigger: 'change'}"
          >
            <el-select filterable v-model="role.id" :placeholder="$t('role.please_choose_role')">
              <el-option
                v-for="item in activeRole(role)"
                :key="item.id"
                :label="$t('role.' + item.id)"
                :value="item.id"
              >
                {{ $t('role.' + item.id) }}
              </el-option>
            </el-select>
            <el-button @click.prevent="removeRole(role)" style="margin-left: 20px;" v-if="form.roles.length > 1">
              {{ $t('commons.delete') }}
            </el-button>
          </el-form-item>
          <div v-if="role.id === 'org_admin'">
            <el-form-item :label="$t('organization.select_organization')"
                          :prop="'roles.' + index + '.ids'"
                          :rules="{required: true, message: $t('organization.select_organization'), trigger: 'change'}"
            >
              <el-select filterable v-model="role.ids" :placeholder="$t('organization.select_organization')" multiple>
                <el-option
                  v-for="item in form.orgList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
          </div>
          <div v-if="role.id === 'org_member'">
            <el-form-item :label="$t('organization.select_organization')"
                          :prop="'roles.' + index + '.ids'"
                          :rules="{required: true, message: $t('organization.select_organization'), trigger: 'change'}"
            >
              <el-select filterable v-model="role.ids" :placeholder="$t('organization.select_organization')" multiple>
                <el-option
                  v-for="item in form.orgList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
          </div>
          <div v-if="role.id === 'test_manager'">
            <el-form-item :label="$t('workspace.select')"
                          :prop="'roles.' + index + '.ids'"
                          :rules="{required: true, message: $t('workspace.select'), trigger: 'change'}"
            >
              <el-select filterable v-model="role.ids" :placeholder="$t('workspace.select')" multiple>
                <el-option
                  v-for="item in form.wsList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
          </div>
          <div v-if="role.id ==='test_user'">
            <el-form-item :label="$t('workspace.select')"
                          :prop="'roles.' + index + '.ids'"
                          :rules="{required: true, message: $t('workspace.select'), trigger: 'change'}"
            >
              <el-select filterable v-model="role.ids" :placeholder="$t('workspace.select')" multiple>
                <el-option
                  v-for="item in form.wsList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
          </div>
          <div v-if="role.id ==='test_viewer'">
            <el-form-item :label="$t('workspace.select')"
                          :prop="'roles.' + index + '.ids'"
                          :rules="{required: true, message: $t('workspace.select'), trigger: 'change'}"
            >
              <el-select filterable v-model="role.ids" :placeholder="$t('workspace.select')" multiple>
                <el-option
                  v-for="item in form.wsList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
          </div>
        </div>

        <el-form-item>
          <template>
            <el-button type="success" style="width: 100%;" @click="addRole('createUserForm')" :disabled="btnAddRole">
              {{ $t('role.add') }}
            </el-button>
          </template>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="createVisible = false"
          @confirm="createUser('createUserForm')"/>
      </template>
    </el-dialog>

    <!--Modify user information in system settings-->
    <el-dialog :close-on-click-modal="false" :title="$t('user.modify')" :visible.sync="updateVisible" width="35%"
               :destroy-on-close="true"
               @close="handleClose" v-loading="result.loading">
      <el-form :model="form" label-position="right" label-width="120px" size="small" :rules="rule" ref="updateUserForm">
        <el-form-item label="ID" prop="id">
          <el-input v-model="form.id" autocomplete="off" :disabled="true"/>
        </el-form-item>
        <el-form-item :label="$t('commons.username')" prop="name">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.email')" prop="email">
          <el-input v-model="form.email" autocomplete="off" :disabled="form.source === 'LDAP'"/>
        </el-form-item>
        <el-form-item :label="$t('commons.phone')" prop="phone">
          <el-input v-model="form.phone" autocomplete="off"/>
        </el-form-item>
        <div v-for="(role, index) in form.roles" :key="index">
          <el-form-item :label="$t('commons.role')+index"
                        :prop="'roles.' + index + '.id'"
                        :rules="{required: true, message: $t('role.please_choose_role'), trigger: 'change'}"
          >
            <el-select filterable v-model="role.id" :placeholder="$t('role.please_choose_role')" :disabled="!!role.id">
              <el-option
                v-for="item in activeRole(role)"
                :key="item.id"
                :label="$t('role.' + item.id)"
                :value="item.id">
              </el-option>
            </el-select>
            <el-button @click.prevent="removeRole(role)" style="margin-left: 20px;" v-if="form.roles.length > 1">
              {{ $t('commons.delete') }}
            </el-button>
          </el-form-item>
          <div v-if="role.id === 'org_admin'">
            <el-form-item :label="$t('organization.select_organization')"
                          :prop="'roles.' + index + '.ids'"
                          :rules="{required: true, message: $t('organization.select_organization'), trigger: 'change'}"
            >
              <el-select filterable v-model="role.ids" :placeholder="$t('organization.select_organization')" multiple>
                <el-option
                  v-for="item in form.orgList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
          </div>
          <div v-if="role.id === 'org_member'">
            <el-form-item :label="$t('organization.select_organization')"
                          :prop="'roles.' + index + '.ids'"
                          :rules="{required: true, message: $t('organization.select_organization'), trigger: 'change'}"
            >
              <el-select filterable v-model="role.ids" :placeholder="$t('organization.select_organization')" multiple>
                <el-option
                  v-for="item in form.orgList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
          </div>
          <div v-if="role.id === 'test_manager'">
            <el-form-item :label="$t('workspace.select')"
                          :prop="'roles.' + index + '.ids'"
                          :rules="{required: true, message: $t('workspace.select'), trigger: 'change'}"
            >
              <el-select filterable v-model="role.ids" :placeholder="$t('workspace.select')" multiple>
                <el-option
                  v-for="item in form.wsList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
          </div>
          <div v-if="role.id ==='test_user'">
            <el-form-item :label="$t('workspace.select')"
                          :prop="'roles.' + index + '.ids'"
                          :rules="{required: true, message: $t('workspace.select'), trigger: 'change'}"
            >
              <el-select filterable v-model="role.ids" :placeholder="$t('workspace.select')" multiple>
                <el-option
                  v-for="item in form.wsList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
          </div>
          <div v-if="role.id ==='test_viewer'">
            <el-form-item :label="$t('workspace.select')"
                          :prop="'roles.' + index + '.ids'"
                          :rules="{required: true, message: $t('workspace.select'), trigger: 'change'}"
            >
              <el-select filterable v-model="role.ids" :placeholder="$t('workspace.select')" multiple>
                <el-option
                  v-for="item in form.wsList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
          </div>
        </div>
        <el-form-item>
          <template>
            <el-button type="success" style="width: 100%;" @click="addRole('updateUserForm')" :disabled="btnAddRole">
              {{ $t('role.add') }}
            </el-button>
          </template>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="updateVisible = false"
          @confirm="updateUser('updateUserForm')"/>
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
import MsRolesTag from "../../common/components/MsRolesTag";
import {ROLE_ADMIN} from "@/common/js/constants";
import {getCurrentUser} from "../../../../common/js/utils";
import {PHONE_REGEX} from "@/common/js/regex";

export default {
  name: "MsUser",
  components: {
    MsCreateBox,
    MsTablePagination,
    MsTableHeader,
    MsTableOperator,
    MsDialogFooter,
    MsTableOperatorButton,
    MsRolesTag
  },
  data() {
    return {
      queryPath: '/user/special/list',
      deletePath: '/user/special/delete/',
      createPath: '/user/special/add',
      updatePath: '/user/special/update',
      editPasswordPath: '/user/special/password',
      result: {},
      currentUserId: '',
      createVisible: false,
      updateVisible: false,
      editPasswordVisible: false,
      btnAddRole: false,
      multipleSelection: [],
      userRole: [],
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
      }
    }
  },
  activated() {
    this.currentUserId = getCurrentUser().id;
    this.search();
    this.getAllRole();
  },
  methods: {
    create() {
      this.createVisible = true;
      this.getOrgList();
      this.getWsList();
      listenGoBack(this.handleClose);
    },
    edit(row) {
      this.updateVisible = true;
      this.form = Object.assign({}, row);
      this.$get("/organization/list", response => {
        this.$set(this.form, "orgList", response.data);
      });
      this.$get("/workspace/list", response => {
        this.$set(this.form, "wsList", response.data);
      });
      if (row.id) {
        this.$get('/userrole/all/' + encodeURIComponent(row.id), response => {
          let data = response.data;
          this.$set(this.form, "roles", data);
        });
      }
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
        this.result = this.$get(this.deletePath + encodeURIComponent(row.id), () => {
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
            this.createVisible = false;
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
            this.updateVisible = false;
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
      if (!hasRole(ROLE_ADMIN)) {
        return;
      }
      this.result = this.$post(this.buildPagePath(this.queryPath), this.condition, response => {
        let data = response.data;
        this.total = data.itemCount;
        this.tableData = data.listObject;
        let url = "/user/special/user/role";
        for (let i = 0; i < this.tableData.length; i++) {
          if (this.tableData[i].id) {
            this.$get(url + '/' + encodeURIComponent(this.tableData[i].id), result => {
              let data = result.data;
              let roles = data.roles;
              // let userRoles = result.userRoles;
              this.$set(this.tableData[i], "roles", roles);
              this.$set(this.tableData[i], "isLocalUser", this.tableData[i].source === 'LOCAL');
            });
          }
        }
      })
    },
    handleClose() {
      this.form = {roles: [{id: ''}]};
      this.btnAddRole = false;
      removeGoBackListener(this.handleClose);
      this.editPasswordVisible = false;
      this.createVisible = false;
      this.updateVisible = false;
    },
    changeSwitch(row) {
      this.$post('/user/special/update_status', row, () => {
        this.$success(this.$t('commons.modify_success'));
      })
    },
    buildPagePath(path) {
      return path + "/" + this.currentPage + "/" + this.pageSize;
    },
    handleSelectionChange(val) {
      this.multipleSelection = val;
    },
    getOrgList() {
      this.$get("/organization/list", response => {
        this.$set(this.form, "orgList", response.data);
      })
    },
    getWsList() {
      this.$get("/workspace/list", response => {
        this.$set(this.form, "wsList", response.data);
      })
    },
    getAllRole() {
      this.$get("/role/all", response => {
        this.userRole = response.data;
      })
    },
    addRole(validForm) {
      this.$refs[validForm].validate(valid => {
        if (valid) {
          let roleInfo = {};
          roleInfo.selects = [];
          let ids = this.form.roles.map(r => r.id);
          ids.forEach(id => {
            roleInfo.selects.push(id);
          })
          let roles = this.form.roles;
          roles.push(roleInfo);
          if (this.form.roles.length > this.userRole.length - 1) {
            this.btnAddRole = true;
          }
        } else {
          return false;
        }
      })
    },
    removeRole(item) {
      let index = this.form.roles.indexOf(item);
      if (index !== -1) {
        this.form.roles.splice(index, 1)
      }
      if (this.form.roles.length < this.userRole.length) {
        this.btnAddRole = false;
      }
    },
    activeRole(roleInfo) {
      return this.userRole.filter(function (role) {
        let value = true;
        if (!roleInfo.selects) {
          return true;
        }
        if (roleInfo.selects.length === 0) {
          value = true;
        }
        for (let i = 0; i < roleInfo.selects.length; i++) {
          if (role.id === roleInfo.selects[i]) {
            value = false;
          }
        }
        return value;
      })
    }
  }
}
</script>

<style scoped>
</style>
