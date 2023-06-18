import httpStatus from "http-status";
import { useEffect, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import Button from "../../components/base/Button";
import Heading from "../../components/base/Heading";
import Tag from "../../components/base/Tag";
import ErrorAlert from "../../components/base/alerts/ErrorAlert";
import GroupDetail from "../../models/group/GroupDetail";
import { LINKS, LinkType } from "../../router/router";
import useAuth, { AuthenticatedUser } from "../../services/auth";
import { fetchGroup, removeGroupMember } from "../../services/groups";
import { getErrorFromResponse } from "../../util/util";


export default function GroupDetailPage() {

  const { user, removeUser } = useAuth()
  const navigate = useNavigate()

  const location = useLocation()
  const { groupId } = useParams()

  const [error, setError] = useState<string>()
  const [group, setGroup] = useState<GroupDetail>()

  const loadGroup = async (groupId: string, user: AuthenticatedUser) => {
    // if there is no group in the state fetch the data
    const groupFromLocation = location.state

    try {
      if (!groupFromLocation) {
        const res = await fetchGroup(groupId, user.token)
  
        if (res.status === httpStatus.OK) {  
          setGroup(await res.json())
        } else if (res.status === httpStatus.UNAUTHORIZED) {
          removeUser()
          navigate(LINKS[LinkType.LOGIN].build())
        } else {
          const errorResponse = await getErrorFromResponse(res)
          setError(errorResponse ? errorResponse.detail : "An error occurred")
        }
      } else {
        setGroup(groupFromLocation)
      }
    } catch (err) {
      setError("An error occurred")
    }
  }

  useEffect(() => { if (groupId && user) loadGroup(groupId, user) }, [])

  const leaveGroup = async () => {
    const res = await removeGroupMember(groupId!, user!.userId, user!.token)
    if (res.ok) {  
      navigate(LINKS[LinkType.GROUPS].build())
    } else if (res.status === httpStatus.UNAUTHORIZED) {
      removeUser()
      navigate(LINKS[LinkType.LOGIN].build())
    } else {
      const errorResponse = await getErrorFromResponse(res)
      setError(errorResponse ? errorResponse.detail : "An error occurred")
    }
  }

  const isOwner = (userId: string) => userId === group?.ownerId

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
        <div className="flex flex-row w-full flex-wrap justify-between items-center gap-x-3">

          <Button btnType="basic" className="mt-6 sm:w-1/5" onClick={() => navigate(
              LINKS[LinkType.GROUP_SESSIONS].build({groupId: group!.id})
          )}>Start session</Button>
          { isOwner(user!.userId) ?
            <div className="flex flex-row gap-x-3 mt-6 justify-end items-center w-2/3">
              <Button btnType="basic" className="sm:w-44" onClick={() => navigate(
                LINKS[LinkType.GROUP_USERS].build({groupId: group!.id}), 
                {state: group}
              )}>
                Manage members
              </Button>
              <Button btnType="basic" className="sm:w-1/3" onClick={() => navigate(
                LINKS[LinkType.GROUP_EDIT].build({groupId: group!.id}), 
                {state: group}
              )}>
                Edit
              </Button>
            </div>
            :
            <Button btnType="danger" className="mt-6 sm:w-1/4" onClick={leaveGroup}>
              Leave group
            </Button>
          }
        </div>
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
      <Heading className="mb-4">Group details</Heading>
      { group === undefined ? 
        <div>
          <h3>Loading...</h3>
        </div>
        :
        renderGroup()
      }
    </section>
  )
}