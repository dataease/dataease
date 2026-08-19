<script lang="ts" setup>
import icon_add_outlined from "@/assets/svg/icon_add_outlined.svg";
import icon_edit_outlined from "@/assets/svg/icon_edit_outlined.svg";
import icon_deleteTrash_outlined from "@/assets/svg/icon_delete-trash_outlined.svg";
import icon_admin_outlined from "@/assets/svg/icon_admin_outlined.svg";
import field_text from "@/assets/svg/field_text.svg";
import field_time from "@/assets/svg/field_time.svg";
import field_value from "@/assets/svg/field_value.svg";
import field_location from "@/assets/svg/field_location.svg";

import {
  reactive,
  ref,
  nextTick,
  shallowRef,
  computed,
  onBeforeMount,
  watch,
  toRefs,
} from "vue";
import { GridTable } from "@/components/grid-table";
import { cloneDeep } from "lodash-es";
import { useI18n } from "@/hooks/web/useI18n";
import { ElMessage, ElMessageBox } from "element-plus-secondary";
import { fieldEnums } from "./options.js";
import type { Action } from "element-plus-secondary";
import {
  listFieldByDatasetGroup,
  whiteListUsersForPermissions,
  saveColumnPermission,
  columnPermissionList,
  deleteColumnPermission,
} from "@/api/dataset";
import { propTypes } from "@/utils/propTypes";
import { rowPermissionsSubjectTreeApi } from "@/api/auth";
interface Pagination {
  currentPage: number;
  pageSize: number;
  total: number;
}

interface CurCol {
  id?: string;
  name: string;
  deType: number;
  selected?: boolean;
  opt: "Desensitization" | "Prohibit";
  desensitizationRule: {
    m: number;
    n: number;
    customBuiltInRule: "RetainMToN" | "RetainBeforeMAndAfterN";
    builtInRule:
      | "custom"
      | "KeepMiddleThreeCharacters"
      | "KeepFirstAndLastThreeCharacters"
      | "CompleteDesensitization";
  };
}

interface User {
  nickName: string;
  userId: string;
  email: string;
}

interface Column {
  id: string;
  authTargetType: string;
  authTargetId: number;
  datasetId: string;
  updateTime: number;
  permissions: CurCol[];
  whiteListUser: number[];
  datasetName: string;
  authTargetName: string;
  authTargetIds: string;
  whiteListUsers: {
    userId: number;
    deptId: number;
    username: string;
    nickName: string;
    email: string;
  }[];
}

interface ColumnForm {
  authTargetType: "role" | "user" | "sysParams";
  authTargetId: string;
  permissions: {
    enable: boolean;
    columns: (CurCol & { selected?: boolean })[];
  };
  datasetId: string;
  id?: string;
  whiteListUser: any[];
}

const props = defineProps({
  datasetId: {
    required: false,
    default: 0,
    type: String,
  },
  activeName: propTypes.string.def(""),
});

const { datasetId } = toRefs(props);

const { t } = useI18n();

const paginationConfig = reactive<Pagination>({
  currentPage: 1,
  pageSize: 10,
  total: 0,
});
const iconMap = {
  text: field_text,
  value: field_value,
  location: field_location,
  time: field_time,
};
const defaultForm: ColumnForm = {
  authTargetId: "",
  id: "",
  authTargetType: "user",
  datasetId: "",
  permissions: {
    enable: true,
    columns: [],
  },
  whiteListUser: [],
};

const defaultCol: CurCol = {
  opt: "Prohibit",
  name: "",
  deType: 0,
  desensitizationRule: {
    m: 1,
    n: 1,
    customBuiltInRule: "RetainMToN",
    builtInRule: "custom",
  },
};
const columnPermissionForm = reactive<ColumnForm>(cloneDeep(defaultForm));

const state = reactive<{
  columnList: Column[];
  tableData: CurCol[];
}>({
  columnList: [],
  tableData: [],
});

const loadingRowPermission = ref(false);
const update_column_permission = ref(false);
const update_column_permission_dialog_title = ref("");
const colKeywords = ref("");
const whiteListUsers = shallowRef<User[]>([]);
const filedList = shallowRef([]);
const authTarge = ref(false);
const isIndeterminate = ref(false);
const curCol = reactive<CurCol>(cloneDeep(defaultCol));
const targetObjs = shallowRef<{ name: string; id: string }[]>([]);
const subjectTreeMap = reactive<Record<string, any[]>>({});
const targetObjTreeProps = {
  label: "name",
  value: "id",
  children: "children",
  disabled: "disabled",
};
const emptyTips = computed(() => {
  return (
    t("auth.select") + " " + t(`auth.${columnPermissionForm.authTargetType}`)
  );
});

const preview = computed(() => {
  return previewFormatter(curCol);
});

const previewFormatter = (val) => {
  const { customBuiltInRule = "", builtInRule = "" } =
    val?.desensitizationRule || {};
  const m = val?.desensitizationRule?.m ?? 1;
  const n = val?.desensitizationRule?.n ?? 1;
  if (builtInRule && builtInRule !== "custom") {
    let obj = optRules.find((ele) => ele.value === builtInRule);
    if (obj?.label) {
      return obj?.label;
    }
  }
  if (customBuiltInRule === "RetainMToN") {
    return [
      ...Array(m - 1).fill("*"),
      ...Array(n + 1 - m).fill("X"),
      "***",
    ].join("");
  }
  if (customBuiltInRule === "RetainBeforeMAndAfterN") {
    return [...Array(m).fill("X"), "***", ...Array(n).fill("X")].join("");
  }
  return "";
};

watch([columnPermissionForm.authTargetId], ([val]) => {
  if (val) {
    authTarge.value = false;
  }
});

const isUpdateTableData = ref(false);

watch(colKeywords, (val) => {
  const tableData = columnPermissionForm.permissions.columns || [];
  if (!val) {
    state.tableData = [...tableData];
  } else {
    state.tableData = tableData.filter((ele) => ele.name.includes(val));
  }
  isUpdateTableData.value = true;
  initSelect();
});

onBeforeMount(() => {
  defaultForm.datasetId = props.datasetId;
  initFieldLists();
  search();
});

const typeList = ["role", "user"];
const optRules = [
  {
    label: "******",
    value: "CompleteDesensitization",
  },
  {
    label: "XXX***XXX",
    value: "KeepFirstAndLastThreeCharacters",
  },
  {
    label: "***XXX***",
    value: "KeepMiddleThreeCharacters",
  },
  {
    label: t("commons.custom"),
    value: "custom",
  },
];
const regionList = [
  {
    label: t("data_set.to_nth_digits"),
    value: "RetainMToN",
  },
  {
    label: t("data_set.last_n_digits"),
    value: "RetainBeforeMAndAfterN",
  },
];

const formatter = (_, __, cellValue) => {
  return cellValue ? t(`auth.${cellValue}`) : "-";
};
const formatterWhiteListUsers = (_, __, cellValue) => {
  return cellValue && cellValue.length !== 0
    ? cellValue.map((ele) => ele.name).join("、")
    : "-";
};

const initFieldLists = () => {
  listFieldByDatasetGroup(datasetId.value).then((res) => {
    filedList.value = res.data;
  });
};

const validateAuthTarge = () => {
  authTarge.value = !columnPermissionForm.authTargetId;
  return authTarge.value;
};
const onTypeChange = (authType?: "role" | "user") => {
  authTarge.value = false;
  targetObjs.value = [];
  whiteListUsers.value = [];
  columnPermissionForm.whiteListUser = [];
  columnPermissionForm.authTargetId = "";
  const currentType = authType || columnPermissionForm.authTargetType;
  nextTick(() => {
    fetchTypeObjsList(currentType);
  });
};

const loadWhiteUserList = () => {
  whiteListUsers.value = [];
  const { authTargetType, authTargetId } = columnPermissionForm;
  let param = {};
  param = {
    authTargetId: authTargetId,
    section: 1,
    authTargetType: authTargetType,
    datasetId: datasetId.value,
  };
  whiteListUsersForPermissions(param).then((res) => {
    whiteListUsers.value = res.data || [];
  });
};

const permissionInfo = (item) => {
  const params = {
    authTargetId: item.authTargetId,
    authTargetType: item.authTargetType,
  };
};

const setDesensitizationRules = ref(false);

const search = () => {
  columnPermissionList(
    paginationConfig.currentPage,
    paginationConfig.pageSize,
    datasetId.value,
  ).then((res) => {
    const columnList = res.data.records;
    paginationConfig.total = res.data.total;
    state.columnList = columnList.map((ele) => {
      const item: Column = {
        ...ele,
        permissions: JSON.parse(ele.permissions),
        whiteListUser: JSON.parse(ele.whiteListUser),
      };
      permissionInfo(item);
      return item;
    });
    paginationConfig.total = 1;
  });
};

const create = (permissionObj) => {
  Object.assign(curCol, cloneDeep(defaultCol));
  if (!permissionObj) {
    targetObjs.value = [];
    Object.assign(columnPermissionForm, cloneDeep(defaultForm));
    columnPermissionForm.datasetId = datasetId.value;
    filedList.value.forEach((filed) => {
      columnPermissionForm.permissions.columns.push({
        id: filed.id,
        name: filed.name,
        deType: filed.deType,
        opt: "Prohibit",
        desensitizationRule: {
          builtInRule: "CompleteDesensitization",
          customBuiltInRule: "RetainMToN",
          m: 1,
          n: 1,
        },
      });
    });
    update_column_permission_dialog_title.value = t(
      "dataset.column_permission.add",
    );
  } else {
    update_column_permission_dialog_title.value = t(
      "dataset.column_permission.edit",
    );
    Object.assign(columnPermissionForm, cloneDeep(permissionObj));

    let columnsPermissions = columnPermissionForm.permissions.columns;
    columnPermissionForm.permissions.columns = [];

    for (let i = 0; i < filedList.value.length; i++) {
      let item: CurCol & { selected?: boolean } = {
        id: filedList.value[i].id,
        name: filedList.value[i].name,
        deType: filedList.value[i].deType,
        opt: "Prohibit",
        desensitizationRule: {
          builtInRule: "CompleteDesensitization",
          customBuiltInRule: "RetainMToN",
          m: 1,
          n: 1,
        },
      };
      for (let j = 0; j < columnsPermissions.length; j++) {
        if (item.id === columnsPermissions[j].id) {
          item.selected = columnsPermissions[j].selected;
          item.opt = columnsPermissions[j].opt;
          if (columnsPermissions[j].desensitizationRule !== undefined) {
            item.desensitizationRule =
              columnsPermissions[j].desensitizationRule;
          }
        }
      }
      columnPermissionForm.permissions.columns.push(item);
    }
  }
  targetObjs.value = [];
  const type = columnPermissionForm.authTargetType === "user" ? 0 : 1;
  getSubjectTreeByType(type).then((treeData) => {
    targetObjs.value = treeData;
  });
  loadWhiteUserList();
  state.tableData = [...columnPermissionForm.permissions.columns];
  update_column_permission.value = true;
  initSelect();
};

const initSelect = () => {
  isUpdateTableData.value = true;
  mapId.value = [];
  nextTick(() => {
    state.tableData.forEach((ele) => {
      if (ele.selected) {
        mapId.value.push(ele.id);
        tableDesensitization.value.toggleRowSelection(ele, true);
      }
    });
    isUpdateTableData.value = false;
  });
};

const fetchTypeObjsList = (authType?: "role" | "user") => {
  targetObjs.value = [];
  const type =
    (authType || columnPermissionForm.authTargetType) === "user" ? 0 : 1;
  getSubjectTreeByType(type).then((treeData) => {
    targetObjs.value = treeData;
  });
  columnPermissionForm.whiteListUser = [];
  loadWhiteUserList();
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
  const currentDatasetId = String(
    columnPermissionForm.datasetId || datasetId.value || "",
  );
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
const closeDialog = () => {
  update_column_permission.value = false;
  resetTaskForm();
};

const tableDesensitization = ref();

const resetTaskForm = () => {
  Object.assign(columnPermissionForm, cloneDeep(defaultForm));
  Object.assign(curCol, cloneDeep(defaultCol));
  state.tableData = [];
  isIndeterminate.value = false;
};

const deletePermission = (item) => {
  ElMessageBox.confirm(t("data_set.the_column_permissions"), {
    confirmButtonText: t("dataset.confirm"),
    cancelButtonText: t("dataset.cancel"),
    confirmButtonType: "danger",
    type: "warning",
    autofocus: false,
    showClose: false,
    callback: (action: Action) => {
      if (action === "confirm") {
        deleteColumnPermission({ id: item.id }).then(() => {
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

const changeUserList = () => {
  columnPermissionForm.whiteListUser = [];
  loadWhiteUserList();
  authTarge.value = false;
};

const save = () => {
  if (validateAuthTarge()) return;
  let params: Omit<ColumnForm, "whiteListUser" | "permissions"> & {
    permissions?: string;
    whiteListUser?: string;
  } = {
    ...cloneDeep(columnPermissionForm),
    whiteListUser: JSON.stringify(columnPermissionForm.whiteListUser),
    permissions: JSON.stringify(columnPermissionForm.permissions),
  };
  saveColumnPermission(params).then(() => {
    ElMessage({
      message: t("dataset.save_success"),
      type: "success",
      showClose: true,
    });
    search();
  });
  update_column_permission.value = false;
  resetTaskForm();
};
const selectCur = (ele) => {
  Object.assign(curCol, cloneDeep(ele));
  setDesensitizationRules.value = true;
  const { m = 1, n = 1 } = curCol.desensitizationRule;
  curCol.desensitizationRule.m = m || 1;
  curCol.desensitizationRule.n = n || 1;
};

const mapId = ref([]);

const handleSelectionChange = (val) => {
  if (isUpdateTableData.value) return;
  mapId.value = val.map((ele) => ele.id);
  state.tableData.forEach((filed) => {
    const selected = mapId.value.includes(filed.id);
    if (!selected && filed.opt === "Desensitization") {
      filed.opt = "Prohibit";
    }
    filed.selected = selected;
  });
};
const regionChange = () => {
  const { customBuiltInRule, m, n } = curCol.desensitizationRule;

  if (m === null) {
    curCol.desensitizationRule.m = n;
  }

  if (n === null) {
    curCol.desensitizationRule.n = m;
  }

  if (customBuiltInRule === "RetainMToN" && m > n && m !== null && n !== null) {
    ElMessage({
      message: t("dataset.column_permission.mgtn"),
      type: "error",
      showClose: true,
    });
    nextTick(() => {
      curCol.desensitizationRule.m = n;
    });
  }
};
const handleSizeChange = (pageSize: number) => {
  paginationConfig.currentPage = 1;
  paginationConfig.pageSize = pageSize;
  search();
};

const saveCurCol = () => {
  setDesensitizationRules.value = false;
  state.tableData.some((ele) => {
    if (ele.id === curCol.id) {
      Object.assign(ele, curCol);
      return true;
    }
    return false;
  });
};
const handleCurrentChange = (currentPage: number) => {
  paginationConfig.currentPage = currentPage;
  search();
};
</script>

<template>
  <el-button
    v-if="props.activeName === 'column'"
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
    v-if="props.activeName === 'column'"
    @size-change="handleSizeChange"
    @current-page="handleCurrentChange"
    :pagination="paginationConfig"
    :table-data="state.columnList"
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
    <el-table-column
      :formatter="formatterWhiteListUsers"
      prop="whiteListUsers"
      :label="t('auth.white_list')"
    >
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

        <el-button @click="deletePermission(scope.row)" text>
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
    :title="update_column_permission_dialog_title"
    modal-class="column-permissions"
    :wrapperClosable="false"
    :size="896"
    v-loading="loadingRowPermission"
    v-model="update_column_permission"
    direction="rtl"
    :before-close="closeDialog"
  >
    <div class="title-form_primary between">
      <span>{{ t("auth.column_permission") }}</span>
      <el-switch
        v-model="columnPermissionForm.permissions.enable"
        inactive-color="#BBBFC4"
        :inactive-text="t('auth.enable_column')"
      >
      </el-switch>
    </div>
    <div class="auth-type">
      <p class="type">{{ t("dataset.type") }}</p>
      <el-radio
        v-model="columnPermissionForm.authTargetType"
        @change="onTypeChange"
        v-for="ele in typeList"
        :key="ele"
        :label="ele"
        >{{ t(`auth.${ele}`) }}
      </el-radio>
      <el-tree-select
        v-model="columnPermissionForm.authTargetId"
        @change="changeUserList"
        :placeholder="emptyTips"
        class="select-item target-objs"
        :props="targetObjTreeProps"
        :data="targetObjs"
        node-key="id"
        check-strictly
        clearable
        filterable
      >
      </el-tree-select>
      <div
        v-if="authTarge"
        style="top: 85px; left: 0"
        class="ed-form-item__error"
      >
        {{ emptyTips }}
      </div>
      <p class="type">{{ t("auth.set_rules") }}</p>
    </div>
    <el-input
      clearable
      style="margin-bottom: 12px"
      v-model="colKeywords"
      :placeholder="t('auth.search_by_field')"
    ></el-input>
    <div class="mrbt40">
      <el-table
        :data="state.tableData"
        style="width: 100%"
        max-height="387"
        ref="tableDesensitization"
        class="table-container"
        header-cell-class-name="header-cell"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" :label="t('dataset.field_name')">
          <template v-slot:default="scope">
            <div class="flex-align-center">
              <el-icon>
                <Icon :class="`field-icon-${fieldEnums[scope.row.deType]}`">
                  <component
                    :is="iconMap[fieldEnums[scope.row.deType]]"
                    :class="`field-icon-${fieldEnums[scope.row.deType]}`"
                    class="svg-icon"
                  ></component>
                </Icon>
              </el-icon>
              &nbsp;
              {{ scope.row.name }}
            </div>
          </template>
        </el-table-column>
        <el-table-column align="center" :label="t('data_set.rule_preview')">
          <template #default="scope">
            {{
              scope.row.opt === "Prohibit" || !scope.row.selected
                ? t("data_set.prohibit_viewing")
                : previewFormatter(scope.row)
            }}
          </template>
        </el-table-column>
        <el-table-column :label="t('common.operate')" fixed="right" width="186">
          <template #default="scope">
            <div class="flex-align-center">
              <el-radio-group
                :disabled="!mapId.includes(scope.row.id)"
                v-model="scope.row.opt"
              >
                <el-radio label="Prohibit">{{
                  t("dataset.column_permission.prohibit")
                }}</el-radio>
                <el-radio label="Desensitization">{{
                  t("dataset.column_permission.desensitization")
                }}</el-radio>
              </el-radio-group>
              <el-button
                :disabled="
                  !mapId.includes(scope.row.id) || scope.row.opt === 'Prohibit'
                "
                @click="selectCur(scope.row)"
                text
              >
                <template #icon>
                  <Icon name="icon_admin_outlined"
                    ><icon_admin_outlined class="svg-icon"
                  /></Icon>
                </template>
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <template v-if="columnPermissionForm.authTargetType !== 'user'">
      <div class="title-form_primary m16">
        <span>{{ t("auth.white_list") }}</span>
        <span class="explain">{{ t("auth.white_user_not") }}</span>
      </div>
      <div>
        <el-select
          popper-class="role-add-name"
          multiple
          class="white-list"
          :placeholder="t('user.select_users')"
          clearable
          filterable
          style="width: 100%"
          v-model="columnPermissionForm.whiteListUser"
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
    </template>
    <template #footer>
      <el-button secondary @click="closeDialog">{{
        t("dataset.cancel")
      }}</el-button>
      <el-button type="primary" @click="save()"
        >{{ t("dataset.confirm") }}
      </el-button>
    </template>
  </el-drawer>
  <el-dialog
    class="set-desensitization-rules"
    :title="t('data_set.set_desensitization_rules')"
    v-model="setDesensitizationRules"
    width="420px"
  >
    <div class="border-right">
      <el-radio
        v-for="ele in optRules"
        :key="ele.label"
        v-model="curCol.desensitizationRule.builtInRule"
        :label="ele.value"
      >
        {{ ele.label }}
      </el-radio>
      <template v-if="curCol.desensitizationRule.builtInRule === 'custom'">
        <el-select
          @change="regionChange"
          v-model="curCol.desensitizationRule.customBuiltInRule"
        >
          <el-option
            :key="ele.value"
            v-for="ele in regionList"
            :label="ele.label"
            :value="ele.value"
          ></el-option>
        </el-select>
        <div class="number">
          &nbsp;M &nbsp;
          <el-input-number
            ref="numberM"
            @change="regionChange"
            v-model="curCol.desensitizationRule.m"
            controls-position="right"
            :min="1"
            step-strictly
            :max="20"
          ></el-input-number>
          &nbsp; N &nbsp;
          <el-input-number
            @change="regionChange"
            v-model="curCol.desensitizationRule.n"
            controls-position="right"
            :min="1"
            :max="20"
            step-strictly
          ></el-input-number>
        </div>
        <div class="preview">
          {{ t("dataset.preview") }} <span class="label">{{ preview }}</span>
        </div>
      </template>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="setDesensitizationRules = false"
          >{{ t("chart.cancel") }}
        </el-button>
        <el-button type="primary" @click="saveCurCol"
          >{{ t("chart.confirm") }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style lang="less" scoped>
.column-permissions {
  .m16 {
    margin-bottom: 16px;
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
    position: relative;
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

  .mrbt40 {
    margin-bottom: 40px;

    .ed-radio {
      margin-right: 16px;
    }
  }

  .between {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
}
</style>
<style lang="less">
.set-desensitization-rules {
  .border-right {
    width: 100%;
    .ed-radio {
      width: 100%;
    }

    .ed-select,
    .number,
    .preview {
      width: calc(100% - 20px);
      margin-left: 20px;
    }

    .number {
      display: flex;
      align-items: center;
      margin: 16px 0 16px 20px;
    }

    .ed-input-number {
      width: 136px;
    }

    .preview {
      color: #646a73;
      font-family: var(--de-custom_font, "PingFang");
      font-size: 14px;
      font-style: normal;
      font-weight: 400;
      line-height: 22px; /* 157.143% */
      .label {
        color: #1f2329;
      }
    }
  }
}
.column-permissions {
  .ed-switch__label.is-active {
    color: #1f2329 !important;
  }
}
</style>
