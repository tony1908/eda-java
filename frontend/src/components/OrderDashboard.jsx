const TYPE_CONFIG = {
  'order.created':      { label: 'Orden Creada',     color: '#c678dd', icon: '🛒' },
  'order.canceled':     { label: 'Orden Cancelada',  color: '#5c6370', icon: '❌' },
  'payment.completed':  { label: 'Pago Completado',  color: '#98c379', icon: '✅' },
  'payment.failed':     { label: 'Pago Fallido',     color: '#e06c75', icon: '⚠️' },
  'shipping.shipped':   { label: 'Enviado',          color: '#61afef', icon: '📦' },
}

export default function OrderDashboard({ events }) {
  return (
    <section className="panel orders-panel">
      <h2>Pedidos en Tiempo Real</h2>
      {events.length === 0 && (
        <p className="empty">Esperando eventos de pedidos...</p>
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
                {data.customerId && <div><strong>Cliente:</strong> {data.customerId}</div>}
                {data.totalAmount !== undefined && <div><strong>Total:</strong> ${data.totalAmount?.toFixed(2)}</div>}
                {data.items && (
                  <div className="event-items">
                    {data.items.map((item, j) => (
                      <span key={j} className="item-tag">{item.productName} x{item.quantity}</span>
                    ))}
                  </div>
                )}
                {data.reason && <div><strong>Razon:</strong> {data.reason}</div>}
                {data.trackingNumber && <div><strong>Tracking:</strong> {data.trackingNumber}</div>}
              </div>
            </div>
          )
        })}
      </div>
    </section>
  )
}
