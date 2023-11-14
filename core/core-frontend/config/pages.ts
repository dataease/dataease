import path from 'path'
import { PAGES, ROOT_DIR } from './pagesConfig'
export default {
  build: {
    rollupOptions: {
      // 多页支持
      input: PAGES.reduce((map, { name }) => {
        map[name] = path.resolve(ROOT_DIR, `${name}.html`)
        return map
      }, {})
    }
  }
}
