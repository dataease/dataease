import Vue from 'vue'

import Picture from '@/components/canvas/customComponent/Picture'
import VText from '@/components/canvas/customComponent/VText'
import VButton from '@/components/canvas/customComponent/VButton'
import Group from '@/components/canvas/customComponent/Group'
import RectShape from '@/components/canvas/customComponent/RectShape'
import UserView from '@/components/canvas/customComponent/UserView'
import DeVideo from '@/components/canvas/customComponent/DeVideo'
import DeFrame from '@/components/canvas/customComponent/DeFrame'
import DeStreamMedia from '@/components/canvas/customComponent/DeStreamMedia'
import DeRichText from '@/components/canvas/customComponent/DeRichText'
Vue.component('DeRichText', DeRichText)
Vue.component('DeStreamMedia', DeStreamMedia)
// eslint-disable-next-line
Vue.component('Picture', Picture)
Vue.component('VText', VText)
Vue.component('VButton', VButton)
Vue.component('Group', Group)
Vue.component('RectShape', RectShape)
Vue.component('UserView', UserView)
Vue.component('DeVideo', DeVideo)
Vue.component('DeFrame', DeFrame)

