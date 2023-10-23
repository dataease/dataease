import { WritableStream } from 'htmlparser2/lib/WritableStream'
import fs from 'node:fs'
import pkg from '../package.json' assert { type: "json" };
const suffix = `${pkg.version}-${pkg.name}`

let htmlStr = ''
const eleArr = []

function produceTag(obj, name) {
  eleArr.push({
    name,
    attributes: obj,
  })
  let innerProperty = ''
  Object.entries(obj).forEach(([key, value]) => {
    if (['href', 'src'].includes(key)) {
      innerProperty += ` ${key}="https://de2.fit2cloud.com${value}" `
    } else {
      innerProperty += value ? ` ${key}="${value}" ` : ''
    }
  })
  htmlStr += `\n<${name} crossorigin ${innerProperty}></${name}>`
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
  const template = `<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>flushbonading</title>
    ${htmlStr}
</head>
<style>
    .demo-top {
        display: inline-flex;
        height: 100vh;
        justify-content: center;
        align-items: center;
    }

    #dataease-container {
        display: inline-flex;
        width: 300px;
        height: 300px;
        overflow: auto;
    }

    .demo-bottom {
        display: inline-block;
    }
</style>
<body>
    <div class="demo-top">
        flushbonading
    </div>
    <div id="dataease-container">
    </div>
    <div class="demo-bottom">
        flushbonading
    </div>
</body>
<script type="module">
    DataEaseBi.create('DashboardEditor', { baseUrl: 'https://de2.fit2cloud.com/', token: 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOjEsIm9pZCI6MSwiZXhwIjoxNjg2NTgzMDg5fQ.JYvk4Oe6as9Xbf-EPf3w5w9OexUo0pZUsFXXMZFM57U' })
    DataEaseBi.initialize({ container: '#dataease-container' })
</script>

</html>`
  
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
        element[key] = \`\${preUrl}\${value}\`
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

  fs.writeFile('../dist/demo.html', template, err => {
    console.log('写入成功')
  })

  fs.writeFile(`../dist/js/div_import_${suffix}.js`, templateJs, err => {
    console.log('写入成功templateJs')
  })
})
