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
    class="img-enlarge-dialog"
    append-to-body
    destroy-on-close
    close-on-click-modal
    v-model="value"
  >
    <div class="img-content">
      <img :src="imageUrl" alt="Preview Image" />
    </div>
  </el-dialog>
</template>

<style lang="less" scoped>
.img-content {
  width: 100vw;
  height: 100vh;
  overflow-y: auto;
  padding: 60px 120px;
  img {
    width: 100%;
    height: 100%;
    object-fit: contain;
  }
}
</style>
