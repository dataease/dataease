<script lang="ts" setup>
import mysqlDs from "@/assets/svg/mysql-ds.svg";
import oracleDs from "@/assets/svg/oracle-ds.svg";
import sqlServerDs from "@/assets/svg/sqlServer-ds.svg";
import TiDBDs from "@/assets/svg/TiDB-ds.svg";
import impalaDs from "@/assets/svg/impala-ds.svg";
import mariadbDs from "@/assets/svg/mariadb-ds.svg";
import StarRocksDs from "@/assets/svg/StarRocks-ds.svg";
import pgDs from "@/assets/svg/pg-ds.svg";
import mongoDs from "@/assets/svg/mongo-ds.svg";
import ckDs from "@/assets/svg/ck-ds.svg";
import db2Ds from "@/assets/svg/db2-ds.svg";
import redshiftDs from "@/assets/svg/redshift-ds.svg";
import APIDs from "@/assets/svg/API-ds.svg";
import ExcelDs from "@/assets/svg/Excel-ds.svg";
import dorisDs from "@/assets/svg/doris-ds.svg";
import esDs from "@/assets/svg/es-ds.svg";
import kingbaseDs from "@/assets/svg/KingBase.svg";
import ExcelRemoteDs from "@/assets/svg/Excel-remote-ds.svg";
import icon_searchOutline_outlined from "@/assets/svg/icon_search-outline_outlined.svg";
import dvFolder from "@/assets/svg/dv-folder.svg";
import dvDashboardSpine from "@/assets/svg/dv-dashboard-spine.svg";
import dvScreenSpine from "@/assets/svg/dv-screen-spine.svg";
import icon_dataset from "@/assets/svg/icon_dataset.svg";
import { ref, reactive, onMounted, nextTick } from "vue";
import request from "@/config/axios";
import { Icon } from "@/components/icon-custom";
import { ElMessage, ElMessageBox } from "element-plus-secondary";
import { useI18n } from "@/hooks/web/useI18n";
import EmptyBackground from "@/components/empty-background/src/EmptyBackground.vue";
import { VxeTable, VxeColumn, VxeColgroup } from "vxe-table";
import "vxe-table/lib/style.css";
import {
  ColumnOption,
  whileLoop,
  levelMatch,
  standaloneNode,
  getExtLevel,
  checkedStandalone,
  unCheckedStandalone,
  perChanged,
  customSort,
  getStringWidth,
  isRoleCheckboxHidden,
} from "./options";
import DynamicResourceColumn from "./DynamicResourceColumn.vue";
import {
  resourceTreeApi,
  resourcePermissionApi,
  resourcePermissionSaveApi,
  subjectTreeApi,
} from "@/api/auth";

interface PermissionRequest {
  id: string;
  type: number;
  flag: string;
  system: boolean;
  oid?: number;
}
const { t } = useI18n();
const { isSystem } = defineProps<{
  isSystem: boolean;
}>();
const activeName = ref("user");
const activeAuth = ref("resource");
/* const nickName = ref(""); */
const menukey = ref("");
const resourcekey = ref("");
const targetkey = ref("");

const selectedResourceType = ref("panel");
const selectedResourceId = ref("");
const selectedResourceOid = ref();
const selectedResourceName = ref("");
const selectedResourceIsLeaf = ref(false);
const selectedMenuId = ref("");
const resourceTreeRef = ref<any>(null);
const menuTreeRef = ref<any>(null);
const loading = ref(false);
const leftLoading = ref(false);
const noValidNode = ref(false);
const resourceList = [
  {
    id: "panel",
    name: t("auth.panel"),
  },
  { id: "screen", name: t("auth.screen") },
  { id: "dataset", name: t("auth.dataset") },
  { id: "datasource", name: t("auth.datasource") },
  { id: "data_filling", name: t("data_fill.data_fill") },
  { id: "spreadsheet", name: t("spreadsheet.title") },
];
const defaultProps = {
  children: "children",
  label: "name",
  value: "id",
  disabled: "disabled",
};
const state = reactive({
  tableData: [] as any[],
  tableColumn: [] as ColumnOption[],
  globalColumn: [] as ColumnOption[],
  treeMap: {},
  uncommitted: [] as any[],
  sourceData: {},
  resourceTreeData: [] as any[],
  resourceBaseMap: {},
});
state.globalColumn = [
  {
    type: "dataset, menu, data_filling, spreadsheet",
    label: t("auth.use"),
    weightLevel: 1,
  },
  { type: "panel, screen, datasource", label: t("auth.check"), weightLevel: 1 },
  { type: "datasource", label: t("auth.use"), weightLevel: 2 },
  {
    type: "panel, screen",
    label: t("auth.export"),
    weightLevel: 4,
    children: [
      { type: "panel, screen", label: t("common.resource"), weightLevel: 4 },
      { type: "panel, screen", label: t("data_set.view"), weightLevel: 5 },
      { type: "panel, screen", label: t("chart.details"), weightLevel: 6 },
    ],
  },
  { type: "dataset", label: t("auth.export"), weightLevel: 4 },
  {
    type: "datasource, dataset, panel, screen, data_filling, spreadsheet",
    label: t("auth.manage"),
    weightLevel: 7,
  },
  { label: t("auth.auth"), weightLevel: 9 },
];

const tableHeight = ref("100%");

const updateTableHeight = () => {
  nextTick(() => {
    const treeTableEl = document.querySelector(".resource-panel .tree-table");
    if (treeTableEl && treeTableEl.clientHeight > 0) {
      tableHeight.value = `${treeTableEl.clientHeight - 96}px`;
    }
  });
};

const activeNameChange = async (tabName) => {
  await activaNameChangeHandler(tabName);
  if (
    (selectedMenuId.value && activeAuth.value === "menu") ||
    (selectedResourceId.value && activeAuth.value === "resource")
  ) {
    loadPermission(getSubjectType());
  }
};

const activaNameChangeHandler = async (tabName) => {
  targetkey.value = "";
  const type = tabName === "user" ? 0 : 1;
  const cacheKey = `subject_${type}`;

  if (!state.treeMap[cacheKey]) {
    const res = await subjectTreeApi({ system: isSystem, type, lazy: false });
    const treeData = res.data || [];
    state.treeMap[cacheKey] = treeData;
  }
  state.tableData = state.treeMap[cacheKey];
};

const activeAuthChange = async (tabName) => {
  resourcekey.value = "";
  menukey.value = "";
  if (tabName === "menu") {
    const id = "menu";
    if (state.resourceBaseMap[id]) {
      getColumn(id);
      state.resourceTreeData = state.resourceBaseMap[id];
    } else {
      const res = await resourceTreeApi("menu", isSystem);
      getColumn(id);
      state.resourceTreeData = res.data;
      state.resourceBaseMap[id] = res.data;
    }
    activeName.value = isSystem ? "user" : "role";
  }

  if (tabName === "resource") {
    const id = selectedResourceType.value;
    getColumn(id);
    state.resourceTreeData = state.resourceBaseMap[id];
    activeName.value = "user";
  }
  await activaNameChangeHandler(activeName.value);
  selectFirstItem();
};

const findFirstValidNode = (treeData: any[], isMenu: boolean): any | null => {
  if (!treeData?.length) return null;
  const stack = [...treeData];
  while (stack.length) {
    const node = stack.shift();
    if (!node) continue;
    const isValid = isMenu ? node.leaf : !node.disabled;
    if (isValid) return node;
    if (node.children?.length) {
      stack.push(...node.children);
    }
  }
  return null;
};

const selectFirstItem = () => {
  const isMenu = activeAuth.value === "menu";

  // 菜单树：始终需要找第一个 leaf=true 的节点
  // 资源树：isSystem 时所有节点可用保持原逻辑，非 isSystem 时找第一个 disabled !== true 的节点
  const needsValidSearch = isMenu || !isSystem;

  if (needsValidSearch) {
    const validNode = findFirstValidNode(state.resourceTreeData, isMenu);
    if (!validNode) {
      noValidNode.value = true;
      return;
    }
    noValidNode.value = false;

    if (isMenu) {
      nextTick(() => {
        menuIdChange(validNode);
      });
    } else {
      nextTick(() => {
        selectedResourceId.value = validNode["id"];
        selectedResourceOid.value = validNode["oid"];
        selectedResourceName.value = validNode["name"];
        loadPermission(getSubjectType());
        resourceTreeRef?.value?.setCurrentKey(validNode["id"]);
      });
    }
    return;
  }

  // isSystem 且资源树：保持原有逻辑
  if (state.resourceTreeData?.length) {
    const node = state.resourceTreeData[0];
    nextTick(() => {
      selectedResourceId.value = node["id"];
      selectedResourceOid.value = node["oid"];
      selectedResourceName.value = node["name"];
      loadPermission(getSubjectType());
      resourceTreeRef?.value?.setCurrentKey(node["id"]);
    });
  }
};
const menuIdChange = async (data) => {
  if (!data.leaf) {
    ElMessage.warning("菜单目录不能授权，请选择具体菜单");
    let targetId = selectedMenuId.value;
    if (!targetId) {
      targetId = data.children?.[0]?.id;
    }
    menuTreeRef?.value.setCurrentKey(targetId);
    return;
  }
  const change = (id: string) => {
    if (id === selectedMenuId.value) {
      return;
    }
    selectedMenuId.value = id;
    selectedResourceOid.value = undefined;
    loadPermission(getSubjectType());
  };

  if (await uncommittedTips()) {
    change(data.id);
  } else {
    menuTreeRef?.value.setCurrentKey(selectedMenuId.value);
  }
};
const resourceIdChange = async (data) => {
  if (data.disabled) {
    ElMessage.warning("资源不可用，请选择其他资源");
    resourceTreeRef.value.setCurrentKey(selectedResourceId.value);
    return;
  }
  const change = (data) => {
    if (data.id === selectedResourceId.value) {
      return;
    }
    selectedResourceIsLeaf.value = data.leaf;
    selectedResourceId.value = data.id;
    selectedResourceOid.value = data.oid;
    selectedResourceName.value = data.name;
    getColumn(selectedResourceType.value);
    loadPermission(getSubjectType());
  };

  if (await uncommittedTips()) {
    change(data);
  } else {
    resourceTreeRef.value.setCurrentKey(selectedResourceId.value);
  }
};
const resourceTypeClick = async (id: string) => {
  const change = async (id: string) => {
    if (selectedResourceType.value === id) {
      return;
    }
    resourcekey.value = "";
    selectedResourceType.value = id;
    if (state.resourceBaseMap[id]) {
      state.resourceTreeData = state.resourceBaseMap[id];
    } else {
      const res = await resourceTreeApi(id, isSystem);
      const sortData = customSort(res.data, id);
      state.resourceTreeData = sortData;
      state.resourceBaseMap[id] = sortData;
    }
    nextTick(() => {
      selectFirstItem();
      nextTick(() => {
        getColumn(id);
      });
    });
  };
  if (await uncommittedTips()) {
    change(id);
  }
};

const hideRootRole = (typeCode: number) => {
  if (activeName.value !== "role") {
    return;
  }
  const traverse = (nodes) => {
    nodes.forEach((row) => {
      const rowTypeCode = row.attrs?.typeCode ?? row.typeCode;
      if (rowTypeCode !== null && rowTypeCode !== undefined && rowTypeCode < typeCode) {
        const stack = [row];
        while (stack.length) {
          const node = stack.pop();
          node["dataHidden"] = true;
          if (node?.children?.length) {
            node.children.forEach((kid) => stack.push(kid));
          }
        }
      } else if (row.children?.length) {
        traverse(row.children);
      }
    });
  };
  traverse(state.tableData);
};
const setRootRoleStyle = () => {
  if (activeName.value !== "role") {
    return;
  }
  // 仅在非系统模式 + 菜单权限场景下隐藏根角色
  // 根角色对菜单是隐式完整权限，不参与授权
  const shouldHideRoot = !isSystem && activeAuth.value === "menu";
  const stack = [...state.tableData];
  while (stack.length) {
    const node = stack.pop()!;
    node["dataHidden"] = false;
    if (shouldHideRoot && node.type === 1) {
      node["dataHidden"] = node.attrs?.root;
    }
    if (node?.children?.length) {
      node.children.forEach((kid: any) => stack.push(kid));
    }
  }
};
const getSubjectType = () => (activeName.value === "user" ? 0 : 1);

const xpackMenuIds = [
  "7",
  "8",
  "10",
  "13",
  "62",
  "63",
  "65",
  "21",
  "22",
  "23",
  "24",
];
const loadPermission = (type: number) => {
  resetTableData(state.tableData);
  loading.value = true;
  const param: PermissionRequest = {
    id: selectedResourceId.value,
    flag: selectedResourceType.value.toUpperCase(),
    type,
    system: !!isSystem,
    oid: activeAuth.value !== "menu" ? selectedResourceOid.value : undefined,
  };
  const isMenuAuth = activeAuth.value === "menu";
  if (isMenuAuth) {
    param["id"] = selectedMenuId.value;
    param["flag"] = "MENU";
    param["type"] = isSystem ? type : 1;
  }

  resourcePermissionApi(param).then((res) => {
    const vo = res.data;
    const permissionMap = groupPermission(vo);
    setRootRoleStyle();
    const restrictedMenuIds = ["13", "62", "63"];
    if (isMenuAuth && restrictedMenuIds.includes(selectedMenuId.value)) {
      hideRootRole(1);
    } else if (isMenuAuth && xpackMenuIds.includes(selectedMenuId.value)) {
      hideRootRole(9);
    } else if (selectedResourceId.value === "0" && type) {
      hideRootRole(7);
    }
    fillTableData(state.tableData, permissionMap, null);
    loading.value = false;
  });
};
const groupPermission = (vo) => {
  const map = new Map();
  const origins = vo.permissionOrigins;
  const permissions = vo.permissions;
  const cols = state.tableColumn;

  const buildPermissionMap = (type, list, originItem) => {
    list?.length &&
      list.forEach((item) => {
        const { id } = item;
        const originLevelobj = buildCallback(type, item, originItem);
        const obj = Object.assign({ id }, originLevelobj);
        map.set(id, obj);
      });
  };
  const buildCallback = (type: number, item, originItem) => {
    if (type === 0) {
      const originLevelobj = {};
      whileLoop(cols, true, (col) => {
        originLevelobj["level" + col.weightLevel] = {
          show: false,
          roles: new Set<string>(),
          orgs: new Set<string>(),
        };
      });
      originLevelobj["weight"] = item["weight"];
      originLevelobj["ext"] = item["ext"];
      // weight=9 时 ext 应补全为该资源类型的完整导出权限
      if (item["weight"] === 9 && !item["ext"]) {
        const extMap = { panel: 111, screen: 111, dataset: 1 };
        originLevelobj["ext"] = extMap[selectedResourceType.value] || 0;
      }
      return originLevelobj;
    } else {
      const { id } = item;
      const originLevelobj = map.get(id) || { id };

      originLevelobj["weight"] = originLevelobj["weight"] || 0;
      originLevelobj["ext"] = originLevelobj["ext"] || 0;
      whileLoop(cols, true, (col) => {
        const weightLevel = col.weightLevel;
        const temp = originLevelobj["level" + weightLevel] || {};
        const roleMatch = levelMatch(item, weightLevel);
        temp["show"] =
          temp["show"] ||
          (!levelMatch(originLevelobj, weightLevel) && roleMatch);
        if (roleMatch) {
          const extendKey =
            originItem && originItem["type"] === 2 ? "orgs" : "roles";
          const extendInfos = temp[extendKey] || new Set<string>();
          extendInfos.add(originItem["name"]);
          temp[extendKey] = extendInfos;
        }
        originLevelobj["level" + weightLevel] = temp;
      });
      return originLevelobj;
    }
  };
  buildPermissionMap(0, permissions, null);

  origins?.length &&
    origins.forEach((item) => {
      const pers = item.permissions;
      buildPermissionMap(1, pers, item);
    });
  state.uncommitted = [];
  state.sourceData = map;
  return map;
};

const fillTableData = (rows, maps, pmap) => {
  rows?.forEach((row) => {
    const temp = (maps?.get && maps.get(row.id)) || {};
    whileLoop(state.tableColumn, true, (col) => {
      const weightLevel = col.weightLevel;
      temp["value" + weightLevel] = false;
      const match = row.type < 2 && levelMatch(temp, weightLevel);
      if (match) {
        temp["value" + weightLevel] = true;
        if (pmap) {
          pmap[weightLevel] = pmap[weightLevel] || 0;
          pmap[weightLevel]++;
        }
      }
    });
    Object.assign(row, temp);
    if (row.children?.length) {
      fillTableData(row.children, maps, null);
    }
  });
};
const resetTableData = (rows) => {
  const keys: string[] = [
    "id",
    "name",
    "children",
    "readonly",
    "typeCode",
    "root",
    "account",
    "attrs",
    "type",
    "disabled",
    "hidden",
    "pid",
  ];
  rows?.length &&
    rows.forEach((item) => {
      for (const key in item) {
        if (
          Object.prototype.hasOwnProperty.call(item, key) &&
          !keys.includes(key)
        ) {
          delete item[key];
        }
      }
      if (item.children?.length) {
        resetTableData(item.children);
      }
    });
};

const save = (callback) => {
  const param = {
    permissions: state.uncommitted,
    type: getSubjectType(),
    flag: selectedResourceType.value,
  };
  const method = resourcePermissionSaveApi;
  let treeRef = resourceTreeRef.value;
  if (activeAuth.value === "menu") {
    param["type"] = isSystem ? getSubjectType() : 1;
    param["flag"] = "menu";
    treeRef = menuTreeRef.value;
  }
  const resourceList = getResourceList(treeRef);
  if (!resourceList?.length) {
    ElMessage.error(t("auth.was_not_obtained"));
    return;
  }
  param["resourceList"] = resourceList;
  loading.value = true;
  method(param).then(() => {
    ElMessage.success(t("common.save_success"));
    loadPermission(param["type"] || 0);
    callback && callback instanceof Function && callback();
    loading.value = false;
  });
};

const getResourceList = (treeRef) => {
  let node: any = null;
  if (activeAuth.value !== "menu") {
    node = treeRef.getNode(selectedResourceId.value)?.data;
  } else {
    node = treeRef.getNode(selectedMenuId.value)?.data;
  }
  if (!node) {
    return null;
  }

  if (activeAuth.value === "menu" || node.id === "0") {
    return [{ id: node.id, oid: node.oid }];
  }

  const stack = [node];
  const resourceList: { id: string; oid: string | null }[] = [];
  while (stack.length) {
    const item = stack.pop();
    resourceList.push({ id: item.id, oid: item.oid });
    if (item.children?.length) {
      item.children.forEach((kid) => stack.push(kid));
    }
  }
  return resourceList;
};

const reset = () => {
  state.uncommitted = [];
  resetTableData(state.tableData);
  fillTableData(state.tableData, state.sourceData, null);
};
const independentAuth = (row, level) => {
  row["independent" + level] = true;
  nextTick(() => {
    row["value" + level] = true;
    rowWeightChanged(row, level);
    row["independent" + level] = false;
  });
};

const cascadeCheckAll = (item) => {
  const level = item.weightLevel;
  const check = item["checkAll"];
  const realLevel = getExtLevel(level);
  const standalone = realLevel === 4;
  const checkStandalone = standaloneNode(level);
  if (check) {
    whileLoop(state.tableColumn, true, (col) => {
      if (
        level >= col.weightLevel &&
        (!checkStandalone || col.weightLevel === 1 || col.weightLevel === level)
      ) {
        col["checkAll"] = true;
      }
    });
  } else {
    whileLoop(state.tableColumn, true, (col) => {
      const curLevel = col.weightLevel;
      if (
        curLevel >= level &&
        (!standalone || curLevel === 9 || curLevel === level)
      ) {
        col["checkAll"] = false;
      }
    });
  }
};
const onCheckAll = (col) => {
  const level = col.weightLevel;
  const check = col["checkAll"];
  const isMenu = activeAuth.value === "menu";
  let stack = [...state.tableData];
  cascadeCheckAll(col);
  if (isMenu) {
    while (stack.length) {
      const row = stack.pop();
      if (row.type === 2) {
        row["value" + level] = check;
        if (row.children?.length) {
          row.children.forEach((kid: any) => stack.push(kid));
        }
        continue;
      }
      if (
        !row["dataHidden"] &&
        !row["hidden"] &&
        !row.root &&
        !(
          activeName.value === "role" &&
          isRoleCheckboxHidden(row, level, "menu")
        )
      ) {
        row["value" + level] = check;
        rowWeightChanged(row, level);
      }
      if (row.children?.length) {
        row.children.forEach((kid: any) => stack.push(kid));
      }
    }
    return;
  }
  const isRole = activeName.value === "role";
  const isRootResource = !isMenu && selectedResourceId.value === "0";
  while (stack.length) {
    const row = stack.pop();
    if (row.type === 2) {
      row["value" + level] = check;
      if (row.children?.length) {
        row.children.forEach((kid: any) => stack.push(kid));
      }
      continue;
    }
    if (
      (isRole && isRoleCheckboxHidden(row, level, activeAuth.value)) ||
      (isRootResource && level < 7) ||
      row["hidden"] ||
      row["dataHidden"] ||
      (isRole && row.attrs?.root && row['level' + level]?.show)
    ) {
      continue;
    }
    row["value" + level] = check;
    rowWeightChanged(row, level);
    if (row.children?.length) {
      row.children.forEach((kid: any) => stack.push(kid));
    }
  }
};
const rowWeightChanged = (row, level) => {
  if (row["hidden"] || row["dataHidden"]) return;
  // 根角色隐式权限不可编辑，跳过
  if (activeName.value === 'role' && row.attrs?.root && row['level' + level]?.show) return;

  if (row.type === 2) {
    const check = row["value" + level];
    if (row.children?.length) {
      for (const child of row.children) {
        if (child.type === 2) continue;
        if (child["hidden"] || child["dataHidden"]) continue;
        if (activeName.value === 'role' && isRoleCheckboxHidden(child, level, activeAuth.value)) continue;
        // 根角色隐式权限不可编辑，跳过
        if (activeName.value === 'role' && child.attrs?.root && child['level' + level]?.show) continue;
        child["value" + level] = check;
        rowWeightChanged(child, level);
      }
    }
    return;
  }

  const isRootResource =
    activeAuth.value === "resource" && selectedResourceId.value === "0";
  const check = row["value" + level];
  const checkStandalone = standaloneNode(level);
  const realLevel = getExtLevel(level);
  const standalone = realLevel === 4;
  let hasExport = false;
  if (check) {
    whileLoop(state.tableColumn, true, (col) => {
      if (
        level >= col.weightLevel &&
        (!checkStandalone || col.weightLevel === 1 || col.weightLevel === level)
      ) {
        row["value" + col.weightLevel] = true;
      }
      if (col.weightLevel === 4) {
        hasExport = true;
      }
    });
    row["weight"] = Math.max(row?.weight || 0, level);
    if (hasExport) {
      const ext = checkedStandalone(row.ext, level);
      row["ext"] = ext;
    }
  } else {
    let finalWeight = 0;
    whileLoop(state.tableColumn, true, (col) => {
      const curLevel = col.weightLevel;
      if (
        curLevel >= level &&
        (!standalone || curLevel === 9 || curLevel === level)
      ) {
        row["value" + curLevel] = false;
        // 取消授权后恢复角色/组织覆盖效果
        const levelObj = row["level" + curLevel];
        if (
          levelObj &&
          ((levelObj["roles"] && levelObj["roles"].size) ||
            (levelObj["orgs"] && levelObj["orgs"].size))
        ) {
          row["level" + curLevel]["show"] = true;
        }
      }
      if (curLevel === 4) {
        hasExport = true;
      }
    });
    whileLoop(state.tableColumn, true, (col) => {
      const curLevel = col.weightLevel;
      if (row["value" + curLevel]) {
        finalWeight = Math.max(finalWeight, curLevel);
      }
    });
    row["weight"] = getExtLevel(finalWeight);
    if (isRootResource && finalWeight < 7) {
      row["weight"] = 0;
    }
    if (hasExport) {
      row["ext"] = unCheckedStandalone(row.ext, level);
    }
  }
  let item = state.sourceData["get"](row.id);
  if (!item) {
    item = { id: row.id, weight: 0, ext: 0 };
  }

  if (perChanged(row, item)) {
    add2Uncommitted(row.id, row["weight"], row["ext"]);
  } else {
    removeFromUncommitted(row.id);
  }
};
const add2Uncommitted = (id: string, weight: number, ext?: number) => {
  const baseIdList = ["admin", "readonly"];
  if (baseIdList.includes(id)) {
    return;
  }
  let match = false;
  state.uncommitted.forEach((item) => {
    if (item.id === id) {
      item.weight = weight;
      item["ext"] = ext;
      match = true;
      return false;
    }
  });
  match || state.uncommitted.push({ id, weight, ext });
};
const removeFromUncommitted = (id) => {
  const baseIdList = ["admin", "readonly"];
  if (baseIdList.includes(id)) {
    return;
  }
  let len = state.uncommitted.length;
  if (!len) {
    return;
  }
  while (len--) {
    const item = state.uncommitted[len];
    if (item.id === id) {
      state.uncommitted.splice(len, 1);
    }
  }
};
const uncommittedTips = async () => {
  if (!state.uncommitted.length) {
    return true;
  }
  const promise = new Promise((res) => {
    ElMessageBox.confirm(t("components.sure_to_exit"), {
      confirmButtonType: "primary",
      type: "warning",
      confirmButtonText: t("commons.confirm"),
      cancelButtonText: t("dataset.cancel"),
      autofocus: false,
      showClose: false,
    })
      .then(() => {
        reset();
        res(true);
      })
      .catch(() => {
        res(false);
      });
  });
  return await promise;
};
const getColumn = (type: string) => {
  let array = state.globalColumn.filter(
    (item) => !item.type || item.type.includes(type),
  );
  if (
    activeAuth.value === "resource" &&
    (!selectedResourceId.value || selectedResourceId.value === "0")
  ) {
    array = array.filter((item) => item.weightLevel >= 7);
  }
  state.tableColumn = array;
};
const loadResourceTree = (resolve, reject) => {
  loading.value = true;
  const id = selectedResourceType.value;
  resourceTreeApi(id, isSystem)
    .then((res) => {
      getColumn(id);
      const sortData = customSort(res.data, id);
      state.resourceTreeData = sortData;
      state.resourceBaseMap[id] = sortData;
      loading.value = false;
      resolve && resolve(res);
    })
    .catch((e) => {
      loading.value = false;
      reject && reject(e);
    });
};

const filterMenu = (val) => {
  menuTreeRef.value?.filter(val);
};
const filterMenuNode = (value: string, data) => {
  if (!value) return true;
  return data.name.toLocaleLowerCase().includes(value.toLocaleLowerCase());
};
const filterResource = (val) => {
  resourceTreeRef.value?.filter(val);
};
const filterResourceNode = (value: string, data) => {
  if (!value) return true;
  return data.name.toLocaleLowerCase().includes(value.toLocaleLowerCase());
};

const dynamicResourceClass = (param) => {
  const row = param.row;
  const classes: string[] = [];
  if (row.hidden || row.dataHidden) classes.push("dynamic-resource-hidden");
  if (row.disabled) classes.push("de-row-disabled");
  return classes.join(" ");
};
const matchFilter = (row, val): boolean => {
  let match =
    !val || row.name.toLocaleLowerCase().includes(val.toLocaleLowerCase());
  if (row.children?.length) {
    for (let index = 0; index < row.children.length; index++) {
      const kid = row.children[index];
      const kidMatch = matchFilter(kid, val);
      if (kidMatch && !match) {
        match = kidMatch;
      }
    }
  }
  row.hidden = !match;
  return match;
};
const filterTarget = (val) => {
  state.tableData.forEach((item) => {
    matchFilter(item, val);
  });
};
const iconMap = {
  mysql: mysqlDs,
  oracle: oracleDs,
  sqlServer: sqlServerDs,
  TiDB: TiDBDs,
  impala: impalaDs,
  mariadb: mariadbDs,
  StarRocks: StarRocksDs,
  pg: pgDs,
  mongo: mongoDs,
  ck: ckDs,
  db2: db2Ds,
  redshift: redshiftDs,
  es: esDs,
  API: APIDs,
  Excel: ExcelDs,
  ExcelRemote: ExcelRemoteDs,
  kingbase: kingbaseDs,
  doris: dorisDs,
};
const flagIconMap = {
  1: oracleDs,
  2: sqlServerDs,
  3: TiDBDs,
  5: impalaDs,
  6: mariadbDs,
  7: StarRocksDs,
  9: pgDs,
  10: mongoDs,
  11: ckDs,
  12: db2Ds,
  13: redshiftDs,
  14: esDs,
  15: APIDs,
  16: ExcelDs,
  25: dvFolder,
  26: dorisDs,
  27: mysqlDs,
  29: ExcelRemoteDs,
  31: kingbaseDs,
};
// 数据源插件图标，从接口 /xpackComponent/dsPlugins 动态获取
const pluginIconMap = ref<Record<number, string>>({});
const loadDsPlugins = () => {
  request.get({ url: "/xpackComponent/dsPlugins" }).then(res => {
    const map = {};
    (res.data || []).forEach(item => {
      if (item.flag != null && item.icon) {
        map[Math.abs(item.flag)] = item.icon;
      }
    });
    pluginIconMap.value = map;
  }).catch(() => {});
};
loadDsPlugins();
const getDsTypeIcon = (extraFlag: number) => {
  const flag = Math.abs(extraFlag || 0);
  const coreIcon = flagIconMap[flag];
  if (coreIcon) {
    return { isPlugin: false, icon: coreIcon };
  }
  const pluginIcon = pluginIconMap.value[flag];
  if (pluginIcon) {
    return { isPlugin: true, icon: pluginIcon };
  }
  return { isPlugin: false, icon: dvFolder };
};
onMounted(() => {
  leftLoading.value = true;
  const p1 = new Promise((resolve, reject) => {
    loadResourceTree(resolve, reject);
  });
  const p2 = activaNameChangeHandler("user");
  Promise.all([p1, p2])
    .then(() => {
      loading.value = false;
      leftLoading.value = false;
      selectFirstItem();
      updateTableHeight();
    })
    .catch((e) => {
      loading.value = false;
      leftLoading.value = false;
      console.error(e);
    });

  // Observe tree-table resize to keep vxe-table height in sync
  const treeTableEl = document.querySelector(".resource-panel .tree-table");
  if (treeTableEl) {
    const observer = new ResizeObserver(() => {
      updateTableHeight();
    });
    observer.observe(treeTableEl);
  }
});

defineExpose({
  uncommittedTips,
});
</script>

<template>
  <div class="user-role" v-loading="leftLoading">
    <div class="filter-user-role">
      <el-tabs
        class="tabs-mr"
        :class="activeAuth === 'menu' && 'tabs-res'"
        v-model="activeAuth"
        @tab-change="activeAuthChange"
      >
        <el-tab-pane :label="t('auth.resource')" name="resource"></el-tab-pane>
        <el-tab-pane :label="t('auth.menu')" name="menu"></el-tab-pane>
      </el-tabs>
      <el-input
        v-if="activeAuth === 'menu'"
        class="filter-input"
        v-model="menukey"
        clearable
        :placeholder="t('auth.search_name')"
        @change="filterMenu"
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
    <el-scrollbar v-if="activeAuth === 'menu'" class="menu-tree">
      <el-tree
        menu
        ref="menuTreeRef"
        node-key="id"
        :data="state.resourceTreeData"
        :props="defaultProps"
        @node-click="menuIdChange"
        :current-node-key="selectedMenuId"
        :highlight-current="true"
        :expand-on-click-node="false"
        :default-expand-all="true"
        :filter-node-method="filterMenuNode"
      >
        <template #default="{ node, data }">
          <span
            class="custom-tree-node f18"
            :class="{ 'is-disabled': !data.leaf }"
          >
            <el-icon v-if="!data.leaf">
              <Icon name="dv-folder"><dvFolder class="svg-icon" /></Icon>
            </el-icon>
            <span
              class="label-tooltip"
              :title="data.name"
              v-html="data.colorName && menukey ? data.colorName : node.label"
            />
          </span>
        </template>
      </el-tree>
    </el-scrollbar>

    <template v-else>
      <div class="resource-type-container">
        <div
          :key="ele.name"
          v-for="ele in resourceList"
          class="list-item_primary"
          :class="{ 'is-active': selectedResourceType === ele.id }"
          @click="resourceTypeClick(ele.id)"
        >
          <span>{{ ele.name }}</span>
        </div>
      </div>
    </template>
  </div>
  <div v-if="activeAuth === 'resource'" class="resource-list">
    <el-input
      :placeholder="t('auth.search_name')"
      class="filter-input"
      v-model="resourcekey"
      clearable
      @change="filterResource"
    >
      <template #prefix>
        <el-icon>
          <Icon name="icon_search-outline_outlined"
            ><icon_searchOutline_outlined class="svg-icon"
          /></Icon>
        </el-icon>
      </template>
    </el-input>
    <el-scrollbar class="resource-tree">
      <el-tree
        menu
        ref="resourceTreeRef"
        node-key="id"
        :data="state.resourceTreeData"
        :current-node-key="selectedResourceId"
        :props="defaultProps"
        :expand-on-click-node="false"
        :highlight-current="true"
        @node-click="resourceIdChange"
        :filter-node-method="filterResourceNode"
      >
        <template #default="{ node, data }">
          <span
            class="custom-tree-node f18"
            :class="{ 'is-disabled': node.disabled || data.root }"
          >
            <el-icon v-if="!data.leaf">
              <Icon name="dv-folder"><dvFolder class="svg-icon" /></Icon>
            </el-icon>
            <el-icon v-else-if="selectedResourceType === 'panel'">
              <Icon name="dv-dashboard-spine"
                ><dvDashboardSpine class="svg-icon"
              /></Icon>
            </el-icon>
            <el-icon v-else-if="selectedResourceType === 'screen'">
              <Icon name="dv-screen-spine"
                ><dvScreenSpine class="svg-icon"
              /></Icon>
            </el-icon>
            <el-icon v-else-if="selectedResourceType === 'dataset'">
              <Icon name="icon_dataset"><icon_dataset class="svg-icon" /></Icon>
            </el-icon>
            <el-icon v-else-if="selectedResourceType === 'datasource'">
              <Icon
                :static-content="
                  getDsTypeIcon(data.extraFlag).isPlugin
                    ? getDsTypeIcon(data.extraFlag).icon
                    : undefined
                "
                ><component
                  v-if="!getDsTypeIcon(data.extraFlag).isPlugin"
                  :is="getDsTypeIcon(data.extraFlag).icon"
                  class="svg-icon"
                ></component
              ></Icon>
            </el-icon>
            <span
              class="label-tooltip"
              :title="data.name"
              v-html="
                data.colorName && resourcekey ? data.colorName : node.label
              "
            />
          </span>
        </template>
      </el-tree>
    </el-scrollbar>
  </div>
  <div
    class="resource-panel"
    :class="[{ 'menu-current': activeAuth === 'menu' }]"
    v-loading="loading"
  >
    <template v-if="noValidNode">
      <div class="no-valid-node">
        <span>缺失有效资源节点</span>
      </div>
    </template>
    <template v-else>
      <div class="tab-search">
        <el-tabs
          class="tabs-mr"
          :class="{ 'single-panel-tab': activeAuth === 'menu' }"
          v-model="activeName"
          @tab-change="activeNameChange"
        >
          <el-tab-pane
            v-if="isSystem || activeAuth === 'resource'"
            :label="t('auth.user')"
            name="user"
          ></el-tab-pane>
          <el-tab-pane
            v-if="!(isSystem && activeAuth === 'menu')"
            :label="t('auth.role')"
            name="role"
          ></el-tab-pane>
        </el-tabs>

        <div class="search-table-bt">
          <el-button
            v-if="!state.uncommitted.length"
            disabled
            @click="save"
            type="info"
            >{{ t("common.save") }}</el-button
          >
          <el-button v-else @click="save" type="primary">{{
            t("common.save")
          }}</el-button>
        </div>
      </div>
      <div class="resource-table">
        <div class="tree-table">
          <el-input
            :placeholder="t('chart.search')"
            class="search-table-input"
            v-model="targetkey"
            clearable
            @change="filterTarget"
          >
            <template #prefix>
              <el-icon>
                <Icon name="icon_search-outline_outlined"
                  ><icon_searchOutline_outlined class="svg-icon"
                /></Icon>
              </el-icon>
            </template>
          </el-input>
          <vxe-table
            ref="tableRef"
            :border="true"
            auto-resize
            :height="tableHeight"
            show-overflow="title"
            :column-config="{ resizable: true }"
            :row-config="{ keyField: 'id' }"
            :virtual-y-config="{ enabled: true, gt: 0 }"
            :tree-config="{ children: 'children', indent: 20 }"
            class="table-container ed-table--border"
            :data="state.tableData"
            style="width: 100%"
            :row-class-name="dynamicResourceClass"
            header-cell-class-name="header-cell"
          >
            <template #empty>
              <empty-background
                :description="t('data_set.no_data')"
                img-type="tree"
              />
            </template>
            <vxe-column
              field="name"
              show-overflow="tooltip"
              :title="t('common.name')"
              min-width="200"
              fixed="left"
              tree-node
            >
              <template #default="scope">
                <div v-if="scope.row.type === 2" class="de-row-disabled">
                  <span
                    v-html="
                      scope.row.colorName && targetkey
                        ? scope.row.colorName
                        : scope.row.name
                    "
                  />
                </div>
                <div
                  v-else-if="activeName === 'role'"
                  class="list-item_primary-row"
                >
                  <span class="flex-align-center label">
                    <span
                      v-html="
                        scope.row.colorName && targetkey
                          ? scope.row.colorName
                          : scope.row.name
                      "
                    />
                    <span
                      v-if="scope.row.attrs?.root"
                      class="mark flex-center"
                      >{{ t("role.system") }}</span
                    >
                    <span v-else class="de-mark flex-center">{{
                      scope.row.attrs?.typeCode === 9
                        ? t("role.manager")
                        : scope.row.attrs?.typeCode === 7
                          ? t("role.analyst")
                          : t("role.staff")
                    }}</span>
                  </span>
                </div>
                <span
                  v-else
                  class="entity-item"
                  :title="`${scope.row.name} (${scope.row.attrs?.account || ''})`"
                >
                  <span
                    v-html="
                      scope.row.colorName && targetkey
                        ? scope.row.colorName
                        : scope.row.name
                    "
                  />
                  <span class="user-account">{{
                    ` (${scope.row.attrs?.account || ""})`
                  }}</span>
                </span>
              </template>
            </vxe-column>

            <template v-for="item in state.tableColumn" :key="item.label">
              <vxe-colgroup
                v-if="item.children?.length"
                :title="item.label"
                :width="getStringWidth(item.label, 14) + 48"
                :fixed="item.weightLevel >= 7 ? 'right' : ''"
              >
                <template #header>
                  <div class="dynamic-table-header">
                    <span>{{ item.label }}</span>
                  </div>
                </template>
                <vxe-column
                  v-for="kidItem in item.children"
                  :key="kidItem.label"
                  align="center"
                  :width="getStringWidth(kidItem.label, 14) + 48"
                  :title="kidItem.label"
                >
                  <template #header>
                    <div class="dynamic-table-header">
                      <el-tooltip
                        class="box-item"
                        effect="dark"
                        :content="t('component.allSelect')"
                        placement="top"
                      >
                        <el-checkbox
                          v-model="kidItem['checkAll']"
                          @change="onCheckAll(kidItem)"
                        />
                      </el-tooltip>
                      <span>{{ kidItem.label }}</span>
                    </div>
                  </template>
                  <template #default="scope">
                    <dynamic-resource-column
                      :active-auth="activeAuth"
                      :active-name="activeName"
                      :selected-resource-id="selectedResourceId"
                      :item="kidItem"
                      :scope="scope"
                      @independent-auth="independentAuth"
                      @row-weight-changed="rowWeightChanged"
                    />
                  </template>
                </vxe-column>
              </vxe-colgroup>

              <vxe-column
                v-else
                align="center"
                :width="getStringWidth(item.label, 14) + 48"
                :title="item.label"
                :fixed="item.weightLevel >= 7 ? 'right' : ''"
              >
                <template #header>
                  <div class="dynamic-table-header">
                    <el-tooltip
                      class="box-item"
                      effect="dark"
                      :content="t('component.allSelect')"
                      placement="top"
                    >
                      <el-checkbox
                        v-model="item['checkAll']"
                        @change="onCheckAll(item)"
                      />
                    </el-tooltip>
                    <span>{{ item.label }}</span>
                  </div>
                </template>
                <template #default="scope">
                  <dynamic-resource-column
                    :active-auth="activeAuth"
                    :active-name="activeName"
                    :selected-resource-id="selectedResourceId"
                    :item="item"
                    :scope="scope"
                    @independent-auth="independentAuth"
                    @row-weight-changed="rowWeightChanged"
                  />
                </template>
              </vxe-column>
            </template>
          </vxe-table>
        </div>
      </div>
    </template>
  </div>
</template>

<style lang="less" scoped>
@import "@/style/mixin.less";

@width: 20px;
@width_table: 30px;
.user-role {
  width: 250px;
  float: left;
  height: 100%;
  padding-bottom: 24px;

  .menu-tree {
    height: calc(100% - 24px);
    width: 100%;
    padding: 16px 24px;
  }

  .filter-user-role {
    position: sticky;
    top: 0;
    left: 0;
    background: #fff;
    width: 500px;
    border-top-left-radius: 12px;

    .filter-input {
      display: none;
      margin: 16px 24px;
      width: 200px;
    }

    .tabs-res {
      max-width: 210px;
    }

    .tabs-mr {
      .border-bottom-tab(20px);
      margin-right: 20px;
    }
  }
}
.resource-panel {
  width: calc(100% - 500px);
  float: right;
  border-left: 1px solid rgba(31, 35, 41, 0.15);
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;

  .no-valid-node {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    font-size: 14px;
    color: var(--el-text-color-placeholder, #a8abb2);
  }

  .tab-search {
    height: 46px;
    flex-shrink: 0;
    position: relative;

    .search-table-bt {
      position: absolute;
      right: 25px;
      top: 7px;
    }
    .tabs-mr {
      .border-bottom-tab(30px);
      margin: 0 30px;
    }
  }
  .resource-table {
    width: 100%;
    height: calc(100% - 46px);
    .tree-table {
      width: 100%;
      float: right;
      height: 100%;
      padding: 16px 24px 24px;
      display: flex;
      flex-direction: column;
      .search-table-input {
        margin-bottom: 16px;
        flex-shrink: 0;
      }
      .table-container {
        height: calc(100% - 48px);
        flex: 1;
        min-height: 0;
        :deep(.dynamic-table-header) {
          display: flex;
          align-items: center;
          height: 23px;
          justify-content: center;
          column-gap: 4px;
        }
        :deep(.vxe-header--column) {
          border-top: none;
        }
        :deep(.vxe-body--column:last-child) {
          border-right: none;
        }
        :deep(.dynamic-row-col-head) {
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
      }
    }
  }
}

.menu-current {
  width: calc(100% - 250px);
}

.resource-list {
  border-left: 1px solid rgba(31, 35, 41, 0.15);
  float: left;
  width: 250px;
  height: 100%;
  padding: 24px;

  padding-top: 46px;
  .filter-input {
    margin-top: 16px;
  }
  .resource-tree {
    height: calc(100% - 56px);
    margin-top: 10px;
  }
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  box-sizing: content-box;
  padding-right: 4px;
  overflow: hidden;
  font-size: 14px;
  &.f18 {
    .ed-icon {
      font-size: 18px;
    }
  }
  &.is-disabled {
    color: var(--el-text-color-placeholder, #a8abb2);
    cursor: not-allowed;

    .ed-icon {
      opacity: 0.4;
    }
  }

  .label-tooltip {
    width: calc(100% - 66px);
    margin-left: 8.75px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }
}
.resource-type-container {
  font-size: 14px;
  margin-top: 16px;
  div {
    margin: 0 24px;
    display: flex;
    align-items: center;
    span {
      line-height: 22px;
      height: 22px;
    }
  }

  .is-active {
    background-color: var(--ed-color-primary-1a, #3370ff1a) !important;
    color: var(--ed-color-primary) !important;
  }
}

.role-auth-tips {
  span {
    display: block;
  }
}
.independent-auth {
  margin-left: 5px;
}
.user-role-per-checked {
  margin-right: 0;
}
.de-auth-check {
  height: 23px;
}
:deep(.dynamic-resource-hidden) {
  display: none !important;
}
.de-row-disabled {
  color: var(--el-text-color-placeholder, #a8abb2);
}
.entity-item {
  display: flex;
  column-gap: 4px;
  height: 22px;
  line-height: 22px;
  .user-account {
    color: #8f959e;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }
}
.list-item_primary-row {
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
}
.single-panel-tab {
  :deep(.ed-tabs__active-bar) {
    display: none;
  }
  :deep(.ed-tabs__item) {
    color: var(--ed-text-color-regular);
  }
}
</style>
