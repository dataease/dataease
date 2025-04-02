<template>
  <div
    class="template-item-main"
    :class="[
      {
        ['template-item-main-active']: active
      }
    ]"
    @click.stop="previewTemplate"
  >
    <div class="template-item-img">
      <div class="template-item-img-inner" :style="classBackground" />
    </div>
    <div class="demonstration">{{ template.title }}</div>
  </div>
</template>

<script lang="ts" setup>
import { imgUrlTrans } from '@/utils/imgUtils'
import { computed } from 'vue'
const emits = defineEmits(['previewTemplate'])

const props = defineProps({
  template: {
    type: Object,
    default() {
      return {}
    }
  },
  baseUrl: {
    type: String
  },
  active: {
    type: Boolean,
    required: false,
    default: false
  }
})

const classBackground = computed(() => {
  return {
    background: `url(${imgUrlTrans(thumbnailUrl.value)}) no-repeat`,
    'background-size': `100% 100%`
  }
})

const thumbnailUrl = computed(() => {
  if (
    props.template.thumbnail.indexOf('http') > -1 ||
    props.template.thumbnail.indexOf('static-resource') > -1
  ) {
    return props.template.thumbnail
  } else {
    return props.baseUrl + props.template.thumbnail
  }
})

const previewTemplate = () => {
  emits('previewTemplate', props.template)
}
</script>

<style scoped lang="less">
.template-item-main {
  margin: 0 0 12px 0;
  position: relative;
  box-sizing: border-box;
  width: 192px;
  height: 145px;
  background-color: var(--ContentBG, #ffffff);
  border: 1px solid #dee0e3;
  border-radius: 4px;
  flex: none;
  order: 0;
  flex-grow: 0;
  cursor: pointer;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.template-item-main-active {
  border: 2px solid var(--ed-color-primary) !important;
}
.template-item-img {
  flex: 1;
  display: flex;
  padding: 4px 4px 0;
}
.template-item-img-inner {
  flex: 1;
}

.demonstration {
  height: 38px;
  padding: 8px 12px;
  font-weight: 500;
  font-size: 14px;
  line-height: 22px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  border-top: 1px solid #dee0e3;
}

.template-item-main:hover {
  border: solid 1px var(--ed-color-primary);
}
</style>
