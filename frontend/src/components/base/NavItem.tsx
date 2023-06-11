import { BaseComponentProps } from "../../util/util";
import { Link } from "react-router-dom";

export default function NavItem(props: {
  link: string,
} & BaseComponentProps) {
  return (
    <Link to={props.link} className={`px-4 py-2 bg-theme-bg-1 ring-1 ring-slate-100 rounded-md font-medium text-gray-800 hover:opacity-80 transition-opacity ${props.classes ?? ''}`}>{props.children}</Link>
  )
}