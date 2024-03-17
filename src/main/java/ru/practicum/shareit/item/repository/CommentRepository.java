package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment AS c " +
            "JOIN FETCH c.author " +
            "WHERE c.item.id = :itemId")
    List<Comment> findAllCommentsByItemId(Long itemId);

    @Query("SELECT c FROM Comment AS c " +
            "JOIN FETCH c.author " +
            "WHERE c.author.id = :authorId")
    List<Comment> findAllCommentsByAuthorId(@Param("authorId") Long userId);
}
