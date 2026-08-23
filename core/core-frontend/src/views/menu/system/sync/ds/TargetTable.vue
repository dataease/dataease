<script setup lang="ts">
import icon_succeed_filled from "@/assets/svg/icon_succeed_filled.svg";
import icon_close_filled from "@/assets/svg/icon_close_filled.svg";
import icon_searchOutline_outlined from "@/assets/svg/icon_search-outline_outlined.svg";
import iconFilter from "@/assets/svg/icon-filter.svg";
import icon_edit_outlined from "@/assets/svg/icon_edit_outlined.svg";
import icon_sync_target_to_datasource from "@/assets/svg/icon_sync_target_to_datasource.svg";
import icon_deleteTrash_outlined from "@/assets/svg/icon_delete-trash_outlined.svg";
import {useI18n} from "@/hooks/web/useI18n";
import {computed, onMounted, reactive, ref, watch} from "vue";
import {convertFilterText, FilterText} from "@/components/filter-text";
import {Icon} from "@/components/icon-custom";
import GridTable from "@/components/grid-table/src/GridTable.vue";
import {
  batchDelApi,
  deleteByIdApi,
  getByIdApi,
  loadSyncPlugin,
  sourceDsPageApi,
  targetDsPageApi,
} from "@/api/sync/syncDatasource";
import EditorDatasource from "./form/index.vue";
import {ElIcon, ElMessage, ElMessageBox} from "element-plus-secondary";
import {dsTypes, filterOption} from "./form/option";
import DrawerMain from "@/components/drawer-main/src/DrawerMain.vue";
import SyncForm from "./SyncForm.vue";
import {symmetricDecrypt} from "@/utils/encryption";

const props = defineProps({
  activeName: {
    type: String,
    default: "",
  },
});
const {t} = useI18n();
const loading = ref(false);
const drawerMainRef = ref();
const multipleTableRef = ref();
const datasourceEditor = ref();
const activeSource = computed(() => props.activeName === "source");
const state = reactive({
  dsList: [],
  filterTexts: [],
  paginationConfig: {
    currentPage: 1,
    pageSize: 10,
    total: 0,
  },
  conditions: [],
  orders: [],
  multipleSelection: [],
});
const keyword = ref(null);
state.filterTexts = [];
const searchCondition = (conditions) => {
  state.conditions = conditions;
  search();
  fillFilterText();
  drawerMainClose();
};
const fillFilterText = () => {
  const textArray = state.conditions?.length
      ? convertFilterText(state.conditions, filterOption)
      : [];
  state.filterTexts = [...textArray];
  Object.assign(state.filterTexts, textArray);
};
const clearFilter = (params?: number | undefined) => {
  let index = params ? params : 0;
  if (isNaN(index)) {
    state.filterTexts = [];
  } else {
    state.filterTexts.splice(index, 1);
  }
  drawerMainRef.value.clearFilter(index);
};
const pageChange = (index) => {
  if (typeof index !== "number") {
    return;
  }
  state.paginationConfig.currentPage = index;
  search();
};
const sizeChange = (size) => {
  state.paginationConfig.pageSize = size;
  search();
};
const sortChange = (param: any) => {
  state.orders = [];
  if (param.order && param.prop === "createTime") {
    const type = param.order.substring(0, param.order.indexOf("ending"));
    state.orders.push("create_time " + type);
    search();
  }
};
const handleSelectionChange = (rows: any) => {
  state.multipleSelection = rows;
};
const clearSelection = () => {
  multipleTableRef.value?.clearSelection();
};
const rowCheckStatus = (_row: any) => {
  return true;
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
const createDatasource = () => {
  datasourceEditor.value.init(null, activeSource.value);
};
const refreshGrid = () => {
  search();
};
onMounted(() => {
  search();
});

watch(
    () => props.activeName,
    () => {
      search();
    }
);
const searchThen = (res) => {
  loading.value = false;
  state.dsList = res.data.records;
  if (state.paginationConfig.currentPage > 1 && state.dsList.length === 0) {
    state.paginationConfig.currentPage--;
    search();
  }
  state.paginationConfig.total = res.data.total;
  imgType.value = getEmptyImg();
  emptyDesc.value = getEmptyDesc();
};
const buildParam = () => {
  const param = {};
  if (state.conditions?.length) {
    state.conditions.forEach((condition) => {
      if (condition["value"]) {
        param[condition["field"]] = condition["value"];
      }
    });
  }
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

  if (activeSource.value) {
    sourceDsPageApi(
        state.paginationConfig.currentPage,
        state.paginationConfig.pageSize,
        buildParam()
    )
        .then((res) => {
          searchThen(res);
        })
        .catch(() => {
          loading.value = false;
        });
  } else {
    targetDsPageApi(
        state.paginationConfig.currentPage,
        state.paginationConfig.pageSize,
        buildParam()
    )
        .then((res) => {
          searchThen(res);
        })
        .catch(() => {
          loading.value = false;
        });
  }
};

const drawerMainOpen = async () => {
  await listSyncPlugin();
  const datasourceRole = activeSource.value ? 1 : 2
  const typeOptions = [...dsTypes, ...pluginDs.value]
      .filter((item) => Number(item.datasourceRole) === datasourceRole)
      .map((item) => [item.type, {id: item.type, name: item.name}] as const)
  filterOption[0].option = Array.from(
      new Map<string, {id: string; name: string}>(typeOptions).values()
  );
  drawerMainRef.value.init();
};
const drawerMainClose = () => {
  drawerMainRef.value.close();
};
const pluginDs = ref([])
// 同步插件由类型和数据源角色共同确定
const findSyncPlugin = data => {
  return pluginDs.value.find(ele =>
    ele.type?.toLowerCase() === data.type?.toLowerCase() &&
    Number(ele.datasourceRole) === Number(data.datasourceRole)
  )
}
const dsInfo = ref();
const getDsInfo = (data) => {
  return getByIdApi(data.id).then((res) => {
    const pluginInfo = findSyncPlugin(res.data)
    let {
      configuration,
      datasourceRole,
      desc,
      id,
      name,
      status,
      statusRemark,
      type,
    } = res.data;
    if (configuration) {
      configuration = JSON.parse(
          symmetricDecrypt(configuration)
      );
    }
    Object.assign(dsInfo, {
      configuration,
      datasourceRole,
      desc,
      id,
      name,
      status,
      statusRemark,
      type,
      isPlugin: !!pluginInfo,
      staticMap: pluginInfo?.staticMap,
      systemDatasourceType: pluginInfo?.systemDatasourceType
    });
  });
};
const edit = async (row) => {
  // 等待插件元数据后再判断表单类型，避免 PostgreSQL 被误判为内置目标数据源。
  await listSyncPlugin();
  await getDsInfo(row);
  datasourceEditor.value.init(dsInfo, false);
};
const delHandler = (row) => {
  ElMessageBox.confirm(t("sync_datasource.ds_delete_confirm"), {
    confirmButtonText: t("common.delete"),
    cancelButtonText: t("common.cancel"),
    showCancelButton: true,
    confirmButtonType: "danger",
    type: "warning",
    autofocus: false,
    showClose: false,
  }).then(() => {
    deleteByIdApi(row.id).then(() => {
      refreshGrid();
    });
  });
};
const batchDelHandler = () => {
  ElMessageBox.confirm(
      t("sync_datasource.confirm_batch_delete_target_ds", [
        " " + state.multipleSelection.length + " ",
      ]),
      {
        confirmButtonType: "danger",
        type: "warning",
        confirmButtonText: t("common.delete"),
        cancelButtonText: t("dataset.cancel"),
        autofocus: false,
        showClose: false,
      }
  )
      .then(() => {
        batchDel();
      })
      .catch(() => {
        clearSelection();
      });
};
const batchDel = () => {
  const ids = state.multipleSelection.map((item) => item.id);
  loading.value = true;
  batchDelApi(ids).then(() => {
    loading.value = false;
    ElMessage.success(t("common.delete_success"));
    search();
  });
};
const timestampFormatDate = (value) => {
  if (!value) {
    return "-";
  }
  return new Date(value)["format"]();
};
const syncFormDialogRef = ref();
const showSyncFormDialog = async (row) => {
  syncFormDialogRef.value.syncTargetToDatasourceFormVisible = true;
  await listSyncPlugin();
  await getDsInfo(row);
  syncFormDialogRef.value.init(dsInfo);
};
const canSyncToDatasource = row => {
  const pluginInfo = findSyncPlugin(row)
  return !pluginInfo || !!pluginInfo.systemDatasourceType
}
const typeMap = dsTypes.reduce((pre, next) => {
  pre[next.type] = next.name
  return pre
}, {})
let syncPluginLoadPromise: Promise<void> | null = null
const listSyncPlugin = () => {
  if (!syncPluginLoadPromise) {
    syncPluginLoadPromise = loadSyncPlugin()
        .then(res => {
          pluginDs.value = res.data || []
          pluginDs.value.forEach(ele => {
            typeMap[ele.type] = ele.name
          })
        })
        .catch(error => {
          syncPluginLoadPromise = null
          throw error
        })
  }
  return syncPluginLoadPromise
}
void listSyncPlugin().catch(() => undefined)
</script>

<template>
  <div class="source-ds-table de-search-table">
    <div
        :class="!!state.multipleSelection.length && 'source-ds-table-selection'"
        class="source-ds de-search-table"
    >
      <el-row class="source-ds-table__filter top-operate">
        <el-col :span="12">
          <el-button @click="createDatasource" type="primary">
            {{
              activeSource
                  ? t("sync_datasource.add_source_ds")
                  : t("sync_datasource.add_target_ds")
            }}
          </el-button>
        </el-col>
        <el-col :span="12" class="right-filter">
          <el-input
              v-model="keyword"
              clearable
              :placeholder="t('sync_manage.ds_search_placeholder')"
              @change="search"
          >
            <template #prefix>
              <el-icon>
                <Icon name="icon_search-outline_outlined"
                >
                  <icon_searchOutline_outlined class="svg-icon"
                  />
                </Icon>
              </el-icon>
            </template>
          </el-input>
          <el-button
              @click="drawerMainOpen"
              :plain="!!state.conditions.length"
              :class="
              state.conditions.length
                ? 'filter-condition-button'
                : 'filter-button'
            "
          >
            <template #icon>
              <Icon name="icon-filter">
                <iconFilter class="svg-icon"/>
              </Icon>
            </template>
            {{
              t("common.filter") +
              (state.conditions.length ? `(${state.conditions?.length})` : "")
            }}
          </el-button>
        </el-col>
      </el-row>
      <filter-text
          @clear-filter="clearFilter"
          :total="state.paginationConfig.total"
          :filter-texts="state.filterTexts"
      ></filter-text>
      <div
          :class="[
          state.filterTexts.length
            ? 'is-in-filter'
            : 'source-ds-table__content',
        ]"
      >
        <GridTable
            ref="multipleTableRef"
            :pagination="state.paginationConfig"
            :table-data="state.dsList"
            :empty-desc="emptyDesc"
            :empty-img="imgType"
            class="popper-max-width"
            @current-change="pageChange"
            @size-change="sizeChange"
            @sort-change="sortChange"
            @selection-change="handleSelectionChange"
            :show-empty-img="!loading"
            :data-loading="loading"
        >
          <el-table-column
              type="selection"
              min-width="30"
              :selectable="rowCheckStatus"
          />
          <el-table-column
              key="name"
              show-overflow-tooltip
              prop="name"
              :label="t('sync_datasource.name')"
              min-width="150"
          >
            <template #default="scope">
              <div class="de-one-line">{{ scope.row.name }}</div>
            </template>
          </el-table-column>
          <el-table-column
              key="typeName"
              prop="typeName"
              :label="t('sync_datasource.type')"
          />
          <el-table-column
              key="desc"
              show-overflow-tooltip
              prop="desc"
              :label="t('sync_datasource.desc')"
              min-width="150"
          >
            <template #default="scope">
              <div class="de-one-line">
                {{ scope.row.desc ? scope.row.desc : "-" }}
              </div>
            </template>
          </el-table-column>
          <el-table-column
              prop="status"
              key="status"
              :label="t('sync_datasource.status')"
              show-overflow-tooltip
          >
            <template #default="scope">
              <div style="display: flex; align-items: center">
                <el-icon size="16px">
                  <Icon
                      :style="scope.row.status === 'Success' ? '' : 'color: red'"
                  >
                    <component
                        :is="
                        scope.row.status === 'Success'
                          ? icon_succeed_filled
                          : icon_close_filled
                      "
                        :style="
                        scope.row.status === 'Success' ? '' : 'color: red'
                      "
                        class="svg-icon"
                    ></component
                    >
                  </Icon>
                </el-icon>
                <span style="padding-left: 8px">{{
                    scope.row.status === "Success"
                        ? t("sync_datasource.valid")
                        : t("sync_datasource.invalid")
                  }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column
              prop="createTime"
              :label="t('sync_datasource.create_time')"
              min-width="170"
          >
            <template #default="scope">
              <span>{{ timestampFormatDate(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column
              width="110"
              fixed="right"
              key="_operation"
              :label="t('common.operate')"
          >
            <template #default="scope">
              <div class="operate-icon-container">
                <el-tooltip
                    effect="dark"
                    :content="t('common.edit')"
                    placement="top"
                >
                  <el-button text @click="edit(scope.row)">
                    <template #icon>
                      <Icon name="icon_edit_outlined"
                      >
                        <icon_edit_outlined class="svg-icon"
                        />
                      </Icon>
                    </template>
                  </el-button>
                </el-tooltip>
                <el-tooltip
                    v-if="canSyncToDatasource(scope.row)"
                    effect="dark"
                    :content="t('sync_datasource.sync_ds')"
                    placement="top"
                >
                  <el-button
                      text
                      @click="showSyncFormDialog(scope.row)"
                      class="del-button"
                  >
                    <template #icon>
                      <Icon name="icon_sync_target_to_datasource"
                      >
                        <icon_sync_target_to_datasource class="svg-icon"
                        />
                      </Icon>
                    </template>
                  </el-button>
                </el-tooltip>
                <el-tooltip
                    effect="dark"
                    :content="t('common.delete')"
                    placement="top"
                >
                  <el-button
                      text
                      @click="delHandler(scope.row)"
                      class="del-button"
                  >
                    <template #icon>
                      <Icon name="icon_delete-trash_outlined"
                      >
                        <icon_deleteTrash_outlined class="svg-icon"
                        />
                      </Icon>
                    </template>
                  </el-button>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
        </GridTable>
      </div>
    </div>
  </div>
  <div
      v-if="state.multipleSelection.length"
      class="bottom-bar flex-align-center"
  >
    <el-button
        type="danger"
        class="batch-delete-button"
        plain
        @click="batchDelHandler"
    >
      {{ t("user.batch_del") }}
    </el-button>
    <span class="bottom-info">{{
        t("user.selection_info", [state.multipleSelection.length])
      }}</span>
    <el-button text @click="clearSelection">
      {{ t("user.clear_button") }}
    </el-button>
  </div>
  <drawer-main
      :filter-options="filterOption"
      @trigger-filter="searchCondition"
      ref="drawerMainRef"
  ></drawer-main>
  <EditorDatasource
      @refresh="refreshGrid"
      ref="datasourceEditor"
  ></EditorDatasource>
  <SyncForm ref="syncFormDialogRef"></SyncForm>
</template>

<style scoped lang="less">
.operate-icon-container {
  .ed-button {
    margin-right: 4px;
  }
}

.source-ds-table {
  border-radius: 4px;
}
</style>
