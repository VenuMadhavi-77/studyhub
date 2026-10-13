package com.studyhub.platform.modules.resource.service;

import com.studyhub.platform.common.exception.ResourceNotFoundException;
import com.studyhub.platform.modules.auth.entity.User;
import com.studyhub.platform.modules.auth.repository.UserRepository;
import com.studyhub.platform.modules.resource.dto.BookmarkResponse;
import com.studyhub.platform.modules.resource.dto.ResourceResponse;
import com.studyhub.platform.modules.resource.entity.Bookmark;
import com.studyhub.platform.modules.resource.entity.Resource;
import com.studyhub.platform.modules.resource.repository.BookmarkRepository;
import com.studyhub.platform.modules.resource.repository.ResourceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public BookmarkService(BookmarkRepository bookmarkRepository,
                           ResourceRepository resourceRepository,
                           UserRepository userRepository) {
        this.bookmarkRepository = bookmarkRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BookmarkResponse toggleBookmark(Long resourceId, Long userId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", resourceId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Optional<Bookmark> bookmarkOpt = bookmarkRepository.findByUserIdAndResourceId(userId, resourceId);
        boolean bookmarked;

        if (bookmarkOpt.isPresent()) {
            bookmarkRepository.deleteByUserIdAndResourceId(userId, resourceId);
            bookmarked = false;
        } else {
            Bookmark newBookmark = new Bookmark(user, resource);
            bookmarkRepository.save(newBookmark);
            bookmarked = true;
        }

        return new BookmarkResponse(resourceId, bookmarked);
    }

    @Transactional(readOnly = true)
    public Page<ResourceResponse> getUserBookmarks(Long userId, Pageable pageable) {
        return bookmarkRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(bookmark -> ResourceResponse.fromEntity(bookmark.getResource()));
    }
}
