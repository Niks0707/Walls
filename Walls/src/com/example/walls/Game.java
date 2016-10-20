package com.example.walls;

import java.util.Random;
import java.util.Vector;
//Класс, реализующий логику программы
public class Game {

	public int StartCardsCount;
	public int SelectedCard;
	public int CpuCardN;
	public int CpuCardP;
	public int[] CardsPlayerN;
	public int[] CardsPlayerP;
	public int[] CardsCpuN;
	public int[] CardsCpuP;
	public int WallPlayer;
	public int WallCpu;
	public int GameResult;
	public int BeginWallValue;
	Random r;

	public Game(int startCardsCount, int beginWallValue) {
		StartCardsCount = startCardsCount;
		RegenerateCards();
		SelectedCard = -1;
		WallCpu = WallPlayer = BeginWallValue = beginWallValue;
		GameResult = 0;
	}
	//Метод инициализирует карты
	private int[] generateCards(Boolean isPositive) {
		r = new Random();
		int[] cards = new int[StartCardsCount];
		for (int i = 0; i < StartCardsCount; i++) {

			if (!isPositive) {
				cards[i] = -1 * r.nextInt(10);
			} else {
				cards[i] = r.nextInt(5);
			}
			if (cards[i] == 0) {
				i--;
			}
		}
		return cards;
	}
	//Метод реализует ход игрока
	public void movePlayer(int selectedCard) {
		WallCpu += CardsPlayerN[selectedCard];
		WallPlayer += CardsPlayerP[selectedCard];
		CardsPlayerN = removeCard(CardsPlayerN, selectedCard);
		CardsPlayerP = removeCard(CardsPlayerP, selectedCard);
		if (WallPlayer > BeginWallValue) {
			WallPlayer = BeginWallValue;
		}
		if (WallCpu <= 0) {
			GameResult = 2;
			WallCpu = 0;
		}
	}
	//Метод вызывает реализацию всех карт
	public void RegenerateCards() {
		CardsPlayerN = generateCards(false);
		CardsCpuN = generateCards(false);
		CardsPlayerP = generateCards(true);
		CardsCpuP = generateCards(true);
	}

	//Метод реализует ход противника
	public void moveCpu() {
		int move = dijkstraTSP();// CpuRandom();
		WallPlayer += CardsCpuN[move];
		WallCpu += CardsCpuP[move];
		CpuCardN = CardsCpuN[move];
		CpuCardP = CardsCpuP[move];
		CardsCpuN = removeCard(CardsCpuN, move);
		CardsCpuP = removeCard(CardsCpuP, move);

		if (WallCpu > BeginWallValue) {
			WallCpu = BeginWallValue;
		}
		if (WallPlayer <= 0) {
			GameResult = 1;
			WallPlayer = 0;
		}

	}
	
	private int CpuRandom() {
		r = new Random();
		int move = -1;
		if (CardsCpuN.length > 0) {
			if (CardsCpuN.length != 1) {
				move = r.nextInt(CardsCpuN.length - 1);
			} else {
				move = 0;
			}
		}
		return move;
	}
	//Метод удаляет карту из массива 
	private int[] removeCard(int[] cards, int index) {
		int[] tempCards = new int[cards.length - 1];
		int k = 0;
		for (int i = 0; i < cards.length; i++)
			if (i != index) {
				tempCards[k] = cards[i];
				k++;
			}
		return tempCards;
	}
	//Метод Дейктсры, который возвращает номер карты компьютера
	private int dijkstraTSP() {
		int nr_points = 5;
		int[][] Cost = { CardsCpuN, CardsCpuP, CardsCpuN, CardsCpuP, CardsCpuN };
		int[] mask;
		Vector<Integer> nod1 = new Vector<Integer>();
		Vector<Integer> nod2 = new Vector<Integer>();
		Vector<Integer> weight = new Vector<Integer>();
		mask = new int[nr_points];
		for (int i = 0; i < nr_points; i++)
			mask[i] = 0;
		int[] dd = new int[nr_points];
		int[] pre = new int[nr_points];
		int[] path = new int[nr_points + 1];
		int init_vert = 0, pos_in_path = 0, new_vert = 0;
		for (int i = 0; i < nr_points; i++) {
			dd[i] = Cost[init_vert][i];
			pre[i] = init_vert;
			path[i] = -1;
		}
		pre[init_vert] = 0;
		path[0] = init_vert;
		pos_in_path++;
		mask[init_vert] = 1;

		for (int k = 0; k < nr_points - 1; k++) {
			for (int j = 0; j < nr_points; j++)
				if (dd[j] != 0 && mask[j] == 0) {
					new_vert = j;
					break;
				}

			for (int j = 0; j < nr_points; j++)
				if (dd[j] < dd[new_vert] && mask[j] == 0 && dd[j] != 0)
					new_vert = j;

			mask[new_vert] = 1;
			path[pos_in_path] = new_vert;
			pos_in_path++;
			for (int j = 0; j < nr_points; j++) {
				if (mask[j] == 0) {
					if (dd[j] > dd[new_vert] + Cost[new_vert][j]) {
						dd[j] = dd[new_vert] + Cost[new_vert][j];
					}
				}
			}
		}
		path[nr_points] = init_vert;

		for (int i = 0; i < nr_points; i++) {
			nod1.addElement(path[i]);
			nod2.addElement(path[i + 1]);
			weight.addElement(Cost[path[i]][path[i + 1]]);
		}
		return nod1.get(0);
	}

}
