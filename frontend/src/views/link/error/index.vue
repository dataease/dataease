<template>
  <div class="pwd-body">

    <div class="countdown">
      <svg
        :width="size"
        :height="size"
      >
        <circle
          fill="transparent"
          :stroke-width="stroke"
          stroke="#270B58"
          :r="radius"
          :cx="circleOffset"
          :cy="circleOffset"
        />
        <circle
          class="circle"
          fill="transparent"
          :stroke-width="stroke"
          stroke="#F36F21"
          :r="radius"
          :cx="circleOffset"
          :cy="circleOffset"
          :stroke-dasharray="circumference"
          :stroke-dashoffset="progress"
          stroke-linecap="round"
        />
      </svg>
      <span>{{ countdown }}</span>
    </div>
    <div>
      <h1>链接无效 即将跳转登录页面</h1>
    </div>
  </div>
</template>

<script>
export default {
  name: 'LinkError',
  data() {
    return {
      size: 200,
      stroke: 30,
      percentage: 100,
      timer: null,
      seconds: 5
    }
  },
  computed: {
    radius() {
      return (this.size / 2) - (this.stroke / 2)
    },
    circleOffset() {
      return this.size / 2
    },
    circumference() {
      return this.radius * 2 * Math.PI
    },
    progress() {
      return this.circumference - this.circumference * this.percentage / 100
    },
    countdown() {
      return Math.ceil(this.seconds * this.percentage / 100)
    }

  },
  mounted() {
    this.$nextTick(() => {
      this.animate()
    })
  },
  methods: {
    animate() {
      this.timer = setInterval(() => {
        this.percentage -= 1 / 10

        if (this.percentage <= 0) {
          clearInterval(this.timer)
          this.percentage = 100
          this.$store.dispatch('user/logout')
          window.location.href = '/login'
        }
      }, this.seconds * 1000 / 100 / 10)
    }
  }
}
</script>

<style scoped>
.circle {
  transform: rotate(-90deg);
  transform-origin: 50% 50%;;
}

.countdown {
  display: inline-block;
  position: relative;
  margin-top: 15%;
}

span {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translateX(-50%) translateY(-50%);
  font-size: 180px;
  font-family: monospace;
  color: #F36F21;
}

.pwd-body {
  display: block;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  text-align: center;
  background-color: #F7F8FA;
}
</style>
