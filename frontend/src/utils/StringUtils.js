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
