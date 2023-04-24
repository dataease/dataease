<script lang="ts" setup>
import { propTypes } from '@/utils/propTypes'
import { onBeforeMount, watch, reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import ApiVariable from './ApiVariable.vue'
import CodeEdit from './CodeEdit.vue'
import Convert from './convert.js'
import { KeyValue, BODY_TYPE } from './ApiTestModel.js'

export interface ApiBodyItem {
  raw?: string
  typeChange: string
  format?: string
  jsonSchema?: string
  type?: string
  kvs: Item[]
}

export interface Item {
  name: string
  value: string
  description: string
  type: string
}
defineProps({
  isReadOnly: propTypes.bool.def(false),
  isShowEnable: propTypes.bool.def(false)
})
const { t } = useI18n()
let headers = []
const modes = ['text', 'json', 'xml', 'html']
const hasOwnProperty = Object.prototype.hasOwnProperty
const propIsEnumerable = Object.prototype.propertyIsEnumerable
const apiBody = reactive<ApiBodyItem>({
  raw: '',
  typeChange: '',
  format: '',
  jsonSchema: '',
  type: '',
  kvs: []
})

const initApiBody = (body, header) => {
  Object.assign(apiBody, body)
  headers = header
}

watch([apiBody.raw], () => {
  if (apiBody.format !== 'JSON-SCHEMA' && apiBody.raw) {
    try {
      const MsConvert = new Convert()
      const data = MsConvert.format(JSON.parse(apiBody.raw))
      if (apiBody.jsonSchema) {
        apiBody.jsonSchema = deepAssign(data)
      } else {
        apiBody.jsonSchema = data
      }
    } catch (ex) {
      apiBody.jsonSchema = ''
    }
  }
})

onBeforeMount(() => {
  if (!apiBody.type) {
    apiBody.type = BODY_TYPE.FORM_DATA
  }
  if (apiBody.kvs) {
    apiBody.kvs.forEach(param => {
      if (!param.type) {
        param.type = 'text'
      }
    })
  }
})

const isObj = x => {
  const type = typeof x
  return x !== null && (type === 'object' || type === 'function')
}

const toObject = val => {
  if (val === null || val === undefined) {
    return
  }
  return Object(val)
}

const assignKey = (to, from, key) => {
  const val = from[key]
  if (val === undefined || val === null) {
    return
  }

  if (!hasOwnProperty.call(to, key) || !isObj(val)) {
    to[key] = val
  } else {
    to[key] = assign(Object(to[key]), from[key])
  }
}

const assign = (to, from) => {
  if (to === from) {
    return to
  }
  from = Object(from)
  for (const key in from) {
    if (hasOwnProperty.call(from, key)) {
      assignKey(to, from, key)
    }
  }

  if (Object.getOwnPropertySymbols) {
    const symbols = Object.getOwnPropertySymbols(from)

    for (let i = 0; i < symbols.length; i++) {
      if (propIsEnumerable.call(from, symbols[i])) {
        assignKey(to, from, symbols[i])
      }
    }
  }

  return to
}

const deepAssign = function (target) {
  target = toObject(target)
  for (let s = 1; s < arguments.length; s++) {
    assign(target, arguments[s])
  }
  return target
}
const modeChange = () => {
  switch (apiBody.type) {
    case 'JSON':
      setContentType('application/json')
      break
    case 'XML':
      setContentType('text/xml')
      break
    case 'WWW_FORM':
      setContentType('application/x-www-form-urlencoded')
      break
    case 'BINARY':
      setContentType('application/octet-stream')
      break
    default:
      removeContentType()
      break
  }
}
watch(
  apiBody,
  val => {
    emits('bodyChange', val)
  },
  { deep: true }
)
const setContentType = value => {
  let isType = false
  headers.forEach(item => {
    if (item.name === 'Content-Type') {
      item.value = value
      isType = true
    }
  })
  if (!isType) {
    headers.unshift(new KeyValue({ name: 'Content-Type', value: value }))
    emits('headersChange', headers)
  }
}
const removeContentType = () => {
  let oldVal = headers.length
  headers = headers.filter(ele => {
    return ele.name !== 'Content-Type'
  })
  let newVal = headers.length
  if (oldVal !== newVal) {
    emits('headersChange', headers)
  }
}
const moveParameters = val => {
  const pre = apiBody.kvs[val]
  const next = apiBody.kvs[val + 1]
  apiBody.kvs.splice(val, 2, ...[next, pre])
  emits('kvsChange', val)
}
const changeParameters = val => {
  if (!isNaN(val)) {
    apiBody.kvs.splice(val, 1)
  } else {
    apiBody.kvs.push(val)
  }
  emits('kvsChange', val)
}
defineExpose({
  initApiBody
})

const emits = defineEmits(['headersChange', 'bodyChange', 'kvsChange'])
</script>

<template>
  <div>
    <el-radio-group v-model="apiBody.type">
      <el-radio :disabled="isReadOnly" :label="BODY_TYPE.FORM_DATA" @change="modeChange">
        {{ t('datasource.body_form_data') }}
      </el-radio>

      <el-radio :disabled="isReadOnly" :label="BODY_TYPE.WWW_FORM" @change="modeChange">
        {{ t('datasource.body_x_www_from_urlencoded') }}
      </el-radio>

      <el-radio :disabled="isReadOnly" :label="BODY_TYPE.JSON" @change="modeChange">
        {{ t('datasource.body_json') }}
      </el-radio>

      <el-radio :disabled="isReadOnly" :label="BODY_TYPE.XML" @change="modeChange">
        {{ t('datasource.body_xml') }}
      </el-radio>

      <el-radio :disabled="isReadOnly" :label="BODY_TYPE.RAW" @change="modeChange">
        {{ t('datasource.body_raw') }}
      </el-radio>
    </el-radio-group>
    <div v-if="apiBody.type == 'Form_Data' || apiBody.type == 'WWW_FORM'" style="min-width: 1200px">
      <api-variable
        :is-read-only="isReadOnly"
        :parameters="apiBody.kvs"
        :is-show-enable="isShowEnable"
        type="body"
        @change-parameters="changeParameters"
        @move-parameters="moveParameters"
      />
    </div>
    <div v-if="apiBody.type == 'JSON'">
      <code-edit
        ref="codeEdit"
        :read-only="isReadOnly"
        v-model="apiBody.raw"
        :data="apiBody.raw"
        :modes="modes"
        mode="json"
        height="200px"
      />
    </div>

    <div v-if="apiBody.type == 'XML'" class="ms-body">
      <code-edit
        ref="codeEdit"
        :read-only="isReadOnly"
        v-model="apiBody.raw"
        :data="apiBody.raw"
        height="200px"
        mode="xml"
        :modes="modes"
      />
    </div>

    <div v-if="apiBody.type == 'Raw'" class="ms-body">
      <code-edit
        ref="codeEdit"
        :read-only="isReadOnly"
        v-model="apiBody.raw"
        :data="apiBody.raw"
        height="200px"
        :modes="modes"
      />
    </div>
  </div>
</template>

<style lang="less" scoped>
.ms-body {
  padding: 15px 0;
  height: 400px;
}
</style>
