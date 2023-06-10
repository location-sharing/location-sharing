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