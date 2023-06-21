import { NotificationContext } from "../../context/NotificationContext";
import {useContext, useEffect, useState} from "react"
import Notification, { UserNotification } from "../../models/notification/UserNotification";
import Alert from "../base/alerts/Alert";
import ErrorAlert from "../base/alerts/ErrorAlert";
import useAuth from "../../services/auth";
import { generateRandomString } from "../../util/util";
import Button from "../base/Button";



export default function NotificationPanel() {

  const { notifications, addNotification, removeNotification, clear } = useContext(NotificationContext)
  const { user } = useAuth()
  const [notificationsEnabled, setNotificationsEnabled] = useState<boolean>(true)
  const [showNotifications, setShowNotifications] = useState<boolean>(true)

  useEffect(() => {
    if (!user || !notificationsEnabled) return;

    const notificationsUrl = `${process.env.REACT_APP_API_NOTIFICATION_SERVICE}/api/notifications?token=${user.token}`

    const eventSource = new EventSource(notificationsUrl, {
      withCredentials: true,
    })

    eventSource.addEventListener("UserNotification", (event: MessageEvent<string>) => {
      try {
        const userNotification: UserNotification = JSON.parse(event.data)
        const notification: Notification = {
          id: generateRandomString(5),
          ...userNotification
        }  
        addNotification(notification)
      } catch (err) {
        console.error(err)
      }
    })
    return () => { eventSource.close(); clear() }
  }, [user, notificationsEnabled])


  const getNotificationElement = (notification: Notification) => {
    switch(notification.type) {
      case "SUCCESS": 
        return <Alert 
          key={notification.id}
          title={notification.title}
          message={notification.message ?? ""}
          onClose={() => removeNotification(notification.id)}
        ></Alert>
      case "ERROR": 
        return <ErrorAlert 
          key={notification.id}
          title={notification.title}
          message={notification.message ?? ""}
          onClose={() => removeNotification(notification.id)}
        ></ErrorAlert>
    }
  }

  if (!user) return (null);

  if (!showNotifications) {
    return (
      <div className="absolute bottom-0 left-0 p-4 sm:w-96 sm:max-w-md z-50">
        <Button btnType="basic" onClick={() => setShowNotifications(prev => !prev)}>Show notifications</Button>
      </div>
    ) 
  }

  return (
    <div className="absolute bottom-0 left-0 p-4 w-[120px] sm:w-96 sm:max-w-md z-50">
      <div className="overflow-y-auto max-h-[270px] [&>*]:mb-3">
        {notifications.map(notification => 
          getNotificationElement(notification)
        )}
      </div>
      <div className="w-full flex flex-row gap-x-3">
        <Button btnType="basic" onClick={() => setShowNotifications(prev => !prev)}>Hide notifications</Button>
        <Button btnType="basic" onClick={() => setNotificationsEnabled(prev => !prev)}>
          {notificationsEnabled ? "Disable notifications" : "Enable notifications"}
        </Button>
      </div>
    </div>
  )
}