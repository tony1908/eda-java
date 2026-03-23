import { useState, useEffect, useRef } from "react";

export default function useSSE(url, eventTypes) {

    const [events, setEvents] = useState([]);
    const [isConnected, setIsConnected] = useState(false);
    const sourceRef = useRef(null);

    useEffect(() => {
        const source = new EventSource(url);
        sourceRef.current = source;

        source.onopen = () => {
            setIsConnected(true);
        };

        source.onerror = (error) => {
            console.error("Error al conectar con el servidor SSE:", error);
            setIsConnected(false);
        };

        eventTypes.forEach(eventType => {
            source.addEventListener(eventType, (event) => {
                const parsed = JSON.parse(event.data)
                setEvents(prev => [{
                    id: event.lastEventId,
                    type,
                    topic: parsed.topic,
                    data: parsed.data,
                    timestamp: new Date().toLocaleTimeString()
                }, ...prev].slice(0, 100))
            })
        })

        return () => {
            source.close();
        };
    }, [url, eventTypes.join(',')]);

    return { events, isConnected };
}
