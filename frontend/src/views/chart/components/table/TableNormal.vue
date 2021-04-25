<template>
  <div>
    <p style="width: 100%;">{{ chart.title }}</p>
    <ux-grid
      ref="plxTable"
      size="mini"
      style="width: 100%;"
      :height="height"
      :checkbox-config="{highlight: true}"
      :width-resize="true"
    >
      <ux-table-column
        v-for="field in fields"
        :key="field.originName"
        min-width="200px"
        :field="field.originName"
        :resizable="true"
        sortable
        :title="field.name"
      >
        <!--        <template slot="header">-->
        <!--          <span>{{ field.name }}</span>-->
        <!--        </template>-->
      </ux-table-column>
    </ux-grid>
  </div>
</template>

<script>
export default {
  name: 'TableNormal',
  props: {
    chart: {
      type: Object,
      required: true
    },
    filter: {
      type: Object,
      required: false,
      default: function() {
        return {}
      }
    }
  },
  data() {
    return {
      fields: [],
      height: 500
    }
  },
  watch: {
    chart() {
      this.init()
    }
  },
  mounted() {
    this.calcHeight()
    this.init()
  },
  methods: {
    init() {
      this.fields = JSON.parse(JSON.stringify(this.chart.data.fields))
      const datas = JSON.parse(JSON.stringify(this.chart.data.tableRow))
      this.$refs.plxTable.reloadData(datas)
      const that = this
      window.onresize = function() {
        that.calcHeight()
      }
    },
    calcHeight() {
      const currentHeight = document.documentElement.clientHeight
      this.height = currentHeight - 56 - 40 - 84 - 24 - 16 * 2 - 20
    }
  }
}
</script>

<style scoped>

</style>
