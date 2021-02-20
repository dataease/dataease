<template>
  <div v-loading="result.loading">

    <el-card class="table-card">
      <template v-slot:header>
        <ms-table-header :condition.sync="condition" @search="initTableData" @create="create"
                         :create-tip="$t('organization.create')" :title="$t('commons.organization')"/>
      </template>
      <!-- system menu organization table-->
      <el-table border class="adjust-table" :data="tableData" style="width: 100%">
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
      <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </el-card>

    <!-- dialog of organization member -->
    <el-dialog :close-on-click-modal="false" :visible.sync="dialogOrgMemberVisible" width="70%" :destroy-on-close="true" @close="closeFunc"
               class="dialog-css">
      <ms-table-header :condition.sync="dialogCondition" @create="addMember" @search="dialogSearch"
                       :create-tip="$t('member.create')" :title="$t('commons.member')"/>
      <!-- organization member table -->
      <el-table :border="true" class="adjust-table" :data="memberLineData" style="width: 100%;margin-top:5px;">
        <el-table-column prop="id" label="ID"/>
        <el-table-column prop="name" :label="$t('commons.username')"/>
        <el-table-column prop="email" :label="$t('commons.email')"/>
        <el-table-column prop="phone" :label="$t('commons.phone')"/>
        <el-table-column :label="$t('commons.role')" width="140">
          <template v-slot:default="scope">
            <ms-roles-tag :roles="scope.row.roles"/>
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

    <!-- add organization form -->
    <el-dialog :close-on-click-modal="false" :title="$t('organization.create')" :visible.sync="dialogOrgAddVisible" width="30%" @closed="closeFunc"
               :destroy-on-close="true">
      <el-form :model="form" label-position="right" label-width="100px" size="small" :rules="rule"
               ref="createOrganization">
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.description')" prop="description">
          <el-input v-model="form.description" autocomplete="off" type="textarea"/>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="dialogOrgAddVisible = false"
          @confirm="createOrganization('createOrganization')"/>
      </template>
    </el-dialog>

    <!-- update organization form -->
    <el-dialog :close-on-click-modal="false" :title="$t('organization.modify')" :visible.sync="dialogOrgUpdateVisible" width="30%"
               :destroy-on-close="true"
               @close="closeFunc">
      <el-form :model="form" label-position="right" label-width="100px" size="small" :rules="rule"
               ref="updateOrganizationForm">
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.description')" prop="description">
          <el-input v-model="form.description" autocomplete="off"/>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="dialogOrgUpdateVisible = false"
          @confirm="updateOrganization('updateOrganizationForm')"/>
      </template>
    </el-dialog>

    <!-- add organization member form -->
    <el-dialog :close-on-click-modal="false" :title="$t('member.create')" :visible.sync="dialogOrgMemberAddVisible" width="30%"
               :destroy-on-close="true"
               @close="closeFunc">
      <el-form :model="memberForm" ref="form" :rules="orgMemberRule" label-position="right" label-width="100px"
               size="small">
        <el-form-item :label="$t('commons.member')" prop="userIds">
          <el-select filterable v-model="memberForm.userIds" multiple :placeholder="$t('member.please_choose_member')"
                     class="select-width" :filter-method="dataFilter">
            <el-option
              v-for="item in memberForm.userList"
              :key="item.id"
              :label="item.name"
              :value="item.id">
              <span class="org-member-id">{{ item.id }}</span>
              <span class="org-member-email">{{ item.email }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('commons.role')" prop="roleIds">
          <el-select filterable v-model="memberForm.roleIds" multiple :placeholder="$t('role.please_choose_role')"
                     class="select-width">
            <el-option
              v-for="item in memberForm.roles"
              :key="item.id"
              :label="item.name"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="dialogOrgMemberAddVisible = false"
          @confirm="submitForm('form')"/>
      </template>
    </el-dialog>

    <!-- update organization member form -->
    <el-dialog :close-on-click-modal="false" :title="$t('member.modify')" :visible.sync="dialogOrgMemberUpdateVisible" width="30%"
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
        <el-form-item :label="$t('commons.role')" prop="roleIds"
                      :rules="{required: true, message: $t('role.please_choose_role'), trigger: 'change'}">
          <el-select filterable v-model="memberForm.roleIds" multiple :placeholder="$t('role.please_choose_role')"
                     class="select-width">
            <el-option
              v-for="item in memberForm.allroles"
              :key="item.id"
              :label="item.name"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="dialogOrgMemberUpdateVisible = false"
          @confirm="updateOrgMember('updateUserForm')"/>
      </template>
    </el-dialog>

    <ms-delete-confirm :title="$t('organization.delete')" @delete="_handleDelete" ref="deleteConfirm"/>

  </div>
</template>

<script>
import MsCreateBox from "../CreateBox";
import MsTablePagination from "../../common/pagination/TablePagination";
import MsTableHeader from "../../common/components/MsTableHeader";
import MsRolesTag from "../../common/components/MsRolesTag";
import MsTableOperator from "../../common/components/MsTableOperator";
import MsTableOperatorButton from "../../common/components/MsTableOperatorButton";
import MsDialogFooter from "../../common/components/MsDialogFooter";
import {
  getCurrentOrganizationId,
  getCurrentUser,
  listenGoBack,
  refreshSessionAndCookies,
  removeGoBackListener
} from "@/common/js/utils";
import {DEFAULT, ORGANIZATION} from "@/common/js/constants";
import MsDeleteConfirm from "../../common/components/MsDeleteConfirm";

export default {
  name: "MsOrganization",
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
  data() {
    return {
      queryPath: '/organization/list',
      deletePath: '/organization/delete/',
      createPath: '/organization/add',
      updatePath: '/organization/update',
      result: {},
      dialogOrgAddVisible: false,
      dialogOrgUpdateVisible: false,
      dialogOrgMemberVisible: false,
      dialogOrgMemberAddVisible: false,
      dialogOrgMemberUpdateVisible: false,
      multipleSelection: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      dialogCurrentPage: 1,
      dialogPageSize: 10,
      dialogTotal: 0,
      currentRow: {},
      condition: {},
      dialogCondition: {},
      tableData: [],
      memberLineData: [],
      form: {},
      memberForm: {},
      rule: {
        name: [
          {required: true, message: this.$t('organization.input_name'), trigger: 'blur'},
          {min: 2, max: 25, message: this.$t('commons.input_limit', [2, 25]), trigger: 'blur'}
        ],
        description: [
          {max: 50, message: this.$t('commons.input_limit', [0, 50]), trigger: 'blur'}
        ]
      },
      orgMemberRule: {
        userIds: [
          {required: true, message: this.$t('member.please_choose_member'), trigger: ['blur']}
        ],
        roleIds: [
          {required: true, message: this.$t('role.please_choose_role'), trigger: ['blur']}
        ]
      }
    }
  },
  activated() {
    this.initTableData();
  },
  methods: {
    create() {
      this.dialogOrgAddVisible = true;
      listenGoBack(this.closeFunc);
    },
    addMember() {
      this.dialogOrgMemberAddVisible = true;
      this.memberForm = {};
      this.result = this.$get('/user/list/', response => {
        this.$set(this.memberForm, "userList", response.data);
        this.$set(this.memberForm, "copyUserList", response.data);
      });
      this.result = this.$get('/role/list/org', response => {
        this.$set(this.memberForm, "roles", response.data);
      })
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
    edit(row) {
      this.dialogOrgUpdateVisible = true;
      this.form = Object.assign({}, row);
      listenGoBack(this.closeFunc);
    },
    editMember(row) {
      this.dialogOrgMemberUpdateVisible = true;
      this.memberForm = Object.assign({}, row);
      let roleIds = this.memberForm.roles.map(r => r.id);
      this.result = this.$get('/role/list/org', response => {
        this.$set(this.memberForm, "allroles", response.data);
      })
      // 编辑时填充角色信息
      this.$set(this.memberForm, 'roleIds', roleIds);
      listenGoBack(this.closeFunc);
    },
    cellClick(row) {
      // 保存当前点击的组织信息到currentRow
      this.currentRow = row;
      this.dialogOrgMemberVisible = true;
      let param = {
        name: '',
        organizationId: row.id
      };
      let path = "/user/special/org/member/list";
      this.result = this.$post(path + "/" + this.dialogCurrentPage + "/" + this.dialogPageSize, param, res => {
        let data = res.data;
        this.memberLineData = data.listObject;
        let url = "/userrole/list/org/" + row.id;
        for (let i = 0; i < this.memberLineData.length; i++) {
          this.$get(url + "/" + encodeURIComponent(this.memberLineData[i].id), response => {
            let roles = response.data;
            this.$set(this.memberLineData[i], "roles", roles);
          })
        }
        this.dialogTotal = data.itemCount;
      });
      listenGoBack(this.closeFunc);
    },
    dialogSearch() {
      let row = this.currentRow;
      this.dialogOrgMemberVisible = true;
      let param = this.dialogCondition;
      this.$set(param, 'organizationId', row.id);
      let path = "/user/special/org/member/list";
      this.result = this.$post(path + "/" + this.dialogCurrentPage + "/" + this.dialogPageSize, param, res => {
        let data = res.data;
        this.memberLineData = data.listObject;
        let url = "/userrole/list/org/" + row.id;
        for (let i = 0; i < this.memberLineData.length; i++) {
          this.$get(url + "/" + encodeURIComponent(this.memberLineData[i].id), response => {
            let roles = response.data;
            this.$set(this.memberLineData[i], "roles", roles);
          })
        }
        this.dialogTotal = data.itemCount;
      });
    },
    handleDelete(organization) {
      this.$refs.deleteConfirm.open(organization);
    },
    _handleDelete(organization) {
      this.$confirm(this.$t('organization.delete_confirm'), '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        this.result = this.$get(this.deletePath + organization.id, () => {
          let lastOrganizationId = getCurrentOrganizationId();
          let sourceId = organization.id;
          if (lastOrganizationId === sourceId) {
            refreshSessionAndCookies(DEFAULT, sourceId);
          }
          this.$success(this.$t('commons.delete_success'));
          this.initTableData();
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
        this.result = this.$get('/user/special/org/member/delete/' + this.currentRow.id + '/' + encodeURIComponent(row.id), () => {
          let sourceId = this.currentRow.id;
          let currentUser = getCurrentUser();
          let userId = row.id;
          if (currentUser.id === userId) {
            refreshSessionAndCookies(ORGANIZATION, sourceId);
          }
          this.$success(this.$t('commons.remove_success'))
          this.cellClick(this.currentRow);
        });
      }).catch(() => {
        this.$info(this.$t('commons.remove_cancel'));
      });
    },
    createOrganization(createOrganizationForm) {
      this.$refs[createOrganizationForm].validate(valid => {
        if (valid) {
          this.result = this.$post(this.createPath, this.form, () => {
            this.$success(this.$t('commons.save_success'));
            this.initTableData();
            this.dialogOrgAddVisible = false;
          });
        } else {
          return false;
        }
      })
    },
    updateOrganization(updateOrganizationForm) {
      this.$refs[updateOrganizationForm].validate(valid => {
        if (valid) {
          this.result = this.$post(this.updatePath, this.form, () => {
            this.$success(this.$t('commons.modify_success'));
            this.dialogOrgUpdateVisible = false;
            this.initTableData();
          });
        } else {
          return false;
        }
      })
    },
    initTableData() {
      this.result = this.$post(this.queryPath + "/" + this.currentPage + "/" + this.pageSize, this.condition, response => {
        let data = response.data;
        this.tableData = data.listObject;
        for (let i = 0; i < this.tableData.length; i++) {
          let param = {
            name: '',
            organizationId: this.tableData[i].id
          }
          let path = "user/special/org/member/list/all";
          this.$post(path, param, res => {
            let member = res.data;
            this.$set(this.tableData[i], "memberSize", member.length);
          })
        }
        this.total = data.itemCount;
      })
    },
    closeFunc() {
      this.memberLineData = [];
      this.initTableData();
      this.form = {};
      removeGoBackListener(this.closeFunc);
      this.dialogOrgAddVisible = false;
      this.dialogOrgUpdateVisible = false;
      this.dialogOrgMemberVisible = false;
      this.dialogOrgMemberAddVisible = false;
      this.dialogOrgMemberUpdateVisible = false;
    },
    handleSelectionChange(val) {
      this.multipleSelection = val;
    },
    submitForm(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          let param = {
            userIds: this.memberForm.userIds,
            roleIds: this.memberForm.roleIds,
            organizationId: this.currentRow.id
          };
          this.result = this.$post("user/special/org/member/add", param, () => {
            let sign = "other";
            let sourceId = this.currentRow.id;
            refreshSessionAndCookies(sign, sourceId);
            this.cellClick(this.currentRow);
            this.dialogOrgMemberAddVisible = false;
          })
        } else {
          return false;
        }
      });
    },
    updateOrgMember(formName) {
      let param = {
        id: this.memberForm.id,
        name: this.memberForm.name,
        email: this.memberForm.email,
        phone: this.memberForm.phone,
        roleIds: this.memberForm.roleIds,
        organizationId: this.currentRow.id
      }
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.result = this.$post("/organization/member/update", param, () => {
            this.$success(this.$t('commons.modify_success'));
            this.dialogOrgMemberUpdateVisible = false;
            this.cellClick(this.currentRow);
          });
        }
      });
    },
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

</style>
