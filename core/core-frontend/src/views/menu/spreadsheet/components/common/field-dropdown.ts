import type { ElDropdown } from 'element-plus-secondary'

export const fieldDropdownPopperOptions: InstanceType<
  typeof ElDropdown
>['$props']['popperOptions'] = {
  modifiers: [
    {
      name: 'matchFieldWidth',
      enabled: true,
      phase: 'beforeRead',
      fn({ state }) {
        const width = state.rects.reference.width
        // 定位前同步弹层宽度，让翻转和对齐计算使用字段行的宽度。
        state.rects.popper.width = width
        state.styles.popper = { ...state.styles.popper, width: `${width}px` }
      }
    }
  ]
}
