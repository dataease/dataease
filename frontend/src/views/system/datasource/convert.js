const isBoolean = require('lodash.isboolean')
const isEmpty = require('lodash.isempty')
const isInteger = require('lodash.isinteger')
const isNull = require('lodash.isnull')
const isNumber = require('lodash.isnumber')
const isObject = require('lodash.isobject')
const isString = require('lodash.isstring')
const isArray = Array.isArray

class Convert {
  constructor() {
    this._option = {
      $id: 'http://example.com/root.json',
      $schema: 'http://json-schema.org/draft-07/schema#',
    }
    this._object = null
  }

  /**
   * 转换函数
   * @param {*} object 需要转换的对象
   * @param {*} ?option 可选参数，目前只有能设置 root 节点的 $id 和 $schema
   */
  format(object, option = {}) {
    // 数据校验，确保传入的的object只能是对象或数组
    if (!isObject(object)) {
      throw new TypeError('传入参数只能是对象或数组')
    }
    // 合并属性
    this._option = Object.assign(this._option, option)
    // 需要转换的对象
    this._object = object
    let convertRes
    // 数组类型和对象类型结构不一样
    if (isArray(object)) {
      convertRes = this._arrayToSchema()
    } else {
      convertRes = this._objectToSchema()
    }
    // 释放
    this._object = null
    return convertRes
  }

  /**
   * 数组类型转换成JSONSCHEMA
   */
  _arrayToSchema() {
    // root节点基本信息
    const result = this._value2object(this._object, this._option.$id, '', true)
    if (this._object.length > 0) {
      const itemArr = []
      for (let index = 0; index < this._object.length; index++) {
        // 创建items对象的基本信息
        const objectItem = this._object[index]
        let item = this._value2object(objectItem, `#/items`, 'items')
        if (isObject(objectItem) && !isEmpty(objectItem)) {
          // 递归遍历
          const objectItemSchema = this._json2schema(objectItem, `#/items`)
          // 合并对象
          item = Object.assign(item, objectItemSchema)
        }
        itemArr.push(item)
      }
      result['items'] = itemArr
    }
    return result
  }

  /**
   * 对象类型转换成JSONSCHEMA
   */
  _objectToSchema() {
    let baseResult = this._value2object(this._object, this._option.$id, '', true)
    const objectSchema = this._json2schema(this._object)
    baseResult = Object.assign(baseResult, objectSchema)
    return baseResult
  }

  /**
   * 递归函数，转换object对象为json schmea 格式
   * @param {*} object 需要转换对象
   * @param {*} name $id值
   */
  _json2schema(object, name = '') {
    // 如果递归值不是对象，那么return掉
    if (!isObject(object)) {
      return
    }
    // 处理当前路径$id
    if (name === '' || name == undefined) {
      name = '#'
    }
    const result = {}
    // 判断传入object是对象还是数组。
    if (isArray(object)) {
      result.items = {}
    } else {
      result.properties = {}
    }
    // 遍历传入的对象
    for (const key in object) {
      if (object.hasOwnProperty(key)) {
        const element = object[key]
        // 如果只是undefined。跳过
        if (element === undefined) {
          continue
        }
        const $id = `${name}/properties/${key}`
        // 判断当前 element 的值 是否也是对象，如果是就继续递归，不是就赋值给result
        if (!result['properties']) {
          continue
        }
        if (isObject(element)) {
          // 创建当前属性的基本信息
          result['properties'][key] = this._value2object(element, $id, key)
          if (isArray(element)) {
            // 针对空数组和有值的数组做不同处理
            if (element.length > 0) {
              // 是数组
              const itemArr = []
              for (let index = 0; index < element.length; index++) {
                const elementItem = element[index]
                // 创建items对象的基本信息
                let item = this._value2object(elementItem, `${$id}/items`, key + 'items')
                // 判断第一项是否是对象,且对象属性不为空
                if (isObject(elementItem) && !isEmpty(elementItem)) {
                  // 新增的properties才合并进来
                  item = Object.assign(item, this._json2schema(elementItem, `${$id}/items`))
                }
                itemArr.push(item)
              }
              result['properties'][key]['items'] = itemArr
            }
          } else {
            // 不是数组，递归遍历获取，然后合并对象属性
            result['properties'][key] = Object.assign(result['properties'][key], this._json2schema(element, $id))
          }
        } else {
          // 一般属性直接获取基本信息
          if (result['properties']) {
            result['properties'][key] = this._value2object(element, $id, key)
          }
        }
      }
    }
    return result
  }

  /**
   * 把json的值转换成对象类型
   * @param {*} value
   * @param {*} $id
   * @param {*} key
   */
  _value2object(value, $id, key = '', root = false) {
    const objectTemplate = {
      $id: $id,
      title: `The ${key} Schema`,
      mock: {
        'mock': value
      },
    }

    // 判断是否为初始化root数据
    if (root) {
      objectTemplate['$schema'] = this._option.$schema
      objectTemplate['title'] = `The Root Schema`
      objectTemplate['mock'] = undefined
    }
    if (isBoolean(value)) {
      objectTemplate.type = 'boolean'
    } else if (isInteger(value)) {
      objectTemplate.type = 'integer'
    } else if (isNumber(value)) {
      objectTemplate.type = 'number'
    } else if (isString(value)) {
      objectTemplate.type = 'string'
    } else if (isNull(value)) {
      objectTemplate.type = 'null'
    } else if (isArray(value)) {
      objectTemplate.type = 'array'
      objectTemplate['mock'] = undefined
    } else if (isObject(value)) {
      objectTemplate.type = 'object'
      objectTemplate['mock'] = undefined
    }

    return objectTemplate
  }
}

