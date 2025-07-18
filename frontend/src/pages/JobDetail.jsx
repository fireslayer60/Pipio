import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import JobLogTerminal from '../components/JobLogTerminal';

const JobDetail = () => {
  const { id } = useParams();
  const [job, setJob] = useState(null);

  useEffect(() => {
    axios.get(`http://localhost:8080/jobs/details/${id}`)
      .then(res => setJob(res.data))
      .catch(err => console.error(err));
  }, [id]);

  if (!job) return <div>Loading...</div>;

  return (
    <div className="max-w-3xl mx-auto p-4">
      <h1 className="text-2xl font-bold mb-4">Job #{job.id}</h1>
      <p>Status: <span className="font-semibold">{job.status}</span></p>

      <h2 className="text-xl mt-6 mb-2">Step Timeline</h2>
        <div className="space-y-4 border-l-2 border-gray-300 pl-4">
        {job.pipeline.stages.flatMap(stage =>
            stage.steps.map((step, index) => {
            let icon = '🕓';
            if (step.status === 'SUCCESS') icon = '✅';
            else if (step.status === 'FAILURE') icon = '❌';
            else if (step.status === 'RUNNING') icon = '🚀';

            return (
                <div key={step.id ?? index} className="relative">
                <div className="absolute -left-6 top-1 text-xl">{icon}</div>
                <div className="p-3 border bg-white shadow rounded">
                    <p><strong>Command:</strong> {step.runCommand}</p>
                    <p><strong>Status:</strong> {step.status}</p>
                </div>
                </div>
            );
            })
        )}
        </div>

        <JobLogTerminal jobId={id} />

    </div>
  );
};

export default JobDetail;
