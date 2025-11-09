//package com.recipemate.domain.post.service;
//
//import com.recipemate.domain.post.dto.CreatePostRequest;
//import com.recipemate.domain.post.dto.PostResponse;
//import com.recipemate.domain.post.entity.Post;
//import com.recipemate.domain.post.repository.PostRepository;
//import com.recipemate.domain.user.entity.User;
//import com.recipemate.domain.user.repository.UserRepository;
//import com.recipemate.global.common.PostCategory;
//import com.recipemate.global.common.UserRole;
//import com.recipemate.global.config.CacheConfig;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.cache.CacheManager;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.transaction.annotation.Transactional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest
//@ActiveProfiles("test")
//@Transactional
//class PostServiceCacheTest {
//
//    @Autowired
//    private PostService postService;
//
//    @Autowired
//    private PostRepository postRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private CacheManager cacheManager;
//
//    private User testUser;
//
//    @BeforeEach
//    void setUp() {
//        // 캐시 초기화
//        if (cacheManager.getCache(CacheConfig.VIEW_COUNTS_CACHE) != null) {
//            cacheManager.getCache(CacheConfig.VIEW_COUNTS_CACHE).clear();
//        }
//
//        // 테스트 사용자 생성 (전화번호 형식: 010-1234-5678)
//        long uniqueNum = System.nanoTime() % 10000; // 0~9999
//        String phone = String.format("010-1234-%04d", uniqueNum); // 4자리로 패딩
//
//        testUser = User.builder()
//                .email("test" + System.nanoTime() + "@example.com")
//                .password("password")
//                .nickname("testuser" + System.nanoTime())
//                .phoneNumber(phone)
//                .role(UserRole.USER)
//                .build();
//        testUser = userRepository.save(testUser);
//    }
//
//    @Test
//    @DisplayName("게시글 목록 조회 시 캐싱이 동작한다")
//    void testPostListCaching() {
//        // given: 테스트 게시글 생성
//        CreatePostRequest request1 = new CreatePostRequest();
//        request1.setTitle("Test Post 1");
//        request1.setContent("Content 1");
//        request1.setCategory(PostCategory.TIPS);
//
//        CreatePostRequest request2 = new CreatePostRequest();
//        request2.setTitle("Test Post 2");
//        request2.setContent("Content 2");
//        request2.setCategory(PostCategory.TIPS);
//
//        postService.createPost(testUser.getId(), request1);
//        postService.createPost(testUser.getId(), request2);
//
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // when: 첫 번째 조회
//        long startTime = System.currentTimeMillis();
//        Page<PostResponse> firstResult = postService.getPostList(PostCategory.TIPS, null, pageable);
//        long firstQueryTime = System.currentTimeMillis() - startTime;
//
//        // 두 번째 조회 (캐시에서 조회)
//        startTime = System.currentTimeMillis();
//        Page<PostResponse> secondResult = postService.getPostList(PostCategory.TIPS, null, pageable);
//        long secondQueryTime = System.currentTimeMillis() - startTime;
//
//        // then: 같은 결과를 반환하고, 캐시가 적용되어 더 빠름
//        assertThat(firstResult.getContent()).hasSizeGreaterThanOrEqualTo(2);
//        assertThat(secondResult.getContent()).hasSizeGreaterThanOrEqualTo(2);
//        assertThat(firstResult.getContent().get(0).getId())
//                .isEqualTo(secondResult.getContent().get(0).getId());
//
//        // 캐시를 사용한 두 번째 조회가 더 빠르거나 같아야 함
//        assertThat(secondQueryTime).isLessThanOrEqualTo(firstQueryTime + 10);
//    }
//
//    @Test
//    @DisplayName("다른 카테고리 조회 시 별도의 캐시 키를 사용한다")
//    void testDifferentCategoryCacheKeys() {
//        // given: 다른 카테고리의 게시글 생성
//        CreatePostRequest tipsPost = new CreatePostRequest();
//        tipsPost.setTitle("Tips Post");
//        tipsPost.setContent("Tips Content");
//        tipsPost.setCategory(PostCategory.TIPS);
//
//        CreatePostRequest freePost = new CreatePostRequest();
//        freePost.setTitle("Free Post");
//        freePost.setContent("Free Content");
//        freePost.setCategory(PostCategory.FREE);
//
//        postService.createPost(testUser.getId(), tipsPost);
//        postService.createPost(testUser.getId(), freePost);
//
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // when: 각 카테고리별로 조회
//        Page<PostResponse> tipsResults = postService.getPostList(PostCategory.TIPS, null, pageable);
//        Page<PostResponse> freeResults = postService.getPostList(PostCategory.FREE, null, pageable);
//
//        // then: 각각 올바른 게시글만 조회됨
//        assertThat(tipsResults.getContent()).hasSizeGreaterThanOrEqualTo(1);
//        assertThat(tipsResults.getContent()).allMatch(p -> p.getCategory() == PostCategory.TIPS);
//
//        assertThat(freeResults.getContent()).hasSizeGreaterThanOrEqualTo(1);
//        assertThat(freeResults.getContent()).allMatch(p -> p.getCategory() == PostCategory.FREE);
//    }
//
//    @Test
//    @DisplayName("게시글 생성 시 목록 캐시가 무효화된다")
//    void testCacheEvictionOnCreate() {
//        // given: 첫 번째 게시글 생성 및 목록 조회
//        CreatePostRequest request1 = new CreatePostRequest();
//        request1.setTitle("First Post");
//        request1.setContent("First Content");
//        request1.setCategory(PostCategory.TIPS);
//
//        postService.createPost(testUser.getId(), request1);
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // 캐시에 저장
//        Page<PostResponse> firstResult = postService.getPostList(PostCategory.TIPS, null, pageable);
//        int firstCount = firstResult.getContent().size();
//
//        // when: 새 게시글 생성 (캐시 무효화 발생)
//        CreatePostRequest request2 = new CreatePostRequest();
//        request2.setTitle("Second Post");
//        request2.setContent("Second Content");
//        request2.setCategory(PostCategory.TIPS);
//
//        postService.createPost(testUser.getId(), request2);
//
//        // then: 캐시가 무효화되어 새로운 데이터 조회됨
//        Page<PostResponse> secondResult = postService.getPostList(PostCategory.TIPS, null, pageable);
//        assertThat(secondResult.getContent()).hasSize(firstCount + 1);
//    }
//
//    @Test
//    @DisplayName("게시글 삭제 시 목록 캐시가 무효화된다")
//    void testCacheEvictionOnDelete() {
//        // given: 게시글 생성 및 목록 조회
//        CreatePostRequest request = new CreatePostRequest();
//        request.setTitle("Test Post");
//        request.setContent("Test Content");
//        request.setCategory(PostCategory.TIPS);
//
//        PostResponse created = postService.createPost(testUser.getId(), request);
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // 캐시에 저장
//        Page<PostResponse> firstResult = postService.getPostList(PostCategory.TIPS, null, pageable);
//        int firstCount = firstResult.getContent().size();
//
//        // when: 게시글 삭제 (캐시 무효화 발생)
//        postService.deletePost(testUser.getId(), created.getId());
//
//        // then: 캐시가 무효화되어 새로운 데이터 조회됨 (soft delete이므로 삭제된 게시글은 조회되지 않음)
//        Page<PostResponse> secondResult = postService.getPostList(PostCategory.TIPS, null, pageable);
//        assertThat(secondResult.getContent()).hasSize(firstCount - 1);
//    }
//
//    @Test
//    @DisplayName("게시글 조회 시 조회수가 증가한다")
//    void testViewCountIncrement() {
//        // given: 테스트 게시글 생성
//        CreatePostRequest request = new CreatePostRequest();
//        request.setTitle("Test Post");
//        request.setContent("Test Content");
//        request.setCategory(PostCategory.TIPS);
//
//        PostResponse created = postService.createPost(testUser.getId(), request);
//        assertThat(created.getViewCount()).isEqualTo(0);
//
//        // when: 게시글 상세 조회 (조회수 증가)
//        PostResponse detail1 = postService.getPostDetail(created.getId());
//        PostResponse detail2 = postService.getPostDetail(created.getId());
//
//        // then: 조회수가 증가함
//        assertThat(detail1.getViewCount()).isEqualTo(1);
//        assertThat(detail2.getViewCount()).isEqualTo(2);
//
//        // DB에서 직접 확인
//        Post post = postRepository.findById(created.getId()).orElseThrow();
//        assertThat(post.getViewCount()).isEqualTo(2);
//    }
//}
