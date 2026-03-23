import OrderDashboard from './components/OrderDashboard'
import InventoryPanel from './components/InventoryPanel'

// TODO: Importar el hook useSSE desde './hooks/useSSE'

// TODO: Definir los tipos de evento para cada panel
// const ORDER_EVENTS = ['order.created', 'order.canceled', 'payment.completed', 'payment.failed', 'shipping.shipped']
// const INVENTORY_EVENTS = ['inventory.reserved', 'inventory.insufficient', 'stock.low']

export default function App() {
  // TODO: Reemplazar estos arrays vacios con el hook useSSE
  // const orders = useSSE('/api/events/stream', ORDER_EVENTS)
  // const inventory = useSSE('/api/events/stream', INVENTORY_EVENTS)
  const orderEvents = []
  const inventoryEvents = []
  const totalEvents = orderEvents.length + inventoryEvents.length
  const isConnected = false

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
        <OrderDashboard events={orderEvents} />
        <InventoryPanel events={inventoryEvents} />
      </main>
    </div>
  )
}
