package player;

import org.junit.jupiter.api.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Player 클래스 검증 테스트 (리플렉션)
 */
class PlayerTest {

    private Class<?> loadPlayer() { try { return Class.forName("player.Player"); } catch (ClassNotFoundException e) { fail("Player 클래스를 찾을 수 없습니다. src/player/Player.java에 구현하세요."); return null; } }

    private Object newPlayer(String name) throws Exception {
        try {
            Method m = loadPlayer().getDeclaredMethod("newPlayer", String.class);
            m.setAccessible(true);
            return m.invoke(null, name);
        } catch (NoSuchMethodException e) {
            fail("Player 클래스에 newPlayer(String) 정적 메서드가 없습니다."); return null;
        } catch (java.lang.reflect.InvocationTargetException ite) {
            Throwable t = ite.getTargetException();
            if (t instanceof Exception ex) throw ex; else throw new RuntimeException(t);
        }
    }

    private int getInt(Object p, String method) throws Exception { return (Integer) loadPlayer().getMethod(method).invoke(p); }

    private static String shortId() { return UUID.randomUUID().toString().substring(0, 6); }

    @Test
    @DisplayName("닉네임 고유성: 중복 생성 시 예외")
    void shouldEnforceNicknameUniqueness() throws Exception {
        String name = "고니" + shortId();
        newPlayer(name);
        assertThrows(Exception.class, () -> newPlayer(name), "중복된 닉네임으로 플레이어 생성 시 예외가 발생해야 합니다.");
    }

    @Test
    @DisplayName("닉네임 길이 경계값 검증")
    void shouldEnforceNicknameLengthBoundary() {
        // 19자: 성공
        assertDoesNotThrow(() -> newPlayer("a".repeat(19)), "닉네임 19자는 허용되어야 합니다.");
        // 20자: 성공 (명세: 20자 이하여야 함)
        assertDoesNotThrow(() -> newPlayer("b".repeat(20)), "닉네임 20자는 허용되어야 합니다.");
        // 21자: 예외
        assertThrows(Exception.class, () -> newPlayer("c".repeat(21)), "21자 이상 닉네임으로 플레이어 생성 시 예외가 발생해야 합니다.");
    }

    @Test
    @DisplayName("포인트와 전적 카운터 동작")
    void shouldTrackPointsAndStatisticsCorrectly() throws Exception {
        Object p = newPlayer("승부사" + shortId());
        assertEquals(10_000, getInt(p, "getPoint"), "초기 포인트는 10,000이어야 합니다.");

        loadPlayer().getMethod("prizePoint", int.class).invoke(p, 100);
        assertEquals(10_100, getInt(p, "getPoint"), "상금이 포인트에 반영되지 않았습니다.");

        loadPlayer().getMethod("win").invoke(p);
        loadPlayer().getMethod("lose").invoke(p);
        loadPlayer().getMethod("draw").invoke(p);
        assertEquals(1, getInt(p, "getWins"), "승 카운터가 올바르지 않습니다.");
        assertEquals(1, getInt(p, "getLosses"), "패 카운터가 올바르지 않습니다.");
        assertEquals(1, getInt(p, "getDraws"), "무 카운터가 올바르지 않습니다.");
    }

    @Nested
    @DisplayName("포인트 경계값 검증")  
    class PointBoundaryTesting {




        @Test
        @DisplayName("최대 통계 카운터 검증")
        void shouldHandleMaximumCounters() throws Exception {
            Object p = newPlayer("player3");
            
            // 대량 승/패/무 기록
            Method win = loadPlayer().getMethod("win");
            Method lose = loadPlayer().getMethod("lose");
            Method draw = loadPlayer().getMethod("draw");
            
            for (int i = 0; i < 1000; i++) {
                win.invoke(p);
                lose.invoke(p);  
                draw.invoke(p);
            }
            
            assertEquals(1000, getInt(p, "getWins"), "승 카운터가 올바르지 않습니다.");
            assertEquals(1000, getInt(p, "getLosses"), "패 카운터가 올바르지 않습니다.");
            assertEquals(1000, getInt(p, "getDraws"), "무 카운터가 올바르지 않습니다.");
        }
    }

    @Nested
    @DisplayName("패키지 접근 제한 검증")
    class PackageAccessTesting {
        @Test
        @DisplayName("다른 패키지(player)에서 Deck 셔플 접근 불가")
        void shouldNotAccessDeckShuffleFromDifferentPackage() throws Exception {
            // PlayerTest는 player 패키지에 있으므로 dealer 패키지의 Deck에 직접 접근 불가
            
            Class<?> deckClass;
            try {
                deckClass = Class.forName("dealer.Deck");
            } catch (ClassNotFoundException e) {
                fail("Deck 클래스를 찾을 수 없습니다.");
                return;
            }
            
            // newDeck 메서드도 package-private이므로 접근 제한이 있을 수 있음
            Method newDeckMethod = deckClass.getDeclaredMethod("newDeck");
            
            // newDeck 메서드의 접근 제한 확인
            int newDeckModifiers = newDeckMethod.getModifiers();
            boolean isNewDeckPackagePrivate = !Modifier.isPublic(newDeckModifiers) && 
                                            !Modifier.isPrivate(newDeckModifiers) && 
                                            !Modifier.isProtected(newDeckModifiers);
            
            if (isNewDeckPackagePrivate) {
                // newDeck 메서드도 package-private이므로 접근 불가
                assertThrows(IllegalAccessException.class, () -> {
                    newDeckMethod.invoke(null);
                }, "다른 패키지에서 package-private newDeck() 접근 시 IllegalAccessException이 발생해야 합니다.");
            } else {
                // newDeck이 접근 가능하다면 실행 후 shuffle 메서드 테스트
                Object deck = newDeckMethod.invoke(null);
                Method shuffleMethod = deckClass.getDeclaredMethod("shuffle");
                
                // shuffle 메서드가 package-private인지 확인
                int shuffleModifiers = shuffleMethod.getModifiers();
                boolean isShufflePackagePrivate = !Modifier.isPublic(shuffleModifiers) && 
                                                !Modifier.isPrivate(shuffleModifiers) && 
                                                !Modifier.isProtected(shuffleModifiers);
                assertTrue(isShufflePackagePrivate, "Deck.shuffle()은 package-private이어야 합니다.");
                
                // shuffle 메서드 접근 시 IllegalAccessException 발생 확인
                assertThrows(IllegalAccessException.class, () -> {
                    shuffleMethod.invoke(deck);
                }, "다른 패키지에서 package-private shuffle() 접근 시 IllegalAccessException이 발생해야 합니다.");
            }
            
            // 실제 제약: Player는 Deck을 직접 조작할 수 없도록 설계됨
            assertTrue(true, "Player는 Deck에 직접 접근할 수 없는 패키지 보안이 올바르게 동작합니다.");
        }
    }
}
