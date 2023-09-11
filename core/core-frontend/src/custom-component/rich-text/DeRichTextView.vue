<template>
  <div
    class="rich-main-class"
    :class="{ 'edit-model': canEdit }"
    :style="autoStyle"
    @keydown.stop
    @keyup.stop
    @dblclick="setEdit"
  >
    <Editor
      v-if="editShow"
      :id="tinymceId"
      v-model="myValue"
      style="width: 100%; height: 100%"
      :init="init"
      :disabled="!canEdit"
      @onClick="onClick"
    />
  </div>
</template>

<script setup lang="ts">
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
import { computed, nextTick, reactive, ref, toRefs, watch, onMounted } from 'vue'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import eventBus from '@/utils/eventBus'
import { guid } from '@/views/visualized/data/dataset/form/util'
import { getData } from '@/api/chart'
import { storeToRefs } from 'pinia'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
const snapshotStore = snapshotStoreWithOut()
const errMsg = ref('')
const curFields = ref([])
const dvMainStore = dvMainStoreWithOut()
const { canvasViewInfo } = storeToRefs(dvMainStore)

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
  }
})

const { scale, element, editMode, active, disabled } = toRefs(props)

const state = reactive({
  data: null,
  totalItems: 0
})
const dataRowSelect = ref({})
const dataRowNameSelect = ref({})
const drawLeft = ref('none')
const drawRight = ref('auto')
const initReady = ref(false)
const editReady = ref(false)
const editShow = ref(true)
const canEdit = ref(false)
// 初始化配置
const tinymceId = 'tinymce-view-' + element.value.id
const myValue = ref('')
const init = ref({
  selector: '#' + tinymceId,
  toolbar_items_size: 'small',
  language_url: '/tinymce/langs/zh_CN.js', // 汉化路径是自定义的，一般放在public或static里面
  language: 'zh_CN',
  skin_url: '/tinymce/skins/ui/oxide', // 皮肤
  content_css: '/tinymce/skins/content/default/content.css',
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
    '微软雅黑=Microsoft YaHei;宋体=SimSun;黑体=SimHei;仿宋=FangSong;华文黑体=STHeiti;华文楷体=STKaiti;华文宋体=STSong;华文仿宋=STFangsong;Andale Mono=andale mono,times;Arial=arial,helvetica,sans-serif;Arial Black=arial black,avant garde;Book Antiqua=book antiqua,palatino;Comic Sans MS=comic sans ms,sans-serif;Courier New=courier new,courier;Georgia=georgia,palatino;Helvetica=helvetica;Impact=impact,chicago;Symbol=symbol;Tahoma=tahoma,arial,helvetica,sans-serif;Terminal=terminal,monaco;Times New Roman=times new roman,times;Trebuchet MS=trebuchet ms,geneva;Verdana=verdana,geneva;Webdings=webdings;Wingdings=wingdings',
  fontsize_formats: '12px 14px 16px 18px 20px 22px 24px 28px 32px 36px 48px 56px 72px', // 字体大小
  menubar: false,
  placeholder: '双击输入文字',
  inline: true, // 开启内联模式
  branding: false
})

const editStatus = computed(() => {
  return editMode.value === 'edit'
})

const autoStyle = computed(() => {
  return {
    height: 100 / scale.value + '%!important',
    width: 100 / scale.value + '%!important',
    left: 50 * (1 - 1 / scale.value) + '%', // 放大余量 除以 2
    top: 50 * (1 - 1 / scale.value) + '%',
    transform: 'scale(' + scale.value + ')'
  }
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
  val => {
    if (canEdit.value) {
      const ed = tinymce.editors[tinymceId]
      element.value.propValue.textValue = ed.getContent()
    }
    if (initReady.value) {
      // snapshotStore.recordSnapshotCache()
    }
  }
)

const changeRightDrawOpen = param => {
  if (param) {
    drawLeft.value = 'auto!important'
    drawRight.value = '380px'
  } else {
    drawLeft.value = 'none'
    drawRight.value = 'auto'
  }
}

const viewInit = () => {
  eventBus.on('fieldSelect-' + element.value.id, fieldSelect)
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
          dataRowNameSelect.value[ele] !== undefined ? dataRowNameSelect.value[ele] : '[获取中...]'
        )
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
}
const onClick = e => {
  const node = tinymce.activeEditor.selection.getNode()
  resetSelect(node)
}
const resetSelect = (node?) => {
  const edInner = tinymce.get(tinymceId)
  if (edInner.dom) {
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
const setEdit = () => {
  if (editStatus.value && canEdit.value === false) {
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
  })
}
const chartResize = () => {
  // ignore
}

const calcData = (view: Chart, callback) => {
  if (view.tableId) {
    const v = JSON.parse(JSON.stringify(view))
    getData(v)
      .then(res => {
        if (res.code && res.code !== 0) {
          errMsg.value = res.msg
        } else {
          state.data = res?.data
          state.totalItems = res?.totalItems
          const curViewInfo = canvasViewInfo.value[element.value.id]
          curViewInfo['curFields'] = res.data.fields
          initCurFields(res)
        }
      })
      .finally(() => {
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
  curFields.value = []
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
        curFields.value.push(field)
      }
    })
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
      rowDataFormat(rowData, yDataeaseNames, yDataeaseNamesCfg)
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

const rowDataFormat = (rowData, yDataeaseNames, yDataeaseNamesCfg) => {
  for (const key in rowData) {
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
  background: rgba(51, 112, 255, 0.04);
}
</style>

<style lang="less">
.tox-tinymce-inline {
  left: var(--drawLeft);
  right: var(--drawRight);
}
</style>
