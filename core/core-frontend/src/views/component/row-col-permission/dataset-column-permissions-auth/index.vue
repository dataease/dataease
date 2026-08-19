<script lang="ts" setup>
import icon_admin_outlined from "@/assets/svg/icon_admin_outlined.svg";
import field_text from "@/assets/svg/field_text.svg";
import field_time from "@/assets/svg/field_time.svg";
import field_value from "@/assets/svg/field_value.svg";
import field_location from "@/assets/svg/field_location.svg";
import { ref, reactive, computed, nextTick, provide } from "vue";
import { useI18n } from "@/hooks/web/useI18n";
import { ElMessage } from "element-plus-secondary";
import { fieldEnums } from "../dataset-column-permissions/options.js";
import { cloneDeep } from "lodash-es";
import RowAuth from "../dataset-row-permissions/auth-tree/RowAuth.vue";
import request from "@/config/axios";

interface User {
  nickName: string;
  account: string;
  userId: string;
  email: string;
  id: string;
  name?: string;
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

const { t } = useI18n();
const iconMap = {
  text: field_text,
  value: field_value,
  location: field_location,
  time: field_time,
};
const datasetTableFiled = ref([]);
const listFieldByDatasetGroup = (datasetId: number) =>
  request.post({ url: "/datasetField/listByDatasetGroup/" + datasetId });

const whiteListUsersForPermissions = (data = {}) => {
  return request.post({ url: "/dataset/rowPermissions/whiteListUsers", data });
};

const dataSetRowPermissionInfo = (data = {}) =>
  request.post({
    url: "/dataset/rowPermissions/dataSetRowPermissionInfo",
    data,
  });

const dataSetColumnPermissionInfo = (data = {}) =>
  request.post({
    url: "/dataset/columnPermissions/info",
    data,
  });
const fetchFiledList = (datasetId) => {
  listFieldByDatasetGroup(datasetId).then((res) => {
    datasetTableFiled.value = res.data;
  });
};

const showDrawer = ref(false);
const show_row_column_permission = ref(false);
const loadingRowPermission = ref(false);
const isUpdateTableData = ref(false);
const mapId = ref<string[]>([]);
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
const tableDesensitization = ref();
const curCol = reactive<CurCol>(cloneDeep(defaultCol));
const rowAuth = ref();
const setDesensitizationRules = ref(false);
const dialogTitle = ref("");
const defaultForm = reactive({
  authTargetId: "",
  authTargetType: "",
  datasetId: "",
  whiteListUser: [],
  enable: true,
  id: 0,
});
const whiteListUsers = ref<User[]>([]);
const columnPermissionForm = reactive({
  authTargetId: "",
  id: "",
  authTargetType: "",
  datasetId: "",
  permissions: {
    enable: true,
    columns: [],
  },
  whiteListUser: [],
});
const authDetail = reactive({
  datasetId: "",
  authTargetId: "",
  authTarget: "",
  authSource: "",
  authTargetType: "",
});
provide("getAuthTargetType", authDetail);

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

const datasetPermissionsTabActive = ref("RowPermissions");

const activeText = computed(() => {
  return datasetPermissionsTabActive.value === "RowPermissions"
    ? t("auth.enable_row")
    : t("auth.enable_column");
});

const getRowPermission = () => {
  dataSetRowPermissionInfo(authDetail).then((res) => {
    const {
      whiteListUser = "[]",
      enable = false,
      id = "",
      tree,
    } = res.data || { enable: true };
    defaultForm.authTargetType = authDetail.authTargetType;
    defaultForm.authTargetId = authDetail.authTargetId;
    defaultForm.datasetId = authDetail.datasetId;
    defaultForm.whiteListUser = JSON.parse(whiteListUser);
    defaultForm.id = id;
    defaultForm.enable = enable;
    nextTick(() => {
      rowAuth.value.init(tree || {});
    });
  });
};

const getColumnPermission = () => {
  dataSetColumnPermissionInfo(authDetail).then((res) => {
    if (!res.data) {
      columnPermissionForm.authTargetType = authDetail.authTargetType;
      columnPermissionForm.authTargetId = authDetail.authTargetId;
      columnPermissionForm.datasetId = authDetail.datasetId;
      columnPermissionForm.id = "";
    } else {
      columnPermissionForm.id = "";
      Object.assign(columnPermissionForm, cloneDeep(res.data));
      columnPermissionForm.permissions = JSON.parse(res.data.permissions);
      columnPermissionForm.whiteListUser = JSON.parse(res.data.whiteListUser);
    }

    let columnsPermissions = columnPermissionForm.permissions.columns;
    columnPermissionForm.permissions.columns = [];

    for (let i = 0; i < datasetTableFiled.value.length; i++) {
      let item: CurCol & { selected?: boolean } = {
        id: datasetTableFiled.value[i].id,
        name: datasetTableFiled.value[i].name,
        deType: datasetTableFiled.value[i].deType,
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
    initSelect();
  });
};

provide("filedList", datasetTableFiled);

const init = (detail) => {
  datasetPermissionsTabActive.value = "RowPermissions";
  authDetail.authTargetType = detail.authTargetType;
  authDetail.authTargetId = detail.authTargetId;
  authDetail.datasetId = detail.datasetId;
  showDrawer.value = true;
  show_row_column_permission.value = true;

  dialogTitle.value = detail.name;
  fetchFiledList(detail.datasetId);

  provide("filedList", datasetTableFiled);
  getRowPermission();

  if (authDetail.authTargetType === "role") {
    let param = {};
    param = {
      authTargetId: detail.authTargetId,
      section: 1,
      authTargetType: detail.authTargetType,
      datasetId: detail.datasetId,
    };
    whiteListUsers.value = [];
    whiteListUsersForPermissions(param).then((res) => {
      whiteListUsers.value = res.data || [];
    });
  }
};

const clearData = () => {
  rowAuth.value.relationList = [];
  rowAuth.value.logic = "or";
  datasetPermissionsTabActive.value = "RowPermissions";
  show_row_column_permission.value = false;
  showDrawer.value = false;
};

const handleClickTabs = (tab) => {
  datasetPermissionsTabActive.value = tab.paneName;
  if (datasetPermissionsTabActive.value === "RowPermissions") return;
  getColumnPermission();
};

const emits = defineEmits(["savePermissions"]);

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
  let rowParams = JSON.parse(JSON.stringify(defaultForm));
  rowParams.expressionTree = JSON.stringify({ items, logic });
  rowParams.whiteListUser = JSON.stringify(rowParams.whiteListUser);

  emits("savePermissions", rowParams, {
    ...cloneDeep(columnPermissionForm),
    whiteListUser: JSON.stringify(columnPermissionForm.whiteListUser),
    permissions: JSON.stringify(columnPermissionForm.permissions),
  });
  showDrawer.value = false;
};

const confirm = () => {
  rowAuth.value.submit();
};

const handleSelectionChange = (val) => {
  if (isUpdateTableData.value) return;
  mapId.value = val.map((ele) => ele.id) as string[];
  columnPermissionForm.permissions.columns.forEach((filed) => {
    const selected = mapId.value.includes(filed.id as string);
    if (!selected && filed.opt === "Desensitization") {
      filed.opt = "Prohibit";
    }
    filed.selected = selected;
  });
};

const initSelect = () => {
  isUpdateTableData.value = true;
  mapId.value = [];
  nextTick(() => {
    columnPermissionForm.permissions.columns.forEach((ele) => {
      if (ele.selected) {
        mapId.value.push(ele.id);
        tableDesensitization.value.toggleRowSelection(ele, true);
      }
    });
    isUpdateTableData.value = false;
  });
};
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

const selectCur = (ele) => {
  Object.assign(curCol, cloneDeep(ele));
  setDesensitizationRules.value = true;
  const { m = 1, n = 1 } = curCol.desensitizationRule;
  curCol.desensitizationRule.m = m || 1;
  curCol.desensitizationRule.n = n || 1;
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

const saveCurCol = () => {
  setDesensitizationRules.value = false;
  columnPermissionForm.permissions.columns.some((ele) => {
    if (ele.id === curCol.id) {
      Object.assign(ele, curCol);
      return true;
    }
    return false;
  });
};

defineExpose({
  init,
});
</script>

<template>
  <el-drawer
    :title="`${dialogTitle} ${t('auth.row_column')}`"
    v-model="showDrawer"
    :before-close="clearData"
    modal-class="dataset-column-permissions-auth"
    size="896px"
    direction="rtl"
  >
    <el-tabs @tab-click="handleClickTabs" v-model="datasetPermissionsTabActive">
      <el-tab-pane :label="$t('dataset.row_permissions')" name="RowPermissions">
      </el-tab-pane>
      <el-tab-pane
        :label="$t('dataset.column_permissions')"
        name="ColumnPermissions"
      ></el-tab-pane>
    </el-tabs>
    <div class="row-rules between">
      <span>{{
        datasetPermissionsTabActive === "RowPermissions"
          ? $t("auth.row_permission")
          : $t("auth.column_permission")
      }}</span>
      <el-switch
        v-if="datasetPermissionsTabActive === 'RowPermissions'"
        v-model="defaultForm.enable"
        :active-text="activeText"
        inactive-color="#BBBFC4"
      >
      </el-switch>
      <el-switch
        v-else
        v-model="columnPermissionForm.permissions.enable"
        :active-text="activeText"
        inactive-color="#BBBFC4"
      >
      </el-switch>
    </div>
    <div
      v-show="
        datasetPermissionsTabActive === 'RowPermissions' &&
        show_row_column_permission
      "
      class="relation-tree-container"
    >
      <row-auth ref="rowAuth" @save="save"></row-auth>
    </div>
    <div
      v-show="
        datasetPermissionsTabActive === 'ColumnPermissions' &&
        show_row_column_permission
      "
      class="mrbt40"
    >
      <el-table
        :data="columnPermissionForm.permissions.columns"
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
                <Icon :class="`field-icon-${fieldEnums[scope.row.deType]}`"
                  ><component
                    :is="iconMap[fieldEnums[scope.row.deType]]"
                    :class="`field-icon-${fieldEnums[scope.row.deType]}`"
                    class="svg-icon"
                  ></component
                ></Icon>
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
                <el-radio label="Prohibit"
                  >{{ t("dataset.column_permission.prohibit") }}
                </el-radio>
                <el-radio label="Desensitization"
                  >{{ t("dataset.column_permission.desensitization") }}
                </el-radio>
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
    <template
      v-if="
        datasetPermissionsTabActive === 'RowPermissions' &&
        authDetail.authTargetType !== 'user'
      "
    >
      <div class="row-rules">
        <span>{{ $t("auth.white_list") }}</span>
        <span class="explain">{{ $t("auth.white_user_not") }}</span>
      </div>
      <div>
        <el-select
          popper-class="role-add-name"
          multiple
          clearable
          class="white-list"
          v-model="defaultForm.whiteListUser"
          :placeholder="$t('user.select_users')"
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
    <template
      v-if="
        datasetPermissionsTabActive === 'ColumnPermissions' &&
        authDetail.authTargetType !== 'user'
      "
    >
      <div class="row-rules">
        <span>{{ $t("auth.white_list") }}</span>
        <span class="explain">{{ $t("auth.white_user_not") }}</span>
      </div>
      <div>
        <el-select
          popper-class="role-add-name"
          multiple
          clearable
          class="white-list"
          v-model="columnPermissionForm.whiteListUser"
          :placeholder="$t('user.select_users')"
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
      <el-button secondary @click="clearData"
        >{{ $t("common.cancel") }}
      </el-button>
      <el-button type="primary" @click="confirm"
        >{{ $t("common.sure") }}
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
            step-strictly
            :max="20"
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
.dataset-column-permissions-auth {
  .relation-tree-container {
    border: 1px solid #e2e4e7;
    border-radius: 6px;
    max-height: 60vh;
    padding: 16px;
    overflow-x: auto;
  }

  .row-rules {
    display: flex;
    align-items: center;
    position: relative;
    font-family: var(--de-custom_font, "PingFang");
    font-size: 14px;
    font-weight: 500;
    line-height: 22px;
    padding-left: 10px;
    margin: 24px 0;

    &::before {
      content: "";
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      height: 14px;
      width: 2px;
      background: var(--ed-color-primary);
    }

    .explain {
      font-size: 12px;
      font-weight: 400;
      line-height: 20px;
      margin-left: 8px;
      color: #646a73;
    }
  }

  .white-list {
    width: 100%;
    :deep(.ed-select-tags-wrapper) {
      .ed-tag {
        vertical-align: middle;
      }
    }
  }

  .between {
    justify-content: space-between;
  }

  .relation-tree-container {
    border: 1px solid #e2e4e7;
    border-radius: 6px;
    max-height: 60vh;
    padding: 16px;
    overflow-x: auto;
  }

  .mrbt40 {
    width: 100%;
    max-height: calc(100% - 300px);
    overflow-y: auto;
    margin-bottom: 40px;
    border: 1px solid var(--deBorderBase, "#DCDFE6");
    border-radius: 6px;
    display: flex;
    justify-content: space-between;

    .name-email-search {
      width: 100%;
      margin-bottom: 24px;
    }
  }
}
</style>

<style lang="less">
.dataset-column-permissions-auth {
  .ed-switch__label.is-active {
    color: #1f2329 !important;
  }
}
</style>
