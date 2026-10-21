import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { transformSync } from 'esbuild'
import vm from 'node:vm'

const source = transformSync(
  readFileSync(new URL('../../src/views/chart/components/js/tooltip-carousel-scroll.ts', import.meta.url), 'utf8'),
  { loader: 'ts', format: 'cjs' }
).code
function setup(tooltip) {
  let now = 0, id = 0
  const jobs = new Map()
  const schedule = (fn, delay) => { jobs.set(++id, { at: now + delay, fn }); return id }
  const context = {
    module: { exports: {} },
    setTimeout: (fn, delay) => schedule(fn, delay),
    clearTimeout: id => jobs.delete(id),
    requestAnimationFrame: fn => schedule(() => fn(now), 16),
    cancelAnimationFrame: id => jobs.delete(id)
  }
  vm.runInNewContext(source, context)
  let calls = 0
  const cancel = context.module.exports.scheduleTooltipCarousel(() => tooltip, 2000, () => calls++)
  const advance = duration => {
    const end = now + duration
    while (true) {
      const entry = [...jobs].filter(([, job]) => job.at <= end).sort((a, b) => a[1].at - b[1].at)[0]
      if (!entry) break
      jobs.delete(entry[0]); now = entry[1].at; entry[1].fn()
    }
    now = end
  }
  return { advance, cancel, calls: () => calls, pending: () => jobs.size }
}
const short = setup({ isConnected: true, scrollTop: 0, scrollHeight: 100, clientHeight: 100 })
short.advance(2000); assert.equal(short.calls(), 0)
short.advance(100); assert.equal(short.calls(), 1)

const element = { isConnected: true, scrollTop: 60, scrollHeight: 340, clientHeight: 100 }
const long = setup(element)
long.advance(100); assert.equal(element.scrollTop, 0)
long.advance(3000); assert.ok(element.scrollTop > 0 && element.scrollTop < 240); assert.equal(long.calls(), 0)
long.advance(4100); assert.equal(element.scrollTop, 240); assert.equal(long.calls(), 0)
long.advance(1000); assert.equal(long.calls(), 1); assert.equal(long.pending(), 0)

const paused = setup({ ...element, scrollTop: 0 })
paused.advance(2000); paused.cancel(); paused.advance(30000)
assert.equal(paused.calls(), 0); assert.equal(paused.pending(), 0)
const destroyed = setup(element)
destroyed.cancel(); destroyed.advance(30000)
assert.equal(destroyed.calls(), 0); assert.equal(destroyed.pending(), 0)
const absent = setup(null)
absent.advance(2200); assert.equal(absent.calls(), 1)
console.log('PASS: short timing, overflow reaches bottom before switching, reset, pause/destroy cancellation, missing tooltip')
