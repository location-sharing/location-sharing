import httpStatus from "http-status";
import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate, useParams } from "react-router-dom";
import Heading from "../../components/base/Heading";
import GroupDetail from "../../models/group/GroupDetail";
import Button from "../../components/base/Button";
import Tag from "../../components/base/Tag";
import ErrorAlert from "../../components/base/alerts/ErrorAlert";
import { LINKS, LinkType } from "../../router/router";
import useAuth from "../../services/auth";
import { getErrorFromResponse } from "../../util/util";
import GroupUser from "../../models/group/GroupUser";

const fetchGroup = (groupId: string, token: string) => fetch(
  `http://localhost:8083/api/groups/${groupId}`,
  {
    headers: {
      'Authorization': `Bearer ${token}`
    },
  }
)

export default function GroupDetailPage() {

  const { user } = useAuth()
  const navigate = useNavigate()

  const location = useLocation()
  const { groupId } = useParams()

  const [error, setError] = useState<string>()
  const [group, setGroup] = useState<GroupDetail>()

  const loadGroup = async () => {

    // if there is no group in the state fetch the data
    const groupFromLocation = location.state?.group

    if (!groupFromLocation) {
      const res = await fetchGroup(groupId!, user!.token)

      if (res.status === httpStatus.OK) {  
        setGroup(await res.json())
      } else if (res.status === httpStatus.UNAUTHORIZED) {
        navigate(LINKS[LinkType.LOGIN].build())
      } else {
        const errorResponse = await getErrorFromResponse(res)
        setError(errorResponse ? errorResponse.detail : "An error occurred")
      }
    } else {
      setGroup(groupFromLocation)
    }
  }

  useEffect(() => { loadGroup() }, [])

  const isOwner = (userId: string) => {
    return userId === group?.ownerId
  }



  const renderGroup = () => {
    return (
      <div>
        <div className="flex flex-col gap-y-6 p-4 ring-1 ring-slate-200 rounded-md">
          <div className="grid grid-cols-2">
            <h4 className="font-medium text-xl">Name:</h4>
            <p className="text-lg">{group!.name}</p>
          </div>
          <div className="grid grid-cols-2">
            <h4 className="font-medium text-xl">Members:</h4>
            <ul className="max-h-44 overflow-auto">
              {group!.users.map(user => 
                <li className="flex flex-row gap-x-4 mb-3" key={user.name}>
                  <p className="text-lg">{user.name}</p>
                  { isOwner(user.id) ? <Tag>Owner</Tag> : null}
                </li>
              )}
            </ul>
          </div>
        </div>
        { isOwner(user!.userId) ?
          <div className="flex flex-row flex-wrap justify-end gap-x-3">
            <Button classes="mt-6 sm:w-1/4" onClick={() => navigate(LINKS[LinkType.GROUP_USERS].build({groupId: group!.id}), {state: {group}})}>
              Manage members
            </Button>
            <Button classes="mt-6 sm:w-1/6" onClick={() => navigate(LINKS[LinkType.GROUP_EDIT].build({groupId: group!.id}), {state: {group}})}>
              Edit
            </Button>
          </div>
          
          :
          null
        }
      </div>

    )
  }

  return (
    <section className="relative w-full top-12 sm:top-28 sm:max-w-2xl h-1/2 mx-auto px-4">
      { error ? 
        <div className="relative bottom-12 w-full">
          <ErrorAlert title="Error while fetching groups" message={error} onClose={() => setError(undefined)}/> 
        </div>
        : 
        null
      }
      <Heading classes="mb-4">Group details</Heading>
      {/* <div className="w-full h-full flex flex-col border border-solid border-red-500"> */}
        { group === undefined ? 
          <div>
            <h3>Loading...</h3>
          </div>
          :
          renderGroup()
        }
      {/* </div> */}
    </section>
  )
}