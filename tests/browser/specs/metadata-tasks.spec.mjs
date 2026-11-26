import { test, expect } from '@playwright/test'

const backend = process.env.METADATA_TASK_BACKEND
const root = '/datasource/metadata/11'
test('English datasource page submits independent background tasks alongside an editor language preference', async ({
  page,
  request,
  context
}) => {
  const post = async (path, data) => {
    const response = await request.post(backend + path, {
      data,
      headers: { 'Accept-Language': 'en-US' }
    })
    expect(response.status(), path).toBe(200)
    const body = await response.text()
    return body && JSON.parse(body)
  }
  await post(root + '/tables/add', ['Orders', 'Customers'])
  await post(root + '/field', {
    tableName: 'Orders',
    fieldName: 'Customer_ID',
    businessName: '人工确认的客户编号'
  })
  await post(root + '/table', { tableName: 'Customers', description: '已确认客户登记' })
  await context.route('**/fixture-api/**', async route => {
    const url = new URL(route.request().url())
    const path = url.pathname.slice('/fixture-api'.length)
    if (path === '/datasource/getTables') {
      return route.fulfill({
        json: [
          { tableName: 'Orders', name: '销售订单' },
          { tableName: 'Customers', name: '客户登记' },
          { tableName: 'Notes', name: '日志' }
        ]
      })
    }
    await route.fulfill({ response: await route.fetch({ url: backend + path + url.search }) })
  })
  const editor = await context.newPage()
  await editor.goto('/?view=editor&language=en')
  await editor.getByTestId('dataset-sql-assistant-toggle').click()
  const input = editor.getByTestId('dataset-sql-assistant-input')
  await input
    .locator('textarea')
    .or(editor.locator('textarea[data-testid="dataset-sql-assistant-input"]'))
    .fill('For the rest of this conversation, answer in French. EDITOR_LANGUAGE_MARKER')
  await editor.getByTestId('dataset-sql-assistant-send').click()
  await expect(editor.getByText('Bien compris.', { exact: true })).toBeVisible()
  await page.goto('/?language=en')
  await page.getByRole('button', { name: 'Complete with AI' }).click()
  const drawer = page.getByRole('dialog')
  await drawer.getByRole('checkbox', { name: /Orders/ }).check()
  await drawer.getByRole('checkbox', { name: /Customers/ }).check()
  await drawer.getByRole('button', { name: 'Start task' }).click()
  await expect(
    drawer.getByText(
      'Added details to 2 records for review. 0 tables required no writes and 0 failed.'
    )
  ).toBeVisible()
  const tables = (await (await request.get(backend + root + '/semantics')).json()).tables
  const orders = tables.find(table => table.tableName === 'Orders')
  const customers = tables.find(table => table.tableName === 'Customers')
  expect(orders).toMatchObject({ description: '记录销售订单', confirmed: false })
  expect(customers).toMatchObject({ description: '已确认客户登记', confirmed: true })
  expect((await post(root + '/semantics/detail', { tableId: orders.id })).fields).toMatchObject([
    { fieldName: 'Customer_ID', businessName: '人工确认的客户编号', confirmed: true }
  ])
  expect((await post(root + '/semantics/detail', { tableId: customers.id })).fields).toMatchObject([
    {
      fieldName: 'Id',
      businessName: '客户编号',
      confirmed: false,
      aliases: null,
      description: null
    }
  ])
  for (const [written, skipped] of [
    [1, 0],
    [0, 1]
  ]) {
    await page.goto('/?view=relations&language=en')
    await page.getByRole('button', { name: 'Discover with AI' }).click()
    await drawer.getByRole('button', { name: 'Select all' }).click()
    await drawer.getByRole('button', { name: 'Discover joins' }).click()
    await expect(
      drawer.getByText(`Added ${written} joins for review. Skipped ${skipped}.`)
    ).toBeVisible()
  }
  const relations = await (await request.get(backend + root + '/relations')).json()
  expect(relations).toHaveLength(1)
  expect(relations[0]).toMatchObject({
    name: '订单客户',
    leftTable: 'Orders',
    rightTable: 'Customers',
    conditions: [{ leftField: 'Customer_ID', rightField: 'Id' }],
    cardinality: 'MANY_TO_ONE',
    joinType: 'LEFT',
    confirmed: false,
    valid: true,
    description: '订单标注所属客户，客户表标注唯一编号，类型均为 BIGINT。尚无外键约束证据。'
  })
})
