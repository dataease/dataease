<template>
  <el-row style="flex-direction: column">
    <el-row v-loading="state.slidersLoading">
      <div class="direction-left">
        <span>&nbsp;</span>
        <ul v-show="state.currentIndex > 1" class="direction">
          <li class="left" @click="move(state.sliderWidth, 1, state.speed)">
            <svg
              class="icon"
              width="15px"
              height="15.00px"
              viewBox="0 0 1024 1024"
              version="1.1"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                fill="#ffffff"
                d="M481.233 904c8.189 0 16.379-3.124 22.628-9.372 12.496-12.497 12.496-32.759 0-45.256L166.488 512l337.373-337.373c12.496-12.497 12.496-32.758 0-45.255-12.498-12.497-32.758-12.497-45.256 0l-360 360c-12.496 12.497-12.496 32.758 0 45.255l360 360c6.249 6.249 14.439 9.373 22.628 9.373z"
              />
            </svg>
          </li>
        </ul>
      </div>
      <el-col :span="24">
        <el-row id="slider">
          <div class="slider-window">
            <ul v-if="!state.slidersLoading" class="container" :style="containerStyle">
              <li>
                <div class="item-area" style="overflow: hidden">
                  <subject-template-item
                    v-for="item in state.sliders[state.sliders.length - 1]"
                    :key="item.id"
                    :subject-item="item"
                    @subjectDelete="subjectDelete"
                  />
                </div>
              </li>
              <li v-for="(itemSlider, index) in state.sliders" :key="index">
                <div class="item-area">
                  <subject-template-item
                    v-for="item in itemSlider"
                    :key="item.id"
                    :subject-item="item"
                    @subjectDelete="subjectDelete"
                  />
                </div>
              </li>
              <li>
                <div class="item-area">
                  <subject-template-item
                    v-for="item in state.sliders[0]"
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
      <div class="direction-right">
        <span>&nbsp;</span>
        <ul v-show="state.currentIndex < state.sliders.length" class="direction">
          <li class="right" @click="move(state.sliderWidth, -1, state.speed)">
            <svg
              class="icon"
              width="15px"
              height="15.00px"
              viewBox="0 0 1024 1024"
              version="1.1"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                fill="#ffffff"
                d="M557.179 904c-8.189 0-16.379-3.124-22.628-9.372-12.496-12.497-12.496-32.759 0-45.256L871.924 512 534.551 174.627c-12.496-12.497-12.496-32.758 0-45.255 12.498-12.497 32.758-12.497 45.256 0l360 360c12.496 12.497 12.496 32.758 0 45.255l-360 360c-6.249 6.249-14.439 9.373-22.628 9.373z"
              />
            </svg>
          </li>
        </ul>
      </div>
    </el-row>
    <el-row>
      <el-col :span="7" style="height: 30px" />
      <el-col :span="10" style="height: 30px">
        <span hidden>B</span>
        <ul class="dots">
          <li
            v-for="(dot, i) in state.sliders"
            :key="i"
            :class="{ dotted: i === state.currentIndex - 1 }"
            @click="jump(i + 1)"
          />
        </ul>
      </el-col>
      <el-col :span="7" class="save-area">
        <span
          ><a @click="saveSelfSubject">{{ $t('commons.save') }}</a></span
        >
      </el-col>
    </el-row>
  </el-row>
</template>

<script setup lang="ts">
import SubjectTemplateItem from './SubjectTemplateItem.vue'
import {
  querySubjectWithGroupApi,
  saveOrUpdateSubject,
  deleteSubject
} from '@/api/dataVisualization'
import { reactive, toRefs, computed, onMounted } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus-secondary'
import { deepCopy } from '@/utils/utils'
const dvMainStore = dvMainStoreWithOut()
const { canvasStyleData } = storeToRefs(dvMainStore)
const emit = defineEmits(['reload'])
import { guid } from '@/views/visualized/data/dataset/form/util.js'

const props = defineProps({
  initialSpeed: {
    type: Number,
    default: 30
  },
  initialInterval: {
    type: Number,
    default: 3
  }
})

const { initialSpeed, initialInterval } = toRefs(props)

const state = reactive({
  temp: null,
  sliders: [],
  slidersLoading: false,
  sliderWidth: 290,
  imgWidth: 290,
  currentIndex: 1,
  distance: -290,
  transitionEnd: true,
  speed: initialSpeed.value,
  saveSubjectVisible: false
})

const containerStyle = computed(() => {
  return {
    transform: `translate3d(${state.distance}px, 0, 0)`
  }
})

const interval = computed(() => {
  return initialInterval.value * 1000
})

const querySubjectWithGroup = () => {
  state.slidersLoading = true
  querySubjectWithGroupApi({})
    .then(response => {
      state.sliders = []
      state.sliders = response.data
      state.slidersLoading = false
      if (state.sliders.length < state.currentIndex) {
        state.currentIndex = 1
        emit('reload')
      }
    })
    .catch(() => {
      state.slidersLoading = false
    })
}

const subjectDelete = id => {
  deleteSubject(id).then(response => {
    ElMessage.success('删除成功')
    querySubjectWithGroup()
  })
}
const saveSelfSubject = () => {
  const canvasStyle = deepCopy(canvasStyleData.value)
  canvasStyle.themeId = guid()
  const request = {
    details: JSON.stringify(canvasStyle)
  }
  state.slidersLoading = true
  saveOrUpdateSubject(request)
    .then(response => {
      ElMessage.success('保存成功')
      querySubjectWithGroup()
    })
    .catch(() => {
      state.slidersLoading = false
    })
}

const animate = (des, direc, speed) => {
  if (state.temp) {
    window.clearInterval(state.temp)
    state.temp = null
  }
  state.temp = window.setInterval(() => {
    if ((direc === -1 && des < state.distance) || (direc === 1 && des > state.distance)) {
      state.distance += speed * direc
    } else {
      state.transitionEnd = true
      window.clearInterval(state.temp)
      state.distance = des
      if (des < -state.sliderWidth * state.sliders.length) state.distance = -state.sliderWidth
      if (des > -state.sliderWidth) state.distance = -state.sliderWidth * state.sliders.length
    }
  }, 20)
}

const move = (offset, direction, speed) => {
  if (!state.transitionEnd) return
  state.transitionEnd = false
  direction === -1
    ? (state.currentIndex += offset / state.sliderWidth)
    : (state.currentIndex -= offset / state.sliderWidth)
  if (state.currentIndex > state.sliders.length) state.currentIndex = 1
  if (state.currentIndex < 1) state.currentIndex = state.sliders.length

  const destination = state.distance + offset * direction
  animate(destination, direction, speed)
}

const jump = index => {
  const direction = index - state.currentIndex >= 0 ? -1 : 1
  const offset = Math.abs(index - state.currentIndex) * state.sliderWidth
  const jumpSpeed =
    Math.abs(index - state.currentIndex) === 0
      ? state.speed
      : Math.abs(index - state.currentIndex) * state.speed
  move(offset, direction, jumpSpeed)
}

onMounted(() => {
  querySubjectWithGroup()
})
</script>
<style scoped>
.item-area {
  width: 290px;
  height: 250px;
}

.save-area {
  height: 30px;
  margin: auto;
  font-size: 12px;
  color: #3685f2;
}

* {
  padding: 0;
  margin: 0;
  box-sizing: border-box;
}

ol,
ul {
  list-style: none;
}

#slider {
  text-align: center;
}

.slider-window {
  position: relative;
  width: 290px;
  height: 250px;
  margin: 0 auto;
  overflow: hidden;
}

.container {
  position: absolute;
  display: flex;
}

.left,
.right {
  position: absolute;
  top: 50%;
  width: 20px;
  height: 20px;
  cursor: pointer;
  background-color: rgba(0 0 0 / 3%);
  border-radius: 50%;
  transform: translateY(-50%);
}

.left {
  padding-top: 2px;
  padding-left: 5px;
}

.right {
  padding-top: 2px;
  padding-right: 5px;
}

img {
  user-select: none;
}

.dots {
  position: absolute;
  bottom: 10px;
  left: 50%;
  transform: translateX(-50%);
}

.dots li {
  display: inline-block;
  width: 7px;
  height: 7px;
  margin: 0 3px;
  cursor: pointer;
  background-color: #333;
  border: 1px solid white;
  border-radius: 50%;
}

.dots .dotted {
  background-color: orange;
}

.direction {
  width: 100%;
}

.direction-left {
  position: absolute;
  top: 110px;
  left: 2px;
  z-index: 2;
  width: 22px;
  height: 22px;
}

.direction-right {
  position: absolute;
  top: 110px;
  right: 2px;
  z-index: 2;
  width: 22px;
  height: 22px;
}
</style>
