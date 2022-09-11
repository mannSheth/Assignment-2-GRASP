import java.awt.desktop.SystemEventListener;
import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Main {

    public static HashMap<String, Integer> canditates = new HashMap<>();

    public static void main(String[] args) {
        int[][] pLatin;
        pLatin = readPartialLatinSquare(new File(args[0]));
        printSquares(pLatin);
        System.out.println("##########################################");
        System.out.println("##########################################");
        grasp(0.3f, 20000, pLatin);
    }

    public static void grasp(float startingAlpha, int maxIterations, int[][] partial)
    {
        long startTime = System.nanoTime();
        long endTime = -1;
        long duration;
        int[][] originalPartial = copy2d(partial);
        int[][] bestLatin = partial;
        int bestScore  = scoreWhole(partial);
        int newScore;
        int finalIteration = -1;
        float alphaForSolution = -1;
        for(int i  = 0; i < maxIterations; i++)
        {
            int[][] newSol = greedyRandom(startingAlpha, originalPartial);
            newSol = repairSquare(newSol);
            newSol = localSearch(newSol);
            newScore = scoreWhole(newSol);
            if( newScore < bestScore)
            {
                bestLatin = copy2d(newSol);
                bestScore = newScore;
                if(bestScore == 0)
                {
                    finalIteration = i + 1;
                    endTime = System.nanoTime();
                    break;
                }
                finalIteration = i + 1;
                endTime = System.nanoTime();
            }
        }
        if(endTime == -1)
        {
            endTime = System.nanoTime();
        }
        duration = endTime - startTime;
        long totalduration = System.nanoTime() - startTime;
        System.out.println("Iterations: " + finalIteration);
        System.out.println("Duration to best score: " + duration/1_000_000_000.0 + "s");
        if(bestScore != 0)
        {
            System.out.println("Total Time for all the Iterations (20,000): " + totalduration/1_000_000_000.0 + "s");
        }
        System.out.println("Score: " + bestScore);
        printSquares(bestLatin);
        checkLatinSquare(bestLatin);
    }

    private static int scoreWhole(int[][] solution)
    {
        int[][] square = copy2d(solution);
        int finalScore = 0;
        for (int col = 0; col < solution.length; col++) {
            for (int row = 0; row < solution.length; row++) {
                float holder = findCost(row, col, square[row][col], square);
                finalScore += holder;
            }
        }
        return finalScore;
    }
    public static int score(int[][] latin)
    {
        int missing = 0;
        for(int col = 0; col < latin.length; col++)
        {
            for(int row = 0; row < latin.length; row++)
            {
                if(latin[row][col] == 0)
                {
                    missing++;
                }
            }
        }
        return missing;
    }

    public static int[][] greedyRandom(float alpha, int[][] initial)
    {
        int[][] solution = copy2d(initial);
        Random rand = new Random();
        generateCandidates(solution);
        while(!canditates.isEmpty())
        {
            List<String> restrictedList = rcl(alpha);
            String chosenCandidate = restrictedList.get(rand.nextInt(restrictedList.size()));
            String[] candidateInfo = chosenCandidate.split(",");
            int row = Integer.parseInt(candidateInfo[0]);
            int col = Integer.parseInt(candidateInfo[1]);
            int val = Integer.parseInt(candidateInfo[2]);

            solution[row][col] = val;
            canditates.clear();
            generateCandidates(solution);
        }
        return solution;
    }

    public static void generateCandidates(int[][] start)
    {
        for(int col = 0; col < start.length; col++)
        {
            for (int row = 0; row < start.length; row++)
            {
                if(start[row][col] == 0)
                {
                    for (int i = 1; i <= start.length; i++) {
                        String key = row + "," + col + "," + i;
                        int value = findCost(row, col, i, start);
                        canditates.put(key, value);
                    }
                }
            }
        }
    }

    public static int[][] swap(int row, int col, int toRow, int toCol, int[][] toSwap)
    {
        int[][] newSquare = copy2d(toSwap);
        int holder = newSquare[toRow][toCol];
        newSquare[toRow][toCol] = newSquare[row][col];
        newSquare[row][col] = holder;
        return newSquare;
    }

    public static List<String> rcl (float alpha)
    {
        List<String> restrictedList = new LinkedList<>();
        int minCost = Collections.min(canditates.values());
        int maxCost = Collections.max(canditates.values());

        float restriction = minCost + (alpha * (maxCost - minCost));
        for (Map.Entry<String, Integer> entry : canditates.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            if((float)value <= restriction)
            {
                restrictedList.add(key);
            }
        }
        return restrictedList;
    }
    public static int findCost(int row, int col, int val, int[][] ref)
    {
        int cost = 0;
        for (int i = 0; i < ref.length; i++) {

            if(ref[row][i] == val || ref[row][i] == -val )
            {
                if(i != col)
                {
                    cost++;
                }
            }
            if(ref[i][col] == val || ref[i][col] == -val)
            {
                if(i != row)
                {
                    cost++;
                }
            }
        }
        return cost;
    }

    public static int[][] repairSquare(int[][]square)
    {
        int[][] toRepair = copy2d(square);
        HashMap<Integer, Integer> counts = new HashMap<>();
        for (int i = 0; i < square.length; i++) {
            counts.put(i+1, 0);
        }
        for (int col = 0; col < toRepair.length; col++)
        {
            for (int row = 0; row < toRepair.length; row++)
            {
                int value = toRepair[row][col];
                if(value < 0)
                {
                    value = -value;
                }
                counts.merge(value, 1, Integer::sum);
            }
        }
        while(!counts.values().stream().allMatch(x -> x == toRepair.length))
        {
            int highestOccurrence = Collections.max(counts.entrySet(), Map.Entry.comparingByValue()).getKey();
            int lowestOccurrence = Collections.min(counts.entrySet(), Map.Entry.comparingByValue()).getKey();
            boolean done = false;
            for (int col = 0; col < toRepair.length; col++)
            {
                if(done)
                    break;
                for (int row = 0; row < toRepair.length; row++)
                {
                    if(toRepair[col][row] == highestOccurrence)
                    {
                        toRepair[col][row] = lowestOccurrence;
                        counts.put(highestOccurrence, counts.get(highestOccurrence) - 1);
                        counts.put(lowestOccurrence, counts.get(lowestOccurrence) + 1);
                        done = true;
                        break;
                    }
                }
            }
        }
        return toRepair;
    }

    public static int[][] localSearch(int[][] square)
    {
        int[][] initial  = copy2d(square);
        String[] collisions = findCollisions(square);
        int bestScore;
        for(String collision : collisions)
        {
            String[] key = collision.split(",");
            int r = Integer.parseInt(key[0]);
            int c = Integer.parseInt(key[1]);
            int value = Integer.parseInt(key[2]);
            boolean done = false;
            bestScore = findCost(r, c, value, initial);

            for (int col = 0; col < square.length; col++)
            {
                for(int row = 0; row < square.length; row++)
                {
                    if(row != r && col != c && square[row][col] > 0)
                    {
                        int[][] holder = swap(r, c, row, col, initial);
                        int score = findCost(row, col, value, holder);
                        if (score < bestScore) {
                            bestScore = score;
                            initial  = copy2d(holder);
                        }
                    }
                }
            }
        }
        return initial;
    }

    public static String[] findCollisions(int[][] square)
    {
        HashSet<String> collisions = new HashSet<>();

        for (int col = 0; col < square.length; col++) {
            for (int row = 0; row < square.length; row++) {
                if(!(square[row][col] < 0))
                {
                    if (findCost(row, col, square[row][col], square) > 1)
                    {
                        collisions.add(row +"," + col + "," + square[row][col]);
                    }
                }

            }
        }
        return collisions.toArray(new String[collisions.size()]);
    }

    public static void printSquares(int[][] latin)
    {
        Arrays.stream(latin).forEach((row) -> {
            System.out.print("[");
            Arrays.stream(row).forEach((el) ->{
                if(el < 0)
                {
                    el *= -1;
                    if(el < 10)
                    {
                        System.out.print(" 0" + el + " ");
                    }
                    else
                    {
                        System.out.print(" " + el + " ");
                    }
                }
                else
                {
                    if(el < 10)
                    {
                        System.out.print(" 0" + el + " ");
                    }
                    else
                    {
                        System.out.print(" " + el + " ");
                    }
                }
            } );
            System.out.println("]");
        });
    }

    public static int[][] readPartialLatinSquare(File file)
    {
        int[][] pLatin = null;
        try
        {
            Scanner mScanner = new Scanner(file);
            int sizeOfGrid = Integer.parseInt(mScanner.nextLine());
            pLatin = new int[sizeOfGrid][sizeOfGrid];
            for(int row = 0; row < sizeOfGrid; row++)
            {
                String[] values = mScanner.nextLine().split(" ");
                for(int col = 0; col < sizeOfGrid; col++ )
                {
                    if(values[col].equals("_"))
                    {
                        pLatin[row][col] = 0;
                    }
                    else
                    {
                        pLatin[row][col] = -Integer.parseInt(values[col]);
                    }
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return pLatin;
    }

    public static int[][] copy2d(int[][] matrix) {
        int [][] myInt = new int[matrix.length][];
        for(int i = 0; i < matrix.length; i++)
            myInt[i] = matrix[i].clone();
        return myInt;
    }

    static void checkLatinSquare(int mat[][])
    {

        int N = mat.length;


        HashSet<Integer>[] rows = new HashSet[N];


        HashSet<Integer>[] cols = new HashSet[N];

        for(int i = 0; i < N; i++)
        {
            rows[i] = new HashSet<Integer>();
            cols[i] = new HashSet<Integer>();
        }


        int invalid = 0;

        for(int i = 0; i < N; i++)
        {
            for(int j = 0; j < N; j++)
            {
                int value = mat[i][j];
                if(value < 0)
                {
                    rows[i].add(-mat[i][j]);
                    cols[j].add(-mat[i][j]);
                }
                else
                {
                    rows[i].add(mat[i][j]);
                    cols[j].add(mat[i][j]);
                }


                if (mat[i][j] > N)
                {
                    invalid++;
                }
            }
        }


        int numrows = 0;

        int numcols = 0;

        for(int i = 0; i < N; i++)
        {
            if (rows[i].size() != N)
            {
                numrows++;
            }
            if (cols[i].size() != N)
            {
                numcols++;
            }
        }

        if (numcols == 0 &&
                numrows == 0 && invalid == 0)
            System.out.print("Valid" + "\n");
        else
            System.out.print("Not Valid" + "\n");

    }
}
