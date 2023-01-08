# BRC 파일

BRC(Beatblocks Raw Chart) 파일은 YAML 형식으로 작성하는 채보 파일로서, 이 튜토리얼은 인게임 에디터(TBD)가 아닌, 텍스트 파일 자체를 작성하는 데 필요합니다.<br>
YAML 형식에 맞춰 작성하며, 올바르지 않은 형식의 경우 Beatblocks가 해석할 수 없습니다.<br>
[`convert` 명령어](Commands.md)로 파일을 BIC로 변환할 수 있으며, 파일에 문제가 있다면 오류가 발생합니다.

```
format: RAW-<version>
sound-file: <String>
song: <String>
artist: <String>
creator: <String>
difficulty: <double>
bpm: <double>
time: <Time>
offset: <int>
keys: <int>
unit: <int>
chart: 
  - beats: <int>
    lane: <int>
  :
  :
```

### 타입

- `version`**:** `x.y` 꼴의 채보 파일 버전. 현재 `1.0`
- `String`**:** 문자열, 쌍따옴표( " " ) 안에 작성
- `int`**:** 정수
- `double`**:** 소수
- `boolean`**:** `true` 또는 `false`
- `Time`**:** `mm:ss`꼴의 시간 표현, `String`처럼 사용
  - ex) `"15:32"` - 15분 32초
  - `00:00` 또는 음수의 시간은 표현 불가

---
### 구성요소

<table>
    <thead>
        <tr>
            <th>키</th>
            <th>설명</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <th>format</th>
            <td>채보 파일의 형식. 기본값 <code>RAW-1.0</code></td>
        </tr>
        <tr>
            <th><nobr>sound-file</nobr></th>
            <td><ul><li>사용할 음악 파일의 이름</li><li>채보 파일과 같은 폴더에 존재해야 함</li><li>ogg 형식의 파일이어야 하며, 확장자( .ogg )를 제외함</li></ul></td>
        </tr>
        <tr>
            <th>song</th>
            <td>음악 제목</td>
        </tr>
        <tr>
            <th>artist</th>
            <td>작곡가의 이름</td>
        </tr>
        <tr>
            <th>creator</th>
            <td><ul><li>채보 작성자의 이름</li><li>마인크래프트 닉네임으로 하는 것을 추천</li></ul></td>
        </tr>
        <tr>
            <th>difficulty</th>
            <td><ul><li>난이도, 0.1 부터 10 사이의 소수</li><li><code>x.y</code> 꼴의 정수 부분 한 자리, 소수 부분 한 자리 (소수 부분은 생략 가능)</li></ul></td>
        </tr>
        <tr>
            <th>bpm</th>
            <td><ul><li>음악의 BPM</li><li>실제 플레이에서 변경되더라도 음악 내에서 유지되는 기본 BPM을 사용</li><li>게임플레이 중 노트 생성에는 관여하지 않음</li></ul></td>
        </tr>
        <tr>
            <th>time</th>
            <td>음악의 길이</td>
        </tr>
        <tr>
            <th>keys</th>
            <td><ul><li>사용할 키의 개수를 표시</li><li>가능한 값은 2, 4, 6, 8</li><li>각 값별 사용하는 라인<ul><li>2<b>:</b> 4 ~ 5</li><li>4<b>:</b> 3 ~ 6</li><li>6<b>:</b> 2 ~ 7</li><li>8<b>:</b> 1 ~ 8</li></ul></li></ul></td>
        </tr>
        <tr>
            <th>unit</th>
            <td><ul><li>아래 <code>chart</code>에서 사용할 `beats` 키에 대해, "한 박자"를 정의할 단위</li><li>예를 들어 100이라면 한 박자는 100, 반 박자는 50 이 됨</li></ul></td>
        </tr>
        <tr>
            <th>chart</th>
        <td>
            <ul>
                <li>구성 요소는 `beats`, `lane`</li>
                <li>`beats` : 이 노트 이후 다음 노트가 생성될때까지 기다릴 박자
                    <ul><li>`unit`을 기준으로 함</li></ul>
                <li>라인 : 노트가 내려올 라인
                    <ul>
                        <li>0~3 또는 5~8 까지의 정수값</li>
                        <li>순서대로 0<b>:</b> 1번 라인, 1<b>:</b> 2번 라인, <b>···</b> , 3<b>:</b> 4번 라인, 5<b>:</b> 5번 라인, <b>···</b> , 8<b>:</b> 8번 라인</li>
                        <li>`keys`에 명시된 사용할 라인을 벗어날 수 없음</li>
                    </ul>
                <li>각 구성 요소가 파일 끝까지, 각 노트마다 한 줄씩 표현</li>
            </ul>
        </td>
      </tr>
    </tbody>
</table>