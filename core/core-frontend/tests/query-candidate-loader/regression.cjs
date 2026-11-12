/* eslint-disable @typescript-eslint/no-var-requires -- Node 回归入口。 */
const assert = require('node:assert/strict')
const { test } = require('node:test')
const { loadCandidateLoader, createQueryScope } = require('./load.cjs')

const deferred = () => {
  let resolve, reject
  const promise = new Promise((a, b) => {
    resolve = a
    reject = b
  })
  return { promise, resolve, reject }
}
const setup = () => {
  const timers = new Map(),
    errors = [],
    scope = createQueryScope(),
    loading = { value: false }
  let id = 0
  const create = loadCandidateLoader({
    console: { error: error => errors.push(error) },
    setTimeout: fn => {
      timers.set(++id, fn)
      return id
    },
    clearTimeout: id => timers.delete(id)
  })
  const loader = create(scope.createQuery('options'), loading)
  return {
    loader,
    loading,
    scope,
    errors,
    timers,
    flush: async () => {
      const callbacks = [...timers.values()]
      timers.clear()
      callbacks.forEach(fn => fn())
      await Promise.resolve()
    }
  }
}

test('equal inputs share one pending task, apply once, and fetch again after completion', async () => {
  const ctx = setup(),
    response = deferred()
  let calls = 0,
    writes = 0
  const task = async request => {
    calls++
    await response.promise
    if (request.isCurrent()) writes++
  }
  const first = ctx.loader.run({ fields: ['a'], resultMode: 0 }, task, 300)
  const second = ctx.loader.run({ resultMode: 0, fields: ['a'] }, task, 300)
  assert.equal(first, second)
  assert.equal(ctx.timers.size, 1)
  assert.equal(ctx.loading.value, true)
  await ctx.flush()
  assert.equal(calls, 1)
  response.resolve()
  await first
  assert.equal(writes, 1)
  assert.equal(ctx.loading.value, false)
  assert.equal(ctx.scope.pendingCount, 0)
  await ctx.loader.run({ fields: ['a'], resultMode: 0 }, task)
  assert.equal(calls, 2)
})

test('changed input aborts immediately during delay and only runs latest task', async () => {
  const ctx = setup(),
    requests = []
  const old = ctx.loader.run(
    { field: 'old' },
    async r => {
      requests.push(r)
    },
    300
  )
  const latest = ctx.loader.run(
    { field: 'new' },
    async r => {
      requests.push(r)
    },
    300
  )
  await old
  assert.equal(ctx.timers.size, 1)
  assert.equal(ctx.loading.value, true)
  await ctx.flush()
  await latest
  assert.equal(requests.length, 1)
  assert.equal(ctx.scope.pendingCount, 0)
})

test('late responses and failures cannot apply or clear latest loading', async () => {
  const ctx = setup(),
    oldResponse = deferred(),
    latestResponse = deferred(),
    writes = []
  let oldRequest
  const old = ctx.loader.run('old', async r => {
    oldRequest = r
    await oldResponse.promise
    if (r.isCurrent()) writes.push('old')
  })
  const latest = ctx.loader.run('latest', async r => {
    await latestResponse.promise
    if (r.isCurrent()) writes.push('latest')
  })
  assert.equal(oldRequest.signal.aborted, true)
  oldResponse.reject(new Error('stale'))
  await old
  assert.equal(ctx.loading.value, true)
  assert.deepEqual(ctx.errors, [])
  latestResponse.resolve()
  await latest
  assert.deepEqual(writes, ['latest'])
  assert.equal(ctx.loading.value, false)
})

test('current failures release loading, report error and allow same-input retry', async () => {
  const ctx = setup(),
    error = new Error('network')
  await ctx.loader.run('field', async () => {
    throw error
  })
  assert.deepEqual(ctx.errors, [error])
  assert.equal(ctx.loading.value, false)
  let calls = 0
  await ctx.loader.run('field', async () => {
    calls++
  })
  assert.equal(calls, 1)
  assert.equal(ctx.scope.pendingCount, 0)
})

test('synchronous transport errors do not leave a completed task reusable', async () => {
  const ctx = setup()
  await ctx.loader.run('field', () => {
    throw new Error('sync')
  })
  let calls = 0
  await ctx.loader.run('field', async () => {
    calls++
  })
  assert.equal(calls, 1)
  assert.equal(ctx.errors.length, 1)
})

test('invalidate cancels queued work and completed workflow callbacks become stale', async () => {
  const ctx = setup()
  let calls = 0,
    completedRequest
  const queued = ctx.loader.run(
    'queued',
    async () => {
      calls++
    },
    300
  )
  ctx.loader.invalidate()
  await queued
  assert.equal(ctx.timers.size, 0)
  assert.equal(calls, 0)
  await ctx.loader.run('complete', async r => {
    completedRequest = r
  })
  assert.equal(completedRequest.isCurrent(), true)
  ctx.loader.invalidate()
  assert.equal(completedRequest.isCurrent(), false)
})

test('dispose cancels delays, ignores late writes and rejects future work', async () => {
  const ctx = setup(),
    response = deferred()
  let calls = 0,
    writes = 0
  const old = ctx.loader.run('old', async r => {
    await response.promise
    if (r.isCurrent()) writes++
  })
  const queued = ctx.loader.run(
    'queued',
    async () => {
      calls++
    },
    300
  )
  ctx.loader.dispose()
  response.resolve()
  await Promise.all([old, queued])
  await ctx.loader.run('future', async () => {
    calls++
  })
  assert.equal(calls, 0)
  assert.equal(writes, 0)
  assert.equal(ctx.loading.value, false)
  assert.equal(ctx.timers.size, 0)
  assert.equal(ctx.scope.pendingCount, 0)
})

test('equal requests in separate component loaders never share work', async () => {
  const a = setup(),
    b = setup(),
    response = deferred()
  let calls = 0
  const task = async () => {
    calls++
    await response.promise
  }
  const first = a.loader.run({ field: 'same' }, task)
  const second = b.loader.run({ field: 'same' }, task)
  assert.notEqual(first, second)
  assert.equal(calls, 2)
  a.loader.dispose()
  assert.equal(b.loading.value, true)
  response.resolve()
  await Promise.all([first, second])
})

test('pending keys are snapshots and cannot be changed through caller mutation', async () => {
  const ctx = setup(),
    response = deferred(),
    key = { fields: ['a'] }
  const first = ctx.loader.run(key, async () => {
    await response.promise
  })
  key.fields[0] = 'b'
  const second = ctx.loader.run(key, async () => {
    await response.promise
  })
  assert.notEqual(first, second)
  response.resolve()
  await Promise.all([first, second])
})

for (const cancellation of ['invalidate', 'dispose', 'supersede', 'scope-dispose']) {
  test(`${cancellation} settles the returned promise even when transport never settles`, async () => {
    const ctx = setup(),
      latestResponse = deferred()
    let oldRequest,
      writes = 0
    const old = ctx.loader.run('old', async request => {
      oldRequest = request
      await new Promise(() => undefined)
      if (request.isCurrent()) writes++
    })
    const duplicate = ctx.loader.run('old', async () => {
      writes++
    })
    assert.equal(old, duplicate)
    let latest
    if (cancellation === 'supersede') {
      latest = ctx.loader.run('latest', async request => {
        await latestResponse.promise
        if (request.isCurrent()) writes++
      })
    } else if (cancellation === 'scope-dispose') {
      ctx.scope.dispose()
    } else {
      ctx.loader[cancellation]()
    }
    assert.equal(
      await Promise.race([
        old.then(() => true),
        new Promise(resolve => setImmediate(() => resolve(false)))
      ]),
      true
    )
    assert.equal(oldRequest.signal.aborted, true)
    assert.equal(writes, 0)
    assert.equal(ctx.scope.pendingCount, cancellation === 'supersede' ? 1 : 0)
    if (latest) {
      assert.equal(ctx.loading.value, true)
      latestResponse.resolve()
      await latest
      assert.equal(writes, 1)
      assert.equal(ctx.loading.value, false)
    }
  })
}

test('late rejection after cancellation remains handled and cannot clear latest loading', async () => {
  const ctx = setup(),
    oldResponse = deferred(),
    latestResponse = deferred()
  const old = ctx.loader.run('old', async () => {
    await oldResponse.promise
  })
  const latest = ctx.loader.run('latest', async () => {
    await latestResponse.promise
  })
  await old
  oldResponse.reject(new Error('late failure'))
  await new Promise(resolve => setImmediate(resolve))
  assert.deepEqual(ctx.errors, [])
  assert.equal(ctx.loading.value, true)
  latestResponse.resolve()
  await latest
  assert.equal(ctx.loading.value, false)
})
