<template>
  <el-menu :unique-opened="true" class="header-user-menu align-right"
           mode="horizontal"
           background-color="#2c2a48"
           text-color="#fff"
           active-text-color="#fff"
  >
    <el-submenu index="1">
      <template slot="title">
        <font-awesome-icon class="icon global" :icon="['fas', 'globe']"/>
        <span>{{language}}</span>
      </template>
      <el-menu-item v-for="(value, key) in languageMap" :key="key" @click="changeLanguage(key)">
        {{value}} <i class="el-icon-check" v-if="language === value"/>
      </el-menu-item>
    </el-submenu>
  </el-menu>
</template>

<script>
  import {DEFAULT_LANGUAGE, EN_US, TokenKey, ZH_CN, ZH_TW} from '../../../../common/js/constants';
  import {getCurrentUser} from "../../../../common/js/utils";

  export default {
    name: "MsLanguageSwitch",
    data() {
      return {
        currentUserInfo: {},
        language: '',
        languageMap: {
          [ZH_CN]: '简体中文',
          [EN_US]: 'English',
          [ZH_TW]: '繁體中文',
        }
      };
    },
    created() {
      let lang = this.currentUser().language;
      this.currentUserInfo = this.currentUser();
      if (!lang) {
        lang = localStorage.getItem(DEFAULT_LANGUAGE);
      }
      this.checkLanguage(lang)
    },
    methods: {
      checkLanguage(lang) {
        if (!lang) return;
        this.$setLang(lang);
        switch (lang) {
          case ZH_CN:
            this.language = this.languageMap[ZH_CN];
            break;
          case ZH_TW:
            this.language = this.languageMap[ZH_TW];
            break;
          case EN_US:
            this.language = this.languageMap[EN_US];
            break;
          default:
            this.language = this.languageMap[ZH_CN];
            break;
        }
      },
      currentUser: () => {
        return getCurrentUser();
      },
      changeLanguage(language) {
        let user = {
          id: this.currentUser().id,
          language: language
        };
        this.checkLanguage(language);
        this.result = this.$post("/user/update/current", user, response => {
          localStorage.setItem(TokenKey, JSON.stringify(response.data));
          window.location.reload();
        });
      }
    }
  }
</script>

<style scoped>

  .el-icon-check {
    color: #44b349;
    margin-left: 10px;
  }

  .align-right {
    float: right;
  }

  .icon {
    width: 24px;
  }

  .global {
    color: #fff;
  }
</style>
