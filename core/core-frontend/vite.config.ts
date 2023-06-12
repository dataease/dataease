import common from './config/common';
import base from './config/base';
import dev from './config/dev';
import lib from './config/lib';
import electron from './config/electron';
import pages from './config/pages';
import { defineConfig, mergeConfig } from 'vite'

export default defineConfig(({mode}) => {
  if (mode === 'dev') {
    return mergeConfig(common , {...dev, ...pages})
  }

  if (mode === 'desktop') {
    return mergeConfig(common , electron)
  }

  if (mode === 'lib') {
    return mergeConfig(common , lib)
  }

  return mergeConfig(common, {...base, ...pages})
})