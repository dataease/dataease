<template>

  <el-form :model="consumer" :rules="rules" ref="consumer" label-width="100px" size="small" :disabled="isReadOnly">
    <div class="dubbo-form-description" v-if="description">
      {{ description }}
    </div>
    <el-form-item label="Timeout" prop="timeout" class="dubbo-form-item">
      <el-input type="number" v-model="consumer.timeout" :placeholder="$t('commons.input_content')"/>
    </el-form-item>

    <el-form-item label="Version" prop="version" class="dubbo-form-item">
      <el-input v-model="consumer.version" maxlength="30" show-word-limit
                :placeholder="$t('commons.input_content')"/>
    </el-form-item>

    <el-form-item label="Retries" prop="retries" class="dubbo-form-item">
      <el-input type="number" v-model="consumer.retries" :placeholder="$t('commons.input_content')"/>
    </el-form-item>

    <el-form-item label="Cluster" prop="cluster" class="dubbo-form-item">
      <el-input v-model="consumer.cluster" maxlength="300" show-word-limit
                :placeholder="$t('commons.input_content')"/>
    </el-form-item>

    <el-form-item label="Group" prop="group" class="dubbo-form-item">
      <el-input v-model="consumer.group" maxlength="300" show-word-limit
                :placeholder="$t('commons.input_content')"/>
    </el-form-item>

    <el-form-item label="Connections" prop="connections" class="dubbo-form-item">
      <el-input type="number" v-model="consumer.connections" :placeholder="$t('commons.input_content')"/>
    </el-form-item>

    <el-form-item label="Async" prop="async" class="dubbo-form-item">
      <el-select v-model="consumer.async" class="select-100" clearable>
        <el-option v-for="option in asyncOptions" :key="option" :label="option" :value="option"/>
      </el-select>
    </el-form-item>

    <el-form-item label="LoadBalance" prop="loadBalance" class="dubbo-form-item">
      <el-select v-model="consumer.loadBalance" class="select-100" clearable>
        <el-option v-for="option in loadBalances" :key="option" :label="option" :value="option"/>
      </el-select>
    </el-form-item>
  </el-form>

</template>

<script>
import './dubbo.css'
import {ConsumerAndService, RegistryCenter} from "@/business/components/api/test/model/ScenarioModel";

export default {
  name: "MsDubboConsumerService",
  props: {
    description: String,
    consumer: ConsumerAndService,
    isReadOnly: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      asyncOptions: ConsumerAndService.ASYNC_OPTIONS,
      loadBalances: ConsumerAndService.LOAD_BALANCE_OPTIONS,
      methods: [],
      rules: {
        version: [
          {max: 30, message: this.$t('commons.input_limit', [0, 30]), trigger: 'blur'}
        ],
        cluster: [
          {max: 300, message: this.$t('commons.input_limit', [0, 300]), trigger: 'blur'}
        ],
        group: [
          {max: 300, message: this.$t('commons.input_limit', [0, 300]), trigger: 'blur'}
        ]
      }
    }
  }
}
</script>
