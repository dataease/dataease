<template>
  <div
    :class="[
      {
        ['background-selected']: themeSelected
      },
      'subject-template'
    ]"
  >
    <div class="vertical-layout" @click.stop="subjectChange">
      <!-- 背景-->
      <div class="allBack common-background" :style="customBackground" />
      <!-- 视图组件 背景-->
      <div
        style="inset: 17px 10px 10px; position: absolute"
        :style="chartBackground"
        class="chart_area"
      />
      <!-- 视图组件 主题-->
      <div style="inset: 20px 13px 15px; position: absolute">
        <div style="position: absolute; inset: 0px 4px; width: auto; height: auto">
          <!--柱形-->
          <div
            style="left: 0px; top: 11px; bottom: 0px; width: 3px; position: absolute"
            :style="columnBackgroundLeft"
          />
          <div
            style="left: 5px; top: 6px; bottom: 0px; width: 3px; position: absolute"
            :style="columnBackgroundMiddle"
          />
          <div
            style="left: 10px; top: 2px; bottom: 0px; width: 3px; position: absolute"
            :style="columnBackgroundRight"
          />

          <!--柱形-->
          <div
            style="left: 20px; top: 11px; bottom: 0px; width: 3px; position: absolute"
            :style="columnBackgroundLeft"
          />
          <div
            style="left: 25px; top: 2px; bottom: 0px; width: 3px; position: absolute"
            :style="columnBackgroundMiddle"
          />
          <div
            style="left: 30px; top: 6px; bottom: 0px; width: 3px; position: absolute"
            :style="columnBackgroundRight"
          />

          <!--柱形-->
          <div
            style="left: 40px; top: 2px; bottom: 0px; width: 3px; position: absolute"
            :style="columnBackgroundLeft"
          />
          <div
            style="left: 45px; top: 6px; bottom: 0px; width: 3px; position: absolute"
            :style="columnBackgroundMiddle"
          />
          <div
            style="left: 50px; top: 11px; bottom: 0px; width: 3px; position: absolute"
            :style="columnBackgroundRight"
          />

          <!--柱形-->
          <div
            style="left: 60px; top: 6px; bottom: 0px; width: 3px; position: absolute"
            :style="columnBackgroundLeft"
          />
          <div
            style="left: 65px; top: 11px; bottom: 0px; width: 3px; position: absolute"
            :style="columnBackgroundMiddle"
          />
          <div
            style="left: 70px; top: 2px; bottom: 0px; width: 3px; position: absolute"
            :style="columnBackgroundRight"
          />

          <!--柱形-->
          <div
            style="left: 80px; top: 6px; bottom: 0px; width: 3px; position: absolute"
            :style="columnBackgroundLeft"
          />
          <div
            style="left: 85px; top: 11px; bottom: 0px; width: 3px; position: absolute"
            :style="columnBackgroundMiddle"
          />
          <div
            style="left: 90px; top: 2px; bottom: 0px; width: 3px; position: absolute"
            :style="columnBackgroundRight"
          />
        </div>
      </div>

      <!-- 表格表头颜色 -->
      <div
        style="left: 10px; right: 10px; top: 10px; height: 6px; position: absolute"
        :style="tableHeadBackground"
      />

      <!-- 字体颜色 -->
      <div style="left: 14px; top: 10px; height: 6px; position: absolute; vertical-align: middle">
        <div
          style="
            width: 1px;
            height: 2px;
            position: relative;
            flex-shrink: 0;
            margin-top: 2px;
            margin-right: 1px;
            float: left;
          "
          :style="tableFontColor"
        />
        <div
          style="
            width: 1px;
            height: 2px;
            position: relative;
            flex-shrink: 0;
            margin-top: 2px;
            margin-right: 1px;
            float: left;
          "
          :style="tableFontColor"
        />
        <div
          style="
            width: 1px;
            height: 2px;
            position: relative;
            flex-shrink: 0;
            margin-top: 2px;
            margin-right: 1px;
            float: left;
          "
          :style="tableFontColor"
        />
        <div
          style="
            width: 1px;
            height: 2px;
            position: relative;
            flex-shrink: 0;
            margin-top: 2px;
            margin-right: 1px;
            float: left;
          "
          :style="tableFontColor"
        />
        <div
          style="
            width: 1px;
            height: 2px;
            position: relative;
            flex-shrink: 0;
            margin-top: 2px;
            margin-right: 1px;
            float: left;
          "
          :style="tableFontColor"
        />
        <div
          style="
            width: 1px;
            height: 2px;
            position: relative;
            flex-shrink: 0;
            margin-top: 2px;
            margin-right: 1px;
            float: left;
          "
          :style="tableFontColor"
        />
      </div>
    </div>
    <div class="title-main" @dblclick="setEdit">
      <div class="title-area">
        <el-input
          v-if="canEdit"
          ref="nameInput"
          v-model="subjectItem.name"
          size="mini"
          @blur="loseFocus()"
        />
        <span v-if="!canEdit" style="margin-top: 8px; margin-left: 8px" :title="subjectItem.name">{{
          subjectItem.name
        }}</span>
      </div>
    </div>
    <i
      v-if="subjectItem.type === 'self' && !canEdit"
      class="el-icon-delete delete-icon"
      @click.stop="subjectDelete"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, toRefs, watch } from 'vue'
import { imgUrlTrans } from '@/utils/imgUtils'
import { hexColorToRGBA } from '@/views/chart/components/js/util'
import { chartTransStr2Object } from '@/utils/canvasUtils'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
const dvMainStore = dvMainStoreWithOut()
const { canvasStyleData } = storeToRefs(dvMainStore)

const state = reactive({
  defaultSubject: {},
  subjectItemDetails: null,
  canEdit: false
})

const props = defineProps({
  subjectItem: {
    type: Object,
    required: true
  }
})

const { subjectItem } = toRefs(props)

const customBackground = computed(() => {
  let style = {
    width: '100%',
    height: '100%',
    background: 'background: 0% 0% / cover rgb(255, 255, 255)',
    backgroundSize: '100% 100% !important'
  }
  if (state.subjectItemDetails) {
    if (
      state.subjectItemDetails.panel.backgroundType === 'image' &&
      state.subjectItemDetails.panel.imageUrl
    ) {
      return {
        width: '100%',
        height: '100%',
        background: `url(${imgUrlTrans(state.subjectItemDetails.panel.imageUrl)}) no-repeat`
      }
    } else {
      return {
        width: '100%',
        height: '100%',
        background: state.subjectItemDetails.panel.color
      }
    }
  }
  return style
})

const columnBackgroundLeft = computed(() => {
  let style = {}
  if (state.subjectItemDetails) {
    style = {
      opacity: state.subjectItemDetails.chartInfo.chartColor.alpha / 100,
      background: state.subjectItemDetails.chartInfo.chartColor.colors[0]
    }
  }
  return style
})
const columnBackgroundMiddle = computed(() => {
  let style = {}
  if (state.subjectItemDetails) {
    style = {
      opacity: state.subjectItemDetails.chartInfo.chartColor.alpha / 100,
      background: state.subjectItemDetails.chartInfo.chartColor.colors[1]
    }
  }
  return style
})

const columnBackgroundRight = computed(() => {
  let style = {}
  if (state.subjectItemDetails) {
    style = {
      opacity: state.subjectItemDetails.chartInfo.chartColor.alpha / 100,
      background: state.subjectItemDetails.chartInfo.chartColor.colors[2]
    }
  }
  return style
})

const tableHeadBackground = computed(() => {
  let style = {}
  if (state.subjectItemDetails) {
    style = {
      opacity: state.subjectItemDetails.chartInfo.chartColor.alpha / 100,
      background: state.subjectItemDetails.chartInfo.chartColor.tableHeaderBgColor
    }
  }
  return style
})

const tableFontColor = computed(() => {
  let style = {}
  if (state.subjectItemDetails) {
    style = {
      background: state.subjectItemDetails.chartInfo.chartColor.tableFontColor
    }
  }
  return style
})

const chartBackground = computed(() => {
  const style = {}
  if (state.subjectItemDetails && state.subjectItemDetails.chartInfo.chartCommonStyle) {
    const commonBackground = state.subjectItemDetails.chartInfo.chartCommonStyle
    style['padding'] = (commonBackground.innerPadding || 0) + 'px'
    style['border-radius'] = (commonBackground.borderRadius || 0) + 'px'
    let colorRGBA = ''
    if (commonBackground.backgroundColorSelect) {
      colorRGBA = hexColorToRGBA(commonBackground.color, commonBackground.alpha)
    }
    if (commonBackground.enable) {
      if (
        commonBackground.backgroundType === 'innerImage' &&
        typeof commonBackground.innerImage === 'string'
      ) {
        const innerImage = commonBackground.innerImage.replace('svg', 'png')
        style['background'] = `url(${imgUrlTrans(innerImage)}) no-repeat ${colorRGBA}`
      } else if (
        commonBackground.backgroundType === 'outerImage' &&
        typeof commonBackground.outerImage === 'string'
      ) {
        style['background'] = `url(${imgUrlTrans(
          commonBackground.outerImage
        )}) no-repeat ${colorRGBA}`
      } else {
        style['background-color'] = colorRGBA
      }
    } else {
      style['background-color'] = colorRGBA
    }
    style['overflow'] = 'hidden'
  }
  return style
})

const themeSelected = computed(() => {
  return (
    state.subjectItemDetails && state.subjectItemDetails.themeId === canvasStyleData.value.themeId
  )
})

watch(
  subjectItem.value,
  newVal => {
    state.subjectItemDetails = chartTransStr2Object(JSON.parse(newVal.details), 'Y')
  },
  { deep: true }
)
</script>

<style scoped>
.allBack {
  background-size: 100% 100% !important;
}

.subject-template {
  width: 135px;
  height: 110px;
  position: relative;
  float: left;
  margin: 5px;
  display: inline-block;
  border: 1px solid #dee0e3;
  border-radius: 5px;
  z-index: 2;
}

.subject-template:hover {
  border: solid 1px #4b8fdf;
  color: deepskyblue;
  cursor: pointer;
}

.demonstration {
  display: block;
  text-align: center;
  margin: 10px auto;
  width: 150px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.vertical-layout {
  overflow: hidden;
  position: absolute;
  inset: 0px 0px 30px;
  width: 131px;
  height: 78px;
  margin: 0 auto;
  box-sizing: border-box;
}

.vertical-layout > i {
  float: right;
  color: gray;
  margin: 2px;
  position: relative;
}

.vertical-layout > i:hover {
  color: red;
}

.theme-selected-icon {
  z-index: 2;
  font-size: 16px;
  position: absolute;
  bottom: 0px;
  right: 0px;
  color: #4b8fdf;
}

.title-area {
  background-color: #ffffff;
  color: #1f2329;
  font-size: 12px;
  height: 30px;
  line-height: 30px;
  text-align: left;
  white-space: pre;
  text-overflow: ellipsis;
  margin-left: 1px;
  margin-right: 1px;
  overflow: hidden;
}

.common-background {
  border-radius: 5px 5px 0 0;
  position: absolute;
}

.background-selected {
  border: solid 2px #4b8fdf;
}

.delete-icon {
  position: absolute;
  bottom: 8px;
  right: 8px;
}

.delete-icon:hover {
  color: red;
}

.title-main {
  position: absolute;
  left: 0px;
  right: 0px;
  bottom: 0px;
  height: 30px;
  float: left;
}

.subject-template:hover > .title-main {
  width: 115px;
}

.subject-template:hover > .el-icon-delete {
  z-index: 10;
  display: block;
}

.subject-template ::v-deep .el-icon-delete {
  display: none;
}

.chart_area {
  background-size: 100% 100% !important;
}
</style>
