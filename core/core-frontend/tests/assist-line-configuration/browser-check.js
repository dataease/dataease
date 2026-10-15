async page => {
  const errors = []
  page.on('pageerror', error => errors.push(String(error)))
  await page.goto(new URL('/tests/assist-line-configuration/index.html', page.url()).href)
  await page.getByRole('heading', { name: '辅助线配置回归', exact: true }).waitFor()
  let assertions = 0
  const expect = (value, message) => {
    if (!value) throw new Error(message)
    assertions++
  }
  const draft = async () => JSON.parse(await page.locator('#draft').textContent())
  const mode = async name => page.getByRole('button', { name, exact: true }).click()
  const add = async () => mode('添加辅助线')
  const select = async (label, index) => {
    const select = page
      .locator('.config-field')
      .filter({ has: page.locator('label', { hasText: label }) })
      .locator('.ed-select')
    await select.click()
    const id = await select.getByRole('combobox').getAttribute('aria-controls')
    await page.locator(`[id="${id}"]`).getByRole('option').nth(index).click()
  }
  await mode('单指标分组图')
  await add()
  expect((await page.locator('.binding-row').count()) === 0, '单指标分组图不展示冗余配置')
  expect(
    (await draft())[0].followField === false && !(await draft())[0].boundFieldId,
    '整体参考线默认不跟随'
  )
  await mode('保存并重开')
  expect((await draft())[0].followField === false, '重开后默认规则保留')
  await select('参考类型', 1)
  expect(await page.getByText('聚合方式', { exact: true }).isVisible(), '动态线保留聚合配置')
  expect(
    (await page.locator('.binding-row').count()) === 0,
    '新建单指标动态线同样不展示冗余显隐配置'
  )

  await mode('双轴图')
  await add()
  let line = (await draft())[0]
  expect(line.boundFieldId === 'salary' && line.followField, '新建双轴固定线绑定左轴唯一指标')
  expect(
    await page.getByRole('checkbox', { name: '随该轴指标显隐' }).isChecked(),
    '唯一指标跟随开关可用'
  )
  expect(
    !(await page.getByText('绑定指标（可选）', { exact: true }).count()),
    '唯一指标不展示重复下拉'
  )
  await page.locator('.binding-row .ed-checkbox').click()
  expect((await draft())[0].followField === false, '可关闭跟随')
  await page.locator('.binding-row .ed-checkbox').click()
  await select('所属轴', 1)
  line = (await draft())[0]
  expect(
    line.yAxisType === 'right' && line.boundFieldId === 'commission',
    '切换轴后显式绑定对应指标'
  )
  await mode('保存并重开')
  expect((await draft())[0].boundFieldId === 'commission', '双轴绑定保存后回显')
  await select('参考类型', 1)
  expect((await draft())[0].fieldId === 'commission', '动态线计算字段来自对应轴')
  expect(
    !(await page.getByText('绑定指标（可选）', { exact: true }).count()),
    '动态线无重复绑定控件'
  )

  await mode('历史分组图')
  expect(await page.getByRole('checkbox').isChecked(), '历史分组图保留编辑入口')
  await mode('保存并重开')
  expect(
    (await draft())[0].boundFieldId === 'salary' && (await draft())[0].followField,
    '历史配置不静默重置'
  )

  await mode('历史未绑定分组图')
  expect((await page.locator('.binding-row').count()) === 0, '历史未绑定固定线初始无冗余配置')
  expect(
    (await draft())[0].field === '0' &&
      (await draft())[0].followField === true &&
      (await draft())[0].boundFieldId === '',
    '打开历史未绑定固定线不改写默认跟随值'
  )
  await select('参考类型', 1)
  await page.locator('.binding-row .ed-checkbox').waitFor({ timeout: 5000 })
  expect(
    await page.locator('.binding-row .ed-checkbox').isVisible(),
    '历史未绑定固定线切换动态线后立即出现跟随开关'
  )
  expect(await page.getByRole('checkbox').isChecked(), '切换动态线保留已有跟随状态')
  expect(await page.getByRole('checkbox').isEnabled(), '切换动态线后可立即关闭跟随')
  expect(
    (await draft())[0].fieldId === 'salary' && (await draft())[0].boundFieldId === '',
    '动态线沿用计算字段且不补写固定线绑定'
  )
  await page.locator('.binding-row .ed-checkbox').click()
  expect((await draft())[0].followField === false, '无需重开即可关闭历史动态线跟随')
  await select('参考类型', 0)
  await select('参考类型', 1)
  expect(
    (await draft())[0].followField === false &&
      (await page.locator('.binding-row .ed-checkbox').isVisible()),
    '重复切换类型不重置跟随值且本次编辑入口保留'
  )
  await page.locator('.binding-row .ed-checkbox').click()
  await mode('保存并重开')
  expect(await page.getByRole('checkbox').isChecked(), '保存重开后动态跟随配置和入口保留')
  await page.locator('.binding-row .ed-checkbox').click()
  await mode('保存并重开')
  expect(
    (await draft())[0].field === '1' &&
      (await draft())[0].followField === false &&
      (await draft())[0].boundFieldId === '',
    '保存重开后关闭跟随的动态配置保留'
  )

  await mode('失效绑定双轴图')
  expect(
    await page.locator('.binding-row > label:not(.ed-checkbox)').isVisible(),
    '失效绑定保留选择器供处理'
  )
  await mode('保存并重开')
  expect((await draft())[0].boundFieldId === 'removed', '旧阈值不自动转绑')

  await mode('历史未绑定双轴图')
  expect(!(await page.getByRole('checkbox').isChecked()), '未绑定历史配置不能显示为已跟随')
  await mode('保存并重开')
  expect((await draft())[0].boundFieldId === '', '打开保存不自动绑定历史未绑定配置')
  await page.locator('.binding-row .ed-checkbox').click()
  expect((await draft())[0].boundFieldId === 'salary', '用户开启后再绑定唯一指标')

  await mode('多指标双轴图')
  await add()
  expect(
    await page.locator('.binding-row > label:not(.ed-checkbox)').isVisible(),
    '多指标保留选择器'
  )
  expect(await page.getByRole('checkbox').isDisabled(), '未选指标不能开启跟随')
  const binding = page.locator('.binding-row .ed-select')
  await binding.click()
  const optionsId = await binding.getByRole('combobox').getAttribute('aria-controls')
  await page.locator(`[id="${optionsId}"]`).getByRole('option').nth(1).click()
  expect((await draft())[0].boundFieldId === 'bonus', '多指标选择持久化')
  await select('所属轴', 1)
  expect((await draft())[0].boundFieldId === 'commission', '单指标轴自动关联')
  await select('所属轴', 0)
  expect(
    (await draft())[0].boundFieldId === '' && (await page.getByRole('checkbox').isDisabled()),
    '切回多指标轴不跨轴沿用绑定'
  )
  const visibilityChecks = await page.locator('body').getAttribute('data-visibility-checks')
  expect(Number(visibilityChecks) >= 40, '显隐逻辑回归未完成')
  expect(errors.length === 0, `页面异常: ${errors.join('; ')}`)
  return {
    status: 'PASS',
    uiAssertions: assertions,
    visibilityAssertions: Number(visibilityChecks)
  }
}
