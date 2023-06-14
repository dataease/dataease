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
        :style="{ color: curComponent.commonBackground.innerImageColor }"
        class-name="svg-background"
        :name="mainIconClass"
      />
    </div>
    <span class="demonstration">{{ template.name }}</span>
  </div>
</template>

<script setup lang="ts">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { hexColorToRGBA } from '@/views/chart/components/js/util'
import { storeToRefs } from 'pinia'
import { imgUrlTrans } from '@/utils/imgUtils'
import { computed, toRefs } from 'vue'
const dvMainStore = dvMainStoreWithOut()
const { curComponent } = storeToRefs(dvMainStore)
import Icon from '@/components/icon-custom/src/Icon.vue'

const props = defineProps({
  template: {
    type: Object,
    default() {
      return {}
    }
  }
})

const { template } = toRefs(props)

const itemStyle = computed(() => {
  if (curComponent.value.commonBackground.backgroundColorSelect) {
    return {
      'background-color': hexColorToRGBA(
        curComponent.value.commonBackground.color,
        curComponent.value.commonBackground.alpha
      )
    }
  } else {
    return {}
  }
})

const mainIconClass = computed(() => {
  return template.value.url.replace('board/', '').replace('.svg', '')
})

const itemActive = computed(() => {
  return (
    curComponent.value &&
    curComponent.value.commonBackground &&
    curComponent.value.commonBackground.innerImage === template.value.url
  )
})

const setBoard = () => {
  curComponent.value.commonBackground.innerImage = template.value.url
}

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
</script>

<style scoped>
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
  width: 130px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.template-img {
  position: relative;
  height: 80px;
  width: 130px;
  margin: 0 auto;
  /*box-shadow: 0 0 2px 0 rgba(31, 31, 31, 0.15), 0 1px 2px 0 rgba(31, 31, 31, 0.15);*/
  /*border: solid px #fff;*/
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
