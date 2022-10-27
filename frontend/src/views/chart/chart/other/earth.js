
export function base3dEarthOption(chart_option) {
    console.log('地图？',chart_option)
    let ROOT_PATH = 'https://echarts.apache.org/examples'

    chart_option.globe.baseTexture = ROOT_PATH + '/data-gl/asset/world.topo.bathy.200401.jpg'
    chart_option.globe.heightTexture = ROOT_PATH + '/data-gl/asset/world.topo.bathy.200401.jpg'
    chart_option.globe.environment = ROOT_PATH + '/data-gl/asset/starfield.jpg'
    chart_option.globe.light.ambientCubemap.texture = ROOT_PATH + '/data-gl/asset/pisa.hdr'
    
    return chart_option
}