import {post} from "@/plugins/request"

export function saveLicense(data) {
  return post("/samples/license/save", data)
}


