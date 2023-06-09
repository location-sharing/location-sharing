import { PropsWithChildren } from "react"

export default function InputLabel(props: {
  htmlFor: string,
} & PropsWithChildren) {
  return (
    <label htmlFor={props.htmlFor} className="block mb-2 text-sm font-medium text-gray-900">{props.children}</label>
  )
}