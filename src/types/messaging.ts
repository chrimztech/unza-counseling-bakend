/**
 * Frontend TypeScript Interfaces for Messaging
 * Compatible with backend Message entity and ConversationDto
 */

// Message interface matching backend Message entity
export interface Message {
  id: string;
  senderId: string;
  recipientId: string;
  sender?: MessageParticipant;
  recipient?: MessageParticipant;
  subject: string;
  content: string;
  conversationId?: string;
  read: boolean;
  delivered: boolean;
  archived: boolean;
  starred: boolean;
  sentAt: string;
  readAt?: string;
}

// Message participant (sender/recipient info)
export interface MessageParticipant {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  profilePicture?: string;
  userType: 'COUNSELOR' | 'CLIENT' | 'ADMIN';
  specialization?: string; // For counselors
  studentId?: string; // For clients
  programme?: string; // For clients
}

// Conversation interface matching backend ConversationDto
export interface Conversation {
  conversationId: string;
  partnerId: string;
  partnerUsername: string;
  partnerEmail: string;
  partnerFirstName: string;
  partnerLastName: string;
  partnerFullName: string;
  partnerProfilePicture?: string;
  partnerType: 'COUNSELOR' | 'CLIENT';
  partnerSpecialization?: string;
  partnerStudentId?: string;
  partnerProgramme?: string;
  lastMessageContent?: string;
  lastMessageTime?: string;
  unreadCount: number;
  isOnline?: boolean;
  lastSeen?: string;
}

// Message request for sending messages
export interface SendMessageRequest {
  recipientId: number;
  subject: string;
  content: string;
}

// WebSocket message payload
export interface WebSocketMessage {
  type: 'NEW_MESSAGE' | 'TYPING_START' | 'TYPING_STOP' | 'DELIVERED' | 'READ' | 'ONLINE' | 'OFFLINE';
  payload: any;
  timestamp: string;
}

// Typing indicator payload
export interface TypingIndicator {
  type: 'TYPING_START' | 'TYPING_STOP';
  partnerId: string;
  partnerName?: string;
  timestamp?: string;
}

// Delivery status payload
export interface DeliveryStatus {
  type: 'DELIVERED' | 'READ';
  messageId: string;
  deliveredAt?: string;
  readAt?: string;
}

// Message statistics
export interface MessageStatistics {
  totalMessages: number;
  unreadMessages: number;
  sentMessages: number;
  receivedMessages: number;
}

// Unread count response
export interface UnreadCountResponse {
  count: number;
}

// Message search result
export interface SearchResult {
  id: string;
  content: string;
  senderName: string;
  recipientName: string;
  sentAt: string;
  conversationId?: string;
}

// Bulk operation request
export interface BulkOperationRequest {
  messageIds: string[];
}

// Reply request
export interface ReplyRequest {
  content: string;
  originalMessageId?: string;
}

// Forward request
export interface ForwardRequest {
  messageId: string;
  recipientIds: number[];
  additionalContent?: string;
}

// Message state for frontend
export interface MessageState {
  messages: Message[];
  conversations: Conversation[];
  selectedConversation: Conversation | null;
  currentMessages: Message[];
  unreadCount: number;
  isTyping: boolean;
  typingPartnerId: string | null;
  isLoading: boolean;
  error: string | null;
}

// WebSocket connection state
export interface WebSocketState {
  connected: boolean;
  reconnecting: boolean;
  error: string | null;
}

// Notification payload from WebSocket
export interface NotificationPayload {
  type: string;
  message?: Message;
  senderId?: string;
  senderName?: string;
  data?: any;
}
