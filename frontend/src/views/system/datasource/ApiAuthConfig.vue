<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
  <el-tabs v-model="activeName">
    <!-- 认证-->
    <el-tab-pane :label="$t('datasource.verified')" name="verified">

      <el-form :model="authConfig" :rules="rule" ref="authConfig" label-position="right">
        <el-form-item :label="$t('datasource.verification_method')" prop="verification">
          <el-select v-model="authConfig.verification" @change="change"
                     :placeholder="$t('datasource.verification_method')" filterable size="small">
            <el-option
                v-for="item in options"
                :key="item.name"
                :label="item.name"
                :value="item.name">
            </el-option>
          </el-select>

        </el-form-item>

        <el-form-item :label="$t('datasource.username')" prop="username"
                      v-if="authConfig.verification!=undefined && authConfig.verification !='No Auth'">
          <el-input :placeholder="$t('datasource.username')" v-model="authConfig.username"
                    class="ms-http-input" size="small">
          </el-input>
        </el-form-item>

        <el-form-item :label="$t('datasource.password')" prop="password"
                      v-if=" authConfig.verification!=undefined && authConfig.verification !='No Auth'">
          <el-input v-model="authConfig.password" :placeholder="$t('datasource.password')" show-password autocomplete="off"
                    maxlength="50" show-word-limit/>
        </el-form-item>

      </el-form>

    </el-tab-pane>
  </el-tabs>
</template>

<script>

export default {
  name: "ApiAuthConfig",
  components: {},
  props: {
    request: {},
    encryptShow: {
      type: Boolean,
      default: true,
    }
  },
  watch: {
    request() {
      this.initData();
    }
  },
  created() {
    this.initData();
  },
  data() {
    return {
      options: [{name: "No Auth"}, {name: "Basic Auth"}],
      encryptOptions: [{id: false, name: this.$t('commons.encrypted')}],
      activeName: "verified",
      rule: {},
      authConfig: {}
    }
  },
  methods: {
    change() {
      if (this.authConfig.verification === "Basic Auth") {
        this.authConfig.verification = "Basic Auth";
        this.request.authManager = this.authConfig;
      } else {
        this.authConfig.verification = "No Auth";
        this.request.authManager = this.authConfig;
      }
    },
    initData() {
      if (this.request.authManager) {
        this.authConfig = this.request.authManager;
      }
    }
  }
}
</script>

<style scoped>
/deep/ .el-tabs__nav-wrap::after {
  height: 0px;
}
</style>
