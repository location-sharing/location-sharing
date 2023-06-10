import { BaseComponentProps } from "../../util/util"

export default function InputLabel(props: {
  htmlFor: string,
} & BaseComponentProps) {
  return (
    <label htmlFor={props.htmlFor} className="block mb-2 text-sm font-medium text-gray-900">{props.children}</label>
  )
}