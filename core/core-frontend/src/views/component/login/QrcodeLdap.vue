<template>
  <div
    v-for="(item, index) in validComponentList"
    @click="execute(item, index)"
    class="item"
    :class="{ 'qrcode': item.key !== 'ldap' }"
  >
    <el-icon>
      <Icon>
        <component :is="item.icon" class="svg-icon"></component>
      </Icon>
    </el-icon>
    <span class="name">
      {{ item.title }}
    </span>
  </div>
</template>

<script lang="ts" setup>
import { Icon } from "@/components/icon-custom";
import icon_qr_outlined from "@/assets/svg/icon_qr_outlined.svg";
import logo_ldap from "@/assets/svg/logo_ldap.svg";
import icon_pc_outlined from "@/assets/svg/icon_pc_outlined.svg";
import { onMounted, ref } from "vue";
import { propTypes } from "@/utils/propTypes";
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()

const props = defineProps({
  qrcode: propTypes.bool.def(false),
  ldap: propTypes.bool.def(false),
});

interface OptionItem {
  key: string;
  icon: string;
  title: string;
}

const componentList = ref<OptionItem[]>([
  {
    key: "qrcode",
    icon: icon_qr_outlined,
    title: t('login.qr_code'),
  },
  {
    key: "ldap",
    icon: logo_ldap,
    title: "LDAP",
  },
  {
    key: "account",
    icon: icon_pc_outlined,
    title: t('user.account')
  },
]);
const componentMap = ref({});
const validComponentList = ref<OptionItem[]>([]);

const activeComponent = ref<string>("account");
const initActiveComponent = () => {
  validComponentList.value = [];
  componentList.value.forEach((item) => {
    if (item.key !== activeComponent.value && props[item.key]) {
      validComponentList.value.push(item);
    }
  });
};

const formatOptionMap = () => {
  componentMap.value["qrcode"] = componentList.value[0];
  componentMap.value["ldap"] = componentList.value[1];
  componentMap.value["account"] = componentList.value[2];
};

const emits = defineEmits(["status-change"]);
const execute = (item: OptionItem, index: number) => {
  validComponentList.value[index] = componentMap.value[activeComponent.value];
  activeComponent.value = item.key;
  if (activeComponent.value !== "qrcode") {
    showDefaultTabs();
  } else {
    hiddenDefaultTabs();
  }
  emits("status-change", activeComponent.value);
};

const hiddenDefaultTabs = () => {
  const dom = document.getElementsByClassName("default-login-tabs");
  const len = dom?.length || 0;
  if (len) {
    dom[0]["style"]["display"] = "none";
    if (len > 1) {
      dom[1]["style"]["display"] = "none";
      if (len > 2 && dom[2]) {
        dom[2]["style"]["display"] = "none";
      }
    }
  }
};
const showDefaultTabs = () => {
  const dom = document.getElementsByClassName("default-login-tabs");
  const len = dom?.length || 0;
  if (len) {
    dom[0]["style"]["display"] = "";
    if (len > 1) {
      dom[1]["style"]["display"] = "";
      if (len > 2 && dom[2]) {
        dom[2]["style"]["display"] = "";
      }
    }
  }
};

const setActive = (active) => {
  const curActive = active || "account";
  let index = -1;
  let item: OptionItem = {
    key: "account",
    icon: "icon_pc_outlined",
    title: t('user.account')
  };
  for (let i = 0; i < validComponentList.value.length; i++) {
    const element = validComponentList.value[i];
    if (element.key === curActive) {
      item = element;
      index = i;
    }
  }
  validComponentList.value[index] = componentMap.value[activeComponent.value];
  activeComponent.value = item.key;
};
defineExpose({
  setActive,
});
onMounted(() => {
  formatOptionMap();
  initActiveComponent();
});
</script>
<style lang="less" scoped>
.item {
  width: 32px;
  cursor: pointer;
  &.qrcode,
  &.account {
    .ed-icon {
      padding: 5px;
    }
  }

  .ed-icon {
    font-size: 32px;
    border: 1px solid #dee0e3;
    border-radius: 50%;
  }
  display: flex;
  align-items: center;
  flex-direction: column;
  justify-content: space-between;

  .name {
    margin-top: 8px;
    color: #000;
    text-align: center;
    font-family: var(--de-custom_font, "PingFang");
    font-size: 12px;
    font-style: normal;
    font-weight: 400;
    line-height: 20px; /* 166.667% */
    display: none;
  }
}
</style>
