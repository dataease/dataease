import common from './config/common';
import base from './config/base';
import dev from './config/dev';
import electron from './config/electron';
import { defineConfig, mergeConfig } from 'vite'

export default defineConfig(({mode}) => {
  if (mode === 'dev') {
    return mergeConfig(common , dev)
  }

  if (mode === 'desktop') {
    return mergeConfig(common , electron)
  }

  return mergeConfig(common, base)
})