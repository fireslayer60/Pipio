import {React,useState} from 'react';
import axios from 'axios';


function PipelineForm({ onSubmit }) {

    const [pipelineName, setPipelineName] = useState('');
  const [yamlFile, setYamlFile] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!yamlFile || !pipelineName) {
      alert("Please provide a name and YAML file.");
      return;
    }

    const formData = new FormData();
    formData.append("name", pipelineName);
    formData.append("file", yamlFile);

    try {
      const res = await axios.post("http://localhost:8080/pipelines", formData, {
        headers: { "Content-Type": "multipart/form-data" }
      });
      console.log("Pipeline created with ID:", res.data);
    } catch (err) {
      console.error("Error creating pipeline", err);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4 p-4">
      <input
        type="text"
        placeholder="Pipeline name"
        className="border p-2 w-full"
        value={pipelineName}
        onChange={(e) => setPipelineName(e.target.value)}
      />

      <input
        type="file"
        accept=".yml,.yaml"
        className="border p-2 w-full"
        onChange={(e) => setYamlFile(e.target.files[0])}
      />

      <button
        type="submit"
        className="bg-blue-600 text-white px-4 py-2 rounded"
      >
        Create Pipeline
      </button>
    </form>
  );
}

export default PipelineForm
