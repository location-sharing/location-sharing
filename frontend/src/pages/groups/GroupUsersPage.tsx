import httpStatus from "http-status"
import { useEffect, useState } from "react"
import { useLocation, useNavigate } from "react-router-dom"
import Heading from "../../components/base/Heading"
import Input from "../../components/base/Input"
import InputLabel from "../../components/base/InputLabel"
import Tag from "../../components/base/Tag"
import Alert from "../../components/base/alerts/Alert"
import ErrorAlert from "../../components/base/alerts/ErrorAlert"
import GroupDetail from "../../models/group/GroupDetail"
import GroupUser from "../../models/group/GroupUser"
import { LINKS, LinkType } from "../../router/router"
import useAuth from "../../services/auth"
import { addGroupMember, changeGroupOwner, fetchGroup, removeGroupMember } from "../../services/groups"
import { getErrorFromResponse } from "../../util/util"


export default function GroupUsersPage() {

  const { user } = useAuth()
  const navigate = useNavigate()

  const groupFromLocation = useLocation().state

  const [error, setError] = useState<string>()
  const [notification, setNotification] = useState<string>()

  const [group, setGroup] = useState<GroupDetail>()
  const [memberName, setMemberName] = useState<string>()

  useEffect(() => {    
    setGroup(groupFromLocation)
  }, [groupFromLocation])

  const handleAddMemberSubmit: React.FormEventHandler = async (event) => {
    event.preventDefault()

    try {
      const res = await addGroupMember(group?.id!, memberName!, user!.token)

      if (res.ok) {
  
        // TODO: wait for a confirm notification or update the UI optimistically (add the user to the list)
        setNotification(`If user '${memberName}' exists, it should be added within a couple seconds.`)
  
      } else if (res.status == httpStatus.UNAUTHORIZED) {
        navigate(LINKS[LinkType.LOGIN].build())
      } else {
        const errorResponse = await getErrorFromResponse(res)
        setError(errorResponse ? errorResponse.detail : "An error occurred")
      }
    } catch (err) {
      setError("An error occurred")
    }
  }

  const reloadGroup = async () => {
    try {
      const res = await fetchGroup(group!.id, user!.token)
      if (res.ok) {
        setGroup(await res.json())  
      } else if (res.status == httpStatus.UNAUTHORIZED) {
        navigate(LINKS[LinkType.LOGIN].build())
      } else {
        const errorResponse = await getErrorFromResponse(res)
        setError(errorResponse ? errorResponse.detail : "An error occurred")
      }
    } catch (err) {
      setError("An error occurred")
    }
  }
  
  const removeGroupUser = async (userId: string) => {
    try {
      const res = await removeGroupMember(group!.id, userId, user!.token)
      if (res.ok) {
        reloadGroup()
      } else if (res.status == httpStatus.UNAUTHORIZED) {
        navigate(LINKS[LinkType.LOGIN].build())
      } else {
        const errorResponse = await getErrorFromResponse(res)
        setError(errorResponse ? errorResponse.detail : "An error occurred")
      }
    } catch (err) {
      setError("An error occurred")
    }
  }

  const changeOwner = async (username: string) => {
    try {
      const res = await changeGroupOwner(group!.id, username, user!.token)
      if (res.ok) {
        navigate(LINKS[LinkType.GROUP_DETAIL].build({ groupId: group!.id }))
      } else if (res.status == httpStatus.UNAUTHORIZED) {
        navigate(LINKS[LinkType.LOGIN].build())
      } else {
        const errorResponse = await getErrorFromResponse(res)
        setError(errorResponse ? errorResponse.detail : "An error occurred")
      }
    } catch (err) {
      setError("An error occurred")
    }
  }

  const isOwner = (userId: string) => userId === group?.ownerId

  return (
    <section className="relative w-full top-12 sm:top-28 sm:max-w-2xl h-1/2 mx-auto px-4">
      { error ? 
        <div className="relative bottom-12 mx-auto">
          <ErrorAlert title="Error while updating group" message={error} onClose={() => setError(undefined)}/> 
        </div>
        : 
        null
      }
      { notification ? 
        <div className="relative bottom-12 mx-auto">
          <Alert title="Notification" message={notification} onClose={() => setNotification(undefined)}/> 
        </div>
        : 
        null
      }
      <Heading classes="mb-4">Manage group members</Heading>
      <div className="w-full h-full border border-solid ring-1 ring-slate-50 rounded-md">
        
        <div className="w-3/4 h-full flex flex-col gap-y-4 mx-auto">
          <div className="my-12">
            <h4 className="font-medium w-full mb-4">Members:</h4>
            <ul className="max-h-44 overflow-auto w-full">
              {group?.users.map((user: GroupUser) => 
                <li className="flex flex-row gap-x-4 mb-3 items-center w-full justify-between" key={user.name}>
                  <div className="flex flex-row items-center gap-x-3">
                    <p className="text-lg">{user.name}</p>
                    { isOwner(user.id) ? 
                      <Tag>Owner</Tag> 
                      : 
                      null
                    }
                  </div>
                  { !isOwner(user.id) ?
                    <div className="flex flex-row gap-x-2 p-2">
                      <button onClick={() => { removeGroupUser(user.id) }} className="bg-red-700 text-white text-md hover:bg-red-600 px-2 py-1 rounded-md">Remove</button>
                      <button onClick={() => { changeOwner(user.name) }} className="bg-theme-bg-1 font-medium ring-2 ring-slate-100 text-md hover:opacity-80 px-2 py-1 rounded-md">Set Owner</button>
                    </div>
                    :
                    null
                  }
                </li>
              )}
            </ul>
          </div>

          <form onSubmit={async e => handleAddMemberSubmit(e)} className="w-full mx-auto flex flex-row justify-between gap-4 flex-wrap items-end">
            <div className="w-full flex-1">
              <InputLabel htmlFor="name">New member's username</InputLabel>
              <Input type="text" id="name" name="name" required onChange={e => setMemberName(e.target.value)}/>
            </div> 
            <button type="submit" className="w-full sm:w-1/3 px-3 py-2 h-10 text-white rounded-md bg-green-700 hover:bg-green-600">Add member</button>
          </form>
        </div>

      </div>
    </section>
  )
}