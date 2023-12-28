<script lang="ts" setup>
import { toRefs } from 'vue'

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
    window.open(cardInfo.value.url)
  }
}
</script>

<template>
  <div class="doc-card" @click="openBlank">
    <el-row class="base-show">
      <Icon class-name="item-top-icon" :name="cardInfo.icon" />
    </el-row>
    <el-row class="base-show show-content"> {{ cardInfo.name }}</el-row>
  </div>
</template>

<style lang="less" scoped>
.doc-card {
  padding-top: 2px;
  margin-top: 16px;
  margin-left: 16px;
  width: 80px;
  height: 50px;
  cursor: pointer;
  &:hover {
    background-color: rgba(30, 39, 56, 0.05);
  }
  &:active {
    background-color: rgba(30, 39, 56, 0.1);
  }
}
.base-show {
  justify-content: center;
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
