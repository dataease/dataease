<script setup lang="ts">
import { propTypes } from '@/utils/propTypes'
import { ElTreeSelect, ElPopover, ElIcon } from 'element-plus-secondary'
import { computed, reactive, ref, watch } from 'vue'
import { Icon } from '@/components/icon-custom'

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

const elPopoverU = ref(null)
const more = ref(null)
const filterTree = ref(null)
const statusChange = (value: string | number) => {
  state.activeStatus = state.activeStatus.filter(ele => ele?.value !== value)
  state.currentStatus = state.currentStatus.filter(val => val !== value)
  emits(
    'filter-change',
    state.activeStatus.map(item => item.value)
  )
}

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

watch(
  () => state.currentStatus,
  () => {
    treeChange()
  },
  { deep: true, immediate: true }
)

const optionListNotSelect = computed(() => {
  return [...props.optionList]
})
const clear = () => {
  state.activeStatus = []
  state.currentStatus = []
}
const emits = defineEmits(['filter-change'])
defineExpose({
  clear
})
</script>

<template>
  <div class="filter">
    <span>{{ title }}</span>
    <div class="filter-item">
      <span
        v-for="ele in state.activeStatus"
        :key="ele.value"
        class="item active"
        @click="statusChange(ele.value)"
        >{{ $t(ele.label) }}</span
      >
      <slot v-if="!!optionListNotSelect.length">
        <el-popover
          :show-arrow="false"
          ref="elPopoverU"
          placement="bottom"
          popper-class="filter-popper"
          width="200"
          trigger="click"
        >
          <el-tree-select
            ref="filterTree"
            node-key="value"
            :teleported="false"
            style="width: 100%"
            v-model="state.currentStatus"
            :data="optionListNotSelect"
            :highlight-current="true"
            multiple
            :render-after-expand="false"
            :placeholder="$t('common.please_select') + $t('user.role')"
            show-checkbox
            check-on-click-node
          />
          <template #reference>
            <span ref="more" class="more">
              <el-icon>
                <Icon name="icon_add_outlined"> </Icon>
              </el-icon>
              更多
            </span>
          </template>
        </el-popover>
      </slot>
    </div>
  </div>
</template>
<style lang="less" scope>
.filter {
  display: flex;
  min-height: 46px;

  > :nth-child(1) {
    color: var(--deTextSecondary, #1f2329);
    font-family: 'PingFang SC';
    font-style: normal;
    font-weight: 400;
    font-size: 14px;
    line-height: 24px;
    white-space: nowrap;
    width: 116px;
  }

  .filter-item {
    flex: 1;

    .item,
    .more {
      font-family: PingFang SC;
      white-space: nowrap;
      font-size: 14px;
      font-weight: 400;
      line-height: 24px;
      margin-right: 12px;
      text-align: center;
      padding: 1px 6px;
      background: var(--deTextPrimary5, #f5f6f7);
      color: var(--deTextPrimary, #1f2329);
      border-radius: 2px;
      cursor: pointer;
      display: inline-block;
      margin-bottom: 12px;
    }

    .active,
    .more:hover {
      background: var(--primary10, rgba(51, 112, 255, 0.1));
      color: var(--primaryselect, #0c296e);
    }

    .more {
      white-space: nowrap;
      display: inline-flex;
      align-items: center;
      i {
        margin-right: 5px;
      }
    }
  }
}
</style>
<style lang="less">
.filter-popper {
  padding: 0 !important;
  background: #fff !important;
}
</style>
