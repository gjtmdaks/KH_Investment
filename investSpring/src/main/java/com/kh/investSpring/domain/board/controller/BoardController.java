package com.kh.investSpring.domain.board.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.investSpring.domain.board.dto.BoardCreateRequest;
import com.kh.investSpring.domain.board.dto.BoardListResponse;
import com.kh.investSpring.domain.board.service.BoardService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board")
public class BoardController {

    private final BoardService boardService;

    /**
     * 종목 댓글/대댓글 목록 조회
     *
     * GET /api/board/stocks/{stockCode}
     *
     * 로그인하지 않아도 조회 가능.
     * 단, 로그인한 사용자가 있으면 likedByMe 계산에 userNo를 사용함.
     */
    @GetMapping("/stocks/{stockCode}")
    public ResponseEntity<List<BoardListResponse>> selectBoardListByStockCode(
            @PathVariable String stockCode,
            HttpServletRequest request
    ) {
        Long userNo = getLoginUserNo(request, false);

        List<BoardListResponse> boardList =
                boardService.selectBoardListByStockCode(stockCode, userNo);

        return ResponseEntity.ok(boardList);
    }

    /**
     * 댓글/대댓글 작성
     *
     * POST /api/board/stocks/{stockCode}
     *
     * request.parentId == null 이면 일반 댓글
     * request.parentId != null 이면 대댓글
     */
    @PostMapping("/stocks/{stockCode}")
    public ResponseEntity<BoardListResponse> insertBoardPost(
            @PathVariable String stockCode,
            @RequestBody BoardCreateRequest request,
            HttpServletRequest servletRequest
    ) {
        Long userNo = getLoginUserNo(servletRequest, true);

        BoardListResponse createdPost =
                boardService.insertBoardPost(stockCode, userNo, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }

    /**
     * 댓글/대댓글 소프트 삭제
     *
     * DELETE /api/board/{boardNo}
     */
    @DeleteMapping("/{boardNo}")
    public ResponseEntity<Void> updateBoardDeletedYnByBoardNo(
            @PathVariable Long boardNo,
            HttpServletRequest request
    ) {
        Long userNo = getLoginUserNo(request, true);

        boardService.updateBoardDeletedYnByBoardNo(boardNo, userNo);

        return ResponseEntity.noContent().build();
    }

    /**
     * 좋아요
     *
     * POST /api/board/{boardNo}/like
     */
    @PostMapping("/{boardNo}/like")
    public ResponseEntity<BoardListResponse> insertBoardLike(
            @PathVariable Long boardNo,
            HttpServletRequest request
    ) {
        Long userNo = getLoginUserNo(request, true);

        BoardListResponse response =
                boardService.insertBoardLike(boardNo, userNo);

        return ResponseEntity.ok(response);
    }

    /**
     * 좋아요 취소
     *
     * DELETE /api/board/{boardNo}/like
     */
    @DeleteMapping("/{boardNo}/like")
    public ResponseEntity<BoardListResponse> deleteBoardLike(
            @PathVariable Long boardNo,
            HttpServletRequest request
    ) {
        Long userNo = getLoginUserNo(request, true);

        BoardListResponse response =
                boardService.deleteBoardLike(boardNo, userNo);

        return ResponseEntity.ok(response);
    }

    /**
     * 로그인 사용자 번호 조회
     *
     * 현재는 session의 userNo 기준.
     * 만약 프로젝트에서 JWT 인증 정보를 request attribute나 SecurityContext에 저장하고 있으면
     * 이 메서드만 그 방식에 맞게 바꾸면 됨.
     */
    private Long getLoginUserNo(HttpServletRequest request, boolean required) {
        Object userNo = request.getAttribute("userNo");

        if (userNo instanceof Long) {
            return (Long) userNo;
        }

        if (userNo instanceof Integer) {
            return ((Integer) userNo).longValue();
        }

        if (userNo instanceof String) {
            try {
                return Long.parseLong((String) userNo);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        HttpSession session = request.getSession(false);

        if (session != null) {
            Object sessionUserNo = session.getAttribute("userNo");

            if (sessionUserNo instanceof Long) {
                return (Long) sessionUserNo;
            }

            if (sessionUserNo instanceof Integer) {
                return ((Integer) sessionUserNo).longValue();
            }

            if (sessionUserNo instanceof String) {
                try {
                    return Long.parseLong((String) sessionUserNo);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }

        if (required) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "로그인이 필요합니다."
            );
        }

        return null;
    }
}