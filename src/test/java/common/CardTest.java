package common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Card 클래스 검증 테스트
 * - 컴파일 타임에 Card 타입에 결합하지 않고, 리플렉션으로 정확한 한글 메시지와 함께 실패합니다.
 */
class CardTest {

    private Class<?> loadCardClass() {
        try {
            return Class.forName("common.Card");
        } catch (ClassNotFoundException e) {
            fail("Card 클래스를 찾을 수 없습니다. src/common/Card.java에 package common; 선언과 함께 구현하세요.");
            return null; // unreached
        }
    }

    private Constructor<?> requireCtor(Class<?> cardClass) {
        try {
            return cardClass.getConstructor(Suit.class, Rank.class);
        } catch (NoSuchMethodException e) {
            fail("Card 클래스에 Card(Suit, Rank) 생성자가 없습니다. public 생성자를 구현하세요.");
            return null;
        }
    }

    private Method requireMethod(Class<?> cardClass, String name, Class<?>... parameterTypes) {
        try {
            return cardClass.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            String sig = name + "(" +
                    (parameterTypes.length == 0 ? "" : parameterTypes[0].getSimpleName() + (parameterTypes.length == 2 ? ", " + parameterTypes[1].getSimpleName() : "")) +
                    ")";
            fail("Card 클래스에 " + sig + " 메서드가 없습니다. 명세를 확인하여 구현하세요.");
            return null;
        }
    }

    private Object newCard(Class<?> cardClass, Constructor<?> ctor, Suit s, Rank r) {
        try {
            return ctor.newInstance(s, r);
        } catch (ReflectiveOperationException e) {
            fail("Card 생성자 호출에 실패했습니다. 생성자가 public인지 확인하세요.");
            return null;
        }
    }

    @Nested
    @DisplayName("구현 및 계약")
    class Contract {
        @Test
        @DisplayName("클래스와 생성자 및 접근자 존재")
        void shouldExistWithCorrectApi() {
            Class<?> cardClass = loadCardClass();
            Constructor<?> ctor = requireCtor(cardClass);
            requireMethod(cardClass, "getSuit");
            requireMethod(cardClass, "getRank");
            requireMethod(cardClass, "compareTo", cardClass);
            requireMethod(cardClass, "toString");
            Object card = newCard(cardClass, ctor, Suit.SPADES, Rank.ACE);
            assertNotNull(card, "Card 생성이 null을 반환하면 안 됩니다.");
        }
    }

    @Nested
    @DisplayName("동등성")
    class Equality {
        @Test
        @DisplayName("같은 무니와 랭크는 동등")
        void shouldImplementEqualsAndHashCodeCorrectly() {
            Class<?> cardClass = loadCardClass();
            Constructor<?> ctor = requireCtor(cardClass);
            Object a = newCard(cardClass, ctor, Suit.HEARTS, Rank.KING);
            Object b = newCard(cardClass, ctor, Suit.HEARTS, Rank.KING);
            Object c = newCard(cardClass, ctor, Suit.DIAMONDS, Rank.KING);
            assertEquals(a, b, "같은 무늬/랭크인데 equals가 true가 아닙니다. suit, rank 기반으로 equals를 구현하세요.");
            assertEquals(a.hashCode(), b.hashCode(), "같은 값의 hashCode가 달라서는 안 됩니다.");
            assertNotEquals(a, c, "무늬가 다른 경우 equals는 false여야 합니다.");
        }
    }

    @Nested
    @DisplayName("정렬")
    class Ordering {
        @Test
        @DisplayName("랭크 우선, 동랭크는 무늬")
        void shouldOrderByRankThenSuit() throws Exception {
            Class<?> cardClass = loadCardClass();
            Constructor<?> ctor = requireCtor(cardClass);
            Method cmp = requireMethod(cardClass, "compareTo", cardClass);
            Object two = newCard(cardClass, ctor, Suit.CLUBS, Rank.TWO);
            Object three = newCard(cardClass, ctor, Suit.CLUBS, Rank.THREE);
            int c1 = (Integer) cmp.invoke(three, two);
            assertTrue(c1 > 0, "THREE가 TWO보다 작거나 같게 비교됩니다. 랭크가 먼저 비교되도록 하세요.");
            Object s10 = newCard(cardClass, ctor, Suit.SPADES, Rank.TEN);
            Object h10 = newCard(cardClass, ctor, Suit.HEARTS, Rank.TEN);
            int c2 = (Integer) cmp.invoke(s10, h10);
            assertNotEquals(0, c2, "동일 랭크에서 무늬 비교가 이루어지지 않았습니다. 무늬로 tie-break 하세요.");
        }
    }

    @Nested
    @DisplayName("접근자")
    class Accessors {
        @Test
        @DisplayName("getSuit과 getRank 반환값")
        void shouldReturnCorrectSuitAndRank() throws Exception {
            Class<?> cardClass = loadCardClass();
            Constructor<?> ctor = requireCtor(cardClass);
            Method getSuit = requireMethod(cardClass, "getSuit");
            Method getRank = requireMethod(cardClass, "getRank");
            Object card = newCard(cardClass, ctor, Suit.SPADES, Rank.ACE);
            Object suit = getSuit.invoke(card);
            Object rank = getRank.invoke(card);
            assertAll(
                    () -> assertTrue(suit instanceof Enum && ((Enum<?>) suit).name().equals("SPADES"), "getSuit()가 SPADES를 반환하지 않습니다."),
                    () -> assertTrue(rank instanceof Enum && ((Enum<?>) rank).name().equals("ACE"), "getRank()가 ACE를 반환하지 않습니다.")
            );
        }
    }

    @Nested
    @DisplayName("문자열 표현")
    class StringFormat {
        @Test
        @DisplayName("\"♣️ A\", \"♠️10\" 형식")
        void shouldFormatAsUnicodeEmoji() {
            Class<?> cardClass = loadCardClass();
            Constructor<?> ctor = requireCtor(cardClass);
            Object clubA = newCard(cardClass, ctor, Suit.CLUBS, Rank.ACE);
            Object spade10 = newCard(cardClass, ctor, Suit.SPADES, Rank.TEN);
            assertAll(
                    () -> assertEquals("\u2663\uFE0F A", clubA.toString(), "toString 포맷이 요구사항(무늬 + 공백 정렬된 랭크)과 다릅니다."),
                    () -> assertEquals("\u2660\uFE0F10", spade10.toString(), "toString 포맷이 요구사항과 다릅니다.")
            );
        }
    }

    @Nested
    @DisplayName("예외 처리")
    class ExceptionHandling {
        @Test
        @DisplayName("null 파라미터 처리")
        void shouldThrowExceptionForNullParameters() {
            Class<?> cardClass = loadCardClass();
            Constructor<?> ctor = requireCtor(cardClass);
            
            assertThrows(Exception.class, () -> ctor.newInstance(null, Rank.ACE), 
                    "Suit이 null일 때 예외가 발생해야 합니다.");
            assertThrows(Exception.class, () -> ctor.newInstance(Suit.SPADES, null), 
                    "Rank가 null일 때 예외가 발생해야 합니다.");
        }
    }
}
