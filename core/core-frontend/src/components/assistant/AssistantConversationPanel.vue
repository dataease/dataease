<script
  setup
  lang="ts"
  generic="T extends { questionId: string; question: string; answer: string }"
>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, useId, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import AssistantAnswer from './AssistantAnswer.vue'

const props = defineProps<{
  title: string
  turns: T[]
  disabled?: boolean
  progress?: number
  workspace?: HTMLElement | null
  editorReserve?: number
  inputTestId?: string
  sendTestId?: string
}>()
const input = defineModel<string>({ default: '' })
const emit = defineEmits<{ (event: 'send'): void; (event: 'resize', width: number): void }>()
const { t } = useI18n()
const composing = ref(false)
const groups = computed(() => {
  const groups = new Map<string, { turn: T; index: number }[]>()
  props.turns.forEach((turn, index) => {
    const group = groups.get(turn.questionId)
    if (group) group.push({ turn, index })
    else groups.set(turn.questionId, [{ turn, index }])
  })
  return [...groups.values()]
})
const send = () => {
  if (input.value.trim() && !props.disabled) emit('send')
}
const keydown = (event: KeyboardEvent) => {
  if (
    event.key !== 'Enter' ||
    event.shiftKey ||
    event.isComposing ||
    composing.value ||
    event.keyCode === 229
  )
    return
  event.preventDefault()
  if (!event.repeat) send()
}
const conversation = ref<HTMLElement>()
const logId = useId()
const locatedQuestion = ref<string>()
let highlightTimer: number | undefined
const questionGroup = (log: HTMLElement, id: string) =>
  log.querySelector<HTMLElement>(`[data-question-id="${CSS.escape(id)}"]`)
const locateQuestion = (id: string) => {
  const log = conversation.value
  if (!log) return
  const question = questionGroup(log, id)?.querySelector<HTMLElement>('.question')
  if (!question) return
  const top =
    log.scrollTop + question.getBoundingClientRect().top - log.getBoundingClientRect().top - 12
  const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  if (reducedMotion) log.scrollTop = top
  else log.scrollTo({ top, behavior: 'smooth' })
  locatedQuestion.value = id
  window.clearTimeout(highlightTimer)
  highlightTimer = window.setTimeout(() => {
    locatedQuestion.value = undefined
  }, 1600)
}
const latestScrollTop = (log: HTMLElement) => {
  const latest = props.turns.at(-1)
  if (latest && latest.questionId !== groups.value.at(-1)?.[0].turn.questionId) {
    const group = questionGroup(log, latest.questionId)
    if (group) {
      return Math.max(
        0,
        log.scrollTop + group.getBoundingClientRect().bottom - log.getBoundingClientRect().bottom
      )
    }
  }
  return Math.max(0, log.scrollHeight - log.clientHeight)
}
const scrollToLatest = () => {
  const log = conversation.value
  if (log) log.scrollTop = latestScrollTop(log)
}
watch(
  [() => props.turns.length, () => props.progress, () => props.disabled],
  async ([count], [previous]) => {
    const log = conversation.value
    if (log && count === previous && Math.abs(latestScrollTop(log) - log.scrollTop) > 4) return
    await nextTick()
    scrollToLatest()
  }
)

const minimum = ref(0)
const maximum = ref(0)
const preferred = ref<number>()
const clamp = (width: number) => Math.min(maximum.value, Math.max(minimum.value, width))
const width = computed(() => clamp(preferred.value ?? minimum.value))
const measure = () => {
  if (!props.workspace) return
  const space = props.workspace.clientWidth
  minimum.value = Math.min(360, Math.round(window.innerWidth * 0.36), space)
  maximum.value = Math.max(minimum.value, space - (props.editorReserve ?? 240))
}
watch(width, size => emit('resize', size))
watch(() => props.editorReserve, measure)
let observer: ResizeObserver | undefined
onMounted(() => {
  scrollToLatest()
  if (props.workspace) {
    measure()
    observer = new ResizeObserver(measure)
    observer.observe(props.workspace)
    window.addEventListener('resize', measure)
  }
})
onBeforeUnmount(() => {
  window.clearTimeout(highlightTimer)
  observer?.disconnect()
  window.removeEventListener('resize', measure)
})

let drag: { id: number; x: number; width: number } | undefined
const startDrag = (event: PointerEvent) => {
  if (!event.isPrimary || event.button !== 0) return
  const grip = event.currentTarget as HTMLElement
  grip.setPointerCapture(event.pointerId)
  drag = { id: event.pointerId, x: event.clientX, width: width.value }
}
const moveDrag = (event: PointerEvent) => {
  if (drag?.id === event.pointerId) resizeTo(drag.width + drag.x - event.clientX)
}
const resizeTo = (size: number) => {
  preferred.value = size <= minimum.value ? undefined : clamp(size)
}
const resizeKey = (event: KeyboardEvent) => {
  const targets: Record<string, number> = {
    ArrowLeft: width.value + 16,
    ArrowRight: width.value - 16,
    Home: minimum.value,
    End: maximum.value
  }
  if (!(event.key in targets)) return
  event.preventDefault()
  resizeTo(targets[event.key])
}
</script>

<template>
  <aside
    class="assistant-panel"
    :aria-label="title"
    :style="width ? { width: width + 'px' } : undefined"
  >
    <div
      v-if="workspace && maximum > minimum"
      class="width-grip"
      role="separator"
      tabindex="0"
      aria-orientation="vertical"
      :aria-label="t('ai_conversation.resize_panel')"
      :aria-controls="logId"
      :aria-valuemin="minimum"
      :aria-valuemax="maximum"
      :aria-valuenow="width"
      @pointerdown="startDrag"
      @pointermove="moveDrag"
      @lostpointercapture="drag = undefined"
      @keydown="resizeKey"
      @dblclick="preferred = undefined"
    ></div>
    <header class="assistant-heading">
      <strong>{{ title }}</strong>
      <slot name="header-actions" />
    </header>
    <div :id="logId" ref="conversation" class="conversation" role="log">
      <article
        v-for="group in groups"
        :key="group[0].turn.questionId"
        :data-question-id="group[0].turn.questionId"
      >
        <p class="question" :class="{ located: locatedQuestion === group[0].turn.questionId }">
          {{ group[0].turn.question }}
        </p>
        <component
          :is="attempt < group.length - 1 ? 'details' : 'div'"
          v-for="({ turn, index }, attempt) in group"
          :key="index"
          class="attempt"
        >
          <summary v-if="attempt < group.length - 1">
            {{ t('ai_conversation.previous_answer') }} <slot name="previous" :turn="turn" />
          </summary>
          <div class="attempt-content">
            <slot name="attempt" :turn="turn" :index="index">
              <AssistantAnswer v-if="turn.answer" :text="turn.answer" />
            </slot>
          </div>
        </component>
      </article>
      <slot name="notice" />
    </div>
    <footer class="composer">
      <slot name="reference" :locate-question="locateQuestion" />
      <el-input
        type="textarea"
        :data-testid="inputTestId"
        v-model="input"
        :aria-label="t('ai_conversation.message')"
        :placeholder="t('ai_conversation.message')"
        @compositionstart="composing = true"
        @compositionend="composing = false"
        @keydown="keydown"
      />
      <div class="composer-actions">
        <small>{{ t('ai_conversation.send_shortcut') }}</small>
        <slot name="actions">
          <el-button
            :data-testid="sendTestId"
            type="primary"
            :disabled="!input.trim() || disabled"
            @click="send"
            >{{ t('ai_conversation.send') }}</el-button
          >
        </slot>
      </div>
    </footer>
  </aside>
</template>

<style scoped lang="less">
.assistant-panel {
  position: relative;
  width: min(360px, 36vw);
  height: 100%;
  min-width: 0;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  border-left: 1px solid var(--ed-border-color);
  background: var(--ed-bg-color);
  color: var(--ed-text-color-primary);
  font-size: 14px;
  line-height: 22px;
  text-align: start;

  :deep(pre),
  :deep(code) {
    font: 13px/20px ui-monospace, SFMono-Regular, Consolas, monospace;
  }
  :deep(pre) {
    margin: 0;
    overflow: auto;
  }
  :deep(summary) {
    cursor: pointer;
    color: var(--ed-text-color-secondary);
    font-size: 13px;
  }
  :deep(small) {
    font-size: 12px;
    line-height: 18px;
    color: var(--ed-text-color-secondary);
  }
}
.assistant-heading,
.composer-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
}
.assistant-heading {
  flex: none;
  padding: 10px 16px;
  border-bottom: 1px solid var(--ed-border-color);
  strong {
    font-size: 16px;
    font-weight: 600;
  }
}
.conversation {
  min-height: 0;
  min-width: 0;
  flex: 1;
  overflow: auto;
  overscroll-behavior: contain;
  padding: 16px;
}
article,
.attempt-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 0;
  overflow-wrap: anywhere;
}
article + article {
  margin-top: 12px;
  padding-top: 16px;
  border-top: 1px solid var(--ed-border-color);
}
.question {
  align-self: flex-end;
  max-width: 94%;
  padding: 7px 12px;
  margin: 0;
  border-radius: 10px 10px 3px 10px;
  background: color-mix(in srgb, var(--ed-color-primary) 12%, var(--ed-bg-color));
  white-space: pre-wrap;
}
.question.located {
  outline: 2px solid var(--ed-color-primary);
  outline-offset: 2px;
}
.attempt[open] > summary {
  margin-bottom: 10px;
}
.composer {
  flex: none;
  min-width: 0;
  padding: 12px 16px;
  border-top: 1px solid var(--ed-border-color);
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 8px;
}
.composer-actions > small {
  margin-right: auto;
}
.width-grip {
  position: absolute;
  left: -4px;
  top: 0;
  bottom: 0;
  width: 8px;
  z-index: 1;
  cursor: col-resize;
  touch-action: none;
  user-select: none;

  &:hover,
  &:focus-visible {
    background: var(--ed-color-primary-light-7);
  }
}
</style>
