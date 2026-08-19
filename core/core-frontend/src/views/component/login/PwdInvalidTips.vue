<template>
  <div class="de-v2-head-tips" v-if="expDay > 0 && expDay < 7 && !tipClosed">
    <el-alert
      class="lic_alert"
      :title="t('login.pwd_exp_tips', [expDay])"
      type="warning"
      show-icon
      center
      @close="closeTip"
    />
  </div>
</template>
<script lang="ts" setup>
import { computed, ref } from 'vue'
import { useCache } from '@/hooks/web/useCache'
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()
const { wsCache } = useCache()
const expDay = computed(() => {
  return wsCache.get('pwd-validity-period') || 0
})

const tipClosed = ref(false)

const closeTip = () => {
  tipClosed.value = true
}
</script>

<style lang="less" scoped>
.de-v2-head-tips {
  position: absolute;
  width: 100%;
  top: 0;
  margin: auto;
}
</style>