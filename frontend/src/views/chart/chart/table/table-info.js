import { TableSheet, S2Event } from '@antv/s2'
import { getCustomTheme, getSize } from '@/views/chart/chart/common/common_table'

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

  // add drill list
  if (chart.drill) {
    let drillFields = []
    try {
      drillFields = JSON.parse(chart.drillFields)
    } catch (err) {
      drillFields = JSON.parse(JSON.stringify(chart.drillFields))
    }

    const drillField = drillFields[chart.drillFilters.length]

    const drillFilters = JSON.parse(JSON.stringify(chart.drillFilters))
    const drillExp = drillFilters[drillFilters.length - 1].datasetTableField

    // 移除所有下钻字段
    const removeField = []
    for (let i = 0; i < chart.drillFilters.length; i++) {
      const ele = chart.drillFilters[i].datasetTableField
      removeField.push(ele.dataeaseName)
    }

    // build field
    fields.forEach(ele => {
      if (removeField.indexOf(ele.dataeaseName) < 0) {
        // 用下钻字段替换当前字段
        if (drillExp.dataeaseName === ele.dataeaseName) {
          columns.push(drillField.dataeaseName)
          meta.push({
            field: drillField.dataeaseName,
            name: drillField.name
          })
        } else {
          columns.push(ele.dataeaseName)
          meta.push({
            field: ele.dataeaseName,
            name: ele.name
          })
        }
      }
    })
  } else {
    fields.forEach(ele => {
      columns.push(ele.dataeaseName)
      meta.push({
        field: ele.dataeaseName,
        name: ele.name
      })
    })
  }

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
    style: getSize(chart)
  }

  // 开始渲染
  if (s2) {
    s2.destroy()
  }
  s2 = new TableSheet(containerDom, s2DataConfig, s2Options)

  // click
  s2.on(S2Event.DATA_CELL_CLICK, action)

  // theme
  const customTheme = getCustomTheme(chart)
  s2.setThemeCfg({ theme: customTheme })

  return s2
}

export function baseTableNormal(s2, container, chart, action, tableData) {
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

  // add drill list
  if (chart.drill) {
    const drillFields = JSON.parse(chart.drillFields)
    const drillField = drillFields[chart.drillFilters.length]

    const drillFilters = JSON.parse(JSON.stringify(chart.drillFilters))
    const drillExp = drillFilters[drillFilters.length - 1].datasetTableField

    // 移除所有下钻字段
    const removeField = []
    for (let i = 0; i < chart.drillFilters.length; i++) {
      const ele = chart.drillFilters[i].datasetTableField
      removeField.push(ele.dataeaseName)
    }

    // build field
    fields.forEach(ele => {
      if (removeField.indexOf(ele.dataeaseName) < 0) {
        // 用下钻字段替换当前字段
        if (drillExp.dataeaseName === ele.dataeaseName) {
          columns.push(drillField.dataeaseName)
          meta.push({
            field: drillField.dataeaseName,
            name: drillField.name
          })
        } else {
          columns.push(ele.dataeaseName)
          meta.push({
            field: ele.dataeaseName,
            name: ele.name
          })
        }
      }
    })
  } else {
    fields.forEach(ele => {
      columns.push(ele.dataeaseName)
      meta.push({
        field: ele.dataeaseName,
        name: ele.name
      })
    })
  }

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
    style: getSize(chart)
  }

  // 开始渲染
  if (s2) {
    s2.destroy()
  }
  s2 = new TableSheet(containerDom, s2DataConfig, s2Options)

  // click
  s2.on(S2Event.DATA_CELL_CLICK, action)

  // theme
  const customTheme = getCustomTheme(chart)
  s2.setThemeCfg({ theme: customTheme })

  return s2
}
