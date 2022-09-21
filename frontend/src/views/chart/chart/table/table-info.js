import { TableSheet, S2Event, PivotSheet } from '@antv/s2'
import { getCustomTheme, getSize } from '@/views/chart/chart/common/common_table'
import { DEFAULT_COLOR_CASE, DEFAULT_TOTAL } from '@/views/chart/chart/chart'
import { formatterItem, valueFormatter } from '@/views/chart/chart/formatter'
import { hexColorToRGBA } from '@/views/chart/chart/util'

export function baseTableInfo(s2, container, chart, action, tableData) {
  const containerDom = document.getElementById(container)

  // fields
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
          const f = getCurrentField(chart.xaxis, ele)
          columns.push(ele.dataeaseName)
          meta.push({
            field: ele.dataeaseName,
            name: ele.name,
            formatter: function(value) {
              if (!f) {
                return value
              }
              if (value === null || value === undefined) {
                return value
              }
              if (f.groupType === 'd') {
                return value
              } else {
                if (f.formatterCfg) {
                  const v = valueFormatter(value, f.formatterCfg)
                  return v.includes('NaN') ? value : v
                } else {
                  const v = valueFormatter(value, formatterItem)
                  return v.includes('NaN') ? value : v
                }
              }
            }
          })
        }
      }
    })
  } else {
    fields.forEach(ele => {
      const f = getCurrentField(chart.xaxis, ele)
      columns.push(ele.dataeaseName)
      meta.push({
        field: ele.dataeaseName,
        name: ele.name,
        formatter: function(value) {
          if (!f) {
            return value
          }
          if (value === null || value === undefined) {
            return value
          }
          if (f.groupType === 'd') {
            return value
          } else {
            if (f.formatterCfg) {
              const v = valueFormatter(value, f.formatterCfg)
              return v.includes('NaN') ? value : v
            } else {
              const v = valueFormatter(value, formatterItem)
              return v.includes('NaN') ? value : v
            }
          }
        }
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
    style: getSize(chart),
    conditions: getConditions(chart)
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
  if (!containerDom) return

  // fields
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
          const f = getCurrentField(chart.yaxis, ele)
          columns.push(ele.dataeaseName)
          meta.push({
            field: ele.dataeaseName,
            name: ele.name,
            formatter: function(value) {
              if (!f) {
                return value
              }
              if (value === null || value === undefined) {
                return value
              }
              if (f.formatterCfg) {
                return valueFormatter(value, f.formatterCfg)
              } else {
                return valueFormatter(value, formatterItem)
              }
            }
          })
        }
      }
    })
  } else {
    fields.forEach(ele => {
      const f = getCurrentField(chart.yaxis, ele)
      columns.push(ele.dataeaseName)
      meta.push({
        field: ele.dataeaseName,
        name: ele.name,
        formatter: function(value) {
          if (!f) {
            return value
          }
          if (value === null || value === undefined) {
            return value
          }
          if (f.formatterCfg) {
            return valueFormatter(value, f.formatterCfg)
          } else {
            return valueFormatter(value, formatterItem)
          }
        }
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
    style: getSize(chart),
    conditions: getConditions(chart)
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

export function baseTablePivot(s2, container, chart, action, tableData) {
  const containerDom = document.getElementById(container)

  // row and column
  const columnFields = JSON.parse(chart.xaxis)
  const rowFields = JSON.parse(chart.xaxisExt)
  const valueFields = JSON.parse(chart.yaxis)
  const c = []; const r = []; const v = []
  columnFields.forEach(ele => {
    c.push(ele.dataeaseName)
  })
  rowFields.forEach(ele => {
    r.push(ele.dataeaseName)
  })
  valueFields.forEach(ele => {
    v.push(ele.dataeaseName)
  })

  // fields
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
          const f = getCurrentField(chart.yaxis, ele)
          columns.push(ele.dataeaseName)
          meta.push({
            field: ele.dataeaseName,
            name: ele.name,
            formatter: function(value) {
              if (!f) {
                return value
              }
              if (value === null || value === undefined) {
                return value
              }
              if (f.formatterCfg) {
                return valueFormatter(value, f.formatterCfg)
              } else {
                return valueFormatter(value, formatterItem)
              }
            }
          })
        }
      }
    })
  } else {
    fields.forEach(ele => {
      const f = getCurrentField(chart.yaxis, ele)
      columns.push(ele.dataeaseName)
      meta.push({
        field: ele.dataeaseName,
        name: ele.name,
        formatter: function(value) {
          if (!f) {
            return value
          }
          if (value === null || value === undefined) {
            return value
          }
          if (f.formatterCfg) {
            return valueFormatter(value, f.formatterCfg)
          } else {
            return valueFormatter(value, formatterItem)
          }
        }
      })
    })
  }

  // data config
  const s2DataConfig = {
    fields: {
      rows: r,
      columns: c,
      values: v
    },
    meta: meta,
    data: tableData
  }

  // total config
  let totalCfg = {}
  const chartObj = JSON.parse(JSON.stringify(chart))
  if (chartObj.customAttr) {
    let customAttr = null
    if (Object.prototype.toString.call(chartObj.customAttr) === '[object Object]') {
      customAttr = JSON.parse(JSON.stringify(chartObj.customAttr))
    } else {
      customAttr = JSON.parse(chartObj.customAttr)
    }
    if (customAttr.totalCfg) {
      totalCfg = customAttr.totalCfg
    } else {
      totalCfg = JSON.parse(JSON.stringify(DEFAULT_TOTAL))
    }
  }
  totalCfg.row.subTotalsDimensions = r
  totalCfg.col.subTotalsDimensions = c

  // options
  const s2Options = {
    width: containerDom.offsetWidth,
    height: containerDom.offsetHeight,
    style: getSize(chart),
    totals: totalCfg,
    conditions: getConditions(chart)
  }

  // 开始渲染
  if (s2) {
    s2.destroy()
  }
  s2 = new PivotSheet(containerDom, s2DataConfig, s2Options)

  // click
  s2.on(S2Event.DATA_CELL_CLICK, action)

  // theme
  const customTheme = getCustomTheme(chart)
  s2.setThemeCfg({ theme: customTheme })

  return s2
}

function getCurrentField(valueFieldList, field) {
  let list = []
  let res = null
  try {
    list = JSON.parse(valueFieldList)
  } catch (err) {
    list = JSON.parse(JSON.stringify(valueFieldList))
  }
  if (list) {
    for (let i = 0; i < list.length; i++) {
      const f = list[i]
      if (field.dataeaseName === f.dataeaseName) {
        res = f
        break
      }
    }
  }

  return res
}

function getConditions(chart) {
  const res = {
    text: [],
    background: []
  }
  let conditions
  try {
    const senior = JSON.parse(chart.senior)
    conditions = senior.threshold ? senior.threshold.tableThreshold : null
  } catch (err) {
    const senior = JSON.parse(JSON.stringify(chart.senior))
    conditions = senior.threshold ? senior.threshold.tableThreshold : null
  }

  if (conditions && conditions.length > 0) {
    // table item color
    let valueColor = DEFAULT_COLOR_CASE.tableFontColor
    let valueBgColor = DEFAULT_COLOR_CASE.tableItemBgColor
    if (chart.customAttr) {
      const customAttr = JSON.parse(chart.customAttr)
      // color
      if (customAttr.color) {
        const c = JSON.parse(JSON.stringify(customAttr.color))
        valueColor = c.tableFontColor
        valueBgColor = hexColorToRGBA(c.tableItemBgColor, c.alpha)
      }
    }

    for (let i = 0; i < conditions.length; i++) {
      const field = conditions[i]
      res.text.push({
        field: field.field.dataeaseName,
        mapping(value) {
          return {
            fill: mappingColor(value, valueColor, field, 'color')
          }
        }
      })
      res.background.push({
        field: field.field.dataeaseName,
        mapping(value) {
          return {
            fill: mappingColor(value, valueBgColor, field, 'backgroundColor')
          }
        }
      })
    }
  }
  return res
}

function mappingColor(value, defaultColor, field, type) {
  let color
  for (let i = 0; i < field.conditions.length; i++) {
    let flag = false
    const t = field.conditions[i]
    if (field.field.deType === 2 || field.field.deType === 3 || field.field.deType === 4) {
      const tv = parseFloat(t.value)
      if (t.term === 'eq') {
        if (value === tv) {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'not_eq') {
        if (value !== tv) {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'lt') {
        if (value < tv) {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'gt') {
        if (value > tv) {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'le') {
        if (value <= tv) {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'ge') {
        if (value >= tv) {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'between') {
        const min = parseFloat(t.min)
        const max = parseFloat(t.max)
        if (min <= value && value <= max) {
          color = t[type]
          flag = true
        }
      }
      if (flag) {
        break
      } else if (i === field.conditions.length - 1) {
        color = defaultColor
      }
    } else if (field.field.deType === 0 || field.field.deType === 5) {
      const tv = t.value
      if (t.term === 'eq') {
        if (value === tv) {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'not_eq') {
        if (value !== tv) {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'like') {
        if (value.includes(tv)) {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'not like') {
        if (!value.includes(tv)) {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'null') {
        if (value === null || value === undefined || value === '') {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'not_null') {
        if (value !== null && value !== undefined && value !== '') {
          color = t[type]
          flag = true
        }
      }
      if (flag) {
        break
      } else if (i === field.conditions.length - 1) {
        color = defaultColor
      }
    } else {
      // time
      const tv = new Date(t.value.replace(/-/g, '/') + ' GMT+8').getTime()
      const v = new Date(value.replace(/-/g, '/') + ' GMT+8').getTime()
      if (t.term === 'eq') {
        if (v === tv) {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'not_eq') {
        if (v !== tv) {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'lt') {
        if (v < tv) {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'gt') {
        if (v > tv) {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'le') {
        if (v <= tv) {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'ge') {
        if (v >= tv) {
          color = t[type]
          flag = true
        }
      }
      if (flag) {
        break
      } else if (i === field.conditions.length - 1) {
        color = defaultColor
      }
    }
  }
  return color
}
