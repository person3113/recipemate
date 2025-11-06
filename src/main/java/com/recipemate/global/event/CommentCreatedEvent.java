package com.recipemate.global.event;

import com.recipemate.domain.comment.entity.Comment;
import lombok.Getter;

@Getter
public class CommentCreatedEvent {
    private final Comment comment;

    public CommentCreatedEvent(Comment comment) {
        this.comment = comment;
    }
}
