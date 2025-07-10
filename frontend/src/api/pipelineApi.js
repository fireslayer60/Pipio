import axios from "axios";

const API = axios.create({baseURL:"http://localhost:8080"});

export const fetchPipelines = () => API.get('/pipelines/getAll');
export const createPipeline = () => API.post('/pipelines');