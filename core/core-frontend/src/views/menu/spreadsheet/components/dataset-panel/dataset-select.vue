<script lang="ts" setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { getDatasetTree, getDsDetailsWithPerm } from '@/api/dataset'
import type { Field } from '@/api/chart'
import type { Tree } from '@/views/visualized/data/dataset/form/CreatDsGroup.vue'
import { Search, ArrowDown, ArrowUp, Close } from '@element-plus/icons-vue'
import { ElFormItem, FormInstance } from 'element-plus-secondary'
import { useAppStoreWithOut } from '@/store/modules/app'
import dvFolder from '@/assets/svg/dv-folder.svg'
import icon_dataset from '@/assets/svg/icon_dataset.svg'
import icon_dataset_outlined from '@/assets/svg/icon_dataset_outlined.svg'
import icon_done_outlined from '@/assets/svg/icon_done_outlined.svg'

const { t } = useI18n()
const appStore = useAppStoreWithOut()
const isDataEaseBi = computed(() => appStore.getIsDataEaseBi || appStore.getIsIframe)

interface Props {
  modelValue?: string | number
  disabled?: boolean
  popoverWidth?: number
  clearable?: boolean
  clearConfirmText?: string
  showDatasetIcon?: boolean
  showCreateDataset?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
  popoverWidth: 280,
  clearable: false,
  clearConfirmText: '',
  showDatasetIcon: false,
  showCreateDataset: true
})

const emit = defineEmits<{
  'update:modelValue': [value: string | number]
  'datasetChange': [datasetId: string | number]
  'fieldsLoaded': [fields: { dimensions: Field[]; quotas: Field[] }]
  'datasetNodeChange': [node?: Tree]
  'addDataset': []
  clear: []
}>()

const loading = ref(false)
const datasetTree = ref<Tree[]>([])
const datasetSelectorPopover = ref()
const datasetSelector = ref()
const searchStr = ref('')
const _popoverShow = ref(false)

// 选中的值
const selectedValue = computed({
  get() {
    return props.modelValue
  },
  set(val) {
    emit('update:modelValue', val)
    if (val) {
      emit('datasetChange', val)
    }
  }
})

const normalizeDatasetId = (value?: string | number) =>
  value === undefined || value === null ? '' : String(value)

// 扁平化树获取所有叶子节点
const flattedTree = computed(() => {
  const result: Tree[] = []
  const flat = (nodes: Tree[]) => {
    nodes.forEach(node => {
      if (node.leaf) {
        result.push(node)
      }
      if (node.children?.length) {
        flat(node.children)
      }
    })
  }
  flat(datasetTree.value)
  return result
})

// 当前选中的节点
const selectedNode = computed(() => {
  const datasetId = normalizeDatasetId(selectedValue.value)
  return flattedTree.value.find(node => normalizeDatasetId(node.id) === datasetId)
})

// 显示的名称
const selectedNodeName = computed(() => {
  return selectedNode.value?.name || ''
})

// 表单数据用于验证
const form = computed(() => ({
  name: selectedNodeName.value
}))

const formRef = ref<FormInstance>()

// 树节点属性配置
const dsSelectProps = {
  label: 'name',
  children: 'children',
  value: 'id',
  isLeaf: (node: Tree) => !node.children?.length
}

// 显示树的条件
const showTree = computed(() => {
  return datasetTree.value && datasetTree.value.length > 0 && !loading.value
})

// 加载数据集树
const loadDatasetTree = async () => {
  loading.value = true
  try {
    const res = await getDatasetTree({})
    datasetTree.value = res?.[0]?.children || []
    emitDatasetNodeChange()
  } catch (error) {
    datasetTree.value = []
    emitDatasetNodeChange()
  } finally {
    loading.value = false
  }
}

// 搜索过滤
const filterNode = (value: string, data: Tree) => {
  if (!value) return true
  return data.name?.includes(value)
}

watch(searchStr, (val) => {
  datasetSelector.value?.filter(val)
})

watch(
  () => props.modelValue,
  () => emitDatasetNodeChange(),
  { flush: 'post' }
)

const emitDatasetNodeChange = (datasetId: string | number = selectedValue.value) => {
  const normalizedId = normalizeDatasetId(datasetId)
  const node = flattedTree.value.find(item => normalizeDatasetId(item.id) === normalizedId)
  emit('datasetNodeChange', node)
}

// 加载数据集字段
const loadDatasetFields = async (datasetId: string | number) => {
  try {
    const res = await getDsDetailsWithPerm([datasetId])
    if (res && res.length > 0) {
      const fields = res[0].fields || {}
      emit('fieldsLoaded', {
        dimensions: fields.dimensionList || [],
        quotas: fields.quotaList || []
      })
    } else {
      emit('fieldsLoaded', { dimensions: [], quotas: [] })
    }
  } catch (error) {
    // 获取失败，清空字段
    emit('fieldsLoaded', { dimensions: [], quotas: [] })
  }
}

// 处理节点点击
const dsClick = (data: Tree) => {
  if (data.leaf) {
    // 重复点击已选中的数据集，只关闭弹窗不执行任何操作
    if (normalizeDatasetId(selectedValue.value) === normalizeDatasetId(data.id)) {
      datasetSelectorPopover.value?.hide()
      return
    }
    // 选择新数据集
    selectedValue.value = data.id
    emit('datasetNodeChange', data)
    // 加载新数据集的字段
    loadDatasetFields(data.id)
    datasetSelectorPopover.value?.hide()
  }
}

// Popover 显示/隐藏
const onPopoverShow = () => {
  _popoverShow.value = true
}

const onPopoverHide = () => {
  _popoverShow.value = false
}

// 刷新
const refresh = () => {
  loadDatasetTree()
}

// 新建数据集
const addDataset = () => {
  datasetSelectorPopover.value?.hide()
  emit('addDataset')
}

const open = () => {
  if (!props.disabled) {
    datasetSelectorPopover.value?.show?.()
  }
}

const clearSelection = () => {
  datasetSelectorPopover.value?.hide?.()
  emit('clear')
}

onMounted(() => {
  loadDatasetTree()
})

defineExpose({
  open,
  refresh,
  loadDatasetTree,
  loadDatasetFields
})
</script>

<template>
  <div class="dataset-select">
    <el-popover
      ref="datasetSelectorPopover"
      trigger="click"
      placement="bottom-start"
      :width="props.popoverWidth"
      popper-class="spreadsheet-dataset-select-popover"
      :show-arrow="false"
      @show="onPopoverShow"
      @hide="onPopoverHide"
      :disabled="props.disabled"
      :offset="4"
    >
      <template #reference>
        <el-form ref="formRef" :model="form">
          <el-form-item>
            <div class="dataset-select-trigger">
              <div class="trigger-content">
                <el-icon v-if="props.showDatasetIcon" class="trigger-dataset-icon">
                  <Icon>
                    <icon_dataset_outlined class="svg-icon" />
                  </Icon>
                </el-icon>
                <span v-if="selectedNodeName" class="trigger-text">{{ selectedNodeName }}</span>
                <span v-else class="trigger-placeholder">
                  {{ t('spreadsheet.dataset_replacement.select_dataset') }}
                </span>
              </div>
              <el-icon
                v-show="!props.disabled"
                class="trigger-arrow"
                :class="{
                  reverse: _popoverShow,
                  'has-clear': props.clearable && Boolean(selectedNodeName)
                }"
              >
                <ArrowUp v-if="_popoverShow" />
                <ArrowDown v-else />
              </el-icon>
              <el-popconfirm
                v-if="props.clearable && selectedNodeName"
                :title="props.clearConfirmText"
                :confirm-button-text="t('commons.message_box.confirm')"
                :cancel-button-text="t('commons.message_box.cancel')"
                width="220"
                @confirm="clearSelection"
              >
                <template #reference>
                  <el-icon class="trigger-clear" @click.stop>
                    <Close />
                  </el-icon>
                </template>
              </el-popconfirm>
            </div>
          </el-form-item>
        </el-form>
      </template>
      <template #default>
        <el-container
          class="dataset-select-container"
          :style="{ width: `${props.popoverWidth}px` }"
        >
          <el-header class="dataset-select-header">
            <div class="header-title">
              <span>{{ t('spreadsheet.dataset_replacement.dataset') }}</span>
              <el-button type="primary" link class="refresh-btn" @click="refresh">
                {{ t('spreadsheet.dataset_replacement.refresh') }}
              </el-button>
            </div>
            <el-input
              v-model="searchStr"
              :placeholder="t('spreadsheet.dataset_replacement.search')"
              :prefix-icon="Search"
              clearable
              class="dataset-search-input"
            />
          </el-header>
          <el-main class="dataset-select-main">
            <el-scrollbar max-height="252px">
              <div v-if="loading" v-loading="loading" class="dataset-loading"></div>
              <el-tree
                v-if="showTree"
                ref="datasetSelector"
                node-key="id"
                :data="datasetTree"
                :props="dsSelectProps"
                :render-after-expand="false"
                :filter-node-method="filterNode"
                @node-click="dsClick"
                :empty-text="t('spreadsheet.dataset_replacement.no_related_data')"
                class="dataset-tree"
              >
                <template #default="{ node, data }">
                  <div
                    class="tree-row-item"
                    :class="{ active: selectedValue === data.id }"
                  >
                    <div class="m-icon">
                      <el-icon v-if="!data.leaf">
                        <Icon name="dv-folder">
                          <dvFolder class="svg-icon" />
                        </Icon>
                      </el-icon>
                      <el-icon v-if="data.leaf">
                        <Icon name="icon_dataset">
                          <icon_dataset class="svg-icon dataset-icon" />
                        </Icon>
                      </el-icon>
                    </div>
                    <span class="node-label">{{ node.label }}</span>
                    <el-icon v-if="selectedValue === data.id" class="checked-item">
                      <Icon name="icon_done_outlined">
                        <icon_done_outlined class="svg-icon" />
                      </Icon>
                    </el-icon>
                  </div>
                </template>
              </el-tree>
            </el-scrollbar>
          </el-main>
          <el-footer
            v-if="!isDataEaseBi && props.showCreateDataset"
            class="dataset-select-footer"
          >
            <div class="footer-container">
              <el-button type="primary" link class="add-btn" @click="addDataset">
                <el-icon><Plus /></el-icon>
                <span>{{ t('spreadsheet.dataset_replacement.create_dataset') }}</span>
              </el-button>
            </div>
          </el-footer>
        </el-container>
      </template>
    </el-popover>
  </div>
</template>

<style lang="less" scoped>
.dataset-select {
  width: 100%;

  :deep(.ed-form-item) {
    margin-bottom: 0;
  }

  :deep(.ed-input__wrapper) {
    cursor: pointer;

    .ed-input__inner {
      cursor: pointer;
    }
  }

  .dataset-select-trigger {
    width: 100%;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 12px;
    background: #fff;
    border: 1px solid #dcdfe6;
    border-radius: 6px;
    cursor: pointer;
    transition: border-color 0.2s;

    &:hover {
      border-color: #c0c4cc;
    }

    .trigger-content {
      min-width: 0;
      flex: 1;
      display: flex;
      align-items: center;
      gap: 8px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;

      .trigger-dataset-icon {
        width: 16px;
        height: 16px;
        flex-shrink: 0;
        color: #14c0ff;

        .svg-icon {
          width: 16px;
          height: 16px;
        }
      }

      .trigger-text {
        min-width: 0;
        overflow: hidden;
        text-overflow: ellipsis;
        font-size: 12px;
        color: #1f2329;
      }

      .trigger-placeholder {
        font-size: 12px;
        color: #8f959e;
      }
    }

    .trigger-arrow {
      font-size: 14px;
      color: #8f959e;
      transition: transform var(--ed-transition-duration);
      flex-shrink: 0;
      margin-left: 4px;

      &.reverse {
        transform: rotateZ(-180deg);
      }
    }

    .trigger-clear {
      display: none;
      flex-shrink: 0;
      margin-left: 4px;
      color: #646a73;
      font-size: 14px;

      &:hover {
        color: #1f2329;
      }
    }

    &:hover {
      .trigger-arrow.has-clear {
        display: none;
      }

      .trigger-clear {
        display: inline-flex;
      }
    }
  }

}
</style>

<style lang="less">
// Popover 样式 - 非 scoped 以影响下拉内容
.spreadsheet-dataset-select-popover {
  --ed-popover-padding: 0 !important;
  padding: 0 !important;

  .ed-popover__content {
    padding: 0 !important;
  }

  .dataset-select-container {
    box-sizing: border-box;
    width: 280px;
    background: #fff;
    border: 1px solid #e5e7eb;
    border-radius: 6px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    overflow: hidden;

    .dataset-select-header {
      --ed-header-height: auto;
      --ed-header-padding: 0;
      padding: 12px 12px 8px;
      border-bottom: none;

      .header-title {
        width: 100%;
        display: flex;
        flex-direction: row;
        justify-content: space-between;
        align-items: center;
        height: 20px;
        margin-bottom: 8px;
        font-size: 12px;
        font-weight: 500;
        color: #1f2329;

        .refresh-btn {
          font-size: 12px;
          font-weight: 400;
          cursor: pointer;
          min-width: 30px;
          padding: 0;
          color: #3370ff;

          &:hover {
            color: #285fdb;
          }
        }
      }

      .dataset-search-input {
        width: 100%;

        :deep(.ed-input__wrapper) {
          background-color: #fff;
          border: 1px solid #dcdfe6;
          border-radius: 6px;
          padding: 0 11px;
          height: 30px;

          .ed-input__inner {
            font-size: 12px;
          }

          &.is-focus {
            border-color: #3370ff;
          }
        }

        :deep(.ed-input__prefix) {
          color: #8f959e;
          font-size: 14px;
          margin-right: 6px;
        }

        :deep(.ed-input__suffix) {
          color: #8f959e;
          font-size: 14px;
        }
      }
    }

    .dataset-select-main {
      --ed-main-padding: 0;
      padding: 0;
      overflow: visible;
      height: auto;

      .dataset-loading {
        width: 100%;
        height: 60px;

        .ed-loading-mask {
          background-color: transparent;
        }
      }

      .ed-scrollbar {
        .ed-scrollbar__bar {
          right: 2px;

          .ed-scrollbar__thumb {
            background-color: rgba(0, 0, 0, 0.15);
            border-radius: 3px;

            &:hover {
              background-color: rgba(0, 0, 0, 0.25);
            }
          }
        }
      }

      .dataset-tree {
        .ed-tree-node__content {
          height: 28px;
          padding: 0 12px;
          margin: 2px 0;

          &:hover {
            background-color: #f5f7fa;
          }
        }

        .ed-tree-node__expand-icon {
          color: #8f959e;
          font-size: 12px;
          margin-right: 4px;

          &.is-leaf {
            color: transparent;
            margin-right: 4px;
          }
        }

        .tree-row-item {
          display: flex;
          align-items: center;
          width: 100%;
          height: 100%;
          overflow: hidden;
          font-size: 12px;
          font-weight: 400;
          line-height: 20px;
          color: #1f2329;

          .m-icon {
            display: flex;
            align-items: center;
            justify-content: center;
            margin-right: 8px;
            flex-shrink: 0;

            .ed-icon {
              font-size: 16px;
            }

            .svg-icon {
              width: 16px;
              height: 16px;

              &.dataset-icon {
                color: #3370ff;
              }
            }
          }

          .node-label {
            flex: 1;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          &.active {
            color: #3370ff;
            background-color: transparent;

            .ed-tree-node__content {
              background-color: transparent;
            }
          }

          .checked-item {
            margin-left: auto;
            color: #3370ff;
            font-size: 14px;
            flex-shrink: 0;
          }
        }
      }

      .ed-tree__empty-block {
        position: relative;
        min-height: 40px;
        color: #646a73;
        font-size: 12px;
        text-align: center;
        padding: 10px;
      }
    }

    .dataset-select-footer {
      --ed-footer-height: 40px;
      --ed-footer-padding: 0 12px;
      border-top: 1px solid #e5e7eb;
      padding: 0 12px;
      height: 40px;

      .footer-container {
        height: 100%;
        display: flex;
        align-items: center;
      }

      .add-btn {
        font-size: 12px;
        font-weight: 400;
        padding: 4px;
        display: flex;
        align-items: center;
        gap: 4px;
        color: #3370ff;

        &:hover {
          color: var(--ed-color-primary);
          border-color: transparent;
          background-color: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1));
        }

        .ed-icon {
          font-size: 14px;
        }
      }
    }
  }
}
</style>
