// 替换所有 标准模板格式 为 $panelName$
export function pdfTemplateReplaceAll(content, source, target) {
  const pattern = '\\$' + source + '\\$'
  content = content.replace(new RegExp(pattern, 'gm'), target)
  return content
}

