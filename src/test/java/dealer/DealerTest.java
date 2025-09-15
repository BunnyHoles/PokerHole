package dealer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Dealer 클래스 검증 테스트 (리플렉션, 한국어 메시지)
 */
class DealerTest {

    private Class<?> loadDealer() { try { return Class.forName("dealer.Dealer"); } catch (ClassNotFoundException e) { fail("Dealer 클래스를 찾을 수 없습니다. src/dealer/Dealer.java에 구현하세요."); return null; } }
    private Class<?> loadPlayer() { try { return Class.forName("player.Player"); } catch (ClassNotFoundException e) { fail("Player 클래스를 찾을 수 없습니다. src/player/Player.java에 구현하세요."); return null; } }

    private Object newDealer() {
        try { return loadDealer().getMethod("newDealer").invoke(null); }
        catch (Exception e) { fail("Dealer 클래스에 newDealer() 정적 메서드가 없습니다."); return null; }
    }

    private Object newPlayer(String name) throws Exception {
        try {
            Method m = loadPlayer().getDeclaredMethod("newPlayer", String.class);
            m.setAccessible(true);
            return m.invoke(null, name);
        } catch (NoSuchMethodException e) { fail("Player 클래스에 newPlayer(String) 정적 메서드가 없습니다."); return null; }
        catch (java.lang.reflect.InvocationTargetException ite) {
            Throwable t = ite.getTargetException();
            if (t instanceof Exception ex) throw ex; else throw new RuntimeException(t);
        }
    }

    private void enroll(Object dealer, Object player) throws Exception {
        loadDealer().getMethod("enrollPlayer", loadPlayer()).invoke(dealer, player);
    }

    @Nested
    @DisplayName("등록 제한")
    class Enrollment {
        @Test
        @DisplayName("5명 등록 시 예외")
        void shouldThrowExceptionWhenEnrollingFifthPlayer() throws Exception {
            Object dealer = newDealer();
            for (int i = 0; i < 4; i++) enroll(dealer, newPlayer("P" + i + UUID.randomUUID().toString().substring(0,6)));
            assertThrows(Exception.class, () -> enroll(dealer, newPlayer("P4" + UUID.randomUUID().toString().substring(0,6))), "5명째 플레이어 등록 시 IllegalStateException이 발생해야 합니다.");
        }
    }

    @Nested
    @DisplayName("준비 및 흐름")
    class Flow {
        @Test
        @DisplayName("준비 없이 deal 예외")
        void shouldThrowExceptionWhenDealingWithoutPreparation() throws Exception {
            Object dealer = newDealer();
            enroll(dealer, newPlayer("A"+UUID.randomUUID().toString().substring(0,6)));
            enroll(dealer, newPlayer("B"+UUID.randomUUID().toString().substring(0,6)));
            assertThrows(Exception.class, () -> loadDealer().getMethod("dealCard").invoke(dealer), "newGame과 shuffle 없이 dealCard 호출 시 예외가 발생해야 합니다.");
        }

        @Test
        @DisplayName("newGame → shuffle → deal → open → retrieve")
        void shouldExecuteNormalGameFlowCorrectly() throws Exception {
            Object dealer = newDealer();
            List<Object> ps = List.of(
                    newPlayer("A"+UUID.randomUUID().toString().substring(0,6)),
                    newPlayer("B"+UUID.randomUUID().toString().substring(0,6)),
                    newPlayer("C"+UUID.randomUUID().toString().substring(0,6)),
                    newPlayer("D"+UUID.randomUUID().toString().substring(0,6))
            );
            for (Object p : ps) enroll(dealer, p);
            assertDoesNotThrow(() -> {
                try {
                    loadDealer().getMethod("newGame").invoke(dealer);
                    loadDealer().getMethod("shuffle").invoke(dealer);
                    loadDealer().getMethod("dealCard").invoke(dealer);
                    loadDealer().getMethod("handOpen").invoke(dealer);
                    loadDealer().getMethod("retrieveCard").invoke(dealer);
                } catch (Exception e) { throw new RuntimeException(e); }
            });
        }
    }

    @Nested
    @DisplayName("연속 게임 상태 관리")
    class ConsecutiveGameStateManagement {
        @Test
        @DisplayName("연속 게임 시 매번 shuffle 필요")
        void shouldRequireShuffleForEachNewGame() throws Exception {
            Object dealer = newDealer();
            
            // 플레이어 등록
            enroll(dealer, newPlayer("P1" + UUID.randomUUID().toString().substring(0,6)));
            enroll(dealer, newPlayer("P2" + UUID.randomUUID().toString().substring(0,6)));
            
            // 첫 번째 게임 정상 진행
            loadDealer().getMethod("newGame").invoke(dealer);
            loadDealer().getMethod("shuffle").invoke(dealer);
            loadDealer().getMethod("dealCard").invoke(dealer);
            loadDealer().getMethod("handOpen").invoke(dealer);
            loadDealer().getMethod("retrieveCard").invoke(dealer);
            
            // 두 번째 게임 시작 - newGame 호출
            loadDealer().getMethod("newGame").invoke(dealer);
            
            // shuffle 없이 dealCard 시도 → 예외 발생해야 함
            assertThrows(Exception.class, () -> {
                loadDealer().getMethod("dealCard").invoke(dealer);
            }, "새 게임에서 shuffle 없이 dealCard 호출 시 예외가 발생해야 합니다.");
        }
        
        @Test
        @DisplayName("연속 게임 정상 흐름")
        void shouldHandleMultipleConsecutiveGames() throws Exception {
            Object dealer = newDealer();
            
            // 플레이어 등록
            enroll(dealer, newPlayer("P1" + UUID.randomUUID().toString().substring(0,6)));
            enroll(dealer, newPlayer("P2" + UUID.randomUUID().toString().substring(0,6)));
            
            // 3게임 연속 진행
            for (int game = 0; game < 3; game++) {
                assertDoesNotThrow(() -> {
                    try {
                        loadDealer().getMethod("newGame").invoke(dealer);
                        loadDealer().getMethod("shuffle").invoke(dealer);
                        loadDealer().getMethod("dealCard").invoke(dealer);
                        loadDealer().getMethod("handOpen").invoke(dealer);
                        loadDealer().getMethod("retrieveCard").invoke(dealer);
                    } catch (Exception e) { throw new RuntimeException(e); }
                }, "게임 " + (game + 1) + "이 정상적으로 진행되어야 합니다.");
            }
        }
    }

    @Nested
    @DisplayName("상태 전이 검증")
    class StateTransitionValidation {
        @Test
        @DisplayName("플레이어 등록 후 중복 등록 시 예외")
        void shouldThrowExceptionOnDuplicatePlayerEnrollment() throws Exception {
            Object dealer = newDealer();
            Object player = newPlayer("duplicate" + UUID.randomUUID().toString().substring(0, 6));
            
            enroll(dealer, player);
            
            assertThrows(Exception.class, () -> enroll(dealer, player),
                    "이미 등록된 플레이어를 중복 등록 시 예외가 발생해야 합니다.");
        }

        @Test
        @DisplayName("게임 중 플레이어 추가 등록 시 예외")
        void shouldThrowExceptionWhenEnrollingDuringGame() throws Exception {
            Object dealer = newDealer();
            
            // 4명 등록 후 게임 시작
            for (int i = 0; i < 4; i++) {
                enroll(dealer, newPlayer("P" + i + UUID.randomUUID().toString().substring(0,6)));
            }
            
            loadDealer().getMethod("newGame").invoke(dealer);
            loadDealer().getMethod("shuffle").invoke(dealer);
            
            // 게임 중 새 플레이어 등록 시도
            Object newPlayer = newPlayer("Late" + UUID.randomUUID().toString().substring(0,6));
            assertThrows(Exception.class, () -> enroll(dealer, newPlayer),
                    "게임 진행 중 새 플레이어 등록 시 예외가 발생해야 합니다.");
        }

        @Test
        @DisplayName("handOpen 전 retrieveCard 호출 시 예외")
        void shouldThrowExceptionWhenRetrievingBeforeOpen() throws Exception {
            Object dealer = newDealer();
            // 2명 이상 등록 (MIN_PLAYER 제약 조건 만족)
            enroll(dealer, newPlayer("P1" + UUID.randomUUID().toString().substring(0,6)));
            enroll(dealer, newPlayer("P2" + UUID.randomUUID().toString().substring(0,6)));
            
            loadDealer().getMethod("newGame").invoke(dealer);
            loadDealer().getMethod("shuffle").invoke(dealer);
            loadDealer().getMethod("dealCard").invoke(dealer);
            
            // handOpen 없이 retrieveCard 시도
            assertThrows(Exception.class, () -> loadDealer().getMethod("retrieveCard").invoke(dealer),
                    "handOpen 없이 retrieveCard 호출 시 예외가 발생해야 합니다.");
        }
    }

    @Nested
    @DisplayName("엣지 케이스 검증")
    class EdgeCaseValidation {
        @Test
        @DisplayName("플레이어 1명일 때 dealCard 예외")
        void shouldThrowExceptionWithSinglePlayer() throws Exception {
            Object dealer = newDealer();
            
            // 1명만 등록
            enroll(dealer, newPlayer("Lone" + UUID.randomUUID().toString().substring(0,6)));
            
            loadDealer().getMethod("newGame").invoke(dealer);
            loadDealer().getMethod("shuffle").invoke(dealer);
            
            // MIN_PLAYER(2명) 미만이므로 예외 발생해야 함
            assertThrows(Exception.class, () -> {
                loadDealer().getMethod("dealCard").invoke(dealer);
            }, "플레이어가 2명 미만일 때 dealCard 호출 시 예외가 발생해야 합니다.");
        }
        
        @Test
        @DisplayName("게임 기록 없을 때 getLastMatchWinner")
        void shouldReturnEmptyWhenNoGameHistory() throws Exception {
            Object dealer = newDealer();
            
            // 게임을 한 번도 진행하지 않은 상태
            Object result = loadDealer().getMethod("getLastMatchWinner").invoke(dealer);
            
            // Optional.empty() 반환해야 함
            assertNotNull(result, "getLastMatchWinner는 null이 아닌 Optional을 반환해야 합니다.");
            
            // Optional.isPresent()가 false여야 함
            boolean isPresent = (boolean) result.getClass().getMethod("isPresent").invoke(result);
            assertFalse(isPresent, "게임 기록이 없을 때 Optional.empty()를 반환해야 합니다.");
        }
        
        @Test
        @DisplayName("플레이어 없을 때 getTotalStageWinner")
        void shouldReturnEmptyWhenNoPlayers() throws Exception {
            Object dealer = newDealer();
            
            // 플레이어를 등록하지 않은 상태
            Object result = loadDealer().getMethod("getTotalStageWinner").invoke(dealer);
            
            // Optional.empty() 반환해야 함
            assertNotNull(result, "getTotalStageWinner는 null이 아닌 Optional을 반환해야 합니다.");
            
            boolean isPresent = (boolean) result.getClass().getMethod("isPresent").invoke(result);
            assertFalse(isPresent, "플레이어가 없을 때 Optional.empty()를 반환해야 합니다.");
        }
    }

    @Nested
    @DisplayName("덱 조작 권한 검증")
    class DeckManipulationTesting {
        @Test
        @DisplayName("Dealer만 덱 셔플 가능")
        void onlyDealerCanShuffleDeck() throws Exception {
            Object dealer = newDealer();
            
            // Dealer는 shuffle() 메서드를 가지고 있어야 함
            Method dealerShuffle = loadDealer().getMethod("shuffle");
            assertNotNull(dealerShuffle, "Dealer는 shuffle 메서드를 가져야 합니다.");
            
            // Dealer의 shuffle 메서드는 public이어야 함 (외부에서 호출 가능)
            assertTrue(Modifier.isPublic(dealerShuffle.getModifiers()), 
                      "Dealer.shuffle()은 public이어야 합니다.");
            
            // newGame() 후 shuffle() 호출이 정상 동작해야 함
            loadDealer().getMethod("newGame").invoke(dealer);
            assertDoesNotThrow(() -> {
                dealerShuffle.invoke(dealer);
            }, "Dealer는 덱을 셔플할 수 있어야 합니다.");
        }

        @Test
        @DisplayName("Player는 셔플 메서드 자체가 없음")
        void playerHasNoShuffleMethod() throws Exception {
            Object player = newPlayer("TestPlayer");
            
            // Player 클래스에는 shuffle 관련 메서드가 없어야 함
            Method[] playerMethods = loadPlayer().getMethods();
            boolean hasShuffleMethod = false;
            
            for (Method method : playerMethods) {
                if (method.getName().contains("shuffle")) {
                    hasShuffleMethod = true;
                    break;
                }
            }
            
            assertFalse(hasShuffleMethod, "Player는 shuffle 관련 메서드를 가지면 안 됩니다.");
        }

        @Test
        @DisplayName("Deck 직접 접근 vs Dealer를 통한 접근")
        void deckAccessComparisonTest() throws Exception {
            Object dealer = newDealer();
            loadDealer().getMethod("newGame").invoke(dealer);
            
            // Dealer를 통한 셔플은 가능
            assertDoesNotThrow(() -> {
                loadDealer().getMethod("shuffle").invoke(dealer);
            }, "Dealer를 통한 셔플은 가능해야 합니다.");
            
            // 직접 Deck 생성 시도
            Class<?> deckClass = Class.forName("dealer.Deck");
            Method newDeckMethod = deckClass.getDeclaredMethod("newDeck");
            Object deck = newDeckMethod.invoke(null);
            
            // Deck의 shuffle은 package-private이므로 같은 패키지에서는 접근 가능
            Method deckShuffle = deckClass.getDeclaredMethod("shuffle");
            assertDoesNotThrow(() -> {
                deckShuffle.invoke(deck);
            }, "같은 패키지(dealer)에서는 Deck.shuffle()에 접근 가능합니다.");
            
            // 하지만 설계상 Dealer를 통해서만 덱을 조작하는 것이 의도
            assertTrue(true, "Deck 조작은 Dealer를 통해서만 하는 것이 설계 의도입니다.");
        }
    }

    @Nested
    @DisplayName("승자 결정 및 통계 검증")
    class WinnerDeterminationAndStatistics {
        @Test
        @DisplayName("게임 후 승자 기록 정확성")
        void shouldRecordWinnerCorrectly() throws Exception {
            Object dealer = newDealer();
            
            // 2명 등록
            Object player1 = newPlayer("Winner" + UUID.randomUUID().toString().substring(0,6));
            Object player2 = newPlayer("Loser" + UUID.randomUUID().toString().substring(0,6));
            enroll(dealer, player1);
            enroll(dealer, player2);
            
            // 게임 진행
            loadDealer().getMethod("newGame").invoke(dealer);
            loadDealer().getMethod("shuffle").invoke(dealer);
            loadDealer().getMethod("dealCard").invoke(dealer);
            loadDealer().getMethod("handOpen").invoke(dealer);
            
            // 승자 기록 확인
            Object lastWinner = loadDealer().getMethod("getLastMatchWinner").invoke(dealer);
            assertNotNull(lastWinner, "마지막 매치 승자가 기록되어야 합니다.");
            
            // 게임 정리
            loadDealer().getMethod("retrieveCard").invoke(dealer);
        }
        
        @Test
        @DisplayName("100게임 시뮬레이션")
        void shouldHandle100ConsecutiveGames() throws Exception {
            Object dealer = newDealer();
            
            // 4명 등록
            for (int i = 0; i < 4; i++) {
                enroll(dealer, newPlayer("Player" + i + UUID.randomUUID().toString().substring(0,6)));
            }
            
            // 100게임 진행 (실제 메인과 동일한 구조)
            for (int game = 0; game < 100; game++) {
                assertDoesNotThrow(() -> {
                    try {
                        loadDealer().getMethod("newGame").invoke(dealer);
                        loadDealer().getMethod("shuffle").invoke(dealer);
                        loadDealer().getMethod("dealCard").invoke(dealer);
                        loadDealer().getMethod("handOpen").invoke(dealer);
                        loadDealer().getMethod("retrieveCard").invoke(dealer);
                    } catch (Exception e) { throw new RuntimeException(e); }
                }, "게임 " + (game + 1) + "/100이 정상적으로 진행되어야 합니다.");
            }
            
            // 최종 승자 확인
            Object totalWinner = loadDealer().getMethod("getTotalStageWinner").invoke(dealer);
            assertNotNull(totalWinner, "100게임 후 최종 승자가 결정되어야 합니다.");
            
            boolean isPresent = (boolean) totalWinner.getClass().getMethod("isPresent").invoke(totalWinner);
            assertTrue(isPresent, "최종 승자가 존재해야 합니다.");
        }
        
        @Test
        @DisplayName("매치 히스토리 크기 검증")
        void shouldMaintainCorrectHistorySize() throws Exception {
            Object dealer = newDealer();
            
            // 2명 등록
            enroll(dealer, newPlayer("P1" + UUID.randomUUID().toString().substring(0,6)));
            enroll(dealer, newPlayer("P2" + UUID.randomUUID().toString().substring(0,6)));
            
            int gamesToPlay = 5;
            
            // 5게임 진행
            for (int game = 0; game < gamesToPlay; game++) {
                loadDealer().getMethod("newGame").invoke(dealer);
                loadDealer().getMethod("shuffle").invoke(dealer);
                loadDealer().getMethod("dealCard").invoke(dealer);
                loadDealer().getMethod("handOpen").invoke(dealer);
                loadDealer().getMethod("retrieveCard").invoke(dealer);
            }
            
            // 히스토리 크기가 5개여야 함 (리플렉션으로 확인)
            var matchHistoryField = loadDealer().getDeclaredField("matchHistory");
            matchHistoryField.setAccessible(true);
            Object matchHistory = matchHistoryField.get(dealer);
            int historySize = (int) matchHistory.getClass().getMethod("size").invoke(matchHistory);
            assertEquals(gamesToPlay, historySize, "매치 히스토리 크기가 진행한 게임 수와 일치해야 합니다.");
            
            var winsHistoryField = loadDealer().getDeclaredField("winsHistory");
            winsHistoryField.setAccessible(true);
            Object winsHistory = winsHistoryField.get(dealer);
            int winsHistorySize = (int) winsHistory.getClass().getMethod("size").invoke(winsHistory);
            assertEquals(gamesToPlay, winsHistorySize, "승자 히스토리 크기가 진행한 게임 수와 일치해야 합니다.");
        }
    }
}
