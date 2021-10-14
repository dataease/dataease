<template>
  <div v-if="editMode == 'edit'" class="v-text" @keydown="handleKeydown" @keyup="handleKeyup">
    <!-- tabindex >= 0 使得双击时聚集该元素 -->
    <div
      v-if="canEdit"
      ref="text"
      :contenteditable="canEdit"
      :class="{ canEdit }"
      :tabindex="element.id"
      :style="{ verticalAlign: element.style.verticalAlign }"
      @dblclick="setEdit"
      @paste="clearStyle"
      @mousedown="handleMousedown"
      @blur="handleBlur"
      @input="handleInput"
      v-html="element.propValue"
    />
    <div v-if="!canEdit" :style="{ verticalAlign: element.style.verticalAlign }" @dblclick="setEdit" v-html="element.propValue" />
  </div>
  <div v-else class="v-text">
    <div :style="{ verticalAlign: element.style.verticalAlign }" v-html="textInfo" />
  </div>
</template>

<script>
import { keycodes } from '@/components/canvas/utils/shortcutKey.js'

export default {
  props: {
    // eslint-disable-next-line vue/require-default-prop
    propValue: {
      type: String,
      require: true
    },
    // eslint-disable-next-line vue/require-default-prop
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
    }

  },
  data() {
    return {
      canEdit: false,
      ctrlKey: 17,
      isCtrlDown: false
    }
  },
  computed: {
    textInfo() {
      if (this.element && this.element.hyperlinks && this.element.hyperlinks.enable) {
        return "<a title='" + this.element.hyperlinks.content + "' target='" + this.element.hyperlinks.openMode + "' href='" + this.element.hyperlinks.content + "'>" + this.element.propValue + '</a>'
      } else {
        return this.element.propValue
      }
    }
  },

  watch: {
    active: {
      handler(newVal, oldVla) {
        this.removeSelectText()
      },
      deep: true
    }
  },
  methods: {
    handleInput(e) {
      this.$emit('input', this.element, e.target.innerHTML)
      this.$store.commit('recordStyleChange')
    },

    handleKeydown(e) {
      if (e.keyCode === this.ctrlKey) {
        this.isCtrlDown = true
      } else if (this.isCtrlDown && this.canEdit && keycodes.includes(e.keyCode)) {
        e.stopPropagation()
      } else if (e.keyCode === 46) { // deleteKey
        e.stopPropagation()
      }
    },

    handleKeyup(e) {
      if (e.keyCode === this.ctrlKey) {
        this.isCtrlDown = false
      }
    },

    handleMousedown(e) {
      if (this.canEdit) {
        e.stopPropagation()
      }
    },

    clearStyle(e) {
      e.preventDefault()
      const clp = e.clipboardData
      const text = clp.getData('text/plain') || ''
      if (text !== '') {
        document.execCommand('insertText', false, text)
      }

      this.$emit('input', this.element, e.target.innerHTML)
    },

    handleBlur(e) {
      this.element.propValue = e.target.innerHTML || '&nbsp;'
      this.canEdit = false
    },

    setEdit() {
      this.canEdit = true
      // // // 聚焦到单元格
      // setTimeout(() => {
      //   this.$refs['text'].focus()
      // }, 500)
      // 全选
      this.selectText(this.$refs.text)
    },

    selectText(element) {
      const selection = window.getSelection()
      const range = document.createRange()
      // range.selectNodeContents(element)
      // selection.removeAllRanges()
      // selection.addRange(range)
    },

    removeSelectText() {
      const selection = window.getSelection()
      selection.removeAllRanges()
    }
  }
}
</script>

<style lang="scss" scoped>
.v-text {
    width: 100%;
    height: 100%;
    display: table;

    div {
        display: table-cell;
        width: 100%;
        height: 100%;
        outline: none;

    }

    .canEdit {
        cursor: text;
        height: 100%;
    }
}
::v-deep a:hover {
  text-decoration: underline!important;
  color: blue!important;
}

</style>
