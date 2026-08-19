<template>
  <InfoTemplate
    v-if="existInfo"
    ref="infoTemplate"
    :label-tooltips="tooltips"
    setting-key="mfa"
    :setting-title="t('setting_mfa.title')"
    :setting-data="state.templateList"
    @edit="edit"
  />
  <mfa-edit ref="editor" @saved="refresh" />
</template>

<script lang="ts" setup>
import { ref } from "vue";
import InfoTemplate from "@/views/system/common/InfoTemplate.vue";
import MfaEdit from "./MfaEdit.vue";
import request from "@/config/axios";
import { SettingRecord } from "@/views/system/common/SettingTemplate";
import { reactive } from "vue";
import { cloneDeep } from "lodash-es";
import { useI18n } from "@/hooks/web/useI18n";
const { t } = useI18n();
const editor = ref();
const infoTemplate = ref();

const settingList = reactive([
  {
    pkey: "mfa.status",
    pval: "",
    type: "text",
    sort: 1,
  },
  {
    pkey: "mfa.platformEnable",
    pval: "",
    type: "text",
    sort: 2,
  },
  {
    pkey: "mfa.otpName",
    pval: "",
    type: "text",
    sort: 4,
  },
  {
    pkey: "mfa.rate",
    pval: "",
    type: "text",
    sort: 5,
  }
  
]);

const existInfo = ref(false);
const tooltips = [
  {
    key: "setting_mfa.platformEnable",
    val: t("setting_mfa.platform_tips"),
  }
];
const state = reactive({
  templateList: [] as SettingRecord[],
});
let originData = [] as any[];

const search = (cb) => {
  const url = "/perSetting/mfa/query";
  originData = [];
  state.templateList = [];
  request.get({ url }).then(async (res) => {
    const apiData = res.data || [];
    const merged = settingList.map((template) => {
      const found = apiData.find((item) => item.pkey === template.pkey);
      return found ? { ...template, ...found } : { ...template };
    });
    originData = cloneDeep(merged);
    for (let index = 0; index < merged.length; index++) {
      const item = merged[index];
      if (item.pkey === "mfa.platformEnable") {
        item.pval = item.pval === "true" ? t("data_fill.enable") : t("system.not_enabled");
      } else if (item.pkey === "mfa.status") {
        item.pval = t(`setting_mfa.status_${item.pval}`)
      }
      item.pkey = "setting_" + item.pkey;
      state.templateList.push(item);
    }
    existInfo.value = true;
    cb && cb();
  });
};
const refresh = () => {
  search(() => {
    if (existInfo.value && infoTemplate.value?.init) {
      infoTemplate.value.init();
    }
  });
};
refresh();

const edit = () => {
  editor?.value.edit(cloneDeep(originData));
};

</script>
