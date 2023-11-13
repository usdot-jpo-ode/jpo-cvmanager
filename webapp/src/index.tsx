import React from 'react'
import ReactDOM from 'react-dom'
import { Provider } from 'react-redux'
import 'mapbox-gl/dist/mapbox-gl.css'
import { setupStore } from './store.js'
import App from './App.js'

ReactDOM.render(
  <Provider store={setupStore({})}>
    <App />
  </Provider>,
  document.getElementById('root')
)
