const TYPE_CONFIG = {
  'inventory.reserved':     { label: 'Stock Reservado',    color: '#98c379', icon: '📦' },
  'inventory.insufficient': { label: 'Stock Insuficiente', color: '#e06c75', icon: '🚫' },
  'stock.low':              { label: 'Stock Bajo',         color: '#e5c07b', icon: '⚠️' },
}

export default function InventoryPanel({ events }) {
  return (
    <section className="panel inventory-panel">
      <h2>Inventario</h2>
      {events.length === 0 && (
        <p className="empty">Esperando alertas de inventario...</p>
      )}
      <div className="event-list">
        {events.map((event, i) => {
          const config = TYPE_CONFIG[event.type] || { label: event.type, color: '#00b0f0', icon: '📋' }
          const data = event.data || {}
          return (
            <div key={event.id || i} className="event-card" style={{ borderLeftColor: config.color }}>
              <div className="event-header">
                <span className="event-badge" style={{ backgroundColor: config.color }}>
                  {config.icon} {config.label}
                </span>
                <span className="event-time">{event.timestamp}</span>
              </div>
              <div className="event-body">
                {data.orderId && <div><strong>Orden:</strong> {data.orderId}</div>}
                {data.productId && <div><strong>Producto:</strong> {data.productId}</div>}
                {data.productName && <div><strong>Nombre:</strong> {data.productName}</div>}
                {data.quantity !== undefined && <div><strong>Cantidad:</strong> {data.quantity}</div>}
                {data.currentStock !== undefined && <div><strong>Stock actual:</strong> {data.currentStock}</div>}
                {data.items && data.items.map((item, j) => (
                  <div key={j} className="item-detail">
                    {item.productName}: {item.quantity} uds (stock: {item.currentStock})
                  </div>
                ))}
              </div>
            </div>
          )
        })}
      </div>
    </section>
  )
}
