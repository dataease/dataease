<template>
  <el-breadcrumb separator-class="el-icon-arrow-right" class="node-breadcrumb">
    <el-breadcrumb-item class="node-breadcrumb">
      <a @click="showAll" >
        <i class="el-icon-s-home"></i>&nbsp;
        {{title}}
      </a>
    </el-breadcrumb-item>

    <el-breadcrumb-item v-for="node in data" :key="node.id">{{node.name}}</el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script>
    export default {
      name: "NodeBreadcrumb",
      data() {
        return {
          data: []
        }
      },
      props: {
        nodes: {
          type: Array
        },
        title: {
          type: String,
          default() {
            return this.$t('test_track.plan_view.all_case')
          }
        }
      },
      watch: {
        nodes() {
          this.filterData();
        }
      },
      methods: {
        showAll() {
          this.$emit('refresh');
        },
        filterData() {
          this.data = this.nodes;
          for (let index in this.data) {
            if (this.data[index].id === 'root') {
              this.data.splice(index, 1);
              break;
            }
          }
          if (this.data.length > 4) {
            let lastData = this.data[this.data.length - 1];
            this.data.splice(1, this.data.length);
            this.data.push({id:lastData.id + '1', name:'...'});
            this.data.push(lastData);
          }
        }
      }
    }
</script>

<style scoped>

  .el-breadcrumb__item {
    margin: 0 auto;
  }

</style>
