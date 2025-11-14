# 공구 상태 (GroupBuyStatus) 업데이트 로직 개선 계획

## 1. 문제 상황

- **현상:** 공구(GroupBuy)의 마감일(`deadline`)을 현재로부터 이틀 이내 (예: 몇 시간 뒤)로 설정하거나 수정해도, 상태(`status`)가 `IMMINENT`(마감 임박)로 변경되지 않고 `RECRUITING`(모집중)으로 유지됩니다.
- **원인:** `codebase_investigator` 분석 결과, 마감일에 따라 상태를 `IMMINENT`로 변경하는 로직이 `GroupBuyScheduler`의 `updateImminentGroupBuys` 메서드에만 존재합니다. 이 스케줄러는 매일 자정에만 동작하므로, 공구 생성 및 수정 시점에는 해당 로직이 실행되지 않습니다. `GroupBuyService`에서는 공구 생성 시 상태를 `RECRUITING`으로 고정하고, 수정 시에는 상태를 재계산하지 않는 것이 문제입니다.

## 2. 해결 방안

`GroupBuyService`에 마감일을 기반으로 상태를 결정하는 로직을 추가하여, 공구 생성 및 수정 시점에 즉시 올바른 상태가 설정되도록 개선합니다. 스케줄러는 현재처럼 매일 상태를 보정하는 역할(fallback)을 계속 수행합니다.

## 3. 상세 계획

### 3.1. `GroupBuyService`에 상태 결정 로직 추가

`GroupBuyService.java` 파일에 마감일을 인자로 받아 적절한 `GroupBuyStatus`를 반환하는 private 메서드를 추가합니다. 이 로직은 기존 `GroupBuyScheduler`의 로직을 재사용합니다.

```java
// GroupBuyService.java

private GroupBuyStatus determineStatus(LocalDateTime deadline) {
    if (deadline.isBefore(LocalDateTime.now().plusDays(2))) {
        return GroupBuyStatus.IMMINENT;
    }
    return GroupBuyStatus.RECRUITING;
}
```

### 3.2. 공구 생성(`createGroupBuy`) 로직 수정

`createGroupBuy` 메서드에서 `GroupBuy` 엔티티를 생성한 후, 위에서 만든 `determineStatus` 메서드를 호출하여 초기 상태를 설정합니다.

- `groupBuy.updateStatus()` 와 같은 메서드를 호출하여 상태를 업데이트합니다. (`GroupBuy.java`를 보면 `updateStatus`가 존재합니다.)

```java
// GroupBuyService.java - createGroupBuy 메서드 내

// ... GroupBuy 엔티티 생성 후 ...
GroupBuyStatus initialStatus = determineStatus(request.getDeadline());
groupBuy.updateStatus(initialStatus);

groupBuyRepository.save(groupBuy);
// ...
```

### 3.3. 공구 수정(`updateGroupBuy`) 로직 수정

`updateGroupBuy` 메서드에서 `groupBuy.update(...)`를 통해 엔티티의 정보가 업데이트된 후, 새로운 마감일을 기준으로 상태를 다시 계산하고 업데이트합니다.

```java
// GroupBuyService.java - updateGroupBuy 메서드 내

// ... groupBuy.update(request) 호출 후 ...
GroupBuyStatus updatedStatus = determineStatus(groupBuy.getDeadline());
groupBuy.updateStatus(updatedStatus);

// ...
```

## 4. 기대 효과

- 사용자가 공구를 생성하거나 마감일을 수정하는 즉시, 마감일이 이틀 이내라면 상태가 `IMMINENT`로 정확하게 표시됩니다.
- 실시간으로 상태가 반영되어 사용자 경험이 개선됩니다.
- 기존 스케줄러는 매일 자정마다 상태를 동기화하는 보조적인 역할을 계속 수행하여 데이터 정합성을 유지합니다.
