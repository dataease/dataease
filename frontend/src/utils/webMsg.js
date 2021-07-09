export const msgTypes = [
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
