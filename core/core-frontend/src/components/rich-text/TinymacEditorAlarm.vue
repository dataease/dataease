<template>
  <div class="de-tinymce-container_alarm ed-textarea__inner">
    <editor class="de-tinymce-content_alarm" v-model="myValue" :id="tinymceId" :init="init" />
  </div>
</template>
<script lang="ts" setup>
import { ref, toRefs, watch, onMounted, onBeforeUnmount } from 'vue'
import { useEmitt } from '@/hooks/web/useEmitt'
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
import { propTypes } from '@/utils/propTypes'
const props = defineProps({
  modelValue: String,
  fieldList: propTypes.arrayOf(
    propTypes.shape({
      deType: propTypes.number,
      id: propTypes.string,
      name: propTypes.string,
      groupType: propTypes.string
    })
  )
})
const myValue = ref()
const { modelValue } = toRefs(props)
myValue.value = modelValue
watch(
  () => props.modelValue,
  newValue => {
    myValue.value = newValue
  }
)

const emits = defineEmits(['update:modelValue', 'change'])
watch(
  () => myValue.value,
  newValue => {
    emits('update:modelValue', newValue)
    emits('change', newValue)
  }
)

const tinymceId = 'tinymce-view-alarm'
const init = ref({
  selector: '#' + tinymceId,
  toolbar_items_size: 'small',
  language_url: formatDataEaseBi('./tinymce-dataease-private/langs/zh_CN.js'), // 汉化路径是自定义的，一般放在public或static里面
  language: 'zh_CN',
  skin_url: formatDataEaseBi('./tinymce-dataease-private/skins/ui/oxide'), // 皮肤
  content_css: formatDataEaseBi('./tinymce-dataease-private/skins/content/default/content.css'),
  plugins:
    'advlist autolink link image lists charmap  media wordcount table contextmenu directionality pagebreak', // 插件
  // 工具栏
  toolbar:
    'undo redo |fontselect fontsizeselect |forecolor backcolor bold italic |underline strikethrough | splitDateButton lineheight| formatselect |' +
    'alignleft aligncenter alignright | bullist numlist |' +
    ' blockquote subscript superscript removeformat | table image media link',
  toolbar_location: '/',
  font_formats:
    '微软雅黑=Microsoft YaHei;宋体=SimSun;黑体=SimHei;仿宋=FangSong;华文黑体=STHeiti;华文楷体=STKaiti;华文宋体=STSong;华文仿宋=STFangsong;Andale Mono=andale mono,times;Arial=arial,helvetica,sans-serif;Arial Black=arial black,avant garde;Book Antiqua=book antiqua,palatino;Comic Sans MS=comic sans ms,sans-serif;Courier New=courier new,courier;Georgia=georgia,palatino;Helvetica=helvetica;Impact=impact,chicago;Symbol=symbol;Tahoma=tahoma,arial,helvetica,sans-serif;Terminal=terminal,monaco;Times New Roman=times new roman,times;Trebuchet MS=trebuchet ms,geneva;Verdana=verdana,geneva;Webdings=webdings;Wingdings=wingdings',
  fontsize_formats: '12px 14px 16px 18px 20px 22px 24px 28px 32px 36px 48px 56px 72px', // 字体大小
  menubar: false,
  placeholder: '',
  outer_placeholder: '双击输入文字',
  inline: false,
  branding: true,
  setup: editor => {
    const emoticons = `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16" fill="none">
<path d="M8.66683 4.66671H11.0002C11.1843 4.66671 11.3335 4.81595 11.3335 5.00004V5.66671C11.3335 5.8508 11.1843 6.00004 11.0002 6.00004H8.66683V11C8.66683 11.1841 8.51759 11.3334 8.3335 11.3334H7.66683C7.48273 11.3334 7.3335 11.1841 7.3335 11V6.00004H5.00016C4.81607 6.00004 4.66683 5.8508 4.66683 5.66671V5.00004C4.66683 4.81595 4.81607 4.66671 5.00016 4.66671H7.3335C7.3335 4.66671 8.66683 4.68559 8.66683 4.66671Z" fill="#1F2329"/>
<path fill-rule="evenodd" clip-rule="evenodd" d="M1.3335 2.00004C1.3335 1.63185 1.63197 1.33337 2.00016 1.33337H14.0002C14.3684 1.33337 14.6668 1.63185 14.6668 2.00004V14C14.6668 14.3682 14.3684 14.6667 14.0002 14.6667H2.00016C1.63197 14.6667 1.3335 14.3682 1.3335 14V2.00004ZM2.66683 13.3334V2.66671H13.3335V13.3334H2.66683Z" fill="#1F2329"/>
</svg>`

    const icon_text_outlined = `<svg width="24" height="24" viewBox="0 0 24 24"  xmlns="http://www.w3.org/2000/svg">
<path d="M13 5V20.5C13 20.5657 12.9871 20.6307 12.9619 20.6913C12.9368 20.752 12.9 20.8071 12.8536 20.8536C12.8071 20.9 12.752 20.9368 12.6913 20.9619C12.6307 20.9871 12.5657 21 12.5 21H11.5C11.4343 21 11.3693 20.9871 11.3087 20.9619C11.248 20.9368 11.1929 20.9 11.1464 20.8536C11.1 20.8071 11.0632 20.752 11.0381 20.6913C11.0129 20.6307 11 20.5657 11 20.5V5H3.5C3.43434 5 3.36932 4.98707 3.30866 4.96194C3.248 4.93681 3.19288 4.89998 3.14645 4.85355C3.10002 4.80712 3.06319 4.752 3.03806 4.69134C3.01293 4.63068 3 4.56566 3 4.5V3.5C3 3.43434 3.01293 3.36932 3.03806 3.30866C3.06319 3.248 3.10002 3.19288 3.14645 3.14645C3.19288 3.10002 3.248 3.06319 3.30866 3.03806C3.36932 3.01293 3.43434 3 3.5 3H20.5C20.5657 3 20.6307 3.01293 20.6913 3.03806C20.752 3.06319 20.8071 3.10002 20.8536 3.14645C20.9 3.19288 20.9368 3.248 20.9619 3.30866C20.9871 3.36932 21 3.43434 21 3.5V4.5C21 4.63261 20.9473 4.75979 20.8536 4.85355C20.7598 4.94732 20.6326 5 20.5 5H13Z" fill="#3370FF"/>
</svg>
`

    const icon_calendar_outlined = `<svg width="16" height="16" viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg">
<path d="M4.83301 1.33398C5.10915 1.33398 5.33301 1.55784 5.33301 1.83398V2.00065H10.6663V1.83398C10.6663 1.55784 10.8902 1.33398 11.1663 1.33398H11.4997C11.7758 1.33398 11.9997 1.55784 11.9997 1.83398V2.00065H13.9997C14.3679 2.00065 14.6663 2.29913 14.6663 2.66732V14.0007C14.6663 14.3688 14.3679 14.6673 13.9997 14.6673H1.99967C1.63148 14.6673 1.33301 14.3688 1.33301 14.0007L1.33301 2.66732C1.33301 2.29913 1.63148 2.00065 1.99967 2.00065H3.99967V1.83398C3.99967 1.55784 4.22353 1.33398 4.49967 1.33398H4.83301ZM10.6663 3.33398H5.33301V3.50065C5.33301 3.77679 5.10915 4.00065 4.83301 4.00065H4.49967C4.22353 4.00065 3.99967 3.77679 3.99967 3.50065V3.33398H2.66634V13.334H13.333V3.33398H11.9997V3.50065C11.9997 3.77679 11.7758 4.00065 11.4997 4.00065H11.1663C10.8902 4.00065 10.6663 3.77679 10.6663 3.50065V3.33398ZM5.99967 6.83398C5.99967 6.55784 5.77582 6.33398 5.49967 6.33398H4.49967C4.22353 6.33398 3.99967 6.55784 3.99967 6.83398V7.83398C3.99967 8.11013 4.22353 8.33398 4.49967 8.33398H5.49967C5.77582 8.33398 5.99967 8.11013 5.99967 7.83398V6.83398ZM6.99967 6.83398C6.99967 6.55784 7.22353 6.33398 7.49967 6.33398H8.49967C8.77582 6.33398 8.99967 6.55784 8.99967 6.83398V7.83398C8.99967 8.11013 8.77582 8.33398 8.49967 8.33398H7.49967C7.22353 8.33398 6.99967 8.11013 6.99967 7.83398V6.83398ZM5.99967 9.83398C5.99967 9.55784 5.77582 9.33398 5.49967 9.33398H4.49967C4.22353 9.33398 3.99967 9.55784 3.99967 9.83398V10.834C3.99967 11.1101 4.22353 11.334 4.49967 11.334H5.49967C5.77582 11.334 5.99967 11.1101 5.99967 10.834V9.83398ZM6.99967 9.83398C6.99967 9.55784 7.22353 9.33398 7.49967 9.33398H8.49967C8.77582 9.33398 8.99967 9.55784 8.99967 9.83398V10.834C8.99967 11.1101 8.77582 11.334 8.49967 11.334H7.49967C7.22353 11.334 6.99967 11.1101 6.99967 10.834V9.83398ZM11.9997 6.83398C11.9997 6.55784 11.7758 6.33398 11.4997 6.33398H10.4997C10.2235 6.33398 9.99967 6.55784 9.99967 6.83398V7.83398C9.99967 8.11013 10.2235 8.33398 10.4997 8.33398H11.4997C11.7758 8.33398 11.9997 8.11013 11.9997 7.83398V6.83398Z" fill="#3370FF"/>
<path d="M11.9997 9.83398C11.9997 9.55784 11.7758 9.33398 11.4997 9.33398H10.4997C10.2235 9.33398 9.99967 9.55784 9.99967 9.83398V10.834C9.99967 11.1101 10.2235 11.334 10.4997 11.334H11.4997C11.7758 11.334 11.9997 11.1101 11.9997 10.834V9.83398Z"  fill="#3370FF" />
</svg>
`
    const icon_number_outlined = `<svg width="24" height="24" viewBox="0 0 24 24"  xmlns="http://www.w3.org/2000/svg">
<path d="M8.70351 2C8.94543 2 9.13197 2.21307 9.1 2.45287L8.67565 5.63553H15.9457L16.3842 2.34714C16.4107 2.14841 16.5802 2 16.7807 2H18.0359C18.2778 2 18.4643 2.21307 18.4324 2.45287L18.008 5.63553H21.7283C21.9492 5.63553 22.1283 5.81461 22.1283 6.03553V7.29781C22.1283 7.51872 21.9492 7.69781 21.7283 7.69781H17.733L16.7636 14.9689H20.3949C20.6158 14.9689 20.7949 15.1479 20.7949 15.3689V16.6311C20.7949 16.8521 20.6158 17.0311 20.3949 17.0311H16.4886L15.8724 21.6529C15.8459 21.8516 15.6763 22 15.4759 22H14.2207C13.9787 22 13.7922 21.7869 13.8242 21.5471L14.4263 17.0311H7.15623L6.54 21.6529C6.5135 21.8516 6.34399 22 6.14351 22H4.88831C4.64639 22 4.45985 21.7869 4.49182 21.5471L5.09395 17.0311H1.19493C0.974016 17.0311 0.794929 16.8521 0.794929 16.6311V15.3689C0.794929 15.1479 0.974015 14.9689 1.19493 14.9689H5.36892L6.3384 7.69781H2.52826C2.30735 7.69781 2.12826 7.51872 2.12826 7.29781V6.03553C2.12826 5.81461 2.30735 5.63553 2.52826 5.63553H6.61337L7.05182 2.34714C7.07831 2.14841 7.24783 2 7.44831 2H8.70351ZM14.7013 14.9689L15.6707 7.69781H8.40067L7.4312 14.9689H14.7013Z" fill="#04B49C"/>
</svg>
`
    editor.ui.registry.addIcon('emoticons', emoticons)
    editor.ui.registry.addIcon('icon_calendar_outlined', icon_calendar_outlined)
    editor.ui.registry.addIcon('icon_number_outlined', icon_number_outlined)
    editor.ui.registry.addIcon('icon_text_outlined', icon_text_outlined)

    const iconMap = [
      'icon_text_outlined',
      'icon_calendar_outlined',
      'icon_number_outlined',
      'icon_number_outlined',
      'icon_number_outlined'
    ]

    editor.ui.registry.addSplitButton('splitDateButton', {
      icon: 'emoticons',
      tooltip: '图表选中字段',
      onAction: _ => () => {
        editor.insertContent('')
      },
      onItemAction: (_, value) => {
        fieldSelect(value)
      },
      fetch: callback => {
        const items = props.fieldList.map(ele => {
          return {
            id: ele.id,
            icon: iconMap[ele.deType],
            type: 'choiceitem',
            text: ele.name,
            value: ele.name
          }
        })
        callback(items)
      }
    })
  }
})
const viewInit = () => {
  tinymce.init({})
}
const fieldSelect = name => {
  const ed = tinymce.editors[tinymceId]
  const obj = props.fieldList.find(ele => ele.name === name)
  const field = {
    id: obj.id,
    name: obj.name,
    backgroundColor: obj.groupType === 'd' ? '#3370FF33' : '#00D6B933',
    color: obj.groupType === 'd' ? '#2B5FD9' : '#04B49C'
  }
  const fieldId = 'changeText-' + field.id || ''
  const value =
    `<span style="background: ${field.backgroundColor};color: ${field.color}" id="` +
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
const moreBarElementClick = () => {
  if (!moreBarElement) return
  if (!document.querySelector('.tox.tox-tinymce-aux')?.children.length) return
  moreBarElement.nextSibling.querySelector('.tox-tbtn').click()
}

useEmitt({
  name: 'moreBarElementClick',
  callback: moreBarElementClick
})

let moreBarElement = null
onMounted(() => {
  setTimeout(() => {
    moreBarElement = document.querySelectorAll(
      '.de-tinymce-container_alarm .tox-toolbar__primary .tox-toolbar__group'
    )[4]
  }, 1000)
})

onBeforeUnmount(() => {
  moreBarElement = null
})

defineExpose({
  viewInit
})
</script>

<style lang="less">
.de-tinymce-container_alarm {
  --ed-input-text-color: var(--ed-text-color-regular);
  --ed-input-border: var(--ed-border);
  --ed-input-hover-border: var(--ed-border-color-hover);
  --ed-input-focus-border: var(--ed-color-primary);
  --ed-input-transparent-border: 0 0 0 1px transparent inset;
  --ed-input-border-color: var(--ed-border-color);
  --ed-input-border-radius: var(--ed-border-radius-base);
  --ed-input-bg-color: var(--ed-fill-color-blank);
  --ed-input-icon-color: var(--ed-text-color-placeholder);
  --ed-input-placeholder-color: var(--ed-text-color-placeholder);
  --ed-input-hover-border-color: var(--ed-border-color-hover);
  --ed-input-clear-hover-color: var(--ed-text-color-secondary);
  --ed-input-focus-border-color: var(--ed-color-primary);
  --ed-input-width: 100%;
  height: fit-content;
  max-height: 300px;
  overflow-y: scroll;
  padding: 0;
  .tox-statusbar {
    display: none !important;
  }

  .tox-split-button {
    height: 24px !important;
  }
}
</style>
<style>
.tox {
  z-index: 2213 !important;
}
</style>
