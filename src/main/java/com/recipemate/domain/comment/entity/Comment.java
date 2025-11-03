package com.recipemate.domain.comment.entity;

import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.post.entity.Post;
import com.recipemate.domain.user.entity.User;
import com.recipemate.global.common.BaseEntity;
import com.recipemate.global.common.CommentType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_comment_group_buy_id", columnList = "group_buy_id"),
        @Index(name = "idx_comment_post_id", columnList = "post_id"),
        @Index(name = "idx_comment_parent_id", columnList = "parent_id"),
        @Index(name = "idx_comment_author_id", columnList = "author_id"),
        @Index(name = "idx_comment_group_buy_deleted", columnList = "group_buy_id, deleted_at, created_at"),
        @Index(name = "idx_comment_post_deleted", columnList = "post_id, deleted_at, created_at"),
        @Index(name = "idx_comment_parent_deleted", columnList = "parent_id, deleted_at, created_at")
})
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false, foreignKey = @ForeignKey(name = "fk_comment_author"))
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_buy_id", foreignKey = @ForeignKey(name = "fk_comment_group_buy"))
    private GroupBuy groupBuy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", foreignKey = @ForeignKey(name = "fk_comment_post"))
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(name = "fk_comment_parent"))
    private Comment parent;

    @Column(nullable = false, length = 1000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommentType type;

    //== 비즈니스 로직 ==//

    /**
     * 댓글이 게시글 또는 공구 중 정확히 하나에만 연결되어 있는지 검증합니다.
     * @throws IllegalStateException 댓글이 게시글과 공구 둘 다 연결되어 있거나, 둘 다 없는 경우
     */
    public void validateTarget() {
        if ((groupBuy == null && post == null) || (groupBuy != null && post != null)) {
            throw new IllegalStateException("댓글은 게시글 또는 공구 중 정확히 하나에만 연결되어야 합니다");
        }
    }

    /**
     * 댓글 내용을 수정합니다.
     * @param newContent 새로운 댓글 내용
     */
    public void updateContent(String newContent) {
        this.content = newContent;
    }

    /**
     * 댓글을 소프트 삭제하고 내용을 "삭제된 댓글입니다"로 변경합니다.
     */
    public void markAsDeleted() {
        this.content = "삭제된 댓글입니다";
        this.delete();  // BaseEntity의 delete() 메서드 호출
    }
}
