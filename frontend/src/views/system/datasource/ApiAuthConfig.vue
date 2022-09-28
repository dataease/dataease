<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
  <el-form
    :model="authConfig"
    :rules="rule"
    ref="authConfig"
    label-position="right"
  >
    <el-form-item
      :label="$t('datasource.verification_method')"
      prop="verification"
    >
      <el-select
        v-model="authConfig.verification"
        @change="change"
        :placeholder="$t('datasource.verification_method')"
        filterable
        size="small"
      >
        <el-option
          v-for="item in options"
          :key="item.name"
          :label="item.name"
          :value="item.name"
        >
        </el-option>
      </el-select>
    </el-form-item>
    <el-row :gutter="24">
      <el-col :span="12">
        <el-form-item
          :label="$t('datasource.username')"
          prop="username"
          v-if="
            authConfig.verification != undefined &&
            authConfig.verification != 'No Auth'
          "
        >
          <el-input
            :placeholder="$t('datasource.username')"
            v-model="authConfig.username"
            class="ms-http-input"
            size="small"
          >
          </el-input>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item
          :label="$t('datasource.password')"
          prop="password"
          v-if="
            authConfig.verification != undefined &&
            authConfig.verification != 'No Auth'
          "
        >
          <dePwd
           v-model="authConfig.password"
          :placeholder="$t('datasource.password')"
        />
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>

<script>
import dePwd from '@/components/deCustomCm/dePwd.vue'
export default {
  name: 'ApiAuthConfig',
  components: { dePwd },
  props: {
    request: {},
    encryptShow: {
      type: Boolean,
      default: true
    }
  },
  watch: {
    request() {
      this.initData()
    }
  },
  created() {
    this.initData()
  },
  data() {
    return {
      options: [{ name: 'No Auth' }, { name: 'Basic Auth' }],
      activeName: 'verified',
      rule: {},
      authConfig: {}
    }
  },
  methods: {
    change() {
      if (this.authConfig.verification === 'Basic Auth') {
        this.authConfig.verification = 'Basic Auth'
        this.request.authManager = this.authConfig
      } else {
        this.authConfig.verification = 'No Auth'
        this.request.authManager = this.authConfig
      }
    },
    initData() {
      if (this.request.authManager) {
        this.authConfig = this.request.authManager
      }
    }
  }
}
</script>
