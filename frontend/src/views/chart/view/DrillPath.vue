<template>
  <div v-if="drillFilters && drillFilters.length > 0">
    <el-breadcrumb separator-class="el-icon-arrow-right" class="drill-style">
      <el-breadcrumb-item class="drill-item" @click.native="drillJump(0)">{{ $t('commons.all') }}</el-breadcrumb-item>
      <el-breadcrumb-item v-for="(filter,index) in drillFilters" :key="index" class="drill-item" @click.native="drillJump(index + 1)">{{ filter.value[0] }}</el-breadcrumb-item>
    </el-breadcrumb>
  </div>
</template>

<script>
export default {
  name: 'DrillPath',
  props: {
    drillFilters: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
    }
  },
  watch: {
  },
  mounted() {
  },
  methods: {
    drillJump(index) {
      if (index < this.drillFilters.length) {
        this.$emit('onDrillJump', index)
      }
    }
  }
}
</script>

<style scoped>
.drill-style {
  font-size: 12px;
}
.drill-style >>> .el-breadcrumb__separator{
  margin: 0!important;
}
.drill-item{
  cursor: pointer;
}
</style>
