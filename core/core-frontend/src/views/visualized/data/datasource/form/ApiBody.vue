<script lang="ts" setup>
import { propTypes } from '@/utils/propTypes'
import { onBeforeMount, watch, toRefs, PropType } from 'vue'
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
const props = defineProps({
  isReadOnly: propTypes.bool.def(false),
  isShowEnable: propTypes.bool.def(false),
  body: {
    type: Object as PropType<ApiBodyItem>,
    default: () => ({
      raw: '',
      typeChange: '',
      format: '',
      jsonSchema: '',
      type: '',
      kvs: []
    })
  },
  headers: {
    type: Array as PropType<Item[]>,
    default: () => []
  }
})
const { t } = useI18n()
const modes = ['text', 'json', 'xml', 'html']
const hasOwnProperty = Object.prototype.hasOwnProperty
const propIsEnumerable = Object.prototype.propertyIsEnumerable
const { body: apiBody, headers } = toRefs(props)

watch(
  () => apiBody.value.raw,
  () => {
    if (apiBody.value.format !== 'JSON-SCHEMA' && apiBody.value.raw) {
      try {
        const MsConvert = new Convert()
        const data = MsConvert.format(JSON.parse(apiBody.value.raw))
        if (apiBody.value.jsonSchema) {
          apiBody.value.jsonSchema = deepAssign(data)
        } else {
          apiBody.value.jsonSchema = data
        }
      } catch (ex) {
        apiBody.value.jsonSchema = ''
      }
    }
  }
)

onBeforeMount(() => {
  if (!apiBody.value.type) {
    apiBody.value.type = BODY_TYPE.FORM_DATA
  }
  if (apiBody.value.kvs) {
    apiBody.value.kvs.forEach(param => {
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
  switch (apiBody.value.type) {
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

const setContentType = value => {
  let isType = false
  headers.value.forEach(item => {
    if (item.name === 'Content-Type') {
      item.value = value
      isType = true
    }
  })
  if (!isType) {
    headers.value.unshift(new KeyValue({ name: 'Content-Type', value: value }))
  }
}
const removeContentType = () => {
  for (const index in headers.value) {
    if (headers.value[index].name === 'Content-Type') {
      headers.value.splice(parseInt(index), 1)
      emits('headersChange')
      return
    }
  }
}
const changeParameters = val => {
  if (!isNaN(val)) {
    apiBody.value.kvs.splice(val, 1)
  } else {
    apiBody.value.kvs.push(val)
  }
}

const emits = defineEmits(['headersChange'])
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
      />
    </div>
    <div v-if="apiBody.type == 'JSON'" class="ms-body">
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
}
</style>
