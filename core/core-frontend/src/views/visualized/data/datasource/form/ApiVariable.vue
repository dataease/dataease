<script lang="ts" setup>
import { propTypes } from '@/utils/propTypes'
import { computed, onBeforeMount, PropType, toRefs } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { KeyValue } from './ApiTestModel.js'
import { guid } from '@/views/visualized/data/dataset/form/util.js'
import draggable from 'vuedraggable'

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

const { parameters, suggestions } = toRefs(props)

onBeforeMount(() => {
  if (parameters.value.length === 0 || parameters.value[parameters.value.length - 1].name) {
    parameters.value.push(
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

const remove = (index: number) => {
  if (isDisable(index)) return
  // 移除整行输入控件及内容
  parameters.value.splice(index, 1)
}
const change = () => {
  parameters.value.push(
    new KeyValue({
      type: 'text',
      enable: true,
      uuid: guid(),
      contentType: 'text/plain'
    })
  )
}
const isDisable = (index: number) => {
  return parameters.value.length - 1 === index
}
const querySearch = (queryString, cb) => {
  const results = queryString
    ? suggestions.value.filter(createFilter(queryString))
    : suggestions.value
  cb(results)
}
const createFilter = (queryString: string) => {
  return restaurant => {
    return restaurant.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0
  }
}
</script>

<template>
  <div class="api-variable">
    <span v-if="description" class="kv-description">
      {{ description }}
    </span>
    <draggable tag="div" :list="parameters" handle=".handle">
      <template #item="{ element, index }">
        <div :key="index" style="margin-bottom: 16px">
          <el-row :gutter="8">
            <el-icon class="drag handle">
              <Icon name="icon_drag_outlined"></Icon>
            </el-icon>
            <el-col :span="6">
              <el-input
                v-if="!suggestions"
                v-model="element.name"
                :disabled="isReadOnly"
                maxlength="200"
                :placeholder="keyText"
                show-word-limit
              >
                <template #prepend>
                  <el-select
                    v-if="type === 'body'"
                    v-model="element.type"
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
                v-model="element.name"
                :disabled="isReadOnly"
                :fetch-suggestions="querySearch"
                :placeholder="keyText"
                show-word-limit
              />
            </el-col>

            <el-col v-if="element.type !== 'file'" :span="6">
              <el-input
                v-model="element.value"
                :disabled="isReadOnly"
                class="input-with-autocomplete"
                :placeholder="valueText"
                value-key="name"
                highlight-first-item
                @select="change"
              />
            </el-col>

            <el-col :span="5">
              <el-input
                v-model="element.description"
                maxlength="200"
                :placeholder="$t('common.description')"
                show-word-limit
              />
            </el-col>
            <el-col :span="5">
              <el-autocomplete
                v-if="suggestions"
                v-model="element.name"
                :disabled="isReadOnly"
                :fetch-suggestions="querySearch"
                :placeholder="keyText"
                show-word-limit
              />
            </el-col>

            <el-col :span="1">
              <el-button text :disabled="isDisable(index) || isReadOnly" @click="remove(index)">
                <template #icon>
                  <Icon name="icon_delete-trash_outlined"></Icon>
                </template>
              </el-button>
            </el-col>
          </el-row>
        </div>
      </template>
    </draggable>

    <el-button @click="change" text>
      <template #icon>
        <icon name="icon_add_outlined"></icon>
      </template>
      添加参数
    </el-button>
  </div>
</template>

<style lang="less" scoped>
.api-variable {
  & > .ed-input,
  .ed-autocomplete {
    width: 100%;
  }
  .drag {
    margin-top: 10px;
    cursor: pointer;
  }
}
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

.ed-autocomplete {
  width: 100%;
}
</style>
