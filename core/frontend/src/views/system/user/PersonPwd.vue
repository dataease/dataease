<template>
  <layout-content>
    <div style="width: 100%;display: flex;justify-content: center;">
      <el-card class="box-card about-card">
        <div class="form-header">
          <span>{{ $t('user.change_password') }}</span>
        </div>
        <PasswordUpdateForm />
      </el-card>
    </div>
  </layout-content>
</template>

<script>
import LayoutContent from '@/components/business/LayoutContent'
import PasswordUpdateForm from './PasswordUpdateForm'
export default {
  components: { LayoutContent, PasswordUpdateForm },
  mounted() {
    this.$nextTick(() => {
      this.$store.dispatch('app/toggleSideBarHide', true)
    })
  },
  created() {
    this.$store.dispatch('app/toggleSideBarHide', true)
  },
}
</script>
<style lang="scss" scoped>
.about-card {
  background: inherit;
  margin-top: 5%;
  flex-direction: row;
  width: 640px;
  min-width: 640px;
  height: auto;
  position: relative;
  ::v-deep div.el-card__header {
    padding: 0;
  }
}
.form-header {
    line-height: 60px;
    font-size: 18px;
}
</style>
