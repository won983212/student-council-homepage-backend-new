package com.dku.council.domain.post.repository.post;

import com.dku.council.domain.post.model.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * ACTIVE상태인 post만 가져옵니다.
     */
    @Query("select p from Post p where p.id = :id and p.status = 'ACTIVE'")
    Optional<Post> findById(@Param("id") Long id);

    /**
     * ACTIVE상태인 post만 가져옵니다.
     */
    @Query("select p from Post p where p.user.id=:userId and p.status='ACTIVE'")
    Page<Post> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 활성화 여부와 상관없이 게시글을 가져옵니다. 관리자만 사용할 수 있습니다.
     */
    @Query("select p from Post p where p.id=:id")
    Optional<Post> findByIdWithAdmin(@Param("id") Long id);

    /**
     * 활성화 여부와 상관없이 작성자의 모든 게시글을 가져옵니다. 관리자만 사용할 수 있습니다.
     */
    @Query("select p from Post p where p.user.id=:userId")
    Page<Post> findAllByUserIdWithAdmin(@Param("userId") Long userId, Pageable pageable);

}