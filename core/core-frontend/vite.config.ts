import {
  lazyRouteDevDependencies,
  observedDevDependencies,
  getElementPlusStyleDependencies
} from './config/dev-dependencies'
import common from './config/common'
import base from './config/base'
import distributed from './config/distributed'
import dev from './config/dev'
import pages from './config/pages'
import { defineConfig, mergeConfig } from 'vite'

export default defineConfig(({ mode }) => {
  if (mode === 'dev') {
    return mergeConfig(mergeConfig(common, mergeConfig(dev, pages)), {
      optimizeDeps: {
        include: [
          ...getElementPlusStyleDependencies(),
          ...lazyRouteDevDependencies,
          ...observedDevDependencies
        ]
      }
    })
  }

  if (mode === 'distributed') {
    return mergeConfig(common, mergeConfig(distributed, pages))
  }

  return mergeConfig(common, mergeConfig(base, pages))
})
