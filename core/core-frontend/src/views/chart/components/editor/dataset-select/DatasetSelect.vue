<script setup lang="ts">
import { Tree } from '../../../../visualized/data/dataset/form/CreatDsGroup.vue'
import { computed, onMounted, ref, watch } from 'vue'
import { Plus, Search } from '@element-plus/icons-vue'
import { useI18n } from '@/hooks/web/useI18n'
import { Field, getFieldByDQ } from '@/api/chart'
import _ from 'lodash'
import { useRouter } from 'vue-router'
import { getDatasetTree } from '@/api/dataset'
import { ElFormItem, FormInstance } from 'element-plus-secondary'

const { resolve } = useRouter()

const props = withDefaults(
  defineProps<{
    themes?: EditorTheme
    modelValue?: string | number
    stateObj: any
    viewId: string
  }>(),
  {
    datasetTree: () => [],
    themes: 'dark'
  }
)

const datasetSelector = ref(null)

const loadingDatasetTree = ref(false)

const datasetTree = ref<Tree[]>([])

const initDataset = () => {
  loadingDatasetTree.value = true
  getDatasetTree({})
    .then(res => {
      datasetTree.value = (res as unknown as Tree[]) || []
    })
    .finally(() => {
      loadingDatasetTree.value = false
      formRef.value.validate()
    })
}

const emits = defineEmits(['update:modelValue', 'update:stateObj'])

const _modelValue = computed({
  get() {
    return props.modelValue
  },
  set(v) {
    emits('update:modelValue', v)
  }
})

const state = computed({
  get() {
    return props.stateObj
  },
  set(v) {
    emits('update:stateObj', v)
  }
})

const dsSelectProps = {
  label: 'name',
  children: 'children',
  value: 'id',
  isLeaf: node => !node.children?.length
}

const { t } = useI18n()

const formRef = ref<FormInstance>()

const searchStr = ref<string>()

watch(searchStr, val => {
  datasetSelector.value!.filter(val)
})

const showTree = computed(() => {
  return datasetTree.value && datasetTree.value.length > 0 && !loadingDatasetTree.value
})

const showEmptyInfo = computed(() => {
  return !showTree.value && !loadingDatasetTree.value
})
// const showEmptySearchInfo = computed(() => {
//   return showEmptyInfo.value && !props.loadingDatasetTree
// })

const computedTree = computed(() => {
  if (showTree.value) {
    if (datasetTree.value[0].id === '0') {
      return datasetTree.value[0].children
    }
  }
  return datasetTree.value
})

const flattedTree = computed(() => {
  return _.filter(flatTree(computedTree.value), node => node.leaf)
})

const selectedNode = computed(() => {
  return _.find(flattedTree.value, node => node.id === _modelValue.value)
})

const exist = computed(() => {
  if (_modelValue.value) {
    if (selectedNode.value === undefined) {
      return false
    }
  }
  return true
})

const selectedNodeName = computed(() => {
  if (!exist.value) {
    return '数据集不存在'
  }
  return selectedNode.value?.name
})

const form = computed(() => {
  return { name: selectedNodeName.value }
})

const rules = ref([
  {
    validator: (rule: any, value: any, callback: any) => {
      if (!exist.value) {
        callback(new Error())
      } else {
        callback()
      }
    },
    trigger: ['change', 'blur']
  }
])

function flatTree(tree: Tree[]) {
  let result = _.cloneDeep(tree)
  _.forEach(tree, node => {
    if (node.children && node.children.length > 0) {
      result = _.union(result, flatTree(node.children))
    }
  })
  return result
}

const filterNode = (value: string, data: Tree) => {
  if (!value) return true
  return data.name?.includes(value)
}

const refresh = () => {
  initDataset()
}
const addDataset = () => {
  const { href } = resolve('/dataset')
  window.open(href, '_blank')
}

const datasetSelectorPopover = ref()

const dsClick = (data: Tree) => {
  if (data.leaf) {
    //选中赋值
    _modelValue.value = data.id
    getFields(data.id, props.viewId)
    //关闭弹窗
    datasetSelectorPopover.value?.hide()
  }
}
const getFields = (id, chartId) => {
  if (id) {
    getFieldByDQ(id, chartId)
      .then(res => {
        state.value.dimension = (res.dimensionList as unknown as Field[]) || []
        state.value.quota = (res.quotaList as unknown as Field[]) || []
        state.value.dimensionData = JSON.parse(JSON.stringify(state.value.dimension))
        state.value.quotaData = JSON.parse(JSON.stringify(state.value.quota))
      })
      .catch(e => {
        state.value.dimension = []
        state.value.quota = []
        state.value.dimensionData = []
        state.value.quotaData = []
      })
  } else {
    state.value.dimension = []
    state.value.quota = []
    state.value.dimensionData = []
    state.value.quotaData = []
  }
}
const _popoverShow = ref(false)
function onPopoverShow() {
  _popoverShow.value = true
}
function onPopoverHide() {
  _popoverShow.value = false
}

function getNode(nodeId: number) {
  return datasetSelector?.value?.getNode(nodeId)
}

defineExpose({ getNode })

onMounted(() => {
  initDataset()
})
</script>

<template>
  <div>
    <el-popover
      ref="datasetSelectorPopover"
      trigger="click"
      placement="bottom-start"
      :width="320"
      popper-class="customDatasetSelect"
      :show-arrow="false"
      @show="onPopoverShow"
      @hide="onPopoverHide"
      :effect="themes"
      offset="4"
    >
      <template #reference>
        <el-form ref="formRef" :model="form">
          <el-form-item prop="name" :rules="rules">
            <el-input
              size="middle"
              :effect="themes"
              v-model="selectedNodeName"
              readonly
              placeholder="请选择数据集"
            >
              <template #suffix>
                <el-icon class="input-arrow-icon" :class="{ reverse: _popoverShow }">
                  <ArrowDown />
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
        </el-form>
      </template>
      <template #default>
        <el-container>
          <el-header>
            <div class="m-title" :class="{ dark: themes === 'dark' }">
              <div>{{ t('dataset.datalist') }}</div>
              <el-button type="primary" link class="refresh-btn" @click="refresh">
                {{ t('commons.refresh') }}
              </el-button>
            </div>
            <el-input
              size="middle"
              :effect="themes"
              v-model="searchStr"
              :placeholder="t('dataset.search')"
              :prefix-icon="Search"
              clearable
            />
          </el-header>
          <el-main :class="{ dark: themes === 'dark' }">
            <el-scrollbar max-height="252px" always>
              <div class="m-loading" v-if="loadingDatasetTree" v-loading="loadingDatasetTree"></div>
              <div class="empty-info" v-if="showEmptyInfo">暂无数据集</div>
              <!--          <div class="empty-info" v-if="showEmptySearchInfo">暂无相关数据</div>-->
              <el-tree
                :class="{ dark: themes === 'dark' }"
                v-if="showTree"
                ref="datasetSelector"
                node-key="id"
                :data="computedTree"
                :teleported="false"
                :props="dsSelectProps"
                :render-after-expand="false"
                filterable
                @node-click="dsClick"
                :filter-node-method="filterNode"
                empty-text="暂无相关数据"
              >
                <template #default="{ node, data }">
                  <div
                    class="tree-row-item"
                    :class="{ dark: themes === 'dark', active: _modelValue === data.id }"
                  >
                    <div class="m-icon">
                      <el-icon v-if="!data.leaf">
                        <Icon name="dv-folder" />
                      </el-icon>
                      <el-icon v-if="data.leaf">
                        <Icon name="icon_dataset" />
                      </el-icon>
                    </div>
                    <el-tooltip
                      effect="dark"
                      :content="node.label"
                      placement="top"
                      :enterable="false"
                    >
                      {{ node.label }}
                    </el-tooltip>

                    <el-icon class="checked-item" v-if="_modelValue === data.id">
                      <Icon name="icon_done_outlined" />
                    </el-icon>
                  </div>
                </template>
              </el-tree>
            </el-scrollbar>
          </el-main>
          <el-footer>
            <el-button type="primary" :icon="Plus" link class="add-btn" @click="addDataset">
              新建数据集
            </el-button>
          </el-footer>
        </el-container>
      </template>
    </el-popover>
  </div>
</template>

<style scoped lang="less">
:deep(.ed-input__wrapper) {
  cursor: pointer;
  padding: 1px 11px;

  .ed-input__inner {
    cursor: pointer;
    font-size: 12px;
  }
}
:deep(.ed-form-item) {
  margin-bottom: 0;
}
:deep(.ed-form-item.is-error .ed-input__wrapper) {
  input {
    color: var(--ed-color-danger);
  }
}
</style>

<style lang="less">
.input-arrow-icon {
  font-size: 16px;
  transform: rotateZ(0);
  transition: transform var(--ed-transition-duration);

  &.reverse {
    transform: rotateZ(-180deg);
  }
}
.customDatasetSelect {
  --ed-popover-padding: 0;
  max-height: 356px;

  .ed-container {
    max-height: 356px;

    .ed-header {
      --ed-header-height: 68px;
      --ed-header-padding: 0 11px;

      .m-title {
        width: 100%;
        display: flex;
        flex-direction: row;
        justify-content: space-between;
        align-items: center;
        height: 28px;
        padding-top: 8px;

        color: #1f2329;
        font-size: 12px;
        font-style: normal;
        font-weight: 500;
        line-height: 20px;

        .refresh-btn {
          font-size: 12px;
          font-weight: 400;
          cursor: pointer;
          min-width: 30px;
          min-width: 30px;
        }

        &.dark {
          color: #ebebeb;
        }
      }

      .ed-input {
        padding: 4px 0;
        font-size: 12px;
      }
    }

    .ed-footer {
      --ed-footer-height: 36px;
      --ed-footer-padding: 0 11px;
      border-top: rgba(31, 35, 41, 0.15) 1px solid;

      display: flex;
      flex-direction: row;
      align-items: center;

      .add-btn {
        font-size: 12px;
        font-weight: 400;
      }
    }

    .ed-main {
      --ed-main-padding: 0;
      overflow-x: hidden;

      .empty-info {
        color: #646a73;
        font-size: 12px;
        font-style: normal;
        font-weight: 400;
        line-height: 20px;
      }

      .m-loading {
        width: 100%;
        height: 80px;
        .ed-loading-mask {
          background-color: transparent;
        }
      }

      &.dark {
        background-color: #303133;
        color: #ebebeb;

        .empty-info {
          color: #a6a6a6;
        }
      }

      .ed-tree__empty-block {
        position: unset;
        color: #646a73;
        font-size: 12px;
        font-style: normal;
        font-weight: 400;
        line-height: 20px;
        min-height: 20px;
        text-align: start;
        .ed-tree__empty-text {
          position: inherit;
          transform: inherit;
          color: inherit;
          font-size: inherit;
        }
      }

      .ed-tree {
        &.dark {
          color: #ebebeb;
          background-color: #303133;

          .ed-tree__empty-block {
            color: #a6a6a6;
          }

          .ed-tree-node__expand-icon {
            color: #a6a6a6;
            &.is-leaf {
              color: transparent;
            }
          }

          .ed-tree-node__content:hover {
            background: rgba(235, 235, 235, 0.1);
          }
          .ed-tree-node:not(.is-effect):focus > .ed-tree-node__content {
            background: rgba(235, 235, 235, 0.1);
          }
        }
      }

      .tree-row-item {
        display: block;
        overflow-x: hidden;
        text-overflow: ellipsis;
        word-break: break-all;
        white-space: nowrap;
        font-size: 12px;
        font-style: normal;
        font-weight: 400;
        line-height: 20px;

        &::-webkit-scrollbar {
          display: none;
        }

        .m-icon {
          margin-right: 4px;
          font-size: 16px;
          height: 20px;
          display: inline-block;
          vertical-align: bottom;
        }

        &.active {
          color: #3370ff;
          padding-right: 30px;
        }
        .checked-item {
          position: absolute;
          right: 10px;
          padding-top: 2px;
          color: #3370ff;
          font-size: 16px;
        }
      }
    }
  }
}
</style>
