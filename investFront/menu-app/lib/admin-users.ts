import { API_BASE_URL } from "@/lib/api-base";

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

  const response = await fetch(
    `${API_BASE_URL}/admin/api/users${queryString ? `?${queryString}` : ""}`,
    {
      method: "GET",
      cache: "no-store",
    }
  );

  if (!response.ok) {
    throw new Error(`회원 목록 조회 실패 (${response.status})`);
  }

  return response.json();
}