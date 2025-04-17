import { Base64 } from 'js-base64'

const originNameHandle = (arr = []) => {
  arr.forEach(ele => {
    if (ele.extField === 2) {
      ele.originName = Base64.encode(ele.originName)
    }
  })
}

const originNameHandleBack = (arr = []) => {
  arr.forEach(ele => {
    if (ele.extField === 2) {
      ele.originName = Base64.decode(ele.originName)
    }
  })
}

const originNameHandleWithArr = (obj = {}, fields) => {
  fields.forEach(ele => {
    originNameHandle(obj?.[ele] || [])
  })
}

const originNameHandleBackWithArr = (obj = {}, fields) => {
  fields.forEach(ele => {
    originNameHandleBack(obj?.[ele] || [])
  })
}

export {
  originNameHandle,
  originNameHandleBack,
  originNameHandleWithArr,
  originNameHandleBackWithArr
}
