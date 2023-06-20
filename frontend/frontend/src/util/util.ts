import ServerError from "../models/errors/ServerError"

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



const characters ='ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';

export function generateRandomString(length: number) {
    let result = ' ';
    const charactersLength = characters.length;
    for ( let i = 0; i < length; i++ ) {
        result += characters.charAt(Math.floor(Math.random() * charactersLength));
    }
    return result;
}

export function generateRandomPastelColor() {
  // appropriate saturation & lightness values give a pastel look
  return `hsl(
    ${360 * Math.random()},
    ${10 + 70 * Math.random()}%,
    70%)`
}



// calculates the Haversine distance between 2 points
// not my code, super optimized 
// doesn't account for altitude over the earth's mean radius  
export function distanceInKm(lat1: number, lon1: number, lat2: number, lon2: number) {
  var p = 0.017453292519943295;    // Math.PI / 180
  var c = Math.cos;
  var a = 0.5 - c((lat2 - lat1) * p)/2 + 
          c(lat1 * p) * c(lat2 * p) * 
          (1 - c((lon2 - lon1) * p))/2;

  return 12742 * Math.asin(Math.sqrt(a)); // 2 * R; R = 6371 km
}
