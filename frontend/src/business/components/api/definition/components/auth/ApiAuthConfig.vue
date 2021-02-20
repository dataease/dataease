<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
  <el-tabs v-model="activeName">
    <!-- 认证-->
    <el-tab-pane :label="$t('api_test.definition.request.verified')" name="verified">

      <el-form :model="authConfig" :rules="rule" ref="authConfig" label-position="right" label-width="80px">
        <el-form-item :label="$t('api_test.definition.request.verification_method')" prop="verification">
          <el-select v-model="authConfig.verification" @change="change"
                     :placeholder="$t('api_test.definition.request.verification_method')" filterable size="small">
            <el-option
              v-for="item in options"
              :key="item.name"
              :label="item.name"
              :value="item.name">
            </el-option>
          </el-select>

        </el-form-item>

        <el-form-item :label="$t('api_test.request.tcp.username')" prop="username"
                      v-if="authConfig.verification!=undefined && authConfig.verification !='No Auth'">
          <el-input :placeholder="$t('api_test.request.tcp.username')" v-model="authConfig.username"
                    class="ms-http-input" size="small">
          </el-input>
        </el-form-item>

        <el-form-item :label="$t('commons.password')" prop="password" v-if=" authConfig.verification!=undefined && authConfig.verification !='No Auth'">
          <el-input v-model="authConfig.password" :placeholder="$t('commons.password')" show-password autocomplete="off"
                    maxlength="50" show-word-limit/>
        </el-form-item>

      </el-form>

    </el-tab-pane>

    <!--加密-->
    <el-tab-pane :label="$t('api_test.definition.request.encryption')" name="encryption">
      <el-form :model="authConfig" size="small" :rules="rule"
               ref="authConfig">

        <el-form-item :label="$t('api_test.definition.request.encryption')" prop="encryption">
          <el-select v-model="authConfig.encrypt"
                     :placeholder="$t('api_test.definition.request.verification_method')" filterable size="small">
            <el-option
              v-for="item in encryptOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
    </el-tab-pane>
  </el-tabs>
</template>

<script>
  import {createComponent} from "../jmeter/components";

  export default {
    name: "MsApiAuthConfig",
    components: {},
    props: {
      request: {},
    },
    created() {
      if (this.request.hashTree) {
        for (let index in this.request.hashTree) {
          if (this.request.hashTree[index].type == 'AuthManager') {
            this.request.authManager = this.request.hashTree[index];
            this.request.hashTree.splice(index, 1);
          }
        }
      }
      if (this.request.authManager) {
        this.authConfig = this.request.authManager;
      }
    },
    data() {
      return {
        options: [{name: "No Auth"}, {name: "Basic Auth"}],
        encryptOptions: [{id: false, name: "不加密"}],
        activeName: "verified",
        rule: {},
        authConfig: {},
      }
    },
    methods: {
      change() {
        if (this.authConfig.verification === "Basic Auth") {
          let authManager = createComponent("AuthManager");
          authManager.verification = "Basic Auth";
          authManager.environment = this.request.useEnvironment;
          this.request.hashTree.push(authManager);
          this.authConfig = authManager;
        } else {
          for (let index in this.request.hashTree) {
            if (this.request.hashTree[index].type === "AuthManager") {
              this.request.hashTree.splice(index, 1);
            }
          }
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
