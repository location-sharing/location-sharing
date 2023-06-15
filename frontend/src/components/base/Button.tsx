
export default function Button(props: {
  btnType?: "basic" | "danger",
  replaceStyles?: boolean
} & React.ButtonHTMLAttributes<HTMLButtonElement>) {

  const propsCopy = {...props}
  delete propsCopy.btnType
  delete propsCopy.replaceStyles

  let classes = ""

  switch(props.btnType) {
    case "basic":
      classes = "text-white bg-sky-700 hover:bg-sky-600 focus:outline-none focus:ring-sky-500 focus:ring-2 font-medium rounded-lg text-sm px-3 py-2 text-center"
      break
    case "danger":
      classes="text-white bg-red-800 hover:bg-red-700 focus:outline-none focus:ring-red-500 focus:ring-2 font-medium rounded-lg text-sm px-3 py-2 text-center"
      break        
  }
  propsCopy.className = classes

  if (props.replaceStyles) {
    propsCopy.className = props.className
  } else {
    propsCopy.className = `${classes} ${props.className}`
  }

  return (
    <button
      {...propsCopy}
    >
      {props.children}
    </button>
  )
}