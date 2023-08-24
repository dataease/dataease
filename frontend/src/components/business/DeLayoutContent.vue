<template>
  <div class="layout-container">
    <p class="route-title">
      <back-button
        v-if="showBack"
        :path="backPath"
        :name="backName"
        :to="backTo"
      />
      <span>{{ routeTitle }}</span>
    </p>
    <div
      class="container-wrapper"
      :class="[needInnerPadding ? 'layout-inner-padding' : '']"
    >
      <slot />
    </div>
  </div>
</template>

<script>
import BackButton from '@/components/backButton'

export default {
  name: 'DeLayoutContent',
  components: { BackButton },
  props: {
    // eslint-disable-next-line vue/require-default-prop
    header: String,
    // eslint-disable-next-line vue/require-default-prop
    backPath: String,
    // eslint-disable-next-line vue/require-default-prop
    backName: String,
    // eslint-disable-next-line vue/require-default-prop
    backTo: Object
  },
  computed: {
    routeTitle() {
      return this.header || this.$route.meta?.title || ''
    },
    showBack({ backPath, backName, backTo }) {
      return backPath || backName || backTo
    },
    needInnerPadding() {
      return ['system-app-template', 'sys-identification', 'sys-abutment', 'sys-task-email', 'system-dept', 'system-dept-form', 'system-auth', 'sys-appearance', 'system-param', 'system-template', 'sys-task-dataset', 'sys-msg-web-all', 'system-plugin'].includes(this.$route.name)
    }
  }
}
</script>

<style lang="scss" scoped>
.layout-container {
  transition: 0.3s;
  background-color: var(--ContentBG);
  overflow: auto;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 1px 4px 0 rgba(0, 0, 0, 14%);
  box-sizing: border-box;
  background-color: var(--MainBG, #f5f6f7);
  overflow: hidden;
  padding: 24px 24px 24px 24px;
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;

  .route-title {
    font-family: PingFang SC;
    font-size: 20px;
    font-weight: 500;
    line-height: 28px;
    text-align: left;
    color: var(--TextPrimary, #1F2329);
    width: 100%;
    margin: 0;
  }

  .container-wrapper {
    width: 100%;
    overflow: auto;
    background-color: var(--ContentBG, #FFFFFF);
    margin-top: 24px;
    padding: 24px;
    flex: 1;
  }

  .layout-inner-padding {
    padding: 0;
    margin-top: 16px;
    overflow: hidden;
  }
}
</style>
