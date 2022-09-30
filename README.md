# Beatblocks
마인크래프트에서 리듬게임 만들기

사용방법 및 튜토리얼은 천천히 업데이트 예정

~~난 왜 처음에 이따구로 만들어서 디버깅을 못하게 했을까~~

-----

## 명령어

#### 공통

- `/singleplayer` - 싱글플레이어 모드 활성화
  - 사용시 [싱글플레이](README.md?readme=1#싱글플레이-전용)의 명령어 사용 가능
  - 현재 서버 시작시 자동으로 이 명령어를 실행하게 설정되어 있음([참고](src/main/java/net/spacedvoid/beatblocks/common/events/ServerLoadedEvent.java#L11))
  
- `/beatblocks charts`
  - `..list` - 차트의 목록 불러오기
  - `..query <chart>` - 차트의 정보 불러오기
  - `..reload` - 차트 목록 다시 불러오기
  
- `/beatblocks board <board>` - `<board>`**:** `singleplayer`, `multiplayer`(현재 미지원) 
  - 게임에 사용될 구조물을 불러옴
  
- `/buildresource` - 게임에 사용될 리소스팩을 빌드
  - 콘솔창을 통해 실행 가능하나, 현재 테스트가 필요함.
  
 #### 싱글플레이 전용
 - `/beatblocks game start [player]` - 명령어를 실행한 플레이어 또는 `[player]` 플레이어로 싱글플레이 게임 시작
   - 게임은 현재 테스트가 필요해 정상적으로 작동하지 않을 수 있음
   
#### 디버그 전용
아래 명령어들은 `config.yml` 에서 `debug`를 `true`로 해야 작동

- `/beatblocks parserversion` - 차트 파일 구문 분석기의 버전 표시

- `/testexception` - 고의로 발생된 예외의 stacktrace를 표시
  - stacktrace를 표시하는 기능의 테스트 목적
  
-----  

## TODO

- 차트 파일의 구조 설명
- 차트 파일 및 음악 파일을 저장하는 방법 설명
