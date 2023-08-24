<template>
  <editor
    v-model="formatData"
    :lang="mode"
    :theme="theme"
    :height="height"
    @init="editorInit"
  />
</template>

<script>
import { formatJson, formatXml } from './format-utils'

export default {
  name: 'CodeEdit',
  components: { editor: require('vue2-ace-editor') },
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
        return true
      }
    },
    readOnly: {
      type: Boolean,
      default() {
        return false
      }
    },
    mode: {
      type: String,
      default() {
        return 'text'
      }
    },
    modes: {
      type: Array,
      default() {
        return ['text', 'json', 'xml']
      }
    }
  },
  data() {
    return {
      formatData: ''
    }
  },
  watch: {
    formatData() {
      this.$emit('update:data', this.formatData)
    },
    mode() {
      this.format()
    }
  },
  mounted() {
    if (!this.data) {
      this.formatData = ''
    }
    this.format()
  },
  methods: {
    editorInit: function(editor) {
      require('brace/ext/language_tools')
      this.modes.forEach(mode => {
        require('brace/mode/' + mode)
      })
      require('brace/theme/' + this.theme)
      require('brace/snippets/javascript')
      if (this.readOnly) {
        editor.setReadOnly(true)
      }
      if (this.init) {
        this.init(editor)
      }
    },
    format() {
      if (this.enableFormat) {
        if (this.data) {
          switch (this.mode) {
            case 'json':
              this.formatData = formatJson(this.data)
              break
            case 'xml':
              this.formatData = formatXml(this.data)
              break
            default:
              this.formatData = this.data
          }
        }
      } else {
        this.formatData = this.data
      }
    }
  }
}
</script>

<style scoped>

</style>
