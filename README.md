# devly 서버 구조도
![real](https://github.com/user-attachments/assets/491bedd1-b7d1-43bd-b00e-3123552843bd)


## 프로젝트 목표
* 생성형 AI 프롬프트와 배치 잡을 활용해 양질 컨텐츠 생성을 자동화하고, 서비스를 제공하는 것이 목표입니다.
* 단순한 기능 구현뿐 아니라 대용량 트래픽 처리까지 고려한 기능을 구현하는 것이 목표입니다.
* 객체지향 원리와 여러 이론적 토대위에서 올바른 코드를 작성하는 것이 목표입니다.
* 문서화, 단위테스트는 높은 우선순위를 두어 작성했고 CI/CD를 통한 자동화 또한 구현하여 쉽게 협업이 가능한 프로젝트로 만들었습니다.

  
## 기술적 issue 해결 과정
* 블로그 작성 중 ...


## 프로젝트 중점 사항
* 문서화
* Service Layer를 고립시켜 의존적이지 않은 단위테스트 작성
* 서버의 확장성
* Mysql에서 인덱스 설정과 실행계획 분석 후 쿼리 튜닝
* 스프링의 @Transactional 을 깊게 이해하고 활용해 견고한 로직 구현
* Redis Pipeline을 이용하여 한번에 많은 데이터 추가 시 네트워크 병목 개선
* Jenkins를 사용하여 CI/CD 환경 구축
* Mysql Replication – AOP를 이용하여 Master/Slave로 데이터베이스 이중화
* Nginx의 Reversed-Proxy를 이용하여 로드밸런싱
* Nginx의 Micro caching을 이용해 요청 처리
* Ngrinder를 이용하여 성능 테스트
