<template>
  <el-dialog
    ref="canvasCacheDialogRef"
    :append-to-body="true"
    v-model="dialogShow"
    width="340px"
    :show-close="false"
    trigger="click"
  >
    <el-row style="height: 20px">
      <el-col :span="3">
        <Icon name="warn-tree" style="width: 20px; height: 20px; float: right" />
      </el-col>
      <el-col :span="21">
        <span style="font-size: 13px; margin-left: 10px; font-weight: bold; line-height: 20px">
          {{ dialogInfo.tips }}
        </span>
      </el-col>
    </el-row>
    <template #footer>
      <div class="dialog-footer">
        <el-button size="mini" @click="doUseCache(false)">否 </el-button>
        <el-button type="primary" size="mini" @click="doUseCache(true)">是 </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
const dvMainStore = dvMainStoreWithOut()
const dialogShow = ref(false)
const { t } = useI18n()
import { useCache } from '@/hooks/web/useCache'
const { wsCache } = useCache()
const emits = defineEmits(['doUseCache'])

const dialogInfo = {
  resourceId: null,
  title: '',
  tips: ''
}

const dialogInit = initInfo => {
  const canvasTypeName = initInfo.canvasType === 'dataV' ? '数据大屏' : '仪表板'
  dialogInfo.resourceId = initInfo.resourceId
  dialogInfo.title = '存在未保存的' + canvasTypeName
  dialogInfo.tips = canvasTypeName + '存在未保存的修改，立即恢复？'
  dialogShow.value = true
}

const doUseCache = flag => {
  emits('doUseCache', flag)
  dialogShow.value = false
}

defineExpose({
  dialogInit
})
</script>

<style lang="less" scoped></style>
