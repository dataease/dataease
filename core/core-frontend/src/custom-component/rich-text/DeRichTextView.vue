<template>
  <div class="rich-main-class" :style="autoStyle" @dblclick="setEdit" v-loading="state.loading">
    <!--    <de-rich-editor ref="editor" v-model="formState.contents" @getContent="getContent" />-->
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
import 'tinymce/plugins/directionality'
import 'tinymce/plugins/nonbreaking'
import 'tinymce/plugins/pagebreak'
import { uuid } from 'vue-uuid'
import { ref, computed, nextTick, onMounted, reactive, toRefs, watch } from 'vue'
import eventBus from '@/utils/eventBus'
import { getData } from '@/api/chart'
import DeRichEditor from '@/custom-component/rich-text/DeRichEditor.vue'

const props = defineProps({
  scale: {
    type: Number,
    required: false,
    default: 1
  },
  element: {
    type: Object,
    require: true
  },
  position: {
    type: String,
    require: false,
    default: 'canvas'
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

const { scale, element, position, active, disabled } = toRefs(props)

const curFields = ref([])
const dataRowSelect = ref({})
const dataRowNameSelect = ref({})

const myValue = ref('')

const state = reactive({
  loading: false,
  drawLeft: 'none',
  drawRight: 'auto',
  initReady: false,
  editReady: false,
  editShow: true,
  canEdit: false,
  // 初始化配置
  tinymceId: 'tinymce-view-' + element.value['id'],
  myValue: '',
  init: {
    selector: '#tinymce-view-' + element.value['id'],
    toolbar_items_size: 'small',
    language_url: '/tinymce/langs/zh_CN.js', // 汉化路径是自定义的，一般放在public或static里面
    language: 'zh_CN',
    skin_url: '/tinymce/skins/ui/oxide', // 皮肤
    content_css: '/tinymce/skins/content/default/content.css',
    plugins:
      'advlist autolink link image lists charmap  media wordcount table directionality pagebreak', // 插件
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
  }
})

const editStatus = computed(() => {
  return position.value === 'canvas'
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

const changeRightDrawOpen = param => {
  if (param) {
    state.drawLeft = 'auto!important'
    state.drawRight = '380px'
  } else {
    state.drawLeft = 'none'
    state.drawRight = 'auto'
  }
}

const formState = reactive({ contents: '' })
const getContent = (v: string) => {
  formState.contents = v
}

const viewInit = () => {
  // do viewInit
}
const initCurFieldsChange = () => {
  if (!state.canEdit) {
    myValue.value = assignment(element.value['propValue'].textValue)
  }
}
const assignment = content => {
  const on = content.match(/\[(.+?)\]/g)
  if (on) {
    on.forEach(itm => {
      const ele = itm.slice(1, -1)
      content = content.replace(
        itm,
        dataRowNameSelect.value[ele] !== undefined
          ? dataRowNameSelect.value[ele]
          : '[无法获取字段值]'
      )
    })
  }
  content = content.replace('class="base-selected"', '')
  resetSelect()
  return content
}
const fieldSelect = field => {
  const ed = tinymce.get(state.tinymceId)
  const fieldId = 'changeText-' + uuid.v1()
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
  const edInner = tinymce.get(state.tinymceId)
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
  if (editStatus.value && state.canEdit === false) {
    state.canEdit = true
    element.value['editing'] = true
    myValue.value = element.value['propValue'].textValue
    const ed = tinymce.get(state.tinymceId)
    ed.setContent(myValue.value)
    reShow()
  }
}
const reShow = () => {
  state.editShow = false
  nextTick(() => {
    state.editShow = true
  })
}
const chartResize = () => {
  // ignore
}

watch(
  [active.value],
  val => {
    if (!val) {
      const ed = tinymce.get(state.tinymceId)
      if (state.canEdit) {
        element.value['propValue'].textValue = ed.getContent()
      }
      state.canEdit = false
      reShow()
      myValue.value = assignment(element.value['propValue'].textValue)
      ed.setContent(myValue.value)
    }
  },
  { deep: true }
)

watch(
  [myValue.value],
  () => {
    if (state.canEdit) {
      const ed = tinymce.get(state.tinymceId)
      element.value['propValue'].textValue = ed.getContent()
    }
  },
  { deep: true }
)

const initCurFields = chartDetails => {
  curFields.value = []
  dataRowSelect.value = {}
  dataRowNameSelect.value = {}
  if (chartDetails.data && chartDetails.data.sourceFields) {
    const checkAllAxisStr =
      chartDetails.xaxis + chartDetails.xaxisExt + chartDetails.yaxis + chartDetails.yaxisExt
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
    let yAxis = []
    try {
      yAxis = JSON.parse(chartDetails.yaxis)
    } catch (err) {
      yAxis = JSON.parse(JSON.stringify(chartDetails.yaxis))
    }
    const yDataeaseNames = []
    const yDataeaseNamesCfg = []
    yAxis.forEach(yItem => {
      yDataeaseNames.push(yItem.dataeaseName)
      yDataeaseNamesCfg[yItem.dataeaseName] = yItem.formatterCfg
    })
    // this.rowDataFormat(rowData, yDataeaseNames, yDataeaseNamesCfg)
    for (const key in rowData) {
      dataRowSelect.value[nameIdMap[key]] = rowData[key]
      dataRowNameSelect.value[sourceFieldNameIdMap[key]] = rowData[key]
    }
  }
  element.value['propValue']['innerType'] = chartDetails.type
  element.value['propValue']['render'] = chartDetails.render
  initCurFieldsChange()
}

const calcData = view => {
  if (!view.tableId) {
    return
  }
  state.loading = true
  const v = JSON.parse(JSON.stringify(view))
  getData(v)
    .then(res => {
      initCurFields(res?.data)
    })
    .finally(() => {
      state.loading = false
    })
}

const renderChart = view => {
  // do nothing
}

defineExpose({
  calcData,
  renderChart
})

onMounted(() => {
  viewInit()
  eventBus.on('change_panel_right_draw', changeRightDrawOpen)
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

:deep(ol) {
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

.tox-tinymce-inline {
  left: var(--drawLeft);
  right: var(--drawRight);
}
</style>
