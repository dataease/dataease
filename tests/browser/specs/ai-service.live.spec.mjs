import { test, expect } from '@playwright/test'

// Real gateway/authentication/backend; do not mock responses or save configuration.
test.beforeEach(async ({ page }) => {
  await page.goto('/#/sys-setting/ai-service')
  await expect(page.getByRole('menuitem', { name: 'AI 服务', exact: true })).toBeVisible()
  await expect(page.getByRole('heading', { name: '模型管理', exact: true })).toBeVisible()
})

test('真实菜单、页面路由及六列表头', async ({ page }) => {
  await expect(page.getByRole('columnheader')).toHaveText([
    '模型名称',
    '模型标识',
    '状态',
    '使用范围',
    '测试结果',
    '操作'
  ])
  await expect(page.getByRole('tab', { name: '使用设置', exact: true })).toBeVisible()
  await expect(page.getByText('加载失败，请重试。', { exact: true })).toHaveCount(0)
  await page.screenshot({ path: 'output/ai-live-models.png' })
})

test('真实新增草稿、手动标识、密钥显示及放弃修改', async ({ page }) => {
  const writes = []
  page.on('request', (r) => {
    if (
      ['PUT', 'DELETE'].includes(r.method()) ||
      (r.method() === 'POST' &&
        /\/ai\/(?:settings|models\/\d+(?:\/enabled)?)$/.test(new URL(r.url()).pathname)) ||
      (r.method() === 'POST' && /\/api\/ai\/models$/.test(r.url()))
    )
      writes.push(r.url())
  })
  await page.getByRole('button', { name: '新增模型', exact: true }).click()
  const drawer = page.getByRole('dialog', { name: '新增模型', exact: true })
  await expect(drawer.getByPlaceholder('例如：通用分析模型')).toBeVisible()
  expect(Math.round((await drawer.boundingBox()).width)).toBe(600)
  await expect(drawer.getByRole('button', { name: '保存', exact: true })).toBeDisabled()
  await drawer.getByPlaceholder('例如：通用分析模型').fill('页面交互检查，不保存')
  const key = drawer.getByPlaceholder('请输入 API Key')
  await key.fill('synthetic-ui-key')
  await expect(key).toHaveAttribute('type', 'password')
  await drawer.getByRole('button', { name: '显示', exact: true }).click()
  await expect(key).toHaveAttribute('type', 'text')
  await drawer.getByRole('button', { name: '隐藏', exact: true }).click()
  await page.screenshot({ path: 'output/ai-live-model-drawer.png' })
  await drawer.getByRole('button', { name: '手动输入', exact: true }).click()
  await drawer.getByPlaceholder('请输入模型标识').fill('synthetic-model')
  await drawer.getByRole('button', { name: '取消', exact: true }).click()
  await expect(page.getByRole('dialog').filter({ hasText: '放弃' })).toBeVisible()
  await page.getByRole('button', { name: '放弃修改', exact: true }).click()
  await expect(drawer).not.toBeVisible()
  expect(writes).toEqual([])
})

test('真实加密请求拒绝回环地址，发现与测试分别反馈失败', async ({ page }) => {
  await page.getByRole('button', { name: '新增模型', exact: true }).click()
  const drawer = page.getByRole('dialog', { name: '新增模型', exact: true })
  await drawer.getByPlaceholder('例如：通用分析模型').fill('安全失败交互检查，不保存')
  await drawer.getByPlaceholder('https://api.example.com/v1').fill('http://127.0.0.1:9/v1')
  await drawer.getByPlaceholder('请输入 API Key').fill('synthetic-ui-key')
  await drawer.getByRole('button', { name: '手动输入', exact: true }).click()
  await drawer.getByPlaceholder('请输入模型标识').fill('synthetic-model')
  const discovery = page.waitForResponse((r) => r.url().endsWith('/api/ai/models/discover'))
  await drawer.getByRole('button', { name: '测试并刷新模型', exact: true }).click()
  const rejectedDiscovery = await discovery
  expect(rejectedDiscovery.status()).toBe(500)
  await expect(
    drawer.getByText('获取模型列表失败，可重试或手动输入。', { exact: true })
  ).toBeVisible()
  await expect(drawer.getByPlaceholder('请输入模型标识')).toHaveValue('synthetic-model')
  const generation = page.waitForResponse((r) => r.url().endsWith('/api/ai/models/test'))
  await drawer.getByRole('button', { name: '测试模型', exact: true }).click()
  const rejectedTest = await generation
  expect(rejectedTest.status()).toBe(500)
  await expect(drawer.getByRole('button', { name: '保存', exact: true })).toBeDisabled()
  await expect(
    drawer.getByText('模型测试未通过，请检查连接配置后重试。', { exact: true })
  ).toBeVisible()
})

test('真实组织配置与批量策略取消不写入', async ({ page }) => {
  const writes = []
  page.on('request', (r) => {
    if (r.method() === 'POST' && r.url().includes('/api/ai/org-policies/batch'))
      writes.push(r.url())
  })
  await page.getByRole('tab', { name: '使用设置', exact: true }).click()
  const configure = page.getByRole('button', { name: '配置', exact: true }).first()
  await expect(configure).toBeVisible()
  await configure.click()
  let drawer = page.getByRole('dialog')
  await expect(drawer.getByRole('radio', { name: '继承系统默认', exact: true })).toBeChecked()
  await expect(drawer.getByRole('radio')).toHaveCount(3)
  await drawer.getByRole('button', { name: '取消', exact: true }).click()
  await page
    .getByRole('checkbox', { name: '全选当前搜索结果', exact: true })
    .locator('xpath=ancestor::label')
    .click()
  await page.getByRole('button', { name: '批量配置', exact: true }).click()
  drawer = page.getByRole('dialog')
  await expect(drawer.getByRole('radio')).toHaveCount(3)
  await drawer.getByRole('button', { name: '取消', exact: true }).click()
  await expect(drawer).not.toBeVisible()
  expect(writes).toEqual([])
})

for (const name of ['数据使用范围', '调用限制']) {
  test(`真实${name}抽屉取消不保存`, async ({ page }) => {
    const writes = []
    page.on('request', (r) => {
      if (
        ['POST', 'PUT', 'DELETE'].includes(r.method()) &&
        /\/ai\/(?:settings|models(?:\/\d+(?:\/enabled)?)?)$/.test(new URL(r.url()).pathname)
      )
        writes.push(r.url())
    })
    await page.getByRole('tab', { name: '使用设置', exact: true }).click()
    await page.getByRole('button', { name, exact: true }).click()
    const drawer = page.getByRole('dialog', { name, exact: true })
    await expect(drawer).toBeVisible()
    await expect(drawer.getByRole('button', { name: '取消', exact: true })).toBeInViewport()
    if (name === '调用限制') await expect(drawer.getByRole('spinbutton')).toHaveCount(5)
    await drawer.getByRole('button', { name: '取消', exact: true }).click()
    await expect(drawer).not.toBeVisible()
    expect(writes).toEqual([])
  })
}
