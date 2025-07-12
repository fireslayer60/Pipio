import {useEffect, useState,React }from 'react'
import { fetchPipelines, createPipeline } from '../api/pipelineApi';
import PipelineCard from '../components/PipelineCard';
import PipelineForm from '../components/PipelineForm';
import { useNavigate } from 'react-router-dom';

const Pipelines = () => {
  const [pipelines, setPipelines] = useState([]);
  const navigate = useNavigate();

  const loadPipelines = async () => {
    const { data } = await fetchPipelines();

    setPipelines(data);
  };

  const handleCreate = async (newPipeline) => {
    await createPipeline(newPipeline);
    loadPipelines(); 
  };

  useEffect(() => {
    loadPipelines();
  }, []);

  return (
    <div className="max-w-2xl mx-auto p-6">
      <h1 className="text-2xl font-bold mb-4">Pipelines</h1>
      <PipelineForm onSubmit={handleCreate} />
      <button
        onClick={() => navigate('/jobs')}
        className="bg-green-600 text-white px-4 py-2 mb-4 rounded hover:bg-green-700"
      >
        View Job History
      </button>
      {pipelines.map((pipeline) => (
        <PipelineCard key={pipeline.id} pipeline={pipeline} />
      ))}
    </div>
  );
};

export default Pipelines;
