<template>
  <el-row>
    <el-row v-loading="slidersLoading">
      <el-col :span="2">
        <span>&nbsp;</span>
        <ul
          v-show="currentIndex>1"
          class="direction"
        >
          <li
            class="left"
            @click="move(sliderWidth, 1, speed)"
          >
            <svg
              class="icon"
              width="15px"
              height="15.00px"
              viewBox="0 0 1024 1024"
              version="1.1"
              xmlns="http://www.w3.org/2000/svg"
            ><path
              fill="#ffffff"
              d="M481.233 904c8.189 0 16.379-3.124 22.628-9.372 12.496-12.497 12.496-32.759 0-45.256L166.488 512l337.373-337.373c12.496-12.497 12.496-32.758 0-45.255-12.498-12.497-32.758-12.497-45.256 0l-360 360c-12.496 12.497-12.496 32.758 0 45.255l360 360c6.249 6.249 14.439 9.373 22.628 9.373z"
            /></svg>
          </li>
        </ul>
      </el-col>
      <el-col :span="20">
        <el-row id="slider">
          <div class="window">
            <ul
              v-if="!slidersLoading"
              class="container"
              :style="containerStyle"
            >
              <li>
                <div style="width:240px; height: 208px;overflow: hidden">
                  <subject-template-item
                    v-for="item in sliders[sliders.length - 1]"
                    :key="item.id"
                    :subject-item="item"
                    @subjectDelete="subjectDelete"
                  />
                </div>
              </li>
              <li
                v-for="(itemSlider, index) in sliders"
                :key="index"
              >
                <div style="width:240px; height: 208px;">
                  <subject-template-item
                    v-for="item in itemSlider"
                    :key="item.id"
                    :subject-item="item"
                    @subjectDelete="subjectDelete"
                  />
                </div>
              </li>
              <li>
                <div style="width:240px; height: 208px;">
                  <subject-template-item
                    v-for="item in sliders[0]"
                    :key="item.id"
                    :subject-item="item"
                    @subjectDelete="subjectDelete"
                  />
                </div>
              </li>
            </ul>
          </div>
        </el-row>
      </el-col>
      <el-col :span="2">
        <span>&nbsp;</span>
        <ul
          v-show="currentIndex<sliders.length"
          class="direction"
        >
          <li
            class="right"
            @click="move(sliderWidth, -1, speed)"
          >
            <svg
              class="icon"
              width="15px"
              height="15.00px"
              viewBox="0 0 1024 1024"
              version="1.1"
              xmlns="http://www.w3.org/2000/svg"
            ><path
              fill="#ffffff"
              d="M557.179 904c-8.189 0-16.379-3.124-22.628-9.372-12.496-12.497-12.496-32.759 0-45.256L871.924 512 534.551 174.627c-12.496-12.497-12.496-32.758 0-45.255 12.498-12.497 32.758-12.497 45.256 0l360 360c12.496 12.497 12.496 32.758 0 45.255l-360 360c-6.249 6.249-14.439 9.373-22.628 9.373z"
            /></svg>
          </li>
        </ul>
      </el-col>
    </el-row>
    <el-row>
      <el-col
        :span="7"
        style="height: 30px"
      />
      <el-col
        :span="10"
        style="height: 30px"
      >
        <span hidden>B</span>
        <ul class="dots">
          <li
            v-for="(dot, i) in sliders"
            :key="i"
            :class="{dotted: i === (currentIndex-1)}"
            @click="jump(i+1)"
          />
        </ul>
      </el-col>
      <el-col
        :span="7"
        style="margin: auto;height: 30px;font-size:12px;color:#3685f2"
      >
        <span><a @click="saveSelfSubject">{{ $t('commons.save') }}</a></span>
      </el-col>
    </el-row>
  </el-row>

</template>

<script>
import SubjectTemplateItem from './SubjectTemplateItem'
import { querySubjectWithGroup, saveOrUpdateSubject, deleteSubject } from '@/api/panel/panel'
import { mapState } from 'vuex'
import { deepCopy } from '@/components/canvas/utils/utils'
import { uuid } from 'vue-uuid'

export default {
  name: 'Slider',
  components: {
    SubjectTemplateItem
  },
  props: {
    initialSpeed: {
      type: Number,
      default: 30
    },
    initialInterval: {
      type: Number,
      default: 3
    }
  },
  data() {
    return {
      sliders: [],
      slidersLoading: false,
      sliderWidth: 240,
      imgWidth: 240,
      currentIndex: 1,
      distance: -240,
      transitionEnd: true,
      speed: this.initialSpeed,
      saveSubjectVisible: false
    }
  },
  computed: {
    containerStyle() {
      return {
        transform: `translate3d(${this.distance}px, 0, 0)`
      }
    },
    interval() {
      return this.initialInterval * 1000
    },
    ...mapState([
      'canvasStyleData'
    ])
  },
  mounted() {
    this.querySubjectWithGroup()
  },
  methods: {
    subjectDelete(id) {
      deleteSubject(id).then(response => {
        this.$message({
          message: '删除成功',
          type: 'success',
          showClose: true
        })
        this.querySubjectWithGroup()
      })
    },
    saveSelfSubject() {
      const canvasStyle = deepCopy(this.canvasStyleData)
      canvasStyle.themeId = uuid.v1()
      const request = {
        details: JSON.stringify(canvasStyle)
      }
      this.slidersLoading = true
      saveOrUpdateSubject(request).then(response => {
        this.$message({
          message: '保存成功',
          type: 'success',
          showClose: true
        })
        this.querySubjectWithGroup()
      }).catch(() => {
        this.slidersLoading = false
      })
    },
    querySubjectWithGroup() {
      const _this = this
      _this.slidersLoading = true
      querySubjectWithGroup({}).then(response => {
        _this.sliders = []
        _this.sliders = response.data
        _this.slidersLoading = false
        if (_this.sliders.length < _this.currentIndex) {
          _this.currentIndex = 1
          this.$emit('reload')
        }
      }).catch(() => {
        this.slidersLoading = false
      })
    },
    move(offset, direction, speed) {
      if (!this.transitionEnd) return
      this.transitionEnd = false
      direction === -1 ? this.currentIndex += offset / this.sliderWidth : this.currentIndex -= offset / this.sliderWidth
      if (this.currentIndex > this.sliders.length) this.currentIndex = 1
      if (this.currentIndex < 1) this.currentIndex = this.sliders.length

      const destination = this.distance + offset * direction
      this.animate(destination, direction, speed)
    },
    animate(des, direc, speed) {
      if (this.temp) {
        window.clearInterval(this.temp)
        this.temp = null
      }
      this.temp = window.setInterval(() => {
        if ((direc === -1 && des < this.distance) || (direc === 1 && des > this.distance)) {
          this.distance += speed * direc
        } else {
          this.transitionEnd = true
          window.clearInterval(this.temp)
          this.distance = des
          if (des < -this.sliderWidth * this.sliders.length) this.distance = -this.sliderWidth
          if (des > -this.sliderWidth) this.distance = -this.sliderWidth * this.sliders.length
        }
      }, 20)
    },
    jump(index) {
      const direction = index - this.currentIndex >= 0 ? -1 : 1
      const offset = Math.abs(index - this.currentIndex) * this.sliderWidth
      const jumpSpeed = Math.abs(index - this.currentIndex) === 0 ? this.speed : Math.abs(index - this.currentIndex) * this.speed
      this.move(offset, direction, jumpSpeed)
    }
  }
}
</script>
<style>
  *{
    box-sizing: border-box;
    margin:0;
    padding:0;
  }
  ol,ul{
    list-style: none;
  }
  #slider{
    text-align: center;
  }
  .window{
    position:relative;
    width:240px;
    height:208px;
    margin:0 auto;
    overflow:hidden;
  }
  .container{
    display:flex;
    position:absolute;
  }
  .left, .right{
    position:absolute;
    top:50%;
    transform:translateY(-50%);
    width:20px;
    height:20px;
    background-color:rgba(0,0,0,.3);
    border-radius:50%;
    cursor:pointer;
  }
  .left{
    padding-left:5px;
    padding-top:2px;
  }
  .right{
    padding-right:5px;
    padding-top:2px;
  }
  img{
    user-select: none;
  }
  .dots{
    position:absolute;
    bottom:10px;
    left:50%;
    transform:translateX(-50%);
  }
  .dots li{
    display:inline-block;
    width:7px;
    height:7px;
    margin:0 3px;
    border:1px solid white;
    border-radius:50%;
    background-color:#333;
    cursor:pointer;
  }
  .dots .dotted{
    background-color:orange;
  }
  .direction{
    width: 100%;
  }
</style>
