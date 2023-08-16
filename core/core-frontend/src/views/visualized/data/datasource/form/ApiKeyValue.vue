<script lang="ts" setup>
import { propTypes } from '@/utils/propTypes'
import { computed, onBeforeMount, PropType, toRefs } from 'vue'
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
})

const remove = (index: number) => {
  if (isDisable(index)) return
  // 移除整行输入控件及内容
  items.value.splice(index, 1)
}
const change = () => {
  items.value.push(new KeyValue({ enable: true }))
}
const isDisable = (index: number) => {
  return items.value.length - 1 === index
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
  <div class="api-key-value">
    <draggable tag="div" :list="items" handle=".handle">
      <template #item="{ element, index }">
        <div style="margin-bottom: 16px">
          <el-row :gutter="8">
            <el-icon class="drag handle">
              <Icon name="icon_drag_outlined"></Icon>
            </el-icon>
            <el-col :span="8" v-if="!unShowSelect">
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

            <el-col :span="7">
              <el-input
                v-if="!needMock"
                v-model="element.value"
                :disabled="isReadOnly"
                :placeholder="unShowSelect ? t('commons.description') : valueText"
                show-word-limit
              />
            </el-col>

            <el-col :span="7" v-if="showDesc">
              <el-input
                v-model="element.description"
                maxlength="200"
                :placeholder="t('commons.description')"
                show-word-limit
              />
            </el-col>

            <el-col :span="1">
              <el-button text :disabled="isDisable(index)" @click="remove(index)">
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

<style lang="less">
.api-key-value {
  & > .ed-input,
  .ed-autocomplete {
    width: 100%;
  }
  .drag {
    margin-top: 10px;
    cursor: pointer;
  }
}
</style>
