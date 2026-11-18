import { reportSaveState } from './report-api'
export default {
  setup(_, { expose }) {
    expose({
      getFormData: async () => {
        if (reportSaveState.stepThrows)
          throw new Error('Synthetic validation failure')
        return reportSaveState.stepValid ? {} : false
      },
      setOwnSelectHeight() {},
      setDataPermission() {},
    })
    return () => null
  },
}
