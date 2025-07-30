import React from 'react'
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Pipelines from "./pages/Pipelines";
import JobHistory from "./pages/JobHistory";
import JobDetail from './pages/JobDetail';
import PipelineDetails from './pages/PipelineDetails';
import PipelineBuilder from './pages/PipelineBuilder';
export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Pipelines />} />
        <Route path="/jobs" element={<JobHistory />} /> 
        <Route path="/jobs/:id" element={<JobDetail />} />
        <Route path="/pipeline/:id" element={<PipelineDetails />} />
        <Route path="/builder" element={<PipelineBuilder />} />


      </Routes>
    </BrowserRouter>
  );
}
