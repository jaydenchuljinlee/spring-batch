# Spring Batch 학습 프로젝트

이 프로젝트는 Spring Batch의 다양한 기능을 학습하고 구현한 예제 프로젝트입니다.

## 주요 기능

### 1. JobStep 구성 (JobStepConfiguration)
- 부모 Job과 자식 Job의 계층 구조 구현
- JobStep을 통한 Job 간 데이터 전달
- StepExecutionListener를 활용한 실행 컨텍스트 관리

### 2. 병렬 처리 (ParallelJobConfiguration)
- Flow를 활용한 병렬 처리 구현
- SimpleAsyncTaskExecutor를 사용한 비동기 실행
- 스레드별 실행 컨텍스트 관리

### 3. 재시도 및 건너뛰기 (RetrySkipBatchJobConfiguration)
- FaultTolerant 기능을 통한 오류 처리
- retry와 skip 정책 설정
- 실패 시 대체 작업 수행 (failedStep)

### 4. 재시도 정책 (RetryPolicyJobConfiguration)
- 커스텀 RetryPolicy 구현
- SkipListener를 통한 처리 건너뛰기 로깅
- 다양한 예외 처리 전략

### 5. 파티셔닝 (PartitionUserJob)
- 데이터를 여러 파티션으로 분할 처리
- TaskExecutorPartitionHandler를 통한 병렬 처리
- 스레드 풀 설정 및 관리

## 기술 스택
- Spring Boot 3.1.9
- Spring Batch
- MySQL
- Kotlin

## 실행 방법
1. MySQL 데이터베이스 설정
2. application.yml에서 데이터베이스 접속 정보 설정
3. `spring.batch.job.enabled=true`로 설정하여 배치 잡 활성화
4. 애플리케이션 실행

## 주요 학습 포인트
- Spring Batch의 기본 구조와 컴포넌트
- Job과 Step의 계층 구조
- 병렬 처리와 파티셔닝
- 오류 처리와 재시도 정책
- 실행 컨텍스트 관리
