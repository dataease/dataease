import type { LinearScale } from '@antv/g2'
import type { Scale } from '@antv/g2/esm/runtime/types/component'
import type { G2GuideComponentOptions } from '@antv/g2/esm/runtime/types/options'

type MixAxisRange = { domain: number[]; ticks: number[] }

export interface MixAxisState {
  auto: [boolean, boolean]
  column: boolean
  domains?: [number[], number[]]
  preserve: [boolean, boolean]
  fixed?: [MixAxisRange | undefined, MixAxisRange | undefined]
}

export type MixAxisScaleOptions = LinearScale & {
  dataeaseMixAxis?: 0 | 1
  dataeaseMixAxisSource?: boolean
  dataeaseMixAxisState?: () => MixAxisState
}

const roundTick = (value: number) => (value === 0 ? 0 : Number(value.toPrecision(15)))

/**
 * 使用现有数值轴的等距刻度，跳过空数据、常数域和不完整的刻度范围
 */
const getAxisRange = (scale: Scale): MixAxisRange | undefined => {
  const domain = scale.getOptions().domain as number[]
  const ticks = scale.getTicks?.() as number[] | undefined
  if (
    domain?.length !== 2 ||
    !domain.every(Number.isFinite) ||
    domain[1] <= domain[0] ||
    !Number.isFinite(domain[1] - domain[0]) ||
    !ticks ||
    ticks.length < 2 ||
    !ticks.every(Number.isFinite)
  ) {
    return
  }
  const span = domain[1] - domain[0]
  if (
    ticks.some(
      (tick, index) => Math.abs((tick - domain[0]) / span - index / (ticks.length - 1)) > 1e-10
    )
  ) {
    return
  }
  return { domain: [...domain], ticks: [...ticks] }
}

/**
 * 向上取常用的 1、2、5 刻度步长，避免直接等分轴域产生循环小数
 */
const getNiceStep = (value: number): number => {
  const power = 10 ** Math.floor(Math.log10(value))
  const ratio = value / power
  return (ratio <= 1 + 1e-10 ? 1 : ratio <= 2 + 1e-10 ? 2 : ratio <= 5 + 1e-10 ? 5 : 10) * power
}

/**
 * 固定区间数时只扩大自动轴，保留数据并使用易读的刻度步长
 */
const fitAxisRange = (range: MixAxisRange, intervals: number): MixAxisRange | undefined => {
  const [min, max] = range.domain
  let step = getNiceStep((max - min) / intervals)
  if (min < 0 && max > 0 && intervals === 1) {
    step = getNiceStep(Math.max(-min, max))
    return Number.isFinite(step) && step > 0
      ? { domain: [-step, step], ticks: [-step, step] }
      : undefined
  }
  while (Number.isFinite(step) && step > 0) {
    const start = roundTick(Math.floor(min / step + 1e-10) * step)
    const end = roundTick(start + intervals * step)
    if (
      Number.isFinite(start) &&
      Number.isFinite(end) &&
      start <= min + step * 1e-10 &&
      end >= max - step * 1e-10
    ) {
      const ticks = Array.from({ length: intervals + 1 }, (_, index) =>
        roundTick(start + index * step)
      )
      return { domain: [start, end], ticks }
    }
    step = getNiceStep(step * 1.01)
  }
}

/**
 * 让自动轴的布局测量和实际绘制使用同一份轴域与刻度
 */
const setAxisRange = (
  component: G2GuideComponentOptions,
  axisIndex: number,
  range: MixAxisRange
) => {
  const tickMethod = () => range.ticks
  component.scales.forEach((descriptor: MixAxisScaleOptions) => {
    if (descriptor.dataeaseMixAxisSource && descriptor.dataeaseMixAxis === axisIndex) {
      descriptor.domain = [...range.domain]
      descriptor.nice = false
      descriptor.tickMethod = tickMethod
    }
  })
  component.tickMethod = tickMethod
}

/**
 * 在测量前仅调整自动侧，双侧自动时以左轴为参照
 * 参照轴沿用已有范围和刻度，布局与绘图使用相同结果
 */
export const prepareMixAxisAlignment = (
  components: G2GuideComponentOptions[],
  makeScale: (component: G2GuideComponentOptions) => Scale
): G2GuideComponentOptions[] => {
  const axes: [G2GuideComponentOptions | undefined, G2GuideComponentOptions | undefined] = [
    undefined,
    undefined
  ]
  const descriptors: [MixAxisScaleOptions | undefined, MixAxisScaleOptions | undefined] = [
    undefined,
    undefined
  ]
  components.forEach(component => {
    if (component.type !== 'axisY') {
      return
    }
    const descriptor = component.scales?.find(
      (scale: MixAxisScaleOptions) =>
        scale.dataeaseMixAxisSource && (scale.dataeaseMixAxis === 0 || scale.dataeaseMixAxis === 1)
    ) as MixAxisScaleOptions | undefined
    if (descriptor) {
      axes[descriptor.dataeaseMixAxis] = component
      descriptors[descriptor.dataeaseMixAxis] = descriptor
    }
  })
  const [leftAxis, rightAxis] = axes
  const [leftDescriptor, rightDescriptor] = descriptors
  const state = leftDescriptor?.dataeaseMixAxisState?.()
  descriptors.forEach((descriptor, index) => {
    const axisState = descriptor?.dataeaseMixAxisState?.()
    if (!axisState) {
      return
    }
    axisState.domains = undefined
    const fixedRange = axisState.auto[index] && axisState.fixed?.[index]
    // 另一侧为空或隐藏时仍需提前恢复保域轴，不能在绘图阶段再改变布局已测量的范围
    if (fixedRange) {
      setAxisRange(axes[index], index, fixedRange)
    }
  })
  if (
    !leftAxis ||
    !rightAxis ||
    !leftDescriptor ||
    !rightDescriptor ||
    !state ||
    rightDescriptor.dataeaseMixAxisState?.() !== state ||
    state.auto.every(auto => !auto)
  ) {
    return []
  }
  const left = (state.auto[0] && state.fixed?.[0]) || getAxisRange(makeScale(leftAxis))
  const right = (state.auto[1] && state.fixed?.[1]) || getAxisRange(makeScale(rightAxis))
  if (!left || !right) {
    return []
  }
  const ranges: [MixAxisRange, MixAxisRange] = [left, right]
  // 两侧自动时左轴作为参照，单侧手动时保留手动轴，只调整另一侧
  const referenceIndex = state.auto[1] ? 0 : 1
  const targetIndex = referenceIndex === 0 ? 1 : 0
  const intervals = ranges[referenceIndex].ticks.length - 1
  const target = ranges[targetIndex]
  let fitted: MixAxisRange | undefined
  if (state.fixed?.[targetIndex]) {
    // 辅助线只冻结范围，刻度仍可在该范围内跟随参照轴
    const [min, max] = target.domain
    fitted = {
      domain: [...target.domain],
      ticks: Array.from({ length: intervals + 1 }, (_, index) =>
        roundTick(min + ((max - min) * index) / intervals)
      )
    }
  } else {
    // 非负柱线的右侧自动轴包含零，数量和比率使用同一条底边
    const domain =
      state.column && targetIndex === 1 && target.domain[0] > 0
        ? [0, target.domain[1]]
        : target.domain
    fitted = fitAxisRange({ ...target, domain }, intervals)
  }
  if (!fitted) {
    return []
  }
  ranges[targetIndex] = fitted
  if (
    ranges.some(
      range =>
        !range.domain.every(Number.isFinite) ||
        range.domain[1] <= range.domain[0] ||
        range.ticks.some(
          (tick, index) => !Number.isFinite(tick) || (index > 0 && tick <= range.ticks[index - 1])
        )
    )
  ) {
    return []
  }
  setAxisRange(axes[targetIndex], targetIndex, fitted)
  state.domains = [[...ranges[0].domain], [...ranges[1].domain]]
  ranges.forEach((range, index) => {
    if (state.preserve[index]) {
      state.fixed ??= [undefined, undefined]
      state.fixed[index] = { domain: [...range.domain], ticks: [...range.ticks] }
    }
  })
  return [leftAxis, rightAxis]
}
