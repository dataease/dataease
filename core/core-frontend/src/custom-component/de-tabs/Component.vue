<template>
  <de-full-tabs
    v-model="editableTabsValue"
    addable
    addType="dropdown"
    :dropdownMenus="menus"
    @command="handleCommand"
    @tab-remove="removeTab"
  >
    <el-tab-pane
      :key="item.name"
      v-for="item in editableTabs"
      :label="item.title"
      :name="item.name"
      closable
    >
      {{ item.content }}
    </el-tab-pane>
  </de-full-tabs>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import DeFullTabs from '@/custom-component/de-tabs/DeFullTabs.vue'
const menus = [
  {
    command: 'debug',
    label: '快捷调试'
  },
  {
    command: 'ADD',
    label: '创建接口'
  },
  {
    command: 'CLOSE_ALL',
    label: '关闭所有标签'
  }
]
const editableTabsValue = ref('1')

const editableTabs = ref([
  {
    name: '1',
    title: 'Tab 1',
    content: 'Tab 1 content'
  }
]) as any

function handleCommand(name: string, obj: any) {
  if (obj.command === 'CLOSE_ALL') {
    editableTabs.value = []
    editableTabsValue.value = ''
  } else {
    editableTabs.value.push({
      name: name,
      title: obj.label,
      content: `${obj.label} content`,
      closable: true
    })
    editableTabsValue.value = name
  }
}
function removeTab(targetName: string) {
  let tabs = editableTabs.value
  let activeName = editableTabsValue.value
  if (activeName === targetName) {
    tabs.forEach((tab: any, index: number) => {
      if (tab.name === targetName) {
        let nextTab = tabs[index + 1] || tabs[index - 1]
        if (nextTab) {
          activeName = nextTab.name
        }
      }
    })
  }

  editableTabsValue.value = activeName
  editableTabs.value = tabs.filter((tab: any) => tab.name !== targetName)
}
</script>
<style lang="scss" scoped></style>
