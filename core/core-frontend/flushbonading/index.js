import { WritableStream } from 'htmlparser2/lib/WritableStream'
import fs from 'node:fs'
import pkg from '../package.json' with { type: "json" };
const suffix = `${pkg.version}-${pkg.name}`

const eleArr = []

function produceTag(obj, name) {
  eleArr.push({
    name,
    attributes: obj,
  })
}
const parserStream = new WritableStream({
  onopentag(name, attributes) {
    /*
     * This fires when a new tag is opened.
     *
     * If you don't need an aggregated `attributes` object,
     * have a look at the `onopentagname` and `onattribute` events.
     */
    if (['script', 'link'].includes(name) && attributes.rel !== 'icon') {
      produceTag(attributes, name)
    }
  }
})

const htmlStream = fs.createReadStream('../dist/panel.html')
htmlStream.pipe(parserStream).on('finish', () => {
  const templateJs = `let head = document.createElement('head')
  let suffix = \`${suffix}\`


  const getPrefix = () => {
    let prefix = ''
    Array.from(document.querySelector('head').children).some(ele => {
      if (['SCRIPT', 'LINK'].includes(ele.nodeName)) {
        let url = ''
        if (ele.nodeName === 'LINK') {
          url = ele.href
        } else if (ele.nodeName === 'SCRIPT') {
          url = ele.src
        }
        if (url.includes(suffix)) {
          prefix = new URL(url).origin
          const index = url.indexOf(\`/js/div_import_${suffix}\`)
          if (index > 0) {
            prefix = url.substring(0, index)
          }
          return true
        }
      }
    })
    return prefix
  }

  const eleArrStr = ${JSON.stringify(eleArr)}
  const eleArr = eleArrStr
  const preUrl = getPrefix()
  
  function produceTag(obj, name) {
    let element = document.createElement(name)
    Object.entries(obj).forEach(([key, value]) => {
      if (['href', 'src'].includes(key)) {
        const relativeVal = value.startsWith('./') ? value.substr(1) : value
        element[key] = \`\${preUrl}\${relativeVal}\`
      } else {
        element.setAttribute(key, value || '')
      }
    })
    element.setAttribute('crossorigin', '')
    head.appendChild(element)
  }
  
  eleArr.forEach((ele) => {
    produceTag(ele.attributes, ele.name)
  })
  document.documentElement.insertBefore(head, document.querySelector('head'))`

  fs.writeFile(`../dist/js/div_import_${suffix}.js`, templateJs, err => {
  })
})
