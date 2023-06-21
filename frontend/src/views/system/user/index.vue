<template>
  <de-layout-content class="de-search-table">
    <el-row class="top-operate">
      <el-col :span="12">
        <deBtn
          v-permission="['user:add']"
          type="primary"
          icon="el-icon-plus"
          @click="create"
        >{{ $t("user.create") }}</deBtn>

        <plugin-com
          v-if="isPluginLoaded"
          ref="ImportUserCom"
          component-name="ImportUser"
        />

      </el-col>
      <el-col
        :span="12"
        class="right-user"
      >
        <el-input
          ref="search"
          v-model="nickName"
          :placeholder="$t('role.search_by_name_email')"
          prefix-icon="el-icon-search"
          class="name-email-search"
          size="small"
          clearable
          @blur="initSearch"
          @clear="initSearch"
        />
        <deBtn
          :secondary="!cacheCondition.length"
          :plain="!!cacheCondition.length"
          icon="iconfont icon-icon-filter"
          @click="filterShow"
        >{{ $t('user.filter') }}<template v-if="filterTexts.length">
          ({{ filterTexts.length }})
        </template>
        </deBtn>
        <el-dropdown
          trigger="click"
          :hide-on-click="false"
        >
          <deBtn
            secondary
            icon="el-icon-setting"
          >{{ $t('user.list') }}</deBtn>
          <el-dropdown-menu
            slot="dropdown"
            class="list-columns-select"
          >
            <p class="title">{{ $t('user.list_info') }}</p>
            <el-checkbox
              v-model="checkAll"
              :indeterminate="isIndeterminate"
              @change="handleCheckAllChange"
            >{{ $t('dataset.check_all') }}</el-checkbox>
            <el-checkbox-group
              v-model="checkedColumnNames"
              @change="handleCheckedColumnNamesChange"
            >
              <el-checkbox
                v-for="column in columnNames"
                :key="column.props"
                :label="column.props"
              >{{ $t(column.label) }}</el-checkbox>
            </el-checkbox-group>
          </el-dropdown-menu>
        </el-dropdown>
      </el-col>
    </el-row>
    <div
      v-if="filterTexts.length"
      class="filter-texts"
    >
      <span class="sum">{{ paginationConfig.total }}</span>
      <span class="title">{{ $t('user.result_one') }}</span>
      <el-divider direction="vertical" />
      <i
        v-if="showScroll"
        class="el-icon-arrow-left arrow-filter"
        @click="scrollPre"
      />
      <div class="filter-texts-container">
        <p
          v-for="(ele, index) in filterTexts"
          :key="ele"
          class="text"
        >
          {{ ele }} <i
            class="el-icon-close"
            @click="clearOneFilter(index)"
          />
        </p>
      </div>
      <i
        v-if="showScroll"
        class="el-icon-arrow-right arrow-filter"
        @click="scrollNext"
      />
      <el-button
        type="text"
        class="clear-btn"
        icon="el-icon-delete"
        @click="clearFilter"
      >{{ $t('user.clear_filter') }}</el-button>
    </div>
    <div
      id="resize-for-filter"
      class="table-container"
      :class="[filterTexts.length ? 'table-container-filter' : '']"
    >
      <grid-table
        v-if="canLoadDom"
        v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
        :table-data="data"
        :columns="checkedColumnNames"
        current-row-key="email"
        :pagination="paginationConfig"
        @columnsChange="columnsChange"
        @sort-change="sortChange"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      >
        <el-table-column
          prop="username"
          key="username"
          label="ID"
        />
        <el-table-column
          key="nickName"
          show-overflow-tooltip
          prop="nickName"
          sortable="custom"
          :label="$t('commons.nick_name')"
        />
        <!-- <el-table-column prop="gender" :label="$t('commons.gender')" width="60" /> -->
        <el-table-column
          prop="from"
          key="from"
          :label="$t('user.source')"
          width="80"
        >
          <template slot-scope="scope">
            <div>
              {{
                scope.row.from === 0
                  ? "LOCAL"
                  : scope.row.from === 1
                    ? "LDAP"
                    : scope.row.from === 2
                      ? "OIDC"
                      : scope.row.from === 3
                        ? "CAS"
                        : scope.row.from === 4
                          ? "Wecom"
                          : scope.row.from === 5
                            ? "Dingtalk"
                            : scope.row.from === 6
                              ? "Lark"
                              : scope.row.from === 7
                                ? "INT Lark" : '-'
              }}
            </div>
          </template>
        </el-table-column>

        <el-table-column
          key="email"
          show-overflow-tooltip
          prop="email"
          :label="$t('commons.email')"
        />
        <el-table-column
          v-if="isPluginLoaded"
          key="dept"
          show-overflow-tooltip
          prop="dept"
          sortable="custom"
          :label="$t('commons.organization')"
        >
          <template slot-scope="scope">
            <div>{{ (scope.row.dept && scope.row.dept.deptName) || "-" }}</div>
          </template>
        </el-table-column>
        <el-table-column
          v-if="isPluginLoaded"
          key="roles"
          prop="roles"
          :label="$t('commons.role')"
        >
          <template slot-scope="scope">
            <el-tooltip
              popper-class="de-table-tooltips"
              class="item"
              effect="dark"
              placement="top"
            >
              <!-- // {{}}会将数据解释为普通文本，而非 HTML 代码。 -->
              <div
                slot="content"
                v-html="filterRoles(scope.row.roles)"
              />
              <div class="de-one-line">{{ filterRoles(scope.row.roles) }}</div>
            </el-tooltip>
          </template>

        </el-table-column>
        <el-table-column
          key="status"
          prop="status"
          sortable="custom"
          :label="$t('commons.status')"
          width="80"
        >
          <template #default="scope">
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
          key="createTime"
          show-overflow-tooltip
          prop="createTime"
          sortable="custom"
          :label="$t('commons.create_time')"
          width="180"
        >
          <template #default="scope">
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>

        <el-table-column
          key="__operation"
          slot="__operation"
          :label="$t('commons.operating')"
          fixed="right"
          :width="operateWidth"
        >
          <template slot-scope="scope">
            <el-button
              v-permission="['user:edit']"
              class="de-text-btn mr2"
              type="text"
              @click="edit(scope.row)"
            >{{ $t("commons.edit") }}</el-button>
            <el-popover
              :ref="'initPwd' + scope.row.userId"
              placement="left"
              width="321"
              popper-class="reset-pwd"
              trigger="click"
            >
              <svg-icon class="reset-pwd-icon" icon-class="icon_info_filled" />
              <div class="tips">{{ $t('user.recover_pwd') }}</div>
              <div class="editer-form-title">
                <span
                  class="pwd"
                  type="text"
                >{{
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
                <deBtn
                  type="primary"
                  @click="resetPwd(scope.row.userId)"
                >{{ $t("fu.search_bar.ok") }}</deBtn>
              </div>

              <el-button
                slot="reference"
                v-permission="['user:editPwd']"
                :disabled="resetPwdDisabled(scope.row)"
                class="de-text-btn mar16"
                type="text"
              >{{ $t("member.edit_password") }}</el-button>
            </el-popover>
            <el-button
              v-if="scope.row.id !== 1"
              v-permission="['user:del']"
              class="de-text-btn"
              type="text"
              @click="del(scope.row)"
            >{{ $t("commons.delete") }}</el-button>
            <el-button
              v-if="scope.row.locked"
              v-permission="['user:edit']"
              class="de-text-btn"
              type="text"
              @click="unlock(scope.row)"
            >{{ $t("commons.unlock") }}</el-button>
            <span v-else>&nbsp;</span>
          </template>
        </el-table-column>
      </grid-table>
    </div>
    <keep-alive>
      <filterUser
        ref="filterUser"
        @search="filterDraw"
      />
    </keep-alive>
    <user-editer
      ref="userEditer"
      @saved="search"
    />
  </de-layout-content>
</template>

<script>
import userEditer from './UserEditer.vue'
import { columnOptions } from './options'
import DeLayoutContent from '@/components/business/DeLayoutContent'
import { addOrder, formatOrders } from '@/utils/index'
import { pluginLoaded, defaultPwd } from '@/api/user'
import bus from '@/utils/bus'
import { Base64 } from 'js-base64'
/* import { ldapStatus, pluginLoaded } from '@/api/user' */
import {
  userLists,
  delUser,
  editPassword,
  editStatus,
  allRoles,
  unLock
} from '@/api/system/user'
import { mapGetters } from 'vuex'
import filterUser from './FilterUser.vue'
import GridTable from '@/components/gridTable/index.vue'
import PluginCom from '@/views/system/plugin/PluginCom'
import _ from 'lodash'
export default {
  components: { DeLayoutContent, GridTable, filterUser, userEditer, PluginCom },
  data() {
    return {
      checkAll: true,
      checkedColumnNames: columnOptions.map((ele) => ele.props),
      columnNames: columnOptions,
      isIndeterminate: false,
      paginationConfig: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
      data: [],
      filterTexts: [],
      dialogVisible: false,
      form: {
        roles: [
          {
            id: ''
          }
        ]
      },
      ruleForm: {},
      rule: {
        newPassword: [
          {
            required: true,
            message: this.$t('user.input_password'),
            trigger: 'blur'
          },
          {
            required: true,
            pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[^]{8,30}$/,
            message: this.$t('member.password_format_is_incorrect'),
            trigger: 'blur'
          }
        ]
      },
      cacheCondition: [],
      depts: null,
      roles: [],
      nickName: '',
      userRoles: [],
      orderConditions: [],
      isPluginLoaded: false,
      defaultPWD: 'DataEase123..',
      canLoadDom: false,
      showScroll: false,
      resizeForFilter: null,
      operateWidth: 168
    }
  },
  computed: {
    ...mapGetters(['user'])
  },
  watch: {
    filterTexts: {
      handler() {
        this.getScrollStatus()
      },
      deep: true
    }
  },
  mounted() {
    bus.$on('reload-user-grid', this.search)
    this.allRoles()
    this.search()
    document.addEventListener('keypress', this.entryKey)
    this.resizeObserver()
  },
  beforeCreate() {
    pluginLoaded()
      .then((res) => {
        this.isPluginLoaded = res.success && res.data
        if (!this.isPluginLoaded) {
          this.checkedColumnNames = this.checkedColumnNames.filter(ele => !['dept', 'roles'].includes(ele))
          this.columnNames = this.columnNames.filter(ele => !['dept', 'roles'].includes(ele.props))
        }
        this.canLoadDom = true
      })
      .catch((e) => {
        this.canLoadDom = true
      })
    defaultPwd().then((res) => {
      if (res && res.data) {
        this.defaultPWD = res.data
      }
    })
  },
  destroyed() {
    document.removeEventListener('keypress', this.entryKey)
    bus.$off('reload-user-grid', this.search)
  },
  methods: {
    resetPwdDisabled(row) {
      return ((row.from ?? '') !== '') && row.from > 0
    },
    resizeObserver() {
      this.resizeForFilter = new ResizeObserver(entries => {
        if (!this.filterTexts.length) return
        this.layoutResize()
      })
      this.resizeForFilter.observe(document.querySelector('#resize-for-filter'))
    },
    layoutResize: _.debounce(function() {
      this.getScrollStatus()
    }, 200),
    scrollPre() {
      const dom = document.querySelector('.filter-texts-container')
      dom.scrollLeft -= 10
      if (dom.scrollLeft <= 0) {
        dom.scrollLeft = 0
      }
    },
    scrollNext() {
      const dom = document.querySelector('.filter-texts-container')
      dom.scrollLeft += 10
      const width = dom.scrollWidth - dom.offsetWidth
      if (dom.scrollLeft > width) {
        dom.scrollLeft = width
      }
    },
    clearFilter() {
      this.$refs.filterUser.clearFilter()
    },
    clearOneFilter(index) {
      this.$refs.filterUser.clearOneFilter(index)
      this.$refs.filterUser.search()
    },
    entryKey(event) {
      const keyCode = event.keyCode
      if (keyCode === 13) {
        this.$refs.search.blur()
      }
    },
    filterRoles(cellValue) {
      const roleNames = cellValue.map((ele) => ele.roleName)
      return roleNames.length ? roleNames.join() : '-'
    },
    initSearch() {
      this.handleCurrentChange(1)
    },
    filterShow() {
      this.$refs.filterUser.init()
    },
    handleCheckAllChange(val) {
      this.checkedColumnNames = val
        ? columnOptions.map((ele) => ele.props)
        : []
      if (!this.isPluginLoaded) {
        this.checkedColumnNames = this.checkedColumnNames.filter(ele => !['dept', 'roles'].includes(ele))
      }
      this.isIndeterminate = false
    },
    handleCheckedColumnNamesChange(value) {
      const checkedCount = value.length
      this.checkAll = checkedCount === this.columnNames.length
      this.isIndeterminate =
        checkedCount > 0 && checkedCount < this.columnNames.length
    },
    resetPwd(userId) {
      editPassword({ userId, newPassword: Base64.encode(this.defaultPWD) })
        .then((res) => {
          this.$success(this.$t('commons.modify_success'))
          this.initSearch()
        })
        .finally(() => {
          this.$refs['initPwd' + userId].doClose()
        })
    },
    sortChange({ column, prop, order }) {
      this.orderConditions = []
      if (!order) {
        this.initSearch()
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
      this.initSearch()
    },
    onCopy(e) {
      this.openMessageSuccess('commons.copy_success')
    },
    onError(e) {},
    handleSizeChange(pageSize) {
      this.paginationConfig.currentPage = 1
      this.paginationConfig.pageSize = pageSize
      this.search()
    },
    handleCurrentChange(currentPage) {
      this.paginationConfig.currentPage = currentPage
      this.search()
    },
    filterDraw(condition, filterTexts = []) {
      this.cacheCondition = condition
      this.filterTexts = filterTexts
      this.initSearch()
    },
    getScrollStatus() {
      this.$nextTick(() => {
        const dom = document.querySelector('.filter-texts-container')
        this.showScroll = dom && dom.scrollWidth > dom.offsetWidth
      })
    },
    columnsChange() {
      const arr = this.data
      this.data = []
      this.$nextTick(() => {
        this.data = arr
      })
    },
    search() {
      const param = {
        orders: formatOrders(this.orderConditions),
        conditions: [...this.cacheCondition]
      }
      if (this.nickName) {
        param.conditions.push({
          field: `concat(nick_name, ',' , email)`,
          operator: 'like',
          value: this.nickName
        })
      }
      const { currentPage, pageSize } = this.paginationConfig
      userLists(currentPage, pageSize, param).then((response) => {
        this.data = response.data.listObject
        this.dynamicOprtateWidth()
        this.paginationConfig.total = response.data.itemCount
      })
    },
    create() {
      this.$refs.userEditer.init()
    },

    edit(row) {
      this.$refs.userEditer.init(row)
    },
    del(row) {
      this.$confirm(this.$t('user.sure_delete'), '', {
        confirmButtonText: this.$t('commons.delete'),
        cancelButtonText: this.$t('commons.cancel'),
        cancelButtonClass: 'de-confirm-fail-btn de-confirm-fail-cancel',
        confirmButtonClass: 'de-confirm-fail-btn de-confirm-fail-confirm',
        customClass: 'de-confirm de-confirm-fail',
        iconClass: 'el-icon-warning'
      })
        .then(() => {
          delUser(encodeURIComponent(row.userId)).then((res) => {
            this.openMessageSuccess('commons.delete_success')
            this.initSearch()
          })
        })
        .catch(() => {
          this.$info(this.$t('commons.delete_cancel'))
        })
    },
    openMessageSuccess(text) {
      const h = this.$createElement
      this.$message({
        message: h('p', null, [
          h('span', null, this.$t(text))
        ]),
        iconClass: 'el-icon-success',
        customClass: 'de-message-success de-message'
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
      editStatus(param).then((res) => {
        this.$success(this.$t('commons.modify_success'))
      })
    },
    allRoles() {
      allRoles().then((res) => {
        this.roles = res.data
      })
    },
    unlock(row) {
      unLock(row.username).then(res => {
        row.locked = false
        this.data.forEach(item => {
          if (item.username === row.username) {
            item.locked = false
          }
        })
        this.dynamicOprtateWidth()
        this.$success(this.$t('commons.unlock_success'))
      })
    },
    dynamicOprtateWidth() {
      if (this.data && this.data.some(item => item.locked)) {
        this.operateWidth = 200
        return
      }
      this.operateWidth = 168
    }
  }
}
</script>

<style scoped lang="scss">
.table-container {
  height: calc(100% - 50px);

  .mar16 {
    margin: 0 -2px 0 4px;
  }

  .mr2 {
    margin-left: -3px;
  }
}

.table-container-filter {
  height: calc(100% - 110px);
}
</style>
<style lang="scss">
.reset-pwd-icon {
  margin-top: 4px;
  color: rgb(255, 153, 0);
}
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
.list-columns-select {
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
.de-one-line {
  max-width: 80px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.de-table-tooltips {
  max-width: 200px;
}
</style>

