export function getItemType(dimensionData, quotaData, item) {
  // 将item的字段在数据集维度、指标字段中查询一遍，如果遇到id不存在、字段类型不一致、维度指标不一致，则提示
  const status = item.groupType
  let checked = false
  if (status === 'd') {
    for (let i = 0; i < dimensionData.length; i++) {
      const ele = dimensionData[i]
      if (ele.id === item.id && ele.deType === item.deType && ele.groupType === item.groupType) {
        checked = true
        break
      }
    }
  }
  if (status === 'q') {
    for (let i = 0; i < quotaData.length; i++) {
      const ele = quotaData[i]
      if (ele.id === item.id && ele.deType === item.deType && ele.groupType === item.groupType) {
        checked = true
        break
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

export function getRemark(chart) {
  const remark = {}
  if (chart.customStyle) {
    const customStyle = JSON.parse(chart.customStyle)
    if (customStyle.text) {
      const title = JSON.parse(JSON.stringify(customStyle.text))
      remark.show = title.remarkShow ? title.remarkShow : false
      remark.content = title.remark ? title.remark : ''
      remark.bgFill = title.remarkBackgroundColor ? title.remarkBackgroundColor : '#ffffffff'
    }
  }
  return remark
}

