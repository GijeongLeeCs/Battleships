/**
 * This strategy selects the first available move at random.
 * It is easy to beat this strategy because it is totally random.
 *
 * @author Kartikey Bihani
 *
 */

package model;
import java.util.Random;

public class RandomAI implements BoardStrategy {

	private static Random generator;

	public RandomAI() {
		generator = new Random();
	}

	// Find an open spot while ignoring possible wins and stops
	public Point desiredMove(Board board) {
		boolean set = false;
		while (!set) {
			int row = generator.nextInt(10);
			int col = generator.nextInt(10);

			if (!board.used(row, col)) {
				set = true;
				return new Point(row, col);
			}

		}
		return null;
	}

}
