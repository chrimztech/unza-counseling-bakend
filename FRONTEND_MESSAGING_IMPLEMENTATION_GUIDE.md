# Frontend Messaging Implementation Guide

This guide explains how to implement the messaging feature in your React frontend using the backend API.

## Backend API Endpoints

### Messages Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/messages/send` | Send a new message |
| POST | `/api/v1/messages` | Send a message (alternative) |
| GET | `/api/v1/messages` | Get all received messages |
| GET | `/api/v1/messages/{id}` | Get message by ID |
| PUT | `/api/v1/messages/{id}` | Update a message |
| DELETE | `/api/v1/messages/{id}` | Delete a message |
| GET | `/api/v1/messages/conversation/{conversationId}` | Get messages by conversation |
| GET | `/api/v1/messages/search?query=` | Search messages |
| PUT | `/api/v1/messages/{id}/read` | Mark message as read |
| PUT | `/api/v1/messages/{id}/delivered` | Mark message as delivered |
| PUT | `/api/v1/messages/read-all` | Mark all messages as read |
| GET | `/api/v1/messages/unread-count` | Get unread count |
| GET | `/api/v1/messages/statistics` | Get message statistics |

### Conversations Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/conversations` | Get all conversations |
| GET | `/api/v1/conversations/{partnerId}` | Get messages with partner |
| PUT | `/api/v1/conversations/{partnerId}/read` | Mark conversation as read |

### Additional Features

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/messages/{id}/reply` | Reply to a message |
| POST | `/api/v1/messages/{id}/forward` | Forward a message |
| PUT | `/api/v1/messages/{id}/archive` | Archive a message |
| PUT | `/api/v1/messages/{id}/star` | Star a message |
| GET | `/api/v1/messages/archived` | Get archived messages |
| GET | `/api/v1/messages/starred` | Get starred messages |
| POST | `/api/v1/messages/bulk-delete` | Bulk delete messages |
| POST | `/api/v1/messages/bulk-read` | Bulk mark as read |

---

## Message Service Usage

Your existing `messageService.ts` is already configured. Here's how to use each feature:

### 1. Getting Conversations

```typescript
import { messageService } from '../services';

// Get all conversations
const conversations = await messageService.getConversations();
console.log(conversations);
// Output: ConversationDto[] with partner info, last message, unread count
```

### 2. Getting Messages with a Partner

```typescript
// Get messages exchanged with a specific user
const partnerId = 25;
const messages = await messageService.getConversationWithPartner(partnerId);
console.log(messages);
// Output: Message[] sorted by sentAt descending
```

### 3. Sending a Message

```typescript
const newMessage = await messageService.sendMessage({
  recipientId: 25,
  subject: 'Hello',
  content: 'How are you doing?'
});
console.log(newMessage);
```

### 4. Replying to a Message

```typescript
const reply = await messageService.replyToMessage(
  messageId: 123,
  content: 'This is my reply!'
);
console.log(reply);
```

### 5. Forwarding a Message

```typescript
const forwarded = await messageService.forwardMessage(
  messageId: 123,
  recipientIds: [25, 26, 27],  // Multiple recipients
  additionalContent: 'Check this out!'  // Optional additional content
);
console.log(forwarded);  // Array of forwarded messages
```

### 6. Marking Messages as Read

```typescript
// Mark single message as read
await messageService.markMessageAsRead('123');

// Mark conversation as read
await messageService.markConversationAsRead(partnerId);

// Mark all messages as read
await messageService.markAllAsRead();
```

### 7. Getting Unread Count

```typescript
const count = await messageService.getUnreadCount();
console.log(`You have ${count} unread messages`);
```

### 8. Message Statistics

```typescript
const stats = await messageService.getMessageStatistics();
console.log(stats);
// Output: { totalMessages, unreadMessages, sentMessages, receivedMessages }
```

### 9. Archiving & Starring Messages

```typescript
// Archive a message
await messageService.archiveMessage('123');

// Unarchive a message
await messageService.unarchiveMessage('123');

// Star a message
await messageService.starMessage('123');

// Unstar a message
await messageService.unstarMessage('123');

// Get archived messages
const archived = await messageService.getArchivedMessages();

// Get starred messages
const starred = await messageService.getStarredMessages();
```

### 10. Searching Messages

```typescript
const results = await messageService.searchMessages('help');
console.log(results);
```

### 11. Bulk Operations

```typescript
// Bulk delete
await messageService.bulkDeleteMessages(['123', '124', '125']);

// Bulk mark as read
await messageService.bulkMarkAsRead(['123', '124', '125']);
```

---

## Redux Integration Example

If using Redux Toolkit with RTK Query:

```typescript
// features/messages/messagesApi.ts
import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';

export const messagesApi = createApi({
  reducerPath: 'messagesApi',
  baseQuery: fetchBaseQuery({ baseUrl: '/api/v1' }),
  endpoints: (builder) => ({
    getConversations: builder.query({
      query: () => '/conversations',
    }),
    getConversationWithPartner: builder.query({
      query: (partnerId) => `/conversations/${partnerId}`,
    }),
    sendMessage: builder.mutation({
      query: (body) => ({
        url: '/messages/send',
        method: 'POST',
        body,
      }),
    }),
    markAsRead: builder.mutation({
      query: (messageId) => ({
        url: `/messages/${messageId}/read`,
        method: 'PUT',
      }),
    }),
    markConversationAsRead: builder.mutation({
      query: (partnerId) => ({
        url: `/conversations/${partnerId}/read`,
        method: 'PUT',
      }),
    }),
  }),
});

export const {
  useGetConversationsQuery,
  useGetConversationWithPartnerQuery,
  useSendMessageMutation,
  useMarkAsReadMutation,
  useMarkConversationAsReadMutation,
} = messagesApi;
```

### Usage in Components

```typescript
// MessagesScreen.tsx
import { useGetConversationsQuery, useSendMessageMutation } from '../features/messages/messagesApi';

const MessagesScreen = () => {
  const { data: conversations, isLoading } = useGetConversationsQuery();
  const [sendMessage] = useSendMessageMutation();

  const handleSendMessage = async (text: string) => {
    await sendMessage({
      recipientId: selectedPartnerId,
      subject: 'New Message',
      content: text,
    });
  };

  if (isLoading) return <LoadingSpinner />;

  return (
    <div>
      <ConversationList conversations={conversations} />
    </div>
  );
};
```

---

## TypeScript Interfaces

Your frontend types should match these backend DTOs:

```typescript
// Message interface
interface Message {
  id: string;
  senderId: string;
  recipientId: string;
  sender?: {
    id: string;
    firstName: string;
    lastName: string;
    email: string;
  };
  recipient?: {
    id: string;
    firstName: string;
    lastName: string;
    email: string;
  };
  subject: string;
  content: string;
  conversationId?: string;
  read: boolean;
  delivered: boolean;
  createdAt: string;
  readAt?: string;
}

// Conversation interface
interface Conversation {
  id: string;
  participantId: string;
  participantName: string;
  participantEmail: string;
  lastMessage: Message;
  unreadCount: number;
  messages: Message[];
}

// Message Request
interface MessageRequest {
  recipientId: number;
  subject: string;
  content: string;
}
```

---

## Implementation Checklist

- [ ] Ensure backend is running
- [ ] Update messageService.ts with endpoint calls (already done)
- [ ] Create Redux slices/queries for state management
- [ ] Build MessagesScreen component
- [ ] Implement conversation list sidebar
- [ ] Implement message thread view
- [ ] Add text input and send button
- [ ] Handle message sending errors
- [ ] Implement unread count badge
- [ ] Add loading states
- [ ] Implement search functionality
- [ ] Add archive/star functionality
- [ ] Test with real backend data

---

## Testing with cURL

Test the API directly:

```bash
# Get conversations
curl -X GET http://localhost:8080/api/v1/conversations \
  -H "Authorization: Bearer <your-token>"

# Send a message
curl -X POST http://localhost:8080/api/v1/messages/send \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{"recipientId": 25, "subject": "Hello", "content": "Test message"}'

# Mark conversation as read
curl -X PUT http://localhost:8080/api/v1/conversations/25/read \
  -H "Authorization: Bearer <your-token>"
```

---

## Error Handling

Common errors and solutions:

| Error | Solution |
|-------|----------|
| 401 Unauthorized | Check authentication token |
| 403 Forbidden | User doesn't have permission |
| 404 Not Found | Verify endpoint URL |
| 500 Server Error | Check backend logs |

```typescript
try {
  const result = await messageService.getConversations();
} catch (error) {
  console.error('Failed to fetch conversations:', error);
  // Show error message to user
}
```

---

## Next Steps

1. **Rebuild the backend** after changes:
   ```bash
   ./mvnw clean compile
   ```

2. **Start the backend**:
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Test the frontend** - Your `messageService.ts` is already configured to use these endpoints!

4. **Debugging tips**:
   - Check browser console for API errors
   - Verify CORS settings on backend
   - Ensure authentication token is being sent
   - Check network tab for request/response details
