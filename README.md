# HTML to Image Converter

이 프로젝트는 HTML 템플릿과 JSON 데이터를 사용하여 동적으로 이미지를 생성하는 Spring Boot 기반의 웹 애플리케이션입니다. Playwright를 사용하여 HTML을 렌더링하고, Mustache 템플릿 엔진을 통해 데이터를 HTML에 주입합니다.

## 주요 기능

- **HTML 템플릿 기반 이미지 생성**: 미리 정의된 HTML 템플릿에 동적 데이터를 적용하여 이미지를 생성합니다.
- **JSON 데이터 주입**: `application/json` 형식으로 데이터를 받아 HTML 템플릿에 동적으로 채워 넣습니다.
- **두 가지 API 엔드포인트 제공**:
    1.  생성된 이미지의 URL을 반환하는 API
    2.  생성된 이미지 파일을 직접 반환하는 API
- **외부 템플릿 지원**: 클래스패스에 포함된 기본 템플릿 외에, 외부 파일 시스템에 위치한 템플릿도 사용할 수 있습니다.
- **성능 최적화**: Apache Commons Pool2를 사용한 브라우저 인스턴스 풀링으로 동시 요청 처리 성능을 향상시켰습니다.
- **안정성 개선**:
    - Playwright `Page` 객체 사용 후 명시적으로 닫아 메모리 누수를 방지합니다.
    - Playwright 스크린샷 작업의 기본 타임아웃을 60초로 연장하여 복잡한 페이지 렌더링 시 발생할 수 있는 타임아웃 오류를 줄였습니다.
    - 브라우저 풀 설정이 `application.properties`의 값을 정확히 반영하도록 개선하고, 유휴 브라우저 인스턴스를 주기적으로 제거(Eviction)하여 장기적인 메모리 사용 안정성을 확보했습니다.

## 기술 스택

- **Java 17**
- **Spring Boot 3.x**
- **Playwright**: Headless 브라우저를 제어하여 HTML을 이미지로 캡처
- **Apache Commons Pool2**: 브라우저 인스턴스 풀링을 통한 성능 최적화
- **Mustache**: HTML 템플릿 엔진
- **Gradle**: 의존성 관리 및 빌드

## 이미지 생성 과정

이 애플리케이션은 다음과 같은 과정을 통해 HTML로부터 이미지를 생성합니다.

```
   HTML/CSS/JS
        ↓
Playwright → Chromium 실행
        ↓
Blink (렌더링 엔진)
        ↓
GPU/CPU 메모리 내 화면 렌더링 (Frame Buffer)
        ↓
Playwright가 캡처 요청 → Skia 등으로 이미지 인코딩
        ↓
파일로 저장 (PNG, JPEG 등)
```

1.  **HTML/CSS/JS**: 클라이언트로부터 받은 데이터가 적용된 최종 HTML 콘텐츠입니다.
2.  **Playwright → Chromium 실행**: Playwright가 내부적으로 Chromium 브라우저 인스턴스를 실행합니다.
3.  **Blink (렌더링 엔진)**: Chromium의 렌더링 엔진인 Blink가 HTML, CSS, JS를 해석하여 페이지를 렌더링할 준비를 합니다.
4.  **GPU/CPU 메모리 내 화면 렌더링**: Blink의 렌더링 결과물이 GPU 또는 CPU의 프레임 버퍼(화면을 그리기 위한 메모리 공간)에 그려집니다.
5.  **Playwright가 캡처 요청**: Playwright가 렌더링된 결과물을 이미지로 캡처하도록 명령합니다. 이 과정에서 그래픽 라이브러리인 Skia가 사용될 수 있습니다.
6.  **파일로 저장**: 캡처된 이미지는 PNG, JPEG 등의 형식으로 인코딩되어 파일로 저장되거나 클라이언트에게 직접 전송됩니다.

## API 엔드포인트

### 1. 이미지 URL 반환

- **URL**: `POST /api/images/{templateName}/url`
- **설명**: 지정된 HTML 템플릿과 데이터를 사용하여 이미지를 생성하고, 서버에 저장한 뒤 해당 이미지에 접근할 수 있는 URL을 반환합니다.
- **Request Body**:
  ```json
  {
    "data": {
      "key1": "value1",
      "key2": "value2"
    }
  }
  ```
- **Success Response (200 OK)**:
  ```json
  {
    "imageUrl": "/images/generated/xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx.png"
  }
  ```

### 2. 이미지 파일 반환

- **URL**: `POST /api/images/{templateName}/file`
- **설명**: 지정된 HTML 템플릿과 데이터를 사용하여 이미지를 생성하고, 생성된 이미지 파일을 직접 `image/png` 형식으로 반환합니다.
- **Request Body**:
  ```json
  {
    "data": {
      "key1": "value1",
      "key2": "value2"
    }
  }
  ```
- **Success Response (200 OK)**:
  - `Content-Type`: `image/png`
  - `Content-Disposition`: `attachment; filename="generated-image.png"`
  - **Body**: 이미지 파일의 바이너리 데이터

## 사용 방법

1.  **프로젝트 클론 및 빌드**:
    ```bash
    git clone https://github.com/your-username/buj.git
    cd buj
    ./gradlew build
    ```

2.  **애플리케이션 실행**:
    ```bash
    java -jar build/libs/buj-0.0.1-SNAPSHOT.jar
    ```

3.  **API 요청**:
    - **URL 반환 예시 (cURL)**:
      ```bash
      curl -X POST http://localhost:8080/api/images/report-template/url \
           -H "Content-Type: application/json" \
           -d '{"data": {"title": "월간 보고서", "author": "홍길동"}}'
      ```
    - **파일 반환 예시 (cURL)**:
      ```bash
      curl -X POST http://localhost:8080/api/images/report-template/file \
           -H "Content-Type: application/json" \
           -d '{"data": {"title": "월간 보고서", "author": "홍길동"}}' \
           --output report.png
      ```

## 템플릿 관리

- **기본 템플릿**: `src/main/resources/templates` 디렉토리에 `.html` 파일로 저장됩니다.
- **외부 템플릿**: 애플리케이션 실행 시 `app.template.external-path` 프로퍼티를 설정하여 외부 템플릿 경로를 지정할 수 있습니다.
  ```bash
  java -jar build/libs/buj-0.0.1-SNAPSHOT.jar --app.template.external-path=/path/to/your/templates
  ```
  외부 경로에 템플릿이 존재하면 내부 템플릿보다 우선적으로 사용됩니다.