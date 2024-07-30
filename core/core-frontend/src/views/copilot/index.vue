<script lang="ts" setup>
import { ref, shallowRef, computed, watch, nextTick } from 'vue'
import { ElMessageBox } from 'element-plus-secondary'
import {
  getDatasetTree,
  clearAllCopilot,
  copilotFields,
  getListCopilot,
  copilotChat
} from '@/api/dataset'
import { useElementSize } from '@vueuse/core'
import { fieldType } from '@/utils/attr'
import DialogueChart from '@/views/copilot/DialogueChart.vue'
import { type Tree } from '@/views/visualized/data/dataset/form/CreatDsGroup.vue'
import { cloneDeep } from 'lodash-es'
const quota = shallowRef([])
const dimensions = shallowRef([])
const datasetTree = shallowRef([])
const historyArr = ref([])
const datasetId = ref('')
const questionInput = ref('')
const showLeft = ref(false)
const defaultProps = {
  children: 'children',
  label: 'name'
}

const dsSelectProps = {
  label: 'name',
  children: 'children',
  value: 'id',
  isLeaf: node => !node.children?.length
}
const expandedD = ref(true)
const expandedQ = ref(true)
const dfs = arr => {
  return arr.filter(ele => {
    if (!!ele.children?.length && !ele.leaf) {
      ele.children = dfs(ele.children)
      return !!ele.children?.length
    }
    return ele.leaf
  })
}

const computedTree = computed(() => {
  if (datasetTree.value[0]?.id === '0') {
    return dfs(datasetTree.value[0].children)
  }
  return dfs(datasetTree.value)
})

const isActive = computed(() => {
  return questionInput.value.trim().length && !!datasetId.value
})
const initDataset = async () => {
  await getDatasetTree({}).then(res => {
    datasetTree.value = (res as unknown as Tree[]) || []
  })
  getListCopilot().then(res => {
    const allList = (res as unknown as { history: object }[]) || []
    historyBack = allList[allList.length - 1]?.history || []
    historyArr.value = cloneDeep(allList).map(ele => ({ ...ele, loading: false }))
    if (!!allList.length) {
      datasetId.value = allList[0].datasetGroupId
      oldId = datasetId.value
      datasetId.value && getOptions(datasetId.value)
      if (oldId && !oldName) {
        nextTick(() => {
          dfsName(computedTree.value)
        })
      }
    }
  })
}

const treeSelectRef = ref()
let oldId = ''
let currentId = ''
let oldName = ''

const dfsName = arr => {
  return arr.filter(ele => {
    if (ele.id === oldId) {
      oldName = ele.name
    }
    if (!!ele.children?.length && !ele.leaf) {
      ele.children = dfsName(ele.children)
      return !!ele.children?.length
    }
    return ele.leaf
  })
}
const handleDatasetChange = () => {
  if (!!oldId && !!historyArr.value.length) {
    currentId = datasetId.value
    datasetId.value = oldId
    const msg = `当前数据集为【${oldName}】，切换数据集将清空当前会话。`
    ElMessageBox.confirm('确定要切换数据集吗？', {
      confirmButtonType: 'primary',
      type: 'warning',
      tip: msg,
      autofocus: false,
      showClose: false
    }).then(() => {
      datasetId.value = currentId
      oldId = datasetId.value
      oldName = treeSelectRef.value.getCurrentNode().name
      getOptions(datasetId.value)
      clearAllCopilot().then(() => {
        historyArr.value = []
        historyBack = []
      })
    })
  } else {
    oldId = datasetId.value
    oldName = treeSelectRef.value.getCurrentNode().name
    getOptions(datasetId.value)
  }
}
const getOptions = id => {
  copilotFields(id).then(res => {
    dimensions.value = res.data?.dimensionList || []
    quota.value = res.data?.quotaList || []
  })
}
initDataset()
let historyBack = []

const questionInputRef = ref()
const overHeight = ref(false)
const { height } = useElementSize(questionInputRef)
watch(
  () => height.value,
  val => {
    overHeight.value = val > 48
  }
)

const handleShowLeft = val => {
  showLeft.value = val
  historyArr.value.forEach(ele => {
    ele.loading = true
  })

  setTimeout(() => {
    historyArr.value.forEach(ele => {
      ele.loading = false
    })
  }, 300)
}
const copilotChatLoading = ref(false)
const inputRef = ref()
const queryAnswer = (event?: KeyboardEvent) => {
  if (event?.keyCode === 229) {
    return
  }

  if (event?.keyCode === 13) {
    event.preventDefault()
  }
  let copyAuestionInput = questionInput.value
  if (!isActive.value || copilotChatLoading.value) return
  questionInput.value = ''
  inputRef.value.blur()
  nextTick(() => {
    overHeight.value = false
  })
  historyArr.value.push({
    msgType: 'user',
    chart: {},
    id: `${+new Date()}`,
    question: copyAuestionInput,
    chartData: {
      data: {},
      title: ''
    },
    msgStatus: 1
  })
  copilotChatLoading.value = true
  copilotChat({
    datasetGroupId: datasetId.value,
    question: copyAuestionInput,
    history: historyBack
  })
    .then(res => {
      historyArr.value.push({ ...(res || {}), loading: false })
      historyBack = res.history || []
    })
    .finally(() => {
      copilotChatLoading.value = false
    })
}
</script>

<template>
  <div class="copilot">
    <div class="copilot-analysis">
      <el-icon style="margin-right: 8px; font-size: 24px">
        <Icon name="copilot" />
      </el-icon>
      Copilot 对话分析
    </div>
    <div class="copilot-service">
      <div class="dialogue">
        <div class="copilot-dialogue" :style="{ height: `calc(100vh - ${height + 152}px)` }">
          <DialogueChart key="isWelcome" isWelcome></DialogueChart>
          <DialogueChart :copilotInfo="ele" v-for="ele in historyArr" :key="ele.id"></DialogueChart>
          <DialogueChart v-if="copilotChatLoading" key="isAnswer" isAnswer></DialogueChart>
        </div>
        <div class="question-input" :class="overHeight && 'over-height'" ref="questionInputRef">
          <el-input
            v-model="questionInput"
            @keydown.stop
            ref="inputRef"
            @keydown.enter="queryAnswer"
            :autosize="{ minRows: 1, maxRows: 8 }"
            type="textarea"
            :placeholder="$t('common.inputText')"
          >
          </el-input>
          <el-icon v-if="copilotChatLoading" class="copilot-icon circular-input_icon">
            <Icon name="icon_loading_outlined"></Icon>
          </el-icon>
          <el-icon v-else class="copilot-icon" @click="queryAnswer" :class="isActive && 'active'">
            <Icon :name="isActive ? 'active-btn_copilot' : 'btn_copilot'"></Icon>
          </el-icon>
        </div>
      </div>
      <div class="dataset-select" :style="{ width: showLeft ? 0 : '280px' }">
        <el-tooltip effect="dark" content="收起" placement="left">
          <p v-show="!showLeft" class="arrow-right" @click="handleShowLeft(true)">
            <el-icon>
              <Icon name="icon_right_outlined"></Icon>
            </el-icon>
          </p>
        </el-tooltip>

        <el-tooltip effect="dark" content="展开" placement="left">
          <p v-show="showLeft" class="left-outlined" @click="handleShowLeft(false)">
            <el-icon>
              <Icon name="icon_left_outlined"></Icon>
            </el-icon>
          </p>
        </el-tooltip>
        <div class="title-dataset_select">选择数据集</div>
        <div style="margin: 0 16px" class="tree-select">
          <el-tree-select
            v-model="datasetId"
            :data="computedTree"
            placeholder="请选择数据集"
            @change="handleDatasetChange"
            :props="dsSelectProps"
            style="width: 100%"
            ref="treeSelectRef"
            placement="bottom"
            :render-after-expand="false"
            filterable
            popper-class="dataset-tree"
          >
            <template #default="{ node, data }">
              <div class="content">
                <el-icon size="18px" v-if="!data.leaf">
                  <Icon name="dv-folder"></Icon>
                </el-icon>
                <el-icon size="18px" v-if="data.leaf">
                  <Icon name="icon_dataset"></Icon>
                </el-icon>
                <span class="label ellipsis" style="margin-left: 8px" :title="node.label">{{
                  node.label
                }}</span>
              </div>
            </template>
          </el-tree-select>
        </div>
        <div class="preview-field">
          <div :class="['field-d', { open: expandedD }]">
            <div :class="['title', { expanded: expandedD }]" @click="expandedD = !expandedD">
              <ElIcon class="expand">
                <Icon name="icon_expand-right_filled"></Icon>
              </ElIcon>
              &nbsp;{{ $t('chart.dimension') }}
            </div>
            <el-tree v-if="expandedD" :data="dimensions" :props="defaultProps">
              <template #default="{ data }">
                <span class="custom-tree-node father">
                  <el-icon>
                    <Icon
                      :name="`field_${fieldType[data.deExtractType]}`"
                      :className="`field-icon-${
                        fieldType[[2, 3].includes(data.deExtractType) ? 2 : 0]
                      }`"
                    ></Icon>
                  </el-icon>
                  <span :title="data.name" class="label-tooltip">{{ data.name }}</span>
                </span>
              </template>
            </el-tree>
          </div>
          <div :class="['field-q', { open: expandedQ }]">
            <div :class="['title', { expanded: expandedQ }]" @click="expandedQ = !expandedQ">
              <ElIcon class="expand">
                <Icon name="icon_expand-right_filled"></Icon>
              </ElIcon>
              &nbsp;{{ $t('chart.quota') }}
            </div>
            <el-tree v-if="expandedQ" :data="quota" :props="defaultProps">
              <template #default="{ data }">
                <span class="custom-tree-node father">
                  <el-icon>
                    <Icon
                      :name="`field_${fieldType[data.deExtractType]}`"
                      :className="`field-icon-${
                        fieldType[[2, 3].includes(data.deExtractType) ? 2 : 0]
                      }`"
                    ></Icon>
                  </el-icon>
                  <span :title="data.name" class="label-tooltip">{{ data.name }}</span>
                </span>
              </template>
            </el-tree>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
.copilot {
  width: 100%;
  height: 100%;

  .copilot-analysis {
    background-color: #fff;
    padding: 16px 24px;
    display: flex;
    align-items: center;
    font-weight: 500;
    border-bottom: 1px solid #1f232926;
  }

  .copilot-service {
    width: 100%;
    height: calc(100% - 58px);
    display: flex;
    overflow-y: auto;
    .dialogue {
      flex: 1;
      position: relative;
      .copilot-dialogue {
        padding: 0 160px;
        padding-top: 24px;
        position: relative;
        overflow-y: auto;
        padding-bottom: 25px;
      }
      .question-input {
        min-height: 47px;
        width: calc(100% - 360px);
        margin-left: 20px;
        display: flex;
        align-items: center;
        justify-content: center;
        position: absolute;
        border: 1px solid #fff;
        bottom: 25px;
        border-radius: 8px;
        left: 180px;
        box-sizing: border-box;
        background: #fff;
        box-shadow: 0px 6px 24px 0px #1f232914;
        &:hover {
          border: 1px solid var(--ed-color-primary);
        }

        &:has(.ed-textarea__inner:focus) {
          border: 1px solid var(--ed-color-primary);
        }

        :deep(.ed-textarea__inner) {
          border-radius: 8px !important;
          box-shadow: none;
          resize: none;
          padding: 12px 16px;
          max-height: 200px;
        }

        &.over-height :deep(.ed-textarea__inner) {
          padding-bottom: 40px;
        }

        .copilot-icon {
          position: absolute !important;
          bottom: 12px;
          right: 16px;
          font-size: 24px;
          cursor: not-allowed;
          position: relative;
          &.active {
            cursor: pointer;
            &::after {
              content: '';
              position: absolute;
              height: 32px;
              width: 32px;
              border-radius: 8px;
              display: none;
              background: #3370ff1a;
            }
            &:hover {
              &::after {
                display: block;
              }
            }
          }
        }

        @keyframes circular {
          0% {
            transform: rotate(0);
          }

          100% {
            transform: rotate(360deg);
          }
        }

        .circular-input_icon {
          animation: circular 1s infinite;
        }
      }
    }

    .dataset-select {
      width: 280px;
      height: calc(100vh - 115px);
      background: #fff;
      border-left: 1px solid #1f232926;
      position: relative;

      .left-outlined {
        position: absolute;
        font-size: 12px;
        cursor: pointer;
        left: -20px;
        top: 16px;
        width: 20px;
        height: 24px;
        border-top-left-radius: 50%;
        border-bottom-left-radius: 50%;
        box-shadow: 0px 4px 8px 0px #0000001a;
        border: 1px solid #dee0e3;
        background: #fff;
        display: flex;
        align-items: center;
        & > .ed-icon {
          margin-left: 6px;
        }

        &:hover {
          width: 24px;
          left: -24px;
          color: var(--ed-color-primary, #3370ff);
          & > .ed-icon {
            margin-left: 8px;
          }
        }
      }

      .arrow-right {
        position: absolute;
        top: 16px;
        z-index: 2;
        cursor: pointer;
        margin: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        left: -12px;
        height: 24px;
        width: 24px;
        box-shadow: 0px 4px 8px rgba(0, 0, 0, 0.1);
        border: 1px solid #dee0e3;
        background: #fff;
        font-size: 12px;
        border-radius: 50%;
        &:hover {
          color: var(--ed-color-primary, #3370ff);
        }
      }

      .title-dataset_select {
        width: 100%;
        margin: 16px 16px 12px 16px;
        font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
        font-size: 14px;
        font-weight: 500;
        line-height: 22px;
      }
      .preview-field {
        padding: 0 8px;
        width: 100%;
        height: calc(100% - 100px);
        position: relative;
        overflow-y: auto;

        :deep(.ed-tree-node__content) {
          border-radius: 4px;
          &:hover {
            background: #1f23291a;
          }
        }

        :deep(.ed-tree-node.is-current > .ed-tree-node__content:not(.is-menu):after) {
          display: none;
        }

        .custom-tree-node {
          width: calc(100% - 32px);
          display: flex;
          align-items: center;
          padding-right: 8px;
          box-sizing: content-box;

          .label-tooltip {
            margin-left: 5.33px;
            width: 70%;
            font-size: 14px;
            overflow: hidden;
            white-space: nowrap;
            text-overflow: ellipsis;
          }
        }

        .field-d,
        .field-q {
          position: relative;
          height: 49px;

          &.open {
            max-height: 50%;
            height: 50%;
          }
          .title {
            cursor: pointer;
            position: sticky;
            top: 0px;
            height: 49px;
            font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
            font-style: normal;
            font-weight: 500;
            font-size: 14px;
            line-height: 22px;
            color: #1f2329;
            display: flex;
            align-items: center;
            z-index: 9;
            background-color: #fff;

            .expand {
              font-size: 10px;
            }

            &.expanded {
              .expand {
                transform: rotate(90deg);
              }
            }
          }
          overflow-y: auto;
        }

        .field-d {
          max-height: calc(100% - 50px);
          border-bottom: 1px solid rgba(31, 35, 41, 0.15);
        }
      }
    }
  }
}
</style>
