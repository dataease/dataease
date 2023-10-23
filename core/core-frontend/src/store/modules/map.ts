import { defineStore } from 'pinia'
import { store } from '@/store'
import { FeatureCollection } from '@antv/l7plot/dist/esm/plots/choropleth/types'
interface MapStore {
  mapCache: Record<string, FeatureCollection>
}
export const useMapStore = defineStore('map', {
  state: (): MapStore => ({
    mapCache: {}
  }),
  actions: {
    setMap({ id, geoJson }) {
      this.mapCache[id] = geoJson
    }
  }
})

export const useMapStoreWithOut = () => useMapStore(store)
