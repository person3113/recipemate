==> Common ways to troubleshoot your deploy: https://render.com/docs/troubleshooting-deploys
==> Exited with status 1
at org.springframework.boot.loader.launch.JarLauncher.main(JarLauncher.java:40)
at org.springframework.boot.loader.launch.Launcher.launch(Launcher.java:64)
at org.springframework.boot.loader.launch.Launcher.launch(Launcher.java:106)
at java.base/java.lang.reflect.Method.invoke(Unknown Source)
at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(Unknown Source)
at com.recipemate.RecipeMateApplication.main(RecipeMateApplication.java:18)
at org.springframework.boot.SpringApplication.run(SpringApplication.java:1350)
at org.springframework.boot.SpringApplication.run(SpringApplication.java:1361)
at org.springframework.boot.SpringApplication.run(SpringApplication.java:313)
at org.springframework.boot.SpringApplication.prepareEnvironment(SpringApplication.java:353)
at org.springframework.boot.SpringApplicationRunListeners.environmentPrepared(SpringApplicationRunListeners.java:63)
at org.springframework.boot.SpringApplicationRunListeners.doWithListeners(SpringApplicationRunListeners.java:112)
at org.springframework.boot.SpringApplicationRunListeners.doWithListeners(SpringApplicationRunListeners.java:118)
at java.base/java.lang.Iterable.forEach(Unknown Source)
at org.springframework.boot.SpringApplicationRunListeners.lambda$environmentPrepared$2(SpringApplicationRunListeners.java:64)
at org.springframework.boot.context.event.EventPublishingRunListener.environmentPrepared(EventPublishingRunListener.java:81)
at org.springframework.boot.context.event.EventPublishingRunListener.multicastInitialEvent(EventPublishingRunListener.java:136)
at org.springframework.context.event.SimpleApplicationEventMulticaster.multicastEvent(SimpleApplicationEventMulticaster.java:138)
at org.springframework.context.event.SimpleApplicationEventMulticaster.multicastEvent(SimpleApplicationEventMulticaster.java:156)
at org.springframework.context.event.SimpleApplicationEventMulticaster.invokeListener(SimpleApplicationEventMulticaster.java:178)
at org.springframework.context.event.SimpleApplicationEventMulticaster.doInvokeListener(SimpleApplicationEventMulticaster.java:185)
at org.springframework.boot.env.EnvironmentPostProcessorApplicationListener.onApplicationEvent(EnvironmentPostProcessorApplicationListener.java:115)
at org.springframework.boot.env.EnvironmentPostProcessorApplicationListener.onApplicationEnvironmentPreparedEvent(EnvironmentPostProcessorApplicationListener.java:132)
at org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor.postProcessEnvironment(ConfigDataEnvironmentPostProcessor.java:89)
at org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor.postProcessEnvironment(ConfigDataEnvironmentPostProcessor.java:96)
at org.springframework.boot.context.config.ConfigDataEnvironment.processAndApply(ConfigDataEnvironment.java:236)
at org.springframework.boot.context.config.ConfigDataEnvironment.withProfiles(ConfigDataEnvironment.java:279)
at org.springframework.boot.context.config.ConfigDataEnvironment.getIncludedProfiles(ConfigDataEnvironment.java:301)
at org.springframework.boot.context.properties.bind.BindResult.ifBound(BindResult.java:76)
at org.springframework.boot.context.config.ConfigDataEnvironment.lambda$getIncludedProfiles$1(ConfigDataEnvironment.java:303)
at org.springframework.boot.context.config.InactiveConfigDataAccessException.throwIfPropertyFound(InactiveConfigDataAccessException.java:126)
org.springframework.boot.context.config.InactiveConfigDataAccessException: Inactive property source 'Config resource 'class path resource [application.yml]' via location 'optional:classpath:/' (document #1)' imported from location 'class path resource [application.yml]' cannot contain property 'spring.profiles.include' [origin: class path resource [application.yml] from app.jar - 54:14]
01:33:54.611 [main] ERROR org.springframework.boot.SpringApplication -- Application run failed
==> Deploying...
Upload succeeded
Pushing image to registry...
#21 DONE 13.2s
#21 writing cache image manifest sha256:93bcf4ed3f0fd3ebf22ea3eb030ec1f700b9115c40982b37dbcd39b55d9ee410 done
#21 preparing build cache for export
#21 exporting cache to client directory
#20 DONE 3.0s
#20 exporting config sha256:8f812fd0749ace030ad1af0f37ff8e31e22366cf10b5343843411ae81b7d9970 done
#20 exporting manifest sha256:f0fe34f57c6bd70d654c2fd9e9b5455602053281c05e5b7cde134fc8f9c9ca9f done
#20 exporting layers 2.1s done
#20 exporting layers
#20 exporting to docker image format
#19 DONE 3.7s
#19 [stage-1 5/5] COPY --from=builder /app/build/libs/*.jar app.jar
#18 DONE 80.9s
#18 80.25 4 actionable tasks: 4 executed
#18 80.25 BUILD SUCCESSFUL in 1m 19s
#18 80.25
#18 80.25 > Task :bootJar
#18 72.35 > Task :resolveMainClassName
#18 72.09 > Task :classes
#18 72.09 > Task :processResources
#18 72.09
#18 55.59 Note: Recompile with -Xlint:unchecked for details.
#18 55.59 Note: /app/src/main/java/com/recipemate/global/util/ImageUploadUtil.java uses unchecked or unsafe operations.
#18 55.59 Note: Recompile with -Xlint:deprecation for details.
#18 55.59 Note: Some input files use or override a deprecated API.
#18 45.99 > Task :compileJava
#18 3.648 Daemon will be stopped at the end of the build
#18 1.849 To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/8.5/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
#18 [builder 7/7] RUN gradle bootJar --no-daemon -x test
#17 DONE 18.4s
#17 [builder 6/7] COPY src src/
#16 DONE 46.3s
#16 45.74 1 actionable task: 1 executed
#16 45.74 BUILD SUCCESSFUL in 45s
#16 45.74
#16 45.67 A web-based, searchable dependency report is available by adding the --scan option.
#16 45.67
#16 45.67 (n) - A dependency or dependency configuration that cannot be resolved.
#16 45.67
#16 45.67 (*) - Indicates repeated occurrences of a transitive dependency subtree. Gradle expands transitive dependency subtrees only once per project; repeat occurrences only display the root of the subtree, followed by this annotation.
#16 45.67 (c) - A dependency constraint, not a dependency. The dependency affected by the constraint occurs elsewhere in the tree.
#16 45.67
#16 45.67 \--- org.junit.platform:junit-platform-launcher (n)
#16 45.67 testRuntimeOnly - Runtime only dependencies for source set 'test'. (n)
#16 45.67
#16 45.67 \--- org.junit.platform:junit-platform-launcher -> 1.12.2 (*)
#16 45.67 |    \--- org.springframework:spring-test:6.2.12 (*)
#16 45.67 |    +--- org.springframework:spring-core:6.2.12 (*)
#16 45.67 |    +--- org.springframework.security:spring-security-web:6.5.6 (*)
#16 45.67 |    +--- org.springframework.security:spring-security-core:6.5.6 (*)
#16 45.67 +--- org.springframework.security:spring-security-test -> 6.5.6
#16 45.67 |         \--- org.junit.platform:junit-platform-engine:1.12.2 (*)
#16 45.67 |         +--- org.junit:junit-bom:5.12.2 (*)
#16 45.67 |    \--- org.junit.platform:junit-platform-launcher -> 1.12.2
#16 45.67 |    +--- org.xmlunit:xmlunit-core:2.10.4
#16 45.67 |    +--- org.springframework:spring-test:6.2.12 (*)
#16 45.67 |    +--- org.springframework:spring-core:6.2.12 (*)
#16 45.67 |    |    \--- com.vaadin.external.google:android-json:0.0.20131108.vaadin1
#16 45.67 |    +--- org.skyscreamer:jsonassert:1.5.3
#16 45.67 |    |    \--- org.junit.jupiter:junit-jupiter-api:5.11.4 -> 5.12.2 (*)
#16 45.67 |    |    +--- org.mockito:mockito-core:5.17.0 (*)
#16 45.67 |    +--- org.mockito:mockito-junit-jupiter:5.17.0
#16 45.67 |    |    \--- org.objenesis:objenesis:3.3
#16 45.67 |    |    +--- net.bytebuddy:byte-buddy-agent:1.15.11 -> 1.17.8
#16 45.67 |    |    +--- net.bytebuddy:byte-buddy:1.15.11 -> 1.17.8
#16 45.67 |    +--- org.mockito:mockito-core:5.17.0
#16 45.67 |    |         \--- org.junit.jupiter:junit-jupiter-api:5.12.2 (*)
#16 45.67 |    |         |    \--- org.junit.platform:junit-platform-commons:1.12.2 (*)
#16 45.67 |    |         |    +--- org.opentest4j:opentest4j:1.3.0
#16 45.67 |    |         |    +--- org.junit:junit-bom:5.12.2 (*)
#16 45.67 |    |         +--- org.junit.platform:junit-platform-engine:1.12.2
#16 45.67 |    |         +--- org.junit:junit-bom:5.12.2 (*)
#16 45.67 |    |    \--- org.junit.jupiter:junit-jupiter-engine:5.12.2
#16 45.67 |    |    |    \--- org.junit.jupiter:junit-jupiter-api:5.12.2 (*)
#16 45.67 |    |    |    +--- org.junit:junit-bom:5.12.2 (*)
#16 45.67 |    |    +--- org.junit.jupiter:junit-jupiter-params:5.12.2
#16 45.67 |    |    |         \--- org.junit:junit-bom:5.12.2 (*)
#16 45.67 |    |    |    \--- org.junit.platform:junit-platform-commons:1.12.2
#16 45.67 |    |    |    +--- org.opentest4j:opentest4j:1.3.0
#16 45.67 |    |    |    +--- org.junit:junit-bom:5.12.2 (*)
#16 45.67 |    |    +--- org.junit.jupiter:junit-jupiter-api:5.12.2
#16 45.67 |    |    |    \--- org.junit.platform:junit-platform-commons:1.12.2 (c)
#16 45.67 |    |    |    +--- org.junit.platform:junit-platform-launcher:1.12.2 (c)
#16 45.67 |    |    |    +--- org.junit.platform:junit-platform-engine:1.12.2 (c)
#16 45.67 |    |    |    +--- org.junit.jupiter:junit-jupiter-params:5.12.2 (c)
#16 45.67 |    |    |    +--- org.junit.jupiter:junit-jupiter-engine:5.12.2 (c)
#16 45.67 |    |    |    +--- org.junit.jupiter:junit-jupiter-api:5.12.2 (c)
#16 45.67 |    |    |    +--- org.junit.jupiter:junit-jupiter:5.12.2 (c)
#16 45.67 |    |    +--- org.junit:junit-bom:5.12.2
#16 45.67 |    +--- org.junit.jupiter:junit-jupiter:5.12.2
#16 45.67 |    +--- org.hamcrest:hamcrest:3.0
#16 45.67 |    |    \--- org.hamcrest:hamcrest:2.1 -> 3.0
#16 45.67 |    +--- org.awaitility:awaitility:4.2.2
#16 45.67 |    |    \--- net.bytebuddy:byte-buddy:1.17.7 -> 1.17.8
#16 45.67 |    +--- org.assertj:assertj-core:3.27.6
#16 45.67 |    +--- net.minidev:json-smart:2.5.2 (*)
#16 45.67 |    +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.4 (*)
#16 45.67 |    |    \--- org.slf4j:slf4j-api:2.0.11 -> 2.0.17
#16 45.66 |    |    |         \--- org.ow2.asm:asm:9.7.1
#16 45.66 |    |    |    \--- net.minidev:accessors-smart:2.5.2
#16 45.66 |    |    +--- net.minidev:json-smart:2.5.0 -> 2.5.2
#16 45.66 |    +--- com.jayway.jsonpath:json-path:2.9.0
#16 45.66 |    |    \--- org.springframework.boot:spring-boot-autoconfigure:3.5.7 (*)
#16 45.66 |    |    +--- org.springframework.boot:spring-boot-test:3.5.7 (*)
#16 45.66 |    |    +--- org.springframework.boot:spring-boot:3.5.7 (*)
#16 45.66 |    +--- org.springframework.boot:spring-boot-test-autoconfigure:3.5.7
#16 45.66 |    |         \--- org.springframework:spring-core:6.2.12 (*)
#16 45.66 |    |    \--- org.springframework:spring-test:6.2.12
#16 45.66 |    |    +--- org.springframework.boot:spring-boot:3.5.7 (*)
#16 45.66 |    +--- org.springframework.boot:spring-boot-test:3.5.7
#16 45.66 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 45.66 +--- org.springframework.boot:spring-boot-starter-test -> 3.5.7
#16 45.66 |    \--- org.checkerframework:checker-qual:3.49.5
#16 45.66 +--- org.postgresql:postgresql -> 42.7.8
#16 45.66 +--- com.h2database:h2 -> 2.3.232
#16 45.66 |         \--- io.projectreactor:reactor-core:3.7.6 -> 3.7.12 (*)
#16 45.66 |    \--- io.github.openfeign.querydsl:querydsl-core:7.0
#16 45.66 +--- io.github.openfeign.querydsl:querydsl-jpa:7.0
#16 45.66 |    \--- org.apache.httpcomponents.core5:httpcore5:5.2.5 -> 5.3.6
#16 45.66 |    |    \--- org.slf4j:slf4j-api:1.7.36 -> 2.0.17
#16 45.66 |    |    |    \--- org.apache.httpcomponents.core5:httpcore5:5.3.6
#16 45.66 |    |    +--- org.apache.httpcomponents.core5:httpcore5-h2:5.3.6
#16 45.66 |    |    +--- org.apache.httpcomponents.core5:httpcore5:5.3.6
#16 45.66 |    +--- org.apache.httpcomponents.client5:httpclient5:5.3.1 -> 5.5.1
#16 45.66 |    +--- org.apache.commons:commons-lang3:3.1 -> 3.18.0
#16 45.66 |    +--- com.cloudinary:cloudinary-core:2.3.0
#16 45.66 +--- com.cloudinary:cloudinary-http5:2.3.0
#16 45.66 +--- net.coobird:thumbnailator:0.4.19
#16 45.66 |    \--- org.springframework:spring-context-support:6.2.12 (*)
#16 45.66 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 45.66 +--- org.springframework.boot:spring-boot-starter-cache -> 3.5.7
#16 45.66 |         \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
#16 45.66 |         |    \--- org.springframework:spring-core:6.2.12 (*)
#16 45.66 |         |    +--- org.springframework:spring-context:6.2.12 (*)
#16 45.66 |         |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 45.66 |         +--- org.springframework:spring-context-support:6.2.12
#16 45.66 |         +--- org.springframework:spring-aop:6.2.12 (*)
#16 45.66 |         |    \--- org.springframework:spring-core:6.2.12 (*)
#16 45.66 |         |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 45.66 |         |    +--- jakarta.xml.bind:jakarta.xml.bind-api:3.0.1 -> 4.0.4 (*)
#16 45.66 |         +--- org.springframework:spring-oxm:6.2.12
#16 45.66 |         +--- org.springframework:spring-tx:6.2.12 (*)
#16 45.66 |         |    \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
#16 45.66 |         |    +--- org.springframework:spring-tx:6.2.12 (*)
#16 45.66 |         |    +--- org.springframework:spring-context:6.2.12 (*)
#16 45.66 |         |    +--- org.springframework.data:spring-data-commons:3.5.5 (*)
#16 45.66 |         +--- org.springframework.data:spring-data-keyvalue:3.5.5
#16 45.66 |    \--- org.springframework.data:spring-data-redis:3.5.5
#16 45.66 |    |         \--- org.reactivestreams:reactive-streams:1.0.4
#16 45.66 |    |    \--- io.projectreactor:reactor-core:3.6.6 -> 3.7.12
#16 45.66 |    |    +--- io.netty:netty-transport:4.1.118.Final -> 4.1.128.Final (*)
#16 45.66 |    |    |         \--- io.netty:netty-transport:4.1.128.Final (*)
#16 45.66 |    |    |         +--- io.netty:netty-buffer:4.1.128.Final (*)
#16 45.66 |    |    |         +--- io.netty:netty-common:4.1.128.Final
#16 45.66 |    |    |    \--- io.netty:netty-codec:4.1.128.Final
#16 45.66 |    |    |    |    \--- io.netty:netty-transport:4.1.128.Final (*)
#16 45.66 |    |    |    |    +--- io.netty:netty-buffer:4.1.128.Final (*)
#16 45.66 |    |    |    |    +--- io.netty:netty-common:4.1.128.Final
#16 45.66 |    |    |    +--- io.netty:netty-transport-native-unix-common:4.1.128.Final
#16 45.66 |    |    |    |    \--- io.netty:netty-resolver:4.1.128.Final (*)
#16 45.66 |    |    |    |    +--- io.netty:netty-buffer:4.1.128.Final (*)
#16 45.66 |    |    |    |    +--- io.netty:netty-common:4.1.128.Final
#16 45.66 |    |    |    +--- io.netty:netty-transport:4.1.128.Final
#16 45.66 |    |    |    |    \--- io.netty:netty-common:4.1.128.Final
#16 45.66 |    |    |    +--- io.netty:netty-buffer:4.1.128.Final
#16 45.66 |    |    |    |    \--- io.netty:netty-common:4.1.128.Final
#16 45.66 |    |    |    +--- io.netty:netty-resolver:4.1.128.Final
#16 45.66 |    |    |    +--- io.netty:netty-common:4.1.128.Final
#16 45.66 |    |    +--- io.netty:netty-handler:4.1.118.Final -> 4.1.128.Final
#16 45.66 |    |    +--- io.netty:netty-common:4.1.118.Final -> 4.1.128.Final
#16 45.66 |    |    |    \--- org.slf4j:slf4j-api:1.7.36 -> 2.0.17
#16 45.66 |    |    +--- redis.clients.authentication:redis-authx-core:0.1.1-beta2
#16 45.66 |    +--- io.lettuce:lettuce-core:6.6.0.RELEASE
#16 45.66 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 45.66 +--- org.springframework.boot:spring-boot-starter-data-redis -> 3.5.7
#16 45.66 |    \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
#16 45.66 |    +--- org.thymeleaf:thymeleaf-spring6:3.1.3.RELEASE (*)
#16 45.66 +--- org.thymeleaf.extras:thymeleaf-extras-springsecurity6 -> 3.1.3.RELEASE
#16 45.66 +--- org.springframework:spring-aspects -> 6.2.12 (*)
#16 45.66 +--- org.springframework.retry:spring-retry -> 2.0.12
#16 45.66 |         \--- org.springframework:spring-web:6.2.12 (*)
#16 45.66 |         +--- org.springframework:spring-expression:6.2.12 (*)
#16 45.66 |         +--- org.springframework:spring-core:6.2.12 (*)
#16 45.66 |         +--- org.springframework:spring-context:6.2.12 (*)
#16 45.66 |         +--- org.springframework:spring-beans:6.2.12 (*)
#16 45.66 |         +--- org.springframework:spring-aop:6.2.12 (*)
#16 45.66 |    \--- org.springframework:spring-webmvc:6.2.12
#16 45.66 |    +--- org.springframework:spring-web:6.2.12 (*)
#16 45.66 |    |         \--- org.apache.tomcat.embed:tomcat-embed-core:10.1.48
#16 45.66 |    |    \--- org.apache.tomcat.embed:tomcat-embed-websocket:10.1.48
#16 45.66 |    |    +--- org.apache.tomcat.embed:tomcat-embed-el:10.1.48
#16 45.66 |    |    +--- org.apache.tomcat.embed:tomcat-embed-core:10.1.48
#16 45.66 |    |    +--- jakarta.annotation:jakarta.annotation-api:2.1.1
#16 45.66 |    +--- org.springframework.boot:spring-boot-starter-tomcat:3.5.7
#16 45.66 |    |         \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
#16 45.66 |    |         +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (*)
#16 45.66 |    |         +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (*)
#16 45.66 |    |    \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.19.2
#16 45.66 |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
#16 45.66 |    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (*)
#16 45.66 |    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (*)
#16 45.66 |    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.19.2 (*)
#16 45.66 |    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.2
#16 45.66 |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
#16 45.66 |    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (*)
#16 45.66 |    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (*)
#16 45.66 |    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.19.2
#16 45.66 |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
#16 45.66 |    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
#16 45.66 |    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.19.2
#16 45.66 |    |    |    |         \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.19.2 (c)
#16 45.66 |    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.2 (c)
#16 45.66 |    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.19.2 (c)
#16 45.66 |    |    |    |         +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (c)
#16 45.66 |    |    |    |         +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (c)
#16 45.66 |    |    |    |         +--- com.fasterxml.jackson.core:jackson-annotations:2.19.2 (c)
#16 45.66 |    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2
#16 45.66 |    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.19.2
#16 45.66 |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.19.2
#16 45.66 |    |    +--- org.springframework:spring-web:6.2.12 (*)
#16 45.66 |    |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 45.66 |    +--- org.springframework.boot:spring-boot-starter-json:3.5.7
#16 45.66 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 45.66 +--- org.springframework.boot:spring-boot-starter-web -> 3.5.7
#16 45.66 |         \--- com.fasterxml:classmate:1.5.1 -> 1.7.1
#16 45.66 |         +--- org.jboss.logging:jboss-logging:3.4.3.Final -> 3.6.1.Final
#16 45.66 |         +--- jakarta.validation:jakarta.validation-api:3.0.2
#16 45.66 |    \--- org.hibernate.validator:hibernate-validator:8.0.3.Final
#16 45.66 |    +--- org.apache.tomcat.embed:tomcat-embed-el:10.1.48
#16 45.66 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 45.66 +--- org.springframework.boot:spring-boot-starter-validation -> 3.5.7
#16 45.66 |         \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
#16 45.66 |         |    \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
#16 45.66 |         |    +--- org.unbescape:unbescape:1.1.6.RELEASE
#16 45.66 |         |    +--- org.attoparser:attoparser:2.0.7.RELEASE
#16 45.66 |         +--- org.thymeleaf:thymeleaf:3.1.3.RELEASE
#16 45.66 |    \--- org.thymeleaf:thymeleaf-spring6:3.1.3.RELEASE
#16 45.66 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 45.66 +--- org.springframework.boot:spring-boot-starter-thymeleaf -> 3.5.7
#16 45.66 |              \--- io.micrometer:micrometer-observation:1.14.12 -> 1.15.5 (*)
#16 45.66 |              +--- org.springframework:spring-core:6.2.12 (*)
#16 45.66 |              +--- org.springframework:spring-beans:6.2.12 (*)
#16 45.66 |         \--- org.springframework:spring-web:6.2.12
#16 45.66 |         +--- org.springframework:spring-expression:6.2.12 (*)
#16 45.66 |         +--- org.springframework:spring-context:6.2.12 (*)
#16 45.66 |         +--- org.springframework:spring-beans:6.2.12 (*)
#16 45.66 |         +--- org.springframework:spring-aop:6.2.12 (*)
#16 45.66 |         +--- org.springframework:spring-core:6.2.12 (*)
#16 45.66 |         +--- org.springframework.security:spring-security-core:6.5.6 (*)
#16 45.66 |    \--- org.springframework.security:spring-security-web:6.5.6
#16 45.66 |    |    \--- org.springframework:spring-core:6.2.12 (*)
#16 45.66 |    |    +--- org.springframework:spring-context:6.2.12 (*)
#16 45.66 |    |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 45.66 |    |    +--- org.springframework:spring-aop:6.2.12 (*)
#16 45.66 |    |    |    \--- io.micrometer:micrometer-observation:1.14.12 -> 1.15.5 (*)
#16 45.66 |    |    |    +--- org.springframework:spring-expression:6.2.12 (*)
#16 45.66 |    |    |    +--- org.springframework:spring-core:6.2.12 (*)
#16 45.66 |    |    |    +--- org.springframework:spring-context:6.2.12 (*)
#16 45.66 |    |    |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 45.66 |    |    |    +--- org.springframework:spring-aop:6.2.12 (*)
#16 45.66 |    |    |    +--- org.springframework.security:spring-security-crypto:6.5.6
#16 45.66 |    |    +--- org.springframework.security:spring-security-core:6.5.6
#16 45.66 |    +--- org.springframework.security:spring-security-config:6.5.6
#16 45.66 |    +--- org.springframework:spring-aop:6.2.12 (*)
#16 45.66 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 45.66 +--- org.springframework.boot:spring-boot-starter-security -> 3.5.7
#16 45.66 |         \--- org.aspectj:aspectjweaver:1.9.22.1 -> 1.9.24
#16 45.66 |    \--- org.springframework:spring-aspects:6.2.12
#16 45.66 |    |    \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
#16 45.66 |    |    +--- jakarta.annotation:jakarta.annotation-api:2.0.0 -> 2.1.1
#16 45.66 |    |    +--- org.antlr:antlr4-runtime:4.13.0
#16 45.66 |    |    +--- org.springframework:spring-core:6.2.12 (*)
#16 45.66 |    |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 45.66 |    |    +--- org.springframework:spring-tx:6.2.12 (*)
#16 45.66 |    |    +--- org.springframework:spring-aop:6.2.12 (*)
#16 45.66 |    |    +--- org.springframework:spring-context:6.2.12 (*)
#16 45.66 |    |    |    \--- org.springframework:spring-tx:6.2.12 (*)
#16 45.66 |    |    |    +--- org.springframework:spring-jdbc:6.2.12 (*)
#16 45.66 |    |    |    +--- org.springframework:spring-core:6.2.12 (*)
#16 45.66 |    |    |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 45.66 |    |    +--- org.springframework:spring-orm:6.2.12
#16 45.66 |    |    |    \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
#16 45.66 |    |    |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 45.66 |    |    |    +--- org.springframework:spring-core:6.2.12 (*)
#16 45.66 |    |    +--- org.springframework.data:spring-data-commons:3.5.5
#16 45.66 |    +--- org.springframework.data:spring-data-jpa:3.5.5
#16 45.66 |    |    \--- org.antlr:antlr4-runtime:4.13.0
#16 45.66 |    |    +--- jakarta.inject:jakarta.inject-api:2.0.1
#16 45.66 |    |    |         \--- com.sun.istack:istack-commons-runtime:4.1.2
#16 45.66 |    |    |         +--- org.glassfish.jaxb:txw2:4.0.6
#16 45.66 |    |    |         |    \--- jakarta.activation:jakarta.activation-api:2.1.4
#16 45.66 |    |    |         +--- org.eclipse.angus:angus-activation:2.0.3
#16 45.66 |    |    |         +--- jakarta.activation:jakarta.activation-api:2.1.4
#16 45.66 |    |    |         +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.4 (*)
#16 45.66 |    |    |    \--- org.glassfish.jaxb:jaxb-core:4.0.6
#16 45.66 |    |    +--- org.glassfish.jaxb:jaxb-runtime:4.0.2 -> 4.0.6
#16 45.66 |    |    |    \--- jakarta.activation:jakarta.activation-api:2.1.4
#16 45.66 |    |    +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.0 -> 4.0.4
#16 45.66 |    |    +--- net.bytebuddy:byte-buddy:1.15.11 -> 1.17.8
#16 45.66 |    |    +--- com.fasterxml:classmate:1.5.1 -> 1.7.1
#16 45.66 |    |    +--- io.smallrye:jandex:3.2.0
#16 45.66 |    |    +--- org.hibernate.common:hibernate-commons-annotations:7.0.3.Final
#16 45.66 |    |    +--- org.jboss.logging:jboss-logging:3.5.0.Final -> 3.6.1.Final
#16 45.66 |    |    +--- jakarta.transaction:jakarta.transaction-api:2.0.1
#16 45.66 |    |    +--- jakarta.persistence:jakarta.persistence-api:3.1.0
#16 45.66 |    +--- org.hibernate.orm:hibernate-core:6.6.33.Final
#16 45.66 |    |              \--- org.springframework:spring-core:6.2.12 (*)
#16 45.66 |    |              +--- org.springframework:spring-beans:6.2.12 (*)
#16 45.66 |    |         \--- org.springframework:spring-tx:6.2.12
#16 45.66 |    |         +--- org.springframework:spring-core:6.2.12 (*)
#16 45.66 |    |         +--- org.springframework:spring-beans:6.2.12 (*)
#16 45.66 |    |    \--- org.springframework:spring-jdbc:6.2.12
#16 45.66 |    |    |    \--- org.slf4j:slf4j-api:2.0.17
#16 45.66 |    |    +--- com.zaxxer:HikariCP:6.3.3
#16 45.66 |    |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 45.66 |    +--- org.springframework.boot:spring-boot-starter-jdbc:3.5.7
#16 45.66 |    |    \--- org.yaml:snakeyaml:2.4
#16 45.66 |    |    +--- org.springframework:spring-core:6.2.12 (*)
#16 45.66 |    |    +--- jakarta.annotation:jakarta.annotation-api:2.1.1
#16 45.66 |    |    |         \--- org.slf4j:slf4j-api:2.0.17
#16 45.66 |    |    |    \--- org.slf4j:jul-to-slf4j:2.0.17
#16 45.66 |    |    |    |    \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
#16 45.66 |    |    |    |    +--- org.apache.logging.log4j:log4j-api:2.24.3
#16 45.66 |    |    |    +--- org.apache.logging.log4j:log4j-to-slf4j:2.24.3
#16 45.66 |    |    |    |    \--- org.slf4j:slf4j-api:2.0.17
#16 45.66 |    |    |    |    +--- ch.qos.logback:logback-core:1.5.20
#16 45.66 |    |    |    +--- ch.qos.logback:logback-classic:1.5.20
#16 45.66 |    |    +--- org.springframework.boot:spring-boot-starter-logging:3.5.7
#16 45.66 |    |    |    \--- org.springframework.boot:spring-boot:3.5.7 (*)
#16 45.66 |    |    +--- org.springframework.boot:spring-boot-autoconfigure:3.5.7
#16 45.66 |    |    |              \--- io.micrometer:micrometer-commons:1.15.5
#16 45.66 |    |    |         \--- io.micrometer:micrometer-observation:1.14.12 -> 1.15.5
#16 45.66 |    |    |         |    \--- org.springframework:spring-core:6.2.12 (*)
#16 45.66 |    |    |         +--- org.springframework:spring-expression:6.2.12
#16 45.66 |    |    |         +--- org.springframework:spring-core:6.2.12 (*)
#16 45.66 |    |    |         +--- org.springframework:spring-beans:6.2.12 (*)
#16 45.66 |    |    |         |    \--- org.springframework:spring-core:6.2.12 (*)
#16 45.66 |    |    |         |    |    \--- org.springframework:spring-core:6.2.12 (*)
#16 45.66 |    |    |         |    +--- org.springframework:spring-beans:6.2.12
#16 45.66 |    |    |         +--- org.springframework:spring-aop:6.2.12
#16 45.66 |    |    |    \--- org.springframework:spring-context:6.2.12
#16 45.66 |    |    |    |    \--- org.springframework:spring-jcl:6.2.12
#16 45.66 |    |    |    +--- org.springframework:spring-core:6.2.12
#16 45.66 |    |    +--- org.springframework.boot:spring-boot:3.5.7
#16 45.66 |    +--- org.springframework.boot:spring-boot-starter:3.5.7
#16 45.66 +--- org.springframework.boot:spring-boot-starter-data-jpa -> 3.5.7
#16 45.37 testRuntimeClasspath - Runtime classpath of source set 'test'.
#16 45.37
#16 45.37 \--- org.springframework.security:spring-security-test (n)
#16 45.37 +--- org.springframework.boot:spring-boot-starter-test (n)
#16 45.37 testImplementation - Implementation only dependencies for source set 'test'. (n)
#16 45.37
#16 45.37 No dependencies
#16 45.37 testCompileOnly - Compile only dependencies for source set 'test'. (n)
#16 45.37
#16 45.37      \--- org.springframework:spring-test:6.2.12 (*)
#16 45.37      +--- org.springframework:spring-core:6.2.12 (*)
#16 45.37      +--- org.springframework.security:spring-security-web:6.5.6 (*)
#16 45.37      +--- org.springframework.security:spring-security-core:6.5.6 (*)
#16 45.37 \--- org.springframework.security:spring-security-test -> 6.5.6
#16 45.37 |    \--- org.xmlunit:xmlunit-core:2.10.4
#16 45.37 |    +--- org.springframework:spring-test:6.2.12 (*)
#16 45.37 |    +--- org.springframework:spring-core:6.2.12 (*)
#16 45.37 |    |    \--- com.vaadin.external.google:android-json:0.0.20131108.vaadin1
#16 45.37 |    +--- org.skyscreamer:jsonassert:1.5.3
#16 45.37 |    |    \--- org.mockito:mockito-core:5.17.0 (*)
#16 45.37 |    +--- org.mockito:mockito-junit-jupiter:5.17.0
#16 45.37 |    |    \--- net.bytebuddy:byte-buddy-agent:1.15.11 -> 1.17.8
#16 45.37 |    |    +--- net.bytebuddy:byte-buddy:1.15.11 -> 1.17.8
#16 45.37 |    +--- org.mockito:mockito-core:5.17.0
#16 45.37 |    |         \--- org.apiguardian:apiguardian-api:1.1.2
#16 45.37 |    |         +--- org.junit.jupiter:junit-jupiter-api:5.12.2 (*)
#16 45.37 |    |         +--- org.junit:junit-bom:5.12.2 (*)
#16 45.37 |    |    \--- org.junit.jupiter:junit-jupiter-params:5.12.2
#16 45.37 |    |    |    \--- org.apiguardian:apiguardian-api:1.1.2
#16 45.37 |    |    |    |    \--- org.apiguardian:apiguardian-api:1.1.2
#16 45.37 |    |    |    |    +--- org.junit:junit-bom:5.12.2 (*)
#16 45.37 |    |    |    +--- org.junit.platform:junit-platform-commons:1.12.2
#16 45.37 |    |    |    +--- org.opentest4j:opentest4j:1.3.0
#16 45.37 |    |    |    +--- org.junit:junit-bom:5.12.2 (*)
#16 45.37 |    |    +--- org.junit.jupiter:junit-jupiter-api:5.12.2
#16 45.37 |    |    |    \--- org.junit.platform:junit-platform-commons:1.12.2 (c)
#16 45.37 |    |    |    +--- org.junit.jupiter:junit-jupiter-params:5.12.2 (c)
#16 45.37 |    |    |    +--- org.junit.jupiter:junit-jupiter-api:5.12.2 (c)
#16 45.37 |    |    |    +--- org.junit.jupiter:junit-jupiter:5.12.2 (c)
#16 45.37 |    |    +--- org.junit:junit-bom:5.12.2
#16 45.37 |    +--- org.junit.jupiter:junit-jupiter:5.12.2
#16 45.37 |    +--- org.hamcrest:hamcrest:3.0
#16 45.37 |    |    \--- org.hamcrest:hamcrest:2.1 -> 3.0
#16 45.37 |    +--- org.awaitility:awaitility:4.2.2
#16 45.37 |    |    \--- net.bytebuddy:byte-buddy:1.17.7 -> 1.17.8
#16 45.37 |    +--- org.assertj:assertj-core:3.27.6
#16 45.37 |    |         \--- org.ow2.asm:asm:9.7.1
#16 45.37 |    |    \--- net.minidev:accessors-smart:2.5.2
#16 45.37 |    +--- net.minidev:json-smart:2.5.2
#16 45.37 |    |    \--- jakarta.activation:jakarta.activation-api:2.1.4
#16 45.37 |    +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.4
#16 45.37 |    +--- com.jayway.jsonpath:json-path:2.9.0
#16 45.37 |    |    \--- org.springframework.boot:spring-boot-autoconfigure:3.5.7 (*)
#16 45.37 |    |    +--- org.springframework.boot:spring-boot-test:3.5.7 (*)
#16 45.37 |    |    +--- org.springframework.boot:spring-boot:3.5.7 (*)
#16 45.37 |    +--- org.springframework.boot:spring-boot-test-autoconfigure:3.5.7
#16 45.37 |    |         \--- org.springframework:spring-core:6.2.12 (*)
#16 45.36 |    |    \--- org.springframework:spring-test:6.2.12
#16 45.36 |    |    +--- org.springframework.boot:spring-boot:3.5.7 (*)
#16 45.36 |    +--- org.springframework.boot:spring-boot-test:3.5.7
#16 45.36 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 45.36 +--- org.springframework.boot:spring-boot-starter-test -> 3.5.7
#16 45.36 |         \--- io.projectreactor:reactor-core:3.7.6 -> 3.7.12 (*)
#16 45.36 |    \--- io.github.openfeign.querydsl:querydsl-core:7.0
#16 45.36 +--- io.github.openfeign.querydsl:querydsl-jpa:7.0
#16 45.36 |    \--- org.apache.httpcomponents.core5:httpcore5:5.2.5 -> 5.3.6
#16 45.36 |    |    \--- org.slf4j:slf4j-api:1.7.36 -> 2.0.17
#16 45.36 |    |    |    \--- org.apache.httpcomponents.core5:httpcore5:5.3.6
#16 45.36 |    |    +--- org.apache.httpcomponents.core5:httpcore5-h2:5.3.6
#16 45.36 |    |    +--- org.apache.httpcomponents.core5:httpcore5:5.3.6
#16 45.36 |    +--- org.apache.httpcomponents.client5:httpclient5:5.3.1 -> 5.5.1
#16 45.36 |    +--- org.apache.commons:commons-lang3:3.1 -> 3.18.0
#16 45.36 |    +--- com.cloudinary:cloudinary-core:2.3.0
#16 45.36 +--- com.cloudinary:cloudinary-http5:2.3.0
#16 45.36 +--- net.coobird:thumbnailator:0.4.19
#16 45.36 |    \--- org.springframework:spring-context-support:6.2.12 (*)
#16 45.36 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 45.36 +--- org.springframework.boot:spring-boot-starter-cache -> 3.5.7
#16 45.36 |         \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
#16 45.36 |         |    \--- org.springframework:spring-core:6.2.12 (*)
#16 45.36 |         |    +--- org.springframework:spring-context:6.2.12 (*)
#16 45.36 |         |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 45.36 |         +--- org.springframework:spring-context-support:6.2.12
#16 45.36 |         +--- org.springframework:spring-aop:6.2.12 (*)
#16 45.36 |         |    \--- org.springframework:spring-core:6.2.12 (*)
#16 45.36 |         |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 45.36 |         +--- org.springframework:spring-oxm:6.2.12
#16 45.36 |         +--- org.springframework:spring-tx:6.2.12 (*)
#16 45.36 |         |    \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
#16 45.36 |         |    +--- org.springframework:spring-tx:6.2.12 (*)
#16 45.36 |         |    +--- org.springframework:spring-context:6.2.12 (*)
#16 45.36 |         |    +--- org.springframework.data:spring-data-commons:3.5.5 (*)
#16 45.36 |         +--- org.springframework.data:spring-data-keyvalue:3.5.5
#16 45.36 |    \--- org.springframework.data:spring-data-redis:3.5.5
#16 45.36 |    |         \--- org.reactivestreams:reactive-streams:1.0.4
#16 45.36 |    |    \--- io.projectreactor:reactor-core:3.6.6 -> 3.7.12
#16 45.36 |    |    +--- io.netty:netty-transport:4.1.118.Final -> 4.1.128.Final (*)
#16 45.36 |    |    |         \--- io.netty:netty-transport:4.1.128.Final (*)
#16 45.36 |    |    |         +--- io.netty:netty-buffer:4.1.128.Final (*)
#16 45.36 |    |    |         +--- io.netty:netty-common:4.1.128.Final
#16 45.36 |    |    |    \--- io.netty:netty-codec:4.1.128.Final
#16 45.36 |    |    |    |    \--- io.netty:netty-transport:4.1.128.Final (*)
#16 45.36 |    |    |    |    +--- io.netty:netty-buffer:4.1.128.Final (*)
#16 45.36 |    |    |    |    +--- io.netty:netty-common:4.1.128.Final
#16 45.36 |    |    |    +--- io.netty:netty-transport-native-unix-common:4.1.128.Final
#16 45.36 |    |    |    |    \--- io.netty:netty-resolver:4.1.128.Final (*)
#16 45.36 |    |    |    |    +--- io.netty:netty-buffer:4.1.128.Final (*)
#16 45.36 |    |    |    |    +--- io.netty:netty-common:4.1.128.Final
#16 45.36 |    |    |    +--- io.netty:netty-transport:4.1.128.Final
#16 45.36 |    |    |    |    \--- io.netty:netty-common:4.1.128.Final
#16 45.36 |    |    |    +--- io.netty:netty-buffer:4.1.128.Final
#16 45.36 |    |    |    |    \--- io.netty:netty-common:4.1.128.Final
#16 45.36 |    |    |    +--- io.netty:netty-resolver:4.1.128.Final
#16 45.36 |    |    |    +--- io.netty:netty-common:4.1.128.Final
#16 45.36 |    |    +--- io.netty:netty-handler:4.1.118.Final -> 4.1.128.Final
#16 45.36 |    |    +--- io.netty:netty-common:4.1.118.Final -> 4.1.128.Final
#16 45.36 |    |    |    \--- org.slf4j:slf4j-api:1.7.36 -> 2.0.17
#16 45.36 |    |    +--- redis.clients.authentication:redis-authx-core:0.1.1-beta2
#16 45.36 |    +--- io.lettuce:lettuce-core:6.6.0.RELEASE
#16 45.36 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 45.36 +--- org.springframework.boot:spring-boot-starter-data-redis -> 3.5.7
#16 45.36 |    \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
#16 45.36 |    +--- org.thymeleaf:thymeleaf-spring6:3.1.3.RELEASE (*)
#16 45.36 +--- org.thymeleaf.extras:thymeleaf-extras-springsecurity6 -> 3.1.3.RELEASE
#16 45.36 +--- org.springframework:spring-aspects -> 6.2.12 (*)
#16 45.36 +--- org.springframework.retry:spring-retry -> 2.0.12
#16 45.36 |         \--- org.springframework:spring-web:6.2.12 (*)
#16 45.36 |         +--- org.springframework:spring-expression:6.2.12 (*)
#16 45.36 |         +--- org.springframework:spring-core:6.2.12 (*)
#16 45.36 |         +--- org.springframework:spring-context:6.2.12 (*)
#16 45.36 |         +--- org.springframework:spring-beans:6.2.12 (*)
#16 45.36 |         +--- org.springframework:spring-aop:6.2.12 (*)
#16 45.36 |    \--- org.springframework:spring-webmvc:6.2.12
#16 45.36 |    +--- org.springframework:spring-web:6.2.12 (*)
#16 45.36 |    |         \--- org.apache.tomcat.embed:tomcat-embed-core:10.1.48
#16 45.36 |    |    \--- org.apache.tomcat.embed:tomcat-embed-websocket:10.1.48
#16 45.36 |    |    +--- org.apache.tomcat.embed:tomcat-embed-el:10.1.48
#16 45.36 |    |    +--- org.apache.tomcat.embed:tomcat-embed-core:10.1.48
#16 45.36 |    |    +--- jakarta.annotation:jakarta.annotation-api:2.1.1
#16 45.36 |    +--- org.springframework.boot:spring-boot-starter-tomcat:3.5.7
#16 45.36 |    |         \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
#16 45.36 |    |         +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (*)
#16 45.36 |    |         +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (*)
#16 45.36 |    |    \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.19.2
#16 45.36 |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
#16 45.36 |    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (*)
#16 45.36 |    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (*)
#16 45.36 |    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.19.2 (*)
#16 45.36 |    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.2
#16 45.36 |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
#16 45.36 |    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (*)
#16 45.36 |    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (*)
#16 45.36 |    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.19.2
#16 45.36 |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
#16 45.36 |    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
#16 45.36 |    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.19.2
#16 45.36 |    |    |    |         \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.19.2 (c)
#16 45.36 |    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.2 (c)
#16 45.36 |    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.19.2 (c)
#16 45.36 |    |    |    |         +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (c)
#16 45.36 |    |    |    |         +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (c)
#16 45.36 |    |    |    |         +--- com.fasterxml.jackson.core:jackson-annotations:2.19.2 (c)
#16 45.36 |    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2
#16 45.36 |    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.19.2
#16 45.36 |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.19.2
#16 45.36 |    |    +--- org.springframework:spring-web:6.2.12 (*)
#16 45.36 |    |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 45.36 |    +--- org.springframework.boot:spring-boot-starter-json:3.5.7
#16 45.36 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 45.36 +--- org.springframework.boot:spring-boot-starter-web -> 3.5.7
#16 45.36 |         \--- com.fasterxml:classmate:1.5.1 -> 1.7.1
#16 45.36 |         +--- org.jboss.logging:jboss-logging:3.4.3.Final -> 3.6.1.Final
#16 45.36 |         +--- jakarta.validation:jakarta.validation-api:3.0.2
#16 45.36 |    \--- org.hibernate.validator:hibernate-validator:8.0.3.Final
#16 45.36 |    +--- org.apache.tomcat.embed:tomcat-embed-el:10.1.48
#16 45.36 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 45.36 +--- org.springframework.boot:spring-boot-starter-validation -> 3.5.7
#16 45.36 |         \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
#16 45.36 |         |    \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
#16 45.36 |         |    +--- org.unbescape:unbescape:1.1.6.RELEASE
#16 45.36 |         |    +--- org.attoparser:attoparser:2.0.7.RELEASE
#16 45.36 |         +--- org.thymeleaf:thymeleaf:3.1.3.RELEASE
#16 45.36 |    \--- org.thymeleaf:thymeleaf-spring6:3.1.3.RELEASE
#16 45.36 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 45.36 +--- org.springframework.boot:spring-boot-starter-thymeleaf -> 3.5.7
#16 45.36 |              \--- io.micrometer:micrometer-observation:1.14.12 -> 1.15.5 (*)
#16 45.36 |              +--- org.springframework:spring-core:6.2.12 (*)
#16 45.36 |              +--- org.springframework:spring-beans:6.2.12 (*)
#16 45.36 |         \--- org.springframework:spring-web:6.2.12
#16 45.36 |         +--- org.springframework:spring-expression:6.2.12 (*)
#16 45.36 |         +--- org.springframework:spring-context:6.2.12 (*)
#16 45.36 |         +--- org.springframework:spring-beans:6.2.12 (*)
#16 45.36 |         +--- org.springframework:spring-aop:6.2.12 (*)
#16 45.36 |         +--- org.springframework:spring-core:6.2.12 (*)
#16 45.36 |         +--- org.springframework.security:spring-security-core:6.5.6 (*)
#16 45.36 |    \--- org.springframework.security:spring-security-web:6.5.6
#16 45.36 |    |    \--- org.springframework:spring-core:6.2.12 (*)
#16 45.36 |    |    +--- org.springframework:spring-context:6.2.12 (*)
#16 45.36 |    |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 45.36 |    |    +--- org.springframework:spring-aop:6.2.12 (*)
#16 45.36 |    |    |    \--- io.micrometer:micrometer-observation:1.14.12 -> 1.15.5 (*)
#16 45.36 |    |    |    +--- org.springframework:spring-expression:6.2.12 (*)
#16 45.36 |    |    |    +--- org.springframework:spring-core:6.2.12 (*)
#16 45.36 |    |    |    +--- org.springframework:spring-context:6.2.12 (*)
#16 45.36 |    |    |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 45.36 |    |    |    +--- org.springframework:spring-aop:6.2.12 (*)
#16 45.36 |    |    |    +--- org.springframework.security:spring-security-crypto:6.5.6
#16 45.36 |    |    +--- org.springframework.security:spring-security-core:6.5.6
#16 45.36 |    +--- org.springframework.security:spring-security-config:6.5.6
#16 45.36 |    +--- org.springframework:spring-aop:6.2.12 (*)
#16 45.36 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 45.36 +--- org.springframework.boot:spring-boot-starter-security -> 3.5.7
#16 45.36 |         \--- org.aspectj:aspectjweaver:1.9.22.1 -> 1.9.24
#16 45.36 |    \--- org.springframework:spring-aspects:6.2.12
#16 45.36 |    |    \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
#16 45.36 |    |    +--- jakarta.annotation:jakarta.annotation-api:2.0.0 -> 2.1.1
#16 45.36 |    |    +--- org.antlr:antlr4-runtime:4.13.0
#16 45.36 |    |    +--- org.springframework:spring-core:6.2.12 (*)
#16 45.36 |    |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 45.36 |    |    +--- org.springframework:spring-tx:6.2.12 (*)
#16 45.36 |    |    +--- org.springframework:spring-aop:6.2.12 (*)
#16 45.36 |    |    +--- org.springframework:spring-context:6.2.12 (*)
#16 45.36 |    |    |    \--- org.springframework:spring-tx:6.2.12 (*)
#16 45.36 |    |    |    +--- org.springframework:spring-jdbc:6.2.12 (*)
#16 45.36 |    |    |    +--- org.springframework:spring-core:6.2.12 (*)
#16 45.36 |    |    |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 45.36 |    |    +--- org.springframework:spring-orm:6.2.12
#16 45.36 |    |    |    \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
#16 45.36 |    |    |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 45.36 |    |    |    +--- org.springframework:spring-core:6.2.12 (*)
#16 45.36 |    |    +--- org.springframework.data:spring-data-commons:3.5.5
#16 45.36 |    +--- org.springframework.data:spring-data-jpa:3.5.5
#16 45.36 |    |    \--- jakarta.transaction:jakarta.transaction-api:2.0.1
#16 45.36 |    |    +--- jakarta.persistence:jakarta.persistence-api:3.1.0
#16 45.36 |    +--- org.hibernate.orm:hibernate-core:6.6.33.Final
#16 45.36 |    |              \--- org.springframework:spring-core:6.2.12 (*)
#16 45.36 |    |              +--- org.springframework:spring-beans:6.2.12 (*)
#16 45.36 |    |         \--- org.springframework:spring-tx:6.2.12
#16 45.36 |    |         +--- org.springframework:spring-core:6.2.12 (*)
#16 45.36 |    |         +--- org.springframework:spring-beans:6.2.12 (*)
#16 45.36 |    |    \--- org.springframework:spring-jdbc:6.2.12
#16 45.36 |    |    |    \--- org.slf4j:slf4j-api:2.0.17
#16 45.36 |    |    +--- com.zaxxer:HikariCP:6.3.3
#16 45.36 |    |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 45.36 |    +--- org.springframework.boot:spring-boot-starter-jdbc:3.5.7
#16 45.36 |    |    \--- org.yaml:snakeyaml:2.4
#16 45.36 |    |    +--- org.springframework:spring-core:6.2.12 (*)
#16 45.36 |    |    +--- jakarta.annotation:jakarta.annotation-api:2.1.1
#16 45.36 |    |    |         \--- org.slf4j:slf4j-api:2.0.17
#16 45.36 |    |    |    \--- org.slf4j:jul-to-slf4j:2.0.17
#16 45.36 |    |    |    |    \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
#16 45.36 |    |    |    |    +--- org.apache.logging.log4j:log4j-api:2.24.3
#16 45.36 |    |    |    +--- org.apache.logging.log4j:log4j-to-slf4j:2.24.3
#16 45.36 |    |    |    |    \--- org.slf4j:slf4j-api:2.0.17
#16 45.36 |    |    |    |    +--- ch.qos.logback:logback-core:1.5.20
#16 45.36 |    |    |    +--- ch.qos.logback:logback-classic:1.5.20
#16 45.36 |    |    +--- org.springframework.boot:spring-boot-starter-logging:3.5.7
#16 45.36 |    |    |    \--- org.springframework.boot:spring-boot:3.5.7 (*)
#16 45.36 |    |    +--- org.springframework.boot:spring-boot-autoconfigure:3.5.7
#16 45.36 |    |    |              \--- io.micrometer:micrometer-commons:1.15.5
#16 45.36 |    |    |         \--- io.micrometer:micrometer-observation:1.14.12 -> 1.15.5
#16 45.36 |    |    |         |    \--- org.springframework:spring-core:6.2.12 (*)
#16 45.36 |    |    |         +--- org.springframework:spring-expression:6.2.12
#16 45.36 |    |    |         +--- org.springframework:spring-core:6.2.12 (*)
#16 45.36 |    |    |         +--- org.springframework:spring-beans:6.2.12 (*)
#16 45.36 |    |    |         |    \--- org.springframework:spring-core:6.2.12 (*)
#16 45.36 |    |    |         |    |    \--- org.springframework:spring-core:6.2.12 (*)
#16 45.36 |    |    |         |    +--- org.springframework:spring-beans:6.2.12
#16 45.36 |    |    |         +--- org.springframework:spring-aop:6.2.12
#16 45.36 |    |    |    \--- org.springframework:spring-context:6.2.12
#16 45.36 |    |    |    |    \--- org.springframework:spring-jcl:6.2.12
#16 45.36 |    |    |    +--- org.springframework:spring-core:6.2.12
#16 45.36 |    |    +--- org.springframework.boot:spring-boot:3.5.7
#16 45.36 |    +--- org.springframework.boot:spring-boot-starter:3.5.7
#16 45.36 +--- org.springframework.boot:spring-boot-starter-data-jpa -> 3.5.7
#16 42.84 testCompileClasspath - Compile classpath for source set 'test'.
#16 42.84
#16 42.84 No dependencies
#16 42.84 testAnnotationProcessor - Annotation processors and their dependencies for source set 'test'.
#16 42.84
#16 42.84 No dependencies
#16 42.84 testAndDevelopmentOnly - Configuration for test and development-only dependencies such as Spring Boot's DevTools.
#16 42.84
#16 42.84 \--- org.postgresql:postgresql (n)
#16 42.84 +--- com.h2database:h2 (n)
#16 42.84 runtimeOnly - Runtime-only dependencies for the 'main' feature. (n)
#16 42.84
#16 42.84 No dependencies
#16 42.84 runtimeElements - Runtime elements for the 'main' feature. (n)
#16 42.84
#16 42.84      \--- org.checkerframework:checker-qual:3.49.5
#16 42.84 \--- org.postgresql:postgresql -> 42.7.8
#16 42.84 +--- com.h2database:h2 -> 2.3.232
#16 42.84 |         \--- io.projectreactor:reactor-core:3.7.6 -> 3.7.12 (*)
#16 42.84 |    \--- io.github.openfeign.querydsl:querydsl-core:7.0
#16 42.84 +--- io.github.openfeign.querydsl:querydsl-jpa:7.0
#16 42.84 |    \--- org.apache.httpcomponents.core5:httpcore5:5.2.5 -> 5.3.6
#16 42.84 |    |    \--- org.slf4j:slf4j-api:1.7.36 -> 2.0.17
#16 42.84 |    |    |    \--- org.apache.httpcomponents.core5:httpcore5:5.3.6
#16 42.84 |    |    +--- org.apache.httpcomponents.core5:httpcore5-h2:5.3.6
#16 42.84 |    |    +--- org.apache.httpcomponents.core5:httpcore5:5.3.6
#16 42.84 |    +--- org.apache.httpcomponents.client5:httpclient5:5.3.1 -> 5.5.1
#16 42.84 |    +--- org.apache.commons:commons-lang3:3.1 -> 3.18.0
#16 42.84 |    +--- com.cloudinary:cloudinary-core:2.3.0
#16 42.84 +--- com.cloudinary:cloudinary-http5:2.3.0
#16 42.84 +--- net.coobird:thumbnailator:0.4.19
#16 42.84 |    \--- org.springframework:spring-context-support:6.2.12 (*)
#16 42.84 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 42.84 +--- org.springframework.boot:spring-boot-starter-cache -> 3.5.7
#16 42.84 |         \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
#16 42.84 |         |    \--- org.springframework:spring-core:6.2.12 (*)
#16 42.84 |         |    +--- org.springframework:spring-context:6.2.12 (*)
#16 42.84 |         |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.84 |         +--- org.springframework:spring-context-support:6.2.12
#16 42.84 |         +--- org.springframework:spring-aop:6.2.12 (*)
#16 42.84 |         |    \--- org.springframework:spring-core:6.2.12 (*)
#16 42.84 |         |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.84 |         |    +--- jakarta.xml.bind:jakarta.xml.bind-api:3.0.1 -> 4.0.4 (*)
#16 42.84 |         +--- org.springframework:spring-oxm:6.2.12
#16 42.84 |         +--- org.springframework:spring-tx:6.2.12 (*)
#16 42.84 |         |    \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
#16 42.84 |         |    +--- org.springframework:spring-tx:6.2.12 (*)
#16 42.84 |         |    +--- org.springframework:spring-context:6.2.12 (*)
#16 42.84 |         |    +--- org.springframework.data:spring-data-commons:3.5.5 (*)
#16 42.84 |         +--- org.springframework.data:spring-data-keyvalue:3.5.5
#16 42.84 |    \--- org.springframework.data:spring-data-redis:3.5.5
#16 42.84 |    |         \--- org.reactivestreams:reactive-streams:1.0.4
#16 42.84 |    |    \--- io.projectreactor:reactor-core:3.6.6 -> 3.7.12
#16 42.84 |    |    +--- io.netty:netty-transport:4.1.118.Final -> 4.1.128.Final (*)
#16 42.84 |    |    |         \--- io.netty:netty-transport:4.1.128.Final (*)
#16 42.84 |    |    |         +--- io.netty:netty-buffer:4.1.128.Final (*)
#16 42.84 |    |    |         +--- io.netty:netty-common:4.1.128.Final
#16 42.84 |    |    |    \--- io.netty:netty-codec:4.1.128.Final
#16 42.84 |    |    |    |    \--- io.netty:netty-transport:4.1.128.Final (*)
#16 42.84 |    |    |    |    +--- io.netty:netty-buffer:4.1.128.Final (*)
#16 42.84 |    |    |    |    +--- io.netty:netty-common:4.1.128.Final
#16 42.84 |    |    |    +--- io.netty:netty-transport-native-unix-common:4.1.128.Final
#16 42.84 |    |    |    |    \--- io.netty:netty-resolver:4.1.128.Final (*)
#16 42.84 |    |    |    |    +--- io.netty:netty-buffer:4.1.128.Final (*)
#16 42.84 |    |    |    |    +--- io.netty:netty-common:4.1.128.Final
#16 42.84 |    |    |    +--- io.netty:netty-transport:4.1.128.Final
#16 42.84 |    |    |    |    \--- io.netty:netty-common:4.1.128.Final
#16 42.84 |    |    |    +--- io.netty:netty-buffer:4.1.128.Final
#16 42.84 |    |    |    |    \--- io.netty:netty-common:4.1.128.Final
#16 42.84 |    |    |    +--- io.netty:netty-resolver:4.1.128.Final
#16 42.84 |    |    |    +--- io.netty:netty-common:4.1.128.Final
#16 42.84 |    |    +--- io.netty:netty-handler:4.1.118.Final -> 4.1.128.Final
#16 42.84 |    |    +--- io.netty:netty-common:4.1.118.Final -> 4.1.128.Final
#16 42.84 |    |    |    \--- org.slf4j:slf4j-api:1.7.36 -> 2.0.17
#16 42.83 |    |    +--- redis.clients.authentication:redis-authx-core:0.1.1-beta2
#16 42.83 |    +--- io.lettuce:lettuce-core:6.6.0.RELEASE
#16 42.83 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 42.83 +--- org.springframework.boot:spring-boot-starter-data-redis -> 3.5.7
#16 42.83 |    \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
#16 42.83 |    +--- org.thymeleaf:thymeleaf-spring6:3.1.3.RELEASE (*)
#16 42.83 +--- org.thymeleaf.extras:thymeleaf-extras-springsecurity6 -> 3.1.3.RELEASE
#16 42.83 +--- org.springframework:spring-aspects -> 6.2.12 (*)
#16 42.83 +--- org.springframework.retry:spring-retry -> 2.0.12
#16 42.83 |         \--- org.springframework:spring-web:6.2.12 (*)
#16 42.83 |         +--- org.springframework:spring-expression:6.2.12 (*)
#16 42.83 |         +--- org.springframework:spring-core:6.2.12 (*)
#16 42.83 |         +--- org.springframework:spring-context:6.2.12 (*)
#16 42.83 |         +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.83 |         +--- org.springframework:spring-aop:6.2.12 (*)
#16 42.83 |    \--- org.springframework:spring-webmvc:6.2.12
#16 42.83 |    +--- org.springframework:spring-web:6.2.12 (*)
#16 42.83 |    |         \--- org.apache.tomcat.embed:tomcat-embed-core:10.1.48
#16 42.83 |    |    \--- org.apache.tomcat.embed:tomcat-embed-websocket:10.1.48
#16 42.83 |    |    +--- org.apache.tomcat.embed:tomcat-embed-el:10.1.48
#16 42.83 |    |    +--- org.apache.tomcat.embed:tomcat-embed-core:10.1.48
#16 42.83 |    |    +--- jakarta.annotation:jakarta.annotation-api:2.1.1
#16 42.83 |    +--- org.springframework.boot:spring-boot-starter-tomcat:3.5.7
#16 42.83 |    |         \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
#16 42.83 |    |         +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (*)
#16 42.83 |    |         +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (*)
#16 42.83 |    |    \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.19.2
#16 42.83 |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
#16 42.83 |    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (*)
#16 42.83 |    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (*)
#16 42.83 |    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.19.2 (*)
#16 42.83 |    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.2
#16 42.83 |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
#16 42.83 |    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (*)
#16 42.83 |    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (*)
#16 42.83 |    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.19.2
#16 42.83 |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
#16 42.83 |    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
#16 42.83 |    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.19.2
#16 42.83 |    |    |    |         \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.19.2 (c)
#16 42.83 |    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.2 (c)
#16 42.83 |    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.19.2 (c)
#16 42.83 |    |    |    |         +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (c)
#16 42.83 |    |    |    |         +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (c)
#16 42.83 |    |    |    |         +--- com.fasterxml.jackson.core:jackson-annotations:2.19.2 (c)
#16 42.83 |    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2
#16 42.83 |    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.19.2
#16 42.83 |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.19.2
#16 42.83 |    |    +--- org.springframework:spring-web:6.2.12 (*)
#16 42.83 |    |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 42.83 |    +--- org.springframework.boot:spring-boot-starter-json:3.5.7
#16 42.83 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 42.83 +--- org.springframework.boot:spring-boot-starter-web -> 3.5.7
#16 42.83 |         \--- com.fasterxml:classmate:1.5.1 -> 1.7.1
#16 42.83 |         +--- org.jboss.logging:jboss-logging:3.4.3.Final -> 3.6.1.Final
#16 42.83 |         +--- jakarta.validation:jakarta.validation-api:3.0.2
#16 42.83 |    \--- org.hibernate.validator:hibernate-validator:8.0.3.Final
#16 42.83 |    +--- org.apache.tomcat.embed:tomcat-embed-el:10.1.48
#16 42.83 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 42.83 +--- org.springframework.boot:spring-boot-starter-validation -> 3.5.7
#16 42.83 |         \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
#16 42.83 |         |    \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
#16 42.83 |         |    +--- org.unbescape:unbescape:1.1.6.RELEASE
#16 42.83 |         |    +--- org.attoparser:attoparser:2.0.7.RELEASE
#16 42.83 |         +--- org.thymeleaf:thymeleaf:3.1.3.RELEASE
#16 42.83 |    \--- org.thymeleaf:thymeleaf-spring6:3.1.3.RELEASE
#16 42.83 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 42.83 +--- org.springframework.boot:spring-boot-starter-thymeleaf -> 3.5.7
#16 42.83 |              \--- io.micrometer:micrometer-observation:1.14.12 -> 1.15.5 (*)
#16 42.83 |              +--- org.springframework:spring-core:6.2.12 (*)
#16 42.83 |              +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.83 |         \--- org.springframework:spring-web:6.2.12
#16 42.83 |         +--- org.springframework:spring-expression:6.2.12 (*)
#16 42.83 |         +--- org.springframework:spring-context:6.2.12 (*)
#16 42.83 |         +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.83 |         +--- org.springframework:spring-aop:6.2.12 (*)
#16 42.83 |         +--- org.springframework:spring-core:6.2.12 (*)
#16 42.83 |         +--- org.springframework.security:spring-security-core:6.5.6 (*)
#16 42.83 |    \--- org.springframework.security:spring-security-web:6.5.6
#16 42.83 |    |    \--- org.springframework:spring-core:6.2.12 (*)
#16 42.83 |    |    +--- org.springframework:spring-context:6.2.12 (*)
#16 42.83 |    |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.83 |    |    +--- org.springframework:spring-aop:6.2.12 (*)
#16 42.83 |    |    |    \--- io.micrometer:micrometer-observation:1.14.12 -> 1.15.5 (*)
#16 42.83 |    |    |    +--- org.springframework:spring-expression:6.2.12 (*)
#16 42.83 |    |    |    +--- org.springframework:spring-core:6.2.12 (*)
#16 42.83 |    |    |    +--- org.springframework:spring-context:6.2.12 (*)
#16 42.83 |    |    |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.83 |    |    |    +--- org.springframework:spring-aop:6.2.12 (*)
#16 42.83 |    |    |    +--- org.springframework.security:spring-security-crypto:6.5.6
#16 42.83 |    |    +--- org.springframework.security:spring-security-core:6.5.6
#16 42.83 |    +--- org.springframework.security:spring-security-config:6.5.6
#16 42.83 |    +--- org.springframework:spring-aop:6.2.12 (*)
#16 42.83 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 42.83 +--- org.springframework.boot:spring-boot-starter-security -> 3.5.7
#16 42.83 |         \--- org.aspectj:aspectjweaver:1.9.22.1 -> 1.9.24
#16 42.83 |    \--- org.springframework:spring-aspects:6.2.12
#16 42.83 |    |    \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
#16 42.83 |    |    +--- jakarta.annotation:jakarta.annotation-api:2.0.0 -> 2.1.1
#16 42.83 |    |    +--- org.antlr:antlr4-runtime:4.13.0
#16 42.83 |    |    +--- org.springframework:spring-core:6.2.12 (*)
#16 42.83 |    |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.83 |    |    +--- org.springframework:spring-tx:6.2.12 (*)
#16 42.83 |    |    +--- org.springframework:spring-aop:6.2.12 (*)
#16 42.83 |    |    +--- org.springframework:spring-context:6.2.12 (*)
#16 42.83 |    |    |    \--- org.springframework:spring-tx:6.2.12 (*)
#16 42.83 |    |    |    +--- org.springframework:spring-jdbc:6.2.12 (*)
#16 42.83 |    |    |    +--- org.springframework:spring-core:6.2.12 (*)
#16 42.83 |    |    |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.83 |    |    +--- org.springframework:spring-orm:6.2.12
#16 42.83 |    |    |    \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
#16 42.83 |    |    |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.83 |    |    |    +--- org.springframework:spring-core:6.2.12 (*)
#16 42.83 |    |    +--- org.springframework.data:spring-data-commons:3.5.5
#16 42.83 |    +--- org.springframework.data:spring-data-jpa:3.5.5
#16 42.83 |    |    \--- org.antlr:antlr4-runtime:4.13.0
#16 42.83 |    |    +--- jakarta.inject:jakarta.inject-api:2.0.1
#16 42.83 |    |    |         \--- com.sun.istack:istack-commons-runtime:4.1.2
#16 42.83 |    |    |         +--- org.glassfish.jaxb:txw2:4.0.6
#16 42.83 |    |    |         |    \--- jakarta.activation:jakarta.activation-api:2.1.4
#16 42.83 |    |    |         +--- org.eclipse.angus:angus-activation:2.0.3
#16 42.83 |    |    |         +--- jakarta.activation:jakarta.activation-api:2.1.4
#16 42.83 |    |    |         +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.4 (*)
#16 42.83 |    |    |    \--- org.glassfish.jaxb:jaxb-core:4.0.6
#16 42.83 |    |    +--- org.glassfish.jaxb:jaxb-runtime:4.0.2 -> 4.0.6
#16 42.83 |    |    |    \--- jakarta.activation:jakarta.activation-api:2.1.4
#16 42.83 |    |    +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.0 -> 4.0.4
#16 42.83 |    |    +--- net.bytebuddy:byte-buddy:1.15.11 -> 1.17.8
#16 42.83 |    |    +--- com.fasterxml:classmate:1.5.1 -> 1.7.1
#16 42.83 |    |    +--- io.smallrye:jandex:3.2.0
#16 42.83 |    |    +--- org.hibernate.common:hibernate-commons-annotations:7.0.3.Final
#16 42.83 |    |    +--- org.jboss.logging:jboss-logging:3.5.0.Final -> 3.6.1.Final
#16 42.83 |    |    +--- jakarta.transaction:jakarta.transaction-api:2.0.1
#16 42.83 |    |    +--- jakarta.persistence:jakarta.persistence-api:3.1.0
#16 42.83 |    +--- org.hibernate.orm:hibernate-core:6.6.33.Final
#16 42.83 |    |              \--- org.springframework:spring-core:6.2.12 (*)
#16 42.83 |    |              +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.83 |    |         \--- org.springframework:spring-tx:6.2.12
#16 42.83 |    |         +--- org.springframework:spring-core:6.2.12 (*)
#16 42.83 |    |         +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.83 |    |    \--- org.springframework:spring-jdbc:6.2.12
#16 42.83 |    |    |    \--- org.slf4j:slf4j-api:2.0.17
#16 42.83 |    |    +--- com.zaxxer:HikariCP:6.3.3
#16 42.83 |    |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 42.83 |    +--- org.springframework.boot:spring-boot-starter-jdbc:3.5.7
#16 42.83 |    |    \--- org.yaml:snakeyaml:2.4
#16 42.83 |    |    +--- org.springframework:spring-core:6.2.12 (*)
#16 42.83 |    |    +--- jakarta.annotation:jakarta.annotation-api:2.1.1
#16 42.83 |    |    |         \--- org.slf4j:slf4j-api:2.0.17
#16 42.83 |    |    |    \--- org.slf4j:jul-to-slf4j:2.0.17
#16 42.83 |    |    |    |    \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
#16 42.83 |    |    |    |    +--- org.apache.logging.log4j:log4j-api:2.24.3
#16 42.83 |    |    |    +--- org.apache.logging.log4j:log4j-to-slf4j:2.24.3
#16 42.83 |    |    |    |    \--- org.slf4j:slf4j-api:2.0.17
#16 42.83 |    |    |    |    +--- ch.qos.logback:logback-core:1.5.20
#16 42.83 |    |    |    +--- ch.qos.logback:logback-classic:1.5.20
#16 42.83 |    |    +--- org.springframework.boot:spring-boot-starter-logging:3.5.7
#16 42.83 |    |    +--- org.springframework.boot:spring-boot-autoconfigure:3.5.7 (*)
#16 42.83 |    |    +--- org.springframework.boot:spring-boot:3.5.7 (*)
#16 42.83 |    +--- org.springframework.boot:spring-boot-starter:3.5.7
#16 42.83 +--- org.springframework.boot:spring-boot-starter-data-jpa -> 3.5.7
#16 42.83 |         \--- org.springframework.boot:spring-boot:3.5.7 (*)
#16 42.83 |    \--- org.springframework.boot:spring-boot-autoconfigure:3.5.7
#16 42.83 |    |              \--- io.micrometer:micrometer-commons:1.15.5
#16 42.83 |    |         \--- io.micrometer:micrometer-observation:1.14.12 -> 1.15.5
#16 42.83 |    |         |    \--- org.springframework:spring-core:6.2.12 (*)
#16 42.83 |    |         +--- org.springframework:spring-expression:6.2.12
#16 42.83 |    |         +--- org.springframework:spring-core:6.2.12 (*)
#16 42.83 |    |         +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.83 |    |         |    \--- org.springframework:spring-core:6.2.12 (*)
#16 42.83 |    |         |    |    \--- org.springframework:spring-core:6.2.12 (*)
#16 42.83 |    |         |    +--- org.springframework:spring-beans:6.2.12
#16 42.83 |    |         +--- org.springframework:spring-aop:6.2.12
#16 42.83 |    |    \--- org.springframework:spring-context:6.2.12
#16 42.83 |    |    |    \--- org.springframework:spring-jcl:6.2.12
#16 42.83 |    |    +--- org.springframework:spring-core:6.2.12
#16 42.83 |    +--- org.springframework.boot:spring-boot:3.5.7
#16 42.83 +--- org.springframework.boot:spring-boot-devtools -> 3.5.7
#16 42.83 runtimeClasspath - Runtime classpath of source set 'main'.
#16 42.83
#16 42.83 \--- com.sun.istack:istack-commons-runtime:{strictly 4.1.2} -> 4.1.2 (c)
#16 42.83 +--- org.glassfish.jaxb:txw2:{strictly 4.0.6} -> 4.0.6 (c)
#16 42.83 +--- org.eclipse.angus:angus-activation:{strictly 2.0.3} -> 2.0.3 (c)
#16 42.83 +--- org.apache.logging.log4j:log4j-api:{strictly 2.24.3} -> 2.24.3 (c)
#16 42.83 +--- ch.qos.logback:logback-core:{strictly 1.5.20} -> 1.5.20 (c)
#16 42.83 +--- org.reactivestreams:reactive-streams:{strictly 1.0.4} -> 1.0.4 (c)
#16 42.83 +--- io.netty:netty-codec:{strictly 4.1.128.Final} -> 4.1.128.Final (c)
#16 42.83 +--- io.netty:netty-transport-native-unix-common:{strictly 4.1.128.Final} -> 4.1.128.Final (c)
#16 42.83 +--- io.netty:netty-buffer:{strictly 4.1.128.Final} -> 4.1.128.Final (c)
#16 42.83 +--- io.netty:netty-resolver:{strictly 4.1.128.Final} -> 4.1.128.Final (c)
#16 42.83 +--- io.micrometer:micrometer-commons:{strictly 1.15.5} -> 1.15.5 (c)
#16 42.83 +--- com.fasterxml.jackson:jackson-bom:{strictly 2.19.2} -> 2.19.2 (c)
#16 42.83 +--- com.fasterxml.jackson.core:jackson-core:{strictly 2.19.2} -> 2.19.2 (c)
#16 42.83 +--- com.fasterxml.jackson.core:jackson-annotations:{strictly 2.19.2} -> 2.19.2 (c)
#16 42.83 +--- org.unbescape:unbescape:{strictly 1.1.6.RELEASE} -> 1.1.6.RELEASE (c)
#16 42.83 +--- org.attoparser:attoparser:{strictly 2.0.7.RELEASE} -> 2.0.7.RELEASE (c)
#16 42.83 +--- org.springframework.security:spring-security-crypto:{strictly 6.5.6} -> 6.5.6 (c)
#16 42.83 +--- org.glassfish.jaxb:jaxb-core:{strictly 4.0.6} -> 4.0.6 (c)
#16 42.83 +--- jakarta.activation:jakarta.activation-api:{strictly 2.1.4} -> 2.1.4 (c)
#16 42.83 +--- org.springframework:spring-jcl:{strictly 6.2.12} -> 6.2.12 (c)
#16 42.83 +--- org.slf4j:jul-to-slf4j:{strictly 2.0.17} -> 2.0.17 (c)
#16 42.83 +--- org.apache.logging.log4j:log4j-to-slf4j:{strictly 2.24.3} -> 2.24.3 (c)
#16 42.83 +--- ch.qos.logback:logback-classic:{strictly 1.5.20} -> 1.5.20 (c)
#16 42.83 +--- org.apache.httpcomponents.core5:httpcore5-h2:{strictly 5.3.6} -> 5.3.6 (c)
#16 42.83 +--- org.springframework:spring-oxm:{strictly 6.2.12} -> 6.2.12 (c)
#16 42.83 +--- org.springframework.data:spring-data-keyvalue:{strictly 3.5.5} -> 3.5.5 (c)
#16 42.83 +--- io.projectreactor:reactor-core:{strictly 3.7.12} -> 3.7.12 (c)
#16 42.83 +--- io.netty:netty-transport:{strictly 4.1.128.Final} -> 4.1.128.Final (c)
#16 42.83 +--- io.netty:netty-handler:{strictly 4.1.128.Final} -> 4.1.128.Final (c)
#16 42.83 +--- io.netty:netty-common:{strictly 4.1.128.Final} -> 4.1.128.Final (c)
#16 42.83 +--- redis.clients.authentication:redis-authx-core:{strictly 0.1.1-beta2} -> 0.1.1-beta2 (c)
#16 42.83 +--- io.micrometer:micrometer-observation:{strictly 1.15.5} -> 1.15.5 (c)
#16 42.83 +--- org.apache.tomcat.embed:tomcat-embed-websocket:{strictly 10.1.48} -> 10.1.48 (c)
#16 42.83 +--- org.apache.tomcat.embed:tomcat-embed-core:{strictly 10.1.48} -> 10.1.48 (c)
#16 42.83 +--- com.fasterxml.jackson.module:jackson-module-parameter-names:{strictly 2.19.2} -> 2.19.2 (c)
#16 42.83 +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:{strictly 2.19.2} -> 2.19.2 (c)
#16 42.83 +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:{strictly 2.19.2} -> 2.19.2 (c)
#16 42.83 +--- com.fasterxml.jackson.core:jackson-databind:{strictly 2.19.2} -> 2.19.2 (c)
#16 42.83 +--- jakarta.validation:jakarta.validation-api:{strictly 3.0.2} -> 3.0.2 (c)
#16 42.83 +--- org.thymeleaf:thymeleaf:{strictly 3.1.3.RELEASE} -> 3.1.3.RELEASE (c)
#16 42.83 +--- org.springframework:spring-expression:{strictly 6.2.12} -> 6.2.12 (c)
#16 42.83 +--- org.springframework.security:spring-security-core:{strictly 6.5.6} -> 6.5.6 (c)
#16 42.83 +--- org.springframework:spring-beans:{strictly 6.2.12} -> 6.2.12 (c)
#16 42.83 +--- org.springframework:spring-tx:{strictly 6.2.12} -> 6.2.12 (c)
#16 42.83 +--- org.springframework:spring-context:{strictly 6.2.12} -> 6.2.12 (c)
#16 42.83 +--- org.springframework:spring-orm:{strictly 6.2.12} -> 6.2.12 (c)
#16 42.83 +--- org.springframework.data:spring-data-commons:{strictly 3.5.5} -> 3.5.5 (c)
#16 42.83 +--- org.antlr:antlr4-runtime:{strictly 4.13.0} -> 4.13.0 (c)
#16 42.83 +--- jakarta.inject:jakarta.inject-api:{strictly 2.0.1} -> 2.0.1 (c)
#16 42.83 +--- org.glassfish.jaxb:jaxb-runtime:{strictly 4.0.6} -> 4.0.6 (c)
#16 42.83 +--- jakarta.xml.bind:jakarta.xml.bind-api:{strictly 4.0.4} -> 4.0.4 (c)
#16 42.83 +--- net.bytebuddy:byte-buddy:{strictly 1.17.8} -> 1.17.8 (c)
#16 42.83 +--- com.fasterxml:classmate:{strictly 1.7.1} -> 1.7.1 (c)
#16 42.83 +--- io.smallrye:jandex:{strictly 3.2.0} -> 3.2.0 (c)
#16 42.83 +--- org.hibernate.common:hibernate-commons-annotations:{strictly 7.0.3.Final} -> 7.0.3.Final (c)
#16 42.83 +--- org.jboss.logging:jboss-logging:{strictly 3.6.1.Final} -> 3.6.1.Final (c)
#16 42.83 +--- jakarta.transaction:jakarta.transaction-api:{strictly 2.0.1} -> 2.0.1 (c)
#16 42.83 +--- jakarta.persistence:jakarta.persistence-api:{strictly 3.1.0} -> 3.1.0 (c)
#16 42.83 +--- org.springframework:spring-jdbc:{strictly 6.2.12} -> 6.2.12 (c)
#16 42.83 +--- com.zaxxer:HikariCP:{strictly 6.3.3} -> 6.3.3 (c)
#16 42.83 +--- org.yaml:snakeyaml:{strictly 2.4} -> 2.4 (c)
#16 42.83 +--- org.springframework:spring-core:{strictly 6.2.12} -> 6.2.12 (c)
#16 42.83 +--- jakarta.annotation:jakarta.annotation-api:{strictly 2.1.1} -> 2.1.1 (c)
#16 42.83 +--- org.springframework.boot:spring-boot-starter-logging:{strictly 3.5.7} -> 3.5.7 (c)
#16 42.83 +--- org.springframework.boot:spring-boot-autoconfigure:{strictly 3.5.7} -> 3.5.7 (c)
#16 42.83 +--- org.springframework.boot:spring-boot:{strictly 3.5.7} -> 3.5.7 (c)
#16 42.83 +--- org.checkerframework:checker-qual:{strictly 3.49.5} -> 3.49.5 (c)
#16 42.83 +--- io.github.openfeign.querydsl:querydsl-core:{strictly 7.0} -> 7.0 (c)
#16 42.83 +--- org.apache.httpcomponents.core5:httpcore5:{strictly 5.3.6} -> 5.3.6 (c)
#16 42.83 +--- org.apache.httpcomponents.client5:httpclient5:{strictly 5.5.1} -> 5.5.1 (c)
#16 42.83 +--- org.apache.commons:commons-lang3:{strictly 3.18.0} -> 3.18.0 (c)
#16 42.83 +--- com.cloudinary:cloudinary-core:{strictly 2.3.0} -> 2.3.0 (c)
#16 42.83 +--- org.springframework:spring-context-support:{strictly 6.2.12} -> 6.2.12 (c)
#16 42.83 +--- org.springframework.data:spring-data-redis:{strictly 3.5.5} -> 3.5.5 (c)
#16 42.83 +--- io.lettuce:lettuce-core:{strictly 6.6.0.RELEASE} -> 6.6.0.RELEASE (c)
#16 42.83 +--- org.slf4j:slf4j-api:{strictly 2.0.17} -> 2.0.17 (c)
#16 42.83 +--- org.aspectj:aspectjweaver:{strictly 1.9.24} -> 1.9.24 (c)
#16 42.83 +--- org.springframework:spring-webmvc:{strictly 6.2.12} -> 6.2.12 (c)
#16 42.83 +--- org.springframework:spring-web:{strictly 6.2.12} -> 6.2.12 (c)
#16 42.83 +--- org.springframework.boot:spring-boot-starter-tomcat:{strictly 3.5.7} -> 3.5.7 (c)
#16 42.83 +--- org.springframework.boot:spring-boot-starter-json:{strictly 3.5.7} -> 3.5.7 (c)
#16 42.83 +--- org.hibernate.validator:hibernate-validator:{strictly 8.0.3.Final} -> 8.0.3.Final (c)
#16 42.83 +--- org.apache.tomcat.embed:tomcat-embed-el:{strictly 10.1.48} -> 10.1.48 (c)
#16 42.83 +--- org.thymeleaf:thymeleaf-spring6:{strictly 3.1.3.RELEASE} -> 3.1.3.RELEASE (c)
#16 42.83 +--- org.springframework.security:spring-security-web:{strictly 6.5.6} -> 6.5.6 (c)
#16 42.83 +--- org.springframework.security:spring-security-config:{strictly 6.5.6} -> 6.5.6 (c)
#16 42.83 +--- org.springframework:spring-aop:{strictly 6.2.12} -> 6.2.12 (c)
#16 42.83 +--- org.springframework.data:spring-data-jpa:{strictly 3.5.5} -> 3.5.5 (c)
#16 42.83 +--- org.hibernate.orm:hibernate-core:{strictly 6.6.33.Final} -> 6.6.33.Final (c)
#16 42.83 +--- org.springframework.boot:spring-boot-starter-jdbc:{strictly 3.5.7} -> 3.5.7 (c)
#16 42.83 +--- org.springframework.boot:spring-boot-starter:{strictly 3.5.7} -> 3.5.7 (c)
#16 42.83 +--- org.postgresql:postgresql:{strictly 42.7.8} -> 42.7.8 (c)
#16 42.83 +--- com.h2database:h2:{strictly 2.3.232} -> 2.3.232 (c)
#16 42.83 +--- io.github.openfeign.querydsl:querydsl-jpa:{strictly 7.0} -> 7.0 (c)
#16 42.83 +--- com.cloudinary:cloudinary-http5:{strictly 2.3.0} -> 2.3.0 (c)
#16 42.83 +--- net.coobird:thumbnailator:{strictly 0.4.19} -> 0.4.19 (c)
#16 42.83 +--- org.springframework.boot:spring-boot-starter-cache:{strictly 3.5.7} -> 3.5.7 (c)
#16 42.83 +--- org.springframework.boot:spring-boot-starter-data-redis:{strictly 3.5.7} -> 3.5.7 (c)
#16 42.83 +--- org.thymeleaf.extras:thymeleaf-extras-springsecurity6:{strictly 3.1.3.RELEASE} -> 3.1.3.RELEASE (c)
#16 42.83 +--- org.springframework.retry:spring-retry:{strictly 2.0.12} -> 2.0.12 (c)
#16 42.83 +--- org.springframework.boot:spring-boot-starter-web:{strictly 3.5.7} -> 3.5.7 (c)
#16 42.83 +--- org.springframework.boot:spring-boot-starter-validation:{strictly 3.5.7} -> 3.5.7 (c)
#16 42.83 +--- org.springframework.boot:spring-boot-starter-thymeleaf:{strictly 3.5.7} -> 3.5.7 (c)
#16 42.83 +--- org.springframework.boot:spring-boot-starter-security:{strictly 3.5.7} -> 3.5.7 (c)
#16 42.83 +--- org.springframework:spring-aspects:{strictly 6.2.12} -> 6.2.12 (c)
#16 42.83 +--- org.springframework.boot:spring-boot-starter-data-jpa:{strictly 3.5.7} -> 3.5.7 (c)
#16 42.83 |    \--- org.checkerframework:checker-qual:3.49.5
#16 42.83 +--- org.postgresql:postgresql -> 42.7.8
#16 42.83 +--- com.h2database:h2 -> 2.3.232
#16 42.83 |         \--- io.projectreactor:reactor-core:3.7.6 -> 3.7.12 (*)
#16 42.83 |    \--- io.github.openfeign.querydsl:querydsl-core:7.0
#16 42.83 +--- io.github.openfeign.querydsl:querydsl-jpa:7.0
#16 42.83 |    \--- org.apache.httpcomponents.core5:httpcore5:5.2.5 -> 5.3.6
#16 42.83 |    |    \--- org.slf4j:slf4j-api:1.7.36 -> 2.0.17
#16 42.83 |    |    |    \--- org.apache.httpcomponents.core5:httpcore5:5.3.6
#16 42.83 |    |    +--- org.apache.httpcomponents.core5:httpcore5-h2:5.3.6
#16 42.83 |    |    +--- org.apache.httpcomponents.core5:httpcore5:5.3.6
#16 42.83 |    +--- org.apache.httpcomponents.client5:httpclient5:5.3.1 -> 5.5.1
#16 42.83 |    +--- org.apache.commons:commons-lang3:3.1 -> 3.18.0
#16 42.83 |    +--- com.cloudinary:cloudinary-core:2.3.0
#16 42.83 +--- com.cloudinary:cloudinary-http5:2.3.0
#16 42.83 +--- net.coobird:thumbnailator:0.4.19
#16 42.83 |    \--- org.springframework:spring-context-support:6.2.12 (*)
#16 42.83 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 42.83 +--- org.springframework.boot:spring-boot-starter-cache -> 3.5.7
#16 42.83 |         \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
#16 42.83 |         |    \--- org.springframework:spring-core:6.2.12 (*)
#16 42.83 |         |    +--- org.springframework:spring-context:6.2.12 (*)
#16 42.83 |         |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.83 |         +--- org.springframework:spring-context-support:6.2.12
#16 42.83 |         +--- org.springframework:spring-aop:6.2.12 (*)
#16 42.83 |         |    \--- org.springframework:spring-core:6.2.12 (*)
#16 42.83 |         |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.83 |         |    +--- jakarta.xml.bind:jakarta.xml.bind-api:3.0.1 -> 4.0.4 (*)
#16 42.83 |         +--- org.springframework:spring-oxm:6.2.12
#16 42.83 |         +--- org.springframework:spring-tx:6.2.12 (*)
#16 42.83 |         |    \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
#16 42.83 |         |    +--- org.springframework:spring-tx:6.2.12 (*)
#16 42.83 |         |    +--- org.springframework:spring-context:6.2.12 (*)
#16 42.83 |         |    +--- org.springframework.data:spring-data-commons:3.5.5 (*)
#16 42.83 |         +--- org.springframework.data:spring-data-keyvalue:3.5.5
#16 42.83 |    \--- org.springframework.data:spring-data-redis:3.5.5
#16 42.83 |    |         \--- org.reactivestreams:reactive-streams:1.0.4
#16 42.83 |    |    \--- io.projectreactor:reactor-core:3.6.6 -> 3.7.12
#16 42.83 |    |    +--- io.netty:netty-transport:4.1.118.Final -> 4.1.128.Final (*)
#16 42.83 |    |    |         \--- io.netty:netty-transport:4.1.128.Final (*)
#16 42.83 |    |    |         +--- io.netty:netty-buffer:4.1.128.Final (*)
#16 42.83 |    |    |         +--- io.netty:netty-common:4.1.128.Final
#16 42.83 |    |    |    \--- io.netty:netty-codec:4.1.128.Final
#16 42.83 |    |    |    |    \--- io.netty:netty-transport:4.1.128.Final (*)
#16 42.83 |    |    |    |    +--- io.netty:netty-buffer:4.1.128.Final (*)
#16 42.83 |    |    |    |    +--- io.netty:netty-common:4.1.128.Final
#16 42.83 |    |    |    +--- io.netty:netty-transport-native-unix-common:4.1.128.Final
#16 42.83 |    |    |    |    \--- io.netty:netty-resolver:4.1.128.Final (*)
#16 42.83 |    |    |    |    +--- io.netty:netty-buffer:4.1.128.Final (*)
#16 42.83 |    |    |    |    +--- io.netty:netty-common:4.1.128.Final
#16 42.83 |    |    |    +--- io.netty:netty-transport:4.1.128.Final
#16 42.83 |    |    |    |    \--- io.netty:netty-common:4.1.128.Final
#16 42.83 |    |    |    +--- io.netty:netty-buffer:4.1.128.Final
#16 42.83 |    |    |    |    \--- io.netty:netty-common:4.1.128.Final
#16 42.83 |    |    |    +--- io.netty:netty-resolver:4.1.128.Final
#16 42.83 |    |    |    +--- io.netty:netty-common:4.1.128.Final
#16 42.83 |    |    +--- io.netty:netty-handler:4.1.118.Final -> 4.1.128.Final
#16 42.83 |    |    +--- io.netty:netty-common:4.1.118.Final -> 4.1.128.Final
#16 42.83 |    |    |    \--- org.slf4j:slf4j-api:1.7.36 -> 2.0.17
#16 42.83 |    |    +--- redis.clients.authentication:redis-authx-core:0.1.1-beta2
#16 42.83 |    +--- io.lettuce:lettuce-core:6.6.0.RELEASE
#16 42.83 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 42.83 +--- org.springframework.boot:spring-boot-starter-data-redis -> 3.5.7
#16 42.83 |    \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
#16 42.83 |    +--- org.thymeleaf:thymeleaf-spring6:3.1.3.RELEASE (*)
#16 42.83 +--- org.thymeleaf.extras:thymeleaf-extras-springsecurity6 -> 3.1.3.RELEASE
#16 42.83 +--- org.springframework:spring-aspects -> 6.2.12 (*)
#16 42.83 +--- org.springframework.retry:spring-retry -> 2.0.12
#16 42.83 |         \--- org.springframework:spring-web:6.2.12 (*)
#16 42.83 |         +--- org.springframework:spring-expression:6.2.12 (*)
#16 42.83 |         +--- org.springframework:spring-core:6.2.12 (*)
#16 42.83 |         +--- org.springframework:spring-context:6.2.12 (*)
#16 42.83 |         +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.83 |         +--- org.springframework:spring-aop:6.2.12 (*)
#16 42.83 |    \--- org.springframework:spring-webmvc:6.2.12
#16 42.83 |    +--- org.springframework:spring-web:6.2.12 (*)
#16 42.83 |    |         \--- org.apache.tomcat.embed:tomcat-embed-core:10.1.48
#16 42.83 |    |    \--- org.apache.tomcat.embed:tomcat-embed-websocket:10.1.48
#16 42.83 |    |    +--- org.apache.tomcat.embed:tomcat-embed-el:10.1.48
#16 42.83 |    |    +--- org.apache.tomcat.embed:tomcat-embed-core:10.1.48
#16 42.83 |    |    +--- jakarta.annotation:jakarta.annotation-api:2.1.1
#16 42.83 |    +--- org.springframework.boot:spring-boot-starter-tomcat:3.5.7
#16 42.83 |    |         \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
#16 42.83 |    |         +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (*)
#16 42.83 |    |         +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (*)
#16 42.83 |    |    \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.19.2
#16 42.83 |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
#16 42.83 |    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (*)
#16 42.83 |    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (*)
#16 42.83 |    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.19.2 (*)
#16 42.83 |    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.2
#16 42.83 |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
#16 42.83 |    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (*)
#16 42.83 |    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (*)
#16 42.83 |    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.19.2
#16 42.83 |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
#16 42.83 |    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
#16 42.83 |    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.19.2
#16 42.83 |    |    |    |         \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.19.2 (c)
#16 42.83 |    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.2 (c)
#16 42.83 |    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.19.2 (c)
#16 42.83 |    |    |    |         +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (c)
#16 42.83 |    |    |    |         +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (c)
#16 42.83 |    |    |    |         +--- com.fasterxml.jackson.core:jackson-annotations:2.19.2 (c)
#16 42.83 |    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2
#16 42.83 |    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.19.2
#16 42.83 |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.19.2
#16 42.83 |    |    +--- org.springframework:spring-web:6.2.12 (*)
#16 42.83 |    |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 42.83 |    +--- org.springframework.boot:spring-boot-starter-json:3.5.7
#16 42.83 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 42.83 +--- org.springframework.boot:spring-boot-starter-web -> 3.5.7
#16 42.83 |         \--- com.fasterxml:classmate:1.5.1 -> 1.7.1
#16 42.83 |         +--- org.jboss.logging:jboss-logging:3.4.3.Final -> 3.6.1.Final
#16 42.83 |         +--- jakarta.validation:jakarta.validation-api:3.0.2
#16 42.83 |    \--- org.hibernate.validator:hibernate-validator:8.0.3.Final
#16 42.83 |    +--- org.apache.tomcat.embed:tomcat-embed-el:10.1.48
#16 42.83 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 42.83 +--- org.springframework.boot:spring-boot-starter-validation -> 3.5.7
#16 42.83 |         \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
#16 42.83 |         |    \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
#16 42.83 |         |    +--- org.unbescape:unbescape:1.1.6.RELEASE
#16 42.83 |         |    +--- org.attoparser:attoparser:2.0.7.RELEASE
#16 42.83 |         +--- org.thymeleaf:thymeleaf:3.1.3.RELEASE
#16 42.83 |    \--- org.thymeleaf:thymeleaf-spring6:3.1.3.RELEASE
#16 42.83 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 42.83 +--- org.springframework.boot:spring-boot-starter-thymeleaf -> 3.5.7
#16 42.83 |              \--- io.micrometer:micrometer-observation:1.14.12 -> 1.15.5 (*)
#16 42.83 |              +--- org.springframework:spring-core:6.2.12 (*)
#16 42.83 |              +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.83 |         \--- org.springframework:spring-web:6.2.12
#16 42.83 |         +--- org.springframework:spring-expression:6.2.12 (*)
#16 42.83 |         +--- org.springframework:spring-context:6.2.12 (*)
#16 42.83 |         +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.83 |         +--- org.springframework:spring-aop:6.2.12 (*)
#16 42.83 |         +--- org.springframework:spring-core:6.2.12 (*)
#16 42.83 |         +--- org.springframework.security:spring-security-core:6.5.6 (*)
#16 42.83 |    \--- org.springframework.security:spring-security-web:6.5.6
#16 42.83 |    |    \--- org.springframework:spring-core:6.2.12 (*)
#16 42.83 |    |    +--- org.springframework:spring-context:6.2.12 (*)
#16 42.83 |    |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.83 |    |    +--- org.springframework:spring-aop:6.2.12 (*)
#16 42.83 |    |    |    \--- io.micrometer:micrometer-observation:1.14.12 -> 1.15.5 (*)
#16 42.83 |    |    |    +--- org.springframework:spring-expression:6.2.12 (*)
#16 42.83 |    |    |    +--- org.springframework:spring-core:6.2.12 (*)
#16 42.83 |    |    |    +--- org.springframework:spring-context:6.2.12 (*)
#16 42.83 |    |    |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.83 |    |    |    +--- org.springframework:spring-aop:6.2.12 (*)
#16 42.83 |    |    |    +--- org.springframework.security:spring-security-crypto:6.5.6
#16 42.83 |    |    +--- org.springframework.security:spring-security-core:6.5.6
#16 42.83 |    +--- org.springframework.security:spring-security-config:6.5.6
#16 42.83 |    +--- org.springframework:spring-aop:6.2.12 (*)
#16 42.83 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 42.83 +--- org.springframework.boot:spring-boot-starter-security -> 3.5.7
#16 42.83 |         \--- org.aspectj:aspectjweaver:1.9.22.1 -> 1.9.24
#16 42.83 |    \--- org.springframework:spring-aspects:6.2.12
#16 42.83 |    |    \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
#16 42.83 |    |    +--- jakarta.annotation:jakarta.annotation-api:2.0.0 -> 2.1.1
#16 42.83 |    |    +--- org.antlr:antlr4-runtime:4.13.0
#16 42.83 |    |    +--- org.springframework:spring-core:6.2.12 (*)
#16 42.83 |    |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.83 |    |    +--- org.springframework:spring-tx:6.2.12 (*)
#16 42.83 |    |    +--- org.springframework:spring-aop:6.2.12 (*)
#16 42.83 |    |    +--- org.springframework:spring-context:6.2.12 (*)
#16 42.83 |    |    |    \--- org.springframework:spring-tx:6.2.12 (*)
#16 42.83 |    |    |    +--- org.springframework:spring-jdbc:6.2.12 (*)
#16 42.83 |    |    |    +--- org.springframework:spring-core:6.2.12 (*)
#16 42.83 |    |    |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.83 |    |    +--- org.springframework:spring-orm:6.2.12
#16 42.83 |    |    |    \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
#16 42.83 |    |    |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.83 |    |    |    +--- org.springframework:spring-core:6.2.12 (*)
#16 42.83 |    |    +--- org.springframework.data:spring-data-commons:3.5.5
#16 42.83 |    +--- org.springframework.data:spring-data-jpa:3.5.5
#16 42.83 |    |    \--- org.antlr:antlr4-runtime:4.13.0
#16 42.83 |    |    +--- jakarta.inject:jakarta.inject-api:2.0.1
#16 42.83 |    |    |         \--- com.sun.istack:istack-commons-runtime:4.1.2
#16 42.83 |    |    |         +--- org.glassfish.jaxb:txw2:4.0.6
#16 42.83 |    |    |         |    \--- jakarta.activation:jakarta.activation-api:2.1.4
#16 42.83 |    |    |         +--- org.eclipse.angus:angus-activation:2.0.3
#16 42.83 |    |    |         +--- jakarta.activation:jakarta.activation-api:2.1.4
#16 42.83 |    |    |         +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.4 (*)
#16 42.83 |    |    |    \--- org.glassfish.jaxb:jaxb-core:4.0.6
#16 42.83 |    |    +--- org.glassfish.jaxb:jaxb-runtime:4.0.2 -> 4.0.6
#16 42.83 |    |    |    \--- jakarta.activation:jakarta.activation-api:2.1.4
#16 42.83 |    |    +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.0 -> 4.0.4
#16 42.83 |    |    +--- net.bytebuddy:byte-buddy:1.15.11 -> 1.17.8
#16 42.83 |    |    +--- com.fasterxml:classmate:1.5.1 -> 1.7.1
#16 42.83 |    |    +--- io.smallrye:jandex:3.2.0
#16 42.83 |    |    +--- org.hibernate.common:hibernate-commons-annotations:7.0.3.Final
#16 42.83 |    |    +--- org.jboss.logging:jboss-logging:3.5.0.Final -> 3.6.1.Final
#16 42.83 |    |    +--- jakarta.transaction:jakarta.transaction-api:2.0.1
#16 42.83 |    |    +--- jakarta.persistence:jakarta.persistence-api:3.1.0
#16 42.83 |    +--- org.hibernate.orm:hibernate-core:6.6.33.Final
#16 42.83 |    |              \--- org.springframework:spring-core:6.2.12 (*)
#16 42.83 |    |              +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.83 |    |         \--- org.springframework:spring-tx:6.2.12
#16 42.83 |    |         +--- org.springframework:spring-core:6.2.12 (*)
#16 42.83 |    |         +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.83 |    |    \--- org.springframework:spring-jdbc:6.2.12
#16 42.83 |    |    |    \--- org.slf4j:slf4j-api:2.0.17
#16 42.83 |    |    +--- com.zaxxer:HikariCP:6.3.3
#16 42.83 |    |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 42.83 |    +--- org.springframework.boot:spring-boot-starter-jdbc:3.5.7
#16 42.83 |    |    \--- org.yaml:snakeyaml:2.4
#16 42.83 |    |    +--- org.springframework:spring-core:6.2.12 (*)
#16 42.83 |    |    +--- jakarta.annotation:jakarta.annotation-api:2.1.1
#16 42.83 |    |    |         \--- org.slf4j:slf4j-api:2.0.17
#16 42.83 |    |    |    \--- org.slf4j:jul-to-slf4j:2.0.17
#16 42.83 |    |    |    |    \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
#16 42.83 |    |    |    |    +--- org.apache.logging.log4j:log4j-api:2.24.3
#16 42.83 |    |    |    +--- org.apache.logging.log4j:log4j-to-slf4j:2.24.3
#16 42.83 |    |    |    |    \--- org.slf4j:slf4j-api:2.0.17
#16 42.83 |    |    |    |    +--- ch.qos.logback:logback-core:1.5.20
#16 42.83 |    |    |    +--- ch.qos.logback:logback-classic:1.5.20
#16 42.83 |    |    +--- org.springframework.boot:spring-boot-starter-logging:3.5.7
#16 42.83 |    |    |    \--- org.springframework.boot:spring-boot:3.5.7 (*)
#16 42.83 |    |    +--- org.springframework.boot:spring-boot-autoconfigure:3.5.7
#16 42.83 |    |    |              \--- io.micrometer:micrometer-commons:1.15.5
#16 42.83 |    |    |         \--- io.micrometer:micrometer-observation:1.14.12 -> 1.15.5
#16 42.83 |    |    |         |    \--- org.springframework:spring-core:6.2.12 (*)
#16 42.83 |    |    |         +--- org.springframework:spring-expression:6.2.12
#16 42.83 |    |    |         +--- org.springframework:spring-core:6.2.12 (*)
#16 42.83 |    |    |         +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.83 |    |    |         |    \--- org.springframework:spring-core:6.2.12 (*)
#16 42.83 |    |    |         |    |    \--- org.springframework:spring-core:6.2.12 (*)
#16 42.82 |    |    |         |    +--- org.springframework:spring-beans:6.2.12
#16 42.82 |    |    |         +--- org.springframework:spring-aop:6.2.12
#16 42.82 |    |    |    \--- org.springframework:spring-context:6.2.12
#16 42.82 |    |    |    |    \--- org.springframework:spring-jcl:6.2.12
#16 42.82 |    |    |    +--- org.springframework:spring-core:6.2.12
#16 42.82 |    |    +--- org.springframework.boot:spring-boot:3.5.7
#16 42.82 |    +--- org.springframework.boot:spring-boot-starter:3.5.7
#16 42.82 +--- org.springframework.boot:spring-boot-starter-data-jpa -> 3.5.7
#16 42.46 productionRuntimeClasspath
#16 42.46
#16 42.46 No dependencies
#16 42.46 mainSourceElements - List of source directories contained in the Main SourceSet. (n)
#16 42.46
#16 42.46 \--- io.github.openfeign.querydsl:querydsl-jpa:7.0 (n)
#16 42.46 +--- com.cloudinary:cloudinary-http5:2.3.0 (n)
#16 42.46 +--- net.coobird:thumbnailator:0.4.19 (n)
#16 42.46 +--- org.springframework.boot:spring-boot-starter-cache (n)
#16 42.46 +--- org.springframework.boot:spring-boot-starter-data-redis (n)
#16 42.46 +--- org.thymeleaf.extras:thymeleaf-extras-springsecurity6 (n)
#16 42.46 +--- org.springframework:spring-aspects (n)
#16 42.46 +--- org.springframework.retry:spring-retry (n)
#16 42.46 +--- org.springframework.boot:spring-boot-starter-web (n)
#16 42.46 +--- org.springframework.boot:spring-boot-starter-validation (n)
#16 42.46 +--- org.springframework.boot:spring-boot-starter-thymeleaf (n)
#16 42.46 +--- org.springframework.boot:spring-boot-starter-security (n)
#16 42.46 +--- org.springframework.boot:spring-boot-starter-data-jpa (n)
#16 42.46 implementation - Implementation dependencies for the 'main' feature. (n)
#16 42.46
#16 42.46           \--- org.springframework.boot:spring-boot:3.5.7 (*)
#16 42.46      \--- org.springframework.boot:spring-boot-autoconfigure:3.5.7
#16 42.46      |              \--- io.micrometer:micrometer-commons:1.15.5
#16 42.46      |         \--- io.micrometer:micrometer-observation:1.14.12 -> 1.15.5
#16 42.46      |         |    \--- org.springframework:spring-core:6.2.12 (*)
#16 42.46      |         +--- org.springframework:spring-expression:6.2.12
#16 42.46      |         +--- org.springframework:spring-core:6.2.12 (*)
#16 42.46      |         +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.46      |         |    \--- org.springframework:spring-core:6.2.12 (*)
#16 42.46      |         |    |    \--- org.springframework:spring-core:6.2.12 (*)
#16 42.46      |         |    +--- org.springframework:spring-beans:6.2.12
#16 42.36      |         +--- org.springframework:spring-aop:6.2.12
#16 42.36      |    \--- org.springframework:spring-context:6.2.12
#16 42.36      |    |    \--- org.springframework:spring-jcl:6.2.12
#16 42.36      |    +--- org.springframework:spring-core:6.2.12
#16 42.36      +--- org.springframework.boot:spring-boot:3.5.7
#16 42.36 \--- org.springframework.boot:spring-boot-devtools -> 3.5.7
#16 42.22 developmentOnly - Configuration for development-only dependencies such as Spring Boot's DevTools.
#16 42.22
#16 42.22 No dependencies
#16 42.22 default - Configuration for default artifacts. (n)
#16 42.22
#16 42.22 \--- org.projectlombok:lombok (n)
#16 42.14 compileOnly - Compile-only dependencies for the 'main' feature. (n)
#16 42.14
#16 42.14      \--- io.github.openfeign.querydsl:querydsl-core:7.0 (*)
#16 42.14 \--- io.github.openfeign.querydsl:querydsl-jpa:7.0
#16 42.14 |    \--- org.apache.httpcomponents.core5:httpcore5:5.2.5 -> 5.3.6
#16 42.14 |    |    \--- org.slf4j:slf4j-api:1.7.36 -> 2.0.17
#16 42.14 |    |    |    \--- org.apache.httpcomponents.core5:httpcore5:5.3.6
#16 42.14 |    |    +--- org.apache.httpcomponents.core5:httpcore5-h2:5.3.6
#16 42.14 |    |    +--- org.apache.httpcomponents.core5:httpcore5:5.3.6
#16 42.14 |    +--- org.apache.httpcomponents.client5:httpclient5:5.3.1 -> 5.5.1
#16 42.14 |    +--- org.apache.commons:commons-lang3:3.1 -> 3.18.0
#16 42.14 |    +--- com.cloudinary:cloudinary-core:2.3.0
#16 42.14 +--- com.cloudinary:cloudinary-http5:2.3.0
#16 42.14 +--- net.coobird:thumbnailator:0.4.19
#16 42.14 |    \--- org.springframework:spring-context-support:6.2.12 (*)
#16 42.14 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 42.14 +--- org.springframework.boot:spring-boot-starter-cache -> 3.5.7
#16 42.14 |         \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
#16 42.14 |         |    \--- org.springframework:spring-core:6.2.12 (*)
#16 42.14 |         |    +--- org.springframework:spring-context:6.2.12 (*)
#16 42.14 |         |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.14 |         +--- org.springframework:spring-context-support:6.2.12
#16 42.14 |         +--- org.springframework:spring-aop:6.2.12 (*)
#16 42.14 |         |    \--- org.springframework:spring-core:6.2.12 (*)
#16 42.14 |         |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.14 |         +--- org.springframework:spring-oxm:6.2.12
#16 42.14 |         +--- org.springframework:spring-tx:6.2.12 (*)
#16 42.14 |         |    \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
#16 42.14 |         |    +--- org.springframework:spring-tx:6.2.12 (*)
#16 42.14 |         |    +--- org.springframework:spring-context:6.2.12 (*)
#16 42.14 |         |    +--- org.springframework.data:spring-data-commons:3.5.5 (*)
#16 42.14 |         +--- org.springframework.data:spring-data-keyvalue:3.5.5
#16 42.14 |    \--- org.springframework.data:spring-data-redis:3.5.5
#16 42.14 |    |    \--- io.projectreactor:reactor-core:3.6.6 -> 3.7.12 (*)
#16 42.14 |    |    +--- io.netty:netty-transport:4.1.118.Final -> 4.1.128.Final (*)
#16 42.14 |    |    |         \--- io.netty:netty-transport:4.1.128.Final (*)
#16 42.14 |    |    |         +--- io.netty:netty-buffer:4.1.128.Final (*)
#16 42.14 |    |    |         +--- io.netty:netty-common:4.1.128.Final
#16 42.14 |    |    |    \--- io.netty:netty-codec:4.1.128.Final
#16 42.14 |    |    |    |    \--- io.netty:netty-transport:4.1.128.Final (*)
#16 42.14 |    |    |    |    +--- io.netty:netty-buffer:4.1.128.Final (*)
#16 42.14 |    |    |    |    +--- io.netty:netty-common:4.1.128.Final
#16 42.14 |    |    |    +--- io.netty:netty-transport-native-unix-common:4.1.128.Final
#16 42.13 |    |    |    |    \--- io.netty:netty-resolver:4.1.128.Final (*)
#16 42.13 |    |    |    |    +--- io.netty:netty-buffer:4.1.128.Final (*)
#16 42.13 |    |    |    |    +--- io.netty:netty-common:4.1.128.Final
#16 42.13 |    |    |    +--- io.netty:netty-transport:4.1.128.Final
#16 42.13 |    |    |    |    \--- io.netty:netty-common:4.1.128.Final
#16 42.13 |    |    |    +--- io.netty:netty-buffer:4.1.128.Final
#16 42.13 |    |    |    |    \--- io.netty:netty-common:4.1.128.Final
#16 42.13 |    |    |    +--- io.netty:netty-resolver:4.1.128.Final
#16 42.13 |    |    |    +--- io.netty:netty-common:4.1.128.Final
#16 42.13 |    |    +--- io.netty:netty-handler:4.1.118.Final -> 4.1.128.Final
#16 42.13 |    |    +--- io.netty:netty-common:4.1.118.Final -> 4.1.128.Final
#16 42.13 |    |    |    \--- org.slf4j:slf4j-api:1.7.36 -> 2.0.17
#16 42.13 |    |    +--- redis.clients.authentication:redis-authx-core:0.1.1-beta2
#16 42.13 |    +--- io.lettuce:lettuce-core:6.6.0.RELEASE
#16 42.13 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 42.13 +--- org.springframework.boot:spring-boot-starter-data-redis -> 3.5.7
#16 42.13 |    \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
#16 42.13 |    +--- org.thymeleaf:thymeleaf-spring6:3.1.3.RELEASE (*)
#16 42.13 +--- org.thymeleaf.extras:thymeleaf-extras-springsecurity6 -> 3.1.3.RELEASE
#16 42.13 +--- org.springframework:spring-aspects -> 6.2.12 (*)
#16 42.13 +--- org.springframework.retry:spring-retry -> 2.0.12
#16 42.13 |         \--- org.springframework:spring-web:6.2.12 (*)
#16 42.13 |         +--- org.springframework:spring-expression:6.2.12 (*)
#16 42.13 |         +--- org.springframework:spring-core:6.2.12 (*)
#16 42.13 |         +--- org.springframework:spring-context:6.2.12 (*)
#16 42.13 |         +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.13 |         +--- org.springframework:spring-aop:6.2.12 (*)
#16 42.13 |    \--- org.springframework:spring-webmvc:6.2.12
#16 42.13 |    +--- org.springframework:spring-web:6.2.12 (*)
#16 42.13 |    |         \--- org.apache.tomcat.embed:tomcat-embed-core:10.1.48
#16 42.13 |    |    \--- org.apache.tomcat.embed:tomcat-embed-websocket:10.1.48
#16 42.13 |    |    +--- org.apache.tomcat.embed:tomcat-embed-el:10.1.48
#16 42.13 |    |    +--- org.apache.tomcat.embed:tomcat-embed-core:10.1.48
#16 42.13 |    |    +--- jakarta.annotation:jakarta.annotation-api:2.1.1
#16 42.13 |    +--- org.springframework.boot:spring-boot-starter-tomcat:3.5.7
#16 42.13 |    |         \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
#16 42.13 |    |         +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (*)
#16 42.13 |    |         +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (*)
#16 42.13 |    |    \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.19.2
#16 42.13 |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
#16 42.13 |    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (*)
#16 42.13 |    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (*)
#16 42.13 |    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.19.2 (*)
#16 42.13 |    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.2
#16 42.13 |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
#16 42.13 |    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (*)
#16 42.13 |    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (*)
#16 42.13 |    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.19.2
#16 42.13 |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
#16 42.13 |    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
#16 42.13 |    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.19.2
#16 42.13 |    |    |    |         \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.19.2 (c)
#16 42.13 |    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.2 (c)
#16 42.13 |    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.19.2 (c)
#16 42.13 |    |    |    |         +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (c)
#16 42.13 |    |    |    |         +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (c)
#16 42.13 |    |    |    |         +--- com.fasterxml.jackson.core:jackson-annotations:2.19.2 (c)
#16 42.13 |    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2
#16 42.13 |    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.19.2
#16 42.13 |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.19.2
#16 42.13 |    |    +--- org.springframework:spring-web:6.2.12 (*)
#16 42.13 |    |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 42.13 |    +--- org.springframework.boot:spring-boot-starter-json:3.5.7
#16 42.13 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 42.13 +--- org.springframework.boot:spring-boot-starter-web -> 3.5.7
#16 42.13 |         \--- com.fasterxml:classmate:1.5.1 -> 1.7.1
#16 42.13 |         +--- org.jboss.logging:jboss-logging:3.4.3.Final -> 3.6.1.Final
#16 42.13 |         +--- jakarta.validation:jakarta.validation-api:3.0.2
#16 42.13 |    \--- org.hibernate.validator:hibernate-validator:8.0.3.Final
#16 42.13 |    +--- org.apache.tomcat.embed:tomcat-embed-el:10.1.48
#16 42.13 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 42.13 +--- org.springframework.boot:spring-boot-starter-validation -> 3.5.7
#16 42.13 |         \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
#16 42.13 |         |    \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
#16 42.13 |         |    +--- org.unbescape:unbescape:1.1.6.RELEASE
#16 42.13 |         |    +--- org.attoparser:attoparser:2.0.7.RELEASE
#16 42.13 |         +--- org.thymeleaf:thymeleaf:3.1.3.RELEASE
#16 42.13 |    \--- org.thymeleaf:thymeleaf-spring6:3.1.3.RELEASE
#16 42.13 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 42.13 +--- org.springframework.boot:spring-boot-starter-thymeleaf -> 3.5.7
#16 42.13 |              \--- io.micrometer:micrometer-observation:1.14.12 -> 1.15.5 (*)
#16 42.13 |              +--- org.springframework:spring-core:6.2.12 (*)
#16 42.13 |              +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.13 |         \--- org.springframework:spring-web:6.2.12
#16 42.13 |         +--- org.springframework:spring-expression:6.2.12 (*)
#16 42.13 |         +--- org.springframework:spring-context:6.2.12 (*)
#16 42.13 |         +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.13 |         +--- org.springframework:spring-aop:6.2.12 (*)
#16 42.13 |         +--- org.springframework:spring-core:6.2.12 (*)
#16 42.13 |         +--- org.springframework.security:spring-security-core:6.5.6 (*)
#16 42.13 |    \--- org.springframework.security:spring-security-web:6.5.6
#16 42.13 |    |    \--- org.springframework:spring-core:6.2.12 (*)
#16 42.13 |    |    +--- org.springframework:spring-context:6.2.12 (*)
#16 42.13 |    |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.13 |    |    +--- org.springframework:spring-aop:6.2.12 (*)
#16 42.13 |    |    |    \--- io.micrometer:micrometer-observation:1.14.12 -> 1.15.5 (*)
#16 42.13 |    |    |    +--- org.springframework:spring-expression:6.2.12 (*)
#16 42.13 |    |    |    +--- org.springframework:spring-core:6.2.12 (*)
#16 42.13 |    |    |    +--- org.springframework:spring-context:6.2.12 (*)
#16 42.13 |    |    |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.13 |    |    |    +--- org.springframework:spring-aop:6.2.12 (*)
#16 42.13 |    |    |    +--- org.springframework.security:spring-security-crypto:6.5.6
#16 42.13 |    |    +--- org.springframework.security:spring-security-core:6.5.6
#16 42.13 |    +--- org.springframework.security:spring-security-config:6.5.6
#16 42.13 |    +--- org.springframework:spring-aop:6.2.12 (*)
#16 42.13 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 42.13 +--- org.springframework.boot:spring-boot-starter-security -> 3.5.7
#16 42.13 |         \--- org.aspectj:aspectjweaver:1.9.22.1 -> 1.9.24
#16 42.13 |    \--- org.springframework:spring-aspects:6.2.12
#16 42.13 |    |    \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
#16 42.13 |    |    +--- jakarta.annotation:jakarta.annotation-api:2.0.0 -> 2.1.1
#16 42.13 |    |    +--- org.antlr:antlr4-runtime:4.13.0
#16 42.13 |    |    +--- org.springframework:spring-core:6.2.12 (*)
#16 42.13 |    |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.13 |    |    +--- org.springframework:spring-tx:6.2.12 (*)
#16 42.13 |    |    +--- org.springframework:spring-aop:6.2.12 (*)
#16 42.13 |    |    +--- org.springframework:spring-context:6.2.12 (*)
#16 42.13 |    |    |    \--- org.springframework:spring-tx:6.2.12 (*)
#16 42.13 |    |    |    +--- org.springframework:spring-jdbc:6.2.12 (*)
#16 42.13 |    |    |    +--- org.springframework:spring-core:6.2.12 (*)
#16 42.13 |    |    |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.13 |    |    +--- org.springframework:spring-orm:6.2.12
#16 42.13 |    |    |    \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
#16 42.13 |    |    |    +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.13 |    |    |    +--- org.springframework:spring-core:6.2.12 (*)
#16 42.13 |    |    +--- org.springframework.data:spring-data-commons:3.5.5
#16 42.13 |    +--- org.springframework.data:spring-data-jpa:3.5.5
#16 42.13 |    |    \--- jakarta.transaction:jakarta.transaction-api:2.0.1
#16 42.13 |    |    +--- jakarta.persistence:jakarta.persistence-api:3.1.0
#16 42.13 |    +--- org.hibernate.orm:hibernate-core:6.6.33.Final
#16 42.13 |    |              \--- org.springframework:spring-core:6.2.12 (*)
#16 42.13 |    |              +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.13 |    |         \--- org.springframework:spring-tx:6.2.12
#16 42.13 |    |         +--- org.springframework:spring-core:6.2.12 (*)
#16 42.13 |    |         +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.13 |    |    \--- org.springframework:spring-jdbc:6.2.12
#16 42.13 |    |    |    \--- org.slf4j:slf4j-api:2.0.17
#16 42.13 |    |    +--- com.zaxxer:HikariCP:6.3.3
#16 42.13 |    |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
#16 42.13 |    +--- org.springframework.boot:spring-boot-starter-jdbc:3.5.7
#16 42.13 |    |    \--- org.yaml:snakeyaml:2.4
#16 42.13 |    |    +--- org.springframework:spring-core:6.2.12 (*)
#16 42.13 |    |    +--- jakarta.annotation:jakarta.annotation-api:2.1.1
#16 42.13 |    |    |         \--- org.slf4j:slf4j-api:2.0.17
#16 42.13 |    |    |    \--- org.slf4j:jul-to-slf4j:2.0.17
#16 42.13 |    |    |    |    \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
#16 42.13 |    |    |    |    +--- org.apache.logging.log4j:log4j-api:2.24.3
#16 42.13 |    |    |    +--- org.apache.logging.log4j:log4j-to-slf4j:2.24.3
#16 42.13 |    |    |    |    \--- org.slf4j:slf4j-api:2.0.17
#16 42.13 |    |    |    |    +--- ch.qos.logback:logback-core:1.5.20
#16 42.13 |    |    |    +--- ch.qos.logback:logback-classic:1.5.20
#16 42.13 |    |    +--- org.springframework.boot:spring-boot-starter-logging:3.5.7
#16 42.13 |    |    |    \--- org.springframework.boot:spring-boot:3.5.7 (*)
#16 42.13 |    |    +--- org.springframework.boot:spring-boot-autoconfigure:3.5.7
#16 42.13 |    |    |              \--- io.micrometer:micrometer-commons:1.15.5
#16 42.13 |    |    |         \--- io.micrometer:micrometer-observation:1.14.12 -> 1.15.5
#16 42.13 |    |    |         |    \--- org.springframework:spring-core:6.2.12 (*)
#16 42.13 |    |    |         +--- org.springframework:spring-expression:6.2.12
#16 42.13 |    |    |         +--- org.springframework:spring-core:6.2.12 (*)
#16 42.12 |    |    |         +--- org.springframework:spring-beans:6.2.12 (*)
#16 42.12 |    |    |         |    \--- org.springframework:spring-core:6.2.12 (*)
#16 42.12 |    |    |         |    |    \--- org.springframework:spring-core:6.2.12 (*)
#16 42.12 |    |    |         |    +--- org.springframework:spring-beans:6.2.12
#16 42.12 |    |    |         +--- org.springframework:spring-aop:6.2.12
#16 42.12 |    |    |    \--- org.springframework:spring-context:6.2.12
#16 42.12 |    |    |    |    \--- org.springframework:spring-jcl:6.2.12
#16 42.12 |    |    |    +--- org.springframework:spring-core:6.2.12
#16 42.12 |    |    +--- org.springframework.boot:spring-boot:3.5.7
#16 42.12 |    +--- org.springframework.boot:spring-boot-starter:3.5.7
#16 42.12 +--- org.springframework.boot:spring-boot-starter-data-jpa -> 3.5.7
#16 42.12 |    \--- jakarta.annotation:jakarta.annotation-api:3.0.0 -> 2.1.1
#16 42.12 |    +--- jakarta.persistence:jakarta.persistence-api:3.2.0 -> 3.1.0
#16 42.12 |    |    \--- jakarta.annotation:jakarta.annotation-api:3.0.0 -> 2.1.1
#16 42.12 |    |    +--- io.github.classgraph:classgraph:4.8.179
#16 42.12 |    |    +--- jakarta.inject:jakarta.inject-api:2.0.1.MR -> 2.0.1
#16 42.12 |    |    |    \--- io.github.classgraph:classgraph:4.8.179
#16 42.12 |    |    |    +--- org.eclipse.jdt:ecj:3.40.0
#16 42.02 |    |    +--- io.github.openfeign.querydsl:querydsl-codegen-utils:7.0
#16 42.02 |    |    |         \--- org.reactivestreams:reactive-streams:1.0.4
#16 42.02 |    |    |    \--- io.projectreactor:reactor-core:3.7.6 -> 3.7.12
#16 42.02 |    |    +--- io.github.openfeign.querydsl:querydsl-core:7.0
#16 42.02 |    +--- io.github.openfeign.querydsl:querydsl-codegen:7.0
#16 42.02 +--- io.github.openfeign.querydsl:querydsl-apt:7.0
#16 42.02 +--- org.projectlombok:lombok -> 1.18.42
#16 36.42 compileClasspath - Compile classpath for source set 'main'.
#16 36.42
#16 36.42 No dependencies
#16 36.42 bootArchives - Configuration for Spring Boot archive artifacts. (n)
#16 36.42
#16 36.42      \--- jakarta.annotation:jakarta.annotation-api:3.0.0 -> 2.1.1
#16 36.42      +--- jakarta.persistence:jakarta.persistence-api:3.2.0 -> 3.1.0
#16 36.42      |    \--- jakarta.annotation:jakarta.annotation-api:3.0.0 -> 2.1.1
#16 36.42      |    +--- io.github.classgraph:classgraph:4.8.179
#16 36.42      |    +--- jakarta.inject:jakarta.inject-api:2.0.1.MR -> 2.0.1
#16 36.42      |    |    \--- io.github.classgraph:classgraph:4.8.179
#16 36.42      |    |    +--- org.eclipse.jdt:ecj:3.40.0
#16 36.42      |    +--- io.github.openfeign.querydsl:querydsl-codegen-utils:7.0
#16 36.42      |    |         \--- org.reactivestreams:reactive-streams:1.0.4
#16 36.42      |    |    \--- io.projectreactor:reactor-core:3.7.6 -> 3.7.12
#16 36.42      |    +--- io.github.openfeign.querydsl:querydsl-core:7.0
#16 36.42      +--- io.github.openfeign.querydsl:querydsl-codegen:7.0
#16 36.42 \--- io.github.openfeign.querydsl:querydsl-apt:7.0
#16 36.42 +--- org.projectlombok:lombok -> 1.18.42
#16 32.06 annotationProcessor - Annotation processors and their dependencies for source set 'main'.
#16 32.06
#16 32.06 ------------------------------------------------------------
#16 32.06 Root project 'recipemate-api' - Recipe-based smart grocery group buying platform for students
#16 32.06 ------------------------------------------------------------
#16 32.06
#16 32.06 > Task :dependencies
#16 32.06
#16 2.162 Daemon will be stopped at the end of the build
#16 0.762 To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/8.5/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
#16 0.663
#16 0.663 For more details see https://docs.gradle.org/8.5/release-notes.html
#16 0.663
#16 0.663  - Improved error and warning messages
#16 0.663  - Faster first use with Kotlin DSL
#16 0.663  - Support for running on Java 21
#16 0.663 Here are the highlights of this release:
#16 0.662
#16 0.662 Welcome to Gradle 8.5!
#16 0.662
#16 [builder 5/7] RUN gradle dependencies --no-daemon || true
#15 DONE 0.0s
#15 [builder 4/7] COPY gradle gradle/
#14 DONE 0.0s
#14 [builder 3/7] COPY build.gradle settings.gradle gradlew ./
#13 DONE 0.0s
#13 [builder 2/7] WORKDIR /app
#9 DONE 23.0s
#9 extracting sha256:1b2010acb2c2f72b7c96a939846c10d9796df96c67f60b271c88f62010e4b002 0.0s done
#9 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
#9 DONE 23.0s
#9 extracting sha256:38c3a1b03835788cfba7bd295627422fb24ce2d03897cc5df857e07ebba62e96 1.0s done
#9 extracting sha256:38c3a1b03835788cfba7bd295627422fb24ce2d03897cc5df857e07ebba62e96
#9 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
#9 DONE 22.0s
#9 extracting sha256:1e25bf6602788516b96f9136306fc7eae2cee151d94343b0d47023e5b36ed5bd 0.9s done
#9 extracting sha256:1e25bf6602788516b96f9136306fc7eae2cee151d94343b0d47023e5b36ed5bd
#9 extracting sha256:516905b70d0910e39f224893c8efc6801b1e1d053bf0031afdb3ef18728a4088 0.0s done
#9 extracting sha256:c4451c8b3dbe1616b4f1d08ac6cbd135f48e3d3393f2c37402ac18b0b845dfdc 0.0s done
#9 extracting sha256:98795b95bb4830f88ab5d81f355f8a6f3bd833705d858023f9b58c42457db454 0.0s done
#9 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
#9 DONE 21.0s
#9 extracting sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 2.6s done
#9 extracting sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e
#9 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 158.61MB / 158.61MB 10.8s done
#9 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 158.61MB / 158.61MB 10.8s
#9 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 134.22MB / 158.61MB 9.3s
#9 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 125.83MB / 158.61MB 9.0s
#9 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
#12 DONE 0.1s
#12 [stage-1 4/5] RUN mkdir -p /app/uploads && chown -R spring:spring /app
#11 DONE 0.1s
#11 [stage-1 3/5] RUN addgroup -S spring && adduser -S spring -G spring
#9 ...
#9 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
#10 DONE 1.4s
#10 [stage-1 2/5] WORKDIR /app
#9 ...
#9 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 117.44MB / 158.61MB 8.6s
#9 extracting sha256:59648cfc069f04a8d0eece9cae80e25155888dc9b5de722b58603efafaa0d78b 3.6s done
#9 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 109.05MB / 158.61MB 8.1s
#9 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 100.66MB / 158.61MB 8.0s
#9 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
#7 DONE 14.8s
#7 extracting sha256:23d1cf1d02f064d13d647e4816e26b17678a634769ec583b4eb29d5a68d415f7 1.8s done
#7 [stage-1 1/5] FROM docker.io/library/eclipse-temurin:21-jre-alpine@sha256:326837fba06a8ff5482a17bafbd65319e64a6e997febb7c85ebe7e3f73c12b11
#9 ...
#9 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 92.27MB / 158.61MB 7.2s
#9 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 75.50MB / 158.61MB 6.2s
#9 extracting sha256:59648cfc069f04a8d0eece9cae80e25155888dc9b5de722b58603efafaa0d78b
#9 sha256:59648cfc069f04a8d0eece9cae80e25155888dc9b5de722b58603efafaa0d78b 13.14MB / 13.14MB 3.1s done
#9 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 58.72MB / 158.61MB 5.3s
#9 sha256:38c3a1b03835788cfba7bd295627422fb24ce2d03897cc5df857e07ebba62e96 132.54MB / 132.54MB 8.3s done
#9 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
#7 DONE 12.9s
#7 extracting sha256:15a75c308165465141d861c1b7f7ec799b18fc84d5d8b87396dc29c3aa4894ff 1.6s done
#7 extracting sha256:15a75c308165465141d861c1b7f7ec799b18fc84d5d8b87396dc29c3aa4894ff
#7 extracting sha256:3a857bcc1a1505748a54fef25bb81f303468a26d3de127b6b0044b1ffde6275a 3.3s done
#7 [stage-1 1/5] FROM docker.io/library/eclipse-temurin:21-jre-alpine@sha256:326837fba06a8ff5482a17bafbd65319e64a6e997febb7c85ebe7e3f73c12b11
#9 ...
#9 extracting sha256:4abcf20661432fb2d719aaf90656f55c287f8ca915dc1c92ec14ff61e67fbaf8 0.1s done
#9 sha256:59648cfc069f04a8d0eece9cae80e25155888dc9b5de722b58603efafaa0d78b 13.14MB / 13.14MB 2.6s
#9 extracting sha256:4abcf20661432fb2d719aaf90656f55c287f8ca915dc1c92ec14ff61e67fbaf8
#9 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 33.55MB / 158.61MB 3.8s
#9 sha256:59648cfc069f04a8d0eece9cae80e25155888dc9b5de722b58603efafaa0d78b 8.39MB / 13.14MB 2.3s
#9 sha256:4abcf20661432fb2d719aaf90656f55c287f8ca915dc1c92ec14ff61e67fbaf8 3.41MB / 3.41MB 2.3s done
#9 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 25.17MB / 158.61MB 3.2s
#9 sha256:38c3a1b03835788cfba7bd295627422fb24ce2d03897cc5df857e07ebba62e96 109.05MB / 132.54MB 6.0s
#9 sha256:59648cfc069f04a8d0eece9cae80e25155888dc9b5de722b58603efafaa0d78b 0B / 13.14MB 0.6s
#9 sha256:4abcf20661432fb2d719aaf90656f55c287f8ca915dc1c92ec14ff61e67fbaf8 0B / 3.41MB 0.6s
#9 sha256:98795b95bb4830f88ab5d81f355f8a6f3bd833705d858023f9b58c42457db454 179B / 179B 1.3s done
#9 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 8.39MB / 158.61MB 2.0s
#9 sha256:c4451c8b3dbe1616b4f1d08ac6cbd135f48e3d3393f2c37402ac18b0b845dfdc 716B / 716B 1.2s done
#9 sha256:516905b70d0910e39f224893c8efc6801b1e1d053bf0031afdb3ef18728a4088 1.33kB / 1.33kB 1.2s done
#9 sha256:38c3a1b03835788cfba7bd295627422fb24ce2d03897cc5df857e07ebba62e96 83.89MB / 132.54MB 5.6s
#9 sha256:1e25bf6602788516b96f9136306fc7eae2cee151d94343b0d47023e5b36ed5bd 34.82MB / 34.82MB 4.8s done
#9 sha256:1b2010acb2c2f72b7c96a939846c10d9796df96c67f60b271c88f62010e4b002 167B / 167B 1.5s done
#9 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
#7 ...
#7 extracting sha256:3a857bcc1a1505748a54fef25bb81f303468a26d3de127b6b0044b1ffde6275a
#7 extracting sha256:93902f35c01693819c190c354786cbb573f4ef49a5b865d6c34f2197839cc3e4 2.9s done
#7 sha256:3a857bcc1a1505748a54fef25bb81f303468a26d3de127b6b0044b1ffde6275a 53.17MB / 53.17MB 6.1s done
#7 sha256:3a857bcc1a1505748a54fef25bb81f303468a26d3de127b6b0044b1ffde6275a 53.17MB / 53.17MB 6.0s
#7 sha256:3a857bcc1a1505748a54fef25bb81f303468a26d3de127b6b0044b1ffde6275a 41.94MB / 53.17MB 4.5s
#7 sha256:3a857bcc1a1505748a54fef25bb81f303468a26d3de127b6b0044b1ffde6275a 13.47MB / 53.17MB 4.4s
#7 extracting sha256:93902f35c01693819c190c354786cbb573f4ef49a5b865d6c34f2197839cc3e4
#7 extracting sha256:2d35ebdb57d9971fea0cac1582aa78935adf8058b2cc32db163c98822e5dfa1b 1.0s done
#7 sha256:93902f35c01693819c190c354786cbb573f4ef49a5b865d6c34f2197839cc3e4 16.29MB / 16.29MB 3.7s done
#7 sha256:3a857bcc1a1505748a54fef25bb81f303468a26d3de127b6b0044b1ffde6275a 8.39MB / 53.17MB 3.8s
#7 sha256:93902f35c01693819c190c354786cbb573f4ef49a5b865d6c34f2197839cc3e4 8.39MB / 16.29MB 2.7s
#7 extracting sha256:2d35ebdb57d9971fea0cac1582aa78935adf8058b2cc32db163c98822e5dfa1b
#7 sha256:2d35ebdb57d9971fea0cac1582aa78935adf8058b2cc32db163c98822e5dfa1b 3.80MB / 3.80MB 1.6s done
#7 sha256:2d35ebdb57d9971fea0cac1582aa78935adf8058b2cc32db163c98822e5dfa1b 0B / 3.80MB 0.2s
#7 sha256:15a75c308165465141d861c1b7f7ec799b18fc84d5d8b87396dc29c3aa4894ff 128B / 128B 1.1s done
#7 sha256:23d1cf1d02f064d13d647e4816e26b17678a634769ec583b4eb29d5a68d415f7 2.28kB / 2.28kB 1.1s done
#7 sha256:93902f35c01693819c190c354786cbb573f4ef49a5b865d6c34f2197839cc3e4 0B / 16.29MB 0.2s
#7 sha256:3a857bcc1a1505748a54fef25bb81f303468a26d3de127b6b0044b1ffde6275a 0B / 53.17MB 0.2s
#7 sha256:15a75c308165465141d861c1b7f7ec799b18fc84d5d8b87396dc29c3aa4894ff 0B / 128B 0.2s
#7 sha256:23d1cf1d02f064d13d647e4816e26b17678a634769ec583b4eb29d5a68d415f7 0B / 2.28kB 0.2s
#7 [stage-1 1/5] FROM docker.io/library/eclipse-temurin:21-jre-alpine@sha256:326837fba06a8ff5482a17bafbd65319e64a6e997febb7c85ebe7e3f73c12b11
#9 DONE 1.5s
#9 resolve docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87 0.0s done
#9 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
#7 DONE 0.7s
#7 [stage-1 1/5] FROM docker.io/library/eclipse-temurin:21-jre-alpine@sha256:326837fba06a8ff5482a17bafbd65319e64a6e997febb7c85ebe7e3f73c12b11
#8 DONE 0.5s
#8 transferring context: 1.75MB 0.1s done
#8 [internal] load build context
#7 ...
#7 resolve docker.io/library/eclipse-temurin:21-jre-alpine@sha256:326837fba06a8ff5482a17bafbd65319e64a6e997febb7c85ebe7e3f73c12b11 0.0s done
#7 [stage-1 1/5] FROM docker.io/library/eclipse-temurin:21-jre-alpine@sha256:326837fba06a8ff5482a17bafbd65319e64a6e997febb7c85ebe7e3f73c12b11
#6 DONE 0.0s
#6 transferring context: 735B done
#6 [internal] load .dockerignore
#2 DONE 7.1s
#2 [internal] load metadata for docker.io/library/gradle:8.5-jdk21-alpine
#5 DONE 4.5s
#5 [internal] load metadata for docker.io/library/eclipse-temurin:21-jre-alpine
#4 DONE 0.0s
#4 [auth] library/eclipse-temurin:pull render-prod/docker-mirror-repository/library/eclipse-temurin:pull token for us-west1-docker.pkg.dev
#3 DONE 0.0s
#3 [auth] library/gradle:pull render-prod/docker-mirror-repository/library/gradle:pull token for us-west1-docker.pkg.dev
#2 ...
#2 [internal] load metadata for docker.io/library/gradle:8.5-jdk21-alpine
#1 DONE 0.0s
#1 transferring dockerfile: 1.35kB done
#1 [internal] load build definition from Dockerfile
==> Checking out commit 6d407548c4195f94cb80785f70914bdb8d73e0db in branch develop
==> Cloning from https://github.com/person3113/recipemate