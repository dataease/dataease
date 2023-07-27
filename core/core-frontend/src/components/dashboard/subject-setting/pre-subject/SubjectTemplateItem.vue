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
      <img
        v-if="subjectItem.coverUrl"
        :src="imgUrlTrans(subjectItem.coverUrl)"
        alt=""
        width="172"
        height="79"
      />
      <Icon v-else name="dv-no-img" style="width: 172px; height: 79px"></Icon>
    </div>
    <div class="title-main">
      <div class="title-area">
        <span style="margin-top: 8px; margin-left: 8px" :title="subjectItem.name">{{
          subjectItem.name
        }}</span>
      </div>
      <div class="edit-area" v-if="subjectItem.type === 'self'">
        <el-icon @click="subjectDelete()"><Delete class="custom-icon" /></el-icon>
        <el-icon @click="subjectEdit()"> <EditPen class="custom-icon" /> </el-icon>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, toRefs, watch } from 'vue'
import { imgUrlTrans } from '@/utils/imgUtils'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'

import { storeToRefs } from 'pinia'
import { ElMessageBox } from 'element-plus-secondary'
import Icon from '@/components/icon-custom/src/Icon.vue'
import { adaptCurThemeCommonStyleAll } from '@/utils/canvasStyle'
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()

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

const themeSelected = computed(() => {
  return (
    state.subjectItemDetails && state.subjectItemDetails.themeId === canvasStyleData.value.themeId
  )
})
const emit = defineEmits(['subjectDelete', 'onSubjectChange', 'subjectEdit'])
const subjectDelete = () => {
  ElMessageBox.confirm('确定删除[' + subjectItem.value.name + ']吗?', {
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    showClose: false
  }).then(() => {
    emit('subjectDelete', subjectItem.value.id)
  })
}

const subjectEdit = () => {
  emit('subjectEdit')
}

const subjectChange = () => {
  if (!themeSelected.value) {
    dvMainStore.setCanvasStyle(JSON.parse(subjectItem.value.details))
    snapshotStore.recordSnapshot('subjectChange')
    adaptCurThemeCommonStyleAll()
    emit('onSubjectChange')
  }
}

onMounted(() => {
  state.subjectItemDetails = JSON.parse(subjectItem.value.details)
})
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
  border: dashed 1px #4b8fdf;
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
  border: solid 1px #4b8fdf !important;
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
  width: 180px;
}

//.subject-template:hover > .title-main {
//  width: 150px;
//}

.subject-template:hover :deep(.custom-icon) {
  display: block;
}

.subject-template :deep(.ed-input__wrapper) {
  box-shadow: 0 0 0 0;
}

.chart-area {
  background-size: 100% 100% !important;
}

.edit-area {
  display: inline-block;
  line-height: 35px;
}

.custom-icon {
  display: none;
  margin: -13px 0 0 0;
  &:hover {
    color: red;
  }
}
</style>
