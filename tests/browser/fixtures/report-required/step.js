export default {
  setup(_, { expose }) {
    expose({
      getFormData: async () => ({}),
      setOwnSelectHeight() {},
      setDataPermission() {},
    })
    return () => null
  },
}
