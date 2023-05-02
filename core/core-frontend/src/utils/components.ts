import RectShape from '@/custom-component/rect-shape/Component.vue'
import VText from '@/custom-component/v-text/Component.vue'
import Group from '@/custom-component/group/Component.vue'
import UserView from '@/custom-component/user-view/Component.vue'

export const componentsMap = {
  RectShape: RectShape,
  VText: VText,
  Group: Group,
  UserView: UserView
}

export default function findComponent(key) {
  return componentsMap[key]
}
