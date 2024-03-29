### TDD로 Point 구현

### 요구 사항

- PATCH  `/point/{id}/charge` : 포인트를 충전한다.
- PATCH `/point/{id}/use` : 포인트를 사용한다.
- GET `/point/{id}` : 포인트를 조회한다.
- GET `/point/{id}/histories` : 포인트 내역을 조회한다.
- 잔고가 부족할 경우, 포인트 사용은 실패하여야 합니다.
- 동시에 여러 건의 포인트 충전, 이용 요청이 들어올 경우 순차적으로 처리되어야 합니다.

### 진행 사항

- [유저의 포인트를 조회하는 api 개발](https://github.com/rlatmd0829/TDD-point-management/commit/6bf6327a35673a5bd11baf9ca2430833c33d0185)
- [유저의 포인트를 충전하는 api 개발](https://github.com/rlatmd0829/TDD-point-management/commit/0ec38b76cfa6769a81dfd665234f0342e469a8c9)
- [유저의 포인트 내역을 조회하는 api 개발](https://github.com/rlatmd0829/TDD-point-management/commit/7dc659112189aa84be531670602cbab4696fd163)
- [유저의 포인트 사용하는 api 개발](https://github.com/rlatmd0829/TDD-point-management/commit/a72aca5135d543ebb677092a4ab34e53e9072604)
- [동시에 여러 건의 포인트 충전, 이용 요청이 들어올 경우 테스트 작성](https://github.com/rlatmd0829/TDD-point-management/commit/d3e6a19515ae1a5c9fa755d4661fb78b1f483028)
- [통합테스트 작성](https://github.com/rlatmd0829/TDD-point-management/commit/4b31cad5443389cb45798df5f32eb3579368eef4)
