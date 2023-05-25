<script lang="ts" setup>
import { propTypes } from '@/utils/propTypes'
import { computed, onBeforeMount, PropType } from 'vue'
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

onBeforeMount(() => {
  if (!props.items.length || props.items[props.items.length - 1].name) {
    emits('changeHeaders', new KeyValue({ enable: true, name: '', value: '' }))
  }
})

const moveBottom = (index: number) => {
  if (props.items.length < 2 || index === props.items.length - 2) {
    return
  }
  emits('moveHeaders', index)
}
const moveTop = (index: number) => {
  if (index === 0) {
    return
  }
  emits('moveHeaders', index - 1)
}
const remove = (index: number) => {
  if (isDisable(index)) return
  // 移除整行输入控件及内容
  emits('changeHeaders', index)
}
const change = () => {
  const isNeedCreate = !props.items.some(item => !item.name && !item.value)
  if (isNeedCreate) {
    emits('changeHeaders', new KeyValue({ enable: true }))
  }
}
const isDisable = (index: number) => {
  return props.items.length - 1 === index
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
const emits = defineEmits(['changeHeaders', 'moveHeaders'])
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
            v-if="suggestions"
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
.el-autocomplete {
  width: 100%;
}
</style>
