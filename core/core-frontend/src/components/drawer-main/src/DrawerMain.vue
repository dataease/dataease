<script lang="ts" setup>
import { ref, reactive, computed } from 'vue'
import { ElDrawer, ElButton } from 'element-plus-secondary'
import { propTypes } from '@/utils/propTypes'
import DrawerFilter from '@/components/drawer-filter/src/DrawerFilter.vue'
import DrawerEnumFilter from '@/components/drawer-filter/src/DrawerEnumFilter.vue'
import DrawerTimeFilter from '@/components/drawer-filter/src/DrawerTimeFilter.vue'
import DrawerTreeFilter from '@/components/drawer-filter/src/DrawerTreeFilter.vue'
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()
const props = defineProps({
  filterOptions: propTypes.arrayOf(
    propTypes.shape({
      type: propTypes.string,
      field: propTypes.string,
      option: propTypes.array,
      title: propTypes.string,
      property: propTypes.shape({})
    })
  ),
  title: propTypes.string
})
const myRefs = ref([])
const componentList = computed(() => {
  return props.filterOptions
})

const state = reactive({
  conditions: []
})
const userDrawer = ref(false)

const init = () => {
  userDrawer.value = true
}
const clearInnerTag = (index?: number) => {
  if (isNaN(index)) {
    for (let i = 0; i < componentList.value.length; i++) {
      myRefs.value[i]?.clear()
    }
    return
  }
  const condition = state.conditions[index]
  const field = condition?.field
  for (let i = 0; i < componentList.value.length; i++) {
    if (componentList.value[i].field === field) {
      myRefs.value[i]?.clear()
    }
  }
}
const clearFilter = (id?: number) => {
  clearInnerTag(id)
  if (isNaN(id)) {
    const len = state.conditions.length
    state.conditions.splice(0, len)
  } else {
    state.conditions.splice(id, 1)
  }
  trigger()
}
const filterChange = (value, field, operator) => {
  let exits = false
  let len = state.conditions.length
  while (len--) {
    const condition = state.conditions[len]
    if (condition.field === field) {
      exits = true
      condition['value'] = value
    }
    if (!value?.length) {
      state.conditions.splice(len, 1)
    }
  }
  if (!exits && value?.length) {
    state.conditions.push({ field, value, operator })
  }
}
const reset = () => {
  clearFilter()
  userDrawer.value = false
}
const close = () => {
  userDrawer.value = false
}
const emits = defineEmits(['trigger-filter'])
const trigger = () => {
  emits('trigger-filter', state.conditions)
}
defineExpose({
  init,
  clearFilter,
  close
})
</script>

<template>
  <el-drawer
    :title="t('common.filter_condition')"
    v-model="userDrawer"
    size="600px"
    custom-class="drawer-main-container"
    direction="rtl"
  >
    <div v-for="(component, index) in componentList" :key="index">
      <drawer-tree-filter
        :ref="el => (myRefs[index] = el)"
        v-if="component.type === 'tree-select'"
        :option-list="component.option"
        :title="component.title"
        :property="component.property"
        @filter-change="v => filterChange(v, component.field, 'in')"
      />
      <drawer-filter
        :ref="el => (myRefs[index] = el)"
        v-if="component.type === 'select'"
        :option-list="component.option"
        :title="component.title"
        :property="component.property"
        @filter-change="v => filterChange(v, component.field, 'in')"
      />
      <drawer-enum-filter
        :ref="el => (myRefs[index] = el)"
        v-if="component.type === 'enum'"
        :option-list="component.option"
        :title="component.title"
        @filter-change="v => filterChange(v, component.field, 'in')"
      />
      <drawer-time-filter
        :ref="el => (myRefs[index] = el)"
        v-if="component.type === 'time'"
        :title="component.title"
        :property="component.property"
        @filter-change="v => filterChange(v, component.field, component.operator)"
      />
    </div>

    <template #footer>
      <el-button @click="reset">{{ t('commons.reset') }}</el-button>
      <el-button @click="trigger" type="primary">{{ t('common.sure') }}</el-button>
    </template>
  </el-drawer>
</template>

<style lang="less">
.drawer-main-container {
  width: 600px;
  .ed-drawer__body {
    padding: 16px 24px 80px !important;
  }
  .ed-drawer__footer {
    padding-top: 10px !important;
    padding-right: 24px !important;
  }
}
</style>
