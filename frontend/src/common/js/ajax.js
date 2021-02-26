import {Message, MessageBox} from 'element-ui';
import axios from "axios";
import i18n from '../../i18n/i18n'
import {getToken, removeToken, tokenKey} from '@/utils/auth'
import { setToken } from '../../utils/auth';

export default {
  install(Vue) {

    // 登入请求不重定向
    let unRedirectUrls = new Set(['signin','ldap/signin','/signin', '/ldap/signin']);

    if (!axios) {
      window.console.error('You have to install axios');
      return
    }

    if (!Message) {
      window.console.error('You have to install Message of ElementUI');
      return
    }

    let login = function () {
      MessageBox.alert(i18n.t('commons.tips'), i18n.t('commons.prompt'), {
        callback: () => {
          removeToken();
          axios.post("/logout");
          localStorage.setItem('Admin-Token', "{}");
          window.location.href = "/login"
        }
      });
    };

    axios.defaults.withCredentials = true;

    // request拦截器
    axios.interceptors.request.use(
      config => {
        if (getToken()) {
          config.headers['Authorization'] = getToken() // 让每个请求携带自定义token 请根据实际情况自行修改
        }
        config.headers['Content-Type'] = 'application/json'
        return config
      },
      error => {
        Promise.reject(error)
      }
    )

    axios.interceptors.response.use(response => {
      const key = tokenKey();
      if(response.headers[key.toLowerCase()]){
        setToken(response.headers[key.toLowerCase()])
      }
      if (response.headers["authentication-status"] === "invalid") {
        login();
      }
      return response;
    }, error => {
      return Promise.reject(error);
    });

    function then(success, response, result) {
      if (!response.data) {
        success(response);
      } else if (response.data.success) {
        success(response.data);
      } else {
        if (response.data.message) {
          Message.warning(response.data.message);
        }
      }
      result.loading = false;
    }

    function exception(error, result, url) {
      if (error.response && error.response.status === 401 && !unRedirectUrls.has(url)) {
        login();
        return;
      }
      if (error.response && error.response.status === 403 && !unRedirectUrls.has(url)) {
        window.location.href = "/";
        return;
      }
      result.loading = false;
      window.console.error(error.response || error.message);
      if (error.response && error.response.data) {
        if (error.response.headers["authentication-status"] !== "invalid") {
          Message.error({message: error.response.data.message || error.response.data, showClose: true});
        }
      } else {
        Message.error({message: error.message, showClose: true});
      }
    }

    Vue.prototype.$get = function (url, success) {
      let result = {loading: true};
      if (!success) {
        return axios.get(url);
      } else {
        axios.get(url).then(response => {
          then(success, response, result);
        }).catch(error => {
          exception(error, result, url);
        });
        return result;
      }
    };

    Vue.prototype.$post = function (url, data, success, failure) {
      let result = {loading: true};
      if (!success) {
        return axios.post(url, data);
      } else {
        axios.post(url, data).then(response => {
          then(success, response, result);
        }).catch(error => {
          exception(error, result, url);
          if (failure) {
            then(failure, error, result);
          }
        });
        return result;
      }
    };

    Vue.prototype.$request = function (axiosRequestConfig, success, failure) {
      let result = {loading: true};
      if (!success) {
        return axios.request(axiosRequestConfig);
      } else {
        axios.request(axiosRequestConfig).then(response => {
          then(success, response, result);
        }).catch(error => {
          exception(error, result);
          if (failure) {
            then(failure, error, result);
          }
        });
        return result;
      }
    };

    Vue.prototype.$all = function (array, callback) {
      if (array.length < 1) return;
      axios.all(array).then(axios.spread(callback));
    };

    Vue.prototype.$fileDownload = function (url) {
      axios.get(url, {responseType: 'blob'})
        .then(response => {
          let fileName = window.decodeURI(response.headers['content-disposition'].split('=')[1]);
          let link = document.createElement("a");
          link.href = window.URL.createObjectURL(new Blob([response.data], {type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8"}));
          link.download = fileName;
          link.click();
        });
    };

    Vue.prototype.$fileUpload = function (url, file, files, param, success, failure) {
      let formData = new FormData();
      if (file) {
        formData.append("file", file);
      }
      if (files) {
        files.forEach(f => {
          formData.append("files", f);
        })
      }
      formData.append('request', new Blob([JSON.stringify(param)], {type: "application/json"}));
      let axiosRequestConfig = {
        method: 'POST',
        url: url,
        data: formData,
        headers: {
          'Content-Type': undefined
        }
      };
      return Vue.prototype.$request(axiosRequestConfig, success, failure);
    }

  }
}
