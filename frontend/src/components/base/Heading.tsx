import { BaseComponentProps } from "../../util/util";

export default function Heading(props: BaseComponentProps) {
  return (
    <h1 className={`font-bold text-3xl py-6 px-4 ${props.classes ?? ''}`}>{props.children}</h1>
  )
}