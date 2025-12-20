import api, { endpoints } from './api';
import { Client } from '../types/api';

// Client Service
export const clientService = {
  // Get all clients
  getClients: async (): Promise<Client[]> => {
    const response = await api.get(endpoints.clients.list);
    return response.data;
  },

  // Get client by ID
  getClient: async (id: string): Promise<Client> => {
    const response = await api.get(endpoints.clients.get(id));
    return response.data;
  },

  // Create new client
  createClient: async (clientData: Partial<Client>): Promise<Client> => {
    const response = await api.post(endpoints.clients.create, clientData);
    return response.data;
  },

  // Update client
  updateClient: async (id: string, clientData: Partial<Client>): Promise<Client> => {
    const response = await api.put(endpoints.clients.update(id), clientData);
    return response.data;
  },

  // Delete client
  deleteClient: async (id: string): Promise<void> => {
    await api.delete(endpoints.clients.delete(id));
  },
};