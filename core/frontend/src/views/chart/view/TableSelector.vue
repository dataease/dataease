<template>
  <de-container>
    <de-aside-container>
      <dataset-group-selector-tree
        :checked-table="checkedTable"
        :privileges="privileges"
        :mode="mode"
        :clear-empty-dir="clearEmptyDir"
        :type="type"
        :custom-type="customType"
        :show-mode="showMode"
        @getTable="getTable"
      />
    </de-aside-container>
    <de-main-container>
      <dataset-table-data :table="table" />
    </de-main-container>
  </de-container>
</template>

<script>
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import DeAsideContainer from '@/components/dataease/DeAsideContainer'

import DatasetGroupSelectorTree from '../../dataset/common/DatasetGroupSelectorTree'
import DatasetTableData from '../../dataset/common/DatasetTableData'
import { getTable } from '@/api/dataset/dataset'

export default {
  name: 'TableSelector',
  components: {
    DatasetTableData,
    DeMainContainer, DeContainer, DeAsideContainer, DatasetGroupSelectorTree
  },
  props: {
    mode: {
      type: Number,
      required: false,
      default: -1
    },
    type: {
      type: String,
      required: false,
      default: null
    },
    showMode: {
      type: String,
      required: false,
      default: null
    },
    customType: {
      type: Array,
      required: false,
      default: null
    },
    privileges: {
      type: String,
      required: false,
      default: null
    },
    previewForTask: {
      type: Boolean,
      required: false,
      default: false
    },
    clearEmptyDir: {
      type: Boolean,
      required: false,
      default: false
    },
    checkedTable: {
      type: Object,
      required: false,
      default: null
    }
  },
  data() {
    return {
      table: {}
    }
  },
  computed: {},
  created() {
  },
  mounted() {
    this.getTable(this.checkedTable)
  },
  methods: {
    getTable(table) {
      // this.table = table
      table && table.id && getTable(table.id).then(response => {
        this.table = response.data
        this.table.previewForTask = this.previewForTask
        this.$emit('getTable', this.table)
      }).catch(res => {
        this.table = {}
      })
    }
  }
}
</script>

<style scoped>
  .ms-aside-container {
    height: 50vh;
    min-width: 180px;
    max-width: 280px;
    padding: 0 0;
  }

  .ms-main-container {
    height: 50vh;
    border: 1px solid #E6E6E6;
    border-left: 0 solid;
  }
</style>
