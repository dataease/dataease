<script setup lang="ts">
import { shallowRef } from 'vue'
import { CANVAS_MATERIAL } from '@/custom-component/common/ComponentConfig'
import { iconCanvasMap } from '@/components/icon-group/canvas-icon-list'
import DeDecoration from '@/custom-component/de-decoration/Component.vue'
import Icon from '@/components/icon-custom/src/Icon.vue'
import { useI18n } from '@/hooks/web/useI18n'
import eventBus from '@/utils/eventBus'

const { t } = useI18n()
const activeGroup = shallowRef(CANVAS_MATERIAL[0])

const insert = (innerType: string) => {
  eventBus.emit('handleNew', { componentName: activeGroup.value.category, innerType })
}

const startDrag = (event: DragEvent, innerType: string) => {
  event.dataTransfer?.setData('id', activeGroup.value.category + '&' + innerType)
}
</script>

<template>
  <section class="component-assets">
    <p class="component-assets-hint">{{ t('material.component_hint') }}</p>
    <div class="component-assets-body">
      <nav class="component-assets-categories" :aria-label="t('material.categories')">
        <button
          v-for="group in CANVAS_MATERIAL"
          :key="group.category"
          type="button"
          :aria-pressed="activeGroup.category === group.category"
          @click="activeGroup = group"
        >
          {{ group.title }}
        </button>
      </nav>
      <div
        class="component-assets-grid"
        :class="{ 'component-assets-icons': activeGroup.category === 'CanvasIcon' }"
      >
        <button
          v-for="item in activeGroup.details"
          :key="item.value"
          type="button"
          draggable="true"
          :title="item.title || item.value"
          class="component-asset"
          @click="insert(item.value)"
          @dragstart="startDrag($event, item.value)"
        >
          <span class="component-asset-preview">
            <Icon v-if="['outer_svg', 'graphical'].includes(item.type)">
              <component
                :is="item.icon"
                class="svg-icon component-asset-icon"
                :class="{ 'component-asset-border': item.type === 'outer_svg' }"
              />
            </Icon>
            <DeDecoration
              v-else-if="item.type === 'de_decoration'"
              :cur-style="{ width: 530, height: 373 }"
              :element="{ innerType: item.value }"
              :scale="0.2"
            />
            <component
              :is="iconCanvasMap[item.value]"
              v-else
              class="svg-icon component-asset-glyph"
            />
          </span>
          <span v-if="item.title" class="component-asset-name">{{ item.title }}</span>
        </button>
      </div>
    </div>
  </section>
</template>

<style lang="less" scoped>
.component-assets {
  color: var(--ed-text-color-primary);
}
.component-assets-hint {
  margin: 0 0 16px;
  color: var(--ed-text-color-secondary);
  line-height: 20px;
}
.component-assets-body {
  display: flex;
  gap: 20px;
  height: min(520px, calc(100vh - 245px));
  min-height: 240px;
}
.component-assets-categories {
  flex: 0 0 168px;
  box-sizing: border-box;
  padding: 8px 0;
  border: 1px solid var(--ed-border-color);
  border-radius: 6px;
  overflow-y: auto;

  button {
    display: block;
    width: 100%;
    padding: 12px 16px;
    border: 0;
    border-left: 3px solid transparent;
    background: transparent;
    color: inherit;
    text-align: left;
    font: inherit;
    cursor: pointer;

    &:hover,
    &:focus-visible {
      background: var(--ed-fill-color-light);
    }

    &[aria-pressed='true'] {
      color: var(--ed-color-primary);
      border-left-color: var(--ed-color-primary);
      background: var(--ed-color-primary-1a, #3370ff1a);
      font-weight: 500;
    }
  }
}
.component-assets-grid {
  display: grid;
  flex: 1;
  min-width: 0;
  align-content: start;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  overflow-y: auto;
}
.component-asset {
  min-width: 0;
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
  text-align: left;
  font: inherit;
  cursor: pointer;

  &:hover .component-asset-preview,
  &:focus-visible .component-asset-preview {
    border-color: var(--ed-color-primary);
  }
}
.component-asset-preview {
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  aspect-ratio: 1.72;
  padding: 8px 12px;
  border: 1px solid var(--ed-border-color);
  border-radius: 6px;
  background: var(--ed-fill-color-light);
  overflow: hidden;
  pointer-events: none;
}
.component-asset-icon {
  width: 100%;
  height: 100%;
}
.component-asset-border {
  color: #1094e5;
}
.component-asset-glyph {
  width: 28px;
  height: 28px;
  fill: currentColor;
}
.component-asset-name {
  display: block;
  margin-top: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
  line-height: 20px;
}

@media (max-width: 900px) {
  .component-assets-categories {
    flex-basis: 136px;
  }
  .component-assets-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 600px) {
  .component-assets-categories {
    flex-basis: 104px;
  }
  .component-assets-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
.component-assets-grid.component-assets-icons {
  grid-template-columns: repeat(auto-fill, minmax(44px, 1fr));
  gap: 8px;

  .component-asset-preview {
    aspect-ratio: 1;
    padding: 6px;
  }
}
</style>
