# Minecraft Chzzk / Soop 후원 연동 플러그인

## **지원 버전**
Java 버전: 21 이상, 17

※ Java 17 버전은 아래 다운로드에서 17버전을 다운받고 파일명에서 -jdk17을 제거 후 사용해주세요. 마인크래프트 1.20.4까지만 공식 지원 됩니다.

마인크래프트 서버 버전: Paper 1.18 ~ 1.21.4

## **빌드 방법**

1. 터미널에서 `gradlew jar` 실행

## **다운로드 방법**

1. Github(https://github.com/taromati/done-connector) 의 Release 에서 다운로드
    - [자바21버전](https://github.com/taromati/done-connector/releases/download/1.8.1/done-connector-1.8.1.jar)
    - [자바17버전](https://github.com/taromati/done-connector/releases/download/1.8.1/done-connector-1.8.1-jdk17.jar)
## **실행 방법**

1. plugins 폴더에 done-connector-1.8.1.jar 파일을 넣고 마인크래프트 서버를 1회 실행 후 종료
2. plugins 폴더에서 done-connector/config.yml 파일 수정
3. 마인크래프트 서버 실행


## **사용 방법**

* 플러그인 적용 후 서버 실행시 자동으로 기능 활성화
* `/done [on|off|reconnect|reload|add]` 명령어로 기능 제어
* `/done on` 후원자 연동 기능 활성화
* `/done off` 후원자 연동 기능 비활성화
* `/done reconnect all` 전체 재접속
* `/done reconnect <닉네임>` 해당 닉네임 재접속, 컨피그에서 치지직/숲 바로 아래 단계의 닉네임 혹은 마크닉네임 입력, 자동완성은 마크닉네임만 지원
* `/done reload` 설정 파일 리로드 및 재접속
* `/done add <플랫폼> <방송닉> <방송ID> <마크닉>` 도네연결 임시 추가, reload가 어려운 상황에서 임시로 연결 추가. 서버 재기동시에 없어짐

## 1.8.1 추가사항
* config.yml에 `숲풍선갯수로출력: false`을 추가해주세요, true로 할 경우 금액이 아닌 별풍선 갯수로 전달됩니다.