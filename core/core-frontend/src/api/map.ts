import request from '@/config/axios'
import { FeatureCollection } from '@antv/l7plot/dist/esm/plots/choropleth/types'

export const getWorldTree = (): Promise<IResponse<AreaNode>> => {
  return request.get({ url: '/map/worldTree' })
}

export const getGeoJson = (areaId: string): Promise<IResponse<FeatureCollection>> => {
  let prefix = '/map'
  let areaCode = areaId
  if (isCustomGeo(areaId)) {
    prefix = '/geo'
    areaCode = getBusiGeoCode(areaId)
  }
  const realCountry = areaCode.substring(0, 3)
  const url = `${prefix}/${realCountry}/${areaCode}.json`
  return request.get({ url })
}

const isCustomGeo = (id: string) => {
  return id.startsWith('geo_')
}
const getBusiGeoCode = (id: string) => {
  return id.substring(4)
}
