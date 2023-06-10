import { BaseComponentProps } from "../../util/util"

export function List(props: BaseComponentProps) {
  return (
    <ul className={`w-full h-full overflow-y-auto flex flex-col gap-y-3 p-6 ring-1 ring-gray-200 rounded-md ${props.classes ?? ''}`}>
      {props.children}
    </ul>
  )
}