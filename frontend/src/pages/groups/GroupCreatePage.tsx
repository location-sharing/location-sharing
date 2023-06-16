import httpStatus from "http-status"
import { useState } from "react"
import { useNavigate } from "react-router-dom"
import Heading from "../../components/base/Heading"
import Input from "../../components/base/Input"
import InputLabel from "../../components/base/InputLabel"
import ErrorAlert from "../../components/base/alerts/ErrorAlert"
import GroupCreate from "../../models/group/GroupCreate"
import { LINKS, LinkType } from "../../router/router"
import useAuth from "../../services/auth"
import { getErrorFromResponse } from "../../util/util"
import GroupDetail from "../../models/group/GroupDetail"
import { createGroup } from "../../services/groups"

export default function GroupCreatePage() {

  const { user, removeUser } = useAuth()
  const navigate = useNavigate()

  const [error, setError] = useState<string>()

  const [groupName, setGroupName] = useState<string>()

  const handleSubmit: React.FormEventHandler = async (event) => {
    event.preventDefault()

    const group: GroupCreate = {
      name: groupName!
    }

    const res = await createGroup(group, user!.token)
    if (res.ok) {
      const createdGroup: GroupDetail = await res.json()
      navigate(LINKS[LinkType.GROUP_DETAIL].build({groupId: createdGroup.id}), { state: createdGroup })

    } else if (res.status === httpStatus.UNAUTHORIZED) {
      removeUser()
      navigate(LINKS[LinkType.LOGIN].build())
    } else {
      const errorResponse = await getErrorFromResponse(res)
      setError(errorResponse ? errorResponse.detail : "An error occurred")
    }
  }

  return (
    <section className="relative w-full top-12 sm:top-28 sm:max-w-2xl h-1/2 mx-auto px-4">
      { error ? 
        <div className="relative bottom-12 mx-auto">
          <ErrorAlert title="Error while creating group" message={error} onClose={() => setError(undefined)}/> 
        </div>
        : 
        null
      }
      <Heading className="mb-4">Create a new group</Heading>
      <div className="w-full h-1/2 border border-solid ring-1 ring-slate-50 rounded-md">
        <form onSubmit={async e => handleSubmit(e)} className="w-1/2 h-full mx-auto flex flex-col gap-y-4 justify-center">
          <div>
            <InputLabel htmlFor="name">Group name</InputLabel>
            <Input type="text" id="name" name="name" required onChange={e => setGroupName(e.target.value)}></Input>
          </div>
          <button type="submit" className="px-3 py-2 text-white rounded-md bg-green-700 hover:bg-green-600">Submit</button>
        </form>
      </div>
    </section>
  )
}