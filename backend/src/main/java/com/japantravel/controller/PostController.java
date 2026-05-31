package com.japantravel.controller;

import com.japantravel.dto.Dtos.*;
import com.japantravel.repository.PostRepository;
import com.japantravel.security.AppUserPrincipal;
import com.japantravel.security.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PostController {

    private final PostRepository repo;
    private final CurrentUser currentUser;

    public PostController(PostRepository repo, CurrentUser currentUser) {
        this.repo = repo;
        this.currentUser = currentUser;
    }

    // ── posts ─────────────────────────────────────────────────────────────
    @GetMapping("/posts")
    public PostPage listPosts(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int size) {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 20;
        List<Post> items = repo.listPaged(page * size, size);
        return new PostPage(items, repo.count(), page, size);
    }

    @GetMapping("/posts/{id}")
    public Post getPost(@PathVariable long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/posts")
    public ResponseEntity<Post> createPost(@RequestBody PostCreate req) {
        AppUserPrincipal p = currentUser.require();
        Long id = repo.insert(p.getId(), req.title(), req.body());
        Post created = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/posts/{id}")
    public Post updatePost(@PathVariable long id, @RequestBody PostCreate req) {
        AppUserPrincipal p = currentUser.require();
        Post existing = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (existing.userId() != p.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        repo.update(id, p.getId(), req.title(), req.body());
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable long id) {
        AppUserPrincipal p = currentUser.require();
        Post existing = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (existing.userId() != p.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        repo.delete(id, p.getId());
        return ResponseEntity.noContent().build();
    }

    // ── comments ──────────────────────────────────────────────────────────
    @GetMapping("/posts/{id}/comments")
    public List<PostComment> listComments(@PathVariable long id) {
        if (repo.findById(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return repo.listComments(id);
    }

    @PostMapping("/posts/{id}/comments")
    public ResponseEntity<PostComment> createComment(@PathVariable long id,
                                                     @RequestBody CommentCreate req) {
        AppUserPrincipal p = currentUser.require();
        if (repo.findById(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Long commentId = repo.insertComment(id, p.getId(), req.body());
        PostComment created = repo.listComments(id).stream()
                .filter(c -> c.id() == commentId)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable long id) {
        AppUserPrincipal p = currentUser.require();
        boolean removed = repo.deleteComment(id, p.getId());
        if (!removed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return ResponseEntity.noContent().build();
    }
}
