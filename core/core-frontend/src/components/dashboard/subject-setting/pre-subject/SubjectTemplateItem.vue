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
      <img src="@/assets/img/subject_dark.png" alt="" width="172" height="79" />
    </div>
    <div class="title-main" @dblclick="setEdit">
      <div class="title-area">
        <el-input
          v-if="state.canEdit"
          ref="nameInput"
          v-model="subjectItem.name"
          size="mini"
          @blur="loseFocus()"
        />
        <span
          v-if="!state.canEdit"
          style="margin-top: 8px; margin-left: 8px"
          :title="subjectItem.name"
          >{{ subjectItem.name }}</span
        >
      </div>
      <div v-if="subjectItem.type === 'self' && !state.canEdit">
        <el-icon @click="subjectDelete()"><DeleteFilled /></el-icon>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, toRefs, watch } from 'vue'
import { imgUrlTrans } from '@/utils/imgUtils'
import { hexColorToRGBA } from '@/views/chart/components/js/util'
import { chartTransStr2Object } from '@/utils/canvasUtils'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'

import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus-secondary'
import { saveOrUpdateSubject } from '@/api/visualization/dataVisualization'
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()

const { canvasStyleData } = storeToRefs(dvMainStore)
const nameInput = ref(null)

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

const emit = defineEmits(['subjectDelete', 'onSubjectChange', 'templateEdit'])

const subjectDelete = () => {
  emit('subjectDelete', subjectItem.value.id)
}

const subjectChange = () => {
  if (!themeSelected.value) {
    dvMainStore.setCanvasStyle(JSON.parse(subjectItem.value.details))
    snapshotStore.recordSnapshot()
    emit('onSubjectChange')
  }
}
const templateEdit = () => {
  emit('templateEdit')
}

const handleDelete = () => {
  return null
}
// 双击事件
const setEdit = () => {
  if (subjectItem.value.type === 'self') {
    state.canEdit = true
  } else {
    ElMessage.success('默认主题无法编辑')
  }
  // 将单元格变为输入框
  // // 聚焦到单元格
  setTimeout(() => {
    nameInput.value.focus()
  }, 20)
}
// 当输入框失去焦点时不显示输入框
const loseFocus = () => {
  if (
    subjectItem.value.name &&
    subjectItem.value.name.length > 0 &&
    subjectItem.value.name.length < 20
  ) {
    const request = {
      id: subjectItem.value.id,
      name: subjectItem.value.name
    }
    saveOrUpdateSubject(request).then(response => {
      ElMessage.success('保存成功')
      state.canEdit = false
    })
  } else {
    ElMessage.warning('主题名称不能为空')
  }
}
const selectChange = (callback, editCell) => {
  if (!callback) {
    editCell.edit = false
  }
}
</script>

<style scoped lang="less">
.all-back {
  background-size: 100% 100% !important;
}

.subject-template {
  position: relative;
  z-index: 2;
  display: inline-block;
  float: left;
  width: 182px;
  height: 116px;
  margin: 4px;
  border: 1px solid #dee0e3;
  border-radius: 5px;
}

.subject-template:hover {
  color: deepskyblue;
  cursor: pointer;
  border: solid 1px #4b8fdf;
}

.demonstration {
  display: block;
  width: 150px;
  margin: 10px auto;
  overflow: hidden;
  text-align: center;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.vertical-layout {
  position: absolute;
  padding: 4px 4px 0 4px;
  width: 180px;
  height: 84px;
  margin: 0 auto;
  overflow: hidden;
  //inset: 0 0 30px;
  box-sizing: border-box;
  background-size: contain;
}

.vertical-layout > i {
  position: relative;
  float: right;
  margin: 2px;
  color: gray;
}

.vertical-layout > i:hover {
  color: red;
}

.theme-selected-icon {
  position: absolute;
  right: 0;
  bottom: 0;
  z-index: 2;
  font-size: 16px;
  color: #4b8fdf;
}

.title-area {
  height: 30px;
  margin-right: 1px;
  margin-left: 1px;
  overflow: hidden;
  font-size: 12px;
  line-height: 30px;
  color: #1f2329;
  text-align: left;
  text-overflow: ellipsis;
  white-space: pre;
  flex: 1;
}

.common-background {
  position: absolute;
  border-radius: 5px 5px 0 0;
}

.background-selected {
  border: solid 2px #4b8fdf;
}

.delete-icon {
  position: absolute;
  right: 8px;
  bottom: 8px;
}

.delete-icon:hover {
  color: red;
}

.title-main {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  float: left;
  height: 31px;
  display: flex;
  border-top: 1px solid #dee0e3;
}

.subject-template:hover > .title-main {
  width: 150px;
}

.subject-template:hover > .el-icon-delete {
  z-index: 10;
  display: block;
}

.subject-template ::v-deep .el-icon-delete {
  display: none;
}

.chart-area {
  background-size: 100% 100% !important;
}
</style>
