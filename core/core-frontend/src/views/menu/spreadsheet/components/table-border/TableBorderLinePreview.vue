<script setup lang="ts">
import { BorderStyleTypes } from '@univerjs/core'
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    lineStyle?: BorderStyleTypes
    width?: number
  }>(),
  {
    lineStyle: BorderStyleTypes.THIN,
    width: 72
  }
)

interface LineVisual {
  strokeWidth: number
  dasharray?: string
  double?: boolean
}

const lineVisuals: Partial<Record<BorderStyleTypes, LineVisual>> = {
  [BorderStyleTypes.THIN]: { strokeWidth: 1 },
  [BorderStyleTypes.HAIR]: { strokeWidth: 0.5 },
  [BorderStyleTypes.DOTTED]: { strokeWidth: 1, dasharray: '1 3' },
  [BorderStyleTypes.DASHED]: { strokeWidth: 1, dasharray: '8 4' },
  [BorderStyleTypes.DASH_DOT]: { strokeWidth: 1, dasharray: '8 4 1 4' },
  [BorderStyleTypes.DASH_DOT_DOT]: { strokeWidth: 1, dasharray: '8 4 1 4 1 4' },
  [BorderStyleTypes.MEDIUM]: { strokeWidth: 2 },
  [BorderStyleTypes.MEDIUM_DASHED]: { strokeWidth: 2, dasharray: '8 4' },
  [BorderStyleTypes.MEDIUM_DASH_DOT]: { strokeWidth: 2, dasharray: '8 4 1 4' },
  [BorderStyleTypes.MEDIUM_DASH_DOT_DOT]: {
    strokeWidth: 2,
    dasharray: '8 4 1 4 1 4'
  },
  [BorderStyleTypes.THICK]: { strokeWidth: 3 },
  [BorderStyleTypes.DOUBLE]: { strokeWidth: 1, double: true }
}

const visual = computed<LineVisual>(() =>
  lineVisuals[props.lineStyle] || lineVisuals[BorderStyleTypes.THIN]!
)
</script>

<template>
  <svg
    aria-hidden="true"
    :width="width"
    height="8"
    viewBox="0 0 120 8"
    preserveAspectRatio="none"
    xmlns="http://www.w3.org/2000/svg"
  >
    <template v-if="visual.double">
      <line x1="0" y1="2.5" x2="120" y2="2.5" stroke="currentColor" stroke-width="1" />
      <line x1="0" y1="5.5" x2="120" y2="5.5" stroke="currentColor" stroke-width="1" />
    </template>
    <line
      v-else
      x1="0"
      y1="4"
      x2="120"
      y2="4"
      stroke="currentColor"
      :stroke-width="visual.strokeWidth"
      :stroke-dasharray="visual.dasharray"
      stroke-linecap="butt"
    />
  </svg>
</template>
