<script setup lang="ts">
import { nextTick, ref, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import {
  MAX_MAP_RANGES,
  insertMapBoundary,
  removeMapBoundary,
  validMapBoundaries
} from './mapRange'

const props = defineProps<{ modelValue: number[]; themes: EditorTheme }>()
const emit = defineEmits<{ (event: 'update:modelValue', value: number[]): void }>()
const { t } = useI18n()
const draft = ref<(number | undefined)[]>([...props.modelValue])
const host = ref<HTMLElement>()
const error = ref('')
watch(
  () => props.modelValue,
  value => {
    if (JSON.stringify(value) !== JSON.stringify(draft.value)) {
      draft.value = [...value]
      error.value = ''
    }
  },
  { deep: true }
)
const save = () => {
  if (!validMapBoundaries(draft.value)) {
    error.value = t('chart.map_boundary_invalid')
    return
  }
  error.value = ''
  emit('update:modelValue', [...draft.value])
}
const insert = async (index: number) => {
  const next = insertMapBoundary(draft.value as number[], index)
  if (!next) {
    error.value = t('chart.map_boundary_cannot_insert')
    return
  }
  draft.value = next
  save()
  await nextTick()
  const input = host.value?.querySelectorAll<HTMLInputElement>('.map-boundary-row input')[index + 1]
  input?.focus()
  input?.select()
}
const remove = (index: number) => {
  const next = removeMapBoundary(draft.value as number[], index)
  if (!next) return
  draft.value = next
  save()
}
</script>

<template>
  <div ref="host" class="map-boundaries">
    <div class="map-range-count">
      <span>{{ t('chart.legend_num') }}</span>
      <span>{{ draft.length - 1 }}</span>
      <span class="map-range-hint">{{ t('chart.map_range_count_auto') }}</span>
    </div>
    <template v-for="(_, index) in draft" :key="index">
      <div class="map-boundary-row">
        <label :for="`map-boundary-${index}`">{{
          index === 0
            ? t('chart.min')
            : index === draft.length - 1
            ? t('chart.max')
            : t('chart.map_boundary')
        }}</label>
        <el-input-number
          :id="`map-boundary-${index}`"
          v-model="draft[index]"
          :effect="themes"
          controls-position="right"
          @change="save"
        />
        <el-button
          v-if="index > 0 && index < draft.length - 1"
          text
          class="map-boundary-delete"
          :title="t('chart.map_boundary_delete')"
          :aria-label="t('chart.map_boundary_delete')"
          @click="remove(index)"
        >
          <el-icon
            ><Icon><icon_deleteTrash_outlined class="svg-icon" /></Icon
          ></el-icon>
        </el-button>
        <span v-else class="map-boundary-delete" />
      </div>
      <el-button
        v-if="index < draft.length - 1"
        class="map-boundary-add"
        text
        type="primary"
        :disabled="draft.length - 1 >= MAX_MAP_RANGES"
        @click="insert(index)"
        >+ {{ t('chart.map_boundary_add') }}</el-button
      >
    </template>
    <div v-if="error" class="map-range-error" role="alert">{{ error }}</div>
    <div class="map-range-hint">{{ t('chart.map_boundary_hint') }}</div>
  </div>
</template>

<style scoped lang="less">
.map-boundaries {
  width: 100%;
  margin-bottom: 16px;
}
.map-range-count {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 12px;
}
.map-boundary-row {
  display: flex;
  align-items: center;
  gap: 8px;
  label {
    flex: 0 0 48px;
  }
  :deep(.ed-input-number) {
    flex: 1;
    width: 0;
    min-width: 0;
  }
}
.map-boundary-delete {
  flex: 0 0 24px;
  width: 24px;
  padding: 0;
  color: inherit;
}
.map-boundary-add {
  display: flex;
  margin: 2px auto;
  height: 28px;
}
.map-range-hint {
  font-size: 12px;
  color: var(--ed-text-color-secondary, #8f959e);
  line-height: 18px;
}
.map-boundaries > .map-range-hint {
  margin-top: 8px;
}
.map-range-error {
  color: var(--ed-color-danger);
  font-size: 12px;
  margin-top: 8px;
}
</style>
