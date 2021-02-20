<template>
  <el-form :model="config" :rules="rules" ref="config" label-width="100px" size="small" :disabled="isReadOnly">
    <div class="dubbo-form-description" v-if="description">
      {{ description }}
    </div>
    <el-form-item label="Protocol" prop="protocol" class="dubbo-form-item">
      <el-select v-model="config.protocol" class="select-100" clearable>
        <el-option v-for="p in protocols" :key="p" :label="p" :value="p"/>
      </el-select>
    </el-form-item>

    <el-form-item label="Group" prop="group" class="dubbo-form-item">
      <el-input v-model="config.group" maxlength="300" show-word-limit
                :placeholder="$t('commons.input_content')"/>
    </el-form-item>

    <el-form-item label="Namespace" prop="namespace" class="dubbo-form-item">
      <el-input v-model="config.namespace" maxlength="300" show-word-limit
                :placeholder="$t('commons.input_content')"/>
    </el-form-item>

    <el-form-item label="Timeout" prop="timeout" class="dubbo-form-item">
      <el-input type="number" v-model="config.timeout" :placeholder="$t('commons.input_content')"/>
    </el-form-item>

    <el-form-item label="Address" prop="address" class="dubbo-form-item-long">
      <el-input v-model="config.address" maxlength="300" show-word-limit
                :placeholder="$t('commons.input_content')"/>
    </el-form-item>

    <el-form-item label="UserName" prop="username" class="dubbo-form-item">
      <el-input v-model="config.username" maxlength="100" show-word-limit
                :placeholder="$t('commons.input_content')"/>
    </el-form-item>

    <el-form-item label="Password" prop="password" class="dubbo-form-item">
      <el-input v-model="config.password" maxlength="30" show-word-limit show-password autocomplete="new-password"
                :placeholder="$t('commons.input_content')"/>
    </el-form-item>


  </el-form>
</template>

<script>
import './dubbo.css'
import {ConfigCenter} from "@/business/components/api/test/model/ScenarioModel";

export default {
  name: "MsDubboConfigCenter",
  props: {
    description: String,
    config: ConfigCenter,
    isReadOnly: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      protocols: ConfigCenter.PROTOCOLS,
      methods: [],
      rules: {
        group: [
          {max: 300, message: this.$t('commons.input_limit', [0, 300]), trigger: 'blur'}
        ],
        namespace: [
          {max: 300, message: this.$t('commons.input_limit', [0, 300]), trigger: 'blur'}
        ],
        username: [
          {max: 100, message: this.$t('commons.input_limit', [0, 300]), trigger: 'blur'}
        ],
        password: [
          {max: 30, message: this.$t('commons.input_limit', [0, 300]), trigger: 'blur'}
        ],
        address: [
          {max: 300, message: this.$t('commons.input_limit', [0, 300]), trigger: 'blur'}
        ]
      }
    }
  }
}
</script>
