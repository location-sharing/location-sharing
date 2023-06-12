export type GroupEventType = 
"CONNECTED_NOTIFICATION" |
"DISCONNECTED_NOTIFICATION" |
"MESSAGE"; 

export default interface GroupEvent {
  groupId: string,
  userId: string,
  type: GroupEventType,
  payload?: string
}