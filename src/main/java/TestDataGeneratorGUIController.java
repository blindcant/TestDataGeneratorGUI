import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Created by blindcant on 7/07/17.
 * https://docs.oracle.com/javase/8/javafx/api/javafx/fxml/doc-files/introduction_to_fxml.html#controllers
 *
 * While it can be convenient to write simple event handlers in script, either inline or defined in external files,
 * it is often preferable to define more complex application logic in a compiled, strongly-typed language such as Java.
 * As discussed earlier, the fx:controller attribute allows a caller to associate a "controller" class with an FXML document.
 * A controller is a compiled class that implements the "code behind" the object hierarchy defined by the document.
 */
public class TestDataGeneratorGUIController implements Initializable
{
	// @@@ INSTANCE VARIABLES @@@
	// ### System Properties ### https://docs.oracle.com/javase/tutorial/essential/environment/sysprop.html
	public static final String USER_HOME_PATH = System.getProperty("user.home");
	public static final String OS_SEPERATOR = System.getProperty("file.separator");
	public static final String OS_NEWLINE = System.getProperty("line.separator");
	public static final String SYSTEM_ARCHITECTURE = System.getProperty("os.arch");
	public static final String SYSTEM_OS_NAME = System.getProperty("os.name");
	public static final String SYSTEM_OS_VERSION = System.getProperty("os.version");
	public static final String PROGRAM_NAME = "Test Data Generator & Exporter";
	public static final String PROGRAM_VERSION = "1.8";
	
	// ### File I/O ###
	DirectoryChooser directoryChooser = new DirectoryChooser();
	FileChooser fileChooser = new FileChooser();

	// ### User Provided Parameters ###
	private File absolutePathOutput;
	private int amountToGenerate;
	private boolean includeHeaderRowInOutput;
	private boolean wrapOutputWithDoubleQuotes;
	private String outputFileDelimiter;
	private String outputFileCharacterEncoding;
	
	// ### Interface Controls ###
	// *** menu-bar ***
	@FXML private MenuItem menuItemBrowse;
	@FXML private MenuItem menuItemRun;
	@FXML private MenuItem menuItemQuit;
	
	@FXML private MenuItem menuItemHelp;
	@FXML private MenuItem menuItemAbout;
	@FXML private MenuItem menuItemLicence;
	
	// *** buttons ***
	@FXML private Button buttonBrowse;
	@FXML private Button buttonRun;
	@FXML private Button buttonQuit;
	
	// *** textfields ***
	@FXML private TextField textFieldOutputPath;
	@FXML private TextField textFieldAmountToCreate;
	@FXML private TextField textFieldRenameOutputFileName;
	
	// *** checkboxes ***
	@FXML private CheckBox checkBoxOutputHeaderRow;
	@FXML private CheckBox checkBoxRenameOutputFileName;
	@FXML private CheckBox checkBoxOutputDoubleQuotes;
	
	// *** combo box ***
	@FXML private ComboBox comboBoxFileDelimiters;
	@FXML private ComboBox comboBoxOutputFileCharacterEncoding;
	
	// *** progress bar ***
	@FXML private ProgressBar progressBar;
	
	// *** textarea ***
	@FXML private TextArea textAreaProgramOutput;
	//Console Class stream redirection
	Console console;
	PrintStream printStream;
	PrintWriter printWriter;
	
	// @@@ MAIN METHOD @@@
	
	// @@@ CONSTRUCTOR(S) @@@
	
	// @@@ METHODS @@@
	// ### Initialise GUI ###
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		//Auto scroll TextArea Pane Listener- https://stackoverflow.com/a/20568196
		textAreaProgramOutput.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
			{
				//Scroll to the bottom of the page
				textAreaProgramOutput.setScrollTop(Double.MAX_VALUE);
				//Scroll to the top of the page
				//textArea_consoleOutput.setScrollTop(Double.MIN_VALUE);
			}
		});

		//Hide the progress bar
		progressBar.setVisible(false);
		
		//Add items to the combo box
		comboBoxFileDelimiters.getItems().addAll("Comma", "Pipe", "Semi-Colon", "Space", "Tab");
		comboBoxOutputFileCharacterEncoding.getItems().addAll("ASCII", "Latin", "UTF-8", "UTF-16BE", "UTF-16LE");

		//Event handler listening
		//menu
		menuItemAbout.setOnAction(new javafx.event.EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				Alert alertAbout = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.CLOSE);
				alertAbout.setTitle(PROGRAM_NAME + " v" + PROGRAM_VERSION);
				alertAbout.setHeaderText("About Information");
				alertAbout.setContentText("O/S:\t" + SYSTEM_OS_NAME + ", " + SYSTEM_ARCHITECTURE + ", " + SYSTEM_OS_VERSION + "." + OS_NEWLINE
						+ "Program Name: " + PROGRAM_NAME + OS_NEWLINE
						+ "Program Version: " + PROGRAM_VERSION + OS_NEWLINE
						+ "Program Author: Dallas Hall (hmd911)");
				alertAbout.showAndWait();
			}
		});

		menuItemLicence.setOnAction(new javafx.event.EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				Alert aboutAlert = new Alert(Alert.AlertType.INFORMATION, "Licence Information.", ButtonType.CLOSE);
				aboutAlert.setTitle("Licence for " + PROGRAM_NAME);
				aboutAlert.setHeaderText("Test Data Generator Licence.");
				aboutAlert.setContentText("Test Data Generator - creates a variety of random data in a variety of formats that can be used in program testing, such as database loading.\n" +
						"Copyright (C) Dallas Hall, 2017.\n" +
						"\n" +
						"This program is free software: you can redistribute it and/or modify" +
						"it under the terms of the GNU General Public License as published by" +
						"the Free Software Foundation, either version 3 of the License, or" +
						" (at your option) any later version.\n" +
						"\n" +
						"This program is distributed in the hope that it will be useful, " +
						"but WITHOUT ANY WARRANTY; without even the implied warranty of" +
						"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the " +
						"GNU General Public License for more details.\n" +
						"\n" +
						"You should have received a copy of the GNU General Public License" +
						"along with this program.  If not, see <http://www.gnu.org/licenses/>.");
				aboutAlert.showAndWait();
			}
		});

		menuItemHelp.setOnAction(new javafx.event.EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				Alert helpAlert = new Alert(Alert.AlertType.INFORMATION, "Help Information.", ButtonType.CLOSE);
				helpAlert.setTitle("Help for " + PROGRAM_NAME);
				helpAlert.setHeaderText("Miscellaneous Help Information.");
				helpAlert.setContentText("1) Read the README.txt file"
						+ OS_NEWLINE + "2) Mouse over things for tool tips for additional hints."
						+ OS_NEWLINE + "3) Follow the information in the pop-up windows.");

				helpAlert.show();
			}
		});

		//GUI
		//Console output via Console Class - https://stackoverflow.com/a/9494522
		console = new Console(textAreaProgramOutput);
		printStream =  new PrintStream(console, true);
		System.setOut(printStream);
		System.setErr(printStream);

		//user defined table check box event listener - http://docs.oracle.com/javafx/2/ui_controls/checkbox.htm
		checkBoxRenameOutputFileName.selectedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				if (checkBoxRenameOutputFileName.isSelected())
				{
					textFieldRenameOutputFileName.setEditable(true);
					textFieldRenameOutputFileName.setOpacity(1.0);
				} else
				{
					textFieldRenameOutputFileName.setEditable(false);
					textFieldRenameOutputFileName.setOpacity(0.5);
				}
			}
		});

		//Console output via StreamGobbler Class for external programs : https://stackoverflow.com/a/33386692
	}
	
	// ### Run The Application ###
	
	// ### Runtime Specific Methods ###
	private boolean validateUserInput()
	{
		//check that a folder was chosen
		if (absolutePathOutput == null)
		{
			//display popup
			//System.out.println("ERRRRAR LULS - no fucking files");
			showValidationErrorMessage("output");
			return false;
		}
		//check that only numbers were entered
		else if(textFieldAmountToCreate == null || textFieldAmountToCreate.getText().equals("") || !textFieldAmountToCreate.getText().matches("^[0-9]+$") || Integer.parseInt(textFieldAmountToCreate.getText()) <= 0)
		{
			//display popup
			//System.out.println("ERRRRAR LULS - stupid user input in the textfield");
			showValidationErrorMessage("illegal amount");
			return false;
		}
		//check that the number entered is not greater than 500k
		else if(Integer.parseInt(textFieldAmountToCreate.getText()) > 500000)
		{
			//display popup
			//System.out.println("ERRRRAR LULS - stupid user input in the textfield");
			showValidationErrorMessage("high amount");
			return false;
		}
		//combo boxes not picked
		else if(comboBoxFileDelimiters.getSelectionModel().isEmpty())
		{
			//display popup
			//System.out.println("ERRRRAR LULS - stupid user input in the combobox");
			showValidationErrorMessage("delimiter");
			return false;
		}
		else if (comboBoxOutputFileCharacterEncoding.getSelectionModel().isEmpty())
		{
			//display popup
			//System.out.println("ERRRRAR LULS - stupid user input in the combobox");
			showValidationErrorMessage("encoding");
			return false;
		}

		//validation checking passed
		else
		{
			amountToGenerate = Integer.parseInt(textFieldAmountToCreate.getText());
			includeHeaderRowInOutput = checkBoxOutputHeaderRow.isSelected();
			wrapOutputWithDoubleQuotes = checkBoxOutputDoubleQuotes.isSelected();
			outputFileCharacterEncoding = comboBoxOutputFileCharacterEncoding.getValue().toString();
			
			String comboValueCheck = comboBoxFileDelimiters.getValue().toString();
			if (comboValueCheck.equals("Tab"))
			{
				outputFileDelimiter = "\t";
			}
			else if (comboValueCheck.equals("Comma"))
			{
				outputFileDelimiter = ",";
			}
			else if (comboValueCheck.equals("Semi-Colon"))
			{
				outputFileDelimiter = ";";
			}
			else if (comboValueCheck.equals("Pipe"))
			{
				outputFileDelimiter = "|";
			}
			else
			{
				outputFileDelimiter = " ";
			}

			comboValueCheck = comboBoxOutputFileCharacterEncoding.getValue().toString();
			if (comboValueCheck.equals("ASCII"))
			{
				outputFileCharacterEncoding = "ASCII";
			}
			else if (comboValueCheck.equals("Latin"))
			{
				outputFileCharacterEncoding = "Latin";
			}
			else if (comboValueCheck.equals("UTF-8"))
			{
				outputFileCharacterEncoding = "UTF-8";
			}
			else if (comboValueCheck.equals("UTF-16BE"))
			{
				if (showUTF16BEWarning())
				{
					outputFileCharacterEncoding = "UTF-16BE";
				}
				else
				{
					System.out.println("Please choose a different file encoding and try again.\n");
					return false;
				}
			}
			else if (comboValueCheck.equals("UTF-16LE"))
			{
				outputFileCharacterEncoding = "UTF-16LE";
			}

			System.out.println("@@@ Program Parameters @@@");
			System.out.println();
			System.out.println("Saving to " + absolutePathOutput);
			System.out.println("Include header? " + includeHeaderRowInOutput);
			System.out.println("Wrap \"output\"? " + wrapOutputWithDoubleQuotes);
			System.out.println("Generating " + amountToGenerate);
			System.out.println("Delimiter is '" + outputFileDelimiter + "'");
			System.out.println("File encoding is: " + outputFileCharacterEncoding);
			System.out.println();
			return true;
		}
	}
	
	// ### All GUI Items ###
	// *** Menubar items ***
	// --- File ---
	
	// --- About ---
	@FXML
	private void clickMenuItemHelp()
	{
	
	}
	
	@FXML
	private void clickMenuItemAbout()
	{
	
	}
	
	@FXML
	private void clickMenuItemLicence()
	{
	
	}
	
	// *** GUI items ***
	
	// *** Shared between menu and GUI ***
	@FXML
	private void browseFileSystem()
	{
		//https://docs.oracle.com/javafx/2/ui_controls/file-chooser.htm
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Choose Save Location");
		directoryChooser.setInitialDirectory(new File(USER_HOME_PATH));
		absolutePathOutput = directoryChooser.showDialog(new Stage());
		if (absolutePathOutput != null)
		{
			textFieldOutputPath.setText(absolutePathOutput.getAbsolutePath());
		}
		//This resets the textField when the user cancels - https://stackoverflow.com/a/28268821
		else
		{
			textFieldOutputPath.setText(null);
		}
	}
	
	@FXML
	private void tryRuntime()
	{
		boolean readyToRun = validateUserInput();
		if (readyToRun)
		{
			//System.out.println("YEAH BOI LETS RUNN DIS SHIZ");
			//create a JavaFX Task class, which can be used to coordinate with the GUI progess bar -
			//This task is going to use an object and return that object so we can use/manipulate its state
			Task<TestDataGenerator> taskGenerateTestData = new Task<TestDataGenerator>()
			{
				@Override public TestDataGenerator call()
				{
					TestDataGenerator testDataGenerator = new TestDataGenerator(absolutePathOutput, amountToGenerate, includeHeaderRowInOutput, wrapOutputWithDoubleQuotes, outputFileDelimiter, outputFileCharacterEncoding, textFieldRenameOutputFileName.getText());
					return testDataGenerator;
				}
			};
			//Update the progressBar based on the status of the task : https://stackoverflow.com/a/32773187
			taskGenerateTestData.setOnRunning(e ->
			                         {
				                         progressBar.setVisible(taskGenerateTestData.isRunning());
			                         });
			taskGenerateTestData.setOnFailed(e ->
			                        {
				                        progressBar.setVisible(taskGenerateTestData.isRunning());
			                        });
			taskGenerateTestData.setOnSucceeded(e ->
			                           {
				                           progressBar.setVisible(taskGenerateTestData.isRunning());
			                           });
			//Run the Task inside a new thread
			new Thread(taskGenerateTestData).start();
			//Below code from : https://stackoverflow.com/a/25224921
/*			Thread thread = new Thread(taskGenerateTestData);
			thread.setDaemon(true);
			thread.start();*/
		}
	}
	
	@FXML
	private void quitProgram()
	{
		Alert alertQuit = new Alert(Alert.AlertType.CONFIRMATION, "");
		alertQuit.setTitle(PROGRAM_NAME + " v" + PROGRAM_VERSION);
		alertQuit.setHeaderText("Quit.");
		alertQuit.setContentText("Are you sure you want to quit?");
		//https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/Alert.html
		Optional<ButtonType> clickResult = alertQuit.showAndWait();
		if (clickResult.isPresent() && clickResult.get() == ButtonType.OK) {
			Platform.exit();
		}
	}
	
	
	// ### Pop ups ### https://stackoverflow.com/a/28887273
	// *** Validation Errors ***
	private void showValidationErrorMessage(String errorFlag)
	{
		if (errorFlag.equals("output"))
		{
			Alert alert =  new Alert(Alert.AlertType.ERROR, "An output directory hasn't been selected.\nClick \"Browse\" and choose one.");
			alert.setTitle(PROGRAM_NAME + " v" + PROGRAM_VERSION);
			alert.setHeaderText("Output File Location Error.");
			alert.showAndWait();
		}
		else if (errorFlag.equals("illegal amount"))
		{
			Alert alert =  new Alert(Alert.AlertType.ERROR, "The amount entered is incorrect.\nEnter positive whole numbers only.");
			alert.setTitle(PROGRAM_NAME + " v" + PROGRAM_VERSION);
			alert.setHeaderText("Test Data Creation Amount Error.");
			alert.showAndWait();
		}
		else if (errorFlag.equals("high amount"))
		{
			Alert alert =  new Alert(Alert.AlertType.ERROR, "The amount entered is greater than 500,000.\nThe VDI can't handle this much data.\nIf you need larger numbers, generate it multiple times.");
			alert.setTitle(PROGRAM_NAME + " v" + PROGRAM_VERSION);
			alert.setHeaderText("Test Data Creation Amount Error.");
			alert.showAndWait();
		}
		else if (errorFlag.equals("delimiter"))
		{
			Alert alert =  new Alert(Alert.AlertType.ERROR, "A column delimiter hasn't been selected.\nClick \"File Delimiter\" & choose one.");
			alert.setTitle(PROGRAM_NAME + " v" + PROGRAM_VERSION);
			alert.setHeaderText("Output File Delimiter Error.");
			alert.showAndWait();
		}
		else if (errorFlag.equals("encoding"))
		{
			Alert alert =  new Alert(Alert.AlertType.ERROR, "The output file character set encoding hasn't been selected.\nClick \"File Charset Encoding\" & choose one.");
			alert.setTitle(PROGRAM_NAME + " v" + PROGRAM_VERSION);
			alert.setHeaderText("Output File Encoding Error.");
			alert.showAndWait();
		}
	}

	//warnings
	@FXML
	private boolean showUTF16BEWarning()
	{
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "");
		alert.setTitle(PROGRAM_NAME + " v" + PROGRAM_VERSION);
		alert.setHeaderText("UTF-16 BIG ENDIAN");
		alert.setContentText("UTF-16 BE will add 2 bytes (0xFE & 0xFF) to the start of your file.  These are necessary for the file to be read BUT it can cause unintended results when processing the data later on x86 based platforms.\nAre you sure you want to use UTF-16 BE encoding?");
		//https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/Alert.html
		Optional<ButtonType> clickResult = alert.showAndWait();
		if (clickResult.isPresent() && clickResult.get() == ButtonType.OK) {
			return true;
		}
		else
		{
			return false;
		}
	}

	// ### Helpers ###
	// *** Getters ***
	
	public File getAbsolutePathOutput()
	{
		return absolutePathOutput;
	}
	
	public int getAmountToGenerate()
	{
		return amountToGenerate;
	}
	
	public boolean isIncludeHeaderRowInOutput()
	{
		return includeHeaderRowInOutput;
	}
	
	public String getOutputFileDelimiter()
	{
		return outputFileDelimiter;
	}
}
