import { getCurrentInstance, inject, onBeforeUnmount, provide, type InjectionKey } from 'vue'
import { createQueryScope, type QueryScope } from '@/config/axios/queryScope'

const queryScopeKey: InjectionKey<QueryScope> = Symbol('queryScope')

export const useQueryScope = () => inject(queryScopeKey, undefined)

export const provideQueryScope = (inherit = false) => {
  const parent = useQueryScope()
  if (inherit && parent) return parent
  const scope = createQueryScope()
  provide(queryScopeKey, scope)
  onBeforeUnmount(scope.dispose)
  return scope
}

export const useLatestQuery = (purpose: string) => {
  const inherited = inject(queryScopeKey, undefined)
  const scope = inherited || createQueryScope()
  const instance = getCurrentInstance()
  const query = scope.createQuery(purpose, instance ? `vue-${instance.uid}` : undefined)
  onBeforeUnmount(() => {
    query.dispose()
    if (!inherited) scope.dispose()
  })
  return query
}
