<script lang="ts" setup>
defineProps({
  cardInfo: {
    type: Object,
    default() {
      return {
        name: '',
        icon: ''
      }
    }
  }
})
const emits = defineEmits(['openBlank'])
const openBlank = () => {
  emits('openBlank')
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
  padding: 8px 0;
  width: 96px;
  height: 66px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  &:hover,
  &:active {
    background-color: #1f23291a;
    border-radius: 6px;
  }
}

.show-content {
  font-size: 14px;
  color: #1f2329;
  line-height: 22px;
  font-weight: 400;
  margin-top: 4px;
}

.item-top-icon {
  width: 24px;
  height: 24px;
}
</style>
