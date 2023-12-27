<script setup lang="ts">
import { propTypes } from '@/utils/propTypes'
import { ElTreeSelect } from 'element-plus-secondary'
import { computed, reactive, ref } from 'vue'

const props = defineProps({
  optionList: propTypes.arrayOf(
    propTypes.shape({
      value: propTypes.string,
      label: propTypes.string,
      children: Array,
      disabled: Boolean
    })
  ),
  title: propTypes.string
})

const state = reactive({
  currentStatus: [],
  activeStatus: []
})

const emits = defineEmits(['filter-change'])
const filterTree = ref()
const treeChange = () => {
  const nodes = state.currentStatus.map(id => {
    return filterTree.value?.getNode(id).data
  })
  state.activeStatus = [...nodes]
  emits(
    'filter-change',
    state.activeStatus.map(item => item.value)
  )
}
const optionListNotSelect = computed(() => {
  return [...props.optionList]
})
const clear = () => {
  state.currentStatus = []
}
defineExpose({
  clear
})
</script>

<template>
  <div class="filter">
    <span>{{ title }}</span>
    <div class="filter-item">
      <el-tree-select
        node-key="value"
        ref="filterTree"
        :teleported="false"
        style="width: 100%"
        @change="treeChange"
        v-model="state.currentStatus"
        :data="optionListNotSelect"
        :highlight-current="true"
        multiple
        :render-after-expand="false"
        :placeholder="$t('common.please_select') + $t('user.role')"
        show-checkbox
        check-on-click-node
      />
    </div>
  </div>
</template>
<style lang="less" scope>
.filter {
  display: flex;
  min-height: 46px;

  > :nth-child(1) {
    color: var(--deTextSecondary, #1f2329);
    font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
    font-style: normal;
    font-weight: 400;
    font-size: 14px;
    line-height: 24px;
    white-space: nowrap;
    width: 116px;
  }

  .filter-item {
    flex: 1;
  }
}
</style>
