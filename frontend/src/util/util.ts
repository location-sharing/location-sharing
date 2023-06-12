import ServerError from "../models/errors/ServerError"

export interface BaseComponentProps extends React.PropsWithChildren {
  classes?: string
}

export async function getErrorFromResponse(response: Response) {
  const body: ServerError = await response.json()
  if (body.title && body.detail) {
    const error: ServerError = {
      title: body.title,
      detail: body.detail
    }
    return error
  }
}

export function objectToQueryString(obj: {[key: string]: string}): string {
  const keyValuePairs = Array<String>();
  Object.keys(obj).forEach(key => {
    keyValuePairs.push(`${encodeURIComponent(key)}=${encodeURIComponent(obj[key])}`);
  })  
  return keyValuePairs.join('&');
}
