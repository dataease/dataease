<template>
  <el-dialog
    v-model="dialogVisible"
    :show-close="true"
    :close-on-click-modal="false"
    :title="title"
    @close="closeHandler"
    width="600"
  >
    <div class="threshold-reci-container">
      <el-form
        ref="thresholdReciForm"
        class="threshold-form"
        :model="formState"
        :rules="reciRules"
        label-width="180px"
        label-position="top"
        :scroll-to-error="true"
      >
        <el-form-item :label="t('threshold.notification_method')" prop="reciFlagList">
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
          <reci-select ref="reciSelector" v-model="reciVal" />
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
      </el-form>
      
    </div>
    <div class="reci-dialog-footer">
      <el-button secondary @click="closeHandler">{{ t('chart.cancel') }}</el-button>
      <el-button type="primary" @click="saveHandler">{{ t('common.sure') }}</el-button>
    </div>
  </el-dialog>
</template>

<script lang="ts" setup>
import peoples from "@/assets/svg/peoples.svg";
import { ref, reactive, nextTick } from "vue";
import { useI18n } from "@/hooks/web/useI18n";
import {
  queryCategoryStatusApi,
  larkGroupOptionApi,
  larksuiteGroupOptionApi,
} from "../../../menu/system/report/api";
import {
  reciRules,
  LarkGroupItem,
  PlatformCategory,
  platformOptions,
} from "./FormPage";
import { ElMessage } from "element-plus-secondary";
import { thresholdBatchReciApi, webhookOptionsApi } from "../api";
import ReciSelect from '../../reci-select/index.vue'

const { t } = useI18n();
const title = ref(t('threshold.recipient_setting'));
const dialogVisible = ref(false);
const chartIdList = ref([] as string[]);
const platformCategory = ref({ email: true } as PlatformCategory);

const thresholdReciForm = ref(null);
const reciVal = ref([] as string[]);
const reciSelector = ref()
const groupSelect = ref();
const groupTags = ref();
const suiteGroupSelect = ref();
const suiteGroupTags = ref();

const state = reactive({
  larkGroupOptions: [] as LarkGroupItem[],
  larksuiteGroupOptions: [] as LarkGroupItem[],
  larkGroupMap: new Map<string, LarkGroupItem>(),
  larksuiteGroupMap: new Map<string, LarkGroupItem>(),
  webhookOptions: [] as any[]
});

const defaultFormData = ref({
  reciFlagList: [1],
  uidList: [] as string[],
  ridList: [] as string[],
  emailList: [],
  larkGroupList: [],
  larksuiteGroupList: [],
  webhookList: [] as string[]
});
const formState = ref({
  ...defaultFormData.value,
});

// method area
const init = async (idList: string) => {
  chartIdList.value = [...idList];
  dialogVisible.value = true;
  await loadPlatformStatus();
  await loadWebhookOptions();
};

const closeHandler = () => {
  chartIdList.value = [];
  formState.value = Object.assign(formState.value, defaultFormData.value);
  reciVal.value = []
  dialogVisible.value = false;
};

const setGroupHeight = () => {
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

const loadWebhookOptions = () => {
  webhookOptionsApi().then(res => {
    state.webhookOptions = res.data
  })
}
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
      state.larksuiteGroupMap = state.larksuiteGroupOptions.reduce((acc, item) => {
        acc.set(item.chat_id, item);
        return acc;
      }, new Map());
    }
  });
};

const validateReciFlag = () => {
  return !reciVal.value?.length || formState.value.reciFlagList?.length
}
const validateCur = () => {
  const valid =
    reciVal.value?.length ||
    formState.value.emailList?.length ||
    formState.value.larkGroupList?.length ||
    formState.value.larksuiteGroupList?.length ||
    formState.value.webhookList?.length;
  if (valid && validateReciFlag()) {
    return true
  }
  ElMessage.error(t('threshold.notification_methods_cannot_be_empty'));
  return false;
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
const saveHandler = async () => {
  const fdata = await getFormData();
  if (fdata) {
    fdata["idList"] = chartIdList.value;
    thresholdBatchReciApi(fdata).then(() => {
      ElMessage.success(t("commons.save_success"));
      closeHandler();
    });
  }
};
defineExpose({ init });
</script>

<style lang="less" scoped>
.threshold-reci-container {
  width: 100%;
  height: auto;
  .threshold-form {
    padding-bottom: 8px;
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
}
.reci-dialog-footer {
  display: flex;
  align-items: center;
  justify-content: end;
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
</style>
