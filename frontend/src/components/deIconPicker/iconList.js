import { TypeUtil } from './utils/index'
import fontAwesome from './fontAwesome'
import elementUI from './elementUI'
import eIconList from './eIconList'

const add = function(list, item) {
  let arr = []
  if (item && TypeUtil.isArray(item)) {
    arr = list.concat(item)
  } else if (item && TypeUtil.isString(item)) {
    arr = arr.concat(list)
    arr.push(item)
  }
  return arr
}

const remove = function(list, item) {
  if (item && TypeUtil.isArray(item)) {
    for (let i = 0; i < item.length; i++) {
      for (let j = 0; j < list.length; j++) {
        if (list[j] === item[i]) {
          list.splice(j, 1)
          j--
        }
      }
    }
  } else if (item && TypeUtil.isString(item)) {
    list = list.filter(function(i) {
      return i !== item
    })
  }
  return list
}

const iconList = {
  list: [],
  group: {},

  addIcon: function(item) {
    this.list = add(this.list, item)
  },
  addGroup: function(item) {
    const { value, type } = item
    this.group[type] = this.group[type] ? this.group[type] : []
    if (TypeUtil.isArray(value)) {
      this.group[type] = this.group[type].concat(value)
    } else {
      this.group[type].push(value)
    }
  },
  initGroup: function(data) {
    this.group = JSON.parse(JSON.stringify(data))
  },

  removeIcon: function(item) {
    this.list = remove(this.list, item)
  }
}

export { fontAwesome, elementUI, eIconList }
export default iconList
