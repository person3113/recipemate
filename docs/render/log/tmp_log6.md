	Maximum pool size: undefined/unknown
	Minimum pool size: undefined/unknown
	Isolation level: undefined/unknown
	Autocommit mode: undefined/unknown
	Database version: 18.1
	Database driver: undefined/unknown
	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
2025-11-24T03:24:26.258Z  INFO 1 --- [RecipeMate] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
2025-11-24T03:24:25.758Z  WARN 1 --- [RecipeMate] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2025-11-24T03:24:24.755Z  INFO 1 --- [RecipeMate] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2025-11-24T03:24:24.660Z  INFO 1 --- [RecipeMate] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@68f1b89
2025-11-24T03:24:19.561Z  INFO 1 --- [RecipeMate] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2025-11-24T03:24:19.061Z  INFO 1 --- [RecipeMate] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2025-11-24T03:24:13.859Z  INFO 1 --- [RecipeMate] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2025-11-24T03:24:13.258Z  INFO 1 --- [RecipeMate] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.33.Final
2025-11-24T03:24:12.460Z  INFO 1 --- [RecipeMate] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2025-11-24T03:24:05.757Z  INFO 1 --- [RecipeMate] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 42100 ms
2025-11-24T03:24:05.755Z  INFO 1 --- [RecipeMate] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2025-11-24T03:24:04.255Z  INFO 1 --- [RecipeMate] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.48]
2025-11-24T03:24:04.255Z  INFO 1 --- [RecipeMate] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2025-11-24T03:24:03.959Z  INFO 1 --- [RecipeMate] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8080 (http)
==> Detected a new open port HTTP:8080
2025-11-24T03:23:46.655Z  INFO 1 --- [RecipeMate] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 500 ms. Found 0 Redis repository interfaces.
2025-11-24T03:23:46.655Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.wishlist.repository.WishlistRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:23:46.654Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.user.repository.UserRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:23:46.559Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.user.repository.PointHistoryRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:23:46.559Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.user.repository.PersistentTokenJpaRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:23:46.559Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.user.repository.MannerTempHistoryRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:23:46.559Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.user.repository.AddressRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:23:46.559Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.search.repository.SearchKeywordRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:23:46.558Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.review.repository.ReviewRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:23:46.558Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.report.repository.ReportRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:23:46.558Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.recipewishlist.repository.RecipeWishlistRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:23:46.558Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.recipe.repository.RecipeStepRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:23:46.557Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.recipe.repository.RecipeRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:23:46.557Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.recipe.repository.RecipeIngredientRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:23:46.459Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.recipe.repository.RecipeCorrectionRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:23:46.459Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.post.repository.PostRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:23:46.459Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.post.repository.PostImageRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:23:46.459Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.notification.repository.NotificationRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:23:46.458Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.like.repository.PostLikeRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:23:46.458Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.like.repository.CommentLikeRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:23:46.458Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.groupbuy.repository.ParticipationRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:23:46.457Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.groupbuy.repository.GroupBuyRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:23:46.457Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.groupbuy.repository.GroupBuyImageRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:23:46.457Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.directmessage.repository.DirectMessageRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:23:46.456Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.comment.repository.CommentRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:23:46.454Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.badge.repository.BadgeRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:23:45.958Z  INFO 1 --- [RecipeMate] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data Redis repositories in DEFAULT mode.
2025-11-24T03:23:45.957Z  INFO 1 --- [RecipeMate] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2025-11-24T03:23:45.456Z  INFO 1 --- [RecipeMate] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 3901 ms. Found 25 JPA repository interfaces.
2025-11-24T03:23:41.455Z  INFO 1 --- [RecipeMate] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2025-11-24T03:23:41.357Z  INFO 1 --- [RecipeMate] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
Deploy cancelled
2025-11-24T03:23:22.160Z  INFO 1 --- [RecipeMate] [           main] com.recipemate.RecipeMateApplication     : The following 1 profile is active: "prod"
2025-11-24T03:23:22.157Z  INFO 1 --- [RecipeMate] [           main] com.recipemate.RecipeMateApplication     : Starting RecipeMateApplication v0.0.1-SNAPSHOT using Java 21.0.9 with PID 1 (/app/app.jar started by spring in /app)
:: Spring Boot ::                (v3.5.7)
=========|_|==============|___/=/_/_/_/
'  |____| .__|_| |_|_| |_\__, | / / / /
\\/  ___)| |_)| | | | | || (_| |  ) ) ) )
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
  .   ____          _            __ _ _
==> Docs on specifying a port: https://render.com/docs/web-services#port-binding
==> New primary port detected: 8080. Restarting deploy to update network configuration...
==> Detected a new open port HTTP:8080
	at java.base/java.lang.Thread.run(Unknown Source) ~[na:na]
	at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:63) ~[tomcat-embed-core-10.1.48.jar!/:na]
	at org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:491) ~[tomcat-embed-core-10.1.48.jar!/:na]
	at org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:973) ~[tomcat-embed-core-10.1.48.jar!/:na]
	at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:52) ~[tomcat-embed-core-10.1.48.jar!/:na]
	at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1774) ~[tomcat-embed-core-10.1.48.jar!/:na]
	at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:903) ~[tomcat-embed-core-10.1.48.jar!/:na]
	at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:63) ~[tomcat-embed-core-10.1.48.jar!/:na]
	at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:399) ~[tomcat-embed-core-10.1.48.jar!/:na]
	at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:342) ~[tomcat-embed-core-10.1.48.jar!/:na]
	at org.apache.catalina.valves.RemoteIpValve.invoke(RemoteIpValve.java:733) ~[tomcat-embed-core-10.1.48.jar!/:na]
	at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:72) ~[tomcat-embed-core-10.1.48.jar!/:na]
	at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:83) ~[tomcat-embed-core-10.1.48.jar!/:na]
	at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:113) ~[tomcat-embed-core-10.1.48.jar!/:na]
	at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:482) ~[tomcat-embed-core-10.1.48.jar!/:na]
	at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:88) ~[tomcat-embed-core-10.1.48.jar!/:na]
	at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:165) ~[tomcat-embed-core-10.1.48.jar!/:na]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:138) ~[tomcat-embed-core-10.1.48.jar!/:na]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:162) ~[tomcat-embed-core-10.1.48.jar!/:na]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.2.12.jar!/:6.2.12]
	at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201) ~[spring-web-6.2.12.jar!/:6.2.12]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:138) ~[tomcat-embed-core-10.1.48.jar!/:na]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:162) ~[tomcat-embed-core-10.1.48.jar!/:na]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.2.12.jar!/:6.2.12]
	at org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93) ~[spring-web-6.2.12.jar!/:6.2.12]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:138) ~[tomcat-embed-core-10.1.48.jar!/:na]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:162) ~[tomcat-embed-core-10.1.48.jar!/:na]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.2.12.jar!/:6.2.12]
	at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100) ~[spring-web-6.2.12.jar!/:6.2.12]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:138) ~[tomcat-embed-core-10.1.48.jar!/:na]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:162) ~[tomcat-embed-core-10.1.48.jar!/:na]
	at org.springframework.web.filter.DelegatingFilterProxy.doFilter(DelegatingFilterProxy.java:278) ~[spring-web-6.2.12.jar!/:6.2.12]
	at org.springframework.web.filter.DelegatingFilterProxy.invokeDelegate(DelegatingFilterProxy.java:362) ~[spring-web-6.2.12.jar!/:6.2.12]
	at org.springframework.security.config.annotation.web.configuration.WebMvcSecurityConfiguration$CompositeFilterChainProxy.doFilter(WebMvcSecurityConfiguration.java:240) ~[spring-security-config-6.5.6.jar!/:6.5.6]
	at org.springframework.web.filter.CompositeFilter.doFilter(CompositeFilter.java:74) ~[spring-web-6.2.12.jar!/:6.2.12]
	at org.springframework.web.filter.CompositeFilter$VirtualFilterChain.doFilter(CompositeFilter.java:113) ~[spring-web-6.2.12.jar!/:6.2.12]
	at org.springframework.web.servlet.handler.HandlerMappingIntrospector.lambda$createCacheFilter$4(HandlerMappingIntrospector.java:267) ~[spring-webmvc-6.2.12.jar!/:6.2.12]
	at org.springframework.web.filter.CompositeFilter$VirtualFilterChain.doFilter(CompositeFilter.java:113) ~[spring-web-6.2.12.jar!/:6.2.12]
	at org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration$CompositeFilterChainProxy.doFilter(WebSecurityConfiguration.java:319) ~[spring-security-config-6.5.6.jar!/:6.5.6]
	at org.springframework.web.filter.CompositeFilter.doFilter(CompositeFilter.java:74) ~[spring-web-6.2.12.jar!/:6.2.12]
	at org.springframework.web.filter.CompositeFilter$VirtualFilterChain.doFilter(CompositeFilter.java:113) ~[spring-web-6.2.12.jar!/:6.2.12]
	at org.springframework.web.filter.ServletRequestPathFilter.doFilter(ServletRequestPathFilter.java:52) ~[spring-web-6.2.12.jar!/:6.2.12]
	at org.springframework.web.filter.CompositeFilter$VirtualFilterChain.doFilter(CompositeFilter.java:113) ~[spring-web-6.2.12.jar!/:6.2.12]
	at org.springframework.security.web.FilterChainProxy.doFilter(FilterChainProxy.java:191) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.security.web.FilterChainProxy.doFilterInternal(FilterChainProxy.java:233) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:374) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.2.12.jar!/:6.2.12]
	at org.springframework.security.web.session.DisableEncodeUrlFilter.doFilterInternal(DisableEncodeUrlFilter.java:42) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:374) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.2.12.jar!/:6.2.12]
	at org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter.doFilterInternal(WebAsyncManagerIntegrationFilter.java:62) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:374) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.security.web.context.SecurityContextHolderFilter.doFilter(SecurityContextHolderFilter.java:69) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.security.web.context.SecurityContextHolderFilter.doFilter(SecurityContextHolderFilter.java:82) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:374) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.2.12.jar!/:6.2.12]
	at org.springframework.security.web.header.HeaderWriterFilter.doFilterInternal(HeaderWriterFilter.java:75) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.security.web.header.HeaderWriterFilter.doHeadersAfter(HeaderWriterFilter.java:90) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:374) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.2.12.jar!/:6.2.12]
	at org.springframework.security.web.csrf.CsrfFilter.doFilterInternal(CsrfFilter.java:117) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:374) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.security.web.authentication.logout.LogoutFilter.doFilter(LogoutFilter.java:93) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.security.web.authentication.logout.LogoutFilter.doFilter(LogoutFilter.java:107) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:374) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter.doFilter(AbstractAuthenticationProcessingFilter.java:229) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter.doFilter(AbstractAuthenticationProcessingFilter.java:235) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:374) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.security.web.savedrequest.RequestCacheAwareFilter.doFilter(RequestCacheAwareFilter.java:63) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:374) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter.doFilter(SecurityContextHolderAwareRequestFilter.java:179) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:374) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter.doFilter(RememberMeAuthenticationFilter.java:105) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter.doFilter(RememberMeAuthenticationFilter.java:150) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:374) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.security.web.authentication.AnonymousAuthenticationFilter.doFilter(AnonymousAuthenticationFilter.java:100) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:374) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.security.web.access.ExceptionTranslationFilter.doFilter(ExceptionTranslationFilter.java:119) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.security.web.access.ExceptionTranslationFilter.doFilter(ExceptionTranslationFilter.java:125) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:374) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.security.web.access.intercept.AuthorizationFilter.doFilter(AuthorizationFilter.java:101) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:365) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.security.web.FilterChainProxy.lambda$doFilterInternal$3(FilterChainProxy.java:231) ~[spring-security-web-6.5.6.jar!/:6.5.6]
	at org.springframework.web.filter.CompositeFilter$VirtualFilterChain.doFilter(CompositeFilter.java:108) ~[spring-web-6.2.12.jar!/:6.2.12]
	at org.springframework.web.filter.CompositeFilter$VirtualFilterChain.doFilter(CompositeFilter.java:108) ~[spring-web-6.2.12.jar!/:6.2.12]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:138) ~[tomcat-embed-core-10.1.48.jar!/:na]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:162) ~[tomcat-embed-core-10.1.48.jar!/:na]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.2.12.jar!/:6.2.12]
	at org.springframework.web.filter.HiddenHttpMethodFilter.doFilterInternal(HiddenHttpMethodFilter.java:91) ~[spring-web-6.2.12.jar!/:6.2.12]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:138) ~[tomcat-embed-core-10.1.48.jar!/:na]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:162) ~[tomcat-embed-core-10.1.48.jar!/:na]
	at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:51) ~[tomcat-embed-websocket-10.1.48.jar!/:na]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:138) ~[tomcat-embed-core-10.1.48.jar!/:na]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193) ~[tomcat-embed-core-10.1.48.jar!/:na]
	at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:658) ~[tomcat-embed-core-10.1.48.jar!/:na]
	at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:885) ~[spring-webmvc-6.2.12.jar!/:6.2.12]
	at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:587) ~[tomcat-embed-core-10.1.48.jar!/:na]
	at jakarta.servlet.http.HttpServlet.doHead(HttpServlet.java:211) ~[tomcat-embed-core-10.1.48.jar!/:na]
	at org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:903) ~[spring-webmvc-6.2.12.jar!/:6.2.12]
	at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1014) ~[spring-webmvc-6.2.12.jar!/:6.2.12]
	at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:979) ~[spring-webmvc-6.2.12.jar!/:6.2.12]
	at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1089) ~[spring-webmvc-6.2.12.jar!/:6.2.12]
	at org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:87) ~[spring-webmvc-6.2.12.jar!/:6.2.12]
	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:896) ~[spring-webmvc-6.2.12.jar!/:6.2.12]
	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:991) ~[spring-webmvc-6.2.12.jar!/:6.2.12]
	at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:118) ~[spring-webmvc-6.2.12.jar!/:6.2.12]
	at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:191) ~[spring-web-6.2.12.jar!/:6.2.12]
	at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:258) ~[spring-web-6.2.12.jar!/:6.2.12]
	at java.base/java.lang.reflect.Method.invoke(Unknown Source) ~[na:na]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(Unknown Source) ~[na:na]
	at com.recipemate.global.controller.HomeController.home(HomeController.java:44) ~[!/:0.0.1-SNAPSHOT]
	at com.recipemate.domain.groupbuy.service.GroupBuyService$$SpringCGLIB$$0.getPopularGroupBuys(<generated>) ~[!/:0.0.1-SNAPSHOT]
	at org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:728) ~[spring-aop-6.2.12.jar!/:6.2.12]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184) ~[spring-aop-6.2.12.jar!/:6.2.12]
	at org.springframework.cache.interceptor.CacheInterceptor.invoke(CacheInterceptor.java:65) ~[spring-context-6.2.12.jar!/:6.2.12]
	at org.springframework.cache.interceptor.CacheAspectSupport.execute(CacheAspectSupport.java:410) ~[spring-context-6.2.12.jar!/:6.2.12]
	at org.springframework.cache.interceptor.CacheAspectSupport.execute(CacheAspectSupport.java:446) ~[spring-context-6.2.12.jar!/:6.2.12]
	at org.springframework.cache.interceptor.CacheAspectSupport.findCachedValue(CacheAspectSupport.java:523) ~[spring-context-6.2.12.jar!/:6.2.12]
	at org.springframework.cache.interceptor.CacheAspectSupport.findInCaches(CacheAspectSupport.java:574) ~[spring-context-6.2.12.jar!/:6.2.12]
	at org.springframework.cache.interceptor.AbstractCacheInvoker.doGet(AbstractCacheInvoker.java:78) ~[spring-context-6.2.12.jar!/:6.2.12]
	at org.springframework.cache.support.AbstractValueAdaptingCache.get(AbstractValueAdaptingCache.java:58) ~[spring-context-6.2.12.jar!/:6.2.12]
	at org.springframework.data.redis.cache.RedisCache.lookup(RedisCache.java:187) ~[spring-data-redis-3.5.5.jar!/:3.5.5]
	at org.springframework.data.redis.cache.RedisCache.deserializeCacheValue(RedisCache.java:361) ~[spring-data-redis-3.5.5.jar!/:3.5.5]
	at org.springframework.data.redis.serializer.RedisSerializationContext$SerializationPair.read(RedisSerializationContext.java:277) ~[spring-data-redis-3.5.5.jar!/:3.5.5]
	at org.springframework.data.redis.serializer.DefaultRedisElementReader.read(DefaultRedisElementReader.java:46) ~[spring-data-redis-3.5.5.jar!/:3.5.5]
	at org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer.deserialize(GenericJackson2JsonRedisSerializer.java:281) ~[spring-data-redis-3.5.5.jar!/:3.5.5]
	at org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer.deserialize(GenericJackson2JsonRedisSerializer.java:309) ~[spring-data-redis-3.5.5.jar!/:3.5.5]
	at org.springframework.data.redis.serializer.JacksonObjectReader.lambda$create$0(JacksonObjectReader.java:54) ~[spring-data-redis-3.5.5.jar!/:3.5.5]
	at com.fasterxml.jackson.databind.ObjectMapper.readValue(ObjectMapper.java:3989) ~[jackson-databind-2.19.2.jar!/:2.19.2]
	at com.fasterxml.jackson.databind.ObjectMapper._readMapAndClose(ObjectMapper.java:4971) ~[jackson-databind-2.19.2.jar!/:2.19.2]
	at com.fasterxml.jackson.databind.deser.DefaultDeserializationContext.readRootValue(DefaultDeserializationContext.java:342) ~[jackson-databind-2.19.2.jar!/:2.19.2]
	at com.fasterxml.jackson.databind.deser.impl.TypeWrappedDeserializer.deserialize(TypeWrappedDeserializer.java:74) ~[jackson-databind-2.19.2.jar!/:2.19.2]
	at com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializerNR.deserializeWithType(UntypedObjectDeserializerNR.java:111) ~[jackson-databind-2.19.2.jar!/:2.19.2]
	at com.fasterxml.jackson.databind.jsontype.impl.AsPropertyTypeDeserializer.deserializeTypedFromAny(AsPropertyTypeDeserializer.java:237) ~[jackson-databind-2.19.2.jar!/:2.19.2]
	at com.fasterxml.jackson.databind.jsontype.impl.AsArrayTypeDeserializer.deserializeTypedFromArray(AsArrayTypeDeserializer.java:55) ~[jackson-databind-2.19.2.jar!/:2.19.2]
	at com.fasterxml.jackson.databind.jsontype.impl.AsArrayTypeDeserializer._deserialize(AsArrayTypeDeserializer.java:98) ~[jackson-databind-2.19.2.jar!/:2.19.2]
	at com.fasterxml.jackson.databind.jsontype.impl.AsArrayTypeDeserializer._locateTypeId(AsArrayTypeDeserializer.java:174) ~[jackson-databind-2.19.2.jar!/:2.19.2]
	at com.fasterxml.jackson.databind.DeserializationContext.reportWrongTokenException(DeserializationContext.java:1726) ~[jackson-databind-2.19.2.jar!/:2.19.2]
	at com.fasterxml.jackson.databind.DeserializationContext.wrongTokenException(DeserializationContext.java:1941) ~[jackson-databind-2.19.2.jar!/:2.19.2]
	at com.fasterxml.jackson.databind.exc.MismatchedInputException.from(MismatchedInputException.java:59) ~[jackson-databind-2.19.2.jar!/:2.19.2]
 at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 1, column: 2]
com.fasterxml.jackson.databind.exc.MismatchedInputException: Unexpected token (END_ARRAY), expected VALUE_STRING: need String, Number of Boolean value that contains type id (for subtype of java.lang.Object)
 at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 1, column: 2] ] with root cause
2025-11-24T03:22:17.233Z ERROR 1 --- [RecipeMate] [nio-8080-exec-5] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed: org.springframework.data.redis.serializer.SerializationException: Could not read JSON:Unexpected token (END_ARRAY), expected VALUE_STRING: need String, Number of Boolean value that contains type id (for subtype of java.lang.Object)
2025-11-24T03:22:16.236Z  WARN 1 --- [RecipeMate] [nio-8080-exec-5] c.r.domain.recipe.service.RecipeService  : DB에 레시피가 없습니다
2025-11-24T03:22:16.233Z  INFO 1 --- [RecipeMate] [nio-8080-exec-5] c.r.domain.recipe.service.RecipeService  : DB 기반 랜덤 레시피 조회 요청: count=3
2025-11-24T03:22:12.633Z  WARN 1 --- [RecipeMate] [nio-8080-exec-4] c.r.domain.recipe.service.RecipeService  : DB에 레시피가 없습니다
2025-11-24T03:22:12.437Z  WARN 1 --- [RecipeMate] [nio-8080-exec-1] c.r.domain.recipe.service.RecipeService  : DB에 레시피가 없습니다
2025-11-24T03:22:12.437Z  WARN 1 --- [RecipeMate] [nio-8080-exec-3] c.r.domain.recipe.service.RecipeService  : DB에 레시피가 없습니다
2025-11-24T03:22:12.437Z  WARN 1 --- [RecipeMate] [nio-8080-exec-2] c.r.domain.recipe.service.RecipeService  : DB에 레시피가 없습니다
2025-11-24T03:22:12.234Z  INFO 1 --- [RecipeMate] [nio-8080-exec-1] c.r.domain.recipe.service.RecipeService  : DB 기반 랜덤 레시피 조회 요청: count=3
2025-11-24T03:22:12.233Z  INFO 1 --- [RecipeMate] [nio-8080-exec-2] c.r.domain.recipe.service.RecipeService  : DB 기반 랜덤 레시피 조회 요청: count=3
2025-11-24T03:22:12.232Z  INFO 1 --- [RecipeMate] [nio-8080-exec-4] c.r.domain.recipe.service.RecipeService  : DB 기반 랜덤 레시피 조회 요청: count=3
2025-11-24T03:22:12.137Z  INFO 1 --- [RecipeMate] [nio-8080-exec-3] c.r.domain.recipe.service.RecipeService  : DB 기반 랜덤 레시피 조회 요청: count=3
2025-11-24T03:22:09.336Z  INFO 1 --- [RecipeMate] [nio-8080-exec-4] c.r.domain.recipe.service.RecipeService  : DB 기반 인기 레시피 조회 요청: size=5
2025-11-24T03:22:05.034Z  INFO 1 --- [RecipeMate] [nio-8080-exec-1] c.r.domain.recipe.service.RecipeService  : DB 기반 인기 레시피 조회 요청: size=5
2025-11-24T03:22:05.034Z  INFO 1 --- [RecipeMate] [nio-8080-exec-3] c.r.domain.recipe.service.RecipeService  : DB 기반 인기 레시피 조회 요청: size=5
2025-11-24T03:22:05.034Z  INFO 1 --- [RecipeMate] [nio-8080-exec-2] c.r.domain.recipe.service.RecipeService  : DB 기반 인기 레시피 조회 요청: size=5
2025-11-24T03:21:57.232Z  INFO 1 --- [RecipeMate] [           main] c.r.d.g.service.GroupBuyScheduler        : 서버 시작 - 공구 상태 일괄 업데이트 완료
2025-11-24T03:21:57.137Z  INFO 1 --- [RecipeMate] [           main] c.r.d.g.service.GroupBuyScheduler        : D-1, D-2 공구 0 건을 IMMINENT 상태로 변경했습니다.
2025-11-24T03:21:56.933Z  INFO 1 --- [RecipeMate] [           main] c.r.d.g.service.GroupBuyScheduler        : 마감일이 지난 공구 0 건을 CLOSED 상태로 변경했습니다.
2025-11-24T03:21:55.735Z  INFO 1 --- [RecipeMate] [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 96 ms
2025-11-24T03:21:55.638Z  INFO 1 --- [RecipeMate] [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2025-11-24T03:21:55.638Z  INFO 1 --- [RecipeMate] [nio-8080-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2025-11-24T03:21:53.744Z  INFO 1 --- [RecipeMate] [           main] c.r.d.g.service.GroupBuyScheduler        : 서버 시작 - 공구 상태 일괄 업데이트 시작
2025-11-24T03:21:53.535Z  INFO 1 --- [RecipeMate] [           main] com.recipemate.RecipeMateApplication     : Started RecipeMateApplication in 208.998 seconds (process running for 218.85)
2025-11-24T03:21:53.135Z  INFO 1 --- [RecipeMate] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path '/'
2025-11-24T03:21:42.438Z  INFO 1 --- [RecipeMate] [           main] r$InitializeUserDetailsManagerConfigurer : Global AuthenticationManager configured with UserDetailsService bean with name customUserDetailsService
2025-11-24T03:21:38.033Z  INFO 1 --- [RecipeMate] [           main] o.s.b.a.w.s.WelcomePageHandlerMapping    : Adding welcome page template: index
2025-11-24T03:21:37.336Z  WARN 1 --- [RecipeMate] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
==> Docs on specifying a port: https://render.com/docs/web-services#port-binding
==> No open ports detected, continuing to scan...
==> Docs on specifying a port: https://render.com/docs/web-services#port-binding
==> No open ports detected, continuing to scan...
2025-11-24T03:20:17.836Z  INFO 1 --- [RecipeMate] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2025-11-24T03:20:11.542Z  INFO 1 --- [RecipeMate] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2025-11-24T03:20:09.444Z  INFO 1 --- [RecipeMate] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2025-11-24T03:19:46.639Z  WARN 1 --- [RecipeMate] [           main] o.h.boot.model.internal.ToOneBinder      : HHH000491: 'com.recipemate.domain.recipewishlist.entity.RecipeWishlist.recipe' uses both @NotFound and FetchType.LAZY. @ManyToOne and @OneToOne associations mapped with @NotFound are forced to EAGER fetching.
	Maximum pool size: undefined/unknown
	Minimum pool size: undefined/unknown
	Isolation level: undefined/unknown
	Autocommit mode: undefined/unknown
	Database version: 18.1
	Database driver: undefined/unknown
	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
2025-11-24T03:19:41.034Z  INFO 1 --- [RecipeMate] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
2025-11-24T03:19:40.333Z  WARN 1 --- [RecipeMate] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2025-11-24T03:19:39.438Z  INFO 1 --- [RecipeMate] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2025-11-24T03:19:39.436Z  INFO 1 --- [RecipeMate] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@28fef9a2
2025-11-24T03:19:34.438Z  INFO 1 --- [RecipeMate] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2025-11-24T03:19:33.937Z  INFO 1 --- [RecipeMate] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2025-11-24T03:19:28.934Z  INFO 1 --- [RecipeMate] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2025-11-24T03:19:28.434Z  INFO 1 --- [RecipeMate] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.33.Final
2025-11-24T03:19:27.534Z  INFO 1 --- [RecipeMate] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2025-11-24T03:19:19.934Z  INFO 1 --- [RecipeMate] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 44191 ms
2025-11-24T03:19:19.839Z  INFO 1 --- [RecipeMate] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2025-11-24T03:19:18.233Z  INFO 1 --- [RecipeMate] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.48]
2025-11-24T03:19:18.233Z  INFO 1 --- [RecipeMate] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2025-11-24T03:19:17.839Z  INFO 1 --- [RecipeMate] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8080 (http)
==> Docs on specifying a port: https://render.com/docs/web-services#port-binding
==> No open ports detected, continuing to scan...
2025-11-24T03:19:00.738Z  INFO 1 --- [RecipeMate] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 497 ms. Found 0 Redis repository interfaces.
2025-11-24T03:19:00.738Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.wishlist.repository.WishlistRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:19:00.738Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.user.repository.UserRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:19:00.737Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.user.repository.PointHistoryRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:19:00.737Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.user.repository.PersistentTokenJpaRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:19:00.737Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.user.repository.MannerTempHistoryRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:19:00.737Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.user.repository.AddressRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:19:00.736Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.search.repository.SearchKeywordRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:19:00.736Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.review.repository.ReviewRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:19:00.736Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.report.repository.ReportRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:19:00.736Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.recipewishlist.repository.RecipeWishlistRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:19:00.736Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.recipe.repository.RecipeStepRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:19:00.735Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.recipe.repository.RecipeRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:19:00.734Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.recipe.repository.RecipeIngredientRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:19:00.634Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.recipe.repository.RecipeCorrectionRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:19:00.634Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.post.repository.PostRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:19:00.634Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.post.repository.PostImageRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:19:00.634Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.notification.repository.NotificationRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:19:00.633Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.like.repository.PostLikeRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:19:00.633Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.like.repository.CommentLikeRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:19:00.633Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.groupbuy.repository.ParticipationRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:19:00.540Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.groupbuy.repository.GroupBuyRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:19:00.539Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.groupbuy.repository.GroupBuyImageRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:19:00.539Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.directmessage.repository.DirectMessageRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:19:00.539Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.comment.repository.CommentRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:19:00.537Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.badge.repository.BadgeRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T03:19:00.139Z  INFO 1 --- [RecipeMate] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data Redis repositories in DEFAULT mode.
2025-11-24T03:19:00.138Z  INFO 1 --- [RecipeMate] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2025-11-24T03:18:59.637Z  INFO 1 --- [RecipeMate] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 4397 ms. Found 25 JPA repository interfaces.
2025-11-24T03:18:55.138Z  INFO 1 --- [RecipeMate] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2025-11-24T03:18:55.136Z  INFO 1 --- [RecipeMate] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2025-11-24T03:18:34.734Z  INFO 1 --- [RecipeMate] [           main] com.recipemate.RecipeMateApplication     : The following 1 profile is active: "prod"
2025-11-24T03:18:34.636Z  INFO 1 --- [RecipeMate] [           main] com.recipemate.RecipeMateApplication     : Starting RecipeMateApplication v0.0.1-SNAPSHOT using Java 21.0.9 with PID 1 (/app/app.jar started by spring in /app)
 :: Spring Boot ::                (v3.5.7)
 =========|_|==============|___/=/_/_/_/
  '  |____| .__|_| |_|_| |_\__, | / / / /
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
/\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
.   ____          _            __ _ _
==> Deploying...
Upload succeeded
Pushing image to registry...
#21 DONE 21.0s
#21 writing cache image manifest sha256:cfa075f418c2c4dc7bf4c560cb243b579c82937db172efe31d1a6391220c6f3f done
#21 preparing build cache for export
#21 exporting cache to client directory
#20 DONE 2.9s
#20 exporting config sha256:176a7f5b870026abcd00f5fcce83d9c7eb56a97f6e92954064e6f1eb567f76ef done
#20 exporting manifest sha256:8a0c06cf4277dbee854ee14af227f641935d5c5fbdbe54333b337be5e3e7cbb5 done
#20 exporting layers 2.0s done
#20 exporting layers
#20 exporting to docker image format
#19 DONE 8.6s
#19 [stage-1 5/5] COPY --from=builder /app/build/libs/*.jar app.jar
#18 DONE 83.0s
#18 82.29 4 actionable tasks: 4 executed
#18 82.29 BUILD SUCCESSFUL in 1m 22s
#18 82.29
#18 82.29 > Task :bootJar
#18 72.80 > Task :resolveMainClassName
#18 72.50 > Task :classes
#18 72.40 > Task :processResources
#18 72.40
#18 56.40 Note: Recompile with -Xlint:unchecked for details.
#18 56.40 Note: /app/src/main/java/com/recipemate/global/util/ImageUploadUtil.java uses unchecked or unsafe operations.
#18 56.40 Note: Recompile with -Xlint:deprecation for details.
#18 56.30 Note: Some input files use or override a deprecated API.
#18 46.50 > Task :compileJava
#18 2.888 Daemon will be stopped at the end of the build
#18 0.988 To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/8.5/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
#18 [builder 7/7] RUN gradle bootJar --no-daemon -x test
#17 DONE 0.6s
#17 [builder 6/7] COPY src src/