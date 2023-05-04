<script setup lang="ts">
import CommonAttr from '@/custom-component/common/CommonAttr.vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import UserViewEditor from '@/views/chart/components/editor/index.vue'

const dvMainStore = dvMainStoreWithOut()
const { curComponent } = storeToRefs(dvMainStore)
</script>

<template>
  <div class="attr-list">
    <CommonAttr></CommonAttr>
    <!--    <user-view-editor></user-view-editor>-->
  </div>
</template>
