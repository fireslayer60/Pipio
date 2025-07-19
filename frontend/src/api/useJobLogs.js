import { useEffect, useState } from 'react';

const useJobLogs = (jobId) => {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!jobId) return;

    const fetchLogs = async () => {
      setLoading(true);
      setError(null);

      try {
        const response = await fetch(`http://localhost:8080/jobs/logs/${jobId}`);
        if (!response.ok) throw new Error('Failed to fetch logs');

        const data = await response.json();
        const messages = data.map((log) => log.logMessage); // Extract just the messages
        setLogs(messages);
      } catch (err) {
        console.error("❌ Error fetching logs:", err);
        setError(err.message || 'Unknown error');
      } finally {
        setLoading(false);
      }
    };

    fetchLogs();
  }, [jobId]);

  return { logs, loading, error };
};

export default useJobLogs;
