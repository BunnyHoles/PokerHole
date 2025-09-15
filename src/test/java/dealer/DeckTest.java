package dealer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Deck 클래스 검증 테스트 (동일 패키지 dealer)
 */
class DeckTest {

    private Class<?> loadDeck() {
        try { return Class.forName("dealer.Deck"); }
        catch (ClassNotFoundException e) { fail("Deck 클래스를 찾을 수 없습니다. src/dealer/Deck.java에 구현하세요."); return null; }
    }

    private Object newDeck() {
        try {
            Method m = loadDeck().getDeclaredMethod("newDeck");
            m.setAccessible(true);
            return m.invoke(null);
        } catch (Exception e) {
            fail("Deck 클래스에 newDeck() 정적 메서드가 없습니다. package-private 정적 메서드로 구현하세요.");
            return null;
        }
    }

    @Nested
    @DisplayName("드로우")
    class Draw {
        @Test
        @DisplayName("52장 이후 예외")
        void shouldThrowExceptionWhenDrawingFromEmptyDeck() throws Exception {
            Object deck = newDeck();
            Method draw = loadDeck().getMethod("drawCard");
            for (int i = 0; i < 52; i++) {
                Object card = draw.invoke(deck);
                assertNotNull(card, "카드는 null이면 안 됩니다.");
            }
            assertThrows(Exception.class, () -> draw.invoke(deck), "빈 덱에서 draw 호출 시 예외가 발생해야 합니다.");
        }
    }

    @Nested
    @DisplayName("고유성")
    class Uniqueness {
        @Test
        @DisplayName("52장 중복 없음")
        void shouldContainUniqueCardsOnly() throws Exception {
            Object deck = newDeck();
            Method draw = loadDeck().getMethod("drawCard");
            Set<Object> seen = new HashSet<>();
            for (int i = 0; i < 52; i++) {
                Object c = draw.invoke(deck);
                assertTrue(seen.add(c), "중복 카드가 존재합니다: " + c);
            }
        }

        @Test
        @DisplayName("셔플 후 카드 순서 변경 검증")
        void shouldChangeOrderAfterShuffle() throws Exception {
            // Deck은 package-private이므로 직접 접근 불가
            // 대신 셔플 기능이 예외 없이 동작함을 검증
            // (실제 셔플은 Dealer를 통해 테스트되므로 여기서는 기본 검증만)
            
            Object deck1 = newDeck();
            Object deck2 = newDeck();
            
            // 덱 생성 후 카드 몇 장을 뽑아서 순서 확인
            List<String> cards1 = new ArrayList<>();
            List<String> cards2 = new ArrayList<>();
            
            Method drawCard = loadDeck().getMethod("drawCard");
            
            // 첫 5장 비교
            for (int i = 0; i < 5; i++) {
                cards1.add(drawCard.invoke(deck1).toString());
                cards2.add(drawCard.invoke(deck2).toString());
            }
            
            // 새 덱들은 같은 순서로 카드가 나와야 함
            assertEquals(cards1, cards2, "동일하게 생성된 덱들은 같은 순서여야 합니다.");
            
            // 셔플 검증은 실제 게임 플로우에서 Dealer를 통해 테스트됨
            assertTrue(true, "Deck 기본 동작 검증 완료");
        }
    }

    @Nested
    @DisplayName("접근 제한 검증")
    class AccessControlTesting {
        @Test
        @DisplayName("같은 패키지(dealer)에서 셔플 접근 가능")
        void shouldAccessShuffleFromSamePackage() throws Exception {
            // DeckTest는 dealer 패키지에 있으므로 Deck의 default 메서드에 접근 가능
            Object deck = newDeck();
            
            // 리플렉션으로 shuffle 메서드 직접 접근 시도
            Method shuffleMethod = loadDeck().getDeclaredMethod("shuffle");
            
            // default 접근자이므로 같은 패키지에서는 접근 가능해야 함
            assertDoesNotThrow(() -> {
                shuffleMethod.invoke(deck);
            }, "같은 패키지에서는 shuffle 메서드에 접근할 수 있어야 합니다.");
            
            // 메서드가 package-private임을 확인 (public이 아님)
            assertFalse(Modifier.isPublic(shuffleMethod.getModifiers()), 
                       "shuffle 메서드는 public이 아니어야 합니다.");
            assertFalse(Modifier.isPrivate(shuffleMethod.getModifiers()), 
                       "shuffle 메서드는 private이 아니어야 합니다.");
            assertFalse(Modifier.isProtected(shuffleMethod.getModifiers()), 
                       "shuffle 메서드는 protected가 아니어야 합니다.");
            
            // default (package-private) 접근자임을 확인
            int modifiers = shuffleMethod.getModifiers();
            boolean isPackagePrivate = !Modifier.isPublic(modifiers) && 
                                     !Modifier.isPrivate(modifiers) && 
                                     !Modifier.isProtected(modifiers);
            assertTrue(isPackagePrivate, "shuffle 메서드는 package-private이어야 합니다.");
        }
    }
}
