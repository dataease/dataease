import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex);

const Dataset = {
  state: {
    sceneData: "",
    table: ""
  },
  mutations: {
    setSceneData(state, sceneData) {
      state.sceneData = sceneData;
    },
    setTable(state, table) {
      state.table = table;
    }
  }
}

const Chart = {
  state: {
    chartSceneData: "",
    chart: "",
    tableId: "",
    sceneId: "",
    viewId: ""
  },
  mutations: {
    setChartSceneData(state, chartSceneData) {
      state.chartSceneData = chartSceneData;
    },
    setChart(state, chart) {
      state.chart = chart;
    },
    setTableId(state, tableId) {
      state.tableId = tableId;
    },
    setSceneId(state, sceneId) {
      state.sceneId = sceneId;
    },
    setViewId(state, viewId) {
      state.viewId = viewId;
    }
  }
}

const Common = {
  state: {
    projectId: ""
  },
  mutations: {
    setProjectId(state, projectId) {
      state.projectId = projectId;
    }
  }
}

const API = {
  state: {
    test: {}
  },
  mutations: {
    setTest(state, test) {
      state.test = test;
    },
    clearTest(state) {
      state.test = {};
    }
  }
}

const Switch = {
  state: {
    value: "new"
  },
  mutations: {
    setValue(state, value) {
      state.value = value;
    }
  }
}

const IsReadOnly = {
  state: {
    flag: true
  },
  mutations: {
    setFlag(state, value) {
      state.flag = value;
    }
  }
}

export default new Vuex.Store({
  modules: {
    api: API,
    common: Common,
    switch: Switch,
    isReadOnly: IsReadOnly,
    dataset: Dataset,
    chart: Chart
  }
})
