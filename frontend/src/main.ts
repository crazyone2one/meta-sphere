import {createApp} from 'vue'
import './style.css'
import App from './App.vue'
import 'virtual:uno.css'
import naive from './naive'
import router from './router'
import store from './store'

const app = createApp(App)
app.use(naive)
app.use(router)
app.use(store)
app.mount('#app')
