<template>
  <div v-loading="result.loading">
    <el-card class="table-card">
      <template v-slot:header>
        <ms-table-header :condition.sync="condition" @search="initTableData" @create="create"
                         :create-tip="$t('member.create')" :title="$t('commons.member')"/>
      </template>
      <el-table  border class="adjust-table" :data="tableData" style="width: 100%">
        <el-table-column prop="id" label="ID"/>
        <el-table-column prop="name" :label="$t('commons.username')"/>
        <el-table-column prop="email" :label="$t('commons.email')"/>
        <el-table-column prop="phone" :label="$t('commons.phone')"/>
        <el-table-column prop="roles" :label="$t('commons.role')" width="120">
          <template v-slot:default="scope">
            <ms-roles-tag :roles="scope.row.roles" type="success"/>
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.operating')">
          <template v-slot:default="scope">
            <ms-table-operator :tip2="$t('commons.remove')" @editClick="edit(scope.row)" @deleteClick="del(scope.row)"
                               v-permission="['test_manager']"/>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </el-card>

    <el-dialog :close-on-click-modal="false" :title="$t('member.create')" :visible.sync="createVisible" width="30%" :destroy-on-close="true"
               @close="handleClose">
      <el-form :model="form" ref="form" :rules="rules" label-position="right" label-width="100px" size="small">
        <el-form-item :label="$t('commons.member')" prop="memberSign" :rules="{required: true, message: $t('member.input_id_or_email'), trigger: 'change'}">
          <el-autocomplete
            class="input-with-autocomplete"
            v-model="form.memberSign"
            :placeholder="$t('member.input_id_or_email')"
            :trigger-on-focus="false"
            :fetch-suggestions="querySearch"
            size="small"
            highlight-first-item
            value-key="email"
            @select="handleSelect"
          >
            <template v-slot:default="scope">
              <span class="workspace-member-name">{{scope.item.id}}</span>
              <span class="workspace-member-email">{{scope.item.email}}</span>
            </template>
          </el-autocomplete>
        </el-form-item>
        <el-form-item :label="$t('commons.role')" prop="roleIds">
          <el-select v-model="form.roleIds" multiple :placeholder="$t('role.please_choose_role')" class="select-width">
            <el-option
              v-for="item in form.roles"
              :key="item.id"
              :label="$t('role.' + item.id)"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="createVisible = false"
          @confirm="submitForm('form')"/>
      </template>
    </el-dialog>

    <el-dialog :close-on-click-modal="false" :title="$t('member.modify')" :visible.sync="updateVisible" width="30%" :destroy-on-close="true"
               @close="handleClose">
      <el-form :model="form" label-position="right" label-width="100px" size="small" ref="updateUserForm">
        <el-form-item label="ID" prop="id">
          <el-input v-model="form.id" autocomplete="off" :disabled="true"/>
        </el-form-item>
        <el-form-item :label="$t('commons.username')" prop="name">
          <el-input v-model="form.name" autocomplete="off" :disabled="true"/>
        </el-form-item>
        <el-form-item :label="$t('commons.email')" prop="email">
          <el-input v-model="form.email" autocomplete="off" :disabled="true"/>
        </el-form-item>
        <el-form-item :label="$t('commons.phone')" prop="phone">
          <el-input v-model="form.phone" autocomplete="off" :disabled="true"/>
        </el-form-item>
        <el-form-item :label="$t('commons.role')" prop="roleIds" :rules="{required: true, message: $t('role.please_choose_role'), trigger: 'change'}">
          <el-select v-model="form.roleIds" multiple :placeholder="$t('role.please_choose_role')" class="select-width">
            <el-option
              v-for="item in form.allroles"
              :key="item.id"
              :label="$t('role.' + item.id)"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="updateVisible = false"
          @confirm="updateWorkspaceMember('updateUserForm')"/>
      </template>
    </el-dialog>

  </div>
</template>

<script>
  import MsCreateBox from "../CreateBox";
  import MsTablePagination from "../../common/pagination/TablePagination";
  import MsTableHeader from "../../common/components/MsTableHeader";
  import MsRolesTag from "../../common/components/MsRolesTag";
  import MsTableOperator from "../../common/components/MsTableOperator";
  import MsDialogFooter from "../../common/components/MsDialogFooter";
  import {getCurrentUser, listenGoBack, removeGoBackListener} from "../../../../common/js/utils";

  export default {
    name: "MsMember",
    components: {MsCreateBox, MsTablePagination, MsTableHeader, MsRolesTag, MsTableOperator, MsDialogFooter},
    data() {
      return {
        result: {},
        form: {},
        createVisible: false,
        updateVisible: false,
        queryPath: "/user/ws/member/list",
        condition: {},
        tableData: [],
        userList: [],
        rules: {
          userIds: [
            {required: true, message: this.$t('member.please_choose_member'), trigger: ['blur']}
          ],
          roleIds: [
            {required: true, message: this.$t('role.please_choose_role'), trigger: ['blur']}
          ]
        },
        multipleSelection: [],
        currentPage: 1,
        pageSize: 10,
        total: 0,
      }
    },
    activated: function () {
      this.initTableData();
    },
    methods: {
      currentUser: () => {
        return getCurrentUser();
      },
      initTableData() {
        if (this.currentUser().lastWorkspaceId === null) {
          return false;
        }
        this.loading = true;
        let param = {
          name: this.condition.name,
          workspaceId: this.currentUser().lastWorkspaceId
        };

        this.result = this.$post(this.buildPagePath(this.queryPath), param, response => {
          let data = response.data;
          this.tableData = data.listObject;
          let url = "/userrole/list/ws/" + this.currentUser().lastWorkspaceId;
          for (let i = 0; i < this.tableData.length; i++) {
            this.$get(url + "/" + encodeURIComponent(this.tableData[i].id), response => {
              let roles = response.data;
              this.$set(this.tableData[i], "roles", roles);
            })
          }
          this.total = data.itemCount;
        })

      },
      buildPagePath(path) {
        return path + "/" + this.currentPage + "/" + this.pageSize;
      },
      handleClose() {
        this.form = {};
        this.createVisible = false;
        this.updateVisible = false;
        removeGoBackListener(this.handleClose);
      },
      del(row) {
        this.$confirm(this.$t('member.remove_member'), '', {
          confirmButtonText: this.$t('commons.confirm'),
          cancelButtonText: this.$t('commons.cancel'),
          type: 'warning'
        }).then(() => {
          this.result = this.$get('/user/ws/member/delete/' + this.currentUser().lastWorkspaceId + '/' + encodeURIComponent(row.id),() => {
            this.$success(this.$t('commons.remove_success'));
            this.initTableData();
          });
        }).catch(() => {
          this.$info(this.$t('commons.remove_cancel'));
        });
      },
      edit(row) {
        this.updateVisible = true;
        this.form = Object.assign({}, row);
        let roleIds = this.form.roles.map(r => r.id);
        this.result = this.$get('/role/list/test', response => {
          this.$set(this.form, "allroles", response.data);
        })
        // 编辑使填充角色信息
        this.$set(this.form, 'roleIds', roleIds);
        listenGoBack(this.handleClose);
      },
      updateWorkspaceMember(formName) {
        let param = {
          id: this.form.id,
          name: this.form.name,
          email: this.form.email,
          phone: this.form.phone,
          roleIds: this.form.roleIds,
          workspaceId: this.currentUser().lastWorkspaceId
        }
        this.$refs[formName].validate((valid) => {
          if (valid) {
            this.result = this.$post("/workspace/member/update", param, () => {
              this.$success(this.$t('commons.modify_success'));
              this.updateVisible = false;
              this.initTableData();
            });
          }
        });
      },
      create() {
        this.form = {};
        // let param = {
        //   name: this.condition.name,
        //   organizationId: this.currentUser().lastOrganizationId
        // };
        let wsId = this.currentUser().lastWorkspaceId;
        if (typeof wsId == "undefined" || wsId == null || wsId == "") {
          this.$warning(this.$t('workspace.please_select_a_workspace_first'));
          return false;
        }
        // this.$post('/user/org/member/list/all', param, response => {
        //   this.createVisible = true;
        //   this.userList = response.data;
        // })
        this.$get('/user/list/', response => {
          this.createVisible = true;
          this.userList = response.data;
        })
        this.result = this.$get('/role/list/test', response => {
          this.$set(this.form, "roles", response.data);
        })
        listenGoBack(this.handleClose);
      },
      submitForm(formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            let userIds = [];
            let userId = this.form.userId;
            let email  = this.form.memberSign;
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
              roleIds: this.form.roleIds,
              workspaceId: this.currentUser().lastWorkspaceId
            };
            this.result = this.$post("user/ws/member/add", param, () => {
              this.$success(this.$t('commons.save_success'));
              this.initTableData();
              this.createVisible = false;
            })
          }
        });
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
    }
  }
</script>

<style scoped>

  .el-table__row:hover .edit {
    opacity: 1;
  }

  .select-width {
    width: 100%;
  }

  .workspace-member-name {
    float: left;
  }

  .workspace-member-email {
    float: right;
    color: #8492a6;
    font-size: 13px;
  }

  .input-with-autocomplete {
    width: 100%;
  }

</style>
