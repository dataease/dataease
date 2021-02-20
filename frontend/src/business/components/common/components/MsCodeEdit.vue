<template>
    <editor v-model="formatData" :lang="mode" @init="editorInit" :theme="theme" :height="height"/>
</template>

<script>
  import {formatHtml, formatJson, formatXml} from "../../../../common/js/format-utils";
  import toDiffableHtml from 'diffable-html';

    export default {
      name: "MsCodeEdit",
      components: { editor: require('vue2-ace-editor')},
      data() {
        return {
          formatData: ''
        }
      },
      props: {
        height: [String, Number],
        data: {
          type: String
        },
        theme: {
          type: String,
          default() {
            return 'chrome'
          }
        },
        init: {
          type: Function
        },
        enableFormat: {
          type: Boolean,
          default() {
            return true;
          }
        },
        readOnly: {
          type: Boolean,
          default() {
            return false;
          }
        },
        mode: {
          type: String,
          default() {
            return 'text';
          }
        },
        modes: {
          type: Array,
          default() {
            return ['text', 'json', 'xml', 'html'];
          }
        }
      },
      mounted() {
        if (!this.data) {
          this.formatData = "";
        }
        this.format();
      },
      watch: {
        formatData() {
          this.$emit('update:data', this.formatData);
        },
        mode() {
          this.format();
        }
      },
      methods: {
        editorInit: function (editor) {
          require('brace/ext/language_tools') //language extension prerequsite...
          this.modes.forEach(mode => {
            require('brace/mode/' + mode); //language
          });
          require('brace/theme/' + this.theme)
          require('brace/snippets/javascript') //snippet
          if (this.readOnly) {
            editor.setReadOnly(true);
          }
          if (this.init) {
            this.init(editor);
          }
        },
        format() {
          if (this.enableFormat) {
            if (this.data) {
              switch (this.mode) {
                case 'json':
                  this.formatData = formatJson(this.data);
                  break;
                case 'html':
                  this.formatData = toDiffableHtml(this.data);
                  break;
                case 'xml':
                  this.formatData = formatXml(this.data);
                  break;
                default:
                  this.formatData = this.data;
              }
            }
          } else {
            this.formatData = this.data;
          }
        }
      }
    }
</script>

<style scoped>

</style>
