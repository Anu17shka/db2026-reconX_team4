// TICKET-ADV116 — useTradeStream() — SSE subscription returning live trades.
import { useState, useEffect } from 'react';

const MAX_TRADES = 200;

export function useTradeStream(url = '/api/v1/trades/stream') {
  const [trades, setTrades] = useState([]);
  const [isConnected, setConnected] = useState(false);

  useEffect(() => {
    const source = new EventSource(url);

    source.onopen = () => setConnected(true);
    source.onmessage = (e) => {
      const trade = JSON.parse(e.data);
      setTrades((prev) => [trade, ...prev].slice(0, MAX_TRADES));
    };
    source.onerror = () => setConnected(false);

    return () => source.close();
  }, [url]);

  return { trades, isConnected };
}
