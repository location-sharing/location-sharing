export default function Input(props: {
  type: string, 
  name: string,
  id?: string,
  placeholder?: string, 
  required?: boolean,
}) {
  return (
    <input type={props.type} name={props.name} id={props.id} placeholder={props.placeholder} required={props.required}
    className="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5"/>
  )
}