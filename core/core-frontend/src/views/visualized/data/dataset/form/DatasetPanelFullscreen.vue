<script setup lang="ts">
import { Icon } from '@/components/icon-custom'
import icon_magnify_outlined from '@/assets/svg/icon_magnify_outlined.svg'
import icon_minify_outlined from '@/assets/svg/icon_minify_outlined.svg'
import { useI18n } from '@/hooks/web/useI18n'

defineProps<{ fullscreen: boolean; label: string }>()
const emit = defineEmits<{ toggle: [] }>()
const { t } = useI18n()
</script>

<template>
  <div class="panel-fullscreen">
    <el-tooltip
      :content="fullscreen ? t('visualization.ext_fullscreen') : label"
      placement="bottom"
    >
      <button
        type="button"
        class="hover-icon"
        :aria-pressed="fullscreen"
        :aria-label="fullscreen ? t('visualization.ext_fullscreen') : label"
        @click="emit('toggle')"
      >
        <el-icon
          ><Icon
            ><component
              class="svg-icon"
              :is="fullscreen ? icon_minify_outlined : icon_magnify_outlined" /></Icon
        ></el-icon>
      </button>
    </el-tooltip>
  </div>
</template>

<style scoped lang="less">
.panel-fullscreen {
  display: flex;
}
button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 0;
  background: transparent;
}
</style>
