<script setup lang="ts">
import dvInfo from '@/assets/svg/dv-info.svg'
import {computed, onMounted, watch} from "vue";
import {getDatasourceListByTypeApi, getDatasourceTableListApi, type IDsTable, type ISource, ITaskInfoRes,} from "@/api/sync/syncTask";
import {Icon} from "@/components/icon-custom";
import {ElIcon, ElMessage} from "element-plus-secondary";
import {validateByIdApi} from "@/api/sync/syncDatasource";
import {useI18n} from "@/hooks/web/useI18n";

const {t} = useI18n();

interface DsType {
  type: string;
  name: string;
  catalog: string;
}

const props = withDefaults(
    defineProps<{
      modelValue: ITaskInfoRes;
      dsTypeListData: Array<DsType>;
      isEdit: boolean;
    }>(),
    {
      modelValue: () => {
        return {
          source: {
            tableExtract: "0",
          } as ISource,
        } as ITaskInfoRes;
      },
      dsTypeListData: [],
      isEdit: false,
    }
);
const form = computed<ITaskInfoRes>({
  get() {
    return props.modelValue;
  },
  set(value) {
    emits("update:modelValue", value);
  },
});
const emits = defineEmits(["update:modelValue", "changeLoading"]);

// 虚拟下拉仅渲染可视项，避免大表量场景创建全部选项节点
const sourceTableOptions = computed(() => {
  return (form.value.source.dsTableList || []).map((item: IDsTable) => ({
    label: item.remark ? `${item.name} (${item.remark})` : item.name,
    value: item.name,
  }));
});

onMounted(() => {
  if (!form.value.source.tableExtract) {
    form.value.source.tableExtract = "0";
  }
  // 新增时默认选择第一个数据库类型，如果已经获取到了数据库表，则不指定数据库类型
  if (
      !props.isEdit &&
      props.dsTypeListData.length > 0 &&
      !form.value.source.dsTableList
  ) {
    if (props.dsTypeListData[0].type) {
      form.value.source.type = props.dsTypeListData[0].type;
    }
  }
  if (props.isEdit && form.value.editable === true) {
    getDataSourceList();
    getDataSourceTableList();
  }
  if (props.isEdit && form.value.editable === false && form.value.source.datasourceId) {
    form.value.source.dsList = [
      {
        id: form.value.source.datasource?.id,
        name: form.value.source.datasource?.name,
      }
    ];
  }
});

watch(
    () => form.value.source.datasourceId,
    () => {
      if (
          form.value.source.tableExtract === "0" &&
          form.value.source.datasourceId != ""
      ) {
        form.value.source.incrementField = "";
        getDataSourceTableList();
      }
    }
);

const getDataSourceTableList = () => {
  emits("changeLoading", true);
  validateByIdApi(form.value.source.datasourceId).then((res) => {
    if (res.data) {
      form.value.source.dsTableList = [];
      getDatasourceTableListApi(form.value.source.datasourceId)
          .then((res) => {
            emits("changeLoading", false);
            form.value.source.dsTableList = res.data;
            if (!props.isEdit && res.data.length > 0) {
              form.value.source.tables = res.data[0].name;
            }
            if (res.data.length === 0) {
              form.value.source.tables = "";
              form.value.source.incrementField = "";
            }
            if (
                form.value.source.incrementField &&
                form.value.source.incrementField != ""
            ) {
              form.value.source.incrementCheckbox = "on";
            } else {
              form.value.source.incrementCheckbox = "off";
            }
          })
          .catch((err) => {
            emits("changeLoading", false);
            form.value.source.tables = "";
            form.value.source.dsTableList = [];
            ElMessage.error(t('sync_task.msg_get_database_table_failed'));
          });
    } else {
      emits("changeLoading", false);
      form.value.source.tables = "";
      form.value.source.dsTableList = [];
      ElMessage.error(t('sync_task.msg_source_database_status_is_abnormal'));
    }
  });
};

watch(
    () => form.value.source.type,
    () => {
      getDataSourceList();
      clearSourceForm();
    }
);

const clearSourceForm = () => {
  form.value.source.tableExtract = "0";
  form.value.source.datasourceId = "";
  form.value.source.tables = "";
  form.value.source.incrementField = "";
  form.value.source.dsTableList = [];
};

const getDataSourceList = () => {
  getDatasourceListByTypeApi(form.value.source.type).then((data) => {
    const res = data.data?.filter(i => i.datasourceRole === 1);
    form.value.source.dsList = res;
    if (res.length === 0) {
      clearSourceForm();
    }
  });
};

watch(
    () => form.value.source.tableExtract,
    () => {
      form.value.source.incrementField = "";
      if (
          form.value.source.datasourceId &&
          form.value.source.datasourceId != "" &&
          form.value.source.tableExtract === "0"
      ) {
        getDataSourceTableList();
      } else {
        form.value.source.incrementCheckbox = "off";
      }
    }
);

const esQueryTmp = "{\n" +
    "  \"match_all\" : {}\n" +
    "}"
</script>
<template>
  <el-row :gutter="24" :justify="'center'">
    <el-col :span="12">
      <el-form-item :label="t('sync_task.database_type')" prop="source.type">
        <el-select v-model="form.source.type" :placeholder="t('sync_task.please_choose_database_type')">
          <el-option
              v-for="item in dsTypeListData"
              :key="item.type"
              :label="item.name"
              :value="item.type"
          />
        </el-select>
      </el-form-item>
    </el-col>
  </el-row>
  <el-row :gutter="24" :justify="'center'">
    <el-col :span="12">
      <el-form-item :label="t('sync_task.database')" prop="source.datasourceId">
        <el-select
            v-model="form.source.datasourceId"
            :filterable="true"
            on-change=""
            :placeholder="t('sync_task.please_choose_database')"
        >
          <el-option
              v-for="item in form.source.dsList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
          />
        </el-select>
      </el-form-item>
    </el-col>
  </el-row>
  <el-row :gutter="24" :justify="'center'">
    <el-col :span="12">
      <el-form-item :label="t('sync_task.query_method')" prop="source.tableExtract">
        <el-select
            v-model="form.source.tableExtract"
            :placeholder="t('sync_task.please_choose_data_extraction_method')"
        >
          <el-option value="0" :label="t('sync_task.table')">{{ t('sync_task.table') }}</el-option>
          <el-option value="1" label="SQL" v-if="form.source.type !== 'elasticsearch'">SQL</el-option>
        </el-select>
      </el-form-item>
    </el-col>
  </el-row>
  <el-row
      :gutter="24"
      :justify="'center'"
      v-if="form.source.tableExtract === '0'"
  >
    <el-col :span="12">
      <el-form-item :label="t('sync_task.table')" prop="source.tables">
        <el-select-v2
            v-model="form.source.tables"
            :options="sourceTableOptions"
            :filterable="true"
            style="width: 100%"
            :placeholder="t('sync_task.please_choose_table')"
        />
      </el-form-item>
    </el-col>
  </el-row>
  <el-row
      :gutter="24"
      :justify="'center'"
      v-if="form.source.tableExtract === '1'"
  >
    <el-col :span="12">
      <el-form-item label="SQL" prop="source.query">
        <template #label>
          <span class="item-label-class">
            <span>SQL</span>
            <el-tooltip class="item" effect="dark" placement="right-start">
              <template #content>
                <p style="max-width: 500px">
                  {{ t('sync_task.sql_tip_1') }}
                </p>
                <p style="max-width: 500px">
                  {{ t('sync_task.sql_tip_2') }}
                </p>
              </template>
              <el-icon>
                <Icon name="dv-info"><dvInfo class="svg-icon"/></Icon>
              </el-icon>
            </el-tooltip>
          </span>
        </template>

        <el-input
            type="textarea"
            v-model="form.source.query"
            :placeholder="t('sync_task.please_enter_sql')"
            autosize
        />
      </el-form-item>
    </el-col>
  </el-row>
  <el-row
      :gutter="24"
      :justify="'center'"
      v-if="form.source.tableExtract === '0' && form.source.type === 'elasticsearch'"
  >
    <el-col :span="12">
      <el-form-item label="Search" prop="source.esQuery">
        <template #label>
          <span class="item-label-class">
            {{ t('sync_task.es_params_label') }}
            <el-tooltip class="item" effect="dark" placement="right-start">
              <template #content>
                <p style="max-width: 500px">
                  {{ t('sync_task.es_params_tip') }}
                </p>
              </template>
              <el-icon>
                <Icon name="dv-info"><dvInfo class="svg-icon"/></Icon>
              </el-icon>
            </el-tooltip>
          </span>
        </template>

        <el-input
            type="textarea"
            v-model="form.source.esQuery"
            :placeholder="esQueryTmp"
            autosize
        />
      </el-form-item>
    </el-col>
  </el-row>
</template>
<style lang="less" scoped>
.ed-checkbox.ed-checkbox--default {
  height: 20px;
}
.select-table-label {
  height: 22px;
  font-family: var(--de-custom_font, "PingFang");
  font-weight: 400;
  font-style: normal;
  overflow: hidden;
  text-overflow: ellipsis;
  -o-text-overflow: ellipsis;
  -webkit-text-overflow: ellipsis;
  -moz-text-overflow: ellipsis;
  white-space: nowrap;
  width: 90%;

  :nth-child(1) {
    font-size: 14px;
    line-height: 22px;
    color: #1f2329 !important;
  }

  :nth-child(2) {
    color: #8d9199;
    font-size: 12px;
    line-height: 20px;
    margin-top: 1px;
  }
}
</style>
