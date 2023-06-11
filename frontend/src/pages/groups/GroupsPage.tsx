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
import useAuth from "../../services/auth"
import { getErrorFromResponse } from "../../util/util"

const groupsUrl = "http://localhost:8083/api/groups"

const fetchGroups = (token: string) => fetch(
  groupsUrl,
  {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  }
)

export default function GroupsPage() {

  const navigate = useNavigate()
  const [error, setError] = useState<string>()
  const [groups, setGroups] = useState<Array<Group>>()

  const { user } = useAuth()

  const loadGroups = async () => {
    try {
      const response = await fetchGroups(user!.token)

      if (response.status === httpStatus.OK) {  
        setGroups(await response.json())
      } else if (response.status === httpStatus.UNAUTHORIZED) {
        navigate(LINKS[LinkType.LOGIN].build())
      } else {
        const errorResponse = await getErrorFromResponse(response)
        setError(errorResponse ? errorResponse.detail : "An error occurred")
      }
    } catch (error: any) {      
      setError("An error occurred.")  
    }
  }

  useEffect(() => { loadGroups() }, [])

  const renderGroups = () => {
    if (groups?.length === 0) {
      return (
        <div>
          <p>It seems you are not a member of a group yet.</p>
        </div>
      )
    } else {
      return (
        <List>
          {
            groups?.map(group => {
              return (
                <ListItem key={group.id}>
                  <div className="flex flex-row flex-nowrap justify-between items-center">
                    <p className="text-gray-600">{group.name}</p>
                    <div className="flex flex-row flex-nowrap justify-between gap-x-4">
                      { group.ownerId === user?.userId ? 
                        <Tag>Owner</Tag>
                        : 
                        null
                      }
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
        { groups === undefined ? 
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