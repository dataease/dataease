<script lang="ts" setup>
import icon_searchOutline_outlined from "@/assets/svg/icon_search-outline_outlined.svg";
import icon_add_outlined from "@/assets/svg/icon_add_outlined.svg";
import icon_edit_outlined from "@/assets/svg/icon_edit_outlined.svg";
import icon_deleteTrash_outlined from "@/assets/svg/icon_delete-trash_outlined.svg";
import icon_member_filled from "@/assets/svg/icon_member_filled.svg";
import icon_assigned_outlined from "@/assets/svg/icon_assigned_outlined.svg";
import { ref, reactive, onMounted, nextTick, watch } from "vue";
import { Icon } from "@/components/icon-custom";
import GridTable from "@/components/grid-table/src/GridTable.vue";
import OrgUser from "./OrgUser.vue";
import {
  searchRoleApi,
  userSelectedForRoleApi,
  roleDelApi,
  unMountUserApi,
} from "@/api/user";
import RoleForm from "./RoleForm.vue";
import { ElMessage, ElMessageBox } from "element-plus-secondary";
import { useI18n } from "@/hooks/web/useI18n";
import { setColorName } from "@/utils/utils";
import icon_succeed_filled from "@/assets/svg/icon_succeed_filled.svg";
import icon_ban_filled from "@/assets/svg/icon_ban_filled.svg";
const selectedRoleId = ref("");
const selectedRoleName = ref("");
const selectedRoleRoot = ref(false);
const roleKeyword = ref("");
const selectedFilterkey = ref("");
const roleFormRef = ref(null);
const { t } = useI18n();

const loading = ref(false);
interface Tree {
  id: string;
  name: string;
  readonly: boolean;
  children?: Tree[];
  disabled: boolean;
  root?: boolean;
}

const handleNodeClick = (data: Tree) => {
  if (data.disabled) {
    return;
  }
  selectedRoleId.value = data.id;
  selectedRoleName.value = data.name;
  selectedRoleRoot.value = data.root;
  selectedSearch(data.id);
};

const state = reactive({
  paginationConfig: {
    currentPage: 1,
    pageSize: 10,
    total: 0,
  },
  optionUserList: [],
  addedUserList: [],
  roleData: [],
  checkList: [],
});
const order = ref(null);
state.roleData = [
  {
    id: "system",
    name: t("role.system_role"),
    children: null,
    disabled: true,
    root: true,
  },
  {
    id: "custom",
    name: t("role.custom_role"),
    children: null,
    disabled: true,
    root: false,
  },
];

const selectedSearch = (rid?: string) => {
  const param = { rid, order: order.value, keyword: selectedFilterkey.value };
  if (rid) {
    loading.value = true;
    const page = state.paginationConfig.currentPage;
    const limit = state.paginationConfig.pageSize;
    userSelectedForRoleApi(page, limit, param)
      .then((res) => {
        if (res?.data?.total) {
          const records = res.data.records;
          records.forEach((item) => {
            setColorName(item, selectedFilterkey.value);
            setColorName(
              item,
              selectedFilterkey.value,
              "account",
              "colorAccount"
            );
          });
          state.addedUserList = records;
          state.paginationConfig.total = res.data.total;
        } else {
          state.addedUserList = [];
          state.paginationConfig.total = 0;
        }
        imgType.value = getEmptyImg();
        emptyDesc.value = getEmptyDesc();
      })
      .finally(() => {
        loading.value = false;
      });
  }
};

const roleSearch = (resolve, reject) => {
  loading.value = true;
  const apiCall = searchRoleApi();
  apiCall
    .then((res) => {
      const roles = res.data;
      const map = groupBy(roles);
      state.roleData[0].children = map.get(true);
      state.roleData[1].children = map.get(false);
      loading.value = false;
      if (selectedRoleId.value) {
        selectedRoleName.value = getNode()?.name;
      }
      resolve && resolve(res);
    })
    .catch((e) => {
      loading.value = false;
      reject && reject(e);
    });
};

const groupBy = (list: Tree[]) => {
  const map = new Map();
  list.forEach((item) => {
    const root = item.root;
    let arr = map.get(root);
    if (!arr) {
      arr = [];
    }
    item.disabled = false;
    arr.push(item);
    map.set(root, arr);
  });
  return map;
};

const roleAdd = () => {
  roleFormRef.value.init();
};

const roleEdit = (row) => {
  if (row.root) {
    return;
  }
  roleFormRef.value.edit(row.id);
};

const delHandler = (row) => {
  if (row.root) {
    return;
  }
  ElMessageBox.confirm(t("role.confirm_delete"), {
    confirmButtonType: "danger",
    type: "warning",
    autofocus: false,
    confirmButtonText: t("common.delete"),
    cancelButtonText: t("dataset.cancel"),
    dangerouslyUseHTMLString: true,
    message:
      '<strong style="font-size: 16px;">' +
      t("role.confirm_delete") +
      "</strong></br>" +
      t("role.delete_tips"),
    showClose: false,
  }).then(() => {
    loading.value = true;
    roleDelApi(row.id).then(() => {
      ElMessage.success(t("common.delete_success"));
      roleSaved("modify");
      if (selectedRoleId.value === row.id) {
        selectedRoleId.value = "";
        selectedRoleName.value = "";
        selectedRoleRoot.value = false;
      }
    });
  });
};
const emits = defineEmits(["refresh", "refresh-grid"]);
const roleSaved = (type: string) => {
  roleSearch(null, null);
  emits("refresh");
  if (type === "modify") {
    emits("refresh-grid");
  }
};

const getNode = () => {
  let result = null;
  state.roleData.forEach((group) => {
    const nodes = group["children"];
    nodes?.forEach((node) => {
      if (node.id === selectedRoleId.value) {
        result = node;
      }
    });
  });
  return result;
};


const unBindUser = (uid: string) => {
  const param = { uid, rid: selectedRoleId.value };
  loading.value = true;
  ElMessageBox.confirm(t("role.confirm_unbind_user"), {
    confirmButtonType: "danger",
    type: "warning",
    autofocus: false,
    tip: "",
    confirmButtonText: t("commons.confirm"),
    cancelButtonText: t("dataset.cancel"),
    showClose: false,
  })
    .then(() => {
      unMountUserHandler(param, () => {
        selectedSearch(selectedRoleId.value);
      });
    })
    .catch(() => {
      loading.value = false;
    });

};

const unMountUserHandler = (param: any, callback?) => {
  loading.value = true;
  unMountUserApi(param)
    .then(() => {
      ElMessage({
        message: t("role.unbind_success"),
        type: "success",
      });
      emits("refresh-grid");
      callback && callback();
    })
    .finally(() => {
      loading.value = false;
    });
};



const triggerFilterRole = () => {
  const value = roleKeyword.value;
  state.roleData.forEach((roleGroup) => {
    roleGroup.children?.forEach((data) => {
      setColorName(data, value);
      data["hidden"] = value && !data.name.includes(value);
    });
  });
};
const imgType = ref();
const emptyDesc = ref("");
const getEmptyImg = (): string => {
  if (selectedFilterkey.value) {
    return "tree";
  }
  return "noneWhite";
};

const getEmptyDesc = (): string => {
  if (selectedFilterkey.value) {
    return t("work_branch.relevant_content_found");
  }

  return "";
};
const filterSelected = () => {
  selectedSearch(selectedRoleId.value);
};
const addOrgUserDialog = ref();
const handleAddMember = () => {
  addOrgUserDialog.value.init();
};

const pageChange = (index) => {
  if (typeof index !== "number") {
    return;
  }
  state.paginationConfig.currentPage = index;
  selectedSearch(selectedRoleId.value);
};
const sizeChange = (size) => {
  state.paginationConfig.currentPage = 1;
  state.paginationConfig.pageSize = size;
  selectedSearch(selectedRoleId.value);
};
const sortChange = (param) => {
  order.value = null;
  if (param.order && param.prop === "name") {
    const type = param.order.substring(0, param.order.indexOf("ending"));
    order.value = "name " + type;
  } else {
    order.value = null;
  }
  selectedSearch(selectedRoleId.value);
};
const userAddPopper = ref(false);

const handleVisibleChange = (val: boolean) => {
  userAddPopper.value = val;
};
const refreshGrid = () => {
  selectedSearch(selectedRoleId.value);
  emits("refresh-grid");
};
const selectFirst = () => {
  nextTick(() => {
    const node = state.roleData[0].children[0];
    handleNodeClick(node);
  });
};
onMounted(() => {
  const p = new Promise((resolve, reject) => {
    roleSearch(resolve, reject);
  });
  p.then(() => {
    loading.value = false;
    selectFirst();
  }).catch(() => {
    loading.value = false;
  });
});
</script>

<template>
  <div class="role-manage" v-loading="loading">
    <div class="role-list role-height">
      <div class="title">
        <div class="text w100 flex-align-center">
          <span>{{ t("role.role_title") }}</span>
        </div>
        <el-input
          class="m16 w100"
          v-model="roleKeyword"
          clearable
          @change="triggerFilterRole"
          :placeholder="t('commons.search')"
        >
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"
                ><icon_searchOutline_outlined class="svg-icon"
              /></Icon>
            </el-icon>
          </template>
        </el-input>
      </div>
      <el-scrollbar class="role-tree-container">
        <div v-for="(roleGroup, index) in state.roleData" :key="roleGroup.id">
          <div class="role-title text flex-align-center">
            <span>{{ roleGroup.name }}</span>
            <span class="icon-span">
              <el-icon
                v-if="!roleGroup.root"
                @click="roleAdd"
                class="hover-icon"
              >
                <Icon name="icon_add_outlined"
                  ><icon_add_outlined class="svg-icon"
                /></Icon>
              </el-icon>
            </span>
          </div>
          <div
            class="list-item_primary"
            :class="{
              'de-role-hidden': role.hidden,
              'de-is-active': selectedRoleId === role.id,
            }"
            v-for="role in roleGroup.children"
            :key="role.id"
            @click.stop="handleNodeClick(role)"
          >
            <span class="flex-align-center label">
              <span v-if="role.colorName" v-html="role.colorName" />
              <span
                :title="role.name"
                style="
                  max-width: 120px;
                  text-overflow: ellipsis;
                  overflow: hidden;
                  white-space: nowrap;
                "
                v-else
                >{{ role.name }}</span
              >
              <el-tooltip
                class="box-item"
                effect="dark"
                :content="t('role.system_role')"
                placement="top"
                v-if="role.root"
              >
                <span class="mark flex-center">{{ t("role.system") }}</span>
              </el-tooltip>
              <span v-else class="de-mark flex-center">
                {{ role.typeCode === 0 ? t("role.staff") : role.typeCode === 7 ? t("role.analyst") : t("role.manager") }}
              </span>
            </span>
            <span
              v-if="!role.root"
              class="btn-list"
              :class="{ 'de-disabled-btn': role.root }"
            >
              <el-tooltip
                class="box-item"
                effect="dark"
                :content="
                  role.root ? t('role.system_role_edit_tips') : t('common.edit')
                "
                placement="top"
              >
                <el-icon @click.stop="roleEdit(role)" class="hover-icon">
                  <Icon name="icon_edit_outlined"
                    ><icon_edit_outlined class="svg-icon"
                  /></Icon>
                </el-icon>
              </el-tooltip>

              <el-tooltip
                class="box-item"
                effect="dark"
                :content="
                  role.root
                    ? t('role.system_role_del_tips')
                    : t('common.delete')
                "
                placement="top"
              >
                <el-icon @click.stop="delHandler(role)" class="hover-icon">
                  <Icon name="icon_delete-trash_outlined"
                    ><icon_deleteTrash_outlined class="svg-icon"
                  /></Icon>
                </el-icon>
              </el-tooltip>
            </span>
          </div>
          <el-divider v-if="!index" />
        </div>
      </el-scrollbar>
    </div>
    <div class="added-user-list role-height" v-if="selectedRoleId">
      <div class="user-info flex-align-center">
        <span class="text">{{ selectedRoleName }}</span>
        <span v-if="selectedRoleRoot" class="mark flex-center">{{
          t("role.system")
        }}</span>
        <el-divider direction="vertical" />
        <el-icon>
          <Icon name="icon_member_filled"
            ><icon_member_filled class="svg-icon"
          /></Icon>
        </el-icon>
        <span class="user-num">{{ state.paginationConfig.total }}</span>
      </div>
      <el-row>
        <el-col :span="12">
          
          <el-button type="primary" @click="handleAddMember">
            {{ t("org.add_member") }}
          </el-button>
        </el-col>
        <el-col :span="12" style="margin-bottom: 16px; text-align: right">
          <el-input
            style="width: 240px"
            v-model="selectedFilterkey"
            clearable
            @change="filterSelected"
            :placeholder="t('role.user_search_placeholder')"
          >
            <template #prefix>
              <el-icon>
                <Icon name="icon_search-outline_outlined"
                  ><icon_searchOutline_outlined class="svg-icon"
                /></Icon>
              </el-icon>
            </template>
          </el-input>
        </el-col>
      </el-row>
      <div class="user-table">
        <GridTable
          :pagination="state.paginationConfig"
          :table-data="state.addedUserList"
          :empty-desc="emptyDesc"
          :empty-img="imgType"
          @current-change="pageChange"
          @size-change="sizeChange"
          @sort-change="sortChange"
          :show-empty-img="!loading"
        >
          <el-table-column
            key="name"
            show-overflow-tooltip
            prop="name"
            :label="t('user.name')"
            width="150"
          >
            <template v-slot:default="scope">
              <span v-if="scope.row.colorName" v-html="scope.row.colorName" />
              <span v-else>{{ scope.row.name }}</span>
            </template>
          </el-table-column>

          <el-table-column
            prop="account"
            show-overflow-tooltip
            key="user_name"
            :label="t('user.account')"
            width="150"
          >
            <template v-slot:default="scope">
              <span
                v-if="scope.row.colorAccount"
                v-html="scope.row.colorAccount"
              />
              <span v-else>{{ scope.row.account }}</span>
            </template>
          </el-table-column>

          <el-table-column
            prop="email"
            show-overflow-tooltip
            key="email"
            :label="t('common.email')"
          />
          <el-table-column :label="t('user.state')" width="100">
            <template #default="{ row }">
              <div style="display: flex; align-items: center">
                <el-icon size="16px">
                  <Icon>
                    <icon_succeed_filled v-if="row.enable" />
                    <icon_ban_filled v-else />
                  </Icon>
                </el-icon>
                <span style="padding: 0 8px 0 8px">{{ row.enable ? t('user.enable_success') : t('user.disable_success') }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column
            width="100"
            key="_operation"
            fixed="right"
            :label="t('common.operate')"
          >
            <template #default="scope">
              <el-tooltip
                :content="t('commons.remove')"
                effect="dark"
                placement="top"
              >
                <el-button @click="unBindUser(scope.row.id)" text>
                  <template #icon>
                    <Icon name="icon_assigned_outlined"
                      ><icon_assigned_outlined class="svg-icon"
                    /></Icon>
                  </template>
                </el-button>
              </el-tooltip>
            </template>
          </el-table-column>
        </GridTable>
      </div>
    </div>
    <el-empty
      v-else-if="!loading"
      class="added-user-list role-height"
      :description="t('role.empty_description')"
    />
  </div>
  <role-form ref="roleFormRef" @saved="roleSaved" />
  <OrgUser
    ref="addOrgUserDialog"
    :rid="selectedRoleId"
    @refresh-grid="refreshGrid"
  ></OrgUser>
</template>

<style lang="less" scoped>
.role-manage {
  display: flex;
  width: 100%;
  height: 100%;

  .role-height {
    height: calc(100vh - 165px);
    overflow: auto;
    position: relative;
  }
  .role-height-option {
    height: calc(100vh - 170px);
    position: relative;
    overflow: hidden;
    .content {
      height: calc(100% - 140px);
      overflow: auto;
    }
  }

  .role-list {
    width: 269px;
    padding: 24px 7px;
    .title {
      padding: 0 17px;
    }

    .m16 {
      margin: 16px 0;
    }
    .role-tree-container {
      height: calc(100% - 112px);
      .text {
        .icon-span {
          color: var(--ed-color-primary);
          margin: 0 16px 0 auto;
          font-size: 16px;
        }
      }
      .role-title {
        color: #8d9199;
        font-family: var(--de-custom_font, "PingFang");
        font-size: 14px;
        font-style: normal;
        font-weight: 500;
        line-height: 22px;
        height: 40px;
        padding-left: 16px;
      }
      .de-role-hidden {
        display: none !important;
      }
      .de-is-active {
        background-color: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1)) !important;
        color: var(--ed-menu-active-color) !important;
      }
      .list-item_primary {
        padding: 0 16px;
        .mark {
          height: 16px;
          border-radius: 2px;
          margin-left: 8px;
          background-color: var(--ed-color-primary-33, #3370ff33);
          color: var(--ed-menu-active-color);
          font-family: var(--de-custom_font, "PingFang");
          font-size: 10px;
          font-weight: 500;
          line-height: 13px;
          padding: 0 4px;
        }
        .de-mark {
          height: 16px;
          border-radius: 2px;
          margin-left: 8px;
          background-color: rgb(232 233 233);
          color: #646a73;
          font-family: var(--de-custom_font, "PingFang");
          font-size: 10px;
          font-weight: 500;
          line-height: 13px;
          padding: 0 4px;
        }
        .btn-list {
          display: none;
        }
        &:hover {
          cursor: pointer;
          color: #1f2329;
          background: #1f23291a;
          .btn-list {
            display: flex;
            align-items: center;
          }
          .de-disabled-btn {
            i {
              color: #bbbfc4;
              cursor: not-allowed;
              background-image: none;
              background-color: var(--el-button-disabled-bg-color);
              border-color: var(--el-button-disabled-border-color);
            }
          }
        }
      }

      .ed-divider--horizontal {
        margin: 4px 13px;
        width: calc(100% - 26px);
      }
    }
  }

  .title {
    display: flex;
    justify-content: space-between;
    font-family: var(--de-custom_font, "PingFang");
    font-size: 20px;
    font-weight: 500;
    color: var(--TextPrimary, #1f2329);
    box-sizing: border-box;
    flex-wrap: wrap;
    position: sticky;
    top: 0;
    left: 24px;
    z-index: 5;
    background: white;
    &::before {
      content: "";
      width: 100%;
      height: 24px;
      top: -23px;
      position: absolute;
      z-index: 5;
      left: 0;
      background: white;
    }
  }
  .foot1 {
    display: flex;
    margin-top: 10px;
    button {
      width: 100%;
    }
  }

  .m24 {
    margin: 24px 0;
  }
  .w100 {
    width: 100%;
  }

  .added-user-list {
    flex: 1;
    border-left: 2px solid var(--MainBG, #f5f6f7);
    padding: 24px;

    .ed-input {
      width: 120px;
      height: 32px;
    }
    .user-info {
      margin-bottom: 16px;
      font-style: normal;
      font-family: var(--de-custom_font, "PingFang");
      font-size: 16px;
      .text {
        font-style: normal;
        font-weight: 500;
        line-height: 24px;
      }
      .user-num {
        color: #8d9199;
        font-weight: 400;
        line-height: 16px;
      }
      .mark {
        height: 16px;
        border-radius: 2px;
        margin-left: 8px;
        background-color: var(--ed-color-primary-33, #3370ff33);
        color: var(--ed-menu-active-color);
        font-family: var(--de-custom_font, "PingFang");
        font-size: 10px;
        font-weight: 500;
        line-height: 13px;
        padding: 0 4px;
      }
    }

    .user-table {
      height: calc(100% - 90px);
    }

    .user-list-item {
      float: left;
      width: 150px;
      height: 30px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 24px 0 0 24px;
      border: 1px solid #ccc;
      .role-remove-icon {
        display: none;
        top: 0;
        right: 0;
        width: 12px;
        height: 12px;
        color: var(--ed-color-primary);
        background: var(--ed-color-primary-33, #3370ff33);
      }
      &:hover {
        cursor: pointer;
        border-color: var(--ed-color-primary-33, #3370ff33);
        .role-remove-icon {
          display: block;
        }
      }
    }
  }

  .add-user-list {
    width: 269px;
    padding: 24px;

    .user-list-item {
      width: 100%;
      height: 30px;
      margin-bottom: 24px;
      padding-left: 24px;
      border: 1px solid #ccc;
    }
  }
}

.add-out-icon {
  cursor: pointer;
  color: var(--ed-text-color-regular);
  background-color: var(--ed-color-white);
  :hover {
    color: var(--ed-color-primary) !important;
    background: var(--ed-color-primary-33, #3370ff33) !important;
  }
}
</style>

<style lang="less">
.menu-user-add_popper {
  margin-top: -10px !important;
  .ed-popper__arrow {
    display: none;
  }
}
</style>
