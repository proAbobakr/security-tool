import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App'
import './index.css'

// Entry point of the React application
// This is where React attaches to the DOM
ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
)
