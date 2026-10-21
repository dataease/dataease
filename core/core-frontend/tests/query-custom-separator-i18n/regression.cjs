// Run: node --test tests/query-custom-separator-i18n/regression.cjs
const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')
const vm = require('node:vm')
const { test } = require('node:test')
const ts = require('typescript')
const { createI18n } = require('vue-i18n')
const { baseCompile } = require('@intlify/message-compiler')

const load = file => {
  const exports = {}
  const code = ts.transpileModule(fs.readFileSync(path.resolve(__dirname, '../..', file), 'utf8'), {
    compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2020 }
  }).outputText
  vm.runInNewContext(code, { exports })
  return exports
}
const { splitConditionValues } = load('src/custom-component/v-query/condition-utils.ts')
const expected = {
  'zh-CN': '请输入分隔符，例如：| 或 @@',
  tw: '請輸入分隔符，例如：| 或 @@',
  en: 'Enter a separator, e.g. | or @@'
}

for (const [locale, placeholder] of Object.entries(expected)) {
  test(`${locale}: custom separator messages compile and display literal @@`, () => {
    const messages = load(`src/locales/${locale}.ts`).default
    const keys = [
      'multi_condition',
      'separator_custom',
      'custom_separator_placeholder',
      'custom_separator_required',
      'separator_config_hint',
      'default_separator_hint',
      'multi_condition_paste_hint'
    ]
    const errors = []
    keys.forEach(key =>
      baseCompile(messages.v_query[key], { onError: error => errors.push(error) })
    )
    assert.deepEqual(errors, [])
    const i18n = createI18n({
      legacy: false,
      locale,
      fallbackLocale: locale,
      messages: { [locale]: messages }
    })
    try {
      assert.equal(i18n.global.t('v_query.custom_separator_placeholder'), placeholder)
      for (const separator of ['@@', '|', '@:example', '{value}']) {
        const hint = i18n.global.t('v_query.default_separator_hint', { separator })
        assert.ok(hint.includes(separator), 'Custom separator is interpolated as literal text')
      }
    } finally {
      i18n.dispose()
    }
  })
}

test('custom @@ delimiter still splits values for IN matching', () => {
  assert.deepEqual(Array.from(splitConditionValues(' alpha@@ beta @@@@ gamma ', 'custom', '@@')), [
    'alpha',
    'beta',
    'gamma'
  ])
})
