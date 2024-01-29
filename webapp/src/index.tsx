import React from 'react'
import ReactDOM from 'react-dom'
import { Provider } from 'react-redux'
import 'mapbox-gl/dist/mapbox-gl.css'
import { setupStore } from './store'
import App from './App'

ReactDOM.render(
  <Provider store={setupStore({})}>
    <App />
  </Provider>,
  document.getElementById('root')
)
