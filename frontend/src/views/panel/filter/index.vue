<template>

  <div>
    <el-card class="filter-card-class">
      <div slot="header">
        <span>卡片名称</span>
      </div>
      <div style="position: relative;margin-left: 5px;">
        <div v-for="o in 4" :key="o" class="item">
          <div draggable>
            <el-button style="width: 90px;" size="mini" type="primary" icon="el-icon-edit" plain> {{ '条件 ' + o }}</el-button>
          </div>
        </div>
      </div>

    </el-card>
  </div>

</template>

<script>
export default {
  name: 'FilterGroup',
  data() {
    return {

    }
  },
  methods: {

  }
}
</script>

<style lang="scss" scoped>

  .item {
    font-size: 12px;
    width: 100px;
    height: 36px;
    position: relative;
    float: left;
    margin-top: 10px;
    margin-left: 10px;
  }

  .clearfix:before,
  .clearfix:after {
    display: table;
    content: "";
  }
  .clearfix:after {
    clear: both
  }

  .box-card {
    width: 480px;
  }
</style>
