import Group from "./Group";
import GroupUser from "./GroupUser";

export default interface GroupDetail {
  id: string,
  name: string,
  ownerId: string,
  users: Array<GroupUser>
}