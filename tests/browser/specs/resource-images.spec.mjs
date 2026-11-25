import { test, expect } from '@playwright/test'
const png = Buffer.from(
  'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+a5WQAAAAASUVORK5CYII=',
  'base64'
)
async function loaded(page) {
  await expect(page.locator('#canvas img')).toHaveAttribute(
    'src',
    /^data:image\/png/
  )
  await expect
    .poll(() => page.locator('#canvas img').evaluate((img) => img.naturalWidth))
    .toBe(1)
  expect(
    await page.locator('#canvas').evaluate((el) => el.style.backgroundImage)
  ).toContain('data:image/png')
}
test('CSS and img render once; changing identity invalidates shared cache', async ({
  page
}) => {
  const requests = []
  await page.route('**/de2api/static-resource/*', async (route) => {
    requests.push(route.request().headers())
    await route.fulfill({ contentType: 'image/png', body: png })
  })
  await page.goto('/')
  await loaded(page)
  expect(requests.length).toBe(1)
  await page.evaluate(() => {
    window.fixture.embedded.token = 'embed-a'
  })
  await expect.poll(() => requests.length).toBe(2)
  await loaded(page)
  await page.evaluate(() => {
    window.fixture.link.getLinkToken = 'share-a'
  })
  await expect.poll(() => requests.length).toBe(3)
  await loaded(page)
  await page.evaluate(() => {
    window.fixture.user.getOid = '2'
  })
  await expect.poll(() => requests.length).toBe(4)
  await loaded(page)
  expect(
    await page.evaluate(() =>
      window.fixture.authenticatedImageUrl('https://external.test/picture.png')
    )
  ).toBe('https://external.test/picture.png')
  for (const path of [
    '/static-resource/../secret',
    '/static-resource/a%2fb.png',
    '/static-resource/a.png?token=x'
  ])
    expect(
      await page.evaluate(
        (path) => window.fixture.authenticatedImageUrl(path),
        path
      )
    ).toBe('')
})
for (const status of [401, 403, 404])
  test(`image ${status} stays failed and export rejects`, async ({ page }) => {
    let calls = 0
    await page.route('**/de2api/static-resource/*', async (route) => {
      calls++
      await route.fulfill({
        status,
        contentType: 'application/json',
        body: '{}'
      })
    })
    await page.goto('/')
    await expect.poll(() => calls).toBe(1)
    await expect
      .poll(() =>
        page.evaluate(async () => {
          try {
            await window.fixture.wait()
            return false
          } catch {
            return true
          }
        })
      )
      .toBe(true)
    expect(calls).toBe(1)
    expect(new URL(page.url()).pathname).toBe('/')
  })
test('export waits; late old response cannot overwrite new identity', async ({
  page
}) => {
  let release
  const gate = new Promise((resolve) => {
    release = resolve
  })
  let calls = 0
  await page.route('**/de2api/static-resource/*', async (route) => {
    calls++
    if (calls === 1) await gate
    await route.fulfill({ contentType: 'image/png', body: png }).catch(() => {})
  })
  await page.goto('/')
  await expect.poll(() => calls).toBe(1)
  await page.evaluate(() => {
    window.exportDone = false
    window.fixture
      .wait()
      .then(() => (window.exportDone = true))
      .catch(() => {})
  })
  expect(await page.evaluate(() => window.exportDone)).toBe(false)
  await page.evaluate(() => {
    window.fixture.embedded.token = 'new-identity'
  })
  await expect.poll(() => calls).toBe(2)
  await loaded(page)
  release()
  await page.evaluate(() => window.fixture.wait())
  await loaded(page)
})

// Use real useCache storage prefixes and real cross-page storage events.
test('unrelated and unchanged storage writes preserve loaded images', async ({
  page,
  context
}) => {
  let requests = 0
  await context.route('**/de2api/static-resource/*', async (route) => {
    const target = route.request().frame().page() === page
    if (target) requests++
    await route.fulfill({ contentType: 'image/png', body: png })
  })
  await page.goto('/')
  await loaded(page)
  const writer = await context.newPage()
  await writer.goto('/')
  await loaded(writer)
  await page.evaluate(() => {
    window.storageEvents = 0
    window.addEventListener('storage', () => window.storageEvents++)
  })
  await writer.evaluate(() =>
    localStorage.setItem('unrelated-preference', 'dark')
  )
  await expect.poll(() => page.evaluate(() => window.storageEvents)).toBe(1)
  await loaded(page)
  expect(requests).toBe(1)

  await writer.evaluate(() => window.fixture.cache.set('user.token', 'login-a'))
  await expect.poll(() => requests).toBe(2)
  await loaded(page)
  // Change serialized storage without changing the cached token value.
  await writer.evaluate(() => {
    const key = Object.keys(localStorage).find((key) =>
      key.endsWith('user.token')
    )
    localStorage.setItem(key, localStorage.getItem(key) + ' ')
  })
  await expect.poll(() => page.evaluate(() => window.storageEvents)).toBe(3)
  await loaded(page)
  expect(requests).toBe(2)
})

test('cross-page identity change and cache clear invalidate pending images', async ({
  page,
  context
}) => {
  let requests = 0
  let release
  const gate = new Promise((resolve) => {
    release = resolve
  })
  await context.route('**/de2api/static-resource/*', async (route) => {
    const target = route.request().frame().page() === page
    if (target) requests++
    if (target && requests === 1) await gate
    await route.fulfill({ contentType: 'image/png', body: png }).catch(() => {})
  })
  await page.goto('/')
  await expect.poll(() => requests).toBe(1)
  const writer = await context.newPage()
  await writer.goto('/')
  await loaded(writer)
  await writer.evaluate(() => window.fixture.cache.set('user.token', 'login-b'))
  await expect.poll(() => requests).toBe(2)
  await loaded(page)
  release()
  await page.evaluate(() => window.fixture.wait())
  await loaded(page)
  await writer.evaluate(() => window.fixture.cache.set('user.oid', '2'))
  await expect.poll(() => requests).toBe(3)
  await loaded(page)
  await writer.evaluate(() => localStorage.clear())
  await expect.poll(() => requests).toBe(4)
  await loaded(page)
})
