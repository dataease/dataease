<script lang="ts" setup>
import { propTypes } from '@/utils/propTypes'
import { computed, onBeforeMount, PropType } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { KeyValue } from './ApiTestModel.js'
import { guid } from '@/views/visualized/data/dataset/form/util.js'

export interface Item {
  name: string
  value: string
  description: string
  type: string
}
const props = defineProps({
  keyPlaceholder: propTypes.string.def(''),
  valuePlaceholder: propTypes.string.def(''),
  description: propTypes.string.def(''),
  type: propTypes.string.def(''),
  isReadOnly: propTypes.bool.def(false),
  parameters: {
    type: Array as PropType<Item[]>,
    default: () => []
  },
  suggestions: {
    type: Array,
    default: () => []
  }
})

const { t } = useI18n()
const keyText = computed(() => {
  return props.keyPlaceholder || t('datasource.key')
})
const valueText = computed(() => {
  return props.valuePlaceholder || t('datasource.value')
})

onBeforeMount(() => {
  if (props.parameters.length === 0 || props.parameters[props.parameters.length - 1].name) {
    emits(
      'changeParameters',
      new KeyValue({
        type: 'text',
        enable: true,
        required: true,
        uuid: guid(),
        contentType: 'text/plain'
      })
    )
  }
})

const typeChange = item => {
  if (item.type === 'file') {
    item.contentType = 'application/octet-stream'
  } else if (item.type === 'text') {
    item.contentType = 'text/plain'
  } else {
    item.contentType = 'application/json'
  }
}

const moveBottom = (index: number) => {
  if (props.parameters.length < 2 || index === props.parameters.length - 2) {
    return
  }
  emits('moveParameters', index)
}
const moveTop = (index: number) => {
  if (index === 0) {
    return
  }
  emits('moveParameters', index - 1)
}
const remove = (index: number) => {
  if (isDisable(index)) return
  // 移除整行输入控件及内容
  emits('changeParameters', index)
}
const change = () => {
  const isNeedCreate = !props.parameters.some(item => !item.name && !item.value)
  if (isNeedCreate) {
    emits(
      'changeParameters',
      new KeyValue({
        type: 'text',
        enable: true,
        uuid: guid(),
        contentType: 'text/plain'
      })
    )
  }
}
const isDisable = (index: number) => {
  return props.parameters.length - 1 === index
}
const querySearch = (queryString, cb) => {
  const suggestions = props.suggestions
  const results = queryString ? suggestions.filter(createFilter(queryString)) : suggestions
  cb(results)
}
const createFilter = (queryString: string) => {
  return restaurant => {
    return restaurant.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0
  }
}
const emits = defineEmits(['changeParameters', 'moveParameters'])
</script>

<template>
  <div style="margin-bottom: 20px">
    <span v-if="description" class="kv-description">
      {{ description }}
    </span>
    <div v-for="(item, index) in parameters" :key="index" style="margin-top: 10px">
      <el-row :gutter="20" justify="space-between" align="middle">
        <span style="margin-left: 10px" />
        <i class="el-icon-top" style="cursor: pointer" @click="moveTop(index)" />
        <i class="el-icon-bottom" style="cursor: pointer" @click="moveBottom(index)" />

        <el-col :span="4">
          <el-input
            v-if="!suggestions"
            v-model="item.name"
            :disabled="isReadOnly"
            maxlength="200"
            :placeholder="keyText"
            show-word-limit
            @change="change"
          >
            <template #prepend>
              <el-select
                v-if="type === 'body'"
                v-model="item.type"
                :disabled="isReadOnly"
                class="kv-type"
                @change="typeChange(item)"
              >
                <el-option value="text" />
                <el-option value="json" />
              </el-select>
            </template>
          </el-input>

          <el-autocomplete
            v-else
            v-model="item.name"
            :disabled="isReadOnly"
            :fetch-suggestions="querySearch"
            :placeholder="keyText"
            show-word-limit
            @change="change"
          />
        </el-col>

        <el-col v-if="item.type !== 'file'" :span="4">
          <el-input
            v-model="item.value"
            :disabled="isReadOnly"
            class="input-with-autocomplete"
            :placeholder="valueText"
            value-key="name"
            highlight-first-item
            @select="change"
          />
        </el-col>

        <el-col :span="4">
          <el-input
            v-model="item.description"
            maxlength="200"
            :placeholder="$t('commons.description')"
            show-word-limit
          />
        </el-col>
        <el-col :span="4">
          <el-autocomplete
            v-if="suggestions"
            v-model="item.name"
            :disabled="isReadOnly"
            :fetch-suggestions="querySearch"
            :placeholder="keyText"
            show-word-limit
            @change="change"
          />
        </el-col>

        <el-col :span="2">
          <el-button text :disabled="isDisable(index) || isReadOnly" @click="remove(index)">
            <template #icon>
              <Icon name="icon_delete-trash_outlined"></Icon>
            </template>
          </el-button>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<style lang="less" scoped>
.kv-description {
  font-size: 13px;
}

.kv-row {
  margin-top: 10px;
}

.kv-checkbox {
  width: 20px;
  margin-right: 10px;
}

.kv-delete {
  width: 60px;
}

.el-autocomplete {
  width: 100%;
}
</style>
