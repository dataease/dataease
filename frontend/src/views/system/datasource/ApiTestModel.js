export class BaseConfig {
  set(options, notUndefined) {
    options = this.initOptions(options)
    for (const name in options) {
      if (Object.prototype.hasOwnProperty.call(options, name)) {
        if (!(this[name] instanceof Array)) {
          if (notUndefined === true) {
            this[name] = options[name] === undefined ? this[name] : options[name]
          } else {
            this[name] = options[name]
          }
        }
      }
    }
  }

  sets(types, options) {
    options = this.initOptions(options)
    if (types) {
      for (const name in types) {
        if (Object.prototype.hasOwnProperty.call(types, name) && Object.prototype.hasOwnProperty.call(options, name)) {
          options[name].forEach(o => {
            this[name].push(new types[name](o))
          })
        }
      }
    }
  }

  initOptions(options) {
    return options || {}
  }

  isValid() {
    return true
  }
}

export class KeyValue extends BaseConfig {
  constructor(options) {
    options = options || {}
    options.enable = options.enable === undefined ? true : options.enable

    super()
    this.name = undefined
    this.value = undefined
    this.type = undefined
    this.files = undefined
    this.enable = undefined
    this.uuid = undefined
    this.time = undefined
    this.contentType = undefined
    this.set(options)
  }

  isValid() {
    return (!!this.name || !!this.value) && this.type !== 'file'
  }

  isFile() {
    return (!!this.name || !!this.value) && this.type === 'file'
  }
}

export class Body extends BaseConfig {
  constructor(options) {
    super()
    this.type = 'KeyValue'
    this.raw = undefined
    this.kvs = []
    this.binary = []
    this.set(options)
    this.sets({ kvs: KeyValue }, { binary: KeyValue }, options)
  }

  isValid() {
    if (this.isKV()) {
      return this.kvs.some(kv => {
        return kv.isValid()
      })
    } else {
      return !!this.raw
    }
  }

  isKV() {
    return [BODY_TYPE.FORM_DATA, BODY_TYPE.WWW_FORM, BODY_TYPE.BINARY].indexOf(this.type) > 0
  }
}

export const BODY_TYPE = {
  KV: 'KeyValue',
  FORM_DATA: 'Form_Data',
  RAW: 'Raw',
  WWW_FORM: 'WWW_FORM',
  XML: 'XML',
  JSON: 'JSON'
}

export class Scenario extends BaseConfig {
  constructor(options = {}) {
    super()
    this.id = undefined
    this.name = undefined
    this.url = undefined
    this.variables = []
    this.headers = []
    this.requests = []
    this.environmentId = undefined
    this.dubboConfig = undefined
    this.environment = undefined
    this.enableCookieShare = false
    this.enable = true
    this.databaseConfigs = []
    this.tcpConfig = undefined
    this.set(options)
    this.sets({
      variables: KeyValue,
      headers: KeyValue
    }, options)
  }

  initOptions(options = {}) {
    options.databaseConfigs = options.databaseConfigs || []
    return options
  }

  clone() {
    const clone = new Scenario(this)
    return clone
  }

  isValid() {
    if (this.enable) {
      for (let i = 0; i < this.requests.length; i++) {
        const validator = this.requests[i].isValid(this.environmentId, this.environment)
        if (!validator.isValid) {
          return validator
        }
      }
    }
    return { isValid: true }
  }

  isReference() {
    return this.id.indexOf('#') !== -1
  }
}
