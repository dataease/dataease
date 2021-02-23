<template>
    <div style="height: 100%" v-loading="result.loading">
        <el-container style="width: 100%; height: 100%;border: 1px solid #eee">
            <el-aside width="30%" style="border: 1px solid #eee">
                <el-card class="table-card">
                    <template v-slot:header>
                        <ms-table-header :condition.sync="condition" @search="search" @create="create" :create-tip="$t('user.create')" :title="$t('commons.user')"/>                                        
                    </template>
                    <el-table border class="adjust-table" :data="tableData" style="width: 100%">
                        
                        <el-table-column prop="name" label="名称" />
                        <el-table-column :show-overflow-tooltip="true" width="135px" prop="createTime" label="创建日期">
                            <template v-slot:default="scope">
                                <span>{{ scope.row.createTime | timestampFormatDate }}</span>
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('commons.operating')">
                            <template v-slot:default="scope">
                                <ms-table-operator @editClick="edit(scope.row)" @deleteClick="handleDelete(scope.row)"/>
                            </template>
                        </el-table-column>
                    </el-table>
                    <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize" :total="total"/>
                           
                </el-card>
            </el-aside>
            <el-main style="">
                <el-tabs v-model="activeName" @tab-click="handleClick">
                    <el-tab-pane label="菜单飞配" name="first">菜单飞配</el-tab-pane>
                    <el-tab-pane label="数据分配" name="second">数据分配</el-tab-pane>                
                </el-tabs>
            </el-main>
        </el-container>
    </div>    
    
</template>
<script>
import MsCreateBox from "../CreateBox";
import MsTablePagination from "../../common/pagination/TablePagination";
import MsTableHeader from "../../common/components/MsTableHeader";
import MsTableOperator from "../../common/components/MsTableOperator";
import MsDialogFooter from "../../common/components/MsDialogFooter";
import MsTableOperatorButton from "../../common/components/MsTableOperatorButton";

export default {
    name: 'role',
    components: {
        MsCreateBox,
        MsTablePagination,
        MsTableHeader,
        MsTableOperator,
        MsDialogFooter,
        MsTableOperatorButton
    },
    data() {
      return {
        result: {},
        queryPath: '/user/special/list',
        deletePath: '/user/special/delete/',
        createPath: '/user/special/add',
        updatePath: '/user/special/update',
        currentPage: 1,
        pageSize: 10,
        total: 0,
        condition: {},
        tableData: [],
        activeName: 'second'
      };
    },
    methods: {
      handleClick(tab, event) {
        console.log(tab, event);
      },
      search(){
        this.result = this.$post(this.queryPath, this.condition, response => {
            let data = response.data;
            this.total = data.itemCount;
            this.tableData = data.listObject;
            
        })
      },
      edit(row){

      },
      handleDelete(row){

      }
    }
}
</script>

<style scoped>

 
</style>
