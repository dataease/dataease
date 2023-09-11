<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
  <el-form
    ref="authConfig"
    :model="authConfig"
    :rules="rule"
    label-position="right"
  >
    <el-form-item
      :label="$t('datasource.verification_method')"
      prop="verification"
    >
      <el-select
        v-model="authConfig.verification"
        :placeholder="$t('datasource.verification_method')"
        filterable
        size="small"
        @change="change"
      >
        <el-option
          v-for="item in options"
          :key="item.name"
          :label="item.name"
          :value="item.name"
        />
      </el-select>
    </el-form-item>
    <el-row :gutter="24">
      <el-col :span="12">
        <el-form-item
          v-if="
            authConfig.verification != undefined &&
              authConfig.verification != 'No Auth'
          "
          :label="$t('datasource.username')"
          prop="username"
        >
          <el-input
            v-model="authConfig.username"
            :placeholder="$t('datasource.username')"
            class="ms-http-input"
            size="small"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item
          v-if="
            authConfig.verification != undefined &&
              authConfig.verification != 'No Auth'
          "
          :label="$t('datasource.password')"
          prop="password"
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
import dePwd from '@/components/deCustomCm/DePwd.vue'
export default {
  name: 'ApiAuthConfig',
  components: { dePwd },
  props: {
    request: {
      type: Object,
      default: () => {}
    },
    encryptShow: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      options: [{ name: 'No Auth' }, { name: 'Basic Auth' }],
      activeName: 'verified',
      rule: {},
      authConfig: {}
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
