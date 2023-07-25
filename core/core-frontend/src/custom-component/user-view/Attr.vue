<script setup lang="ts">
import CommonAttr from '@/custom-component/common/CommonAttr.vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { toRefs } from 'vue'
const props = defineProps({
  themes: {
    type: String,
    default: 'dark'
  }
})

const { themes } = toRefs(props)
const dvMainStore = dvMainStoreWithOut()
const { curComponent } = storeToRefs(dvMainStore)
</script>

<template>
  <div class="attr-list">
    <CommonAttr :themes="themes"></CommonAttr>
    <!--    <user-view-editor></user-view-editor>-->
  </div>
</template>
