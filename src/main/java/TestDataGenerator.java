import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by blindcant on 9/06/17.
 *
 * Fastload manual
 * http://www.info.teradata.com/browse.cfm > Teradata Utilities > Teradata Tools & Utilities Suite (version 14.10) > Fastload
 * Page 39 = fastload.exe parameters
 * Page 55 = sesssion charsets
 * Page 87 = script commands in full detail
 *
 * Terdata Parallel Transports details
 * https://developer.teradata.com/sites/all/files/documentation/linked_docs/2436020A_TPT-Reference-13.10.pdf
 * Page 73 = Unicode details
 *
 * Teradata tools session charsets details
 * http://www.info.teradata.com/htmlpubs/DB_TTU_14_00/index.html#page/Interface_Tools/B035_2425_071A/2425ch05.14.13.html
 *
 * Teradata tools BOM details
 * https://downloads.teradata.com/tools/articles/whats-a-bom-and-why-do-i-care
 * http://downloads.teradata.com/tools/articles/how-do-standalone-utilities-handle-byte-order-mark
 *
 * BOM in general
 * https://www.cs.umd.edu/class/sum2003/cmsc311/Notes/Data/endian.html
 *
 */
public class TestDataGenerator
{
	//# INSTANCE VARIABLES #
	// internal data storage
	private List<String[]> testDataStringArray = new ArrayList<>();
	private List<byte[]> testDataByteArray = new ArrayList<>();
	private List<String> femaleNames = new ArrayList<>();
	private List<String> maleNames = new ArrayList<>();
	private List<String> surnames = new ArrayList<>();
	private List<String[]> addresses = new ArrayList<>();
	private static final char[] FILE_DELIMITERS = {',', '|', ';', ' ', '\t'};
	private static final String[] FILE_HEADER = {"PERSON ID", "TITLE", "FIRST NAME", "MIDDLE NAME", "SURNAME", "SEX", "DOB", "HOME ADDRESS", "MAILING ADDRESS", "PERSONAL EMAIL", "WORK EMAIL", "HOME PHONE", "MOBILE PHONE", "BIRTH CERTIFICATE", "DRIVERS LICENCE", "PASSPORT", "CURRENCY SIGN", "CURRENCY CODE", "BANK BALANCE"};
	
	//File I/O
	//https://stackoverflow.com/a/10163028 - relative path to the class path
	private static final String ABSOLUTE_PATH = "input-files";
	private BufferedReader stdinFile = null;
	//private PrintWriter stdoutFile = null; //old java.io
	private BufferedWriter stdoutFile = null;
	private String delimter;
	
	//Character Sets
	private static final Charset ASCII = Charset.forName("US-ASCII");
	private static final Charset LATIN = Charset.forName("ISO-8859-1");
	private static final Charset UTF8 = Charset.forName("UTF-8");
	//https://docs.oracle.com/javase/8/docs/api/java/nio/charset/Charset.html
	//defaults to big endian without byte order marking, which then writes the BOM to the file which I think is necesasry to read it
	//because x86 is LE - https://stackoverflow.com/questions/1024951/does-my-amd-based-machine-use-little-endian-or-big-endian
	private static final Charset UTF16 = Charset.forName("UTF-16");
	private static final Charset UTF16BE = Charset.forName("UTF-16BE");
	private static final Charset UTF16LE = Charset.forName("UTF-16LE");
	
	//Test Data
	//names
	private String[] femaleTitles = {"MS", "MRS", "DANE", "HON", "DR", "PROF", "QC"};
	private String[] maleTitles = {"MR", "SIR", "LORD", "HON", "DR", "PROF", "QC"};
	//addresses
	private String[] unitTypes = {"Unit", "Apartment", "Flat", "Villa", "Suite", "Townhouse"};
	private String[] deliveryTypes = {"PO Box", "GPO Box", "Locked Bag", "Private Bag", "RMB", "CMB"};
	private String[] streetTypes = {"Drive", "Way", "Circuit", "Street", "Highway", "Road", "Boulevard", "Court", "Parade", "Avenue", "Lane"};
	private String[] stateCodes = {"QLD", "NSW", "ACT", "VIC", "SA", "WA", "NT", "TAS"};
	private String[] suburbPreFixes = {"Lower Mount", "Mount", "Upper Mount", "North", "South", "East", "West", "Port"};
	private String[] suburbPostfixes = {"Heights", "Plains", "Park", "Hill", "Ridge", "Beach", "Bay", "Point", "North", "South", "East", "West"};
	//emails
	private String[] personalEmailDomains = {"@yahoo.com.au", "@gmail.com", "@hotmail.com", "@msn.co.uk", "@duam.co.kr", "@some-isp.com.au"};
	private String[] workEmailDomains = {"@some-school.edu.au", "@some-company.com", "@some-department.gov.au", "@some-organisation.org"};
	//currency - dollar, pound, euro, japanese yen/ chinese yuan, korean won, russian ruble, indian rupee
	//the below doesn't work because of https://stackoverflow.com/a/22120295
	//private String[] currencySignsString = {"$", "£", "€", "¥", "₩", "₽", "₹"};
	//https://en.wikipedia.org/wiki/ISO/IEC_8859-1#Codepage_layout
	private static final char[] CURRENCY_SIGNS = {'\u0024', '\u00A3', '\u20AC', '\u00A5', '\u20A9', '\u20BD', '\u20B9'};
	private static final  String[] CURRENCY_CODES = {"AUD", "USD", "NZD", "GBP", "EUR", "JPY", "CNY", "KPW", "KRW", "RUB", "INR"};

	//Error Handling
	boolean noError = false;

	//# MAIN METHOD #
/*	public static void main(String[] args)
	{
		TestDataGenerator testRun = new TestDataGenerator();
	}*/
	
	//# CONSTRUCTOR(S) #
	public TestDataGenerator(File outputAbsolutePath, int amountToGenerate, boolean hasHeader, boolean useDoubleQuotes, String delimiter, String fileEncoding, String userFileName)
	{
		//grab and format current date
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat timeFormat = new SimpleDateFormat("HH-mm-ss");
		String currentDate = "_@date_" + dateFormat.format(new Date()) + "_@time_" + timeFormat.format(new Date());
		this.delimter = delimiter;
		
		System.out.println("@@@ Creating Name & Address Data @@@");
		System.out.println();
		readDataFromFile("yob2016-top_1000_male.txt", "male");
		readDataFromFile("yob2016-top_1000_female.txt", "female");
		readDataFromFile("top_1000_surnames_2010.txt", "surname");
		//readDataFromFile("openaddresses_10000_au.csv", "addresses");

		if (noError)
		{
			boolean isEvenNumber = amountToGenerate % 2 == 0;
			if (isEvenNumber)
			{
				int blockSize = calculateBlockSize2(amountToGenerate);
				generateTestDataBlocks(amountToGenerate / blockSize, blockSize, useDoubleQuotes, isEvenNumber);
			} else
			{
				int newAmountToGenerate = amountToGenerate - 1;
				int blockSize = calculateBlockSize2(newAmountToGenerate);

				generateTestDataBlocks(newAmountToGenerate / blockSize, blockSize, useDoubleQuotes, isEvenNumber);
				generateTestData(1, useDoubleQuotes);
			}
			//printTestData(10);
			if (null == userFileName || userFileName.equals(""))
			{
				writeTestDataToFile(outputAbsolutePath, TestDataGeneratorGUIController.OS_SEPERATOR, amountToGenerate + "_rows_of_random_data" + currentDate + ".txt", hasHeader, useDoubleQuotes, delimiter, fileEncoding);
			} else
			{
				writeTestDataToFile(outputAbsolutePath, TestDataGeneratorGUIController.OS_SEPERATOR, userFileName + ".txt", hasHeader, useDoubleQuotes, delimiter, fileEncoding);
			}
		
/*		//Use concurrency to run a Task on a new Thread - still really slow
		Runnable taskPrintData = () -> {
			//printData("male");
			//printData("female");
			//printData("surname");
			//printData("addresses");
			System.out.println("*** Printing 1 Sample Row ***");
			printTestData(1);
		};
		
		taskPrintData.run();
		Thread thread = new Thread(taskPrintData);
		thread.start();*/
			System.out.println("@@@ Printing 5 Sample Rows @@@");
			System.out.println();
			printHeader(useDoubleQuotes);
			printTestData(5);
		}
	}
	
	//# METHODS #
	private void readDataFromFile(String fileName, String dataFlag)
	{
		try
		{
			//record starting point of I/O
			Instant startTime = Instant.now();
			
			System.out.println("### Reading in " + fileName + " ###");
			System.out.println();
			//create BufferedReader object that wraps a FileReader object
			//stdinFile = new BufferedReader(new FileReader(ABSOLUTE_PATH + fileName));

			//https://stackoverflow.com/a/2417546 - use slash instead of file.seperator within Java
			//https://stackoverflow.com/a/38888561 - failing to find file inside executable
			InputStream jarResource = getClass().getResourceAsStream("/input-files/" + fileName);
			stdinFile = new BufferedReader(new InputStreamReader(jarResource));

			// store the current line here
			String currentLine;
			
			// Store the current line as a String into currentLine.  The readLine() method returns null when the end of line is reached.
			while ((currentLine = stdinFile.readLine()) != null)
			{
				if (dataFlag.toLowerCase().equals("male"))
				{
					maleNames.add(currentLine);
				}
				else if (dataFlag.toLowerCase().equals("female"))
				{
					femaleNames.add(currentLine);
				}
				else if (dataFlag.toLowerCase().equals("surname"))
				{
					surnames.add(currentLine);
				}
				else if (dataFlag.toLowerCase().equals("addresses"))
				{
					addresses.add(currentLine.split(","));
				}
				else
				{
					System.out.println("<ERROR>\nThe flag entered is incorrect.  It must be male, female, or surname.\n</EROROR>");
				}
			}
			// close the open resources
			stdinFile.close();
			
			//record finishing point of I/O
			Instant finishTime = Instant.now();
			
			//display runtime
			System.out.println("Done. Read time was: " + Duration.between(startTime, finishTime).toString().substring(2) + "\n");
			System.out.println();
			noError = true;
		} catch (IOException e)
		{
			System.out.println("<ERROR>\nCouldn't read from file.");
			e.printStackTrace();
			System.out.println("</ERROR>\n");
		}catch (Exception e)
		{
			System.out.println("<ERROR>\nCouldn't read from file.");
			e.printStackTrace();
			System.out.println("</ERROR>\n");
		}
	}
	
	private void writeTestDataToFile(File outputAbosolutePath, String osSeperator, String fileName, boolean includeHeader, boolean useDoubleQuotesOnHeader, String delimiter, String fileEncoding)
	{
		try
		{
			//record starting point of I/O
			Instant startTime = Instant.now();
			System.out.println("@@@ Writing out to " + fileName + " @@@");
			System.out.println();
			
			//create BufferedReader object that wraps a FileReader object - OLD JAVA IO
			//stdoutFile = new PrintWriter(new FileWriter(ABSOLUTE_PATH + fileName));
			if (fileEncoding.equals("ASCII"))
			{
				stdoutFile = Files.newBufferedWriter(Paths.get(outputAbosolutePath + "/" + fileName), ASCII);
			}
			else if (fileEncoding.equals("Latin"))
			{
				stdoutFile = Files.newBufferedWriter(Paths.get(outputAbosolutePath + "/" + fileName), LATIN);
			}
			else if (fileEncoding.equals("UTF-8"))
			{
				stdoutFile = Files.newBufferedWriter(Paths.get(outputAbosolutePath + "/" + fileName), UTF8);
			}
			else if (fileEncoding.equals("UTF-16BE"))
			{
				stdoutFile = Files.newBufferedWriter(Paths.get(outputAbosolutePath + "/" + fileName), UTF16);
			}
			else if (fileEncoding.equals("UTF-16LE"))
			{
				stdoutFile = Files.newBufferedWriter(Paths.get(outputAbosolutePath + "/" + fileName), UTF16LE);
			}
			
			//write header if necessary, with or without quotes
			if (includeHeader && useDoubleQuotesOnHeader)
			{
				for (int i = 0; i < FILE_HEADER.length; i++)
				{
					if (i != (FILE_HEADER.length - 1))
					{
						//stdoutFile.print(fileHeader[i] + "\t");
						stdoutFile.write( "\"" + FILE_HEADER[i] + "\"" + delimiter);
					}
					else
					{
						//stdoutFile.println(fileHeader[i]);
						stdoutFile.write("\"" + FILE_HEADER[i]  + "\"");
						stdoutFile.newLine();
					}
				}
			}
			else if (includeHeader && !useDoubleQuotesOnHeader)
			{
				for (int i = 0; i < FILE_HEADER.length; i++)
				{
					if (i != (FILE_HEADER.length - 1))
					{
						//stdoutFile.print(fileHeader[i] + "\t");
						stdoutFile.write( FILE_HEADER[i] + delimiter);
					}
					else
					{
						//stdoutFile.println(fileHeader[i]);
						stdoutFile.write(FILE_HEADER[i]);
						stdoutFile.newLine();
					}
				}
			}
			
			//read data from ArrayList and store to a string
			String currentLine;
			for (String[] tmpString : testDataStringArray)
			{
				for (int i = 0; i < tmpString.length; i++)
				{
					if (i != (tmpString.length - 1))
					{
						//stdoutFile.print(tmpString[i] + "\t");
						stdoutFile.write(tmpString[i] + delimiter);
					}
					else
					{
						//stdoutFile.println(tmpString[i]);
						stdoutFile.write(tmpString[i] );
						stdoutFile.newLine();
					}
				}
			}
			
			// close the open resources
			stdoutFile.close();
			
			//record finishing point of I/O
			Instant finishTime = Instant.now();
			
			//display runtime
			System.out.println("Done. Write time was: " + Duration.between(startTime, finishTime).toString().substring(2) + "\n");
			System.out.println();
		} catch (IOException e)
		{
			System.out.println("<ERROR>\nCouldn't write to file.");
			e.printStackTrace();
			System.out.println("</ERROR>\n");
		}
		catch (Exception e)
		{
			System.out.println("<ERROR>\nCouldn't write to file.");
			e.printStackTrace();
			System.out.println("</ERROR>\n");
		}
	}
	
	private void printData(String dataFlag)
	{
		System.out.println("@@@ Printing the top 1000 U.S. census names for " + dataFlag + "s @@@");
		System.out.println();
		if (dataFlag.toLowerCase().equals("male"))
		{
			for (String tmpString : maleNames)
			{
				System.out.println(tmpString);
			}
		}
		else if (dataFlag.toLowerCase().equals("female"))
		{
			for (String tmpString : femaleNames)
			{
				System.out.println(tmpString);
			}
		}
		else if (dataFlag.toLowerCase().equals("surname"))
		{
			for (String tmpString : surnames)
			{
				System.out.println(tmpString);
			}
		}
		else if (dataFlag.toLowerCase().equals("addresses"))
		{
			for (String[] tmpArray : addresses)
			{
				for (int i = 0; i < tmpArray.length; i++)
				{
					if (i < tmpArray.length - 1)
					{
						System.out.print(tmpArray[i] + " ");
					}
					else
					{
						System.out.print(tmpArray[i] + "\n");
					}
				}
			}
		}
		else
		{
			System.out.println("<ERROR>\nThe flag entered is incorrect.  It must be male, female, or surname.\n</EROROR>");
		}
		
		System.out.println("Done.\n");
	}
	
	private int calculateBlockSize(int amountToGenerate)
	{
		if (amountToGenerate % 2 == 0)
		{
			for (int i = 30; i > 0; i--)
			{
				int result = (int) Math.pow(2, i);
				if (amountToGenerate % result == 0)
				{
					return result;
				}
			}
		}
		else
		{
			for (int i = 3; i < amountToGenerate / 3; i += 2)
			{
				int result = amountToGenerate % i;
				if (result == 0)
				{
					return result;
				}
			}
		}
		return 1;
	}
	
	private int calculateBlockSize2(int amountToGenerate)
	{
		int largestFactor = 0;
		for (int i = 0; i < amountToGenerate; i++)
		{
			int result = (int) Math.pow(2, i);
			if (amountToGenerate % result == 0 && result > largestFactor)
			{
				largestFactor = result;
			}
			if (result > amountToGenerate)
			{
				return largestFactor;
			}
		}
		return 1;
	}
	
	private void generateTestDataBlocks(int blockSize, int blockAmount, boolean useDoubleQuotes, boolean isEvenNumber)
	{
		//record starting point of I/O
		Instant startTime = Instant.now();
		if (isEvenNumber)
		{
			System.out.println("@@@ Generating " + (blockSize * blockAmount) + " rows of test data @@@");
		}
		else
		{
			System.out.println("@@@ Generating " + ((blockSize * blockAmount) +  1) + " rows of test data @@@");
		}
		System.out.println();
		
		for (int i = 0; i < blockAmount; i++)
		{
			generateTestData(blockSize, useDoubleQuotes);
		}
		//record finshing point of I/O
		Instant finishTime = Instant.now();
		if (isEvenNumber)
		{
			System.out.println("Done. Generated " + (blockSize * blockAmount) + " rows via " + blockAmount + " block(s) of " + blockSize + " at a time.");
		}
		else
		{
			System.out.println("Done. Generated " + (blockSize * blockAmount) + " rows via " + blockAmount + " block(s) of " + blockSize + " at a time.");
			System.out.println("1 additional row was also generated.");
		}
		System.out.println("The time elapsed was: " + Duration.between(startTime, finishTime).toString().substring(2) + "\n");
		System.out.println();
	}
	
	private void generateTestData(int amountToGenerate, boolean useDoubleQuotes)
	{
		//record starting point of I/O
		Instant startTime = Instant.now();
		//System.out.println("*** Generating " + amountToGenerate + " of test data ***");
		
		//system information
		long personID;
		
		//characteristics
		char sex;
		int yearOfBirth;
		int monthOfBirth;
		int dayOfBirth;
		
		//names
		String title;
		String firstName;
		String middleName;
		String surname;
		
		//contact information
		String homePhoneNumber;
		String mobileNumber;
		String peronsalEmail;
		String workEmail;
		String homeAddress;
		String mailingAddress;
		
		//currency information
		double bankBalance;
		String currencySign;
		//char currencySign;
		String currencyCode;
		
		//identity information
		String birthCertificate;
		String driversLicence;
		String passport;
		
		
		for (int i = 0; i < amountToGenerate; i++)
		{
			//TEMPORARY VARIABLES
			//pesudo random number
			int prn = ThreadLocalRandom.current().nextInt(0, 2);
			String tmpString;
			char tmpChar;
			
			//system information
			personID = ThreadLocalRandom.current().nextLong(2000000000, 3000000000L);
			
			//characteristics 0 = male, 1 = female
			yearOfBirth = ThreadLocalRandom.current().nextInt(1900, 2018);
			monthOfBirth = ThreadLocalRandom.current().nextInt(1, 13);
			dayOfBirth = ThreadLocalRandom.current().nextInt(1, 32);
			
			if (prn == 0)
			{
				sex = 'M';
				if (yearOfBirth < 2000)
				{
					title = maleTitles[ThreadLocalRandom.current().nextInt(0, maleTitles.length)];
				}
				else
				{
					title = "MSTR";
				}
				
				//names
				firstName = createName("first", true);
				middleName = createName("middle", true);
				surname = createName("surname", true);
				
			}
			else
			{
				sex = 'F';
				if (yearOfBirth < 2000)
				{
					title = femaleTitles[ThreadLocalRandom.current().nextInt(0, femaleTitles.length)];
				}
				else
				{
					title = "MISS";
				}
				
				//names
				firstName = createName("first", false);
				middleName = createName("middle", false);
				surname = createName("surname", false);
			}
			
			//addresses, <=3 = random home address only, <=6 = random mailing address only, <=9 random home and mailing address, 10 is none
			prn = ThreadLocalRandom.current().nextInt(0, 11);
			if (prn <= 3)
			{
				homeAddress = createAddress(true);
				mailingAddress = "";
			}
			else if (prn <= 6)
			{
				homeAddress = "";
				mailingAddress = createAddress(false);
			}
			else if (prn <= 9)
			{
				homeAddress = createAddress(true);
				mailingAddress = createAddress(false);
			}
			else
			{
				homeAddress = "";
				mailingAddress = "";
			}
			
			//email addresses <=3 random personal email only, <=6 = random work email only, <=9 random personal and work email, 10 = none
			//if born after 2000 no work email, if born between 2000 and 2007, personal email only.  born after 2007 means no email at all.  else is random
			if (yearOfBirth < 2007)
			{
				prn = ThreadLocalRandom.current().nextInt(0, 11);
				if (prn <= 3)
				{
					peronsalEmail = createEmail(true, title, firstName, surname, yearOfBirth + "");
					workEmail = "";
				}
				else if (prn <= 6 && yearOfBirth < 2000)
				{
					peronsalEmail = "";
					workEmail = createEmail(false, title, firstName, surname, yearOfBirth + "");
				}
				else if (prn <= 9 && yearOfBirth < 2000)
				{
					peronsalEmail = createEmail(true, title, firstName, surname, yearOfBirth + "");
					workEmail = createEmail(false, title, firstName, surname, yearOfBirth + "");
				}
				else if (prn != 10 && (yearOfBirth >= 2000 || yearOfBirth <= 2007))
				{
					peronsalEmail = createEmail(true, title, firstName, surname, yearOfBirth + "");
					workEmail = "";
				}
				else
				{
					peronsalEmail = "";
					workEmail = "";
				}
			}
			else
			{
				peronsalEmail = "";
				workEmail = "";
			}
			
			//phone number: <=30 random homePhoneNumber, <=60 = random mobileNumber, <=90 both, and > 91 = none
			//if born after 2000 no homePhoneNumber, if born between 2000 and 2007, mobileNumber only.  born after 2007 means no phone at all.  else is random
			if (yearOfBirth < 2007)
			{
				prn = ThreadLocalRandom.current().nextInt(1, 101);
				if (prn <= 30)
				{
					homePhoneNumber = createPhone(false);
					mobileNumber = "";
				}
				else if (prn <= 60 && yearOfBirth < 2000)
				{
					homePhoneNumber = "";
					mobileNumber = createPhone(true);
				}
				else if (prn <= 90 && yearOfBirth < 2000)
				{
					homePhoneNumber = createPhone(false);
					mobileNumber = createPhone(true);
				}
				else if (prn <= 90 && (yearOfBirth >= 2000 || yearOfBirth <= 2007))
				{
					homePhoneNumber = "";
					mobileNumber = createPhone(true);
				}
				else
				{
					homePhoneNumber = "";
					mobileNumber = "";
				}
			}
			else
			{
				homePhoneNumber = "";
				mobileNumber = "";
			}
			
			
			//birth certificate, 60% chance that we have it
			prn = ThreadLocalRandom.current().nextInt(1, 101);
			if (prn <= 60)
			{
				birthCertificate = createID("birth certificate", yearOfBirth + "");
			}
			else
			{
				birthCertificate = "";
			}
			
			//drivers licence.  born <= 2000 80% chance of DL
			prn = ThreadLocalRandom.current().nextInt(1, 101);
			if (yearOfBirth <= 2000 && prn <= 80)
			{
				driversLicence = createID("drivers licence", yearOfBirth + "");
			}
			else
			{
				driversLicence = "";
			}
			//passport. All have 60% chance of passport
			prn = ThreadLocalRandom.current().nextInt(0, 11);
			if (yearOfBirth <= 2000 && prn <= 60)
			{
				passport = createID("passport", yearOfBirth + "");
			}
			else
			{
				passport = "";
			}
			
			//currency symbols. 30% chance for $ (dollar), 10% for ₤ (pound), 10% for € (euro), 10% for ¥ (yen/yuan), 10% for ₩ (won), 10% for (ruble), 10% for (rupee), & 10% for nothing ( )
			//currency codes.
			prn = ThreadLocalRandom.current().nextInt(1, 101);
			if (prn <= 30)
			{
				currencySign = String.valueOf(CURRENCY_SIGNS[0]);
				//assign an international currency
				prn = ThreadLocalRandom.current().nextInt(1, 101);
				if (prn >= 50)
				{
					currencyCode = CURRENCY_CODES[0];
				}
				else if (prn <= 65)
				{
					currencyCode = CURRENCY_CODES[1];
				}
				else if (prn <= 80)
				{
					currencyCode = CURRENCY_CODES[2];
				}
				else
				{
					currencyCode = "";
				}
			}
			else if (prn <= 40)
			{
				currencySign = String.valueOf(CURRENCY_SIGNS[1]);
				//assign an international currency
				prn = ThreadLocalRandom.current().nextInt(1, 101);
				if (prn <= 50)
				{
					currencyCode = CURRENCY_CODES[3];
				}
				else
				{
					currencyCode = "";
				}
			}
			else if (prn <= 50)
			{
				currencySign = String.valueOf(CURRENCY_SIGNS[2]);
				//assign an international currency
				prn = ThreadLocalRandom.current().nextInt(1, 101);
				if (prn <= 50)
				{
					currencyCode = CURRENCY_CODES[4];
				}
				else
				{
					currencyCode = "";
				}
			}
			else if (prn <= 60)
			{
				currencySign = String.valueOf(CURRENCY_SIGNS[3]);
				//assign an international currency
				prn = ThreadLocalRandom.current().nextInt(1, 101);
				if (prn <= 33)
				{
					currencyCode = CURRENCY_CODES[5];
				}
				else if (prn <= 67)
				{
					currencyCode = CURRENCY_CODES[6];
				}
				else
				{
					currencyCode = "";
				}
			}
			else if (prn <= 70)
			{
				currencySign = String.valueOf(CURRENCY_SIGNS[4]);
				//assign an international currency
				prn = ThreadLocalRandom.current().nextInt(1, 101);
				if (prn <= 33)
				{
					currencyCode = CURRENCY_CODES[7];
				}
				else if (prn <= 67)
				{
					currencyCode = CURRENCY_CODES[8];
				}
				else
				{
					currencyCode = "";
				}
			}
			//russian and indian disabled due to Teradata 14 not supporting the unicode symbols.
/*			//10% chance of Russian Ruple
			else if (prn <= 80)
			{
				currencySign = String.valueOf(CURRENCY_SIGNS[5]);

				//assign an international currency
				prn = ThreadLocalRandom.current().nextInt(1, 101);
				//50% chance of RUB
				if (prn <= 50)
				{
					currencyCode = CURRENCY_CODES[9];
				}
				//50% chance of nothing
				else
				{
					currencyCode = "";
				}
			}
			//10% chance of Indian Rubee
			else if (prn <= 90)
			{
				currencySign = String.valueOf(CURRENCY_SIGNS[6]);

				//assign an international currency
				prn = ThreadLocalRandom.current().nextInt(1, 101);
				//50% chance of INR
				if (prn <= 50)
				{
					currencyCode = CURRENCY_CODES[10];
				}
				//50% chance of nothing
				else
				{
					currencyCode = "";
				}
			}*/
			else
			{
				currencySign = "";
				//currencySign = '\u0000';
				currencyCode = "";
			}
			
			//bank balance.  > 2000 have 80% chance somewhere between 0 and 1,000, else 1,000-10,000.
			// < 2000 have 20% chance tosomewhere between 0 and 1,000, 40% between 1,000 and 10,000, 30% between 10,000 and 100,000, else 100,000 and 1,000,000
			prn = ThreadLocalRandom.current().nextInt(1, 101);
			if (yearOfBirth > 2000 && prn <= 80)
			{
				bankBalance = ThreadLocalRandom.current().nextDouble(0.00, 1000.00);
			}
			else if (yearOfBirth > 2000 && prn > 80)
			{
				bankBalance = ThreadLocalRandom.current().nextDouble(1000.00, 10000.00);
			}
			else if (yearOfBirth < 2000 && prn <= 20)
			{
				bankBalance = ThreadLocalRandom.current().nextDouble(0.00, 1000.00);
			}
			else if (yearOfBirth < 2000 && prn <= 60)
			{
				bankBalance = ThreadLocalRandom.current().nextDouble(1000.00, 10000.00);
			}
			else if (yearOfBirth < 2000 && prn <= 80)
			{
				bankBalance = ThreadLocalRandom.current().nextDouble(10000.00, 100000.00);
			}
			else
			{
				bankBalance = ThreadLocalRandom.current().nextDouble(100000.00, 1000000.00);
			}
			if (useDoubleQuotes)
			{
				String[] tmpStringArray = {"\"" + personID + "\"", "\"" +  title+ "\"", "\"" +  firstName+ "\"", "\"" +  middleName+ "\"", "\"" +  surname+ "\"", "\"" +  sex + ""+ "\"", "\"" +  yearOfBirth + "/" + monthOfBirth + "/" + dayOfBirth+ "\"", "\"" +  homeAddress+ "\"", "\"" +  mailingAddress+ "\"", "\"" +  peronsalEmail+ "\"", "\"" +  workEmail+ "\"", "\"" +  homePhoneNumber+ "\"", "\"" +  mobileNumber+ "\"", "\"" +  birthCertificate+ "\"", "\"" +  driversLicence+ "\"", "\"" +  passport+ "\"", "\"" +  currencySign+ "\"", "\"" +  currencyCode+ "\"", "\"" +  String.format("%7.2f", bankBalance).trim() + "\""};
				testDataStringArray.add(tmpStringArray);
			}
			else
			{
				String[] tmpStringArray = {personID + "", title, firstName, middleName, surname, sex + "", yearOfBirth + "/" + monthOfBirth + "/" + dayOfBirth, homeAddress, mailingAddress, peronsalEmail, workEmail, homePhoneNumber, mobileNumber, birthCertificate, driversLicence, passport, currencySign, currencyCode, String.format("%7.2f", bankBalance).trim()};
				//String[] tmpStringArray = {personID + "", title, firstName, middleName, surname, sex + "", yearOfBirth + "/" + monthOfBirth + "/" + dayOfBirth, homeAddress, mailingAddress, peronsalEmail, workEmail, homePhoneNumber, mobileNumber, birthCertificate, driversLicence, passport, Character.toString(currencySign), currencyCode, String.format("%7.2f", bankBalance).trim()};
				testDataStringArray.add(tmpStringArray);
			}
		}
		
		//record finishing point of I/O
		//Instant finishTime = Instant.now();
		
		//display runtime
		//System.out.println("Done. Creation time was: " + Duration.between(startTime, finishTime).toString().substring(2) + "\n");
	}
	
	private String createName(String nameFlag, boolean isMale)
	{
		//generate 1 or 2 names.
		int tmpInt = ThreadLocalRandom.current().nextInt(1, 101);
		if (nameFlag.toLowerCase().equals("first") && isMale)
		{
			//80% chance of 1 name
			String firstName = new String(maleNames.get(ThreadLocalRandom.current().nextInt(0, maleNames.size())));
			;
			if (tmpInt >= 80)
			{
				tmpInt = ThreadLocalRandom.current().nextInt(0, 11);
				if (tmpInt <= 80)
				{
					firstName = firstName + "-" + maleNames.get(ThreadLocalRandom.current().nextInt(0, maleNames.size()));
				}
				else
				{
					firstName = firstName + " " + maleNames.get(ThreadLocalRandom.current().nextInt(0, maleNames.size()));
				}
			}
			return firstName.toUpperCase();
		}
		else if (nameFlag.toLowerCase().equals("first") && !isMale)
		{
			String firstName = new String(femaleNames.get(ThreadLocalRandom.current().nextInt(0, femaleNames.size())));
			;
			if (tmpInt >= 80)
			{
				tmpInt = ThreadLocalRandom.current().nextInt(1, 101);
				if (tmpInt <= 80)
				{
					firstName = firstName + "-" + femaleNames.get(ThreadLocalRandom.current().nextInt(0, femaleNames.size()));
				}
				else
				{
					firstName = firstName + " " + femaleNames.get(ThreadLocalRandom.current().nextInt(0, femaleNames.size()));
				}
			}
			return firstName.toUpperCase();
		}
		else if (nameFlag.toLowerCase().equals("middle") && isMale)
		{
			//75% chance of 1 name, 75% for a hyphen in a second name
			String middleName = new String(maleNames.get(ThreadLocalRandom.current().nextInt(0, maleNames.size())));
			;
			if (tmpInt >= 75)
			{
				tmpInt = ThreadLocalRandom.current().nextInt(1, 101);
				if (tmpInt <= 75)
				{
					middleName = middleName + "-" + maleNames.get(ThreadLocalRandom.current().nextInt(0, maleNames.size()));
				}
				else
				{
					middleName = middleName + " " + maleNames.get(ThreadLocalRandom.current().nextInt(0, maleNames.size()));
				}
			}
			return middleName.toUpperCase();
		}
		else if (nameFlag.toLowerCase().equals("middle") && !isMale)
		{
			String middleName = new String(femaleNames.get(ThreadLocalRandom.current().nextInt(0, femaleNames.size())));
			;
			if (tmpInt >= 75)
			{
				tmpInt = ThreadLocalRandom.current().nextInt(1, 101);
				if (tmpInt <= 75)
				{
					middleName = middleName + "-" + femaleNames.get(ThreadLocalRandom.current().nextInt(0, femaleNames.size()));
				}
				else
				{
					middleName = middleName + " " + femaleNames.get(ThreadLocalRandom.current().nextInt(0, femaleNames.size()));
				}
			}
			return middleName.toUpperCase();
		}
		else if (nameFlag.toLowerCase().equals("surname"))
		{
			//60% chance of 1 name
			String surname = new String(surnames.get(ThreadLocalRandom.current().nextInt(0, surnames.size())));
			;
			if (tmpInt >= 60)
			{
				surname = surname + "-" + surnames.get(ThreadLocalRandom.current().nextInt(0, surnames.size()));
			}
			
			return surname.toUpperCase();
		}
		else
		{
			return "Incorrect flag.";
		}
	}
	
	private String createAddress(boolean isResidential)
	{
		
		//building blocks
		String deliveryType = "";
		String unitType = "";
		String unitNumber = "";
		String streetNumber = "";
		String deliveryNumber = "";
		String streetName = "";
		String streetType = "";
		String suburb = "";
		String state = "";
		String postcode = "";
		
		//final address
		String address;
		
		int prn;
		
		if (isResidential)
		{
			//what type of address, 0 = house, 1 = unit
			prn = ThreadLocalRandom.current().nextInt(0, 2);
			if (prn == 1)
			{
				unitType = unitTypes[ThreadLocalRandom.current().nextInt(0, unitTypes.length)];
				unitNumber = ThreadLocalRandom.current().nextInt(1, 501) + "";
			}
			streetNumber = ThreadLocalRandom.current().nextInt(1, 1001) + "";
			
			//how many street names, 1 or 2
			prn = ThreadLocalRandom.current().nextInt(1, 3);
			streetName = femaleNames.get(ThreadLocalRandom.current().nextInt(0, femaleNames.size()));
			if (prn == 2)
			{
				//choose whether a hyphen or space
				prn = ThreadLocalRandom.current().nextInt(0, 2);
				if (prn == 0)
				{
					streetName = streetName + " " + maleNames.get(ThreadLocalRandom.current().nextInt(0, maleNames.size()));
				}
				else
				{
					streetName = streetName + "-" + femaleNames.get(ThreadLocalRandom.current().nextInt(0, femaleNames.size()));
				}
			}
			streetType = streetTypes[ThreadLocalRandom.current().nextInt(0, streetTypes.length)];
		}
		else
		{
			deliveryType = deliveryTypes[ThreadLocalRandom.current().nextInt(0, deliveryTypes.length)];
			deliveryNumber = ThreadLocalRandom.current().nextInt(1, 1001) + "";
		}
		
		//common address details
		prn = ThreadLocalRandom.current().nextInt(1, 101);
		suburb = surnames.get(ThreadLocalRandom.current().nextInt(0, surnames.size()));
		if (prn <= 50)
		{
			prn = ThreadLocalRandom.current().nextInt(1, 101);
			if (prn <= 40)
			{
				suburb = suburb + " " + suburbPostfixes[(ThreadLocalRandom.current().nextInt(0, suburbPostfixes.length))];
			}
			else if (prn <= 80)
			{
				suburb = suburbPreFixes[(ThreadLocalRandom.current().nextInt(0, suburbPreFixes.length))] + " " + suburb;
				
			}
			else
			{
				suburb = suburb + " " + surnames.get(ThreadLocalRandom.current().nextInt(0, surnames.size()));
			}
		}
		state = stateCodes[ThreadLocalRandom.current().nextInt(0, stateCodes.length)];
		postcode = (short) ThreadLocalRandom.current().nextInt(2000, 9000) + "";
		
		if (isResidential && unitType.isEmpty())
		{
			address = streetNumber + " " + streetName + " " + streetType + ", " + suburb + ", " + state + ", " + postcode;
		}
		else if (isResidential && !unitType.isEmpty())
		{
			address = unitType + " " + unitNumber + "/" + streetNumber + " " + streetName + " " + streetType + ", " + suburb + ", " + state + ", " + postcode;
		}
		else
		{
			address = deliveryType + " " + deliveryNumber + ", " + suburb + ", " + state + ", " + postcode;
		}
		return address.toUpperCase() + ".";
	}
	
	private String createEmail(boolean personalEmail, String title, String firstName, String surname, String yearOfBirth)
	{
		int prn = ThreadLocalRandom.current().nextInt(0, 2);
		String email = "";
		
		if (personalEmail)
		{
			if (prn == 1)
			{
				email = new String(firstName.replaceAll(" ", "_") + yearOfBirth.substring(2) + personalEmailDomains[ThreadLocalRandom.current().nextInt(0, personalEmailDomains.length)]);
			}
			else
			{
				email = new String(surname.replaceAll(" ", "_") + yearOfBirth.substring(2) + personalEmailDomains[ThreadLocalRandom.current().nextInt(0, personalEmailDomains.length)]);
			}
		}
		else
		{
			if (prn == 1)
			{
				email = new String(title + "." + firstName.replaceAll(" ", "_") + "." + surname.replaceAll(" ", "_") + workEmailDomains[ThreadLocalRandom.current().nextInt(0, workEmailDomains.length)]);
			}
			else
			{
				email = new String(firstName.replaceAll(" ", "_") + "." + surname.replaceAll(" ", "_") + workEmailDomains[ThreadLocalRandom.current().nextInt(0, workEmailDomains.length)]);
			}
		}
		
		return email.toUpperCase();
	}
	
	private String createPhone(boolean isMobile)
	{
		String phoneNumber;
		if (isMobile)
		{
			phoneNumber = "+" + ThreadLocalRandom.current().nextLong(61400000000L, 61500000000L);
			;
		}
		else
		{
			int areaCodeNumber;
			int prn = ThreadLocalRandom.current().nextInt(1, 101);
			{
				if (prn <= 50)
				{
					areaCodeNumber = ThreadLocalRandom.current().nextInt(2, 4);
				}
				else
				{
					areaCodeNumber = ThreadLocalRandom.current().nextInt(5, 10);
				}
			}
			int geographicCodeNumber = ThreadLocalRandom.current().nextInt(20, 100);
			int localNumber = ThreadLocalRandom.current().nextInt(100000, 1000000);
			phoneNumber = "0" + areaCodeNumber + geographicCodeNumber + localNumber;
		}
		return phoneNumber;
	}
	
	private String createID(String idType, String year)
	{
		String idNumber;
		//https://www.usi.gov.au/about/forms-id/birth-certificate-australian
		if (idType.toLowerCase().equals("birth certificate"))
		{
			idNumber = year + "/" + ThreadLocalRandom.current().nextInt(10000, 100000);
		}
		//https://www.usi.gov.au/about/forms-id/drivers-licence
		else if (idType.toLowerCase().equals("drivers licence"))
		{
			idNumber = ThreadLocalRandom.current().nextInt(1000000, 10000000) + "";
		}
		//https://www.usi.gov.au/about/forms-id/non-australian-passport-australian-visa
		else if (idType.toLowerCase().equals("passport"))
		{
			int prn = ThreadLocalRandom.current().nextInt(1, 3);
			if (prn == 1)
			{
				idNumber = (char) ThreadLocalRandom.current().nextInt(65, 91) + "" + ThreadLocalRandom.current().nextInt(1000000, 10000000);
			}
			else
			{
				idNumber = (char) ThreadLocalRandom.current().nextInt(65, 91) + "" + (char) ThreadLocalRandom.current().nextInt(65, 91) + "" + ThreadLocalRandom.current().nextInt(1000000, 10000000);
			}
		}
		else
		{
			idNumber = "";
		}
		return idNumber;
	}
	
	private void printHeader(boolean useDoubleQuotes)
	{
		for (int i = 0; i < FILE_HEADER.length; i++)
		{
			if (useDoubleQuotes)
			{
				System.out.print("\"" + FILE_HEADER[i] + "\"" + delimter);
			}
			else
			{
				System.out.print(FILE_HEADER[i] + delimter);
			}
		}
		System.out.println();
	}
	
	private void printTestData(int amountToPrint)
	{
		for (int i = 0; i < amountToPrint; i++)
		{
			String[] tmpString = testDataStringArray.get(i);
			for (int j = 0; j < tmpString.length; j++)
			{
				System.out.print(tmpString[j] + delimter);
			}
			System.out.println();
		}
	}
}