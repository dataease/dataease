import request from "@/config/axios";

export const sysOrgMemberPageApi = (data: any) => {
  return request.post({ url: "/sys/org/member/page", data });
};

export const sysOrgMemberCandidatesApi = (data: any) => {
  return request.post({ url: "/sys/org/member/candidates", data });
};

export const sysOrgMemberRoleOptionsApi = (orgId: string) => {
  return request.post({ url: "/sys/org/member/roleOptions", data: { orgId } });
};

export const sysOrgMemberAddApi = (data: any) => {
  return request.post({ url: "/sys/org/member/add", data });
};

export const sysOrgMemberRemoveApi = (userId: string, orgId: string) => {
  return request.post({ url: `/sys/org/member/remove/${userId}/${orgId}` });
};

export const sysOrgMemberSwitchRoleApi = (data: any) => {
  return request.post({ url: "/sys/org/member/switchRole", data });
};

export const lazyTreeApi = (data: any) => request.post({ url: "/org/page/lazyTree", data });

// Reuse existing org APIs for CRUD
export { saveApi as createApi, updateApi as editApi, deleteApi } from "@/api/org";
