<template>
<el-col>
  <!-- group -->
  <el-col v-if="!sceneMode">
    <el-row class="title-css">
      <span class="title-text">
        {{ $t('dataset.datalist') }}
      </span>
    </el-row>
    <el-divider/>

    <el-row>
      <el-form>
        <el-form-item class="form-item">
          <el-input
            size="mini"
            :placeholder="$t('dataset.search')"
            prefix-icon="el-icon-search"
            v-model="search"
            clearable>
          </el-input>
        </el-form-item>
      </el-form>
    </el-row>

    <el-col class="custom-tree-container">
      <div class="block">
        <el-tree
          :default-expanded-keys="expandedArray"
          :data="data"
          node-key="id"
          :expand-on-click-node="true"
          @node-click="nodeClick">
        <span class="custom-tree-node" slot-scope="{ node, data }">
          <span>
            <span v-if="data.type === 'scene'">
              <el-button
                icon="el-icon-folder"
                type="text"
                size="mini">
              </el-button>
            </span>
            <span style="margin-left: 6px">{{ data.name }}</span>
          </span>
        </span>
        </el-tree>
      </div>
    </el-col>
  </el-col>

  <!--scene-->
  <el-col v-if="sceneMode">
    <el-row class="title-css">
      <span class="title-text">
        {{currGroup.name}}
      </span>
      <el-button icon="el-icon-back" size="mini" @click="back" style="float: right">
        {{$t('dataset.back')}}
      </el-button>
    </el-row>
    <el-divider/>
    <el-row>
      <el-form>
        <el-form-item class="form-item">
          <el-input
            size="mini"
            :placeholder="$t('dataset.search')"
            prefix-icon="el-icon-search"
            v-model="search"
            clearable>
          </el-input>
        </el-form-item>
      </el-form>
    </el-row>
    <el-tree
      :data="tableData"
      node-key="id"
      :expand-on-click-node="true"
      @node-click="sceneClick">
        <span class="custom-tree-node" slot-scope="{ node, data }">
          <span>
            <span>
              ({{data.type}})
            </span>
            <span>
              <span style="margin-left: 6px" v-if="data.mode === 0"><i class="el-icon-s-operation"></i></span>
              <span style="margin-left: 6px" v-if="data.mode === 1"><i class="el-icon-time"></i></span>
            </span>
            <span style="margin-left: 6px">{{ data.name }}</span>
          </span>
        </span>
    </el-tree>
  </el-col>
</el-col>
</template>

<script>
export default {
  name: "DatasetGroupSelector",
  data() {
    return {
      sceneMode: false,
      search: '',
      data: [],
      tableData: [],
      currGroup: null,
      expandedArray: [],
      groupForm: {
        name: '',
        pid: null,
        level: 0,
        type: '',
        children: [],
        sort: 'type desc,name asc'
      },
      tableForm: {
        name: '',
        sort: 'type asc,create_time desc,name asc'
      },
    }
  },
  computed: {},
  mounted() {
    this.tree(this.groupForm);
    this.tableTree();
  },
  activated() {
    this.tree(this.groupForm);
    this.tableTree();
  },
  watch: {
    // search(val){
    //   this.groupForm.name = val;
    //   this.tree(this.groupForm);
    // }
  },
  methods: {
    close() {
      this.editGroup = false;
      this.groupForm = {
        name: '',
        pid: null,
        level: 0,
        type: '',
        children: [],
        sort: 'type desc,name asc'
      }
    },

    closeTable() {
      this.editTable = false;
      this.tableForm = {
        name: '',
      }
    },

    tree(group) {
      this.$post("/dataset/group/tree", group, response => {
        this.data = response.data;
      })
    },

    tableTree() {
      this.tableData = [];
      if (this.currGroup) {
        this.$post('/dataset/table/list', {
          sort: 'type asc,create_time desc,name asc',
          sceneId: this.currGroup.id
        }, response => {
          this.tableData = response.data;
        });
      }
    },

    nodeClick(data, node) {
      // console.log(data);
      // console.log(node);
      if (data.type === 'scene') {
        this.sceneMode = true;
        this.currGroup = data;
        this.tableTree();
      }
      if (node.expanded) {
        this.expandedArray.push(data.id);
      } else {
        let index = this.expandedArray.indexOf(data.id);
        if (index > -1) {
          this.expandedArray.splice(index, 1);
        }
      }
      // console.log(this.expandedArray);
    },

    back() {
      this.sceneMode = false;
    },

    sceneClick(data, node) {
      // console.log(data);
      this.$emit("getTable", data);
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

  .custom-position {
    flex: 1;
    display: flex;
    align-items: center;
    font-size: 14px;
    flex-flow: row nowrap;
  }

  .form-item {
    margin-bottom: 0;
  }

  .title-css {
    height: 26px;
  }

  .title-text {
    line-height: 26px;
  }
</style>
