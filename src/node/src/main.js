import Vue from 'vue'
import i18n from './plugins/i18n'
import router from './plugins/router'
import Apps from './Apps.vue'
import VueMomentJS from 'vue-momentjs'
import moment from 'moment'

import './plugins/axios'
import './plugins/vuetify'
import './plugins/logger'
import 'roboto-fontface/css/roboto/roboto-fontface.css'
import 'material-design-icons-iconfont/dist/material-design-icons.css'

import '@progress/kendo-ui'
import '@progress/kendo-theme-default/dist/all.css'

import { TreeViewInstaller } from '@progress/kendo-treeview-vue-wrapper'
import { GridInstaller } from '@progress/kendo-grid-vue-wrapper'
import { DataSourceInstaller } from '@progress/kendo-datasource-vue-wrapper'


Vue.config.productionTip = false

Vue.use(TreeViewInstaller)
Vue.use(GridInstaller)
Vue.use(DataSourceInstaller)

Vue.use(VueMomentJS, moment)



new Vue({
  i18n,
  router,
  render: h => h(Apps)
}).$mount('#app')
