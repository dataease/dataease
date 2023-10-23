<script setup lang="ts">
import { propTypes } from '@/utils/propTypes'
import { reactive } from 'vue'

const props = defineProps({
  optionList: propTypes.arrayOf(
    propTypes.shape({
      id: propTypes.string,
      name: propTypes.string
    })
  ),
  title: propTypes.string
})

const state = reactive({
  activeStatus: [],
  optionList: []
})

const nodeChange = (id: string | number) => {
  const len = state.activeStatus.indexOf(id)
  if (len >= 0) {
    state.activeStatus.splice(len, 1)
  } else {
    state.activeStatus.push(id)
  }
  emits('filter-change', state.activeStatus)
}
const clear = () => {
  state.activeStatus = []
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
        v-for="ele in props.optionList"
        :key="ele.id"
        class="item"
        :class="[state.activeStatus.includes(ele.id) ? 'active' : '']"
        @click="nodeChange(ele.id)"
        >{{ ele.name }}</span
      >
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
