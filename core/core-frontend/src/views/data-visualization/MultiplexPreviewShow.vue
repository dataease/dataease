<script lang="ts" setup>
import { computed, nextTick, onBeforeMount, reactive, ref, toRefs, watch } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { getCanvasStyle } from '@/utils/style'
import EmptyBackground from '../../components/empty-background/src/EmptyBackground.vue'
const dvMainStore = dvMainStoreWithOut()
const viewShow = ref(true)

const props = defineProps({
  canvasStyleData: {
    type: Object,
    required: true
  },
  componentData: {
    type: Object,
    required: true
  },
  canvasViewInfo: {
    type: Object,
    required: true
  },
  dvInfo: {
    type: Object,
    required: true
  }
})

const { canvasStyleData, componentData, canvasViewInfo, dvInfo } = toRefs(props)

const canvasStyle = computed(() => getCanvasStyle(canvasStyleData.value))

const filterNodeMethod = (value, data) => {
  return !value || data.multiplexActive
}

const nodeClick = data => {
  const components = componentData.value.filter(ele => ele.id === data.id)
  state.multiplexInfo = components[0]
  viewShow.value = false
  nextTick(() => {
    viewShow.value = true
  })
}

const multiplexInfoTree = ref(null)

const state = reactive({
  sourceMultiplexInfo: {},
  showSelected: false,
  curMultiplexViewInfo: {},
  curDatasetInfo: {},
  initState: false,
  viewId: null,
  treeProp: {
    id: 'id',
    label: 'label',
    name: 'name'
  },
  multiplexInfo: null
})
const curMultiplexTargetComponentsInfo = ref([])

const targetViewCheckedChange = (val, data) => {
  nextTick(() => {
    multiplexingCheck(val, data)
    multiplexInfoTree.value.setCurrentKey(data.id)
    nodeClick(data)
  })
}

const getIconName = item => {
  if (item.component === 'UserView') {
    const viewInfo = canvasViewInfo.value[item.id]
    return `${viewInfo.type}`
  } else {
    return item.icon
  }
}

watch(
  () => dvInfo.value,
  () => {
    if (dvInfo.value) {
      init()
    }
  }
)

watch(
  () => state.showSelected,
  newValue => {
    multiplexInfoTree.value?.filter(newValue)
  }
)

const init = () => {
  dvMainStore.initCurMultiplexingComponents()
  curMultiplexTargetComponentsInfo.value = []
  componentData.value?.forEach(item => {
    curMultiplexTargetComponentsInfo.value.push({
      id: item.id,
      label: item.label,
      icon: item.icon,
      multiplexActive: false,
      component: item.component
    })
  })
}

const multiplexingCheck = (val, data) => {
  if (val) {
    const components = componentData.value.filter(ele => ele.id === data.id)
    // push
    dvMainStore.addCurMultiplexingComponent({
      component: components[0],
      componentId: data.id
    })
  } else {
    // remove
    dvMainStore.removeCurMultiplexingComponentWithId(data.id)
  }
}

onBeforeMount(() => {
  init()
})
</script>

<template>
  <el-row class="preview">
    <el-col :span="6" style="height: 100%; overflow-y: auto">
      <el-row class="tree-head">
        <span class="head-text">选择组件</span>
        <span class="head-filter"
          >仅看已选 <el-switch size="small" v-model="state.showSelected" />
        </span>
      </el-row>
      <el-tree
        class="custom-tree-multiplex"
        menu
        ref="multiplexInfoTree"
        :empty-text="'暂无可用组件'"
        :filter-node-method="filterNodeMethod"
        :data="curMultiplexTargetComponentsInfo"
        node-key="targetViewId"
        highlight-current
        @node-click="nodeClick"
      >
        <template #default="{ data }">
          <span class="custom-tree-node">
            <span>
              <div @click.stop>
                <span class="auth-span">
                  <el-checkbox
                    v-model="data.multiplexActive"
                    @change="targetViewCheckedChange($event, data)"
                  />
                </span>
              </div>
            </span>
            <span>
              <span class="tree-select-field">
                <Icon
                  class-name="view-type-icon"
                  style="margin-right: 4px"
                  :name="getIconName(data)"
                />
                {{ data.label }}
              </span>
            </span>
          </span>
        </template>
      </el-tree>
    </el-col>
    <el-col :span="18" class="preview-show">
      <div class="view-show-content-outer">
        <div class="view-show-content">
          <ComponentWrapper
            v-if="viewShow && state.multiplexInfo && state.multiplexInfo.id"
            class="wrapper-content"
            :style="canvasStyle"
            :view-info="canvasViewInfo[state.multiplexInfo.id]"
            :config="state.multiplexInfo"
            :canvas-style-data="canvasStyleData"
            :dv-info="dvInfo"
            :canvas-view-info="canvasViewInfo"
          />
          <empty-background v-else description="当前未选择组件" img-type="select" />
        </div>
      </div>
    </el-col>
  </el-row>
</template>

<style scoped lang="less">
.root-class {
  margin: 15px 0px 5px;
  justify-content: right;
}

.preview {
  border-radius: 4px;
  width: 100%;
  height: 100% !important;
  overflow: hidden;
  background-size: 100% 100% !important;
}

.preview-show {
  height: 100%;
  padding-left: 12px;
  background-size: 100% 100% !important;
}

.slot-class {
  color: white;
}

.bottom {
  margin-top: 10px;
  justify-content: center;
}

.ellip {
  margin-left: 10px;
  margin-right: 10px;
  overflow: hidden; /*超出部分隐藏*/
  white-space: nowrap; /*不换行*/
  text-overflow: ellipsis; /*超出部分文字以...显示*/
  text-align: center;
  background-color: #f7f8fa;
  color: #3d4d66;
  font-size: 12px;
  line-height: 24px;
  height: 24px;
  border-radius: 3px;
}

.select-filed {
  overflow: hidden; /*超出部分隐藏*/
  white-space: nowrap; /*不换行*/
  text-overflow: ellipsis; /*超出部分文字以...显示*/
  color: #3d4d66;
  font-size: 12px;
  border-radius: 3px;
}

.custom-position {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  flex-flow: row nowrap;
  color: #9ea6b2;
  flex-direction: column;
  span {
    line-height: 22px;
    color: #646a73;
  }
}

.tree-style {
  padding: 10px 15px;
  height: 100%;
  overflow-y: auto;
}
.custom-tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
}

.auth-span {
  float: left;
  width: 30px;
  margin-left: -8px;
}

.tree-head {
  height: 40px;
  line-height: 40px;
  font-size: 12px;
  color: #3d4d66;
  background-color: white;
  .head-text {
    margin-left: 16px;
    font-weight: 500;
    font-size: 14px;
    color: #1f2329;
  }
  .head-filter {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: end;
    margin-right: 16px;
    font-weight: 400;
    font-size: 12px;
    color: #646a73;
    .ed-switch {
      margin-left: 8px;
    }
  }
}

.padding-lr {
  padding: 0 4px;
}

.field-height {
  height: calc(100% - 25px);
  margin-top: 12px;
}

.drag-list {
  height: calc(100% - 26px);
  overflow: auto;
}

.item-dimension {
  display: flex !important;
  align-items: center;
  padding: 2px 10px;
  margin: 2px 2px 0 2px;
  border: solid 1px #eee;
  text-align: left;
  color: #606266;
  font-size: 14px;
  background-color: white;
  display: block;
  word-break: break-all;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.item-dimension + .item-dimension {
  margin-top: 2px;
}

.item-dimension:hover {
  color: #1890ff;
  background: #e8f4ff;
  border-color: #a3d3ff;
  cursor: pointer;
}

.item-quota {
  padding: 2px 10px;
  margin: 2px 2px 0 2px;
  border: solid 1px #eee;
  text-align: left;
  color: #606266;
  background-color: white;
  display: block;
  word-break: break-all;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.item-quota + .item-quota {
  margin-top: 2px;
}

.item-quota:hover {
  color: #67c23a;
  background: #f0f9eb;
  border-color: #b2d3a3;
  cursor: pointer;
}

.blackTheme .item-quota:hover {
}

span {
  font-size: 12px;
}

.set-name-area {
  font-weight: 600;
  margin-right: 20px;
}

:deep(.ed-row) {
  width: 100%;
}

.dv-selector {
  width: 100%;
}

.top-area {
  float: left;
  line-height: 33px;
  display: flex;
  flex-direction: row;
  align-items: center;
}

.top-area-text {
  font-weight: 400;
  font-size: 14px;
  color: #646a73;
  margin-left: 24px;
}

.top-area-value {
  font-weight: 400;
  font-size: 14px;
  color: #1f2329;
  display: flex;
  flex-direction: row;
  align-items: center;
}
.view-type-icon {
  color: var(--ed-color-primary);
  width: 22px;
  height: 16px;
}
.content-head {
  height: 22px;
  margin-top: 10px;
  margin-left: 16px;
  font-weight: 500;
  font-size: 14px;
  color: #1f2329;
  line-height: 32px;
  margin-right: 16px;
}
.link-icon-join {
  font-size: 20px;
  margin-top: 7px;
  margin-left: 8px;
  margin-right: 8px;
}
.inner-content {
  width: 100%;
  padding: 16px 16px 8px 16px;
  font-size: 14px !important;
}

.outer-content {
  height: 340px;
  border-radius: 4px;
}

.padding-lr {
  height: 500px;
  border: 1px solid var(--deCardStrokeColor, #dee0e3);
  border-radius: 4px;
  padding: 12px;
  box-sizing: border-box;
  margin-left: 12px;
  width: 214px;
  overflow-y: hidden;
}

.mb8 {
  margin-bottom: 8px;
  display: inline-flex;
  align-items: center;

  i {
    margin-left: 4.67px;
  }
}

.field-height {
  height: calc(50% - 41px);
  margin-top: 4px;
  overflow-y: auto;
}

.class-na {
  margin-top: 8px;
  text-align: center;
  font-size: 14px;
  color: var(--deTextDisable);
}
.outer-content-mirror {
  border: 1px solid #bbbfc4;
  border-radius: 4px;
  height: 100%;
  overflow: hidden;
}
.url-text {
  width: 100%;
  line-height: 14px;
  margin-bottom: 8px;
}

.tree-select-field {
  font-size: 14px;
  display: flex;
  align-items: center;
}

.custom-tree-multiplex {
  height: calc(100% - 40px);
  overflow-y: auto;
}
.m-del-icon-btn {
  color: #646a73;
  margin-top: 4px;
  margin-left: 4px;

  &:hover {
    background: rgba(31, 35, 41, 0.1) !important;
  }
  &:focus {
    background: rgba(31, 35, 41, 0.1) !important;
  }
  &:active {
    background: rgba(31, 35, 41, 0.2) !important;
  }
}

.custom-option {
  font-size: 14px;
  display: flex;
  align-items: center;
}

.enlarge-inner {
  position: relative;
  width: 100%;
  height: 100%;
}
.view-show-content-outer {
  width: 100%;
  height: 100%;
  padding: 12px;
  background: #ffffff;
}
.view-show-content {
  position: relative;
  width: 100%;
  height: 100%;
  .wrapper-content {
    width: 100%;
    height: 100%;
    background-size: 100% 100% !important;
  }
}
</style>
