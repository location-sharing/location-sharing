export default function Alert(props: {
  title: string, 
  message: string
}) {
  return (
    <div className="
    absolute
    top-2
    bg-slate-300 border-t-4 
    border-sky-700 rounded-b
    text-sky-800 
    px-4 
    py-3
    ">
      <p className="font-bold">{props.title}</p>
      <p>{props.message}</p>
    </div>
  )
}