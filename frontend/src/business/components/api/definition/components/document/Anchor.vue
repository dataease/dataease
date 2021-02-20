<template>
  <div class="container">
    <div class="wrapper">
      <div class="section" style="width:80%;margin-left: 8px" v-for="(item, index) in list" :key="index">
        <div class="border" style="width:100%;height:200px;font-size:30px;text-align:center;color:black;">
          <el-divider><i class="el-icon-mobile-phone">111</i></el-divider>
          {{item.name}}
        </div>
      </div>
    </div>
    <div id="nac" style="height:500px;"></div>
    <nav style="position:fixed;right:30px;top:100px;">
      <span class="nav1 hand" v-for="(item, index) in navList" :key="index" @click="jump(index)"
            :class="index==0?'current':''">{{item}}</span>
    </nav>
  </div>
</template>

<script>
// import $ from 'jquery';

export default {
  name:"MsAnchor",
  data() {
    return {
      scroll: '',
      list: [{
        name: "第一条wwwwwwww",
        backgroundcolor: "#90B2A3"
      }, {
        name: "第二条eeeeeeee",
        backgroundcolor: "#A593B2"
      }, {
        name: "第三条",
        backgroundcolor: "#A7B293"
      }, {
        name: "第四条",
        backgroundcolor: "#0F2798"
      }, {
        name: "第五条",
        backgroundcolor: "#0A464D"
      }],
      navList: [11111111, 2111111111111, 31111, 4111111, 5111111111]
    }
  },
  methods: {
    dataScroll: function () {
      this.scroll = document.documentElement.scrollTop || document.body.scrollTop;
    },
    jump(index) {
      let jump = document.getElementsByClassName('section');
      // 获取需要滚动的距离
      let total = jump[index].offsetTop;
      // Chrome
      document.body.scrollTop = total;
      // Firefox
      document.documentElement.scrollTop = total;
      // Safari
      window.pageYOffset = total;
      // $('html, body').animate({
      // 'scrollTop': total
      // }, 400);
    },
    loadSroll: function () {
      // var self = this;
      // var $navs = $(".nav1");
      // var sections = document.getElementsByClassName('section');
      // for (var i = sections.length - 1; i >= 0; i--) {
      //   if (self.scroll >= sections[i].offsetTop - 100) {
      //     $navs.eq(i).addClass("current").siblings().removeClass("current")
      //     break;
      //   }
      // }
    }
  },
  watch: {
    scroll: function () {
      this.loadSroll()
    }
  },
  mounted() {
    window.addEventListener('scroll', this.dataScroll);
  }
}
</script>

<style>
* {
  padding: 0;
  margin: 0;
}

.nav1 {
  display: block;
  height: 40px;
  text-align: center;
  line-height: 40px;
  background: #eee;
}

.navs1 .active {
  color: #847ec3;
  background-color: #e2e2e2;
}

.hand {
  cursor: pointer;
}

.current {
  color: #fff;
  background: #847ec3;
}

.border {
  border: 1px solid #eee;
  border-top: 0
}
</style>
