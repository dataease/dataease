import VText from '@/custom-component/v-text/Component.vue'
import VQuery from '@/custom-component/v-query/Component.vue'
import Group from '@/custom-component/group/Component.vue'
import UserView from '@/custom-component/user-view/Component.vue'
import Picture from '@/custom-component/picture/Component.vue'
import CanvasBoard from '@/custom-component/canvas-board/Component.vue'
import CanvasIcon from '@/custom-component/canvas-icon/Component.vue'
import DeTabs from '@/custom-component/de-tabs/Component.vue'
import DeGraphical from '@/custom-component/de-graphical/Component.vue'
import CircleShape from '@/custom-component/circle-shape/Component.vue'
import RectShape from '@/custom-component/rect-shape/Component.vue'
import SvgTriangle from '@/custom-component/svgs/svg-triangle/Component.vue'
import DeTimeClock from '@/custom-component/de-time-clock/Component.vue'
import GroupArea from '@/custom-component/group-area/Component.vue'
import DeFrame from '@/custom-component/de-frame/ComponentFrame.vue'
import DeVideo from '@/custom-component/de-video/Component.vue'
import DeStreamMedia from '@/custom-component/de-stream-media/Component.vue'
import ScrollText from '@/custom-component/scroll-text/Component.vue'
import PopArea from '@/custom-component/pop-area/Component.vue'
import PictureGroup from '@/custom-component/picture-group/Component.vue'
export const componentsMap = {
  VText: VText,
  VQuery,
  Group: Group,
  UserView: UserView,
  Picture: Picture,
  CanvasBoard: CanvasBoard,
  CanvasIcon: CanvasIcon,
  DeTabs: DeTabs,
  DeGraphical: DeGraphical,
  CircleShape: CircleShape,
  RectShape: RectShape,
  SvgTriangle: SvgTriangle,
  DeTimeClock: DeTimeClock,
  GroupArea: GroupArea,
  DeFrame: DeFrame,
  DeVideo: DeVideo,
  DeStreamMedia: DeStreamMedia,
  ScrollText: ScrollText,
  PopArea: PopArea,
  PictureGroup: PictureGroup
}

export default function findComponent(key) {
  return componentsMap[key]
}
