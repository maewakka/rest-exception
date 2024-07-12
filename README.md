# RestExceptionHandler 라이브러리 사용 안내

이 라이브러리는 외부 yml 파일을 통해 에러에 대한 내용을 설정하고, 해당 설정에 따라 `BizException`을 처리하는 기능을 제공합니다.

## 설치 및 설정

### 1. 의존성 추가

프로젝트에 필요한 의존성을 추가합니다. `build.gradle`의 예시는 다음과 같습니다:

```groovy
repositories {
	mavenCentral()
	maven {
		url 'https://woonexus.site/repository/woo-maven-repo/'
	}
}

dependencies {
  ...
  implementation 'com.woo:rest-exception:1.0.0'
}
```

### 2. 에러 정보 설정

기본 경로는 resources 폴더 하위에 error/exception.yml 을 참조하도록 설정되어있습니다. 경로는 ErrorConfig 의 setResource 메서드를 통해 지정이 가능합니다.

```yaml
user_not_found:
  status: 404
  message: 유저를 찾을 수 없습니다.
sign_up_fail:
  status: 409
  message: 회원 가입에 실패하였습니다.
login_fail:
  status: 403
  message: 로그인에 실패하였습니다. ID/PW 를 확인하세요.
...
```

key : { status: 상태, message: 메세지 } 형태를 지켜 작성해주시면 됩니다. status 는 반환할 상태코드 번호, message 는 반환할 메세지 입니다.

### 3. Bean 등록

해당 RestExceptionHandler 를 Bean 으로 등록하여주면, Spring Boot 에서 해당 HandlerExceptionResolver 최우선 순위로 적용되어 동작하게 됩니다.

```java
@Configuration
public class WebConfig {
    @Bean
    public RestExceptionHandler setRestExceptionHandler() throws Exception {
        ErrorConfig errorConfig = ErrorConfig.build();

        return RestExceptionHandler.setErrorConfig(errorConfig);
    }
}
```

## 사용 방법

```java
public void login(String id, String password) {

    ...
    if(!checkUser(id, password)) throw new BizException("login_fail");
}
```

BizException 안에 전달할 key 값은 exception.yml 에서 작성한 key를 선택하여 넣으시면 됩니다.

<img width="1372" alt="image" src="https://github.com/user-attachments/assets/4e5ed699-7b6e-496c-94e0-fab458bf35b2">

응답은 위와 같은 형태로 받게 됩니다.
