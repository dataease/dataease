<template>
<el-col>
  <el-row>
    <el-row style="height: 26px;">
      <span v-show="false">{{tableId}}</span>
      <span style="line-height: 26px;">
        {{table.name}}
      </span>
      <el-row style="float: right">
        <el-button size="mini">
          {{$t('dataset.edit')}}
        </el-button>
        <el-button size="mini">
          {{$t('dataset.create_view')}}
        </el-button>
      </el-row>
    </el-row>
    <el-divider/>

    <el-tabs v-model="tabActive">
      <el-tab-pane :label="$t('dataset.data_preview')" name="dataPreview">
        <tab-data-preview :table="table" :fields="fields" :data="data"/>
      </el-tab-pane>
      <el-tab-pane label="tab2" name="tab2">
        tab2
      </el-tab-pane>
      <el-tab-pane label="tab3" name="tab3">
        tab3
      </el-tab-pane>
      <el-tab-pane label="tab4" name="tab4">
        tab4
      </el-tab-pane>
    </el-tabs>
  </el-row>
</el-col>
</template>

<script>
import TabDataPreview from "./TabDataPreview";

export default {
  name: "ViewTable",
  components: {TabDataPreview},
  data() {
    return {
      table: {
        name: ''
      },
      fields: [],
      data: [],
      tabActive: 'dataPreview',
    }
  },
  computed: {
    tableId() {
      console.log(this.$store.state.dataset.table);
      this.initTable(this.$store.state.dataset.table);
      return this.$store.state.dataset.table;
    }
  },
  created() {
    this.resetTable();
  },
  mounted() {
    this.resetTable();
  },
  activated() {
    this.resetTable();
  },
  methods: {
    initTable(id) {
      console.log(id);
      if (id !== null) {
        this.fields = [];
        this.data = [];
        this.$post('/dataset/table/get/' + id, null, response => {
          this.table = response.data;
          this.initPreviewData();
        })
      }
    },

    initPreviewData() {
      if (this.table.id) {
        this.$post('/dataset/table/getPreviewData', this.table, response => {
          this.fields = response.data.fields;
          this.data = response.data.data;
        });
      }
    },

    initTableFields() {
      if (this.table.id) {
        this.$post('/dataset/table/getFields', this.table, response => {
        });
      }
    },
    initTableData() {
      if (this.table.id) {
        this.$post('/dataset/table/getData', this.table, response => {
        });
      }
    },

    resetTable() {
      this.table = {
        name: ''
      }
    }
  },
  watch: {}
}
</script>

<style scoped>
  .el-divider--horizontal {
    margin: 12px 0;
  }

  .form-item {
    margin-bottom: 6px;
  }
</style>
