import request from "@/utils/request";

export function getPortalList({ page_number, page_size }) {
  return request({
    url: `/portal/data/list/${page_number}/${page_size}`,
    method: "get",
    loading: true,
  });
}

export function savePortal(data) {
  return request({
    url: `/portal/data/save`,
    method: "post",
    loading: true,
    data,
  });
}

export function deletePortal(id) {
  return request({
    url: `/portal/data/delete/${id}`,
    method: "post",
    laoding: true,
  });
}

export function updatePortal(data) {
  return request({
    url: `/portal/data/update`,
    method: "post",
    loading: true,
    data,
  });
}

