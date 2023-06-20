import GroupCreate from "../models/group/GroupCreate"
import GroupUpdate from "../models/group/GroupUpdate"
import { objectToQueryString } from "../util/util"

const groupsUrl = `${process.env.REACT_APP_API_GROUP_SERVICE}/api/groups`

export const createGroup = (group: GroupCreate, token: string) => fetch(
  groupsUrl,
  {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(group)
  }
)


export const fetchGroups = (token: string) => fetch(
  groupsUrl,
  {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  }
)

export const fetchGroup = (groupId: string, token: string) => fetch(
  `${groupsUrl}/${groupId}`,
  {
    headers: {
      'Authorization': `Bearer ${token}`
    },
  }
)

export const updateGroup = (groupId: string, group: GroupUpdate, token: string) => fetch(
  `${groupsUrl}/${groupId}`,
  {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(group)
  }
)

export const deleteGroup = (groupId: string, token: string) => fetch(
  `${groupsUrl}/${groupId}`,
  {
    method: 'DELETE',
    headers: {
      'Authorization': `Bearer ${token}`
    },
  }
)

export const addGroupMember = (groupId: string, username: string, token: string) => fetch(
  `${groupsUrl}/${groupId}/users?` + objectToQueryString({ username }),
  {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`
    },
  }
)

export const removeGroupMember = (groupId: string, userId: string, token: string) => fetch(
  `${groupsUrl}/${groupId}/users?` + objectToQueryString({ userId }),
  {
    method: 'DELETE',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  }
)

export const changeGroupOwner = (groupId: string, username: string, token: string) => fetch(
  `${groupsUrl}/${groupId}/owner?` + objectToQueryString({ username }),
  {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`
    },
  }
)