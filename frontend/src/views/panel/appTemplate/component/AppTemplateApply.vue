<template>
  <el-drawer
    v-closePress
    :title="'应用模板'"
    :visible.sync="applyDrawer"
    custom-class="de-user-drawer"
    size="600px"
    direction="rtl"
  >
    <ds-form v-if="applyDrawer" :reference-position="'appMarket'" :outer-params="outerParams" @closeDraw="close"></ds-form>
  </el-drawer>
</template>

<script>
import DeDatePick from '@/components/deCustomCm/deDatePick.vue'
import DsForm from '@/views/system/datasource/DsForm'
export default {
  components: {
    DsForm,
    DeDatePick
  },
  name: 'AppTemplateApply',
  data() {
    return {
      outerParams: {},
      applyDrawer: false
    }
  },
  computed: {
  },
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
      this.$emit("closeDraw")
      this.applyDrawer = false
    }
  }
}
</script>

<style scoped>
::v-deep .el-drawer__body{
  padding: 0px 0px!important;
}
</style>
