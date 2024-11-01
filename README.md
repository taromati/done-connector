# Minecraft Chzzk / AfreecaTV 후원 연동 플러그인

## **지원 버전**
Java 버전: 21 이상

※ Java 17 버전은 [1.7.2 버전](https://github.com/taromati/done-connector/releases/download/1.7.2/done-connector-1.7.2.jar)을 이용해 주세요. 1.7.2 버전은 마인크래프트 1.20.4까지만 지원 됩니다.

마인크래프트 서버 버전: Paper 1.18 ~ 1.21.3

## **빌드 방법**

1. 터미널에서 `gradlew jar` 실행

## **다운로드 방법**
1. Github(https://github.com/taromati/done-connector) 의 Release 에서 [다운로드](https://github.com/taromati/done-connector/releases/download/1.8.0/done-connector-1.8.0.jar)

## **실행 방법**

1. plugins 폴더에 done-connector-1.8.0.jar 파일을 넣고 마인크래프트 서버를 1회 실행 후 종료
2. plugins 폴더에서 done-connector/config.yml 파일 수정
3. 마인크래프트 서버 실행


## **사용 방법**

* 플러그인 적용 후 서버 실행시 자동으로 기능 활성화
* `/done [on|off|reconnect|reload]` 명령어로 기능 제어
* `/done on` 후원자 연동 기능 활성화
* `/done off` 후원자 연동 기능 비활성화
* `/done reconnect all` 전체 재접속
* `/done reconnect <닉네임>` 해당 닉네임 재접속, 컨피그에서 치지직/아프리카 바로 아래 단계의 닉네임을 입력
* `/done reload` 설정 파일 리로드 및 재접속
