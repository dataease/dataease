<template>
  <div class="play-container">
    <el-tag closable>Tag 1</el-tag>
    <el-select-v2
      v-model="value"
      :options="options"
      placeholder="Please select"
      style="width: 240px"
      clearable
      multiple
    >
      <template #default="{ item }">
        <div class="hj">
          <span style="margin-right: 8px">{{ item.label }}</span>
        </div>
        <div class="hj">
          <span style="margin-right: 8px">{{ item.label }}</span>
        </div>
      </template>
    </el-select-v2>
    <div v-loading="true" />
  </div>
</template>

<script setup lang="ts">
import { ElTag, ElSelectV2 } from 'element-plus-secondary'
import { ref } from 'vue'
const initials = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j']

const value = ref([])
const options = ref(
  Array.from({ length: 1000 }).map((_, idx) => ({
    value: `Option ${idx + 1}`,
    label: `${initials[idx % 10]}${idx}`
  }))
)
</script>
