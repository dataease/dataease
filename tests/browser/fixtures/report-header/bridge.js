export const useI18n = () => ({ t: (key) => key })
export const Icon = { template: '<span><slot /></span>' }
export const pixelOptions = []
export const baseRules = {}
export const queryTreeApi = async () => [
  { id: 'dv', leaf: true, extraFlag1: true, label: '合成仪表板' },
]
export const findById = async () => ({ data: {} })
export const viewOptionApi = async () => ({
  data: [{ id: 'view', title: '合成图表', type: 'bar' }],
})
export default { render: () => null }
