<script lang="ts" setup>
import icon_searchOutline_outlined from "@/assets/svg/icon_search-outline_outlined.svg";
/* import icon_deleteTrash_outlined from "@/assets/svg/icon_delete-trash_outlined.svg"; */
import icon_succeed_filled from "@/assets/svg/icon_succeed_filled.svg";
import icon_ban_filled from "@/assets/svg/icon_ban_filled.svg";
import icon_resetpassword from "@/assets/svg/icon_resetpassword.svg";
import icon_warning_filled from "@/assets/svg/icon_warning_filled.svg";
import icon_mfa_reset from "@/assets/svg/icon_mfa_reset.svg";
import icon_lock from "@/assets/svg/icon-lock.svg";
import { ref, reactive, onMounted, computed, unref } from "vue";
import { ElTabs, ElTabPane, ElIcon } from "element-plus-secondary";
import { Icon } from "@/components/icon-custom";
import RoleManage from "./RoleManage.vue";
import { useI18n } from "@/hooks/web/useI18n";
import GridTable from "@/components/grid-table/src/GridTable.vue";
import {
  memberPageApi,
  defaultPwdApi,
  resetPwdApi,
  userUnlockApi,
} from "@/api/user";
import { searchRoleApi } from "@/api/user";
import request from "@/config/axios";
import { ElMessage, ElMessageBox } from "element-plus-secondary";
import { setColorName } from "@/utils/utils";
import useClipboard from "vue-clipboard3";
import { useUserStoreWithOut } from "@/store/modules/user";
import { logoutHandler } from "@/utils/logout";
import { debounce } from "lodash-es";

const { t } = useI18n();
const userStore = useUserStoreWithOut();
const curUid = computed(() => userStore.getUid);
const { toClipboard } = useClipboard();
const activeName = ref("user");
const loading = ref(false);
const handleClick = () => {};
interface FieldSort {
  field: string;
  type: boolean;
}

const state = reactive({
  userList: [],
  paginationConfig: {
    currentPage: 1,
    pageSize: 10,
    total: 0,
  },
  orders: [] as FieldSort[],
});
const keyword = ref(null);

const buildParam = () => {
  const param = {};
  if (keyword.value) {
    param["keyword"] = keyword.value;
  }
  if (state.orders?.length) {
    state.orders.forEach((item) => {
      param[item["field"]] = item.type;
    });
  }
  return param;
};

const search = () => {
  loading.value = true;
  memberPageApi(
    state.paginationConfig.currentPage,
    state.paginationConfig.pageSize,
    buildParam(),
  ).then((res) => {
    const records = res.data.records;
    records.forEach((item) => {
      setColorName(item, keyword.value);
      setColorName(item, keyword.value, "account", "colorAccount");
      setColorName(item, keyword.value, "email", "colorEmail");
    });
    imgType.value = getEmptyImg();
    emptyDesc.value = getEmptyDesc();
    records.forEach((row: any) => {
      row.tempRoleIds = (row.roleItems || []).map((r: any) => r.id);
      row.rolePopoverVisible = false;
      row.rolePopoverKey = `${row.id}_${Date.now()}`;
    });
    state.userList = records;
    state.paginationConfig.total = res.data.total;
    if (
      state.paginationConfig.currentPage > 1 &&
      res.data?.pages < state.paginationConfig.currentPage &&
      !records?.length
    ) {
      pageChange(1);
    }
    loading.value = false;
  });
};

const refreshGrid = () => {
  search();
};
const refreshRole = () => {
  // role saved in RoleManage, member grid may need refresh
};
onMounted(() => {
  search();
});
const pageChange = (index: number) => {
  if (typeof index !== "number") {
    return;
  }
  state.paginationConfig.currentPage = index;
  search();
};
const sizeChange = (size: number) => {
  state.paginationConfig.currentPage = 1;
  state.paginationConfig.pageSize = size;
  search();
};
const sortChange = (param: any) => {
  state.orders = [];
  if (param.order && param.prop === "createTime") {
    const type = param.order.substring(0, param.order.indexOf("ending"));
    state.orders.push({
      field: "timeDesc",
      type: type !== "asc",
    });
    search();
  }
};

const imgType = ref();
const emptyDesc = ref("");
const getEmptyImg = (): string => {
  if (keyword.value) {
    return "tree";
  }
  return "noneWhite";
};

const getEmptyDesc = (): string => {
  if (keyword.value) {
    return t("work_branch.relevant_content_found");
  }
  return "";
};

// Role editing
const roleOptions = ref<any[]>([]);
let roleOptionsLoaded = false;

const loadRoleOptions = async () => {
  if (roleOptionsLoaded) return;
  const res = await searchRoleApi();
  roleOptions.value = res.data || [];
  roleOptionsLoaded = true;
};

const closeRolePopover = (row: any) => {
  row.rolePopoverVisible = false;
};

const confirmRole = async (row: any) => {
  const { mountUserApi, unMountUserApi } = await import("@/api/user");
  const originalIds = (row.roleItems || []).map((r: any) => r.id);
  const currentIds = row.tempRoleIds || [];
  const toAdd = currentIds.filter((id: string) => !originalIds.includes(id));
  const toRemove = originalIds.filter((id: string) => !currentIds.includes(id));
  try {
    for (const roleId of toAdd) {
      await mountUserApi({ uids: [row.id], rid: roleId });
    }
    for (const roleId of toRemove) {
      await unMountUserApi({ uid: row.id, rid: roleId });
    }
    closeRolePopover(row);
    await search();
    ElMessage.success(t("common.save_success"));
  } catch (e) {
    ElMessage.error(t("commons.save_failed"));
  }
};
const debounceConfirmRole = debounce(confirmRole, 300);

const defaultPwd = ref(null);

const loadRestInfo = async () => {
  if (defaultPwd.value) {
    return;
  }
  const res = await defaultPwdApi();
  defaultPwd.value = res.data;
};
const resetPwd = (row: any) => {
  resetPwdApi(row.id).then(() => {
    ElMessage.success(t("user.reset_success"));
    closeResetInfo(row);
    if (row.id === curUid.value) {
      logoutHandler();
    }
  });
};
const closeResetInfo = (row: any) => {
  row.popoverRef?.hide();
  row.resetInfoShow = false;
};
const copyPwd = async () => {
  try {
    await toClipboard(defaultPwd.value);
    ElMessage.success(t("common.copy_success"));
  } catch (e) {
    ElMessage.warning(t("common.copy_unsupported"), e);
  }
};
const setPopoverRef = (el: any, row: any) => {
  row.popoverRef = el;
};
const setButtonRef = (el: any, row: any) => {
  row.buttonRef = el;
};
const onClickOutside = (row: any) => {
  if (row.popoverRef) {
    unref(row.popoverRef).popperRef?.delayHide?.();
  }
};
const resetMfaHandler = (row: any) => {
  const url = `/user/mfaRest/${row.id}`;
  request.post({ url }).then(() => {
    ElMessage.success(t("user.reset_success"));
  });
};

const unlockHandler = (row: any) => {
  ElMessageBox.confirm(t("user.confirm_unlock"), {
    confirmButtonType: "danger",
    type: "warning",
    confirmButtonText: t("user.unlock_user"),
    cancelButtonText: t("commons.cancel"),
    autofocus: false,
    showClose: false,
  })
    .then(() => {
      userUnlockApi(row.id).then(() => {
        ElMessage.success(t("user.unlock_user_success"));
        search();
      });
    })
    .catch(() => {});
};

const cancelRole = (row: any) => {
  row.tempRoleIds = (row.roleItems || []).map((r: any) => r.id);
  closeRolePopover(row);
};
</script>
<template>
  <div class="member-top-bar">
    <el-tabs v-model="activeName" @tab-click="handleClick">
      <el-tab-pane :label="t('commons.member')" name="user"></el-tab-pane>
      <el-tab-pane :label="t('system.role')" name="role"></el-tab-pane>
    </el-tabs>
  </div>
  <div v-if="activeName === 'user'" class="user-table">
    <div class="member-header">
      <div class="member-header-left">
        <span class="member-title">{{ t("commons.member") }}</span>
      </div>
      <div class="member-header-right">
        <el-input
          v-model="keyword"
          clearable
          :placeholder="t('user.search_placeholder')"
          @change="search"
          style="width: 240px"
        >
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined">
                <icon_searchOutline_outlined class="svg-icon" />
              </Icon>
            </el-icon>
          </template>
        </el-input>
      </div>
    </div>
    <div class="user-table__content">
      <GridTable
        :pagination="state.paginationConfig"
        :table-data="state.userList"
        :loading="loading"
        :empty-desc="emptyDesc"
        :empty-img="imgType"
        class="popper-max-width"
        @current-change="pageChange"
        @size-change="sizeChange"
        @sort-change="sortChange"
        :show-empty-img="!loading"
      >
        <el-table-column
          prop="name"
          :label="t('user.name')"
          show-overflow-tooltip
          width="150"
        />
        <el-table-column
          prop="account"
          :label="t('user.account')"
          show-overflow-tooltip
        />
        <el-table-column
          prop="email"
          :label="t('common.email')"
          show-overflow-tooltip
          width="200"
        />
        <el-table-column
          :label="t('user.role')"
          min-width="200"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <el-popover
              :key="row.rolePopoverKey"
              trigger="click"
              v-model:visible="row.rolePopoverVisible"
              placement="bottom-start"
              popper-class="role-popover"
              width="200"
              @show="loadRoleOptions"
            >
              <template #reference>
                <div class="role-text">
                  <span>{{
                    (row.roleItems || []).map((r: any) => r.name).join(", ")
                  }}</span>
                  <el-icon class="el-icon-animate">
                    <ArrowDownBold />
                  </el-icon>
                </div>
              </template>
              <div class="role-container">
                <el-checkbox-group v-model="row.tempRoleIds">
                  <el-checkbox
                    v-for="option in roleOptions"
                    :key="option.id"
                    :value="option.id"
                    :label="option.name"
                  />
                </el-checkbox-group>
              </div>
              <el-divider class="role-divider" style="margin: 8px 0" />
              <div class="role-actions">
                <el-button size="small" @click="cancelRole(row)">{{
                  t("commons.cancel")
                }}</el-button>
                <el-button
                  size="small"
                  type="primary"
                  @click="debounceConfirmRole(row)"
                  >{{ t("commons.confirm") }}</el-button
                >
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column :label="t('user.state')" width="100">
          <template #default="{ row }">
            <div style="display: flex; align-items: center">
              <el-icon size="16px">
                <Icon>
                  <icon_succeed_filled v-if="row.enable" />
                  <icon_ban_filled v-else />
                </Icon>
              </el-icon>
              <span style="padding: 0 8px 0 8px">{{
                row.enable
                  ? t("user.enable_success")
                  : t("user.disable_success")
              }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column width="120" fixed="right" :label="t('common.operate')">
          <template #default="{ row }">
            <div class="operate-icon-container">
              <el-tooltip
                effect="dark"
                :content="t('user.reset_pwd')"
                placement="top"
              >
                <el-button
                  text
                  :disabled="row.origin !== 0"
                  :ref="
                    (el) => {
                      setButtonRef(el, row);
                    }
                  "
                  v-click-outside="onClickOutside(row)"
                >
                  <template #icon>
                    <Icon name="icon_resetpassword"
                      ><icon_resetpassword class="svg-icon"
                    /></Icon>
                  </template>
                </el-button>
              </el-tooltip>
              <el-popover
                placement="right"
                :width="300"
                :virtual-ref="row.buttonRef"
                trigger="click"
                :ref="
                  (el) => {
                    setPopoverRef(el, row);
                  }
                "
                @show="loadRestInfo"
                :show-arrow="true"
              >
                <div class="reset-pwd-confirm">
                  <div class="confirm-header">
                    <span class="icon-span">
                      <el-icon>
                        <Icon name="icon_warning_filled"
                          ><icon_warning_filled class="svg-icon"
                        /></Icon>
                      </el-icon>
                    </span>
                    <span class="header-span">{{
                      t("user.reset_confirm")
                    }}</span>
                  </div>
                  <div class="confirm-content">
                    <span>{{ defaultPwd }}</span>
                    <el-button text @click="copyPwd">{{
                      t("common.copy")
                    }}</el-button>
                  </div>
                  <div v-if="row.id === curUid" class="confirm-warning">
                    <span>{{ t("user.modify_cur_pwd") }}</span>
                  </div>
                  <div class="confirm-foot">
                    <el-button @click="closeResetInfo(row)">{{
                      t("common.cancel")
                    }}</el-button>
                    <el-button type="primary" @click="resetPwd(row)">
                      {{ t("common.sure") }}
                    </el-button>
                  </div>
                </div>
              </el-popover>
              <el-tooltip
                effect="dark"
                :content="t('setting_mfa.reset_key_tips')"
                placement="top"
              >
                <el-button text @click="resetMfaHandler(row)">
                  <template #icon>
                    <Icon name="icon_mfa_reset"
                      ><icon_mfa_reset class="svg-icon"
                    /></Icon>
                  </template>
                </el-button>
              </el-tooltip>
              <el-tooltip
                v-if="row.locked && row.id !== curUid"
                effect="dark"
                :content="t('user.unlock_user')"
                placement="top"
              >
                <el-button text @click="unlockHandler(row)">
                  <template #icon>
                    <Icon name="icon_lock"><icon_lock class="svg-icon" /></Icon>
                  </template>
                </el-button>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </GridTable>
    </div>
  </div>
  <div v-else-if="activeName === 'role'" class="role-content">
    <role-manage
      @refresh="refreshRole"
      @refresh-grid="refreshGrid"
    ></role-manage>
  </div>
</template>

<style lang="less" scoped>
.member-top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;

  :deep(.ed-tabs) {
    width: 100%;
  }
}

.user-table,
.role-content {
  height: calc(100% - 59px);
  box-sizing: border-box;
  background: white;
  padding: 24px;
  border-radius: 12px;

  .user-table__content {
    height: calc(100vh - 296px);
  }
}

.role-content {
  padding: 0;
}

.member-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  .member-header-left {
    display: flex;
    align-items: center;
    gap: 12px;

    .member-title {
      font-size: 16px;
      font-weight: 500;
    }
  }

  .member-header-right {
    display: flex;
    align-items: center;
    gap: 12px;
  }
}

.role-text {
  cursor: pointer;
  font-size: 13px;
  display: flex;
  column-gap: 8px;
  align-items: center;
  &:hover {
    opacity: 0.8;
  }
}

.operate-icon-container {
  font-size: 16px;
  display: flex;

  .ed-button {
    width: 24px;
    height: 24px;
    line-height: 24px;
  }
}

.reset-pwd-confirm {
  padding: 5px 15px;
  .confirm-header {
    width: 100%;
    min-height: 40px;
    line-height: 40px;
    display: flex;
    flex-direction: row;
    .icon-span {
      color: var(--ed-color-warning);
      font-size: 22px;
      i {
        top: 3px;
      }
    }
    .header-span {
      font-size: 16px;
      font-weight: bold;
      margin-left: 10px;
      white-space: pre-wrap;
      word-break: keep-all;
    }
  }
  .confirm-foot {
    padding: 0;
    display: flex;
    flex-wrap: wrap;
    justify-content: flex-end;
    align-items: center;
    margin-top: 15px;
    .ed-button {
      min-width: 48px;
      height: 28px;
      line-height: 28px;
      font-size: 12px;
    }
  }
  .confirm-warning {
    font-size: 12px;
    color: var(--ed-color-danger);
    margin-left: 33px;
  }
  .confirm-content {
    margin-left: 33px;
    display: flex;
    align-items: center;
  }
}
</style>

<style lang="less">
.role-popover {
  padding: var(--ed-popover-padding) 0 !important;

  .ed-popper__arrow {
    display: none;
  }
  .ed-popover__title {
    display: none;
  }

  .ed-popover__content {
    overflow: hidden;
  }

  .role-container {
    display: flex;
    flex-direction: column;
    gap: 4px;
    padding: 0 8px;
    max-height: 200px;
    overflow-y: auto;

    .ed-checkbox-group {
      display: flex;
      flex-direction: column;
      height: fit-content;
    }
    :deep(.ed-checkbox) {
      height: 26px;
    }

    :deep(.ed-checkbox-group) {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    :deep(.ed-checkbox__label) {
      font-size: 13px;
      color: #1f2329;
    }
  }

  .role-divider {
    margin: 8px 0 !important;
  }

  .role-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    padding: 0 4px;
  }
}

.ed-message-box__message {
  .tip {
    margin-top: 8px;
  }
}
.popper-max-width {
  .ed-popper.is-dark {
    white-space: pre-wrap;
    max-width: 300px;
  }
}
</style>
