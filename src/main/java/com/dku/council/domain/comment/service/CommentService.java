package com.dku.council.domain.comment.service;

public interface CommentService {

    /**
     * Post에 댓글을 추가합니다.
     *
     * @param postId  게시글 ID
     * @param userId  사용자 ID
     * @param content 댓글 내용
     */
    void add(Long postId, Long userId, String content);

    /**
     * 댓글을 수정합니다. 사용자가 댓글을 수정할 수 있는 권한이 있어야합니다.
     *
     * @param commentId 댓글 ID
     * @param userId    사용자 ID
     * @param content   댓글 내용
     */
    void edit(Long commentId, Long userId, String content);

    /**
     * 댓글을 삭제합니다. 사용자가 댓글을 삭제할 수 있는 권한이 있어야합니다.
     *
     * @param commentId 댓글 ID
     * @param userId    사용자 ID
     */
    /**
     * 댓글을 삭제합니다. 사용자가 댓글을 삭제할 수 있는 권한이 있어야 합니다. Admin 은 즉시 삭제 가능.
     * @param commentId 댓글 ID
     * @param userId    사용자 ID
     * @param isAdmin   Admin 여부
     */
    void delete(Long commentId, Long userId, boolean isAdmin);
}