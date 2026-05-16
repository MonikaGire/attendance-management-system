import { useEffect, useRef, useState } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

export function useWebSocket() {
  const [connected, setConnected] = useState(false)
  const clientRef = useRef(null)
  const subscriptionsRef = useRef({})

  useEffect(() => {
    const wsUrl = (import.meta.env.VITE_WS_URL || '') + '/api/ws'

    const client = new Client({
      webSocketFactory: () => new SockJS(wsUrl),
      reconnectDelay: 5000,
      onConnect: () => {
        setConnected(true)
        Object.entries(subscriptionsRef.current).forEach(([topic, callback]) => {
          client.subscribe(topic, (msg) => callback(JSON.parse(msg.body || 'null')))
        })
      },
      onDisconnect: () => setConnected(false),
      onStompError: (frame) => {
        console.error('STOMP error', frame)
        setConnected(false)
      },
    })

    client.activate()
    clientRef.current = client

    return () => {
      client.deactivate()
    }
  }, [])

  const subscribe = (topic, callback) => {
    subscriptionsRef.current[topic] = callback
    if (clientRef.current?.connected) {
      clientRef.current.subscribe(topic, (msg) =>
        callback(JSON.parse(msg.body || 'null'))
      )
    }
  }

  const unsubscribe = (topic) => {
    delete subscriptionsRef.current[topic]
  }

  return { connected, subscribe, unsubscribe }
}
