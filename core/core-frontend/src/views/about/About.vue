<script lang="ts" setup>
import { ref } from 'vue'
import aboutBg from '@/assets/img/about-bg.png'

const dialogVisible = ref(true)
</script>

<template>
  <el-dialog title="关于" width="840px" v-model="dialogVisible" class="about-dialog">
    <img width="792" height="180" :src="aboutBg" />
    <el-icon class="logo">
      <icon name="logo"></icon>
    </el-icon>
    <div class="content">
      <div class="item">
        <div class="label">授权给</div>
        <div class="value">授权给</div>
      </div>
      <div class="item">
        <div class="label">代理商</div>
        <div class="value">代理商</div>
      </div>
      <div class="item">
        <div class="label">过期时间</div>
        <div class="value">过期时间</div>
      </div>
      <div class="item">
        <div class="label">授权数量</div>
        <div class="value">授权数量</div>
      </div>
      <div class="item">
        <div class="label">版本</div>
        <div class="value">版本</div>
      </div>
      <div class="item">
        <div class="label">版本号</div>
        <div class="value">版本号</div>
      </div>
      <div class="item">
        <div class="label">序列号</div>
        <div class="value">序列号</div>
      </div>
      <div class="item">
        <div class="label">备注</div>
        <div class="value ellipsis">备注</div>
      </div>

      <div style="margin-top: 24px">
        <el-button plain> 更新 License </el-button>
        <el-button plain> 获取技术支持 </el-button>
      </div>
    </div>
  </el-dialog>
</template>

<style lang="less">
.about-dialog {
  img {
    border-radius: 4px;
    border-bottom-left-radius: 0;
    border-bottom-right-radius: 0;
  }

  .logo {
    font-size: 400px;
    position: absolute;
    top: -40px;
    left: 228px;
  }

  .content {
    border-radius: 4px;
    border: 1px solid #dee0e3;
    border-top-left-radius: 0;
    border-top-right-radius: 0;
    padding: 24px 40px;
    margin-top: -7px;

    .item {
      font-family: PingFang SC;
      font-size: 16px;
      font-style: normal;
      font-weight: 400;
      line-height: 24px;
      margin-bottom: 16px;
      display: flex;
      .label {
        color: #646a73;
        width: 300px;
      }

      .value {
        margin-left: 24px;
        max-width: 388px;
      }
    }
  }
}
</style>
