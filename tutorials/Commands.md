# 명령어

---

### 공통

- `/singleplayer`**:** 싱글플레이어 모드 활성화
    - 사용시 [싱글플레이](./Commands.md#싱글플레이-전용)의 명령어 사용 가능
    - 현재 서버 시작시 자동으로 이 명령어를 실행하게 설정되어 있음([참고](../src/main/java/net/spacedvoid/beatblocks/common/events/ServerLoadedEvent.java#L11))

- `/charts`
    - `... list`**:** 채보 목록 불러오기
    - `... query <chart: String>`**:** 채보 정보 불러오기
    - `... reload`**:** 차트 목록 다시 불러오기

- `/board` - 자신의 보드 구조물 위치 표시
    - `...<boardType: String>`**:** `singleplayer`, `multiplayer`(현재 미지원)
    - 게임에 사용될 구조물을 불러옴

- `/buildresource`**:** 게임에 사용될 리소스팩을 빌드 및 호스팅, 서버 콘솔에서만 실행 가능
    - `... [includeUnloaded: boolean]`**:** 모든 차트 파일을 불러온 후 빌드(기본값 `true`)

### 싱글플레이 전용
- `/beatblocks`
  - `... singleplayer`
    - `... <chart: String>`**:** 해당 채보로 자신의 싱글플레이 게임 시작
      - `... <player: Player>`**:** 해당 채보로 해당 플레이어의 싱글플레이 게임 시작
  - `... stop`**:** 자신의 게임 중지
    - `... <player: Player>`**:** 해당 플레이어의 게임 중지

### 디버그 전용
아래 명령어들은 `config.yml` 에서 `debug`를 `true`로 해야 작동

- `/parserversion`**:** 차트 파일 구문 분석기의 버전 표시

- `/testexception`**:** 고의로 발생된 예외의 stacktrace를 표시
    - stacktrace를 표시하는 기능의 테스트 목적