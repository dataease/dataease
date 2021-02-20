<template>
  <el-col v-if="auth">
    <el-row v-if="licenseHeader != null">
      <el-col>
        <component :is="licenseHeader"></component>
      </el-col>
    </el-row>
    <el-row id="header-top" type="flex" justify="space-between" align="middle">
      <el-col :span="12">
        <img :src="'/display/file/logo'" class="logo" alt="">
        <ms-top-menus/>
      </el-col>

      <el-col :span="12" class="align-right">
        <!-- float right -->
        <ms-user/>
        <ms-language-switch/>
        <ms-header-org-ws/>
      </el-col>
    </el-row>

    <ms-view/>
  </el-col>
</template>

<script>
import MsTopMenus from "./components/common/head/HeaderTopMenus";
import MsView from "./components/common/router/View";
import MsUser from "./components/common/head/HeaderUser";
import MsHeaderOrgWs from "./components/common/head/HeaderOrgWs";
import MsLanguageSwitch from "./components/common/head/LanguageSwitch";
import {saveLocalStorage} from "@/common/js/utils";

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const header = requireComponent.keys().length > 0 ? requireComponent("./license/LicenseMessage.vue") : {};
const display = requireComponent.keys().length > 0 ? requireComponent("./display/Display.vue") : {};

export default {
  name: 'app',
  props: {},
  data() {
    return {
      licenseHeader: null,
      auth: false,
      header: {},
      logoId: '_blank',
    }
  },
  created() {
    if (localStorage.getItem("store")) {
      this.$store.replaceState(Object.assign({}, this.$store.state, JSON.parse(localStorage.getItem("store"))))
    }
    window.addEventListener("beforeunload", () => {
      localStorage.setItem("store", JSON.stringify(this.$store.state))
    })
  },
  beforeCreate() {
    this.$get("/isLogin").then(response => {
      if (response.data.success) {
        this.$setLang(response.data.data.language);
        saveLocalStorage(response.data);
        this.auth = true;
        // 是否显示校验信息
        if (header.default !== undefined) {
          this.licenseHeader = "LicenseMessage";
        }

        if (display.default !== undefined) {
          display.default.showHome(this);
        }
      } else {
        window.location.href = "/login"
      }
    }).catch(() => {
      window.location.href = "/login"
    });
  },
  components: {
    MsLanguageSwitch,
    MsUser,
    MsView,
    MsTopMenus,
    MsHeaderOrgWs,
    "LicenseMessage": header.default
  }
}
</script>


<style scoped>
#header-top {
  width: 100%;
  padding: 0 10px;
  background-color: rgb(44, 42, 72);
  color: rgb(245, 245, 245);
  font-size: 14px;
  height: 40px;
}

.logo {
  width: 156px;
  margin-bottom: 0;
  border: 0;
  margin-right: 20px;
  display: inline-block;
  line-height: 37px;
  background-size: 156px 30px;
  box-sizing: border-box;
  height: 37px;
  background-repeat: no-repeat;
  background-position: 50% center;
}

.menus > * {
  color: inherit;
  padding: 0;
  max-width: 180px;
  white-space: pre;
  cursor: pointer;
  line-height: 40px;
}

.header-top-menus {
  display: inline-block;
  border: 0;
}

.menus > a {
  padding-right: 15px;
  text-decoration: none;
}

.align-right {
  float: right;
}

.license-head {
  height: 30px;
  background: #BA331B;
  text-align: center;
  line-height: 30px;
  color: white;
}
</style>
