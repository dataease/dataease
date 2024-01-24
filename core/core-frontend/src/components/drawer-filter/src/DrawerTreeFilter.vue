<script setup lang="ts">
import { propTypes } from '@/utils/propTypes'
import { ElTreeSelect } from 'element-plus-secondary'
import { computed, reactive, ref, PropType, toRefs } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()
interface TreeConfig {
  checkStrictly: boolean
  showCheckbox: boolean
  checkOnClickNode: boolean
  placeholder: string
}
const props = defineProps({
  optionList: propTypes.arrayOf(
    propTypes.shape({
      value: propTypes.string,
      label: propTypes.string,
      children: Array,
      disabled: Boolean
    })
  ),
  title: propTypes.string,
  property: Object as PropType<TreeConfig>
})

const { property } = toRefs(props)
const treeConfig = computed(() => {
  let obj = Object.assign(
    {
      checkStrictly: false,
      showCheckbox: true,
      checkOnClickNode: true,
      placeholder: t('user.role')
    },
    property.value
  )
  return obj
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
        :placeholder="$t('common.please_select') + treeConfig.placeholder"
        :show-checkbox="treeConfig.showCheckbox"
        :check-strictly="treeConfig.checkStrictly"
        :check-on-click-node="treeConfig.checkOnClickNode"
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
