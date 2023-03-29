import RectShape from '@/custom-component/rect-shape/Component.vue'
import Group from '@/custom-component/group/Component.vue'

export const componentsMap = {
  RectShape: RectShape,
  Group: Group
}

export default function findComponent(key) {
  return componentsMap[key]
}
