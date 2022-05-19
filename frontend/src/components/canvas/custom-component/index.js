import Vue from 'vue'

import Picture from '@/components/canvas/custom-component/Picture'
import VText from '@/components/canvas/custom-component/VText'
import VButton from '@/components/canvas/custom-component/VButton'
import Group from '@/components/canvas/custom-component/Group'
import RectShape from '@/components/canvas/custom-component/RectShape'
import UserView from '@/components/canvas/custom-component/UserView'
import DeVideo from '@/components/canvas/custom-component/DeVideo'
import DeFrame from '@/components/canvas/custom-component/DeFrame'
import DeStreamMedia from '@/components/canvas/custom-component/DeStreamMedia'
import DeRichText from '@/components/canvas/custom-component/DeRichText'
Vue.component('DeRichText', DeRichText)
Vue.component('DeStreamMedia', DeStreamMedia)
Vue.component('Picture', Picture)
Vue.component('VText', VText)
Vue.component('VButton', VButton)
Vue.component('Group', Group)
Vue.component('RectShape', RectShape)
Vue.component('UserView', UserView)
Vue.component('DeVideo', DeVideo)
Vue.component('DeFrame', DeFrame)

