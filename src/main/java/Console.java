/**
 * https://stackoverflow.com/questions/13841884/redirecting-system-out-to-a-textarea-in-javafx
 */
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

public class Console extends OutputStream
{
	private TextArea output;
	
	public Console(TextArea textArea)
	{
		this.output = textArea;
	}

/*	@Override
	public void write(int i) throws IOException
	{
		output.appendText(String.valueOf((char) i));
	}*/
	
	public void write(final int i) throws IOException {
		Platform.runLater(new Runnable() {
			public void run() {
				output.appendText(String.valueOf((char) i));
			}
		});
	}
	
}
