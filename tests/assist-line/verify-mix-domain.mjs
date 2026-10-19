import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import ts from '../../core/core-frontend/node_modules/typescript/lib/typescript.js'

// 直接执行生产轴域同步逻辑，比例尺边界用最小实现替换。
const source = readFileSync(
  new URL(
    '../../core/core-frontend/src/views/chart/components/js/panel/charts/g2/mix/common.ts',
    import.meta.url
  ),
  'utf8'
)
const compiled = ts.transpileModule(
  source.slice(
    source.indexOf('export const getAssistLineAxisIndex'),
    source.indexOf('export const getMixLabelTransform')
  ),
  {
    compilerOptions: {
      module: ts.ModuleKind.ESNext,
      target: ts.ScriptTarget.ES2022
    }
  }
).outputText
const { getMixAssistLineOptions } = await import(
  `data:text/javascript;base64,${Buffer.from(compiled).toString('base64')}`
)

const scale = options => ({
  options: { ...options },
  getOptions() {
    return this.options
  },
  update(update) {
    Object.assign(this.options, update)
  }
})
const create = (lines, index = 0, configuredDomain) => {
  const axis = { scale: { y: { key: index === 0 ? 'left' : 'right' } } }
  if (configuredDomain) axis.scale.y.domain = configuredDomain
  const result = getMixAssistLineOptions(axis, lines, index, lines)
  const source = scale({
    ...axis.scale.y,
    domain: configuredDomain ?? [0, 60],
    range: [1, 0]
  })
  const points = scale({
    key: axis.scale.y.key,
    domain: source.options.domain
  })
  const otherAxis = scale({
    key: index === 0 ? 'right' : 'left',
    domain: [10, 100]
  })
  const assist = scale({ ...result.scaleY, domain: [0, 1] })
  return {
    ...result,
    source,
    points,
    otherAxis,
    assist,
    sync(domain) {
      source.update({ domain })
      axis.scale.y.groupTransform([source, points, otherAxis, assist])
    }
  }
}

const fixed = {
  field: '0',
  value: 35,
  boundFieldId: 'amount',
  followField: false,
  yAxisType: 'left'
}
const fixedFollow = { ...fixed, value: 15, followField: true }
const run = create([fixed, fixedFollow])
run.sync([0, 60])
for (const domain of [[0, 20], [0, 1], [], [0, 60]]) {
  run.sync(domain)
  assert.deepEqual(run.source.options.domain, [0, 60])
  assert.deepEqual(run.points.options.domain, [0, 60])
  assert.deepEqual(run.assist.options.domain, [0, 60])
  assert.deepEqual(run.otherAxis.options.domain, [10, 100])
  assert.equal(run.visibility(fixed), 'visible')
}

const followOnly = create([fixedFollow])
followOnly.sync([0, 60])
followOnly.sync([0, 10])
assert.deepEqual(followOnly.assist.options.domain, [0, 10])
assert.equal(followOnly.visibility(fixedFollow), 'hidden')

const legacy = create([{ ...fixed, followField: undefined }])
legacy.sync([0, 60])
legacy.sync([0, 1])
assert.equal(legacy.visibility(fixed), 'visible')
const unbound = create([{ ...fixed, followField: true, boundFieldId: '' }])
unbound.sync([0, 60])
unbound.sync([0, 1])
assert.equal(unbound.visibility(fixed), 'visible')

// 动态响应可能不包含 followField；是否保留轴域必须使用持久化配置。
const axis = { scale: { y: { key: 'right' } } }
const dynamic = {
  field: '1',
  fieldId: 'target',
  value: 25,
  yAxisType: 'right'
}
const dynamicRun = getMixAssistLineOptions(axis, [dynamic], 1, [{ ...dynamic, followField: false }])
const dynamicSource = scale({ ...axis.scale.y, domain: [20, 30] })
const dynamicAssist = scale(dynamicRun.scaleY)
axis.scale.y.groupTransform([dynamicSource, dynamicAssist])
dynamicSource.update({ domain: [0, 1] })
axis.scale.y.groupTransform([dynamicSource, dynamicAssist])
assert.equal(dynamicRun.visibility(dynamic), 'visible')
assert.deepEqual(dynamicSource.options.domain, [20, 30])

const dynamicFollowAxis = { scale: { y: { key: 'right' } } }
const dynamicFollow = getMixAssistLineOptions(dynamicFollowAxis, [dynamic], 1, [
  fixed,
  { ...dynamic, followField: true }
])
const dynamicFollowSource = scale({ ...dynamicFollowAxis.scale.y, domain: [20, 30] })
const dynamicFollowAssist = scale(dynamicFollow.scaleY)
dynamicFollowAxis.scale.y.groupTransform([dynamicFollowSource, dynamicFollowAssist])
dynamicFollowSource.update({ domain: [0, 1] })
dynamicFollowAxis.scale.y.groupTransform([dynamicFollowSource, dynamicFollowAssist])
assert.equal(dynamicFollow.visibility(dynamic), 'hidden')
assert.deepEqual(dynamicFollowAssist.options.domain, [0, 1])
// 数据刷新创建的新配置应使用新的初始轴域，不复用上轮数据的范围。
const refreshed = create([fixed])
refreshed.sync([0, 20])
assert.equal(refreshed.visibility(fixed), 'hidden')
axis.scale.y.groupTransform([dynamicAssist])
assert.equal(dynamicRun.visibility(dynamic), 'visible')

const manual = create([fixed], 0, [0, 30])
manual.sync([0, 30])
assert.equal(manual.visibility(fixed), 'hidden')
assert.deepEqual(manual.assist.options.domain, [0, 30])
const outside = create([fixed])
outside.sync([0, 30])
outside.sync([0, 1])
assert.equal(outside.visibility(fixed), 'hidden')

const negative = create([{ ...fixed, value: -35 }])
negative.sync([-60, 0])
negative.sync([-10, 0])
assert.equal(negative.visibility({ value: -35 }), 'visible')
assert.equal(negative.visibility({ value: -61 }), 'hidden')

const delayed = create([fixed])
delayed.sync([])
assert.equal(delayed.visibility(fixed), 'hidden')
delayed.sync([0, 60])
delayed.sync([0, 1])
assert.equal(delayed.visibility(fixed), 'visible')
assert.deepEqual(delayed.visibleLineData, [fixed])
assert.deepEqual(create([{ ...fixed, value: NaN }]).visibleLineData, [])

console.log('PASS: mixed-chart assist-line domains, filtering, axes and legacy configuration')
