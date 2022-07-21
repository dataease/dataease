<template>
  <de-layout-content>
    <el-row class="top-operate">
      <el-col :span="12">
        <el-button
          class="btn"
          type="primary"
          v-permission="['user:add']"
          icon="el-icon-plus"
          @click="create"
          >{{ $t("user.create") }}</el-button
        >
      </el-col>
      <el-col :span="12" class="right-user">
        <el-input
          :placeholder="$t('role.search_by_name_email')"
          prefix-icon="el-icon-search"
          class="name-email-search"
          size="small"
          clearable
          ref="search"
          v-model="nikeName"
          @blur="initSearch"
          @clear="initSearch"
        >
        </el-input>
        <el-button
          class="normal btn"
          icon="el-icon-setting"
          @click="filterShow"
          >筛选</el-button
        >
        <el-dropdown trigger="click" :hide-on-click="false">
          <el-button class="normal btn" icon="el-icon-setting"
            >列表项</el-button
          >
          <el-dropdown-menu class="list-colums-slect" slot="dropdown">
            <p class="title">请选择列表中要展示的信息</p>
            <el-checkbox
              :indeterminate="isIndeterminate"
              v-model="checkAll"
              @change="handleCheckAllChange"
              >全选</el-checkbox
            >
            <el-checkbox-group
              v-model="checkedColumnNames"
              @change="handleCheckedColumnNamesChange"
            >
              <el-checkbox v-for="column in columnNames" :label="column.props" :key="column.props">{{
               $t(column.label)
              }}</el-checkbox>
            </el-checkbox-group>
          </el-dropdown-menu>
        </el-dropdown>
      </el-col>
    </el-row>
    <div class="table-container">
      <grid-table
        v-if="canLoadDom"
        v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
        :tableData="data"
        :columns="checkedColumnNames"
        :pagination="paginationConfig"
        @sort-change="sortChange"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      >
        <el-table-column prop="username" label="ID" />
        <el-table-column
          show-overflow-tooltip
          key="nickName"
          prop="nickName"
          sortable="custom"
          :label="$t('commons.nick_name')"
        />
        <!-- <el-table-column prop="gender" :label="$t('commons.gender')" width="60" /> -->
        <el-table-column prop="from" :label="$t('user.source')" width="80">
          <template slot-scope="scope">
            <div>
              {{
                scope.row.from === 0
                  ? "LOCAL"
                  : scope.row.from === 1
                  ? "LDAP"
                  : "OIDC"
              }}
            </div>
          </template>
        </el-table-column>

        <el-table-column
          show-overflow-tooltip
          key="email"
          prop="email"
          :label="$t('commons.email')"
        />
        <el-table-column
          v-if="isPluginLoaded"
          show-overflow-tooltip
          key="dept"
          prop="dept"
          sortable="custom"
          :label="$t('commons.organization')"
        >
          <template slot-scope="scope">
            <div>{{ scope.row.dept && scope.row.dept.deptName || "-" }}</div>
          </template>
        </el-table-column>
        <el-table-column
          v-if="isPluginLoaded"
          prop="roles"
          :formatter="filterRoles"
          key="roles"
          show-overflow-tooltip
          :label="$t('commons.role')"
        >
        </el-table-column>
        <el-table-column
          key="status"
          prop="status"
          sortable="custom"
          :label="$t('commons.status')"
          width="80"
        >
          <template v-slot:default="scope">
            <el-switch
              v-model="scope.row.enabled"
              :active-value="1"
              :inactive-value="0"
              :disabled="!checkPermission(['user:edit']) || scope.row.isAdmin"
              inactive-color="#DCDFE6"
              @change="changeSwitch(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column
          show-overflow-tooltip
          prop="createTime"
          key="createTime"
          sortable="custom"
          :label="$t('commons.create_time')"
          width="180"
        >
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>

        <el-table-column slot="__operation" label="操作" fixed="right" width="168">
          <template slot-scope="scope">
            <el-button
              v-permission="['user:edit']"
              @click="edit(scope.row)"
              class="text-btn"
              type="text"
              >{{ $t("commons.edit") }}</el-button
            >
            <el-popover
              placement="left"
              width="321"
              :ref="'initPwd' + scope.row.userId"
              popper-class="reset-pwd"
              trigger="hover"
            >
              <i class="el-icon-warning"></i>
              <div class="tips">是否恢复为初始密码?</div>
              <div class="editer-form-title">
                <span class="pwd" type="text">{{
                  $t("commons.default_pwd") + "：" + defaultPWD
                }}</span>
                <el-button
                  v-clipboard:copy="defaultPWD"
                  v-clipboard:success="onCopy"
                  v-clipboard:error="onError"
                  class="btn-text"
                  type="text"
                >
                  {{ $t("commons.copy") }}
                </el-button>
              </div>
              <div class="foot">
                <!-- <el-button class="btn normal">{{
                  $t("fu.search_bar.cancel")
                }}</el-button> -->
                <el-button @click="resetPwd(scope.row.userId)" type="primary" class="btn">{{
                  $t("fu.search_bar.ok")
                }}</el-button>
              </div>

              <el-button
                slot="reference"
                v-permission="['user:editPwd']"
                class="text-btn mar16"
                @click="editPassword(scope.row)"
                type="text"
                >{{ $t("member.edit_password") }}</el-button
              >
            </el-popover>
            <el-button
              v-permission="['user:del']"
              @click="del(scope.row)"
              class="text-btn"
              type="text"
              >{{ $t("commons.delete") }}</el-button
            >
          </template>
        </el-table-column>
      </grid-table>
    </div>
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
        @keypress.enter.native="editUserPassword('editPasswordForm')"
      >
        <el-form-item :label="$t('member.new_password')" prop="newPassword">
          <el-input
            v-model="ruleForm.newPassword"
            type="password"
            autocomplete="off"
            show-password
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="text" @click="editPasswordVisible = false">{{
          $t("commons.cancel")
        }}</el-button>
        <el-button
          type="primary"
          @click="editUserPassword('editPasswordForm')"
          >{{ $t("commons.confirm") }}</el-button
        >
      </div>
    </el-dialog>
    <keep-alive>
      <filterUser ref="filterUser" @search="filterDraw" ></filterUser>
    </keep-alive>
    <user-editer @saved="search" ref="userEditer" ></user-editer>
  </de-layout-content>
</template>

<script>
import userEditer from './userEditer.vue'
const columnOptions = [{
  label: 'ID',
  props: 'username'
},{
  label: 'commons.nick_name',
  props: 'nickName'
},{
  label: 'user.source',
  props: 'from'
},{
  label: 'commons.email',
  props: 'email'
},{
  label: 'commons.organization',
  props: 'dept'
},{
  label: 'commons.role',
  props: 'roles'
},{
  label: 'commons.status',
  props: 'status'
},{
  label: 'commons.create_time',
  props: 'createTime'
},];
import DeLayoutContent from "@/components/business/DeLayoutContent";
import {
  addOrder,
  formatOrders,
} from "@/utils/index";
import { pluginLoaded, defaultPwd } from "@/api/user";
/* import { ldapStatus, pluginLoaded } from '@/api/user' */
import {
  userLists,
  delUser,
  editPassword,
  editStatus,
  allRoles,
} from "@/api/system/user";
import { mapGetters } from "vuex";
import filterUser from "./filterUser.vue";
import GridTable from "@/components/gridTable/index.vue";
export default {
  components: { DeLayoutContent, GridTable, filterUser, userEditer },
  data() {
    return {
      checkAll: true,
      checkedColumnNames: columnOptions.map(ele => ele.props),
      columnNames: columnOptions,
      isIndeterminate: false,
      paginationConfig: {
        currentPage: 1,
        pageSize: 10,
        total: 0,
      },
      data: [],
      dialogVisible: false,
      editPasswordVisible: false,
      form: {
        roles: [
          {
            id: "",
          },
        ],
      },
      ruleForm: {},
      rule: {
        newPassword: [
          {
            required: true,
            message: this.$t("user.input_password"),
            trigger: "blur",
          },
          {
            required: true,
            pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[^]{8,30}$/,
            message: this.$t("member.password_format_is_incorrect"),
            trigger: "blur",
          },
        ],
      },
      cacheCondition: [],
      depts: null,
      roles: [],
      nikeName: "",
      userRoles: [],
      orderConditions: [],
      isPluginLoaded: false,
      defaultPWD: "DataEase123..",
      canLoadDom: false,
    };
  },
  computed: {
    ...mapGetters(["user"]),
  },
  mounted() {
    this.allRoles();
    this.search();
    document.addEventListener('keypress', this.entryKey)
  },
  beforeCreate() {
    pluginLoaded()
      .then((res) => {
        this.isPluginLoaded = res.success && res.data;
        if (this.isPluginLoaded) {
          // this.searchConfig.components.push(...this.extraFilterComponents);
        }
        this.canLoadDom = true;
      })
      .catch((e) => {
        this.canLoadDom = true;
      });
    defaultPwd().then((res) => {
      if (res && res.data) {
        this.defaultPWD = res.data;
      }
    });
  },
  destroyed () {
    document.removeEventListener('keypress', this.entryKey)
  },
  methods: {
    entryKey (event) {
      const keyCode = event.keyCode
      if (keyCode === 13) {
        this.$refs.search.blur()
      }
    },
    filterRoles(row, column, cellValue) {
      const roleNames = cellValue.map(ele => ele.roleName);
      return roleNames.length ? roleNames.join() : "-";
    },
    initSearch() {
      this.handleCurrentChange(1);
    },
    filterShow() {
      this.$refs.filterUser.init();
    },
    handleCheckAllChange(val) {
      this.checkedColumnNames = val ? columnOptions.map(ele => ele.props) : [];
      this.isIndeterminate = false;
    },
    handleCheckedColumnNamesChange(value) {
      let checkedCount = value.length;
      this.checkAll = checkedCount === this.columnNames.length;
      this.isIndeterminate =
        checkedCount > 0 && checkedCount < this.columnNames.length;
    },
    resetPwd(userId) {
      editPassword({ userId, newPassword: this.defaultPWD }).then((res) => {
            this.$success(this.$t("commons.modify_success"));
            this.initSearch();
          }).finally(() => {
            this.$refs['initPwd' + userId].doClose();
          });
    },
    sortChange({ column, prop, order }) {
      this.orderConditions = [];
      if (!order) {
        this.initSearch();
        return;
      }
      if (prop === "dept") {
        prop = "u.deptId";
      }
      if (prop === "status") {
        prop = "u.enabled";
      }
      this.orderConditions = [];
      addOrder({ field: prop, value: order }, this.orderConditions);
      this.initSearch();
    },
    onCopy(e) {
      this.$success(this.$t("commons.copy_success"));
    },
    onError(e) {},
    handleSizeChange(pageSize) {
      this.paginationConfig.pageSize = pageSize;
      this.search();
    },
    handleCurrentChange(currentPage) {
      this.paginationConfig.currentPage = currentPage;
      this.search();
    },
    filterDraw(condition) {
      this.cacheCondition = condition;
      this.initSearch()
    },
    search() {
      const param = {
        orders: formatOrders(this.orderConditions),
        conditions: [...this.cacheCondition],
      };
      if (this.nikeName) {
        param.conditions.push({
            field: `concat(nick_name, ',' , email)`,
            operator: 'like',
            value: this.nikeName
          })
      }
      const { currentPage, pageSize } = this.paginationConfig;
      userLists(currentPage, pageSize, param).then((response) => {
        this.data = response.data.listObject;
        this.paginationConfig.total = response.data.itemCount;
      });
    },
    create() {
      this.$refs.userEditer.init();
    },

    edit(row) {
      this.$refs.userEditer.init(row);
    },
    editPassword(row) {
      this.editPasswordVisible = true;
      const tempForm = Object.assign({}, row);
      this.ruleForm = { userId: tempForm.userId };
    },
    del(row) {
      this.$confirm(this.$t("确定删除该用户吗？"), "", {
        confirmButtonText: this.$t("commons.delete"),
        cancelButtonText: this.$t("commons.cancel"),
        cancelButtonClass: "de-confirm-fail-btn de-confirm-fail-cancel",
        confirmButtonClass: "de-confirm-fail-btn de-confirm-fail-confirm",
        customClass: "de-confirm de-confirm-fail",
        iconClass: "el-icon-warning",
      })
        .then(() => {
          delUser(encodeURIComponent(row.userId)).then((res) => {
            this.openMessageSuccess();
            this.initSearch();
          });
        })
       .catch(() => {
          this.$info(this.$t("commons.delete_cancel"));
        });
    },
    openMessageSuccess() {
      const h = this.$createElement;
      this.$message({
        message: h("p", null, [h("span", null, this.$t("commons.delete_success"))]),
        iconClass: "el-icon-success",
        customClass: "de-message-success de-message",
      });
    },
    editUserPassword(editPasswordForm) {
      this.$refs[editPasswordForm].validate((valid) => {
        if (valid) {
          editPassword(this.ruleForm).then((res) => {
            this.$success(this.$t("commons.modify_success"));
            this.editPasswordVisible = false;
            this.initSearch();
            this.user &&
              this.user.userId &&
              this.user.userId === editPasswordForm.userId &&
              window.location.reload();
          });
        } else {
          return false;
        }
      });
    },
    handleClose() {
      this.depts = null;
      this.formType = "add";
      this.form = {};
      this.editPasswordVisible = false;
      this.dialogVisible = false;
    },
    changeSwitch(row) {
      const { userId, enabled } = row;
      const param = { userId: userId, enabled: enabled };
      editStatus(param).then((res) => {
        this.$success(this.$t("commons.modify_success"));
      });
    },
    allRoles() {
      allRoles().then((res) => {
        this.roles = res.data;
      });
    },
  },
};
</script>

<style scoped lang="scss">
.table-container {
  height: calc(100% - 50px);

  .text-btn {
    font-family: PingFang SC;
    font-size: 14px;
    font-weight: 400;
    line-height: 22px;
    letter-spacing: 0px;
    text-align: center;
    margin-left: 2px;
    border: none;
    padding: 2px 4px;
  }

  .text-btn:hover {
    background: rgba(51, 112, 255, 0.1);
  }
  .disable-btn {
    color: #bbbfc4;
  }

  .mar16 {
    margin: 0 -2px 0 4px;
  }
}
.top-operate {
  margin-bottom: 16px;

  .btn {
    border-radius: 4px;
    padding: 5px 12px 5px 12px;
    //styleName: 中文/桌面端/正文 14 22 Regular;
    font-family: PingFang SC;
    font-size: 14px;
    font-weight: 400;
    line-height: 20px;
    letter-spacing: 0px;
    text-align: center;
    border: none;
    box-sizing: border-box;
  }

  .normal {
    color: #1f2329;
    border: 1px solid #bbbfc4;
    margin-left: 12px;
  }

  .right-user {
    text-align: right;
    display: flex;
    align-items: center;
    justify-content: end;

    .el-input--medium .el-input__icon {
      line-height: 32px;
    }
  }

  .name-email-search {
    width: 240px;
  }
}
</style>
<style lang="scss">
.reset-pwd {
  padding: 20px 24px !important;
  display: flex;
  flex-wrap: wrap;

  .tips {
    font-family: PingFang SC;
    font-size: 14px;
    font-weight: 500;
    line-height: 22px;
    margin-left: 8.67px;
    color: #1f2329;
  }

  i {
    font-size: 14.666666030883789px;
    color: #ff8800;
    line-height: 22px;
  }
  .editer-form-title {
    margin: 4px 0 16px 24px;

    .pwd,
    .btn-text {
      font-family: PingFang SC;
      font-size: 14px;
      font-weight: 400;
      line-height: 22px;
      text-align: left;
    }

    .pwd {
      margin-right: 8px;
      color: #1f2329;
    }

    .btn-text {
      border: none;
    }
  }

  .foot {
    text-align: right;
    width: 100%;
    .btn {
      border-radius: 4px;
      padding: 4px 12px 4px 12px;
      font-family: PingFang SC;
      font-size: 14px;
      font-weight: 400;
      line-height: 20px;
      letter-spacing: 0px;
      text-align: center;
      border: none;
      box-sizing: border-box;
    }

    .normal {
      color: #1f2329;
      border: 1px solid #bbbfc4;
      margin-left: 12px;
    }
  }
}
.list-colums-slect {
  padding: 8px 11px !important;
  width: 238px;

  .title,
  .el-checkbox {
    font-family: PingFang SC;
    font-size: 14px;
    font-weight: 400;
    padding: 5px 0;
    margin: 0;
    color: #8f959e;
  }

  .el-checkbox {
    color: #1f2329;
    width: 100%;
  }
}
</style>
<style lang="scss">
.de-message {
  min-width: 20px !important;
  padding: 16px 20px !important;
  flex-direction: row;
  box-shadow: 0px 4px 8px 0px #1F23291A;
  span {
    font-family: PingFang SC;
    font-size: 14px;
    font-weight: 500;
    line-height: 22px;
    letter-spacing: 0px;
    text-align: left;
    color: #1f2329;
  }

  i {
    height: 14.666666984558105px;
    width: 14.666669845581055px;
    margin-right: 8.67px;
  }
}
.de-message-fail {
  border: 1px solid #f54a45 !important;
  background: #fef1f1 !important;
  i {
    color: #f54a45;
  }
}

.de-message-success {
  border: 1px solid #34c724 !important;
  background: #f0fbef !important;
  i {
    color: #34c724;
  }
}
</style>
<style lang="scss">
.de-confirm {
  border: none;
  .el-message-box__header {
    display: none;
  }

  .el-message-box__content {
    padding: 24px;
  }

  .el-message-box__container {
    display: flex;
    align-items: center;
  }
  .el-message-box__status {
    height: 22px;
    width: 22px;
    font-size: 22px !important;
    margin-right: 17px;
  }

  .el-message-box__message {
    //styleName: 中文/桌面端/四级标题 16 24 Medium;
    font-family: PingFang SC;
    font-size: 16px;
    font-weight: 500;
    line-height: 24px;
    letter-spacing: 0px;
    text-align: left;
    color: #1f2329;
  }

  .el-message-box__btns {
    padding: 0;
  }

  .de-confirm-fail-btn {
    height: 32px;
    width: 80px;
    border-radius: 4px;
    font-family: PingFang SC;
    font-size: 14px;
    font-weight: 400;
    line-height: 22px;
    text-align: center;
    padding: 0;
  }

  .de-confirm-fail-cancel {
    background: #ffffff;
    border: 1px solid #bbbfc4;
    color: #1f2329;
  }

  .de-confirm-fail-confirm,
  .de-confirm-fail-confirm:hover {
    background: #f54a45 !important;
    border: none;
    color: #ffffff;
  }

}

.de-confirm-fail {
  padding: 0 24px 24px 0 !important;
  .el-message-box__status {
    color: #ff8800 !important;
  }
}
</style>
