export default interface Notification extends UserNotification {
  id: string
}

// this is what we get from the server
export interface UserNotification {
  type: UserNotificationType
  title: string,
  message?: string,
  userId?: string, // the server also always sets this
  groupId?: string,
  groupName?: string,
}

type UserNotificationType = "SUCCESS" | "ERROR"