package domain.blackjackgame;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import domain.card.Card;
import domain.card.CardFactory;
import domain.user.Dealer;
import domain.user.Player;
import domain.user.User;

public class BlackJackGame {
	private static final int ACE = 1;
	private static final int ONE_WINNER = 1;
	private static final int FIRST_TWO = 2;
	private static final int ACE_DIFFERENCE = 10;
	private static final int DEALER_CARD_STANDARD = 16;
	private static final int BLACKJACK = 21;
	private static final double FIRST_WIN = 1.5;

	private final Scanner scanner;
	private final List<Player> players;
	private final List<Card> cards;
	private final Set<Integer> checkSameCard;
	private final Dealer dealer;

	public BlackJackGame() {
		scanner = new Scanner(System.in);
		players = new ArrayList<>();
		dealer = new Dealer();
		cards = CardFactory.create();
		checkSameCard = new HashSet<>();
	}

	public void init() {
		while (!inputPlayer()) {
		}
		if (startGame()) {
			printCards();
			twoCardPlayerWin();
			return;
		}
		printCards();
		if (playGame()) {
			dealerFail();
			return;
		}
		calculateResult();
		decideWinner();
	}

	private boolean inputPlayer() {
		System.out.println("게임에 참여할 사람의 이름을 입력하세요.(쉼표 기준으로 분리)");
		String[] playerNames = scanner.nextLine().split(",");
		if (playerNames.length == 0) {
			return false;
		}
		for (String playerName : playerNames) {
			while (!inputBettingMoney(playerName)) {
			}
		}
		return true;
	}

	private boolean inputBettingMoney(String playerName) {
		System.out.println(playerName + "의 배팅 금액은?");
		try {
			double bettingMoney = Double.parseDouble(scanner.nextLine());
			Player player = new Player(playerName, bettingMoney);
			players.add(player);
			return true;
		} catch (Exception e) {
			System.out.println("잘못된 입력입니다. 다시 입력해 주세요");
			return false;
		}
	}

	private boolean startGame() {
		giveFirstTwoCards(dealer);
		for (Player player : players) {
			giveFirstTwoCards(player);
		}
		System.out.print("딜러와 ");
		for (Player player : players) {
			System.out.print(player.getName() + " ");
		}
		System.out.println("에게 2장의 카드를 나누어주었습니다.");
		calculateResult();
		List<User> blackJackList = getBlackJackWinnerList();
		if (blackJackList.size() > 0) {
			return true;
		}
		calculateOringinalScore();
		return false;
	}

	private void giveFirstTwoCards(User user) {
		for (int i = 0; i < FIRST_TWO; i++) {
			giveCard(user);
		}
	}

	private void giveCard(User user) {
		Card randomCard = randomizeCard();
		user.addCard(randomCard);
	}

	private Card randomizeCard() {
		Random generator = new Random();
		int randomIndex = 0;
		while (checkSameCard.contains(randomIndex = generator.nextInt(cards.size()))) {
		}
		Card randomCard = cards.get(randomIndex);
		checkSameCard.add(randomIndex);
		return randomCard;
	}

	private boolean playGame() {
		if (dealer.getScore() <= DEALER_CARD_STANDARD) {
			System.out.println("딜러는 16이하라 한장의 카드를 더 받았습니다.");
			giveCard(dealer);
		}
		if (isFailScore(dealer.getScore())) {
			return true;
		}
		for (Player player : players) {
			getMoreCards(player);
		}
		return false;
	}

	private void decideWinner() {
		List<User> blackJackList = getBlackJackWinnerList();
		if (blackJackList.size() == 0) { // 블랙잭이 없을 경우
			blackJackList = findWinner();
			if (blackJackList.size() == 0) {
				playerFail();
				return;
			}
		}
		if (blackJackList.contains(dealer) && blackJackList.size() == ONE_WINNER) {// 딜러만 블랙잭일 경우
			playerFail();
			return;
		}
		playerWin(blackJackList);
		return;
	}

	private void twoCardPlayerWin() {
		List<User> blackJackList = getBlackJackWinnerList();
		System.out.println("### 최종 수익 ###");
		double dealerMoney = 0;
		for (Player player : players) {
			if (blackJackList.contains(player)) {
				dealerMoney -= player.getBettingMoney() * FIRST_WIN;
				System.out.println(player.getName() + ": " + player.getBettingMoney() * FIRST_WIN);
				continue;
			}
			dealerMoney += player.getBettingMoney();
			System.out.println(player.getName() + ": " + player.getBettingMoney() * -1);
		}
		System.out.println("딜러: " + dealerMoney);
	}

	private void playerWin(List<User> blackJackList) {
		System.out.println("### 최종 수익 ###");
		double dealerMoney = 0;
		for (Player player : players) {
			if (blackJackList.contains(player)) {
				dealerMoney -= player.getBettingMoney();
				System.out.println(player.getName() + ": " + player.getBettingMoney());
				continue;
			}
			dealerMoney += player.getBettingMoney();
			System.out.println(player.getName() + ": " + player.getBettingMoney() * -1);
		}
		System.out.println("딜러: " + dealerMoney);
	}

	private List<User> findWinner() {
		List<User> winnerList = new ArrayList<>();
		for (Player player : players) {
			if (!player.getFail() && dealer.getScore() <= player.getScore()) {
				winnerList.add(player);
			}
		}
		return winnerList;
	}

	private List<User> getBlackJackWinnerList() {
		List<User> winnerList = new ArrayList<>();
		for (Player player : players) {
			if (player.getScore() == BLACKJACK) {
				winnerList.add(player);
			}
		}
		if (dealer.getScore() == BLACKJACK) {
			winnerList.add(dealer);
		}
		return winnerList;
	}

	private void playerFail() {
		System.out.println("### 최종 수익 ###");
		double dealerMoney = 0;
		for (Player player : players) {
			System.out.println(player.getName() + ": " + player.getBettingMoney() * -1);
			dealerMoney += player.getBettingMoney();
		}
		System.out.println("딜러: " + dealerMoney);
	}

	private void dealerFail() {
		printCards();
		System.out.println("딜러가 21을 초과했습니다.");
		System.out.println("### 최종 수익 ###");
		double dealerMoney = 0;
		for (Player player : players) {
			System.out.println(player.getName() + ": " + player.getBettingMoney());
			dealerMoney -= player.getBettingMoney();
		}
		System.out.println("딜러: " + dealerMoney);
	}

	private void calculateOringinalScore() {
		calculateOriginalUserScore(dealer);
		for (Player player : players) {
			calculateOriginalUserScore(player);
		}
	}

	private void calculateOriginalUserScore(User user) {
		if (!user.hasAce())
			return;
		for (Card card : user.getCards()) {
			if (card.getSymbol().getScore() == ACE) {
				user.setScore(user.getScore() - ACE_DIFFERENCE);
			}
		}
	}

	private void calculateResult() {
		calculateUserScore(dealer);
		for (Player player : players) {
			calculateUserScore(player);
		}
	}

	private void calculateUserScore(User user) {
		if (!user.hasAce())
			return;
		for (Card card : user.getCards()) {
			if (isFailScore(user.getScore() + ACE_DIFFERENCE)) {
				return;
			}
			if (card.getSymbol().getScore() == ACE) {
				user.setScore(user.getScore() + ACE_DIFFERENCE);
			}
		}
	}

	private void getMoreCards(Player player) {
		while (!isFailScore(player.getScore())) {
			System.out.println(player.getName() + "는 한장의 카드를 더 받겠습니까?(예는 y, 아니오는 n)");
			String input = scanner.nextLine();
			if (input.equals("y")) {
				giveCard(player);
				printCards();
				continue;
			}
			if (input.equals("n")) {
				return;
			}
			System.out.println("잘못된 입력을 하셨습니다. 다시 입력해주세요.");
		}
		System.out.println("플레이어 " + player.getName() + "은 21을 초과하였습니다.");
		player.setFail();
	}

	private boolean isFailScore(int score) {
		if (score > BLACKJACK) {
			return true;
		}
		return false;
	}

	private void printCards() {
		System.out.println("========================================================================");
		System.out.print("딜러의 카드: ");
		dealer.printCards();
		for (Player player : players) {
			System.out.print(player.getName() + "의 카드: ");
			player.printCards();
		}
		System.out.println("========================================================================");
	}
}
