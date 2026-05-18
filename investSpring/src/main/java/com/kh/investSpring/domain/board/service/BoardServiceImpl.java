package com.kh.investSpring.domain.board.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.kh.investSpring.domain.board.dao.BoardDao;
import com.kh.investSpring.domain.board.dto.BoardCreateRequest;
import com.kh.investSpring.domain.board.dto.BoardDto;
import com.kh.investSpring.domain.board.dto.BoardListResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardDao boardDao;

    /**
     * 종목 댓글/대댓글 목록 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<BoardListResponse> selectBoardListByStockCode(String stockCode, Long userNo) {
        validateStockCode(stockCode);

        List<BoardDto> boardList = boardDao.selectBoardListByStockCode(stockCode, userNo);

        return boardList.stream()
                .map(this::toBoardListResponse)
                .collect(Collectors.toList());
    }

    /**
     * 댓글/대댓글 작성
     */
    @Override
    @Transactional
    public BoardListResponse insertBoardPost(
            String stockCode,
            Long userNo,
            BoardCreateRequest request
    ) {
        validateStockCode(stockCode);
        validateUserNo(userNo);
        validateCreateRequest(request);

        Long parentId = request.getParentId();

        if (parentId != null) {
            BoardDto parentBoard = boardDao.selectBoardByBoardNo(parentId, userNo);

            if (parentBoard == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "부모 댓글을 찾을 수 없습니다."
                );
            }

            if (parentBoard.getParentId() != null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "대댓글에는 다시 답글을 달 수 없습니다."
                );
            }

            if (!stockCode.equals(parentBoard.getStockCode())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "다른 종목의 댓글에는 답글을 달 수 없습니다."
                );
            }
        }

        BoardDto boardDto = new BoardDto();
        boardDto.setUserNo(userNo);
        boardDto.setStockCode(stockCode);
        boardDto.setContent(request.getContent().trim());
        boardDto.setParentId(parentId);
        boardDto.setLikeCount(0);
        boardDto.setDeletedYn("N");

        int result = boardDao.insertBoardPost(boardDto);

        if (result != 1) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "댓글 작성에 실패했습니다."
            );
        }

        BoardDto createdBoard = boardDao.selectBoardByBoardNo(boardDto.getBoardNo(), userNo);

        if (createdBoard == null) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "작성된 댓글을 조회하지 못했습니다."
            );
        }

        return toBoardListResponse(createdBoard);
    }

    /**
     * 댓글/대댓글 소프트 삭제
     */
    @Override
    @Transactional
    public void updateBoardDeletedYnByBoardNo(Long boardNo, Long userNo) {
        validateBoardNo(boardNo);
        validateUserNo(userNo);

        BoardDto boardDto = boardDao.selectBoardByBoardNo(boardNo, userNo);

        if (boardDto == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "댓글을 찾을 수 없습니다."
            );
        }

        if (!Objects.equals(boardDto.getUserNo(), userNo)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "본인이 작성한 댓글만 삭제할 수 있습니다."
            );
        }

        int result = boardDao.updateBoardDeletedYnByBoardNo(boardNo);

        if (result != 1) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "댓글 삭제에 실패했습니다."
            );
        }
    }

    /**
     * 좋아요
     */
    @Override
    @Transactional
    public BoardListResponse insertBoardLike(Long boardNo, Long userNo) {
        validateBoardNo(boardNo);
        validateUserNo(userNo);

        BoardDto boardDto = boardDao.selectBoardByBoardNo(boardNo, userNo);

        if (boardDto == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "댓글을 찾을 수 없습니다."
            );
        }

        int likeExists = boardDao.selectBoardLikeCountByBoardNoAndUserNo(boardNo, userNo);

        if (likeExists > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "이미 좋아요를 누른 댓글입니다."
            );
        }

        int insertResult = boardDao.insertBoardLike(boardNo, userNo);

        if (insertResult != 1) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "좋아요 처리에 실패했습니다."
            );
        }

        boardDao.updateBoardLikeCountIncrease(boardNo);

        BoardDto updatedBoard = boardDao.selectBoardByBoardNo(boardNo, userNo);

        return toBoardListResponse(updatedBoard);
    }

    /**
     * 좋아요 취소
     */
    @Override
    @Transactional
    public BoardListResponse deleteBoardLike(Long boardNo, Long userNo) {
        validateBoardNo(boardNo);
        validateUserNo(userNo);

        BoardDto boardDto = boardDao.selectBoardByBoardNo(boardNo, userNo);

        if (boardDto == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "댓글을 찾을 수 없습니다."
            );
        }

        int likeExists = boardDao.selectBoardLikeCountByBoardNoAndUserNo(boardNo, userNo);

        if (likeExists == 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "좋아요를 누르지 않은 댓글입니다."
            );
        }

        int deleteResult = boardDao.deleteBoardLike(boardNo, userNo);

        if (deleteResult != 1) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "좋아요 취소에 실패했습니다."
            );
        }

        boardDao.updateBoardLikeCountDecrease(boardNo);

        BoardDto updatedBoard = boardDao.selectBoardByBoardNo(boardNo, userNo);

        return toBoardListResponse(updatedBoard);
    }

    private BoardListResponse toBoardListResponse(BoardDto boardDto) {
        return BoardListResponse.builder()
                .boardNo(boardDto.getBoardNo())
                .userNo(boardDto.getUserNo())
                .userName(boardDto.getUserName())
                .stockCode(boardDto.getStockCode())
                .content(boardDto.getContent())
                .createdAt(boardDto.getCreatedAt())
                .likeCount(boardDto.getLikeCount() == null ? 0 : boardDto.getLikeCount())
                .parentId(boardDto.getParentId())
                .commentType(boardDto.getParentId() == null ? "COMMENT" : "REPLY")
                .likedByMe("Y".equals(boardDto.getLikedByMe()))
                .build();
    }

    private void validateStockCode(String stockCode) {
        if (stockCode == null || stockCode.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "종목코드가 필요합니다."
            );
        }
    }

    private void validateUserNo(Long userNo) {
        if (userNo == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "로그인이 필요합니다."
            );
        }
    }

    private void validateBoardNo(Long boardNo) {
        if (boardNo == null || boardNo <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "댓글 번호가 올바르지 않습니다."
            );
        }
    }

    private void validateCreateRequest(BoardCreateRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "댓글 내용이 필요합니다."
            );
        }

        String content = request.getContent();

        if (content == null || content.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "댓글 내용을 입력해주세요."
            );
        }

        if (content.trim().length() > 2000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "댓글은 2,000자 이하로 입력해주세요."
            );
        }
    }
}