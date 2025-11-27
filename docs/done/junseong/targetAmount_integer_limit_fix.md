# 공동구매 금액 입력 제한 문제 해결

## 문제 상황

공동구매 작성 시 목표 금액(targetAmount)에 매우 큰 숫자를 입력하면 다음과 같은 오류가 발생:

```
Failed to convert property value of type 'java.lang.String' to required type 'java.lang.Integer' for property 'targetAmount'; 
For input string: "100000000000000000000000"
```

## 원인 분석

1. **데이터 타입 제한**: `targetAmount` 필드가 `Integer` 타입으로 정의됨
2. **Integer 범위**: Java의 `Integer`는 최대 2,147,483,647 (약 21억)까지만 저장 가능
3. **클라이언트 검증 부재**: HTML 폼에서 최대값 제한이 없어 사용자가 범위를 초과하는 값 입력 가능
4. **서버 검증 부족**: DTO에서 최대값 검증이 없어 런타임 에러 발생

## 해결 방법

### 1. HTML 폼 검증 추가

**파일**: `src/main/resources/templates/group-purchases/form.html`

목표 금액 입력 필드에 최대값 제한 및 안내 메시지 추가:

```html
<input type="number" 
       id="targetAmount" 
       th:field="*{targetAmount}"
       class="form-control" 
       min="0"
       max="2147483647"  <!-- Integer.MAX_VALUE -->
       step="1000"
       placeholder="0"
       required>
```

```html
<div class="form-text">
    <i class="bi bi-info-circle"></i> 최대 21억원까지 입력 가능합니다.
</div>
```

### 2. 서버 측 검증 추가

**파일**: `CreateGroupBuyRequest.java`

```java
@NotNull(message = "총 금액은 필수입니다")
@Min(value = 0, message = "총 금액은 0원 이상이어야 합니다")
@Max(value = 2147483647, message = "총 금액은 21억원을 초과할 수 없습니다")
private Integer targetAmount;
```

**파일**: `UpdateGroupBuyRequest.java`

```java
@NotNull(message = "총 금액은 필수입니다")
@Min(value = 0, message = "총 금액은 0원 이상이어야 합니다")
@Max(value = 2147483647, message = "총 금액은 21억원을 초과할 수 없습니다")
private Integer targetAmount;
```

## 개선 효과

1. **사용자 경험 향상**: 클라이언트 측에서 즉시 입력 제한 확인 가능
2. **명확한 안내**: 최대 입력 가능 금액을 사용자에게 명시
3. **서버 안정성**: 서버 측 검증으로 예외 발생 방지
4. **일관된 검증**: 생성과 수정 모두에 동일한 검증 규칙 적용

## 참고 사항

- Java `Integer` 최대값: 2,147,483,647 (2^31 - 1)
- 만약 21억원 이상의 금액이 필요한 경우, `Integer` → `Long` 타입으로 변경 고려
  - Long 최대값: 9,223,372,036,854,775,807 (약 922경)
  - 단, DB 스키마 및 모든 관련 코드 수정 필요

## 테스트 방법

1. 공동구매 작성 페이지 접속
2. 목표 금액에 2,147,483,647 이상의 값 입력 시도
3. 브라우저에서 입력 제한 확인
4. 서버 측 검증 메시지 확인

## 수정 일자

2025-01-27

