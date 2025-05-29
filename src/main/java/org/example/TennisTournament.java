package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TennisTournament {
    private static final Random random = new Random();
    private static final int NUM_PLAYERS = 16;

    record MatchResult(int winner, List<Integer> setWinners) {}

    static class MatchTask implements Callable<MatchResult> {
        private final int player1;
        private final int player2;

        MatchTask(int player1, int player2) {
            this.player1 = player1;
            this.player2 = player2;
        }

        @Override
        public MatchResult call() throws Exception {
            // Simulate match duration between 1.5 and 2 seconds
            Thread.sleep(1500 + random.nextInt(501));

            List<Integer> setWinners = new ArrayList<>();
            int player1Sets = 0;
            int player2Sets = 0;

            // Play up to 3 sets
            while (player1Sets < 2 && player2Sets < 2) {
                // Random score for each player (higher score wins the set)
                int player1Score = random.nextInt(100);
                int player2Score = random.nextInt(100);
                if (player1Score > player2Score) {
                    setWinners.add(player1);
                    player1Sets++;
                } else {
                    setWinners.add(player2);
                    player2Sets++;
                }
            }

            // Determine match winner
            int winner = player1Sets == 2 ? player1 : player2;
            return new MatchResult(winner, setWinners);
        }
    }

    public static void main(String[] args) throws Exception {
        // Initialize players (1 to 16)
        List<Integer> players = new ArrayList<>();
        for (int i = 1; i <= NUM_PLAYERS; i++) {
            players.add(i);
        }

        // Create thread pool for concurrent matches
        ExecutorService executor = Executors.newFixedThreadPool(8);
        String[] roundNames = {"OCTAVOS DE FINAL", "CUARTOS DE FINAL", "SEMIFINAL", "FINAL"};
        int roundIndex = 0;

        // Process each round
        while (players.size() > 1) {
            System.out.println("\n===== " + roundNames[roundIndex] + " =====");
            List<Integer> nextRoundPlayers = new ArrayList<>();
            List<Future<MatchResult>> futures = new ArrayList<>();

            // Create match tasks for the current round
            for (int i = 0; i < players.size(); i += 2) {
                int player1 = players.get(i);
                int player2 = players.get(players.size() - 1 - i); // Pair 1 vs 16, 2 vs 15, etc.
                futures.add(executor.submit(new MatchTask(player1, player2)));
            }

            // Process match results
            for (int i = 0; i < futures.size(); i++) {
                MatchResult result = futures.get(i).get();
                int player1 = players.get(i * 2);
                int player2 = players.get(players.size() - 1 - i * 2);

                // Print match details
                System.out.println("\nJugador " + player1 + " vs Jugador " + player2);
                for (int j = 0; j < result.setWinners().size(); j++) {
                    System.out.println("Set " + (j + 1) + ": Jugador " + result.setWinners().get(j));
                }
                System.out.println("Ganador del partido: Jugador " + result.winner());

                // Add winner to next round
                nextRoundPlayers.add(result.winner());
            }

            // Update players for the next round
            players = nextRoundPlayers;
            roundIndex++;
        }

        // Print tournament champion
        System.out.println("\n🏆 ¡Campeón del torneo: Jugador " + players.get(0) + "!");

        // Shutdown executor
        executor.shutdown();
    }
}