<template>
  <div>
    <el-table
      v-for="(table, index) in tables"
      :key="index"
      :data="table.tableData"
      border
      size="mini"
      highlight-current-row>
      <el-table-column v-for="(title, index) in table.titles" :key="index" :label="title" min-width="150px">
        <template v-slot:default="scope">
          <el-popover
            placement="top"
            trigger="click">
            <el-container>
              <div>{{ scope.row[title] }}</div>
            </el-container>
            <span class="table-content" slot="reference">{{ scope.row[title] }}</span>
          </el-popover>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
    export default {
      name: "MsSqlResultTable",
      data() {
        return {
          tables: [],
          titles: []
        }
      },
      props: {
        body: String
      },
      created() {
        if (!this.body) {
          return;
        }
        let rowArry = this.body.split("\n");
        this.getTableData(rowArry);
        if (this.tables.length > 1) {
          for (let i = 0; i < this.tables.length; i++) {
            if (this.tables[i].titles.length === 1 && i < this.tables.length - 1) {
              this.tables[i].tableData.splice(this.tables[i].tableData.length - 1, 1);
            }
          }

          let lastTable = this.tables[this.tables.length - 1];
          if (lastTable.titles.length === 1) {
            if (lastTable.tableData.length > 4) {
              lastTable.tableData.splice(lastTable.tableData.length - 4, 4);
            } else {
              this.tables.splice(this.tables.length - 1, 1);
            }
          } else {
            this.tables.splice(this.tables.length - 1, 1);
          }
        } else {
          let table = this.tables[0];
          table.tableData.splice(table.tableData.length - 4, 4);
        }
      },
      methods: {
        getTableData(rowArry) {
          let titles;
          let result = [];
          for (let i = 0; i < rowArry.length; i++) {
            let colArray = rowArry[i].split("\t");
            if (i === 0) {
              titles = colArray;
            } else {
              if (colArray.length != titles.length) {
                // 创建新的表
                if (colArray.length === 1 && colArray[0] === '') {
                  this.getTableData(rowArry.slice(i + 1));
                } else {
                  this.getTableData(rowArry.slice(i));
                }
                break;
              } else {
                let item = {};
                for (let j = 0; j < colArray.length; j++) {
                  item[titles[j]] = (colArray[j] ? colArray[j] : "");
                }
                result.push(item);
              }
            }
          }

          this.tables.splice(0, 0, {
            titles: titles,
            tableData: result
          });
        }
      }
    }
</script>

<style scoped>

  .el-table {
    margin-bottom: 20px;
  }

  .el-table >>> .cell {
    white-space: nowrap;
  }

  .table-content {
    cursor: pointer;
  }

  .el-container {
    overflow:auto;
    max-height: 500px;
  }

</style>
