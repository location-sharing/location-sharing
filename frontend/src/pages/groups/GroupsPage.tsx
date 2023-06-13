import httpStatus from "http-status"
import { useEffect, useState } from "react"
import { Link, useNavigate } from "react-router-dom"
import Button from "../../components/base/Button"
import Heading from "../../components/base/Heading"
import { List } from "../../components/base/List"
import ListItem from "../../components/base/ListItem"
import Tag from "../../components/base/Tag"
import ErrorAlert from "../../components/base/alerts/ErrorAlert"
import Group from "../../models/group/Group"
import { LINKS, LinkType } from "../../router/router"
import useAuth, { AuthenticatedUser } from "../../services/auth"
import { getErrorFromResponse } from "../../util/util"
import { fetchGroups } from "../../services/groups"
import { fetchActiveGroupUsers } from "../../services/session"

export default function GroupsPage() {

  const navigate = useNavigate()
  const [error, setError] = useState<string>()

  interface ActiveUser {
    id: string,
    username: string
  }
  interface GroupWithActiveUsers extends Group {
    activeUsers: Array<ActiveUser>
  }
  const [groupsWithActiveUsers, setGroups] = useState<Array<GroupWithActiveUsers>>()

  const { user, removeUser } = useAuth()

  const loadGroups = async () => {
    try {
      const response = await fetchGroups(user!.token)

      if (response.status === httpStatus.OK) {  

        const groups: Array<Group> = await response.json()
        const groupsWithActiveUsers = Array<GroupWithActiveUsers>()

        for (let group of groups) {
          const users = await loadActiveGroupUsers(group.id)
          groupsWithActiveUsers.push({
            id: group.id,
            name: group.name,
            ownerId: group.ownerId,
            activeUsers: users
          })
        }

        setGroups(groupsWithActiveUsers)  
      } else if (response.status === httpStatus.UNAUTHORIZED) {
        removeUser()
        navigate(LINKS[LinkType.LOGIN].build())
      } else {
        const errorResponse = await getErrorFromResponse(response)
        setError(errorResponse ? errorResponse.detail : "An error occurred")
      }
    } catch (error: any) {      
      setError("An error occurred.")  
    }
  }

  const loadActiveGroupUsers = async (groupId: string): Promise<Array<ActiveUser>> => {
    try {
      const response = await fetchActiveGroupUsers(groupId, user!.token)
      if (response.status === httpStatus.OK) {  
        return await response.json()
      } else if (response.status === httpStatus.UNAUTHORIZED) {
        removeUser()
        navigate(LINKS[LinkType.LOGIN].build())
      } else {
        const errorResponse = await getErrorFromResponse(response)
        setError(errorResponse ? errorResponse.detail : "An error occurred")
      }
    } catch (err) {
      setError("An error occurred.")
    }
    return []
  }
  

  useEffect(() => { loadGroups() }, [])

  const renderGroups = () => {
    if (groupsWithActiveUsers?.length === 0) {
      return (
        <div>
          <p className="text-lg">It seems you are not a member of any group yet.</p>
        </div>
      )
    } else {
      return (
        <List>
          {
            groupsWithActiveUsers?.map(group => {
              return (
                <ListItem key={group.id}>
                  <div className="flex flex-row flex-wrap justify-between items-center gap-4">
                    <div className="flex- flex-col w-1/3">
                      <p className="text-gray-600 w-1/4">{group.name}</p>
                      { group.activeUsers.length > 0 ?
                        <div className="grid grid-cols-2 ">
                          <p>Online:</p>
                          <ul>
                            {group.activeUsers.map(user => <li key={user.id}>{user.username}</li>)}
                          </ul>
                        </div>
                        :
                        null
                      }
                    </div>
                    <div className="w-full sm:w-3/5 flex flex-row flex-nowrap justify-between gap-x-4">
                      { group.ownerId === user?.userId ? 
                        <Tag>Owner</Tag>
                        : 
                        null
                      }
                      <Button onClick={() => navigate(LINKS[LinkType.GROUP_SESSIONS].build({groupId: group.id}))}>
                        {group.activeUsers.length > 0 ?
                          "Join Session"
                          :
                          "Start Session"
                        }
                      </Button>
                      <Button onClick={() => navigate(LINKS[LinkType.GROUP_DETAIL].build({groupId: group.id}))}>View</Button>
                    </div>
                  </div>
                </ListItem>
              )
            })
          }
        </List>
      )
    }
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
      <div className="flex flex-row flex-wrap w-full justify-between items-center">
        <Heading classes="mb-4">
          Groups
        </Heading>
        <Link to={LINKS[LinkType.GROUP_CREATE].build()}>
          <button className="px-3 py-2 mr-4 text-white bg-green-700 rounded-md hover:bg-green-600">New Group</button>
        </Link>
      </div>
      <div className="w-full h-full flex flex-row justify-center items-center gap-x-12">
        { groupsWithActiveUsers === undefined ? 
          <div>
            <h3>Loading...</h3>
          </div>
          :
          renderGroups()
        }
      </div>
    </section>
  )
}