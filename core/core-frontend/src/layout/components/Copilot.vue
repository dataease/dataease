<script lang="ts" setup>
import { onMounted, ref } from 'vue'
const visible = ref(true)
const emits = defineEmits(['confirm'])

const confirm = () => {
  emits('confirm')
}

onMounted(() => {
  // do
})
</script>
<template>
  <el-popover
    :visible="visible"
    placement="bottom"
    popper-class="copilot-popper-tips"
    :width="288"
    show-arrow
  >
    <div class="copilot-popper-tips-content">
      <p class="title">Copilot 对话分析</p>
      <p class="constant">
        你好，我是 Copilot 对话分析
        <br />点击一下，开启可视化图表解答模式～<br />&nbsp;
      </p>
      <div class="bottom">
        <el-button size="middle" @click="confirm"> 我知道了 </el-button>
      </div>
    </div>
    <template #reference>
      <div class="copilot-popper-tips-icon">
        <el-icon style="margin: 2px" class="ai-icon">
          <Icon name="copilot" />
        </el-icon>
      </div>
    </template>
  </el-popover>
</template>

<style lang="less">
.copilot-popper-tips {
  z-index: 10001 !important;
  padding: 24px !important;
  box-shadow: none !important;
  border: 0px !important;
  background: var(--ed-color-primary) !important;
  .ed-popper__arrow::before {
    border: 1px solid var(--ed-color-primary) !important;
    background: var(--ed-color-primary) !important;
  }
}
.copilot-popper-tips-content {
  color: rgba(255, 255, 255, 1);
  .title {
    font-family: PingFang SC;
    font-size: 20px;
    font-weight: 500;
    line-height: 28px;
  }
  .content {
    font-family: PingFang SC;
    font-size: 14px;
    font-weight: 500;
    line-height: 22px;
    text-align: left;
  }
  .bottom {
    line-height: 22px;
    text-align: right;
    button {
      border: 0px !important;
      border-color: #ffffff !important;
      font-weight: 500;
      color: rgba(51, 112, 255, 1) !important;
    }
  }
}

.copilot-popper-tips-icon {
  margin: 0 8px;
  z-index: 10003;
  border-radius: 50%;
  background: #ffffff;
  width: 28px;
  height: 28px;
}
</style>
