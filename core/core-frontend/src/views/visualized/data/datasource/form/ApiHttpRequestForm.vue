<script lang="ts" setup>
import { ref, watch, onBeforeMount, PropType, toRefs } from 'vue'
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
  request: {
    type: Object as PropType<ApiRequest>,
    default: () => ({
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
  { value: 'X-DE-TOKEN' },
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

const { request: apiRequest } = toRefs(props)
onBeforeMount(() => {
  if (!props.referenced && props.showScript) {
    spanCount.value = 21
  } else {
    spanCount.value = 24
  }
  init()
})

watch(
  () => apiRequest.value.changeId,
  () => {
    if (apiRequest.value.headers?.length > 1) {
      activeName.value = 'headers'
    }
    if (apiRequest.value.rest?.length > 1) {
      activeName.value = 'rest'
    }
    if (apiRequest.value.arguments?.length > 1) {
      activeName.value = 'parameters'
    }
    if (apiRequest.value.body) {
      apiRequest.value.body.typeChange = apiRequest.value.changeId
      emits('changeId', apiRequest.value.changeId)
    }
  }
)

const init = () => {
  if (!apiRequest.value.body) {
    apiRequest.value.body = new Body()
  }
  if (!apiRequest.value.body.kvs) {
    apiRequest.value.body.kvs = []
  }
  if (!apiRequest.value.rest) {
    apiRequest.value.rest = []
  }
  if (!apiRequest.value.arguments) {
    apiRequest.value.arguments = []
  }
}
const emits = defineEmits(['changeId'])
</script>

<template>
  <div class="request-content">
    <el-tabs v-model="activeName" class="request-tabs">
      <!-- 请求头-->
      <el-tab-pane :label="t('datasource.headers')" name="headers" key="headers">
        <api-key-value
          :show-desc="true"
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
        />
      </el-tab-pane>

      <!--请求体-->
      <el-tab-pane
        key="body"
        :label="t('datasource.request_body')"
        name="body"
        style="overflow: hidden"
      >
        <api-body
          ref="bodyRef"
          :headers="apiRequest.headers"
          :body="apiRequest.body"
          :is-show-enable="isShowEnable"
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
        <api-auth-config :request="apiRequest" />
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
    margin: 0 24px;
    min-height: 200px;

    :deep(.ed-tabs__item) {
      font-family: PingFang SC;
      font-size: 14px;
      font-style: normal;
      font-weight: 400;
    }

    :deep(.ed-tabs__content) {
      padding-top: 16px;
    }
  }

  .ms-el-link {
    float: right;
    margin-right: 45px;
  }
}
</style>
