import { BaseComponentProps } from "../../util/util";

export default function Tag(props: BaseComponentProps) {
  return (
    <span className={`inline-flex items-center rounded-md bg-gray-50 px-2 py-1 text-xs font-medium text-gray-500 ring-1 ring-inset ring-gray-500/10 ${props.classes??''}`}>
      {props.children}
    </span>
  )
}