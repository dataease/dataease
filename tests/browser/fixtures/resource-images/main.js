import { createApp, h } from 'vue'
import {
  authenticatedImageUrl,
  waitForResourceImages
} from '@/utils/resourceImages'
import { user, embedded, link, useCache } from './boundary.js'
window.fixture = {
  cache: useCache().wsCache,
  user,
  embedded,
  link,
  authenticatedImageUrl,
  wait: () => waitForResourceImages(document.getElementById('canvas'))
}
createApp({
  render: () =>
    h(
      'div',
      {
        id: 'canvas',
        style: {
          width: '300px',
          height: '200px',
          backgroundImage: `url("${authenticatedImageUrl(
            '/static-resource/background.png'
          )}")`
        }
      },
      [
        h('img', {
          src: authenticatedImageUrl('/static-resource/background.png')
        })
      ]
    )
}).mount('#app')
