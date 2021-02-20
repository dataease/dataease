<template>
  <div>
    <el-row style="margin: 20px">
      <span style="margin-right: 10px">
            {{$t('api_test.request.connect_timeout')}}:
      </span>
      <span style="margin-right: 10px">
        <el-input-number size="small" :disabled="isReadOnly" v-model="request.connectTimeout" :placeholder="$t('commons.millisecond')" :max="1000*10000000" :min="0"/>
      </span>
      <span style="margin-right: 10px">
        {{$t('api_test.request.response_timeout')}}:
      </span>
      <span style="margin-right: 10px">
          <el-input-number size="small" :disabled="isReadOnly" v-model="request.responseTimeout" :placeholder="$t('commons.millisecond')" :max="1000*10000000" :min="0"/>
      </span>
    </el-row>
    <el-row style="margin: 20px">
      <span style="margin-right: 10px">
      <el-checkbox class="follow-redirects-item" v-model="request.followRedirects">{{$t('api_test.request.follow_redirects')}}</el-checkbox>
      </span>
    </el-row>
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
