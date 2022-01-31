import { TableSheet } from '@antv/s2'
import { getCustomTheme } from '@/views/chart/chart/common/common_table'

export function baseTableInfo(s2, container, chart, action, tableData) {
  const containerDom = document.getElementById(container)

  // data
  const fields = chart.data.fields
  if (!fields || fields.length === 0) {
    if (s2) {
      s2.destroy()
    }
    return
  }

  const columns = []
  const meta = []
  fields.forEach(ele => {
    columns.push(ele.dataeaseName)

    meta.push({
      field: ele.dataeaseName,
      name: ele.name
    })
  })

  // data config
  const s2DataConfig = {
    fields: {
      columns: columns
    },
    meta: meta,
    data: tableData
  }

  // options
  const s2Options = {
    width: containerDom.offsetWidth,
    height: containerDom.offsetHeight,
    // showSeriesNumber: true
    style: {
      cellCfg: {
        width: 500
      }
    }
  }

  // 开始渲染
  if (s2) {
    s2.destroy()
  }
  s2 = new TableSheet(containerDom, s2DataConfig, s2Options)

  // theme
  const customTheme = getCustomTheme(chart)
  s2.setThemeCfg({ theme: customTheme })

  return s2
}
