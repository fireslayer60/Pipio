import { useState } from "react";
import { DndContext } from '@dnd-kit/core';
import { SortableContext, useSortable, arrayMove, rectSortingStrategy } from '@dnd-kit/sortable';

import { v4 as uuidv4 } from "uuid";
import yaml from "js-yaml";

function Step({ step, onChange, onDelete, id }) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({ id });
  return (
    <div ref={setNodeRef} {...attributes} {...listeners} className="bg-white rounded p-2 mb-2 shadow flex items-center">
      <textarea
        value={step.run}
        onChange={e => onChange(e.target.value)}
        className="flex-1 border-none outline-none bg-transparent text-sm"
        rows={step.run.includes('\n') ? step.run.split('\n').length : 1}
      />
      <button className="ml-2 text-red-500" onClick={onDelete}>❌</button>
    </div>
  );
}

function Stage({ stage, onNameChange, onAddStep, onStepChange, onStepDelete, stageIndex, onDeleteStage }) {
  return (
    <div className="border rounded p-4 mb-4 bg-gray-50">
      <div className="flex items-center mb-2">
        <input
          className="text-lg font-bold border-b border-gray-300 bg-transparent flex-1"
          placeholder="Stage Name"
          value={stage.name}
          onChange={e => onNameChange(e.target.value)}
        />
        <button className="ml-4 text-red-500" onClick={onDeleteStage}>Delete Stage</button>
      </div>
      <SortableContext items={stage.steps.map(s => s.id)} strategy={rectSortingStrategy}>
        {stage.steps.map((step, i) => (
          <Step
            key={step.id}
            id={step.id}
            step={step}
            onChange={text => onStepChange(i, text)}
            onDelete={() => onStepDelete(i)}
          />
        ))}
      </SortableContext>
      <button className="mt-2 text-blue-500" onClick={onAddStep}>+ Add Step</button>
    </div>
  );
}

export default function PipelineBuilder() {
  const [baseImage, setBaseImage] = useState();
  const [repoUrl, setRepoUrl] = useState();
  const [stages, setStages] = useState([]);

  // Handlers for stages and steps
  const addStage = () => {
    setStages([...stages, { name: "", steps: [] }]);
  };

  const deleteStage = idx => {
    setStages(stages.filter((_, i) => i !== idx));
  };

  const handleNameChange = (idx, val) => {
    const copy = [...stages];
    copy[idx].name = val;
    setStages(copy);
  };

  const addStep = (stageIdx) => {
    const copy = [...stages];
    copy[stageIdx].steps.push({ id: uuidv4(), run: "" });
    setStages(copy);
  };

  const handleStepChange = (stageIdx, stepIdx, val) => {
    const copy = [...stages];
    copy[stageIdx].steps[stepIdx].run = val;
    setStages(copy);
  };

  const handleStepDelete = (stageIdx, stepIdx) => {
    const copy = [...stages];
    copy[stageIdx].steps.splice(stepIdx, 1);
    setStages(copy);
  };

  // Export to YAML
  const exportYaml = () => {
    // Prepare the data structure
    const data = {
      baseImage, repoUrl,
      stages: stages.map(s => ({
        name: s.name,
        steps: s.steps.map(st => ({ run: st.run }))
      }))
    };
    const str = yaml.dump(data, { lineWidth: -1 });
    // Download file
    const blob = new Blob([str], { type: "text/yaml" });
    const link = document.createElement("a");
    link.href = URL.createObjectURL(blob);
    link.download = "pipeline.yaml";
    link.click();
  };

  return (
    <div className="max-w-3xl mx-auto py-8 space-y-4">
      <h1 className="text-2xl font-bold">Pipeline Builder</h1>
      <div className="flex space-x-4">
        <input
          className="flex-1 border p-2 rounded"
          placeholder="Base Image"
          value={baseImage}
          onChange={e => setBaseImage(e.target.value)}
        />
        <input
          className="flex-1 border p-2 rounded"
          placeholder="Repo URL"
          value={repoUrl}
          onChange={e => setRepoUrl(e.target.value)}
        />
      </div>
      <button className="bg-blue-500 text-white px-4 py-2 rounded mt-4" onClick={addStage}>+ Add Stage</button>
      {stages.map((stage, i) => (
        <Stage
          key={i}
          stage={stage}
          onNameChange={val => handleNameChange(i, val)}
          onAddStep={() => addStep(i)}
          onStepChange={(stepIdx, val) => handleStepChange(i, stepIdx, val)}
          onStepDelete={stepIdx => handleStepDelete(i, stepIdx)}
          stageIndex={i}
          onDeleteStage={() => deleteStage(i)}
        />
      ))}
      <div className="flex justify-end">
        <button className="bg-green-500 text-white px-4 py-2 rounded" onClick={exportYaml}>
          Export as YAML
        </button>
      </div>
    </div>
  );
}
