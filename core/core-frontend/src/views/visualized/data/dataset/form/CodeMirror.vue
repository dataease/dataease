<script lang="ts" setup>
import { sql } from '@codemirror/lang-sql'
import { basicSetup } from 'codemirror'
import { Decoration, EditorView, ViewPlugin, WidgetType, MatchDecorator } from '@codemirror/view'
import { propTypes } from '@/utils/propTypes'

const props = defineProps({
  domId: propTypes.string.def('editor'),
  height: propTypes.string.def('250px'),
  quotaMap: propTypes.arrayOf(String).def(() => []),
  dimensionMap: propTypes.arrayOf(String).def(() => [])
})

const codeComInit = (doc: string, sqlMode?: boolean) => {
  function _optionalChain(ops) {
    let lastAccessLHS = undefined
    let value = ops[0]
    let i = 1
    while (i < ops.length) {
      const op = ops[i]
      const fn = ops[i + 1]
      i += 2
      if ((op === 'optionalAccess' || op === 'optionalCall') && value == null) {
        return undefined
      }
      if (op === 'access' || op === 'optionalAccess') {
        lastAccessLHS = value
        value = fn(value)
      } else if (op === 'call' || op === 'optionalCall') {
        value = fn((...args) => value.call(lastAccessLHS, ...args))
        lastAccessLHS = undefined
      }
    }
    return value
  } //!placeholderMatcher

  const placeholderMatcher = new MatchDecorator({
    regexp: /\[(.*?)\]/g,
    decoration: match =>
      Decoration.replace({
        widget: new PlaceholderWidget(match[1])
      })
  })

  //!placeholderPlugin

  const placeholders = ViewPlugin.fromClass(
    class {
      placeholders
      constructor(view) {
        this.placeholders = placeholderMatcher.createDeco(view)
      }
      update(update) {
        this.placeholders = placeholderMatcher.updateDeco(update, this.placeholders)
      }
    },
    {
      decorations: instance => instance.placeholders,
      provide: plugin =>
        EditorView.atomicRanges.of(view => {
          return (
            _optionalChain([
              view,
              'access',
              _ => _.plugin,
              'call',
              _2 => _2(plugin),
              'optionalAccess',
              _3 => _3.placeholders
            ]) || Decoration.none
          )
        })
    }
  )

  //!placeholderWidget

  class PlaceholderWidget extends WidgetType {
    name: string
    constructor(name: string) {
      super()
      this.name = name
    }
    eq(other) {
      return this.name == other.name
    }
    toDOM() {
      let elt = document.createElement('span')
      elt.textContent = `[${this.name}]`
      const { dimensionMap, quotaMap } = props
      if (!dimensionMap?.length && !quotaMap?.length) {
        return elt
      }
      const isQuota = quotaMap.includes(this.name)
      elt.style.borderRadius = '2px'
      elt.style.margin = '0 4px'
      elt.style.padding = '0 6px'
      elt.style.background = isQuota ? 'rgba(0, 214, 185, 0.20)' : 'rgba(51, 112, 255, 0.20)'
      elt.style.color = isQuota ? '#04B49C' : '#2B5FD9'
      return elt
    }
    ignoreEvent() {
      return false
    }
  }
  const extensionsAttach = sqlMode ? [basicSetup, sql(), placeholders] : [basicSetup, placeholders]
  return new EditorView({
    doc,
    extensions: extensionsAttach,
    parent: document.querySelector(`#${props.domId}`)
  })
}

defineExpose({
  codeComInit
})
</script>

<template>
  <div :style="{ height: height }" class="editor-placeholder" :id="domId"></div>
</template>

<style lang="less" scoped>
.editor-placeholder {
  width: 100%;
}
</style>
<style lang="less">
.cm-editor,
.cm-scroller {
  height: 100% !important;
  background: #eff0f1;
}
</style>
