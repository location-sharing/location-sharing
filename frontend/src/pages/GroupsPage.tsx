import Button from "../components/base/Button"
import { List } from "../components/base/List"
import ListItem from "../components/base/ListItem"
import Tag from "../components/base/Tag"
import Group from "../models/group/Group"
import { useNavigate } from "react-router-dom"
import { useState, useEffect } from "react"
import { LINKS } from "../router/router"
import httpStatus from "http-status"
import { getErrorFromResponse } from "../util/util"
import ErrorAlert from "../components/base/alerts/ErrorAlert"
import { getAuth } from "../services/auth"
import Heading from "../components/base/Heading"

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

  // TODO: set user in a context in the login component
  const [user, setUser] = useState<any>()

  const loadGroups = async () => {
    try {
      const user = getAuth()
      if (!user) {
        navigate(LINKS.LOGIN)
      }

      setUser(user)

      const response = await fetchGroups(user!.token!)

      if (response.status === httpStatus.OK) {  
        setGroups(await response.json())
      } else if (response.status === httpStatus.UNAUTHORIZED) {
        navigate(LINKS.LOGIN)
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
                <ListItem>
                  <div className="flex flex-row flex-nowrap justify-between items-center">
                    <p className="text-gray-600">{group.name}</p>
                    <div className="flex flex-row flex-nowrap justify-between gap-x-4">
                      { group.ownerId === user?.userId ? 
                        <Tag>Owner</Tag>
                        : 
                        null
                      }
                      <Button onClick={() => navigate(`${LINKS.GROUPS}/${group.id}`)}>View</Button>
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
      { error ? <ErrorAlert title="Error while fetching groups" message={error} onClose={() => setError(undefined)}/> : null}
      <Heading classes="mb-4">Groups</Heading>
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