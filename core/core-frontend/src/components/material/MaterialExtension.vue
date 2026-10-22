<script setup lang="ts">
import { defineAsyncComponent, defineComponent, h, shallowRef } from 'vue'
import { ElButton } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'

const createExtension = () =>
  defineAsyncComponent({
    loader: () => import('virtual:dataease-material'),
    loadingComponent: defineComponent({
      inheritAttrs: false,
      setup() {
        const { t } = useI18n()
        return () =>
          h('div', { class: 'material-extension-state', role: 'status' }, t('material.loading'))
      }
    }),
    errorComponent: defineComponent({
      inheritAttrs: false,
      setup() {
        const { t } = useI18n()
        return () =>
          h('div', { class: 'material-extension-state', role: 'alert' }, [
            h('span', t('material.load_failed')),
            h(
              ElButton,
              {
                text: true,
                type: 'primary',
                onClick: () => {
                  Extension.value = createExtension()
                }
              },
              () => t('material.retry')
            )
          ])
      }
    })
  })
const Extension = shallowRef(createExtension())
</script>

<template>
  <Extension v-bind="$attrs" />
</template>

<style scoped>
.material-extension-state {
  padding: 32px;
  color: var(--ed-text-color-secondary);
  text-align: center;
}
</style>
