<script lang="ts" setup>
import { toRefs } from 'vue'
import { propTypes } from '@/utils/propTypes'
const props = defineProps({
  tabList: propTypes.arrayOf(
    propTypes.shape({
      label: String,
      value: String
    })
  ),
  activeTab: propTypes.string.def('')
})

const emits = defineEmits(['TabClick'])
const { activeTab } = toRefs(props)
const handleTabClick = tab => {
  activeTab.value = tab.value
  emits('TabClick', tab)
}
</script>

<template>
  <div class="sheet-tabs">
    <div
      v-for="tab in tabList"
      :key="tab.label"
      :class="[{ active: activeTab === tab.value }, 'sheet-tab']"
      @click="handleTabClick(tab)"
    >
      {{ tab.label }}
    </div>
    <div class="tab-btn">
      <el-icon>
        <Icon name="icon_expand-left_filled"></Icon>
      </el-icon>
      <el-icon>
        <Icon name="icon_expand-right_filled"></Icon>
      </el-icon>
    </div>
  </div>
</template>

<style lang="less" scoped>
.sheet-tabs {
  border-top-left-radius: 3px;
  overflow-x: auto;
  display: flex;

  .tab-btn {
    padding: 8px 12px;
    display: flex;
    justify-content: space-between;
    width: 60px;
    height: 28px;
    position: sticky;
    right: 0;
    background: #fff;

    .ed-icon {
      color: #8d9199;
      cursor: pointer;

      &:hover {
        color: #3370ff;
      }

      & + .ed-icon {
        margin-left: 12px;
      }
    }
  }

  .sheet-tab {
    color: #1f2329;
    cursor: pointer;
    position: relative;
    padding: 0 20px;
    display: flex;
    align-items: center;
    height: 36px;
    border-bottom: 1px solid rgba(31, 35, 41, 0.15);
    &:hover {
      color: #3370ff;
    }

    &::after,
    &::before {
      content: '';
      position: absolute;
      height: 24px;
      width: 1px;
      top: 50%;
      transform: translateY(-50%);
      background: rgba(31, 35, 41, 0.15);
    }

    &::after {
      right: -0.5px;
    }
    &::before {
      left: -0.5px;
    }

    & + .active {
      &::after {
        display: none;
      }
    }
  }
  .active {
    box-shadow: 0px -1px 0px 0px #f5f6f7 inset;
    color: #3370ff;
    border: 1px solid rgba(31, 35, 41, 0.15);
    border-bottom: none;
    border-top-left-radius: 4px;
    border-top-right-radius: 4px;
    background: #f5f6f7;

    &::before,
    &::after {
      display: none;
    }

    & + .sheet-tab {
      &::before {
        display: none;
      }
    }
  }
}
</style>
