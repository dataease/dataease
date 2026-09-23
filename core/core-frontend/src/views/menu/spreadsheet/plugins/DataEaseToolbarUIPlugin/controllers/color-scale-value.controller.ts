import { Disposable, IConfigService, Inject, LocaleService } from '@univerjs/core'
import { type IUniverUIConfig, UI_PLUGIN_CONFIG_KEY } from '@univerjs/ui'
import { ElMessage } from 'element-plus-secondary'

export class ColorScaleValueController extends Disposable {
  constructor(
    @IConfigService configService: IConfigService,
    @Inject(LocaleService) localeService: LocaleService
  ) {
    super()
    const config = configService.getConfig<IUniverUIConfig>(UI_PLUGIN_CONFIG_KEY)
    const configured = config && config.container
    const container =
      typeof configured === 'string' ? document.querySelector<HTMLElement>(configured) : configured
    if (!container) return
    const text = (key: string) => localeService.t(`sheets-conditional-formatting-ui.${key}`)
    const validate = (event: Event) => {
      if (!(event.target instanceof Element)) return
      const button = event.target.closest('button')
      if (!button || button.textContent?.trim() !== text('panel.submit')) return
      const editor = button.parentElement?.parentElement
      if (!editor) return
      // 原生确认逻辑不等待保存命令结果就关闭编辑器；在捕获阶段校验以保留草稿。
      const styleType = editor.querySelector(':scope > [data-u-comp="select"]')
      if (styleType?.textContent?.trim() !== text('ruleType.colorScale')) return
      const labels = [text('valueType.min'), text('panel.medianValue'), text('valueType.max')]
      const rows = labels
        .map(label => {
          const heading = Array.from(editor.querySelectorAll('div')).find(
            element => element.children.length === 0 && element.textContent?.trim() === label
          )
          return heading?.nextElementSibling
        })
        .filter((row): row is Element => !!row)
      if (rows.length !== 3) return
      const types = rows.map(row =>
        row.querySelector('[data-u-comp="select"]')?.textContent?.trim()
      )
      const number = text('valueType.num')
      const noMiddle = types[1] === text('valueType.none')
      if (types[0] !== number || types[2] !== number || (!noMiddle && types[1] !== number)) return
      const activeRows = noMiddle ? [rows[0], rows[2]] : rows
      const inputs = activeRows
        .map(row => row.querySelector<HTMLInputElement>('input'))
        .filter((input): input is HTMLInputElement => !!input)
      if (inputs.length !== activeRows.length) return
      const values = inputs.map(input => Number(input.value))
      const invalid = inputs.findIndex(
        (input, index) =>
          input.value.trim() === '' ||
          !Number.isFinite(values[index]) ||
          (index > 0 && values[index] <= values[index - 1])
      )
      if (invalid === -1) return
      event.preventDefault()
      event.stopImmediatePropagation()
      ElMessage.warning({
        message: localeService.t(
          `dataease-conditional-formatting.${
            noMiddle ? 'colorScaleTwoValues' : 'colorScaleThreeValues'
          }`
        ),
        grouping: true
      })
      inputs[invalid].focus()
    }
    container.addEventListener('click', validate, true)
    this.disposeWithMe({ dispose: () => container.removeEventListener('click', validate, true) })
  }
}
