const { test, expect } = require(process.env.PLAYWRIGHT_TEST_MODULE || '@playwright/test')
const open = async page => {
  await page.goto('/#/user-center/index?tab=1')
  await page.getByRole('button', { name: '打开个人信息' }).click()
  await page.getByRole('button', { name: '取消绑定', exact: true }).click()
  const dialog = page.getByRole('dialog').filter({ hasText: '确认取消绑定 MFA？' })
  await expect(dialog).toBeVisible()
  return { dialog, input: dialog.getByPlaceholder('请输入六位验证码') }
}
for (const code of ['', '123', '123456']) {
  test(`Enter with '${code}' preserves URL and dialog without unbinding`, async ({ page }) => {
    const requests = [],
      navigations = [],
      errors = []
    page.on('pageerror', error => errors.push(error.message))
    page.on('framenavigated', frame => {
      if (frame === page.mainFrame()) navigations.push(frame.url())
    })
    await page.route('**/user/mfaUnbind/*', async route => {
      requests.push(route.request().url())
      await route.fulfill({ json: { data: '0' } })
    })
    const { dialog, input } = await open(page)
    const url = page.url(),
      count = navigations.length
    await input.fill(code)
    await input.press('Enter')
    await page.waitForTimeout(300)
    expect(page.url()).toBe(url)
    expect(navigations).toHaveLength(count)
    await expect(dialog).toBeVisible()
    expect(requests).toHaveLength(0)
    expect(errors).toEqual([])
  })
}
test('click confirm still validates and completes unbinding', async ({ page }) => {
  const requests = []
  await page.route('**/user/mfaUnbind/*', async route => {
    requests.push(route.request().url())
    await route.fulfill({ json: { data: '0' } })
  })
  const { dialog, input } = await open(page)
  const confirm = dialog.getByRole('button', { name: '取消绑定', exact: true })
  await input.fill('123')
  await confirm.click()
  await expect(dialog.getByText('请输入六位验证码', { exact: true })).toBeVisible()
  expect(requests).toHaveLength(0)
  await input.fill('123456')
  await confirm.click()
  await expect(dialog).toBeHidden()
  expect(requests).toHaveLength(1)
  expect(requests[0]).toMatch(/\/user\/mfaUnbind\/123456$/)
  expect(new URL(page.url()).search).toBe('')
})
test('cancel and reopen remain usable after Enter', async ({ page }) => {
  const { dialog, input } = await open(page)
  await input.fill('123456')
  await input.press('Enter')
  await dialog.getByRole('button', { name: '取消', exact: true }).click()
  await expect(dialog).toBeHidden()
  await page.getByRole('button', { name: '取消绑定', exact: true }).click()
  await expect(page.getByPlaceholder('请输入六位验证码')).toHaveValue('')
})
