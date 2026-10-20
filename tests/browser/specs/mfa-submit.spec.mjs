import { test, expect } from '@playwright/test'

const pageErrors = new WeakMap()
test.beforeEach(async ({ page }) => {
  const errors = []
  pageErrors.set(page, errors)
  page.on('pageerror', error => errors.push(error.message))
})
test.afterEach(async ({ page }) => {
  expect(pageErrors.get(page), 'no uncaught browser errors').toEqual([])
})

async function setup(page, query = '') {
  const requests = []
  let release
  const gate = new Promise(resolve => { release = resolve })
  await page.route('**/mfa/login', async route => {
    requests.push(route.request().postDataJSON())
    await gate
    await route.fulfill({ json: requests.length === 1 ? { code: 0, data: { token: 'synthetic-token', exp: 100 } } : { code: 40001, msg: 'MFA 验证已失效，请重新登录' } })
  })
  await page.goto('/' + query)
  const input = page.getByPlaceholder('请输入六位验证码')
  await input.fill('123456')
  return { requests, release, input }
}

test('一次回车只提交一个请求', async ({ page }) => {
  const { requests, release, input } = await setup(page)
  await input.press('Enter')
  await expect.poll(() => requests.length).toBe(1)
  await page.waitForTimeout(250)
  expect(requests).toHaveLength(1)
  release()
  await expect.poll(() => page.evaluate(() => window.mfaEvents.routes.length)).toBe(1)
})

for (const action of ['double-click', 'enter-and-click', 'held-enter']) {
  test(`${action} 提交期间保持单请求，成功后不再提交`, async ({ page }) => {
    const { requests, release, input } = await setup(page)
    const button = page.getByRole('button', { name: '确认', exact: true })
    if (action === 'double-click') {
      await button.evaluate(el => { el.click(); el.click() })
    } else if (action === 'enter-and-click') {
      await input.press('Enter')
      await button.evaluate(el => el.click())
    } else {
      await input.focus()
      await page.keyboard.down('Enter')
      await page.keyboard.down('Enter')
      await page.keyboard.up('Enter')
    }
    await expect(button).toBeDisabled()
    await expect.poll(() => requests.length).toBe(1)
    await input.press('Enter')
    await page.waitForTimeout(150)
    expect(requests).toHaveLength(1)
    release()
    await expect.poll(() => page.evaluate(() => window.mfaEvents.tokens.length)).toBe(1)
    await expect(button).toBeDisabled()
    // Fixture keeps the component mounted after navigation to exercise the success/unmount gap.
    await input.press('Enter')
    await page.waitForTimeout(150)
    expect(requests).toHaveLength(1)
    expect(await page.evaluate(() => window.mfaEvents.routes.length)).toBe(1)
  })
}

test('错误验证码不跳转，修改验证码可重试', async ({ page }) => {
  const requests = []
  await page.route('**/mfa/login', async route => {
    const data = route.request().postDataJSON()
    requests.push(data)
    await route.fulfill({ json: data.code === '123456'
      ? { code: 40001, msg: '验证码错误' }
      : { code: 0, data: { token: 'synthetic-token', exp: 100 } } })
  })
  await page.goto('/')
  const input = page.getByPlaceholder('请输入六位验证码')
  await input.fill('123456')
  await input.press('Enter')
  await expect(page.getByText('验证码错误', { exact: true })).toBeVisible()
  expect(await page.evaluate(() => window.mfaEvents.tokens.length)).toBe(0)
  expect(await page.evaluate(() => window.mfaEvents.routes.length)).toBe(0)
  await input.fill('654321')
  await input.press('Enter')
  await expect.poll(() => page.evaluate(() => window.mfaEvents.routes.length)).toBe(1)
  expect(requests.map(item => item.code)).toEqual(['123456', '654321'])
})

test('真正过期的挑战不写入 Token、不跳转', async ({ page }) => {
  let calls = 0
  await page.route('**/mfa/login', async route => {
    calls++
    await route.fulfill({ json: { code: 40001, msg: 'MFA 验证已失效，请重新登录' } })
  })
  await page.goto('/')
  const input = page.getByPlaceholder('请输入六位验证码')
  await input.fill('123456')
  await input.press('Enter')
  await expect(page.getByText('MFA 验证已失效，请重新登录', { exact: true })).toBeVisible()
  expect(calls).toBe(1)
  expect(await page.evaluate(() => window.mfaEvents.tokens.length)).toBe(0)
  expect(await page.evaluate(() => window.mfaEvents.routes.length)).toBe(0)
})

test('前端格式校验失败后释放提交锁', async ({ page }) => {
  const { requests, release, input } = await setup(page)
  await input.fill('123')
  await input.press('Enter')
  await expect(page.getByRole('button', { name: '确认', exact: true })).toBeEnabled()
  expect(requests).toHaveLength(0)
  await input.fill('123456')
  await input.press('Enter')
  await expect.poll(() => requests.length).toBe(1)
  release()
  await expect.poll(() => page.evaluate(() => window.mfaEvents.routes.length)).toBe(1)
})

test('移动端只触发一次成功事件', async ({ page }) => {
  const { requests, release, input } = await setup(page, '?mobile')
  await input.press('Enter')
  await expect.poll(() => requests.length).toBe(1)
  release()
  await expect.poll(() => page.evaluate(() => window.mfaEvents.success)).toBe(1)
  expect(await page.evaluate(() => window.mfaEvents.routes.length)).toBe(0)
})

test('个人信息绑定入口同样防重复，完成后仅通知一次', async ({ page }) => {
  let calls = 0
  let release
  const gate = new Promise(resolve => { release = resolve })
  await page.route('**/user/mfaBind', async route => {
    calls++
    await gate
    await route.fulfill({ json: { code: 0, data: null } })
  })
  await page.goto('/?bind')
  await page.getByRole('button', { name: '下一步', exact: true }).click()
  const input = page.getByPlaceholder('请输入六位验证码')
  await input.fill('123456')
  await input.press('Enter')
  await input.press('Enter')
  await expect.poll(() => calls).toBe(1)
  release()
  await expect.poll(() => page.evaluate(() => window.mfaEvents.bindings)).toBe(1)
  await input.press('Enter')
  await page.waitForTimeout(150)
  expect(calls).toBe(1)
  expect(await page.evaluate(() => window.mfaEvents.tokens.length)).toBe(0)
})

test('卸载再挂载不残留全局回车监听', async ({ page }) => {
  const { requests, release } = await setup(page)
  await page.evaluate(() => window.unmountMfa())
  await expect(page.getByPlaceholder('请输入六位验证码')).toHaveCount(0)
  await page.evaluate(() => window.mountMfa())
  await page.getByPlaceholder('请输入六位验证码').fill('123456')
  await page.locator('body').click({ position: { x: 10, y: 200 } })
  await page.keyboard.press('Enter')
  await expect.poll(() => requests.length).toBe(1)
  release()
  await expect.poll(() => page.evaluate(() => window.mfaEvents.routes.length)).toBe(1)
})
