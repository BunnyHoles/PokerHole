// ⚠️ 테스트 안내: 모든 테스트가 성공하면 이 파일과 src/main/java/announcer/Announcer.java의 주석을 제거하세요.
// 관련 파일: src/main/java/announcer/Announcer.java


import announcer.Announcer;
import dealer.Dealer;
import player.Player;

public class PokerHole {
    public static void main(String[] args) {
        runGameWithAnnouncer();
    }

    public static void runGameWithAnnouncer() {
        // 🎩 딜러 입장
        Dealer dealer = Dealer.newDealer();
        Announcer.enrollDealer(dealer);

        // 👥 플레이어 입장
        Announcer.standbyStage();
        Announcer.enrollPlayer(dealer.enrollPlayer(Player.newPlayer("고니")));
        Announcer.enrollPlayer(dealer.enrollPlayer(Player.newPlayer("평경장")));
        Announcer.enrollPlayer(dealer.enrollPlayer(Player.newPlayer("짝귀")));
        Announcer.enrollPlayer(dealer.enrollPlayer(Player.newPlayer("아귀")));

        // 💀 포커 100판 진행
        Announcer.playStage();
        for (int i = 0; i < 100; i++) {
            Announcer.newGame(); // 🎲 새로운 게임을 시작한다
            dealer.newGame();

            Announcer.cardShuffle(); // 🔄 카드를 섞는다
            dealer.shuffle();

            Announcer.dealCard(); // 🃏 카드를 나눠준다
            dealer.dealCard();

            Announcer.handOpen(); // 👀 카드를 오픈한다
            dealer.handOpen();

            Announcer.matchResult(dealer.getLatestMatch()); // 📊 매치 결과를 출력한다
            Announcer.openWinner(dealer.getLastMatchWinner()); // 🏆 매치 승자를 발표한다

            Announcer.endGame(); // 🔚 게임을 종료한다
            dealer.retrieveCard();
        }

        // 🏁 스테이지 결과 발표
        Announcer.endStage();
        Announcer.stageWinner(dealer.getTotalStageWinner()); // 🏆 스테이지 승자를 발표한다
        Announcer.showStageResult(dealer.getPlayers()); // 📈 스테이지 결과 출력
    }
}
