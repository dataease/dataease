<template>
  <el-dialog
    class="market-create-dialog"
    v-model="state.dialogShow"
    width="90vw"
    height="90vh"
    :before-close="close"
    @submit.prevent
  >
    <template-market ref="templateMarketCreateRef" @close="close"></template-market>
  </el-dialog>
</template>

<script setup lang="ts">
import TemplateMarket from '@/views/template-market/index.vue'
import { nextTick, reactive, ref } from 'vue'
const templateMarketCreateRef = ref(null)

const state = reactive({
  dialogShow: false
})
const close = () => {
  state.dialogShow = false
}
const optInit = param => {
  state.dialogShow = true
  nextTick(() => {
    templateMarketCreateRef.value.optInit(param)
  })
}

defineExpose({
  optInit
})
</script>

<style lang="less">
.market-create-dialog {
  .ed-dialog__body {
    padding: 0 !important;
  }
  .ed-dialog__header {
    display: none !important;
  }
}
</style>
