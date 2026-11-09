/* eslint-disable @typescript-eslint/no-var-requires -- 独立 Node 回归入口。 */
const { test } = require('node:test')
const assert = require('node:assert/strict')
const fs = require('node:fs')
const vm = require('node:vm')
const ts = require('typescript')
const { cloneDeep } = require('lodash')
const transpile = source =>
  ts.transpileModule(source, {
    compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2020 }
  }).outputText
const exportsObject = {}
vm.runInNewContext(transpile(fs.readFileSync('src/utils/canvasEditSession.ts', 'utf8')), {
  exports: exportsObject
})
const { createCanvasEditSession, captureCanvasSave, failCanvasEditSession, isCanvasDraft } =
  exportsObject
const deferred = () => {
  let resolve, reject
  const promise = new Promise((yes, no) => {
    resolve = yes
    reject = no
  })
  return { promise, resolve, reject }
}
const fixture = (opt, id = 'a', type = 'dashboard') => {
  const owner = {}
  let location = { id, opt, type }
  let doc = { id, type, name: ' A ', contentId: 'old' }
  const session = createCanvasEditSession(
    owner,
    () => location,
    () => doc
  )
  return {
    owner,
    session,
    get location() {
      return location
    },
    set location(value) {
      location = value
    },
    get doc() {
      return doc
    },
    set doc(value) {
      doc = value
    },
    capture: () => captureCanvasSave(owner, () => doc)
  }
}
for (const type of ['dashboard', 'dataV']) {
  test(type + ': 加载中/失败不可保存，重试成功后可保存', () => {
    const f = fixture(undefined, '9007199254740993123', type)
    const load = f.session.begin()
    assert.equal(f.capture(), undefined)
    load.fail()
    assert.equal(f.capture(), undefined)
    const retry = f.session.begin()
    assert.equal(retry.ready(), true)
    assert.equal(f.capture().isCurrent(), true)
  })
  test(type + ': 同路由 A→B→A 旧代次失效，卸载后异步初始化不能复活', () => {
    const f = fixture(undefined, 'a', type)
    const a = f.session.begin()
    a.ready()
    const save = f.capture()
    f.location = { id: 'b', type }
    assert.equal(save.isCurrent(), false)
    f.session.begin()
    f.location = { id: 'a', type }
    const latest = f.session.begin()
    assert.equal(a.ready(), false)
    assert.equal(latest.ready(), true)
    assert.equal(save.isCurrent(), false)
    f.session.dispose()
    assert.equal(latest.isCurrent(), false)
    assert.equal(f.session.begin().isCurrent(), false)
  })
  test(type + ': 拒绝错误目标，兼容同目标撤销替换 dvInfo 对象', () => {
    const f = fixture(undefined, 'a', type)
    const load = f.session.begin()
    f.doc = { id: 'wrong', type }
    assert.equal(load.ready(), false)
    assert.equal(f.capture(), undefined)
    f.doc = { id: 'a', type }
    f.session.begin().ready()
    const save = f.capture()
    f.doc = { id: 'a', type, name: 'undo' }
    assert.equal(save.isCurrent(), true)
  })
  for (const opt of ['create', 'copy']) {
    test(`${type}: ${opt} 首次保存生成新 ID，路由替换保持会话`, () => {
      const f = fixture(opt, opt === 'copy' ? 'source' : null, type)
      const load = f.session.begin()
      f.doc = { id: opt === 'copy' ? 'copy-id' : null, type }
      assert.equal(load.ready(), true)
      const save = f.capture()
      f.doc.id = 'saved'
      save.saved('saved')
      f.location = { id: 'saved', type }
      assert.equal(save.isCurrent(), true)
      assert.equal(f.capture().isCurrent(), true)
      f.location = { id: 'other', type }
      assert.equal(save.isCurrent(), false)
    })
  }
}

test('共享 Store 的新入口接管后，旧入口销毁不影响新会话', () => {
  const f = fixture()
  f.session.begin().ready()
  const old = f.capture()
  const second = createCanvasEditSession(
    f.owner,
    () => f.location,
    () => f.doc
  )
  second.begin().ready()
  assert.equal(old.isCurrent(), false)
  f.session.dispose()
  assert.equal(f.capture().isCurrent(), true)
})

test('草稿须结构完整且属于同一资源和产品类型', () => {
  const draft = {
    dvInfo: { id: 'a', type: 'dashboard' },
    componentData: [],
    canvasStyleData: {},
    canvasViewInfo: {},
    cacheViewIdInfo: { snapshotCacheViewCalc: [], snapshotCacheViewRender: [] }
  }
  assert.equal(isCanvasDraft(draft, 'a', 'dashboard'), true)
  for (const value of [
    null,
    'invalid',
    {},
    { ...draft, componentData: {} },
    { ...draft, cacheViewIdInfo: null }
  ]) {
    assert.equal(isCanvasDraft(value, 'a', 'dashboard'), false)
  }
  assert.equal(isCanvasDraft(draft, 'b', 'dashboard'), false)
  assert.equal(isCanvasDraft(draft, 'a', 'dataV'), false)
})

// 执行生产保存函数，延迟名称校验及写响应，而非测试复制的保存实现。
const saveSource = fs.readFileSync('src/utils/canvasUtils.ts', 'utf8')
const saveFunctions = saveSource.slice(
  saveSource.indexOf('export function checkCanvasChangePre'),
  saveSource.indexOf('\nconst httpUrlReg')
)
const saveFixture = (opt, id) => {
  const f = fixture(opt, id)
  f.session.begin().ready()
  const names = [],
    writes = [],
    updates = [],
    callbacks = [],
    resets = []
  const c = {
    exports: {},
    cloneDeep,
    captureCanvasSave,
    failCanvasEditSession,
    dvMainStore: f.owner,
    curDvInfo: {
      get value() {
        return f.doc
      }
    },
    componentData: { value: [{ component: 'UserView', linkageFilters: [1] }] },
    canvasStyleData: { value: { color: 'red' } },
    canvasViewInfo: { value: { view: { field: 'original' } } },
    appData: { value: null },
    wsCache: { get: () => 'v3' },
    guid: () => 'new-content',
    t: x => x,
    nameTrim: doc => {
      doc.name = doc.name.trim()
    },
    ElMessage: {
      error() {
        return undefined
      }
    },
    ElMessageBox: { confirm: () => Promise.resolve() },
    isDesktop: () => false,
    checkCanvasChange: () => Promise.resolve({ data: 'ok' }),
    appCanvasNameCheck: () => Promise.resolve({ data: 'success' }),
    dvNameCheck: params => {
      const wait = deferred()
      names.push({ params, ...wait })
      return wait.promise
    },
    snapshotStore: {
      resetStyleChangeTimes: () => resets.push(1),
      resetSnapshot() {
        return undefined
      }
    },
    clearMaterialPreviews() {
      return undefined
    },
    initCanvasData: async (_id, _params, callback) => callback(),
    saveCanvas: payload => {
      const wait = deferred()
      writes.push({ method: 'create', payload, ...wait })
      return wait.promise
    },
    updateCanvas: payload => {
      const wait = deferred()
      writes.push({ method: 'update', payload, ...wait })
      return wait.promise
    }
  }
  f.owner.removeGroupArea = () => undefined
  f.owner.setDataPrepareState = () => undefined
  f.owner.updateDvInfoCall = (status, newId, contentId) => {
    updates.push({ status, newId, contentId })
    if (newId) f.doc.id = newId
    f.doc.contentId = contentId
  }
  vm.runInNewContext(transpile(saveFunctions) + ';this.save = canvasSaveWithParams;', c)
  return {
    f,
    c,
    names,
    writes,
    updates,
    callbacks,
    resets,
    save: () => c.save(null, (...args) => callbacks.push(args))
  }
}
const flush = () => new Promise(resolve => setImmediate(resolve))

test('保存等待校验时切换资源，不再发送写请求', async () => {
  const x = saveFixture(undefined, 'a')
  const saving = x.save()
  assert.equal(x.names.length, 1)
  x.f.location = { id: 'b', type: 'dashboard' }
  x.f.session.begin()
  x.f.doc = { id: 'b', type: 'dashboard' }
  x.names[0].resolve()
  await saving
  assert.equal(x.writes.length, 0)
  assert.equal(x.updates.length, 0)
})

test('保存使用发起时的深拷贝文档、名称和目标', async () => {
  const x = saveFixture(undefined, 'a')
  const saving = x.save()
  x.c.canvasViewInfo.value.view.field = 'edited'
  x.c.canvasStyleData.value.color = 'blue'
  x.f.doc.name = 'Edited'
  x.names[0].resolve()
  await flush()
  const write = x.writes[0]
  assert.equal(write.method, 'update')
  assert.equal(write.payload.id, 'a')
  assert.equal(write.payload.name, 'A')
  assert.equal(write.payload.canvasViewInfo.view.field, 'original')
  assert.equal(JSON.parse(write.payload.canvasStyleData).color, 'red')
  assert.deepEqual(JSON.parse(write.payload.componentData)[0].linkageFilters, [])
  assert.deepEqual(x.c.componentData.value[0].linkageFilters, [1])
  write.resolve({ data: { status: 1 } })
  await saving
  assert.equal(x.updates.length, 1)
  assert.equal(x.callbacks.length, 1)
})

test('已发出的保存完成后，旧结果不回写新文档、不重置修改标志或执行回调', async () => {
  const x = saveFixture(undefined, 'a')
  const saving = x.save()
  x.names[0].resolve()
  await flush()
  assert.equal(x.writes[0].payload.id, 'a')
  x.f.location = { id: 'b', type: 'dashboard' }
  x.f.session.begin()
  x.f.doc = { id: 'b', type: 'dashboard', contentId: 'b-content' }
  x.writes[0].resolve({ data: { status: 1 } })
  await saving
  assert.equal(x.f.doc.contentId, 'b-content')
  assert.equal(x.updates.length, 0)
  assert.equal(x.callbacks.length, 0)
  assert.equal(x.resets.length, 0)
})
for (const opt of ['create', 'copy']) {
  test(opt + ': 使用创建接口，保存成功可继续更新新 ID', async () => {
    const x = saveFixture(opt, opt === 'copy' ? 'source' : null)
    x.f.doc.optType = opt === 'copy' ? 'copy' : null
    const saving = x.save()
    assert.equal(x.writes[0].method, 'create')
    x.writes[0].resolve({ data: 'new-id' })
    await saving
    assert.equal(x.f.doc.id, 'new-id')
    assert.equal(x.callbacks.length, 1)
    x.f.doc.optType = null
    x.f.location = { id: 'new-id', type: 'dashboard' }
    const again = x.save()
    assert.equal(x.names.length, 1)
    x.names[0].resolve()
    await flush()
    assert.equal(x.writes[1].method, 'update')
    x.writes[1].resolve({ data: { status: 1 } })
    await again
  })
}
test('加载未就绪时所有公共保存入口均不发请求', async () => {
  const x = saveFixture(undefined, 'a')
  x.f.session.begin()
  await x.save()
  assert.equal(x.names.length, 0)
  assert.equal(x.writes.length, 0)
})

const extract = (file, names) => {
  const source = fs
    .readFileSync(file, 'utf8')
    .split('<script setup lang="ts">')[1]
    .split('</script>')[0]
  const ast = ts.createSourceFile(file + '.ts', source, ts.ScriptTarget.Latest, true)
  return ast.statements
    .filter(
      statement =>
        ts.isVariableStatement(statement) &&
        statement.declarationList.declarations.some(declaration =>
          names.includes(declaration.name.getText(ast))
        )
    )
    .map(statement => statement.getText(ast))
    .join('\n')
}
for (const [file, type, key] of [
  ['src/views/dashboard/index.vue', 'dashboard', 'resourceId'],
  ['src/views/data-visualization/indexV3.vue', 'dataV', 'dvId']
]) {
  const entryFixture = () => {
    const f = fixture(undefined, 'a', type)
    const loads = [],
      timers = []
    const route = { query: { [key]: 'a' } }
    const c = {
      exports: {},
      editorSession: f.session,
      editorLocation: () => f.location,
      canvasQuery: {
        invalidate() {
          return undefined
        },
        start: () => ({ isCurrent: () => true })
      },
      state: {},
      dataInitState: { value: true },
      templateLoadError: { value: false },
      snapshotStore: {
        initSnapShot() {
          return undefined
        },
        recordSnapshotCache() {
          return undefined
        },
        snapshotPublish: draft => {
          f.doc = draft.dvInfo
        }
      },
      dvMainStore: f.owner,
      embeddedStore: {},
      router: { currentRoute: { value: route } },
      dvInfo: {
        get value() {
          return f.doc
        }
      },
      canvasStyleData: { value: { component: { chartTitle: {} } } },
      canvasCacheOutRef: {
        value: {
          close() {
            return undefined
          },
          dialogInit() {
            return undefined
          }
        }
      },
      canvasCacheOutRefShow: { value: false },
      wsCache: {
        get: () => null,
        delete() {
          return undefined
        }
      },
      checkPer: async () => true,
      initDataset() {
        return undefined
      },
      initScroll() {
        return undefined
      },
      onInitReady() {
        return undefined
      },
      console: {
        error() {
          return undefined
        }
      },
      nextTick: callback => (callback ? Promise.resolve().then(callback) : Promise.resolve()),
      setTimeout: callback => timers.push(callback),
      watermarkFind: async () => ({ data: { settingContent: '{}' } }),
      Base64: { decode: x => x },
      decodeURIComponent,
      decompressionPre: async (_params, callback) =>
        callback({ baseInfo: {}, componentData: [], canvasStyleData: {}, canvasViewInfo: {} }),
      deepCopy: cloneDeep,
      isCanvasDraft,
      eventBus: {
        emit() {
          return undefined
        }
      },
      initCanvasData: (id, params, callback) => {
        const wait = deferred()
        loads.push({
          id,
          params,
          resolve: () => {
            if (params.isCurrent()) {
              f.doc = { id, type, name: id }
              callback()
            }
            wait.resolve()
          },
          reject: wait.reject
        })
        return wait.promise
      }
    }
    for (const method of [
      'setDataPrepareState',
      'setAppDataInfo',
      'setCurComponent',
      'setComponentData',
      'setCanvasStyle',
      'setCanvasViewInfo'
    ])
      f.owner[method] = () => undefined
    f.owner.getAppDataInfo = () => null
    f.owner.createInit = () => {
      f.doc = { id: null, type, name: 'new' }
    }
    vm.runInNewContext(
      transpile(extract(file, ['initializeEditor', 'initLocalCanvasData', 'doUseCache'])) +
        ';this.initialize = initializeEditor;this.restore = doUseCache;',
      c
    )
    return { f, c, loads, timers, route }
  }
  test(type + ': 真实初始化入口加载失败后不可保存，重试恢复', async () => {
    const x = entryFixture()
    const first = x.c.initialize()
    await flush()
    assert.equal(x.f.capture(), undefined)
    assert.equal(x.loads[0].params.awaitLoad, true)
    x.loads[0].reject(new SyntaxError('invalid JSON'))
    await first
    await flush()
    assert.equal(x.f.capture(), undefined)
    const retry = x.c.initialize()
    await flush()
    x.loads[1].resolve()
    await retry
    await flush()
    assert.equal(x.f.capture().isCurrent(), true)
  })
  test(type + ': 真实入口快速 A→B 切换，旧成功和延迟任务均不能覆盖 B', async () => {
    const x = entryFixture()
    const a = x.c.initialize()
    await flush()
    x.route.query[key] = 'b'
    x.f.location = { id: 'b', type }
    const b = x.c.initialize()
    await flush()
    x.loads[1].resolve()
    await b
    x.loads[0].resolve()
    await a
    for (const timer of x.timers) timer()
    assert.equal(x.f.doc.id, 'b')
    assert.equal(x.f.capture().isCurrent(), true)
  })
  test(type + ': 新建和模板入口等待装载完成后才允许保存', async () => {
    const x = entryFixture()
    x.route.query = { opt: 'create', createType: 'template', templateParams: '{}' }
    x.f.location = { id: null, opt: 'create', type, template: '{}' }
    const watermark = deferred()
    x.c.watermarkFind = () => watermark.promise
    const creating = x.c.initialize()
    await flush()
    assert.equal(x.f.capture(), undefined)
    watermark.resolve({ data: { settingContent: '{}' } })
    await creating
    assert.equal(x.f.doc.id, null)
    assert.equal(x.f.capture().isCurrent(), true)
  })
  test(type + ': 模板解包失败保持不可保存', async () => {
    const x = entryFixture()
    x.route.query = { opt: 'create', createType: 'template', templateParams: '{}' }
    x.f.location = { id: null, opt: 'create', type, template: '{}' }
    x.c.decompressionPre = async () => {
      throw new Error('bad template')
    }
    await x.c.initialize()
    assert.equal(x.f.capture(), undefined)
    assert.equal(x.c.templateLoadError.value, true)
  })
  test(type + ': 草稿恢复校验归属，旧弹窗确认不能操作新资源', async () => {
    const x = entryFixture()
    x.c.state.resourceId = 'a'
    x.c.wsCache.get = () => ({ dvInfo: { id: 'wrong', type } })
    x.c.restore(true, 'a')
    await flush()
    assert.equal(x.f.capture(), undefined)
    assert.equal(x.loads.length, 1)
    x.loads[0].resolve()
    x.f.location = { id: 'b', type }
    x.c.state.resourceId = 'b'
    x.c.restore(true, 'a')
    assert.equal(x.loads.length, 1)
  })
}

const { reactive, watch, effectScope } = require('vue')
for (const [file, type, key] of [
  ['src/views/dashboard/index.vue', 'dashboard', 'resourceId'],
  ['src/views/data-visualization/indexV3.vue', 'dataV', 'dvId']
]) {
  test(type + ': 真实路由 watcher 切换重载、保存后替换路由不重载、离开不跳转', () => {
    const source = fs
      .readFileSync(file, 'utf8')
      .split('<script setup lang="ts">')[1]
      .split('</script>')[0]
    const ast = ts.createSourceFile(file + '.ts', source, ts.ScriptTarget.Latest, true)
    const watcher = ast.statements.find(
      statement =>
        ts.isExpressionStatement(statement) &&
        statement.getText(ast).startsWith('watch(') &&
        statement.getText(ast).includes('editorLocation')
    )
    assert.ok(watcher)
    const route = reactive({ currentRoute: { value: { path: '/edit', query: { [key]: 'a' } } } })
    const owner = {}
    let doc = { id: 'a', type }
    const c = {
      exports: {},
      router: route,
      embeddedStore: {},
      dvMainStore: owner,
      dvInfo: {
        get value() {
          return doc
        }
      },
      state: {},
      watch,
      useCanvasEditSession: createCanvasEditSession,
      canvasQuery: {
        invalidate: () => {
          c.invalidations++
        }
      },
      invalidations: 0,
      reloads: 0,
      initializeEditor: () => {
        c.reloads++
      }
    }
    const scope = effectScope()
    scope.run(() =>
      vm.runInNewContext(
        transpile(extract(file, ['editorRoutePath', 'editorLocation', 'editorSession'])) +
          '\nlet editorMounted = true;\n' +
          transpile(watcher.getText(ast)) +
          ';this.session = editorSession;',
        c
      )
    )
    const session = c.session
    session.begin().ready()
    const save = captureCanvasSave(owner, () => doc)
    doc.id = 'saved'
    save.saved('saved')
    route.currentRoute.value.query = { [key]: 'saved' }
    assert.equal(c.reloads, 0)
    route.currentRoute.value.query = { [key]: 'b' }
    assert.equal(c.reloads, 1)
    assert.equal(save.isCurrent(), false)
    route.currentRoute.value = { path: '/dataset-form', query: {} }
    assert.equal(c.reloads, 1)
    assert.equal(c.invalidations, 1)
    scope.stop()
  })
}
for (const file of [
  'src/components/dashboard/DbToolbar.vue',
  'src/components/data-visualization/DvToolbar.vue'
]) {
  const toolbarFixture = () => {
    const x = saveFixture(undefined, 'a')
    const navigation = deferred()
    const reloads = [],
      messages = []
    let shown = 0,
      hidden = 0,
      published = 0
    const c = {
      exports: {},
      captureCanvasSave,
      failCanvasEditSession,
      dvMainStore: x.f.owner,
      dvInfo: x.c.curDvInfo,
      canvasSaveWithParams: x.c.save,
      styleChangeTimes: { value: 1 },
      permissionStore: { getCurrentPath: '/editor' },
      queryList: { value: [] },
      eventBus: {
        emit() {
          return undefined
        }
      },
      nextTick: async callback => callback?.(),
      tryShowLoading: () => shown++,
      tryHideLoading: () => hidden++,
      wsCache: {
        delete() {
          return undefined
        }
      },
      embeddedStore: {},
      router: { replace: () => navigation.promise },
      appData: { value: null },
      initCanvasData: (_id, params, callback) => {
        const wait = deferred()
        reloads.push(wait)
        return wait.promise.then(() => {
          if (params.isCurrent()) callback()
        })
      },
      snapshotStore: { resetSnapshot: () => undefined },
      resourceAppOpt: { value: { close: () => undefined } },
      resourceAppOptShow: { value: false },
      useEmitt: () => ({ emitter: { emit: () => undefined } }),
      publishStatusChange: async () => published++,
      ElMessage: { success: text => messages.push(text), error: text => messages.push(text) },
      t: x => x,
      console: {
        error() {
          return undefined
        }
      }
    }
    x.f.owner.matrixSizeAdaptor = () => undefined
    x.f.owner.setAppDataInfo = value => {
      c.appData.value = value
    }
    vm.runInNewContext(transpile(extract(file, ['saveResource'])) + ';this.save = saveResource;', c)
    return {
      x,
      c,
      navigation,
      reloads,
      messages,
      shown: () => shown,
      hidden: () => hidden,
      published: () => published
    }
  }
  test(file + ': 路由等待期间切换后不发布，loading 必须释放', async () => {
    const f = toolbarFixture()
    const saving = f.c.save({ withPublish: true, status: 1 })
    await flush()
    assert.equal(f.shown(), 1)
    f.x.names[0].resolve()
    await flush()
    f.x.writes[0].resolve({ data: { status: 1 } })
    await flush()
    f.x.f.location = { id: 'b', type: 'dashboard' }
    f.x.f.session.begin()
    f.x.f.doc = { id: 'b', type: 'dashboard' }
    f.navigation.resolve()
    await saving
    assert.equal(f.published(), 0)
    assert.equal(f.hidden(), 1)
  })
  test(file + ': 名称校验失败时结束 loading，不能发布', async () => {
    const f = toolbarFixture()
    const saving = f.c.save({ withPublish: true, status: 1 })
    await flush()
    f.x.names[0].reject(new Error('duplicate'))
    await saving
    assert.equal(f.hidden(), 1)
    assert.equal(f.published(), 0)
    assert.equal(f.x.writes.length, 0)
  })
  for (const withPublish of [false, true]) {
    test(
      file + ': 应用保存后重载失败明确提示已保存，阻止再次写入或发布 ' + withPublish,
      async () => {
        const f = toolbarFixture()
        f.c.appData.value = { dataset: 'synthetic' }
        const saving = f.c.save({ withPublish, status: 1 })
        await flush()
        f.x.names[0].resolve()
        await flush()
        f.x.writes[0].resolve({ data: { status: 1 } })
        f.navigation.resolve()
        await flush()
        assert.equal(f.c.appData.value, null)
        f.reloads[0].reject(new Error('reload failed'))
        await saving
        assert.equal(f.published(), 0)
        assert.deepEqual(f.messages, ['visualization.saved_reload_failed'])
        assert.equal(f.hidden(), withPublish ? 1 : 0)
        assert.equal(f.x.f.capture(), undefined)
        await f.c.save({ withPublish, status: 1 })
        assert.equal(f.x.writes.length, 1)
        // 重新打开已保存资源可恢复编辑，不重复创建应用。
        f.x.f.session.begin().ready()
        assert.equal(f.x.f.capture().isCurrent(), true)
      }
    )
  }
  test(file + ': 应用重载成功后继续发布并释放 loading', async () => {
    const f = toolbarFixture()
    f.c.appData.value = { dataset: 'synthetic' }
    const saving = f.c.save({ withPublish: true, status: 1 })
    await flush()
    f.x.names[0].resolve()
    await flush()
    f.x.writes[0].resolve({ data: { status: 1 } })
    f.navigation.resolve()
    await flush()
    f.reloads[0].resolve()
    await saving
    assert.equal(f.published(), 1)
    assert.equal(f.hidden(), 1)
    assert.equal(f.c.appData.value, null)
    assert.deepEqual(f.messages, [])
  })
  test(file + ': 切换后旧应用重载失败不使新会话失效或显示提示', async () => {
    const f = toolbarFixture()
    f.c.appData.value = { dataset: 'synthetic' }
    const saving = f.c.save({ withPublish: true, status: 1 })
    await flush()
    f.x.names[0].resolve()
    await flush()
    f.x.writes[0].resolve({ data: { status: 1 } })
    f.navigation.resolve()
    await flush()
    f.x.f.location = { id: 'b', type: 'dashboard' }
    f.x.f.doc = { id: 'b', type: 'dashboard' }
    f.x.f.session.begin().ready()
    f.reloads[0].reject(new Error('old reload failed'))
    await saving
    assert.equal(f.x.f.capture().isCurrent(), true)
    assert.equal(f.published(), 0)
    assert.deepEqual(f.messages, [])
    assert.equal(f.hidden(), 1)
  })
}

test('未纳管的独立编辑入口保留保存能力，换文档后旧票据失效', () => {
  const owner = {}
  let document = { id: 'a', type: 'dashboard' }
  const save = captureCanvasSave(owner, () => document)
  assert.equal(save.isCurrent(), true)
  document = { id: 'b', type: 'dashboard' }
  assert.equal(save.isCurrent(), false)
})

test('模板解包晚于资源切换时，不适配旧模板或注册其素材', async () => {
  const wait = deferred()
  let current = true,
    published = false
  const source = fs.readFileSync('src/utils/canvasUtils.ts', 'utf8')
  const start = source.indexOf('export async function decompressionPre')
  const c = { exports: {}, decompression: () => wait.promise }
  vm.runInNewContext(
    transpile(source.slice(start, source.indexOf('\nexport function isDashboard', start))) +
      ';this.unpack = decompressionPre;',
    c
  )
  const unpacking = c.unpack(
    {},
    () => {
      published = true
    },
    () => current
  )
  current = false
  // 故意使用不可解析内容：代次检查应在任何适配或素材副作用之前返回。
  wait.resolve({ data: { componentData: 'invalid-json' } })
  await unpacking
  assert.equal(published, false)
})

test('并行首次保存生成不同 ID，晚到响应不能再次改绑已保存文档', () => {
  const f = fixture('create', null)
  f.session.begin().ready()
  const first = f.capture(),
    second = f.capture()
  f.doc.id = 'created-one'
  first.saved('created-one')
  assert.equal(first.isCurrent(), true)
  assert.equal(second.isCurrent(), false)
})

test('带素材模板保存已落库但重载失败：明确提示并阻止再次保存', async () => {
  const x = saveFixture(undefined, 'a')
  const messages = []
  x.f.doc.materialBundle = { synthetic: true }
  x.f.owner.setAppDataInfo = value => {
    x.c.appData.value = value
  }
  x.c.ElMessage.error = message => messages.push(message)
  x.c.initCanvasData = async () => {
    throw new Error('reload material failed')
  }
  const saving = x.save()
  x.names[0].resolve()
  await flush()
  x.writes[0].resolve({ data: { status: 1 } })
  await assert.rejects(saving, /reload material failed/)
  assert.equal(x.updates.length, 1)
  assert.equal(x.f.doc.contentId, 'new-content')
  assert.equal(x.callbacks.length, 0)
  assert.equal(x.f.capture(), undefined)
  assert.deepEqual(messages, ['visualization.saved_reload_failed'])
  await x.save()
  assert.equal(x.writes.length, 1)
})
