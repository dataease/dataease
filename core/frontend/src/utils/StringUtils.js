import i18n from '@/lang'

// 替换所有 标准模板格式 为 $panelName$
export function pdfTemplateReplaceAll(content, source, target) {
  const pattern = '\\$' + source + '\\$'
  content = content.replace(new RegExp(pattern, 'gm'), target)
  return content
}

export function randomRange(min, max) {
  let returnStr = ''
  const range = (max ? Math.round(Math.random() * (max - min)) + min : min)
  const charStr = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'

  for (var i = 0; i < range; i++) {
    var index = Math.round(Math.random() * (charStr.length - 1))
    returnStr += charStr.substring(index, index + 1)
  }
  return returnStr
}

export function equalsAny(target, ...sources) {
  for (let i = 0; i < sources.length; i++) {
    if (target === sources[i]) {
      return true
    }
  }
  return false
}

export function includesAny(target, ...sources) {
  if (!target || !sources) {
    return false
  }
  for (let i = 0; i < sources.length; i++) {
    if (target.includes(sources[i])) {
      return true
    }
  }
  return false
}

// 替换字符串中的国际化内容, 格式为$t('xxx')
export function replaceInlineI18n(rawString) {
  const res = []
  const reg = /\$t\('([\w.]+)'\)/gm
  let tmp
  if (!rawString) {
    return res
  }
  while ((tmp = reg.exec(rawString)) !== null) {
    res.push(tmp)
  }
  res.forEach((tmp) => {
    rawString = rawString.replaceAll(tmp[0], i18n.t(tmp[1]))
  })
  return rawString
}
