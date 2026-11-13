export const useI18n = () => ({
  t: (key, values) => (values ? `${key}: ${values.join(',')}` : key),
})
export const Icon = { template: '<span><slot /></span>' }
export const deepCopy = (value) =>
  structuredClone(JSON.parse(JSON.stringify(value)))
export default {
  render() {
    return null
  },
}
