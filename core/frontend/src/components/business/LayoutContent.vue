<template>
  <div class="content-container">
    <div
      v-if="$slots.header || header"
      class="content-container__header"
    >
      <slot name="header">
        <back-button
          v-if="showBack"
          :path="backPath"
          :name="backName"
          :to="backTo"
        />
        {{ header }}
      </slot>
    </div>
    <div
      v-if="$slots.toolbar"
      class="content-container__toolbar"
    >
      <slot name="toolbar" />
    </div>
    <slot />
  </div>
</template>

<script>
import BackButton from '@/components/backButton'

export default {
  name: 'LayoutContent',
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
    showBack({ backPath, backName, backTo }) {
      return backPath || backName || backTo
    }
  }
}
</script>

<style lang="scss">
@import "~@/styles/mixin.scss";

.content-container {
  transition: 0.3s;
  background-color: var(--ContentBG);
  overflow: auto;
  height: 100%;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 1px 4px 0 rgba(0, 0, 0, 14%);
  box-sizing: border-box;

  .content-container__header {
    line-height: 60px;
    font-size: 18px;
  }

  .content-container__toolbar {
    @include flex-row(space-between, center);
    margin-bottom: 10px;
  }
}
</style>
