<template>
  <InfoTemplate
    v-if="existInfo"
    ref="infoTemplate"
    :label-tooltips="tooltips"
    setting-key="email"
    :setting-title="t('setting_email.title')"
    :test-connect-text="t('commons.test_connect')"
    :setting-data="state.templateList"
    @edit="edit"
    @check="validate"
  />
  <email-editor ref="editor" @saved="refresh" />
</template>

<script lang="ts" setup>
import { ref } from "vue";
import InfoTemplate from "@/views/system/common/InfoTemplate.vue";
import EmailEditor from "./editor.vue";
import request from "@/config/axios";
import { SettingRecord } from "@/views/system/common/SettingTemplate";
import { reactive } from "vue";
import { cloneDeep } from "lodash-es";
import { useI18n } from "@/hooks/web/useI18n";
import { ElMessage } from "element-plus-secondary";
const { t } = useI18n();
const editor = ref();
const infoTemplate = ref();

const settingList = reactive([
  {
    pkey: "email.host",
    pval: "",
    type: "text",
    sort: 1,
  },
  {
    pkey: "email.port",
    pval: "",
    type: "text",
    sort: 2,
  },
  {
    pkey: "email.account",
    pval: "",
    type: "ptextwd",
    sort: 3,
  },
  {
    pkey: "email.pwd",
    pval: "",
    type: "pwd",
    sort: 4,
  },
  {
    pkey: "email.from",
    pval: "",
    type: "text",
    sort: 5,
  },
  {
    pkey: "email.reci",
    pval: "",
    type: "pwd",
    sort: 6,
  },
  {
    pkey: "email.ssl",
    pval: "false",
    type: "text",
    sort: 7,
  },
  {
    pkey: "email.tsl",
    pval: "false",
    type: "text",
    sort: 8,
  },
]);

const existInfo = ref(false);
const tooltips = [
  {
    key: "setting_email.reci",
    val: t("system.test_mail_recipient"),
  },
  {
    key: "setting_email.ssl",
    val: t("system.to_enable_ssl"),
  },
  {
    key: "setting_email.tsl",
    val: t("system.to_enable_tsl"),
  },
];
const state = reactive({
  templateList: [] as SettingRecord[],
});
let originData = [];

const search = (cb) => {
  const url = "/email/setting/query";
  originData = [];
  state.templateList = [];
  request.get({ url }).then(async (res) => {
    const dbList = res.data?.length ? res.data : [];
    const data = settingList.map((item) => {
      const found = dbList.find((d) => d.pkey === item.pkey);
      return found || { ...item };
    });
    originData = cloneDeep(data);
    for (let index = 0; index < data.length; index++) {
      const item = data[index];
      if (item.pkey === "email.tsl" || item.pkey === "email.ssl") {
        item.pval =
          item.pval === "true"
            ? t("data_fill.enable")
            : t("system.not_enabled");
      } else {
        item.pval = item.pval;
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

const validate = () => {
  request
    .post({ url: "/email/setting/validate", data: originData })
    .then((res) => {
      if (!res.msg) {
        ElMessage.success(t("datasource.validate_success"));
      }
    });
};
</script>
