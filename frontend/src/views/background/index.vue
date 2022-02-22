<template>
  <el-row>
    <el-row class="main-row">
      <el-row v-for="(value, key) in BackgroundShowMap" :key="key">
        <el-col :span="24"><span>{{ key }}</span> </el-col>
        <el-col
          v-for="item in value"
          :key="item.id"
          :span="8"
        >
          <background-item
            :template="item"
          />
        </el-col>
      </el-row>
    </el-row>
    <el-row class="root-class">
      <el-col :span="9" style="text-align: right;vertical-align: middle">
        <el-checkbox v-model="curComponent.commonBackground.enable">{{ $t('commons.enable') }}</el-checkbox>
      </el-col>
      <el-col :span="6">
        <el-button size="mini" @click="cancel()">{{ $t('commons.cancel') }}</el-button>
        <el-button type="primary" size="mini" @click="save()">{{ $t('commons.confirm') }}</el-button>
      </el-col>
    </el-row>
  </el-row>
</template>

<script>
import { queryBackground } from '@/api/background/background'
import BackgroundItem from '@/views/background/BackgroundItem'
import { mapState } from 'vuex'
import eventBus from '@/components/canvas/utils/eventBus'
import { deepCopy } from '@/components/canvas/utils/utils'

export default {
  name: 'Background',
  components: { BackgroundItem },
  data() {
    return {
      BackgroundShowMap: {},
      checked: false,
      backgroundOrigin: {}

    }
  },
  mounted() {
    this.backgroundOrigin = deepCopy(this.curComponent.commonBackground)
    this.queryBackground()
  },
  computed: {
    ...mapState([
      'curComponent'
    ])
  },
  methods: {
    queryBackground() {
      queryBackground().then(response => {
        this.BackgroundShowMap = response.data
      })
    },
    cancel() {
      this.curComponent.commonBackground.enable = this.backgroundOrigin.enable
      this.curComponent.commonBackground.backgroundType = this.backgroundOrigin.backgroundType
      this.curComponent.commonBackground.color = this.backgroundOrigin.color
      this.curComponent.commonBackground.innerImage = this.backgroundOrigin.innerImage
      this.curComponent.commonBackground.outerImage = this.backgroundOrigin.outerImage
      eventBus.$emit('backgroundSetClose')
    },
    save() {
      this.$store.commit('recordSnapshot')
      eventBus.$emit('backgroundSetClose')
    }

  }
}
</script>

<style scoped>
  .el-card-template {
    min-width: 260px;
    min-width: 460px;
    width: 100%;
    height: 100%;
  }

  .main-row{
    height: 60vh;
    overflow-y: auto;
  }

  .root-class {
    margin: 15px 0px 5px;
    text-align: center;
  }

</style>
