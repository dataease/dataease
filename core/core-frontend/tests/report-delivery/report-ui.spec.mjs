import { test, expect } from '../../../../tests/browser/node_modules/@playwright/test/index.mjs'
const target = (id, status, channel = 1) => ({
  id: String(id),
  recipientName: '接收人' + id,
  recipientAddress: 'qa' + id + '@example.invalid',
  channel,
  initialStatus: status,
  currentStatus: status,
  executionAction: 0,
  initialStage: 'SUBMIT',
  initialErrorMessage: status === 2 ? null : '渠道实际错误',
  retryBlock: status === 2 ? 'SUCCESS' : '',
  artifactId: '300',
  title: '冻结报告',
  filter: '日期：2026-10-26',
  generatedTime: 1792972800000,
  files: ['report.png / SHA-256: frozen'],
  attempts: []
})
const initial = () => ({
  instanceId: '200',
  taskName: '定时报告回归',
  resourceName: '销售仪表板',
  format: 0,
  execStatus: 3,
  triggerType: 0,
  startTime: 1792972800000,
  endTime: 1792972801000,
  legacy: false,
  current: true,
  snapshotState: '',
  log: '',
  recipients: [target(1, 2), target(2, 3, 3), target(3, 4, 4)]
})
async function open(page, detail) {
  await page.route('**/report/delivery', route => route.fulfill({ json: { data: detail } }))
  await page.goto('/')
  await page.getByRole('button', { name: '打开执行日志', exact: true }).click()
  await expect(page.getByText('定时报告回归', { exact: true })).toBeVisible()
}
test('summary, channel filter reset, immutable first result and resend records', async ({
  page
}) => {
  const detail = initial()
  let sends = []
  await page.route('**/report/resend', async route => {
    const request = route.request().postDataJSON()
    sends.push(request)
    const row = detail.recipients.find(r => r.id === request.recipientIds[0])
    row.currentStatus = 2
    row.retryBlock = 'SUCCESS'
    row.attempts.push({
      id: '900',
      triggerType: 3,
      attemptNo: 2,
      artifactId: '300',
      status: 2,
      stage: 'SUBMIT',
      operatorName: '测试员',
      createTime: 1792972802000
    })
    await route.fulfill({ json: { data: null } })
  })
  await open(page, detail)
  await expect(page.getByRole('button', { name: /补发失败项/ })).toBeEnabled()
  await page.getByRole('button', { name: /补发失败项/ }).click()
  await expect(page.getByText('冻结报告', { exact: true }).last()).toBeVisible()
  await page.getByRole('button', { name: /确认补发/ }).click()
  await expect.poll(() => sends.length).toBe(1)
  expect(sends[0].recipientIds).toEqual(['2'])
  await page.evaluate(() => window.reportNotice())
  await expect(page.locator('.resend-section')).toContainText('发送成功')
  await expect(page.locator('.resend-section')).toContainText('qa2@example.invalid')
  expect(detail.recipients[1].initialStatus).toBe(3)
})
test('legacy log and cleared copies explain why resend is unavailable', async ({ page }) => {
  const detail = initial()
  detail.legacy = true
  detail.snapshotState = 'LEGACY'
  detail.recipients = []
  await open(page, detail)
  await expect(page.getByText(/此历史记录未保存逐接收目标结果/)).toBeVisible()
  await expect(page.getByRole('button', { name: /补发失败项/ })).toBeDisabled()
})
test('unknown result requires deliberate risk acknowledgement', async ({ page }) => {
  const detail = initial()
  detail.recipients = [target(3, 4, 4)]
  await open(page, detail)
  await page.getByRole('button', { name: /补发原报告/ }).click()
  const dialog = page.getByRole('dialog', { name: '补发原报告', exact: true })
  await expect(dialog.getByRole('button', { name: /确认补发/ })).toBeDisabled()
  await dialog.getByText('我已知晓重复投递风险，仍然补发', { exact: true }).click()
  await expect(dialog.getByRole('checkbox')).toBeChecked()
  await expect(dialog.getByRole('button', { name: /确认补发/ })).toBeEnabled()
})
test('cleared copy cannot be resent and drawer reopens', async ({ page }) => {
  const detail = initial()
  detail.current = false
  detail.snapshotState = 'CLEARED'
  detail.recipients.forEach(row => (row.retryBlock = 'EXPIRED'))
  await open(page, detail)
  await expect(page.getByText(/当次报告副本已清理/)).toBeVisible()
  await expect(page.getByRole('button', { name: /补发失败项/ })).toBeDisabled()
  await page
    .getByRole('button', { name: /close|关闭/i })
    .first()
    .click()
  await page.getByRole('button', { name: '打开执行日志', exact: true }).click()
  await expect(page.getByText('定时报告回归', { exact: true })).toBeVisible()
})
test('clearing a channel filter restores all eligible recipients', async ({ page }) => {
  await open(page, initial())
  await page.getByRole('button', { name: /^\u5168\u90e8 / }).click()
  await page.locator('.channel-select .ed-select__wrapper').click()
  await page.getByRole('option', { name: '\u98de\u4e66', exact: true }).click()
  await expect(
    page.locator('.recipient-table').getByText('qa3@example.invalid', { exact: true })
  ).toBeVisible()
  await expect(
    page.locator('.recipient-table').getByText('qa1@example.invalid', { exact: true })
  ).toHaveCount(0)
  await page.locator('.channel-select').hover()
  await page.locator('.channel-select [class*="select__clear"]').click()
  await expect(
    page.locator('.recipient-table').getByText('qa1@example.invalid', { exact: true })
  ).toBeVisible()
  await expect(
    page.locator('.recipient-table').getByText('qa2@example.invalid', { exact: true })
  ).toBeVisible()
})
test('all success uses green summary and excludes resends', async ({ page }) => {
  const detail = initial()
  detail.execStatus = 2
  detail.recipients = [target(1, 2)]
  await open(page, detail)
  await expect(page.locator('.result-notice.is-success')).toBeVisible()
  await expect(page.getByRole('button', { name: /\u8865\u53d1\u5931\u8d25\u9879/ })).toBeDisabled()
  await expect(page.locator('.resend-section')).toHaveCount(0)
})
test('not-sent recipient with retained report can be resent after fixing configuration', async ({
  page
}) => {
  const detail = initial()
  detail.recipients = [target(1, 5)]
  detail.recipients[0].initialStage = 'PREPARE'
  detail.recipients[0].initialErrorCode = 'EMAIL_CONFIGURATION_INVALID'
  await open(page, detail)
  await expect(page.getByRole('button', { name: /\u8865\u53d1\u539f\u62a5\u544a/ })).toBeEnabled()
  await expect(page.locator('.result-notice.is-success')).toHaveCount(0)
})
test('empty eligible target set does not fabricate a failed recipient', async ({ page }) => {
  const detail = initial()
  detail.recipients = []
  detail.snapshotState = 'MISSING'
  detail.log = 'No eligible recipients'
  await open(page, detail)
  await expect(
    page
      .getByText('\u65e0\u6709\u6548\u63a5\u6536\u4eba\uff0c\u672c\u6b21\u672a\u53d1\u9001', {
        exact: true
      })
      .first()
  ).toBeVisible()
  await expect(page.getByRole('button', { name: /\u8865\u53d1\u5931\u8d25\u9879/ })).toBeDisabled()
})
test('details chunk failure falls back to the existing raw log viewer', async ({ page }) => {
  await page.route('**/src/views/menu/system/report/ReportDeliveryDetails.vue*', route =>
    route.abort()
  )
  await page.route('**/report/logMsg', route =>
    route.fulfill({ json: { data: 'Raw legacy fallback log' } })
  )
  await page.goto('/')
  await page
    .getByRole('button', { name: '\u6253\u5f00\u6267\u884c\u65e5\u5fd7', exact: true })
    .click()
  await expect(page.getByText('Raw legacy fallback log', { exact: true })).toBeVisible()
})
test('mobile drawer stays inside the viewport', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 })
  await open(page, initial())
  const box = await page
    .getByRole('dialog', { name: '\u6267\u884c\u65e5\u5fd7', exact: true })
    .boundingBox()
  expect(box.width).toBeLessThanOrEqual(390)
  expect(await page.evaluate(() => document.documentElement.scrollWidth)).toBeLessThanOrEqual(390)
})

test('SMTP connection failure permits batch resend and preserves original failure', async ({
  page
}) => {
  const detail = initial()
  detail.recipients = [target(1, 3), target(2, 3)]
  detail.recipients.forEach(row => {
    row.initialStage = 'CONNECT'
    row.initialErrorMessage = 'Got bad greeting from SMTP host: test.invalid, response: [EOF]'
  })
  const requests = []
  await page.route('**/report/resend', async route => {
    requests.push(route.request().postDataJSON())
    await route.fulfill({ json: { data: null } })
  })
  await open(page, detail)
  await expect(page.getByText('连接发送服务', { exact: true })).toHaveCount(2)
  const batch = page.getByRole('button', { name: /补发失败项/ })
  await expect(batch).toBeEnabled()
  await batch.click()
  const dialog = page.getByRole('dialog', { name: '补发原报告', exact: true })
  await expect(dialog.getByRole('checkbox')).toHaveCount(0)
  await dialog.getByRole('button', { name: /确认补发/ }).click()
  await expect.poll(() => requests.length).toBe(1)
  expect(requests[0].recipientIds).toEqual(['1', '2'])
  expect(requests[0].confirmUnknown).toBe(false)
})

for (const width of [1440, 390]) {
  test(`original report files remain readable at ${width}px`, async ({ page }) => {
    await page.setViewportSize({ width, height: 900 })
    const detail = initial()
    const row = target(3, 4)
    row.title = '长标题'.repeat(30)
    row.filter = '区域：华东、华南；'.repeat(20)
    row.files = ['report.png', `${'长文件名'.repeat(40)}.xlsx`]
    detail.recipients = [row]
    await page.route('**/report/resend', route => route.abort())
    await open(page, detail)
    await page.getByRole('button', { name: '补发原报告', exact: true }).click()
    const dialog = page.getByRole('dialog', { name: '补发原报告', exact: true })
    const source = dialog.locator('.retry-source')
    await expect(source.locator('dd').first()).toHaveCSS('line-height', '19.5px')
    await expect
      .poll(() =>
        source.evaluate(el => {
          const files = [...el.querySelectorAll('dd:last-child > div')]
          return files
            .slice(1)
            .map((file, i) =>
              Math.round(file.getBoundingClientRect().top - files[i].getBoundingClientRect().bottom)
            )
        })
      )
      .toEqual([8, 8])
    expect(await source.evaluate(el => el.scrollWidth - el.clientWidth)).toBeLessThanOrEqual(1)
    await dialog.getByRole('checkbox').scrollIntoViewIfNeeded()
    await expect(dialog.getByRole('checkbox')).toBeInViewport()
    await expect(dialog.getByRole('button', { name: /确认补发/ })).toBeDisabled()
    await dialog.getByRole('button', { name: '取消', exact: true }).click()
  })
}
