# Project Notes — LLM Agent in TAG

> Meeting with James: **March 9**

---------------------------------------------------------

currently tested games with llmactionplayer

- dots and boxes : 
        - performed poorly, mainly can be because poor prompting of the gamestate
        - hard to compact the gamestate, (an ascii gamesatte?, better represantion but with text?)
- poker :
        - performs 60% ± 13.8% agains basic mcts model, with adding its small gamestate to prompt
        - takes arround 200-300 input tokens per promt (maybe can be optimized a bit more)

----------------------------------------------------------

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


----------------------------------------------------------

random idea: reprompt the llmactionplayer with the action returned from the query, but this time with game state + action id + possible actions = `this is the action you returned, do you confirm it that it will be a *good* move or do you want to change it?`

