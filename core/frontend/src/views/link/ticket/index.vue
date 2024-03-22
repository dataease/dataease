<template>
  <el-empty :description="showIndex === 4 ? errorTips : expTips" />
</template>

<script>
export default {
  name: 'TicketError',
  props: {
    showIndex: {
      type: Number,
      default: 4
    }
  },
  data() {
    return {
      errorTips: 'ticket参数错误',
      expTips: 'ticket已过期'
    }
  }
}
</script>
