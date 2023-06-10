import { BaseComponentProps } from "../../util/util";

export default function NavLink(props: {
  link: string,
} & BaseComponentProps) {
  return (
    <a className={`px-4 py-2 bg-theme-bg-1 ring-1 ring-slate-100 rounded-md font-medium text-gray-800 hover:opacity-80 transition-opacity ${props.classes ?? ''}`} href={props.link}>{props.children}</a>
  )
}