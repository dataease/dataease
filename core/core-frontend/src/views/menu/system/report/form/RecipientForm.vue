<template>
  <div class="report-form-container">
    <div class="report-form-title-container">
      <span class="title-flag" />
      <span class="form-title">{{ t('threshold.recipient') }}</span>
    </div>
    <el-form
      ref="reportReciForm"
      class="report-form"
      :model="formState"
      :rules="reciRules"
      label-width="180px"
      label-position="top"
      :scroll-to-error="true"
    >
      <el-form-item :label="t('report.form.reci_setting')" prop="reciFlagList">
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

      <el-form-item v-if="!isReciPermission" :label="t('threshold.notification_email')" prop="emailList">
        <el-select
          ref="emailSelectRef"
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
        v-if="platformCategory.dingtalk && !isReciPermission"
        :label="t('report.dingtalk_groups')"
        prop="dingtalkGroupList"
      >
        <el-select
          ref="dingtalkGroupSelect"
          v-model="formState.dingtalkGroupList"
          multiple
          collapse-tags
          collapse-tags-tooltip
          :max-collapse-tags="3"
          :placeholder="t('threshold.please_choose_dingtalk_group')"
          style="width: 100%"
          @change="setDingtalkGroupHeight"
          @remove-tag="removeDingtalkGroupTag"
        >
          <el-option
            v-for="item in state.dingtalkGroupOptions"
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
                  @close="removeDingtalkGroupTag(id)"
                  v-for="id in formState.dingtalkGroupList"
                  :key="id"
                  type="info"
                  closable
                >
                  <div v-if="state.dingtalkGroupMap.get(id)">
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
                        {{ state.dingtalkGroupMap.get(id)?.name }}
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
        v-if="platformCategory.lark && !isReciPermission"
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
        v-if="platformCategory.larksuite && !isReciPermission"
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
    </el-form>
  </div>
</template>

<script lang="ts" setup>
import peoples from "@/assets/svg/peoples.svg";
import ReciSelect from '../../../../component/reci-select/index.vue'
import { reactive, ref, onMounted, onUnmounted, nextTick } from "vue";
import {
  reciRules,
  LarkGroupItem,
  PlatformCategory,
  platformOptions,
} from "./formUtil";
import { useI18n } from "@/hooks/web/useI18n";
import { Icon } from "@/components/icon-custom";
import {
  queryCategoryStatusApi,
  larkGroupOptionApi,
  larksuiteGroupOptionApi,
  dingtalkGroupOptionApi
} from "../api";
import { ElMessage } from "element-plus-secondary";

const { t } = useI18n();

const props = defineProps({
  reciFormData: {
    type: Object,
    default: () => {},
  },
  isEdit: {
    type: Boolean,
    default: false,
  },
  resourceId: {
    type: String,
    default: '',
  },
  resourceFlag: {
    type: Number,
    default: 0,
  },
});

const reportReciForm = ref(null);
const reciVal = ref([] as string[]);
const reciSelector = ref()
const groupSelect = ref();
const suiteGroupSelect = ref();
const dingtalkGroupSelect = ref();
const groupTags = ref();
const suiteGroupTags = ref();
const emailSelectRef = ref();
const dingtalkGroupTags = ref();
const platformCategory = ref({
  email: true,
} as PlatformCategory);
const state = reactive({
  dingtalkGroupOptions: [] as LarkGroupItem[],
  larkGroupOptions: [] as LarkGroupItem[],
  larksuiteGroupOptions: [] as LarkGroupItem[],
  dingtalkGroupMap: new Map<string, LarkGroupItem>(),
  larkGroupMap: new Map<string, LarkGroupItem>(),
  larksuiteGroupMap: new Map<string, LarkGroupItem>(),
});
const defaultFormData = ref({
  reciFlagList: [1],
  uidList: [] as string[],
  ridList: [] as string[],
  emailList: [] as string[],
  dingtalkGroupList: [],
  larkGroupList: [],
  larksuiteGroupList: [],
});
const formState = ref({
  ...defaultFormData.value,
});

// method area
const setHeight = () => {
  if(reciSelector?.value?.setHeight) {
    reciSelector.value.setHeight()
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
const setDingtalkGroupHeight = () => {
  if (!dingtalkGroupSelect.value) return;
  setTimeout(() => {
    const input = dingtalkGroupSelect.value.$el.querySelector(
      "input"
    ) as HTMLInputElement;
    const _tags = dingtalkGroupTags.value;
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
const removeDingtalkGroupTag = (val) => {
  let len = formState.value.dingtalkGroupList.length;
  while (len--) {
    const id = formState.value.dingtalkGroupList[len];
    if (id === val) {
      formState.value.dingtalkGroupList.splice(len, 1);
    }
  }
  nextTick(() => {
    setDingtalkGroupHeight();
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
    reportReciForm?.value?.validate((valid) => {
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
  if (props.isEdit && props.reciFormData) {
    for (const key in formState.value) {
      formState.value[key] = props.reciFormData[key];
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

const loadPlatformStatus = async () => {
  const res = await queryCategoryStatusApi();
  if (res["data"]) {
    const list: any[] = res["data"] as any[];
    list.forEach((item) => {
      platformCategory.value[item.name] = item.enable;
    });
    if (platformCategory.value.dingtalk) {
      loadDingtalkGroupOptions();
    }
    if (platformCategory.value.lark) {
      loadLarkGroupOptions();
    }
    if(platformCategory.value.larksuite) {
      loadLarksuiteGroupOptions()
    }
  }
};
const loadDingtalkGroupOptions = () => {
  dingtalkGroupOptionApi().then((res) => {
    const data = res.data;
    if (data.valid) {
      state.dingtalkGroupOptions = data.groupList;
      state.dingtalkGroupMap = state.dingtalkGroupOptions.reduce((acc, item) => {
        acc.set(item.chat_id, item);
        return acc;
      }, new Map());
    }
  });
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

const validateCur = () => {
  const valid =
    reciVal.value?.length ||
    formState.value.emailList?.length ||
    formState.value.dingtalkGroupList?.length ||
    formState.value.larkGroupList?.length ||
    formState.value.larksuiteGroupList?.length;
  if (!valid) {
    ElMessage.error(t('threshold.notification_methods_cannot_be_empty'));
  }
  return valid;
};
const init = async () => {
  await loadPlatformStatus();
  formatReci2Form();
};
const setOwnSelectHeight = () => {
  setHeight();
  setGroupHeight();
  setSuiteGroupHeight();
};
const isReciPermission = ref(false)
const setDataPermission = (permissionData?: number) => {
  isReciPermission.value = !!permissionData;
}
const handleEmailPaste = (event: ClipboardEvent) => {
  const pastedText = event.clipboardData?.getData('text') || ''
  if (!pastedText.includes(';')) {
    return
  }
  event.preventDefault()
  const emails = pastedText
    .split(';')
    .map(s => s.trim())
    .filter(Boolean)
  const existingSet = new Set(formState.value.emailList)
  const newEmails = emails.filter(e => !existingSet.has(e))
  if (newEmails.length) {
    formState.value.emailList = [...formState.value.emailList, ...newEmails]
  }
}

const bindEmailPaste = () => {
  nextTick(() => {
    const input = emailSelectRef.value?.$el?.querySelector('input')
    if (input) {
      input.addEventListener('paste', handleEmailPaste)
    }
  })
}

const unbindEmailPaste = () => {
  const input = emailSelectRef.value?.$el?.querySelector('input')
  if (input) {
    input.removeEventListener('paste', handleEmailPaste)
  }
}

defineExpose({
  getFormData,
  setOwnSelectHeight,
  setDataPermission
});

onMounted(() => {
  init();
  bindEmailPaste();
});

onUnmounted(() => {
  unbindEmailPaste();
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
.report-form-container {
  height: 100%;
  margin: 0 auto;
  width: 600px;
  .report-form-title-container {
    display: flex;
    align-items: center;
    height: 24px;
    line-height: 24px;
    margin-top: 24px;
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

  .report-form {
    width: 600px;
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
</style>
