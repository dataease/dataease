/* eslint-disable @typescript-eslint/no-var-requires -- Node CommonJS 测试入口使用 require。 */
const { test, expect } = require(process.env.PLAYWRIGHT_TEST_MODULE || '@playwright/test')
const http = require('node:http')
const { fixtureScript } = require('./harness.cjs')
let main, foreign, origin, foreignOrigin
const child = `<script>window.received=[];addEventListener('message',event=>received.push({data:event.data,origin:event.origin}))</script>`
const listen = server => new Promise(resolve => server.listen(0, '127.0.0.1', resolve))
test.beforeAll(async () => {
  foreign = http.createServer((_, response) => {
    response.setHeader('Content-Type', 'text/html')
    response.end(child)
  })
  await listen(foreign)
  foreignOrigin = 'http://127.0.0.1:' + foreign.address().port
  main = http.createServer((request, response) => {
    response.setHeader('Content-Type', 'text/html')
    if (request.url.startsWith('/mobile.html') || request.url.startsWith('/child')) {
      response.end(child)
      return
    }
    const base = request.url.includes('cross') ? foreignOrigin + '/' : ''
    response.end(
      `<iframe name="attacker" src="${foreignOrigin}/child"></iframe><iframe name="other" src="/child"></iframe><iframe id="mobile" name="target" src="${
        base || './'
      }mobile.html#/panel"></iframe><script>${fixtureScript(
        base
      )}fixture.newWindow.value=document.getElementById('mobile');fixture.mounted.forEach(fn=>fn());</script>`
    )
  })
  await listen(main)
  origin = 'http://127.0.0.1:' + main.address().port
})
test.afterAll(async () => {
  await Promise.all(
    [main, foreign].filter(Boolean).map(server => new Promise(resolve => server.close(resolve)))
  )
})
const open = async (page, cross = false) => {
  await page.goto(origin + (cross ? '/?cross' : '/'))
  for (const name of ['target', 'other', 'attacker'])
    await page.frame({ name }).waitForFunction(() => Array.isArray(window.received))
}
const send = (frame, data) =>
  frame.evaluate(({ data, origin }) => window.parent.postMessage(data, origin), { data, origin })
const intact = async page => {
  // 给真实跨窗口消息事件留出投递时间，再断言拒绝路径。
  await page.waitForTimeout(150)
  expect(await page.evaluate(() => fixture.state.componentData.value[0].inMobile)).toBe(true)
  expect(await page.evaluate(() => fixture.events.length)).toBe(0)
}

test('同源设计 iframe 的初始化、保存和切回 PC 正常', async ({ page }) => {
  await open(page)
  const target = page.frame({ name: 'target' })
  await send(target, { type: 'panelInit' })
  await expect.poll(() => target.evaluate(() => received.length)).toBe(1)
  expect(await target.evaluate(() => received[0].origin)).toBe(origin)
  const value = { chart: { x: 3, y: 2, sizeX: 4, sizeY: 5, style: {} } }
  await send(target, { type: 'mobileSaveFromMobile', value })
  await send(target, { type: 'mobilePatchFromMobile', value })
  await expect.poll(() => page.evaluate(() => fixture.events.join(','))).toBe('save,pcMode')
  expect(await page.evaluate(() => fixture.state.componentData.value[0].mx)).toBe(3)
})
test('同源的其他 iframe 和外域 iframe 都不能修改画布', async ({ page }) => {
  await open(page)
  for (const name of ['other', 'attacker']) {
    await send(page.frame({ name }), { type: 'delFromMobile', value: 'chart' })
    await send(page.frame({ name }), { type: 'mobileSaveFromMobile', value: {} })
  }
  await intact(page)
})
test('跨域嵌入允许指定 iframe 握手，但拒绝同域其他窗口索取凭据', async ({ page }) => {
  await open(page, true)
  const target = page.frame({ name: 'target' })
  const attacker = page.frame({ name: 'attacker' })
  await send(attacker, { msgOrigin: 'de-fit2cloud' })
  await intact(page)
  expect(await target.evaluate(() => received.length)).toBe(0)
  expect(await attacker.evaluate(() => received.length)).toBe(0)
  await send(target, { msgOrigin: 'de-fit2cloud' })
  await expect.poll(() => target.evaluate(() => received.length)).toBe(1)
  expect(await target.evaluate(() => received[0].data['de-embedded'])).toBe(true)
  await send(target, { type: 'panelInit' })
  await expect.poll(() => target.evaluate(() => received.length)).toBe(2)
})
test('指定 iframe 跳转到外域后，原 WindowProxy 也不能发送配置或收到定向消息', async ({ page }) => {
  await open(page)
  const target = page.frame({ name: 'target' })
  await target.goto(foreignOrigin + '/child')
  await send(target, { type: 'delFromMobile', value: 'chart' })
  await send(target, { msgOrigin: 'de-fit2cloud' })
  await page.evaluate(() => fixture.mobileStatusChange('panelInit', { test: true }))
  await intact(page)
  expect(await target.evaluate(() => received.length)).toBe(0)
})
test('出站配置只发送给面板 ref，卸载后不再处理消息', async ({ page }) => {
  await open(page)
  await page.evaluate(() => fixture.mobileStatusChange('mobileSave', undefined))
  await expect.poll(() => page.frame({ name: 'target' }).evaluate(() => received.length)).toBe(1)
  expect(await page.frame({ name: 'attacker' }).evaluate(() => received.length)).toBe(0)
  expect(await page.frame({ name: 'other' }).evaluate(() => received.length)).toBe(0)
  await page.evaluate(() => fixture.unmounted.forEach(fn => fn()))
  await send(page.frame({ name: 'target' }), { type: 'delFromMobile', value: 'chart' })
  await intact(page)
})
