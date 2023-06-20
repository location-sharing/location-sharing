
export default function InputLabel(props: React.LabelHTMLAttributes<HTMLLabelElement>) {
  return (
    <label className="block mb-2 text-sm font-medium text-gray-900" {...props}>{props.children}</label>
  )
}