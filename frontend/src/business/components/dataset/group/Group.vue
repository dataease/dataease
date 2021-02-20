<template xmlns:el-col="http://www.w3.org/1999/html">
  <el-col>
    <span>
      {{ $t('dataset.datalist') }}
    </span>
    <el-divider/>
    <el-row>
      <el-button icon="el-icon-circle-plus" type="primary" size="mini" @click="addGroup">{{$t('dataset.add_group')}}
      </el-button>
      <el-button icon="el-icon-document" type="primary" size="mini" @click="addScene">{{$t('dataset.add_scene')}}
      </el-button>
    </el-row>

    <el-row style="margin: 12px 0">
      <el-input
        size="mini"
        placeholder="Search"
        prefix-icon="el-icon-search"
        v-model="search"
        clearable>
      </el-input>
    </el-row>

    <el-col class="custom-tree-container">
      <div class="block">
        <el-tree
          :data="data"
          node-key="id"
          :expand-on-click-node="true"
          @node-click="nodeClick">
        <span class="custom-tree-node" slot-scope="{ node, data }">
          <span>
            <span v-if="data.show">
              <el-button
                icon="el-icon-document"
                type="text"
                size="mini">
            </el-button>
            </span>
            <span>{{ data.label }}</span>
          </span>
          <span>
            <el-button
              icon="el-icon-plus"
              type="text"
              size="mini"
              @click.stop="() => append(data)">
            </el-button>
            <el-button
              icon="el-icon-more"
              type="text"
              size="mini"
              @click.stop="() => remove(node, data)">
            </el-button>
          </span>
        </span>
        </el-tree>
      </div>
    </el-col>

    <el-dialog :title="$t('dataset.group')" :visible="editGroup" :show-close="false">
      <el-form :model="groupForm" :rules="groupFormRules" ref="groupForm">
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input v-model="groupForm.name"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="close()">{{$t('commons.cancel')}}</el-button>
        <el-button type="primary" @click="saveGroup()">{{$t('commons.confirm')}}</el-button>
      </div>
    </el-dialog>
  </el-col>
</template>

<script>
  let id = 1000;
  export default {
    name: "Group",
    data() {
      const data = [{
        id: 1,
        label: '一级 1',
        children: [{
          id: 4,
          label: '二级 1-1',
          children: [{
            id: 9,
            label: '三级 1-1-1'
          }, {
            id: 10,
            label: '三级 1-1-2'
          }]
        }]
      }, {
        id: 2,
        label: '一级 2',
        children: [{
          id: 5,
          label: '二级 2-1'
        }, {
          id: 6,
          label: '二级 2-2'
        }]
      }, {
        id: 3,
        label: '一级 3',
        children: [{
          id: 7,
          label: '二级 3-1'
        }, {
          id: 8,
          label: '二级 3-2'
        }]
      }];

      return {
        search: '',
        editGroup: false,
        data: JSON.parse(JSON.stringify(data)),
        groupForm: {
          name: '',
          pid: null,
          level: 0
        },
        groupFormRules: {
          name: [
            {required: true, message: this.$t('commons.input_content'), trigger: 'blur'},
          ],
        }
      }
    },
    mounted() {

    },
    activated() {

    },
    methods: {
      addGroup() {
        this.editGroup = true;

      },

      saveGroup() {
        console.log(this.groupForm);
        this.$refs['groupForm'].validate((valid) => {
          if (valid) {
            this.$post("/dataset/group/save", this.groupForm, response => {
              this.close();
              this.$message({
                message: this.$t('commons.save_success'),
                type: 'success',
                showClose: true,
              });
            })
          } else {
            this.$message({
              message: this.$t('commons.input_content'),
              type: 'error',
              showClose: true,
            });
            return false;
          }
        });
      },

      close() {
        this.editGroup = false;
        this.$refs['groupForm'].resetFields();
      },

      addScene() {
        this.$message(
          {
            message: '添加场景',
            type: 'success'
          }
        )
      },

      nodeClick(data, node) {
        console.log(data);
        console.log(node);
      },

      append(data) {
        const newChild = {id: id++, label: 'testtest', children: [], show: true};
        if (!data.children) {
          this.$set(data, 'children', []);
        }
        data.children.push(newChild);
      },

      remove(node, data) {
        const parent = node.parent;
        const children = parent.data.children || parent.data;
        const index = children.findIndex(d => d.id === data.id);
        children.splice(index, 1);
      },
    }
  }
</script>

<style scoped>
  .el-divider--horizontal {
    margin: 12px 0
  }

  .search-input {
    padding: 12px 0;
  }

  .custom-tree-node {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    padding-right: 8px;
  }
</style>
