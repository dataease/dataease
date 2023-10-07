<script setup lang="ts">
import { toRefs } from 'vue'

const props = defineProps({
  value: String,
  imageUrl: String
})

const { value, imageUrl } = toRefs(props)
</script>

<template>
  <el-dialog
    top="10vh"
    class="img-enlarge-dialog"
    append-to-body="true"
    destroy-on-close="true"
    close-on-click-modal="true"
    v-model="value"
  >
    <div class="img-content">
      <img :src="imageUrl" alt="Preview Image" />
    </div>
  </el-dialog>
</template>

<style lang="less" scoped>
.img-content {
  width: 100%;
  height: 100%;
  overflow-y: auto;
  padding: 24px 24px 20px 24px;
  img {
    width: 100%;
    height: 100%;
    object-fit: contain;
  }
}
</style>
