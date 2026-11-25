<script setup lang="ts">
import { computed, ref } from 'vue'
import { authenticatedImageUrl } from '@/utils/resourceImages'

const props = defineProps({
  modelValue: Boolean,
  imageUrl: String,
  fitToWindow: Boolean
})
const emits = defineEmits(['update:modelValue'])

const scale = ref(1)
const imgStyle = computed(() =>
  props.fitToWindow ? undefined : { transform: `scale(${scale.value})` }
)

const handleWheel = (event: WheelEvent) => {
  if (props.fitToWindow || !event.deltaY) return
  event.preventDefault()
  const ratio = event.deltaY < 0 ? 1.2 : 1 / 1.2
  scale.value = Math.min(7, Math.max(0.2, Number((scale.value * ratio).toFixed(3))))
}
</script>

<template>
  <el-dialog
    :class="fitToWindow ? undefined : 'img-enlarge-dialog'"
    :width="fitToWindow ? 'min(960px, calc(100% - 32px))' : undefined"
    :align-center="fitToWindow"
    append-to-body
    destroy-on-close
    close-on-click-modal
    close-on-press-escape
    :model-value="modelValue"
    @update:model-value="emits('update:modelValue', $event)"
    @closed="scale = 1"
  >
    <div class="img-content" :class="{ 'img-content--fit': fitToWindow }" @wheel="handleWheel">
      <img :style="imgStyle" :src="authenticatedImageUrl(imageUrl)" alt="Preview Image" />
    </div>
  </el-dialog>
</template>

<style lang="less" scoped>
.img-content {
  width: 100%;
  height: calc(100vh - 70px);
  overflow: hidden;
  padding: 60px 120px;
  img {
    width: 100%;
    height: 100%;
    object-fit: contain;
  }
}

.img-content--fit {
  display: flex;
  align-items: center;
  justify-content: center;
  height: auto;
  padding: 0;

  img {
    display: block;
    width: auto;
    height: auto;
    max-width: 100%;
    max-height: min(720px, calc(100dvh - 112px));
  }
}
</style>
