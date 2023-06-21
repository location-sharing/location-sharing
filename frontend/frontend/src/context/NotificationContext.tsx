import { PropsWithChildren, createContext, useState } from "react"
import Notification from "../models/notification/UserNotification"
import { generateRandomString } from "../util/util"

export const NotificationContext = createContext({
  notifications: Array<Notification>(),
  success: (message: string, title?: string) => {},
  error: (message: string, title?: string) => {}, 
  addNotification: (notification: Notification) => {},
  removeNotification: (id: string) => {},
  clear: () => {}
})


const notificationsUrl = `${process.env.REACT_APP_API_NOTIFICATION_SERVICE}/api/notifications`
export default function NotificationProvider(props: PropsWithChildren) {

  const [notifications, setNotifications] = useState<Array<Notification>>([])

  const success = (message: string, title?: string) => {
    const notificationTitle = title ?? "Success"
    const notification: Notification = {
      id: generateRandomString(5),
      type: "SUCCESS",
      title: notificationTitle,
      message: message,
    }
    setNotifications(prevNotifications => [notification, ...prevNotifications])
  }

  const error = (message: string, title?: string) => {
    const notificationTitle = title ?? "Error"
    const notification: Notification = {
      id: generateRandomString(5),
      type: "ERROR",
      title: notificationTitle,
      message: message,
    }
    setNotifications(prevNotifications => [notification, ...prevNotifications])
  }

  const addNotification = (notification: Notification) => {
    setNotifications(prevNotifications => [notification, ...prevNotifications])
  }

  const removeNotification = (id: string) => {
    setNotifications(prevNotifications => prevNotifications.filter(notification => notification.id !== id))
  }

  const clear = () => { setNotifications([]) }

  return (
    <NotificationContext.Provider value={{
      notifications,
      success,
      error,
      addNotification,
      removeNotification,
      clear
    }}>
      {props.children}
    </NotificationContext.Provider>
  )
}