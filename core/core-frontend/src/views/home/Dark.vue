<script lang="ts" setup>
import { ref } from 'vue'
const input = ref('')
const num = ref(1)
const value = ref('')
const radio1 = ref('1')
const checked1 = ref(true)
const activeNames = ref('')
const options = [
  {
    value: 'Option1',
    label: 'Option1'
  },
  {
    value: 'Option2',
    label: 'Option2'
  },
  {
    value: 'Option3',
    label: 'Option3'
  },
  {
    value: 'Option4',
    label: 'Option4'
  },
  {
    value: 'Option5',
    label: 'Option5'
  }
]
</script>

<template>
  <div style="width: 150px; height: 600px; padding: 10px; background-color: #292929">
    <el-input effect="dark" v-model="input" placeholder="Please input"
      ><template #prepend>Http://</template></el-input
    >
    <div>----</div>
    <el-input-number effect="dark" v-model="num" :min="1" :max="10" />
    <div>----</div>
    <el-input-number v-model="num" effect="dark" :min="1" :max="10" controls-position="right" />
    <div>----</div>
    <el-select effect="dark" v-model="value" class="m-2" placeholder="Select">
      <template #prefix> 13 </template>
      <el-option
        v-for="item in options"
        :key="item.value"
        :label="item.label"
        :value="item.value"
      />
    </el-select>
    <div>----</div>
    <el-radio-group v-model="radio1" class="ml-4">
      <el-radio effect="dark" label="1" size="large">Option 1</el-radio>
      <el-radio effect="dark" label="2" size="large">Option 2</el-radio>
    </el-radio-group>
    <div>----</div>
    <el-checkbox effect="dark" v-model="checked1" label="Option 1" />
    <div>----</div>
    <el-collapse v-model="activeNames">
      <el-collapse-item effect="dark" title="一致性 " name="1">
        <div>与现实生活一致：与现实生活的流程、逻辑保持一致，遵循用户习惯的语言和概念；</div>
        <div>
          在界面中一致：所有的元素和结构需保持一致，比如：设计样式、图标和文本、元素的位置等。
        </div>
      </el-collapse-item>
      <el-collapse-item title="一致性1 " name="12">
        <div>与现实生活一致：与现实生活的流程、逻辑保持一致，遵循用户习惯的语言和概念；</div>
        <div>
          在界面中一致：所有的元素和结构需保持一致，比如：设计样式、图标和文本、元素的位置等。
        </div>
      </el-collapse-item>
    </el-collapse>
    <div>----</div>
    <el-dropdown effect="dark" trigger="click">
      <span style="color: #fff"> 13 </span>
      <template #dropdown>
        <el-dropdown-menu effect="dark">
          <el-dropdown-item>Action 1</el-dropdown-item>
          <el-dropdown-item>Action 2</el-dropdown-item>
          <el-dropdown-item>Action 3</el-dropdown-item>
          <el-dropdown-item disabled>Action 4</el-dropdown-item>
          <el-dropdown-item divided>Action 5</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<style lang="less" scoped></style>
