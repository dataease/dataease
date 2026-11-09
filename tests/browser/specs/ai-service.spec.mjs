import { test, expect } from '@playwright/test'
import { readFileSync } from 'node:fs'

const productRadioRule = readFileSync(
  new URL('../../../core/core-frontend/src/style/index.less', import.meta.url),
  'utf8'
).match(/\.ed-radio\s*\{[^}]+\}/)[0]

const makeModel = (id, name, extra = {}) => ({
  id,
  name,
  provider: 'OPENAI_COMPATIBLE',
  baseUrl: 'https://model.invalid/v1',
  modelIdentifier: id === '1001' ? 'general-chat' : 'finance-chat',
  keyConfigured: true,
  enabled: true,
  validated: true,
  configRevision: 1,
  rowVersion: 0,
  processingLocation: 'UNKNOWN',
  processingNote: '',
  lastTestStatus: 'PASSED',
  lastTestTime: 1792632600000,
  organizationReferences: 0,
  historicalOrganizationReferences: 0,
  organizationNames: [],
  systemDefault: false,
  ...extra
})
let state
let requests

test.beforeEach(async ({ page }) => {
  requests = []
  state = {
    settings: {
      enabled: true,
      defaultModelId: '1001',
      allowSamples: false,
      requestTimeoutSeconds: 60,
      maxInputChars: 30000,
      maxOutputTokens: 4096,
      interactiveConcurrency: 5,
      backgroundConcurrency: 2,
      revision: 1,
      rowVersion: 0,
      persistedRevision: 1,
      appliedRevision: 1,
      runtimeState: 'ACTIVE',
      organizationPolicyAvailable: true
    },
    models: [
      makeModel('1001', '通用模型', { systemDefault: true }),
      makeModel('1002', '财务内网模型', {
        organizationReferences: 1,
        organizationNames: ['财务部']
      }),
      makeModel('1003', '测试模型', {
        enabled: false,
        validated: false,
        lastTestStatus: 'UNTESTED',
        lastTestTime: null
      })
    ],
    orgs: [
      {
        id: '21',
        name: '销售部',
        parentName: '总部',
        mode: 'INHERIT',
        modelId: null,
        rowVersion: null,
        effectiveModel: '通用模型'
      },
      {
        id: '22',
        name: '财务部',
        parentName: '总部',
        mode: 'SPECIFIED',
        modelId: '1002',
        rowVersion: 0,
        effectiveModel: '财务内网模型'
      },
      {
        id: '23',
        name: '运营部',
        parentName: '总部',
        mode: 'DISABLED',
        modelId: null,
        rowVersion: 0,
        effectiveModel: null,
        unavailableReason: 'organization_disabled'
      }
    ]
  }
  await page.route('**/fixture-api/**', async (route) => {
    const request = route.request(),
      url = new URL(request.url()),
      path = url.pathname.replace('/fixture-api', '')
    const body = request.postDataJSON()
    requests.push({ path, method: request.method(), body })
    let result = null
    if (path === '/ai/settings') {
      if (request.method() === 'POST') {
        Object.assign(state.settings, body)
        state.models.forEach((model) => {
          model.systemDefault = model.id === body.defaultModelId
        })
      }
      result = state.settings
    } else if (path.endsWith('/impact'))
      result = {
        token: 'synthetic-impact',
        systemDefault: path.includes('/settings/') || path.includes('1001'),
        organizationCount: state.settings.organizationPolicyAvailable ? 2 : 0,
        organizations: state.settings.organizationPolicyAvailable ? ['销售部', '研发部'] : []
      }
    else if (path === '/ai/models/drafts') result = { id: '2001', draftHandle: 'synthetic-draft' }
    else if (path === '/ai/models/discover') result = ['general-chat', 'reasoning-chat']
    else if (path === '/ai/models/test' || path.endsWith('/test'))
      result = { basic: true, structured: true, errorCode: null, testProof: 'synthetic-proof' }
    else if (path === '/ai/models' && request.method() === 'POST') {
      result = makeModel(body.id, body.name, { ...body })
      state.models.push(result)
    } else if (path === '/ai/models') {
      const term = url.searchParams.get('search') || ''
      const items = state.models.filter((m) => (m.name + m.modelIdentifier).includes(term))
      result = { items, total: items.length }
    } else if (path === '/ai/org-policies') {
      const term = url.searchParams.get('search') || ''
      const items = state.orgs.filter((o) => o.name.includes(term))
      result = { items, total: items.length, available: true }
    } else if (path === '/ai/org-policies/batch') result = {}
    else if (path.match(/\/ai\/models\/\d+$/)) {
      result = state.models.find((m) => m.id === path.split('/').pop())
      if (request.method() === 'POST') Object.assign(result, body)
    } else result = {}
    await route.fulfill({ json: result })
  })
  await page.goto('/')
  await expect(page.getByRole('button', { name: '新增模型', exact: true })).toBeEnabled()
})

const openNew = async (page) => {
  await page.getByRole('button', { name: '新增模型', exact: true }).click()
  const dialog = page.getByRole('dialog')
  await expect(dialog.getByPlaceholder('例如：通用分析模型')).toBeVisible()
  await dialog.getByPlaceholder('https://api.example.com/v1').fill('https://mock.invalid/v1')
  await dialog.getByPlaceholder('请输入 API Key').fill('synthetic-not-a-secret')
  return dialog
}

test('AI 目录兼容旧入口并切换至调用记录占位页', async ({ page }) => {
  const errors = []
  page.on('pageerror', (error) => errors.push(error.message))
  await page.goto('/?view=ai-menu')
  await expect(page.getByRole('button', { name: '新增模型', exact: true })).toBeVisible()
  await expect
    .poll(() => page.evaluate(() => window.aiRoute()))
    .toBe('/sys-setting/ai-service/models')
  await page.evaluate(() => window.aiNavigate('ai-records'))
  await expect(page.getByRole('heading', { name: '调用记录', exact: true })).toBeVisible()
  await expect(
    page.getByText('后续将在此展示 AI 服务的调用记录及执行详情。', { exact: true })
  ).toBeVisible()
  await page.screenshot({ path: 'output/ai-records-placeholder.png' })
  expect(errors).toEqual([])
})

test('仅有调用记录菜单时旧入口落到占位页，不请求配置 API', async ({ page }) => {
  requests = []
  await page.goto('/?view=ai-menu&access=records')
  await expect(page.getByRole('heading', { name: '调用记录', exact: true })).toBeVisible()
  await expect
    .poll(() => page.evaluate(() => window.aiRoute()))
    .toBe('/sys-setting/ai-service/records')
  expect(requests).toHaveLength(0)
})

test('发现不要求名称，不自动选择；手动模式及改名免测、连接变更撤销资格', async ({ page }) => {
  const dialog = await openNew(page)
  await dialog.getByRole('button', { name: '测试并刷新模型', exact: true }).click()
  await expect.poll(() => requests.filter((r) => r.path === '/ai/models/discover').length).toBe(1)
  await expect(dialog.getByRole('status')).toHaveText('已加载 2 个模型')
  await expect(dialog.getByRole('status')).toHaveClass(/ai-passed/)
  await expect(dialog.getByRole('button', { name: '保存', exact: true })).toBeDisabled()
  await dialog.getByRole('button', { name: '手动输入', exact: true }).click()
  await dialog.getByPlaceholder('请输入模型标识').fill('manual-model')
  await dialog.getByRole('button', { name: '测试模型', exact: true }).click()
  await expect(dialog.getByRole('status').filter({ hasText: '测试通过' })).toHaveAttribute(
    'title',
    '基础生成和结构化响应均通过。'
  )
  await dialog.getByPlaceholder('例如：通用分析模型').fill('新的模型')
  await expect(dialog.getByRole('button', { name: '保存', exact: true })).toBeEnabled()
  await dialog.getByPlaceholder('请输入 API Key').fill('changed-synthetic-key')
  await expect(dialog.getByPlaceholder('请输入模型标识')).toHaveValue('')
  await expect(dialog.getByRole('button', { name: '保存', exact: true })).toBeDisabled()
})

test('编辑留空密钥，改名直接保存；请求不带旧密钥', async ({ page }) => {
  await page
    .getByRole('row')
    .filter({ hasText: '通用模型' })
    .getByRole('button', { name: '编辑', exact: true })
    .click()
  const dialog = page.getByRole('dialog')
  await expect(dialog.getByPlaceholder('留空保留原密钥')).toHaveValue('')
  await dialog.getByPlaceholder('例如：通用分析模型').fill('通用模型改名')
  await dialog.getByRole('button', { name: '保存', exact: true }).click()
  await expect(dialog).not.toBeVisible()
  const save = requests.find((r) => r.method === 'POST' && r.path === '/ai/models/1001')
  expect(save.body.apiKey).toBe('')
  expect(save.body.testProof).toBeUndefined()
  expect(save.body.expectedVersion).toBe(0)
})

test('同连接刷新失败保留列表，过期测试响应不能恢复资格', async ({ page }) => {
  const dialog = await openNew(page)
  await dialog.getByRole('button', { name: '测试并刷新模型', exact: true }).click()
  await expect.poll(() => requests.filter((r) => r.path === '/ai/models/discover').length).toBe(1)
  await page.route('**/fixture-api/ai/models/discover', (r) => r.fulfill({ status: 503, json: {} }))
  await dialog.getByRole('button', { name: '测试并刷新模型', exact: true }).click()
  await expect(dialog.getByRole('status')).toContainText('获取模型列表失败')
  await expect(dialog.getByRole('status')).toHaveClass(/ai-failed/)
  await dialog.locator('.ed-select').nth(1).click()
  await expect(page.getByRole('option', { name: 'general-chat', exact: true })).toBeVisible()
  await page.getByRole('option', { name: 'general-chat', exact: true }).click()
  await page.route('**/fixture-api/ai/models/test', async (r) => {
    await new Promise((resolve) => setTimeout(resolve, 250))
    await r.fulfill({ json: { basic: true, structured: true, testProof: 'late-proof' } })
  })
  await dialog.getByRole('button', { name: '测试模型', exact: true }).click()
  await dialog.getByPlaceholder('https://api.example.com/v1').fill('https://changed.invalid/v1')
  await page.waitForTimeout(350)
  await expect(dialog.getByRole('button', { name: '保存', exact: true })).toBeDisabled()
  await expect(dialog.getByText('测试通过', { exact: true })).toHaveCount(0)
})

for (const [code, message] of [
  ['dns_failed', '无法解析模型服务域名'],
  ['connection_failed', '无法连接模型服务'],
  ['tls_failed', '模型服务 TLS 握手或证书校验失败']
])
  test(`模型连接错误显示具体原因并保留内网地址和输入 ${code}`, async ({ page }) => {
    const dialog = await openNew(page)
    await dialog
      .getByPlaceholder('https://api.example.com/v1')
      .fill('http://models.internal:11434/gateway/v1')
    await dialog.getByRole('button', { name: '手动输入', exact: true }).click()
    await dialog.getByPlaceholder('请输入模型标识').fill('manual-model')
    await page.route('**/fixture-api/ai/models/test', (route) =>
      route.fulfill({ json: { basic: false, structured: false, errorCode: code, testProof: null } })
    )
    await dialog.getByRole('button', { name: '测试模型', exact: true }).click()
    await expect(dialog.getByRole('alert')).toContainText(message)
    await expect(dialog.getByRole('status')).toHaveText('测试失败')
    await expect(dialog.getByRole('status')).toHaveClass(/ai-failed/)
    await expect(dialog.getByRole('button', { name: '重新测试', exact: true })).toBeEnabled()
    await expect(dialog.getByPlaceholder('https://api.example.com/v1')).toHaveValue(
      'http://models.internal:11434/gateway/v1'
    )
    await expect(dialog.getByPlaceholder('请输入模型标识')).toHaveValue('manual-model')
    await expect(dialog.getByRole('button', { name: '保存', exact: true })).toBeDisabled()
  })

test('刷新空列表提示手动输入；成功提示在连接改变后清除', async ({ page }) => {
  const dialog = await openNew(page)
  await page.route('**/fixture-api/ai/models/discover', (route) => route.fulfill({ json: [] }))
  await dialog.getByRole('button', { name: '测试并刷新模型', exact: true }).click()
  await expect(dialog.getByRole('status')).toHaveText('暂无模型，请手动输入。')
  await expect(dialog.getByRole('button', { name: '保存', exact: true })).toBeDisabled()
  await page.unroute('**/fixture-api/ai/models/discover')
  await dialog.getByRole('button', { name: '测试并刷新模型', exact: true }).click()
  await expect(dialog.getByRole('status')).toHaveText('已加载 2 个模型')
  await dialog.getByPlaceholder('https://api.example.com/v1').fill('https://changed.invalid/v1')
  await expect(dialog.getByRole('status')).toHaveCount(0)
})

test('模型列表测试成功与失败均有消息反馈', async ({ page }) => {
  const row = page.getByRole('row').filter({ hasText: '通用模型' })
  await row.getByRole('button', { name: '测试', exact: true }).click()
  await expect(page.locator('.ed-message--success')).toContainText('“通用模型”测试通过')
  await page.route('**/fixture-api/ai/models/1001/test', (route) =>
    route.fulfill({
      json: {
        basic: false,
        structured: false,
        errorCode: 'provider_authentication',
        testProof: null
      }
    })
  )
  await row.getByRole('button', { name: '测试', exact: true }).click()
  await expect(page.locator('.ed-message--error')).toContainText('模型服务认证失败')
})

test('放弃修改二次确认及切换身份清除编辑状态', async ({ page }) => {
  const dialog = await openNew(page)
  await dialog.getByRole('button', { name: '取消', exact: true }).click()
  await expect(page.getByRole('dialog', { name: '放弃未保存的修改？' })).toBeVisible()
  await page.getByRole('button', { name: '继续编辑', exact: true }).click()
  await expect(dialog.getByPlaceholder('请输入 API Key')).toHaveValue('synthetic-not-a-secret')
  await page.evaluate(() => window.aiSwitchUser())
  await expect(page.getByPlaceholder('请输入 API Key')).toHaveCount(0)
})

test('组织搜索保留隐藏选择，批量初始未选策略且仅提交明确ID', async ({ page }) => {
  await page.getByRole('tab', { name: '使用设置', exact: true }).click()
  await page
    .getByRole('checkbox', { name: '选择销售部', exact: true })
    .locator('xpath=ancestor::label')
    .click()
  await page.getByRole('textbox', { name: '搜索组织名称', exact: true }).fill('财务')
  await expect(page.getByText('其中 1 个不在当前搜索结果中')).toBeVisible()
  await page
    .getByRole('checkbox', { name: '选择财务部', exact: true })
    .locator('xpath=ancestor::label')
    .click()
  await page.getByRole('button', { name: '批量配置', exact: true }).click()
  const dialog = page.getByRole('dialog')
  await expect(dialog.getByRole('button', { name: '应用到 2 个组织' })).toBeDisabled()
  await expect(dialog.locator('.ai-batch-scope')).toContainText('当前策略不一致')
  await dialog.getByRole('button', { name: '查看名单', exact: true }).click()
  await expect(dialog.locator('.ai-selected-list li')).toHaveText([
    '销售部继承系统默认',
    '财务部指定模型 · 财务内网模型'
  ])
  await dialog.screenshot({ path: 'output/ai-org-batch.png' })
  await dialog
    .getByRole('radio', { name: '继承系统默认', exact: true })
    .locator('xpath=ancestor::label')
    .click()
  await dialog.getByRole('button', { name: '应用到 2 个组织' }).click()
  const batch = requests.find((r) => r.path === '/ai/org-policies/batch')
  expect(batch.body.organizations.map((o) => o.id).sort()).toEqual(['21', '22'])
  expect(batch.body.modelId).toBeNull()
})

test('单组织配置对齐原型：策略卡片、生效模型、禁用说明和规则展开', async ({ page }) => {
  await page.addStyleTag({ content: productRadioRule })
  await page.getByRole('tab', { name: '使用设置', exact: true }).click()
  await page
    .getByRole('row')
    .filter({ hasText: '销售部' })
    .getByRole('button', { name: '配置', exact: true })
    .click()
  const dialog = page.getByRole('dialog')
  expect(Math.round((await dialog.boundingBox()).width)).toBe(600)
  await expect(dialog.getByRole('button', { name: '保存', exact: true })).toBeDisabled()
  await expect(dialog.locator('.ai-org-identity')).toContainText('上级组织：总部')
  await expect(dialog.locator('.ai-policy-preview')).toHaveText('生效模型通用模型')
  for (const card of await dialog.locator('.ai-policy-options .ed-radio').all()) {
    const bounds = await card.boundingBox()
    expect(Math.round(bounds.height)).toBe(52)
    expect(Math.round(bounds.width)).toBe(552)
  }
  await dialog.screenshot({ path: 'output/ai-org-single.png' })
  await dialog.locator('summary').click()
  await expect(dialog.locator('.ai-rules li')).toHaveCount(3)
  await expect(dialog.locator('.ai-rules')).toContainText(
    '组织代理按目标组织，后台任务按保存的执行身份。'
  )
  await dialog
    .getByRole('radio', { name: '指定模型', exact: true })
    .locator('xpath=ancestor::label')
    .click()
  await expect(dialog.locator('.ai-policy-preview')).toHaveCount(0)
  await dialog.locator('.ed-select').click()
  await page.getByRole('option', { name: '财务内网模型', exact: true }).click()
  await expect(dialog.locator('.ai-policy-preview')).toContainText('财务内网模型')
  await dialog
    .getByRole('radio', { name: '禁用 AI 服务', exact: true })
    .locator('xpath=ancestor::label')
    .click()
  await expect(dialog.locator('.ai-policy-notice')).toHaveText(
    '保存后，该组织的成员、代理及后台任务将停止新的 AI 调用。'
  )
  await expect(dialog.getByRole('button', { name: '保存', exact: true })).toBeEnabled()
})

for (const viewport of [
  { width: 1600, height: 1000 },
  { width: 1280, height: 720 }
])
  test(`原型布局与设置抽屉 ${viewport.width}`, async ({ page }) => {
    await page.setViewportSize(viewport)
    await expect(page.getByRole('columnheader')).toHaveCount(6)
    // 窄窗口下横向滚动表格，操作按钮仍固定可见且完整落在单元格内。
    await page.setViewportSize({ width: 900, height: viewport.height })
    const actions = page.locator('.ai-row-actions').first()
    await expect(actions.locator('.ai-more-icon svg')).toBeVisible()
    await expect
      .poll(() =>
        actions.evaluate((element) => {
          const bounds = element.getBoundingClientRect()
          const cell = element.closest('td').getBoundingClientRect()
          const buttons = [...element.children].map((child) => child.getBoundingClientRect())
          return {
            left: Math.round(bounds.left - cell.left),
            right: Math.round(cell.right - bounds.right),
            gaps: buttons
              .slice(1)
              .map((button, index) => Math.round(button.left - buttons[index].right))
          }
        })
      )
      .toEqual({ left: 24, right: 24, gaps: [16, 16] })
    const before = await actions.boundingBox()
    await page.locator('.ai-model-table .ed-scrollbar__wrap').evaluate((element) => {
      element.scrollLeft = element.scrollWidth
    })
    await expect.poll(async () => (await actions.boundingBox()).x).toBeCloseTo(before.x, 0)
    expect(
      await actions.locator('button').evaluateAll((buttons) =>
        buttons.every((button) => {
          const bounds = button.getBoundingClientRect()
          const cell = button.closest('td').getBoundingClientRect()
          return (
            bounds.left >= cell.left && bounds.right <= cell.right && bounds.right <= innerWidth
          )
        })
      )
    ).toBe(true)
    await page.setViewportSize(viewport)
    await page.getByRole('tab', { name: '使用设置', exact: true }).click()
    await page.getByRole('button', { name: '调用限制', exact: true }).click()
    const dialog = page.getByRole('dialog')
    await expect(dialog.getByRole('spinbutton')).toHaveCount(5)
    await expect(dialog.getByRole('button', { name: '保存', exact: true })).toBeInViewport()
    await dialog.getByRole('spinbutton').nth(4).fill('6')
    await expect(dialog.getByRole('button', { name: '保存', exact: true })).toBeDisabled()
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true)
    await page.screenshot({ path: `output/ai-limits-${viewport.width}.png` })
  })

test('保存结果未知时按稳定ID核对并保留候选，禁止自动另建', async ({ page }) => {
  const dialog = await openNew(page)
  await dialog.getByPlaceholder('例如：通用分析模型').fill('新的模型')
  await dialog.getByRole('button', { name: '手动输入', exact: true }).click()
  await dialog.getByPlaceholder('请输入模型标识').fill('manual-model')
  await dialog.getByRole('button', { name: '测试模型', exact: true }).click()
  await expect(dialog.getByRole('button', { name: '保存', exact: true })).toBeEnabled()
  await page.route('**/fixture-api/ai/models', async (route) => {
    if (route.request().method() !== 'POST') return route.fallback()
    const body = route.request().postDataJSON()
    state.models.push(makeModel(body.id, body.name, { ...body }))
    await route.fulfill({ status: 503, json: {} })
  })
  await dialog.getByRole('button', { name: '保存', exact: true }).click()
  await expect
    .poll(() => requests.filter((r) => r.path === '/ai/models/2001' && r.method === 'GET').length)
    .toBe(1)
  await expect(dialog.getByPlaceholder('例如：通用分析模型')).toHaveValue('新的模型')
  expect(requests.filter((r) => r.path === '/ai/models/drafts')).toHaveLength(1)
})

test('维护失败保留表单与手动模型标识，样本默认关闭', async ({ page }) => {
  const dialog = await openNew(page)
  await dialog.getByRole('button', { name: '手动输入', exact: true }).click()
  await dialog.getByPlaceholder('请输入模型标识').fill('manual-model')
  await page.route('**/fixture-api/ai/models/test', (route) =>
    route.fulfill({
      json: { basic: true, structured: false, errorCode: 'response_invalid', testProof: null }
    })
  )
  await dialog.getByRole('button', { name: '测试模型', exact: true }).click()
  await expect(dialog.getByRole('alert')).toContainText('模型返回为空、被截断或不符合响应格式')
  await expect(dialog.getByPlaceholder('请输入模型标识')).toHaveValue('manual-model')
  await expect(dialog.getByRole('button', { name: '保存', exact: true })).toBeDisabled()
  await dialog.getByRole('button', { name: '取消', exact: true }).click()
  await page.getByRole('button', { name: '放弃修改', exact: true }).click()
  await page.getByRole('tab', { name: '使用设置', exact: true }).click()
  await page.getByRole('button', { name: '数据使用范围', exact: true }).click()
  await expect(
    page.getByRole('dialog').getByRole('switch', { name: '允许发送样本明细' })
  ).not.toBeChecked()
})

for (const dark of [false, true])
  test(`模型抽屉视觉检查 ${dark ? 'dark' : 'light'}`, async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 720 })
    if (dark) await page.evaluate(() => document.documentElement.classList.add('dark'))
    const dialog = await openNew(page)
    await expect(dialog.getByRole('button', { name: '保存', exact: true })).toBeInViewport()
    await expect(dialog.getByRole('button', { name: '测试模型', exact: true })).toBeInViewport()
    await dialog.getByPlaceholder('选填，不含密钥或连接凭据').scrollIntoViewIfNeeded()
    await expect(dialog.getByPlaceholder('选填，不含密钥或连接凭据')).toBeInViewport()
    await page.screenshot({ path: `output/ai-model-${dark ? 'dark' : 'light'}.png` })
  })

test('系统默认模型入口、菜单和变更/清除确认与原型一致', async ({ page }) => {
  const trigger = page.locator('.ai-default-model-select')
  await expect(trigger).toContainText('系统默认模型')
  await expect(trigger).toHaveAttribute('title', '当前：通用模型')
  expect(Math.round((await trigger.boundingBox()).width)).toBe(146)
  await expect(trigger.locator('svg')).toHaveCount(2)
  await trigger.click()
  await expect(page.getByRole('option')).toHaveText(['不设置默认模型', '通用模型', '财务内网模型'])
  expect(
    Math.round(
      (await page.locator('.ai-default-model-options .ed-select-dropdown').boundingBox()).width
    )
  ).toBe(280)
  const originalViewport = page.viewportSize()
  for (const width of [1600, 1280, 900]) {
    await page.setViewportSize({ width, height: 900 })
    await expect
      .poll(async () => {
        const button = await trigger.boundingBox()
        const menu = await page.locator('.ed-popper.ai-default-model-options').boundingBox()
        return (
          menu.y >= button.y + button.height &&
          Math.abs(menu.x + menu.width - button.x - button.width) <= 2
        )
      })
      .toBe(true)
  }
  await page.setViewportSize(originalViewport)
  await page.getByRole('option', { name: '财务内网模型', exact: true }).click()
  let dialog = page.locator('.ai-model-default-confirm')
  await expect(dialog).toContainText('变更系统默认模型？')
  await expect(dialog.locator('.ai-default-content strong')).toHaveText('财务内网模型')
  await expect(dialog).toContainText('2 个继承系统默认的组织（销售部、研发部）')
  await expect(dialog.locator('.ed-message-box__status')).toHaveCount(0)
  expect(Math.round((await dialog.boundingBox()).width)).toBe(480)
  await dialog.getByRole('button', { name: '取消', exact: true }).click()
  expect(requests.filter((r) => r.path === '/ai/settings' && r.method === 'POST')).toHaveLength(0)
  await expect(page.locator('.ed-message')).toHaveCount(0)
  await trigger.click()
  await page.getByRole('option', { name: '不设置默认模型', exact: true }).click()
  await expect(dialog.getByRole('button', { name: '清除默认模型', exact: true })).toHaveClass(
    /ed-button--danger/
  )
  await expect(dialog).toContainText('不会自动换用其他模型。')
  await dialog.getByRole('button', { name: '取消', exact: true }).click()
  await trigger.click()
  await page.getByRole('option', { name: '财务内网模型', exact: true }).click()
  await expect(dialog.getByRole('button', { name: '确认变更', exact: true })).toHaveClass(
    /ed-button--primary/
  )
  await dialog.screenshot({ path: 'output/ai-default-confirm.png' })
  await dialog.getByRole('button', { name: '确认变更', exact: true }).click()
  await expect(trigger).toHaveAttribute('title', '当前：财务内网模型')
  await expect(
    page.locator('.ed-message--success').filter({ hasText: '系统默认模型已更新' })
  ).toHaveCount(1)
  await expect(trigger).toContainText('系统默认模型')
  expect(requests.find((r) => r.path === '/ai/settings' && r.method === 'POST').body).toMatchObject(
    { defaultModelId: '1002', impactToken: 'synthetic-impact' }
  )
  await trigger.click()
  await page.getByRole('option', { name: '不设置默认模型', exact: true }).click()
  await dialog.getByRole('button', { name: '清除默认模型', exact: true }).click()
  await expect(
    page.locator('.ed-message--success').filter({ hasText: '已清除系统默认模型' })
  ).toHaveCount(1)
  await expect(trigger).toHaveAttribute('title', '当前：不设置默认模型')
})

for (const [option, message] of [
  ['财务内网模型', '设置系统默认模型失败'],
  ['不设置默认模型', '清除系统默认模型失败']
])
  test(`默认模型影响查询失败有提示且不提交：${option}`, async ({ page }) => {
    await page.route('**/fixture-api/ai/settings/impact**', (route) => route.abort('failed'))
    await page.locator('.ai-default-model-select').click()
    await page.getByRole('option', { name: option, exact: true }).click()
    await expect(page.locator('.ed-message--error')).toContainText(message)
    await expect(page.locator('.ed-message--success')).toHaveCount(0)
    expect(requests.filter((r) => r.path === '/ai/settings' && r.method === 'POST')).toHaveLength(0)
    await expect(page.locator('.ai-default-model-select')).toHaveAttribute(
      'title',
      '当前：通用模型'
    )
  })

test('默认模型提交断网提示结果未确认，不报成功或自动重试', async ({ page }) => {
  let submissions = 0
  await page.route('**/fixture-api/ai/settings', (route) => {
    if (route.request().method() !== 'POST') return route.fallback()
    submissions++
    return route.abort('failed')
  })
  await page.locator('.ai-default-model-select').click()
  await page.getByRole('option', { name: '财务内网模型', exact: true }).click()
  await page.getByRole('button', { name: '确认变更', exact: true }).click()
  await expect(page.locator('.ed-message--error')).toContainText('操作结果未确认')
  await expect(page.locator('.ed-message--success')).toHaveCount(0)
  expect(submissions).toBe(1)
})

test('默认模型已保存但运行策略未生效时只提示待生效', async ({ page }) => {
  await page.route('**/fixture-api/ai/settings', (route) => {
    if (route.request().method() !== 'POST') return route.fallback()
    Object.assign(state.settings, route.request().postDataJSON(), { runtimeState: 'UPDATING' })
    return route.fulfill({ json: state.settings })
  })
  await page.locator('.ai-default-model-select').click()
  await page.getByRole('option', { name: '财务内网模型', exact: true }).click()
  await page.getByRole('button', { name: '确认变更', exact: true }).click()
  await expect(page.locator('.ed-message--warning')).toContainText('运行策略尚未生效')
  await expect(page.locator('.ed-message--success')).toHaveCount(0)
})

test('关闭 AI 服务弹窗对齐原型，取消不提交、确认保留配置', async ({ page }) => {
  const master = page.locator('.ai-master-switch .ed-switch')
  await master.click()
  const dialog = page.locator('.ai-model-off-confirm')
  await expect(dialog).toContainText('关闭 AI 服务？')
  await expect(dialog.locator('.ai-disable-content')).toHaveText(
    '所有组织及后台任务将停止新的 AI 调用，现有配置保留。'
  )
  await expect(dialog.locator('.ed-message-box__status')).toHaveCount(0)
  expect(Math.round((await dialog.boundingBox()).width)).toBe(480)
  await expect(dialog.getByRole('button', { name: '关闭服务', exact: true })).toHaveClass(
    /ed-button--danger/
  )
  await expect(dialog.getByText('影响范围', { exact: true })).toHaveCount(0)
  await expect
    .poll(() =>
      dialog.evaluate((element) => {
        let opacity = 1
        for (let node = element; node; node = node.parentElement)
          opacity *= Number(getComputedStyle(node).opacity)
        return opacity
      })
    )
    .toBe(1)
  await dialog.screenshot({ path: 'output/ai-off-confirm.png' })
  await dialog.getByRole('button', { name: '取消', exact: true }).click()
  await expect(page.getByRole('switch', { name: 'AI 服务', exact: true })).toBeChecked()
  expect(requests.filter((r) => r.path === '/ai/settings' && r.method === 'POST')).toHaveLength(0)
  await master.click()
  await dialog.getByRole('button', { name: '关闭服务', exact: true }).click()
  await expect(page.getByRole('switch', { name: 'AI 服务', exact: true })).not.toBeChecked()
  expect(requests.find((r) => r.path === '/ai/settings' && r.method === 'POST').body).toMatchObject(
    { enabled: false, defaultModelId: '1001', impactToken: 'synthetic-impact' }
  )
})

test('默认清空与停用先确认，取消不写入；引用模型禁止删除', async ({ page }) => {
  const first = page.getByRole('row').filter({ hasText: '通用模型' })
  await first.getByRole('button', { name: '更多', exact: true }).click()
  await expect(page.getByRole('menuitem')).toHaveText(['取消默认', '删除'])
  await page.getByRole('menuitem', { name: '取消默认', exact: true }).click()
  await expect(page.getByText('清除系统默认模型？', { exact: true })).toBeVisible()
  await expect(page.getByText(/不会自动换用其他模型/)).toBeVisible()
  await page.getByRole('button', { name: '取消', exact: true }).click()
  expect(requests.filter((r) => r.path === '/ai/settings' && r.method === 'POST')).toHaveLength(0)
  await first.locator('.ed-switch').click()
  await expect(page.getByText('停用“通用模型”？', { exact: true })).toBeVisible()
  const disableDialog = page.locator('.ai-model-disable-confirm')
  await expect(disableDialog).toHaveClass(/ai-model-disable-confirm/)
  expect((await disableDialog.boundingBox()).width).toBe(480)
  await expect(disableDialog.locator('.ed-message-box__status')).toHaveCount(0)
  await expect(disableDialog.getByRole('listitem')).toHaveText([
    '系统默认模型',
    '销售部',
    '研发部',
    '无组织调用与系统任务'
  ])
  await expect(disableDialog.getByRole('button', { name: '确认停用', exact: true })).toHaveClass(
    /ed-button--danger/
  )
  await expect
    .poll(() =>
      disableDialog.evaluate((element) => {
        let opacity = 1
        for (let node = element; node; node = node.parentElement)
          opacity *= Number(getComputedStyle(node).opacity)
        return opacity
      })
    )
    .toBe(1)
  await page.screenshot({ path: 'output/ai-disable-confirm.png' })
  await page.getByRole('button', { name: '取消', exact: true }).click()
  await expect(first.getByRole('switch')).toBeChecked()
  expect(requests.filter((r) => r.path.endsWith('/enabled'))).toHaveLength(0)
  const finance = page.getByRole('row').filter({ hasText: '财务内网模型' })
  await expect(finance.getByText('财务部（组织指定）', { exact: true })).toBeVisible()
  await finance.getByRole('button', { name: '更多', exact: true }).click()
  await page.getByRole('menuitem', { name: '删除', exact: true }).click()
  await expect(page.getByText('该模型仍被引用，无法删除', { exact: true })).toBeVisible()
  await page.getByRole('button', { name: '知道了', exact: true }).click()
  expect(requests.filter((r) => r.method === 'DELETE')).toHaveLength(0)
  await page.route('**/fixture-api/ai/models/1001/enabled', (route) => {
    state.models[0].enabled = false
    return route.fallback()
  })
  await first.locator('.ed-switch').click()
  await page.getByRole('button', { name: '确认停用', exact: true }).click()
  await expect(first.getByRole('switch')).not.toBeChecked()
  expect(requests.find((r) => r.path === '/ai/models/1001/enabled').body).toEqual({
    expectedVersion: 0,
    enabled: false,
    impactToken: 'synthetic-impact'
  })
})

test('策略已保存但尚未生效时显示运行状态', async ({ page }) => {
  await page.getByRole('tab', { name: '使用设置', exact: true }).click()
  await page.getByRole('button', { name: '调用限制', exact: true }).click()
  await page.route('**/fixture-api/ai/settings', async (route) => {
    if (route.request().method() !== 'POST') return route.fallback()
    Object.assign(state.settings, route.request().postDataJSON(), {
      runtimeState: 'UPDATING',
      revision: 2,
      persistedRevision: 2,
      appliedRevision: 1
    })
    await route.fulfill({ json: state.settings })
  })
  await page.getByRole('dialog').getByRole('button', { name: '保存', exact: true }).click()
  await expect(page.getByRole('dialog')).not.toBeVisible()
  await expect(page.locator('.ai-service-page > .ed-alert')).toContainText('运行策略尚未生效')
})

test('社区能力隐藏整块组织配置，不请求组织接口，保留两项全局设置', async ({ page }) => {
  state.settings.organizationPolicyAvailable = false
  await page.reload()
  await page.getByRole('tab', { name: '使用设置', exact: true }).click()
  await expect(page.getByRole('heading', { name: '组织使用配置', exact: true })).toHaveCount(0)
  await expect(page.getByRole('button', { name: '批量配置', exact: true })).toHaveCount(0)
  await expect(page.getByRole('button', { name: '数据使用范围', exact: true })).toBeVisible()
  await expect(page.getByRole('button', { name: '调用限制', exact: true })).toBeVisible()
  expect(requests.filter((r) => r.path.startsWith('/ai/org-policies'))).toHaveLength(0)
})

test('社区历史组织引用不显示为当前使用范围，删除仍明确阻止', async ({ page }) => {
  state.settings.organizationPolicyAvailable = false
  Object.assign(state.models[1], {
    organizationReferences: 0,
    organizationNames: [],
    historicalOrganizationReferences: 1
  })
  await page.reload()
  const finance = page.getByRole('row').filter({ hasText: '财务内网模型' })
  await expect(finance.getByText('财务部（组织指定）', { exact: true })).toHaveCount(0)
  await finance.getByRole('button', { name: '更多', exact: true }).click()
  await page.getByRole('menuitem', { name: '删除', exact: true }).click()
  await expect(
    page
      .getByRole('dialog')
      .filter({ hasText: '“财务内网模型”仍被历史组织策略引用，暂时无法删除。' })
  ).toBeVisible()
  expect(requests.filter((r) => r.method === 'DELETE')).toHaveLength(0)
})

test('能力字段缺失时显示加载失败，不把未知状态当作社区模式', async ({ page }) => {
  delete state.settings.organizationPolicyAvailable
  await page.reload()
  await expect(page.getByText('加载失败，请重试。', { exact: true })).toBeVisible()
  expect(requests.filter((r) => r.path.startsWith('/ai/org-policies'))).toHaveLength(0)
})
