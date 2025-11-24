# Cloudinary 설정 오류 분석 및 해결 방안

## 1. 오류 현상

애플리케이션 실행 시 다음과 같은 `PlaceholderResolutionException`이 발생하며 실행에 실패합니다.

```
org.springframework.util.PlaceholderResolutionException: Could not resolve placeholder 'CLOUDINARY_URL' in value "${CLOUDINARY_URL}"
```

이 오류는 `groupBuyService` → `imageUploadUtil` → `cloudinaryConfig` 빈(Bean)을 생성하는 과정에서 발생하며, 근본 원인은 Spring이 `cloudinary.url` 설정값을 찾지 못하는 데 있습니다.

## 2. 원인 분석

### Spring 설정 파일 구조

1.  **`application.yml` (공통 설정)**
    - 모든 프로필에 공통으로 적용되는 설정 파일입니다.
    - Cloudinary URL이 환경 변수(`CLOUDINARY_URL`)를 참조하도록 설정되어 있습니다.
    ```yaml
    cloudinary:
      url: ${CLOUDINARY_URL}
    ```

2.  **`application-dev.yml` (`dev` 프로필 전용 설정)**
    - `dev` 프로필이 활성화될 때 적용되는 설정 파일입니다.
    - `optional:classpath:application-dev.local.yml`을 통해 로컬 전용 설정 파일을 불러옵니다.
    ```yaml
    spring:
      config:
        import: optional:classpath:application-dev.local.yml
    ```

3.  **`application-dev.local.yml` (로컬 개발 환경 전용)**
    - 실제 민감 정보(API 키, DB 접속 정보 등)를 담는 파일입니다.
    - `.gitignore`에 등록되어 Git에 커밋되지 않습니다.
    - 이 파일에 실제 Cloudinary 접속 URL이 하드코딩되어 있어야 합니다.
    ```yaml
    cloudinary:
      url: cloudinary://<API-KEY>:<API-SECRET>@<CLOUD-NAME>
    ```

### 핵심 원인: `dev` 프로필 비활성화

오류 메시지는 Spring이 `application.yml`에 정의된 `${CLOUDINARY_URL}` 값을 해석하려다 실패했음을 의미합니다. 이는 **`dev` 프로필이 활성화되지 않아** `application-dev.yml`과 `application-dev.local.yml`이 로드되지 않았기 때문입니다.

결과적으로 애플리케이션은 기본(default) 프로필로 실행되어 `application.yml`의 설정을 사용하게 되고, `CLOUDINARY_URL` 환경 변수가 설정되어 있지 않으므로 오류가 발생한 것입니다.

## 3. 해결 방안

로컬 개발 환경에서 애플리케이션을 실행할 때 **`dev` 프로필을 명시적으로 활성화**해야 합니다.

### 방법 1: IDE에서 프로필 활성화 (IntelliJ, STS 등)

가장 간단하고 권장되는 방법입니다.

1.  `Run/Debug Configurations` (실행/디버그 구성) 설정으로 들어갑니다.
2.  **`Active profiles`** 또는 **`활성 프로필`** 항목을 찾습니다.
3.  입력란에 `dev`를 입력하고 저장합니다.
4.  애플리케이션을 다시 실행합니다.

### 방법 2: Gradle Task로 실행 시 프로필 설정

`build.gradle` 파일에 `bootRun` Task의 인자(argument)로 프로필을 설정할 수 있습니다.

```groovy
// build.gradle
bootRun {
    args = ["--spring.profiles.active=dev"]
}
```

또는 터미널에서 직접 실행할 경우 다음과 같이 인자를 전달합니다.

```shell
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 방법 3: `application.properties` 또는 `application.yml`에 직접 설정

`src/main/resources/application.properties` 파일을 생성하거나 `application.yml`에 추가하여 활성화할 프로필을 지정할 수 있습니다. 하지만 이 방법은 다른 환경(예: `prod`)으로 전환 시 수정이 필요하므로 유연성이 떨어져 권장되지 않습니다.

```yaml
# application.yml
spring:
  profiles:
    active: dev
```

## 4. 추가 확인 사항

- **`src/main/resources/application-dev.local.yml` 파일 확인**: 해당 파일이 정확한 위치에 있고, 내부에 `cloudinary.url`이 올바르게 작성되어 있는지 다시 한번 확인하십시오.
- **환경 변수 사용**: 만약 `.env` 파일을 통해 환경 변수를 주입하는 방식을 사용한다면, `dev` 환경용 `.env.dev` 파일에 `CLOUDINARY_URL=...` 값이 올바르게 설정되어 있는지 확인해야 합니다. 하지만 현재 프로젝트 구조에서는 `application-dev.local.yml`을 사용하는 것이 더 자연스럽습니다.
