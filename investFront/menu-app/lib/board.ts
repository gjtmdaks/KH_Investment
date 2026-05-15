import { apiClient } from "@/lib/api-client";

export type BoardPost = {
  boardNo: number;
  userNo: number;
  userName: string;
  stockCode: string;
  content: string;
  createdAt: string;
  likeCount: number;
  parentId: number | null;
};

export type BoardCreateRequest = {
  content: string;
  parentId?: number | null;
};

export async function getStockBoardPosts(stockCode: string) {
  const response = await apiClient.get<BoardPost[]>(
    `/boards/stocks/${stockCode}`
  );

  return response.data;
}

export async function createStockBoardPost(
  stockCode: string,
  request: BoardCreateRequest
) {
  const response = await apiClient.post<BoardPost>(
    `/boards/stocks/${stockCode}`,
    request
  );

  return response.data;
}

export async function deleteBoardPost(boardNo: number) {
  await apiClient.delete(`/boards/${boardNo}`);
}