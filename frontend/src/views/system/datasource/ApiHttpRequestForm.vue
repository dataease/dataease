<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
  <div>
    <el-row>
      <el-col :span="spanCount">
        <!-- HTTP 请求参数 -->
        <div style="border:1px #DCDFE6 solid; height: 100%;border-radius: 4px ;width: 100%">
          <el-tabs
            v-model="activeName"
            class="request-tabs"
          >
            <!-- 请求头-->
            <el-tab-pane
              :label="$t('datasource.headers')"
              name="headers"
            >
              <el-tooltip
                slot="label"
                class="item-tabs"
                effect="dark"
                :content="$t('datasource.headers')"
                placement="top-start"
              >
                <span>{{ $t('datasource.headers') }}
                  <div
                    v-if="headers.length>1"
                    class="el-step__icon is-text ms-api-col ms-header"
                  >
                    <div class="el-step__icon-inner">{{ headers.length - 1 }}</div>
                  </div>
                </span>
              </el-tooltip>
              <api-key-value
                :show-desc="true"
                :suggestions="headerSuggestions"
                :items="headers"
              />
            </el-tab-pane>

            <!--query 参数-->
            <el-tab-pane :label="$t('datasource.query_param')" name="parameters">
              <el-tooltip class="item-tabs" effect="dark" :content="$t('datasource.query_info')"
                          placement="top-start" slot="label">
              <span>{{ $t('datasource.query_param') }}
                <div class="el-step__icon is-text ms-api-col ms-header" v-if="request.arguments.length>1">
                  <div class="el-step__icon-inner">{{ request.arguments.length - 1 }}</div>
                </div>
              </span>
              </el-tooltip>
              <api-variable
                @editScenarioAdvance="editScenarioAdvance"
                :scenario-definition="scenarioDefinition"
                :with-more-setting="true"
                :is-read-only="isReadOnly"
                :isShowEnable="isShowEnable"
                :parameters="request.arguments"
                v-if="activeName === 'parameters'"
              />
            </el-tab-pane>

            <!--请求体-->
            <el-tab-pane
              v-if="isBodyShow"
              :label="$t('datasource.request_body')"
              name="body"
              style="overflow: auto"
            >
              <api-body
                :is-show-enable="isShowEnable"
                :headers="headers"
                :body="request.body"
                @headersChange="reloadBody"
              />
            </el-tab-pane>

            <!-- 认证配置 -->
            <el-tab-pane
              :label="$t('datasource.auth_config')"
              name="authConfig"
            >
              <el-tooltip
                slot="label"
                class="item-tabs"
                effect="dark"
                :content="$t('datasource.auth_config_info')"
                placement="top-start"
              >
                <span>{{ $t('datasource.auth_config') }}</span>
              </el-tooltip>
              <api-auth-config :request="request" />
            </el-tab-pane>

          </el-tabs>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import ApiKeyValue from '@/views/system/datasource/ApiKeyValue'
import ApiBody from '@/views/system/datasource/ApiBody'
import ApiVariable from '@/views/system/datasource/ApiVariable.vue'
import ApiAuthConfig from '@/views/system/datasource/ApiAuthConfig'
import { Body, KeyValue } from '@/views/system/datasource/ApiTestModel'
import Convert from '@/views/system/datasource/convert'

export default {
  name: 'ApiHttpRequestForm',
  components: {
    ApiAuthConfig,
    ApiBody,
    ApiKeyValue,
    ApiVariable
  },
  props: {
    method: String,
    request: {
      type: Object,
      default: () => {}
    },
    definitionTest: {
      type: Boolean,
      default() {
        return false
      }
    },
    showScript: {
      type: Boolean,
      default: true
    },
    headers: {
      type: Array,
      default() {
        return []
      }
    },
    referenced: {
      type: Boolean,
      default: false
    },
    isShowEnable: {
      type: Boolean,
      default: false
    },
    jsonPathList: Array,
    isReadOnly: {
      type: Boolean,
      default: false
    },
    type: String
  },
  data() {
    const validateURL = (rule, value, callback) => {
      try {
        new URL(this.addProtocol(this.request.url))
      } catch (e) {
        callback(this.$t('api_test.request.url_invalid'))
      }
    }
    return {
      activeName: 'headers',
      rules: {
        name: [
          { max: 300, message: this.$t('commons.input_limit', [1, 300]), trigger: 'blur' }
        ],
        url: [
          { max: 500, required: true, message: this.$t('commons.input_limit', [1, 500]), trigger: 'blur' },
          { validator: validateURL, trigger: 'blur' }
        ],
        path: [
          { max: 500, message: this.$t('commons.input_limit', [0, 500]), trigger: 'blur' }
        ]
      },
      spanCount: 21,
      isReloadData: false,
      isBodyShow: true,
      dialogVisible: false,
      hasOwnProperty: Object.prototype.hasOwnProperty,
      propIsEnumerable: Object.prototype.propertyIsEnumerable,
      headerSuggestions: [
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

    }
  },
  watch: {
    'request.changeId'() {
      if (this.request.headers && this.request.headers.length > 1) {
        this.activeName = 'headers'
      }
      if (this.request.rest && this.request.rest.length > 1) {
        this.activeName = 'rest'
      }
      if (this.request.arguments && this.request.arguments.length > 1) {
        this.activeName = 'parameters'
      }
      if (this.request.body) {
        this.request.body.typeChange = this.request.changeId
      }
      this.reload()
    }
  },
  created() {
    if (!this.referenced && this.showScript) {
      this.spanCount = 21
    } else {
      this.spanCount = 24
    }
    this.init()
  },
  methods: {
    generate() {
      if (this.request.body && (this.request.body.jsonSchema || this.request.body.raw)) {
        if (!this.request.body.jsonSchema) {
          const MsConvert = new Convert()
          this.request.body.jsonSchema = MsConvert.format(JSON.parse(this.request.body.raw))
        }
        this.$post('/api/test/data/generator', this.request.body.jsonSchema, response => {
          if (response.data) {
            if (this.request.body.format !== 'JSON-SCHEMA') {
              this.request.body.raw = response.data
            } else {
              const MsConvert = new Convert()
              const data = MsConvert.format(JSON.parse(response.data))
              this.request.body.jsonSchema = this.deepAssign(this.request.body.jsonSchema, data)
            }
            this.reloadBody()
          }
        })
      }
    },
    remove(row) {
      const index = this.request.hashTree.indexOf(row)
      this.request.hashTree.splice(index, 1)
      this.reload()
    },
    copyRow(row) {
      const obj = JSON.parse(JSON.stringify(row))
      this.request.hashTree.push(obj)
      this.reload()
    },
    reload() {
      this.isReloadData = true
      this.$nextTick(() => {
        this.isReloadData = false
      })
    },
    init() {
      if (Object.prototype.toString.call(this.request).match(/\[object (\w+)\]/)[1].toLowerCase() !== 'object') {
        this.request = JSON.parse(this.request)
      }
      if (!this.request.body) {
        this.request.body = new Body()
      }
      if (!this.request.body.kvs) {
        this.request.body.kvs = []
      }
      if (!this.request.rest) {
        this.request.rest = []
      }
      if (!this.request.arguments) {
        this.request.arguments = []
      }
    },
    reloadBody() {
      // 解决修改请求头后 body 显示错位
      this.isBodyShow = false
      this.$nextTick(() => {
        this.isBodyShow = true
      })
    },
    batchAdd() {
      this.$refs.batchAddParameter.open()
    },
    format(array, obj) {
      if (array) {
        let isAdd = true
        for (const i in array) {
          const item = array[i]
          if (item.name === obj.name) {
            item.value = obj.value
            isAdd = false
          }
        }
        if (isAdd) {
          switch (this.activeName) {
            case 'parameters':
              this.request.arguments.unshift(obj)
              break
            case 'rest':
              this.request.rest.unshift(obj)
              break
            case 'headers':
              this.request.headers.unshift(obj)
              break
            default:
              break
          }
        }
      }
    },
    batchSave(data) {
      if (data) {
        const params = data.split('\n')
        const keyValues = []
        params.forEach(item => {
          const line = item.split(/：|:/)
          const required = false
          keyValues.unshift(new KeyValue({
            name: line[0],
            required: !required,
            value: line[1],
            description: line[2],
            type: 'text',
            valid: false,
            file: false,
            encode: true,
            enable: true,
            contentType: 'text/plain'
          }))
        })

        keyValues.forEach(item => {
          switch (this.activeName) {
            case 'parameters':
              this.format(this.request.arguments, item)
              break
            case 'rest':
              this.format(this.request.rest, item)
              break
            case 'headers':
              this.format(this.request.headers, item)
              break
            default:
              break
          }
        })
      }
    },

    isObj(x) {
      const type = typeof x
      return x !== null && (type === 'object' || type === 'function')
    },

    toObject(val) {
      if (val === null || val === undefined) {
        return
      }

      return Object(val)
    },

    assignKey(to, from, key) {
      const val = from[key]

      if (val === undefined || val === null) {
        return
      }
      if (!this.hasOwnProperty.call(to, key) || !this.isObj(val)) {
        to[key] = val
      } else {
        to[key] = this.assign(Object(to[key]), from[key])
      }
    },

    assign(to, from) {
      if (to === from) {
        return to
      }
      from = Object(from)
      for (const key in from) {
        if (this.hasOwnProperty.call(from, key)) {
          this.assignKey(to, from, key)
        }
      }

      if (Object.getOwnPropertySymbols) {
        const symbols = Object.getOwnPropertySymbols(from)

        for (let i = 0; i < symbols.length; i++) {
          if (this.propIsEnumerable.call(from, symbols[i])) {
            this.assignKey(to, from, symbols[i])
          }
        }
      }

      return to
    },

    deepAssign(target) {
      target = this.toObject(target)
      for (let s = 1; s < arguments.length; s++) {
        this.assign(target, arguments[s])
      }
      return target
    }
  }
}
</script>

<style scoped>
.ms-query {
  background: #409EFF;
  color: white;
  height: 18px;
  border-radius: 42%;
}

.ms-header {
  background: #409EFF;
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
</style>
