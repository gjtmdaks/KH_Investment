import { API_BASE_URL } from "@/lib/api-base";

export type BoardPost = {
  boardNo: number;
  userNo: number;
  userName: string | null;
  stockCode: string;
  content: string;
  createdAt: string;
  likeCount: number;
  parentId: number | null;
  commentType: "COMMENT" | "REPLY";
  likedByMe: boolean;
};

export type BoardCreateRequest = {
  content: string;
  parentId: number | null;
};

async function handleJsonResponse<T>(
  response: Response,
  errorMessage: string
): Promise<T> {
  if (!response.ok) {
    const errorText = await response.text();

    console.error("API 요청 실패", {
      status: response.status,
      statusText: response.statusText,
      body: errorText,
    });

    throw new Error(`${errorMessage} (${response.status})`);
  }

  return response.json() as Promise<T>;
}

export async function getStockBoardPosts(
  stockCode: string
): Promise<BoardPost[]> {
  const response = await fetch(`${API_BASE_URL}/api/board/stocks/${stockCode}`, {
    method: "GET",
    credentials: "include",
    cache: "no-store",
  });

  return handleJsonResponse<BoardPost[]>(
    response,
    "댓글 조회에 실패했습니다."
  );
}

export async function createStockBoardPost(
  stockCode: string,
  request: BoardCreateRequest
): Promise<BoardPost> {
  const response = await fetch(`${API_BASE_URL}/api/board/stocks/${stockCode}`, {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(request),
  });

  return handleJsonResponse<BoardPost>(
    response,
    "댓글 작성에 실패했습니다."
  );
}

export async function deleteBoardPost(boardNo: number): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/api/board/${boardNo}`, {
    method: "DELETE",
    credentials: "include",
  });

  if (!response.ok) {
    throw new Error("댓글 삭제에 실패했습니다.");
  }
}

export async function likeBoardPost(boardNo: number): Promise<BoardPost> {
  const response = await fetch(`${API_BASE_URL}/api/board/${boardNo}/like`, {
    method: "POST",
    credentials: "include",
  });

  return handleJsonResponse<BoardPost>(
    response,
    "좋아요 처리에 실패했습니다."
  );
}

export async function unlikeBoardPost(boardNo: number): Promise<BoardPost> {
  const response = await fetch(`${API_BASE_URL}/api/board/${boardNo}/like`, {
    method: "DELETE",
    credentials: "include",
  });

  return handleJsonResponse<BoardPost>(
    response,
    "좋아요 취소에 실패했습니다."
  );
}