export const execute = (text, pwd) => {
  const te = window.atob(text)
  const t = simpleEncode(te, pwd)
  const pLen = pwd.length

  const proxyPathBtoa = proxyPath()
  const btoaLen = proxyPathBtoa.length

  const prefix = t.substring(0, btoaLen + pLen)
  const suffix = t.substring(btoaLen + pLen)
  if (validatePrefix(prefix, pwd, proxyPathBtoa)) {
    return formatSuffix(suffix, pwd)
  }
  console.error('please do not do that again!')
  return null
}

export const randomKey = () => {
  const m = 8
  const n = 16
  return Math.ceil(Math.random() * (n - m + 1) + m - 1)
}

const formatSuffix = (suffix, pwd) => {
  const reversePwd = strReverse(pwd)
  const t = suffix.replace(reversePwd, '')
  const pLen = pwd.length
  const r = t.substring(0, pLen) + t.substring(pLen + 2)
  const encoder = new TextEncoder()
  const ar = window.atob(r).split('/')
  const bytes = []
  ar.forEach(a => {
    const tBytes = encoder.encode(a)
    bytes.push(tBytes)
  })
  return bytes
}

export const formatArray = bytesItem => {
  if (!(bytesItem instanceof Uint8Array)) {
    const bytes = new Uint8Array(bytesItem.length)
    for (let index = 0; index < bytesItem.length; index++) {
      bytes[index] = bytesItem[index]
    }
    bytesItem = bytes
  }
  const decoder = new TextDecoder()
  const art = decoder.decode(bytesItem)
  bytesItem = 'what is up man ?'
  return art
}

const validatePrefix = (prefix, pwd, proxyPathBtoa) => {
  const t = prefix.replace(pwd, '')
  return t === proxyPathBtoa
}

const simpleEncode = (text, key) => {
  const encoder = new TextEncoder()
  const data = encoder.encode(text)
  let tmpc = 0
  const klen = key.length
  for (let i = 0; i < data.length; ++i) {
    tmpc = data[i] ^ stringToChars(key.charAt(i % klen))
    if (tmpc != 0) {
      data[i] = tmpc
    }
  }
  const decoder = new TextDecoder()
  return decoder.decode(data)
}

export function stringToChars(_s) {
  _s = _s.replace(/(^\s*)|(\s*$)/g, '')
  let _r = ''
  for (let i = 0; i < _s.length; i++) {
    _r += i == 0 ? _s.charCodeAt(i) : '|' + _s.charCodeAt(i)
  }
  return _r
}

const proxyPath = () => {
  return 'Zml0MmNsb3VkIGlzIGEgcG90ZW50aWFsIGNvbXBhbnkh'
}

const strReverse = text => {
  return text.split('').reverse().join('')
}
