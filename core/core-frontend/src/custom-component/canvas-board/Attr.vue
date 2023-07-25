<script setup lang="ts">
import CommonAttr from '@/custom-component/common/CommonAttr.vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import {toRefs} from "vue";
const dvMainStore = dvMainStoreWithOut()
const { curComponent } = storeToRefs(dvMainStore)
const props = defineProps({
  themes: {
    type: String,
    default: 'dark'
  }
})

const { themes } = toRefs(props)
</script>

<template>
  <div class="attr-list de-collapse-style">
    <CommonAttr :themes="themes"></CommonAttr>
  </div>
</template>

<style lang="less">


</style>
