import VTextAttr from '@/custom-component/v-text/Attr.vue'
import GroupAttr from '@/custom-component/group/Attr.vue'
import UserViewAttr from '@/custom-component/user-view/Attr.vue'
import PictureAttr from '@/custom-component/picture/Attr.vue'
import CanvasBoardAttr from '@/custom-component/canvas-board/Attr.vue'
import CanvasIconAttr from '@/custom-component/canvas-icon/Attr.vue'
import DeTabsAttr from '@/custom-component/de-tabs/Attr.vue'
import DeGraphicalAttr from '@/custom-component/de-graphical/Attr.vue'
import CircleShapeAttr from '@/custom-component/circle-shape/Attr.vue'
import RectShapeAttr from '@/custom-component/rect-shape/Attr.vue'
import SvgTriangleAttr from '@/custom-component/svgs/svg-triangle/Attr.vue'
import DeTimeClockAttr from '@/custom-component/de-time-clock/Attr.vue'
import GroupAreaAttr from '@/custom-component/group-area/Attr.vue'
import DeFrameAttr from '@/custom-component/de-frame/Attr.vue'
import DeVideoAttr from '@/custom-component/de-video/Attr.vue'
import DeStreamMediaAttr from '@/custom-component/de-stream-media/Attr.vue'
import ScrollTextAttr from '@/custom-component/scroll-text/Attr.vue'
import PopAreaAttr from '@/custom-component/pop-area/Attr.vue'
import PictureGroupAttr from '@/custom-component/picture-group/Attr.vue'
export const componentsMap = {
  VTextAttr: VTextAttr,
  GroupAttr: GroupAttr,
  UserViewAttr: UserViewAttr,
  PictureAttr: PictureAttr,
  CanvasBoardAttr: CanvasBoardAttr,
  CanvasIconAttr: CanvasIconAttr,
  DeTabsAttr: DeTabsAttr,
  DeGraphicalAttr: DeGraphicalAttr,
  CircleShapeAttr: CircleShapeAttr,
  RectShapeAttr: RectShapeAttr,
  SvgTriangleAttr: SvgTriangleAttr,
  DeTimeClockAttr: DeTimeClockAttr,
  GroupAreaAttr: GroupAreaAttr,
  DeFrameAttr: DeFrameAttr,
  DeVideoAttr: DeVideoAttr,
  DeStreamMediaAttr: DeStreamMediaAttr,
  ScrollTextAttr: ScrollTextAttr,
  PopAreaAttr: PopAreaAttr,
  PictureGroupAttr: PictureGroupAttr
}

export function findComponentAttr(component) {
  const key =
    component.component === 'UserView' && component.innerType === 'picture-group'
      ? 'PictureGroupAttr'
      : component.component + 'Attr'
  return componentsMap[key]
}
