<script lang="ts" setup>
import { propTypes } from '@/utils/propTypes'
import { computed, onBeforeMount, PropType, toRefs } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { KeyValue } from './ApiTestModel.js'

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

const moveBottom = (index: number) => {
  if (items.value.length < 2 || index === items.value.length - 2) {
    return
  }
  const thisRow = items.value[index]
  const nextRow = items.value[index + 1]
  items.value[index] = nextRow
  items.value[index + 1] = thisRow
}
const moveTop = (index: number) => {
  if (index === 0) {
    return
  }
  const thisRow = items.value[index]
  const lastRow = items.value[index - 1]
  items.value[index] = lastRow
  items.value[index - 1] = thisRow
}
const remove = (index: number) => {
  if (isDisable(index)) return
  // 移除整行输入控件及内容
  items.value.splice(index, 1)
}
const change = () => {
  const isNeedCreate = !items.value.some(item => !item.name && !item.value)
  if (isNeedCreate) {
    items.value.push(new KeyValue({ enable: true }))
  }
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
  <div>
    <div v-for="(item, index) in items" :key="index" style="margin-top: 10px">
      <el-row :gutter="20" justify="space-between" align="middle">
        <span style="margin-left: 10px" />
        <i class="el-icon-top" style="cursor: pointer" @click="moveTop(index)" />
        <i class="el-icon-bottom" style="cursor: pointer" @click="moveBottom(index)" />

        <el-col :span="6" v-if="!unShowSelect">
          <el-input
            v-if="!suggestions"
            v-model="item.name"
            :disabled="isReadOnly"
            maxlength="200"
            :placeholder="keyText"
            show-word-limit
            @change="change"
          />
          <el-autocomplete
            v-else
            v-model="item.name"
            :disabled="isReadOnly"
            :maxlength="400"
            :fetch-suggestions="querySearch"
            :placeholder="keyText"
            show-word-limit
            @change="change"
          />
        </el-col>

        <el-col :span="6" v-if="unShowSelect">
          <el-input
            v-if="!!suggestions.length"
            v-model="item.name"
            :disabled="isReadOnly"
            maxlength="200"
            :placeholder="keyText"
            show-word-limit
          />
        </el-col>

        <el-col :span="6">
          <el-input
            v-if="!needMock"
            v-model="item.value"
            :disabled="isReadOnly"
            :placeholder="unShowSelect ? t('common.description') : valueText"
            show-word-limit
            @change="change"
          />
        </el-col>

        <el-col :span="6" v-if="showDesc">
          <el-input
            v-model="item.description"
            maxlength="200"
            :placeholder="t('common.description')"
            show-word-limit
          />
        </el-col>

        <el-col :span="2">
          <el-button text :disabled="isDisable(index)" @click="remove(index)">
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
.ed-autocomplete {
  width: 100%;
}
</style>
