import { test, expect } from '@playwright/test'

async function openScenario(page, scenario, languages = ['zh-CN']) {
  await page.addInitScript(
    ({ scenario, languages }) => {
      Object.defineProperty(navigator, 'languages', { get: () => languages })
      Object.defineProperty(navigator, 'language', {
        get: () => languages[0] || ''
      })
      if (!sessionStorage.getItem('scenario')) {
        sessionStorage.setItem('scenario', JSON.stringify(scenario))
      }
    },
    { scenario, languages }
  )
  await page.goto('/')
  await expect
    .poll(() =>
      page
        .evaluate(() => window.languageResult?.language)
        .catch(() => undefined)
    )
    .toBeTruthy()
  // Allow an incorrectly scheduled reload to surface, rather than passing on the first page.
  await page.waitForTimeout(500)
  return page.evaluate(() => window.languageResult)
}

const scenarios = [
  {
    name: 'null user follows English browser',
    user: null,
    languages: ['en-US'],
    expected: 'en',
    visits: 1
  },
  {
    name: 'empty user follows cached language',
    user: '',
    cache: 'tw',
    languages: ['en-US'],
    expected: 'tw',
    visits: 1
  },
  {
    name: 'explicit user overrides cache and browser',
    user: 'en',
    cache: 'tw',
    expected: 'en',
    visits: 2
  },
  {
    name: 'legacy Chinese is normalized',
    user: 'zh_CN',
    expected: 'zh-CN',
    visits: 1
  },
  {
    name: 'simplified script',
    user: null,
    languages: ['zh-Hans'],
    expected: 'zh-CN',
    visits: 1
  },
  {
    name: 'traditional script',
    user: null,
    languages: ['zh-Hant'],
    expected: 'tw',
    visits: 1
  },
  {
    name: 'script overrides region',
    user: null,
    languages: ['zh-Hans-HK'],
    expected: 'zh-CN',
    visits: 1
  },
  {
    name: 'second browser preference',
    user: null,
    languages: ['fr-FR', 'en-GB'],
    expected: 'en',
    visits: 1
  },
  {
    name: 'unsupported inputs fall back to Chinese',
    user: 'removed',
    cache: 'invalid',
    languages: ['fr-FR'],
    expected: 'zh-CN',
    visits: 1
  },
  {
    name: 'removed user language uses valid cache',
    user: 'removed',
    cache: 'en',
    expected: 'en',
    visits: 1
  },
  { name: 'case and separators', user: 'EN_us', expected: 'en', visits: 2 },
  {
    name: 'embedded host does not reload',
    user: 'en',
    embedded: true,
    expected: 'en',
    visits: 1
  },
  {
    name: 'options failure preserves builtins',
    user: 'en',
    failOptions: true,
    expected: 'en',
    visits: 2
  },
  {
    name: 'available custom user language',
    user: 'fr',
    custom: { fr: 'French' },
    expected: 'fr',
    visits: 2
  },
  {
    name: 'custom browser language',
    user: null,
    languages: ['fr-CA'],
    custom: { fr: 'French' },
    expected: 'fr',
    visits: 1
  },
  {
    name: 'custom identifier wins over builtin alias',
    user: 'en-US',
    custom: { 'en-US': 'US' },
    expected: 'en-US',
    visits: 2
  },
  {
    name: 'missing custom user pack switches once from English',
    user: 'fr',
    cache: 'en',
    custom: { fr: 'French' },
    failPack: true,
    expected: 'zh-CN',
    visits: 2
  },
  {
    name: 'no browser preferences defaults to Chinese',
    user: null,
    languages: [],
    expected: 'zh-CN',
    visits: 1
  },
  {
    name: 'missing custom pack falls back without looping',
    user: 'fr',
    cache: 'fr',
    custom: { fr: 'French' },
    failPack: true,
    expected: 'zh-CN',
    visits: 1
  }
]

for (const scenario of scenarios) {
  test(scenario.name, async ({ page }) => {
    await page.route('**/api/i18n/**', (route) =>
      scenario.failPack
        ? route.fulfill({
            status: 404,
            contentType: 'text/javascript',
            body: ''
          })
        : route.fulfill({
            contentType: 'text/javascript',
            body: 'export default { greeting: "Bonjour" }'
          })
    )
    const result = await openScenario(page, scenario, scenario.languages)
    for (const key of [
      'language',
      'cache',
      'locale',
      'i18n',
      'requestLocale',
      'html'
    ]) {
      expect(result[key], key).toBe(scenario.expected)
    }
    expect(result.visits).toBe(scenario.visits)
    await page.reload()
    await expect
      .poll(() =>
        page
          .evaluate(() => window.languageResult?.language)
          .catch(() => undefined)
      )
      .toBe(scenario.expected)
    await page.waitForTimeout(300)
    expect(await page.evaluate(() => window.languageResult.visits)).toBe(
      scenario.visits + 1
    )
  })
}

for (const input of [
  null,
  undefined,
  '',
  '   ',
  '\t\n',
  'removed',
  'en-invalid'
]) {
  for (const mode of ['api', 'store']) {
    test(`rejects ${JSON.stringify(input)} in ${mode} without writes`, async ({
      page
    }) => {
      await openScenario(page, { user: 'en' }, ['en'])
      const before = await page.evaluate(() => window.currentLanguage())
      const result = await page.evaluate(
        ({ input, mode }) => window.attemptSwitch(input, mode),
        { input, mode }
      )
      expect(result.ok).toBe(false)
      expect(result.message).toBeTruthy()
      expect(await page.evaluate(() => window.currentLanguage())).toEqual(
        before
      )
      expect(
        await page.evaluate(() => sessionStorage.getItem('posts'))
      ).toBeNull()
    })
  }
}

for (const [input, expected, custom] of [
  [' zh_CN ', 'zh-CN', {}],
  ['EN_us', 'en', {}],
  ['zh_TW', 'tw', {}],
  [' fr ', 'fr', { fr: 'French' }],
  ['X_company', 'x-company', { 'x-company': 'Company' }]
]) {
  test(`switch API sends canonical ${expected}`, async ({ page }) => {
    await page.route('**/api/i18n/**', (route) =>
      route.fulfill({
        contentType: 'text/javascript',
        body: 'export default {}'
      })
    )
    await openScenario(page, { user: 'en', custom }, ['en'])
    expect(
      await page.evaluate((input) => window.attemptSwitch(input), input)
    ).toEqual({ ok: true })
    expect(
      await page.evaluate(() => JSON.parse(sessionStorage.getItem('posts')))
    ).toEqual([{ lang: expected }])
  })
}

for (const failure of [
  'switchFailure',
  'switchNetworkFailure',
  'switchEmptyResponse'
]) {
  test(`language selector preserves state on ${failure}`, async ({ page }) => {
    await openScenario(page, { user: 'en', [failure]: true }, ['en'])
    const before = await page.evaluate(() => window.currentLanguage())
    await page.locator('.language-item').filter({ hasText: '简体中文' }).click()
    await expect(page.getByRole('alert')).toBeVisible()
    expect(await page.evaluate(() => window.currentLanguage())).toEqual(before)
    expect(await page.evaluate(() => sessionStorage.getItem('visits'))).toBe(
      '1'
    )
    expect(
      await page.evaluate(() => sessionStorage.getItem('permissionResets'))
    ).toBeNull()
  })
}

test('language selector rejects a blank option before sending', async ({
  page
}) => {
  await openScenario(page, { user: 'en', custom: { ' ': 'Blank language' } }, [
    'en'
  ])
  await page
    .locator('.language-item')
    .filter({ hasText: 'Blank language' })
    .click()
  await expect(page.getByRole('alert')).toContainText('Language is required')
  expect(await page.evaluate(() => sessionStorage.getItem('posts'))).toBeNull()
  expect(await page.evaluate(() => window.currentLanguage())).toEqual({
    language: 'en',
    cache: 'en',
    locale: 'en'
  })
})

test('language selector updates state only after API success', async ({
  page
}) => {
  await openScenario(page, { user: 'en' }, ['en'])
  await page.locator('.language-item').filter({ hasText: '简体中文' }).click()
  await expect
    .poll(() =>
      page
        .evaluate(() => window.languageResult?.language)
        .catch(() => undefined)
    )
    .toBe('zh-CN')
  expect(await page.evaluate(() => sessionStorage.getItem('visits'))).toBe('2')
  expect(
    await page.evaluate(() => JSON.parse(sessionStorage.getItem('posts')))
  ).toEqual([{ lang: 'zh-CN' }])
})
