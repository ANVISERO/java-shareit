package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment AS c " +
            "JOIN FETCH c.author " +
            "WHERE c.item.id = :itemId")
    List<Comment> findAllCommentsByItemId(Long itemId);

    @Query("SELECT c FROM Comment AS c " +
            "JOIN FETCH c.item " +
            "WHERE c.item.owner.id = :ownerId")
    List<Comment> findAllCommentsLeftOnOwnerItemsWithId(@Param("ownerId") Long userId);
}
