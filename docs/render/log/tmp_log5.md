==> Common ways to troubleshoot your deploy: https://render.com/docs/troubleshooting-deploys
==> Exited with status 1
==> Docs on specifying a port: https://render.com/docs/web-services#port-binding
==> No open ports detected, continuing to scan...
2025-11-24T02:27:10.837Z  INFO 1 --- [RecipeMate] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.
2025-11-24T02:27:10.542Z  INFO 1 --- [RecipeMate] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2025-11-24T02:27:10.540Z  INFO 1 --- [RecipeMate] [           main] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2025-11-24T02:27:10.536Z  INFO 1 --- [RecipeMate] [           main] c.r.global.util.ImageUploadUtil          : Shutting down ImageUploadUtil executor service
2025-11-24T02:27:08.384Z  INFO 1 --- [RecipeMate] [tomcat-shutdown] o.s.b.w.e.tomcat.GracefulShutdown        : Graceful shutdown complete
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
at com.recipemate.global.controller.HomeController.home(HomeController.java:32) ~[!/:0.0.1-SNAPSHOT]
at com.recipemate.domain.recipe.service.RecipeService$$SpringCGLIB$$0.findPopularRecipes(<generated>) ~[!/:0.0.1-SNAPSHOT]
at org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:728) ~[spring-aop-6.2.12.jar!/:6.2.12]
at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184) ~[spring-aop-6.2.12.jar!/:6.2.12]
at org.springframework.cache.interceptor.CacheInterceptor.invoke(CacheInterceptor.java:65) ~[spring-context-6.2.12.jar!/:6.2.12]
at org.springframework.cache.interceptor.CacheAspectSupport.execute(CacheAspectSupport.java:410) ~[spring-context-6.2.12.jar!/:6.2.12]
at org.springframework.cache.interceptor.CacheAspectSupport.execute(CacheAspectSupport.java:448) ~[spring-context-6.2.12.jar!/:6.2.12]
at org.springframework.cache.interceptor.CacheAspectSupport.evaluate(CacheAspectSupport.java:601) ~[spring-context-6.2.12.jar!/:6.2.12]
at org.springframework.cache.interceptor.CacheAspectSupport.invokeOperation(CacheAspectSupport.java:431) ~[spring-context-6.2.12.jar!/:6.2.12]
at org.springframework.cache.interceptor.CacheInterceptor.lambda$invoke$0(CacheInterceptor.java:55) ~[spring-context-6.2.12.jar!/:6.2.12]
at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184) ~[spring-aop-6.2.12.jar!/:6.2.12]
at org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:119) ~[spring-tx-6.2.12.jar!/:6.2.12]
at org.springframework.transaction.interceptor.TransactionAspectSupport.invokeWithinTransaction(TransactionAspectSupport.java:380) ~[spring-tx-6.2.12.jar!/:6.2.12]
at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163) ~[spring-aop-6.2.12.jar!/:6.2.12]
at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:196) ~[spring-aop-6.2.12.jar!/:6.2.12]
at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:360) ~[spring-aop-6.2.12.jar!/:6.2.12]
at java.base/java.lang.reflect.Method.invoke(Unknown Source) ~[na:na]
at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(Unknown Source) ~[na:na]
at com.recipemate.domain.recipe.service.RecipeService.findPopularRecipes(RecipeService.java:178) ~[!/:0.0.1-SNAPSHOT]
at com.querydsl.jpa.impl.AbstractJPAQuery.fetch(AbstractJPAQuery.java:249) ~[querydsl-jpa-7.0.jar!/:na]
at com.querydsl.jpa.impl.AbstractJPAQuery.getResultList(AbstractJPAQuery.java:197) ~[querydsl-jpa-7.0.jar!/:na]
at org.hibernate.query.Query.getResultList(Query.java:120) ~[hibernate-core-6.6.33.Final.jar!/:6.6.33.Final]
at org.hibernate.query.spi.AbstractSelectionQuery.list(AbstractSelectionQuery.java:143) ~[hibernate-core-6.6.33.Final.jar!/:6.6.33.Final]
at org.hibernate.query.sqm.internal.QuerySqmImpl.doList(QuerySqmImpl.java:380) ~[hibernate-core-6.6.33.Final.jar!/:6.6.33.Final]
at org.hibernate.query.sqm.internal.ConcreteSqmSelectQueryPlan.performList(ConcreteSqmSelectQueryPlan.java:359) ~[hibernate-core-6.6.33.Final.jar!/:6.6.33.Final]
at org.hibernate.query.sqm.internal.ConcreteSqmSelectQueryPlan.withCacheableSqmInterpretation(ConcreteSqmSelectQueryPlan.java:439) ~[hibernate-core-6.6.33.Final.jar!/:6.6.33.Final]
at org.hibernate.query.sqm.internal.ConcreteSqmSelectQueryPlan.lambda$new$1(ConcreteSqmSelectQueryPlan.java:149) ~[hibernate-core-6.6.33.Final.jar!/:6.6.33.Final]
at org.hibernate.sql.exec.spi.JdbcSelectExecutor.list(JdbcSelectExecutor.java:165) ~[hibernate-core-6.6.33.Final.jar!/:6.6.33.Final]
at org.hibernate.sql.exec.spi.JdbcSelectExecutor.executeQuery(JdbcSelectExecutor.java:91) ~[hibernate-core-6.6.33.Final.jar!/:6.6.33.Final]
at org.hibernate.sql.exec.internal.JdbcSelectExecutorStandardImpl.executeQuery(JdbcSelectExecutorStandardImpl.java:102) ~[hibernate-core-6.6.33.Final.jar!/:6.6.33.Final]
at org.hibernate.sql.exec.internal.JdbcSelectExecutorStandardImpl.doExecuteQuery(JdbcSelectExecutorStandardImpl.java:137) ~[hibernate-core-6.6.33.Final.jar!/:6.6.33.Final]
at org.hibernate.sql.exec.internal.JdbcSelectExecutorStandardImpl.resolveJdbcValuesSource(JdbcSelectExecutorStandardImpl.java:355) ~[hibernate-core-6.6.33.Final.jar!/:6.6.33.Final]
at org.hibernate.sql.results.jdbc.internal.JdbcValuesResultSetImpl.<init>(JdbcValuesResultSetImpl.java:74) ~[hibernate-core-6.6.33.Final.jar!/:6.6.33.Final]
at org.hibernate.sql.results.jdbc.internal.DeferredResultSetAccess.getResultSet(DeferredResultSetAccess.java:172) ~[hibernate-core-6.6.33.Final.jar!/:6.6.33.Final]
at org.hibernate.sql.results.jdbc.internal.DeferredResultSetAccess.executeQuery(DeferredResultSetAccess.java:251) ~[hibernate-core-6.6.33.Final.jar!/:6.6.33.Final]
at com.zaxxer.hikari.pool.HikariProxyPreparedStatement.executeQuery(HikariProxyPreparedStatement.java) ~[HikariCP-6.3.3.jar!/:na]
at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeQuery(ProxyPreparedStatement.java:52) ~[HikariCP-6.3.3.jar!/:na]
at org.postgresql.jdbc.PgPreparedStatement.executeQuery(PgPreparedStatement.java:139) ~[postgresql-42.7.8.jar!/:42.7.8]
at org.postgresql.jdbc.PgPreparedStatement.executeWithFlags(PgPreparedStatement.java:196) ~[postgresql-42.7.8.jar!/:42.7.8]
at org.postgresql.jdbc.PgStatement.execute(PgStatement.java:435) ~[postgresql-42.7.8.jar!/:42.7.8]
at org.postgresql.jdbc.PgStatement.executeInternal(PgStatement.java:525) ~[postgresql-42.7.8.jar!/:42.7.8]
at org.postgresql.core.v3.QueryExecutorImpl.execute(QueryExecutorImpl.java:372) ~[postgresql-42.7.8.jar!/:42.7.8]
at org.postgresql.core.v3.QueryExecutorImpl.processResults(QueryExecutorImpl.java:2421) ~[postgresql-42.7.8.jar!/:42.7.8]
at org.postgresql.core.v3.QueryExecutorImpl.receiveErrorResponse(QueryExecutorImpl.java:2736) ~[postgresql-42.7.8.jar!/:42.7.8]
Position: 26
org.postgresql.util.PSQLException: ERROR: column "a1_0.id" must appear in the GROUP BY clause or be used in an aggregate function
Position: 26] [n/a]] with root cause
2025-11-24T02:27:08.237Z ERROR 1 --- [RecipeMate] [nio-8080-exec-1] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed: org.hibernate.exception.SQLGrammarException: JDBC exception executing SQL [select r1_0.id,r1_0.area,a1_0.id,a1_0.comment_notification,a1_0.created_at,a1_0.deleted_at,a1_0.email,a1_0.group_purchase_notification,a1_0.manner_temperature,a1_0.nickname,a1_0.password,a1_0.phone_number,a1_0.points,a1_0.profile_image_url,a1_0.role,a1_0.updated_at,r1_0.calories,r1_0.carbohydrate,r1_0.category,r1_0.created_at,r1_0.deleted_at,r1_0.fat,r1_0.full_image_url,r1_0.instructions,r1_0.last_synced_at,r1_0.protein,r1_0.serving_size,r1_0.sodium,r1_0.source_api,r1_0.source_api_id,r1_0.source_url,r1_0.thumbnail_image_url,r1_0.tips,r1_0.title,r1_0.updated_at,r1_0.youtube_url from recipes r1_0 left join users a1_0 on a1_0.id=r1_0.user_id left join group_buys gb1_0 on gb1_0.recipe_api_id=case when r1_0.source_api='MEAL_DB' then ('meal-'||r1_0.source_api_id) when r1_0.source_api='FOOD_SAFETY' then ('food-'||r1_0.source_api_id) else cast(r1_0.id as varchar) end and gb1_0.deleted_at is null where (r1_0.deleted_at IS NULL) group by r1_0.id order by count(gb1_0.id) desc,r1_0.last_synced_at desc fetch first ? rows only] [ERROR: column "a1_0.id" must appear in the GROUP BY clause or be used in an aggregate function
Position: 26
2025-11-24T02:27:08.142Z ERROR 1 --- [RecipeMate] [nio-8080-exec-1] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: column "a1_0.id" must appear in the GROUP BY clause or be used in an aggregate function
2025-11-24T02:27:08.142Z  WARN 1 --- [RecipeMate] [nio-8080-exec-1] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 42803
2025-11-24T02:27:04.938Z  INFO 1 --- [RecipeMate] [nio-8080-exec-1] c.r.domain.recipe.service.RecipeService  : DB 기반 인기 레시피 조회 요청: size=5
2025-11-24T02:26:57.233Z  INFO 1 --- [RecipeMate] [           main] o.s.b.w.e.tomcat.GracefulShutdown        : Commencing graceful shutdown. Waiting for active requests to complete
... 70 common frames omitted
at org.hibernate.sql.results.jdbc.internal.DeferredResultSetAccess.executeQuery(DeferredResultSetAccess.java:251) ~[hibernate-core-6.6.33.Final.jar!/:6.6.33.Final]
at com.zaxxer.hikari.pool.HikariProxyPreparedStatement.executeQuery(HikariProxyPreparedStatement.java) ~[HikariCP-6.3.3.jar!/:na]
at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeQuery(ProxyPreparedStatement.java:52) ~[HikariCP-6.3.3.jar!/:na]
at org.postgresql.jdbc.PgPreparedStatement.executeQuery(PgPreparedStatement.java:139) ~[postgresql-42.7.8.jar!/:42.7.8]
at org.postgresql.jdbc.PgPreparedStatement.executeWithFlags(PgPreparedStatement.java:196) ~[postgresql-42.7.8.jar!/:42.7.8]
at org.postgresql.jdbc.PgStatement.execute(PgStatement.java:435) ~[postgresql-42.7.8.jar!/:42.7.8]
at org.postgresql.jdbc.PgStatement.executeInternal(PgStatement.java:525) ~[postgresql-42.7.8.jar!/:42.7.8]
at org.postgresql.core.v3.QueryExecutorImpl.execute(QueryExecutorImpl.java:372) ~[postgresql-42.7.8.jar!/:42.7.8]
at org.postgresql.core.v3.QueryExecutorImpl.processResults(QueryExecutorImpl.java:2421) ~[postgresql-42.7.8.jar!/:42.7.8]
at org.postgresql.core.v3.QueryExecutorImpl.receiveErrorResponse(QueryExecutorImpl.java:2736) ~[postgresql-42.7.8.jar!/:42.7.8]
Position: 780
Hint: No operator matches the given name and argument types. You might need to add explicit type casts.
Caused by: org.postgresql.util.PSQLException: ERROR: operator does not exist: group_buy_status = character varying
... 42 common frames omitted
at org.springframework.dao.support.PersistenceExceptionTranslationInterceptor.invoke(PersistenceExceptionTranslationInterceptor.java:138) ~[spring-tx-6.2.12.jar!/:6.2.12]
at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184) ~[spring-aop-6.2.12.jar!/:6.2.12]
at org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:119) ~[spring-tx-6.2.12.jar!/:6.2.12]
at org.springframework.transaction.interceptor.TransactionAspectSupport.invokeWithinTransaction(TransactionAspectSupport.java:380) ~[spring-tx-6.2.12.jar!/:6.2.12]
at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184) ~[spring-aop-6.2.12.jar!/:6.2.12]
at org.springframework.data.projection.DefaultMethodInvokingMethodInterceptor.invoke(DefaultMethodInvokingMethodInterceptor.java:69) ~[spring-data-commons-3.5.5.jar!/:3.5.5]
at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184) ~[spring-aop-6.2.12.jar!/:6.2.12]
at org.springframework.data.repository.core.support.QueryExecutorMethodInterceptor.invoke(QueryExecutorMethodInterceptor.java:149) ~[spring-data-commons-3.5.5.jar!/:3.5.5]
at org.springframework.data.repository.core.support.QueryExecutorMethodInterceptor.doInvoke(QueryExecutorMethodInterceptor.java:170) ~[spring-data-commons-3.5.5.jar!/:3.5.5]
at org.springframework.data.repository.core.support.RepositoryMethodInvoker.invoke(RepositoryMethodInvoker.java:158) ~[spring-data-commons-3.5.5.jar!/:3.5.5]
at org.springframework.data.repository.core.support.RepositoryMethodInvoker.doInvoke(RepositoryMethodInvoker.java:170) ~[spring-data-commons-3.5.5.jar!/:3.5.5]
at org.springframework.data.jpa.repository.query.AbstractJpaQuery.execute(AbstractJpaQuery.java:148) ~[spring-data-jpa-3.5.5.jar!/:3.5.5]
at org.springframework.data.jpa.repository.query.AbstractJpaQuery.doExecute(AbstractJpaQuery.java:160) ~[spring-data-jpa-3.5.5.jar!/:3.5.5]
at org.springframework.data.jpa.repository.query.JpaQueryExecution.execute(JpaQueryExecution.java:95) ~[spring-data-jpa-3.5.5.jar!/:3.5.5]
at org.springframework.data.jpa.repository.query.JpaQueryExecution$CollectionExecution.doExecute(JpaQueryExecution.java:132) ~[spring-data-jpa-3.5.5.jar!/:3.5.5]
at org.hibernate.query.Query.getResultList(Query.java:120) ~[hibernate-core-6.6.33.Final.jar!/:6.6.33.Final]
at org.hibernate.query.spi.AbstractSelectionQuery.list(AbstractSelectionQuery.java:143) ~[hibernate-core-6.6.33.Final.jar!/:6.6.33.Final]
at org.hibernate.query.sqm.internal.QuerySqmImpl.doList(QuerySqmImpl.java:380) ~[hibernate-core-6.6.33.Final.jar!/:6.6.33.Final]
at org.hibernate.query.sqm.internal.ConcreteSqmSelectQueryPlan.performList(ConcreteSqmSelectQueryPlan.java:359) ~[hibernate-core-6.6.33.Final.jar!/:6.6.33.Final]
at org.hibernate.query.sqm.internal.ConcreteSqmSelectQueryPlan.withCacheableSqmInterpretation(ConcreteSqmSelectQueryPlan.java:439) ~[hibernate-core-6.6.33.Final.jar!/:6.6.33.Final]
at org.hibernate.query.sqm.internal.ConcreteSqmSelectQueryPlan.lambda$new$1(ConcreteSqmSelectQueryPlan.java:149) ~[hibernate-core-6.6.33.Final.jar!/:6.6.33.Final]
at org.hibernate.sql.exec.spi.JdbcSelectExecutor.list(JdbcSelectExecutor.java:165) ~[hibernate-core-6.6.33.Final.jar!/:6.6.33.Final]
at org.hibernate.sql.exec.spi.JdbcSelectExecutor.executeQuery(JdbcSelectExecutor.java:91) ~[hibernate-core-6.6.33.Final.jar!/:6.6.33.Final]
at org.hibernate.sql.exec.internal.JdbcSelectExecutorStandardImpl.executeQuery(JdbcSelectExecutorStandardImpl.java:102) ~[hibernate-core-6.6.33.Final.jar!/:6.6.33.Final]
at org.hibernate.sql.exec.internal.JdbcSelectExecutorStandardImpl.doExecuteQuery(JdbcSelectExecutorStandardImpl.java:137) ~[hibernate-core-6.6.33.Final.jar!/:6.6.33.Final]
at org.hibernate.sql.exec.internal.JdbcSelectExecutorStandardImpl.resolveJdbcValuesSource(JdbcSelectExecutorStandardImpl.java:355) ~[hibernate-core-6.6.33.Final.jar!/:6.6.33.Final]
at org.hibernate.sql.results.jdbc.internal.JdbcValuesResultSetImpl.<init>(JdbcValuesResultSetImpl.java:74) ~[hibernate-core-6.6.33.Final.jar!/:6.6.33.Final]
at org.hibernate.sql.results.jdbc.internal.DeferredResultSetAccess.getResultSet(DeferredResultSetAccess.java:172) ~[hibernate-core-6.6.33.Final.jar!/:6.6.33.Final]
at org.hibernate.sql.results.jdbc.internal.DeferredResultSetAccess.executeQuery(DeferredResultSetAccess.java:269) ~[hibernate-core-6.6.33.Final.jar!/:6.6.33.Final]
at org.hibernate.engine.jdbc.spi.SqlExceptionHelper.convert(SqlExceptionHelper.java:94) ~[hibernate-core-6.6.33.Final.jar!/:6.6.33.Final]
at org.hibernate.engine.jdbc.spi.SqlExceptionHelper.convert(SqlExceptionHelper.java:108) ~[hibernate-core-6.6.33.Final.jar!/:6.6.33.Final]
at org.hibernate.exception.internal.StandardSQLExceptionConverter.convert(StandardSQLExceptionConverter.java:58) ~[hibernate-core-6.6.33.Final.jar!/:6.6.33.Final]
at org.hibernate.exception.internal.SQLStateConversionDelegate.convert(SQLStateConversionDelegate.java:91) ~[hibernate-core-6.6.33.Final.jar!/:6.6.33.Final]
Position: 780] [n/a]
Hint: No operator matches the given name and argument types. You might need to add explicit type casts.
Caused by: org.hibernate.exception.SQLGrammarException: JDBC exception executing SQL [select gb1_0.id,gb1_0.category,gb1_0.content,gb1_0.created_at,gb1_0.current_amount,gb1_0.current_headcount,gb1_0.deadline,gb1_0.deleted_at,gb1_0.delivery_method,gb1_0.host_id,h1_0.id,h1_0.comment_notification,h1_0.created_at,h1_0.deleted_at,h1_0.email,h1_0.group_purchase_notification,h1_0.manner_temperature,h1_0.nickname,h1_0.password,h1_0.phone_number,h1_0.points,h1_0.profile_image_url,h1_0.role,h1_0.updated_at,gb1_0.ingredients,gb1_0.is_participant_list_public,gb1_0.latitude,gb1_0.longitude,gb1_0.meetup_location,gb1_0.parcel_fee,gb1_0.recipe_api_id,gb1_0.recipe_image_url,gb1_0.recipe_name,gb1_0.status,gb1_0.target_amount,gb1_0.target_headcount,gb1_0.title,gb1_0.updated_at,gb1_0.version from group_buys gb1_0 join users h1_0 on h1_0.id=gb1_0.host_id where gb1_0.status in (?,?) and gb1_0.deadline<?] [ERROR: operator does not exist: group_buy_status = character varying
at org.springframework.boot.loader.launch.JarLauncher.main(JarLauncher.java:40) ~[app.jar:0.0.1-SNAPSHOT]
at org.springframework.boot.loader.launch.Launcher.launch(Launcher.java:64) ~[app.jar:0.0.1-SNAPSHOT]
at org.springframework.boot.loader.launch.Launcher.launch(Launcher.java:106) ~[app.jar:0.0.1-SNAPSHOT]
at java.base/java.lang.reflect.Method.invoke(Unknown Source) ~[na:na]
at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(Unknown Source) ~[na:na]
at com.recipemate.RecipeMateApplication.main(RecipeMateApplication.java:18) ~[!/:0.0.1-SNAPSHOT]
at org.springframework.boot.SpringApplication.run(SpringApplication.java:1350) ~[spring-boot-3.5.7.jar!/:3.5.7]
at org.springframework.boot.SpringApplication.run(SpringApplication.java:1361) ~[spring-boot-3.5.7.jar!/:3.5.7]
at org.springframework.boot.SpringApplication.run(SpringApplication.java:332) ~[spring-boot-3.5.7.jar!/:3.5.7]
at org.springframework.boot.SpringApplicationRunListeners.ready(SpringApplicationRunListeners.java:80) ~[spring-boot-3.5.7.jar!/:3.5.7]
at org.springframework.boot.SpringApplicationRunListeners.doWithListeners(SpringApplicationRunListeners.java:112) ~[spring-boot-3.5.7.jar!/:3.5.7]
at org.springframework.boot.SpringApplicationRunListeners.doWithListeners(SpringApplicationRunListeners.java:118) ~[spring-boot-3.5.7.jar!/:3.5.7]
at java.base/java.lang.Iterable.forEach(Unknown Source) ~[na:na]
at org.springframework.boot.SpringApplicationRunListeners.lambda$ready$6(SpringApplicationRunListeners.java:80) ~[spring-boot-3.5.7.jar!/:3.5.7]
at org.springframework.boot.context.event.EventPublishingRunListener.ready(EventPublishingRunListener.java:109) ~[spring-boot-3.5.7.jar!/:3.5.7]
at org.springframework.context.support.AbstractApplicationContext.publishEvent(AbstractApplicationContext.java:387) ~[spring-context-6.2.12.jar!/:6.2.12]
at org.springframework.context.support.AbstractApplicationContext.publishEvent(AbstractApplicationContext.java:454) ~[spring-context-6.2.12.jar!/:6.2.12]
at org.springframework.context.event.SimpleApplicationEventMulticaster.multicastEvent(SimpleApplicationEventMulticaster.java:156) ~[spring-context-6.2.12.jar!/:6.2.12]
at org.springframework.context.event.SimpleApplicationEventMulticaster.invokeListener(SimpleApplicationEventMulticaster.java:178) ~[spring-context-6.2.12.jar!/:6.2.12]
at org.springframework.context.event.SimpleApplicationEventMulticaster.doInvokeListener(SimpleApplicationEventMulticaster.java:185) ~[spring-context-6.2.12.jar!/:6.2.12]
at org.springframework.context.event.ApplicationListenerMethodAdapter.onApplicationEvent(ApplicationListenerMethodAdapter.java:174) ~[spring-context-6.2.12.jar!/:6.2.12]
at org.springframework.context.event.ApplicationListenerMethodAdapter.processEvent(ApplicationListenerMethodAdapter.java:255) ~[spring-context-6.2.12.jar!/:6.2.12]
at org.springframework.context.event.ApplicationListenerMethodAdapter.doInvoke(ApplicationListenerMethodAdapter.java:383) ~[spring-context-6.2.12.jar!/:6.2.12]
at java.base/java.lang.reflect.Method.invoke(Unknown Source) ~[na:na]
at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(Unknown Source) ~[na:na]
at com.recipemate.domain.groupbuy.service.GroupBuyScheduler$$SpringCGLIB$$0.onApplicationReady(<generated>) ~[!/:0.0.1-SNAPSHOT]
at org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:728) ~[spring-aop-6.2.12.jar!/:6.2.12]
at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184) ~[spring-aop-6.2.12.jar!/:6.2.12]
at org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:119) ~[spring-tx-6.2.12.jar!/:6.2.12]
at org.springframework.transaction.interceptor.TransactionAspectSupport.invokeWithinTransaction(TransactionAspectSupport.java:380) ~[spring-tx-6.2.12.jar!/:6.2.12]
at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163) ~[spring-aop-6.2.12.jar!/:6.2.12]
at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:196) ~[spring-aop-6.2.12.jar!/:6.2.12]
at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:360) ~[spring-aop-6.2.12.jar!/:6.2.12]
at java.base/java.lang.reflect.Method.invoke(Unknown Source) ~[na:na]
at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(Unknown Source) ~[na:na]
at com.recipemate.domain.groupbuy.service.GroupBuyScheduler.onApplicationReady(GroupBuyScheduler.java:48) ~[!/:0.0.1-SNAPSHOT]
at com.recipemate.domain.groupbuy.service.GroupBuyScheduler.updateExpiredGroupBuys(GroupBuyScheduler.java:88) ~[!/:0.0.1-SNAPSHOT]
at jdk.proxy2/jdk.proxy2.$Proxy168.findByStatusInAndDeadlineBefore(Unknown Source) ~[na:na]
at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:223) ~[spring-aop-6.2.12.jar!/:6.2.12]
at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184) ~[spring-aop-6.2.12.jar!/:6.2.12]
at org.springframework.data.jpa.repository.support.CrudMethodMetadataPostProcessor$CrudMethodMetadataPopulatingMethodInterceptor.invoke(CrudMethodMetadataPostProcessor.java:136) ~[spring-data-jpa-3.5.5.jar!/:3.5.5]
at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184) ~[spring-aop-6.2.12.jar!/:6.2.12]
at org.springframework.dao.support.PersistenceExceptionTranslationInterceptor.invoke(PersistenceExceptionTranslationInterceptor.java:160) ~[spring-tx-6.2.12.jar!/:6.2.12]
at org.springframework.dao.support.DataAccessUtils.translateIfNecessary(DataAccessUtils.java:343) ~[spring-tx-6.2.12.jar!/:6.2.12]
at org.springframework.dao.support.ChainedPersistenceExceptionTranslator.translateExceptionIfPossible(ChainedPersistenceExceptionTranslator.java:61) ~[spring-tx-6.2.12.jar!/:6.2.12]
at org.springframework.orm.jpa.AbstractEntityManagerFactoryBean.translateExceptionIfPossible(AbstractEntityManagerFactoryBean.java:560) ~[spring-orm-6.2.12.jar!/:6.2.12]
at org.springframework.orm.jpa.vendor.HibernateJpaDialect.translateExceptionIfPossible(HibernateJpaDialect.java:241) ~[spring-orm-6.2.12.jar!/:6.2.12]
at org.springframework.orm.jpa.vendor.HibernateJpaDialect.convertHibernateAccessException(HibernateJpaDialect.java:256) ~[spring-orm-6.2.12.jar!/:6.2.12]
at org.springframework.orm.jpa.vendor.HibernateJpaDialect.convertHibernateAccessException(HibernateJpaDialect.java:281) ~[spring-orm-6.2.12.jar!/:6.2.12]
Position: 780] [n/a]; SQL [n/a]
Hint: No operator matches the given name and argument types. You might need to add explicit type casts.
org.springframework.dao.InvalidDataAccessResourceUsageException: JDBC exception executing SQL [select gb1_0.id,gb1_0.category,gb1_0.content,gb1_0.created_at,gb1_0.current_amount,gb1_0.current_headcount,gb1_0.deadline,gb1_0.deleted_at,gb1_0.delivery_method,gb1_0.host_id,h1_0.id,h1_0.comment_notification,h1_0.created_at,h1_0.deleted_at,h1_0.email,h1_0.group_purchase_notification,h1_0.manner_temperature,h1_0.nickname,h1_0.password,h1_0.phone_number,h1_0.points,h1_0.profile_image_url,h1_0.role,h1_0.updated_at,gb1_0.ingredients,gb1_0.is_participant_list_public,gb1_0.latitude,gb1_0.longitude,gb1_0.meetup_location,gb1_0.parcel_fee,gb1_0.recipe_api_id,gb1_0.recipe_image_url,gb1_0.recipe_name,gb1_0.status,gb1_0.target_amount,gb1_0.target_headcount,gb1_0.title,gb1_0.updated_at,gb1_0.version from group_buys gb1_0 join users h1_0 on h1_0.id=gb1_0.host_id where gb1_0.status in (?,?) and gb1_0.deadline<?] [ERROR: operator does not exist: group_buy_status = character varying
2025-11-24T02:26:56.734Z ERROR 1 --- [RecipeMate] [           main] o.s.boot.SpringApplication               : Application run failed
Position: 780
Hint: No operator matches the given name and argument types. You might need to add explicit type casts.
2025-11-24T02:26:55.439Z ERROR 1 --- [RecipeMate] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: operator does not exist: group_buy_status = character varying
2025-11-24T02:26:55.439Z  WARN 1 --- [RecipeMate] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 42883
2025-11-24T02:26:54.936Z  INFO 1 --- [RecipeMate] [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 98 ms
2025-11-24T02:26:54.838Z  INFO 1 --- [RecipeMate] [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2025-11-24T02:26:54.838Z  INFO 1 --- [RecipeMate] [nio-8080-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2025-11-24T02:26:53.337Z  INFO 1 --- [RecipeMate] [           main] c.r.d.g.service.GroupBuyScheduler        : 서버 시작 - 공구 상태 일괄 업데이트 시작
2025-11-24T02:26:52.936Z  INFO 1 --- [RecipeMate] [           main] com.recipemate.RecipeMateApplication     : Started RecipeMateApplication in 205.996 seconds (process running for 216.4)
2025-11-24T02:26:52.635Z  INFO 1 --- [RecipeMate] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path '/'
2025-11-24T02:26:42.337Z  INFO 1 --- [RecipeMate] [           main] r$InitializeUserDetailsManagerConfigurer : Global AuthenticationManager configured with UserDetailsService bean with name customUserDetailsService
2025-11-24T02:26:38.043Z  INFO 1 --- [RecipeMate] [           main] o.s.b.a.w.s.WelcomePageHandlerMapping    : Adding welcome page template: index
2025-11-24T02:26:37.239Z  WARN 1 --- [RecipeMate] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
==> Docs on specifying a port: https://render.com/docs/web-services#port-binding
==> No open ports detected, continuing to scan...
2025-11-24T02:25:20.537Z  INFO 1 --- [RecipeMate] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
==> Docs on specifying a port: https://render.com/docs/web-services#port-binding
==> No open ports detected, continuing to scan...
2025-11-24T02:25:12.938Z  INFO 1 --- [RecipeMate] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2025-11-24T02:25:11.738Z  INFO 1 --- [RecipeMate] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2025-11-24T02:24:50.735Z  WARN 1 --- [RecipeMate] [           main] o.h.boot.model.internal.ToOneBinder      : HHH000491: 'com.recipemate.domain.recipewishlist.entity.RecipeWishlist.recipe' uses both @NotFound and FetchType.LAZY. @ManyToOne and @OneToOne associations mapped with @NotFound are forced to EAGER fetching.
Maximum pool size: undefined/unknown
Minimum pool size: undefined/unknown
Isolation level: undefined/unknown
Autocommit mode: undefined/unknown
Database version: 18.1
Database driver: undefined/unknown
Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
2025-11-24T02:24:45.237Z  INFO 1 --- [RecipeMate] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
2025-11-24T02:24:44.436Z  WARN 1 --- [RecipeMate] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2025-11-24T02:24:43.634Z  INFO 1 --- [RecipeMate] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2025-11-24T02:24:43.543Z  INFO 1 --- [RecipeMate] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@779448b8
2025-11-24T02:24:38.436Z  INFO 1 --- [RecipeMate] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2025-11-24T02:24:38.038Z  INFO 1 --- [RecipeMate] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2025-11-24T02:24:32.433Z  INFO 1 --- [RecipeMate] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2025-11-24T02:24:31.836Z  INFO 1 --- [RecipeMate] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.33.Final
2025-11-24T02:24:31.039Z  INFO 1 --- [RecipeMate] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2025-11-24T02:24:23.335Z  INFO 1 --- [RecipeMate] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 43999 ms
2025-11-24T02:24:23.241Z  INFO 1 --- [RecipeMate] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2025-11-24T02:24:21.544Z  INFO 1 --- [RecipeMate] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.48]
2025-11-24T02:24:21.543Z  INFO 1 --- [RecipeMate] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2025-11-24T02:24:21.437Z  INFO 1 --- [RecipeMate] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8080 (http)
==> Docs on specifying a port: https://render.com/docs/web-services#port-binding
==> No open ports detected, continuing to scan...
2025-11-24T02:24:04.038Z  INFO 1 --- [RecipeMate] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 603 ms. Found 0 Redis repository interfaces.
2025-11-24T02:24:04.037Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.wishlist.repository.WishlistRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:24:04.037Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.user.repository.UserRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:24:04.037Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.user.repository.PointHistoryRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:24:04.037Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.user.repository.PersistentTokenJpaRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:24:04.037Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.user.repository.MannerTempHistoryRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:24:04.036Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.user.repository.AddressRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:24:04.036Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.search.repository.SearchKeywordRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:24:04.036Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.review.repository.ReviewRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:24:04.036Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.report.repository.ReportRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:24:04.036Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.recipewishlist.repository.RecipeWishlistRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:24:04.035Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.recipe.repository.RecipeStepRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:24:04.035Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.recipe.repository.RecipeRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:24:04.034Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.recipe.repository.RecipeIngredientRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:24:03.935Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.recipe.repository.RecipeCorrectionRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:24:03.934Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.post.repository.PostRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:24:03.934Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.post.repository.PostImageRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:24:03.839Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.notification.repository.NotificationRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:24:03.839Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.like.repository.PostLikeRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:24:03.838Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.like.repository.CommentLikeRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:24:03.838Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.groupbuy.repository.ParticipationRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:24:03.838Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.groupbuy.repository.GroupBuyRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:24:03.837Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.groupbuy.repository.GroupBuyImageRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:24:03.837Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.directmessage.repository.DirectMessageRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:24:03.837Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.comment.repository.CommentRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:24:03.834Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.badge.repository.BadgeRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:24:03.235Z  INFO 1 --- [RecipeMate] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data Redis repositories in DEFAULT mode.
2025-11-24T02:24:03.139Z  INFO 1 --- [RecipeMate] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2025-11-24T02:24:02.634Z  INFO 1 --- [RecipeMate] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 4105 ms. Found 25 JPA repository interfaces.
2025-11-24T02:23:58.237Z  INFO 1 --- [RecipeMate] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2025-11-24T02:23:58.235Z  INFO 1 --- [RecipeMate] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2025-11-24T02:23:38.137Z  INFO 1 --- [RecipeMate] [           main] com.recipemate.RecipeMateApplication     : The following 1 profile is active: "prod"
2025-11-24T02:23:38.039Z  INFO 1 --- [RecipeMate] [           main] com.recipemate.RecipeMateApplication     : Starting RecipeMateApplication v0.0.1-SNAPSHOT using Java 21.0.9 with PID 1 (/app/app.jar started by spring in /app)
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
#21 DONE 24.6s
#21 writing cache image manifest sha256:895d070a9e4db395491ce14302601529d44282a884433eb5c032b3731e1308ca done
#21 preparing build cache for export
#21 exporting cache to client directory
#20 DONE 2.9s
#20 exporting config sha256:dd600584abf64736bb2f189e33ee02c24429509900390fdbcce305c55e957aaf done
#20 exporting manifest sha256:bf98e4258b267332a020febe528ff26166588a89a23658841ec35e249f23a61f done
#20 exporting layers 2.0s done
#20 exporting layers
#20 exporting to docker image format
#19 DONE 9.0s
#19 [stage-1 5/5] COPY --from=builder /app/build/libs/*.jar app.jar
#18 DONE 72.7s
#18 72.15 4 actionable tasks: 4 executed
#18 72.15 BUILD SUCCESSFUL in 1m 11s
#18 72.15
#18 72.15 > Task :bootJar
#18 63.97 > Task :resolveMainClassName
#18 63.67 > Task :classes
#18 63.67 > Task :processResources
#18 63.67
#18 47.57 Note: Recompile with -Xlint:unchecked for details.
#18 47.57 Note: /app/src/main/java/com/recipemate/global/util/ImageUploadUtil.java uses unchecked or unsafe operations.
#18 47.57 Note: Recompile with -Xlint:deprecation for details.
#18 47.57 Note: Some input files use or override a deprecated API.
#18 43.48 > Task :compileJava
#18 2.346 Daemon will be stopped at the end of the build
#18 0.847 To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/8.5/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
#18 [builder 7/7] RUN gradle bootJar --no-daemon -x test
#17 DONE 2.2s
#17 [builder 6/7] COPY src src/