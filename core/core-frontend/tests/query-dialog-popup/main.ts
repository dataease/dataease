import { createApp, h, nextTick, ref } from 'vue'
import { ElDialog, ElSelect, ElOption, ElButton } from 'element-plus-secondary'
import 'element-plus-secondary/dist/index.css'
import source from '../../src/custom-component/v-query/QueryConditionConfiguration.vue?raw'

// Read the production dialog's lifecycle setting; use real UI components with local data.
const dialogTag = source.match(/<el-dialog\s[\s\S]*?>/)[0]
const destroyOnClose =
  !new URLSearchParams(location.search).has('baseline') && /\bdestroy-on-close\b/.test(dialogTag)
const wait = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))
const modes = ['single', 'time-range', 'number-range']

createApp({
  setup() {
    const visible = ref(false)
    const mode = ref('single')
    const value = ref<string | string[]>('')
    const result = ref('等待执行')
    const running = ref(false)
    const close = () => (visible.value = false)
    const run = async () => {
      running.value = true
      let checks = 0
      const assert = (condition: boolean, message: string) => {
        if (!condition) throw new Error(message)
        checks++
      }
      try {
        for (const kind of modes) {
          mode.value = kind
          value.value = kind === 'single' ? '' : []
          for (const action of ['cancel', 'confirm', 'close']) {
            visible.value = true
            await nextTick()
            await wait(400)
            const select = document.querySelector<HTMLElement>(
              '.field-select--input .ed-select__wrapper'
            )
            select.click()
            await wait(150)
            const popup = document.querySelector<HTMLElement>('.field-select--dqp')
            assert(!!popup && popup.getBoundingClientRect().height > 0, `${kind}: popup opens`)
            const selector =
              action === 'close' ? '.ed-dialog__headerbtn' : `[data-action="${action}"]`
            document.querySelector<HTMLElement>(selector).click()
            await wait(500)
            assert(!visible.value, `${kind}/${action}: dialog closes`)
            assert(
              !document.querySelector('.field-select--dqp'),
              `${kind}/${action}: popup removed`
            )
            assert(
              !document.querySelector('.field-select--input'),
              `${kind}/${action}: select removed`
            )
          }
        }
        result.value = `PASS: ${checks} assertions, 3 select modes × 3 close/reopen paths`
      } catch (error) {
        result.value = `FAIL: ${error.message}`
      } finally {
        visible.value = false
        running.value = false
      }
    }
    return () =>
      h('main', { style: 'padding:32px' }, [
        h('h1', '查询条件弹窗下拉层回归'),
        h(ElButton, { disabled: running.value, onClick: run }, () => '执行回归'),
        h('pre', { id: 'result' }, result.value),
        h(
          ElDialog,
          {
            modelValue: visible.value,
            'onUpdate:modelValue': v => (visible.value = v),
            title: '查询条件设置',
            destroyOnClose,
            beforeClose: close
          },
          {
            default: () =>
              h(
                ElSelect,
                {
                  key: mode.value,
                  modelValue: value.value,
                  'onUpdate:modelValue': v => (value.value = v),
                  multiple: mode.value !== 'single',
                  multipleLimit: 2,
                  filterable: true,
                  class: 'field-select--input',
                  popperClass: 'field-select--dqp'
                },
                () => ['field-1', 'field-2'].map(v => h(ElOption, { value: v, label: v }))
              ),
            footer: () =>
              ['cancel', 'confirm'].map(action =>
                h(ElButton, { 'data-action': action, onClick: close }, () => action)
              )
          }
        )
      ])
  }
}).mount('#app')
