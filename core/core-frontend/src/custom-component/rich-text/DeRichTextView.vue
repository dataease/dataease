<template>
  <div
    class="rich-main-class"
    :class="{ 'edit-model': canEdit }"
    @keydown.stop
    @keyup.stop
    @dblclick="setEdit"
  >
    <chart-error v-if="isError" :err-msg="errMsg" />
    <Editor
      v-if="editShow && !isError"
      :id="tinymceId"
      v-model="myValue"
      class="custom-text-content"
      :init="init"
      :disabled="!canEdit || disabled"
      @onClick="onClick"
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
import { computed, nextTick, reactive, ref, toRefs, watch, onMounted, PropType } from 'vue'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import eventBus from '@/utils/eventBus'
import { guid } from '@/views/visualized/data/dataset/form/util'
import { getData } from '@/api/chart'
import { storeToRefs } from 'pinia'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import ChartError from '@/views/chart/components/views/components/ChartError.vue'
import { useEmitt } from '@/hooks/web/useEmitt'
const snapshotStore = snapshotStoreWithOut()
const errMsg = ref('')
const dvMainStore = dvMainStoreWithOut()
const { canvasViewInfo } = storeToRefs(dvMainStore)
const isError = ref(false)
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

const { scale, element, editMode, active, disabled, showPosition } = toRefs(props)

const state = reactive({
  data: null,
  totalItems: 0
})
const dataRowSelect = ref({})
const dataRowNameSelect = ref({})
const dataRowFiledName = ref([])
const initReady = ref(false)
const editShow = ref(true)
const canEdit = ref(false)
// 初始化配置
const tinymceId = 'tinymce-view-' + element.value.id
const myValue = ref('')
const init = ref({
  selector: '#' + tinymceId,
  toolbar_items_size: 'small',
  language_url: formatDataEaseBi('/tinymce-dataease-private/langs/zh_CN.js'), // 汉化路径是自定义的，一般放在public或static里面
  language: 'zh_CN',
  skin_url: formatDataEaseBi('/tinymce-dataease-private/skins/ui/oxide'), // 皮肤
  content_css: formatDataEaseBi('/tinymce-dataease-private/skins/content/default/content.css'),
  plugins:
    'advlist autolink link image lists charmap  media wordcount table contextmenu directionality pagebreak', // 插件
  // 工具栏
  toolbar:
    'undo redo |fontselect fontsizeselect |forecolor backcolor bold italic |underline strikethrough link| formatselect |' +
    'alignleft aligncenter alignright | bullist numlist |' +
    ' blockquote subscript superscript removeformat | table image media | fullscreen ' +
    '| bdmap indent2em lineheight formatpainter axupimgs',
  toolbar_location: '/',
  font_formats:
    '阿里巴巴普惠体=阿里巴巴普惠体 3.0 55 Regular L3;微软雅黑=Microsoft YaHei;宋体=SimSun;黑体=SimHei;仿宋=FangSong;华文黑体=STHeiti;华文楷体=STKaiti;华文宋体=STSong;华文仿宋=STFangsong;Andale Mono=andale mono,times;Arial=arial,helvetica,sans-serif;Arial Black=arial black,avant garde;Book Antiqua=book antiqua,palatino;Comic Sans MS=comic sans ms,sans-serif;Courier New=courier new,courier;Georgia=georgia,palatino;Helvetica=helvetica;Impact=impact,chicago;Symbol=symbol;Tahoma=tahoma,arial,helvetica,sans-serif;Terminal=terminal,monaco;Times New Roman=times new roman,times;Trebuchet MS=trebuchet ms,geneva;Verdana=verdana,geneva;Webdings=webdings;Wingdings=wingdings',
  fontsize_formats: '12px 14px 16px 18px 20px 22px 24px 28px 32px 36px 48px 56px 72px', // 字体大小
  menubar: false,
  placeholder: '',
  outer_placeholder: '双击输入文字',
  inline: true, // 开启内联模式
  branding: false
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
        element.value.propValue.textValue = ed.getContent()
      }
      element.value['editing'] = false
      canEdit.value = false
      reShow()
      myValue.value = assignment(element.value.propValue.textValue)
      ed.setContent(myValue.value)
    }
  }
)

watch(
  () => myValue.value,
  () => {
    if (canEdit.value) {
      const ed = tinymce.editors[tinymceId]
      element.value.propValue.textValue = ed.getContent()
    }
    if (initReady.value) {
      snapshotStore.recordSnapshotCache()
    }
  }
)

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
  }
}

const assignment = content => {
  const on = content.match(/\[(.+?)\]/g)
  if (on) {
    on.forEach(itm => {
      if (dataRowFiledName.value.includes(itm)) {
        const ele = itm.slice(1, -1)
        if (initReady.value) {
          content = content.replace(
            itm,
            dataRowNameSelect.value[ele] !== undefined
              ? dataRowNameSelect.value[ele]
              : '[未获取字段值]'
          )
        } else {
          content = content.replace(
            itm,
            dataRowNameSelect.value[ele] !== undefined
              ? dataRowNameSelect.value[ele]
              : '[获取中...]'
          )
        }
      }
    })
  }
  content = content.replace('class="base-selected"', '')
  resetSelect()
  return content
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
  const node = tinymce.activeEditor.selection.getNode()
  resetSelect(node)
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
  }, 100)
}

const calcData = (view: Chart, callback) => {
  isError.value = false
  if (view.tableId || view['dataFrom'] === 'template') {
    const v = JSON.parse(JSON.stringify(view))
    getData(v)
      .then(res => {
        if (res.code && res.code !== 0) {
          isError.value = true
          errMsg.value = res.msg
        } else {
          state.data = res?.data
          state.totalItems = res?.totalItems
          const curViewInfo = canvasViewInfo.value[element.value.id]
          curViewInfo['curFields'] = res.data.fields
          dvMainStore.setViewDataDetails(element.value.id, state.data)
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
    for (const key in rowData) {
      dataRowSelect.value[nameIdMap[key]] = rowData[key]
      dataRowNameSelect.value[sourceFieldNameIdMap[key]] = rowData[key]
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

const renderChart = () => {
  //do nothing
}

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
  font-size: initial;
  width: 100%;
  height: 100%;
  overflow-y: auto !important;
  position: relative;
}

::-webkit-scrollbar {
  width: 0px !important;
  height: 0px !important;
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
  height: 100%;
  overflow-y: auto;
  outline: none !important;
  border: none !important;
}
</style>
