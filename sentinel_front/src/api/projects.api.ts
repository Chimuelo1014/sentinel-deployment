import api from "./axios";

export const getProjects = (params?: {
  page?: number;
  size?: number;
  sort?: string;
}) => {
  return api.get("/api/bff/projects", { params });
};

export const getProjectById = (id: string) => {
  return api.get(`/api/bff/projects/${id}`);
};

export const createProject = (data: any) => {
  return api.post("/api/bff/projects", data);
};

export const updateProject = (id: string, data: any) => {
  return api.put(`/api/bff/projects/${id}`, data);
};

export const deleteProject = (id: string) => {
  return api.delete(`/api/bff/projects/${id}`);
};
