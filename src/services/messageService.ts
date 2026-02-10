import api from './api';

/**
 * Message Type Definitions
 */
export interface ConversationDto {
  conversationId: number;
  partnerId: number;
  partnerUsername: string;
  partnerEmail: string;
  partnerFirstName: string;
  partnerLastName: string;
  lastMessageContent: string;
  lastMessageTime: string;
  unreadCount: number;
}

export interface Message {
  id: number;
  senderId: number;
  senderUsername?: string;
  recipientId: number;
  recipientUsername?: string;
  subject: string;
  content: string;
  conversationId?: number;
  sentAt: string;
  isRead: boolean;
  isDelivered: boolean;
}

export interface MessageRequest {
  recipientId: number;
  subject: string;
  content: string;
}

/**
 * Message Service - Handles all message-related API operations
 */
export const messageService = {
  // ============ CONVERSATIONS ============
  
  /**
   * Get all conversations for the authenticated user
   * GET /api/v1/messages/conversations
   */
  getConversations: async (): Promise<ConversationDto[]> => {
    const response = await api.get('/messages/conversations');
    return response.data;
  },

  /**
   * Get messages with a specific conversation partner
   * GET /api/v1/messages/conversations/{partnerId}
   */
  getConversationWithPartner: async (partnerId: number): Promise<Message[]> => {
    const response = await api.get(`/messages/conversations/${partnerId}`);
    return response.data;
  },

  /**
   * Mark all messages from a partner as read
   * PUT /api/v1/messages/conversations/{partnerId}/read
   */
  markConversationAsRead: async (partnerId: number): Promise<void> => {
    await api.put(`/messages/conversations/${partnerId}/read`);
  },

  // ============ MESSAGES ============

  /**
   * Get all received messages
   * GET /api/v1/messages
   */
  getMessages: async (): Promise<Message[]> => {
    const response = await api.get('/messages');
    return response.data;
  },

  /**
   * Send a new message
   * POST /api/v1/messages
   */
  sendMessage: async (request: MessageRequest): Promise<Message> => {
    const response = await api.post('/messages', request);
    return response.data;
  },

  /**
   * Get a specific message by ID
   * GET /api/v1/messages/{id}
   */
  getMessageById: async (id: number): Promise<Message> => {
    const response = await api.get(`/messages/${id}`);
    return response.data;
  },

  /**
   * Update a message
   * PUT /api/v1/messages/{id}
   */
  updateMessage: async (id: number, request: Partial<MessageRequest>): Promise<Message> => {
    const response = await api.put(`/messages/${id}`, request);
    return response.data;
  },

  /**
   * Delete a message
   * DELETE /api/v1/messages/{id}
   */
  deleteMessage: async (id: number): Promise<void> => {
    await api.delete(`/messages/${id}`);
  },

  /**
   * Get messages by conversation ID (legacy endpoint)
   * GET /api/v1/messages/conversation/{conversationId}
   */
  getMessagesByConversation: async (conversationId: number): Promise<Message[]> => {
    const response = await api.get(`/messages/conversation/${conversationId}`);
    return response.data;
  },

  /**
   * Get messages exchanged with a specific user
   * GET /api/v1/messages/user/{userId}
   */
  getMessagesByUser: async (userId: number): Promise<Message[]> => {
    const response = await api.get(`/messages/user/${userId}`);
    return response.data;
  },

  /**
   * Search messages by content
   * GET /api/v1/messages/search?query={query}
   */
  searchMessages: async (query: string): Promise<Message[]> => {
    const response = await api.get('/messages/search', { params: { query } });
    return response.data;
  },

  /**
   * Mark a single message as read
   * PUT /api/v1/messages/{messageId}/read
   */
  markMessageAsRead: async (messageId: number): Promise<void> => {
    await api.put(`/messages/${messageId}/read`);
  },

  /**
   * Mark a single message as delivered
   * PUT /api/v1/messages/{messageId}/delivered
   */
  markMessageAsDelivered: async (messageId: number): Promise<void> => {
    await api.put(`/messages/${messageId}/delivered`);
  },
};
