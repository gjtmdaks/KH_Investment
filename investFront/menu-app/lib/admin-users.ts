import { apiClient } from "@/lib/api-client";

export type AdminUser = {
  userNo: number;
  userName: string;
  email: string | null;
  phone: string | null;
  provider: string | null;
  createdAt: string;
  status: "ACTIVE" | "STOP" | "DELETE";
  deleteAt: string | null;
  auth: number;
  statusName: string;
  authName: string;
};

export type AdminUserListResponse = {
  users: AdminUser[];
  totalCount: number;
  activeCount: number;
  stopCount: number;
  deleteCount: number;
};

export type AdminUserSearchParams = {
  keyword?: string;
  status?: string;
  auth?: number;
};

export async function getAdminUsers(
  params: AdminUserSearchParams = {}
): Promise<AdminUserListResponse> {
  const searchParams = new URLSearchParams();

  if (params.keyword) {
    searchParams.set("keyword", params.keyword);
  }

  if (params.status && params.status !== "ALL") {
    searchParams.set("status", params.status);
  }

  if (params.auth) {
    searchParams.set("auth", String(params.auth));
  }

  const queryString = searchParams.toString();

  const { data } = await apiClient.get<AdminUserListResponse>(
    `/admin/api/users${queryString ? `?${queryString}` : ""}`
  );

  return data;
}

export async function updateAdminUserAccountStatus(
  userNo: number,
  status: "ACTIVE" | "STOP" | "CLOSE",
  stopEndAt?: string
): Promise<void> {
  await apiClient.patch(`/admin/api/users/${userNo}/account-status`, {
    status,
    stopEndAt,
  });
}

export async function updateAdminUserStatus(
  userNo: number,
  status: "ACTIVE" | "DELETE"
): Promise<void> {
  await apiClient.patch(`/admin/api/users/${userNo}/status`, { status });
}