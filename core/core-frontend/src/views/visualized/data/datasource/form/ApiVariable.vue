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
    label: '参数',
    value: 'params'
  },
  {
    label: '固定值',
    value: 'fixed'
  },
  {
    label: '时间函数',
    value: 'timeFun'
  },
  {
    label: '自定义',
    value: 'custom'
  }
]
const timeFunLists = [
  {
    label: '当天（yyyy-MM-dd）',
    value: 'currentDay yyyy-MM-dd'
  },
  {
    label: '当天（yyyy/MM/dd）',
    value: 'currentDay yyyy/MM/dd'
  }
]
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
            <el-col v-if="element.type !== 'file'" :span="6">
              <el-input
                v-if="activeName === 'params'"
                v-model="element.value"
                :disabled="isReadOnly"
                class="input-with-autocomplete"
                :placeholder="valueText"
                value-key="name"
                highlight-first-item
              />

              <el-select
                v-model="element.value"
                v-if="!needMock && activeName === 'table' && element.nameType === 'params'"
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
                  activeName === 'table' &&
                  element.nameType !== 'params' &&
                  element.nameType !== 'timeFun'
                "
                v-model="element.value"
                :disabled="isReadOnly"
                class="input-with-autocomplete"
                :placeholder="element.nameType === 'fixed' ? '值' : '可用${参数名}，使用参数'"
                value-key="name"
                highlight-first-item
              />
            </el-col>

            <el-col :span="activeName === 'params' ? 10 : 7">
              <el-input
                v-model="element.description"
                maxlength="200"
                :placeholder="$t('common.description')"
                show-word-limit
              />
            </el-col>
            <el-col :span="1">
              <el-button text :disabled="isDisable() || isReadOnly" @click="remove(index)">
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

    <el-button @click="change" text>
      <template #icon>
        <icon name="icon_add_outlined"><icon_add_outlined class="svg-icon" /></icon>
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
