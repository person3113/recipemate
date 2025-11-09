# 공동구매 상태 및 수정 로직 개선 계획 

## 1. 문제 분석

참고 로그
```text
- 공동구매 목표 인원 달성하면 http://localhost:8080/group-purchases/577 같은 상세페이지에서는 마감으로 표시되는데. 그리고 http://localhost:8080/users/me/group-purchases 에서도 상태가 마감으로 되는데. http://localhost:8080/group-purchases/list 에서는 모집중이라고 표시됨. 
	- 그리고 상세페이지에서 주최자 메뉴에 "공구 수정"을 해서 수정하면. 다시 상태가 모집중으로 나타남. 상세페이지에서도 내가 만든 공구에서도.

- db에서 GROUP_BUYS  중에 한 행이 다음과 같아.
161	2025-11-07 23:33:44.839009	null	2025-11-09 14:34:45.94696	VEGETABLE	Moroccan Carrot Soup 레시피에 필요한 재료들을 공동구매합니다!
아래 재료들을 함께 구매하면 더 저렴하게 구입할 수 있습니다.	0	2025-11-09 14:20:00	BOTH	FALSE		1000	meal-53047	https://www.themealdb.com/images/media/meals/jcr46d1614763831.jpg	Moroccan Carrot Soup	CLOSED	2	Moroccan Carrot Soup 재료 공동구매	1000	5	1	필요한 재료:

즉 db하고 http://localhost:8080/group-purchases/161 상세 정보에서는 마감이라고 되어있는데. http://localhost:8080/group-purchases/list 에서는 마감 임박으로 표시되네. 또 db상에서는 마감일이 2025-11-09 14:20:00 이렇게 저장되어있는데. 지금 시점 11.09.16시거든. 그래서 마감이 된 거 같은데. 보다싶이 list 화면에서는 마감 임박으로 되어있는데. 왜 그렇지? 
<div class="card-body">
                        <!-- 제목과 상태 배지 (실시간 계산) -->
                        <div class="d-flex justify-content-between align-items-start mb-2">
                            <h5 class="card-title mb-0">
                                <a href="/group-purchases/161" class="text-decoration-none text-dark">Moroccan Carrot Soup 재료 공동구매</a>
                            </h5>
                            <!-- 실시간 상태 계산 -->
                            
                                <span class="badge bg-warning">마감 임박</span>
                            
                        </div>
                        
                        <!-- 내용 -->
                        <p class="card-text text-muted small">Moroccan Carrot Soup 레시피에 필요한 재료들을 공동구매합니다!

아래 재료들을 함께...</p>
                        
                        <!-- 카테고리 -->
                        <div class="mb-2">
                            <span class="badge bg-light text-dark border">
                                <i class="bi bi-tag"></i> <span>VEGETABLE</span>
                            </span>
                        </div>
                        
                        <!-- 가격 정보 -->
                        <div class="mb-2">
                            <div class="d-flex justify-content-between align-items-center">
                                <span class="text-primary fw-bold">
                                    총 <span>1,000</span>원
                                </span>
                                <span class="text-success fw-bold">
                                    1인당 <span>500</span>원
                                </span>
                            </div>
                        </div>
                        
                        <!-- 참여 인원 -->
                        <div class="mb-2">
                            <span class="text-muted small">
                                <i class="bi bi-people"></i>
                                <span>0</span> / <span>2</span>명
                            </span>
                        </div>
                        
                        <!-- 마감일 -->
                        <div class="mb-2">
                            
                                <span class="text-muted small">
                                    <i class="bi bi-calendar-event"></i>
                                    <span>
                                        오늘 마감 🔥
                                    </span>
                                </span>
                            
                        </div>
                        <!-- 수령 방법 -->
                        <div class="mb-2">
                            <span class="text-muted small">
                                <i class="bi bi-truck"></i>
                                    <span>
                                        <span></span> 또는 택배
                                    </span>
                            </span>
                        </div>
                        
                        <!-- 주최자 정보 -->
                        <div class="text-muted small">
                            <i class="bi bi-person"></i> <span>관리자</span>
                        </div>
                    </div>

- 내가 알기로 마감 1일 전? 아님 2일 전? 이 때부터 공구 수정 막아놓은 걸로 기억하는데. errorcode에 있는지 보고. 그랬을 때 버튼도 안 보이게 하고. 백엔드에서도 에러내기. 

```

### 1.1. 상태 표시 불일치 (목록 vs. 상세)

- **현상**: 데이터베이스와 상세 페이지에서는 `CLOSED` 상태인 공동구매가, 목록 페이지(`list.html`)에서는 "마감 임박"으로 잘못 표시됩니다.
- **원인**: **프론트엔드 템플릿의 자체적인 상태 계산 로직**이 문제입니다. `list.html` 파일은 백엔드에서 전달된 `groupBuy.status` 값을 무시하고, 오직 마감일(`deadline`)만을 기준으로 상태를 "실시간 계산"하여 표시합니다. 이 로직은 '목표 인원 달성'이나 '주최자에 의한 수동 마감'과 같은 다른 `CLOSED` 조건을 고려하지 않기 때문에, DB 상태와 다른 잘못된 정보를 보여주게 됩니다.

### 1.2. 마감된 공동구매 수정 시 상태 변경 오류

- **현상**: `CLOSED` 상태의 공동구매를 수정하면, 상태가 다시 "모집중"으로 변경됩니다.
- **원인**: `GroupBuyService.updateGroupBuy` 메서드에서 업데이트가 성공하면, 기존 상태를 무시하고 새로운 마감일에 따라 상태를 `RECRUITING` 또는 `IMMINENT`로 무조건 재계산합니다. 이로 인해 이미 마감된 공동구매가 다시 열리는 문제가 발생합니다.

### 1.3. 마감 임박/마감된 공동구매 수정 제한 부재

- **현상**: 마감이 임박했거나 이미 마감된 공동구매를 주최자가 수정할 수 있습니다.
- **원인**: `GroupBuyService.updateGroupBuy` 메서드에는 수정을 허용하기 전에 공동구매의 상태를 확인하는 유효성 검사 로직이 없습니다. 주최자인지 여부만 확인할 뿐, `CLOSED` 또는 `IMMINENT` 상태인지 확인하지 않아 이미 종료되었거나 곧 종료될 공동구매를 수정할 수 있게 됩니다.

## 2. 해결 방안

위 문제들을 해결하기 위해 백엔드와 프론트엔드 양쪽에서 수정이 필요합니다.

### 2.1. 프론트엔드 수정 계획 

#### 2.1.1. `list.html`의 상태 표시 로직 수정

- **"실시간 계산" 로직 제거**:
    - `src/main/resources/templates/group-purchases/list.html` 파일에서 마감일 기반으로 상태를 계산하는 `th:block` 부분을 완전히 제거합니다.
- **백엔드 `status` 값 직접 사용**:
    - 대신, 백엔드에서 전달된 `groupBuy.status` 값을 직접 사용하여 상태 배지를 표시하도록 수정합니다. 이를 통해 데이터베이스의 상태와 항상 일치하게 됩니다.

    ```html
    <!-- 수정 후 예시 -->
    <span class="badge"
          th:classappend="${groupBuy.status.name() == 'RECRUITING'} ? 'bg-success' : (${groupBuy.status.name() == 'IMMINENT'} ? 'bg-warning' : 'bg-secondary')"
          th:text="${groupBuy.status.displayName}">
    </span>
    ```

### 2.2. 백엔드 수정 계획 (안정성 강화)

#### 2.2.1. `GroupBuyService.updateGroupBuy` 메서드 개선

1.  **수정 전 상태 유효성 검사 추가**:
    - `updateGroupBuy` 메서드 시작 부분에 공동구매의 현재 상태를 확인하는 로직을 추가합니다.
    - 만약 상태가 `CLOSED`일 경우, `CustomException(ErrorCode.CANNOT_MODIFY_CLOSED_GROUP_BUY)`와 같은 명시적인 예외를 발생시켜 수정을 차단합니다. (필요 시 `ErrorCode`에 추가)
    - 마감 1~2일 전 수정을 막는 정책이라서, `IMMINENT` 상태일 때도 동일하게 예외를 발생시킵니다.

2. **상태 업데이트 로직 수정**:
    - 공동구매 정보 수정 후, 상태를 무조건 재계산하는 로직을 수정합니다.
    - `groupBuy.updateStatus(...)`를 호출하기 전에, 현재 상태가 `CLOSED`가 아닐 경우에만 `determineStatus`를 호출하여 상태를 재계산하도록 조건문을 추가합니다.
    ```java
        // 3.1. 상태 검증 (마감 또는 마감 임박 시 수정 불가)
        if (groupBuy.getStatus() == GroupBuyStatus.CLOSED || groupBuy.getStatus() == GroupBuyStatus.IMMINENT) {
            throw new CustomException(ErrorCode.CANNOT_MODIFY_GROUP_BUY);
        } 
    ```

### 2.3. 프론트엔드 수정 계획 (UX 개선)

#### 2.3.1. `detail.html`의 수정 버튼 조건부 렌더링

- **수정 버튼 표시 조건 강화**:
    - `src/main/resources/templates/group-purchases/detail.html` 파일에서 '공구 수정' 버튼이 주최자이면서 동시에 공동구매 상태가 `RECRUITING`일 때만 표시되도록 `th:if` 조건을 수정합니다.
    - `th:if="${isOwner and groupBuy.status.name() == 'RECRUITING'}"`

## 3. 기대 효과

- **데이터 정합성**: 공동구매 목록과 상세 페이지의 상태가 항상 데이터베이스와 일치하여 사용자에게 정확한 정보를 제공합니다.
- **시스템 안정성**: 마감되었거나 임박한 공동구매의 정보가 수정되거나 상태가 의도치 않게 변경되는 것을 원천적으로 방지하여 시스템의 안정성을 높입니다.
- **UX 개선**: 수정할 수 없는 공동구매에 대해 '수정' 버튼을 숨김으로써 사용자에게 혼란을 주지 않습니다.