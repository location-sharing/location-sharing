export default function ListItem(props: {
  classes?: string,
} & React.PropsWithChildren
  ) {
  return (
    <li className={`bg-white px-4 py-2 ring-1 ring-slate-200 rounded ${props.classes ?? ''}`}>
      {props.children}
    </li>
  )
}