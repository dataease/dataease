import DOMPurify from 'dompurify'

const allowedTags = [
  'a',
  'b',
  'blockquote',
  'br',
  'caption',
  'code',
  'col',
  'colgroup',
  'dd',
  'del',
  'div',
  'dl',
  'dt',
  'em',
  'figcaption',
  'figure',
  'font',
  'h1',
  'h2',
  'h3',
  'h4',
  'h5',
  'h6',
  'hr',
  'i',
  'img',
  'ins',
  'li',
  'mark',
  'ol',
  'p',
  'pre',
  's',
  'small',
  'span',
  'strike',
  'strong',
  'sub',
  'sup',
  'table',
  'tbody',
  'td',
  'tfoot',
  'th',
  'thead',
  'tr',
  'u',
  'ul'
]

const allowedAttributes = [
  'align',
  'alt',
  'bgcolor',
  'border',
  'cellpadding',
  'cellspacing',
  'class',
  'color',
  'colspan',
  'contenteditable',
  'dir',
  'face',
  'height',
  'href',
  'id',
  'lang',
  'rel',
  'reversed',
  'rowspan',
  'scope',
  'size',
  'span',
  'src',
  'start',
  'style',
  'target',
  'title',
  'type',
  'valign',
  'value',
  'width'
]

// CSSOM parses the declarations. Only copy properties whose value grammar cannot load resources.
const allowedStyles = [
  'background-color',
  'border',
  'border-bottom',
  'border-bottom-color',
  'border-bottom-style',
  'border-bottom-width',
  'border-collapse',
  'border-color',
  'border-left',
  'border-left-color',
  'border-left-style',
  'border-left-width',
  'border-radius',
  'border-right',
  'border-right-color',
  'border-right-style',
  'border-right-width',
  'border-spacing',
  'border-style',
  'border-top',
  'border-top-color',
  'border-top-style',
  'border-top-width',
  'border-width',
  'color',
  'direction',
  'float',
  'font-family',
  'font-size',
  'font-style',
  'font-variant',
  'font-weight',
  'height',
  'letter-spacing',
  'line-height',
  'list-style-type',
  'margin',
  'margin-bottom',
  'margin-left',
  'margin-right',
  'margin-top',
  'max-height',
  'max-width',
  'min-height',
  'min-width',
  'overflow-wrap',
  'padding',
  'padding-bottom',
  'padding-left',
  'padding-right',
  'padding-top',
  'table-layout',
  'text-align',
  'text-decoration',
  'text-decoration-color',
  'text-decoration-line',
  'text-decoration-style',
  'text-indent',
  'text-transform',
  'vertical-align',
  'white-space',
  'width',
  'word-break',
  'word-spacing',
  'writing-mode'
]

const rasterImage = /^data:image\/(?:avif|bmp|gif|jpeg|png|webp|x-icon);base64,/i
const dynamicStyleValue = /\b(?:attr|env|url|var)\(/i

export const parseRichTextHtml = (html: string | null | undefined) => {
  const template = document.createElement('template')
  template.content.append(
    DOMPurify.sanitize(html ?? '', {
      ALLOWED_TAGS: allowedTags,
      ALLOWED_ATTR: allowedAttributes,
      ALLOW_DATA_ATTR: false,
      RETURN_DOM_FRAGMENT: true
    })
  )

  template.content.querySelectorAll<HTMLElement>('[style]').forEach(element => {
    const style = element.style
    const declarations = allowedStyles.flatMap(property => {
      const value = style.getPropertyValue(property)
      return value && !dynamicStyleValue.test(value)
        ? [{ property, value, priority: style.getPropertyPriority(property) }]
        : []
    })
    element.removeAttribute('style')
    declarations.forEach(({ property, value, priority }) => {
      style.setProperty(property, value, priority)
    })
  })

  template.content.querySelectorAll('[contenteditable]').forEach(element => {
    if (!element.matches('span.mceNonEditable[contenteditable="false"]')) {
      element.removeAttribute('contenteditable')
    }
  })
  template.content.querySelectorAll('img[src]').forEach(image => {
    const source = image.getAttribute('src')?.trim() ?? ''
    if (/^data:/i.test(source) && !rasterImage.test(source)) {
      image.removeAttribute('src')
    }
  })
  template.content.querySelectorAll<HTMLAnchorElement>('a[target]').forEach(link => {
    if (link.target.toLowerCase() === '_blank') link.relList.add('noopener')
  })

  return template
}

export const sanitizeRichTextHtml = (html: string | null | undefined) =>
  parseRichTextHtml(html).innerHTML
