import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axios from "axios";
import { MdDelete } from "react-icons/md";

export default function PipelineDetails() {
  const { id } = useParams(); // pipeline ID from URL
  const navigate = useNavigate();
  const [pipeline, setPipeline] = useState(null);
  const [secret, setSecret] = useState({ name: "", value: "", type: "env" });
  const [secrets, setSecrets] = useState([]);
  const [jobs, setJobs] = useState([]);

  useEffect(() => {
    axios.get(`http://localhost:8080/pipelines/${id}`)
      .then((res) => setPipeline(res.data))
      .catch((err) => console.error("Failed to fetch pipeline", err));
    
    axios.get(`http://localhost:8080/pipelines/${id}/getSecrets`)
      .then((res) => setSecrets(res.data))
      .catch((err) => console.error("Failed to fetch pipeline", err));
    axios.get(`http://localhost:8080/jobs/getpipeline/${id}`)
      .then(res => setJobs(res.data))
      .catch(err => setError('Failed to load jobs.'));

    
  }, [id]);

  const handleTrigger = async () => {
    try {
      await axios.post(`http://localhost:8080/pipelines/${id}/trigger`);
      alert("Triggered!");
    } catch (err) {
      alert("Trigger failed");
    }
  };

  const handleAddSecret = async () => {
    try {
      await axios.post(`http://localhost:8080/pipelines/${id}/secrets`, secret);
      alert("Secret added!");
    } catch (err) {
      alert("Failed to add secret");
    }
  };

  const handleDelete = async () => {
    try {
      await axios.delete(`http://localhost:8080/pipelines/${id}`);
      alert("Pipeline deleted");
      navigate("/"); // Go back to home
    } catch (err) {
      alert("Delete failed");
    }
  };
  const handleDeleteSecret = async (secretId) => {
    try { 
      await axios.delete(`http://localhost:8080/pipelines/secrets/${secretId}`);
      alert("Secret deleted");
    } catch (err) {
      alert("Failed to delete secret");
    }
  };

  if (!pipeline) return <div className="p-8">Loading...</div>;

  return (
    <div className="max-w-4xl mx-auto p-8 space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold">{pipeline.name}</h1>
          <p className="text-gray-500">Pipeline ID: {pipeline.id}</p>
        </div>
        <div className="space-x-2">
          <button onClick={handleTrigger} className="px-4 py-2 bg-blue-600 text-white rounded-xl hover:bg-blue-700">
            Trigger
          </button>
          <button onClick={handleDelete} className="px-4 py-2 bg-red-600 text-white rounded-xl hover:bg-red-700">
            Delete
          </button>
        </div>
      </div>

      {/* Pipeline Stages */}
      <div className="bg-white rounded-xl shadow p-4">
  <h2 className="text-xl font-semibold mb-4">Stages</h2>
  {pipeline.stages.map((stage, stageIdx) => (
    <div key={stageIdx} className="mb-6 p-4 border rounded-md bg-gray-50">
      <h3 className="font-bold text-lg text-gray-800 mb-3">{stage.name}</h3>
      {stage.steps.map((step, stepIdx) => (
        <div 
          key={stepIdx} 
          className="mb-4 p-3 bg-white rounded shadow-sm border border-gray-200"
        >
          <ul className="list-disc list-inside text-sm text-gray-700">
            {step.runCommand.split('\n').map((line, lineIdx) => {
              
                if(line.trim() === "") return null; // Skip empty lines
                
              
              return(
              <li key={lineIdx} className="whitespace-pre-wrap">{line}</li>
            )})}
          </ul>
        </div>
      ))}
    </div>
  ))}
</div>

      <div className="bg-white rounded-xl shadow p-4">
        <h2 className="text-xl font-semibold mb-4">RepoUrl</h2>
        <h3 className="font-normal text-lg text-gray-600 group-hover:bg-gray-200 px-2 rounded transition-colors duration-200">
                {pipeline.repoUrl==null ? "No Repo URL" : pipeline.repoUrl}
        </h3>
      </div>

      <div className="bg-white rounded-xl shadow p-4">
        <h2 className="text-xl font-semibold mb-4">Secrets</h2>
        {secrets.map((secret, secretIdx) => (
          <div key={secretIdx} className="mb-4">
            <div className="relative group flex items-center">
              <h3 className="font-bold text-lg text-gray-800 group-hover:bg-gray-200 px-2 rounded transition-colors duration-200">
                {secret.name}
                <span className="font-normal"> ({secret.type})</span>
              </h3>
              <MdDelete
                className="ml-2 text-2xl opacity-0 group-hover:opacity-100 transition-opacity duration-200 text-red-500 hover:text-red-700 "
                onClick={()=> {handleDeleteSecret(secret.id)}}
              >
                
              </MdDelete>
            </div>

            
          </div>
        ))}
      </div>

      {/* Add Secret */}
      <div className="bg-white rounded-xl shadow p-4">
        <h2 className="text-xl font-semibold mb-4">Add Secret</h2>
        <div className="space-y-2">
          <input
            type="text"
            placeholder="Name"
            value={secret.name}
            onChange={(e) => setSecret({ ...secret, name: e.target.value })}
            className="w-full p-2 border rounded"
          />
          <input
            type="text"
            placeholder="Value"
            value={secret.value}
            onChange={(e) => setSecret({ ...secret, value: e.target.value })}
            className="w-full p-2 border rounded"
          />
          <button
            onClick={handleAddSecret}
            className="px-4 py-2 bg-green-600 text-white rounded-xl hover:bg-green-700"
          >
            Add Secret
          </button>
        </div>
      </div>
      <div>
      <h2 className="text-xl font-bold mb-4">Jobs for Pipeline {id}</h2>
      <table className="min-w-full border">
        <thead>
          <tr>
            <th className="border px-4 py-2">Job ID</th>
            <th className="border px-4 py-2">Status</th>
            <th className="border px-4 py-2">Attempts</th>
          </tr>
        </thead>
        <tbody>
          {jobs.map(job => (
            <tr key={job.id}>
              <td className="border px-4 py-2">{job.id}</td>
              <td className="border px-4 py-2">{job.status}</td>
              <td className="border px-4 py-2">{job.attempts}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
    </div>
  );
}
