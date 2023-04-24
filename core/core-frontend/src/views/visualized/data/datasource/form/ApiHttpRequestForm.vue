<script lang="ts" setup>
import { ref, watch, onBeforeMount, onMounted, reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import ApiKeyValue from './ApiKeyValue.vue'
import ApiBody from './ApiBody.vue'
import ApiVariable from './ApiVariable.vue'
import ApiAuthConfig from './ApiAuthConfig.vue'
import { Body } from './ApiTestModel.js'
import type { Item } from './ApiKeyValue.vue'
import type { AuthConfig } from './ApiAuthConfig.vue'
import type { ApiBodyItem } from './ApiBody.vue'
export interface ApiRequest {
  changeId: string
  headers: Item[]
  rest: Item[]
  arguments: Item[]
  authManager: AuthConfig
  body: ApiBodyItem
}
const props = defineProps({
  showScript: {
    type: Boolean,
    default: true
  },
  referenced: {
    type: Boolean,
    default: false
  },
  isShowEnable: {
    type: Boolean,
    default: false
  },
  isReadOnly: {
    type: Boolean,
    default: false
  }
})

const { t } = useI18n()
const spanCount = ref(21)
const activeName = ref('headers')
const headerSuggestions = [
  { value: 'Accept' },
  { value: 'Accept-Charset' },
  { value: 'Accept-Language' },
  { value: 'Accept-Datetime' },
  { value: 'Authorization' },
  { value: 'Cache-Control' },
  { value: 'Connection' },
  { value: 'Cookie' },
  { value: 'Content-Length' },
  { value: 'Content-MD5' },
  { value: 'Content-Type' },
  { value: 'Date' },
  { value: 'Expect' },
  { value: 'From' },
  { value: 'Host' },
  { value: 'If-Match' },
  { value: 'If-Modified-Since' },
  { value: 'If-None-Match' },
  { value: 'If-Range' },
  { value: 'If-Unmodified-Since' },
  { value: 'Max-Forwards' },
  { value: 'Origin' },
  { value: 'Pragma' },
  { value: 'Proxy-Authorization' },
  { value: 'Range' },
  { value: 'Referer' },
  { value: 'TE' },
  { value: 'User-Agent' },
  { value: 'Upgrade' },
  { value: 'Via' },
  { value: 'Warning' }
]
const bodyRef = ref()
const apiRequest = reactive<ApiRequest>({
  changeId: '',
  authManager: { verification: '', username: '', password: '' },
  headers: [],
  rest: [],
  arguments: [],
  body: {
    typeChange: '',
    kvs: []
  }
})
onBeforeMount(() => {
  if (!props.referenced && props.showScript) {
    spanCount.value = 21
  } else {
    spanCount.value = 24
  }
  init()
})

onMounted(() => {
  bodyRef.value.initApiBody(apiRequest.body, apiRequest.headers)
})

watch([apiRequest.changeId], () => {
  if (apiRequest.headers?.length > 1) {
    activeName.value = 'headers'
  }
  if (apiRequest.rest?.length > 1) {
    activeName.value = 'rest'
  }
  if (apiRequest.arguments?.length > 1) {
    activeName.value = 'parameters'
  }
  if (apiRequest.body) {
    apiRequest.body.typeChange = apiRequest.changeId
    emits('changeId', apiRequest.changeId)
  }
})

const init = () => {
  //   if (Object.prototype.toString.call(apiRequest).match(/\[object (\w+)\]/)[1].toLowerCase() !== 'object') {
  //      Object.assign(apiRequest, JSON.parse(request))
  //   }
  if (!apiRequest.body) {
    apiRequest.body = new Body()
  }
  if (!apiRequest.body.kvs) {
    apiRequest.body.kvs = []
  }
  if (!apiRequest.rest) {
    apiRequest.rest = []
  }
  if (!apiRequest.arguments) {
    apiRequest.arguments = []
  }
}
const changeHeaders = val => {
  if (!isNaN(val)) {
    apiRequest.headers.splice(val, 1)
  } else {
    apiRequest.headers.push(val)
  }

  headersChange(apiRequest.headers)
}

const moveHeaders = val => {
  const pre = apiRequest.headers[val]
  const next = apiRequest.headers[val + 1]
  apiRequest.headers.splice(val, 2, ...[next, pre])
  headersChange(apiRequest.headers)
}

const authConfigChange = val => {
  emits('authConfigChange', val)
}
const bodyChange = val => {
  apiRequest.body = Object.assign(apiRequest.body, val)
  emits('bodyChange', val)
}
const headersChange = val => {
  apiRequest.headers = val
  emits('headersChange', val)
}
const moveParameters = val => {
  const pre = apiRequest.arguments[val]
  const next = apiRequest.arguments[val + 1]
  apiRequest.arguments.splice(val, 2, ...[next, pre])
  emits('changeParameters', val)
}
const changeParameters = val => {
  if (!isNaN(val)) {
    apiRequest.arguments.splice(val, 1)
  } else {
    apiRequest.arguments.push(val)
  }
  emits('changeParameters', val)
}

const kvsChange = val => {
  apiRequest.body.kvs = val
  emits('kvsChange', val)
}
const emits = defineEmits([
  'changeId',
  'authConfigChange',
  'bodyChange',
  'headersChange',
  'changeParameters',
  'kvsChange'
])
</script>

<template>
  <div class="request-content">
    <el-tabs v-model="activeName" class="request-tabs">
      <!-- 请求头-->
      <el-tab-pane :label="t('datasource.headers')" name="headers" key="headers">
        <api-key-value
          :show-desc="true"
          @changeHeaders="changeHeaders"
          @moveHeaders="moveHeaders"
          :suggestions="headerSuggestions"
          :items="apiRequest.headers"
        />
      </el-tab-pane>

      <!--query 参数-->
      <el-tab-pane key="parameters" :label="t('datasource.query_param')" name="parameters">
        <api-variable
          :is-read-only="isReadOnly"
          :isShowEnable="isShowEnable"
          :parameters="apiRequest.arguments"
          @change-parameters="changeParameters"
          @move-parameters="moveParameters"
        />
      </el-tab-pane>

      <!--请求体-->
      <el-tab-pane
        key="body"
        :label="t('datasource.request_body')"
        name="body"
        style="overflow: auto"
      >
        <api-body
          ref="bodyRef"
          :is-show-enable="isShowEnable"
          @headers-change="headersChange"
          @body-change="bodyChange"
          @kvsChange="kvsChange"
        />
      </el-tab-pane>

      <!-- 认证配置 -->
      <el-tab-pane key="authConfig" :label="t('datasource.auth_config')" name="authConfig">
        <el-tooltip
          class="item-tabs"
          effect="dark"
          :content="t('datasource.auth_config_info')"
          placement="top-start"
        >
          <template #label>
            <span>{{ t('datasource.auth_config') }}</span>
          </template>
        </el-tooltip>
        <api-auth-config @authConfigChange="authConfigChange" :request="apiRequest" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style lang="less" scoped>
.request-content {
  border: 1px #dcdfe6 solid;
  height: 100%;
  border-radius: 4px;
  width: 100%;
  .ms-query {
    background: #409eff;
    color: white;
    height: 18px;
    border-radius: 42%;
  }

  .ms-header {
    background: #409eff;
    color: white;
    height: 18px;
    border-radius: 42%;
  }

  .request-tabs {
    margin: 20px;
    min-height: 200px;
  }

  .ms-el-link {
    float: right;
    margin-right: 45px;
  }
}
</style>
