import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axios from "axios";

export default function PipelineDetails() {
  const { id } = useParams(); // pipeline ID from URL
  const navigate = useNavigate();
  const [pipeline, setPipeline] = useState(null);
  const [secret, setSecret] = useState({ key: "", value: "", type: "ENV" });

  useEffect(() => {
    axios.get(`http://localhost:8080/pipelines/${id}`)
      .then((res) => setPipeline(res.data))
      .catch((err) => console.error("Failed to fetch pipeline", err));
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
          <div key={stageIdx} className="mb-4">
            <h3 className="font-bold text-lg text-gray-800">{stage.name}</h3>
            <ul className="ml-4 mt-2 list-disc text-sm text-gray-700">
              {stage.steps.map((step, stepIdx) => (
                <li key={stepIdx}>
                  <pre className="bg-gray-100 p-2 rounded overflow-auto whitespace-pre-wrap">{step.runCommand}</pre>
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>

      {/* Add Secret */}
      <div className="bg-white rounded-xl shadow p-4">
        <h2 className="text-xl font-semibold mb-4">Add Secret</h2>
        <div className="space-y-2">
          <input
            type="text"
            placeholder="Key"
            value={secret.key}
            onChange={(e) => setSecret({ ...secret, key: e.target.value })}
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
    </div>
  );
}
