import common from './config/common';
import base from './config/base';
import dev from './config/dev';
import lib from './config/lib';
import pages from './config/pages';
import { defineConfig, mergeConfig } from 'vite'

export default defineConfig(({mode}) => {
  if (mode === 'dev') {
    return mergeConfig(common , {...dev, ...pages})
  }

  if (mode === 'lib') {
    return mergeConfig(common , lib)
  }

  return mergeConfig(common, mergeConfig(base, pages))
})