<script lang="ts" setup>
import icon_drag_outlined from '@/assets/svg/icon_drag_outlined.svg'
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import icon_add_outlined from '@/assets/svg/icon_add_outlined.svg'
import { propTypes } from '@/utils/propTypes'
import { computed, onBeforeMount, PropType, toRefs, inject } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { KeyValue } from './ApiTestModel.js'
import { guid } from '@/views/visualized/data/dataset/form/util'
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
  valueList: {
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
        nameType: 'fixed',
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
  if (isDisable()) return
  // 移除整行输入控件及内容
  parameters.value.splice(index, 1)
}
const change = () => {
  parameters.value.push(
    new KeyValue({
      type: 'text',
      enable: true,
      nameType: 'fixed',
      uuid: guid(),
      contentType: 'text/plain'
    })
  )
}
const isDisable = () => {
  return parameters.value.length === 1
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
const changeNameType = element => {
  element.value = ''
}
const activeName = inject('api-active-name')
const options = [
  {
    label: t('data_source.parameter'),
    value: 'params'
  },
  {
    label: t('data_source.page_parameter'),
    value: 'pageParams'
  },
  {
    label: t('data_source.fixed_value'),
    value: 'fixed'
  },
  {
    label: t('data_source.time_function'),
    value: 'timeFun'
  },
  {
    label: t('data_source.customize'),
    value: 'custom'
  }
]
const pageParams = [
  {
    label: '${pageNumber}',
    value: '${pageNumber}'
  },
  {
    label: '${pageSize}',
    value: '${pageSize}'
  },
  {
    label: '${pageToken}',
    value: '${pageToken}'
  }
]
const timeFunLists = [
  {
    label: t('data_source.that_day') + '（yyyy-MM-dd）',
    value: 'currentDay yyyy-MM-dd'
  },
  {
    label: t('data_source.that_day') + '（yyyy/MM/dd）',
    value: 'currentDay yyyy/MM/dd'
  }
]
</script>

<template>
  <div class="api-variable">
    <span v-if="description" class="kv-description">
      {{ description }}
    </span>
    <draggable class="draggable-content_api" tag="div" :list="parameters" handle=".handle">
      <template #item="{ element, index }">
        <div :key="index" style="margin-bottom: 16px">
          <el-row :gutter="8">
            <el-icon class="drag handle">
              <Icon name="icon_drag_outlined"><icon_drag_outlined class="svg-icon" /></Icon>
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
            <el-col :span="3">
              <el-select v-model="element.nameType" @change="changeNameType(element)">
                <el-option
                  v-for="item in options"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-col>
            <el-col v-if="element.type !== 'file'" :span="6">
              <el-select
                v-model="element.value"
                v-if="!needMock && element.nameType === 'params'"
                style="width: 100%"
              >
                <el-option
                  v-for="item in valueList"
                  :key="item.originName"
                  :label="item.name"
                  :value="item.originName"
                />
              </el-select>
              <el-select
                v-model="element.value"
                v-if="!needMock && element.nameType === 'timeFun'"
                style="width: 100%"
              >
                <el-option
                  v-for="item in timeFunLists"
                  :key="item.originName"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
              <el-select
                v-model="element.value"
                v-if="!needMock && element.nameType === 'pageParams'"
                style="width: 100%"
              >
                <el-option
                  v-for="item in pageParams"
                  :key="item.originName"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
              <el-input
                v-if="
                  element.nameType !== 'params' &&
                  element.nameType !== 'timeFun' &&
                  element.nameType !== 'pageParams'
                "
                v-model="element.value"
                :disabled="isReadOnly"
                class="input-with-autocomplete"
                :placeholder="
                  element.nameType === 'fixed'
                    ? t('data_source.value')
                    : t('data_source.name_use_parameters')
                "
                value-key="name"
                highlight-first-item
              />
            </el-col>

            <el-col :span="7">
              <el-input
                v-model="element.description"
                maxlength="200"
                :placeholder="$t('common.description')"
                show-word-limit
              />
            </el-col>
            <el-col :span="1">
              <el-button
                class="api-variable_del"
                text
                :disabled="isDisable() || isReadOnly"
                @click="remove(index)"
              >
                <template #icon>
                  <Icon><icon_deleteTrash_outlined class="svg-icon" /></Icon>
                </template>
              </el-button>
            </el-col>
          </el-row>
        </div>
      </template>
    </draggable>

    <el-button style="margin-top: 14px" @click="change" text>
      <template #icon>
        <icon name="icon_add_outlined"><icon_add_outlined class="svg-icon" /></icon>
      </template>
      {{ t('data_source.add_parameters') }}
    </el-button>
  </div>
</template>

<style lang="less" scoped>
.api-variable {
  padding-bottom: 14px;
  & > .ed-input,
  :deep(.ed-autocomplete) {
    width: 100%;
  }
  .drag {
    margin-top: 10px;
    cursor: pointer;
  }
  :deep(.draggable-content_api) > :last-child {
    margin-bottom: 0 !important;
  }

  .api-variable_del {
    color: #646a73;
    :deep(.ed-icon) {
      font-size: 16px;
    }

    &:hover {
      background: rgba(31, 35, 41, 0.1) !important;
    }
    &:focus {
      background: rgba(31, 35, 41, 0.1) !important;
    }
    &:active {
      background: rgba(31, 35, 41, 0.2) !important;
    }
  }
  .kv-description {
    font-size: 13px;
  }
}
</style>
