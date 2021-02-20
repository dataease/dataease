<template>
    <div>
      <el-form :model="request"  ref="request" label-width="100px">

        <el-form-item :label="$t('api_test.request.connect_timeout')" prop="connectTimeout">
          <el-input-number size="small" :disabled="isReadOnly" v-model="request.connectTimeout" :placeholder="$t('commons.millisecond')" :max="1000*10000000" :min="0"/>
        </el-form-item>

        <el-form-item :label="$t('api_test.request.response_timeout')" prop="responseTimeout">
          <el-input-number size="small" :disabled="isReadOnly" v-model="request.responseTimeout" :placeholder="$t('commons.millisecond')" :max="1000*10000000" :min="0"/>
        </el-form-item>

      </el-form>
    </div>
</template>

<script>
    export default {
      name: "MsApiAdvancedConfig",
      props: {
        request: Object,
        isReadOnly: {
          type: Boolean,
          default: false
        }
      }
    }
</script>

<style scoped>
</style>
