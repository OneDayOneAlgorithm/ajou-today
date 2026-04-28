# 🌟 Ajou Today - 당신의 하루를 채우는 작은 즐거움

![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![AWS EC2](https://img.shields.io/badge/AWS_EC2-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white)
![Nginx](https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)

Ajou Today는 매일 새로운 영감을 주는 **'오늘의 명언'** 서비스와 간단하게 즐길 수 있는 **'티모 버섯 잡기 미니게임'**을 제공하는 종합 웹 포털 프로젝트입니다. 
단순한 웹 개발을 넘어 **AWS 인프라 구축, Nginx 리버스 프록시 설정, 그리고 GitHub Actions를 활용한 무중단 CI/CD 파이프라인 구축**까지 백엔드 시스템의 전체 사이클을 직접 구현한 풀스택/인프라 토이 프로젝트입니다.

🌐 **실제 서비스 주소:** [https://ajou.today](https://ajou.today)

---

## ✨ 주요 기능 (Key Features)

### 1. 오늘의 명언 (Quote Service)
- **명언 조회 API:** Spring Boot 백엔드와 연동하여 MySQL 데이터베이스에서 무작위 명언을 추출해 제공 (`GET /api/quote`)
- **명언 등록:** 데이터베이스 연동을 통해 새로운 명언 및 롤(LoL) 세계관 어록 동적 관리
- **Glassmorphism UI:** CSS3를 활용한 세련된 반투명 유리 질감의 프론트엔드 디자인

### 2. 미니게임: 티모 버섯 잡기 (Mini Game)
- **순발력 테스트:** JavaScript 기반의 동적 위치 렌더링을 통한 인터랙티브 웹 게임
- **실시간 점수 및 타이머:** 브라우저 환경에서 동작하는 실시간 상태(State) 관리 기능 적용 (`/game.html`)

---

## 🛠 기술 스택 (Tech Stack)

### Backend
- **Java 17** / **Spring Boot 3.x**
- **Spring Web** / **Spring Data JPA**
- **MySQL 8.0** (Database)

### Frontend
- HTML5, CSS3, Vanilla JavaScript

### Infrastructure & DevOps
- **AWS EC2 (Ubuntu 22.04):** 클라우드 서버 호스팅
- **Nginx:** 웹 서버 및 리버스 프록시 (포트 80 ➔ 8080 포워딩)
- **GitHub Actions:** CI/CD를 통한 자동 빌드 및 배포 파이프라인 구축
- **가비아 / 호스팅케이알:** 커스텀 도메인 연동

---

## ⚙️ 시스템 아키텍처 (System Architecture)

1. **사용자 요청:** 클라이언트가 `https://ajou.today` 로 접속합니다.
2. **Nginx 프록시:** AWS EC2 내의 Nginx가 80(HTTP) / 443(HTTPS) 포트 요청을 받아 내부의 Spring Boot(8080) 서버로 안전하게 넘겨줍니다. (리버스 프록시)
3. **Spring Boot (WAS):** REST API 요청을 처리하고, 필요시 MySQL DB에서 데이터를 조회/수정합니다.
4. **CI/CD 파이프라인:** - 개발자가 코드를 GitHub `main` 브랜치에 Push합니다.
   - GitHub Actions가 자동으로 프로젝트를 빌드(`gradlew build`)합니다.
   - 빌드된 `.jar` 파일을 EC2 서버로 전송하고 서버를 재시작하여 자동 배포를 완료합니다.

---

## 🚀 트러블 슈팅 및 학습 경험 (Trouble Shooting)

- **보안 강화:** AWS 보안 그룹(Security Group)을 설정하여 데이터베이스(3306) 포트를 외부에서 직접 접근할 수 없도록 차단하고, DBeaver 접속 시 **SSH Tunneling** 기법을 적용하여 보안과 편의성을 동시에 확보했습니다.
- **포트 충돌 및 리버스 프록시:** 사용자가 `:8080` 포트를 직접 치고 들어오지 않게 하기 위해 Nginx를 도입하여 깔끔한 도메인 서비스(`ajou.today`)를 제공했습니다.
- **자동화의 편리함:** 이전에는 수동으로 `.jar`를 빌드하고 FTP로 옮기던 과정을 GitHub Actions의 `deploy.yml` 워크플로우를 작성하여 100% 자동화했습니다.

---

## 👨‍💻 연락처 및 개발자
- **GitHub:** https://github.com/OneDayOneAlgorithm
- **Email:** gudwls9966@gmail.com
