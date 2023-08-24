<template>
  <de-layout-content>
    <app-template-content :show-position="'system-setting'" />
  </de-layout-content>
</template>

<script>
import AppTemplateContent from '@/views/panel/appTemplate/AppTemplateContent'
import DeLayoutContent from '@/components/business/DeLayoutContent'
export default {
  name: 'AppTemplate',
  components: { DeLayoutContent, AppTemplateContent }
}
</script>

