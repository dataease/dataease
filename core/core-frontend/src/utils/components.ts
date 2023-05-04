import RectShape from '@/custom-component/rect-shape/Component.vue'
import RectShapeAttr from '@/custom-component/rect-shape/Attr.vue'
import VText from '@/custom-component/v-text/Component.vue'
import VTextAttr from '@/custom-component/v-text/Attr.vue'
import Group from '@/custom-component/group/Component.vue'
import GroupAttr from '@/custom-component/group/Attr.vue'
import UserView from '@/custom-component/user-view/Component.vue'
import UserViewAttr from '@/custom-component/user-view/Attr.vue'

export const componentsMap = {
  RectShape: RectShape,
  RectShapeAttr: RectShapeAttr,
  VText: VText,
  VTextAttr: VTextAttr,
  Group: Group,
  GroupAttr: GroupAttr,
  UserView: UserView,
  UserViewAttr: UserViewAttr
}

export default function findComponent(key) {
  return componentsMap[key]
}
