<template>
  <div class="attr-list de-collapse-style">
    <CommonAttr :element="curComponent"></CommonAttr>
  </div>
</template>

<script setup lang="ts">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import CommonAttr from '@/custom-component/common/CommonAttr.vue'
import { storeToRefs } from 'pinia'
const dvMainStore = dvMainStoreWithOut()
const { curComponent } = storeToRefs(dvMainStore)
</script>
