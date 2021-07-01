<template>
  <de-container>
    <de-aside-container>
      <dataset-group-selector-tree @getTable="getTable" />
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

// import DatasetGroupSelector from '../../dataset/common/DatasetGroupSelector'
import DatasetGroupSelectorTree from '../../dataset/common/DatasetGroupSelectorTree'
import DatasetTableData from '../../dataset/common/DatasetTableData'
import { getTable } from '@/api/dataset/dataset'

export default {
  name: 'TableSelector',
  components: {
    DatasetTableData,
    DeMainContainer, DeContainer, DeAsideContainer, DatasetGroupSelectorTree
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
  },
  methods: {
    getTable(table) {
      // this.table = table
      getTable(table.id).then(response => {
        this.table = response.data
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
