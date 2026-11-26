// Run: node --test tests/plugin-loader/regression.mjs
import assert from 'node:assert/strict'
import { test } from 'node:test'
import {
  PluginLoadError,
  PluginMethodQueue,
  PluginModuleLoader,
  isStalePluginCacheKey,
  pluginCacheKey
} from '../../src/components/plugin/src/loader.ts'

test('同模块并发加载共享一次 prepare，按 jsname 缓存组件', async () => {
  let prepareCalls = 0
  const adapter = {
    prepare: async moduleName => {
      prepareCalls += 1
      await new Promise(resolve => setTimeout(resolve, 10))
      return { resolve: async jsname => `${moduleName}:${jsname}` }
    }
  }
  const loader = new PluginModuleLoader(adapter)
  const [first, second, other] = await Promise.all([
    loader.load('module-a', 'index'),
    loader.load('module-a', 'index'),
    loader.load('module-a', 'editor-style')
  ])
  assert.equal(prepareCalls, 1)
  assert.equal(first, 'module-a:index')
  assert.equal(second, 'module-a:index')
  assert.equal(other, 'module-a:editor-style')
  assert.equal(loader.moduleState('module-a'), 'ready')
})

test('加载失败结束等待，并在下一次调用时重试', async () => {
  let attempt = 0
  const adapter = {
    prepare: async () => {
      attempt += 1
      if (attempt === 1) throw new Error('boom')
      return { resolve: async () => 'ready' }
    }
  }
  const loader = new PluginModuleLoader(adapter)
  await assert.rejects(
    loader.load('module-b', 'index'),
    error => error instanceof PluginLoadError && error.code === 'LOAD_FAILED'
  )
  assert.equal(loader.moduleState('module-b'), 'failed')
  assert.equal(await loader.load('module-b', 'index'), 'ready')
  assert.equal(attempt, 2)
})

test('UNAVAILABLE 是终态，不重复请求', async () => {
  let attempt = 0
  const adapter = {
    prepare: async () => {
      attempt += 1
      throw new PluginLoadError('UNAVAILABLE', 'no license')
    }
  }
  const loader = new PluginModuleLoader(adapter)
  await assert.rejects(loader.load('module-c', 'index'), /no license/)
  await assert.rejects(loader.load('module-c', 'index'), /no license/)
  assert.equal(attempt, 1)
  assert.equal(loader.moduleState('module-c'), 'unavailable')
})

test('组件解析失败后可重新解析', async () => {
  let resolveAttempt = 0
  const adapter = {
    prepare: async () => ({
      resolve: async () => {
        resolveAttempt += 1
        if (resolveAttempt === 1) throw new Error('missing')
        return 'component'
      }
    })
  }
  const loader = new PluginModuleLoader(adapter)
  await assert.rejects(loader.load('module-d', 'index'), /missing/)
  assert.equal(await loader.load('module-d', 'index'), 'component')
  assert.equal(resolveAttempt, 2)
})

test('加载超时有确定结果，不会无限等待', async () => {
  const adapter = { prepare: () => new Promise(() => undefined) }
  const loader = new PluginModuleLoader(adapter, { timeoutMs: 20 })
  await assert.rejects(
    loader.load('module-e', 'index'),
    error => error instanceof PluginLoadError && error.code === 'TIMEOUT'
  )
  assert.equal(loader.moduleState('module-e'), 'failed')
})

test('组件解析挂起时同样有超时，不会无限等待', async () => {
  const adapter = {
    prepare: async () => ({ resolve: () => new Promise(() => undefined) })
  }
  const loader = new PluginModuleLoader(adapter, { timeoutMs: 20 })
  await assert.rejects(
    loader.load('module-f', 'index'),
    error => error instanceof PluginLoadError && error.code === 'TIMEOUT'
  )
  assert.equal(loader.moduleState('module-f'), 'ready')
})

test('绑定不自动排空，drain 时执行挂载前积压的调用', async () => {
  const queue = new PluginMethodQueue(4)
  const first = queue.push({ methodName: 'calcData', args: [1] })
  const second = queue.push({ methodName: 'renderChart', args: [2] })
  const executed = []
  queue.bind(param => {
    executed.push(param.methodName)
    return `${param.methodName}:${param.args[0]}`
  })
  assert.deepEqual(executed, [])
  assert.equal(queue.size, 2)
  queue.drain()
  assert.deepEqual(await Promise.all([first, second]), ['calcData:1', 'renderChart:2'])
  assert.deepEqual(executed, ['calcData', 'renderChart'])
})

test('绑定后的新调用同步执行，先于挂载前积压调用', async () => {
  const queue = new PluginMethodQueue(4)
  const executed = []
  const queued = queue.push({ methodName: 'calcData' })
  queue.bind(param => {
    executed.push(param.methodName)
    return param.methodName
  })
  assert.equal(await queue.push({ methodName: 'initForm' }), 'initForm')
  assert.deepEqual(executed, ['initForm'])
  queue.drain()
  assert.equal(await queued, 'calcData')
  assert.deepEqual(executed, ['initForm', 'calcData'])
})

test('排队有上限，溢出立即失败', async () => {
  const queue = new PluginMethodQueue(1)
  const pending = queue.push({ methodName: 'calcData' })
  await assert.rejects(
    queue.push({ methodName: 'renderChart' }),
    error => error instanceof PluginLoadError && error.code === 'QUEUE_OVERFLOW'
  )
  queue.close(new PluginLoadError('DISPOSED', 'closed'))
  await assert.rejects(pending, /closed/)
})

test('失败或卸载后调用立即得到确定结果', async () => {
  const queue = new PluginMethodQueue()
  const pending = queue.push({ methodName: 'calcData' })
  queue.close(new PluginLoadError('LOAD_FAILED', 'load failed'))
  await assert.rejects(pending, /load failed/)
  await assert.rejects(queue.push({ methodName: 'renderChart' }), /load failed/)
})

test('切换模块时重置队列，旧调用结束且新调用可继续排队', async () => {
  const queue = new PluginMethodQueue()
  const oldCall = queue.push({ methodName: 'calcData' })
  queue.reset(new PluginLoadError('LOAD_FAILED', 'switched'))
  await assert.rejects(oldCall, /switched/)
  const newCall = queue.push({ methodName: 'initForm' })
  queue.bind(() => 'ready')
  queue.drain()
  assert.equal(await newCall, 'ready')
})

test('就绪后同步执行，异常同步抛出', () => {
  const queue = new PluginMethodQueue()
  queue.bind(param => {
    if (param.methodName === 'submitForm') {
      throw new PluginLoadError('METHOD_NOT_FOUND', param.methodName)
    }
    return `${param.methodName}:ready`
  })
  assert.throws(
    () => queue.push({ methodName: 'submitForm' }),
    error => error instanceof PluginLoadError && error.code === 'METHOD_NOT_FOUND'
  )
  assert.equal(queue.size, 0)
})

test('就绪后的返回值仍可通过 Promise 取得', async () => {
  const queue = new PluginMethodQueue()
  queue.bind(param => param.args[0])
  assert.equal(await queue.push({ methodName: 'calcData', args: [42] }), 42)
})

test('缓存键包含构建版本', () => {
  assert.equal(pluginCacheKey('v1', 'module-a'), 'de-plugin-proxy-v1-module-a')
  assert.notEqual(pluginCacheKey('v1', 'module-a'), pluginCacheKey('v2', 'module-a'))
})

test('升级后旧版本和旧格式缓存键会被识别为待清理', () => {
  assert.equal(isStalePluginCacheKey(pluginCacheKey('old', 'module-a'), 'new'), true)
  assert.equal(isStalePluginCacheKey(pluginCacheKey('new', 'module-a'), 'new'), false)
  assert.equal(isStalePluginCacheKey('de-plugin-proxy-module-a', 'new'), true)
  assert.equal(isStalePluginCacheKey('xpack-model-distributed', 'new'), false)
})
