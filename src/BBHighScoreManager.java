import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/** A helper class that manages the highscore (including read, write, and updating)
 * @author AjwardSunYu
 */
public class BBHighScoreManager
{
	// The file name of the highscore
	private final static String			HIGH_SCORE_FILE_NAME	= "resources/highscores.txt";

	// Keep track of one instance of this object so that the most current highscores data will be provided
	private static BBHighScoreManager	singleton;

	// Stores the top highscores and names of the corresponding players
	private int[]						highScores;
	private String[]					highScoreNames;

	/** Gets the current instance of this object to provide the most current highscores data
	 * @return The current instance of this object
	 */
	public static BBHighScoreManager getSingleton()
	{
		if (singleton == null)
			singleton = new BBHighScoreManager();

		return singleton;
	}

	/** Constructor
	 */
	public BBHighScoreManager()
	{
		// Read the highscores data
		readHighScores();
	}

	/** Gets either 1) the rank that this score will be or 2) how many points to qualify for the highscore chart
	 * @param score The score to check
	 * @return The rank (1 is highest, 5 is lowest) if the score qualifies, the number of points needed to qualify otherwise
	 */
	public int rankOrPointsToQualify(int score)
	{
		// Return what rank this score will be if it is added to the highscores
		for (int i = 0; i < highScores.length; i++)
			if (score > highScores[i])
				return i + 1;
		// Does not qualify, so return how many points player needs to qualify
		return -(highScores[highScores.length - 1] - score - 1);
	}

	/** Gets the highest score in the highscores data
	 * @return The highest score. Note: it will return '0' even if there are no highscores data
	 */
	public int getHighScore()
	{
		return highScores[0];
	}

	/** Reads and parses the highscores file, and stores the highscores data
	 */
	public void readHighScores()
	{
		highScores = new int[5];
		highScoreNames = new String[5];

		try
		{
			BufferedReader inFile = new BufferedReader(new FileReader(HIGH_SCORE_FILE_NAME));

			int i = 0;
			String line = inFile.readLine();
			while (i < 5 && line != null)
			{
				highScoreNames[i] = line;
				highScores[i++] = Integer.parseInt(inFile.readLine());
				line = inFile.readLine();
			}

			inFile.close();
		} catch (IOException e)
		{
			System.out.println("error reading highscores");
		}
	}

	/** Writes the highscores data to a file on the disk
	 */
	public void writeHighScores()
	{
		if (highScores == null)
			return;

		try
		{
			BufferedWriter outFile = new BufferedWriter(new FileWriter(HIGH_SCORE_FILE_NAME));

			for (int i = 0; i < highScores.length; i++)
			{
				if (highScoreNames[i] != null)
					outFile.write(highScoreNames[i]);
				outFile.newLine();
				outFile.write(Integer.toString(highScores[i]));

				if (i != highScores.length - 1)
					outFile.newLine();
			}
			outFile.close();
		} catch (IOException e)
		{
			System.out.println("error saving highscores");
		}
	}

	/** Updates the highscores data with the given score and player's name
	 * @return The ranking of the player if they qualify (1 is highest, 5 is lowest), -1 otherwise
	 */
	public int updateHighScores(int score, String name)
	{
		if (highScores == null)
			readHighScores();

		if (score < highScores[highScores.length - 1])
			return -1;

		int i = 0;
		while (highScores[i] > score)
			i++;

		for (int swapIndex = highScores.length - 1; swapIndex > i; swapIndex--)
		{
			highScores[swapIndex] = highScores[swapIndex - 1];
			highScoreNames[swapIndex] = highScoreNames[swapIndex - 1];
		}

		highScores[i] = score;
		highScoreNames[i] = name;
		writeHighScores();

		return i + 1;
	}

	/** Gets the int array of highscores
	 * @return An array of highscores data
	 */
	public int[] getHighScores()
	{
		return highScores;
	}

	/** Gets the String array of corresponding names of the players
	 * @return An array of corresponding names of the players
	 */
	public String[] getHighScoreNames()
	{
		return highScoreNames;
	}
}
