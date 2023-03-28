<script lang="ts" setup>
import { ref } from 'vue'
import ComponentTest from './ComponentTest.vue'
import { ElButton, ElDialog } from 'element-plus-secondary'

const dialogVisible = ref(true)
</script>

<template>
  <el-dialog v-model="dialogVisible" title="Tips" width="30%">
    <component-test></component-test>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="dialogVisible = false"> Confirm </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<style lang="less" scoped></style>
