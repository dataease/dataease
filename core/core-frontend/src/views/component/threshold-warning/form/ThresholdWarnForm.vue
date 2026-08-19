<template>
  <el-scrollbar class="threshold-form-container">
    <div class="threshold-form-title-container">
      <span class="title-flag" />
      <span class="form-title">{{ t("threshold.notification_setting") }}</span>
    </div>
    <el-form
      ref="thresholdReciForm"
      class="threshold-form"
      :model="formState"
      :rules="reciRules"
      label-width="180px"
      label-position="top"
      :scroll-to-error="true"
    >
      <el-form-item
        :label="t('threshold.notification_method')"
        prop="reciFlagList"
      >
        <el-checkbox-group v-model="formState.reciFlagList">
          <el-checkbox
            v-for="item in platformOptions"
            :key="item.value"
            :label="item.value"
            :class="{
              'disabled-platform-option': !platformCategory[item.flag],
            }"
          >
            {{ item.name }}
          </el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <el-form-item :label="t('threshold.recipient')">
        <reci-select ref="reciSelector" v-model="reciVal" :resource-id="resourceId" :resource-flag="resourceFlag" />
      </el-form-item>

      <el-form-item :label="t('threshold.notification_email')" prop="emailList">
        <el-select
          v-model="formState.emailList"
          multiple
          filterable
          allow-create
          default-first-option
          :reserve-keyword="false"
          :placeholder="t('threshold.please_enter_email')"
          popper-class="reci-custom-email-popper"
          class="reci-custom-email-select"
        >
        </el-select>
      </el-form-item>

      <el-form-item
        v-if="platformCategory.lark"
        :label="t('report.lark_groups')"
        prop="larkGroupList"
      >
        <el-select
          ref="groupSelect"
          v-model="formState.larkGroupList"
          multiple
          collapse-tags
          collapse-tags-tooltip
          :max-collapse-tags="3"
          :placeholder="t('threshold.please_choose_lark_group')"
          style="width: 100%"
          @change="setGroupHeight"
          @remove-tag="removeLarkGroupTag"
        >
          <el-option
            v-for="item in state.larkGroupOptions"
            :key="item.chat_id"
            :label="item.name"
            :value="item.chat_id"
          >
            <span class="custom-option">
              <Icon
                name="peoples"
                class-name="view-type-icon"
                style="width: 14px; height: 14px"
                ><peoples class="svg-icon"
              /></Icon>
              <span style="float: left; margin-left: 4px; font-size: 14px">
                {{ item.name }}
              </span>
            </span>
          </el-option>

          <template #tag="{ data }">
            <div ref="groupTags" class="ed-select__tags" :style="data">
              <div class="ed-select-tags-wrapper has-prefix">
                <el-tag
                  @close="removeLarkGroupTag(id)"
                  v-for="id in formState.larkGroupList"
                  :key="id"
                  type="info"
                  closable
                >
                  <div v-if="state.larkGroupMap.get(id)">
                    <span class="custom-option">
                      <Icon
                        name="peoples"
                        class-name="view-type-icon"
                        style="width: 14px; height: 14px"
                        ><peoples class="svg-icon"
                      /></Icon>
                      <span
                        style="float: left; margin-left: 4px; font-size: 14px"
                      >
                        {{ state.larkGroupMap.get(id)?.name }}
                      </span>
                    </span>
                  </div>
                </el-tag>
              </div>
            </div>
          </template>
        </el-select>
      </el-form-item>

      <el-form-item
        v-if="platformCategory.larksuite"
        :label="t('report.larksuite_groups')"
        prop="larksuiteGroupList"
      >
        <el-select
          ref="suiteGroupSelect"
          v-model="formState.larksuiteGroupList"
          multiple
          collapse-tags
          collapse-tags-tooltip
          :max-collapse-tags="3"
          :placeholder="t('threshold.please_choose_lark_group')"
          style="width: 100%"
          @change="setSuiteGroupHeight"
          @remove-tag="removeLarksuiteGroupTag"
        >
          <el-option
            v-for="item in state.larksuiteGroupOptions"
            :key="item.chat_id"
            :label="item.name"
            :value="item.chat_id"
          >
            <span class="custom-option">
              <Icon
                name="peoples"
                class-name="view-type-icon"
                style="width: 14px; height: 14px"
                ><peoples class="svg-icon"
              /></Icon>
              <span style="float: left; margin-left: 4px; font-size: 14px">
                {{ item.name }}
              </span>
            </span>
          </el-option>

          <template #tag="{ data }">
            <div ref="suiteGroupTags" class="ed-select__tags" :style="data">
              <div class="ed-select-tags-wrapper has-prefix">
                <el-tag
                  @close="removeLarksuiteGroupTag(id)"
                  v-for="id in formState.larksuiteGroupList"
                  :key="id"
                  type="info"
                  closable
                >
                  <div v-if="state.larksuiteGroupMap.get(id)">
                    <span class="custom-option">
                      <Icon
                        name="peoples"
                        class-name="view-type-icon"
                        style="width: 14px; height: 14px"
                        ><peoples class="svg-icon"
                      /></Icon>
                      <span
                        style="float: left; margin-left: 4px; font-size: 14px"
                      >
                        {{ state.larksuiteGroupMap.get(id)?.name }}
                      </span>
                    </span>
                  </div>
                </el-tag>
              </div>
            </div>
          </template>
        </el-select>
      </el-form-item>

      <el-form-item label="Webhook" prop="webhook">
        <el-select
          v-model="formState.webhookList"
          class="custom-clear-class"
          clearable
          multiple
          collapse-tags
          collapse-tags-tooltip
          clear-icon="CircleCloseFilled"
          :placeholder="t('commons.please_select') + 'Webhook'"
          style="width: 100%"
        >
          <el-option
            v-for="item in state.webhookOptions"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item :label="t('threshold.notification_content')" prop="msgType">
        <el-radio-group v-model="formState.msgType" @change="msgTypeChange">
          <el-radio :label="0"> {{ t("threshold.default_msg") }} </el-radio>
          <el-radio :label="1"> {{ t("threshold.custom_msg") }} </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item
        :label="t('threshold.msg_title')"
        prop="msgTitle"
        v-if="formState.msgType"
      >
        <el-input v-model="formState.msgTitle" style="width: 100%" />
      </el-form-item>
      <el-form-item
        :label="t('threshold.msg_content')"
        prop="msgContent"
        v-if="formState.msgType"
      >
        <de-rich-text
          v-model="formState.msgContent"
          :fieldList="fieldList"
          @change="msgContentChange"
        ></de-rich-text>
      </el-form-item>
      <el-form-item label="">
        <template #label>
          <div
            style="
              display: flex;
              align-items: center;
              justify-content: space-between;
            "
          >
            {{ t("threshold.msg_preview") }}
          </div>
        </template>
        <div class="notification-content">
          <div class="notification-item">
            <el-icon>
              <Icon name="icon_notification_filled"
                ><icon_notification_filled class="svg-icon"
              /></Icon>
            </el-icon>
            <div class="notification-detail">
              <div class="title">{{ formState.msgTitle }}</div>
              <div
                ref="previewRef"
                class="content"
                v-html="sanitizeHtml(previewHml || formState.msgContent)"
              />
            </div>
          </div>
        </div>
      </el-form-item>

      <el-form-item :label="t('threshold.repeat_send')" prop="repeatSend">
        <el-switch class="status-switch" v-model="formState.repeatSend" />
      </el-form-item>

      <el-form-item :label="t('threshold.show_field_value')" prop="showFieldValue">
        <el-switch class="status-switch" v-model="formState.showFieldValue" />
      </el-form-item>
    </el-form>
  </el-scrollbar>
</template>

<script lang="ts" setup>
import peoples from "@/assets/svg/peoples.svg";
import icon_notification_filled from "@/assets/svg/icon_notification_filled.svg";
import { reactive, ref, onMounted, computed, nextTick } from "vue";
import {
  reciRules,
  LarkGroupItem,
  PlatformCategory,
  platformOptions,
} from "./FormPage";
import { useI18n } from "@/hooks/web/useI18n";
import { Icon } from "@/components/icon-custom";
import { sanitizeHtml } from "@/utils/utils";
import deRichText from "@/components/rich-text/TinymacEditorAlarm.vue";
import {
  queryCategoryStatusApi,
  larkGroupOptionApi,
  larksuiteGroupOptionApi,
} from "../../../menu/system/report/api";
import { ElMessage } from "element-plus-secondary";
import { contentTemplate, quotaTemplate } from "./FormPage";
import { thresholdPreviewApi, webhookOptionsApi } from "../api";
import ReciSelect from "../../reci-select/index.vue";

const { t } = useI18n();

const props = defineProps({
  warnFormData: {
    type: Object,
    default: () => {},
  },
  isEdit: {
    type: Boolean,
    default: false,
  },
  resourceTable: {
    type: String,
    default: "core",
  },
  fieldList: {
    type: Array,
    default: () => [],
  },
  resourceId: {
    type: String,
    default: '',
  },
  resourceType: {
    type: String,
    default: '',
  },
});

const resourceFlag = computed(() => props.resourceType === 'dataV' ? 2 : 1);

const fieldList = computed(() => {
  const defaultList = [
    {
      id: "0",
      name: t("threshold.detection_time"),
      deType: 1,
      groupType: "d",
    },
    {
      id: "1",
      name: t("threshold.trigger_alarm"),
      deType: 2,
      groupType: "d",
    },
    {
      id: "2",
      name: t("threshold.threshold_record"),
      deType: 2,
      groupType: "d",
    },
  ];
  return [...defaultList, ...props.fieldList];
});
const previewRef = ref()
const thresholdReciForm = ref(null);
const reciVal = ref([] as string[]);
const reciSelector = ref();
const groupSelect = ref();
const groupTags = ref();
const suiteGroupSelect = ref();
const suiteGroupTags = ref();
const previewHml = ref("");
const rulesText = ref("");
let debounceTimer: ReturnType<typeof setTimeout> | null = null;
const platformCategory = ref({
  email: true,
} as PlatformCategory);
const state = reactive({
  larkGroupOptions: [] as LarkGroupItem[],
  larksuiteGroupOptions: [] as LarkGroupItem[],
  larkGroupMap: new Map<string, LarkGroupItem>(),
  larksuiteGroupMap: new Map<string, LarkGroupItem>(),
  webhookOptions: [] as any[],
});
const defaultFormData = ref({
  reciFlagList: [1],
  uidList: [] as string[],
  ridList: [] as string[],
  emailList: [],
  larkGroupList: [],
  larksuiteGroupList: [],
  msgType: 0,
  msgTitle: t("threshold.abnormal_alarm"),
  msgContent: "",
  repeatSend: true,
  showFieldValue: false,
  webhookList: [] as string[],
});
const formState = ref({
  ...defaultFormData.value,
});

// method area
const setHeight = () => {
  if (reciSelector?.value?.setHeight) {
    reciSelector.value.setHeight();
  }
};
const setGroupHeight = () => {
  if (!groupSelect.value) return;
  setTimeout(() => {
    const input = groupSelect.value.$el.querySelector(
      "input"
    ) as HTMLInputElement;
    const _tags = groupTags.value;
    nextTick(() => {
      input.style.minHeight = `${_tags.clientHeight + 8}px`;
    });
  }, 0);
};
const setSuiteGroupHeight = () => {
  if (!suiteGroupSelect.value) return;
  setTimeout(() => {
    const input = suiteGroupSelect.value.$el.querySelector(
      "input"
    ) as HTMLInputElement;
    const _tags = suiteGroupTags.value;
    nextTick(() => {
      input.style.minHeight = `${_tags.clientHeight + 8}px`;
    });
  }, 0);
};
const removeLarkGroupTag = (val) => {
  let len = formState.value.larkGroupList.length;
  while (len--) {
    const id = formState.value.larkGroupList[len];
    if (id === val) {
      formState.value.larkGroupList.splice(len, 1);
    }
  }
  nextTick(() => {
    setGroupHeight();
  });
};
const removeLarksuiteGroupTag = (val) => {
  let len = formState.value.larksuiteGroupList.length;
  while (len--) {
    const id = formState.value.larksuiteGroupList[len];
    if (id === val) {
      formState.value.larksuiteGroupList.splice(len, 1);
    }
  }
  nextTick(() => {
    setSuiteGroupHeight();
  });
};
const getFormData = async () => {
  if (!validateCur()) {
    return;
  }
  const p = new Promise((r, e) => {
    thresholdReciForm?.value?.validate((valid) => {
      r(valid && formatReci2Data());
    });
  });

  return await p;
};

const formatReci2Data = () => {
  const tempUidList = [] as string[];
  const tempRidList = [] as string[];
  reciVal.value.forEach((id) => {
    if (id.startsWith("0")) {
      tempRidList.push(id.substring(1));
    } else {
      tempUidList.push(id.substring(1));
    }
  });
  formState.value.uidList = [...tempUidList];
  formState.value.ridList = [...tempRidList];
  return formState.value;
};

const formatReci2Form = () => {
  if (props.isEdit && props.warnFormData) {
    for (const key in formState.value) {
      formState.value[key] = props.warnFormData[key];
    }
  }
  if (formState.value.uidList) {
    formState.value.uidList.forEach((id) => {
      reciVal.value.push(`1${id}`);
    });
  } else {
    formState.value.uidList = [];
  }
  if (formState.value.ridList) {
    formState.value.ridList.forEach((id) => {
      reciVal.value.push(`0${id}`);
    });
  } else {
    formState.value.ridList = [];
  }
};
const loadWebhookOptions = () => {
  webhookOptionsApi().then((res) => {
    state.webhookOptions = res.data;
  });
};
const loadPlatformStatus = async () => {
  const res = await queryCategoryStatusApi();
  if (res["data"]) {
    const list: any[] = res["data"] as any[];
    list.forEach((item) => {
      platformCategory.value[item.name] = item.enable;
    });
    if (platformCategory.value.lark) {
      loadLarkGroupOptions();
    }
    if (platformCategory.value.larksuite) {
      loadLarksuiteGroupOptions();
    }
  }
};
const loadLarkGroupOptions = () => {
  larkGroupOptionApi().then((res) => {
    const data = res.data;
    if (data.valid) {
      state.larkGroupOptions = data.groupList;
      state.larkGroupMap = state.larkGroupOptions.reduce((acc, item) => {
        acc.set(item.chat_id, item);
        return acc;
      }, new Map());
    }
  });
};
const loadLarksuiteGroupOptions = () => {
  larksuiteGroupOptionApi().then((res) => {
    const data = res.data;
    if (data.valid) {
      state.larksuiteGroupOptions = data.groupList;
      state.larksuiteGroupMap = state.larksuiteGroupOptions.reduce(
        (acc, item) => {
          acc.set(item.chat_id, item);
          return acc;
        },
        new Map()
      );
    }
  });
};
const validateReciFlag = () => {
  return !reciVal.value?.length || formState.value.reciFlagList?.length;
};
const validateCur = () => {
  const valid =
    reciVal.value?.length ||
    formState.value.emailList?.length ||
    formState.value.larkGroupList?.length ||
    formState.value.larksuiteGroupList?.length ||
    formState.value.webhookList?.length;
  if (valid && validateReciFlag()) {
    return true;
  }
  ElMessage.error(t("threshold.notification_methods_cannot_be_empty"));
  return false;
};
const setDefaultContent = (thresholdRules?: string) => {
  if (!thresholdRules) {
    return;
  }
  rulesText.value = thresholdRules;
  const ruleObj = JSON.parse(thresholdRules);
  const items = ruleObj.items;
  const fieldMap = fieldList.value
    .filter((node) => ![0, 1].includes(node["id"]))
    .reduce((acc, item) => {
      acc[item["id"]] = item;
      return acc;
    }, {});

  const stack = [...items];
  const matchFieldList = [];
  const quotoIdMap = {}
  while (stack.length) {
    const node = stack.pop();
    if (node.type === "item") {
      const fieldId = node.fieldId;
      const field = fieldMap[fieldId];
      if (!quotoIdMap[fieldId]) {
        matchFieldList.push(field);
        quotoIdMap[fieldId] = true
      }
      
    } else {
      const subTree = node.subTree;
      subTree.items.forEach((kid) => stack.push(kid));
    }
  }

  if (matchFieldList.length && !formState.value.msgType) {
    const quota = matchFieldList
      .map((row) => {
        const tempText = eval(quotaTemplate);
        return tempText;
      })
      .join(" , ");
    if (quota?.length) {
      formState.value.msgContent = eval(contentTemplate);
    }
  }
  refreshView();
};
const msgTypeChange = (val) => {
  if (!val) {
    formState.value.msgTitle = t("threshold.abnormal_alarm");
    setDefaultContent(rulesText.value);
  }
};
const msgContentChange = (val) => {
  formState.value.msgContent = val;
  if (debounceTimer) {
    clearTimeout(debounceTimer);
  }
  debounceTimer = setTimeout(() => {
    refreshView();
  }, 2000);
};
const refreshView = () => {
  if (
    !props.warnFormData["chartId"] ||
    !rulesText.value ||
    !formState.value.msgContent
  ) {
    return;
  }
  const param = {
    thresholdRules: rulesText.value,
    msgContent: formState.value.msgContent,
    chartId: props.warnFormData["chartId"],
    resourceTable: props.resourceTable,
  };
  thresholdPreviewApi(param).then((res) => {
    previewHml.value = res.data;
  });
};

const init = async () => {
  loadWebhookOptions();
  await loadPlatformStatus();
  formatReci2Form();
};
const setOwnSelectHeight = () => {
  setHeight();
  setGroupHeight();
  setSuiteGroupHeight();
};
defineExpose({
  getFormData,
  setOwnSelectHeight,
  setDefaultContent,
});

onMounted(() => {
  init();
});
</script>
<style lang="less">
.reci-custom-email-popper {
  display: none !important;
}
</style>
<style scoped lang="less">
.custom-option {
  font-size: 14px;
  display: flex;
  align-items: center;
}
.view-type-icon {
  color: var(--ed-color-primary);
  width: 22px;
  height: 16px;
}

.threshold-form-container {
  height: 100%;
  margin: 0 auto;

  .threshold-form-title-container {
    display: flex;
    align-items: center;
    height: 24px;
    line-height: 24px;
    margin-bottom: 16px;
    .title-flag {
      height: 16px;
      line-height: 16px;
      border-left: 2px solid var(--ed-color-primary, #3370ff);
    }
    .form-title {
      color: #1f2329;
      font-weight: 500;
      font-family: var(--de-custom_font, "PingFang");
      line-height: 24px;
      font-size: 16px;
      padding-left: 8px;
    }
  }

  .threshold-form {
    width: 100%;
    padding-bottom: 16px;
    .ed-form-item {
      margin-bottom: 16px;
    }
    .is-error {
      margin-bottom: 40px !important;
    }
    :deep(.disabled-platform-option) {
      display: none;
    }
  }
  :deep(.ed-form-item__label) {
    line-height: 22px;
    height: 22px;
  }

  .notification-content {
    width: 848px;
    min-height: 166px;
    height: auto;
    padding: 16px;
    border-radius: 4px;
    background: #f5f6f7;
    .notification-item {
      display: flex;
      .ed-icon {
        font-size: 16px;
        width: 32px;
        height: 32px;
        color: #ffffff;
        background-color: var(--ed-color-primary, #3370ff);
        margin-right: 8px;
        border-radius: 50%;
      }

      .notification-detail {
        width: 303px;
        min-height: 134px;
        height: auto;
        padding: 16px;
        border-radius: 4px;
        border: 1px solid #dee0e3;
        background-color: #ffffff;

        .title {
          font-size: 16px;
          font-weight: 500;
          line-height: 24px;
        }

        .content {
          font-family: var(--de-custom_font, "PingFang");
          font-size: 14px;
          font-weight: 400;
          line-height: 22px;
        }
      }
    }
  }
}

.dv-selector {
  width: 100%;
}

.label-content-details {
  width: 100%;
  display: flex;
  align-items: center;
}
.reci-custom-email-select {
  width: 100%;
  :deep(.ed-input__suffix) {
    display: none;
  }
  :deep(.ed-select-tags-wrapper) {
    display: flex;
    flex-wrap: wrap;
    grid-row-gap: 4px;
  }
  :deep(.ed-tag) {
    margin: 0px 4px 0 0;
  }
}

.ed-select__tags {
  .ed-select-tags-wrapper {
    display: flex;
    flex-wrap: wrap;
    grid-row-gap: 4px;
  }
  :deep(.ed-tag) {
    margin: 0px 4px 0 0;
  }
}
.custom-clear-class {
  :deep(.ed-input__suffix-inner) {
    column-gap: 4px;
    i:first-child {
      &:hover {
        color: var(--ed-color-primary, #3370ff);
      }
    }
  }
}
</style>
