import i18n from '@/lang'

export const colorCases = [
  {
    name: i18n.t('chart.color_default'),
    value: 'default',
    colors: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc']
  },
  {
    name: i18n.t('chart.color_retro'),
    value: 'retro',
    colors: ['#0780cf', '#765005', '#fa6d1d', '#0e2c82', '#b6b51f', '#da1f18', '#701866', '#f47a75', '#009db2']
  },
  {
    name: i18n.t('chart.color_elegant'),
    value: 'elegant',
    colors: ['#95a2ff', '#fa8080', '#ffc076', '#fae768', '#87e885', '#3cb9fc', '#73abf5', '#cb9bff', '#434348']
  },
  {
    name: i18n.t('chart.color_future'),
    value: 'future',
    colors: ['#63b2ee', '#76da91', '#f8cb7f', '#f89588', '#7cd6cf', '#9192ab', '#7898e1', '#efa666', '#eddd86']
  },
  {
    name: i18n.t('chart.color_gradual'),
    value: 'gradual',
    colors: ['#71ae46', '#96b744', '#c4cc38', '#ebe12a', '#eab026', '#e3852b', '#d85d2a', '#ce2626', '#ac2026']
  },
  {
    name: i18n.t('chart.color_simple'),
    value: 'simple',
    colors: ['#929fff', '#9de0ff', '#ffa897', '#af87fe', '#7dc3fe', '#bb60b2', '#433e7c', '#f47a75', '#009db2']
  },
  {
    name: i18n.t('chart.color_business'),
    value: 'business',
    colors: ['#194f97', '#555555', '#bd6b08', '#00686b', '#c82d31', '#625ba1', '#898989', '#9c9800', '#007f54']
  },
  {
    name: i18n.t('chart.color_gentle'),
    value: 'gentle',
    colors: ['#5b9bd5', '#ed7d31', '#70ad47', '#ffc000', '#4472c4', '#91d024', '#b235e6', '#02ae75', '#5b9bd5']
  },
  {
    name: i18n.t('chart.color_technology'),
    value: 'technology',
    colors: ['#05f8d6', '#0082fc', '#fdd845', '#22ed7c', '#09b0d3', '#1d27c9', '#f9e264', '#f47a75', '#009db2']
  },
  {
    name: i18n.t('chart.color_light'),
    value: 'light',
    colors: ['#884898', '#808080', '#82ae46', '#00a3af', '#ef8b07', '#007bbb', '#9d775f', '#fae800', '#5f9b3c']
  },
  {
    name: i18n.t('chart.color_classical'),
    value: 'classical',
    colors: ['#007bbb', '#ffdb4f', '#dd4b4b', '#2ca9e1', '#ef8b07', '#4a488e', '#82ae46', '#dd4b4b', '#bb9581']
  },
  {
    name: i18n.t('chart.color_fresh'),
    value: 'fresh',
    colors: ['#5f9b3c', '#75c24b', '#83d65f', '#aacf53', '#c7dc68', '#d8e698', '#e0ebaf', '#bbc8e6', '#e5e5e5']
  },
  {
    name: i18n.t('chart.color_energy'),
    value: 'energy',
    colors: ['#ef8b07', '#2a83a2', '#f07474', '#c55784', '#274a78', '#7058a3', '#0095d9', '#75c24b', '#808080']
  },
  {
    name: i18n.t('chart.color_red'),
    value: 'red',
    colors: ['#ff0000', '#ef8b07', '#4c6cb3', '#f8e944', '#69821b', '#9c5ec3', '#00ccdf', '#f07474', '#bb9581']
  },
  {
    name: i18n.t('chart.color_fast'),
    value: 'fast',
    colors: ['#fae800', '#00c039', '#0482dc', '#bb9581', '#ff7701', '#9c5ec3', '#00ccdf', '#00c039', '#ff7701']
  },
  {
    name: i18n.t('chart.color_spiritual'),
    value: 'spiritual',
    colors: ['#00a3af', '#4da798', '#57baaa', '#62d0bd', '#6ee4d0', '#86e7d6', '#aeede1', '#bde1e6', '#e5e5e5']
  }
]

export const gradientColorCases = [
  {
    name: '渐变色1',
    value: 'gradient1',
    colors: [
      ['rgba(144,202,249,0.5)', 'rgba(1,87,155,0.9)'], 
      ['rgba(127,222,234,1)', 'rgba(0,77,65,1)'], 
      ['rgba(129,199,132,1)', 'rgba(26,94,32,1)'],
      ['rgba(255,213,79,1)', 'rgba(230,81,0,1)'],
      ['rgba(186,105,200,1)', 'rgba(74,20,140,1)'],
      ['rgba(239,83,79,1)', 'rgba(152,10,11,1)']
    ]
  }
]

export const isGradientValue = value => {
  return value && gradientColorCases.some(item => item.value === value)
}