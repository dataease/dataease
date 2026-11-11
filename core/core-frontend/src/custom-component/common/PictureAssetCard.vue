<script setup lang="ts">
import { Delete, ZoomIn } from '@element-plus/icons-vue'
import { useI18n } from '@/hooks/web/useI18n'
import { authenticatedImageUrl } from '@/utils/resourceImages'

defineProps<{ url: string; name?: string; order?: number; disabled?: boolean }>()
defineEmits<{ (event: 'preview'): void; (event: 'remove'): void }>()
const { t } = useI18n()
</script>

<template>
  <div class="picture-asset-card" :title="name">
    <img :src="authenticatedImageUrl(url)" alt="" draggable="false" />
    <span v-if="order" class="picture-order">{{ order }}</span>
    <div class="picture-actions">
      <button
        type="button"
        :aria-label="[t('material.preview'), order].filter(Boolean).join(' ')"
        @click="$emit('preview')"
      >
        <el-icon><ZoomIn /></el-icon>
      </button>
      <button
        type="button"
        :aria-label="[t('commons.remove'), order].filter(Boolean).join(' ')"
        :disabled="disabled"
        @click="$emit('remove')"
      >
        <el-icon><Delete /></el-icon>
      </button>
    </div>
  </div>
</template>

<style scoped lang="less">
.picture-asset-card {
  position: relative;
  width: 80px;
  height: 80px;
  overflow: hidden;
  border: 1px solid var(--ed-border-color);
  border-radius: 6px;

  img {
    width: 100%;
    height: 100%;
    object-fit: contain;
  }

  .picture-order {
    position: absolute;
    top: 4px;
    left: 4px;
    min-width: 16px;
    line-height: 16px;
    color: #fff;
    background: #1f2329cc;
    border-radius: 3px;
    text-align: center;
    font-size: 11px;
  }

  .picture-actions {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    opacity: 0;
    background: #0008;

    button {
      display: grid;
      place-items: center;
      width: 24px;
      height: 24px;
      padding: 0;
      color: #fff;
      border: 0;
      border-radius: 4px;
      background: transparent;
      cursor: pointer;

      &:hover,
      &:focus-visible {
        background: #fff3;
      }

      &:disabled {
        opacity: 0.5;
        cursor: default;
      }
    }
  }

  &:hover .picture-actions,
  &:focus-within .picture-actions {
    opacity: 1;
  }
}
</style>
