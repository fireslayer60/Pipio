import axios from "axios";

const API = axios.create({baseURL:"/api"});

export const fetchPipelines = () => API.get('/pipelines/getAll');
export const createPipeline = () => API.post('/pipelines');