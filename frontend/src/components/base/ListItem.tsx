
export default function ListItem(props: React.HTMLAttributes<HTMLLIElement>) {
  return (
    <li className="bg-white px-4 py-2 ring-1 ring-slate-200 rounded"
    {...props}
    >
      {props.children}
    </li>
  )
}