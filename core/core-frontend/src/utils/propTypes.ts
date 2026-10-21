import { createTypes, toValidableType } from 'vue-types'
import type { CSSProperties } from 'vue'

const ProjectTypes = createTypes({
  func: undefined,
  bool: undefined,
  string: undefined,
  number: undefined,
  object: undefined,
  integer: undefined
})

class propTypes extends ProjectTypes {
  static get style() {
    return toValidableType<string | CSSProperties>('style', {
      type: [String, Object],
      default: undefined
    })
  }
}

export { propTypes }
