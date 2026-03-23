import OrderDashboard from './components/OrderDashboard'
import InventoryPanel from './components/InventoryPanel'

import useSSE from './hooks/useSSE'

const ORDER_EVENTS = ['order.created', 'order.canceled', 'payment.completed', 'payment.failed', 'shipping.shipped']
const INVENTORY_EVENTS = ['inventory.reserved', 'inventory.insufficient', 'stock.low']

export default function App() {
  const orders = useSSE('/api/events/stream', ORDER_EVENTS)
  const inventory = useSSE('/api/events/stream', INVENTORY_EVENTS)
  const totalEvents = orders.events.length + inventory.events.length
  const isConnected = orders.isConnected && inventory.isConnected

  return (
    <div className="app">
      <header className="header">
        <h1>EventFlow Dashboard</h1>
        <div className="header-info">
          <span className={`status ${isConnected ? 'connected' : 'disconnected'}`}>
            {isConnected ? 'Conectado' : 'Desconectado'}
          </span>
          <span className="counter">Eventos: {totalEvents}</span>
        </div>
      </header>
      <main className="panels">
        <OrderDashboard events={orders.events} />
        <InventoryPanel events={inventory.events} />
      </main>
    </div>
  )
}
