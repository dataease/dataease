import { expect, test } from '@playwright/test'

const tab = (page, id) => page.locator(`[data-u-comp="slide-tab-item"][data-id="${id}"]`)
const ready = async page => {
  await page.waitForFunction(() => window.fixture?.ready)
  await expect(tab(page, 'first')).toBeVisible()
}

test('preview ignores non-tab context menus and blocks sheet-tab menus', async ({ page }) => {
  await page.goto('/')
  await ready(page)
  const outside = await page.evaluate(() => {
    const canvas = document.querySelector('#sheet canvas')
    let reached = 0
    canvas.addEventListener('contextmenu', () => reached++, { once: true })
    canvas.dispatchEvent(new MouseEvent('contextmenu', { bubbles: true, cancelable: true }))
    return { reached, calls: window.fixture.stats().reduce((sum, item) => sum + item.calls, 0) }
  })
  expect(outside).toEqual({ reached: 1, calls: 0 })
  await page.keyboard.press('Escape')
  await tab(page, 'second').getByText('Sheet 2', { exact: true }).click({ button: 'right' })
  // Univer keeps closed menus mounted outside the viewport.
  await expect(page.getByRole('button', { name: 'Rename', exact: true })).not.toBeInViewport()
  expect(await page.evaluate(() => window.fixture.stats())).toEqual([
    { target: 'slide-tab-bar', capture: false, active: true, calls: 1 }
  ])
  await tab(page, 'second').click()
  await expect.poll(() => page.evaluate(() => window.fixture.activeSheet)).toBe('second')
  await tab(page, 'first').click()
  await expect.poll(() => page.evaluate(() => window.fixture.activeSheet)).toBe('first')
})

test('edit mode retains the native sheet-tab context menu', async ({ page }) => {
  await page.goto('/?mode=edit')
  await ready(page)
  await tab(page, 'first').click({ button: 'right' })
  await expect(page.getByRole('button', { name: 'Rename', exact: true })).toBeInViewport()
  expect(await page.evaluate(() => window.fixture.stats())).toEqual([])
})

test('recreating previews and changing mode cleans up the previous listeners', async ({ page }) => {
  await page.goto('/')
  await ready(page)
  for (let index = 0; index < 2; index++) {
    await page.evaluate(() => window.fixture.mount('preview'))
    await ready(page)
    await tab(page, 'first').click({ button: 'right' })
    await expect(page.getByRole('button', { name: 'Rename', exact: true })).not.toBeInViewport()
    expect(
      await page.evaluate(() => window.fixture.stats().filter(item => item.active).length)
    ).toBe(1)
  }
  expect(await page.evaluate(() => window.fixture.dispose())).toBe(false)
  expect(await page.evaluate(() => window.fixture.stats().some(item => item.active))).toBe(false)
  await page.evaluate(() => window.fixture.mount('edit'))
  await ready(page)
  await tab(page, 'first').click({ button: 'right' })
  await expect(page.getByRole('button', { name: 'Rename', exact: true })).toBeInViewport()
})
