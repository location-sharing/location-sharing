import Button from "./Button"
import ListItem from "./ListItem"
import Tag from "./Tag"

export function List() {

  const ownerId = "iamtheowner"
  const groups = [
    {
      id: "1",
      name: "groupA",
      ownerId: "iamtheowner"
    },
    {
      id: "2",
      name: "groupB",
      ownerId: "owner_id"
    },
    {
      id: "3",
      name: "groupD",
      ownerId: "iamtheowner"
    },
    {
      id: "4",
      name: "groupA",
      ownerId: "iamtheowner"
    },
    {
      id: "5",
      name: "groupDFDF",
      ownerId: "owner_id"
    },
    {
      id: "5",
      name: "groupDF",
      ownerId: "owner_id"
    },
    {
      id: "7",
      name: "groupDFDF",
      ownerId: "owner_id"
    },
    {
      id: "7",
      name: "groupDFDF",
      ownerId: "owner_id"
    },
    {
      id: "7",
      name: "groupDFDF",
      ownerId: "owner_id"
    },
    {
      id: "7",
      name: "groupDFDF",
      ownerId: "owner_id"
    },
    {
      id: "7",
      name: "groupDFDF",
      ownerId: "owner_id"
    },
    {
      id: "7",
      name: "groupDFDF",
      ownerId: "owner_id"
    },
    {
      id: "7",
      name: "groupDFDF",
      ownerId: "owner_id"
    },
    {
      id: "7",
      name: "groupDFDF",
      ownerId: "owner_id"
    },
    {
      id: "7",
      name: "groupDFDF",
      ownerId: "owner_id"
    },
    {
      id: "7",
      name: "groupDFDF",
      ownerId: "owner_id"
    },
  ]

  return (
    <ul className="w-full sm:max-w-2xl h-5/6 overflow-y-auto mx-auto flex flex-col gap-y-3  p-6 ring-1 ring-gray-200 rounded-md">
      {
        groups.map(group => {
          return (
            <ListItem>
              <div className="flex flex-row flex-nowrap justify-between items-center">
                <p className="text-gray-600">{group.name}</p>
                <div className="flex flex-row flex-nowrap justify-between gap-x-4">
                  { group.ownerId === ownerId ? 
                    <Tag>Owner</Tag>
                    : 
                    null
                  }
                  <Button>Open</Button>
                </div>
              </div>
            </ListItem>
          )
        })
      }
    </ul>
  )
}