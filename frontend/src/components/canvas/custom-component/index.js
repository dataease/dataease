import Vue from 'vue'

import Picture from '@/components/canvas/custom-component/Picture'
import DeIcon from "@/components/canvas/custom-component/DeIcon"
import VText from '@/components/canvas/custom-component/VText'
import VButton from '@/components/canvas/custom-component/VButton'
import Group from '@/components/canvas/custom-component/Group'
import RectShape from '@/components/canvas/custom-component/RectShape'
import DeBanner from '@/components/canvas/custom-component/DeBanner'
import UserView from '@/components/canvas/custom-component/UserView'
import DeVideo from '@/components/canvas/custom-component/DeVideo'
import DeFrame from '@/components/canvas/custom-component/DeFrame'
import DeStreamMedia from '@/components/canvas/custom-component/DeStreamMedia'

Vue.component('DeStreamMedia', DeStreamMedia)
Vue.component('Picture', Picture)
Vue.component('DeIcon', DeIcon)
Vue.component('VText', VText)
Vue.component('VButton', VButton)
Vue.component('Group', Group)
Vue.component('RectShape', RectShape)
Vue.component('DeBanner', DeBanner)
Vue.component('UserView', UserView)
Vue.component('DeVideo', DeVideo)
Vue.component('DeFrame', DeFrame)

