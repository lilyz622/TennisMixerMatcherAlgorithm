import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * UNDER CONSTRUCTION.
 * Currently does not handle cases where there are an insufficient number of people to fill 
 * all the positions, and some other cases.
 * This program is optimized for tennis mixers where a team of a f and m pair up
 * against another team of a f and m for each court.
 * 
 * This is the working algorithm for the MixUp.
 * 
 * This algorithm minimizes the occurrence of individuals playing in repeat groups
 * and maximizes the chance that individuals play with others they have not played with
 * or have played with the least number of times. It also tries to ensure that there
 * are an equal number of either sex on competing teams.
 * 
 * It does so by keeping a table of records for the number of times a given pair of players
 * have played either as partners or opponents. This is their pair "score". Then the program
 * creates sets of proposed "test instances" or sets of random groups. From these random 
 * groups, the program selects the test instance with the lowest score, thus sorting all 
 * the players into groups. After the players have been thus sorted into groups, the program
 * then tests which pairs of players have the highest difference in the number of times they
 * have played as partners and as opponents. If they have played as partners more times than
 * as opponents, they will be sorted into opposite teams, or vice versa, until the teams are
 * full.
 * 
 * @author yzhan265
 *
 */

public class TennisMixerMatcher {
	private static /*final*/int GROUPS;
	private static /*final*/ int TEAMS;
	private static /*final*/ int TEAMSIZE;
	private static /*final*/ int GROUPSIZE;
	private static int rounds;
	private static String[][] women;
	private static String[][] men;
	private static String[][] master;
	private static int[][][] records;
	private static int[][] overAllScoreGrid;
	private static int[][] scoreGrid;
//	private static int[][] outGroup;
	private static final int TOTAL=0;
	private static final int OPPONENT=1;
	private static final int PARTNER=2;
	
	public static void main(String[] args){
		
		Logger.getGlobal().setLevel(Level.OFF);
		
//		// Test data
//		String[] womenNames = {"Annie", "Bridget" , "Caris", "Dominique", "Elizabeth","Flora"};
//		String[] menNames = {"Adam", "Bob" , "Chris", "Dylan", "Elijah", "Felix"};
//		
//		women = assignIds(womenNames, 0);
//		men = assignIds(menNames, womenNames.length);
//		
//		master = new String[women.length+men.length][2];
//		System.arraycopy(women, 0, master, 0, women.length);
//		System.arraycopy(men, 0, master, women.length, men.length);
//		Logger.getGlobal().info(Arrays.deepToString(master));
	
//		int[][][] firstGameInstance = {
//				// First group
//				{
//					// First team
//					{
//						0, 5 // Caris and Bob are a team
//					},
//					// Second team
//					{
//						1, 4
//					}
//					
//				},
//				// Second group
//				{
//					// First team
//					{
//						2, 6
//					},
//					//Second team
//					{
//						3,7
//					}
//				}
//		};
//		
//		addAllRecords(firstGameInstance);
//		
//		int testScore = assignScores();
//		Logger.getGlobal().info("TEST score:" + testScore);
//		printRecords();
//		printAScoreGrid(scoreGrid);
		
		// enter names + genders and create ids for each person.
		setUp();
		printRecords();
		printAScoreGrid(overAllScoreGrid);
		printAScoreGrid(scoreGrid);
		for (int i = 0; i < rounds; i++) {
			System.out.println("\nROUND "+(i+1));
			addAllRecords(createTestInstance());
		}
		
		printRecords();
		printAScoreGrid(overAllScoreGrid);
		printAScoreGrid(scoreGrid);
		
	}
	

	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	
	public static void setUp(){
		Scanner kb = new Scanner(System.in);
		System.out.println("Hi! Welcome to MixerMatcher. "
				+ "\n"
				+ "MixerMatcher will randomly assign individuals to teams within groups, based on gender "
				+ "\n"
				+ "and optimized to maximize the assigning of players with different people, minimizing play with the same people.");
		System.out.println();
		boolean error_groups = false;
		do {
			System.out.print("How many courts or groups will we be playing in today: ");
			if (kb.hasNextInt()){
				GROUPS = kb.nextInt();
				error_groups = false;
			} else {
				error_groups = true;
				kb.nextLine();
			}
		} while (error_groups);
		
		
		// TODO: configure number of teams to be optional.
		TEAMS = 2;
//		boolean error_teams = false;
//		do{
//			System.out.println("Will we be competing within our groups today (Y or N): ");
//			if (kb.next().equalsIgnoreCase("Y")){
//				TEAMS = 2;
//				kb.nextLine();
//			} else if (kb.next().equalsIgnoreCase("N")){
//				TEAMS = 1;
//			} else {
//				error_teams = true;
//			}
//		} while (error_teams);
		
		boolean error_rounds = false;
		do {
			System.out.print("How many rounds will we be playing in today: ");
			if (kb.hasNextInt()){
				rounds = kb.nextInt();
				error_rounds = false;
			} else {
				error_rounds = true;
				kb.nextLine();
			}
		} while (error_rounds);
		kb.nextLine();
		
		createNamesAndIds(kb);
		
		kb.close();
		
		// initialize all records.
		records = new int[master.length][master.length][3];
		overAllScoreGrid = new int[master.length][master.length];
		scoreGrid = new int[master.length][master.length];	
		TEAMSIZE = master.length/(GROUPS*TEAMS);
		GROUPSIZE = TEAMS*TEAMSIZE;
	}
	
	/**
	 * Assigns random ids to the men and women.
	 * @param kb
	 */
	private static void createNamesAndIds(Scanner kb){
		String name = null;
//		Scanner kb = new Scanner(System.in);
		List<String> womenNamesAL = new ArrayList<String>();
		List<String> menNamesAL = new ArrayList<String>();
		while (true){
			System.out.println("Enter the names and genders of the players. Enter 0 to finish.");
			System.out.print("Enter a name:");
			name = kb.nextLine();
			if (name.equals("0")){
				break;
			}
			System.out.print(name+"'s gender (M or F):");
			String gender = kb.nextLine();
			if (gender.equalsIgnoreCase("M")){
				menNamesAL.add(name);
			} else if (gender.equalsIgnoreCase("F")){
				womenNamesAL.add(name);
			} else {
//				throw new InputMismatchException("Sorry, invalid input, please enter name and 'M' or 'F' for gender.");
				System.out.println("Sorry, invalid input, please enter name and 'M' or 'F' for gender.");
				continue;
			} 
		}
		
		String[] womenNames = womenNamesAL.toArray(new String[womenNamesAL.size()]);
		String[] menNames = menNamesAL.toArray(new String[menNamesAL.size()]);
		
		women = assignIds(womenNames, 0);
		men = assignIds(menNames, womenNames.length);
		
		master = new String[women.length+men.length][2];
		System.arraycopy(women, 0, master, 0, women.length);
		System.arraycopy(men, 0, master, women.length, men.length);
		System.out.println(Arrays.deepToString(master));

	}
	
	/**
	 * assigns id to names in the nameList given the starting id number.	
	 * @param nameList
	 * @param startingAssignmentNum
	 * @return
	 */
	private static String[][] assignIds (String[] nameList, int startingAssignmentNum){
		int listLength = nameList.length;
		String[][] assignedIds = randomAssignmentGenerator (startingAssignmentNum, listLength, nameList);
		return assignedIds;
	}//assignIds
	
	private static String[][] randomAssignmentGenerator (int startingAssignmentNum, int listLength, String[] nameList) {
		Integer[] randomNumList = new Integer[listLength];
		int endAssignmentNum = listLength + startingAssignmentNum;
		for (int i = startingAssignmentNum; i<endAssignmentNum; i++){
			boolean assigned = false;
			while (!assigned){
				int assignment = (int) (Math.random()*listLength);
//				Logger.getGlobal().info("Random number: "+assignment);
				if (randomNumList[assignment] == null) {
					randomNumList[assignment] = i;
					assigned = true;
				}
//				Logger.getGlobal().info(Arrays.toString(randomNumList));
			}//while
		}//for
		String[][] assigned = new String[listLength][2];
		for (int m = 0; m<listLength; m++){
			assigned[m][0] = String.valueOf(m+startingAssignmentNum);
			assigned[m][1] = nameList[randomNumList[m]-startingAssignmentNum];
//			Logger.getGlobal().info(Arrays.deepToString(assigned));
		}
//		Logger.getGlobal().info(Arrays.deepToString(assigned));
		return assigned;
	}
	
	private static int[] randomAssignmentGenerator (int startingAssignmentNum, int listLength, Integer[] inputList, Integer[] mustIncludeList) {
		Integer[] randomNumList = new Integer[listLength];
		int endAssignmentNum = listLength + startingAssignmentNum;
		for (int i = startingAssignmentNum; i<endAssignmentNum; i++){
			boolean assigned = false;
			while (!assigned){
				int assignment = (int) (Math.random()*listLength);
//				Logger.getGlobal().info("Random number: "+assignment);
				if (randomNumList[assignment] == null) {
					randomNumList[assignment] = i;
					assigned = true;
				}
//				Logger.getGlobal().info(Arrays.toString(randomNumList));
			}//while
		}//for
		
		int[] assigned = new int[listLength];
		for (int m = 0; m<listLength; m++){
			assigned[m] = inputList[randomNumList[m]-startingAssignmentNum];
//			Logger.getGlobal().info(Arrays.deepToString(assigned));
		}
//		Logger.getGlobal().info(Arrays.deepToString(assigned));
		return assigned;
	}
	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	public static int[][][] createTestInstance(){
		int[][] bestGroupInstance = createBestGroupInstance();
//		printAScoreGrid(bestGroupInstance);
		
		int[][][] bestGameInstance = createBestTeamInstance(bestGroupInstance);
		
//		printRecords();
		
		return bestGameInstance;
	}
	
	private static int[][][] createBestTeamInstance(int[][] bestGroupInstance){
		
		int[][][] bestGameInstance = new int[bestGroupInstance.length][TEAMS][TEAMSIZE];
		/* For each group within the game:*/
		for (int groups = 0; groups < bestGroupInstance.length; groups++) {
			int[] currentGroup = bestGroupInstance[groups];
			List<Integer[]> relationshipArray = createRelationshipArray(currentGroup);
			bestGameInstance[groups] = createBestTeams(relationshipArray, currentGroup);
			System.out.println("Group "+groups+" :"+Arrays.deepToString(bestGameInstance[groups]));
		}// for group
		
		return bestGameInstance;
	}
	
	/**
	 * 
	 * @param currentGroup
	 * @return relationshipArray
	 */
	private static List<Integer[]> createRelationshipArray(int[] currentGroup){
			/* STEP 1
			 * 
			 * go through array, calculate relationshipScore from records, and 
			 * sort pairs from largest to smallest relationshipScore.
			 *
			 * relationshipScore = PARTNERS - OPPONENTS and indicates whether the pair 
			 * should be in opposite or on the same team.
			 */
			List<Integer[]> relationshipArray = new ArrayList<Integer[]>();
			
			for (int player1 = 0; player1 < GROUPSIZE; player1++){
				int player1Id = currentGroup[player1];
				// traverse along the group array, calculating the relationshipScore between player1 and player2.
				
				for (int player2 = player1 + 1; player2 < GROUPSIZE; player2++){
					int player2Id = currentGroup[player2];
					int relationshipScore = records[player1Id][player2Id][PARTNER] - records[player1Id][player2Id][OPPONENT];
					// we don't want pairs of the same gender to be on the same team
					// so we will put them at the beginning of the relationshipArray.
					
					int countWomen = 0; // counts the number of women in the pair.
					for (int i = 0; i< women.length; i++){
						if (player1Id == Integer.parseInt(women[i][0])){
							countWomen++;
						} 
						if (player2Id == Integer.parseInt(women[i][0])){
							countWomen++;
						} 
					}
					
					Integer[] relationshipSet = {relationshipScore, player1Id, player2Id};
					
					// if both players in the pair are women or are men, add them to be 
					// sorted into opposite teams first.
					if (countWomen == 0 || countWomen ==2){
						relationshipArray.add(0, relationshipSet);
					}
					else {
						// adds relationshipSet to relationshipArray, placing it in order of relationshipScore
						int count = 0;
						while (count < relationshipArray.size() && Math.abs(relationshipScore) < Math.abs(relationshipArray.get(count)[0])){
							count ++;
						}
						relationshipArray.add(count, relationshipSet);
					}
				}// for player2
			}// for player1
			
//			System.out.println("relationshipArray");
//			for (Integer[] team: relationshipArray){
//				System.out.println(Arrays.toString(team));
//			}
			return relationshipArray;
	}
	
	private static int[][] createBestTeams(List<Integer[]> relationshipArray, int[] currentGroup){
		/*
		 * STEP 2
		 * 
		 * Sort individuals into teams based on their relationshipScores with other members.
		 * 
		 * Starting with the greatest relationshipScore, we sort until either team is full.
		 * Then we add the remaining players to the other team.
		 */
		
		boolean full = false;
		int count = 0;
		int playerCount = 0;
		List<Integer> team1 = new ArrayList<Integer>();
		List<Integer> team2 = new ArrayList<Integer>();
//		System.out.println("Current group:"+Arrays.toString(currentGroup));
		
		while (!full) {
			fillingOptions: 
			{
			// if either teams are full, fill the other team with the rest of the players.
			if (team1.size() == TEAMSIZE){
				for (int remainingPlayer: currentGroup){
					if (!team1.contains(remainingPlayer) && !team2.contains(remainingPlayer)){
//						System.out.println("Remaining player:"+remainingPlayer);
						team2.add(remainingPlayer);
						playerCount++;
					}// if
				}// for remainingPlayer
				full = true;
				break;
			} else if (team2.size() == TEAMSIZE){
				for (int remainingPlayer: currentGroup){
					if (!team1.contains(remainingPlayer) && !team2.contains(remainingPlayer)){
//						System.out.println("Remaining player:"+remainingPlayer);
						team1.add(remainingPlayer);
						playerCount++;
					}// if
				}// for remainingPlayer
				full = true;
				break;
				
			} else {
				 // if both are assigned, nothing happens. 
				 // if one of them are assigned, assign the other.
				for (int i = 0; i < 2; i++){
					if (team1.contains(relationshipArray.get(count)[i+1])){
						if (team2.contains(relationshipArray.get(count)[(i+1)%2+1])){
							;
						} else if (relationshipArray.get(count)[0]<0){
							// pair should be on same team
							team1.add(relationshipArray.get(count)[(i+1)%2+1]);
							playerCount++;
						} else /*(relationshipArray.get(count)[0]>0)*/{
							// pair should be on different teams.
							team2.add(relationshipArray.get(count)[(i+1)%2+1]);
							playerCount++;
						}
						break fillingOptions;
					} else if (team2.contains(relationshipArray.get(count)[i+1])){
						if (team1.contains(relationshipArray.get(count)[(i+1)%2+1])){
							;
						} else if (relationshipArray.get(count)[0]<0){
							// pair should be on same team.
							team2.add(relationshipArray.get(count)[(i+1)%2+1]);
							playerCount++;
						} else /*(relationshipArray.get(count)[0]>0)*/{
							// pair should be on different teams.
							team1.add(relationshipArray.get(count)[(i+1)%2+1]);
							playerCount++;
						}
						break fillingOptions;
					} else {
						;
					}
				}// for i test if they are assigned.
									
				// if neither are assigned, assign both to a team, unless there is not enough room.
				if (relationshipArray.get(count)[0]<0){
					// they should be partners this time. Add them in the team with fewer people.
					if (team1.size()<team2.size() && team1.size()<TEAMSIZE-2){
						team1.add(relationshipArray.get(count)[1]);
						playerCount++;
						team1.add(relationshipArray.get(count)[2]);
						playerCount++;
					} else if (team2.size()<team1.size() && team2.size()<TEAMSIZE-2){
						team2.add(relationshipArray.get(count)[1]);
						playerCount++;
						team2.add(relationshipArray.get(count)[2]);
						playerCount++;
					} else {
						team1.add(relationshipArray.get(count)[1]);
						playerCount++;
						team2.add(relationshipArray.get(count)[2]);
						playerCount++;
					}
				} else {
					// they should be on separate teams.
					team1.add(relationshipArray.get(count)[1]);
					playerCount++;
					team2.add(relationshipArray.get(count)[2]);
					playerCount++;
				}
				break fillingOptions;					
			}// giant if
			}// fillingOptions
		count++;
		Logger.getGlobal().info("filling count:"+count+" / "+TEAMSIZE+"\nCurrent teams: "+
				"Team 1:"+team1.toString() +
				"Team 2:"+team2.toString() +
				"Player Count: "+playerCount);
		}// while !full
		
//		System.out.println("Team 1:"+team1.toString());
//		System.out.println("Team 2:"+team2.toString());
		
		int[][] bestTeams = {listToIntArrayConverter(team1), listToIntArrayConverter(team2)};
		
		return bestTeams;
	}
	
	private static int[] listToIntArrayConverter(List<Integer> listInt){
		int[] intArray = new int[listInt.size()];
		for (int i = 0; i<listInt.size(); i++){
			intArray[i] = listInt.get(i);
		}
		return intArray;
	}
	
	private static int[][] createBestGroupInstance(){
		// First, create the best group.
		int numPossibilities = 40;
		int bestGroupInstanceCount = 0;
		int minScore = -1;
		
		// TODO: initialize bestGroupInstance with the correct size.
		int[][] bestGroupInstance = new int[GROUPS][GROUPSIZE];
		while (bestGroupInstanceCount < numPossibilities){
			bestGroupInstanceCount++;
			
			Integer[] womenId = new Integer[women.length];
			for (int i = 0; i < women.length; i++) {
				womenId[i] = Integer.valueOf(women[i][0]);
			}
			Integer[] menId = new Integer[men.length];
			for (int i = 0; i < men.length; i++) {
				menId[i] = Integer.valueOf(men[i][0]);
			}
			
			int[] randomWomen = randomAssignmentGenerator(0, women.length, womenId, null);
			int[] randomMen = randomAssignmentGenerator(0, men.length, menId, null);
//					Logger.getGlobal().info(Arrays.deepToString(randomWomen));
//					Logger.getGlobal().info(Arrays.deepToString(randomMen));
			int[][] groupInstance = new int[GROUPS][GROUPSIZE];
			// Assign W0, W1 to first group, then W2, W3 to the second, etc.
			for (int group = 0; group < GROUPS; group++) {
				// Populate half the group with females.
				for (int femaleMember = 0; femaleMember < (GROUPSIZE / 2); femaleMember++) {
					// Assign female member id to group.
					groupInstance[group][femaleMember] = randomWomen[femaleMember + group*GROUPSIZE/2];
				}
			}
			
			// Assign M0, M1 to first group, then M2, M3 to the second, etc.
			for (int group = 0; group < GROUPS; group++) {
				// Populate half the group with males.
				for (int maleMember = 0; maleMember < GROUPSIZE / 2; maleMember++) {
					// Assign male member id to group.
					groupInstance[group][maleMember+GROUPSIZE/2] = randomMen[maleMember + group*GROUPSIZE/2];
				}
			}
//					Logger.getGlobal().info(Arrays.deepToString(groupInstance));
			
			// Create groupInstanceScoreGrid with the trial groupInstance scores
			// and calculate the score.
			int trialScore = 0;
			
			for (int group = 0; group < GROUPS; group++){
				for (int member=0; member < GROUPSIZE; member++){
					// For each member, if they are not equal to themselves in the same group,
					// record that they are in the same group with the other members.
					for (int otherMember=0; otherMember < GROUPSIZE; otherMember++){
						int memberId = groupInstance[group][member];
						int otherMemberId = groupInstance[group][otherMember];
						if (memberId!= otherMemberId){
							trialScore = trialScore + scoreGrid[memberId][otherMemberId];
						}
					}
				}
			}
			
//					Logger.getGlobal().info("Trial Count:" + count + "\nTrial Score:"+trialScore);
			
			if (trialScore < minScore || minScore < 0){
				minScore = trialScore;
				bestGroupInstance = groupInstance;
			}
		}
		
//				printGrid(bestGroupInstance);

		// TODO: calculate and record oddGroup and outGroup and numGroup. 
		// TODO: take into account oddGroup and outGroup when assigning groups.

			
		Logger.getGlobal().info("Best Score: "+ minScore);
//				printGrid(bestGroupInstance);
//				printAScoreGrid(scoreGrid);
//		System.out.println("Best score from test:"+minScore);
		return bestGroupInstance;
	}
	
	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	/**
	 * adds the information from a game instance to the records table
	 * @param gameInstance
	 */
	public static int addAllRecords(int[][][] gameInstance){
		for (int group = 0; group<GROUPS; group++){
			for (int team = 0; team<TEAMS; team++){
				for (int teamMember = 0; teamMember < TEAMSIZE; teamMember++){
					addPlayerRecords(gameInstance[group][team][teamMember], gameInstance[group][team], team, gameInstance[group]);
				}
			}
		}

		for (int row = 0; row<records.length; row++){
			for (int column = 0; column < records[0].length; column++){
				overAllScoreGrid[row][column] = records[row][column][0];
			}
		}	
		return assignScores();
	}
	
	/**
	 * helper method for addAllRecords; adds the records from the 
	 * game instance for a single player
	 * @param player
	 * @param teamMembers
	 * @param playerTeam
	 * @param opponentTeam
	 */
	private static void addPlayerRecords(int player, int[] teamMembers, int playerTeam, int[][] opponentTeam){
//		Logger.getGlobal().info("Player: "+player+
//				", Team: "+ Arrays.toString(teamMembers) +
//				", Group: "+ Arrays.deepToString(opponentTeam)
//				);
		
		for (int i=0; i<teamMembers.length; i++){
			if (player != teamMembers[i]){
				records[player][teamMembers[i]][PARTNER]+=1;
				records[player][teamMembers[i]][TOTAL] = records[player][teamMembers[i]][OPPONENT]+ records[player][teamMembers[i]][PARTNER];
			}
		}
		
		for (int oppTeam=0; oppTeam<opponentTeam.length; oppTeam++){
			if (oppTeam != playerTeam){
				for (int opponentMember= 0; opponentMember<opponentTeam[0].length; opponentMember++){
				records[player][opponentTeam[oppTeam][opponentMember]][OPPONENT]+=1;
				records[player][opponentTeam[oppTeam][opponentMember]][TOTAL] = records[player][opponentTeam[oppTeam][opponentMember]][OPPONENT]+ records[player][opponentTeam[oppTeam][opponentMember]][PARTNER];
				}
			}

		}
	}
	
	/**
	 * Created for TESTING
	 * calculates the score for each game.
	 * @param records
	 * @return
	 */
	private static int assignTestScore (int[][] testGrid){
		int[][] scoreTestGrid= new int[testGrid.length][testGrid[0].length];
		int totalScore=0;
		int minScore = testGrid[0][0];
		for (int row = 0; row < testGrid.length; row++){
			for (int column = 0; column < testGrid[0].length; column++){
				int score = testGrid[row][column];
				if (minScore >= score){
					minScore = score;
				}
			}
		}
		
		for (int row = 0; row < testGrid.length; row++){
			for (int column = 0; column < testGrid[0].length; column++){
				scoreTestGrid[row][column] = testGrid[row][column]- minScore;
				totalScore+=scoreTestGrid[row][column];
			}
		}
		Logger.getGlobal().info("Min Score (should be 0): "+minScore);
//		printAScoreGrid(scoreTestGrid);
		return totalScore;
	}
	
	private static int assignScores (){
		scoreGrid= new int[records.length][records[0].length];
		int totalScore=0;
		int minScore = records[0][1][0];
		for (int row = 0; row < scoreGrid.length; row++){
			for (int column = 0; column < scoreGrid[0].length; column++){
				if (row != column) {
					int score = records[row][column][0];
					scoreGrid[row][column] = score;
					if (minScore >= score) {
						minScore = score;
					} 
				}
			}
		}
		
		for (int row = 0; row < scoreGrid.length; row++){
			for (int column = 0; column < scoreGrid[0].length; column++){
				if (row!= column) {
					scoreGrid[row][column] -= minScore;
					totalScore += scoreGrid[row][column];
				}
			}
		}
		Logger.getGlobal().info("Min Score (should be 0): "+minScore);
		return totalScore;
	}
	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
		
	private static void printRecords() {
		System.out.println(Arrays.deepToString(master));
		for (int i = 0; i<records.length; i++){
			System.out.println(Arrays.deepToString(records[i])+Arrays.deepToString(master[i]));
		}
	}
	
	private static void printAScoreGrid(int[][] printableGrid) {
		for (int i = 0; i<printableGrid.length; i++){
			System.out.print(i+"  ");
		}
		
		System.out.println();

		for (int i = 0; i<printableGrid.length; i++){
			System.out.println(i+" " +Arrays.toString(printableGrid[i]));
		}
	}
	
	private static void printGrid(int[][] printableGrid){
		for (int i = 0; i<printableGrid.length; i++){
			System.out.println(Arrays.toString(printableGrid[i]));
		}
	}
	
}
