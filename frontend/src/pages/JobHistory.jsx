import { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from 'react-router-dom';

export default function JobHistory() {
  const [jobs, setJobs] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    axios.get("/api/jobs/all")
      .then((res) => {setJobs(res.data);})
      .catch((err) => console.error("Error fetching jobs", err));
      
  }, []);

  return (
    <div className="p-6">
      <h2 className="text-2xl font-bold mb-4">🧾 Job History</h2>
      <div className="overflow-x-auto">
        <table className="w-full table-auto border">
          <thead>
            <tr className="bg-gray-100 text-left">
              <th className="p-2">ID</th>
              <th className="p-2">Pipeline</th>
              <th className="p-2">Status</th>
              <th className="p-2">Attempts</th>
             
            </tr>
          </thead>
          <tbody>
            {jobs.map((job) => (
              <tr key={job.id} className="border-t" onClick={() => navigate(`/jobs/${job.id}`)}>
                <td className="p-2">{job.id}</td>
                <td className="p-2">{job.pipelineName || "—"}</td>
                <td className="p-2">{job.status}</td>
                <td className="p-2">{job.attempts}</td>
               
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
