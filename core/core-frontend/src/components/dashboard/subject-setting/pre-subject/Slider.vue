<template>
  <el-row style="flex-direction: column; width: 100%">
    <el-row v-loading="state.slidersLoading">
      <div class="direction-left">
        <span>&nbsp;</span>
        <ul v-show="state.currentIndex > 1" class="direction">
          <li class="left" @click="move(state.sliderWidth, 1, state.speed)">
            <svg
              width="36"
              height="40"
              viewBox="0 0 36 40"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <g filter="url(#filter0_d_1423_44506)">
                <rect
                  width="24"
                  height="24"
                  rx="12"
                  transform="matrix(-1 0 0 1 28 4)"
                  fill="white"
                />
                <rect
                  x="-0.5"
                  y="0.5"
                  width="23"
                  height="23"
                  rx="11.5"
                  transform="matrix(-1 0 0 1 27 4)"
                  stroke="#DEE0E3"
                />
              </g>
              <path
                d="M13.8609 16.0005L17.9268 11.9346C18.0244 11.837 18.0244 11.6787 17.9268 11.5811L17.5732 11.2275C17.4756 11.1299 17.3173 11.1299 17.2197 11.2275L12.8003 15.6469C12.605 15.8422 12.605 16.1588 12.8003 16.354L17.2197 20.7735C17.3173 20.8711 17.4756 20.8711 17.5732 20.7735L17.9268 20.4199C18.0244 20.3223 18.0244 20.164 17.9268 20.0664L13.8609 16.0005Z"
                fill="#1F2329"
              />
              <defs>
                <filter
                  id="filter0_d_1423_44506"
                  x="-4"
                  y="0"
                  width="40"
                  height="40"
                  filterUnits="userSpaceOnUse"
                  color-interpolation-filters="sRGB"
                >
                  <feFlood flood-opacity="0" result="BackgroundImageFix" />
                  <feColorMatrix
                    in="SourceAlpha"
                    type="matrix"
                    values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0"
                    result="hardAlpha"
                  />
                  <feOffset dy="4" />
                  <feGaussianBlur stdDeviation="4" />
                  <feComposite in2="hardAlpha" operator="out" />
                  <feColorMatrix type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.1 0" />
                  <feBlend
                    mode="normal"
                    in2="BackgroundImageFix"
                    result="effect1_dropShadow_1423_44506"
                  />
                  <feBlend
                    mode="normal"
                    in="SourceGraphic"
                    in2="effect1_dropShadow_1423_44506"
                    result="shape"
                  />
                </filter>
              </defs>
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
                    @subjectEdit="subjectEdit(item)"
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
                    @subjectEdit="subjectEdit(item)"
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
                    @subjectEdit="subjectEdit(item)"
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
              width="36"
              height="40"
              viewBox="0 0 36 40"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <g filter="url(#filter0_d_1423_44502)">
                <rect x="8" y="4" width="24" height="24" rx="12" fill="white" />
                <rect x="8.5" y="4.5" width="23" height="23" rx="11.5" stroke="#DEE0E3" />
              </g>
              <path
                d="M22.1391 15.9985L18.0732 11.9327C17.9756 11.835 17.9756 11.6768 18.0732 11.5791L18.4268 11.2256C18.5244 11.1279 18.6827 11.1279 18.7803 11.2256L23.1997 15.645C23.395 15.8402 23.395 16.1568 23.1997 16.3521L18.7803 20.7715C18.6827 20.8691 18.5244 20.8691 18.4268 20.7715L18.0732 20.418C17.9756 20.3203 17.9756 20.162 18.0732 20.0644L22.1391 15.9985Z"
                fill="#1F2329"
              />
              <defs>
                <filter
                  id="filter0_d_1423_44502"
                  x="0"
                  y="0"
                  width="40"
                  height="40"
                  filterUnits="userSpaceOnUse"
                  color-interpolation-filters="sRGB"
                >
                  <feFlood flood-opacity="0" result="BackgroundImageFix" />
                  <feColorMatrix
                    in="SourceAlpha"
                    type="matrix"
                    values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0"
                    result="hardAlpha"
                  />
                  <feOffset dy="4" />
                  <feGaussianBlur stdDeviation="4" />
                  <feComposite in2="hardAlpha" operator="out" />
                  <feColorMatrix type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.1 0" />
                  <feBlend
                    mode="normal"
                    in2="BackgroundImageFix"
                    result="effect1_dropShadow_1423_44502"
                  />
                  <feBlend
                    mode="normal"
                    in="SourceGraphic"
                    in2="effect1_dropShadow_1423_44502"
                    result="shape"
                  />
                </filter>
              </defs>
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
          ><a style="cursor: pointer" @click="saveSelfSubject">{{ $t('commons.save') }}</a></span
        >
      </el-col>
    </el-row>
    <subject-edit-dialog
      ref="subjectEditDialogRef"
      @finish="subjectEditFinish"
    ></subject-edit-dialog>
  </el-row>
</template>

<script setup lang="ts">
import SubjectTemplateItem from './SubjectTemplateItem.vue'
import {
  querySubjectWithGroupApi,
  saveOrUpdateSubject,
  deleteSubject
} from '@/api/visualization/dataVisualization'
import { reactive, toRefs, computed, onMounted, ref } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus-secondary'
import { deepCopy } from '@/utils/utils'
const dvMainStore = dvMainStoreWithOut()
const { canvasStyleData } = storeToRefs(dvMainStore)
const emit = defineEmits(['reload'])
import { guid } from '@/views/visualized/data/dataset/form/util.js'
import SubjectEditDialog from '@/components/dashboard/subject-setting/pre-subject/SubjectEditDialog.vue'

const subjectEditDialogRef = ref(null)
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
  sliderWidth: 420,
  imgWidth: 420,
  currentIndex: 1,
  distance: -420,
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

const subjectEditFinish = subjectItem => {
  state.slidersLoading = true
  saveOrUpdateSubject(subjectItem)
    .then(response => {
      ElMessage.success('保存成功')
      querySubjectWithGroup()
    })
    .catch(() => {
      state.slidersLoading = false
    })
}

const subjectEdit = subjectItem => {
  if (subjectItem && subjectItem.id) {
    subjectEditDialogRef.value.optInit(subjectItem, 'edit')
    //编辑
  } else {
    // 新建
    subjectEditDialogRef.value.optInit(subjectItem, 'new')
  }
}
const saveSelfSubject = () => {
  const canvasStyle = deepCopy(canvasStyleData.value)
  canvasStyle.themeId = guid()
  const subjectItemNew = {
    name: '新建主题',
    coverUrl: null,
    details: JSON.stringify(canvasStyle)
  }
  subjectEdit(subjectItemNew)
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
<style scoped lang="less">
.item-area {
  width: 420px;
  height: 290px;
  padding: 0 5px;
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
  width: 400px;
  height: 280px;
  overflow: hidden;
}

.container {
  position: absolute;
  display: flex;
}

.left,
.right {
  position: absolute;
  top: calc(50% - 5px);
  width: 20px;
  height: 20px;
  cursor: pointer;
  border-radius: 50%;
  transform: translateY(-50%);
}

.left {
  left: -5px;
  padding-top: 2px;
  padding-left: 5px;
}

.right {
  right: 15px;
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
