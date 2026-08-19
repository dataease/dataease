import { ref } from 'vue'
import { queryFieldValues } from '../../api/data'
import type { FieldItemData, PluginDataConfig } from '../../types/plugin'

export type CustomSortValuesStatus = 'idle' | 'loading' | 'ready' | 'empty' | 'error'

interface CustomSortValuesSource {
  type: () => string | undefined
  data: () => PluginDataConfig | undefined
  field: () => FieldItemData | null | undefined
}

export const useCustomSortValues = (source: CustomSortValuesSource) => {
  const status = ref<CustomSortValuesStatus>('idle')
  const values = ref<string[]>([])
  const error = ref<unknown>()
  let requestVersion = 0

  const invalidate = () => {
    requestVersion += 1
    values.value = []
    error.value = undefined
    status.value = 'idle'
  }

  const load = async () => {
    const type = source.type()
    const data = source.data()
    const field = source.field()
    const version = ++requestVersion

    if (!type || !data || !field) {
      status.value = 'empty'
      values.value = []
      return
    }

    status.value = 'loading'
    values.value = []
    error.value = undefined

    try {
      const result = await queryFieldValues({ type, data, field })
      if (version !== requestVersion) {
        return
      }

      values.value = result
      status.value = result.length > 0 ? 'ready' : 'empty'
    } catch (loadError) {
      if (version !== requestVersion) {
        return
      }

      values.value = []
      error.value = loadError
      status.value = 'error'
    }
  }

  return { status, values, error, load, invalidate }
}
