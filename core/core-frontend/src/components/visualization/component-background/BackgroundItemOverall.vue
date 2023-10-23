<template>
  <div class="testcase-template">
    <div
      :class="[
        {
          ['template-img-active']: itemActive
        },
        'template-img'
      ]"
      :style="itemStyle"
      @click.stop="setBoard"
    >
      <Icon
        :style="{ color: commonBackground.innerImageColor }"
        class-name="svg-background"
        :name="mainIconClass"
      />
    </div>
    <span class="demonstration">{{ template.name }}</span>
  </div>
</template>

<script setup lang="ts">
import Icon from '@/components/icon-custom/src/Icon.vue'
import { computed, toRefs } from 'vue'
import { hexColorToRGBA } from '@/views/chart/components/js/util'
import { imgUrlTrans } from '@/utils/imgUtils'

const props = defineProps({
  template: {
    type: Object,
    default() {
      return {}
    }
  },
  commonBackground: {
    type: Object,
    required: true
  }
})

const { template, commonBackground } = toRefs(props)
const emits = defineEmits(['borderChange'])

const itemStyle = computed(() => {
  if (commonBackground.value.backgroundColorSelect) {
    return {
      'background-color': hexColorToRGBA(commonBackground.value.color, commonBackground.value.alpha)
    }
  } else {
    return {}
  }
})

const mainIconClass = computed(() => {
  return template.value.url.replace('board/', '').replace('.svg', '')
})
const itemActive = computed(() => {
  return commonBackground.value && commonBackground.value.innerImage === template.value.url
})
const classBackground = computed(() => {
  if (template.value.url) {
    return {
      background: `url(${imgUrlTrans(template.value.url)}) no-repeat`,
      'background-size': `100% 100%`
    }
  } else {
    return {}
  }
})

const setBoard = () => {
  commonBackground.value.innerImage = template.value.url
  emits('borderChange')
}
</script>

<style scoped lang="less">
.testcase-template {
  display: inline-block;
  margin: 5px 0px;
  width: 90px;
}

.demonstration {
  display: block;
  font-size: 8px;
  color: gray;
  text-align: center;
  margin: 10px auto;
  width: 110px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.template-img {
  position: relative;
  height: 70px;
  width: 110px;
  margin: 0 auto;
  box-sizing: border-box;
  border-radius: 3px;
}

.template-img:hover {
  border: solid 1px #4b8fdf;
  border-radius: 3px;
  color: deepskyblue;
  cursor: pointer;
}

.template-img > i {
  display: none;
  float: right;
  color: gray;
  margin: 2px;
}

.template-img > i:hover {
  color: red;
}

.template-img:hover > .el-icon-error {
  display: inline;
}

.template-img:hover > .el-icon-edit {
  display: inline;
}

.template-img-active {
  border: solid 1px red;
  border-radius: 3px;
  color: deepskyblue;
}

.svg-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100% !important;
  height: 100% !important;
}
:deep(.ed-row) {
  flex-direction: column;
}
</style>
