import { defineStore } from 'pinia'
import { store } from '@/store'
import { FeatureCollection } from '@antv/l7plot/dist/esm/plots/choropleth/types'
interface MapStore {
  mapCache: Record<string, FeatureCollection>
  mapKey: {
    key: string
    securityCode: string
    mapType: string
  }
}
export const useMapStore = defineStore('map', {
  state: (): MapStore => ({
    mapCache: {},
    mapKey: {
      key: '',
      securityCode: '',
      mapType: ''
    }
  }),
  actions: {
    setMap({ id, geoJson }) {
      this.mapCache[id] = geoJson
    },
    setKey(key) {
      this.mapKey = key
    }
  }
})

export const useMapStoreWithOut = () => useMapStore(store)
