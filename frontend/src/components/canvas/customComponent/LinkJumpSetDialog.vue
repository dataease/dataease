<template>
  <de-container style="height: 500px">
    <de-aside-container>
      this is test
    </de-aside-container>
    <de-main-container>
      this is test2
    </de-main-container>
  </de-container>
</template>

<script>

import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import DeAsideContainer from '@/components/dataease/DeAsideContainer'
import { mapState } from 'vuex'

export default {
  name: 'UserView',
  components: { DeMainContainer, DeContainer, DeAsideContainer },
  props: {
    chart: {
      type: Object,
      default: null
    },
    chartTable: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      refId: null
    }
  },
  computed: {
    ...mapState([
      'isClickComponent',
      'curComponent',
      'componentData',
      'canvasStyleData'
    ])
  },
  methods: {

  }
}
</script>

<style lang="scss" scoped>
  .ms-aside-container {
    height: 50vh;
    min-width: 400px;
    max-width: 400px;
    padding: 0 0;
  }
  .ms-main-container {
    height: 50vh;
    border: 1px solid #E6E6E6;
    border-left: 0 solid;
  }
  .chart-class{
    height: 100%;
  }
  .table-class{
    height: 100%;
  }
</style>
