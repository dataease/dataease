<template>
  <el-form :model="request" :rules="rules" ref="request" label-width="100px" size="small" v-loading="loading"
           :disabled="isReadOnly">
    <el-button class="get-provider" type="primary" size="small" @click="getProviderList">Get Provider List</el-button>
    <el-row>
      <el-col :span="12">
        <el-form-item label="Interfaces" prop="interfaces">
          <el-select v-model="serviceInterface" class="select-100" @change="changeInterface" :disabled="isDisable">
            <el-option v-for="i in interfaces" :key="i.value" :label="i.label" :value="i.value"/>
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="Methods" prop="methods">
          <el-select v-model="method" class="select-100" @change="changeMethod" :disabled="isDisable">
            <el-option v-for="m in methods" :key="m" :label="m" :value="m"/>
          </el-select>
        </el-form-item>
      </el-col>
    </el-row>

    <el-row>
      <el-col :span="12">
        <el-form-item label="Interface" prop="interface">
          <el-input v-model="request.interface" maxlength="300" show-word-limit
                    :placeholder="$t('commons.input_content')"/>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="Method" prop="method">
          <el-input v-model="request.method" maxlength="300" show-word-limit
                    :placeholder="$t('commons.input_content')"/>
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>

<script>
  import {DubboRequest, RegistryCenter} from "@/business/components/api/test/model/ScenarioModel";

  export default {
    name: "MsDubboInterface",
    props: {
      request: DubboRequest,
      registryCenter: RegistryCenter,
      isReadOnly: {
        type: Boolean,
        default: false
      }
    },
    data() {
      return {
        loading: false,
        providerMap: {},
        serviceInterface: "",
        method: "",
        interfaces: [],
        methods: [],
        rules: {
          interface: [
            {max: 300, message: this.$t('commons.input_limit', [1, 300]), trigger: 'blur'}
          ],
          method: [
            {max: 300, message: this.$t('commons.input_limit', [1, 300]), trigger: 'blur'}
          ]
        }
      }
    },

    methods: {
      changeInterface(value) {
        this.methods = this.providerMap[value].methods;
        this.request.interface = value;
        this.request.consumerAndService.version = this.providerMap[value].version;
      },
      changeMethod(value) {
        this.request.method = value;
      },
      getProviderList() {
        let param = {
          protocol: this.request.registryCenter.protocol || this.request.dubboConfig.registryCenter.protocol,
          address: this.request.registryCenter.address || this.request.dubboConfig.registryCenter.address,
          group: this.request.registryCenter.group || this.request.dubboConfig.registryCenter.group,
        };

        this.loading = true;
        this.$post("/api/dubbo/providers", param).then(response => {
          this.methodMap = {};
          this.interfaces = [];
          response.data.data.forEach(p => {
            this.providerMap[p.serviceInterface] = p;
            this.interfaces.push({label: p.service, value: p.serviceInterface})
          });
          if (this.methodMap[this.request.interface]) {
            this.methods = this.methodMap[this.request.interface].methods;
          }
          this.loading = false;
          this.$success(this.$t('api_test.request.dubbo.get_provider_success'));
        }).catch(() => {
          this.loading = false;
          this.$warning(this.$t('api_test.request.dubbo.check_registry_center'));
        });
      }
    },
    computed: {
      isDisable() {
        return this.interfaces.length === 0;
      }
    }
  }
</script>

<style scoped>
  .get-provider {
    margin-bottom: 22px;
  }

  .select-100 {
    width: 100%;
  }
</style>
