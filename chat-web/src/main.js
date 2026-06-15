import Vue from 'vue'
import App from './App'
import router from './router'
import ElementUI from 'element-ui';
import './assets/style/im.scss';
import './assets/iconfont/iconfont.css';
import { createPinia, PiniaVuePlugin } from 'pinia'
import httpRequest from './api/httpRequest';
import * as socketApi from './api/wssocket';
import * as messageType from './api/messageType';
import * as messageUtil from './api/messageUtil';
import emotion from './api/emotion.js';
import url from './api/url.js';
import str from './api/str.js';
import element from './api/element.js';
import * as  enums from './api/enums.js';
import * as  date from './api/date.js';
import nextSnowflakeId from './api/snowflake.js';
import './utils/directive/dialogDrag';
import useChatStore from './store/chatStore.js'
import useFriendStore from './store/friendStore.js'
import useGroupStore from './store/groupStore.js'
import useUserStore from './store/userStore.js'
import useConfigStore from './store/configStore.js'
import { initDB, getDB } from './db/index.js';


Vue.use(PiniaVuePlugin)
const pinia = createPinia()
Vue.use(ElementUI);
// 挂载全局
Vue.prototype.$wsApi = socketApi;
Vue.prototype.$msgType = messageType;
Vue.prototype.$msgUtil = messageUtil;
Vue.prototype.$date = date;
Vue.prototype.$nextSnowflakeId = nextSnowflakeId
Vue.prototype.$http = httpRequest // http请求方法
Vue.prototype.$emo = emotion; // emo表情
Vue.prototype.$url = url; // url转换
Vue.prototype.$str = str; // 字符串相关
Vue.prototype.$elm = element; // 元素操作
Vue.prototype.$enums = enums; // 枚举
Vue.prototype.$eventBus = new Vue(); // 全局事件
Vue.config.productionTip = false;

initDB().then(() => {
  new Vue({
    el: '#app',
    router,
    pinia,
    render: h => {
      // 挂载数据库
      Vue.prototype.$db = getDB();
      // 挂载全局的pinia
      Vue.prototype.chatStore = useChatStore();
      Vue.prototype.friendStore = useFriendStore();
      Vue.prototype.groupStore = useGroupStore();
      Vue.prototype.userStore = useUserStore();
      Vue.prototype.configStore = useConfigStore();
      return h(App)
    }
  })
})
