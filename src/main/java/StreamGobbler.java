import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

/**
 * This is for external program output (e.g. cmd.exe or bash) - https://stackoverflow.com/a/33386692
 */
class StreamGobbler implements Runnable {
	private InputStream inputStream;
	private Consumer<String> consumeInputLine;
	
	public StreamGobbler(InputStream inputStream, Consumer<String> consumeInputLine) {
		this.inputStream = inputStream;
		this.consumeInputLine = consumeInputLine;
	}
	
	public void run() {
		new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumeInputLine);
	}
}
