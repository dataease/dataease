<script setup lang="ts">
import { ref, watchEffect } from 'vue'
import { Marked } from 'marked'
import DOMPurify from 'dompurify'
import { ElMessage } from 'element-plus-secondary'
import useClipboard from 'vue-clipboard3'
import { useI18n } from '@/hooks/web/useI18n'

const props = defineProps<{ text: string; suggestion?: string | null }>()
const { t } = useI18n()
const { toClipboard } = useClipboard()
const content = ref<HTMLElement>()
const escape = (value: string) =>
  value.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
const markdown = new Marked({
  gfm: true,
  breaks: true,
  renderer: {
    html: token => escape(token.text),
    image: token => escape(token.text)
  }
})

watchEffect(
  () => {
    if (!content.value) return
    let html: string
    try {
      html = markdown.parse(props.text, { async: false })
    } catch {
      content.value.textContent = props.text
      return
    }
    const fragment = DOMPurify.sanitize(html, {
      RETURN_DOM_FRAGMENT: true,
      ALLOWED_TAGS: [
        'p',
        'br',
        'strong',
        'em',
        'del',
        'ul',
        'ol',
        'li',
        'blockquote',
        'pre',
        'code',
        'table',
        'thead',
        'tbody',
        'tr',
        'th',
        'td',
        'hr',
        'a',
        'h1',
        'h2',
        'h3',
        'h4',
        'h5',
        'h6'
      ],
      ALLOWED_ATTR: ['href', 'start'],
      ALLOW_DATA_ATTR: false
    })
    const suggestion = props.suggestion?.replace(/\r\n/g, '\n').trim()
    if (suggestion) {
      for (const block of fragment.querySelectorAll('pre, p')) {
        if (block.textContent?.trim() === suggestion) block.remove()
      }
    }
    for (const pre of fragment.querySelectorAll('pre')) {
      const button = document.createElement('button')
      button.type = 'button'
      button.textContent = t('ai_conversation.copy_code')
      pre.after(button)
    }
    for (const link of fragment.querySelectorAll<HTMLAnchorElement>('a[href]')) {
      link.target = '_blank'
      link.rel = 'noopener noreferrer'
    }
    content.value.replaceChildren(fragment)
  },
  { flush: 'post' }
)

const copyExample = async (event: MouseEvent) => {
  if (!(event.target instanceof HTMLButtonElement)) return
  const code = event.target.previousElementSibling?.querySelector('code')
  if (!code) return
  try {
    await toClipboard((code.textContent ?? '').replace(/\n$/, ''))
    ElMessage.success(t('data_set.copied_successfully'))
  } catch {
    ElMessage.error(t('ai_conversation.copy_code_failed'))
  }
}
</script>

<template>
  <div ref="content" class="assistant-answer" @click="copyExample"></div>
</template>

<style scoped lang="less">
.assistant-answer {
  overflow-x: auto;
  overflow-wrap: anywhere;

  :deep(pre) {
    white-space: pre;
    overflow-x: auto;
  }

  :deep(table) {
    border-collapse: collapse;
  }

  :deep(td),
  :deep(th) {
    padding: 4px 8px;
    border: 1px solid var(--ed-border-color);
  }

  :deep(blockquote) {
    margin-inline: 0;
    padding-inline-start: 12px;
    border-inline-start: 3px solid var(--ed-border-color);
  }
}
</style>
