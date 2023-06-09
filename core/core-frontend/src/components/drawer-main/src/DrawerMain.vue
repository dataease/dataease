<script lang="ts" setup>
import { ref, reactive, computed } from 'vue'
import { ElDrawer, ElButton } from 'element-plus-secondary'
import { propTypes } from '@/utils/propTypes'
import DrawerFilter from '@/components/drawer-filter/src/DrawerFilter.vue'
import DrawerEnumFilter from '@/components/drawer-filter/src/DrawerEnumFilter.vue'
import DrawerTimeFilter from '@/components/drawer-filter/src/DrawerTimeFilter.vue'
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()
const props = defineProps({
  filterOptions: propTypes.arrayOf(
    propTypes.shape({
      type: propTypes.string,
      field: propTypes.string,
      option: propTypes.array,
      title: propTypes.string
    })
  ),
  title: propTypes.string
})

const componentList = computed(() => {
  return props.filterOptions
})

const state = reactive({
  conditions: []
})
const userDrawer = ref(false)

const init = () => {
  userDrawer.value = true
  clearFilter(1)
}

const clearFilter = (id: string | number) => {
  console.log(id)
}
const filterChange = (val, field, ope) => {
  let exits = false
  state.conditions.forEach(condition => {
    if (condition.field === field) {
      exits = true
      condition['val'] = val
    }
  })
  if (!exits) {
    state.conditions.push({ field, val, ope })
  }
}
const cancel = () => {
  userDrawer.value = false
}
const emits = defineEmits(['trigger-filter'])
const trigger = () => {
  console.log('')
  emits('trigger-filter', state.conditions)
}
defineExpose({
  init,
  clearFilter
})
</script>

<template>
  <el-drawer
    :title="t('common.filter_condition')"
    v-model="userDrawer"
    size="680px"
    direction="rtl"
  >
    <div v-for="(component, index) in componentList" :key="index">
      <drawer-filter
        v-if="component.type === 'select'"
        :option-list="component.option"
        :title="component.title"
        @filter-change="filterChange"
      />
      <drawer-enum-filter
        v-if="component.type === 'enum'"
        :option-list="component.option"
        :title="component.title"
        @filter-change="v => filterChange(v, component.field, 'in')"
      />
      <drawer-time-filter
        v-if="component.type === 'time'"
        :title="component.title"
        @filter-change="filterChange"
      />
    </div>

    <template #footer>
      <el-button @click="cancel">{{ t('common.cancel') }}</el-button>
      <el-button @click="trigger" type="primary">{{ t('common.sure') }}</el-button>
    </template>
  </el-drawer>
</template>

<style lang="less" scoped></style>
