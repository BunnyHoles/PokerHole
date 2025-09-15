package common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Hand 클래스 검증 테스트
 * - 컴파일 타임 결합 없이 리플렉션으로 직접 호출하고, 한국어 메시지로 실패를 안내합니다.
 */
class HandTest {

    private Class<?> loadHand() {
        try { return Class.forName("common.Hand"); }
        catch (ClassNotFoundException e) { fail("Hand 클래스를 찾을 수 없습니다. src/common/Hand.java에 package common; 선언과 함께 구현하세요."); return null; }
    }

    private Object newHand() throws Exception {
        return loadHand().getConstructor().newInstance();
    }

    private Method addSR(Object hand) {
        try { return hand.getClass().getMethod("add", Suit.class, Rank.class); }
        catch (NoSuchMethodException e) { fail("Hand 클래스에 add(Suit, Rank) 메서드가 없습니다. public boolean add 메서드를 구현하세요."); return null; }
    }

    private void add(Object hand, Suit s, Rank r) throws Exception {
        addSR(hand).invoke(hand, s, r);
    }

    private void open(Object hand) throws Exception {
        try { hand.getClass().getMethod("open").invoke(hand); }
        catch (NoSuchMethodException e) { fail("Hand 클래스에 open() 메서드가 없습니다. public Hand open 메서드를 구현하세요."); }
    }

    private String tierName(Object hand) throws Exception {
        try { Object tier = hand.getClass().getMethod("getTier").invoke(hand); return ((Enum<?>) tier).name(); }
        catch (NoSuchMethodException e) { fail("Hand 클래스에 getTier() 메서드가 없습니다. public Tier getTier 메서드를 구현하세요."); return null; }
    }

    private int compare(Object a, Object b) throws Exception {
        try { Method m = a.getClass().getMethod("compareTo", a.getClass()); return (Integer) m.invoke(a, b); }
        catch (NoSuchMethodException e) { fail("Hand 클래스에 compareTo(Hand) 메서드가 없습니다. Comparable<Hand> 인터페이스를 구현하세요."); return 0; }
    }

    @Nested
    @DisplayName("API 및 제약사항")
    class ApiAndConstraints {
        @Test
        @DisplayName("메서드 존재 검증 및 5장 제한")
        void shouldEnforceApiConstraintsAndLimit() throws Exception {
            Object h = newHand();
            add(h, Suit.SPADES, Rank.TEN);
            add(h, Suit.HEARTS, Rank.TWO);
            add(h, Suit.DIAMONDS, Rank.NINE);
            add(h, Suit.CLUBS, Rank.FIVE);
            add(h, Suit.SPADES, Rank.KING);
            Exception ex = assertThrows(Exception.class, () -> addSR(h).invoke(h, Suit.CLUBS, Rank.ACE), "6번째 카드 추가 시 IllegalStateException이 발생해야 합니다.");
            Throwable cause = (ex instanceof InvocationTargetException ite) ? ite.getTargetException() : ex;
            assertTrue(cause instanceof IllegalStateException, "5장 제한 위반은 IllegalStateException 이어야 합니다.");
            Object h2 = newHand();
            add(h2, Suit.SPADES, Rank.ACE);
            assertThrows(Exception.class, () -> open(h2), "5장 미만 상태에서 open() 호출 시 예외가 발생해야 합니다.");
        }
    }

    @Nested
    @DisplayName("티어 판정")
    class TierEvaluation {
        @Test
        @DisplayName("로열 플러시")
        void shouldRecognizeRoyalFlush() throws Exception {
            Object h = newHand();
            add(h, Suit.SPADES, Rank.TEN);
            add(h, Suit.SPADES, Rank.JACK);
            add(h, Suit.SPADES, Rank.QUEEN);
            add(h, Suit.SPADES, Rank.KING);
            add(h, Suit.SPADES, Rank.ACE);
            open(h);
            assertEquals("ROYAL_FLUSH", tierName(h), "로열 플러시 판정이 올바르지 않습니다.");
        }

        @Test
        @DisplayName("스트레이트 플러시")
        void shouldRecognizeStraightFlush() throws Exception {
            Object h = newHand();
            add(h, Suit.SPADES, Rank.TWO);
            add(h, Suit.SPADES, Rank.THREE);
            add(h, Suit.SPADES, Rank.FOUR);
            add(h, Suit.SPADES, Rank.FIVE);
            add(h, Suit.SPADES, Rank.SIX);
            open(h);
            assertEquals("STRAIGHT_FLUSH", tierName(h));
        }

        @Test
        @DisplayName("포카드")
        void shouldRecognizeFourOfAKind() throws Exception {
            Object h = newHand();
            add(h, Suit.SPADES, Rank.FIVE);
            add(h, Suit.HEARTS, Rank.FIVE);
            add(h, Suit.DIAMONDS, Rank.FIVE);
            add(h, Suit.CLUBS, Rank.FIVE);
            add(h, Suit.SPADES, Rank.ACE);
            open(h);
            assertEquals("FOUR_OF_A_KIND", tierName(h));
        }

        @Test
        @DisplayName("풀 하우스")
        void shouldRecognizeFullHouse() throws Exception {
            Object h = newHand();
            add(h, Suit.SPADES, Rank.SEVEN);
            add(h, Suit.HEARTS, Rank.SEVEN);
            add(h, Suit.DIAMONDS, Rank.SEVEN);
            add(h, Suit.CLUBS, Rank.TWO);
            add(h, Suit.SPADES, Rank.TWO);
            open(h);
            assertEquals("FULL_HOUSE", tierName(h));
        }

        @Test
        @DisplayName("플러시")
        void shouldRecognizeFlush() throws Exception {
            Object h = newHand();
            add(h, Suit.SPADES, Rank.ACE);
            add(h, Suit.SPADES, Rank.JACK);
            add(h, Suit.SPADES, Rank.NINE);
            add(h, Suit.SPADES, Rank.SEVEN);
            add(h, Suit.SPADES, Rank.FOUR);
            open(h);
            assertEquals("FLUSH", tierName(h));
        }

        @Test
        @DisplayName("스트레이트")
        void shouldRecognizeStraight() throws Exception {
            Object h = newHand();
            add(h, Suit.SPADES, Rank.TWO);
            add(h, Suit.HEARTS, Rank.THREE);
            add(h, Suit.DIAMONDS, Rank.FOUR);
            add(h, Suit.CLUBS, Rank.FIVE);
            add(h, Suit.SPADES, Rank.SIX);
            open(h);
            assertEquals("STRAIGHT", tierName(h));
        }

        @Test
        @DisplayName("쓰리 카드")
        void shouldRecognizeThreeOfAKind() throws Exception {
            Object h = newHand();
            add(h, Suit.SPADES, Rank.TEN);
            add(h, Suit.HEARTS, Rank.TEN);
            add(h, Suit.DIAMONDS, Rank.TEN);
            add(h, Suit.CLUBS, Rank.NINE);
            add(h, Suit.SPADES, Rank.SIX);
            open(h);
            assertEquals("THREE_OF_A_KIND", tierName(h));
        }

        @Test
        @DisplayName("투페어")
        void shouldRecognizeTwoPair() throws Exception {
            Object h = newHand();
            add(h, Suit.SPADES, Rank.ACE);
            add(h, Suit.HEARTS, Rank.ACE);
            add(h, Suit.DIAMONDS, Rank.TWO);
            add(h, Suit.CLUBS, Rank.TWO);
            add(h, Suit.SPADES, Rank.KING);
            open(h);
            assertEquals("TWO_PAIR", tierName(h));
        }

        @Test
        @DisplayName("원페어")
        void shouldRecognizeOnePair() throws Exception {
            Object h = newHand();
            add(h, Suit.SPADES, Rank.TEN);
            add(h, Suit.HEARTS, Rank.TEN);
            add(h, Suit.DIAMONDS, Rank.NINE);
            add(h, Suit.CLUBS, Rank.EIGHT);
            add(h, Suit.SPADES, Rank.SIX);
            open(h);
            assertEquals("ONE_PAIR", tierName(h));
        }

        @Test
        @DisplayName("하이카드")
        void shouldRecognizeHighCard() throws Exception {
            Object h = newHand();
            add(h, Suit.SPADES, Rank.TEN);
            add(h, Suit.HEARTS, Rank.TWO);
            add(h, Suit.DIAMONDS, Rank.NINE);
            add(h, Suit.SPADES, Rank.FIVE);
            add(h, Suit.SPADES, Rank.KING);
            open(h);
            assertEquals("HIGH_CARD", tierName(h));
        }
    }

    @Nested
    @DisplayName("타이브레이크 및 비교")
    class Tiebreakers {
        @Test
        @DisplayName("스트레이트: A-2-3-4-5 < 2-3-4-5-6")
        void shouldCompareStraightWheelVsSixHigh() throws Exception {
            Object a = newHand();
            add(a, Suit.SPADES, Rank.ACE);
            add(a, Suit.HEARTS, Rank.TWO);
            add(a, Suit.DIAMONDS, Rank.THREE);
            add(a, Suit.CLUBS, Rank.FOUR);
            add(a, Suit.SPADES, Rank.FIVE);
            open(a);
            Object b = newHand();
            add(b, Suit.SPADES, Rank.TWO);
            add(b, Suit.HEARTS, Rank.THREE);
            add(b, Suit.DIAMONDS, Rank.FOUR);
            add(b, Suit.CLUBS, Rank.FIVE);
            add(b, Suit.SPADES, Rank.SIX);
            open(b);
            assertEquals("STRAIGHT", tierName(a));
            assertEquals("STRAIGHT", tierName(b));
            assertTrue(compare(b, a) < 0, "6-하이 스트레이트가 휠보다 커야 합니다.");
        }

        @Test
        @DisplayName("무늬 무시: 랭크 동일시 동급")
        void shouldIgnoreSuitsInComparison() throws Exception {
            Object a = newHand();
            add(a, Suit.SPADES, Rank.TEN);
            add(a, Suit.HEARTS, Rank.TEN);
            add(a, Suit.DIAMONDS, Rank.NINE);
            add(a, Suit.CLUBS, Rank.EIGHT);
            add(a, Suit.SPADES, Rank.SIX);
            open(a);
            Object b = newHand();
            add(b, Suit.HEARTS, Rank.TEN);
            add(b, Suit.CLUBS, Rank.TEN);
            add(b, Suit.SPADES, Rank.NINE);
            add(b, Suit.DIAMONDS, Rank.EIGHT);
            add(b, Suit.HEARTS, Rank.SIX);
            open(b);
            assertEquals("ONE_PAIR", tierName(a));
            assertEquals("ONE_PAIR", tierName(b));
            assertEquals(0, compare(a, b), "무늬는 승부에 영향을 주면 안 됩니다.");
        }

        @Test
        @DisplayName("하이카드: 킥커 렉시코 그래픽 비교")
        void shouldCompareHighCardLexicographically() throws Exception {
            Object a = newHand();
            add(a, Suit.SPADES, Rank.ACE);
            add(a, Suit.HEARTS, Rank.TEN);
            add(a, Suit.DIAMONDS, Rank.NINE);
            add(a, Suit.CLUBS, Rank.SEVEN);
            add(a, Suit.SPADES, Rank.FIVE);
            open(a);
            Object b = newHand();
            add(b, Suit.HEARTS, Rank.ACE);
            add(b, Suit.CLUBS, Rank.TEN);
            add(b, Suit.SPADES, Rank.NINE);
            add(b, Suit.DIAMONDS, Rank.SEVEN);
            add(b, Suit.HEARTS, Rank.FOUR);
            open(b);
            assertEquals("HIGH_CARD", tierName(a));
            assertEquals("HIGH_CARD", tierName(b));
            assertTrue(compare(a, b) < 0, "마지막 킥커에서 5가 4보다 커야 합니다.");
        }

        @Test
        @DisplayName("원페어: 킥커 비교")
        void shouldCompareOnePairByKickers() throws Exception {
            Object a = newHand();
            add(a, Suit.SPADES, Rank.TEN);
            add(a, Suit.HEARTS, Rank.TEN);
            add(a, Suit.DIAMONDS, Rank.ACE);
            add(a, Suit.CLUBS, Rank.NINE);
            add(a, Suit.SPADES, Rank.SIX);
            open(a);
            Object b = newHand();
            add(b, Suit.CLUBS, Rank.TEN);
            add(b, Suit.DIAMONDS, Rank.TEN);
            add(b, Suit.SPADES, Rank.ACE);
            add(b, Suit.HEARTS, Rank.EIGHT);
            add(b, Suit.DIAMONDS, Rank.SEVEN);
            open(b);
            assertTrue(compare(a, b) < 0, "9 킥커가 8 킥커보다 커야 합니다.");
        }

        @Test
        @DisplayName("투페어: 높은 페어 → 낮은 페어 → 키커")
        void shouldCompareTwoPairByOrder() throws Exception {
            Object a = newHand();
            add(a, Suit.SPADES, Rank.ACE);
            add(a, Suit.HEARTS, Rank.ACE);
            add(a, Suit.DIAMONDS, Rank.TWO);
            add(a, Suit.CLUBS, Rank.TWO);
            add(a, Suit.SPADES, Rank.KING);
            open(a);
            Object b = newHand();
            add(b, Suit.SPADES, Rank.KING);
            add(b, Suit.HEARTS, Rank.KING);
            add(b, Suit.DIAMONDS, Rank.QUEEN);
            add(b, Suit.CLUBS, Rank.QUEEN);
            add(b, Suit.DIAMONDS, Rank.ACE);
            open(b);
            assertTrue(compare(a, b) < 0, "A,A,2,2 가 K,K,Q,Q 보다 커야 합니다.");
        }

        @Test
        @DisplayName("투페어: 페어 동일시 키커")
        void shouldCompareTwoPairByKickers() throws Exception {
            Object a = newHand();
            add(a, Suit.SPADES, Rank.ACE);
            add(a, Suit.HEARTS, Rank.ACE);
            add(a, Suit.DIAMONDS, Rank.KING);
            add(a, Suit.CLUBS, Rank.KING);
            add(a, Suit.SPADES, Rank.QUEEN);
            open(a);
            Object b = newHand();
            add(b, Suit.HEARTS, Rank.ACE);
            add(b, Suit.DIAMONDS, Rank.ACE);
            add(b, Suit.SPADES, Rank.KING);
            add(b, Suit.CLUBS, Rank.KING);
            add(b, Suit.HEARTS, Rank.JACK);
            open(b);
            assertTrue(compare(a, b) < 0, "동일 페어일 때 Q 킥커가 J보다 커야 합니다.");
        }

        @Test
        @DisplayName("쓰리 카드: 트리플 동일시 키커")
        void shouldCompareThreeOfAKindByKickers() throws Exception {
            Object a = newHand();
            add(a, Suit.SPADES, Rank.TEN);
            add(a, Suit.HEARTS, Rank.TEN);
            add(a, Suit.DIAMONDS, Rank.TEN);
            add(a, Suit.CLUBS, Rank.NINE);
            add(a, Suit.SPADES, Rank.SIX);
            open(a);
            Object b = newHand();
            add(b, Suit.CLUBS, Rank.TEN);
            add(b, Suit.DIAMONDS, Rank.TEN);
            add(b, Suit.HEARTS, Rank.TEN);
            add(b, Suit.SPADES, Rank.NINE);
            add(b, Suit.DIAMONDS, Rank.FIVE);
            open(b);
            assertTrue(compare(a, b) < 0, "동일 트리플일 때 6 킥커가 5보다 커야 합니다.");
        }

        @Test
        @DisplayName("플러시: 5장 렉시코 비교")
        void shouldCompareFlushLexicographically() throws Exception {
            Object a = newHand();
            add(a, Suit.SPADES, Rank.ACE);
            add(a, Suit.SPADES, Rank.TEN);
            add(a, Suit.SPADES, Rank.NINE);
            add(a, Suit.SPADES, Rank.SEVEN);
            add(a, Suit.SPADES, Rank.SIX);
            open(a);
            Object b = newHand();
            add(b, Suit.HEARTS, Rank.ACE);
            add(b, Suit.HEARTS, Rank.TEN);
            add(b, Suit.HEARTS, Rank.NINE);
            add(b, Suit.HEARTS, Rank.SEVEN);
            add(b, Suit.HEARTS, Rank.FIVE);
            open(b);
            assertTrue(compare(a, b) < 0, "플러시 마지막 카드 6이 5보다 커야 합니다.");
        }

        @Test
        @DisplayName("풀 하우스: 트리플 우선 비교")
        void shouldCompareFullHouseByTripleFirst() throws Exception {
            Object a = newHand();
            add(a, Suit.SPADES, Rank.SEVEN);
            add(a, Suit.HEARTS, Rank.SEVEN);
            add(a, Suit.DIAMONDS, Rank.SEVEN);
            add(a, Suit.CLUBS, Rank.TWO);
            add(a, Suit.SPADES, Rank.TWO);
            open(a);
            Object b = newHand();
            add(b, Suit.SPADES, Rank.SIX);
            add(b, Suit.HEARTS, Rank.SIX);
            add(b, Suit.DIAMONDS, Rank.SIX);
            add(b, Suit.CLUBS, Rank.ACE);
            add(b, Suit.SPADES, Rank.ACE);
            open(b);
            assertTrue(compare(a, b) < 0, "777이 666보다 커야 합니다.");
        }

        @Test
        @DisplayName("포카드: 키커 비교")
        void shouldCompareFourOfAKindByKicker() throws Exception {
            Object a = newHand();
            add(a, Suit.SPADES, Rank.FIVE);
            add(a, Suit.HEARTS, Rank.FIVE);
            add(a, Suit.DIAMONDS, Rank.FIVE);
            add(a, Suit.CLUBS, Rank.FIVE);
            add(a, Suit.SPADES, Rank.ACE);
            open(a);
            Object b = newHand();
            add(b, Suit.SPADES, Rank.FIVE);
            add(b, Suit.HEARTS, Rank.FIVE);
            add(b, Suit.DIAMONDS, Rank.FIVE);
            add(b, Suit.CLUBS, Rank.FIVE);
            add(b, Suit.HEARTS, Rank.KING);
            open(b);
            assertTrue(compare(a, b) < 0, "A 킥커가 K보다 커야 합니다.");
        }

        @Test
        @DisplayName("스트레이트 플러시: 하이 비교 및 동률")
        void shouldCompareStraightFlushesCorrectly() throws Exception {
            Object a = newHand();
            add(a, Suit.SPADES, Rank.TWO);
            add(a, Suit.SPADES, Rank.THREE);
            add(a, Suit.SPADES, Rank.FOUR);
            add(a, Suit.SPADES, Rank.FIVE);
            add(a, Suit.SPADES, Rank.SIX);
            open(a);
            Object b = newHand();
            add(b, Suit.HEARTS, Rank.THREE);
            add(b, Suit.HEARTS, Rank.FOUR);
            add(b, Suit.HEARTS, Rank.FIVE);
            add(b, Suit.HEARTS, Rank.SIX);
            add(b, Suit.HEARTS, Rank.SEVEN);
            open(b);
            assertTrue(compare(b, a) < 0, "7하이 스트레이트 플러시가 6하이보다 커야 합니다.");
            Object c = newHand();
            add(c, Suit.CLUBS, Rank.THREE);
            add(c, Suit.CLUBS, Rank.FOUR);
            add(c, Suit.CLUBS, Rank.FIVE);
            add(c, Suit.CLUBS, Rank.SIX);
            add(c, Suit.CLUBS, Rank.SEVEN);
            open(c);
            assertEquals(0, compare(b, c), "동일 하이의 스트레이트 플러시는 동급이어야 합니다.");
        }

        @Test
        @DisplayName("로열 플러시: 동급")
        void shouldTieWithEqualRoyalFlush() throws Exception {
            Object a = newHand();
            add(a, Suit.SPADES, Rank.TEN);
            add(a, Suit.SPADES, Rank.JACK);
            add(a, Suit.SPADES, Rank.QUEEN);
            add(a, Suit.SPADES, Rank.KING);
            add(a, Suit.SPADES, Rank.ACE);
            open(a);
            Object b = newHand();
            add(b, Suit.HEARTS, Rank.TEN);
            add(b, Suit.HEARTS, Rank.JACK);
            add(b, Suit.HEARTS, Rank.QUEEN);
            add(b, Suit.HEARTS, Rank.KING);
            add(b, Suit.HEARTS, Rank.ACE);
            open(b);
            assertEquals(0, compare(a, b), "로열 플러시는 항상 동급이어야 합니다.");
        }

        @Test
        @DisplayName("스트레이트: 같은 하이면 동률")
        void shouldTieWithEqualStraight() throws Exception {
            Object a = newHand();
            add(a, Suit.SPADES, Rank.FOUR);
            add(a, Suit.HEARTS, Rank.FIVE);
            add(a, Suit.DIAMONDS, Rank.SIX);
            add(a, Suit.CLUBS, Rank.SEVEN);
            add(a, Suit.SPADES, Rank.EIGHT);
            open(a);
            Object b = newHand();
            add(b, Suit.HEARTS, Rank.FOUR);
            add(b, Suit.CLUBS, Rank.FIVE);
            add(b, Suit.SPADES, Rank.SIX);
            add(b, Suit.DIAMONDS, Rank.SEVEN);
            add(b, Suit.HEARTS, Rank.EIGHT);
            open(b);
            assertEquals("STRAIGHT", tierName(a));
            assertEquals(0, compare(a, b), "같은 하이의 스트레이트는 동급이어야 합니다.");
        }

        @Test
        @DisplayName("스트레이트: 휠 동률")
        void shouldTieWithEqualWheelStraight() throws Exception {
            Object a = newHand();
            add(a, Suit.SPADES, Rank.ACE);
            add(a, Suit.HEARTS, Rank.TWO);
            add(a, Suit.DIAMONDS, Rank.THREE);
            add(a, Suit.CLUBS, Rank.FOUR);
            add(a, Suit.SPADES, Rank.FIVE);
            open(a);
            Object b = newHand();
            add(b, Suit.HEARTS, Rank.ACE);
            add(b, Suit.CLUBS, Rank.TWO);
            add(b, Suit.SPADES, Rank.THREE);
            add(b, Suit.DIAMONDS, Rank.FOUR);
            add(b, Suit.HEARTS, Rank.FIVE);
            open(b);
            assertEquals(0, compare(a, b), "휠(5-하이) 스트레이트는 동급이어야 합니다.");
        }

        @Test
        @DisplayName("플러시: 동일 랭크면 동률")
        void shouldTieWithEqualFlush() throws Exception {
            Object a = newHand();
            add(a, Suit.SPADES, Rank.ACE);
            add(a, Suit.SPADES, Rank.QUEEN);
            add(a, Suit.SPADES, Rank.TEN);
            add(a, Suit.SPADES, Rank.SEVEN);
            add(a, Suit.SPADES, Rank.FOUR);
            open(a);
            Object b = newHand();
            add(b, Suit.HEARTS, Rank.ACE);
            add(b, Suit.HEARTS, Rank.QUEEN);
            add(b, Suit.HEARTS, Rank.TEN);
            add(b, Suit.HEARTS, Rank.SEVEN);
            add(b, Suit.HEARTS, Rank.FOUR);
            open(b);
            assertEquals(0, compare(a, b), "동일 랭크의 플러시는 동급이어야 합니다.");
        }

        @Test
        @DisplayName("포카드: 동률")
        void shouldTieWithEqualFourOfAKind() throws Exception {
            Object a = newHand();
            add(a, Suit.SPADES, Rank.FIVE);
            add(a, Suit.HEARTS, Rank.FIVE);
            add(a, Suit.DIAMONDS, Rank.FIVE);
            add(a, Suit.CLUBS, Rank.FIVE);
            add(a, Suit.SPADES, Rank.KING);
            open(a);
            Object b = newHand();
            add(b, Suit.HEARTS, Rank.FIVE);
            add(b, Suit.DIAMONDS, Rank.FIVE);
            add(b, Suit.CLUBS, Rank.FIVE);
            add(b, Suit.SPADES, Rank.FIVE);
            add(b, Suit.HEARTS, Rank.KING);
            open(b);
            assertEquals(0, compare(a, b), "동일 포카드와 동일 킥커는 동급이어야 합니다.");
        }

        @Test
        @DisplayName("풀 하우스: 동률")
        void shouldTieWithEqualFullHouse() throws Exception {
            Object a = newHand();
            add(a, Suit.SPADES, Rank.SEVEN);
            add(a, Suit.HEARTS, Rank.SEVEN);
            add(a, Suit.DIAMONDS, Rank.SEVEN);
            add(a, Suit.CLUBS, Rank.TWO);
            add(a, Suit.SPADES, Rank.TWO);
            open(a);
            Object b = newHand();
            add(b, Suit.HEARTS, Rank.SEVEN);
            add(b, Suit.DIAMONDS, Rank.SEVEN);
            add(b, Suit.CLUBS, Rank.SEVEN);
            add(b, Suit.SPADES, Rank.TWO);
            add(b, Suit.HEARTS, Rank.TWO);
            open(b);
            assertEquals(0, compare(a, b), "동일 트리플/페어의 풀 하우스는 동급이어야 합니다.");
        }

        @Test
        @DisplayName("쓰리 카드: 동률")
        void shouldTieWithEqualThreeOfAKind() throws Exception {
            Object a = newHand();
            add(a, Suit.SPADES, Rank.NINE);
            add(a, Suit.HEARTS, Rank.NINE);
            add(a, Suit.DIAMONDS, Rank.NINE);
            add(a, Suit.CLUBS, Rank.FIVE);
            add(a, Suit.SPADES, Rank.FOUR);
            open(a);
            Object b = newHand();
            add(b, Suit.HEARTS, Rank.NINE);
            add(b, Suit.DIAMONDS, Rank.NINE);
            add(b, Suit.CLUBS, Rank.NINE);
            add(b, Suit.SPADES, Rank.FIVE);
            add(b, Suit.HEARTS, Rank.FOUR);
            open(b);
            assertEquals(0, compare(a, b), "동일 트리플/킥커의 쓰리 카드는 동급이어야 합니다.");
        }

        @Test
        @DisplayName("투페어: 동률")
        void shouldTieWithEqualTwoPair() throws Exception {
            Object a = newHand();
            add(a, Suit.SPADES, Rank.QUEEN);
            add(a, Suit.HEARTS, Rank.QUEEN);
            add(a, Suit.DIAMONDS, Rank.JACK);
            add(a, Suit.CLUBS, Rank.JACK);
            add(a, Suit.SPADES, Rank.NINE);
            open(a);
            Object b = newHand();
            add(b, Suit.HEARTS, Rank.QUEEN);
            add(b, Suit.DIAMONDS, Rank.QUEEN);
            add(b, Suit.SPADES, Rank.JACK);
            add(b, Suit.CLUBS, Rank.JACK);
            add(b, Suit.HEARTS, Rank.NINE);
            open(b);
            assertEquals(0, compare(a, b), "동일 두 페어와 동일 킥커는 동급이어야 합니다.");
        }

        @Test
        @DisplayName("원페어: 동률")
        void shouldTieWithEqualOnePair() throws Exception {
            Object a = newHand();
            add(a, Suit.SPADES, Rank.KING);
            add(a, Suit.HEARTS, Rank.KING);
            add(a, Suit.DIAMONDS, Rank.QUEEN);
            add(a, Suit.CLUBS, Rank.JACK);
            add(a, Suit.SPADES, Rank.NINE);
            open(a);
            Object b = newHand();
            add(b, Suit.HEARTS, Rank.KING);
            add(b, Suit.DIAMONDS, Rank.KING);
            add(b, Suit.SPADES, Rank.QUEEN);
            add(b, Suit.CLUBS, Rank.JACK);
            add(b, Suit.HEARTS, Rank.NINE);
            open(b);
            assertEquals(0, compare(a, b), "동일 페어와 동일 3 키커는 동급이어야 합니다.");
        }

        @Test
        @DisplayName("하이카드: 동률")
        void shouldTieWithEqualHighCard() throws Exception {
            Object a = newHand();
            add(a, Suit.SPADES, Rank.ACE);
            add(a, Suit.HEARTS, Rank.QUEEN);
            add(a, Suit.DIAMONDS, Rank.JACK);
            add(a, Suit.CLUBS, Rank.NINE);
            add(a, Suit.SPADES, Rank.SEVEN);
            open(a);
            Object b = newHand();
            add(b, Suit.HEARTS, Rank.ACE);
            add(b, Suit.DIAMONDS, Rank.QUEEN);
            add(b, Suit.CLUBS, Rank.JACK);
            add(b, Suit.SPADES, Rank.NINE);
            add(b, Suit.HEARTS, Rank.SEVEN);
            open(b);
            assertEquals(0, compare(a, b), "동일 랭크의 하이카드는 동급이어야 합니다.");
        }
    }

    @Nested
    @DisplayName("예외 상황 처리")
    class ExceptionScenarios {
        @Test
        @DisplayName("중복 카드 추가 시 예외")
        void shouldThrowExceptionOnDuplicateCard() throws Exception {
            Object h = newHand();
            add(h, Suit.SPADES, Rank.ACE);
            
            // 같은 카드 중복 추가 시 예외가 발생해야 함
            assertThrows(Exception.class, () -> add(h, Suit.SPADES, Rank.ACE), 
                    "중복된 카드 추가 시 예외가 발생해야 합니다.");
        }

        @Test
        @DisplayName("개방되지 않은 핸드에서 getTier 호출 시 예외")
        void shouldThrowExceptionOnUnopenedHandGetTier() throws Exception {
            Object h = newHand();
            add(h, Suit.SPADES, Rank.ACE);
            add(h, Suit.HEARTS, Rank.KING);
            add(h, Suit.DIAMONDS, Rank.QUEEN);
            add(h, Suit.CLUBS, Rank.JACK);
            add(h, Suit.SPADES, Rank.TEN);
            
            // open()을 호출하지 않고 getTier() 호출
            assertThrows(Exception.class, () -> tierName(h), 
                    "개방되지 않은 핸드에서 getTier() 호출 시 예외가 발생해야 합니다.");
        }

        @Test
        @DisplayName("open 되지 않은 핸드에서 compareTo 호출 시 예외")
        void shouldThrowExceptionOnUnopenedHandCompareTo() throws Exception {
            Object h1 = newHand();
            Object h2 = newHand();
            
            add(h1, Suit.SPADES, Rank.ACE);
            add(h1, Suit.HEARTS, Rank.KING);
            add(h1, Suit.DIAMONDS, Rank.QUEEN);
            add(h1, Suit.CLUBS, Rank.JACK);
            add(h1, Suit.SPADES, Rank.TEN);
            
            add(h2, Suit.HEARTS, Rank.ACE);
            add(h2, Suit.DIAMONDS, Rank.KING);
            add(h2, Suit.CLUBS, Rank.QUEEN);
            add(h2, Suit.SPADES, Rank.JACK);
            add(h2, Suit.HEARTS, Rank.TEN);
            
            // h1만 open하고 h2는 open하지 않음
            open(h1);
            
            assertThrows(Exception.class, () -> compare(h1, h2), 
                    "open되지 않은 핸드와 비교 시 예외가 발생해야 합니다.");
        }
    }
}
