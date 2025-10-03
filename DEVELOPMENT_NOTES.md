# EconomyShop 개발 노트

> 플러그인 개발 과정에서의 주요 설계 결정 및 해결한 문제들

## 📐 설계 원칙

### PLUGIN_DEVELOPMENT_STANDARD.md 준수

이 플러그인은 **PLUGIN_DEVELOPMENT_STANDARD.md**를 100% 준수합니다:

1. **독립성**: `depend: []` - 다른 플러그인 없이 작동
2. **호환성**: `softdepend: [ToolEnhancer]` - 선택적 연동
3. **일관성**: ToolEnhancer와 동일한 프로젝트 구조

### 프로젝트 구조

```
src/main/java/com/krangpq/economyshop/
├── EconomyShop.java              # 메인 클래스 (연동 API import 금지!)
├── api/
│   └── EconomyShopAPI.java       # 외부 플러그인용 API
├── commands/                      # 명령어 처리
├── managers/                      # 핵심 로직
├── integration/                   # 연동 전용 (API import 허용)
│   ├── IntegrationManager.java
│   └── ToolEnhancerIntegration.java
├── gui/                          # GUI 시스템
├── data/                         # 데이터 모델
└── utils/                        # 유틸리티
```

**중요**: `ToolEnhancerAPI`는 **오직** `integration/` 패키지에서만 import!

## 🔧 핵심 기능 구현

### 1. 경제 시스템 (EconomyManager)

#### 파일 기반 저장소
```
data/accounts/
├── <UUID>.yml      # 플레이어별 계정 파일
├── <UUID>.yml
└── ...
```

**장점**:
- 다른 서버로 쉽게 이전 가능
- 각 계정이 독립적인 파일
- 손쉬운 백업/복구

**구현 세부사항**:
```java
// 계정 파일 구조
balance: 10000.0
last-login: 1234567890000
```

#### 잔고 제한
- `max-balance`: 최대 잔고 제한 (오버플로우 방지)
- `allow-negative`: 마이너스 잔고 허용 여부
- 입출금 시 자동 검증

### 2. 상점 시스템

#### GUI 세션 관리

**문제**: 플레이어가 여러 GUI를 동시에 열면 충돌 발생

**해결**: ToolEnhancer의 `GuiSession` 패턴 차용
```java
// 세션 시작
guiSession.startSession(player, "main_shop");

// 세션 확인
if (guiSession.isSessionType(player, "category_shop")) {
    // GUI 이벤트 처리
}

// 세션 종료 (GUI 닫을 때)
guiSession.endSession(player);
```

**효과**: 중복 GUI 열림 방지, 이벤트 충돌 방지

#### 구매/판매 시스템

**좌클릭/우클릭 구분**:
```java
if (click.isLeftClick() && !click.isShiftClick()) {
    // 1개 구매
} else if (click.isRightClick() && !click.isShiftClick()) {
    // 64개 구매
} else if (click.isLeftClick() && click.isShiftClick()) {
    // 해당 슬롯 판매
} else if (click.isRightClick() && click.isShiftClick()) {
    // 전체 판매
}
```

**판매 가능 여부 검증**:
- 상점 아이템에 `sell-price` 존재 여부 확인
- 특별 아이템은 대부분 구매만 가능 (sell-price 없음)

### 3. 커스텀 아이템 시스템

#### 개별 파일 저장
```
custom-items/
├── abc12345.yml    # 각 아이템마다 고유 ID
├── def67890.yml
└── ...
```

**장점**:
- 아이템별로 독립적 관리
- 삭제 시 파일만 제거하면 됨
- 수동 편집 가능

**파일 구조**:
```yaml
item:                    # ItemStack 직렬화
  ==: org.bukkit.inventory.ItemStack
  type: DIAMOND_SWORD
  meta:
    display-name: "§c전설의 검"
price: 100000.0
registered-by: "Admin"
registered-at: 1234567890000
```

### 4. ToolEnhancer 연동

#### Integration 패턴

**설계 목표**:
- ToolEnhancer 없어도 정상 작동
- 있으면 자동으로 강화석 상점에 추가
- 메인 클래스에서 ToolEnhancer API 직접 import 금지

**구현**:
```java
// IntegrationManager.java (메인 클래스에서 사용)
public class IntegrationManager {
    private ToolEnhancerIntegration toolEnhancerIntegration;
    
    public void checkIntegrations() {
        if (Bukkit.getPluginManager().getPlugin("ToolEnhancer") != null) {
            toolEnhancerIntegration = new ToolEnhancerIntegration();
        }
    }
    
    public ToolEnhancerIntegration getToolEnhancer() {
        if (toolEnhancerIntegration == null) {
            return new ToolEnhancerIntegration(); // 더미 객체
        }
        return toolEnhancerIntegration;
    }
}

// ToolEnhancerIntegration.java (여기서만 API import)
import com.krangpq.toolenhancer.api.ToolEnhancerAPI;

public class ToolEnhancerIntegration {
    private final ToolEnhancerAPI api;
    
    public ItemStack getEnhancementStone() {
        if (!enabled) return null;
        return api.createEnhancementStone();
    }
}
```

**GUI에서 사용**:
```java
// CategoryShopGui.java
if (category.equals("special")) {
    // 기본 아이템 로드
    // 커스텀 아이템 로드
    
    // ToolEnhancer 연동 아이템 (있을 경우만)
    if (plugin.getIntegrationManager().hasToolEnhancer()) {
        ToolEnhancerIntegration te = plugin.getIntegrationManager().getToolEnhancer();
        ItemStack stone = te.getEnhancementStone();
        if (stone != null) {
            double price = config.getDouble("integrations.toolenhancer.items.enhancement-stone.price");
            inventory.setItem(slot++, createShopItem(stone, price, true));
        }
    }
}
```

## 🐛 해결한 주요 문제들

### 문제 1: ItemStack 비교

**문제**: 판매 시 플레이어 인벤토리에 있는 아이템과 상점 아이템 매칭

**시도한 방법**:
- `item.equals()`: 메타데이터까지 완전히 같아야 함 (❌)
- `item.isSimilar()`: Lore까지 비교 (❌)

**해결**:
```java
// Material만으로 비교 (가장 단순하고 확실)
private ShopItem findShopItemByMaterial(Material material, String category) {
    for (ShopItem item : plugin.getShopManager().getItems(category)) {
        if (item.getMaterial() == material) {
            return item;
        }
    }
    return null;
}
```

### 문제 2: 인벤토리에서 아이템 제거

**문제**: 여러 슬롯에 분산된 동일 아이템 전체 판매

**해결**:
```java
private int removeItems(Player player, Material material, int amount) {
    int remaining = amount;
    
    for (ItemStack item : player.getInventory().getContents()) {
        if (item != null && item.getType() == material) {
            int itemAmount = item.getAmount();
            
            if (itemAmount <= remaining) {
                remaining -= itemAmount;
                player.getInventory().remove(item);
            } else {
                item.setAmount(itemAmount - remaining);
                remaining = 0;
                break;
            }
            
            if (remaining == 0) break;
        }
    }
    
    return amount - remaining; // 실제 제거된 개수
}
```

### 문제 3: GUI 이벤트 충돌

**문제**: 여러 GUI 클래스가 같은 이벤트 리스닝

**해결**: GuiSession으로 세션 타입 구분
```java
@EventHandler
public void onInventoryClick(InventoryClickEvent e) {
    Player player = (Player) e.getWhoClicked();
    
    // 이 GUI의 이벤트인지 확인
    if (!plugin.getGuiSession().isSessionType(player, "category_shop")) {
        return; // 다른 GUI의 이벤트
    }
    
    e.setCancelled(true);
    // 이벤트 처리...
}
```

### 문제 4: 설정 파일 리로드

**문제**: 상점 아이템 변경 후 서버 재시작 필요

**해결**: 리로드 기능 추가
```java
// ShopManager.java
public void reload() {
    shopItems.clear();
    customItems.clear();
    loadShopItems();
    loadCustomItems();
}

// AdminEcoCommand.java
case "리로드":
    plugin.reloadConfig();
    plugin.getShopManager().reload();
    sender.sendMessage("플러그인이 리로드되었습니다!");
```

## 💡 설계 결정 사항

### 왜 파일 기반 저장소?

**고려한 옵션**:
1. **파일 (YAML)**: ✅ 선택
2. **SQLite**: 너무 무거움
3. **MySQL**: 별도 설정 필요

**선택 이유**:
- 다른 서버로 이전 용이
- 별도 설정 불필요
- 소규모 서버에 적합
- 직접 수동 편집 가능

### 왜 대형 창고 GUI?

**고려한 옵션**:
1. **일반 창고 (27칸)**: 너무 작음
2. **대형 창고 (54칸)**: ✅ 선택
3. **페이징 시스템**: 너무 복잡

**선택 이유**:
- 45칸(상품) + 9칸(네비게이션) = 54칸
- 대부분의 상점 아이템 한 화면에 표시
- 페이징 없이 직관적

### 왜 ItemStack 직렬화?

**문제**: 커스텀 아이템을 파일에 저장하는 방법

**해결**: Bukkit의 내장 직렬화 사용
```java
// 저장
config.set("item", itemStack);

// 로드
ItemStack item = config.getItemStack("item");
```

**장점**:
- 메타데이터(이름, Lore, 인챈트 등) 모두 보존
- 다른 플러그인 아이템도 저장 가능
- ToolEnhancer 강화된 아이템도 등록 가능

## 📊 성능 고려사항

### 메모리 최적화

**계정 캐싱**:
```java
private final Map<UUID, PlayerAccount> accounts = new HashMap<>();

// 첫 접근 시에만 로드
public PlayerAccount getAccount(UUID player) {
    if (!accounts.containsKey(player)) {
        accounts.put(player, loadAccount(player));
    }
    return accounts.get(player);
}
```

**상점 아이템 캐싱**:
- 서버 시작 시 한 번만 로드
- 리로드 명령어 사용 시에만 다시 로드

### 파일 I/O 최적화

**지연 저장 (Lazy Save)**:
```java
// 즉시 저장하지 않고 메모리에만 반영
account.setBalance(newBalance);

// 주기적으로 일괄 저장
plugin.getEconomyManager().saveAllAccounts();

// 또는 플러그인 종료 시 자동 저장
@Override
public void onDisable() {
    economyManager.saveAllAccounts();
}
```

## 🔮 향후 개발 계획

### 추가 예정 기능

1. **거래 로그**
    - 모든 경제 활동 기록
    - CSV 형식으로 내보내기

2. **상점 카테고리 확장**
    - config.yml에서 카테고리 추가 가능
    - 동적 GUI 생성

3. **Vault 연동**
    - Vault Economy API 구현
    - 다른 플러그인과 호환성 향상

4. **페이징 시스템**
    - 아이템이 많을 경우 페이지 이동
    - 검색 기능

5. **주식 시스템**
    - 아이템 가격 변동
    - 시장 경제 시뮬레이션

## 🧪 테스트 체크리스트

### 기본 기능
- [ ] 신규 플레이어 계정 자동 생성
- [ ] 잔고 저장/로드
- [ ] 송금 기능
- [ ] 상점 GUI 열기

### 상점 기능
- [ ] 아이템 구매 (1개/64개)
- [ ] 아이템 판매 (슬롯/전체)
- [ ] 잔고 부족 시 오류 메시지
- [ ] 인벤토리 가득 참 시 오류 메시지

### 관리자 기능
- [ ] 돈 지급/차감/설정
- [ ] 커스텀 아이템 등록/삭제
- [ ] 플러그인 리로드

### ToolEnhancer 연동
- [ ] ToolEnhancer 있을 때: 강화석 표시
- [ ] ToolEnhancer 없을 때: 에러 없이 작동
- [ ] 강화석 구매 가능

### 데이터 무결성
- [ ] 서버 재시작 후 잔고 유지
- [ ] 커스텀 아이템 파일 삭제 시 상점에서 제거
- [ ] 최대 잔고 초과 방지

## 📚 참고 자료

- [Spigot API Documentation](https://hub.spigotmc.org/javadocs/spigot/)
- [Bukkit Configuration API](https://www.spigotmc.org/wiki/configuration-api/)
- PLUGIN_DEVELOPMENT_STANDARD.md (프로젝트 표준)
- ToolEnhancer 소스 코드 (GuiSession 패턴)

---

**마지막 업데이트**: 2025-10-03  
**작성자**: KrangPQ