<script lang="ts" setup>
import { nextTick, ref, watch } from 'vue'
import { Icon } from '@/components/icon-custom'
import { ElButton, ElDivider, ElIcon } from 'element-plus-secondary'
import { propTypes } from '@/utils/propTypes'
const props = defineProps({
  filterTexts: propTypes.arrayOf(propTypes.string),
  total: propTypes.number.def(0)
})

const emits = defineEmits(['clearFilter'])
const container = ref(null)

const showScroll = ref(false)
const scrollPre = () => {
  container.value.scrollLeft -= 10
  if (container.value.scrollLeft <= 0) {
    container.value.scrollLeft = 0
  }
}
const scrollNext = () => {
  container.value.scrollLeft += 10
  const width = container.value.scrollWidth - container.value.offsetWidth
  if (container.value.scrollLeft > width) {
    container.value.scrollLeft = width
  }
}
const clearFilter = (index?: number) => {
  emits('clearFilter', index)
}

const clearFilterAll = () => {
  emits('clearFilter', 'empty')
}

watch(
  () => props.filterTexts,
  () => {
    nextTick(() => {
      showScroll.value = container.value?.scrollWidth > container.value?.offsetWidth
    })
  },
  { deep: true }
)
</script>

<template>
  <div v-if="filterTexts.length" class="filter-texts">
    <span class="sum">{{ total }}</span>
    <span class="title">个结果</span>
    <el-divider direction="vertical" />
    <el-icon @click="scrollPre" class="arrow-left arrow-filter" v-if="showScroll">
      <Icon name="icon_left_outlined"></Icon>
    </el-icon>
    <div class="filter-texts-container" ref="container">
      <p v-for="(ele, index) in filterTexts" :key="ele" class="text">
        <el-tooltip effect="dark" :content="ele" placement="top-start">
          {{ ele }}
        </el-tooltip>
        <el-icon @click="clearFilter(index)">
          <Icon name="icon_close_outlined"></Icon>
        </el-icon>
      </p>
      <el-button
        type="text"
        class="clear-btn clear-btn-inner"
        v-if="!showScroll"
        @click="clearFilterAll"
      >
        <template #icon>
          <Icon name="icon_delete-trash_outlined"></Icon>
        </template>
        清空条件</el-button
      >
    </div>
    <el-icon @click="scrollNext" class="arrow-right arrow-filter" v-if="showScroll">
      <Icon name="icon_right_outlined"></Icon>
    </el-icon>
    <el-button
      type="text"
      class="clear-btn"
      style="height: 24px; line-height: 24px"
      v-if="showScroll"
      @click="clearFilterAll"
    >
      <template #icon>
        <Icon name="icon_delete-trash_outlined"></Icon>
      </template>
      清空条件</el-button
    >
  </div>
</template>

<style lang="less" scoped>
.filter-texts {
  display: flex;
  align-items: center;
  margin: 17px 0;
  font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
  font-weight: 400;

  .sum {
    color: #1f2329;
  }

  .title {
    color: #999999;
    margin-left: 8px;
  }

  .text {
    max-width: 280px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    padding: 1px 22px 1px 6px;
    display: inline-block;
    align-items: center;
    color: #0c296e;
    font-size: 14px;
    line-height: 22px;
    background: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1));
    border-radius: 2px;
    margin: 0;
    margin-right: 8px;
    position: relative;

    i {
      position: absolute;
      right: 6px;
      top: 50%;
      font-size: 12px;
      transform: translateY(-50%);
      cursor: pointer;
    }
  }

  .clear-btn {
    color: #646a73;
  }

  .clear-btn:hover {
    color: var(--ed-color-primary);
  }

  .filter-texts-container::-webkit-scrollbar {
    display: none;
  }

  .arrow-filter {
    font-size: 16px;
    width: 24px;
    height: 24px;
    cursor: pointer;
    color: #646a73;
    display: flex;
    justify-content: center;
    align-items: center;
  }

  .arrow-filter:hover {
    background: rgba(31, 35, 41, 0.1);
    border-radius: 4px;
  }

  .ed-icon-arrow-right.arrow-filter {
    margin-left: 5px;
  }

  .ed-icon-arrow-left.arrow-filter {
    margin-right: 5px;
  }

  .filter-texts-container {
    flex: 1;
    overflow-x: auto;
    white-space: nowrap;
    height: 24px;

    .clear-btn-inner {
      margin-top: -16px;
    }
  }
}
</style>
