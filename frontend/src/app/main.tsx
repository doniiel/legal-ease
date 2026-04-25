import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import { ConfigProvider } from 'antd'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ConfigProvider
      theme={{
        token: {
          colorPrimary: '#1a2744',
          fontFamily: "'Roboto', sans-serif",
          borderRadius: 10,
          colorBgContainer: '#ffffff',
          colorText: '#2E2E2E',
        },
        components: {
          Button: {
            controlHeight: 40,
            primaryShadow: 'none',
            padding: 20,
          },
          Input: {
            controlHeight: 44,
            borderRadius: 8,
          },
        },
      }}
    >
      <App />
    </ConfigProvider>
  </StrictMode>,
)
