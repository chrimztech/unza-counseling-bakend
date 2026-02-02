import api from './api';

/**
 * Notification Type Definitions
 */
export interface Notification {
  id: string;
  userId: string;
  title: string;
  message: string;
  type: 'APPOINTMENT' | 'REMINDER' | 'ALERT' | 'MESSAGE' | 'SYSTEM';
  read: boolean;
  createdAt: string;
  readAt?: string;
}

/**
 * Notification Service - Handles all notification API operations
 * Required Roles: Any authenticated user (own notifications only)
 */
export const notificationService = {
  /**
   * Get user notifications
   * GET /api/v1/notifications
   */
  getUserNotifications: async (): Promise<Notification[]> => {
    const response = await api.get('/notifications');
    return response.data;
  },

  /**
   * Mark notification as read
   * PUT /api/v1/notifications/{id}/read
   */
  markAsRead: async (id: string): Promise<void> => {
    await api.put(`/notifications/${id}/read`);
  },

  /**
   * Mark all notifications as read
   * PUT /api/v1/notifications/read-all
   */
  markAllAsRead: async (): Promise<void> => {
    await api.put('/notifications/read-all');
  },

  /**
   * Get unread notification count
   * GET /api/v1/notifications/unread-count
   */
  getUnreadCount: async (): Promise<number> => {
    const response = await api.get('/notifications/unread-count');
    return response.data.count || 0;
  },

  /**
   * Delete notification
   * DELETE /api/v1/notifications/{id}
   */
  deleteNotification: async (id: string): Promise<void> => {
    await api.delete(`/notifications/${id}`);
  }
};
