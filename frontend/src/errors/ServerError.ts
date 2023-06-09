export default interface IServerError {
  title: string,
  detail: string,
}

export async function getErrorFromResponse(response: Response) {
  const body: IServerError = await response.json()
  if (body.title && body.detail) {
    const error: IServerError = {
      title: body.title,
      detail: body.detail
    }
    return error
  }
}