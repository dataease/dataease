import request from '@/config/axios'
import { FeatureCollection } from '@antv/l7plot/dist/esm/plots/choropleth/types'

export const getWorldTree = (): Promise<IResponse<AreaNode>> => {
  return request.get({ url: '/map/worldTree' })
}

export const getGeoJson = (
  country: string,
  areaId: string
): Promise<IResponse<FeatureCollection>> => {
  return request.get({ url: `/map/${country}/${areaId}.json` })
}
