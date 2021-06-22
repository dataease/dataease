<template>
  <div class="bg">
    <Preview />
  </div>
</template>
<script>
import Preview from './Preview'
import { uuid } from 'vue-uuid'
import { findOne } from '@/api/panel/panel'

export default {
  components: { Preview },
  mounted() {
    this.restore()
  },
  methods: {
    restore() {
      this.panelId = this.$route.path.split('/')[2]
      // 加载视图数据
      findOne(this.panelId).then(response => {
        this.$store.commit('setComponentData', this.resetID(JSON.parse(response.data.panelData)))
        this.$store.commit('setCanvasStyle', JSON.parse(response.data.panelStyle))
        const data = {
          id: response.data.id,
          name: response.data.name
        }
        this.$store.dispatch('panel/setPanelInfo', data)
      })
    },
    resetID(data) {
      if (data) {
        data.forEach(item => {
          item.type !== 'custom' && (item.id = uuid.v1())
        })
      }
      return data
    }
  }
}
</script>

<style lang="scss" scoped>
  .bg {
    width: 100%;
    height: 100vh!important;
    min-width: 800px;
    min-height: 600px;
    background-color: #f7f8fa;
  }
</style>

