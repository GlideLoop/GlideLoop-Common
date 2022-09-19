# GlideLoop-Common

GlideLoop-Common은 GlideLoop의 기초 유틸리티 라이브러리입니다.

GlideLoop 프로젝트 내에서는 커먼으로 지칭합니다.<br><br>

### 이 라이브러리는 무슨 역할을 하나요?
커먼은 GlideLoop 프로젝트의 기초 유틸리티 라이브러리입니다.

[코어](https://github.com/GlideLoop/GlideLoop-Core)가 GlideLoop 프로젝트간의 다리 역할을 맡는다면, 커먼은 실질적인 유틸리티 라이브러리를 담당합니다.<br><br>

### 커먼은 코어 라이브러리인가요?
네.
GlideLoop는 [코어](https://github.com/GlideLoop/GlideLoop-Core)를 메인 코어로 가지지만, 코어는 코틀린 런타임 외 다른 라이브러리를 참조하지 않으며, 호환 기능만을 가지도록 설계된 라이브러리입니다.

그렇기에, 일반적인 GlideLoop 프로젝트의 메인 라이브러리들은 커먼을 기준으로 설계됩니다.<br><br>

### 커먼은 어떠한 기능을 가지고 있나요?
리플렉션을 제외한 일반적인 유틸리티 기능을 담당합니다.<br>
특수한 기능의 경우, 다른 라이브러리로 분리됩니다.

