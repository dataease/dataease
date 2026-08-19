<script lang="ts" setup>
import icon_down_outlined1 from "@/assets/svg/icon_down_outlined-1.svg";
import icon_down_outlined from "@/assets/svg/icon_down_outlined.svg";
import icon_add_outlined from "@/assets/svg/icon_add_outlined.svg";
import { computed, reactive, ref, toRefs, watch } from "vue";
import { useI18n } from "@/hooks/web/useI18n";
import { ElIcon, FormInstance, FormRules } from "element-plus-secondary";
import { ElForm, ElMessage } from "element-plus-secondary";
import { cloneDeep } from "lodash-es";
import { getSchemaApi } from "@/api/sync/syncDatasource";
import { Base64 } from "js-base64";
import { CustomPassword } from "@/components/custom-password";
import { dsTypes } from "./option";
import { Icon } from "@/components/icon-custom";
import dvInfo from "@/assets/svg/dv-info.svg";

const { t } = useI18n();
const prop = withDefaults(defineProps<{ form: any; activeStep: any }>(), {
  form: () => {
    return {
      type: "API",
      name: "",
      description: "",
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
        queryTimeout: 30
      }
    };
  },
  activeStep: () => {
    return {
      required: false,
      default: 1,
      type: Number
    };
  }
});
const { form, activeStep } = toRefs(prop);
const state = reactive({
  itemRef: []
});
const schemas = ref([]);
const dsForm = ref<FormInstance>();
const defaultRule = {
  name: [
    {
      required: true,
      message: t("sync_datasource.input_name"),
      trigger: "blur"
    },
    {
      min: 2,
      max: 25,
      message: t("sync_datasource.input_limit_2_25", [2, 25]),
      trigger: "blur"
    }
  ]
};
const rule = ref<FormRules>(cloneDeep(defaultRule));
const initForm = (type) => {

  if (type !== "API") {
    const dsType = dsTypes.find((item) => item.type === type);
    form.value.configuration = {
      dataBase: "",
      extraParams: dsType?.extraParams,
      username: "",
      password: "",
      host: "",
      authMethod: "",
      port: 0,
      initialPoolSize: 5,
      minPoolSize: 5,
      maxPoolSize: 5,
      queryTimeout: 30,
      connectionType: "sid",
      bePort: undefined,
      beIp: undefined,
      fePort: undefined,
      url: "",
      replication: undefined,
      defaultReplication: ""
    };
    schemas.value = [];
    rule.value = cloneDeep(defaultRule);
    setRules();
  }

  form.value.type = type;
  setTimeout(() => {
    dsForm.value?.clearValidate();
  }, 0);
};

const notApiExcelConfig = computed(() => form.value.type !== "API");

const isElasticsearchConfig = computed(() => form.value.type === "elasticsearch");

const authMethodList = [
  {
    id: "passwd",
    label: t("sync_datasource.passwd")
  },
  {
    id: "kerberos",
    label: "Kerberos"
  }
];
const setRules = () => {
  const configRules = {
    "configuration.dataBase": [
      {
        required: true,
        message: t("sync_datasource.please_input_data_base"),
        trigger: "blur"
      }
    ],
    "configuration.authMethod": [
      {
        required: true,
        message: t("sync_datasource.please_select_oracle_type"),
        trigger: "blur"
      }
    ],
    "configuration.username": [
      {
        required: true,
        message: t("sync_datasource.please_input_user_name"),
        trigger: "blur"
      }
    ],
    "configuration.password": [
      {
        required: true,
        message: t("sync_datasource.please_input_password"),
        trigger: "blur"
      }
    ],
    "configuration.host": [
      {
        required: true,
        message: t("common.inputText"),
        trigger: "blur"
      }
    ],
    "configuration.extraParams": [
      {
        required: false,
        message: t("sync_datasource.please_input_url"),
        trigger: "blur"
      }
    ],
    "configuration.port": [
      {
        required: true,
        message: t("common.inputText"),
        trigger: "blur"
      }
    ],
    "configuration.initialPoolSize": [
      {
        required: true,
        message: t("common.inputText") + " " + t("sync_datasource.initial_pool_size"),
        trigger: "blur"
      }
    ],
    "configuration.minPoolSize": [
      {
        required: true,
        message: t("common.inputText") + " " + t("sync_datasource.min_pool_size"),
        trigger: "blur"
      }
    ],
    "configuration.maxPoolSize": [
      {
        required: true,
        message: t("common.inputText") + " " + t("sync_datasource.max_pool_size"),
        trigger: "blur"
      }
    ],
    "configuration.queryTimeout": [
      {
        required: true,
        message: t("common.inputText") + " " + t("sync_datasource.query_timeout"),
        trigger: "blur"
      }
    ]
  };
  if (
    ["oracle", "sqlServer", "pg", "redshift", "db2"].includes(form.value.type)
  ) {
    configRules["configuration.schema"] = [
      {
        required: true,
        message: t("sync_datasource.please_choose_schema"),
        trigger: "blur"
      }
    ];
  }
  rule.value = { ...cloneDeep(configRules), ...cloneDeep(defaultRule) };
};

watch(
  () => form.value.type,
  (val) => {
    if (val !== "API") {
      rule.value = cloneDeep(defaultRule);
      setRules();
    }
  },
  {
    immediate: true
  }
);

const showPriority = ref(false);

const submitForm = () => {
  dsForm.value?.clearValidate();
  return dsForm.value?.validate;
};

const clearForm = () => {
  return dsForm.value?.clearValidate();
};

const resetForm = () => {
  dsForm.value?.resetFields();
};

const showSchema = ref(false);

const getDsSchema = () => {
  showSchema.value = true;
  const validateFrom = dsForm.value?.validate!;
  validateFrom((val) => {
    showSchema.value = false;
    if (val) {
      const request = JSON.parse(JSON.stringify(form.value));
      request.configuration = Base64.encode(
        JSON.stringify(request.configuration)
      );
      getSchemaApi(request).then((res) => {
        schemas.value = res.data;
        ElMessage.success(t("commons.success"));
      });
    }
  });
};

const validatorSchema = () => {
  dsForm.value?.validateField("configuration.schema");
};
const dorisFeLabelPrefix = computed(() => {
  return (form.value.type === "doris" ? "FE " : "");
});
defineExpose({
  submitForm,
  resetForm,
  initForm,
  clearForm
});
const defaultReplication = computed(() => {
  if (form.value.configuration.defaultReplication) {
    return `（${form.value.configuration.defaultReplication}）`;
  }
  return '';
});
</script>

<template>
  <div class="editor-detail">
    <div class="detail-inner">
      <el-form
        ref="dsForm"
        :model="form"
        :rules="rule"
        label-width="180px"
        label-position="top"
        require-asterisk-position="right"
        :scroll-to-error="true"
      >
        <el-form-item
          :label="t('sync_datasource.name' )"
          prop="name"
          v-show="activeStep !== 2"
        >
          <el-input
            v-model="form.name"
            autocomplete="off"
            :placeholder="t('sync_datasource.input_name')"
          />
        </el-form-item>
        <el-form-item
          :label="t('sync_datasource.description')"
          v-show="activeStep !== 2"
        >
          <el-input
            class="description-text"
            type="textarea"
            :placeholder="t('common.inputText')"
            v-model="form.desc"
            :row="10"
            :maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <template v-if="notApiExcelConfig && !isElasticsearchConfig">
          <el-form-item :label="dorisFeLabelPrefix + t('sync_datasource.host')" prop="configuration.host">
            <el-input
              v-model="form.configuration.host"
              :placeholder="t('common.inputText') + ' ' + dorisFeLabelPrefix + t('sync_datasource.host')"
              autocomplete="off"
              :maxlength="255"
            />
          </el-form-item>
          <el-form-item :label="dorisFeLabelPrefix + t('sync_datasource.port')" prop="configuration.port">
            <el-input-number
              v-model="form.configuration.port"
              autocomplete="off"
              step-strictly
              class="text-left"
              :min="0"
              :max="65535"
              :placeholder="t('common.inputText') + ' ' + dorisFeLabelPrefix + t('sync_datasource.port')"
              controls-position="right"
            />
          </el-form-item>
          <el-form-item
            v-if="form.type == 'doris'"
            :label="'FE ' + t('sync_datasource.http_port')"
            prop="configuration.fePort"
          >
            <el-input-number
              v-model="form.configuration.fePort"
              autocomplete="off"
              step-strictly
              class="text-left"
              :min="0"
              :max="65535"
              :placeholder="
                'FE ' + t('sync_datasource.http_port') + '（8030）'
              "
              controls-position="right"
            />
          </el-form-item>
          <el-form-item
            v-if="form.type == 'doris'"
            :label="'BE ' + t('sync_datasource.host')"
            prop="configuration.beIp"
          >
            <el-input
              v-model="form.configuration.beIp"
              :placeholder="'BE ' + t('sync_datasource.host') + ' ' + form.configuration.host"
              autocomplete="off"
              :maxlength="255"
            />
          </el-form-item>
          <el-form-item
            v-if="form.type == 'doris'"
            :label="'BE ' + t('sync_datasource.http_port')"
            prop="configuration.bePort"
          >
            <el-input-number
              v-model="form.configuration.bePort"
              autocomplete="off"
              step-strictly
              class="text-left"
              :min="0"
              :max="65535"
              :placeholder="
                'BE ' + t('sync_datasource.http_port') + '（8040）'
              "
              controls-position="right"
            />
          </el-form-item>
          <el-form-item
            v-if="form.type == 'doris'"
            prop="configuration.replication"
          >
            <template #label>
              <span class="item-label-class">
                {{ t("sync_datasource.replication") }}
                <el-tooltip class="item" effect="dark" placement="right-start">
                <template #content>
                  <p style="max-width: 500px">
                    {{ t("sync_datasource.replication_tip") }}
                  </p>
                </template>
                <el-icon>
                  <Icon name="dv-info"><dvInfo class="svg-icon" /></Icon>
                </el-icon>
              </el-tooltip>
              </span>
            </template>
            <el-input-number
              v-model="form.configuration.replication"
              autocomplete="off"
              step-strictly
              class="text-left"
              :min="1"
              :max="10000"
              :placeholder="t('sync_datasource.replication') + defaultReplication"
              controls-position="right"
            />
          </el-form-item>
          <el-form-item
            :label="t('sync_datasource.data_base')"
            prop="configuration.dataBase"
          >
            <el-input
              v-model="form.configuration.dataBase"
              :placeholder="t('sync_datasource.please_input_data_base')"
              autocomplete="off"
              :maxlength="255"
            />
          </el-form-item>
          <el-form-item
            :label="t('sync_datasource.auth_method')"
            prop="configuration.authMethod"
            v-if="form.type === 'presto'"
          >
            <el-select
              :placeholder="t('common.inputText') + ' ' + t('sync_datasource.auth_method')"
              v-model="form.configuration.authMethod"
              class="de-select"
            >
              <el-option
                v-for="item in authMethodList"
                :key="item.id"
                :label="item.label"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item
            :label="t('sync_datasource.client_principal')"
            prop="configuration.username"
            v-if="form.type === 'presto'"
          >
            <el-input
              :placeholder="
                t('common.inputText') + ' ' + t('sync_datasource.client_principal')
              "
              v-model="form.configuration.username"
              autocomplete="off"
              :maxlength="255"
            />
          </el-form-item>
          <el-form-item
            :label="t('sync_datasource.keytab_Key_path')"
            prop="configuration.password"
            v-if="form.type === 'presto'"
          >
            <CustomPassword
              :placeholder="
                t('common.inputText' )+ ' ' + t('sync_datasource.keytab_Key_path')
              "
              show-password
              type="password"
              v-model="form.configuration.password"
              :maxlength="255"
            />
            <p>
              {{ t("sync_datasource.kerbers_info") }}
            </p>
          </el-form-item>
          <el-form-item
            :label="t('sync_datasource.user_name')"
            v-if="form.type !== 'presto'"
            prop="configuration.username"
          >
            <el-input
              :placeholder="t('common.inputText')  + ' ' +  t('sync_datasource.user_name')"
              v-model="form.configuration.username"
              autocomplete="off"
              :maxlength="255"
            />
          </el-form-item>
          <el-form-item
            :label="t('sync_datasource.password')"
            v-if="form.type !== 'presto'"
          >
            <CustomPassword
              :placeholder="t('common.inputText') + ' ' + t('sync_datasource.password')"
              show-password
              type="password"
              v-model="form.configuration.password"
            />
          </el-form-item>
          <el-form-item :label="t('sync_datasource.extra_params')">
            <el-input
              :placeholder="
                t('common.inputText' )+ ' ' + t('sync_datasource.extra_params')
              "
              v-model="form.configuration.extraParams"
              autocomplete="off"
              :maxlength="2048"
            />
          </el-form-item>
          <el-form-item
            v-if="form.type == 'oracle'"
            :label="t('sync_datasource.connection_mode')"
            prop="configuration.connectionType"
          >
            <el-radio v-model="form.configuration.connectionType" label="sid"
            >{{ t("sync_datasource.oracle_sid") }}
            </el-radio>
            <el-radio
              v-model="form.configuration.connectionType"
              label="serviceName"
            >
              {{ t("sync_datasource.oracle_service_name") }}
            </el-radio>
          </el-form-item>
          <el-form-item
            v-if="
              ['oracle', 'sqlServer', 'pg', 'redshift', 'db2'].includes(
                form.type
              )
            "
            class="schema-label"
            :prop="showSchema ? '' : 'configuration.schema'"
          >
            <template v-slot:label>
              <span class="name"
              >{{ t("sync_datasource.schema") }}<i class="required"
              /></span>
              <el-button text size="small" @click="getDsSchema()">
                <template #icon>
                  <Icon name="icon_add_outlined">
                    <icon_add_outlined class="svg-icon" />
                  </Icon>
                </template>
                {{ t("sync_datasource.get_schema") }}
              </el-button>
            </template>
            <el-select
              v-model="form.configuration.schema"
              filterable
              :placeholder="t('common.please_select')"
              class="de-select"
              @change="validatorSchema"
              @blur="validatorSchema"
            >
              <el-option
                v-for="item in schemas"
                :key="item"
                :label="item"
                :value="item"
              />
            </el-select>
          </el-form-item>
          <span
            v-if="!['es', 'api', 'mongo'].includes(form.type)"
            class="de-expand"
            @click="showPriority = !showPriority"
          >{{ t("sync_datasource.priority") }}
            <el-icon>
              <Icon
              ><component
                :is="showPriority ? icon_down_outlined : icon_down_outlined1"
                class="svg-icon"
              ></component
              ></Icon>
            </el-icon>
          </span>
          <template v-if="showPriority">
            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item
                  :label="t('sync_datasource.initial_pool_size')"
                  prop="configuration.initialPoolSize"
                >
                  <el-input-number
                    v-model="form.configuration.initialPoolSize"
                    controls-position="right"
                    autocomplete="off"
                    :placeholder="
                      t('common.inputText' )+ ' ' + t('sync_datasource.initial_pool_size')
                    "
                    type="number"
                    :min="0"
                    :max="100000000"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item
                  :label="t('sync_datasource.min_pool_size')"
                  prop="configuration.minPoolSize"
                >
                  <el-input-number
                    v-model="form.configuration.minPoolSize"
                    controls-position="right"
                    autocomplete="off"
                    :placeholder="
                      t('common.inputText' )+ ' ' + t('sync_datasource.min_pool_size')
                    "
                    type="number"
                    :min="0"
                    :max="100000000"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item
                  :label="t('sync_datasource.max_pool_size')"
                  prop="configuration.maxPoolSize"
                >
                  <el-input-number
                    v-model="form.configuration.maxPoolSize"
                    controls-position="right"
                    autocomplete="off"
                    :placeholder="
                      t('common.inputText' )+ ' ' + t('sync_datasource.max_pool_size')
                    "
                    type="number"
                    :min="0"
                    :max="100000000"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item
                  :label="t('sync_datasource.query_timeout')"
                  prop="configuration.queryTimeout"
                >
                  <el-input
                    v-model="form.configuration.queryTimeout"
                    autocomplete="off"
                    :placeholder="
                      t('common.inputText' )+ ' ' + t('sync_datasource.query_timeout')
                    "
                    class="input-with-append"
                    type="number"
                    :min="0"
                    :max="100000000"
                  >
                    <template v-slot:append>{{ t("cron.second") }}</template>
                  </el-input>
                </el-form-item>
              </el-col>
            </el-row>
          </template>
        </template>
        <template v-if="isElasticsearchConfig">
          <el-form-item
            :label="$t('datasource.datasource_url')"
            prop="configuration.url"
          >
            <el-input
              v-model="form.configuration.url"
              :placeholder="$t('datasource.please_input_datasource_url')"
              autocomplete="off"
            />
          </el-form-item>
          <el-form-item
            :label="t('sync_datasource.user_name')"
            v-if="form.type !== 'presto'"
          >
            <el-input
              :placeholder="t('common.inputText')  + ' ' +  t('sync_datasource.user_name')"
              v-model="form.configuration.username"
              autocomplete="off"
              :maxlength="255"
            />
          </el-form-item>
          <el-form-item
            :label="t('sync_datasource.password')"
            v-if="form.type !== 'presto'"
          >
            <CustomPassword
              :placeholder="t('common.inputText') + ' ' + t('sync_datasource.password')"
              show-password
              type="password"
              v-model="form.configuration.password"
            />
          </el-form-item>
        </template>
      </el-form>
    </div>
  </div>
</template>

<style lang="less" scoped>
.editor-detail {
  width: 100%;
  display: flex;
  justify-content: center;

  .ed-radio {
    height: 22px;
  }

  .execute-rate-cont {
    background: #f5f6f7;
    border-radius: 4px;
    padding: 16px;
    margin-top: -8px;
  }

  .de-select {
    width: 100%;
  }

  .ed-input-number {
    width: 100%;
  }

  .input-with-append {
    :deep(.ed-input-group__append) {
      width: 55px;
      background: #eff0f1;
      color: #1f2329;
    }
  }

  :deep(.is-controls-right > span) {
    background: #fff;
  }

  .de-expand {
    font-family: var(--de-custom_font, "PingFang");
    font-size: 14px;
    font-weight: 400;
    line-height: 22px;
    color: var(--ed-color-primary);
    cursor: pointer;
    display: inline-flex;
    align-items: center;
    margin-bottom: 16px;

    .ed-icon {
      margin-left: 4px;
    }
  }

  :deep(.ed-date-editor.ed-input) {
    .ed-input__wrapper {
      width: 100%;
    }

    width: 100%;
  }

  .simple-cron {
    height: 32px;

    .ed-select,
    .ed-input-number {
      width: 140px;
      margin: 0 8px;
    }
  }

  .detail-inner {
    width: 800px;
    padding-top: 8px;

    .ed-form-item {
      margin-bottom: 16px;

      :deep(.ed-form-item__error) {
        position: relative;
      }
    }

    .description-text {
      :deep(.ed-textarea__inner) {
        height: 92px;
      }
    }

    .table-info-mr {
      margin: 28px 0 12px 0;
    }

    .info-update {
      height: 22px;
      width: 100%;
      display: flex;
      align-items: center;
      font-family: var(--de-custom_font, "PingFang");
      font-size: 14px;
      font-style: normal;
      font-weight: 400;
      line-height: 22px;
      justify-content: center;

      .update-info-line {
        width: 208px;
        height: 1px;
        background: #bcbdbf;
        margin: 0 8px;
      }

      .info-text,
      .update-text {
        padding-left: 16px;
        position: relative;
        color: #1f2329;
        font-weight: 400;
        font-family: var(--de-custom_font, "PingFang");
        font-size: 14px;
        font-style: normal;
        line-height: 22px;

        &::before {
          width: 8px;
          height: 8px;
          content: "";
          position: absolute;
          left: 0;
          top: 50%;
          transform: translateY(-50%);
          border: 1px solid var(--ed-color-primary);
          border-radius: 50%;
        }

        &.active {
          font-weight: 500;
        }

        &.active::before {
          border: none;
          background: var(--ed-color-primary);
        }
      }
    }

    .detail-operate {
      text-align: right;
      padding: 8px 0;
    }

    .flex-space {
      display: flex;
      align-items: center;
    }
  }
}
</style>

<style lang="less">
.item-label-class {
  display: inline-flex;
  align-items: center;

  i {
    margin-left: 4.67px;
  }
}

.schema-label {
  .ed-form-item__label {
    display: flex !important;
    justify-content: space-between;
    padding-right: 0;

    &::after {
      display: none;
    }

    .name {
      .required::after {
        content: "*";
        color: #f54a45;
        margin-left: 2px;
      }
    }
  }
}
</style>
