<script lang="ts" setup>
import icon_drag_outlined from '@/assets/svg/icon_drag_outlined.svg'
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import icon_add_outlined from '@/assets/svg/icon_add_outlined.svg'
import { propTypes } from '@/utils/propTypes'
import { computed, onBeforeMount, PropType, toRefs, inject } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { KeyValue } from './ApiTestModel.js'
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
  unShowSelect: propTypes.bool.def(false),
  isReadOnly: propTypes.bool.def(false),
  needMock: propTypes.bool.def(false),
  showDesc: propTypes.bool.def(false),
  items: {
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

const { suggestions, items } = toRefs(props)

onBeforeMount(() => {
  if (!items.value.length || items.value[items.value.length - 1].name) {
    items.value.push(new KeyValue({ enable: true, name: '', value: '' }))
  }
  for (let i = 0; i < items.value.length; i++) {
    if (!items.value[i].hasOwnProperty('nameType')) {
      items.value[i].nameType = 'fixed'
    }
  }
})

const activeName = inject('api-active-name')

const remove = (index: number) => {
  if (isDisable()) return
  // 移除整行输入控件及内容
  items.value.splice(index, 1)
}
const change = () => {
  items.value.push(new KeyValue({ enable: true, nameType: 'fixed' }))
}
const isDisable = () => {
  return items.value.length === 1
}
const querySearch = (queryString, cb) => {
  const results = queryString
    ? suggestions.value.filter(createFilter(queryString))
    : suggestions.value
  cb(results)
}

const changeNameType = element => {
  element.value = ''
}
const createFilter = (queryString: string) => {
  return restaurant => {
    return restaurant.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0
  }
}
const options = [
  {
    label: t('data_source.parameter'),
    value: 'params'
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
  <div class="api-key-value">
    <draggable tag="div" :list="items" handle=".handle" class="draggable-content_api">
      <template #item="{ element, index }">
        <div style="margin-bottom: 16px">
          <el-row :gutter="8">
            <el-icon class="drag handle">
              <Icon name="icon_drag_outlined"><icon_drag_outlined class="svg-icon" /></Icon>
            </el-icon>
            <el-col :span="activeName === 'params' ? 8 : 6" v-if="!unShowSelect">
              <el-input
                v-if="!suggestions"
                v-model="element.name"
                :disabled="isReadOnly"
                maxlength="200"
                :placeholder="keyText"
                show-word-limit
              />
              <el-autocomplete
                v-else
                v-model="element.name"
                :disabled="isReadOnly"
                :maxlength="400"
                :fetch-suggestions="querySearch"
                :placeholder="keyText"
                show-word-limit
              />
            </el-col>
            <el-col :span="3" v-if="activeName === 'table'">
              <el-select v-model="element.nameType" @change="changeNameType(element)">
                <el-option
                  v-for="item in options"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-col>
            <el-col :span="8" v-if="unShowSelect">
              <el-input
                v-if="!!suggestions.length"
                v-model="element.name"
                :disabled="isReadOnly"
                maxlength="200"
                :placeholder="keyText"
                show-word-limit
              />
            </el-col>

            <el-col :span="activeName === 'params' ? 7 : 6">
              <el-input
                v-if="!needMock && activeName === 'params'"
                v-model="element.value"
                :disabled="isReadOnly"
                :placeholder="unShowSelect ? t('common.description') : valueText"
                show-word-limit
              />
              <el-select
                v-model="element.value"
                v-if="!needMock && activeName === 'table' && element.nameType === 'params'"
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
                v-if="!needMock && activeName === 'table' && element.nameType === 'timeFun'"
                style="width: 100%"
              >
                <el-option
                  v-for="item in timeFunLists"
                  :key="item.originName"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>

              <el-input
                v-if="
                  !needMock &&
                  activeName === 'table' &&
                  element.nameType !== 'params' &&
                  element.nameType !== 'timeFun'
                "
                v-model="element.value"
                :disabled="isReadOnly"
                :placeholder="
                  element.nameType === 'fixed'
                    ? t('data_source.value')
                    : t('data_source.name_use_parameters')
                "
                show-word-limit
              />
            </el-col>

            <el-col :span="7" v-if="showDesc">
              <el-input
                v-model="element.description"
                maxlength="200"
                :placeholder="t('common.description')"
                show-word-limit
              />
            </el-col>

            <el-col :span="1">
              <el-button
                class="api-variable_del"
                text
                :disabled="isDisable()"
                @click="remove(index)"
              >
                <template #icon>
                  <Icon name="icon_delete-trash_outlined"
                    ><icon_deleteTrash_outlined class="svg-icon"
                  /></Icon>
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
.api-key-value {
  padding-bottom: 14px;

  & > .ed-input,
  .ed-autocomplete {
    width: 100%;
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
  .drag {
    margin-top: 10px;
    cursor: pointer;
  }

  :deep(.draggable-content_api) > :last-child {
    margin-bottom: 0 !important;
  }
}
</style>
