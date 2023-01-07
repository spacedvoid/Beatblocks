# 명령어

### 공통

`/singleplayer`**:** 싱글플레이어 모드 활성화
    - 사용시 [싱글플레이](./Commands.md#싱글플레이-전용)의 명령어 사용 가능
    - 현재 서버 시작시 자동으로 이 명령어를 실행하게 설정되어 있음([참고](../src/main/java/net/spacedvoid/beatblocks/events/ServerLoadedEvent.java#L11))

`/charts`<br>
&nbsp;&nbsp;&nbsp;&nbsp;`... list`**:** 채보 목록 불러오기<br>
&nbsp;&nbsp;&nbsp;&nbsp;`... query <chart: String>`**:** 채보 정보 불러오기<br>
&nbsp;&nbsp;&nbsp;&nbsp;`... reload`**:** 차트 목록 다시 불러오기<br>

`/board` - 자신의 보드 구조물 위치 표시<br>
&nbsp;&nbsp;&nbsp;&nbsp;`... (singleplayer|multiplayer)`**:** 게임에 사용될 구조물을 불러옴

`/buildresource`**:** 게임에 사용될 리소스팩을 빌드 및 호스팅, 서버 콘솔에서만 실행 가능<br>
&nbsp;&nbsp;&nbsp;&nbsp;`... [includeUnloaded: boolean]`**:** 모든 차트 파일을 불러온 후 빌드(기본값 `true`)

`convert <String: path>`<br>
&nbsp;&nbsp;&nbsp;&nbsp; `path`**:** 변환할 BRC 파일의 경로. 파일 선택 후 Ctrl+Shift+C로 복사한 파일 경로를 그대로 넣는것도 가능

### 싱글플레이 전용
`/beatblocks`<br>
&nbsp;&nbsp;&nbsp;&nbsp;`... singleplayer`<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`... <chart: String>`**:** 해당 채보로 자신의 싱글플레이 게임 시작<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`... <player: Player>`**:** 해당 채보로 해당 플레이어의 싱글플레이 게임 시작<br>
&nbsp;&nbsp;&nbsp;&nbsp;`... stop`**:** 자신의 게임 중지<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`... <player: Player>`**:** 해당 플레이어의 게임 중지<br>

### 디버그 전용
아래 명령어들은 `config.yml` 에서 `debug`를 `true`로 해야 작동

`/parserversion`**:** 차트 파일 구문 분석기의 버전 표시

`/testexception`**:** stacktrace를 표시하는 기능 테스트