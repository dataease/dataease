<script setup lang="ts">
import { propTypes } from '@/utils/propTypes'
import { ElSelect, ElOption } from 'element-plus-secondary'
import { computed, reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()
const props = defineProps({
  optionList: propTypes.arrayOf(
    propTypes.shape({
      id: propTypes.string,
      name: propTypes.string
    })
  ),
  title: propTypes.string,
  property: {
    type: Object,
    default: () => {
      placeholder: ''
    }
  }
})

const state = reactive({
  activeStatus: []
})
const emits = defineEmits(['filter-change'])

const selectStatus = ids => {
  emits(
    'filter-change',
    ids.map(item => item.id || item.value)
  )
}

const optionListNotSelect = computed(() => {
  return [...props.optionList]
})
const clear = () => {
  state.activeStatus = []
}
defineExpose({
  clear
})
</script>

<template>
  <div class="filter">
    <span>{{ title }}</span>
    <div class="filter-item">
      <el-select
        :teleported="false"
        style="width: 100%"
        v-model="state.activeStatus"
        value-key="id"
        filterable
        :placeholder="t('common.please_select') + props.property.placeholder"
        multiple
        @change="selectStatus"
      >
        <el-option
          v-for="item in optionListNotSelect"
          :key="item.name"
          :label="item.name"
          :value="item"
        />
      </el-select>
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
