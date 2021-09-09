export function hexColorToRGBA(hex, alpha) {
  const rgb = [] // 定义rgb数组
  if (/^\#[0-9A-F]{3}$/i.test(hex)) { // 判断传入是否为#三位十六进制数
    let sixHex = '#'
    hex.replace(/[0-9A-F]/ig, function(kw) {
      sixHex += kw + kw // 把三位16进制数转化为六位
    })
    hex = sixHex // 保存回hex
  }
  if (/^#[0-9A-F]{6}$/i.test(hex)) { // 判断传入是否为#六位十六进制数
    hex.replace(/[0-9A-F]{2}/ig, function(kw) {
      // eslint-disable-next-line no-eval
      rgb.push(eval('0x' + kw)) // 十六进制转化为十进制并存如数组
    })
    return `rgba(${rgb.join(',')},${alpha / 100})` // 输出RGB格式颜色
  } else {
    return 'rgb(0,0,0)'
  }
}

export function digToHex(dig) {
  let prefix = ''
  const num = parseInt(dig * 2.55)
  if (num < 16) {
    prefix = '0'
  }
  return prefix.concat(num.toString(16).toUpperCase())
}
