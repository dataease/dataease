import DeBoard1 from '@/custom-component/de-decoration/component_details/DeBoard1.vue'
import DeBoard2 from '@/custom-component/de-decoration/component_details/DeBoard2.vue'
import DeBoard3 from '@/custom-component/de-decoration/component_details/DeBoard3.vue'
import DeBoard4 from '@/custom-component/de-decoration/component_details/DeBoard4.vue'
import DeBoard5 from '@/custom-component/de-decoration/component_details/DeBoard5.vue'
import DeBoard6 from '@/custom-component/de-decoration/component_details/DeBoard6.vue'
import DeBoard7 from '@/custom-component/de-decoration/component_details/DeBoard7.vue'
import DeBoard8 from '@/custom-component/de-decoration/component_details/DeBoard8.vue'
import DeBoard9 from '@/custom-component/de-decoration/component_details/DeBoard9.vue'
import DeBoard10 from '@/custom-component/de-decoration/component_details/DeBoard10.vue'
import DeDecoration1 from '@/custom-component/de-decoration/component_details/DeDecoration1.vue'
import DeDecoration2 from '@/custom-component/de-decoration/component_details/DeDecoration2.vue'
import DeDecoration3 from '@/custom-component/de-decoration/component_details/DeDecoration3.vue'
import DeDecoration4 from '@/custom-component/de-decoration/component_details/DeDecoration4.vue'
import DeDecoration5 from '@/custom-component/de-decoration/component_details/DeDecoration5.vue'

const boardInfoMap = {
  DeBoard1: DeBoard1,
  DeBoard2: DeBoard2,
  DeBoard3: DeBoard3,
  DeBoard4: DeBoard4,
  DeBoard5: DeBoard5,
  DeBoard6: DeBoard6,
  DeBoard7: DeBoard7,
  DeBoard8: DeBoard8,
  DeBoard9: DeBoard9,
  DeBoard10: DeBoard10,
  DeDecoration1: DeDecoration1,
  DeDecoration2: DeDecoration2,
  DeDecoration3: DeDecoration3,
  DeDecoration4: DeDecoration4,
  DeDecoration5: DeDecoration5
}

export const findDecoration = name => {
  return boardInfoMap[name]
}

export const calcTwoPointDistance = (pointA, pointB) => {
  const minusX = Math.abs(pointA[0] - pointB[0])
  const minusY = Math.abs(pointA[1] - pointB[1])

  return Math.sqrt(Math.pow(minusX, 2) + Math.pow(minusY, 2))
}

/**
 * @description 获取多个点，每个点之间的距离
 * @param {Point[]} points
 * @return {number[]}
 */
export function getPointDistances(points) {
  return new Array(points.length - 1)
    .fill(0)
    .map((_, i) => calcTwoPointDistance(points[i], points[i + 1]))
}

export function customMergeColor(defaultColor: string[], newColor: []) {
  return defaultColor.map((defaultVal, index) => {
    return newColor && newColor[index] !== null ? newColor[index] : defaultVal
  })
}
