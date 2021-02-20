<template>
  <div class="ms-border">
    <el-table :data="hostTable" style="width: 100%" @cell-dblclick="dblHostTable" class="ht-tb">
      <el-table-column prop="ip" label="IP">
        <template slot-scope="scope">
          <el-input v-if="scope.row.status" v-model="scope.row.ip"></el-input>
          <span v-else>{{scope.row.ip}}</span>
        </template>
      </el-table-column>

      <el-table-column prop="domain" :label="$t('load_test.domain')">
        <template slot-scope="scope">
          <el-input v-if="scope.row.status" v-model="scope.row.domain"></el-input>
          <span v-else>{{scope.row.domain}}</span>
        </template>
      </el-table-column>

      <el-table-column prop="annotation" :label="$t('commons.annotation')">
        <template slot-scope="scope">
          <el-input v-if="scope.row.status" v-model="scope.row.annotation"></el-input>
          <span v-else>{{scope.row.annotation}}</span>
        </template>
      </el-table-column>

      <el-table-column :label="$t('commons.operating')" width="100">
        <template v-slot:default="scope">
          <span>
            <el-button size="mini" p="$t('commons.remove')" icon="el-icon-close" circle @click="remove(scope.row)"
                       class="ht-btn-remove"/>
            <el-button size="mini" p="$t('commons.save')" icon="el-icon-check" circle @click="confirm(scope.row)"
                       class="ht-btn-confirm"/>
          </span>
        </template>

      </el-table-column>
    </el-table>

    <el-button class="ht-btn-add" size="mini" p="$t('commons.add')" icon="el-icon-circle-plus-outline" @click="add">添加
    </el-button>
  </div>
</template>

<script>
  import MsApiVariableInput from "../ApiVariableInput";
  import MsTableOperatorButton from "../../../../common/components/MsTableOperatorButton";

  export default {
    name: "MsApiHostTable",
    components: {MsApiVariableInput, MsTableOperatorButton},
    props: {
      hostTable: Array,
    },
    data() {
      return {
        currentPage: 1,
        pageSize: 5,
        total: 0,
      }
    },
    created() {
      this.init();
    },
    methods: {
      remove: function (row) {
        this.hostTable.splice(this.hostTable.indexOf(row), 1);
      },
      change: function () {
        this.$emit('change', this.hostTable);
      },
      add: function (r) {
        let row = {
          ip: '',
          domain: '',
          status: 'edit',
          annotation: '',
          uuid: this.uuid(),
        }
        // 获取上一行的数据
        for (let i = this.hostTable.length - 1; i >= 0; i--) {
          if (this.hostTable[i].status === '') {
            row.ip = this.hostTable[i].ip;
            row.domain = this.hostTable[i].domain;
            row.annotation = this.hostTable[i].annotation;
            break;
          }
        }

        this.hostTable.push(row);
      },
      confirm: function (row) {
        this.validateIp(row.ip) && this.validateDomain(row.domain) ? row.status = '' : row.status;
        this.$emit('change', this.hostTable);
        if (row.status === "") {
          return true;
        }
        return false;
      },
      init: function () {
        if (this.hostTable === undefined || this.hostTable.length === 0) {
          let row = {
            ip: '',
            domain: '',
            status: 'edit',
            annotation: '',
            uuid: this.uuid()
          }
          this.hostTable.push(row);
        }
      },
      validateIp(ip) {
        let regexp = /^((2(5[0-5]|[0-4]\d))|[0-1]?\d{1,2})(\.((2(5[0-5]|[0-4]\d))|[0-1]?\d{1,2})){3}$/;
        if (regexp.test(ip) == false) {
          this.$warning(this.$t('load_test.input_ip'));
          return false;
        }
        return true;
      },
      validateDomain(domain) {
        let strRegex = "^(?=^.{3,255}$)(http(s)?:\\/\\/)?(www\\.)?[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+(:\\d+)*(\\/\\w+\\.\\w+)*$";
        const re = new RegExp(strRegex);
        if (re.test(domain) && domain.length < 67) {
          return true;
        }
        this.$warning(this.$t('load_test.input_domain'));
        return false;
      },
      dblHostTable: function (row) {
        row.status = 'edit';
      },
      uuid: function () {
        return (((1 + Math.random()) * 0x100000) | 0).toString(16).substring(1);
      },
    }
  }
</script>

<style scoped>

  .ht-btn-remove {
    color: white;
    background-color: #DCDFE6;
  }

  .ht-btn-confirm {
    color: white;
    background-color: #1483F6;
  }

  .ht-btn-add {
    border: 0px;
    margin-top: 10px;
    color: #1483F6;
    background-color: white;
  }

  .ht-tb {
    background-color: white;
    border: 0px;
  }
</style>
