<script lang="ts" setup>
import { toRefs, ref, watch, nextTick } from 'vue'
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

const activeTabIndex = ref(0)

const emits = defineEmits(['TabClick'])
const { activeTab } = toRefs(props)
const handleTabClick = tab => {
  let tabDom = document.getElementById(`tab-${tab.value}`)
  if (tabDom.offsetLeft + tabDom.offsetWidth > tabWrapper.value.offsetWidth) {
    tabWrapper.value.scrollLeft =
      tabDom.offsetLeft + tabDom.offsetWidth - tabWrapper.value.offsetWidth
  } else {
    tabWrapper.value.scrollLeft = 0
  }
  emits('TabClick', tab)
}
const tabWrapper = ref()
const showBtn = ref(false)
watch(
  () => activeTab.value,
  val => {
    activeTabIndex.value = props.tabList.findIndex(ele => ele.value === val)
  },
  { immediate: true }
)

watch(
  () => props.tabList,
  () => {
    nextTick(() => {
      showBtn.value = tabWrapper.value.scrollWidth > tabWrapper.value.offsetWidth
    })
  },
  { immediate: true }
)

const prevClick = () => {
  if (activeTabIndex.value === 0 || props.tabList.length === 1) return
  handleTabClick(props.tabList[activeTabIndex.value - 1])
}

const nextClick = () => {
  if (activeTabIndex.value === props.tabList.length - 1 || props.tabList.length === 1) return
  handleTabClick(props.tabList[activeTabIndex.value + 1])
}
</script>

<template>
  <div class="sheet-tabs">
    <div ref="tabWrapper" class="tab-wrapper">
      <div
        v-for="tab in tabList"
        :key="tab.label"
        :id="`tab-${tab.value}`"
        :class="[{ active: activeTab === tab.value }, 'sheet-tab']"
        @click="handleTabClick(tab)"
      >
        {{ tab.label }}
      </div>
    </div>
    <div class="tab-btn" v-if="showBtn">
      <el-icon :class="(!activeTabIndex || tabList.length === 1) && 'disabled'" @click="prevClick">
        <Icon name="icon_expand-left_filled"></Icon>
      </el-icon>
      <el-icon
        :class="(tabList.length - 1 === activeTabIndex || tabList.length === 1) && 'disabled'"
        @click="nextClick"
      >
        <Icon name="icon_expand-right_filled"></Icon>
      </el-icon>
    </div>
  </div>
</template>

<style lang="less" scoped>
.sheet-tabs {
  border-top-left-radius: 3px;
  width: 100%;
  position: relative;
  padding-right: 60px;

  .tab-wrapper {
    height: 100%;
    display: flex;
    overflow-x: auto;
    &::-webkit-scrollbar {
      display: none;
    }
  }

  .tab-btn {
    padding: 8px 12px;
    display: flex;
    justify-content: space-between;
    width: 60px;
    height: 28px;
    position: absolute;
    right: 0;
    top: 0;
    background: #fff;

    .ed-icon {
      color: #8d9199;
      cursor: pointer;

      &.disabled {
        cursor: not-allowed;
      }

      &:not(.disabled):hover {
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
