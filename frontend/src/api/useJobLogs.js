import { useEffect, useState } from 'react';

const useJobLogs = (jobId) => {
  const [logs, setLogs] = useState([]);
  const [socket, setSocket] = useState(null);

  useEffect(() => {
    if (!jobId) return;

    const ws = new WebSocket(`ws://localhost:8080/ws/logs/${jobId}`);
    setSocket(ws);

    ws.onopen = () => console.log("✅ WebSocket connected");
    ws.onmessage = (event) => {
      setLogs((prevLogs) => [...prevLogs, event.data]);
      console.log(event);
    };
    ws.onerror = (err) => console.error("❌ WebSocket error", err);
    ws.onclose = () => console.log("❌ WebSocket closed");

    return () => ws.close();
  }, [jobId]);

  return { logs, socket };
};

export default useJobLogs;
