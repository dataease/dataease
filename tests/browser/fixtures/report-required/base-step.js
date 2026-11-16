import { reportSaveState } from './report-api'
// Wizard boundaries only: the real required-value predicate is tested separately.
export default {
  setup(_, { expose }) {
    expose({
      getFormData: async () =>
        reportSaveState.valid ? { rid: '1', rtid: 0 } : false,
      setOwnSelectHeight() {},
      setDataPermission() {},
    })
    return () => null
  },
}
