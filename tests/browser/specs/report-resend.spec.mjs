import { test, expect } from '@playwright/test'

test('latest manual resend first, stable ties and unchanged first results', async ({ page }) => {
  const attempt = (id, attemptNo, createTime, status = 3, triggerType = 3) => ({
    id,
    artifactId: id,
    attemptNo,
    createTime,
    status,
    triggerType
  })
  const recipients = [
    {
      id: 'a',
      recipientName: 'Alpha',
      attempts: [
        attempt('9007199254740990', 1, 50, 3, 1),
        attempt('9007199254740991', 2, 100),
        attempt('9007199254740993', 3, 300, 1)
      ]
    },
    {
      id: 'b',
      recipientName: 'Beta',
      attempts: [attempt('9007199254740992', 1, 200), attempt('9007199254740994', 3, 300)]
    },
    { id: 'c', recipientName: 'Gamma', attempts: [attempt('9007199254740995', 2, 300)] },
    { id: 'd', recipientName: 'Missing', attempts: [attempt('9007199254740996', 1, null)] },
    { id: 'e', recipientName: 'Invalid', attempts: [attempt('9007199254740997', 1, 'invalid')] }
  ].map(row => ({
    ...row,
    recipientAddress: `${row.id}@example.invalid`,
    channel: 1,
    initialStatus: 3,
    currentStatus: 3,
    executionAction: 0,
    retryBlock: 'UNAVAILABLE'
  }))
  let reversed = false
  await page.route('**/fixture-delivery', route =>
    route.fulfill({
      json: {
        instanceId: 'fixture-resend',
        taskName: 'Resend sorting',
        execStatus: 3,
        triggerType: 1,
        startTime: 1,
        endTime: 2,
        legacy: false,
        current: true,
        log: '',
        recipients: reversed ? [...recipients].reverse() : recipients
      }
    })
  )
  await page.goto('/__resend')
  const rows = page.locator('.resend-table .ed-table__body tbody tr')
  const expected = [
    '9007199254740994',
    '9007199254740993',
    '9007199254740995',
    '9007199254740992',
    '9007199254740991',
    '9007199254740997',
    '9007199254740996'
  ]
  await expect(rows).toHaveCount(7)
  await expect(rows.locator('.artifact-reference')).toHaveText(expected)
  await expect(rows.nth(1)).toContainText('第 2 次')
  await expect(rows.nth(1)).toContainText('发送中')
  await expect(page.locator('.recipient-table').first()).toContainText('发送失败')
  reversed = true
  await page.getByRole('button', { name: '刷新', exact: true }).click()
  await expect(rows.locator('.artifact-reference')).toHaveText(expected)
})
