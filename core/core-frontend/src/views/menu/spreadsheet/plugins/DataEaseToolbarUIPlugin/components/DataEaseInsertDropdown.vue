<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

type InsertDropdownItem = {
  key: string
  label: string
  commandId?: string
  divided?: boolean
}

type InsertDropdownViewItem = InsertDropdownItem & {
  component: 'button' | 'div'
  paths: string[]
}

const ICON_PATHS: Record<string, string[]> = {
  'pivot-table': [
    'M1 1H6V6H1V1ZM2.5 2.5V4.5H4.5V2.5H2.5ZM7 1H15V6H7V1ZM8.5 2.5V4.5H13.5V2.5H8.5ZM1 7H6V15H1V7ZM2.5 8.5V13.5H4.5V8.5H2.5ZM7 7H9V9H7V7ZM10 7H12V9H10V7ZM13 7H15V9H13V7ZM7 10H9V12H7V10ZM10 10H12V12H10V10ZM13 10H15V12H13V10ZM7 13H9V15H7V13ZM10 13H12V15H10V13ZM13 13H15V15H13V13Z'
  ],
  'summary-table': [
    'M1.33337 2.66659C1.33337 1.93021 1.93033 1.33325 2.66671 1.33325H13.3334C14.0698 1.33325 14.6667 1.93021 14.6667 2.66659V13.3333C14.6667 14.0696 14.0698 14.6666 13.3334 14.6666H2.66671C1.93033 14.6666 1.33337 14.0696 1.33337 13.3333V2.66659ZM5.33337 5.33325V2.66659H2.66671V5.33325V6.66658V13.3333H5.33337V6.66658V5.33325ZM13.3334 2.66659H6.66671V5.33325H13.3334V2.66659ZM6.66671 6.66658V13.3333H13.3334V6.66658H6.66671Z'
  ],
  'detail-table': [
    'M1.68836 1.68824C1.91565 1.46094 2.22393 1.33325 2.54537 1.33325H13.4547C13.7761 1.33325 14.0844 1.46094 14.3117 1.68824C14.539 1.91553 14.6667 2.22381 14.6667 2.54525V13.4546C14.6667 13.776 14.539 14.0843 14.3117 14.3116C14.0844 14.5389 13.7761 14.6666 13.4547 14.6666H2.54537C1.87604 14.6666 1.33337 14.1239 1.33337 13.4546V2.54525C1.33337 2.22381 1.46107 1.91553 1.68836 1.68824ZM2.66671 6.66658V9.33325H5.33337V6.66658H2.66671ZM2.66671 10.6666V13.3333H5.33337V10.6666H2.66671ZM6.66671 13.3333H9.33337V10.6666H6.66671V13.3333ZM10.6667 13.3333H13.3334V10.6666H10.6667V13.3333ZM13.3334 9.33325V6.66658H10.6667V9.33325H13.3334ZM9.33337 6.66658H6.66671V9.33325H9.33337V6.66658ZM13.3334 2.66659H2.66671V5.33325H13.3334V2.66659Z'
  ],
  link: [
    'M8.3507 5.86762C8.6359 5.98709 8.90912 6.14857 9.16155 6.35298C10.5922 7.51152 10.8128 9.61051 9.6543 11.0412L7.76634 13.3726C6.60779 14.8033 4.5088 15.0239 3.07812 13.8654C1.64743 12.7068 1.42682 10.6079 2.58536 9.17717L4.39107 6.94731L4.44862 7.49485C4.48909 7.87994 4.62107 8.23326 4.82067 8.53548L3.62156 10.0163C2.92643 10.8747 3.0588 12.1341 3.91721 12.8292C4.77562 13.5243 6.03502 13.392 6.73014 12.5335L8.6181 10.2021C9.31323 9.34369 9.18087 8.0843 8.32245 7.38917C8.05978 7.17646 7.75956 7.04124 7.44944 6.98059L8.3507 5.86762ZM7.34555 10.0708C7.06036 9.95133 6.78713 9.78986 6.53471 9.58545C5.10403 8.4269 4.88341 6.32791 6.04196 4.89723L7.92992 2.56579C9.08847 1.1351 11.1875 0.914488 12.6181 2.07304C14.0488 3.23158 14.2694 5.33057 13.1109 6.76126L11.3052 8.99112L11.2476 8.44358C11.2072 8.05849 11.0752 7.70516 10.8756 7.40294L12.0747 5.92216C12.7698 5.06375 12.6375 3.80436 11.779 3.10923C10.9206 2.4141 9.66124 2.54647 8.96612 3.40488L7.07816 5.73632C6.38303 6.59473 6.51539 7.85412 7.37381 8.54925C7.63648 8.76196 7.9367 8.89719 8.24682 8.95784L7.34555 10.0708Z'
  ],
  note: [
    'M2 14.6187C2 15.0134 2.31818 15.3334 2.71068 15.3334H7.99679V13.9932H3.33253V2.00007L12.6602 2.00007V8.66675H13.9928V1.37466C13.9928 0.979917 13.6746 0.659912 13.2821 0.659912H2.71068C2.31818 0.659912 2 0.979917 2 1.37466V14.6187Z',
    'M4.67068 7.5121C4.67068 7.41342 4.75023 7.33342 4.84835 7.33342H11.1557C11.2538 7.33342 11.3333 7.41342 11.3333 7.5121V8.49489C11.3333 8.59358 11.2538 8.67358 11.1557 8.67358H4.84835C4.75023 8.67358 4.67068 8.59358 4.67068 8.49489V7.5121Z',
    'M4.84996 4.66674C4.75183 4.66674 4.67229 4.74674 4.67229 4.84543V5.82821C4.67229 5.9269 4.75183 6.0069 4.84996 6.0069H7.82233C7.92045 6.0069 8 5.9269 8 5.82821V4.84543C8 4.74674 7.92045 4.66674 7.82233 4.66674H4.84996Z',
    'M11.3333 10.3334C11.3333 10.1493 11.4826 10.0001 11.6667 10.0001H12.3333C12.5174 10.0001 12.6667 10.1493 12.6667 10.3334V12.0001H14.3333C14.5174 12.0001 14.6667 12.1493 14.6667 12.3334V13.0001C14.6667 13.1842 14.5174 13.3334 14.3333 13.3334H12.6667V15.0001C12.6667 15.1842 12.5174 15.3334 12.3333 15.3334H11.6667C11.4826 15.3334 11.3333 15.1842 11.3333 15.0001V13.3334H9.66667C9.48257 13.3334 9.33333 13.1842 9.33333 13.0001V12.3334C9.33333 12.1493 9.48257 12.0001 9.66667 12.0001H11.3333V10.3334Z'
  ]
}

const props = withDefaults(
  defineProps<{
    items?: InsertDropdownItem[]
    onChange?: (value: string) => void
  }>(),
  {
    items: () => []
  }
)

const viewItems = computed<InsertDropdownViewItem[]>(() =>
  props.items.map(item => ({
    ...item,
    component: item.divided ? 'div' : 'button',
    paths: ICON_PATHS[item.key] || []
  }))
)

const dropdownRef = ref<HTMLElement>()
const adapterClassName = 'dataease-insert-dropdown-adapter'

onMounted(() => {
  dropdownRef.value?.parentElement?.classList.add(adapterClassName)
})

onBeforeUnmount(() => {
  dropdownRef.value?.parentElement?.classList.remove(adapterClassName)
})

const handleClick = (item: InsertDropdownViewItem) => {
  if (!item.commandId) {
    return
  }

  props.onChange?.(item.commandId)
}
</script>

<template>
  <div ref="dropdownRef" class="dataease-insert-dropdown" @click.stop>
    <component
      :is="item.component"
      v-for="item in viewItems"
      :key="item.key"
      :type="item.divided ? undefined : 'button'"
      class="dataease-insert-dropdown__row"
      :class="{
        'dataease-insert-dropdown__row--item': !item.divided,
        'dataease-insert-dropdown__row--divider': item.divided
      }"
      :role="item.divided ? 'separator' : undefined"
      @click="handleClick(item)"
    >
      <span class="dataease-insert-dropdown__icon" :class="`dataease-insert-dropdown__icon--${item.key}`">
        <svg viewBox="0 0 16 16" aria-hidden="true">
          <path v-for="path in item.paths" :key="path" fill-rule="evenodd" clip-rule="evenodd" :d="path" />
        </svg>
      </span>
      <span class="dataease-insert-dropdown__label">{{ item.label }}</span>
    </component>
  </div>
</template>

<style scoped lang="less">
.dataease-insert-dropdown {
  box-sizing: border-box;
  width: 100%;
  height: 100%;
  color: #1f2329;
  font-family: var(--de-custom_font, 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif);
  overflow: hidden;
}

:global(.dataease-insert-dropdown-adapter) {
  box-sizing: border-box !important;
  width: 120px !important;
  min-width: 120px !important;
  max-width: 120px !important;
  height: 145px !important;
  padding: 4px !important;
  border: 0 !important;
  border-radius: 6px !important;
  background: #ffffff !important;
  box-shadow:
    0 0 0 1px #dee0e3,
    0 4px 8px rgba(31, 35, 41, 0.1) !important;
  overflow: hidden !important;
}

.dataease-insert-dropdown__row {
  box-sizing: border-box;
  width: 100%;
}

.dataease-insert-dropdown__row--item {
  height: 32px;
  padding: 5px 8px;
  border: 0;
  border-radius: 4px;
  background: #ffffff;
  color: inherit;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font: inherit;
  text-align: left;

  &:hover {
    background: rgba(31, 35, 41, 0.1);
  }
}

.dataease-insert-dropdown__row--divider {
  height: 9px;
  position: relative;
}

.dataease-insert-dropdown__icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
  color: #1f2329;
  display: inline-flex;
  align-items: center;
  justify-content: center;

  svg {
    display: block;
    width: 16px;
    height: 16px;
    fill: currentColor;
  }
}

.dataease-insert-dropdown__label {
  min-width: 0;
  height: 22px;
  color: #1f2329;
  display: inline-flex;
  align-items: center;
  font-size: 14px;
  line-height: 22px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dataease-insert-dropdown__row--divider {
  &::before {
    content: '';
    position: absolute;
    left: -3px;
    right: -3px;
    top: 3px;
    height: 1px;
    background: rgba(31, 35, 41, 0.15);
  }

  .dataease-insert-dropdown__icon,
  .dataease-insert-dropdown__label {
    display: none;
  }
}
</style>
