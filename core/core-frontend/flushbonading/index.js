import { WritableStream } from 'htmlparser2/lib/WritableStream'
import fs from 'node:fs'

let htmlStr = ''

function produceTag(obj, name) {
  let innerProperty = ''
  Object.entries(obj).forEach(([key, value]) => {
    if (['href', 'src'].includes(key)) {
      innerProperty += ` ${key}="https://de2.fit2cloud.com${value}" `
    } else {
      innerProperty += value ? ` ${key}="${value}" ` : key
    }
  })
  htmlStr += `\n<${name} ${innerProperty}></${name}>`
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
<script >
    // https://de2.fit2cloud.com
    DataEaseBi.create('Dashboard', { baseUrl: 'https://de2.fit2cloud.com/', token: 'oBTKean4bOhSyFFk9g' })
    DataEaseBi.initialize({ container: '#dataease-container' })
</script>

</html>`

  fs.writeFile('../dist/demo.html', template, err => {
    console.log('写入成功')
  })
})
