import Heading from "../components/base/Heading";
import GroupDetail from "../models/group/GroupDetail";
import { useLocation, useParams, useNavigate, Link } from "react-router-dom";
import { useEffect, useState } from "react";
import useAuth from "../services/auth";
import httpStatus from "http-status";
import { LINKS } from "../router/router";
import { getErrorFromResponse } from "../util/util";
import ErrorAlert from "../components/base/alerts/ErrorAlert";

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
        navigate(LINKS.LOGIN)
      } else {
        const errorResponse = await getErrorFromResponse(res)
        setError(errorResponse ? errorResponse.detail : "An error occurred")
      }
    } else {
      setGroup(groupFromLocation)
    }
  }

  useEffect(() => { loadGroup() }, [])

  const renderGroup = () => {
    return (
      <p>{group?.id}</p>
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
      <Link to={LINKS.GROUP_CREATE}>
        <button className="px-3 py-2 text-white bg-green-700 rounded-md hover:bg-green-600">New Group</button>
      </Link>
      <div className="w-full h-full flex flex-row justify-center items-center gap-x-12">
        { group === undefined ? 
          <div>
            <h3>Loading...</h3>
          </div>
          :
          renderGroup()
        }
      </div>
    </section>
  )
}