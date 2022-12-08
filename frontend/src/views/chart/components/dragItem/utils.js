export function getItemType(dimensionData, quotaData, item) {
  // Check whether the current view is in template status
  // ( dimensionData and quotaData have no data). If yes, return 'success' directly
  if (dimensionData.length === 0 && quotaData.length === 0) {
    return 'success'
  }
  // 将item的字段在数据集维度、指标字段中查询一遍，如果遇到id不存在、字段类型不一致、维度指标不一致，则提示
  const status = item.groupType
  let checked = false
  if (status === 'd') {
    for (let i = 0; i < dimensionData.length; i++) {
      const ele = dimensionData[i]
      if (item.chartId) {
        if (ele.dataeaseName === item.dataeaseName && ele.deType === item.deType && ele.groupType === item.groupType) {
          checked = true
          break
        }
      } else {
        if (ele.id === item.id && ele.deType === item.deType && ele.groupType === item.groupType) {
          checked = true
          break
        }
      }
    }
  }
  if (status === 'q') {
    for (let i = 0; i < quotaData.length; i++) {
      const ele = quotaData[i]
      if (item.chartId) {
        if (ele.dataeaseName === item.dataeaseName && ele.deType === item.deType && ele.groupType === item.groupType) {
          checked = true
          break
        }
      } else {
        if (ele.id === item.id && ele.deType === item.deType && ele.groupType === item.groupType) {
          checked = true
          break
        }
      }
    }
  }

  if (checked) {
    if (status === 'd') {
      return ''
    } else if (status === 'q') {
      return 'success'
    }
  } else {
    return 'danger'
  }
}

export function getOriginFieldName(dimensionList, quotaList, field) {
  let originName = ''
  for (let i = 0; i < dimensionList.length; i++) {
    const item = dimensionList[i]
    if (item.id === field.id) {
      originName = item.name
      break
    }
  }
  for (let i = 0; i < quotaList.length; i++) {
    const item = quotaList[i]
    if (item.id === field.id) {
      originName = item.name
      break
    }
  }
  return originName
}

export function resetValueFormatter(item) {
  if (item) {
    item.formatterCfg = {
      type: 'auto', // auto,value,percent
      unit: 1, // 换算单位
      suffix: '', // 单位后缀
      decimalCount: 2, // 小数位数
      thousandSeparator: true// 千分符
    }
  }
}
