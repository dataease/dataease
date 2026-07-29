import { defineStore } from 'pinia'
import { store } from '@/store'
import { FeatureCollection } from '@antv/l7plot/dist/esm/plots/choropleth/types'
import {
  DEFAULT_ONLINE_MAP_CONFIG,
  normalizeOnlineMapConfig,
  type OnlineMapConfig
} from '@/utils/onlineMap'
interface MapStore {
  mapCache: Record<string, FeatureCollection>
  mapKey: OnlineMapConfig
  mapKeyLoaded: boolean
}
export const useMapStore = defineStore('map', {
  state: (): MapStore => ({
    mapCache: {},
    mapKey: { ...DEFAULT_ONLINE_MAP_CONFIG },
    mapKeyLoaded: false
  }),
  actions: {
    setMap({ id, geoJson }) {
      this.mapCache[id] = geoJson
    },
    setKey(key) {
      this.mapKey = normalizeOnlineMapConfig(key)
      this.mapKeyLoaded = true
    }
  }
})

export const useMapStoreWithOut = () => useMapStore(store)
