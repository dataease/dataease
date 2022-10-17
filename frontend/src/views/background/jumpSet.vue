<template>
  <div>
    <p style="font-size: 18px;">跳转设置</p>
    <div style="margin-top: 10px;">
      <el-row>
        <el-col>
          <el-button type="primary" @click="addLink" size="small">新增</el-button>
        </el-col>
      </el-row>
      <el-row>
        <el-col v-for="(item,index) in curComponent.options.jumpList"  :key="index">
          <el-row class="jump_row">
            <el-col :span="4" class="jump_col_4">名称：</el-col>
            <el-col :span="10">
              <el-input v-model="item.jumpName" size="small"></el-input>
            </el-col>
            <el-col :span="4" v-if="curComponent.options.jumpList.length>1" style="padding-left: 5px;">
              <el-button type="danger" size="small" @click="delLink(index)">删除</el-button>
            </el-col>
          </el-row>
          <el-row class="jump_row">
            <el-col :span="4" class="jump_col_4">链接：</el-col>
            <el-col :span="10">
              <el-input v-model="item.jumpLink" size="small"></el-input>
            </el-col>
          </el-row>
        </el-col>
      </el-row>
    </div>
    <el-row class="root-class">
      <el-col :span="24">
        <el-button size="mini" @click="cancel()">{{ $t('commons.cancel') }}</el-button>
        <el-button type="primary" size="mini" @click="save()">{{ $t('commons.confirm') }}</el-button>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { mapState } from 'vuex'

export default {
  name: 'jumpSet',
  props: {
    element: {
      type: Object,
      require: true
    }
  },
  data() {
    return {

    }
  },
  computed: {
    ...mapState([
      'curComponent',
      'componentData'
    ])
  },
  mounted() {
    console.log(this.curComponent)
  },
  methods: {
    save() {
      
      this.$store.commit('recordSnapshot')
      this.$emit('backgroundSetClose')
    },
    cancel() {

      this.$emit('backgroundSetClose')
    },
    addLink() {
      this.curComponent.options.jumpList.push({jumpName: '', jumpLink: ''})
    },
    delLink(index) {
      console.log(index)
      this.curComponent.options.jumpList.splice(index,1)
    }
  }
}
</script>
<style lang="scss">
.root-class {
  margin: 15px 0px 5px;
  text-align: center;
}

.jump_row {
  margin: 10px 20px;

  .jump_col_4 {
    text-align: center;
    font-weight: bold;
  }
}
</style>