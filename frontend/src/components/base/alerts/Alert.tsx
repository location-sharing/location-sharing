export default function Alert(props: {
  title: string, 
  message: string,
  onClose: () => void,
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
      <span className="absolute top-0 bottom-0 right-0 px-4 py-3" onClick={props.onClose}>
         <svg className="fill-current h-6 w-6 text-sky-800" role="button" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20"><title>Close</title><path d="M14.348 14.849a1.2 1.2 0 0 1-1.697 0L10 11.819l-2.651 3.029a1.2 1.2 0 1 1-1.697-1.697l2.758-3.15-2.759-3.152a1.2 1.2 0 1 1 1.697-1.697L10 8.183l2.651-3.031a1.2 1.2 0 1 1 1.697 1.697l-2.758 3.152 2.758 3.15a1.2 1.2 0 0 1 0 1.698z"/></svg>
        </span>
    </div>
  )
}