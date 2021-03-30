/* eslint-disable no-lonely-if */
import { calculateRotatedPointCoordinate, getCenterPoint } from './translate'

const funcs = {
    lt: calculateLeftTop,
    t: calculateTop,
    rt: calculateRightTop,
    r: calculateRight,
    rb: calculateRightBottom,
    b: calculateBottom,
    lb: calculateLeftBottom,
    l: calculateLeft,
}

function calculateLeftTop(style, curPositon, proportion, needLockProportion, pointInfo) {
    const { symmetricPoint } = pointInfo
    let newCenterPoint = getCenterPoint(curPositon, symmetricPoint)
    let newTopLeftPoint = calculateRotatedPointCoordinate(curPositon, newCenterPoint, -style.rotate)
    let newBottomRightPoint = calculateRotatedPointCoordinate(symmetricPoint, newCenterPoint, -style.rotate)
  
    let newWidth = newBottomRightPoint.x - newTopLeftPoint.x
    let newHeight = newBottomRightPoint.y - newTopLeftPoint.y

    if (needLockProportion) {
        if (newWidth / newHeight > proportion) {
            newTopLeftPoint.x += Math.abs(newWidth - newHeight * proportion)
            newWidth = newHeight * proportion
        } else {
            newTopLeftPoint.y += Math.abs(newHeight - newWidth / proportion)
            newHeight = newWidth / proportion
        }

        // 由于现在求的未旋转前的坐标是以没按比例缩减宽高前的坐标来计算的
        // 所以缩减宽高后，需要按照原来的中心点旋转回去，获得缩减宽高并旋转后对应的坐标
        // 然后以这个坐标和对称点获得新的中心点，并重新计算未旋转前的坐标
        const rotatedTopLeftPoint = calculateRotatedPointCoordinate(newTopLeftPoint, newCenterPoint, style.rotate)
        newCenterPoint = getCenterPoint(rotatedTopLeftPoint, symmetricPoint)
        newTopLeftPoint = calculateRotatedPointCoordinate(rotatedTopLeftPoint, newCenterPoint, -style.rotate)
        newBottomRightPoint = calculateRotatedPointCoordinate(symmetricPoint, newCenterPoint, -style.rotate)
    
        newWidth = newBottomRightPoint.x - newTopLeftPoint.x
        newHeight = newBottomRightPoint.y - newTopLeftPoint.y
    }

    if (newWidth > 0 && newHeight > 0) {
        style.width = Math.round(newWidth)
        style.height = Math.round(newHeight)
        style.left = Math.round(newTopLeftPoint.x)
        style.top = Math.round(newTopLeftPoint.y)
    }
}

function calculateRightTop(style, curPositon, proportion, needLockProportion, pointInfo) {
    const { symmetricPoint } = pointInfo
    let newCenterPoint = getCenterPoint(curPositon, symmetricPoint)
    let newTopRightPoint = calculateRotatedPointCoordinate(curPositon, newCenterPoint, -style.rotate)
    let newBottomLeftPoint = calculateRotatedPointCoordinate(symmetricPoint, newCenterPoint, -style.rotate)
  
    let newWidth = newTopRightPoint.x - newBottomLeftPoint.x
    let newHeight = newBottomLeftPoint.y - newTopRightPoint.y

    if (needLockProportion) {
        if (newWidth / newHeight > proportion) {
            newTopRightPoint.x -= Math.abs(newWidth - newHeight * proportion)
            newWidth = newHeight * proportion
        } else {
            newTopRightPoint.y += Math.abs(newHeight - newWidth / proportion)
            newHeight = newWidth / proportion
        }

        const rotatedTopRightPoint = calculateRotatedPointCoordinate(newTopRightPoint, newCenterPoint, style.rotate)
        newCenterPoint = getCenterPoint(rotatedTopRightPoint, symmetricPoint)
        newTopRightPoint = calculateRotatedPointCoordinate(rotatedTopRightPoint, newCenterPoint, -style.rotate)
        newBottomLeftPoint = calculateRotatedPointCoordinate(symmetricPoint, newCenterPoint, -style.rotate)
  
        newWidth = newTopRightPoint.x - newBottomLeftPoint.x
        newHeight = newBottomLeftPoint.y - newTopRightPoint.y
    }
    
    if (newWidth > 0 && newHeight > 0) {
        style.width = Math.round(newWidth)
        style.height = Math.round(newHeight)
        style.left = Math.round(newBottomLeftPoint.x)
        style.top = Math.round(newTopRightPoint.y)
    }
}
  
function calculateRightBottom(style, curPositon, proportion, needLockProportion, pointInfo) {
    const { symmetricPoint } = pointInfo
    let newCenterPoint = getCenterPoint(curPositon, symmetricPoint)
    let newTopLeftPoint = calculateRotatedPointCoordinate(symmetricPoint, newCenterPoint, -style.rotate)
    let newBottomRightPoint = calculateRotatedPointCoordinate(curPositon, newCenterPoint, -style.rotate)
  
    let newWidth = newBottomRightPoint.x - newTopLeftPoint.x
    let newHeight = newBottomRightPoint.y - newTopLeftPoint.y

    if (needLockProportion) {
        if (newWidth / newHeight > proportion) {
            newBottomRightPoint.x -= Math.abs(newWidth - newHeight * proportion)
            newWidth = newHeight * proportion
        } else {
            newBottomRightPoint.y -= Math.abs(newHeight - newWidth / proportion)
            newHeight = newWidth / proportion
        }

        const rotatedBottomRightPoint = calculateRotatedPointCoordinate(newBottomRightPoint, newCenterPoint, style.rotate)
        newCenterPoint = getCenterPoint(rotatedBottomRightPoint, symmetricPoint)
        newTopLeftPoint = calculateRotatedPointCoordinate(symmetricPoint, newCenterPoint, -style.rotate)
        newBottomRightPoint = calculateRotatedPointCoordinate(rotatedBottomRightPoint, newCenterPoint, -style.rotate)
  
        newWidth = newBottomRightPoint.x - newTopLeftPoint.x
        newHeight = newBottomRightPoint.y - newTopLeftPoint.y
    }

    if (newWidth > 0 && newHeight > 0) {
        style.width = Math.round(newWidth)
        style.height = Math.round(newHeight)
        style.left = Math.round(newTopLeftPoint.x)
        style.top = Math.round(newTopLeftPoint.y)
    }
}
  
function calculateLeftBottom(style, curPositon, proportion, needLockProportion, pointInfo) {
    const { symmetricPoint } = pointInfo
    let newCenterPoint = getCenterPoint(curPositon, symmetricPoint)
    let newTopRightPoint = calculateRotatedPointCoordinate(symmetricPoint, newCenterPoint, -style.rotate)
    let newBottomLeftPoint = calculateRotatedPointCoordinate(curPositon, newCenterPoint, -style.rotate)

    let newWidth = newTopRightPoint.x - newBottomLeftPoint.x
    let newHeight = newBottomLeftPoint.y - newTopRightPoint.y

    if (needLockProportion) {
        if (newWidth / newHeight > proportion) {
            newBottomLeftPoint.x += Math.abs(newWidth - newHeight * proportion)
            newWidth = newHeight * proportion
        } else {
            newBottomLeftPoint.y -= Math.abs(newHeight - newWidth / proportion)
            newHeight = newWidth / proportion
        }

        const rotatedBottomLeftPoint = calculateRotatedPointCoordinate(newBottomLeftPoint, newCenterPoint, style.rotate)
        newCenterPoint = getCenterPoint(rotatedBottomLeftPoint, symmetricPoint)
        newTopRightPoint = calculateRotatedPointCoordinate(symmetricPoint, newCenterPoint, -style.rotate)
        newBottomLeftPoint = calculateRotatedPointCoordinate(rotatedBottomLeftPoint, newCenterPoint, -style.rotate)

        newWidth = newTopRightPoint.x - newBottomLeftPoint.x
        newHeight = newBottomLeftPoint.y - newTopRightPoint.y
    }

    if (newWidth > 0 && newHeight > 0) {
        style.width = Math.round(newWidth)
        style.height = Math.round(newHeight)
        style.left = Math.round(newBottomLeftPoint.x)
        style.top = Math.round(newTopRightPoint.y)
    }
}

function calculateTop(style, curPositon, proportion, needLockProportion, pointInfo) {
    const { symmetricPoint, curPoint } = pointInfo
    let rotatedcurPositon = calculateRotatedPointCoordinate(curPositon, curPoint, -style.rotate)
    let rotatedTopMiddlePoint = calculateRotatedPointCoordinate({
        x: curPoint.x,
        y: rotatedcurPositon.y,
    }, curPoint, style.rotate)
  
    // 勾股定理
    let newHeight = Math.sqrt((rotatedTopMiddlePoint.x - symmetricPoint.x) ** 2 + (rotatedTopMiddlePoint.y - symmetricPoint.y) ** 2)
    
    if (newHeight > 0) {
        const newCenter = {
            x: rotatedTopMiddlePoint.x - (rotatedTopMiddlePoint.x - symmetricPoint.x) / 2,
            y: rotatedTopMiddlePoint.y + (symmetricPoint.y - rotatedTopMiddlePoint.y) / 2,
        }

        let width = style.width
        // 因为调整的是高度 所以只需根据锁定的比例调整宽度即可
        if (needLockProportion) {
            width = newHeight * proportion
        }
        
        style.width = width
        style.height = Math.round(newHeight)
        style.top = Math.round(newCenter.y - (newHeight / 2))
        style.left = Math.round(newCenter.x - (style.width / 2))
    }
}
  
function calculateRight(style, curPositon, proportion, needLockProportion, pointInfo) {
    const { symmetricPoint, curPoint } = pointInfo
    const rotatedcurPositon = calculateRotatedPointCoordinate(curPositon, curPoint, -style.rotate)
    const rotatedRightMiddlePoint = calculateRotatedPointCoordinate({
        x: rotatedcurPositon.x,
        y: curPoint.y,
    }, curPoint, style.rotate)
  
    let newWidth = Math.sqrt((rotatedRightMiddlePoint.x - symmetricPoint.x) ** 2 + (rotatedRightMiddlePoint.y - symmetricPoint.y) ** 2)
    if (newWidth > 0) {
        const newCenter = {
            x: rotatedRightMiddlePoint.x - (rotatedRightMiddlePoint.x - symmetricPoint.x) / 2,
            y: rotatedRightMiddlePoint.y + (symmetricPoint.y - rotatedRightMiddlePoint.y) / 2,
        }

        let height = style.height
        // 因为调整的是宽度 所以只需根据锁定的比例调整高度即可
        if (needLockProportion) {
            height = newWidth / proportion
        }
        
        style.height = height
        style.width = Math.round(newWidth)
        style.top = Math.round(newCenter.y - (style.height / 2))
        style.left = Math.round(newCenter.x - (newWidth / 2))
    }
}
  
function calculateBottom(style, curPositon, proportion, needLockProportion, pointInfo) {
    const { symmetricPoint, curPoint } = pointInfo
    const rotatedcurPositon = calculateRotatedPointCoordinate(curPositon, curPoint, -style.rotate)
    const rotatedBottomMiddlePoint = calculateRotatedPointCoordinate({
        x: curPoint.x,
        y: rotatedcurPositon.y,
    }, curPoint, style.rotate)
  
    const newHeight = Math.sqrt((rotatedBottomMiddlePoint.x - symmetricPoint.x) ** 2 + (rotatedBottomMiddlePoint.y - symmetricPoint.y) ** 2)
    if (newHeight > 0) {
        const newCenter = {
            x: rotatedBottomMiddlePoint.x - (rotatedBottomMiddlePoint.x - symmetricPoint.x) / 2,
            y: rotatedBottomMiddlePoint.y + (symmetricPoint.y - rotatedBottomMiddlePoint.y) / 2,
        }

        let width = style.width
        // 因为调整的是高度 所以只需根据锁定的比例调整宽度即可
        if (needLockProportion) {
            width = newHeight * proportion
        }
        
        style.width = width
        style.height = Math.round(newHeight)
        style.top = Math.round(newCenter.y - (newHeight / 2))
        style.left = Math.round(newCenter.x - (style.width / 2))
    }
}
  
function calculateLeft(style, curPositon, proportion, needLockProportion, pointInfo) {
    const { symmetricPoint, curPoint } = pointInfo
    const rotatedcurPositon = calculateRotatedPointCoordinate(curPositon, curPoint, -style.rotate)
    const rotatedLeftMiddlePoint = calculateRotatedPointCoordinate({
        x: rotatedcurPositon.x,
        y: curPoint.y,
    }, curPoint, style.rotate)
  
    const newWidth = Math.sqrt((rotatedLeftMiddlePoint.x - symmetricPoint.x) ** 2 + (rotatedLeftMiddlePoint.y - symmetricPoint.y) ** 2)
    if (newWidth > 0) {
        const newCenter = {
            x: rotatedLeftMiddlePoint.x - (rotatedLeftMiddlePoint.x - symmetricPoint.x) / 2,
            y: rotatedLeftMiddlePoint.y + (symmetricPoint.y - rotatedLeftMiddlePoint.y) / 2,
        }
        
        let height = style.height
        if (needLockProportion) {
            height = newWidth / proportion
        }
        
        style.height = height
        style.width = Math.round(newWidth)
        style.top = Math.round(newCenter.y - (style.height / 2))
        style.left = Math.round(newCenter.x - (newWidth / 2))
    }
}

export default function calculateComponentPositonAndSize(name, style, curPositon, proportion, needLockProportion, pointInfo) {
    funcs[name](style, curPositon, proportion, needLockProportion, pointInfo)
}