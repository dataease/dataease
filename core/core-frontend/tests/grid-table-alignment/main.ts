import { createApp, h, Fragment, ref } from 'vue'
import { ElTableColumn, ElConfigProvider } from 'element-plus-secondary'
import 'element-plus-secondary/dist/index.css'
import GridTable from '../../src/components/grid-table/src/GridTable.vue'
import { formatters } from 'virtual:date-formatters'

const columns = [
  ['name', '姓名', 150],
  ['account', '账号', 80],
  ['org', '组织', 150],
  ['role', '角色', 200],
  ['origin', '用户来源', 90],
  ['sysVariable', '系统变量', 200],
  ['email', '邮箱', 200],
  ['enable', '用户状态', 80],
  ['createTime', '创建时间', 180],
  ['operation', '操作', 124]
]
const rows = Array.from({ length: 10 }, (_, i) => ({
  name: `测试${i}`,
  account: `user${i}`,
  org: '测试组织',
  role: '普通用户',
  origin: 'LOCAL',
  sysVariable: '',
  email: 'test@example.invalid',
  enable: '启用',
  createTime: i === 0 ? new Date(2026, 9, 20, 12, 34, 56).getTime() : null,
  operation: '编辑 重置 更多'
}))
createApp({
  setup() {
    const width = ref(1460)
    return () =>
      h(ElConfigProvider, { namespace: 'ed' }, () => [
        h('h2', '表格列对齐回归'),
        h(
          'button',
          { onClick: () => (width.value = width.value === 1460 ? 1000 : 1460) },
          '切换宽度'
        ),
        h(
          'button',
          {
            onClick: () => {
              const headers = [...document.querySelectorAll('thead th')]
              const cells = [...document.querySelectorAll('tbody tr:first-child td')]
              const report = headers.map((header, i) => {
                const a = header.getBoundingClientRect(),
                  b = cells[i]?.getBoundingClientRect()
                const aligned = !!b && Math.abs(a.x - b.x) < 1 && Math.abs(a.width - b.width) < 1
                return `${aligned ? 'PASS' : 'FAIL'} ${header.textContent}: ${a.x}/${a.width} vs ${
                  b?.x
                }/${b?.width}`
              })
              const date = new Date(2026, 9, 20, 12, 34, 56).getTime()
              formatters.forEach(({ name, format }) =>
                report.push(
                  `${
                    format(date) === '2026-10-20 12:34:56' && format(null) === '-' ? 'PASS' : 'FAIL'
                  } ${name}`
                )
              )
              document.querySelector('#result').textContent = report.join('\n')
            }
          },
          '检查对齐'
        ),
        h('pre', { id: 'result' }),
        h('div', { style: `width:${width.value}px;height:650px` }, [
          h(GridTable, { tableData: rows, pagination: { total: 10 } }, () =>
            h(
              Fragment,
              null,
              columns.map(([prop, label, width]) =>
                h(
                  ElTableColumn,
                  {
                    prop,
                    label,
                    width,
                    fixed: prop === 'operation' ? 'right' : undefined,
                    sortable: prop === 'createTime' ? 'custom' : undefined,
                    key: prop === 'operation' ? '_operation' : prop
                  },
                  prop === 'createTime'
                    ? { default: ({ row }) => h('span', formatters[0].format(row.createTime)) }
                    : undefined
                )
              )
            )
          )
        ])
      ])
  }
}).mount('#app')
