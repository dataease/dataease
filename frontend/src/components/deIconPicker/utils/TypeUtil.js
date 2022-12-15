
export const TypeUtil = {

  isArray: function(obj) {
    return (typeof obj === 'object') && obj.constructor === Array
  },

  isString: function(obj) {
    return (typeof obj === 'string') && obj.constructor === String
  },

  isNumber: function(obj) {
    return (typeof obj === 'number') && obj.constructor === Number
  },

  isDate: function(obj) {
    return (typeof obj === 'object') && obj.constructor === Date
  },

  isFunction: function(obj) {
    return (typeof obj === 'function') && obj.constructor === Function
  },

  isObject: function(obj) {
    return (typeof obj === 'object') && obj.constructor === Object
  }
}
