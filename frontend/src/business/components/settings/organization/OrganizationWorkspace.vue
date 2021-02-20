<template>
  <div>
    <el-card class="table-card" v-loading="result.loading">
      <template v-slot:header>
        <ms-table-header :condition.sync="condition" @search="list" @create="create"
                         :create-tip="$t('workspace.create')" :title="$t('commons.workspace')"/>
      </template>
      <el-table border class="adjust-table" :data="items" style="width: 100%">
        <el-table-column prop="name" :label="$t('commons.name')"/>
        <el-table-column prop="description" :label="$t('commons.description')"/>
        <el-table-column :label="$t('commons.member')">
          <template v-slot:default="scope">
            <el-link type="primary" class="member-size" @click="cellClick(scope.row)">
              {{ scope.row.memberSize }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.operating')">
          <template v-slot:default="scope">
            <ms-table-operator @editClick="edit(scope.row)" @deleteClick="handleDelete(scope.row)"/>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination :change="list" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </el-card>

    <el-dialog :close-on-click-modal="false" :title="$t('workspace.create')" :visible.sync="dialogWsAddVisible" width="30%" @close="close">
      <el-form :model="form" :rules="rules" ref="form" label-position="right" label-width="100px" size="small">
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.description')" prop="description">
          <el-input type="textarea" v-model="form.description"></el-input>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="dialogWsAddVisible = false"
          @confirm="submit('form')"/>
      </template>
    </el-dialog>
    <el-dialog :close-on-click-modal="false" :title="$t('workspace.update')" :visible.sync="dialogWsUpdateVisible" width="30%">
      <el-form :model="form" :rules="rules" ref="form" label-position="right" label-width="100px" size="small">
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.description')" prop="description">
          <el-input type="textarea" v-model="form.description"></el-input>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="dialogWsUpdateVisible = false"
          @confirm="submit('form')"/>
      </template>
    </el-dialog>

    <!-- dialog of workspace member -->
    <el-dialog :close-on-click-modal="false" :visible.sync="dialogWsMemberVisible" width="70%" :destroy-on-close="true" @close="close"
               class="dialog-css">
      <ms-table-header :condition.sync="dialogCondition" @create="addMember" @search="dialogSearch"
                       :create-tip="$t('member.create')" :title="$t('commons.member')"/>
      <!-- organization member table -->
      <el-table :data="memberLineData" style="width: 100%;margin-top: 5px;">
        <el-table-column prop="name" :label="$t('commons.username')"/>
        <el-table-column prop="email" :label="$t('commons.email')"/>
        <el-table-column prop="phone" :label="$t('commons.phone')"/>
        <el-table-column :label="$t('commons.role')" width="120">
          <template v-slot:default="scope">
            <ms-roles-tag :roles="scope.row.roles" type="success"/>
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.operating')">
          <template v-slot:default="scope">
            <ms-table-operator :tip2="$t('commons.remove')" @editClick="editMember(scope.row)" @deleteClick="delMember(scope.row)"/>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination :change="dialogSearch" :current-page.sync="dialogCurrentPage"
                           :page-size.sync="dialogPageSize"
                           :total="dialogTotal"/>
    </el-dialog>

    <!-- add workspace member dialog -->
    <el-dialog :close-on-click-modal="false" :title="$t('member.create')" :visible.sync="dialogWsMemberAddVisible" width="30%"
               :destroy-on-close="true"
               @close="closeFunc">
      <el-form :model="memberForm" ref="form" :rules="wsMemberRule" label-position="right" label-width="100px"
               size="small">
        <el-form-item :label="$t('commons.member')" prop="memberSign" :rules="{required: true, message: $t('member.input_id_or_email'), trigger: 'change'}">
          <el-autocomplete
            class="input-with-autocomplete"
            v-model="memberForm.memberSign"
            :placeholder="$t('member.input_id_or_email')"
            :trigger-on-focus="false"
            :fetch-suggestions="querySearch"
            size="small"
            highlight-first-item
            value-key="email"
            @select="handleSelect"
          >
            <template v-slot:default="scope">
              <span class="ws-member-name">{{scope.item.id}}</span>
              <span class="ws-member-email">{{scope.item.email}}</span>
            </template>
          </el-autocomplete>
        </el-form-item>
        <el-form-item :label="$t('commons.role')" prop="roleIds">
          <el-select v-model="memberForm.roleIds" multiple :placeholder="$t('role.please_choose_role')"
                     class="select-width">
            <el-option
              v-for="item in memberForm.roles"
              :key="item.id"
              :label="$t('role.' + item.id)"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="dialogWsMemberAddVisible = false"
          @confirm="submitForm('form')"/>
      </template>
    </el-dialog>

    <!-- update workspace member dialog -->
    <el-dialog :close-on-click-modal="false" :title="$t('member.modify')" :visible.sync="dialogWsMemberUpdateVisible" width="30%"
               :destroy-on-close="true"
               @close="closeFunc">
      <el-form :model="memberForm" label-position="right" label-width="100px" size="small" ref="updateUserForm">
        <el-form-item label="ID" prop="id">
          <el-input v-model="memberForm.id" autocomplete="off" :disabled="true"/>
        </el-form-item>
        <el-form-item :label="$t('commons.username')" prop="name">
          <el-input v-model="memberForm.name" autocomplete="off" :disabled="true"/>
        </el-form-item>
        <el-form-item :label="$t('commons.email')" prop="email">
          <el-input v-model="memberForm.email" autocomplete="off" :disabled="true"/>
        </el-form-item>
        <el-form-item :label="$t('commons.phone')" prop="phone">
          <el-input v-model="memberForm.phone" autocomplete="off" :disabled="true"/>
        </el-form-item>
        <el-form-item :label="$t('commons.role')" prop="roleIds" :rules="{required: true, message: $t('role.please_choose_role'), trigger: 'change'}">
          <el-select v-model="memberForm.roleIds" multiple :placeholder="$t('role.please_choose_role')"
                     class="select-width">
            <el-option
              v-for="item in memberForm.allroles"
              :key="item.id"
              :label="$t('role.' + item.id)"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="dialogWsMemberUpdateVisible = false"
          @confirm="updateOrgMember('updateUserForm')"/>
      </template>
    </el-dialog>

    <ms-delete-confirm :title="$t('workspace.delete')" @delete="_handleDelete" ref="deleteConfirm"/>

  </div>
</template>

<script>
  import MsCreateBox from "../CreateBox";
  import {Message} from "element-ui";
  import {DEFAULT} from "../../../../common/js/constants";
  import MsTablePagination from "../../common/pagination/TablePagination";
  import MsTableHeader from "../../common/components/MsTableHeader";
  import MsRolesTag from "../../common/components/MsRolesTag";
  import MsTableOperator from "../../common/components/MsTableOperator";
  import MsTableOperatorButton from "../../common/components/MsTableOperatorButton";
  import MsDialogFooter from "../../common/components/MsDialogFooter";
  import {
    getCurrentOrganizationId,
    getCurrentUser,
    getCurrentWorkspaceId, listenGoBack,
    refreshSessionAndCookies, removeGoBackListener
  } from "../../../../common/js/utils";
  import MsDeleteConfirm from "../../common/components/MsDeleteConfirm";

  export default {
    name: "MsOrganizationWorkspace",
    components: {
      MsDeleteConfirm,
      MsCreateBox,
      MsTablePagination,
      MsTableHeader,
      MsRolesTag,
      MsTableOperator,
      MsDialogFooter,
      MsTableOperatorButton
    },
    activated() {
      this.list();
    },
    computed: {
      currentUser: () => {
        return getCurrentUser();
      }
    },
    methods: {
      create() {
        this.dialogWsAddVisible = true;
        this.form = {};
      },
      submit(formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            let saveType = 'add';
            if (this.form.id) {
              saveType = 'update'
            }
            this.$post("/workspace/" + saveType, this.form, () => {
              this.dialogWsAddVisible = false;
              this.dialogWsUpdateVisible = false;
              this.list();
              if (saveType == 'add') {
                Message.success(this.$t('commons.save_success'));
              } else if (saveType == 'update') {
                Message.success(this.$t('commons.modify_success'));
              }
            });
          } else {
            return false;
          }
        });
      },
      edit(row) {
        this.dialogWsUpdateVisible = true;
        this.form = Object.assign({}, row);
      },
      handleDelete(workspace) {
        this.$refs.deleteConfirm.open(workspace);
      },
      _handleDelete(workspace) {
        this.$confirm(this.$t('organization.delete_confirm'), '', {
          confirmButtonText: this.$t('commons.confirm'),
          cancelButtonText: this.$t('commons.cancel'),
          type: 'warning'
        }).then(() => {
          this.$get('/workspace/delete/' + workspace.id, () => {
            let lastWorkspaceId = getCurrentWorkspaceId();
            let sourceId = workspace.id;
            if (lastWorkspaceId === sourceId) {
              let sign = DEFAULT;
              refreshSessionAndCookies(sign, sourceId);
            }
            this.$success(this.$t('commons.delete_success'));
            this.list();
          });
        }).catch(() => {
          this.$message({
            type: 'info',
            message: this.$t('commons.delete_cancelled')
          });
        });

      },
      list() {
        let url = '/workspace/list/' + this.currentPage + '/' + this.pageSize;
        let lastOrganizationId = this.currentUser.lastOrganizationId;
        let userRole = this.currentUser.userRoles.filter(r => r.sourceId === lastOrganizationId);
        if (userRole.length > 0) {
          if (userRole[0].roleId === "org_admin") {
            this.result = this.$post(url, this.condition, response => {
              let data = response.data;
              this.items = data.listObject;
              for (let i = 0; i < this.items.length; i++) {
                let param = {
                  name: '',
                  workspaceId: this.items[i].id
                }
                let path = "user/ws/member/list/all";
                this.$post(path, param, res => {
                  let member = res.data;
                  this.$set(this.items[i], "memberSize", member.length);
                })
              }
              this.total = data.itemCount;
            });
          } else {
            this.items = [];
            this.total = 0;
          }
        }

      },
      addMember() {
        this.dialogWsMemberAddVisible = true;
        this.memberForm = {};
        this.result = this.$get('/user/list/', response => {
          this.userList = response.data;
        });
        this.result = this.$get('/role/list/test', response => {
          this.$set(this.memberForm, "roles", response.data);
        })
        listenGoBack(this.close);
      },
      cellClick(row) {
        // 保存当前点击的组织信息到currentRow
        this.currentWorkspaceRow = row;
        this.dialogWsMemberVisible = true;
        let param = {
          name: '',
          workspaceId: row.id
        };
        let path = "/user/ws/member/list";
        this.result = this.$post(this.buildPagePath(path), param, res => {
          let data = res.data;
          this.memberLineData = data.listObject;
          let url = "/userrole/list/ws/" + row.id;
          // 填充角色信息
          for (let i = 0; i < this.memberLineData.length; i++) {
            this.$get(url + "/" + encodeURIComponent(this.memberLineData[i].id), response => {
              let roles = response.data;
              this.$set(this.memberLineData[i], "roles", roles);
            })
          }
          this.dialogTotal = data.itemCount;
        });
      },
      dialogSearch() {
        let row = this.currentWorkspaceRow;
        this.dialogWsMemberVisible = true;
        let param = this.dialogCondition;
        this.$set(param, 'workspaceId', row.id);
        let path = "/user/ws/member/list";
        this.result = this.$post(this.buildPagePath(path), param, res => {
          let data = res.data;
          this.memberLineData = data.listObject;
          let url = "/userrole/list/ws/" + row.id;
          // 填充角色信息
          for (let i = 0; i < this.memberLineData.length; i++) {
            this.$get(url + "/" + encodeURIComponent(this.memberLineData[i].id), response => {
              let roles = response.data;
              this.$set(this.memberLineData[i], "roles", roles);
            })
          }
          this.dialogTotal = data.itemCount;
        });
      },
      closeFunc() {
        this.form = {};
      },
      submitForm(formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            let userIds = [];
            let userId = this.memberForm.userId;
            let email  = this.memberForm.memberSign;
            let member = this.userList.find(user => user.id === email || user.email === email);
            if (!member) {
              this.$warning(this.$t('member.no_such_user'));
              return false;
            } else {
              userId = member.id;
            }
            userIds.push(userId);
            let param = {
              userIds: userIds,
              roleIds: this.memberForm.roleIds,
              workspaceId: this.currentWorkspaceRow.id
            };
            this.result = this.$post("user/ws/member/add", param, () => {
              this.$success(this.$t('commons.save_success'));
              this.cellClick(this.currentWorkspaceRow);
              this.dialogWsMemberAddVisible = false;
            })
          } else {
            return false;
          }
        });
      },
      editMember(row) {
        this.dialogWsMemberUpdateVisible = true;
        this.memberForm = Object.assign({}, row);
        let roleIds = this.memberForm.roles.map(r => r.id);
        this.result = this.$get('/role/list/test', response => {
          this.$set(this.memberForm, "allroles", response.data);
        })
        // 编辑时填充角色信息
        this.$set(this.memberForm, 'roleIds', roleIds);
        listenGoBack(this.close);
      },
      delMember(row) {
        this.$confirm(this.$t('member.remove_member'), '', {
          confirmButtonText: this.$t('commons.confirm'),
          cancelButtonText: this.$t('commons.cancel'),
          type: 'warning'
        }).then(() => {
          this.result = this.$get('/user/ws/member/delete/' + this.currentWorkspaceRow.id + '/' + encodeURIComponent(row.id), () => {
            this.$success(this.$t('commons.remove_success'));
            this.cellClick(this.currentWorkspaceRow);
          });
        }).catch(() => {
          this.$info(this.$t('commons.remove_cancel'));
        });
      },
      updateOrgMember(formName) {
        let param = {
          id: this.memberForm.id,
          name: this.memberForm.name,
          email: this.memberForm.email,
          phone: this.memberForm.phone,
          roleIds: this.memberForm.roleIds,
          workspaceId: this.currentWorkspaceRow.id
        }
        this.$refs[formName].validate((valid) => {
          if (valid) {
            this.result = this.$post("/workspace/member/update", param, () => {
              this.$success(this.$t('commons.modify_success'));
              this.dialogWsMemberUpdateVisible = false;
              this.cellClick(this.currentWorkspaceRow);
            });
          }
        })
      },
      close: function () {
        removeGoBackListener(this.close);
        this.dialogWsMemberUpdateVisible = false;
        this.dialogWsMemberAddVisible = false;
        this.memberLineData = [];
        this.list();
      },
      buildPagePath(path) {
        return path + "/" + this.dialogCurrentPage + "/" + this.dialogPageSize;
      },
      querySearch(queryString, cb) {
        var userList = this.userList;
        var results = queryString ? userList.filter(this.createFilter(queryString)) : userList;
        // 调用 callback 返回建议列表的数据
        cb(results);
      },
      createFilter(queryString) {
        return (user) => {
          return (user.email.indexOf(queryString.toLowerCase()) === 0 || user.id.indexOf(queryString.toLowerCase()) === 0);
        };
      },
      handleSelect(item) {
        this.$set(this.form, "userId", item.id);
      }
    },
    data() {
      return {
        result: {},
        dialogWsAddVisible: false,
        dialogWsUpdateVisible: false,
        dialogWsMemberVisible: false,
        dialogWsMemberAddVisible: false,
        dialogWsMemberUpdateVisible: false,
        condition: {},
        dialogCondition: {},
        items: [],
        userList: [],
        currentPage: 1,
        pageSize: 10,
        total: 0,
        dialogCurrentPage: 1,
        dialogPageSize: 10,
        dialogTotal: 0,
        memberLineData: [],
        memberForm: {},
        form: {
          // name: "",
          // description: ""
        },
        rules: {
          name: [
            {required: true, message: this.$t('workspace.input_name'), trigger: 'blur'},
            {min: 2, max: 20, message: this.$t('commons.input_limit', [2, 20]), trigger: 'blur'},
            {
              required: true,
              pattern: /^(?!-)(?!.*?-$)[a-zA-Z0-9\u4e00-\u9fa5-]+$/,
              message: this.$t('workspace.special_characters_are_not_supported'),
              trigger: 'blur'
            }
          ],
          description: [
            {max: 50, message: this.$t('commons.input_limit', [0, 50]), trigger: 'blur'}
          ],
        },
        wsMemberRule: {
          userIds: [
            {required: true, message: this.$t('member.please_choose_member'), trigger: ['blur']}
          ],
          roleIds: [
            {required: true, message: this.$t('role.please_choose_role'), trigger: ['blur']}
          ]
        },
        currentWorkspaceRow: {},
      }
    }
  }
</script>

<style scoped>

  .el-table__row:hover .edit {
    opacity: 1;
  }

  .member-size {
    text-decoration: underline;
  }

  .select-width {
    width: 100%;
  }

  .ws-member-name {
    float: left;
  }

  .ws-member-email {
    float: right;
    color: #8492a6;
    font-size: 13px;
  }

  .dialog-css >>> .el-dialog__header {
    padding: 0px;
  }

  .input-with-autocomplete {
    width: 100%;
  }

</style>

