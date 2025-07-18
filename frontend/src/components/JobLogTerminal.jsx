import React from 'react';
import useJobLogs from '../api/useJobLogs';

const JobLogTerminal = ({ jobId }) => {
  const { logs } = useJobLogs(jobId);

  return (
    <div className="bg-black text-green-400 font-mono p-4 rounded-lg h-80 overflow-y-auto shadow-md">
      {logs.map((log, idx) => (
        <div key={idx}>{log}</div>
      ))}
    </div>
  );
};

export default JobLogTerminal;
