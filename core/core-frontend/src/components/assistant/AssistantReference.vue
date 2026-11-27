<script setup lang="ts" generic="T extends string | number">
import { computed, ref, useId, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'

const props = defineProps<{
  options: { value: T; label: string }[]
  text?: string
  caption: string
  showLabel: string
  hideLabel: string
  origin?: { numberLabel: string; questionId: string }
  hint?: string
}>()
const source = defineModel<T>({ required: true })
const emit = defineEmits<{ (event: 'locate', questionId: string): void }>()
const { t } = useI18n()
const sourceId = useId()
const detailId = useId()
const hintId = useId()
const open = ref(false)
const toggle = ref<HTMLButtonElement>()
const closeOnEscape = (event: KeyboardEvent) => {
  if (!open.value) return
  event.stopPropagation()
  open.value = false
  toggle.value?.focus()
}
watch(source, () => {
  open.value = false
})
const summary = computed(() => {
  const lines = (props.text ?? '').split(/\r\n|\r|\n/)
  const index = lines.findIndex(line => line.trim())
  return index < 0 ? '' : lines[index].trim() + (index < lines.length - 1 ? '…' : '')
})
</script>

<template>
  <div class="assistant-reference" @keydown.esc="closeOnEscape">
    <div class="reference-bar">
      <label :for="sourceId">{{ t('ai_conversation.include_basis') }}</label>
      <select
        :id="sourceId"
        v-model="source"
        :aria-label="t('ai_conversation.basis_source')"
        :aria-describedby="props.hint ? hintId : undefined"
        :title="props.hint"
      >
        <option v-for="option in props.options" :key="option.value" :value="option.value">
          {{ option.label }}
        </option>
      </select>
      <button
        v-if="summary"
        ref="toggle"
        type="button"
        :aria-expanded="open"
        :aria-controls="detailId"
        @click="open = !open"
      >
        {{ open ? props.hideLabel : props.showLabel }}
      </button>
    </div>
    <code v-if="summary && !open" class="reference-line">{{ summary }}</code>
    <div v-if="open && summary" :id="detailId" class="reference-detail">
      <strong v-if="props.origin"
        ><button
          type="button"
          :aria-label="
            t('ai_conversation.locate_question', { suggestion: props.origin.numberLabel })
          "
          @click="emit('locate', props.origin.questionId)"
        >
          {{ props.origin.numberLabel }}</button
        >{{ props.caption.slice(props.origin.numberLabel.length) }}</strong
      >
      <strong v-else>{{ props.caption }}</strong>
      <pre tabindex="0">{{ props.text }}</pre>
    </div>
    <small v-if="props.hint" :id="hintId">{{ props.hint }}</small>
  </div>
</template>

<style scoped lang="less">
.assistant-reference {
  min-width: 0;
  color: var(--ed-text-color-primary);
}
.reference-bar {
  display: flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
  label,
  button {
    flex: none;
    font-size: 13px;
  }
  select {
    flex: 1;
    min-width: 0;
    width: 0;
    height: 28px;
    padding: 0 4px;
    border: 1px solid var(--ed-border-color);
    border-radius: 4px;
    color: inherit;
    background: var(--ed-bg-color);
    text-overflow: ellipsis;
    cursor: pointer;
  }
}
button {
  border: 0;
  padding: 0;
  color: var(--ed-color-primary);
  background: transparent;
  cursor: pointer;
}
.reference-line {
  display: block;
  overflow: hidden;
  color: var(--ed-text-color-secondary);
  white-space: nowrap;
  text-overflow: ellipsis;
}
.reference-detail {
  margin-top: 4px;
  padding: 6px 8px;
  border: 1px solid var(--ed-border-color);
  border-radius: 4px;
  background: color-mix(in srgb, var(--ed-text-color-primary) 5%, var(--ed-bg-color));
  overflow-wrap: anywhere;
  strong {
    font-size: 13px;
  }
  strong button {
    font: inherit;
    text-decoration: underline;
  }
  pre {
    overflow: auto;
    max-height: min(120px, 18vh);
    white-space: pre;
    overscroll-behavior: contain;
  }
}
</style>
