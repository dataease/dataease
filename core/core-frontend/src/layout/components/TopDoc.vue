<script lang="ts" setup>
import { ref } from 'vue'
import { Icon } from '@/components/icon-custom'
const helpLink = ref(null)
</script>

<template>
  <div class="right-menu-item hover-effect doc-container">
    <a :href="helpLink" target="_blank">
      <Icon class="doc-icon" name="docs" />
    </a>
  </div>
</template>

<style lang="less">
.right-menu-item {
  display: inline-block;
  padding: 10px 8px;
  height: 100%;
  font-size: 18px;

  vertical-align: text-bottom;

  &.hover-effect {
    cursor: pointer;
    transition: background 0.3s;

    &:hover {
      background-color: rgba(0, 0, 0, 0.025);
    }
  }
}
.doc-container {
  height: 100%;
  padding: 0 8px;
  a {
    display: flex;
    height: 100%;
    width: 100%;
    justify-content: center;
    align-items: center;
  }
}
.doc-icon {
  width: 18px;
  height: 18px;
}
</style>
