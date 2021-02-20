<template>

  <div>
    <ms-chart :options="options" @onClick="onClick">
    </ms-chart>
  </div>

</template>

<script>

import MsChart from "@/business/components/common/chart/MsChart";

export default {
  name: "MsPieChart",
  components: {MsChart},
  mounted() {
    this.getDataNamesByData();
  },
  watch: {
    data() {
      this.getDataNamesByData();
    }
  },
  data() {
    return {
        options: {
          title: {
            text: this.text,
            subtext: this.subtext,
            left: 'center'
          },
          tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>({d}%)'
          },
          legend: {
            orient: 'vertical',
            left: 20,
            data: this.dataNames
          },
          series : [
            {
              name: this.name,
              type: 'pie',
              radius: ['40%', '70%'],
              // roseType: 'angle',
              data: this.data,
              animation: false,
              label: {
                normal: {
                  show: true,
                  position: 'outside',
                  formatter: '{b}:{c}'

                }
              }
            }
          ]
        },
        dataNames: ['示例1','示例2','示例3']
      }
    },
    props: {
      text: {
        type: String,
        default: '图表名称'
      },
      name: {
        type: String,
        default: '数据名称'
      },
      subtext: {
        type: String,
        default: ''
      },
      data: {
        type: Array,
        default() {
          return [
            {
              value:235, name:'示例1',
              itemStyle: {
                color: '#67C23A'
              }
            },
            {
              value:274, name:'示例2',
              itemStyle: {
                color: '#E6A23C'
              }
            },
            {
              value:274, name:'示例3',
              itemStyle: {
                color: '#F56C6C'
              }
            }
          ];
        }
      }
    },
    methods: {
      getDataNamesByData() {
        let itemNames = [];
        this.data.forEach(item => {
          itemNames.push(item.name);
        });
        this.dataNames = itemNames;
      },
      onClick(params){
        this.$emit('onClick', params);
      },
    }
  }
</script>

<style scoped>

  .echarts {
    margin: 0 auto;
  }

</style>
