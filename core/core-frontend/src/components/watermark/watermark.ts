// 动态创建水印元素的封装函数

export function watermark(settings, domId) {
  const watermarkDom = document.getElementById(domId)
  // 默认设置
  const defaultSettings = {
    watermark_txt: '',
    watermark_x: 20, // 水印起始位置x轴坐标
    watermark_y: 20, // 水印起始位置Y轴坐标
    watermark_rows: 20, // 水印行数
    watermark_cols: 20, // 水印列数
    watermark_x_space: 100, // 水印x轴间隔
    watermark_y_space: 50, // 水印y轴间隔
    watermark_color: '#aaa', // 水印字体颜色
    watermark_alpha: 0.4, // 水印透明度
    watermark_fontsize: '15px', // 水印字体大小
    watermark_font: '微软雅黑', // 水印字体
    watermark_width: 210, // 水印宽度
    watermark_height: 80, // 水印长度
    watermark_angle: 20 // 水印倾斜度数
  }
  // 根据函数的入参调整设置
  if (settings && typeof settings === 'object') {
    const src = settings || {}
    for (const key in src) {
      if (src[key] && defaultSettings[key] && src[key] === defaultSettings[key]) {
        continue
      } else if (src[key]) defaultSettings[key] = src[key]
    }
  }
  // 创建虚拟节点对象，在该节点对象中可以放元素，最后只需在页面中添加该节点对象即可。可提高性能
  const oTemp = document.createElement('p')
  // 获取页面最大宽度
  let page_width = watermarkDom.clientWidth
  const cutWidth = page_width * 0.015
  page_width = page_width - cutWidth
  // 获取页面最大高度
  let page_height = watermarkDom.scrollHeight - 56
  page_height = page_height < 400 ? 400 : page_height
  // page_height = Math.max(page_height, window.innerHeight - 30)
  // 如果将水印列数设置为0，或水印列数设置过大，超过页面最大宽度，则重新计算水印列数和水印x轴间隔
  if (
    defaultSettings.watermark_cols === 0 ||
    Math.floor(
      defaultSettings.watermark_x +
        defaultSettings.watermark_width * defaultSettings.watermark_cols +
        defaultSettings.watermark_x_space * (defaultSettings.watermark_cols - 1)
    ) > page_width
  ) {
    defaultSettings.watermark_cols = Math.floor(
      (page_width - defaultSettings.watermark_x + defaultSettings.watermark_x_space) /
        (defaultSettings.watermark_width + defaultSettings.watermark_x_space)
    )
    defaultSettings.watermark_x_space = Math.floor(
      (page_width -
        defaultSettings.watermark_x -
        defaultSettings.watermark_width * defaultSettings.watermark_cols) /
        (defaultSettings.watermark_cols - 1)
    )
  }
  // 如果将水印行数设置为0，或水印行数设置过大，超过页面最大长度，则重新计算水印行数和水印y轴间隔
  if (
    defaultSettings.watermark_rows === 0 ||
    Math.floor(
      defaultSettings.watermark_y +
        defaultSettings.watermark_height * defaultSettings.watermark_rows +
        defaultSettings.watermark_y_space * (defaultSettings.watermark_rows - 1)
    ) > page_height
  ) {
    defaultSettings.watermark_rows = Math.floor(
      (defaultSettings.watermark_y_space + page_height - defaultSettings.watermark_y) /
        (defaultSettings.watermark_height + defaultSettings.watermark_y_space)
    )
    defaultSettings.watermark_y_space = Math.floor(
      (page_height -
        defaultSettings.watermark_y -
        defaultSettings.watermark_height * defaultSettings.watermark_rows) /
        (defaultSettings.watermark_rows - 1)
    )
  }
  defaultSettings.watermark_rows =
    defaultSettings.watermark_rows < 2 ? 2 : defaultSettings.watermark_rows
  defaultSettings.watermark_cols =
    defaultSettings.watermark_cols < 2 ? 2 : defaultSettings.watermark_cols
  let x
  let y
  for (let i = 0; i < defaultSettings.watermark_rows; i++) {
    y =
      defaultSettings.watermark_y +
      (defaultSettings.watermark_y_space + defaultSettings.watermark_height) * i
    for (let j = 0; j < defaultSettings.watermark_cols; j++) {
      x =
        defaultSettings.watermark_x +
        (defaultSettings.watermark_width + defaultSettings.watermark_x_space) * j
      const mask_div = document.createElement('div')
      mask_div.id = 'mask_div' + i + j
      mask_div.className = 'mask_div'
      mask_div.appendChild(document.createTextNode(defaultSettings.watermark_txt))
      // 设置水印div倾斜显示
      mask_div.style.webkitTransform = 'rotate(-' + defaultSettings.watermark_angle + 'deg)'
      mask_div.style.MozTransform = 'rotate(-' + defaultSettings.watermark_angle + 'deg)'
      mask_div.style.msTransform = 'rotate(-' + defaultSettings.watermark_angle + 'deg)'
      mask_div.style.OTransform = 'rotate(-' + defaultSettings.watermark_angle + 'deg)'
      mask_div.style.transform = 'rotate(-' + defaultSettings.watermark_angle + 'deg)'
      mask_div.style.visibility = ''
      mask_div.style.position = 'absolute'
      mask_div.style.left = x + 'px'
      mask_div.style.top = y + 'px'
      mask_div.style.overflow = 'hidden'
      mask_div.style.zIndex = '10'
      // 让水印不遮挡页面的点击事件
      mask_div.style.pointerEvents = 'none'
      mask_div.style.opacity = defaultSettings.watermark_alpha
      mask_div.style.fontSize = defaultSettings.watermark_fontsize
      mask_div.style.fontFamily = defaultSettings.watermark_font
      mask_div.style.color = defaultSettings.watermark_color
      mask_div.style.textAlign = 'center'
      mask_div.style.width = defaultSettings.watermark_width + 'px'
      mask_div.style.height = defaultSettings.watermark_height + 'px'
      mask_div.style.display = 'block'
      oTemp.appendChild(mask_div)
    }
  }
  oTemp.setAttribute('id', 'de-watermark-server')
  watermarkDom.appendChild(oTemp)
}

export function getNow() {
  const d = new Date()
  const year = d.getFullYear()
  const month = change(d.getMonth() + 1)
  const day = change(d.getDate())
  const hour = change(d.getHours())
  const minute = change(d.getMinutes())

  function change(t) {
    if (t < 10) {
      return '0' + t
    } else {
      return t
    }
  }

  const time = year + '-' + month + '-' + day + ' ' + hour + ':' + minute
  return time
}

export function activeWatermark(
  watermarkForm,
  userLoginInfo,
  domId,
  canvasId,
  selfWatermarkStatus,
  scale = 1
) {
  // 清理历史水印
  const historyWatermarkDom = document.getElementById('de-watermark-server')
  if (historyWatermarkDom) {
    historyWatermarkDom.remove()
  }
  if (
    !(
      canvasId === 'canvas-main' &&
      ((watermarkForm.enable && !watermarkForm.enablePanelCustom) ||
        (watermarkForm.enable && selfWatermarkStatus))
    )
  ) {
    return
  }
  let watermark_txt
  let watermark_width = 120
  if (watermarkForm.type === 'custom') {
    watermark_txt = watermarkForm.content
    watermark_txt = watermark_txt.replaceAll('${ip}', userLoginInfo.ip)
    watermark_txt = watermark_txt.replaceAll('${username}', userLoginInfo.account)
    watermark_txt = watermark_txt.replaceAll('${nickName}', userLoginInfo.name)
    watermark_txt = watermark_txt.replaceAll('${time}', getNow())
    watermark_width = watermark_txt.length * watermarkForm.watermark_fontsize * 0.75
    watermark_width = watermark_width > 350 ? 350 : watermark_width
  } else if (watermarkForm.type === 'nickName') {
    watermark_txt = userLoginInfo.name
  } else if (watermarkForm.type === 'ip') {
    watermark_txt = userLoginInfo.ip
    watermark_width = 150
  } else if (watermarkForm.type === 'time') {
    watermark_txt = getNow()
    watermark_width = 200
  } else {
    watermark_txt = userLoginInfo.name
  }
  const settings = {
    watermark_txt: watermark_txt,
    watermark_width: watermark_width * scale,
    watermark_color: watermarkForm.watermark_color,
    watermark_x_space: watermarkForm.watermark_x_space * scale,
    watermark_y_space: watermarkForm.watermark_y_space * scale,
    watermark_fontsize: watermarkForm.watermark_fontsize * scale + 'px'
  }
  watermark(settings, domId)
}

export default { watermark, getNow, activeWatermark }
