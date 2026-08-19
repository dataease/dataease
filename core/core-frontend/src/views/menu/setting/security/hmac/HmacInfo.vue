<template>
  <InfoTemplate
    v-if="existInfo"
    ref="infoTemplate"
    :label-tooltips="tooltips"
    setting-key="hmac"
    :setting-title="t('setting_hmac.title')"
    :setting-data="state.templateList"
    :copy-list="copyList"
    @edit="edit"
  />
  <hmac-edit ref="editor" @saved="refresh" />
</template>

<script lang="ts" setup>
import { ref } from "vue";
import InfoTemplate from "@/views/system/common/InfoTemplate.vue";
import HmacEdit from "./HmacEdit.vue";
import request from "@/config/axios";
import { SettingRecord } from "@/views/system/common/SettingTemplate";
import { reactive } from "vue";
import { cloneDeep } from "lodash-es";
import { useI18n } from "@/hooks/web/useI18n";
const { t } = useI18n();
const editor = ref();
const infoTemplate = ref();
const copyList = ["Secret Key"];
const settingList = reactive([
  {
    pkey: 'hmac.enable',
    pval: "",
    type: "text",
    sort: 1,
  },
  {
    pkey: 'hmac.secretKey',
    pval: "",
    type: "pwd",
    sort: 2,
  },
  {
    pkey: 'hmac.clockSkew',
    pval: "300",
    type: "text",
    sort: 3,
  }
]);

const existInfo = ref(false);
const tooltips = [
  {
    key: 'setting_hmac.clock_skew',
    val: t('setting_hmac.clock_skew_tips')
  }
]
  

const state = reactive({
  templateList: [] as SettingRecord[],
});
let originData = [] as any[];

const search = (cb) => {
  const url = "/perSetting/hmac/query";
  originData = [] as any[];
  state.templateList = [];
  request.get({ url }).then(async (res) => {
    const data = res.data?.length ? res.data : [];
    const tempData = {}
    data.forEach((item) => {
      tempData[item.pkey] = item;
    })
    // originData = cloneDeep(data);
    for (let index = 0; index < settingList.length; index++) {
      const originSettingItem = cloneDeep(settingList[index]);
      const originKey = originSettingItem.pkey;
      if (tempData.hasOwnProperty(originKey)) {
        originSettingItem.pval = tempData[originKey].pval
      }
      originData.push(cloneDeep(originSettingItem));
      const item = originSettingItem;
      if (item.pkey === "hmac.enable") {
        item.pval = item.pval === "true" ? t("data_fill.enable") : t("system.not_enabled");
        item.pkey = "setting_" + item.pkey;
      } else if(item.pkey === "hmac.clockSkew") {
        item.pkey = "setting_hmac.clock_skew";
        item.pval = (item.pval ? item.pval : '300') as any;
      } else {
        item.pval = item.pval ? item.pval : "-";
      }
      if (item.pkey === 'hmac.secretKey') {
        item.pkey = 'Secret Key';
      }
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
