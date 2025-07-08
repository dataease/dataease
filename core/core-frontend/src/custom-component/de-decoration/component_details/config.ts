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
  DeBoard10: DeBoard10
}

export const findDecoration = name => {
  return boardInfoMap[name]
}
