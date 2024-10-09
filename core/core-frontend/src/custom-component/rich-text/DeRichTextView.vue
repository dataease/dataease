<template>
  <div
    class="rich-main-class"
    :class="{ 'edit-model': canEdit }"
    @keydown.stop
    @keyup.stop
    @dblclick="setEdit"
    @click="onClick"
    :style="richTextStyle"
  >
    <chart-error v-if="isError" :err-msg="errMsg" />
    <Editor
      v-if="editShow && !isError"
      v-model="myValue"
      class="custom-text-content"
      :style="wrapperStyle"
      :id="tinymceId"
      :init="init"
      :disabled="!canEdit || disabled"
    />
    <div
      class="rich-placeholder"
      :class="{ 'rich-placeholder--dark': themes === 'dark' }"
      v-if="showPlaceHolder"
    >
      {{ init.outer_placeholder }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { formatDataEaseBi } from '@/utils/url'
import tinymce from 'tinymce/tinymce' // tinymce默认hidden，不引入不显示
import Editor from '@tinymce/tinymce-vue' // 编辑器引入
import 'tinymce/themes/silver/theme' // 编辑器主题
import 'tinymce/icons/default' // 引入编辑器图标icon，不引入则不显示对应图标
// 引入编辑器插件（基本免费插件都在这儿了）
import 'tinymce/plugins/advlist' // 高级列表
import 'tinymce/plugins/autolink' // 自动链接
import 'tinymce/plugins/link' // 超链接
import 'tinymce/plugins/image' // 插入编辑图片
import 'tinymce/plugins/lists' // 列表插件
import 'tinymce/plugins/charmap' // 特殊字符
import 'tinymce/plugins/media' // 插入编辑媒体
import 'tinymce/plugins/wordcount' // 字数统计
import 'tinymce/plugins/table' // 表格
import 'tinymce/plugins/contextmenu' // contextmenu
import 'tinymce/plugins/directionality'
import 'tinymce/plugins/nonbreaking'
import 'tinymce/plugins/pagebreak'
import '@npkg/tinymce-plugins/letterspacing'
import './plugins' //自定义插件
import { computed, nextTick, reactive, ref, toRefs, watch, onMounted, PropType } from 'vue'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import eventBus from '@/utils/eventBus'
import { guid } from '@/views/visualized/data/dataset/form/util'
import { getData } from '@/api/chart'
import { storeToRefs } from 'pinia'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import ChartError from '@/views/chart/components/views/components/ChartError.vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { parseJson } from '@/views/chart/components/js/util'
import { mappingColor } from '@/views/chart/components/js/panel/common/common_table'
import { CHART_FONT_FAMILY } from '@/views/chart/components/editor/util/chart'
import { useAppearanceStoreWithOut } from '@/store/modules/appearance'
const snapshotStore = snapshotStoreWithOut()
const errMsg = ref('')
const dvMainStore = dvMainStoreWithOut()
const { canvasViewInfo } = storeToRefs(dvMainStore)
const isError = ref(false)
const appearanceStore = useAppearanceStoreWithOut()

const props = defineProps({
  scale: {
    type: Number,
    required: false,
    default: 1
  },
  element: {
    type: Object
  },
  editMode: {
    type: String,
    require: false,
    default: 'edit'
  },
  active: {
    type: Boolean,
    require: false,
    default: false
  },
  // 是否禁用
  disabled: {
    type: Boolean,
    default: false
  },
  showPosition: {
    type: String,
    required: false,
    default: 'preview'
  },
  themes: {
    type: String as PropType<EditorTheme>,
    default: 'dark'
  }
})

const { element, editMode, active, disabled, showPosition } = toRefs(props)

const state = reactive({
  emptyValue: '-',
  data: null,
  viewDataInfo: null,
  totalItems: 0,
  firstRender: true,
  previewFirstRender: true
})
const dataRowSelect = ref({})
const dataRowNameSelect = ref({})
const dataRowNameSelectSource = ref({})
const dataRowFiledName = ref([])
const initReady = ref(false)
const editShow = ref(true)
const canEdit = ref(false)
// 初始化配置
const tinymceId = 'tinymce-view-' + element.value.id
const myValue = ref('')

const systemFontFamily = appearanceStore.fontList.map(item => item.name)
const curFontFamily = () => {
  let result = ''
  CHART_FONT_FAMILY.concat(
    appearanceStore.fontList.map(ele => ({
      name: ele.name,
      value: ele.name
    }))
  ).forEach(font => {
    result = result + font.name + '=' + font.name + ';'
  })
  return result
}

const init = ref({
  selector: '#' + tinymceId,
  toolbar_items_size: 'small',
  language_url: formatDataEaseBi('./tinymce-dataease-private/langs/zh_CN.js'), // 汉化路径是自定义的，一般放在public或static里面
  language: 'zh_CN',
  skin_url: formatDataEaseBi('./tinymce-dataease-private/skins/ui/oxide'), // 皮肤
  content_css: formatDataEaseBi('./tinymce-dataease-private/skins/content/default/content.css'),
  plugins:
    'vertical-content advlist autolink link image lists charmap  media wordcount table contextmenu directionality pagebreak letterspacing', // 插件
  // 工具栏
  toolbar:
    'undo redo | fontselect fontsizeselect |forecolor backcolor bold italic letterspacing |underline strikethrough link lineheight| formatselect |' +
    'top-align center-align bottom-align | alignleft aligncenter alignright | bullist numlist |' +
    ' blockquote subscript superscript removeformat | table image ',
  toolbar_location: '/',
  font_formats: curFontFamily(),
  fontsize_formats: '12px 14px 16px 18px 20px 22px 24px 28px 32px 36px 42px 48px 56px 72px', // 字体大小
  menubar: false,
  placeholder: '',
  outer_placeholder: '双击输入文字',
  inline: true, // 开启内联模式
  branding: false,
  icons: 'vertical-content',
  vertical_align: element.value.propValue.verticalAlign,
  setup: function (editor) {
    // 在表格调整大小开始时
    editor.on('ObjectResizeStart', function (e) {
      const { target, width, height } = e
      if (target.nodeName === 'TABLE') {
        // 将宽高根据缩放比例调整
        // e.width = width / props.scale
        // e.height = height / props.scale
      }
    })

    // 在表格调整大小结束时
    editor.on('ObjectResized', function (e) {
      const { target, width, height } = e
      if (target.nodeName === 'TABLE') {
        // 将最终调整的宽高根据缩放比例重设
        // target.style.width = `${width * props.scale}px`
        // target.style.height = `${height  scaleFactor}px`
      }
    })
  }
})

const editStatus = computed(() => {
  return editMode.value === 'edit'
})

watch(
  () => active.value,
  val => {
    if (!val) {
      const ed = tinymce.editors[tinymceId]
      if (canEdit.value) {
        element.value.propValue.textValue = ed?.getContent()
      }
      element.value['editing'] = false
      canEdit.value = false
      reShow()
      myValue.value = assignment(element.value.propValue.textValue)
      console.log('===myValue.value=' + myValue.value)
      ed.setContent(myValue.value)
    }
  }
)

watch(
  () => myValue.value,
  () => {
    if (canEdit.value) {
      const ed = tinymce.editors[tinymceId]
      element.value.propValue.textValue = ed?.getContent()
    }
    if (initReady.value && canEdit.value) {
      snapshotStore.recordSnapshotCache('renderChart', element.value.id)
      initFontFamily(myValue.value)
    }
  }
)
const ALIGN_MAP = {
  'top-align': {},
  'center-align': {
    margin: 'auto'
  },
  'bottom-align': {
    'margin-top': 'auto'
  }
}
const wrapperStyle = computed(() => {
  const align = element.value.propValue.verticalAlign
  if (!align) {
    return {}
  }
  return ALIGN_MAP[align]
})
useEmitt({
  name: 'vertical-change-' + tinymceId,
  callback: align => {
    element.value.propValue.verticalAlign = align
  }
})

const viewInit = () => {
  useEmitt({
    name: 'fieldSelect-' + element.value.id,
    callback: function (val) {
      fieldSelect(val)
    }
  })
  tinymce.init({})
  myValue.value = assignment(element.value.propValue.textValue)
}
const initCurFieldsChange = () => {
  if (!canEdit.value) {
    myValue.value = assignment(element.value.propValue.textValue)
    const ed = tinymce.editors[tinymceId]
    ed.setContent(myValue.value)
  }
}

const jumpTargetAdaptor = () => {
  setTimeout(() => {
    const paragraphs = document.querySelectorAll('p')
    paragraphs.forEach(p => {
      // 如果 p 标签已经有 onclick 且包含 event.stopPropagation，则跳过
      if (
        p.getAttribute('onclick') &&
        p.getAttribute('onclick').includes('event.stopPropagation()')
      ) {
        return // 已经有 stopPropagation，跳过
      }
      // 否则添加 onclick 事件
      p.setAttribute('onclick', 'event.stopPropagation()')
    })
  }, 1000)
}

const assignment = content => {
  const on = content.match(/\[(.+?)\]/g)
  if (on) {
    const thresholdStyleInfo = conditionAdaptor(state.viewDataInfo)
    on.forEach(itm => {
      if (dataRowFiledName.value.includes(itm)) {
        const ele = itm.slice(1, -1)
        let value = dataRowNameSelect.value[ele] !== undefined ? dataRowNameSelect.value[ele] : null
        let targetValue = !!value ? value : state.emptyValue
        if (thresholdStyleInfo && thresholdStyleInfo[ele]) {
          const thresholdStyle = thresholdStyleInfo[ele]
          targetValue = `<span style="color:${thresholdStyle.color};background-color: ${thresholdStyle.backgroundColor}">${targetValue}</span>`
        }
        if (initReady.value) {
          content = content.replace(itm, targetValue)
        } else {
          content = content.replace(itm, !!value ? targetValue : '[获取中...]')
        }
      }
    })
  }
  content = content.replace('class="base-selected"', '')
  //De 本地跳转失效问题
  content = content.replace(/href="#\//g, 'href="/#/')
  content = content.replace(/href=\\"#\//g, 'href=\\"/#/')
  content = content.replace(/href=\\"#\//g, 'href=\\"/#/')
  resetSelect()
  initFontFamily(content)
  jumpTargetAdaptor()
  return content
}
const initFontFamily = htmlText => {
  const regex = /font-family:\s*([^;"]+);/g
  let match
  while ((match = regex.exec(htmlText)) !== null) {
    const font = match[1].trim()
    if (systemFontFamily.includes(font)) {
      appearanceStore.setCurrentFont(font)
    }
  }
}
const fieldSelect = field => {
  const ed = tinymce.editors[tinymceId]
  const fieldId = 'changeText-' + guid()
  const value =
    '<span id="' +
    fieldId +
    '"><span class="mceNonEditable" contenteditable="false" data-mce-content="[' +
    field.name +
    ']">[' +
    field.name +
    ']</span></span>'
  const attachValue = '<span id="attachValue">&nbsp;</span>'
  ed.insertContent(value)
  ed.insertContent(attachValue)
  snapshotStore.resetStyleChangeTimes()
}
const onClick = () => {
  if (canEdit.value) {
    const node = tinymce.activeEditor.selection.getNode()
    resetSelect(node)
  }
}
const resetSelect = (node?) => {
  const edInner = tinymce.get(tinymceId)
  if (edInner?.dom) {
    const nodeArray = edInner.dom.select('.base-selected')
    if (nodeArray) {
      nodeArray.forEach(nodeInner => {
        nodeInner.removeAttribute('class')
      })
    }
    if (node) {
      const pNode = node.parentElement
      if (pNode && pNode.id && pNode.id.indexOf('changeText') > -1) {
        const innerId = '#' + pNode.id
        const domTest = edInner.dom.select(innerId)[0]
        domTest.setAttribute('class', 'base-selected')
        edInner.selection.select(domTest)
      }
    }
  }
}

const computedCanEdit = computed<boolean>(() => {
  return (
    ['canvas', 'canvasDataV'].includes(showPosition.value) &&
    editStatus.value &&
    canEdit.value === false &&
    !isError.value &&
    !disabled.value
  )
})

const showPlaceHolder = computed<boolean>(() => {
  return (
    computedCanEdit.value && (myValue.value == undefined || myValue.value == '') && !isError.value
  )
})

const editActive = computed<boolean>(() => {
  if (element.value.canvasId.includes('Group') && !active.value) {
    return false
  } else {
    return true
  }
})

const setEdit = () => {
  if (computedCanEdit.value && editActive.value) {
    canEdit.value = true
    element.value['editing'] = true
    myValue.value = element.value.propValue.textValue
    const ed = tinymce.editors[tinymceId]
    ed.setContent(myValue.value)
    reShow()
  }
}
const reShow = () => {
  editShow.value = false
  nextTick(() => {
    editShow.value = true
    editCursor()
  })
}

const editCursor = () => {
  setTimeout(() => {
    const myDiv = document.getElementById(tinymceId)
    // 让光标聚焦到文本末尾
    const range = document.createRange()
    const sel = window.getSelection()
    if (myDiv.childNodes) {
      range.setStart(myDiv.childNodes[myDiv.childNodes.length - 1], 1)
      range.collapse(false)
      sel.removeAllRanges()
      sel.addRange(range)
    }
    // 对于一些浏览器，可能需要设置光标到最后的另一种方式
    if (myDiv.focus) {
      myDiv.focus()
    }
    tinymce.init({
      selector: tinymceId,
      plugins: 'table',
      setup: function (editor) {
        editor.on('init', function () {
          console.info('====init====')
        })
      }
    })
  }, 100)
}

const updateEmptyValue = view => {
  state.emptyValue =
    view?.senior?.functionCfg?.emptyDataStrategy === 'custom'
      ? view.senior.functionCfg.emptyDataCustomValue || ''
      : '-'
}

const calcData = (view: Chart, callback) => {
  isError.value = false
  updateEmptyValue(view)
  if (view.tableId || view['dataFrom'] === 'template') {
    const v = JSON.parse(JSON.stringify(view))
    getData(v)
      .then(res => {
        if (res.code && res.code !== 0) {
          isError.value = true
          errMsg.value = res.msg
        } else {
          state.data = res?.data
          state.viewDataInfo = res
          state.totalItems = res?.totalItems
          const curViewInfo = canvasViewInfo.value[element.value.id]
          curViewInfo['curFields'] = res.data.fields
          dvMainStore.setViewDataDetails(element.value.id, res)
          initReady.value = true
          initCurFields(res)
        }
        callback?.()
        nextTick(() => {
          initReady.value = true
        })
      })
      .catch(() => {
        nextTick(() => {
          initReady.value = true
        })
        callback?.()
      })
  } else if (!view.tableId) {
    state.data = []
    state.viewDataInfo = {}
    state.totalItems = 0
    const curViewInfo = canvasViewInfo.value[element.value.id]
    if (curViewInfo) {
      curViewInfo['curFields'] = []
      dvMainStore.setViewDataDetails(element.value.id, state.viewDataInfo)
      initReady.value = true
      initCurFields(curViewInfo)
    }
    initReady.value = true
    callback?.()
    nextTick(() => {
      initReady.value = true
    })
  } else {
    nextTick(() => {
      initReady.value = true
    })
    callback?.()
  }
}

const initCurFields = chartDetails => {
  dataRowFiledName.value = []
  dataRowSelect.value = {}
  dataRowNameSelect.value = {}
  dataRowNameSelectSource.value = {} // 记录原格式，部分数字是经过格式化的，再匹配颜色时会有问题
  if (chartDetails.data && chartDetails.data.sourceFields) {
    const checkAllAxisStr =
      JSON.stringify(chartDetails.xAxis) +
      JSON.stringify(chartDetails.xAxisExt) +
      JSON.stringify(chartDetails.yAxis) +
      JSON.stringify(chartDetails.yAxisExt)
    chartDetails.data.sourceFields.forEach(field => {
      if (checkAllAxisStr.indexOf(field.id) > -1) {
        dataRowFiledName.value.push(`[${field.name}]`)
      }
    })
    if (checkAllAxisStr.indexOf('"记录数*"') > -1) {
      dataRowFiledName.value.push(`[记录数*]`)
    }
    // Get the corresponding relationship between id and value
    const nameIdMap = chartDetails.data.fields.reduce((pre, next) => {
      pre[next['dataeaseName']] = next['id']
      return pre
    }, {})
    const sourceFieldNameIdMap = chartDetails.data.fields.reduce((pre, next) => {
      pre[next['dataeaseName']] = next['name']
      return pre
    }, {})
    const rowData = chartDetails.data.tableRow[0]
    if (chartDetails.type === 'rich-text') {
      let yAxis = JSON.parse(JSON.stringify(chartDetails.yAxis))
      const yDataeaseNames = []
      const yDataeaseNamesCfg = []
      yAxis.forEach(yItem => {
        yDataeaseNames.push(yItem.dataeaseName)
        yDataeaseNamesCfg[yItem.dataeaseName] = yItem.formatterCfg
      })
    }
    const valueFieldMap: Record<string, Axis> = chartDetails.yAxis.reduce((p, n) => {
      p[n.dataeaseName] = n
      return p
    }, {})
    for (const key in rowData) {
      dataRowSelect.value[nameIdMap[key]] = rowData[key]
      let rowDataValue = rowData[key]
      const rowDataValueSource = rowData[key]
      const f = valueFieldMap[key]
      if (f && f.formatterCfg) {
        rowDataValue = valueFormatter(rowDataValue, f.formatterCfg)
      }
      dataRowNameSelect.value[sourceFieldNameIdMap[key]] = rowDataValue
      dataRowNameSelectSource.value[sourceFieldNameIdMap[key]] = rowDataValueSource
    }
  }
  element.value.propValue['innerType'] = chartDetails.type
  element.value.propValue['render'] = chartDetails.render
  if (chartDetails.type === 'rich-text') {
    nextTick(() => {
      initCurFieldsChange()
      eventBus.emit('initCurFields-' + element.value.id)
    })
  }
}

// 初始化此处不必刷新
const renderChart = viewInfo => {
  //do renderView
  updateEmptyValue(viewInfo)
  initCurFieldsChange()
  eventBus.emit('initCurFields-' + element.value.id)
}

const conditionAdaptor = (chart: Chart) => {
  if (!chart || !chart.senior) {
    return
  }
  const { threshold } = parseJson(chart.senior)
  if (!threshold.enable) {
    return
  }
  const res = {}
  const conditions = threshold.tableThreshold ?? []
  if (conditions?.length > 0) {
    for (let i = 0; i < conditions.length; i++) {
      const field = conditions[i]
      let defaultValueColor = 'none'
      let defaultBgColor = 'none'
      res[field.field.name] = {
        color: mappingColor(
          dataRowNameSelectSource.value[field.field.name],
          defaultValueColor,
          field,
          'color'
        ),
        backgroundColor: mappingColor(
          dataRowNameSelectSource.value[field.field.name],
          defaultBgColor,
          field,
          'backgroundColor'
        )
      }
    }
  }
  return res
}

const richTextStyle = computed(() => [{ '--de-canvas-scale': props.scale }])

onMounted(() => {
  viewInit()
})

defineExpose({
  calcData,
  renderChart
})
</script>

<style lang="less" scoped>
.rich-main-class {
  display: flex;
  font-size: initial;
  width: 100%;
  height: 100%;
  overflow-y: auto !important;
  position: relative;
  div::-webkit-scrollbar {
    width: 0px !important;
    height: 0px !important;
  }
  ::v-deep(p) {
    zoom: var(--de-canvas-scale);
  }
  ::v-deep(td span) {
    zoom: var(--de-canvas-scale);
  }
}

:deep(.ol) {
  display: block !important;
  list-style-type: decimal;
  margin-block-start: 1em !important;
  margin-block-end: 1em !important;
  margin-inline-start: 0px !important;
  margin-inline-end: 0px !important;
  padding-inline-start: 40px !important;
}

:deep(ul) {
  display: block !important;
  list-style-type: disc;
  margin-block-start: 1em !important;
  margin-block-end: 1em !important;
  margin-inline-start: 0px !important;
  margin-inline-end: 0px !important;
  padding-inline-start: 40px !important;
}

:deep(li) {
  margin-left: 20px;
  display: list-item !important;
  text-align: -webkit-match-parent !important;
}

:deep(.base-selected) {
  background-color: #b4d7ff;
}

:deep(p) {
  margin: 0px;
  padding: 0px;
}

.edit-model {
  cursor: text;
}

.mceNonEditable {
  background: rgba(51, 112, 255, 0.4);
}

.tox-tinymce-inline {
  left: var(--drawLeft);
  right: var(--drawRight);
}
</style>

<style lang="less">
.tox {
  border-radius: 4px !important;
  border-bottom: 1px solid #ccc !important;
  z-index: 1000;
}
.tox-tbtn {
  height: auto !important;
}
.tox-collection__item-label {
  p {
    color: #1a1a1a !important;
  }
  h1 {
    color: #1a1a1a !important;
  }
  h2 {
    color: #1a1a1a !important;
  }
  h3 {
    color: #1a1a1a !important;
  }
  h4 {
    color: #1a1a1a !important;
  }
  h5 {
    color: #1a1a1a !important;
  }
  h6 {
    color: #1a1a1a !important;
  }
  pre {
    color: #1a1a1a !important;
  }
}

.rich-placeholder {
  position: absolute;
  top: 50%;
  left: 50%;

  color: #646a73;
  font-size: 16px;
  font-style: normal;
  font-weight: 400;
  line-height: 24px;

  transform: translate(-50%, -50%);

  &.rich-placeholder--dark {
    color: #fff;
  }
}

.custom-text-content {
  width: 100%;
  overflow-y: auto;
  outline: none !important;
  border: none !important;
  ol {
    list-style-type: decimal;
  }
}
</style>
