<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'

defineProps<{ message: string; diagnostic: string; saveFailed: boolean }>()
const { t } = useI18n()
const expanded = ref(false)
</script>

<template>
  <span class="connection-error-notice">
    <span class="connection-error-title">{{ message }}</span>
    <span v-if="saveFailed" class="connection-save-failed">
      {{ t('sync_datasource.changes_not_saved') }}
    </span>
    <el-button link type="primary" :aria-expanded="expanded" @click="expanded = !expanded">
      {{
        t(expanded ? 'sync_datasource.hide_error_details' : 'sync_datasource.show_error_details')
      }}
    </el-button>
    <span v-if="expanded" class="connection-error-details">{{ diagnostic }}</span>
  </span>
</template>

<style scoped lang="less">
.connection-error-notice {
  display: block;
  max-width: min(640px, calc(100vw - 100px));
}
.connection-error-title,
.connection-save-failed {
  display: block;
  margin-bottom: 8px;
}
.connection-error-details {
  display: block;
  margin-top: 12px;
  max-height: 240px;
  overflow: auto;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
  font-family: monospace;
  font-size: 12px;
  line-height: 20px;
}
</style>
