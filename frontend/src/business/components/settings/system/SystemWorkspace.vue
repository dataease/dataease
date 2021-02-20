<template>
  <div>
    <el-card class="table-card" v-loading="result.loading">
      <template v-slot:header>
        <ms-table-header :condition.sync="condition" @search="list" @create="create"
                         :create-tip="$t('workspace.create')" :title="$t('commons.workspace')"/>
      </template>
      <!-- workspace table -->
      <el-table border class="adjust-table" :data="items" style="width: 100%">
        <el-table-column prop="name" :label="$t('commons.name')"/>
        <el-table-column prop="description" :label="$t('commons.description')"/>
        <el-table-column prop="organizationName" :label="$t('workspace.organization_name')"/>
        <el-table-column :label="$t('commons.member')">
          <template v-slot:default="scope">
            <el-link type="primary" class="member-size" @click="cellClick(scope.row)">
              {{scope.row.memberSize}}
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

    <!-- add workspace dialog -->
    <el-dialog :close-on-click-modal="false" :title="$t('workspace.create')" :visible.sync="dialogWsAddVisible"
               width="30%" @close="close">
      <el-form :model="form" :rules="rules" ref="form" label-position="right" label-width="100px" size="small">
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.description')" prop="description">
          <el-input type="textarea" v-model="form.description"></el-input>
        </el-form-item>
        <el-form-item :label="$t('workspace.organization_name')" prop="organizationId">
          <el-select filterable v-model="form.organizationId" :placeholder="$t('organization.select_organization')"
                     class="select-width">
            <el-option
              v-for="item in form.orgList"
              :key="item.id"
              :label="item.name"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="dialogWsAddVisible = false"
          @confirm="submit('form')"/>
      </template>
    </el-dialog>

    <!-- update workspace dialog -->
    <el-dialog :close-on-click-modal="false" :title="$t('workspace.update')" :visible.sync="dialogWsUpdateVisible"
               width="30%" @close="close">
      <el-form :model="form" :rules="rules" ref="updateForm" label-position="right" label-width="100px" size="small">
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.description')" prop="description">
          <el-input type="textarea" v-model="form.description"></el-input>
        </el-form-item>
        <el-form-item :label="$t('workspace.organization_name')" prop="organizationId">
          <el-select filterable v-model="form.organizationId" :placeholder="$t('organization.select_organization')"
                     class="select-width">
            <el-option
              v-for="item in form.orgList1"
              :key="item.id"
              :label="item.name"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="dialogWsUpdateVisible = false"
          @confirm="updateWorkspace('updateForm')"/>
      </template>

    </el-dialog>

    <!-- dialog of workspace member -->
    <el-dialog :close-on-click-modal="false" :visible.sync="dialogWsMemberVisible" width="70%" :destroy-on-close="true"
               @close="closeWsMemberDialog" class="dialog-css">
      <ms-table-header :condition.sync="dialogCondition" @create="addMember" @search="dialogSearch"
                       :create-tip="$t('member.create')" :title="$t('commons.member')"/>
      <!-- organization member table -->
      <el-table :data="memberLineData" style="width: 100%;margin-top: 5px;">
        <el-table-column prop="id" label="ID"/>
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
            <ms-table-operator :tip2="$t('commons.remove')" @editClick="editMember(scope.row)"
                               @deleteClick="delMember(scope.row)"/>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination :change="dialogSearch" :current-page.sync="dialogCurrentPage"
                           :page-size.sync="dialogPageSize"
                           :total="dialogTotal"/>
    </el-dialog>

    <!-- add workspace member dialog -->
    <el-dialog :close-on-click-modal="false" :title="$t('member.create')" :visible.sync="dialogWsMemberAddVisible"
               width="30%"
               :destroy-on-close="true"
               @close="handleClose">
      <el-form :model="memberForm" ref="form" :rules="wsMemberRule" label-position="right" label-width="100px"
               size="small">
        <el-form-item :label="$t('commons.member')" prop="userIds">
          <el-select filterable v-model="memberForm.userIds" multiple :placeholder="$t('member.please_choose_member')"
                     class="select-width" :filter-method="dataFilter">
            <el-option
              v-for="item in memberForm.userList"
              :key="item.id"
              :label="item.name"
              :value="item.id">
              <span class="ws-member-id">{{ item.id }}</span>
              <span class="ws-member-email">{{ item.email }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('commons.role')" prop="roleIds">
          <el-select filterable v-model="memberForm.roleIds" multiple :placeholder="$t('role.please_choose_role')"
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
    <el-dialog :close-on-click-modal="false" :title="$t('member.modify')" :visible.sync="dialogWsMemberUpdateVisible"
               width="30%"
               :destroy-on-close="true"
               @close="handleClose">
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
        <el-form-item :label="$t('commons.role')" prop="roleIds"
                      :rules="{required: true, message: $t('role.please_choose_role'), trigger: 'change'}">
          <el-select filterable v-model="memberForm.roleIds" multiple :placeholder="$t('role.please_choose_role')"
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
          @confirm="updateWorkspaceMember('updateUserForm')"/>
      </template>

    </el-dialog>

    <ms-delete-confirm :title="$t('workspace.delete')" @delete="_handleDelete" ref="deleteConfirm"/>

  </div>
</template>

<script>
  import MsCreateBox from "../CreateBox";
  import {Message} from "element-ui";
  import MsTablePagination from "../../common/pagination/TablePagination";
  import MsTableHeader from "../../common/components/MsTableHeader";
  import MsRolesTag from "../../common/components/MsRolesTag";
  import MsTableOperator from "../../common/components/MsTableOperator";
  import MsTableOperatorButton from "../../common/components/MsTableOperatorButton";
  import MsDialogFooter from "../../common/components/MsDialogFooter";
  import {
    getCurrentUser,
    getCurrentWorkspaceId, listenGoBack,
    refreshSessionAndCookies, removeGoBackListener
  } from "@/common/js/utils";
  import {DEFAULT, WORKSPACE} from "@/common/js/constants";
  import MsDeleteConfirm from "../../common/components/MsDeleteConfirm";

  export default {
    name: "MsSystemWorkspace",
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
    methods: {
      create() {
        this.dialogWsAddVisible = true;
        this.form = {};
        this.$get("/organization/list", response => {
          this.$set(this.form, "orgList", response.data);
        })
        listenGoBack(this.close);
      },
      dataFilter(val) {
        if (val) {
          this.memberForm.userList = this.memberForm.copyUserList.filter((item) => {
            if (!!~item.id.indexOf(val) || !!~item.id.toUpperCase().indexOf(val.toUpperCase())) {
              return true
            }
          })
        } else {
          this.memberForm.userList = this.memberForm.copyUserList;
        }
      },
      submit(formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            let saveType = 'special/add';
            if (this.form.id) {
              saveType = 'update'
            }
            this.result = this.$post("/workspace/" + saveType, this.form, () => {
              this.dialogWsAddVisible = false;
              this.list();
              Message.success(this.$t('commons.save_success'));
            });
          } else {
            return false;
          }
        });
      },
      addMember() {
        this.dialogWsMemberAddVisible = true;
        this.result = this.$get('/user/list/', response => {
          this.$set(this.memberForm, "userList", response.data);
          this.$set(this.memberForm, "copyUserList", response.data);
        });
        this.result = this.$get('/role/list/test', response => {
          this.$set(this.memberForm, "roles", response.data);
        });

        listenGoBack(this.handleClose);
      },
      cellClick(row) {
        // 保存当前点击的组织信息到currentRow
        this.currentWorkspaceRow = row;
        this.dialogWsMemberVisible = true;
        let param = {
          name: '',
          workspaceId: row.id
        };
        let path = "/user/special/ws/member/list";
        this.result = this.$post(path + "/" + this.dialogCurrentPage + "/" + this.dialogPageSize, param, res => {
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
        listenGoBack(this.closeWsMemberDialog);
      },
      dialogSearch() {
        let row = this.currentWorkspaceRow;
        this.dialogWsMemberVisible = true;
        let param = this.dialogCondition;
        this.$set(param, 'workspaceId', row.id);
        let path = "/user/special/ws/member/list";
        this.result = this.$post(path + "/" + this.dialogCurrentPage + "/" + this.dialogPageSize, param, res => {
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
      edit(row) {
        this.dialogWsUpdateVisible = true;
        // copy user
        this.form = Object.assign({}, row);
        this.$get("/organization/list", response => {
          this.$set(this.form, "orgList1", response.data);
        })
        listenGoBack(this.close);
      },
      close() {
        this.dialogWsAddVisible = false;
        this.dialogWsUpdateVisible = false;
        removeGoBackListener(this.close);
      },
      updateWorkspace(updateForm) {
        this.$refs[updateForm].validate(valid => {
          if (valid) {
            this.result = this.$post("/workspace/special/update", this.form, () => {
              this.$success(this.$t('commons.modify_success'));
              this.dialogWsUpdateVisible = false;
              this.list();
            });
          } else {
            return false;
          }
        })
      },
      handleClose() {
        this.memberForm = {};
        this.dialogWsMemberAddVisible = false;
        this.dialogWsMemberUpdateVisible = false;
        removeGoBackListener(this.handleClose);
      },
      closeWsMemberDialog() {
        this.memberLineData = [];
        this.list();
        removeGoBackListener(this.closeWsMemberDialog);
        this.dialogWsMemberVisible = false;
      },
      list() {
        let url = '/workspace/list/all/' + this.currentPage + '/' + this.pageSize;
        this.result = this.$post(url, this.condition, response => {
          let data = response.data;
          this.items = data.listObject;
          for (let i = 0; i < this.items.length; i++) {
            let param = {
              name: '',
              workspaceId: this.items[i].id
            }
            let path = "user/special/ws/member/list/all";
            this.$post(path, param, res => {
              let member = res.data;
              this.$set(this.items[i], "memberSize", member.length);
            })
          }
          this.total = data.itemCount;
        });
      },
      submitForm(formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            let param = {
              userIds: this.memberForm.userIds,
              roleIds: this.memberForm.roleIds,
              workspaceId: this.currentWorkspaceRow.id
            };
            this.result = this.$post("user/special/ws/member/add", param, () => {
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
        listenGoBack(this.handleClose);
      },
      handleDelete(workspace) {
        this.$refs.deleteConfirm.open(workspace);
      },
      _handleDelete(workspace) {
        this.$confirm(this.$t('workspace.delete_confirm'), '', {
          confirmButtonText: this.$t('commons.confirm'),
          cancelButtonText: this.$t('commons.cancel'),
          type: 'warning'
        }).then(() => {
          this.$get('/workspace/special/delete/' + workspace.id, () => {
            let lastWorkspaceId = getCurrentWorkspaceId();
            let sourceId = workspace.id;
            if (lastWorkspaceId === sourceId) {
              let sign = DEFAULT;
              refreshSessionAndCookies(sign, sourceId);
            }
            Message.success(this.$t('commons.delete_success'));
            this.list();
          });
        }).catch(() => {
          this.$message({
            type: 'info',
            message: this.$t('commons.delete_cancelled')
          });
        });
      },
      delMember(row) {
        this.$confirm(this.$t('member.remove_member'), '', {
          confirmButtonText: this.$t('commons.confirm'),
          cancelButtonText: this.$t('commons.cancel'),
          type: 'warning'
        }).then(() => {
          this.result = this.$get('/user/special/ws/member/delete/' + this.currentWorkspaceRow.id + '/' + encodeURIComponent(row.id), () => {
            let sourceId = this.currentWorkspaceRow.id;
            let userId = row.id;
            let user = getCurrentUser();
            if (user.id === userId) {
              let sign = WORKSPACE;
              refreshSessionAndCookies(sign, sourceId);
            }
            this.$success(this.$t('commons.remove_success'));
            this.cellClick(this.currentWorkspaceRow);
          });
        }).catch(() => {
          this.$info(this.$t('commons.remove_cancel'));
        });
      },
      updateWorkspaceMember(formName) {
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
        });
      },
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
            {min: 2, max: 25, message: this.$t('commons.input_limit', [2, 25]), trigger: 'blur'},
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
          organizationId: [
            {required: true, message: this.$t('organization.select_organization'), trigger: ['blur']}
          ]
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
    cursor: pointer;
  }

  .ws-member-id {
    float: left;
  }

  .ws-member-email {
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

</style>

