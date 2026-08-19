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
import icon_close_outlined from "@/assets/svg/icon_close_outlined.svg";
import icon_searchOutline_outlined from "@/assets/svg/icon_search-outline_outlined.svg";
import {computed, nextTick, reactive, ref, watch} from "vue";
import {ElIcon, ElMessage, ElMessageBox} from "element-plus-secondary";
import {Icon} from "@/components/icon-custom";
import DsTypeList from "./DsTypeList.vue";
import {useI18n} from "@/hooks/web/useI18n";
import {
  getSchemaApi,
  latestUseApi,
  loadSyncPlugin,
  saveApi,
  updateApi,
  validateApi
} from "@/api/sync/syncDatasource";
import {DsType, dsTypes, nameMap, typeList} from "./option";
import {cloneDeep} from "lodash-es";
import {useCache} from "@/hooks/web/useCache";
import {Base64} from "js-base64";
import EditorDetail from "./EditorDetail.vue";
import Elasticsearch from "@/assets/svg/Elasticsearch.svg";
import PluginComponent from "@/components/plugin/src/PluginComponent.vue";

export interface Configuration {
  dataBase: string;
  connectionType: string;
  schema: string;
  extraParams: string;
  username: string;
  password: string;
  host: string;
  authMethod: string;
  port: number;
  initialPoolSize: number;
  minPoolSize: number;
  maxPoolSize: number;
  queryTimeout: number;
  bePort: number;
  url: string;
}

export interface SyncSetting {
  id: string;
  updateType: string;
  syncRate: string;
  simpleCronValue: number;
  simpleCronType: string;
  startTime: number;
  endTime: number;
  endLimit: string;
  cron: string;
}

export interface Param {
  editType: number;
  pid?: string;
  type?: string;
  id?: string;
  name?: string;
  creator?: string;
}

interface Node {
  name: string;
  id: string;
  type: DsType;
}

interface Tree {
  [key: string]: any;
}

interface Form {
  name: string;
  id?: string;
  desc: string;
  type: string;
  configuration?: Configuration;
  syncSetting?: SyncSetting;
  datasourceRole: number,
  isPlugin?: boolean,
  staticMap?: any
}

const {t} = useI18n();
const {wsCache} = useCache();
const loading = ref<boolean>(false);
const state = reactive({
  datasourceTree: typeList.map((ele) => {
    return {
      name: nameMap[ele],
      type: ele,
    };
  }),
});
const activeStep = ref(0);
const detail = ref();
const xpack = ref()
const latestUseTypes = ref([]);
const currentType = ref<DsType>("OLTP");
const filterText = ref("");
const currentDsType = ref();
const sourceActive = ref();
const isPlugin = ref(false)
const emits = defineEmits(["refresh"]);
const pluginComponentLoadDone = () => {
  xpack?.value?.invokeMethod({methodName: 'initForm', args: [{edit: editDs.value, ...form}]})
}
const selectDsType = (dsInfo) => {
  const type = typeof dsInfo === "string" ? dsInfo : dsInfo.type;
  currentDsType.value = type;
  activeStep.value = 1;
  nextTick(() => {
    detail.value.initForm(type)
    if (!dsTree.value) return;
    currentTypeList.value
        .map((ele) => ele.dbList)
        .flat()
        .some((ele) => {
          if (ele.type === currentDsType.value) {
            dsTree.value.setCurrentNode(ele);
            isPlugin.value = ele['isPlugin']
            return true;
          }
          return false;
        });
  });
};

const handleDsNodeClick = (data) => {
  if (!data.type) return;
  isPlugin.value = data['isPlugin']
  selectDsType(data);
};

const handleNodeClick = (data: Node) => {
  currentType.value = data.type;
};

watch(filterText, (val) => {
  if (activeStep.value === 1) {
    dsTree.value.filter(val.toLocaleLowerCase());
  }
});
const dsTree = ref();
const defaultProps = ref({
  children: "dbList",
  label: "name",
});
const filterNode = (value: string, data: Tree) => {
  if (!value) return true;
  return data.name.toLowerCase().includes(value);
};
const filterTypeByActiveName = (ele: string) => {
  if (sourceActive.value && ele === "OLTP") {
    currentType.value = "OLTP";
    return true;
  }
  if (!sourceActive.value && ele === "OLAP") {
    currentType.value = "OLAP";
    return true;
  }
  return false;
};
const datasourceTree = computed(() => {
  return state.datasourceTree.filter((ele) => {
    return filterTypeByActiveName(ele.type);
  });
});
const databaseList = ref();
const currentTypeList = computed(() => {
  return typeList
      .map((ele, index) => {
        return {name: nameMap[ele], dbList: databaseList.value[index]};
      })
      .filter((ele) => {
        return filterTypeByActiveName(ele.name);
      });
});

const getDatasourceTypes = () => {
  const arr = [[], [], [], [], []] as any[];
  dsTypes.forEach((item) => {
    const index = typeList.findIndex((ele) => ele === item.catalog);
    if (index !== -1) {
      arr[index].push(item);
    }
  });
  databaseList.value = arr.map((ele) => {
    return ele.sort((a: any, b: any) => {
      return (
          a.name.toLowerCase().charCodeAt(0) - b.name.toLowerCase().charCodeAt(0)
      );
    });
  });
};
getDatasourceTypes();
const pluginIndex = ref('')
const pluginDs = ref([])
const listSyncPlugin = () => {
  loadSyncPlugin().then(res => {
        pluginDs.value = res.data
        res.data?.forEach(item => {
          const {name, category, type, icon, extraParams, staticMap, datasourceRole} = item
          const node = {
            name,
            catalog: category,
            type,
            icon,
            extraParams,
            isPlugin: true,
            staticMap,
            datasourceRole
          }
          const index = typeList.findIndex(ele => ele === node.catalog)
          if (index !== -1) {
            let copiedArr = JSON.parse(JSON.stringify(databaseList.value))
            copiedArr[index].push(node)
            databaseList.value = copiedArr
          }
        })
      }
  )
}
listSyncPlugin();

const getPluginStatic = type => {
  const arr = pluginDs.value.filter(ele => {
    return ele.type === type && ele.datasourceRole === (sourceActive?.value ? 1 : 2)
  })
  return pluginIndex.value
      ? pluginIndex.value
      : arr && arr.length > 0
          ? arr[0].staticMap?.index
          : null
}

// Schema 请求统一由同步管理外层发起，源/目标数据源插件只接收这个固定函数，不接触 token 和 HTTP 客户端
const getSyncDatasourcePluginSchema = data => getSchemaApi(data)

const handleSubmit = param => {
  const validateFrom = param.validate
  if (param.eventName === 'saveDs') {
    validateFrom(val => {
      if (val) {
        doSaveDs(param.args)
      } else {
        loading.value = false;
      }
    })
  } else {
    validateFrom(val => {
      if (val) {
        doValidateDs(param.args)
      }
    })
  }
}
const getLatestUseTypes = () => {
  latestUseApi(sourceActive?.value ? "source" : "target").then((res) => {
    latestUseTypes.value = res.data;
  });
};

const setNextStep = () => {
  if (activeStep.value === 1) return;
  activeStep.value = activeStep.value + 1;
};

const next = () => {
  if (!currentDsType.value || currentDsType.value === "") {
    ElMessage.error(t("sync_datasource.select_type"));
    return;
  }
  setNextStep();
};
const prev = () => {
  if (activeStep.value === 1) {
    currentDsType.value = "";
  }
  activeStep.value = activeStep.value - 1;
};

const validateDS = () => {
  const request = JSON.parse(JSON.stringify(form)) as unknown as Omit<
      Form,
      "configuration"
  > & {
    configuration: string;
  };
  request.configuration = Base64.encode(JSON.stringify(request.configuration));
  if (isPlugin.value) {
    xpack?.value?.invokeMethod({
      methodName: 'submitForm',
      args: [{eventName: 'validateDs', args: request}]
    })
  } else {
    loading.value = true;
    const validateFrom = detail.value.submitForm();
    validateFrom((val) => {
      if (val) {
        validateApi(request)
            .then(() => {
              loading.value = false;
              ElMessage.success(t("sync_datasource.validate_success"));
            })
            .catch(() => {
              loading.value = false;
            });
      } else {
        loading.value = false;
      }
    });
  }
};

const typeTitle = computed(() => {
  if (!currentDsType.value) {
    return "";
  }
  let str = "";
  databaseList.value.some((ele) => {
    return ele.some((itx) => {
      if (itx.type === currentDsType.value) {
        str = itx.name;
        return true;
      }
      return false;
    });
  });
  return str;
});

const saveDS = () => {
  console.log("saveDS");
  loading.value = true;
  const request = JSON.parse(JSON.stringify(form)) as unknown as Omit<
      Form,
      "configuration"
  > & {
    configuration: string;
  };
  request.configuration = Base64.encode(JSON.stringify(request.configuration));
  if (isPlugin.value) {
    xpack?.value?.invokeMethod({
      methodName: 'submitForm',
      args: [{eventName: 'saveDs', args: request}]
    })
  } else {
    const validate = detail.value.submitForm();
    validate((val) => {
      if (val) {
        doSaveDs(request)
      } else {
        loading.value = false;
      }
    });
  }
};

const doSaveDs = (request) => {
  if (editDs.value) {
    updateApi(request)
        .then((res) => {
          if (res !== undefined) {
            if (res.data["hasRunningTask"] === "true") {
              ElMessage.warning(t("sync_datasource.has_running_task_msg"));
            } else {
              ElMessage.success(t("common.save_success"));
            }
            visible.value = false;
            emits("refresh");
          }
        })
        .catch(() => {
          loading.value = false;
        });
  } else {
    saveApi(request)
        .then((res) => {
          if (res !== undefined) {
            ElMessage.success(t("common.save_success"));
            visible.value = false;
            emits("refresh");
          }
        })
        .catch(() => {
          loading.value = false;
        });
  }
}

const doValidateDs = request => {
  loading.value = true
  validateApi(request)
      .then(() => {
        ElMessage.success(t('datasource.validate_success'))
      })
      .finally(() => {
        loading.value = false
      })
}

const iconMap = {
  mysql: mysqlDs,
  oracle: oracleDs,
  sqlServer: sqlServerDs,
  TiDB: TiDBDs,
  impala: impalaDs,
  mariadb: mariadbDs,
  StarRocks: StarRocksDs,
  postgresql: pgDs,
  mongo: mongoDs,
  ck: ckDs,
  db2: db2Ds,
  redshift: redshiftDs,
  API: APIDs,
  Excel: ExcelDs,
  doris: dorisDs,
  elasticsearch: Elasticsearch,
};
const defaultForm = {
  type: "OLTP",
  name: "",
  desc: "",
  id: "",
  configuration: {
    dataBase: "",
    extraParams: "",
    username: "",
    password: "",
    host: "",
    authMethod: "",
    port: 0,
    initialPoolSize: 5,
    minPoolSize: 5,
    maxPoolSize: 5,
    queryTimeout: 30,
  } as Configuration,
} as Form;
const form = reactive<Form>(cloneDeep(defaultForm));
const visible = ref(false);
const editDs = ref(false);
const init = (dsInfo: Form, source: boolean) => {
  loading.value = false;
  editDs.value = !!dsInfo;
  activeStep.value = Number(editDs.value);
  visible.value = true;
  sourceActive.value = source;
  currentDsType.value = dsInfo?.type || "";
  isPlugin.value = dsInfo?.isPlugin || false
  pluginIndex.value = isPlugin.value ? dsInfo?.staticMap?.index : null
  getLatestUseTypes();
  if (!!dsInfo) {
    nextTick(() => {
      currentDsType.value = dsInfo.type;
      activeStep.value = 1;
      form.name = dsInfo.name;
      form.id = dsInfo.id;
      form.desc = dsInfo.desc;
      form.type = dsInfo.type;
      form.datasourceRole = dsInfo.datasourceRole;
      form.configuration = dsInfo.configuration;
      nextTick(() => {
        detail.value.clearForm()
        xpack?.value?.invokeMethod({
          methodName: 'clearForm',
          args: []
        })
      })
    });
  } else {
    defaultForm.datasourceRole = source ? 1 : 2;
    Object.assign(form, cloneDeep(defaultForm));
  }
};
const handleCancel = () => {
  visible.value = false;
  currentDsType.value = "";
  detail.value?.resetForm();
};
const beforeClose = () => {
  if (wsCache.get("ds-new-success")) {
    emits("refresh");
    wsCache.set("ds-new-success", false);
  }
  if (editDs.value || activeStep.value !== 0) {
    ElMessageBox.confirm(t("sync_datasource.tips"), {
      confirmButtonText: t("dataset.confirm"),
      cancelButtonText: t("dataset.cancel"),
      confirmButtonType: "primary",
      tip: t("data_set.sure_to_exit"),
      type: "warning",
      autofocus: false,
      showClose: false,
    })
        .then(() => {
          visible.value = false;
          detail.value?.resetForm();
        })
        .catch(() => {
        });
  } else {
    visible.value = false;
    detail.value?.resetForm();
  }
};

defineExpose({
  init,
});
</script>

<template>
  <el-drawer
      :close-on-click-modal="false"
      size="calc(100% - 100px)"
      modal-class="datasource-drawer-fullscreen"
      direction="btt"
      :before-close="beforeClose"
      :show-close="false"
      v-model="visible"
      :z-index="2000"
  >
    <template #header="{ close }">
      <span>{{
          editDs
              ? t("sync_datasource.edit_datasource")
              : t("sync_datasource.add_datasource")
        }}</span>
      <div v-if="!editDs" class="flex-center" style="width: 100%">
        <el-steps
            style="max-width: 450px; flex: 1"
            custom
            :active="activeStep"
            align-center
        >
          <el-step>
            <template #title>
              {{ t("data_set.select_data_source") }}
            </template>
          </el-step>
          <el-step>
            <template #title>
              {{ t("sync_datasource.config_info") }}
            </template>
          </el-step>
        </el-steps>
      </div>
      <el-icon @click="close" class="datasource-close">
        <Icon name="icon_close_outlined"
        >
          <icon_close_outlined class="svg-icon"
          />
        </Icon>
      </el-icon>
    </template>
    <div class="datasource" v-loading="loading">
      <div class="ds-type-select" v-if="!editDs">
        <div class="title">
          <el-input
              :placeholder="t('chart.search')"
              class="m24 w100"
              v-model="filterText"
              clearable
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
        </div>
        <template v-if="activeStep === 0">
          <p
              :class="currentType === 'latestUse' && 'active'"
              @click="
              handleNodeClick({
                type: 'latestUse',
                name: 'latestUse',
                id: 'latestUse',
              })
            "
              class="list-item_primary"
          >
            {{ t("sync_datasource.recently_created") }}
          </p>
          <el-divider/>
          <div
              :key="ele.name"
              @click="handleNodeClick(<Node>ele)"
              v-for="ele in datasourceTree"
              class="list-item_primary"
              :class="currentType === ele.type && 'active'"
          >
            <span :title="ele.name" class="label">{{ ele.name }}</span>
          </div>
        </template>
        <el-tree
            :expand-on-click-node="false"
            menu
            v-if="activeStep > 0"
            ref="dsTree"
            :data="currentTypeList"
            nodeKey="name"
            :props="defaultProps"
            :filter-node-method="filterNode"
            @node-click="handleDsNodeClick"
        >
          <template #default="{ node, data }">
            <span class="custom-tree-node flex-align-center">
              <el-icon
                  v-if="!!data.catalog"
                  class="icon-border"
                  style="width: 18px; height: 18px"
              >
                <Icon
                ><component
                    :is="iconMap[data.type]"
                    class="svg-icon"
                ></component
                ></Icon>
              </el-icon>
              <span :title="node.label" class="label-tooltip">{{
                  node.label
                }}</span>
            </span>
          </template>
        </el-tree>
      </div>
      <div class="ds-editor" :class="editDs && 'edit-ds'">
        <div v-show="activeStep !== 0 && !editDs" class="ds-type-title">
          {{ typeTitle }}
        </div>
        <div
            class="editor-content"
            :class="(activeStep === 0 || editDs) && 'type-title'"
        >
          <ds-type-list
              v-show="activeStep === 0"
              :filter-text="filterText.toLocaleLowerCase()"
              @select-ds-type="selectDsType"
              :current-type="currentType"
              :latest-use-types="latestUseTypes"
          ></ds-type-list>
          <editor-detail
              ref="detail"
              :form="form"
              :editDs="editDs"
              :active-step="activeStep"
              v-show="activeStep !== 0 && currentDsType && !isPlugin"
          ></editor-detail>
          <plugin-component
              :jsname="getPluginStatic(currentDsType)"
              :get-sync-datasource-schema="getSyncDatasourcePluginSchema"
              ref="xpack"
              :form="form"
              :editDs="editDs"
              :active-step="activeStep"
              @submitForm="handleSubmit"
              @pluginComponentLoadDone="pluginComponentLoadDone"
              v-if="
              activeStep !== 0 &&
              currentDsType &&
              visible &&
              isPlugin
            ">
          </plugin-component>
        </div>
      </div>
      <div class="editor-footer">
        <el-button secondary @click="handleCancel">
          {{ t("common.cancel") }}
        </el-button
        >
        <el-button
            v-show="!(activeStep === 0 || editDs)"
            secondary
            @click="prev"
        >
          {{ t("sync_datasource.prev") }}
        </el-button>
        <el-button v-show="activeStep === 1" secondary @click="validateDS">
          {{ t("sync_datasource.validate") }}
        </el-button>
        <el-button v-show="activeStep === 0" type="primary" @click="next">
          {{ t("sync_datasource.next") }}
        </el-button>
        <el-button v-show="activeStep === 1" type="primary" @click="saveDS">
          {{ t("sync_datasource.save") }}
        </el-button>
      </div>
    </div>
  </el-drawer>
</template>

<style lang="less">
.datasource-drawer-fullscreen {
  .ed-drawer__body {
    padding: 0;
  }

  .ed-drawer__header > :first-child {
    flex: none;
    width: auto;
  }

  .ed-drawer__header {
    border-color: rgba(31, 35, 41, 0.15);
    justify-content: space-between;
  }

  .datasource-close {
    cursor: pointer;
  }

  .editor-step {
    position: relative;

    .ed-steps {
      width: 500px;
    }

    .ed-step.is-center .ed-step__line {
      width: 80px;
      right: 40px;
      z-index: 5;
      left: calc(100% - 40px);
    }

    .ed-step__icon.is-icon {
      width: auto;
      position: relative;
      z-index: 0;
    }

    .ed-step__head.is-finish::after {
      right: calc(100% - 66px);
      top: 44%;
    }

    .ed-step__head.is-process .ed-step__icon {
      background-color: transparent;

      .step-icon {
        .icon {
          background: var(--ed-color-primary);
        }
      }
    }

    .ed-step__head.is-finish .ed-step__icon {
      background-color: transparent;

      .step-icon {
        .icon {
          border: 1px solid var(--ed-color-primary);
        }
      }
    }

    .ed-step__head.is-wait .ed-step__icon {
      background-color: transparent;

      .step-icon {
        .icon {
          color: #8f959e;
          border: 1px solid #8f959e;
        }
      }
    }

    .step-icon {
      display: flex;
      padding: 0 48px;
      align-items: center;

      .icon {
        width: 28px;
        height: 28px;
        line-height: 27px;
        border-radius: 50%;
      }

      .title {
        margin-left: 8px;
        color: #1f2329;
        font-size: 14px;
        font-weight: 400;
        line-height: 22px;
      }
    }
  }

  .datasource {
    width: 100%;
    height: 100%;
    background: #fff;
    position: relative;

    .custom-tree-node {
      .ed-icon {
        margin-right: 8px;
      }
    }

    .ds-type-select {
      width: 279px;
      height: calc(100% - 64px);
      padding: 16px 7px;
      border-right: 1px solid rgba(31, 35, 41, 0.15);
      float: left;
      overflow-y: auto;

      .icon-border {
        font-size: 18px;
        margin-right: 8px;
      }

      .title {
        display: flex;
        justify-content: space-between;
        font-family: var(--de-custom_font, "PingFang");
        font-size: 14px;
        font-weight: 500;
        color: var(--TextPrimary, #1f2329);
        box-sizing: border-box;
        flex-wrap: wrap;
        position: sticky;
        top: 0;
        left: 24px;
        z-index: 5;
        background: white;
        padding: 0 17px;

        &::before {
          content: "";
          width: 100%;
          height: 24px;
          top: -24px;
          position: absolute;
          z-index: 5;
          left: 0;
          background: white;
        }
      }

      .ed-divider--horizontal {
        margin: 4px 0 4px 16px;
        width: calc(100% - 34px);
        border-color: rgba(31, 35, 41, 0.15);
      }

      .m24 {
        margin-bottom: 8px;
      }

      .w100 {
        width: 100%;
      }

      .list-item_primary {
        position: relative;
        padding: 8px 17px;
        font-weight: 500;
        font-size: 14px;
      }
    }

    .ds-editor {
      float: left;
      width: calc(100% - 279px);
      height: calc(100% - 64px);

      .ed-input__wrapper {
        padding-right: 12px;
        padding-left: 12px;
      }

      &.edit-ds {
        width: 100%;
      }

      .ds-type-title {
        width: 100%;
        padding: 16px 24px;
        color: #1f2329;
        font-family: var(--de-custom_font, "PingFang");
        font-size: 16px;
        font-style: normal;
        font-weight: 500;
        line-height: 24px;
        border-bottom: 1px solid rgba(31, 35, 41, 0.15);
      }

      .editor-content {
        &::-webkit-scrollbar {
          width: 0 !important;
          height: 0 !important;
        }

        padding: 16px 24px;
        height: calc(100vh - 278px);
        overflow-y: auto;

        &.type-title {
          height: calc(100vh - 221px);
        }
      }
    }

    .editor-footer {
      height: 64px;
      display: flex;
      align-items: center;
      justify-content: flex-end;
      width: 100%;
      padding-right: 24px;
      float: left;
      border-top: 1px solid rgba(31, 35, 41, 0.15);
    }
  }
}
</style>
