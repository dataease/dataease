<template>
  <div style="width: 100%">
    <div style="width: 600px; height: 400px; margin-left: 200px; margin-top: 100px">
      <editor v-model="editorValue" :init="initOptions"></editor>
    </div>
  </div>
</template>

<script setup>
import { defineEmits, defineProps, onMounted, ref, toRefs, watch } from 'vue'
import 'tinymce/tinymce'
import Editor from '@tinymce/tinymce-vue'
import 'tinymce/models/dom'

// 外觀
import 'tinymce/themes/silver'

// Icon
import 'tinymce/icons/default'
// 語言包
import 'tinymce-i18n/langs6/zh-Hans.js'

// 引入插件
// 源代码
import 'tinymce/plugins/code'

const emit = defineEmits(['update:modelValue'])

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  plugins: {
    type: [String, Array],
    default: 'code'
  },
  toolbar: {
    type: [String, Array],
    default: 'undo redo | styles | bold italic cut copy paste forecolor removeformat code'
  }
})

const { modelValue } = toRefs(props)
const editorValue = ref('')
const calcData = (view, callback) => {
  // do
  callback?.()
}

const renderChart = () => {
  // do
}
defineExpose({
  calcData,
  renderChart
})

const initOptions = ref({
  language: 'zh-Hans',
  height: 500,
  skin: false,
  menubar: false,
  inline: true, // 开启内联模式
  content_css: false,
  plugins: props.plugins,
  toolbar: props.toolbar,
  toolbar_mode: 'sliding',
  ...getPasteOption(),
  ...getImageOption()
})
watch(modelValue, newValue => {
  editorValue.value = newValue
})

watch(editorValue, newValue => {
  emit('update:modelValue', newValue)
})

onMounted(() => {
  console.log('初始化tinymce')
})

/*
 * 图片上传 配置项
 * */
function getImageOption() {
  return {
    // images_upload_url: 'http://192.168.3.103:9890/common/upload',
    images_upload_handler: (blobInfo, progress) =>
      new Promise(async (resolve, reject) => {
        // console.log(blobInfo.blobUri())
        const formData = new FormData()
        formData.append('file', blobInfo.blob(), blobInfo.filename())
        // console.log(formData)
        // 模拟调用图片上传接口之后的结果，返回的数据如下格式
        // 格式非固定，根据业务调整
        const res = {
          code: 200,
          url: 'https://dummyimage.com/600x400'
        }
        // console.log(res)
        if (res.code === 200) {
          // 给出的是url地址
          resolve(res.url)
        } else {
          reject('上传图片失败', res)
        }
      })
  }
}

/*
 *  复制粘贴插件 配置项
 * https://www.tiny.cloud/docs/tinymce/6/copy-and-paste/
 * */
function getPasteOption() {
  return {
    paste_preprocess: (editor, args) => {
      console.log(args.content)
    },
    // paste_remove_styles_if_webkit: false,
    /*
     * 此选项允许您指定在 WebKit 中粘贴时要保留的样式。WebKit 有一个怪癖，
     * 它将获取元素的所有计算 CSS 属性并将它们添加到编辑器中的 span 中。由于大多数用户不希望在整个文档中添加随机跨度，
     * 因此我们需要手动清理它，直到修复错误。此选项默认为'none'但可以设置为'all'或要保留的特定样式列表。
     * */
    paste_webkit_styles: 'color'
  }
}
</script>

<style scoped></style>
