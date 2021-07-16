
import { allTypes } from '@/api/system/msg'
import store from '@/store'
export const loadMsgTypes = value => {
  let msgTypes = store.getters.msgTypes
  if (!msgTypes || Object.keys(msgTypes).length === 0) {
    allTypes().then(res => {
      msgTypes = res.data
      const defaultType = { msgTypeId: -1, pid: 0, typeName: 'i18n_msg_type_all' }
      msgTypes.splice(0, 0, defaultType)
      store.dispatch('msg/setMsgTypes', msgTypes)
    })
  }
}

export const getTypeName = value => {
  const msgTypes = store.getters.msgTypes
  for (let index = 0; index < msgTypes.length; index++) {
    const element = msgTypes[index]
    if (element.msgTypeId === value) {
      return element.typeName
    }
  }
}
