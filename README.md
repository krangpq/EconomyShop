# EconomyShop

> 독립적인 경제 및 상점 시스템

## 📋 개요

EconomyShop은 마인크래프트 서버를 위한 완전한 경제 및 상점 시스템입니다. 다른 플러그인 없이도 독립적으로 작동하며, 선택적으로 ToolEnhancer 같은 플러그인과 연동할 수 있습니다.

### 주요 기능

- 💰 **경제 시스템**: 플레이어별 잔고 관리, 송금 기능
- 🛒 **상점 시스템**: 작물, 광물, 특별 아이템 판매/구매
- 📦 **커스텀 아이템**: 관리자가 직접 아이템 등록/삭제
- 🔌 **플러그인 연동**: ToolEnhancer 강화석 판매 (선택사항)
- 📁 **쉬운 데이터 관리**: 파일 기반으로 다른 서버로 이전 가능

## 🚀 설치 방법

1. **EconomyShop.jar** 파일을 `plugins/` 폴더에 넣기
2. 서버 재시작
3. `plugins/EconomyShop/config.yml` 설정 (선택사항)

## 📖 명령어

### 플레이어 명령어

| 명령어 | 별칭 | 설명 |
|--------|------|------|
| `/상점` | `/shop` | 상점 메뉴 열기 |
| `/돈` | `/money`, `/bal`, `/잔고` | 잔고 확인 |
| `/송금 <플레이어> <금액>` | `/pay` | 다른 플레이어에게 송금 |

### 관리자 명령어

| 명령어 | 설명 |
|--------|------|
| `/경제관리 지급 <플레이어> <금액>` | 돈 지급 |
| `/경제관리 차감 <플레이어> <금액>` | 돈 차감 |
| `/경제관리 설정 <플레이어> <금액>` | 잔고 설정 |
| `/경제관리 확인 <플레이어>` | 잔고 확인 |
| `/경제관리 리로드` | 플러그인 리로드 |
| `/상점등록 <가격>` | 손에 든 아이템을 상점에 등록 |
| `/상점삭제 [아이템ID]` | 커스텀 아이템 삭제 (ID 없으면 목록 표시) |

## 🎮 사용 방법

### 상점 이용

1. `/상점` 명령어로 메인 메뉴 열기
2. 카테고리 선택 (작물, 광물, 특별 아이템)
3. 아이템 클릭:
    - **좌클릭**: 1개 구매
    - **우클릭**: 64개 구매
    - **Shift+좌클릭**: 해당 슬롯 아이템 판매
    - **Shift+우클릭**: 인벤토리 내 동일 아이템 전체 판매

### 커스텀 아이템 등록

1. 등록할 아이템을 손에 들기
2. `/상점등록 <가격>` 명령어 입력
3. 아이템이 "특별 아이템" 카테고리에 추가됨
4. 파일은 `plugins/EconomyShop/custom-items/` 폴더에 저장

### 데이터 백업/이전

```
plugins/EconomyShop/
├── data/accounts/        # 플레이어 잔고 (UUID.yml)
├── custom-items/         # 커스텀 아이템 (아이템ID.yml)
└── config.yml           # 설정 파일
```

이 폴더들을 복사하면 다른 서버로 쉽게 이전 가능합니다!

## ⚙️ 설정

### config.yml 주요 설정

```yaml
economy:
  starting-balance: 10000      # 신규 플레이어 시작 잔고
  max-balance: 999999999       # 최대 잔고 제한
  currency-symbol: "원"        # 화폐 단위

integrations:
  toolenhancer:
    enabled: true               # ToolEnhancer 연동 활성화
    items:
      enhancement-stone:
        price: 5000             # 강화석 가격
```

### 상점 아이템 커스터마이징

`plugins/EconomyShop/shops/` 폴더의 YAML 파일 수정:
- `crops.yml`: 작물 상점
- `minerals.yml`: 광물 상점
- `special.yml`: 특별 아이템 상점

## 🔌 ToolEnhancer 연동

ToolEnhancer 플러그인이 설치되어 있으면:
- 특별 아이템 탭에 **강화석** 자동 추가
- config.yml에서 가격 설정 가능
- ToolEnhancer 없어도 정상 작동 (에러 없음)

## 🛠️ 개발자 API

### 다른 플러그인에서 사용하기

```java
// EconomyShop API 가져오기
EconomyShopAPI api = EconomyShopAPI.getInstance();

// 잔고 확인
double balance = api.getBalance(player);

// 돈 지급
api.deposit(player, 1000);

// 돈 차감
api.withdraw(player, 500);

// 잔고 설정
api.setBalance(player, 10000);

// 금액 포맷
String formatted = api.format(1234.56); // "1,235원"
```

## 📝 권한

| 권한 | 기본값 | 설명 |
|------|--------|------|
| `economyshop.use` | `true` | 상점 사용 권한 |
| `economyshop.admin` | `op` | 관리자 권한 |

## 🐛 문제 해결

### 상점이 열리지 않아요
- 권한 확인: `/lp user <플레이어> permission check economyshop.use`
- 다른 GUI가 열려있는지 확인 (이미 사용중 메시지 표시됨)

### ToolEnhancer 연동이 안 돼요
- ToolEnhancer 플러그인이 설치되어 있는지 확인
- config.yml에서 `integrations.toolenhancer.enabled: true` 확인
- 서버 재시작 또는 `/경제관리 리로드`

### 데이터가 사라졌어요
- `plugins/EconomyShop/data/accounts/` 폴더 확인
- 플레이어 UUID.yml 파일이 있는지 확인
- 백업에서 복원 가능

## 📄 라이선스

이 플러그인은 PLUGIN_DEVELOPMENT_STANDARD.md 표준을 따릅니다.

## 👤 제작자

**KrangPQ**

## 🔗 관련 플러그인

- [ToolEnhancer](https://github.com/yourusername/ToolEnhancer) - 도구 강화 시스템

---

**버전**: 1.0.0  
**Minecraft**: 1.20.x  
**API**: Spigot 1.20.1