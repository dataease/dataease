<template>
  <el-drawer
    v-closePress
    v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
    :title="$t('app_template.apply_template')"
    :visible.sync="applyDrawer"
    custom-class="de-user-drawer"
    size="600px"
    :wrapper-closable="false"
    direction="rtl"
  >
    <ds-form
      v-if="applyDrawer"
      :reference-position="'appMarket'"
      :outer-params="outerParams"
      @closeDraw="close"
    />
  </el-drawer>
</template>

<script>
import DsForm from '@/views/system/datasource/DsForm'

export default {
  name: 'AppTemplateApply',
  components: {
    DsForm
  },
  data() {
    return {
      outerParams: {},
      applyDrawer: false
    }
  },
  computed: {},
  mounted() {
  },
  methods: {
    search() {
      this.applyDrawer = false
      this.$emit('search', this.formatCondition(), this.formatText())
    },
    init(params) {
      this.applyDrawer = true
      this.outerParams = params
    },
    close() {
      this.$emit('closeDraw')
      this.applyDrawer = false
    }
  }
}
</script>

<style scoped>
::v-deep .el-drawer__body {
  padding: 0px 0px !important;
}
</style>
