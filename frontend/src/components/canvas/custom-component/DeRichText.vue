<template>
  <div class="rich-main-class" @dblclick="setEdit">
    <Editor
      v-if="editShow"
      :id="tinymceId"
      v-model="myValue"
      style="width: 100%;height: 100%"
      :init="init"
      :disabled="!canEdit"
      @onClick="onClick"
    />
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
import 'tinymce/plugins/contextmenu'// contextmenu
import 'tinymce/plugins/directionality'
import 'tinymce/plugins/nonbreaking'
import 'tinymce/plugins/pagebreak'
import { mapState } from 'vuex'

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
      editShow: true,
      canEdit: false,
      // 初始化配置
      tinymceId: 'tinymce-' + this.element.id,
      myValue: this.propValue,
      init: {
        selector: '#tinymce-' + this.element.id,
        toolbar_items_size: 'small',
        language_url: '/tinymce/langs/zh_CN.js', // 汉化路径是自定义的，一般放在public或static里面
        language: 'zh_CN',
        skin_url: '/tinymce/skins/ui/oxide', // 皮肤
        content_css: '/tinymce/skins/content/default/content.css',
        plugins: 'advlist autolink link image lists charmap  media wordcount table contextmenu directionality pagebreak', // 插件
        // 工具栏
        toolbar: 'undo redo |fontselect fontsizeselect |forecolor backcolor bold italic |underline strikethrough link| formatselect |' +
            'alignleft aligncenter alignright | bullist numlist |' +
            ' blockquote subscript superscript removeformat | table image media | fullscreen ' +
            '| bdmap indent2em lineheight formatpainter axupimgs',
        toolbar_location: '/',
        font_formats: '微软雅黑=Microsoft YaHei;宋体=SimSun;黑体=SimHei;仿宋=FangSong;华文黑体=STHeiti;华文楷体=STKaiti;华文宋体=STSong;华文仿宋=STFangsong;Andale Mono=andale mono,times;Arial=arial,helvetica,sans-serif;Arial Black=arial black,avant garde;Book Antiqua=book antiqua,palatino;Comic Sans MS=comic sans ms,sans-serif;Courier New=courier new,courier;Georgia=georgia,palatino;Helvetica=helvetica;Impact=impact,chicago;Symbol=symbol;Tahoma=tahoma,arial,helvetica,sans-serif;Terminal=terminal,monaco;Times New Roman=times new roman,times;Trebuchet MS=trebuchet ms,geneva;Verdana=verdana,geneva;Webdings=webdings;Wingdings=wingdings',
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
        this.reShow()
      }
    },
    // 监听内容变化
    propValue(newValue) {
      this.myValue = (newValue == null ? '' : newValue)
    },
    myValue(newValue) {
      this.element.propValue = newValue
      this.$store.commit('canvasChange')
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
      if (this.editStatus) {
        this.canEdit = true
        this.element.editing = true
        this.reShow()
      }
    },
    reShow() {
      this.editShow = false
      this.$nextTick(() => {
        this.editShow = true
      })
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
  ::v-deep ol {
    display: block!important;
    list-style-type: decimal;
    margin-block-start: 1em!important;
    margin-block-end: 1em!important;
    margin-inline-start: 0px!important;
    margin-inline-end: 0px!important;
    padding-inline-start: 40px!important;
  }
  ::v-deep ul {
    display: block!important;
    list-style-type: disc;
    margin-block-start: 1em!important;
    margin-block-end: 1em!important;
    margin-inline-start: 0px!important;
    margin-inline-end: 0px!important;
    padding-inline-start: 40px!important;
  }
  ::v-deep li {
    display: list-item!important;
    text-align: -webkit-match-parent!important;
  }
</style>

