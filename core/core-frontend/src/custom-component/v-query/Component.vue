<script lang="ts" setup>
import eventBus from '@/utils/eventBus'
import QueryConditionConfiguration from './QueryConditionConfiguration.vue'
import type { ComponentInfo } from '@/api/chart'
import { onBeforeUnmount, reactive, ref, toRefs, watch, computed, onMounted } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { useI18n } from '@/hooks/web/useI18n'
import { guid } from '@/views/visualized/data/dataset/form/util.js'
// import { cloneDeep } from 'lodash-es'
import { useEmitt } from '@/hooks/web/useEmitt'
import Select from './Select.vue'
import Time from './Time.vue'
const props = defineProps({
  view: {
    type: Object,
    default() {
      return {
        customStyle: ''
      }
    }
  },
  element: {
    type: Object,
    default() {
      return {
        id: null,
        propValue: []
      }
    }
  },
  showPosition: {
    type: String,
    required: true,
    default: ''
  }
})
const { element, view } = toRefs(props)
const { t } = useI18n()
const dvMainStore = dvMainStoreWithOut()
const { curComponent, canvasViewInfo } = storeToRefs(dvMainStore)
const canEdit = ref(false)
const queryConfig = ref()
const defaultStyle = {
  border: '',
  background: '',
  text: '',
  layout: 'horizontal',
  btnList: ['sure'],
  titleShow: false,
  title: ''
}
const customStyle = reactive({ ...defaultStyle })

const curComponentView = computed(() => {
  return (canvasViewInfo.value[element.value.id] || {}).customStyle
})

const filterTypeCom = (displayType: string) => {
  return ['1', '7'].includes(displayType) ? Time : Select
}

watch(
  () => view.value,
  val => {
    if (!val?.customStyle?.component) return
    const {
      show,
      borderShow,
      borderColor,
      bgColorShow,
      btnList,
      bgColor,
      layout,
      titleShow,
      title
    } = val?.customStyle?.component
    if (!show) {
      Object.assign(customStyle, { ...defaultStyle })
      return
    }
    customStyle.background = bgColorShow ? bgColor || '' : ''
    customStyle.border = borderShow ? borderColor || '' : ''
    customStyle.btnList = [...btnList]
    customStyle.layout = layout
    customStyle.titleShow = titleShow
    customStyle.title = title
  },
  {
    deep: true,
    immediate: true
  }
)

watch(
  () => curComponentView.value,
  val => {
    if (!val) return
    const {
      show,
      borderShow,
      borderColor,
      bgColorShow,
      btnList,
      bgColor,
      layout,
      titleShow,
      title
    } = val?.component
    if (!show) {
      Object.assign(customStyle, { ...defaultStyle })
      return
    }
    customStyle.background = bgColorShow ? bgColor || '' : ''
    customStyle.border = borderShow ? borderColor || '' : ''
    customStyle.btnList = [...btnList]
    customStyle.layout = layout
    customStyle.titleShow = titleShow
    customStyle.title = title
  },
  {
    deep: true,
    immediate: true
  }
)
const list = ref([])

watch(
  () => props.element.propValue,
  () => {
    list.value = [...props.element.propValue]
  },
  {
    immediate: true
  }
)
const onComponentClick = () => {
  if (curComponent.value.id !== element.value.id) {
    canEdit.value = false
  }
}

const { emitter } = useEmitt()

onBeforeUnmount(() => {
  emitter.off(`addQueryCriteria${element.value.id}`)
  emitter.off(`editQueryCriteria${element.value.id}`)
  eventBus.off('componentClick', onComponentClick)
})

onMounted(() => {
  emitter.on(`addQueryCriteria${element.value.id}`, addCriteriaConfigOut)
  emitter.on(`editQueryCriteria${element.value.id}`, editQueryCriteria)
})

const dragover = () => {
  // do
}

const infoFormat = (obj: ComponentInfo) => {
  const { id, name, deType, type, datasetId } = obj
  const base = {
    id: guid(),
    name,
    field: {
      id,
      type,
      name,
      deType
    },
    operator: deType === 1 ? 'between' : 'eq',
    defaultValue: '',
    selectValue: '',
    optionValueSource: 0,
    valueSource: [],
    dataset: {
      id: datasetId,
      name: '',
      fields: []
    },
    visible: true,
    defaultValueCheck: false,
    multiple: false,
    displayType: '0',
    checkedFields: [],
    checkedFieldsMap: {}
  }
  if (deType === 1) {
    return base
  }
  return { ...base, parameters: [], parametersCheck: false, parametersList: [] }
}

const drop = e => {
  const componentInfo: ComponentInfo = JSON.parse(e.dataTransfer.getData('dimension') || '{}')
  if (!componentInfo.id) return
  addCriteriaConfigOut()
}

const editeQueryConfig = (queryId: string) => {
  queryConfig.value.setCondition(queryId)
}

const addQueryCriteria = () => {
  const componentInfo: ComponentInfo = {
    id: '',
    name: '未命名',
    deType: 0,
    type: 'VARCHAR',
    datasetId: ''
  }
  list.value.push(infoFormat(componentInfo))
  element.value.propValue = [...list.value]
  editeQueryConfig(list.value[list.value.length - 1].id)
}

const addQueryCriteriaConfig = () => {
  const componentInfo: ComponentInfo = {
    id: '',
    name: '未命名',
    deType: 0,
    type: 'VARCHAR',
    datasetId: ''
  }
  return infoFormat(componentInfo)
}

const editQueryCriteria = () => {
  if (!list.value.length) {
    addQueryCriteria()
    return
  }
  editeQueryConfig(list.value[0].id)
}

const addCriteriaConfigOut = () => {
  queryConfig.value.setConditionOut()
}

const delQueryConfig = index => {
  list.value.splice(index, 1)
  element.value.propValue = [...list.value]
}

const resetData = () => {
  ;(list.value || []).reduce((pre, next) => {
    if (!next.defaultValueCheck) {
      return pre
    }
    next.selectValue = Array.isArray(next.defaultValue) ? [...next.defaultValue] : next.defaultValue
    const keyList = Object.entries(next.checkedFieldsMap)
      .filter(ele => next.checkedFields.includes(ele[0]))
      .filter(ele => !!ele[1])
      .map(ele => ele[0])
    pre = [...new Set([...keyList, ...pre])]
    return pre
  }, [])
}

const clearData = () => {
  ;(list.value || []).reduce((pre, next) => {
    next.selectValue = next.multiple ? [] : ''
    const keyList = Object.entries(next.checkedFieldsMap)
      .filter(ele => next.checkedFields.includes(ele[0]))
      .filter(ele => !!ele[1])
      .map(ele => ele[0])
    pre = [...new Set([...keyList, ...pre])]
    return pre
  }, [])
}
const listVisible = computed(() => {
  return list.value.filter(itx => itx.visible)
})

const addCriteriaConfig = () => {
  queryConfig.value.setConditionInit(queryConfig.value.addCriteriaConfig())
}

const queryData = () => {
  const emitterList = (element.value.propValue || []).reduce((pre, next) => {
    const keyList = Object.entries(next.checkedFieldsMap)
      .filter(ele => next.checkedFields.includes(ele[0]))
      .filter(ele => !!ele[1])
      .map(ele => ele[0])
    pre = [...new Set([...keyList, ...pre])]
    return pre
  }, [])
  if (!emitterList.length) return

  emitterList.forEach(ele => {
    emitter.emit(`query-data-${ele}`)
  })
}
</script>

<template>
  <div class="v-query-container">
    <p v-if="customStyle.titleShow" class="title">{{ customStyle.title }}</p>
    <div
      :class="['v-query', customStyle.layout]"
      @dragover.prevent.stop="dragover"
      @drop.prevent.stop="drop"
    >
      <div v-if="!listVisible.length" class="no-list-label flex-align-center">
        <div class="container flex-align-center">
          将右侧的字段拖拽到这里 或 点击
          <el-button @click="addCriteriaConfig" text> 添加查询条件 </el-button>
        </div>
      </div>
      <div class="query-fields-container">
        <div class="query-item" :key="ele.id" v-for="(ele, index) in listVisible">
          <div class="query-field">
            <div class="label">
              <div class="label-wrapper">
                <div class="label-wrapper-text">{{ ele.name }}</div>
              </div>
              <div class="label-wrapper-tooltip" v-if="showPosition !== 'preview'">
                <el-tooltip effect="dark" content="设置过滤条件" placement="top">
                  <el-icon @click="editeQueryConfig(ele.id)">
                    <Icon name="edit-in"></Icon>
                  </el-icon>
                </el-tooltip>
                <el-tooltip effect="dark" content="删除条件" placement="top">
                  <el-icon @click="delQueryConfig(index)">
                    <Icon name="icon_delete-trash_outlined"></Icon>
                  </el-icon>
                </el-tooltip>
              </div>
            </div>
            <div class="query-select">
              <component
                :config="ele"
                :is-config="false"
                :customStyle="customStyle"
                :is="filterTypeCom(ele.displayType)"
              ></component>
            </div>
          </div>
        </div>
        <div
          class="query-button"
          v-if="!!listVisible.length && customStyle.layout === 'horizontal'"
        >
          <el-button @click.stop="resetData" v-if="customStyle.btnList.includes('reset')" secondary>
            {{ t('chart.reset') }}
          </el-button>
          <el-button @click.stop="clearData" v-if="customStyle.btnList.includes('clear')" secondary>
            {{ t('commons.clear') }}
          </el-button>
          <el-button
            @click.stop="queryData"
            v-if="customStyle.btnList.includes('sure')"
            type="primary"
          >
            {{ t('commons.adv_search.search') }}
          </el-button>
        </div>
      </div>
      <div class="query-button" v-if="!!listVisible.length && customStyle.layout === 'vertical'">
        <el-button @click.stop="resetData" v-if="customStyle.btnList.includes('reset')" secondary>
          {{ t('chart.reset') }}
        </el-button>
        <el-button @click.stop="clearData" v-if="customStyle.btnList.includes('clear')" secondary>
          {{ t('commons.clear') }}
        </el-button>
        <el-button
          @click.stop="queryData"
          v-if="customStyle.btnList.includes('sure')"
          type="primary"
        >
          {{ t('commons.adv_search.search') }}
        </el-button>
      </div>
    </div>
  </div>
  <Teleport to="body">
    <QueryConditionConfiguration
      :add-query-criteria-config="addQueryCriteriaConfig"
      :query-element="element"
      ref="queryConfig"
    ></QueryConditionConfiguration>
  </Teleport>
</template>

<style lang="less" scoped>
.v-query-container {
  width: 100%;
  height: 100%;
  padding: 16px;
  overflow: auto;
  position: relative;

  .no-list-label {
    width: 100%;
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    z-index: 0;
    .container {
      width: 100%;
      justify-content: center;
      color: #646a73;
      text-align: center;
      font-family: PingFang SC;
      font-size: 16px;
      font-style: normal;
      font-weight: 400;
      line-height: 24px;
      .ed-button {
        font-size: 16px;
        font-style: normal;
        font-weight: 400;
        line-height: 24px;
      }
    }
  }
  .title {
    color: #1f2329;
    font-feature-settings: 'clig' off, 'liga' off;
    font-family: PingFang SC;
    font-size: 14px;
    font-style: normal;
    font-weight: 500;
    line-height: 22px;
    letter-spacing: -0.1px;
  }
}
.v-query {
  width: 100%;
  height: 100%;
  line-height: 1.5;
  color: rgba(0, 0, 0, 0.87);
  align-items: center;
  position: relative;
  display: flex;
  z-index: 3;
  margin: auto 0;
  .query-fields-container {
    display: flex;
    flex-wrap: wrap;
    width: 100%;
  }
  .query-item {
    font-size: 12px;
    position: relative;
    line-height: 28px;
    margin: 5px 6px 5px 0;
    max-width: 100%;
    min-width: 60px;
    .query-field {
      position: relative;
      .label {
        display: flex;
        font-size: 12px;
        overflow: hidden;
        padding-right: 6px;
        color: #1f2329;

        .label-wrapper {
          visibility: visible;
          pointer-events: auto;
          cursor: auto;
          font-size: 12px;
          line-height: 16px;
          color: #575757;
          box-sizing: border-box;
          margin: 0;
          padding: 0;
          display: flex;
          flex: 1 1 0;
          overflow: hidden;
        }
        .label-wrapper-text {
          cursor: pointer;
          flex: 0 1 auto;
          max-width: 100%;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        .label-wrapper-tooltip {
          align-items: center;
          background: #fff;
          border-radius: 2px;
          display: none;
          flex: 0 0 auto;
          height: 16px;
          line-height: 16px;
          color: #575757;
        }
      }

      .query-select {
        display: flex;
        flex-wrap: wrap;
        margin-top: -5px;
        line-height: 28px;
        :deep(.ed-date-editor--datetime .ed-input__wrapper) {
          width: 100%;
        }

        :deep(.ed-select-v2) {
          min-width: 170px;
        }
      }
    }
  }
  .query-button {
    align-self: flex-end;
    line-height: 28px;
    margin: auto 0 5px auto;
    z-index: 0;
    button {
      margin: 8px 0 0 8px;
      min-width: 48px;
    }
  }

  &.horizontal {
    .query-fields-container {
      .query-field {
        padding-top: 18px;

        &:hover {
          .label-wrapper-tooltip {
            display: inline-flex !important;
            cursor: pointer;
          }
        }
        .label {
          align-items: center;
          height: 16px;
          left: 0;
          line-height: 16px;
          max-width: 100%;
          position: absolute;
          right: 0;
          top: 0;
        }
      }
    }
  }

  &.vertical {
    line-height: 1.5;
    color: rgba(0, 0, 0, 0.87);
    align-items: center;
    display: flex;
    margin: auto 0;
    .query-fields-container {
      align-items: flex-start;
      flex-direction: column;

      .query-field {
        align-items: flex-start;
        display: flex;
        .label {
          visibility: visible;
          pointer-events: auto;
          cursor: auto;
          line-height: 28px;
          box-sizing: border-box;
          flex: 0 0 auto;
          .label-wrapper-tooltip {
            line-height: 16px;
            box-shadow: 0 0 4px #777;
            position: absolute;
            right: 0;
            top: -5px;
            z-index: 11;
          }
        }
        &:hover {
          .label-wrapper-tooltip {
            display: block;
            cursor: pointer;
          }
        }
      }
    }

    .query-button {
      align-self: flex-end;
      line-height: 28px;
      z-index: 0;
      flex: 0 0 56px;
      margin: auto 0;
    }
  }
}
</style>
<style lang="less">
.load-select {
  .ed-select-dropdown__list {
    & > div {
      &:nth-child(1) {
        .ed-radio__inner::after {
          display: none !important;
        }
      }
    }
  }
  .ed-select-dropdown {
    &:nth-child(1) {
      .ed-checkbox__inner::after {
        display: none !important;
      }
    }
  }
}
</style>
