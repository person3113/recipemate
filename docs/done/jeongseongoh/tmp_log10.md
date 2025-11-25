post 이미지 두 개 업로드는 되고. 수정 폼에서 2 개 중 뒤에 추가한 하나 삭제하고 다시 완료하려고 했는데 다음 오류가 뜸. cloudinary에서는 하나 삭제된 거 같은데. db 상에서나 웹에서는 안 된 느낌. (참고로 배포에서도 로컬에서도 동일)

2025-11-25T11:22:08.220Z  INFO 1 --- [RecipeMate] [io-8080-exec-10] c.r.global.util.ImageUploadUtil          : Successfully uploaded 0 out of 1 images (parallel)
2025-11-25T11:22:08.220Z  WARN 1 --- [RecipeMate] [io-8080-exec-10] c.r.global.util.ImageUploadUtil          : Empty file at index 0, skipping
2025-11-25T11:22:08.213Z  INFO 1 --- [RecipeMate] [io-8080-exec-10] c.r.global.util.ImageUploadUtil          : Image deleted from Cloudinary: recipemate/group-purchases/qv2xfvvqudnahza9xjym (result: ok)
at com.recipemate.domain.post.service.PostService.updatePost(PostService.java:168) ~[!/:0.0.1-SNAPSHOT]
at com.recipemate.global.util.ImageUploadUtil.deleteImages(ImageUploadUtil.java:177) ~[!/:0.0.1-SNAPSHOT]
at com.recipemate.global.util.ImageUploadUtil.extractPublicIdFromUrl(ImageUploadUtil.java:221) ~[!/:0.0.1-SNAPSHOT]
java.lang.IllegalArgumentException: Invalid Cloudinary URL: q_auto
2025-11-25T11:22:07.182Z ERROR 1 --- [RecipeMate] [io-8080-exec-10] c.r.global.util.ImageUploadUtil          : Failed to delete image from Cloudinary: q_auto
java.lang.StringIndexOutOfBoundsException: Range [7, 6) out of bounds for length 6
2025-11-25T11:22:07.181Z ERROR 1 --- [RecipeMate] [io-8080-exec-10] c.r.global.util.ImageUploadUtil          : Failed to extract public_id from URL: q_auto
at com.cloudinary.http5.UploaderStrategy.callApi(UploaderStrategy.java:113) ~[cloudinary-http5-2.3.0.jar!/:na]
at com.cloudinary.strategies.AbstractUploaderStrategy.processResponse(AbstractUploaderStrategy.java:85) ~[cloudinary-core-2.3.0.jar!/:na]
java.lang.RuntimeException: Missing required parameter - public_id
2025-11-25T11:22:07.179Z ERROR 1 --- [RecipeMate] [io-8080-exec-10] c.r.global.util.ImageUploadUtil          : Failed to delete image from Cloudinary: c_limit
info
at com.recipemate.domain.post.service.PostService.updatePost(PostService.java:168) ~[!/:0.0.1-SNAPSHOT]
at com.recipemate.global.util.ImageUploadUtil.deleteImages(ImageUploadUtil.java:177) ~[!/:0.0.1-SNAPSHOT]
at com.recipemate.global.util.ImageUploadUtil.extractPublicIdFromUrl(ImageUploadUtil.java:221) ~[!/:0.0.1-SNAPSHOT]
java.lang.IllegalArgumentException: Invalid Cloudinary URL: h_600
2025-11-25T11:22:06.401Z ERROR 1 --- [RecipeMate] [io-8080-exec-10] c.r.global.util.ImageUploadUtil          : Failed to delete image from Cloudinary: h_600
java.lang.StringIndexOutOfBoundsException: Range [7, 5) out of bounds for length 5
2025-11-25T11:22:06.399Z ERROR 1 --- [RecipeMate] [io-8080-exec-10] c.r.global.util.ImageUploadUtil          : Failed to extract public_id from URL: h_600
2025-11-25T11:22:06.398Z  INFO 1 --- [RecipeMate] [io-8080-exec-10] c.r.global.util.ImageUploadUtil          : Image deleted from Cloudinary: w_800 (result: not found)
2025-11-25T11:20:55.847Z  INFO 1 --- [RecipeMate] [nio-8080-exec-4] c.r.global.util.ImageUploadUtil          : Successfully uploaded 2 out of 2 images (parallel)
2025-11-25T11:20:55.846Z  INFO 1 --- [RecipeMate] [pool-2-thread-2] c.r.global.util.ImageUploadUtil          : Image uploaded successfully in 2144ms: https://res.cloudinary.com/dt9xgsr2z/image/upload/w_800,h_600,c_limit,q_auto,f_auto/v1764069655/recipemate/group-purchases/dfhifr5bymya3vcvikc2.png
2025-11-25T11:20:55.028Z  INFO 1 --- [RecipeMate] [pool-2-thread-3] c.r.global.util.ImageUploadUtil          : Image uploaded successfully in 1326ms: https://res.cloudinary.com/dt9xgsr2z/image/upload/w_800,h_600,c_limit,q_auto,f_auto/v1764069654/recipemate/group-purchases/qv2xfvvqudnahza9xjym.png
2025-11-25T11:20:53.702Z  INFO 1 --- [RecipeMate] [pool-2-thread-2] c.r.global.util.ImageUploadUtil          : Uploading image to Cloudinary: 스크린샷 2025-11-12 141401.png
2025-11-25T11:20:53.702Z  INFO 1 --- [RecipeMate] [pool-2-thread-3] c.r.global.util.ImageUploadUtil          : Uploading image to Cloudinary: 스크린샷 2025-11-20 153825.png

recipemate_postgres=> select * from post_images
recipemate_postgres-> ;
id |         created_at         | deleted_at |         updated_at         | display_order |                                                                      image_url                                                                      | post_id
----+----------------------------+------------+----------------------------+---------------+-----------------------------------------------------------------------------------------------------------------------------------------------------+---------
1 | 2025-11-25 11:20:55.848805 |            | 2025-11-25 11:20:55.848805 |             0 | https://res.cloudinary.com/dt9xgsr2z/image/upload/w_800,h_600,c_limit,q_auto,f_auto/v1764069655/recipemate/group-purchases/dfhifr5bymya3vcvikc2.png |       1
2 | 2025-11-25 11:20:55.854393 |            | 2025-11-25 11:20:55.854393 |             1 | https://res.cloudinary.com/dt9xgsr2z/image/upload/w_800,h_600,c_limit,q_auto,f_auto/v1764069654/recipemate/group-purchases/qv2xfvvqudnahza9xjym.png |       1
(2 rows)

---

2025-11-25T20:34:36.283+09:00  INFO 32004 --- [RecipeMate] [pool-2-thread-1] c.r.global.util.ImageUploadUtil          : Uploading image to Cloudinary: 스크린샷 2025-11-20 153825.png
2025-11-25T20:34:36.283+09:00  INFO 32004 --- [RecipeMate] [pool-2-thread-3] c.r.global.util.ImageUploadUtil          : Uploading image to Cloudinary: 스크린샷 2025-11-12 141401.png
2025-11-25T20:34:37.704+09:00  INFO 32004 --- [RecipeMate] [pool-2-thread-1] c.r.global.util.ImageUploadUtil          : Image uploaded successfully in 1421ms: https://res.cloudinary.com/dt9xgsr2z/image/upload/w_800,h_600,c_limit,q_auto,f_auto/v1764070478/recipemate/group-purchases/xrtawbat8bfbt5hs3ybx.png
2025-11-25T20:34:38.396+09:00  INFO 32004 --- [RecipeMate] [pool-2-thread-3] c.r.global.util.ImageUploadUtil          : Image uploaded successfully in 2113ms: https://res.cloudinary.com/dt9xgsr2z/image/upload/w_800,h_600,c_limit,q_auto,f_auto/v1764070479/recipemate/group-purchases/pku7hay5k1gvprqijiov.png
2025-11-25T20:34:38.397+09:00  INFO 32004 --- [RecipeMate] [nio-8080-exec-4] c.r.global.util.ImageUploadUtil          : Successfully uploaded 2 out of 2 images (parallel)
2025-11-25T20:35:20.544+09:00  INFO 32004 --- [RecipeMate] [nio-8080-exec-5] c.r.global.util.ImageUploadUtil          : Image deleted from Cloudinary: w_800 (result: not found)
2025-11-25T20:35:20.564+09:00 ERROR 32004 --- [RecipeMate] [nio-8080-exec-5] c.r.global.util.ImageUploadUtil          : Failed to extract public_id from URL: h_600
java.lang.StringIndexOutOfBoundsException: Range [7, 5) out of bounds for length 5
at java.base/jdk.internal.util.Preconditions$1.apply(Preconditions.java:55) ~[na:na]
at java.base/jdk.internal.util.Preconditions$1.apply(Preconditions.java:52) ~[na:na]
at java.base/jdk.internal.util.Preconditions$4.apply(Preconditions.java:213) ~[na:na]
at java.base/jdk.internal.util.Preconditions$4.apply(Preconditions.java:210) ~[na:na]
at java.base/jdk.internal.util.Preconditions.outOfBounds(Preconditions.java:98) ~[na:na]
at java.base/jdk.internal.util.Preconditions.outOfBoundsCheckFromToIndex(Preconditions.java:112) ~[na:na]
at java.base/jdk.internal.util.Preconditions.checkFromToIndex(Preconditions.java:349) ~[na:na]
at java.base/java.lang.String.checkBoundsBeginEnd(String.java:4865) ~[na:na]
2025-11-25T20:35:20.602+09:00 ERROR 32004 --- [RecipeMate] [nio-8080-exec-5] c.r.global.util.ImageUploadUtil          : Failed to delete image from Cloudinary: h_600

java.lang.IllegalArgumentException: Invalid Cloudinary URL: h_600
Caused by: java.lang.StringIndexOutOfBoundsException: Range [7, 5) out of bounds for length 5Caused by: java.lang.StringIndexOutOfBoundsException: Range [7, 5) out of bounds for length 5

2025-11-25T20:35:21.313+09:00 ERROR 32004 --- [RecipeMate] [nio-8080-exec-5] c.r.global.util.ImageUploadUtil          : Failed to delete image from Cloudinary: c_limit
java.lang.RuntimeException: Missing required parameter - public_id2025-11-25T20:35:21.318+09:00 ERROR 32004 --- [RecipeMate] [nio-8080-exec-5] c.r.global.util.ImageUploadUtil          : Failed to extract public_id from URL: q_auto
java.lang.StringIndexOutOfBoundsException: Range [7, 6) out of bounds for length 6
at java.base/jdk.internal.util.Preconditions$1.apply(Preconditions.java:55) ~[na:na]
at java.base/jdk.internal.util.Preconditions$1.apply(Preconditions.java:52) ~[na:na]
at java.base/jdk.internal.util.Preconditions$4.apply(Preconditions.java:213) ~[na:na]
at java.base/jdk.internal.util.Preconditions$4.apply(Preconditions.java:210) ~[na:na]
at java.base/jdk.internal.util.Preconditions.outOfBounds(Preconditions.java:98) ~[na:na]
at java.base/jdk.internal.util.Preconditions.outOfBoundsCheckFromToIndex(Preconditions.java:112) ~[na:na]
at java.base/jdk.internal.util.Preconditions.checkFromToIndex(Preconditions.java:349) ~[na:na]
at java.base/java.lang.String.checkBoundsBeginEnd(String.java:4865) ~[na:na]2025-11-25T20:35:21.321+09:00 ERROR 32004 --- [RecipeMate] [nio-8080-exec-5] c.r.global.util.ImageUploadUtil          : Failed to delete image from Cloudinary: q_auto

java.lang.IllegalArgumentException: Invalid Cloudinary URL: q_autoCaused by: java.lang.StringIndexOutOfBoundsException: Range [7, 6) out of bounds for length 6

2025-11-25T20:35:22.310+09:00  INFO 32004 --- [RecipeMate] [nio-8080-exec-5] c.r.global.util.ImageUploadUtil          : Image deleted from Cloudinary: recipemate/group-purchases/xrtawbat8bfbt5hs3ybx (result: ok)2025-11-25T20:35:22.321+09:00  WARN 32004 --- [RecipeMate] [nio-8080-exec-5] c.r.global.util.ImageUploadUtil          : Empty file at index 0, skipping
2025-11-25T20:35:22.321+09:00  INFO 32004 --- [RecipeMate] [nio-8080-exec-5] c.r.global.util.ImageUploadUtil          : Successfully uploaded 0 out of 1 images (parallel)