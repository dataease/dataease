<template>
  <div class="de-tinymce-container ed-textarea__inner">
    <editor class="de-tinymce-content" v-model="myValue" :id="tinymceId" :init="init" />
  </div>
</template>
<script lang="ts" setup>
import { ref, toRefs, watch } from 'vue'
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
  inline: propTypes.bool.def(true)
})
const myValue = ref()
const { modelValue, inline } = toRefs(props)
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

const tinymceId = 'tinymce-view-pf'
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
    'undo redo |fontselect fontsizeselect |forecolor backcolor bold italic |underline strikethrough link lineheight| formatselect |' +
    'alignleft aligncenter alignright | bullist numlist |' +
    ' blockquote subscript superscript removeformat | table image media ',
  toolbar_location: '/',
  font_formats:
    '阿里巴巴普惠体=阿里巴巴普惠体 3.0 55 Regular L3;微软雅黑=Microsoft YaHei;宋体=SimSun;黑体=SimHei;仿宋=FangSong;华文黑体=STHeiti;华文楷体=STKaiti;华文宋体=STSong;华文仿宋=STFangsong;Andale Mono=andale mono,times;Arial=arial,helvetica,sans-serif;Arial Black=arial black,avant garde;Book Antiqua=book antiqua,palatino;Comic Sans MS=comic sans ms,sans-serif;Courier New=courier new,courier;Georgia=georgia,palatino;Helvetica=helvetica;Impact=impact,chicago;Symbol=symbol;Tahoma=tahoma,arial,helvetica,sans-serif;Terminal=terminal,monaco;Times New Roman=times new roman,times;Trebuchet MS=trebuchet ms,geneva;Verdana=verdana,geneva;Webdings=webdings;Wingdings=wingdings',
  fontsize_formats: '12px 14px 16px 18px 20px 22px 24px 28px 32px 36px 48px 56px 72px', // 字体大小
  menubar: false,
  placeholder: '',
  outer_placeholder: '双击输入文字',
  inline: inline.value,
  branding: true
})
tinymce.init({})
</script>

<style lang="less" scoped>
.de-tinymce-container {
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
  width: 100%;
  height: 80px;
  .de-tinymce-content {
    width: 100%;
    height: 100%;
    overflow-y: auto;
    outline: none !important;
    border: none !important;
  }
}

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
</style>
