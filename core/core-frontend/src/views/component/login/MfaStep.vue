<template>
  <div class="mfa-step-container">
    <div class="mfa-back-head">
      <span class="svg-span" @click="back2Login">
        <Icon class="toolbar-icon" name="icon_left_outlined"
          ><icon_left_outlined class="svg-icon toolbar-icon"
        /></Icon>
      </span>
      <span>{{ t("setting_mfa.bind_title") }}</span>
    </div>
    <div class="mfa-step flex-center">
      <el-steps
        v-show="!mobileEnv"
        v-if="step === 1 || step === 2"
        space="150px"
        :active="step - 1"
        align-center
      >
        <el-step>
          <template #icon>
            <div class="step-icon">
              <span class="icon">{{ step === 1 ? 1 : "" }}</span>
              <span class="title">{{ t("setting_mfa.step_1") }}</span>
            </div>
          </template>
        </el-step>
        <el-step>
          <template #icon>
            <div class="step-icon">
              <span class="icon">2</span>
              <span class="title">{{ t("setting_mfa.step_2") }}</span>
            </div>
          </template>
        </el-step>
      </el-steps>
    </div>
    <div v-if="step === 1" class="content step-1">
      <div class="step-1-container">
        <div class="head-title">
          <span>{{ t("setting_mfa.install_app") }}</span>
        </div>
        <div class="import-info" :class="{ 'is-mobile': mobileEnv }">
          <span>{{ t("setting_mfa.install_1") }}</span>
          <span>{{ t("setting_mfa.install_2") }}</span>
        </div>
        <div class="qr-area">
          <div class="qr-container" v-if="!mobileEnv || !isIphone">
            <div class="qr-img">
              <img src="/src/assets/authenticator_android.png" />
            </div>
            <div class="qr-tips">
              <span>{{ `Android ${t("setting_mfa.phone_download")}` }}</span>
            </div>
          </div>
          <div class="qr-container" v-if="!mobileEnv || isIphone">
            <div class="qr-img">
              <img src="/src/assets/authenticator_iphone.png" />
            </div>
            <div class="qr-tips">
              <span>{{ `iphone ${t("setting_mfa.phone_download")}` }}</span>
            </div>
          </div>
        </div>

        <div class="next-btn">
          <el-button type="primary" @click="next">{{
            t("common.next")
          }}</el-button>
        </div>
      </div>
    </div>
    <div v-else-if="step === 2" class="content step-2">
      <div class="step-1-container" :class="{ 'is-mobile-step': mobileEnv }">
        <div class="head-title">
          <span>{{ t("setting_mfa.step_2") }}</span>
        </div>
        <div class="import-info import-info-2">
          <span>{{ t("setting_mfa.scan_qr_tips") }}</span>
        </div>
        <div class="qr-area">
          <div class="qr-container">
            <div class="qr-img">
              <img :src="userQr" />
            </div>
          </div>
        </div>

        <el-form ref="mfaForm" :model="state.form" :rules="rule">
          <el-form-item label="" prop="code">
            <el-input
              v-model="state.form.code"
              :placeholder="t('setting_mfa.code_input_msg', [6])"
              @keydown.enter="enterHandler"
            />
          </el-form-item>
        </el-form>
        <div class="next-btn">
          <el-button @click="back">{{ t("common.prev") }}</el-button>
          <el-button type="primary" @click="next">{{
            t("common.sure")
          }}</el-button>
        </div>
      </div>
    </div>
    <div
      v-else
      class="content step-3"
      :class="{ 'is-mobile-step3': mobileEnv }"
    >
      <div class="code-form-area" :class="{ 'is-mobile-step': mobileEnv }">
        <div class="form-head">
          <span class="title">{{ t("setting_mfa.user_enable") }}</span>
        </div>
        <el-form ref="mfaForm" :model="state.form" :rules="rule">
          <el-form-item label="" prop="code">
            <el-input
              v-model="state.form.code"
              :placeholder="t('setting_mfa.code_input_msg', [6])"
              @keydown.enter="enterHandler"
            />
          </el-form-item>
        </el-form>
        <el-button type="primary" @click="next">{{
          t("common.sure")
        }}</el-button>
        <div class="tips">
          <span>{{ t("setting_mfa.code_miss_tips") }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, reactive, computed, onBeforeUnmount } from "vue";
import request from "@/config/axios";
import { useI18n } from "@/hooks/web/useI18n";
import { useUserStoreWithOut } from "@/store/modules/user";
import router from "@/router";
import { logoutHandler } from "@/utils/logout";
import icon_left_outlined from "@/assets/svg/icon_left_outlined.svg";
import { isMobile } from "@/utils/utils";
const userStore = useUserStoreWithOut();
const { t } = useI18n();

interface MfaData {
  enabled: boolean;
  ready: boolean;
  uid: string;
  origin: number;
}
const props = withDefaults(
  defineProps<{ mfaData: MfaData; isLogin: boolean }>(),
  {
    mfaData: { enabled: false, ready: false, uid: "", origin: 0 },
    isLogin: true,
  }
);
interface MfaForm {
  code: string;
}

const mobileEnv = computed(() => isMobile());
const isIphone = computed(() => {
  const ua = navigator.userAgent;
  if (!ua) {
    return false;
  }
  return ua.match(/(iPhone|iPad|iPod|iOS)/i);
});
const step = ref(1);
const errorMsg = ref("");
const errorCode = ref();
const mfaForm = ref();
const userQr = ref("");
const rule = reactive<any>({
  code: [
    {
      required: true,
      message: t("setting_mfa.code_input_msg", [6]),
      trigger: "blur",
    },
    {
      pattern: /^\d{6}$/,
      message: t("setting_mfa.code_input_msg", [6]),
      trigger: "blur",
    },
  ],
});

const state = reactive({
  form: reactive<MfaForm>({
    code: "",
  }),
});
// method area
const enterHandler = (e) => {
  e.preventDefault();
  next();
};
const next = () => {
  if (step.value !== 1) {
    mfaLogin();
    return;
  }
  step.value = (step.value + 1) % 3;
};
const back = () => {
  step.value = step.value - 1;
};
const getCurLocation = () => {
  let queryRedirectPath = "/workbranch/index";
  if (router.currentRoute.value.query.redirect) {
    queryRedirectPath = router.currentRoute.value.query.redirect as string;
  }
  return queryRedirectPath;
};
const back2Login = () => {
  if (!props.mfaData?.origin) {
    emits("close");
    return;
  }
  logoutHandler(true);
};

const generateQr = () => {
  if (!props.mfaData?.uid) {
    return;
  }
  const url = `/mfa/qr/${props.mfaData.uid}`;
  request.post({ url }).then((res) => {
    userQr.value = res.data?.img;
  });
};
const mfaLogin = () => {
  const formEl = mfaForm.value;
  if (!formEl) return;
  formEl.validate((valid) => {
    if (valid) {
      const url = props.isLogin ? `/mfa/login` : "/user/mfaBind";
      const param = {
        id: props.mfaData.uid,
        code: state.form.code,
      };
      request
        .post({ url, data: param })
        .then((res) => {
          errorMsg.value = "";
          errorCode.value = null;
          if (!props.isLogin) {
            emits("refreshBind", true);
            return;
          }
          if (res.data?.token) {
            userStore.setToken(res.data.token);
            userStore.setExp(res.data.exp);
            userStore.setTime(Date.now());
            if (mobileEnv.value) {
              emits("success");
              return;
            }
            const queryRedirectPath = getCurLocation();
            router.push({ path: queryRedirectPath });
          }
        })
        .catch((msg) => {
          errorMsg.value = msg;
          errorCode.value = param.code;
          setCodeError();
        });
    }
  });
};
const setCodeError = () => {
  const ruleArray: any[] = rule.code as any[];
  const len = ruleArray.length;
  if (!errorMsg.value && len > 2) {
    ruleArray.splice(2, 1);
  } else if (errorMsg.value && len > 2) {
    ruleArray[2]["message"] = errorMsg.value;
  } else if (errorMsg.value && len === 2) {
    ruleArray.push({
      message: errorMsg.value,
      validator: (rule, value, callback) => {
        if (value === errorCode.value) {
          callback(new Error(rule.message));
        }
        callback();
      },
      trigger: "blur",
    });
  }
  mfaForm.value?.validate();
};

const emits = defineEmits(["close", "refreshBind", "success"]);
const keyFunction = (e: any) => {
  if (e?.keyCode === 13) {
    next();
    e.stopPropagation();
  }
};
const removeKeyDown = () => {
  window.removeEventListener("keydown", keyFunction);
};
const addKeyDown = () => {
  window.addEventListener("keydown", keyFunction);
};
onBeforeUnmount(() => {
  removeKeyDown();
});
onMounted(() => {
  step.value = props.mfaData.ready ? 3 : 1;
  if (!props.mfaData?.ready) {
    generateQr();
  }
  addKeyDown();
});
</script>

<style lang="less">
.mfa-step-container {
  .ed-form-item {
    margin-bottom: 24px;
  }
  .is-error {
    margin-bottom: 48px !important;
  }
  .mfa-step {
    height: 56px;
    background-color: #fff;
    border-bottom: 1px solid #1f232926;
    position: relative;

    .ed-step.is-center .ed-step__line {
      width: 80px;
      right: 40px;
      z-index: 5;
      left: calc(100% - 40px);
    }

    .ed-step__icon.is-icon {
      width: auto;
      position: relative;
      z-index: 0;
      height: 25px;
    }

    .ed-step__head {
      line-height: 0;
    }

    .ed-step__head.is-finish::after {
      right: calc(100% - 64px);
      top: 44%;
    }

    .ed-step__head.is-process .ed-step__icon {
      background-color: transparent;

      .step-icon {
        .icon {
          background: var(--ed-color-primary);
        }
        .title {
          color: #1f2329;
          font-weight: 500 !important;
        }
      }
    }

    .ed-step__head.is-finish .ed-step__icon {
      background-color: transparent;

      .step-icon {
        .icon {
          border: 1px solid var(--ed-color-primary);
        }
      }
    }

    .ed-step__head.is-wait .ed-step__icon {
      background-color: transparent;
      .step-icon {
        .icon {
          color: #8f959e;
          border: 1px solid #8f959e;
        }
        .title {
          color: #8f959e;
        }
      }
    }

    .step-icon {
      display: flex;
      padding: 0 48px;
      align-items: center;

      .icon {
        width: 25px;
        height: 25px;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        border-radius: 50%;
      }

      .title {
        margin-left: 8px;
        color: #1f2329;
        font-size: 14px;
        font-weight: 400;
        line-height: 22px;
      }
    }
  }
}
</style>
<style lang="less" scoped>
.mfa-step-container {
  top: 0;
  right: 0;
  width: 100vw;
  height: 100vh;
  position: absolute;
  background-color: #f5f6f7;
  z-index: 20;
  .mfa-back-head {
    display: flex;
    column-gap: 12px;
    align-items: center;
    position: fixed;
    line-height: 24px;
    width: fit-content;
    height: 24px;
    left: 24px;
    top: 16px;
    z-index: 1;
    color: #1f2329;
    font-size: 16px;
    font-weight: 500;
    .svg-span {
      line-height: 20px;
      height: 20px;
      &:hover {
        cursor: pointer;
        background-color: var(--ed-color-primary-1a, #3370ff1a);
        color: var(--ed-color-primary, #3370ff);
      }
    }
  }
  .content {
    display: flex;
    justify-content: center;
    .step-1-container {
      padding-top: 80px;
      width: fit-content;
    }
    .head-title {
      width: 100%;
      display: flex;
      justify-content: center;
      line-height: 28px;
      span {
        font-size: 20px;
        font-weight: 500;
        color: #1f2329;
      }
    }
    .import-info-2 {
      width: 100%;
      justify-content: center !important;
    }
    .is-mobile {
      padding: 0 12px;
    }
    .import-info {
      margin-top: 12px;
      display: inline-grid;
      justify-content: start;
      row-gap: 8px;
      line-height: 22px;
      span {
        font-size: 14px;
        color: #1f2329;
      }
    }
    .qr-area {
      margin-top: 24px;
      display: flex;
      column-gap: 34px;
      justify-content: center;
      .qr-img {
        width: 200px;
        height: 200px;
        padding: 8px;
        border: 1px solid #dee0e3;
        border-radius: 8px;
        img {
          width: 184px;
          height: 184px;
        }
      }
      .qr-tips {
        margin-top: 12px;
        line-height: 22px;
        text-align: center;
        span {
          font-size: 14px;
          color: #646a73;
        }
      }
    }
    form {
      margin-top: 24px;
    }
    .next-btn {
      display: flex;
      line-height: 32px;
      align-items: center;
      justify-content: center;
      column-gap: 12px;
      button {
        width: 190px;
      }
    }
  }
  .is-mobile-step {
    .next-btn {
      display: block !important;
      width: 100%;
      button {
        width: calc(50% - 6px);
      }
    }
  }
  .step-1,
  .step-2 {
    height: calc(100vh - 104px);
    background-color: #fff;
    border-radius: 8px;
    width: calc(100% - 48px);
    margin: 24px;
  }
  .step-3 {
    height: calc(100vh - 56px);
    .code-form-area {
      margin-top: 160px;
      width: 480px;
      border-radius: 12px;
      padding: 40px;
      height: fit-content;
      background-color: #fff;
      border: 1px solid #dee0e3;
      .form-head {
        width: 100%;
        margin-bottom: 24px;
        line-height: 28px;
        .title {
          font-size: 20px;
          color: #1f2329;
          display: block;
        }
      }
      :deep(.ed-input) {
        height: 40px;
        line-height: 40px;
      }
      button {
        width: 100%;
        height: 40px;
        line-height: 40px;
      }
      .tips {
        margin-top: 12px;
        line-height: 22px;
        span {
          font-size: 14px;
          font-weight: 400;
          color: #646a73;
        }
      }
    }
  }
  .is-mobile-step3 {
    padding: 0 24px;
  }
}
</style>
