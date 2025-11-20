# 공구 이미지 수정 버그 분석 및 해결 방안

## 1. 문제 상황

공동구매(Group Buy) 게시글을 수정할 때, 기존에 업로드된 이미지를 삭제하고 새로운 이미지를 추가하면 변경 사항이 적용되지 않는 버그가 발생합니다.

- **전제 조건**: 이미지가 3장 업로드된 공구 게시글이 존재합니다.
- **재현 과정**:
    1. 해당 공구의 '수정' 페이지로 이동합니다.
    2. 기존 이미지 중 일부 또는 전체를 삭제합니다.
    3. 새로운 이미지를 추가합니다.
    4. '수정 완료' 버튼을 누릅니다.
- **기대 결과**: 삭제된 이미지는 사라지고, 새로 추가된 이미지가 정상적으로 보입니다.
- **실제 결과**: 이미지 변경 사항이 전혀 적용되지 않고, 기존 이미지가 그대로 유지됩니다. 서버 로그에는 `IllegalArgumentException: Invalid Cloudinary URL` 오류와 함께 `이미지는 최대 3장까지만 업로드할 수 있습니다.` 라는 후속 오류가 기록됩니다.

## 2. 근본 원인 분석

이 문제의 핵심 원인은 **Cloudinary 이미지 URL에서 `public_id`를 추출하는 로직의 결함**에 있습니다.

### 상세 분석

1.  **이미지 삭제 요청**: 공구 수정 시, 삭제하도록 선택된 이미지들의 URL 목록(`deletedImages`)이 `GroupBuyService`의 `updateGroupBuy` 메서드로 전달됩니다. 이 URL은 Cloudinary의 동적 변환 파라미터(`w_800,h_600,...`)를 포함하고 있습니다.
    - 예시 URL: `.../upload/w_800,h_600,c_limit,q_auto,f_auto/v1763642710/recipemate/group-purchases/abc.png`

2.  **`public_id` 추출 실패**: `updateGroupBuy` 메서드는 `ImageUploadUtil.deleteImages`를 호출하고, 이 메서드는 각 URL에 대해 `extractPublicIdFromUrl` 메서드를 실행하여 Cloudinary에서 이미지를 삭제하는 데 필요한 `public_id`를 추출하려고 시도합니다.

3.  **결함이 있는 파싱 로직**: `extractPublicIdFromUrl` 내부 로직은 URL 경로에 "recipemate"라는 문자열이 포함될 것을 가정하고, 그 이후의 경로만을 조합하여 `public_id`를 생성합니다. 이 방식은 URL에 동적 변환 파라미터(`w_800...`)가 포함된 경우, 해당 파라미터와 버전 정보(`v1763...`)를 제대로 처리하지 못하고 건너뛰게 됩니다. 결과적으로 잘못된 `public_id`가 생성되거나 `IllegalArgumentException`이 발생하여 Cloudinary 이미지 삭제에 실패합니다.

4.  **후속 오류 발생**:
    - Cloudinary에서 이미지 삭제가 실패했기 때문에, 데이터베이스에서도 관련 `GroupBuyImage` 레코드가 삭제되지 않습니다.
    - 이후, 새로 추가된 이미지를 업로드하고 DB에 저장하려는 시점에 (삭제되지 않은 기존 이미지 수 + 새로 추가된 이미지 수)가 최대 허용 개수(3개)를 초과하게 됩니다.
    - 이로 인해 `CustomException(ErrorCode.IMAGE_COUNT_EXCEEDED)` 예외가 발생하고, 전체 트랜잭션이 롤백됩니다.

결론적으로, 이미지 삭제 실패가 모든 변경(이미지 추가, 게시글 내용 수정 등)이 원상 복구되는 원인입니다.

## 3. 해결 방안

`ImageUploadUtil.extractPublicIdFromUrl` 메서드의 `public_id` 추출 로직을 보다 안정적인 방식으로 수정해야 합니다.

### 제안되는 수정 로직

Cloudinary URL 구조는 `.../upload/` 다음에 `{변환 파라미터}/{버전}/{public_id}.{확장자}` 순서로 구성됩니다. `public_id`는 버전 정보(`v`로 시작하는 숫자)와 파일 확장자 사이에 위치합니다.

이 구조를 이용하여 다음과 같이 로직을 개선할 수 있습니다.

1.  URL 문자열에서 버전 정보 (`/v` + 숫자 10자리)의 시작 인덱스를 찾습니다.
2.  URL 문자열에서 마지막 파일 확장자 (`.png`, `.jpg` 등)의 시작 인덱스를 찾습니다.
3.  버전 정보의 끝(`.../v1234567890/` 다음)부터 파일 확장자 앞까지의 문자열을 `public_id`로 추출합니다.

### 수정 코드 예시 (`ImageUploadUtil.java`)

```java
private String extractPublicIdFromUrl(String imageUrl) {
    try {
        // "/upload/" 이후의 경로 추출
        String path = imageUrl.substring(imageUrl.indexOf("/upload/") + 8);

        // 버전 정보(v로 시작하는 10자리 숫자) 제거
        // 예: w_800,h_600/v1234567890/folder/image.png -> folder/image.png
        String pathWithoutVersion = path.replaceAll("v\\d{10}/", "");

        // 변환 파라미터가 있는 경우(첫 세그먼트에 '_' 포함) 제거
        // 예: w_800,h_600/folder/image.png -> folder/image.png
        if (pathWithoutVersion.contains("/") && pathWithoutVersion.substring(0, pathWithoutVersion.indexOf('/')).contains("_")) {
            pathWithoutVersion = pathWithoutVersion.substring(pathWithoutVersion.indexOf('/') + 1);
        }

        // 확장자 제거
        int lastDotIndex = pathWithoutVersion.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return pathWithoutVersion.substring(0, lastDotIndex);
        }

        return pathWithoutVersion; // 확장자가 없는 경우

    } catch (Exception e) {
        log.error("Failed to extract public_id from URL: {}", imageUrl, e);
        throw new IllegalArgumentException("Invalid Cloudinary URL: " + imageUrl, e);
    }
}
```

이 로직은 URL에 변환 파라미터나 버전 정보가 있든 없든, 더 안정적으로 `public_id`를 추출할 수 있어 근본적인 문제 해결이 가능합니다.
