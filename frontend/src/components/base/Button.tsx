import { BaseComponentProps } from "../../util/util"

export default function Button(props: {
  type?: "button"|"submit"|"reset", 
  onClick?: React.MouseEventHandler<HTMLButtonElement>, 
} & BaseComponentProps) {
  return (
    <button type={props.type} 
    className={`text-white bg-sky-700 hover:bg-sky-600 focus:outline-none focus:ring-sky-500 focus:ring-2 font-medium rounded-lg text-sm px-5 py-2.5 text-center ${props.classes??''}`}
    onClick={props.onClick}
    >
      {props.children}
    </button>
  )
}