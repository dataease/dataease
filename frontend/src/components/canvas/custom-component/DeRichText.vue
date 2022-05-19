<template>
  <div v-if="editStatus" class="rich-main-class">
    <Editor
      v-show="canEdit"
      :id="tinymceId"
      v-model="myValue"
      style="width: 100%;height: 100%"
      :init="init"
      :disabled="disabled"
      @onClick="onClick"
    />
    <div v-show="!canEdit" style="width: 100%;height: 100%" @dblclick="setEdit" v-html="myValue" />
  </div>
  <div v-else class="rich-main-class">
    <div v-html="myValue" />
  </div>

</template>

<script>
import tinymce from 'tinymce/tinymce' // tinymce默认hidden，不引入不显示
import Editor from '@tinymce/tinymce-vue'// 编辑器引入
import 'tinymce/themes/silver/theme'// 编辑器主题
import 'tinymce/icons/default' // 引入编辑器图标icon，不引入则不显示对应图标
// 引入编辑器插件（基本免费插件都在这儿了）
import 'tinymce/plugins/advlist' // 高级列表
import 'tinymce/plugins/autolink' // 自动链接
import 'tinymce/plugins/link' // 超链接
import 'tinymce/plugins/image' // 插入编辑图片
import 'tinymce/plugins/lists' // 列表插件
import 'tinymce/plugins/charmap' // 特殊字符
import 'tinymce/plugins/media' // 插入编辑媒体
import 'tinymce/plugins/wordcount'// 字数统计
import 'tinymce/plugins/table'// 表格
import { mapState } from 'vuex'

// const fonts = [
//   '宋体=宋体',
//   '微软雅黑=微软雅黑',
//   '新宋体=新宋体',
//   '黑体=黑体',
//   '楷体=楷体',
//   '隶书=隶书',
//   'Courier New=courier new,courier',
//   'AkrutiKndPadmini=Akpdmi-n',
//   'Andale Mono=andale mono,times',
//   'Arial=arial,helvetica,sans-serif',
//   'Arial Black=arial black,avant garde',
//   'Book Antiqua=book antiqua,palatino',
//   'Comic Sans MS=comic sans ms,sans-serif',
//   'Courier New=courier new,courier',
//   'Georgia=georgia,palatino',
//   'Helvetica=helvetica',
//   'Impact=impact,chicago',
//   'Symbol=symbol',
//   'Tahoma=tahoma,arial,helvetica,sans-serif',
//   'Terminal=terminal,monaco',
//   'Times New Roman=times new roman,times',
//   'Trebuchet MS=trebuchet ms,geneva',
//   'Verdana=verdana,geneva',
//   'Webdings=webdings',
//   'Wingdings=wingdings,zapf dingbats'
// ]

export default {
  name: 'DeRichText',
  components: {
    Editor
  },
  props: {
    propValue: {
      type: String,
      require: true
    },
    element: {
      type: Object
    },
    editMode: {
      type: String,
      require: false,
      default: 'preview'
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
  },
  data() {
    return {
      canEdit: false,
      // 初始化配置
      tinymceId: 'tinymce',
      myValue: this.propValue,
      init: {
        selector: '#tinymce',
        toolbar_items_size: 'small',
        language_url: '/tinymce/langs/zh_CN.js', // 汉化路径是自定义的，一般放在public或static里面
        language: 'zh_CN',
        skin_url: '/tinymce/skins/ui/oxide', // 皮肤
        content_css: '/tinymce/skins/content/default/content.css',
        plugins: 'advlist autolink link image lists charmap  media wordcount table', // 插件
        // 工具栏
        toolbar: 'undo redo |  fontsizeselect | bold italic forecolor backcolor| alignleft aligncenter alignright | lists image media table link',
        toolbar_location: '/',
        fontsize_formats: '12px 14px 16px 18px 20px 22px 24px 28px 32px 36px 48px 56px 72px', // 字体大小
        menubar: false,
        placeholder: '双击输入文字',
        inline: true, // 开启内联模式
        branding: false
      }
    }
  },
  computed: {
    editStatus() {
      return this.editMode === 'edit' && !this.mobileLayoutStatus
    },
    ...mapState([
      'mobileLayoutStatus'
    ])
  },
  watch: {
    // 监听内容变化
    active(val) {
      if (!val) {
        this.canEdit = false
      }
    },
    // 监听内容变化
    propValue(newValue) {
      this.myValue = (newValue == null ? '' : newValue)
    },
    myValue(newValue) {
      this.element.propValue = newValue
      this.$store.state.styleChangeTimes++
    }
  },
  mounted() {
    tinymce.init({})
  },
  methods: {
    onClick(e) {
      this.$emit('onClick', e, tinymce)
    },
    setEdit() {
      this.canEdit = true
      this.element.editing = true
    }
  }
}
</script>

<style lang="scss" scoped>
  .rich-main-class {
    width: 100%;
    height: 100%;
    overflow-y: auto!important;
  }
  ::-webkit-scrollbar {
    width: 0px!important;
    height: 0px!important;
  }
</style>

