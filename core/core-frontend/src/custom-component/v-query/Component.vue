<script lang="ts" setup>
import eventBus from '@/utils/eventBus'
import QueryConditionConfiguration from './QueryConditionConfiguration.vue'
import { type Field } from '@/api/chart'
import { onBeforeUnmount, reactive, ref, toRefs, watch, computed } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { useI18n } from '@/hooks/web/useI18n'
import { guid } from '@/views/visualized/data/dataset/form/util.js'
import { cloneDeep } from 'lodash-es'
import Select from './Select.vue'
import Time from './Time.vue'
const props = defineProps({
  propValue: {
    type: String,
    required: true,
    default: ''
  },
  element: {
    type: Object,
    default() {
      return {
        id: null,
        propValue: ''
      }
    }
  }
})
const { element } = toRefs(props)
const { t } = useI18n()
const dvMainStore = dvMainStoreWithOut()
const { curComponent, canvasViewInfo } = storeToRefs(dvMainStore)
const canEdit = ref(false)
const queryConfig = ref()
const defaultStyle = {
  border: '',
  background: '',
  text: '',
  borderWidth: 1,
  btnList: []
}
const customStyle = reactive({ ...defaultStyle })

const curComponentView = computed(() => {
  return (canvasViewInfo.value[element.value.id] || {}).customStyle
})

const filterTypeCom = (deType: number) => {
  return deType === 1 ? Time : Select
}

watch(
  () => curComponentView.value,
  val => {
    const { show, borderShow, borderColor, borderWidth, bgColorShow, btnList, bgColor } =
      val.component
    if (!show) {
      Object.assign(customStyle, { ...defaultStyle })
      return
    }
    customStyle.background = bgColorShow ? bgColor || '' : ''
    customStyle.border = borderShow ? borderColor || '' : ''
    customStyle.btnList = [...btnList]
    customStyle.borderWidth = borderWidth
  },
  {
    deep: true,
    immediate: true
  }
)

const multipleChange = ele => {
  ele.selectValue = ele.multiple ? [] : undefined
}

const list = ref([])

watch(
  () => props.element.propValue,
  () => {
    ;(props.element.propValue || []).forEach(ele => {
      multipleChange(ele)
    })
    list.value = cloneDeep(props.element.propValue || [])
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

onBeforeUnmount(() => {
  eventBus.off('componentClick', onComponentClick)
})

const dragover = () => {
  // const componentInfo = e.dataTransfer.getData('dimosion')
  // console.log('dragover', e, componentInfo)
}

const infoFormat = (obj: Field & { datasetId: string }) => {
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
    defaultValue: '',
    optionValueSource: 1,
    valueSource: [],
    dataset: {
      id: datasetId,
      name: '',
      fields: []
    },
    visible: true,
    defaultValueCheck: false,
    multiple: false,
    checkedFieldsMap: {}
  }
  if (deType === 1) {
    return base
  }
  return { ...base, options: [], parameters: [], parametersCheck: false }
}

const drop = e => {
  const componentInfo: Field & { datasetId: string } = JSON.parse(
    e.dataTransfer.getData('dimension') || '{}'
  )
  if (!componentInfo.id) return
  list.value.push(infoFormat(componentInfo))
  element.value.propValue = cloneDeep(list.value)
}

const editeQueryConfig = (queryId: string) => {
  queryConfig.value.init(element.value.id, queryId)
}

const delQueryConfig = index => {
  list.value.splice(index, 1)
  element.value.propValue = cloneDeep(list.value)
}
</script>

<template>
  <div class="v-query" @dragover.prevent.stop="dragover" @drop.prevent.stop="drop">
    <div class="query-item vertical" :key="ele.id" v-for="(ele, index) in list">
      <div class="query-field">
        <div class="label">
          <div class="label-wrapper">
            <div class="label-wrapper-text">{{ ele.name }} ({{ ele.field.type }})</div>
          </div>
          <div class="label-wrapper-tooltip">
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
            :config="{
              selectValue: ele.selectValue,
              multiple: ele.multiple,
              options: ele.options
            }"
            :customStyle="customStyle"
            :is="filterTypeCom(ele.field.deType)"
          ></component>
        </div>
      </div>
    </div>
    <div class="query-button" v-if="!!list.length">
      <el-button v-if="customStyle.btnList.includes('reset')" secondary>
        {{ t('chart.reset') }}
      </el-button>
      <el-button v-if="customStyle.btnList.includes('clear')" secondary>
        {{ t('commons.clear') }}
      </el-button>
      <el-button v-if="customStyle.btnList.includes('sure')" type="primary">
        {{ t('common.sure') }}
      </el-button>
    </div>
  </div>
  <Teleport to=".dv-common-layout">
    <QueryConditionConfiguration ref="queryConfig"></QueryConditionConfiguration>
  </Teleport>
</template>

<style lang="less" scoped>
.v-query {
  line-height: 1.5;
  display: flex;
  flex-wrap: wrap;
  width: 100%;
  height: 100%;
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
      }
    }
  }

  .horizontal {
    .query-field {
      padding-top: 18px;
      .label {
        align-items: center;
        height: 16px;
        left: 0;
        line-height: 16px;
        max-width: 100%;
        position: absolute;
        right: 0;
        top: 0;
        .label-wrapper-tooltip {
          display: inline-flex;
        }
      }
    }
  }

  .vertical {
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
    }
  }

  .query-button {
    align-self: flex-end;
    line-height: 28px;
    margin: auto 0 5px auto;
    z-index: 3;
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
