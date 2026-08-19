<script lang="ts" setup>
import icon_add_outlined from "@/assets/svg/icon_add_outlined.svg";
import icon_edit_outlined from "@/assets/svg/icon_edit_outlined.svg";
import icon_deleteTrash_outlined from "@/assets/svg/icon_delete-trash_outlined.svg";
import {
  reactive,
  ref,
  nextTick,
  shallowRef,
  computed,
  provide,
  toRefs,
  watch,
} from "vue";
import { GridTable } from "@/components/grid-table";
import { cloneDeep } from "lodash-es";
import { useI18n } from "@/hooks/web/useI18n";
import { ElMessage, ElMessageBox } from "element-plus-secondary";
import type { Action } from "element-plus-secondary";
import request from "@/config/axios";
import { propTypes } from "@/utils/propTypes";
import RowAuth from "./auth-tree/RowAuth.vue";
import { searchVariableApi } from "@/api/variable";
import { sysVariable } from "./auth-tree/FilterFiled.vue";
import { sysParamsIlns } from "./options.js";
import { rowPermissionsSubjectTreeApi } from "@/api/auth";

const rowPermissionList = (page: number, limit: number, datasetId: number) =>
  request.get({
    url:
      "/dataset/rowPermissions/pager/" + datasetId + "/" + page + "/" + limit,
  });

const listFieldByDatasetGroup = (datasetId: string) =>
  request.post({ url: "/datasetField/listByDatasetGroup/" + datasetId });

const saveRowPermission = (data = {}) => {
  return request.post({ url: "/dataset/rowPermissions/save", data });
};

const whiteListUsersForPermissions = (data = {}) => {
  return request.post({ url: "/dataset/rowPermissions/whiteListUsers", data });
};

const deleteRowPermission = (data = {}) => {
  return request.post({ url: "/dataset/rowPermissions/delete", data });
};

interface Pagination {
  currentPage: number;
  pageSize: number;
  total: number;
}

interface User {
  name: string;
  account: string;
  id: string;
  email: string;
}

interface RowForm {
  authTargetType: "role" | "user" | "sysParams";
  authTargetId: string;
  enable: boolean;
  datasetId: string;
  id?: string;
  whiteListUser: any[];
  whiteListRole: any[];
}

const { t } = useI18n();
const defaultForm = {
  authTargetId: "",
  authTargetType: "role",
  datasetId: "",
  whiteListUser: [],
  whiteListRole: [],
  enable: true,
  id: "",
};
const paginationConfig = reactive<Pagination>({
  currentPage: 1,
  pageSize: 10,
  total: 0,
});

const rowPermissionForm = reactive<RowForm>(
  cloneDeep({
    enable: false,
    authTargetType: "role",
    whiteListUser: [],
    whiteListRole: [],
    authTargetId: "",
    datasetId: "",
    id: "",
  }),
);

const state = reactive({
  rowList: [],
});

const props = defineProps({
  datasetId: {
    required: false,
    default: 0,
    type: String,
  },
  activeName: propTypes.string.def(""),
});

const { datasetId } = toRefs(props);

const loadingRowPermission = ref(false);
const update_row_permission = ref(false);
const update_row_permission_dialog_title = ref("");
const rowAuth = ref();
const whiteListUsers = shallowRef<User[]>([]);
const whiteListRoles = shallowRef<any[]>([]);
const whiteListUserTree = shallowRef<any[]>([]);
const whiteListRoleTree = shallowRef<any[]>([]);
const sysVariables = shallowRef<sysVariable[]>([]);
const targetObjs = shallowRef<any[]>([]);
const subjectTreeMap = reactive<Record<string, any[]>>({});
const targetObjTreeProps = {
  label: "name",
  value: "id",
  children: "children",
  disabled: "disabled",
};
const emptyTips = computed(() => {
  return (
    t("auth.select") +
    t("common.empty") +
    t(`auth.${rowPermissionForm.authTargetType}`)
  );
});
const datasetTableFiled = ref([]);

const initDatasetTableField = () => {
  listFieldByDatasetGroup(datasetId.value).then((res) => {
    datasetTableFiled.value = res.data;
  });
};

provide("filedList", datasetTableFiled);
provide("getAuthTargetType", rowPermissionForm);
provide("sys-params-list", sysVariables);

const typeList = ["role", "user", "sysParams"];

const formatter = (_, __, cellValue) => {
  return cellValue ? t(`auth.${cellValue}`) : "-";
};
const formatterWhiteList = (cellValue) => {
  return cellValue && cellValue.length !== 0
    ? cellValue.map((ele) => ele.name).join("、")
    : "-";
};

const onAuthTypeChange = () => {
  whiteListUsers.value = [];
  whiteListRoles.value = [];
  rowPermissionForm.authTargetId = "";
  fetchTypeObjsList();
  if (rowPermissionForm.authTargetType !== "sysParams") {
    rowPermissionForm.whiteListRole = [];
    changeUserList();
  }
};

const authTargetTypeLoading = ref(false);

watch(
  () => rowPermissionForm.authTargetType,
  (newValue, oldValue) => {
    if (authTargetTypeLoading.value) return;
    if ([newValue, oldValue].includes("sysParams")) {
      nextTick(() => {
        rowAuth.value.init([]);
      });
    }
    if (newValue === "sysParams") {
      fetchRole();
      fetchUser();
    }
  },
);

const changeUserList = () => {
  rowPermissionForm.whiteListUser = [];
  whiteListUsersList();
};

const confirm = () => {
  rowAuth.value.submit();
};

const whiteListUsersList = () => {
  whiteListUsers.value = [];
  const { authTargetType, authTargetId } = rowPermissionForm;
  let param = {};
  param = {
    authTargetId: authTargetId,
    section: 1,
    authTargetType: authTargetType,
    datasetId: datasetId.value,
  };
  whiteListUsersForPermissions(param).then((res) => {
    whiteListUsers.value = [] = res.data;
  });
};

const save = ({ logic, items, errorMessage }) => {
  if (errorMessage) {
    ElMessage({
      message: errorMessage,
      type: "error",
      showClose: true,
    });

    return;
  }
  loadingRowPermission.value = true;

  let params: Omit<RowForm, "whiteListUser" | "whiteListRole"> & {
    expressionTree?: string;
    whiteListUser?: string;
    whiteListRole?: string;
  } = {
    ...cloneDeep(rowPermissionForm),
    whiteListUser: JSON.stringify(rowPermissionForm.whiteListUser),
    whiteListRole: JSON.stringify(rowPermissionForm.whiteListRole),
  };
  params.expressionTree = JSON.stringify({ items, logic });
  saveRowPermission(params).then(() => {
    ElMessage.success(t("common.save_success"));
    search();
    clearData();
  });

  loadingRowPermission.value = false;
};

const clearData = () => {
  Object.assign(rowPermissionForm, cloneDeep(defaultForm));
  rowAuth.value.init({});
  update_row_permission.value = false;
};

const normalizeSubjectTree = (tree: any[] = []) => {
  return tree.reduce((result: any[], node: any) => {
    const children = Array.isArray(node.children)
      ? normalizeSubjectTree(node.children)
      : [];

    const shouldRemove = Number(node?.type) === 2 && children.length === 0;
    if (shouldRemove) {
      return result;
    }

    result.push({
      ...node,
      id: String(node.id),
      children,
      disabled: Boolean(node.disabled) || children.length > 0,
    });
    return result;
  }, []);
};

const getSubjectTreeByType = (type: number) => {
  const currentDatasetId = String(rowPermissionForm.datasetId || datasetId.value || "");
  if (!currentDatasetId || currentDatasetId === "0") {
    return Promise.resolve([]);
  }

  const cacheKey = `subject_${currentDatasetId}_${type}`;
  if (subjectTreeMap[cacheKey]) {
    return Promise.resolve(subjectTreeMap[cacheKey]);
  }

  return rowPermissionsSubjectTreeApi({
    system: false,
    datasetId: currentDatasetId,
    type,
    lazy: false,
  }).then((res) => {
    const treeData = normalizeSubjectTree(res.data || []);
    subjectTreeMap[cacheKey] = treeData;
    return treeData;
  });
};

const fetchTypeObjsList = () => {
  if (rowPermissionForm.authTargetType === "sysParams") {
    targetObjs.value = [];
    return;
  }

  const type = rowPermissionForm.authTargetType === "user" ? 0 : 1;
  getSubjectTreeByType(type).then((treeData) => {
    targetObjs.value = treeData;
  });
};

const fetchRole = () => {
  getSubjectTreeByType(1).then((treeData) => {
    whiteListRoleTree.value = treeData;
  });
};

const fetchUser = () => {
  getSubjectTreeByType(0).then((treeData) => {
    whiteListUserTree.value = treeData;
  });
};

const search = () => {
  rowPermissionList(
    paginationConfig.currentPage,
    paginationConfig.pageSize,
    datasetId.value as unknown as number,
  ).then((res) => {
    state.rowList = res.data.records;
    paginationConfig.total = res.data.total;
  });
};

const variableList = () => {
  searchVariableApi({})
    .then((res) => {
      let list: any = [];
      res.data.forEach((item) => {
        list.push({
          label: item.name,
          value: `\${` + item.id + "}",
          type: item.type,
        });
      });
      sysVariables.value = [...list, ...sysParamsIlns];
    })
    .catch(() => {});
};

variableList();

search();

initDatasetTableField();

const create = (rowPermissionObj) => {
  if (!rowPermissionObj) {
    targetObjs.value = [];
    Object.assign(rowPermissionForm, cloneDeep(defaultForm));
    rowPermissionForm.datasetId = datasetId.value;
    update_row_permission_dialog_title.value = t("dataset.row_permission.add");
  } else {
    Object.assign(rowPermissionForm, rowPermissionObj);
    update_row_permission_dialog_title.value = t("dataset.row_permission.edit");
    listRowPermissions(rowPermissionObj);
  }
  if (rowPermissionForm.authTargetType === "sysParams") {
    fetchRole();
    fetchUser();
  } else {
    whiteListUsersList();
  }
  fetchTypeObjsList();
  update_row_permission.value = true;
};

const deleteRow = (row) => {
  ElMessageBox.confirm(t("data_set.the_row_permissions"), {
    confirmButtonText: t("dataset.confirm"),
    cancelButtonText: t("dataset.cancel"),
    confirmButtonType: "danger",
    type: "warning",
    autofocus: false,
    showClose: false,
    callback: (action: Action) => {
      if (action === "confirm") {
        deleteRowPermission(row).then(() => {
          ElMessage({
            message: t("dataset.delete_success"),
            type: "success",
            showClose: true,
          });
          search();
        });
      }
    },
  });
};

const listRowPermissions = (row) => {
  const {
    whiteListUser = "[]",
    whiteListRole = "[]",
    enable = false,
    id = "",
    authTargetId,
    authTargetType,
    tree,
    datasetId,
  } = row || { enable: true };
  Object.assign(rowPermissionForm, {
    authTargetId: authTargetId ? String(authTargetId) : "",
    authTargetType,
    datasetId,
    id,
    whiteListUser: JSON.parse(whiteListUser),
    whiteListRole: JSON.parse(whiteListRole),
    enable,
  });
  if (authTargetType === "sysParams") {
    rowPermissionForm.whiteListUser = (rowPermissionForm.whiteListUser || []).map((id: any) => String(id));
    rowPermissionForm.whiteListRole = (rowPermissionForm.whiteListRole || []).map((id: any) => String(id));
  }
  authTargetTypeLoading.value = true;
  nextTick(() => {
    rowAuth.value.init(tree || {});
    authTargetTypeLoading.value = false;
  });
  loadingRowPermission.value = false;
};

const handleSizeChange = (pageSize: number) => {
  paginationConfig.currentPage = 1;
  paginationConfig.pageSize = pageSize;
  search();
};
const handleCurrentChange = (currentPage: number) => {
  paginationConfig.currentPage = currentPage;
  search();
};
</script>

<template>
  <el-button
    v-if="props.activeName === 'row'"
    class="add-row-column"
    secondary
    @click="create(null)"
  >
    <template #icon>
      <Icon name="icon_add_outlined"
        ><icon_add_outlined class="svg-icon"
      /></Icon>
    </template>
    {{ t("common.add") }}
  </el-button>
  <GridTable
    v-if="props.activeName === 'row'"
    @size-change="handleSizeChange"
    @current-change="handleCurrentChange"
    :pagination="paginationConfig"
    :table-data="state.rowList"
  >
    <el-table-column
      :formatter="formatter"
      prop="authTargetType"
      :label="t('dataset.row_permission.type')"
    >
    </el-table-column>
    <el-table-column
      prop="authTargetName"
      :label="t('deDataset.restricted_objects')"
    >
    </el-table-column>
    <el-table-column prop="whiteListUsers" :label="t('auth.white_list')">
      <template #default="{ row }">
        {{
          formatterWhiteList([
            ...(row.whiteListUsers || []),
            ...(row.whiteListRoles || []),
          ])
        }}
      </template>
    </el-table-column>
    <el-table-column
      :label="t('commons.operating')"
      fixed="right"
      key="__operation"
      width="130"
    >
      <template #default="scope">
        <el-button @click="create(scope.row)" text>
          <template #icon>
            <Icon name="icon_edit_outlined"
              ><icon_edit_outlined class="svg-icon"
            /></Icon> </template
        ></el-button>

        <el-button @click="deleteRow(scope.row)" text>
          <template #icon>
            <Icon name="icon_delete-trash_outlined"
              ><icon_deleteTrash_outlined class="svg-icon"
            /></Icon>
          </template>
        </el-button>
      </template>
    </el-table-column>
  </GridTable>
  <el-drawer
    :title="update_row_permission_dialog_title"
    modal-class="row-column-permissions"
    :wrapperClosable="false"
    :size="896"
    v-loading="loadingRowPermission"
    v-model="update_row_permission"
    direction="rtl"
    :before-close="clearData"
  >
    <div class="title-form_primary between">
      <span>{{ t("auth.row_permission") }}</span>
      <el-switch
        v-model="rowPermissionForm.enable"
        inactive-color="#BBBFC4"
        :inactive-text="t('auth.enable_row')"
      >
      </el-switch>
    </div>
    <div class="auth-type">
      <p class="type">{{ t("dataset.type") }}</p>
      <el-radio
        v-model="rowPermissionForm.authTargetType"
        @change="onAuthTypeChange"
        v-for="ele in typeList"
        :key="ele"
        :label="ele"
        >{{ t(`auth.${ele}`) }}</el-radio
      >
      <el-tree-select
        v-if="rowPermissionForm.authTargetType !== 'sysParams'"
        class="target-objs"
        :placeholder="emptyTips"
        @change="changeUserList"
        v-model="rowPermissionForm.authTargetId"
        :props="targetObjTreeProps"
        clearable
        check-strictly
        filterable
        node-key="id"
        :data="targetObjs"
      >
      </el-tree-select>
      <p class="type">{{ t("auth.set_rules") }}</p>
    </div>
    <div class="relation-tree-container">
      <RowAuth ref="rowAuth" @save="save"></RowAuth>
    </div>
    <template v-if="rowPermissionForm.authTargetType !== 'user'">
      <div class="title-form_primary m16">
        <span>{{ t("auth.white_list") }}</span>
        <span class="explain">{{ t("auth.white_user_not") }}</span>
      </div>
      <div>
        <el-tree-select
          v-if="rowPermissionForm.authTargetType === 'sysParams'"
          class="white-list"
          v-model="rowPermissionForm.whiteListUser"
          :placeholder="t('user.select_users')"
          :props="targetObjTreeProps"
          :data="whiteListUserTree"
          multiple
          show-checkbox
          check-strictly
          filterable
          clearable
          node-key="id"
        >
        </el-tree-select>
        <el-select
          v-else
          popper-class="role-add-name"
          multiple
          filterable
          clearable
          class="white-list"
          v-model="rowPermissionForm.whiteListUser"
          :placeholder="t('user.select_users')"
        >
          <el-option
            v-for="item in whiteListUsers"
            :key="item.id"
            :label="`${item.name}（${item.account}）`"
            :value="item.id"
          >
            <p class="name">{{ `${item.name}（${item.account}）` }}</p>
          </el-option>
        </el-select>
      </div>
      <div v-if="rowPermissionForm.authTargetType === 'sysParams'">
        <el-tree-select
          multiple
          show-checkbox
          check-strictly
          filterable
          clearable
          class="white-list"
          style="margin-top: 16px"
          v-model="rowPermissionForm.whiteListRole"
          :props="targetObjTreeProps"
          :data="whiteListRoleTree"
          node-key="id"
          :placeholder="`${t('auth.select') + t('common.empty') + t('auth.role')}`"
        >
        </el-tree-select>
      </div>
    </template>

    <template #footer>
      <el-button secondary @click="clearData">{{
        t("dataset.cancel")
      }}</el-button>
      <el-button type="primary" @click="confirm">{{
        t("dataset.confirm")
      }}</el-button>
    </template>
  </el-drawer>
</template>

<style lang="less" scoped>
.row-column-permissions {
  .explain {
    font-size: 12px;
    font-weight: 400;
    line-height: 20px;
    margin-left: 8px;
    color: #646a73;
  }

  .m16 {
    margin: 34px 0 16px 0;
  }

  .white-list {
    width: 100%;
    :deep(.ed-select-tags-wrapper) {
      .ed-tag {
        vertical-align: middle;
      }
    }
  }

  .auth-type {
    width: 100%;

    .ed-radio {
      height: 22px;
    }

    .type {
      margin: 8px 0 8px 0;
      font-family: var(--de-custom_font, "PingFang");
      font-size: 14px;
      font-weight: 400;
      color: #1f2329;
    }

    .target-objs {
      width: 100%;
      margin: 11px 0 8px 0;
    }
  }

  .between {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .relation-tree-container {
    border: 1px solid #e2e4e7;
    border-radius: 4px;
    max-height: 60vh;
    padding: 16px;
    overflow-x: auto;
  }
}
.role-add-name {
  .ed-select-dropdown__item {
    p {
      margin: 0;
      height: 100%;
      font-family: var(--de-custom_font, "PingFang");
      font-weight: 400;
    }

    .name {
      font-size: 14px;
      line-height: 32px;
      color: #1f2329;
    }

    .email {
      font-size: 12px;
      line-height: 20px;
      color: #8f959e;
    }
  }
}
</style>

<style lang="less">
.row-column-permissions {
  .ed-switch__label.is-active {
    color: #1f2329 !important;
  }
}
</style>
