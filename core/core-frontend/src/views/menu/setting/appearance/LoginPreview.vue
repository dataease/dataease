<template>
  <div ref="appLoginView" class="appearance-login-view" :style="customStyle">
    <div class="top-tab-container">
      <div class="flex-top-tabs">
        <div class="tab-card">
          <span>{{ t("system.tab") }}</span>
          <el-icon class="del-icon">
            <Icon name="icon_close_outlined"
              ><icon_close_outlined class="svg-icon"
            /></Icon>
          </el-icon>
        </div>
        <div class="tab-card active">
          <div :title="pageName || 'DataEase'" class="active-span">
            <img :src="pageWeb" alt="" />
            <span>{{ pageName || "DataEase" }}</span>
          </div>
          <el-icon class="del-icon">
            <Icon name="icon_close_outlined"
              ><icon_close_outlined class="svg-icon"
            /></Icon>
          </el-icon>
        </div>
        <div class="tab-card">
          <span>{{ t("system.tab") }}</span>
          <el-icon class="del-icon">
            <Icon name="icon_close_outlined"
              ><icon_close_outlined class="svg-icon"
            /></Icon>
          </el-icon>
        </div>
      </div>
    </div>
    <div class="login-container">
      <div class="left-img" v-if="showLoginImage">
        <el-image class="login-image" fit="cover" :src="pageBg || DeImage" />
      </div>
      <div class="right-container">
        <div class="login-form-center">
          <div class="config-area">
            <div class="login-logo">
              <img
                class="login-logo-icon"
                v-if="pageLogin"
                :src="pageLogin"
                alt=""
              />
              <Icon v-else><DataEase class="svg-icon login-logo-icon" /></Icon>
            </div>
            <div class="login-welcome" v-if="showPageSlogan">
              {{ pageSlogan || t("system.available_to_everyone") }}
            </div>
          </div>
          <div class="form-area">
            <div class="default-login-tabs">
              <el-form size="small">
                <el-form-item class="login-form-item" prop="username">
                  <el-input
                    readonly
                    :placeholder="
                      t('common.account') + '/' + t('commons.email')
                    "
                    autofocus
                  />
                </el-form-item>
                <el-form-item prop="password">
                  <CustomPassword
                    readonly
                    :placeholder="t('common.pwd')"
                    show-password
                    maxlength="30"
                    show-word-limit
                    autocomplete="new-password"
                  />
                </el-form-item>
                <div class="login-btn">
                  <el-button
                    type="primary"
                    class="submit"
                    size="small"
                    :disabled="true"
                  >
                    {{ t("login.btn") }}
                  </el-button>
                </div>
              </el-form>
            </div>
          </div>
        </div>
        <div
          v-if="showFoot"
          class="dynamic-login-foot"
          v-html="pageFootContent"
        />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import icon_close_outlined from "@/assets/svg/icon_close_outlined.svg";
import DataEase from "@/assets/svg/DataEase.svg";
import DeImage from "@/assets/login-desc-de.png";
import { propTypes } from "@/utils/propTypes";
import { useI18n } from "@/hooks/web/useI18n";
import { computed, ref, onMounted, nextTick } from "vue";
import elementResizeDetectorMaker from "element-resize-detector";
import { mixColor } from '@/utils/color'

const basePath = import.meta.env.VITE_API_BASEPATH;
const baseUrl = basePath + "/appearance/image/";
const { t } = useI18n();
const props = defineProps({
  web: propTypes.string.def(""),
  name: propTypes.string.def(""),
  slogan: propTypes.string.def(""),
  themeColor: propTypes.string.def(""),
  customColor: propTypes.string.def(""),
  login: propTypes.string.def(""),
  bg: propTypes.string.def(""),
  height: propTypes.number.def(425),
  foot: propTypes.string.def(""),
  showSlogan: propTypes.string.def("true"),
  footContent: propTypes.string.def(""),
});
const appLoginView = ref();
const loginContainerWidth = ref(0);
const pageWeb = computed(() => {
  return !props.web
    ? "/dataease.svg"
    : props.web.startsWith("blob")
    ? props.web
    : baseUrl + props.web;
});
const pageLogin = computed(() =>
  !props.login
    ? null
    : props.login.startsWith("blob")
    ? props.login
    : baseUrl + props.login
);
const pageBg = computed(() =>
  !props.bg ? null : props.bg.startsWith("blob") ? props.bg : baseUrl + props.bg
);
const pageName = computed(() => props.name);
const pageSlogan = computed(() => props.slogan);
const showPageSlogan = computed(() => props.showSlogan && props.showSlogan === "true");
const showFoot = computed(() => props.foot && props.foot === "true");
const pageFootContent = computed(() =>
  props.foot && props.foot === "true" ? props.footContent : null
);
const pageThemeColor = computed(() => props.themeColor);
const pageCustomColor = computed(() => props.customColor);
const customStyle = computed(() => {
  const result = { height: `${props.height + 23}px` };
  if (pageThemeColor.value === "custom") {
    result["--ed-color-primary"] = pageCustomColor.value;
  } else {
    result["--ed-color-primary"] = "#3370FF";
  }
  result["--ed-color-primary-light-5"] = mixColor(
    "ffffff",
    result["--ed-color-primary"].substring(1),
    0.4
  );
  return result;
});
const showLoginImage = computed<boolean>(() => {
  return !(loginContainerWidth.value < 555);
});
onMounted(() => {
  const erd = elementResizeDetectorMaker();
  erd.listenTo(appLoginView.value, () => {
    nextTick(() => {
      loginContainerWidth.value = appLoginView.value?.offsetWidth;
    });
  });
});
</script>

<style lang="less" scoped>
.appearance-login-view {
  min-width: 390px;
  min-height: 314px;
  width: 100%;
  height: 464px;
  background-color: #fff;
  position: relative;
  .top-tab-container {
    width: 100%;
    height: 22px;
    background-color: #eff0f1;
    .flex-top-tabs {
      display: flex;
      height: 22px;
      align-items: center;
      .active {
        background-color: #fff;
        height: 18px !important;
        line-height: 18px !important;
      }
      .tab-card {
        padding: 0 8px;
        display: flex;
        justify-content: space-between;
        width: 25%;
        min-width: 130px;
        height: 14px;
        line-height: 14px;
        border-right: 1px solid #e0e0e2;
        font-size: 9px;
        align-items: center;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        .del-icon {
          width: 10px;
          height: 10px;
        }
        .active-span {
          display: flex;
          align-items: center;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          img {
            width: 14px;
            height: 14px;
            margin-right: 8px;
          }
        }
      }
    }
    margin-bottom: 2px;
  }

  .login-container {
    height: calc(100% - 24px);
    width: 100%;
    display: flex;
    .left-img {
      overflow: hidden;
      height: 100%;
      width: 40%;
      min-width: 240px;
      .login-image {
        background-size: 100% 100%;
        width: 100%;
        height: 100%;
      }
    }
    .right-container {
      position: relative;
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 290px;
      .login-form-center {
        width: 300px;
        font-size: 10px;
        .config-area {
          .login-logo {
            text-align: center;
            img {
              width: auto;
              max-height: 30px;
              @media only screen and (max-width: 1280px) {
                width: auto;
                max-height: 30px;
              }
            }
            .login-logo-icon {
              width: auto;
              height: 30px;
            }
          }
          .login-welcome {
            text-align: center;
            margin-top: 3px;
            color: #646a73;
            font-family: var(--de-custom_font, "PingFang");
            font-size: 12px;
            font-style: normal;
            font-weight: 400;
            line-height: 16px;
            word-wrap: break-word;
          }
        }
        .form-area {
          margin-top: 24px;
          padding: 24px;
          padding-top: 12px;
          box-shadow: 0px 4px 15px rgba(31, 35, 41, 0.08);
          border: 1px solid #dee0e3;
          border-radius: 2px;

          .login-form-item {
            margin-top: 15px;
          }

          .ed-form-item--default {
            margin-bottom: 15px;
          }
        }
      }
    }
  }
  .dynamic-login-foot {
    visibility: visible;
    width: 100%;
    position: absolute;
    z-index: 302;
    bottom: 0;
    left: 0;
    height: auto;
    padding-top: 1px;
    zoom: 1;
    margin: 0;
  }
}
.login-btn {
  :deep(button) {
    width: 100%;
  }
}
</style>
