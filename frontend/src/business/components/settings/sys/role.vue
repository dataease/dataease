<template>
    <div style="height: 100%" v-loading="result.loading">
        <el-container style="width: 100%; height: 100%;border: 1px solid #eee">
            <el-aside width="70%" style="border: 1px solid #eee">
                <el-card class="table-card">
                    <template v-slot:header>
                        <ms-table-header :condition.sync="condition" @search="search" @create="create" :create-tip="$t('role.add')" :title="$t('commons.role')"/>
                    </template>
                    <el-table border highlight-current-row class="adjust-table" :data="tableData" style="width: 100%;" @row-click="rowClick">

                        <el-table-column prop="name" label="名称" />
                        <el-table-column :show-overflow-tooltip="true"  prop="createTime" label="创建日期">
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
                    <el-tab-pane label="菜单授权" name="first">
                        <el-tree
                            ref="menu"
                            lazy
                            :data="menus"
                            :default-checked-keys="menuIds"
                            :load="getMenuDatas"
                            :props="defaultProps"
                            check-strictly
                            show-checkbox
                            node-key="id"
                            @check="menuChange"
                        />
                    </el-tab-pane>
                    <el-tab-pane label="数据授权" name="second">玩命开发中...</el-tab-pane>
                </el-tabs>
            </el-main>
        </el-container>
        <el-dialog :close-on-click-modal="false" :title="formType=='add' ? $t('role.add') : $t('role.modify')" :visible.sync="dialogVisible" width="580px" @closed="closeFunc"
               :destroy-on-close="true">
            <el-form ref="roleForm" inline :model="form" :rules="rule" size="small" label-width="80px">

                <el-form-item label="角色名称" prop="name">
                    <el-input v-model="form.name" style="width: 380px;" />
                </el-form-item>

                <el-form-item label="描述信息" prop="description">
                    <el-input v-model="form.description" style="width: 380px;" rows="5" type="textarea" />
                </el-form-item>
            </el-form>

            <template v-slot:footer>
                <ms-dialog-footer
                @cancel="dialogVisible = false"
                @confirm="saveRole('roleForm')"/>
            </template>
        </el-dialog>

    </div>

</template>
<script>
import MsCreateBox from "../CreateBox";
import MsTablePagination from "../../common/pagination/TablePagination";
import MsTableHeader from "../../common/components/MsTableHeader";
import MsTableOperator from "../../common/components/MsTableOperator";
import MsDialogFooter from "../../common/components/MsDialogFooter";
import MsTableOperatorButton from "../../common/components/MsTableOperatorButton";
import {
  listenGoBack,
  removeGoBackListener
} from "@/common/js/utils";
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
        queryPath: '/api/role/roleGrid',
        deletePath: '/api/role/delete/',
        createPath: '/api/role/create',
        updatePath: '/api/role/update',
        queryMenusPath: '/api/menu/childNodes/',
        childMenusPath: '/api/menu/childMenus/',
        saveRoleMenusPath: '/api/role/saveRolesMenus',
        currentPage: 1,
        pageSize: 10,
        total: 0,
        condition: {},
        tableData: [],
        menus: [],
        menuIds: [],
        defaultProps: { children: 'children', label: 'label' ,isLeaf: 'isLeaf'},
        activeName: 'first',
        dialogVisible: false,
        formType: 'add',
        form: {},
        rule: {
            name: [
                { required: true, message: '请输入名称', trigger: 'blur' }
            ]
        },
        currentRow: null
      };
    },
    activated() {
        this.search();
    },
    watch: {
        currentRow: 'currentRowChange'
    },
    methods: {
      handleClick(tab, event) {
        console.log(tab, event);
      },
      create(){
          this.form = {}
          this.formType = "add";
          this.dialogVisible = true
          listenGoBack(this.closeFunc)
      },
      search(){
        this.result = this.$post(this.queryPath+ "/" + this.currentPage + "/" + this.pageSize, this.condition, response => {
            let data = response.data
            this.total = data.itemCount
            this.tableData = data.listObject
        })
      },

      edit(row){
        this.formType = 'modify'
        this.dialogVisible = true
        this.form = Object.assign({}, row)
        listenGoBack(this.closeFunc)
      },

      saveRole(roleForm){
        this.$refs[roleForm].validate(valid => {
            if (valid) {
                const url = this.formType=='add' ? this.createPath : this.updatePath
                this.result = this.$post(url, this.form, () => {
                    this.$success(this.$t('commons.save_success'))
                    this.search();
                    this.dialogVisible = false
                });
            } else {
                return false;
            }
        })
      },

      closeFunc() {
        this.dialogVisible = false
        removeGoBackListener(this.closeFunc);
      },

      getMenuDatas(node, resolve){
          this.$post(this.queryMenusPath+(node.data.id ? node.data.id : 0), null, (res) => {
            const datas = res.data
            const nodes = datas.map(data => this.formatNode(data))
            resolve && resolve(nodes)
        })
      },
      formatNode(node) {
          const result = {
              id: node.menuId,
              label: node.title,
              isLeaf: !node.hasChildren,
              children: node.children
          }
          return result
      },
      menuChange(menu){
          this.$post(this.childMenusPath + menu.id, null, res => {
            const childIds = res.data
            if (this.menuIds.indexOf(menu.id) !== -1) {
                for (let i = 0; i < childIds.length; i++) {
                    const index = this.menuIds.indexOf(childIds[i])
                    if (index !== -1) {
                        this.menuIds.splice(index, 1)
                    }
                }
            } else {
                for (let i = 0; i < childIds.length; i++) {
                    const index = this.menuIds.indexOf(childIds[i])
                    if (index === -1) {
                        this.menuIds.push(childIds[i])
                    }
                }
            }
            console.log(this.menuIds)
            this.$refs.menu.setCheckedKeys(this.menuIds)
            this.saveMenus()
          })
      },
      saveMenus(){
        if (!this.currentRow) {
            return
        }
        const param = {roleId: this.currentRow.roleId, menuIds: this.menuIds}
        this.$post(this.saveRoleMenusPath, param, res => {
            this.search()
        })
      },
      rowClick(row,column, event){
        this.currentRow = row
      },
      currentRowChange(newVal, oldVal){
          if (newVal == oldVal) {
              return
          }
          if (!newVal) {
              this.menuIds = []
              return
          }

          this.menuIds = newVal.menuIds;
          this.$refs.menu.setCheckedKeys(this.menuIds)
      },
      handleDelete(row){
        this.$confirm('确认删除角色['+row.name+']？', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
        }).then(() => {
                const url = this.deletePath+row.roleId
                this.$post(url, null, () => {
                    this.$success(this.$t('commons.modify_success'))
                    this.search()
                });
            }).catch(() => {

            })
      }
    }
}
</script>

<style scoped>


</style>
