import httpStatus from "http-status"
import { useEffect, useState } from "react"
import { useLocation, useNavigate, useParams } from "react-router-dom"
import Heading from "../../components/base/Heading"
import Input from "../../components/base/Input"
import InputLabel from "../../components/base/InputLabel"
import ErrorAlert from "../../components/base/alerts/ErrorAlert"
import GroupDetail from "../../models/group/GroupDetail"
import GroupUpdate from "../../models/group/GroupUpdate"
import { LINKS, LinkType } from "../../router/router"
import useAuth from "../../services/auth"
import { getErrorFromResponse } from "../../util/util"
import Tag from "../../components/base/Tag"
import GroupUser from "../../models/group/GroupUser"


const editGroup = (groupId: string, group: GroupUpdate, token: string) => fetch(
  `http://localhost:8083/api/groups/${groupId}`,
  {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(group)
  }
)

export default function GroupUsersPage() {

  const { user } = useAuth()
  const navigate = useNavigate()
  const { groupId } = useParams()
  const { group } = useLocation().state

  const [error, setError] = useState<string>()
  const [groupName, setGroupName] = useState<string>()

  useEffect(() => {
    setGroupName(group.name)
  }, [])

  const handleSubmit: React.FormEventHandler = async (event) => {
    event.preventDefault()

    const group: GroupUpdate = {
      name: groupName!
    }

    const res = await editGroup(groupId!, group, user!.token)
    if (res.status == httpStatus.OK) {
      const editedGroup: GroupDetail = await res.json()
      navigate(LINKS[LinkType.GROUP_DETAIL].build({groupId: editedGroup.id}), {state: editedGroup})
    } else if (res.status == httpStatus.UNAUTHORIZED) {
      navigate(LINKS[LinkType.LOGIN].build())
    } else {
      const errorResponse = await getErrorFromResponse(res)
      setError(errorResponse ? errorResponse.detail : "An error occurred")
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
      <Heading classes="mb-4">Manage group members</Heading>
      <div className="w-full h-full border border-solid ring-1 ring-slate-50 rounded-md">
        
        <div className="w-3/4 h-full flex flex-col gap-y-4 mx-auto">
          <div className="my-12">
            <h4 className="font-medium w-full mb-4">Members:</h4>
            <ul className="max-h-44 overflow-auto w-full">
              {group!.users.map((user: GroupUser) => 
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
                    <div className="flex flex-row gap-x-2">
                      <button className="bg-red-700 text-white text-md hover:bg-red-600 px-2 py-1 rounded-md">Remove</button>
                      <button className="bg-yellow-600 text-white text-md hover:bg-yellow-500 px-2 py-1 rounded-md">Set Owner</button>
                    </div>
                    :
                    null
                  }
                </li>
              )}
            </ul>
          </div>

          <form onSubmit={async e => handleSubmit(e)} className="w-full mx-auto flex flex-col gap-y-4 justify-center">
            <div>
              <InputLabel htmlFor="name">New member name</InputLabel>
              <Input type="text" id="name" name="name" value={groupName??''} required onChange={e => setGroupName(e.target.value)}/>
            </div> 
            <button type="submit" className="px-3 py-2 text-white rounded-md bg-green-700 hover:bg-green-600">Submit</button>
          </form>
        </div>

      </div>
    </section>
  )
}