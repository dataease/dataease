<script lang="ts" setup>
import APIDs from "@/assets/svg/API-ds.svg";
import ExcelDs from "@/assets/svg/Excel-ds.svg";
import StarRocksDs from "@/assets/svg/StarRocks-ds.svg";
import TiDBDs from "@/assets/svg/TiDB-ds.svg";
import ckDs from "@/assets/svg/ck-ds.svg";
import db2Ds from "@/assets/svg/db2-ds.svg";
import dorisDs from "@/assets/svg/doris-ds.svg";
import esDs from "@/assets/svg/es-ds.svg";
import kingbaseDs from "@/assets/svg/KingBase.svg";
import ExcelRemoteDs from "@/assets/svg/Excel-remote-ds.svg";
import dvDashboardSpine from "@/assets/svg/dv-dashboard-spine.svg";
import dvFolder from "@/assets/svg/dv-folder.svg";
import dvScreenSpine from "@/assets/svg/dv-screen-spine.svg";
import icon_dataset from "@/assets/svg/icon_dataset.svg";
import icon_searchOutline_outlined from "@/assets/svg/icon_search-outline_outlined.svg";
import impalaDs from "@/assets/svg/impala-ds.svg";
import mariadbDs from "@/assets/svg/mariadb-ds.svg";
import mongoDs from "@/assets/svg/mongo-ds.svg";
import mysqlDs from "@/assets/svg/mysql-ds.svg";
import oracleDs from "@/assets/svg/oracle-ds.svg";
import pgDs from "@/assets/svg/pg-ds.svg";
import redshiftDs from "@/assets/svg/redshift-ds.svg";
import sqlServerDs from "@/assets/svg/sqlServer-ds.svg";
import EmptyBackground from "@/components/empty-background/src/EmptyBackground.vue";
import { Icon } from "@/components/icon-custom";
import { useI18n } from "@/hooks/web/useI18n";
import { ElMessage, ElMessageBox } from "element-plus-secondary";
import { computed, nextTick, onMounted, reactive, ref } from "vue";
import request from "@/config/axios";

import { VxeColgroup, VxeColumn, VxeTable } from "vxe-table";
// import 'vxe-table/styles/cssvar.scss'
import {
  resourceTreeApi,
  subjectPermissionApi,
  subjectPermissionSaveApi,
  subjectTreeApi,
} from "@/api/auth";
import "vxe-table/lib/style.css";
import DynamicColumn from "./DynamicColumn.vue";
import {
  checkedStandalone,
  ColumnOption,
  customSort,
  getExtLevel,
  getStringWidth,
  levelMatch,
  perChanged,
  standaloneNode,
  unCheckedStandalone,
  whileLoop,
} from "./options";
const { t } = useI18n();
const { isSystem } = defineProps<{
  isSystem: boolean;
}>();
const activeName = ref("user");
const activeAuth = ref("resource");
const nickName = ref("");
const resourceKeyword = ref("");
const selectedTarget = ref("");
const selectedSubjectOid = ref<number>();
const rootRole = ref(false);
const selectedResourceType = ref("panel");
const emptyDescription = ref(t("auth.loading"));
const selectedRoleTypeCode = ref(999);
const loading = ref(false);
const leftLoading = ref(false);
const subjectTreeRef = ref<any>(null);
const excludeMenuFolder = ref(true);
interface PermissionRequest {
  id: string;
  type: number;
  flag: string;
  system: boolean;
  oid?: number;
}
const activeNameChange = (tabName) => {
  rootRole.value = false;
  nickName.value = "";
  selectedTarget.value = "";
  if (!isSystem && activeAuth.value === "menu" && tabName === "user") {
    activeAuth.value = "resource";
    const id = selectedResourceType.value;
    state.tableData = state.treeMap[id] || [];
  }
  if (isSystem && activeAuth.value === "menu" && tabName === "role") {
    activeAuth.value = "resource";
    const id = selectedResourceType.value;
    state.tableData = state.treeMap[id] || [];
  }
  filterTarget("");

  loadTree(tabName === "user" ? 0 : 1);
};

const selectFirstSubject = () => {
  const childNodes = subjectTreeRef.value?.root?.childNodes || [];
  const firstLeaf = findFirstLeafNode(childNodes);
  if (firstLeaf) {
    subjectTreeRef.value?.setCurrentKey(firstLeaf.data.id);
    subjectNodeClick(firstLeaf.data);
  } else {
    emptyDescription.value = t("auth.on_the_left");
  }
};

const findFirstLeafNode = (nodes: any[]): any => {
  for (const node of nodes) {
    if (node.data.type !== 2) return node;
    if (node.childNodes?.length) {
      const found = findFirstLeafNode(node.childNodes);
      if (found) return found;
    }
  }
  return null;
};

const authActiveChange = async (tabName) => {
  resourceKeyword.value = "";
  if (tabName === "menu") {
    const id = "menu";
    if (state.treeMap[id]) {
      getColumn(id);
      state.tableData = state.treeMap[id];
    } else {
      const res = await resourceTreeApi("menu", isSystem);
      getColumn("menu");
      state.tableData = res.data;
      state.treeMap["menu"] = res.data;
    }
    selectedTarget.value && loadPermission(getSubjectType());
  }

  if (tabName === "resource") {
    /* if (selectedResourceType.value === "dataset" && rootRole.value) {
      emptyDescription.value = "";
    } */
    const id = selectedResourceType.value;
    getColumn(id);
    state.tableData = state.treeMap[id];
  }
};

const resourceTypeList = [
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
const state = reactive({
  treeData: [] as any[],
  tableData: [] as any[],
  filterTableData: [],
  tableColumn: [] as ColumnOption[],
  globalColumn: [] as ColumnOption[],
  treeMap: {},
  uncommitted: [] as any[],
  sourceData: {},
  expandedKeys: [] as any[],
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

// 选中主体节点事件
const subjectNodeClick = async (nodeData: any) => {
  if (nodeData.type === 2 || nodeData.disabled) {
    nextTick(() => subjectTreeRef.value?.setCurrentKey(selectedTarget.value));
    return;
  }

  const change = (data: any) => {
    const id = data.id;
    if (selectedTarget.value === id) return;

    if (data.type === 1) {
      rootRole.value = data.attrs?.root || false;
      selectedRoleTypeCode.value = data.attrs?.typeCode || 0;
    } else {
      rootRole.value = false;
      selectedRoleTypeCode.value = 999;
    }
    selectedTarget.value = id;
    selectedSubjectOid.value = data.pid;
    getColumn(
      activeAuth.value === "resource" ? selectedResourceType.value : "menu",
    );
    loadPermission(getSubjectType());
  };

  if (await uncommittedTips()) {
    change(nodeData);
  }
};

// 选中用户事件（兼容旧调用）
/* const targetClick = async (id: string) => {
  const change = (id: string) => {
    if (selectedTarget.value === id) return;
    selectedRoleTypeCode.value = 999;
    getColumn(
      activeAuth.value === "resource" ? selectedResourceType.value : "menu"
    );
    selectedTarget.value = id;
    loadPermission(getSubjectType());
  };
  if (await uncommittedTips()) {
    change(id);
  }
}; */

const resourceTypeClick = async (id: string) => {
  const change = async (id: string) => {
    if (selectedResourceType.value === id) {
      return;
    }
    resourceKeyword.value = "";
    selectedResourceType.value = id;
    if (state.treeMap[id]) {
      state.tableData = state.treeMap[id];
    } else {
      const res = await resourceTreeApi(id, isSystem);
      const sortData = customSort(res.data, id);
      state.tableData = sortData;
      state.treeMap[id] = sortData;
    }
    getColumn(id);
    // 如果有selectedTarget 再查权限
    if (selectedTarget.value) {
      loadPermission(getSubjectType());
    }
  };
  if (await uncommittedTips()) {
    change(id);
  }
};

const getColumn = (type: string) => {
  let array = state.globalColumn.filter(
    (item) => !item.type || item.type.includes(type),
  );
  if (selectedRoleTypeCode.value < 9 && array?.length) {
    const maxLevel = selectedRoleTypeCode.value === 0 ? 3 : 9;
    array = array.filter((item) => item.weightLevel < maxLevel);
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
      state.tableData = sortData;
      state.treeMap[id] = sortData;
      loading.value = false;
      resolve && resolve(res);
    })
    .catch((e) => {
      loading.value = false;
      reject && reject(e);
    });
};

const loadTree = async (type: number) => {
  leftLoading.value = true;
  const res = await subjectTreeApi({
    system: isSystem,
    type,
    lazy: true,
    pid: 0,
  });
  const nodes = (res.data || []).map((n: any) => ({
    ...n,
    disabled: n.type === 2,
    isLeaf: n.type !== 2,
  }));
  state.treeData = nodes;
  leftLoading.value = false;
  nextTick(() => selectFirstSubject());
};

const handleNodeExpand = (data: any) => {
  if (data.children?.length) return;

  subjectTreeApi({
    system: isSystem,
    type: activeName.value === "user" ? 0 : 1,
    lazy: true,
    pid: data.id,
  }).then((res) => {
    data.children = (res.data || []).map((n: any) => ({
      ...n,
      disabled: n.type === 2,
      isLeaf: n.type !== 2,
    }));
  });
};
const hideSysMenu = () => {
  const hiddenMenuIds = ["7"];
  if (selectedRoleTypeCode.value === 0) {
    hiddenMenuIds.push("13", "62", "63");
  }
  const traverse = (nodes) => {
    nodes.forEach((row) => {
      if (hiddenMenuIds.includes(row["id"])) {
        row["dataHidden"] = true;
        row["children"]?.forEach((kid) => {
          kid["dataHidden"] = true;
        });
      }
      if (row.children?.length) {
        traverse(row.children);
      }
    });
  };
  traverse(state.tableData);
};
const getSubjectType = () => (activeName.value === "user" ? 0 : 1);

const loadPermission = (type: number) => {
  whileLoop(state.tableColumn, true, (col) => {
    if (col["checkAll"]) {
      col["checkAll"] = false;
    }
  });
  loading.value = true;
  resetTableData(state.tableData);
  state.expandedKeys = [];
  const isMenuAuth = activeAuth.value === "menu";
  const param: PermissionRequest = {
    id: selectedTarget.value,
    flag: isMenuAuth ? "menu" : selectedResourceType.value.toUpperCase(),
    system: !!isSystem,
    type,
    oid: selectedSubjectOid.value,
  };

  subjectPermissionApi(param).then((res) => {
    const vo = formatVo(res.data);
    loading.value = false;
    emptyDescription.value = "";
    if (stopExecuteRoot(vo, type)) {
      return;
    }
    const permissionMap = groupPermission(vo);
    if (
      isMenuAuth &&
      (!selectedRoleTypeCode.value || selectedRoleTypeCode.value < 9)
    ) {
      hideSysMenu();
    }
    fillTableData(state.tableData, permissionMap);
    nextTick(() => {
      const table = tableRef.value;
      if (table && state.expandedKeys.length) {
        const expandRows: any[] = [];
        const idSet = new Set(state.expandedKeys);
        findRowsByIds(state.tableData, idSet, expandRows);
        table.clearTreeExpand().then(() => {
          if (expandRows.length) {
            table.setTreeExpand(expandRows, true);
          }
        });
      }
    });
  });
};

const formatVo = (vo: any) => {
  const origins = vo.permissionOrigins || [];
  if (origins?.length) {
    origins.forEach((origin) => {
      const { type, originFlag } = origin;
      if (type === 1) {
        if (originFlag) {
          const isRoot = originFlag.root;
          if (isRoot) {
            vo.root = true;
            vo.typeCode = Math.max(vo.typeCode || 0, originFlag.typeCode);
            vo.topRootDirId = originFlag.topRootDirId;
          }
        }
      }
    });
  }
  return vo;
};

// 是否需要停止渲染 内置根角色相关逻辑
const stopExecuteRoot = (vo, type) => {
  let needStop = false;
  if (vo?.root && type === 1 && activeAuth.value === "menu") {
    emptyDescription.value = t("auth.inner_role_tips");
    needStop = true;
  }
  if (!needStop) {
    generateInnerPermissions(vo);
  }
  return needStop;
};
/* const getRoleNameByLevel = (level: number) => {
  if (level === 9) {
    return t("role.org_admin");
  } else if (level === 7) {
    return t("role.org_analyst");
  } else {
    return t("role.average_role");
  }
} */
const findNodeById = (nodes: any[], targetId: string): any | null => {
  for (const node of nodes) {
    if (node.id === targetId) return node;
    if (node.children?.length) {
      const found = findNodeById(node.children, targetId);
      if (found) return found;
    }
  }
  return null;
};
const generateInnerPermissions = (vo) => {
  const isMenuAuth = activeAuth.value === "menu";
  const isDatasource =
    activeAuth.value !== "menu" && selectedResourceType.value === "datasource";
  const extMap = { panel: 111, screen: 111, dataset: 1 };
  const ext = extMap[selectedResourceType.value] || 0;
  if (!vo.permissionOrigins || !vo.permissionOrigins.length) {
    vo.permissionOrigins = [];
    return;
  }
  vo.permissionOrigins.forEach((origin) => {
    if (origin.type === 1 && origin.originFlag && origin.originFlag.root) {
      let innerRoleLevel = isDatasource ? 2 : 1;
      innerRoleLevel = Math.max(origin.originFlag.typeCode, innerRoleLevel);
      const id = isMenuAuth ? "menu" : selectedResourceType.value;
      const data = state.treeMap[id];
      if (data) {
        const tempOriginPermissions = [] as any[];
        let rootNodes = data;
        if (!isMenuAuth) {
          const topRootDirId = origin.originFlag.topRootDirId;
          if (topRootDirId) {
            const targetNode = findNodeById(data, topRootDirId);
            if (targetNode) {
              rootNodes = [targetNode];
            } else {
              origin.permissions = tempOriginPermissions;
              return;
            }
          }
        }
        const stack = [...rootNodes];
        while (stack.length > 0) {
          const node = stack.pop();
          const restrictedMenuIds = ["13", "62", "63"];
          if (
            isMenuAuth &&
            (node.id === "15" ||
              (node.id === "7" && origin.originFlag.typeCode !== 9) ||
              (restrictedMenuIds.includes(node.id) && origin.originFlag.typeCode === 0))
          ) {
            continue;
          }
          tempOriginPermissions.push({
            id: node.id,
            weight: innerRoleLevel,
            ext,
          });
          if (node.children?.length) {
            node.children.forEach((item) => {
              stack.push(item);
            });
          }
        }
        origin.permissions = tempOriginPermissions;
      }
    }
  });
};

const groupPermission = (vo) => {
  const map = new Map();
  const expandedKeys = new Set<string>();
  const origins = vo.permissionOrigins;
  const permissions = vo.permissions;
  const cols = state.tableColumn;

  const buildPermissionMap = (type, list, originItem) => {
    list?.length &&
      list.forEach((item) => {
        const { id, weight } = item;
        const originLevelobj = buildCallback(type, item, originItem);
        const obj = Object.assign({ id }, originLevelobj);
        map.set(id, obj);
        if (weight) {
          expandedKeys.add(id);
        }
      });
  };
  const buildCallback = (type: number, item, originItem: string) => {
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
          const extendKey = originItem["type"] === 2 ? "orgs" : "roles";
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
  expandNodes(Array.from(expandedKeys));
  return map;
};

const expandNodes = (ids: string[]) => {
  const datalist = state.tableData;
  let result: string[] = [];
  const match = (list, targetids, parentlist) => {
    if (!targetids?.length) return;
    for (let i = 0; i < list.length; i++) {
      const item = list[i];
      if (targetids.includes(item.id)) {
        targetids = targetids.filter((id) => id !== item.id);
        result = [...result, ...parentlist];
      }

      if (item.children?.length) {
        parentlist.push(item.id);
        match(item.children, targetids, parentlist);
        const len = parentlist.length;
        len && parentlist.splice(len - 1, 1);
      }
    }
  };
  match(datalist, ids, []);
  state.expandedKeys = Array.from(new Set([...result]));
};

const findRowsByIds = (nodes: any[], ids: Set<string>, result: any[]) => {
  nodes.forEach((node) => {
    if (ids.has(node.id)) {
      result.push(node);
    }
    if (node.children?.length) {
      findRowsByIds(node.children, ids, result);
    }
  });
};

const fillTableData = (rows, maps) => {
  rows?.forEach((row) => {
    const temp = (maps?.get && maps.get(row.id)) || {};
    whileLoop(state.tableColumn, true, (col) => {
      const weightLevel = col.weightLevel;
      temp["value" + weightLevel] = false;
      const match = levelMatch(temp, weightLevel);
      temp["value" + weightLevel] = match;
    });
    Object.assign(row, temp);
    if (row.children?.length) {
      fillTableData(row.children, maps);
    }
  });
};
const onCheckAll = (col) => {
  const level = col.weightLevel;
  const check = col["checkAll"];
  const isMenu = activeAuth.value === "menu";
  const withRootLimit = level >= 7;
  let stack = [...state.tableData];
  cascadeCheckAll(col);
  if (isMenu) {
    while (stack.length) {
      const row = stack.pop();
      if (!row["hidden"] && !row["dataHidden"] && !row["disabled"] &&
          !(rootRole.value && row['level' + level]?.show)) {
        row["value" + level] = check;
        rowWeightChanged(row, level);
      }
    }
    return;
  }
  while (stack.length) {
    const row = stack.pop();
    if (
      (withRootLimit ||
        row.id !== "0" ||
        (!check && !withRootLimit && row.id === "0")) &&
      !row["hidden"] &&
      !row["dataHidden"] &&
      !row["disabled"] &&
      !(rootRole.value && row['level' + level]?.show)
    ) {
      row["value" + level] = check;
      rowWeightChanged(row, level);
    }
    if ((row.id === "0" || row.disabled) && row.children?.length) {
      row.children?.forEach((kid) => stack.push(kid));
    }
  }
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
const independentAuth = (row, level) => {
  row["independent" + level] = true;
  nextTick(() => {
    row["value" + level] = true;
    rowWeightChanged(row, level);
    row["independent" + level] = false;
  });
};
const rowWeightChanged = (row, level) => {
  if (row["hidden"] || row["dataHidden"]) return;
  // 根角色隐式权限不可编辑，跳过
  if (rootRole.value && row['level' + level]?.show) return;
  if (row["disabled"]) {
    childrenWeightChanged(row, level);
    return;
  }
  if (excludeMenuFolder.value && activeAuth.value === "menu" && !row.leaf) {
    childrenWeightChanged(row, level);
    return;
  }
  const isRootResource = activeAuth.value === "resource" && row["id"] === "0";
  const check = row["value" + level];
  const realLevel = getExtLevel(level);
  const standalone = realLevel === 4;
  const checkStandalone = standaloneNode(level);
  let hasExport = false;
  if (check) {
    // 如果是独立节点 勾选自己以及1节点；否则勾选小于等于当前权重的节点
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
    row["weight"] = getExtLevel(Math.max(row?.weight || 0, level));
    if (hasExport) {
      const ext = checkedStandalone(row.ext, level);
      row["ext"] = ext;
    }
  } else {
    let finalWeight = 0;
    // 如果是独立节点 取消自己以及9节点；否则取消大于等于当前权重的节点
    whileLoop(state.tableColumn, true, (col) => {
      const curLevel = col.weightLevel;
      if (
        curLevel >= level &&
        (!standalone || curLevel === 9 || curLevel === level)
      ) {
        row["value" + curLevel] = false;
        // 下面3行用作取消用户授权之后 恢复角色覆盖效果
        const levelObj = row["level" + curLevel];
        if (levelObj && levelObj["roles"] && levelObj["roles"].size) {
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
      row["ext"] = 0;
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
    add2Uncommitted(row.id, row["weight"], row["ext"], row["oid"]);
  } else {
    removeFromUncommitted(row.id);
  }

  childrenWeightChanged(row, level);
};

const childrenWeightChanged = (row, level) => {
  const check = row["value" + level];
  if (row.children?.length && row.id !== "0") {
    row.children.forEach((item) => {
      // 根角色隐式权限不可编辑，跳过
      if (rootRole.value && item['level' + level]?.show) return;
      item["value" + level] = check;
      rowWeightChanged(item, level);
    });
  }
};
const add2Uncommitted = (
  id: string,
  weight: number,
  ext?: number,
  oid?: number,
) => {
  let match = false;
  state.uncommitted.forEach((item) => {
    if (item["id"] === id) {
      item["weight"] = weight;
      item["ext"] = ext;
      item["oid"] = oid;
      match = true;
      return false;
    }
  });
  match || state.uncommitted.push({ id, weight, ext, oid });
};
const removeFromUncommitted = (id) => {
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
const save = (callback) => {
  loading.value = true;
  const param = {
    permissions: state.uncommitted,
    id: selectedTarget.value,
    flag: selectedResourceType.value,
    type: getSubjectType(),
  };
  const method = subjectPermissionSaveApi;
  if (activeAuth.value === "menu") {
    param["type"] = isSystem ? getSubjectType() : 1;
    param["flag"] = "menu";
  }
  method(param).then(() => {
    ElMessage.success(t("common.save_success"));
    loadPermission(param["type"] || 0);
    callback && callback instanceof Function && callback();
    loading.value = false;
  });
};

const reset = () => {
  state.uncommitted = [];
  resetTableData(state.tableData);
  fillTableData(state.tableData, state.sourceData);
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

const beforeActiveNameChange = (newName, oldName) => {
  if (newName !== oldName) {
    return uncommittedTips();
  }
  return true;
};
const beforeActiveAuthChange = (newName, oldName) => {
  if (newName !== oldName) {
    return uncommittedTips();
  }
  return true;
};

const resetTableData = (rows) => {
  const keys: string[] = [
    "id",
    "name",
    "children",
    "leaf",
    "extraFlag",
    "oid",
    "disabled",
    "type",
    "attrs",
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
const filterTarget = (val) => {
  subjectTreeRef.value?.filter(val);
};

const filterSubjectNode = (value: string, data: any) => {
  if (!value) return true;
  if (data.type === 2) return false;
  return data.name?.toLocaleLowerCase().includes(value.toLocaleLowerCase());
};

const dynamicResourceClass = (param) => {
  const row = param.row;
  const classes: string[] = [];
  if (row.hidden || row.dataHidden) {
    classes.push("dynamic-resource-hidden");
  }
  if (activeAuth.value === "resource" && row.disabled) {
    classes.push("de-row-disabled");
  }
  if (activeAuth.value === "menu" && !row.leaf) {
    classes.push("de-row-disabled");
  }
  return classes.join(" ");
};

const matchNum = ref(1);

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
  matchNum.value += !match ? 0 : 1;
  row.hidden = !match;
  return match;
};

const resourceFilter = (val) => {
  matchNum.value = 0;
  state.tableData.forEach((item) => {
    matchFilter(item, val);
  });
};
/* const selectFirstItem = () => {
  if (state.userList?.length) {
    const uid = state.userList[0]["id"];
    targetClick(uid);
  }
}; */
/* const dynamicWidth = computed(() => {
  const dynamic = getStringWidth(t('dataset.row_column_permissions'), 14) + 36
  return Math.min(dynamic, 160)
}) */
/* const showEdit = computed(() => {
  return (
    selectedResourceType.value === "dataset" && activeAuth.value !== "menu"
  );
}); */
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

/* const expandChange = (params) => {
  const { row, expanded } = params;
  if (!expanded) {
    const index = state.expandedKeys.findIndex((item) => item === row["id"]);
    if (index !== -1) {
      state.expandedKeys.splice(index, 1);
    }
  }
  return true;
}; */
onMounted(() => {
  leftLoading.value = true;
  loadTree(0);
  const p1 = new Promise((resolve, reject) => {
    loadResourceTree(resolve, reject);
  });
  Promise.all([p1])
    .then(() => {
      loading.value = false;
    })
    .catch(() => {
      loading.value = false;
      leftLoading.value = false;
    });
});
const tableRef = ref();

const tableHeight = computed(() => {
  const treeTableEl = document.querySelector(".tree-table");
  if (treeTableEl) {
    return `${treeTableEl.clientHeight - 96}px`;
  }
  return "100%";
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
        v-model="activeName"
        @tab-change="activeNameChange"
        :before-leave="beforeActiveNameChange"
      >
        <el-tab-pane :label="t('auth.user')" name="user"></el-tab-pane>
        <el-tab-pane :label="t('auth.role')" name="role"></el-tab-pane>
      </el-tabs>
      <el-input
        :placeholder="t('chart.search')"
        class="filter-input"
        v-model="nickName"
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
    </div>
    <el-scrollbar
      class="subject-tree-container"
      v-if="activeName === 'user' || activeName === 'role'"
    >
      <el-tree
        ref="subjectTreeRef"
        :data="state.treeData"
        node-key="id"
        lazy
        :props="{
          children: 'children',
          label: 'name',
          disabled: 'disabled',
          isLeaf: 'isLeaf',
        }"
        :highlight-current="true"
        :expand-on-click-node="false"
        :filter-node-method="filterSubjectNode"
        @node-expand="handleNodeExpand"
        @node-click="subjectNodeClick"
      >
        <template #default="{ node, data }">
          <span
            class="subject-tree-node"
            :class="{
              'is-disabled': data.type === 2,
              'is-active': selectedTarget === data.id,
            }"
          >
            <span
              v-if="data.type === 0"
              class="entity-item"
              :title="data.name + ' (' + (data.attrs?.account || '') + ')'"
            >
              <span v-html="data.name" />
              <span class="user-account"> ({{ data.attrs?.account }})</span>
            </span>
            <span
              v-else-if="data.type === 1"
              class="flex-align-center label role-item"
            >
              <span v-html="data.name" />
              <!-- <el-tooltip
                class="box-item"
                effect="dark"
                :content="t('auth.inner_role_tips')"
                placement="top"
                v-if="data.attrs?.root"
              >
                <span class="mark flex-center">{{ t("role.system") }}</span>
              </el-tooltip> -->
              <span v-if="data.attrs?.root" class="mark flex-center">{{
                t("role.system")
              }}</span>
              <span v-else class="de-mark flex-center">{{
                data.attrs?.typeCode === 9
                  ? t("role.manager")
                  : data.attrs?.typeCode === 7
                    ? t("role.analyst")
                    : t("role.staff")
              }}</span>
            </span>
            <span v-else class="label-tooltip" :title="data.name">
              <span v-html="data.name" />
            </span>
          </span>
        </template>
      </el-tree>
    </el-scrollbar>
  </div>
  <div class="resource-panel" v-loading="loading">
    <div class="tab-search">
      <el-tabs
        class="tabs-mr"
        :class="{
          'single-panel-tab': !(
            (isSystem && activeName === 'user') ||
            (!isSystem && activeName === 'role')
          ),
        }"
        v-model="activeAuth"
        @tab-change="authActiveChange"
        :before-leave="beforeActiveAuthChange"
      >
        <el-tab-pane :label="t('auth.resource')" name="resource"></el-tab-pane>
        <el-tab-pane
          v-if="
            (isSystem && activeName === 'user') ||
            (!isSystem && activeName === 'role')
          "
          :label="t('auth.menu')"
          name="menu"
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
      <div class="resource-type" v-if="activeAuth === 'resource'">
        <div
          :key="ele.name"
          v-for="ele in resourceTypeList"
          class="list-item_primary"
          :class="{ 'is-active': selectedResourceType === ele.id }"
          @click="resourceTypeClick(ele.id)"
        >
          <span class="resource-type-item">{{ ele.name }}</span>
        </div>
      </div>
      <div
        class="tree-table"
        :class="{ 'full-tree-table': activeAuth === 'menu' }"
      >
        <empty-background
          v-if="emptyDescription"
          :description="emptyDescription"
          img-type="noneWhite"
        />

        <el-input
          v-else
          class="search-table-input"
          v-model="resourceKeyword"
          clearable
          :placeholder="t('auth.search_name')"
          @change="resourceFilter"
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
          :tree-config="{
            children: 'children',
            indent: 20,
            expandRowKeys: state.expandedKeys,
            /* toggleMethod: expandChange */
          }"
          class="table-container ed-table--border"
          v-if="!emptyDescription"
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
            :title="
              activeAuth === 'menu'
                ? t('auth.menu_name')
                : t('auth.resource_name')
            "
            min-width="200"
            fixed="left"
            tree-node
          >
            <template v-slot:default="scope">
              <span class="custom-tree-node f18">
                <el-icon v-if="!scope.row.leaf">
                  <Icon name="dv-folder"><dvFolder class="svg-icon" /></Icon>
                </el-icon>

                <el-icon
                  v-else-if="
                    activeAuth === 'resource' &&
                    selectedResourceType === 'panel'
                  "
                >
                  <Icon name="dv-dashboard-spine"
                    ><dvDashboardSpine class="svg-icon"
                  /></Icon>
                </el-icon>
                <el-icon
                  v-else-if="
                    activeAuth === 'resource' &&
                    selectedResourceType === 'screen'
                  "
                >
                  <Icon name="dv-screen-spine"
                    ><dvScreenSpine class="svg-icon"
                  /></Icon>
                </el-icon>
                <el-icon
                  v-else-if="
                    activeAuth === 'resource' &&
                    selectedResourceType === 'dataset'
                  "
                >
                  <Icon name="icon_dataset"
                    ><icon_dataset class="svg-icon"
                  /></Icon>
                </el-icon>
                <el-icon
                  v-else-if="
                    activeAuth === 'resource' &&
                    selectedResourceType === 'datasource'
                  "
                >
                  <Icon
                    :static-content="
                      getDsTypeIcon(scope.row.extraFlag).isPlugin
                        ? getDsTypeIcon(scope.row.extraFlag).icon
                        : undefined
                    "
                    ><component
                      v-if="!getDsTypeIcon(scope.row.extraFlag).isPlugin"
                      :is="getDsTypeIcon(scope.row.extraFlag).icon"
                      class="svg-icon"
                    ></component
                  ></Icon>
                </el-icon>
                <span
                  v-html="
                    scope.row.colorName ? scope.row.colorName : scope.row.name
                  "
                />
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
                :field="`field_${kidItem.label}`"
                :width="getStringWidth(kidItem.label, 14) + 48"
                :title="kidItem.label"
              >
                <template #header>
                  <div class="dynamic-table-header dynamic-child-header">
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
                  <dynamic-column
                    :active-auth="activeAuth"
                    :root-role="rootRole"
                    :exclude-menu-folder="excludeMenuFolder"
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
              :field="`field_${item.label}`"
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
                <dynamic-column
                  :active-auth="activeAuth"
                  :root-role="rootRole"
                  :exclude-menu-folder="excludeMenuFolder"
                  :item="item"
                  :scope="scope"
                  @independent-auth="independentAuth"
                  @row-weight-changed="rowWeightChanged"
                />
              </template>
            </vxe-column>
          </template>
        </vxe-table>
        <div
          class="empty-img__search"
          v-if="!matchNum && resourceKeyword.length"
        >
          <empty-background
            :description="t('data_set.relevant_content_found')"
            img-type="tree"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
@import "@/style/mixin.less";

.user-role {
  width: 250px;
  float: left;
  height: 100%;
  overflow-y: auto;
  padding-bottom: 24px;
  border-top-left-radius: 12px;

  .filter-user-role {
    position: sticky;
    top: 0;
    left: 0;
    background: #fff;

    .filter-input {
      margin: 16px 8px;
      // width: 200px;
      width: calc(100% - 16px);
    }
    .tabs-mr {
      .border-bottom-tab(30px);
      margin: 0 30px;
    }
  }
  .role-tree-container {
    height: calc(100% - 109px);
  }
  .org-tree-container {
    height: calc(100% - 109px);
    width: 100%;
    padding: 0px 8px;
  }
  .subject-tree-container {
    height: calc(100% - 109px);
    width: 100%;
    padding: 0px 8px;

    .subject-tree-node.is-active {
      background-color: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1));
      color: var(--ed-menu-active-color);
      .entity-item {
        color: inherit;
        :first-child {
          color: inherit;
        }
      }
      .user-account {
        color: inherit;
      }
    }
    .subject-tree-node.is-disabled {
      color: var(--el-text-color-placeholder, #a8abb2);
      cursor: not-allowed;
    }

    .entity-item {
      font-size: 14px;
      height: 22px;
      line-height: 22px;
      overflow: hidden;
      white-space: nowrap;
      text-overflow: ellipsis;
      .user-account {
        color: #8f959e;
      }
    }
    span.role-item {
      width: 100%;
    }
    .mark {
      height: 16px;
      border-radius: 2px;
      margin-left: 8px;
      background-color: var(--ed-color-primary-33, #3370ff33);
      color: var(--ed-menu-active-color);
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
      font-size: 10px;
      font-weight: 500;
      line-height: 13px;
      padding: 0 4px;
    }
  }
}
.resource-panel {
  width: calc(100% - 250px);
  float: right;
  border-left: 1px solid rgba(31, 35, 41, 0.15);
  height: 100%;
  overflow-y: auto;

  .tab-search {
    height: 46px;
    position: relative;

    .search-table-bt {
      position: absolute;
      right: 25px;
      top: 7px;
      // width: 190px;
    }
    .tabs-mr {
      .border-bottom-tab(30px);
      margin: 0 30px;
    }
  }

  .resource-table {
    width: 100%;
    height: calc(100% - 46px);
    .resource-type {
      float: left;
      width: 180px;
      height: 100%;
      padding-top: 12px;
      font-size: 14px;
      div {
        margin: 0 8px;
        display: flex;
        align-items: center;
        span {
          height: 22px;
          line-height: 22px;
        }
      }
    }

    .tree-table {
      width: calc(100% - 180px);
      float: right;
      height: 100%;
      border-left: 1px solid rgba(31, 35, 41, 0.15);
      padding: 16px 24px 24px;
      position: relative;
      .empty-img__search {
        position: absolute;
        top: 141px;
        left: 0;
        width: 100%;
      }
      .search-table-input {
        margin-bottom: 16px;
      }
      .table-container {
        height: calc(100% - 48px);
        .ed-empty {
          padding-top: 80px;
        }
        /* :deep(.vxe-table--render-wrapper) {
          height: 100% !important;
        } */
        :deep(.ed-table__inner-wrapper::before) {
          display: none;
        }

        :deep(.ed-table__border-left-patch) {
          display: none;
        }
        :deep(.dynamic-table-header) {
          width: fit-content;
          display: flex;
          align-items: center;
          height: 23px;
          justify-content: center;
          column-gap: 4px;
        }
        :deep(th) {
          border-top: none;
          &:last-child.ed-table-fixed-column--right {
            border-right: none;
          }
        }
        :deep(td:last-child) {
          border-right: none;
        }
        :deep(.dynamic-row-col-head) {
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
      }
      :deep(.ed-table--border::before) {
        width: 0;
      }
      :deep(.ed-table--border::after) {
        width: 0;
      }
      :deep(.cell) {
        padding: 0 10px;
      }
    }
    .full-tree-table {
      width: calc(100%) !important;
      .table-container {
        height: calc(100% - 25px);
      }
    }
  }
}
.is-active {
  background-color: var(--ed-color-primary-1a, #3370ff1a) !important;
  color: var(--ed-color-primary) !important;
}
.span-is-disabled {
  opacity: 0.25;
  cursor: not-allowed;
  background: none !important;
}
.user-role-container {
  margin: 0 8px;
  font-size: 14px;
}

.custom-tree-node {
  width: calc(100% - 30px);
  // display: flex;
  align-items: center;
  padding-right: 4px;
  box-sizing: content-box;
  position: relative;

  &.f18 {
    .ed-icon {
      font-size: 18px;
    }
  }

  .ed-icon {
    transform: translateY(2px);
  }

  span {
    margin-left: 8px;
  }
}

:deep(.dynamic-resource-hidden) {
  display: none !important;
}

:deep(.de-row-disabled) {
  opacity: 0.45;

  .custom-tree-node {
    color: var(--el-text-color-placeholder, #a8abb2);
  }
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
    background-color: var(
      --ed-color-primary-1a,
      rgba(51, 112, 255, 0.1)
    ) !important;
    color: var(--ed-menu-active-color) !important;
  }
  .list-item_primary {
    padding: 0 16px;
    span.entity-item {
      font-size: 14px;
      height: 22px;
      line-height: 22px;
      overflow: hidden;
      white-space: nowrap;
      text-overflow: ellipsis;
      &:not(.is-active) {
        color: #8f959e;
        :first-child {
          color: #1f2329;
        }
      }
      .user-account:not(.is-active) {
        color: #8f959e;
      }
    }
    span.role-item {
      width: 100%;
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
.single-panel-tab {
  :deep(.ed-tabs__active-bar) {
    display: none;
  }
  :deep(.ed-tabs__item) {
    color: var(--ed-text-color-regular);
  }
}
.org-tree-node {
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
</style>
