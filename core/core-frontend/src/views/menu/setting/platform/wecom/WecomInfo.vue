<template>
  <div class="platform-head-container" :class="{ 'just-head': !existInfo }">
    <div class="platform-setting-head">
      <div class="platform-setting-head-left">
        <div class="lead-left-icon">
          <el-icon>
            <Icon name="logo_wechat-work"
              ><logo_wechatWork class="svg-icon"
            /></Icon>
          </el-icon>
          <span>{{ t("user.wechat_for_business") }}</span>
        </div>
        <div
          v-if="existInfo"
          class="lead-left-status"
          :class="{ invalid: !info.valid }"
        >
          <span>{{
            info.valid
              ? t("datasource.valid")
              : info.corpId
              ? t("datasource.invalid")
              : ""
          }}</span>
        </div>
      </div>
      <div v-if="existInfo" class="platform-setting-head-right">
        <span>{{
          info.enable ? t("system.enabled") : t("system.close")
        }}</span>
        <el-switch
          v-if="info.valid"
          class="status-switch"
          v-model="info.enable"
          @change="switchEnableApi"
        />
        <el-tooltip
          v-else
          class="box-item"
          effect="dark"
          :content="t('system.can_enable_it')"
          placement="top"
        >
          <el-switch
            disabled=""
            class="status-switch"
            v-model="info.enable"
            @change="switchEnableApi"
          />
        </el-tooltip>
      </div>
      <div v-else class="platform-setting-head-right-btn">
        <el-button type="primary" @click="edit">{{
          t("system.access")
        }}</el-button>
      </div>
    </div>
  </div>
  <InfoTemplate
    v-if="existInfo"
    class="platform-setting-main"
    :copy-list="copyList"
    setting-key="wecom"
    setting-title=""
    :hide-head="true"
    :setting-data="settingList"
    @edit="edit"
  />
  <div v-if="existInfo" class="platform-foot-container">
    <el-button type="primary" @click="edit">
      {{ t("commons.edit") }}
    </el-button>
    <el-button
      secondary
      :disabled="!info.corpId || !info.appSecret || !info.callBack"
      @click="validate"
      >{{ t("commons.validate") }}</el-button
    >
  </div>
  <wecom-edit ref="editor" @saved="search" />
</template>

<script lang="ts" setup>
import logo_wechatWork from "@/assets/svg/logo_wechat-work.svg";
import { ref, reactive } from "vue";
import InfoTemplate from "@/views/system/common/InfoTemplate.vue";
import WecomEdit from "./WecomEdit.vue";
import request from "@/config/axios";
import { useI18n } from "@/hooks/web/useI18n";
import { ElMessage } from "element-plus-secondary";
const { t } = useI18n();
const existInfo = ref(false);
const editor = ref();
const copyList = ["APP Key", "APP Secret"];
const settingList = reactive([
  {
    pkey: "corpId",
    pval: "smtp.exmail.qq.com",
    type: "text",
    sort: 1,
  },
  {
    pkey: "agentId",
    pval: "smtp.exmail.qq.com",
    type: "text",
    sort: 2,
  },
  {
    pkey: "APP Secret",
    pval: "smtp.exmail.qq.com",
    type: "pwd",
    sort: 3,
  },
  {
    pkey: t("system.callback_domain_name"),
    pval: "https://de.fit2cloud.com",
    type: "text",
    sort: 4,
  },
]);
const info = ref({});
const mappingArray = ["corpId", "agentId", "appSecret", "callBack", "enable"];
const search = () => {
  const url = "/wecom/info";
  request.get({ url }).then((res) => {
    info.value = res.data;
    existInfo.value = info.value["agentId"];
    for (let index = 0; index < settingList.length; index++) {
      const element = settingList[index];
      const key = mappingArray[index];
      if (index === 4) {
        element["pval"] = res.data[key] ? t("chart.open") : t("chart.close");
      } else {
        element["pval"] = res.data[key] || "-";
      }
    }
  });
};

const switchEnableApi = (enable) => {
  const url = "/wecom/switchEnable";
  const data = { enable };
  request.post({ url, data }).catch(() => {
    info.value.enable = false;
  });
};
const edit = () => {
  editor?.value.edit(info.value);
};
const validate = () => {
  if (info.value?.agentId && info.value?.appSecret && info.value?.callBack) {
    validateHandler();
  }
};
const validateHandler = () => {
  const url = "/wecom/validate";
  const data = info.value;
  request
    .post({ url, data, loading: true })
    .then(() => {
      info.value.valid = true;
      ElMessage.success(t("datasource.validate_success"));
    })
    .catch(() => {
      info.value.enable = false;
      info.value.valid = false;
    });
};
search();
</script>

<style lang="less" scoped>
.platform-head-container {
  height: 41px;
  border-bottom: 1px solid #1f232926;
}
.just-head {
  height: auto !important;
  border: none !important;
}
.platform-setting-head {
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;

  .platform-setting-head-left {
    display: flex;
    .lead-left-icon {
      display: flex;
      line-height: 24px;
      align-items: center;
      i {
        width: 24px;
        height: 24px;
        font-size: 20px;
      }
      span {
        margin-left: 4px;
        font-family: var(--de-custom_font, "PingFang");
        font-size: 16px;
        font-style: normal;
        font-weight: 500;
        line-height: 24px;
      }
    }
    .lead-left-status {
      margin-left: 4px;
      width: 40px;
      height: 24px;
      background: #34c72433;
      padding: 0 6px;
      font-size: 14px;
      border-radius: 2px;
      overflow: hidden;
      span {
        line-height: 24px;
        color: #2ca91f;
      }
    }
    .invalid {
      background: #f54a4533 !important;
      span {
        color: #d03f3b !important;
      }
    }
  }
  .platform-setting-head-right-btn {
    height: 32px;
    line-height: 32px;
  }
  .platform-setting-head-right {
    height: 22px;
    line-height: 24px;
    display: flex;
    span {
      margin-right: 8px;
      font-size: 14px;
      height: 22px;
      line-height: 22px;
    }
    .status-switch {
      line-height: 22px !important;
      height: 22px !important;
    }
  }
}

.platform-setting-main {
  display: inline-block;
  width: 100%;
  padding: 16px 0 0 0 !important;
  ::v-deep(.info-template-content) {
    display: contents !important;
  }
}
.platform-foot-container {
  height: 32px;
  margin-top: -7px;
}
</style>
