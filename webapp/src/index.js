import React from 'react'
import ReactDOM from 'react-dom'
import { Provider } from 'react-redux'
import 'mapbox-gl/dist/mapbox-gl.css'
import App from './App'
import store from './store.js'

ReactDOM.render(
    <Provider store={store}>
        <App />
    </Provider>,
    document.getElementById('root')
)
