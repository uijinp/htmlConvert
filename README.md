# HTML to Image Converter

이 프로젝트는 HTML 템플릿과 JSON 데이터를 사용하여 동적으로 이미지를 생성하는 Spring Boot 기반의 웹 애플리케이션입니다. Playwright를 사용하여 HTML을 렌더링하고, Mustache 템플릿 엔진을 통해 데이터를 HTML에 주입합니다.

## 주요 기능

- **HTML 템플릿 기반 이미지 생성**: 미리 정의된 HTML 템플릿에 동적 데이터를 적용하여 이미지를 생성합니다.
- **JSON 데이터 주입**: `application/json` 형식으로 데이터를 받아 HTML 템플릿에 동적으로 채워 넣습니다.
- **두 가지 API 엔드포인트 제공**:
    1.  생성된 이미지의 URL을 반환하는 API
    2.  생성된 이미지 파일을 직접 반환하는 API
- **외부 템플릿 지원**: 클래스패스에 포함된 기본 템플릿 외에, 외부 파일 시스템에 위치한 템플릿도 사용할 수 있습니다.
- **흐름**
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

## 기술 스택

- **Java 17**
- **Spring Boot 3.x**
- **Playwright**: Headless 브라우저를 제어하여 HTML을 이미지로 캡처
- **Mustache**: HTML 템플릿 엔진
- **Gradle**: 의존성 관리 및 빌드

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
