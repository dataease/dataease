import board_1 from '@/assets/svg/board_1.svg'
import board_2 from '@/assets/svg/board_2.svg'
import board_3 from '@/assets/svg/board_3.svg'
import board_4 from '@/assets/svg/board_4.svg'
import board_5 from '@/assets/svg/board_5.svg'
import board_6 from '@/assets/svg/board_6.svg'
import board_7 from '@/assets/svg/board_7.svg'
import board_8 from '@/assets/svg/board_8.svg'
import board_9 from '@/assets/svg/board_9.svg'
import graphicalCircular from '@/assets/svg/graphical-circular.svg'
import graphicalRect from '@/assets/svg/graphical-rect.svg'
import graphicalTriangle from '@/assets/svg/graphical-triangle.svg'
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()
export const CANVAS_MATERIAL = [
  {
    category: 'CanvasBoard',
    title: t('visualization.board'),
    span: 8,
    details: [
      {
        value: 'board_1',
        type: 'outer_svg',
        title: t('visualization.board_name', [1]),
        icon: board_1
      },
      {
        value: 'board_2',
        type: 'outer_svg',
        title: t('visualization.board_name', [2]),
        icon: board_2
      },
      {
        value: 'board_3',
        type: 'outer_svg',
        title: t('visualization.board_name', [3]),
        icon: board_3
      },
      {
        value: 'board_4',
        type: 'outer_svg',
        title: t('visualization.board_name', [4]),
        icon: board_4
      },
      {
        value: 'board_5',
        type: 'outer_svg',
        title: t('visualization.board_name', [5]),
        icon: board_5
      },
      {
        value: 'board_6',
        type: 'outer_svg',
        title: t('visualization.board_name', [6]),
        icon: board_6
      },
      {
        value: 'board_7',
        type: 'outer_svg',
        title: t('visualization.board_name', [7]),
        icon: board_7
      },
      {
        value: 'board_8',
        type: 'outer_svg',
        title: t('visualization.board_name', [8]),
        icon: board_8
      },
      {
        value: 'board_9',
        type: 'outer_svg',
        title: t('visualization.board_name', [9]),
        icon: board_9
      }
    ]
  },
  {
    category: 'DeDecoration',
    title: t('visualization.decoration'),
    span: 8,
    details: [
      {
        value: 'DeDecoration1',
        type: 'de_decoration',
        title: t('visualization.decoration_name', [1]),
        icon: 'DeDecoration1'
      },
      {
        value: 'DeDecoration2',
        type: 'de_decoration',
        title: t('visualization.decoration_name', [2]),
        icon: 'DeDecoration2'
      },
      {
        value: 'DeDecoration3',
        type: 'de_decoration',
        title: t('visualization.decoration_name', [3]),
        icon: 'DeDecoration3'
      },
      {
        value: 'DeDecoration4',
        type: 'de_decoration',
        title: t('visualization.decoration_name', [4]),
        icon: 'DeDecoration4'
      },
      {
        value: 'DeDecoration5',
        type: 'de_decoration',
        title: t('visualization.decoration_name', [5]),
        icon: 'DeDecoration5'
      },
      {
        value: 'DeBoard1',
        type: 'de_decoration',
        title: t('visualization.decoration_name', [6]),
        icon: 'DeBoard1'
      },
      {
        value: 'DeBoard2',
        type: 'de_decoration',
        title: t('visualization.decoration_name', [7]),
        icon: 'DeBoard2'
      },
      {
        value: 'DeBoard3',
        type: 'de_decoration',
        title: t('visualization.decoration_name', [8]),
        icon: 'DeBoard3'
      },
      {
        value: 'DeBoard4',
        type: 'de_decoration',
        title: t('visualization.decoration_name', [9]),
        icon: 'DeBoard4'
      },
      {
        value: 'DeBoard5',
        type: 'de_decoration',
        title: t('visualization.decoration_name', [10]),
        icon: 'DeBoard5'
      },
      {
        value: 'DeBoard6',
        type: 'de_decoration',
        title: t('visualization.decoration_name', [11]),
        icon: 'DeBoard6'
      },
      {
        value: 'DeBoard7',
        type: 'de_decoration',
        title: t('visualization.decoration_name', [12]),
        icon: 'DeBoard7'
      },
      {
        value: 'DeBoard8',
        type: 'de_decoration',
        title: t('visualization.decoration_name', [13]),
        icon: 'DeBoard8'
      },
      {
        value: 'DeBoard10',
        type: 'de_decoration',
        title: t('visualization.decoration_name', [14]),
        icon: 'DeBoard10'
      }
    ]
  },
  {
    category: 'DeGraphical',
    title: t('visualization.graphic'),
    span: 8,
    details: [
      {
        value: 'RectShape',
        type: 'graphical',
        title: t('visualization.rect_shape'),
        icon: graphicalRect
      },
      {
        value: 'SvgTriangle',
        type: 'graphical',
        title: t('visualization.triangle'),
        icon: graphicalTriangle
      },
      {
        value: 'CircleShape',
        type: 'graphical',
        title: t('visualization.circle_shape'),
        icon: graphicalCircular
      }
    ]
  },
  {
    category: 'CanvasIcon',
    title: t('visualization.icon'),
    span: 4,
    details: [
      { value: 'Plus', type: 'inner_svg', icon: 'Plus' },
      { value: 'Minus', type: 'inner_svg', icon: 'Minus' },
      { value: 'CirclePlus', type: 'inner_svg', icon: 'CirclePlus' },
      { value: 'Search', type: 'inner_svg', icon: 'Search' },
      { value: 'Female', type: 'inner_svg', icon: 'Female' },
      { value: 'Male', type: 'inner_svg', icon: 'Male' },
      { value: 'Aim', type: 'inner_svg', icon: 'Aim' },
      { value: 'House', type: 'inner_svg', icon: 'House' },
      { value: 'FullScreen', type: 'inner_svg', icon: 'FullScreen' },
      { value: 'Loading', type: 'inner_svg', icon: 'Loading' },
      { value: 'Link', type: 'inner_svg', icon: 'Link' },
      { value: 'Service', type: 'inner_svg', icon: 'Service' },
      { value: 'Pointer', type: 'inner_svg', icon: 'Pointer' },
      { value: 'Star', type: 'inner_svg', icon: 'Star' },
      { value: 'Notification', type: 'inner_svg', icon: 'Notification' },
      { value: 'Connection', type: 'inner_svg', icon: 'Connection' },
      { value: 'ChatDotRound', type: 'inner_svg', icon: 'ChatDotRound' },
      { value: 'Setting', type: 'inner_svg', icon: 'Setting' },
      { value: 'Clock', type: 'inner_svg', icon: 'Clock' },
      { value: 'Position', type: 'inner_svg', icon: 'Position' },
      { value: 'Discount', type: 'inner_svg', icon: 'Discount' },
      { value: 'Odometer', type: 'inner_svg', icon: 'Odometer' },
      { value: 'ChatSquare', type: 'inner_svg', icon: 'ChatSquare' },
      { value: 'ChatRound', type: 'inner_svg', icon: 'ChatRound' },
      { value: 'ChatLineRound', type: 'inner_svg', icon: 'ChatLineRound' },
      { value: 'ChatLineSquare', type: 'inner_svg', icon: 'ChatLineSquare' },
      { value: 'ChatDotSquare', type: 'inner_svg', icon: 'ChatDotSquare' },
      { value: 'View', type: 'inner_svg', icon: 'View' },
      { value: 'Hide', type: 'inner_svg', icon: 'Hide' },
      { value: 'Unlock', type: 'inner_svg', icon: 'Unlock' },
      { value: 'Lock', type: 'inner_svg', icon: 'Lock' },
      { value: 'RefreshRight', type: 'inner_svg', icon: 'RefreshRight' },
      { value: 'RefreshLeft', type: 'inner_svg', icon: 'RefreshLeft' },
      { value: 'Refresh', type: 'inner_svg', icon: 'Refresh' },
      { value: 'Bell', type: 'inner_svg', icon: 'Bell' },
      { value: 'MuteNotification', type: 'inner_svg', icon: 'MuteNotification' },
      { value: 'User', type: 'inner_svg', icon: 'User' },
      { value: 'Check', type: 'inner_svg', icon: 'Check' },
      { value: 'CircleCheck', type: 'inner_svg', icon: 'CircleCheck' },
      { value: 'Warning', type: 'inner_svg', icon: 'Warning' },
      { value: 'CircleClose', type: 'inner_svg', icon: 'CircleClose' },
      { value: 'Close', type: 'inner_svg', icon: 'Close' },
      { value: 'PieChart', type: 'inner_svg', icon: 'PieChart' },
      { value: 'More', type: 'inner_svg', icon: 'More' },
      { value: 'Compass', type: 'inner_svg', icon: 'Compass' },
      { value: 'Filter', type: 'inner_svg', icon: 'Filter' },
      { value: 'Switch', type: 'inner_svg', icon: 'Switch' },
      { value: 'Select', type: 'inner_svg', icon: 'Select' },
      { value: 'SemiSelect', type: 'inner_svg', icon: 'SemiSelect' },
      { value: 'CloseBold', type: 'inner_svg', icon: 'CloseBold' },
      { value: 'EditPen', type: 'inner_svg', icon: 'EditPen' },
      { value: 'Edit', type: 'inner_svg', icon: 'Edit' },
      { value: 'Message', type: 'inner_svg', icon: 'Message' },
      { value: 'MessageBox', type: 'inner_svg', icon: 'MessageBox' },
      { value: 'TurnOff', type: 'inner_svg', icon: 'TurnOff' },
      { value: 'Finished', type: 'inner_svg', icon: 'Finished' },
      { value: 'Delete', type: 'inner_svg', icon: 'Delete' },
      { value: 'Crop', type: 'inner_svg', icon: 'Crop' },
      { value: 'SwitchButton', type: 'inner_svg', icon: 'SwitchButton' },
      { value: 'Operation', type: 'inner_svg', icon: 'Operation' },
      { value: 'Open', type: 'inner_svg', icon: 'Open' },
      { value: 'Remove', type: 'inner_svg', icon: 'Remove' },
      { value: 'ZoomOut', type: 'inner_svg', icon: 'ZoomOut' },
      { value: 'ZoomIn', type: 'inner_svg', icon: 'ZoomIn' },
      { value: 'InfoFilled', type: 'inner_svg', icon: 'InfoFilled' },
      { value: 'CircleCheckFilled', type: 'inner_svg', icon: 'CircleCheckFilled' },
      { value: 'SuccessFilled', type: 'inner_svg', icon: 'SuccessFilled' },
      { value: 'WarningFilled', type: 'inner_svg', icon: 'WarningFilled' },
      { value: 'CircleCloseFilled', type: 'inner_svg', icon: 'CircleCloseFilled' },
      { value: 'QuestionFilled', type: 'inner_svg', icon: 'QuestionFilled' },
      { value: 'WarnTriangleFilled', type: 'inner_svg', icon: 'WarnTriangleFilled' },
      { value: 'UserFilled', type: 'inner_svg', icon: 'UserFilled' },
      { value: 'MoreFilled', type: 'inner_svg', icon: 'MoreFilled' },
      { value: 'Tools', type: 'inner_svg', icon: 'Tools' },
      { value: 'HomeFilled', type: 'inner_svg', icon: 'HomeFilled' },
      { value: 'Menu', type: 'inner_svg', icon: 'Menu' },
      { value: 'UploadFilled', type: 'inner_svg', icon: 'UploadFilled' },
      { value: 'Avatar', type: 'inner_svg', icon: 'Avatar' },
      { value: 'HelpFilled', type: 'inner_svg', icon: 'HelpFilled' },
      { value: 'Share', type: 'inner_svg', icon: 'Share' },
      { value: 'StarFilled', type: 'inner_svg', icon: 'StarFilled' },
      { value: 'Comment', type: 'inner_svg', icon: 'Comment' },
      { value: 'Histogram', type: 'inner_svg', icon: 'Histogram' },
      { value: 'Grid', type: 'inner_svg', icon: 'Grid' },
      { value: 'Promotion', type: 'inner_svg', icon: 'Promotion' },
      { value: 'DeleteFilled', type: 'inner_svg', icon: 'DeleteFilled' },
      { value: 'RemoveFilled', type: 'inner_svg', icon: 'RemoveFilled' },
      { value: 'CirclePlusFilled', type: 'inner_svg', icon: 'CirclePlusFilled' },
      { value: 'ArrowLeft', type: 'inner_svg', icon: 'ArrowLeft' },
      { value: 'ArrowUp', type: 'inner_svg', icon: 'ArrowUp' },
      { value: 'ArrowRight', type: 'inner_svg', icon: 'ArrowRight' },
      { value: 'ArrowDown', type: 'inner_svg', icon: 'ArrowDown' },
      { value: 'ArrowLeftBold', type: 'inner_svg', icon: 'ArrowLeftBold' },
      { value: 'ArrowUpBold', type: 'inner_svg', icon: 'ArrowUpBold' },
      { value: 'ArrowRightBold', type: 'inner_svg', icon: 'ArrowRightBold' },
      { value: 'ArrowDownBold', type: 'inner_svg', icon: 'ArrowDownBold' },
      { value: 'DArrowRight', type: 'inner_svg', icon: 'DArrowRight' },
      { value: 'DArrowLeft', type: 'inner_svg', icon: 'DArrowLeft' },
      { value: 'Download', type: 'inner_svg', icon: 'Download' },
      { value: 'Upload', type: 'inner_svg', icon: 'Upload' },
      { value: 'Top', type: 'inner_svg', icon: 'Top' },
      { value: 'Bottom', type: 'inner_svg', icon: 'Bottom' }
    ]
  }
]
