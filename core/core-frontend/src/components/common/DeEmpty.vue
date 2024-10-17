<script setup lang="ts">
import { useI18n } from '@/hooks/web/useI18n'
import EmptyBackground from '../empty-background/src/EmptyBackground.vue'

const { t } = useI18n()
</script>

<template>
  <el-row class="custom-position">
    <empty-background description="t('visualization.select_dimension_hint')" img-type="noneWhite" />
  </el-row>
</template>

<style scoped lang="less">
.custom-position {
  height: 100%;
  width: 100%;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  flex-flow: row nowrap;
  color: #646a73;
  font-weight: 400;
}
</style>
