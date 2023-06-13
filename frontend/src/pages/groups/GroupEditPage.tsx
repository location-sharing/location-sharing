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
import { updateGroup } from "../../services/groups"


export default function GroupEditPage() {

  const { user, removeUser } = useAuth()
  const navigate = useNavigate()
  const { groupId } = useParams()
  const groupFromLocation = useLocation().state

  const [error, setError] = useState<string>()
  const [groupName, setGroupName] = useState<string>()

  useEffect(() => {
    setGroupName(groupFromLocation.name)
  }, [])

  const handleSubmit: React.FormEventHandler = async (event) => {
    event.preventDefault()

    const group: GroupUpdate = {
      name: groupName!
    }

    const res = await updateGroup(groupId!, group, user!.token)
    if (res.status == httpStatus.OK) {
      const editedGroup: GroupDetail = await res.json()
      navigate(LINKS[LinkType.GROUP_DETAIL].build({groupId: editedGroup.id}), {state: editedGroup})
    } else if (res.status == httpStatus.UNAUTHORIZED) {
      removeUser()
      navigate(LINKS[LinkType.LOGIN].build())
    } else {
      const errorResponse = await getErrorFromResponse(res)
      setError(errorResponse ? errorResponse.detail : "An error occurred")
    }
  }

  const isOwner = () => user?.userId === groupFromLocation.ownerId

  return (
    <section className="relative w-full top-12 sm:top-28 sm:max-w-2xl h-1/2 mx-auto px-4">
      { error ? 
        <div className="relative bottom-12 mx-auto">
          <ErrorAlert title="Error while updating group" message={error} onClose={() => setError(undefined)}/> 
        </div>
        : 
        null
      }
      <Heading classes="mb-4">Edit group</Heading>
      <div className="w-full h-1/2 border border-solid ring-1 ring-slate-50 rounded-md flex flex-col justify-center">
        <form onSubmit={async e => handleSubmit(e)} className="w-1/2 mx-auto flex flex-col gap-y-4 justify-center">
          <div>
            <InputLabel htmlFor="name">New group name</InputLabel>
            <Input type="text" id="name" name="name" value={groupName??''} required onChange={e => setGroupName(e.target.value)}/>
          </div> 
          <button type="submit" className="px-3 py-2 text-white rounded-md bg-green-700 hover:bg-green-600">Submit</button>
        </form>
      </div>
    </section>
  )
}