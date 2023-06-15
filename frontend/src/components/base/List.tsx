
export function List(props: React.HTMLAttributes<HTMLUListElement>) {
  return (
    <ul className="w-full h-full overflow-y-auto flex flex-col gap-y-3 p-6 ring-1 ring-gray-200 rounded-md"
    {...props}
    >
      {props.children}
    </ul>
  )
}