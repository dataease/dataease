<template>
  <span class="previous-next-button">
    <span class="head-right-tip" v-if="index + 1 === list.length">
      {{ $t('test_track.plan_view.pre_case') }} : {{list[index - 1] ? list[index - 1].name : ''}}
    </span>
    <span class="head-right-tip" v-if="index + 1 !== list.length">
      {{ $t('test_track.plan_view.next_case') }} : {{list[index + 1] ? list[index + 1].name : ''}}
    </span>

    <el-button plain size="mini" icon="el-icon-arrow-up" :disabled="index + 1 <= 1" @click="handlePre()"/>
    <span>
      {{ index + 1 }}/{{ list.length }}
    </span>
    <el-button plain size="mini" icon="el-icon-arrow-down" :disabled="index + 1 >= list.length" @click="handleNext()"/>
  </span>
</template>

<script>
    export default {
      name: "MsPreviousNextButton",
      data() {
        return {

        }
      },
      props: {
        list: {
          type: Array,
          default() {
            return []
          }
        },
        index: {
          type: Number,
          default() {
            return 0
          }
        }
      },
      methods: {
        handlePre() {
          this.$emit('pre');
        },
        handleNext() {
          this.$emit('next');
        }
      }
    }
</script>

<style scoped>

  .head-right-tip {
    color: darkgrey;
  }

</style>
