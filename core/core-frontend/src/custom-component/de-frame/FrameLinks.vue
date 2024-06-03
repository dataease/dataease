<template>
  <el-row>
    <el-form @submit.prevent ref="form" size="small" style="width: 100%">
      <el-form-item>
        <template #label>
          <span class="data-area-label">
            <span style="margin-right: 4px">
              {{ t('visualization.web_url') }}
            </span>
            <el-tooltip class="item" :effect="toolTip" placement="bottom">
              <template #content>
                <div>
                  {{ t('visualization.web_set_tips') }}
                </div>
              </template>
              <el-icon class="hint-icon" :class="{ 'hint-icon--dark': themes === 'dark' }">
                <Icon name="icon_info_outlined" />
              </el-icon>
            </el-tooltip>
          </span>
        </template>
        <el-input :effect="themes" v-model="state.linkInfoTemp.src" @blur="onBlur" />
      </el-form-item>
    </el-form>
  </el-row>
</template>

<script setup lang="ts">
import { reactive, ref, toRefs, watch, computed } from 'vue'
import { dvMainStoreWithOut } from '../../store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia/dist/pinia'
import { checkAddHttp, deepCopy } from '../../utils/utils'
import { snapshotStoreWithOut } from '../../store/modules/data-visualization/snapshot'
import { useI18n } from '../../hooks/web/useI18n'
import { useEmitt } from '@/hooks/web/useEmitt'
const dvMainStore = dvMainStoreWithOut()
const { curComponent, curActiveTabInner } = storeToRefs(dvMainStore)
const snapshotStore = snapshotStoreWithOut()
const emits = defineEmits(['close'])
const popover = ref(null)
const { t } = useI18n()

const props = defineProps({
  canvasId: {
    type: String,
    require: true
  },
  frameLinks: {
    type: Object,
    required: true
  },
  themes: {
    type: String,
    required: true,
    default: 'dark'
  }
})

const { frameLinks } = toRefs(props)

const toolTip = computed(() => {
  return props.themes === 'dark' ? 'ndark' : 'dark'
})

const state = reactive({
  linkInfoTemp: null,
  componentType: null,
  linkageActiveStatus: false,
  editFilter: ['view', 'custom']
})

const init = () => {
  state.linkInfoTemp = deepCopy(frameLinks.value)
}
const onBlur = () => {
  state.linkInfoTemp.src = checkAddHttp(state.linkInfoTemp.src)
  curComponent.value.frameLinks.src = state.linkInfoTemp.src
  snapshotStore.recordSnapshotCache()
  useEmitt().emitter.emit('frameLinksChange-' + curComponent.value.id)
}
init()
watch(
  () => frameLinks.value,
  () => {
    init()
  },
  { deep: true }
)
</script>

<style lang="less" scoped>
.slot-class {
  color: white;
}

.bottom {
  margin-top: 20px;
  text-align: center;
}
.ellipsis-area {
  margin-left: 10px;
  margin-right: 10px;
  overflow: hidden; /*超出部分隐藏*/
  white-space: nowrap; /*不换行*/
  text-overflow: ellipsis; /*超出部分文字以...显示*/
  background-color: #f7f8fa;
  color: #3d4d66;
  font-size: 12px;
  line-height: 24px;
  height: 24px;
  border-radius: 3px;
}

.select-filed {
  margin-left: 10px;
  margin-right: 10px;
  overflow: hidden; /*超出部分隐藏*/
  white-space: nowrap; /*不换行*/
  text-overflow: ellipsis; /*超出部分文字以...显示*/
  color: #3d4d66;
  font-size: 12px;
  line-height: 35px;
  height: 35px;
  border-radius: 3px;
}

.tips-class {
  color: #909399;
  font-size: 8px;
  margin-left: 3px;
}

.hint-icon {
  cursor: pointer;
  font-size: 14px;
  color: #646a73;

  &.hint-icon--dark {
    color: #a6a6a6;
  }
}
.data-area-label {
  display: flex;
  flex-direction: row;
  align-items: center;
}
</style>
