import { sin, cos } from '@/utils/translate'

export function getStyle(style, filter = []) {
    const needUnit = [
        'fontSize',
        'width',
        'height',
        'top',
        'left',
        'borderWidth',
        'letterSpacing',
        'borderRadius',
    ]

    const result = {}
    Object.keys(style).forEach(key => {
        if (!filter.includes(key)) {
            if (key != 'rotate') {
                result[key] = style[key]

                if (needUnit.includes(key)) {
                    result[key] += 'px'
                }
            } else {
                result.transform = key + '(' + style[key] + 'deg)'
            }
        }
    })

    return result
}

// 获取一个组件旋转 rotate 后的样式
export function getComponentRotatedStyle(style) {
    style = { ...style }
    if (style.rotate != 0) {
        const newWidth = style.width * cos(style.rotate) + style.height * sin(style.rotate)
        const diffX = (style.width - newWidth) / 2 // 旋转后范围变小是正值，变大是负值
        style.left += diffX
        style.right = style.left + newWidth

        const newHeight = style.height * cos(style.rotate) + style.width * sin(style.rotate)
        const diffY = (newHeight - style.height) / 2 // 始终是正
        style.top -= diffY
        style.bottom = style.top + newHeight

        style.width = newWidth
        style.height = newHeight
    } else {
        style.bottom = style.top + style.height
        style.right = style.left + style.width
    }

    return style
}