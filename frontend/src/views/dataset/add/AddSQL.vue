<template>
  <el-col>
    <el-row>
      <el-row style="height: 26px;">
        <span style="line-height: 26px;">
          {{ $t('dataset.add_sql_table') }}
        </span>
        <el-row style="float: right">
          <el-button size="mini" @click="cancel">
            {{ $t('dataset.cancel') }}
          </el-button>
          <el-button size="mini" type="primary">
            {{ $t('dataset.confirm') }}
          </el-button>
        </el-row>
      </el-row>
      <el-divider/>
      <el-row>
        <el-form :inline="true">
          <el-form-item class="form-item">
            <el-select v-model="dataSource" filterable :placeholder="$t('dataset.pls_slc_data_source')" size="mini">
              <el-option
                v-for="item in options"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-form>
      </el-row>
    </el-row>
  </el-col>
</template>

<script>
import { post, listDatasource } from '@/api/dataset/dataset'

export default {
  name: 'AddSQL',
  props: {
    param: Object
  },
  data() {
    return {
      dataSource: '',
      options: []
    }
  },
  mounted() {
    this.initDataSource()
  },
  watch: {},
  methods: {
    initDataSource() {
      listDatasource().then(response => {
        this.options = response.data
      })
    },
    cancel() {
      // this.dataReset()
      this.$emit('switchComponent', { name: '' })
    }
  }
}
</script>

<style scoped lang="scss">
  .el-divider--horizontal {
    margin: 12px 0;
  }

  .form-item {
    margin-bottom: 6px;
  }

  .el-checkbox {
    margin-bottom: 14px;
    margin-left: 0;
    margin-right: 14px;
  }

  .el-checkbox.is-bordered + .el-checkbox.is-bordered {
    margin-left: 0;
  }
</style>
