# 파일 구조

```
plugins                      // 서버의 플러그인 폴더
┗ Beatblocks                 // 데이터 폴더
  ┣ charts                   // 채보 목록 폴더
  ┃ ┣ chart_1                // 채보 폴더
  ┃ ┃ ┣ main.bic             // BIC 채보 파일
  ┃ ┃ ┗ sound1.ogg           // 채보 파일에 명시된 소리 파일
  ┃ ┣ chart_2                // 채보 폴더
  ┃ ┃ ┣ main.bic             // BIC 채보 파일
  ┃ ┃ ┣ sound2.ogg           // 채보 파일에 명시된 소리 파일
  ┃ ┃ ┗ sound3.ogg           // 채보 파일에 명시되지 않은 소리 파일
  ┃ ·
  ┃ ·
  ┃ ·
  ┣ ngrok                     // ngrok 폴더
  ┃ ┣ ngrok.exe               // ngrok 실행파일
  ┃ ┣ ngrok.yml               // ngrok 설정파일
  ┣ out                       // 리소스팩 저장 폴더
  ┃ ┗ beatblocks-resource.zip // 압축된 리소스팩 파일
  ┣ resourcepack              // 리소스팩 폴더
  ┃ ┣ ...
  ┃ .
  ┃ .
  ┃ .
  ┣ config.yml                // Beatblocks 설정 파일
```

## 구조

- `Beatblocks/charts/...`**:** 채보 폴더
   - 이 폴더에는 BIC 채보 파일 하나와 .ogg 음악 파일 하나가 들어감
   - 각 채보 폴더의 이름이 인게임에서 사용될 "채보 이름" (혹은 ID)가 됨 (위 예시에서는 각각 "chart_1", "chart_2")
   - 각 채보 폴더마다 BIC 채보 파일은 1개씩만 포함되어야 함. 그렇지 않으면 해당 폴더는 채보 파일의 목록을 불러올 때 무시됨
   - 채보 파일에 명시된 소리 파일이 반드시 포함되어야 함. 그렇지 않으면 채보 파일을 불러오지 않음.

- `Beatblocks/ngrok`**:** ngrok 폴더
  - ngrok는 생성된 리소스팩을 배포하는데 사용됨
  - https://ngrok.com 참고

- `Beatblocks/out`**:** 리소스팩 저장 폴더
  - 생성된 리소스팩이 모두 여기 저장됨
  - ngrok로 리소스팩을 배포할 때 루트 폴더가 됨

- `Beatblocks/resourcepack`**:** 리소스팩 폴더
  - 압축하지 않은 상태의 리소스팩 폴더
  - 압축된 리소스팩은 리소스팩 저장 폴더로 들어감