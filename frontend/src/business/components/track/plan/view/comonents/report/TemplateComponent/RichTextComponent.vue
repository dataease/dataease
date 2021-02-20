<template>

  <common-component>

    <template v-slot:header>
      <el-input v-if="!isReportView" v-model="preview.title"></el-input>
      <span v-if="isReportView" class="title">{{preview.title}}</span>
    </template>

    <template>
      <ckeditor v-if="!isReportView" :editor="editor" v-model="preview.content" :config="editorConfig"></ckeditor>
      <div class="rich-text-content" v-if="isReportView" v-html="preview.content"></div>
    </template>

  </common-component>

</template>

<script>
    import CommonComponent from "./CommonComponent";
    import ClassicEditor from '@ckeditor/ckeditor5-build-classic';

    export default {
      name: "RichTextComponent",
      components: {CommonComponent},
      data() {
        return {
          editor: ClassicEditor,
          // editorData: '<p>Content of the editor.</p>',
          editorConfig: {
            toolbar: [ 'heading', '|', 'bold', 'italic', 'link', 'bulletedList', 'numberedList', 'blockQuote' ,'insertTable', '|','undo', 'redo']
          }
        }
      },
      props: {
        preview: {
          type: Object,
          default() {
            return {
              title: '',
              content: '<p>Content of the editor.</p>'
            }
          }
        },
        isReportView:
        {
          type: Boolean,
          default: false
        }
      }
    }
</script>

<style scoped>

  .rich-text-content >>> .table td {
    border: solid 1px #e6e6e6;
    min-width: 2em;
    padding: .4em;
  }


</style>
