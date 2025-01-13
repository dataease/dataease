<script lang="ts" setup>
import { toRefs } from 'vue'
import { useCache } from '@/hooks/web/useCache'

const { wsCache } = useCache('localStorage')
const props = defineProps({
  cardInfo: {
    type: Object,
    default() {
      return {
        name: '',
        url: '',
        icon: ''
      }
    }
  }
})
const { cardInfo } = toRefs(props)

const openBlank = () => {
  if (cardInfo.value.url) {
    const openType = wsCache.get('open-backend') === '1' ? '_self' : '_blank'
    window.open(cardInfo.value.url, openType)
  }
}
</script>

<template>
  <div class="doc-card" @click="openBlank">
    <div class="base-show">
      <Icon><component class="svg-icon item-top-icon" :is="cardInfo.icon"></component></Icon>
    </div>
    <div class="base-show show-content">{{ cardInfo.name }}</div>
  </div>
</template>

<style lang="less" scoped>
.doc-card {
  padding: 8px;
  min-width: 96px;
  min-height: 66px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  &:hover,
  &:active {
    background-color: #1f23291a;
    border-radius: 4px;
  }
}

.show-content {
  font-size: 14px;
  color: #1f2329;
  line-height: 22px;
  font-weight: 400;
  margin-top: 4px;
  white-space: nowrap;
  text-align: center;
}

.item-top-icon {
  width: 24px;
  height: 24px;
}
</style>
