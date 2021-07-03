export const msgTypes = [
  // { value: -1, label: '全部类型' },
  // { value: 0, label: '仪表板分享' },
  // { value: 1, label: '数据集同步' }
  { value: -1, label: 'webmsg.all_type' },
  { value: 0, label: 'webmsg.panel_type' },
  { value: 1, label: 'webmsg.dataset_type' }
]

export const getTypeName = value => {
  for (let index = 0; index < msgTypes.length; index++) {
    const element = msgTypes[index]
    if (element.value === value) {
      return element.label
    }
  }
}
