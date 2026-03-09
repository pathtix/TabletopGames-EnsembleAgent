Couple notes about LLMActionPlayer observations on games (dotsandboxes, poker)

TAG Dots and Boxes game state, prompt and returned action

State text = 

Game=[32mDotsAndBoxes[0m { minPlayers = 2 maxPlayers = 6 categories = [Simple, Abstract, TerritoryBuilding] mechanics = [Enclosure][34m GS = true[0m[34m FM = true[0m[34m Params = true[0m[34m GUI = true[0m } , CurrentPlayer=0, Round=0, Turn=14, Scores=[P0=0.0, P1=1.0], Status=GAME_ONGOING, Phase=Main, State={"Edge_Owner_6_061":-1,"Edge_Owner_6_263":-1,"Edge_Owner_6_465":-1,"Edge_Owner_5_060":-1,"Edge_Owner_5_262":1,"Edge_Owner_5_464":-1,"Edge_Owner_6_171":-1,"Edge_Owner_6_373":-1,"Edge_Owner_6_575":-1,"Edge_Owner_7_172":-1,"Edge_Owner_7_374":1,"Edge_Owner_0_111":-1,"Edge_Owner_0_313":-1,"Edge_Owner_0_515":-1,"Edge_Owner_0_001":0,"Edge_Owner_0_203":0,"Edge_Owner_0_405":-1,"Edge_Owner_1_112":-1,"Edge_Owner_1_314":0,"Edge_Owner_2_021":-1,"Edge_Owner_2_223":-1,"Edge_Owner_2_425":-1,"Edge_Owner_1_020":-1,"Edge_Owner_1_222":-1,"Edge_Owner_1_424":1,"Edge_Owner_2_131":-1,"Edge_Owner_2_333":-1,"Edge_Owner_2_535":-1,"Edge_Owner_3_132":-1,"Edge_Owner_3_334":-1,"Edge_Owner_4_041":1,"Edge_Owner_4_243":-1,"Edge_Owner_4_445":-1,"Edge_Owner_3_040":-1,"Edge_Owner_3_242":-1,"Edge_Owner_3_444":-1,"Edge_Owner_4_151":-1,"Edge_Owner_4_353":-1,"Edge_Owner_4_555":-1,"Edge_Owner_5_152":-1,"Edge_Owner_5_354":-1,"Edge_Owner_6_162":-1,"Edge_Owner_6_364":-1,"Edge_Owner_5_161":-1,"Edge_Owner_5_363":-1,"Edge_Owner_5_565":-1,"Edge_Owner_6_070":1,"Edge_Owner_6_272":-1,"Edge_Owner_6_474":-1,"Edge_Owner_7_071":-1,"Edge_Owner_7_273":-1,"Edge_Owner_7_475":-1,"Edge_Owner_0_010":0,"Edge_Owner_0_212":1,"Edge_Owner_0_414":0,"Edge_Owner_0_102":-1,"Edge_Owner_0_304":0,"Edge_Owner_1_011":-1,"Edge_Owner_1_213":-1,"Edge_Owner_1_415":-1,"Edge_Owner_2_122":-1,"Edge_Owner_2_324":1,"Edge_Owner_1_121":-1,"Edge_Owner_1_323":1,"Edge_Owner_1_525":-1,"Edge_Owner_2_030":-1,"Edge_Owner_2_232":-1,"Edge_Owner_2_434":-1,"Edge_Owner_3_031":-1,"Edge_Owner_3_233":-1,"Edge_Owner_3_435":-1,"Edge_Owner_4_142":0,"Edge_Owner_4_344":-1,"Edge_Owner_3_141":-1,"Edge_Owner_3_343":-1,"Edge_Owner_3_545":-1,"Edge_Owner_4_050":-1,"Edge_Owner_4_252":-1,"Edge_Owner_4_454":-1,"Edge_Owner_5_051":-1,"Edge_Owner_5_253":-1,"Edge_Owner_5_455":-1,"Cell_Owner_2_3":-1,"Cell_Edge_Count_2_3":1,"Cell_Owner_2_4":-1,"Cell_Edge_Count_2_4":0,"Cell_Owner_5_0":-1,"Cell_Edge_Count_5_0":0,"Cell_Owner_5_1":-1,"Cell_Edge_Count_5_1":1,"Cell_Owner_5_2":-1,"Cell_Edge_Count_5_2":1,"Cell_Owner_5_3":-1,"Cell_Edge_Count_5_3":0,"Cell_Owner_1_0":-1,"Cell_Edge_Count_1_0":0,"Cell_Owner_5_4":-1,"Cell_Edge_Count_5_4":0,"Cell_Owner_1_1":-1,"Cell_Edge_Count_1_1":0,"Cell_Owner_1_2":-1,"Cell_Edge_Count_1_2":1,"Cell_Owner_1_3":1,"Cell_Edge_Count_1_3":4,"Cell_Owner_1_4":-1,"Cell_Edge_Count_1_4":1,"Cell_Owner_4_0":-1,"Cell_Edge_Count_4_0":1,"Cell_Owner_4_1":-1,"Cell_Edge_Count_4_1":1,"Cell_Owner_4_2":-1,"Cell_Edge_Count_4_2":0,"Cell_Owner_4_3":-1,"Cell_Edge_Count_4_3":0,"Cell_Owner_0_0":-1,"Cell_Edge_Count_0_0":2,"Cell_Owner_4_4":-1,"Cell_Edge_Count_4_4":0,"Cell_Owner_0_1":-1,"Cell_Edge_Count_0_1":1,"Cell_Owner_0_2":-1,"Cell_Edge_Count_0_2":2,"Cell_Owner_0...

Prompt = 

You are controlling a TAG game-playing agent.
You are Player 0. Choose the action that is best for Player 0.
Choose exactly one legal action from the numbered list.

Output contract:
ACTION_ID: <int>

Rules:
- Return exactly one line, do not include any other text, punctuation, or explanation.
- Use exactly the prefix ACTION_ID:
- The id must be one of the listed action ids.

State summary:
Game=[32mDotsAndBoxes[0m { minPlayers = 2 maxPlayers = 6 categories = [Simple, Abstract, TerritoryBuilding] mechanics = [Enclosure][34m GS = true[0m[34m FM = true[0m[34m Params = true[0m[34m GUI = true[0m } , CurrentPlayer=0, Round=0, Turn=14, Scores=[P0=0.0, P1=1.0], Status=GAME_ONGOING, Phase=Main, State={"Edge_Owner_6_061":-1,"Edge_Owner_6_263":-1,"Edge_Owner_6_465":-1,"Edge_Owner_5_060":-1,"Edge_Owner_5_262":1,"Edge_Owner_5_464":-1,"Edge_Owner_6_171":-1,"Edge_Owner_6_373":-1,"Edge_Owner_6_575":-1,"Edge_Owner_7_172":-1,"Edge_Owner_7_374":1,"Edge_Owner_0_111":-1,"Edge_Owner_0_313":-1,"Edge_Ownerx_0_515":-1,"Edge_Owner_0_001":0,"Edge_Owner_0_203":0,"Edge_Owner_0_405":-1,"Edge_Owner_1_112":-1,"Edge_Owner_1_314":0,"Edge_Owner_2_021":-1,"Edge_Owner_2_223":-1,"Edge_Owner_2_425":-1,"Edge_Owner_1_020":-1,"Edge_Owner_1_222":-1,"Edge_Owner_1_424":1,"Edge_Owner_2_131":-1,"Edge_Owner_2_333":-1,"Edge_Owner_2_535":-1,"Edge_Owner_3_132":-1,"Edge_Owner_3_334":-1,"Edge_Owner_4_041":1,"Edge_Owner_4_243":-1,"Edge_Owner_4_445":-1,"Edge_Owner_3_040":-1,"Edge_Owner_3_242":-1,"Edge_Owner_3_444":-1,"Edge_Owner_4_151":-1,"Edge_Owner_4_353":-1,"Edge_Owner_4_555":-1,"Edge_Owner_5_152":-1,"Edge_Owner_5_354":-1,"Edge_Owner_6_162":-1,"Edge_Owner_6_364":-1,"Edge_Owner_5_161":-1,"Edge_Owner_5_363":-1,"Edge_Owner_5_565":-1,"Edge_Owner_6_070":1,"Edge_Owner_6_272":-1,"Edge_Owner_6_474":-1,"Edge_Owner_7_071":-1,"Edge_Owner_7_273":-1,"Edge_Owner_7_475":-1,"Edge_Owner_0_010":0,"Edge_Owner_0_212":1,"Edge_Owner_0_414":0,"Edge_Owner_0_102":-1,"Edge_Owner_0_304":0,"Edge_Owner_1_011":-1,"Edge_Owner_1_213":-1,"Edge_Owner_1_415":-1,"Edge_Owner_2_122":-1,"Edge_Owner_2_324":1,"Edge_Owner_1_121":-1,"Edge_Owner_1_323":1,"Edge_Owner_1_525":-1,"Edge_Owner_2_030":-1,"Edge_Owner_2_232":-1,"Edge_Owner_2_434":-1,"Edge_Owner_3_031":-1,"Edge_Owner_3_233":-1,"Edge_Owner_3_435":-1,"Edge_Owner_4_142":0,"Edge_Owner_4_344":-1,"Edge_Owner_3_141":-1,"Edge_Owner_3_343":-1,"Edge_Owner_3_545":-1,"Edge_Owner_4_050":-1,"Edge_Owner_4_252":-1,"Edge_Owner_4_454":-1,"Edge_Owner_5_051":-1,"Edge_Owner_5_253":-1,"Edge_Owner_5_455":-1,"Cell_Owner_2_3":-1,"Cell_Edge_Count_2_3":1,"Cell_Owner_2_4":-1,"Cell_Edge_Count_2_4":0,"Cell_Owner_5_0":-1,"Cell_Edge_Count_5_0":0,"Cell_Owner_5_1":-1,"Cell_Edge_Count_5_1":1,"Cell_Owner_5_2":-1,"Cell_Edge_Count_5_2":1,"Cell_Owner_5_3":-1,"Cell_Edge_Count_5_3":0,"Cell_Owner_1_0":-1,"Cell_Edge_Count_1_0":0,"Cell_Owner_5_4":-1,"Cell_Edge_Count_5_4":0,"Cell_Owner_1_1":-1,"Cell_Edge_Count_1_1":0,"Cell_Owner_1_2":-1,"Cell_Edge_Count_1_2":1,"Cell_Owner_1_3":1,"Cell_Edge_Count_1_3":4,"Cell_Owner_1_4":-1,"Cell_Edge_Count_1_4":1,"Cell_Owner_4_0":-1,"Cell_Edge_Count_4_0":1,"Cell_Owner_4_1":-1,"Cell_Edge_Count_4_1":1,"Cell_Owner_4_2":-1,"Cell_Edge_Count_4_2":0,"Cell_Owner_4_3":-1,"Cell_Edge_Count_4_3":0,"Cell_Owner_0_0":-1,"Cell_Edge_Count_0_0":2,"Cell_Owner_4_4":-1,"Cell_Edge_Count_4_4":0,"Cell_Owner_0_1":-1,"Cell_Edge_Count_0_1":1,"Cell_Owner_0_2":-1,"Cell_Edge_Count_0_2":2,"Cell_Owner_0...

Legal actions:
0: (0,1) -> (0,2)
1: (1,0) -> (1,1)
2: (1,2) -> (1,3)
3: (1,4) -> (1,5)
4: (1,1) -> (2,1)
5: (1,5) -> (2,5)
6: (2,1) -> (2,2)
7: (2,0) -> (3,0)
8: (2,2) -> (3,2)
9: (2,4) -> (3,4)
10: (3,0) -> (3,1)
11: (3,2) -> (3,3)
12: (3,4) -> (3,5)
13: (3,1) -> (4,1)
14: (3,3) -> (4,3)
15: (3,5) -> (4,5)
16: (4,3) -> (4,4)
17: (4,0) -> (5,0)
18: (4,2) -> (5,2)
19: (4,4) -> (5,4)
20: (5,0) -> (5,1)
21: (5,2) -> (5,3)
22: (5,4) -> (5,5)
23: (5,1) -> (6,1)
24: (5,3) -> (6,3)
25: (5,5) -> (6,5)
26: (6,1) -> (6,2)
27: (6,3) -> (6,4)
28: (6,2) -> (7,2)
29: (6,4) -> (7,4)
30: (7,0) -> (7,1)
31: (7,2) -> (7,3)
32: (7,4) -> (7,5)
33: (0,4) -> (0,5)
34: (0,1) -> (1,1)
35: (0,3) -> (1,3)
36: (0,5) -> (1,5)
37: (1,1) -> (1,2)
38: (1,0) -> (2,0)
39: (1,2) -> (2,2)
40: (2,0) -> (2,1)
41: (2,2) -> (2,3)
42: (2,4) -> (2,5)
43: (2,1) -> (3,1)
44: (2,3) -> (3,3)
45: (2,5) -> (3,5)
46: (3,1) -> (3,2)
47: (3,3) -> (3,4)
48: (3,0) -> (4,0)
49: (3,2) -> (4,2)
50: (3,4) -> (4,4)
51: (4,2) -> (4,3)
52: (4,4) -> (4,5)
53: (4,1) -> (5,1)
54: (4,3) -> (5,3)
55: (4,5) -> (5,5)
56: (5,1) -> (5,2)
57: (5,3) -> (5,4)
58: (5,0) -> (6,0)
59: (5,4) -> (6,4)
60: (6,0) -> (6,1)
61: (6,2) -> (6,3)
62: (6,4) -> (6,5)
63: (6,1) -> (7,1)
64: (6,3) -> (7,3)
65: (6,5) -> (7,5)
66: (7,1) -> (7,2)

ACTION_ID: 2

Llm agent vs random agent 
Dots and Boxes
———————————————-
1 run costs 42-47 calls
Input tokens per query = 2613
output tokens per query = 6


—————————————————————————————————

Action representation optimization

Action legend:
H_y_x means horizontal edge (x,y)->(x+1,y)
V_y_x means vertical edge (x,y)->(x,y+1)

Legal actions:
0: V_1_0
1: V_3_0
2: H_0_0
3: H_2_0
4: H_4_0
5: V_0_1
6: V_2_1
7: V_4_1
8: H_1_1
9: H_3_1
10: H_5_1
11: V_1_2
12: V_3_2
13: H_0_2
14: H_2_2
15: H_4_2
16: V_0_3
17: V_2_3
18: V_4_3
19: H_1_3
20: H_3_3
21: H_5_3
22: V_1_4
23: V_3_4
24: H_0_4
25: H_2_4
26: H_4_4
27: V_0_5
28: V_2_5
29: V_4_5
30: H_1_5
31: H_3_5
32: H_5_5
33: V_1_6
34: V_3_6
35: H_0_6
36: H_2_6
37: H_4_6
38: V_0_7
39: V_2_7
40: V_4_7
41: V_0_0
42: V_2_0
43: V_4_0
44: H_1_0
45: H_3_0
46: H_5_0
47: V_1_1
48: V_3_1
49: H_0_1
50: H_2_1
51: H_4_1
52: V_0_2
53: V_2_2
54: V_4_2
55: H_1_2
56: H_3_2
57: H_5_2
58: V_1_3
59: V_3_3
60: H_0_3
61: H_2_3
62: H_4_3
63: V_0_4
64: V_2_4
65: V_4_4
66: H_1_4
67: H_3_4
68: H_5_4
69: V_1_5
70: V_3_5
71: H_0_5
72: H_2_5
73: H_4_5
74: V_0_6
75: V_2_6
76: V_4_6
77: H_1_6
78: H_3_6
79: H_5_6
80: V_1_7
81: V_3_7

Token amount for a first round = 693

—————————————————————————————

Action legend:
H_y_x means horizontal edge (x,y)->(x+1,y)
V_y_x means vertical edge (x,y)->(x,y+1)

Legal actions:
0 V_1_0
1 V_3_0
2 H_0_0
3 H_2_0
4 H_4_0
5 V_0_1
6 V_2_1
7 V_4_1
8 H_1_1
9 H_3_1
10 H_5_1
11 V_1_2
12 V_3_2
13 H_0_2
14 H_2_2
15 H_4_2
16 V_0_3
17 V_2_3
18 V_4_3
19 H_1_3
20 H_3_3
21 V_1_4
22 V_3_4
23 H_0_4
24 H_2_4
25 H_4_4
26 V_0_5
27 V_2_5
28 V_4_5
29 H_1_5
30 H_3_5
31 H_5_5
32 V_1_6
33 V_3_6
34 H_0_6
35 H_2_6
36 H_4_6
37 V_0_7
38 V_2_7
39 V_4_7
40 V_0_0
41 V_2_0
42 V_4_0
43 H_1_0
44 H_3_0
45 H_5_0
46 V_1_1
47 V_3_1
48 H_0_1
49 H_2_1
50 H_4_1
51 V_0_2
52 V_2_2
53 V_4_2
54 H_1_2
55 H_3_2
56 H_5_2
57 V_1_3
58 V_3_3
59 H_0_3
60 H_2_3
61 H_4_3
62 V_0_4
63 V_2_4
64 V_4_4
65 H_1_4
66 H_3_4
67 H_5_4
68 V_1_5
69 V_3_5
70 H_0_5
71 H_2_5
72 H_4_5
73 V_0_6
74 V_2_6
75 V_4_6
76 H_1_6
77 H_3_6
78 H_5_6
79 V_1_7
80 V_3_7
 removed columns optimization = 604 tokens

—————————————————————————————

LLM agent vs basic MCTS agent

Game: DotsAndBoxes, Players: 2, Mode: RANDOM, TotalGames: 50, GamesPerMatchup: 50
basicmcts got 50.00 points. basicmcts won 100.0% of the 50 games of the tournament. basicmcts won 100.0% of the 50 games it played during the tournament.
basicmcts got a mean score of 33.74.
basicmcts won 100.0% of the 50 games against llm.

llm got 0.00 points. llm won 0.0% of the 50 games of the tournament. llm won 0.0% of the 50 games it played during the tournament.
llm got a mean score of 1.26.
llm won 0.0% of the 50 games against basicmcts.

---- Ranking ---- (+/- are standard errors on the mean calculated using a Normal approximation) 
basicmcts: Win rate 1.00 +/- 0.000	Mean Ordinal 1.00 +/- 0.00
llm: Win rate 0.00 +/- 0.000	Mean Ordinal 2.00 +/- 0.00

==============================================================

Dots and boxes can be a bad choice for LLMs to analyze and play.


———————————————————————————————————————

Poker gamestate for compact state creation:

gameState = {PokerGameState@2879} "-574311203|1457940400|793930898|*|61331496|1348653018|894940|2020480035|1268|*|-1441942839|-1450224737|40359|40545|-1441942839|-590547487|"
 playerDecks = {ArrayList@2899}  size = 2
 playerMoney = {Counter[2]@2900} 
 playerBet = {Counter[2]@2901} 
 drawDeck = {Deck@2902} "{Queen Hearts},{Hearts 7},{Clubs 6},{Diamonds 8},{Hearts 2},{King Clubs},{Hearts 6},{Diamonds 2},{Clubs 10},{Spades 5},{Spades 2},{Queen Spades},{Hearts 10},{Hearts 5},{Diamonds 6},{Spades 9},{Ace Diamonds},{Clubs 4},{King Diamonds},{Spades 8},{Hearts 3},{Diamonds 9},{Spades 3},{Jack Spades},{Spades 6},{Hearts 4},{Clubs 9},{Jack Clubs},{Hearts 9},{King Hearts},{Ace Hearts},{Diamonds 3},{Jack Diamonds},{Ace Clubs},{Diamonds 10},{Clubs 8},{Diamonds 7},{Spades 10},{Clubs 5},{Clubs 2},{Hearts 8},{Queen Diamonds},{Diamonds 4},{Ace Spades},{King Spades},{Diamonds 5},{Spades 4},{Queen Clubs}"
 communityCards = {Deck@2903} "EmptyDeck"
 moneyPots = {ArrayList@2904}  size = 1
 playerNeedsToCall = {boolean[2]@2905} [true, false]
 playerFold = {boolean[2]@2906} [false, false]
 playerAllIn = {boolean[2]@2907} [false, false]
 playerActStreet = {boolean[2]@2908} [false, false]
 bet = false
 bigId = 0
 gameParameters = {PokerGameParameters@2909} 
 gameType = {GameType@2910} "[32mPoker[0m {\n\tminPlayers = 2\n\tmaxPlayers = 14\n\tcategories = [Cards, ComicBook, Number, MoviesTVRadio, Bluffing]\n\tmechanics = [HandManagement, LoseATurn, TakeThat][34m\n\tGS = true[0m[34m\n\tFM = true[0m[34m\n\tParams = true[0m[34m\n\tGUI = true[0m\n}\n"
 allComponents = {Area@2911} "Component{componentID=62, type=AREA, ownerId=-1, componentName='', properties={}}"
 tick = 0
 roundCounter = 0
 turnCounter = 0
 turnOwner = 0
 firstPlayer = 0
 nPlayers = 2
 nTeams = 2
 listeners = {ArrayList@2912}  size = 0
 playerTimer = {ElapsedCpuChessTimer[2]@2913} 
 history = {ArrayList@2914}  size = 0
 historyText = {ArrayList@2915}  size = 0
 gameStatus = {CoreConstants$GameResult@2916} "GAME_ONGOING"
 playerResults = {CoreConstants$GameResult[2]@2917} 
 gamePhase = {PokerGameState$PokerGamePhase@2918} "Preflop"
 actionsInProgress = {Stack@2919}  size = 0
 coreGameParameters = {CoreParameters@2920} 
 gameID = 0
 rnd = {Random@2885} 
 redeterminisationRnd = {Random@2921} 

 -----------------------------------------------------------------------------

Poker game prompt template

promptText =
"""
You are a Texas Hold'em poker agent.
You are Player %d. Maximise your long-run chip count.

Output contract:
ACTION_ID: <int>

Rules:
- Return exactly one line with no other text, punctuation, or explanation.
- Use exactly the prefix ACTION_ID:
- The id must be one of the listed action ids.

Game state:
%s

Action glossary:
- Check  : stay in without betting (only when no bet faces you)
- Call   : match the current bet
- Bet N  : open betting at N chips
- Raise xM : raise to M times the current bet
- AllIn  : commit all remaining chips
- Fold   : surrender your hand

Legal actions (id  action):
%s
""".formatted(gameState.getCurrentPlayer(), stateText, actionsText);


------------------------------------------------------------------------------

## a random game prompt

You are a Texas Hold'em poker agent.
You are Player 0. Maximise your long-run chip count.

Output contract:
ACTION_ID: <int>

Rules:
- Return exactly one line with no other text, punctuation, or explanation.
- Use exactly the prefix ACTION_ID:
- The id must be one of the listed action ids.

Game state:
Phase=Preflop | MyHand=[{Jack Hearts}, {Ace Hearts}] | Community=[None] | Pot=15 | P0: stack=45 bet=5 [BB] | P1: stack=40 bet=10 [SB]

Action glossary:
- Check  : stay in without betting (only when no bet faces you)
- Call   : match the current bet
- Bet N  : open betting at N chips
- Raise xM : raise to M times the current bet
- AllIn  : commit all remaining chips
- Fold   : surrender your hand

Legal actions (id  action):
0 Call
1 Raise x1.0
2 Raise x2.0
3 Raise x3.0
4 Raise x4.0
5 Fold
6 All in

------------------------------------------------------------

input tokens = 239
output tokens = 6

------------------------------------------------------------

A 50 game of LLMActionPlayer vs basicMCTS (LLM model = gemini-2.0-flash-lite)

test start time 11:28 (should time it, internatl tools, this is just a draft testing)

============= Poker - 50 games played ============= 
basicmcts got 20.00 points. basicmcts won 40.0% of the 50 games of the tournament. basicmcts won 40.0% of the 50 games it played during the tournament.
basicmcts got a mean score of 39.30.
basicmcts won 40.0% of the 50 games against llm.

llm got 30.00 points. llm won 60.0% of the 50 games of the tournament. llm won 60.0% of the 50 games it played during the tournament.
llm got a mean score of 60.70.
llm won 60.0% of the 50 games against basicmcts.

---- Ranking ---- (+/- are standard errors on the mean calculated using a Normal approximation) 
llm: Win rate 0.60 +/- 0.069	Mean Ordinal 1.40 +/- 0.07
basicmcts: Win rate 0.40 +/- 0.069	Mean Ordinal 1.60 +/- 0.07

-------------------------------------------------------------

A 50 game of LLMActionPlayer vs basicMCTS (LLM model = gemini-2.0-flash)

============= Poker - 50 games played ============= 
basicmcts got 17.00 points. basicmcts won 34.0% of the 50 games of the tournament. basicmcts won 34.0% of the 50 games it played during the tournament.
basicmcts got a mean score of 32.90.
basicmcts won 34.0% of the 50 games against llm.

llm got 33.00 points. llm won 66.0% of the 50 games of the tournament. llm won 66.0% of the 50 games it played during the tournament.
llm got a mean score of 67.10.
llm won 66.0% of the 50 games against basicmcts.

---- Ranking ---- (+/- are standard errors on the mean calculated using a Normal approximation) 
llm: Win rate 0.66 +/- 0.067	Mean Ordinal 1.34 +/- 0.07
basicmcts: Win rate 0.34 +/- 0.067	Mean Ordinal 1.66 +/- 0.07

----------------------------------------------------------------------------------
(needs more testing for a real performance results, but this is not the main idea of tests)

llm winrate on gemini-2.0-flash = 66% ± 13.4%
llm winrate on gemini-2.0-flash-lite = 60% ± 13.8%



GameResultsAndActionListener action results

need to add game releated information to be able to use this action list as a training data.
phase, chip context, heuristic score?, etc.

GameSeed,GameName,Round,Event,Tick,Turn,PlayerCount,GameID,ActionsReduced(Player),ActionsReduced(Action),ActionsReduced(ActionClass),ActionsReduced(ActionDescription),ActionsReduced(PlayerType),ActionsReduced(Size)
-1295863753,Poker,0,ACTION_CHOSEN,0,0,2,2,0,games.poker.actions.Call@ec7,Call,Call,Basic MCTS,7
-1295863753,Poker,0,ACTION_CHOSEN,1,1,2,2,1,games.poker.actions.AllIn@e36f4c0f,AllIn,All in,llm,4
-1295863753,Poker,1,ACTION_CHOSEN,2,0,2,2,1,games.poker.actions.Call@ec8,Call,Call,llm,7
-1295863753,Poker,1,ACTION_CHOSEN,3,1,2,2,1,games.poker.actions.Check@2550f74,Check,Check,llm,4
-1295863753,Poker,1,ACTION_CHOSEN,4,2,2,2,0,games.poker.actions.Fold@fffffada,Fold,Fold,Basic MCTS,4
-1295863753,Poker,2,ACTION_CHOSEN,5,0,2,2,0,games.poker.actions.Call@ec7,Call,Call,Basic MCTS,5
-1295863753,Poker,2,ACTION_CHOSEN,6,1,2,2,0,games.poker.actions.AllIn@e36f4c0e,AllIn,All in,Basic MCTS,4
-1295863753,Poker,3,ACTION_CHOSEN,7,0,2,2,1,games.poker.actions.Call@ec8,Call,Call,llm,7
-1295863753,Poker,3,ACTION_CHOSEN,8,1,2,2,1,games.poker.actions.Bet@f45,Bet,Bet 5,llm,4
-1295863753,Poker,3,ACTION_CHOSEN,9,2,2,2,0,games.poker.actions.AllIn@e36f4c0e,AllIn,All in,Basic MCTS,4

----------------------------------------------------------------------------------------

A 50 game of LLMActionPlayer vs MCTS (LLM model = gemini-2.0)

MCTS parameters:
{
	"class" : "players.mcts.MCTSParams",
	"K" : 1.0,
	"rolloutLength" : 100,
	"maxTreeDepth" : 30,
	"treePolicy" : "UCB",
	"opponentTreePolicy" : "OneTree",
	"selectionPolicy" : "SIMPLE",
	"information" : "Open_Loop",
	"rolloutType" : "RANDOM",
	"budgetType" : "BUDGET_TIME",
	"budget" : 40,
	"breakMS" : 0,
	"heuristic" : {
 		"class" : "players.heuristics.ScoreHeuristic"
	}
}

============= Poker - 100 games played ============= 
llm got 34.00 points. llm won 34.0% of the 100 games of the tournament. llm won 34.0% of the 100 games it played during the tournament.
llm got a mean score of 33.00.
llm won 34.0% of the 100 games against mcts.

mcts got 66.00 points. mcts won 66.0% of the 100 games of the tournament. mcts won 66.0% of the 100 games it played during the tournament.
mcts got a mean score of 67.00.
mcts won 66.0% of the 100 games against llm.

---- Ranking ---- (+/- are standard errors on the mean calculated using a Normal approximation) 
mcts: Win rate 0.66 +/- 0.047	Mean Ordinal 1.34 +/- 0.05
llm: Win rate 0.34 +/- 0.047	Mean Ordinal 1.66 +/- 0.05

----------------------------------------------------------------------------------------

