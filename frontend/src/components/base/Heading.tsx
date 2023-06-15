export default function Heading(props: {
  replaceStyles?: boolean  
} & React.HTMLAttributes<HTMLHeadingElement>) {

  const propsCopy = {...props}
  if (props.replaceStyles) {
    propsCopy.className = props.className
  } else {
    propsCopy.className = `font-bold text-3xl py-6 px-4 ${props.className}`
  }

  return (
    <h1 {...propsCopy}>{props.children}</h1>
  )
}