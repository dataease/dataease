<template>
  <div
    :class="[
      {
        ['template-item-main-active']: active
      },
      'template-item-main'
    ]"
    @click.stop="previewTemplate"
  >
    <div class="template-item-img" :style="classBackground" />
    <span class="demonstration">{{ template.title }}</span>
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
  width: 182px;
  height: 116px;
  background-color: var(--ContentBG, #ffffff);
  border: 1px solid #dee0e3;
  border-radius: 4px;
  flex: none;
  order: 0;
  flex-grow: 0;
  cursor: pointer;
  overflow: hidden;
}

.template-item-main-active {
  border: 2px solid var(--ed-color-primary) !important;
}
.template-item-img {
  position: absolute;
  width: 182px;
  height: 86px;
  left: 0px;
  top: 0px;
}

.demonstration {
  position: absolute;
  width: 166px;
  height: 20px;
  left: 8px;
  top: 91px;
  font-style: normal;
  font-weight: 400;
  font-size: 12px;
  line-height: 20px;
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.template-item-main:hover {
  border: solid 1px var(--ed-color-primary);
}
</style>
