<script setup lang="ts">
import { computed, ref, watch } from 'vue'

type FunctionItem = {
  name: string
  desc?: string
}

type FunctionCategory = {
  key: string
  label: string
  functions: FunctionItem[]
}

const props = withDefaults(
  defineProps<{
    categories?: FunctionCategory[]
    onChange?: (value: string) => void
  }>(),
  {
    categories: () => []
  }
)

const activeCategoryKey = ref(props.categories[0]?.key || '')

const activeCategory = computed(() =>
  props.categories.find(category => category.key === activeCategoryKey.value) || props.categories[0]
)

watch(
  () => props.categories,
  categories => {
    if (!categories.some(category => category.key === activeCategoryKey.value)) {
      activeCategoryKey.value = categories[0]?.key || ''
    }
  },
  { deep: true }
)

const handleFunctionClick = (item: FunctionItem) => {
  props.onChange?.(item.name)
}
</script>

<template>
  <div class="dataease-function-dropdown" @click.stop>
    <div v-if="categories.length" class="dataease-function-dropdown__content">
      <el-scrollbar max-height="280px" class="dataease-function-dropdown__categories">
        <button
          v-for="category in categories"
          :key="category.key"
          type="button"
          class="dataease-function-dropdown__category"
          :class="{
            'dataease-function-dropdown__category--active': category.key === activeCategory?.key
          }"
          @mouseenter="activeCategoryKey = category.key"
          @click="activeCategoryKey = category.key"
        >
          <span class="dataease-function-dropdown__category-name">{{ category.label }}</span>
          <span class="dataease-function-dropdown__category-count">
            {{ category.functions.length }}
          </span>
        </button>
      </el-scrollbar>

      <el-scrollbar max-height="280px" class="dataease-function-dropdown__functions">
        <button
          v-for="item in activeCategory?.functions"
          :key="item.name"
          type="button"
          class="dataease-function-dropdown__function"
          @click="handleFunctionClick(item)"
        >
          <span class="dataease-function-dropdown__function-name">{{ item.name }}</span>
          <span v-if="item.desc" class="dataease-function-dropdown__function-desc">
            {{ item.desc }}
          </span>
        </button>
      </el-scrollbar>
    </div>

    <div v-else class="dataease-function-dropdown__empty">暂无可用函数</div>
  </div>
</template>

<style scoped lang="less">
.dataease-function-dropdown {
  box-sizing: border-box;
  width: 420px;
  max-width: calc(100vw - 32px);
  padding: 6px;
  color: #1f2329;
  font-family: var(--de-custom_font, 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif);
}

.dataease-function-dropdown__content {
  display: grid;
  grid-template-columns: 136px minmax(0, 1fr);
  gap: 6px;
}

.dataease-function-dropdown__categories {
  padding-right: 4px;
  border-right: 1px solid rgba(31, 35, 41, 0.12);
}

.dataease-function-dropdown__category,
.dataease-function-dropdown__function {
  box-sizing: border-box;
  width: 100%;
  border: 0;
  background: transparent;
  color: inherit;
  cursor: pointer;
  font: inherit;
  text-align: left;
}

.dataease-function-dropdown__category {
  height: 28px;
  padding: 0 8px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  font-size: 12px;
  line-height: 20px;

  &:hover,
  &--active {
    background: rgba(20, 86, 240, 0.08);
    color: #1456f0;
  }
}

.dataease-function-dropdown__category-name {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dataease-function-dropdown__category-count {
  flex-shrink: 0;
  color: #8f959e;
  font-size: 11px;
}

.dataease-function-dropdown__functions {
  min-width: 0;
}

.dataease-function-dropdown__function {
  min-height: 36px;
  padding: 6px 8px;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 2px;

  &:hover {
    background: rgba(31, 35, 41, 0.08);
  }
}

.dataease-function-dropdown__function-name {
  color: #1f2329;
  font-size: 12px;
  font-weight: 500;
  line-height: 18px;
}

.dataease-function-dropdown__function-desc {
  color: #646a73;
  font-size: 11px;
  line-height: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dataease-function-dropdown__empty {
  min-width: 180px;
  padding: 12px;
  color: #8f959e;
  font-size: 12px;
  text-align: center;
}
</style>
