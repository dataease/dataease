<script setup lang="ts">
import CommonAttr from '@/custom-component/common/CommonAttr.vue'
import { dvMainStoreWithOut } from '@/store/modules/dataVisualization/dvMain'
import { storeToRefs } from 'pinia'

const { curComponent } = storeToRefs(dvMainStoreWithOut)
</script>

<template>
  <div class="attr-list">
    <CommonAttr></CommonAttr>
    <el-form>
      <el-form-item label="内容">
        <el-input v-model="curComponent.propValue" type="textarea" :rows="3" />
      </el-form-item>
    </el-form>
  </div>
</template>
