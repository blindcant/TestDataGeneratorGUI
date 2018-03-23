import java.io.BufferedWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by blindcant on 7/07/17.
 */
public class TestCurrency
{
	//# INSTANCE VARIABLES #
	//currency - dollar, pound, euro, japanese yen/ chinese yuan, korean won, russian ruble, indian rupee
	private char[] currencySignsChar = {'\u0024', '\u00A3', '\u20AC', '\u00A5', '\u20A9', '\u20BD', '\u20B9'};
	private byte[] currencySignsByte = {(byte) '\u0024', (byte)'\u00A3', (byte)'\u20AC', (byte)'\u00A5', (byte)'\u20A9', (byte)'\u20BD', (byte)'\u20B9'};
	private String[] currencySignsString = {"$", "£", "€", "¥", "₩", "₽", "₹"};
	private String[] currencyCodes = {"AUD", "USD", "NZD", "GBP", "EUR", "JPY", "CNY", "KPW", "KRW", "RUB", "INR"};
	private ArrayList<String> stringArrayList = new ArrayList<>();
	private char[] charArray = new char[100];
	private byte[] byteArray = new byte[100];
	private String currencySignString;
	private char currencySignChar;
	private byte currencySignByte;
	private String currencyCode;
	int prn;

	//file I/O
	private Path outputPath = Paths.get("C:/fastload-data/temp.txt");
	private BufferedWriter stdoutFile;

	//# MAIN METHOD #
	public static void main(String[] args)
	{
		TestCurrency runTime = new TestCurrency();
	}

	//# CONSTRUCTOR(S) #
	public TestCurrency()
	{
		createData(100);
		//writeArray();
	}

	//# METHODS #

	private void createData(int amount)
	{
		byte[] tmp = new byte[1];
		for (int i = 0; i < amount; i++)
		{
			prn = ThreadLocalRandom.current().nextInt(1, 101);
			//30% chance for dollar
			if (prn <= 30)
			{
				currencySignString = currencySignsString[0];
				currencySignChar = currencySignsChar[0];
				currencySignByte = currencySignsByte[0];
				tmp[0] = currencySignsByte[0];

				//assign an international currency
				prn = ThreadLocalRandom.current().nextInt(1, 101);
				//50% chance for AUD
				if (prn >= 50)
				{
					currencyCode = currencyCodes[0];
				}
				//15% chance for USD
				else if (prn <= 65)
				{
					currencyCode = currencyCodes[1];
				}
				//15% chance for NZD
				else if (prn <= 80)
				{
					currencyCode = currencyCodes[2];
				}
				//20% chance for nothing
				else
				{
					currencyCode = "";
				}
			}
			//10% chance for pounds
			else if (prn <= 40)
			{
				currencySignString = currencySignsString[1];
				currencySignChar = currencySignsChar[1];
				currencySignByte = currencySignsByte[1];
				tmp[0] = currencySignsByte[1];

				//assign an international currency
				prn = ThreadLocalRandom.current().nextInt(1, 101);
				//50% chance of GBP
				if (prn <= 50)
				{
					currencyCode = currencyCodes[3];
				}
				//50% chance for nothing
				else
				{
					currencyCode = "";
				}
			}
			//10% chance for Euro
			else if (prn <= 50)
			{
				currencySignString = currencySignsString[2];
				currencySignChar = currencySignsChar[2];
				currencySignByte = currencySignsByte[2];
				tmp[0] = currencySignsByte[2];

				//assign an international currency
				prn = ThreadLocalRandom.current().nextInt(1, 101);

				if (prn <= 50)
				{
					currencyCode = currencyCodes[4];
				} else
				{
					currencyCode = "";
				}
			}
			//10% chance for Yen/Yuan
			else if (prn <= 60)
			{
				currencySignString = currencySignsString[3];
				currencySignChar = currencySignsChar[3];
				currencySignByte = currencySignsByte[3];
				tmp[0] = currencySignsByte[3];

				//assign an international currency
				prn = ThreadLocalRandom.current().nextInt(1, 101);
				//33% chance for JPY
				if (prn <= 33)
				{
					currencyCode = currencyCodes[5];
				}
				//33% chance for CNY
				else if (prn <= 67)
				{
					currencyCode = currencyCodes[6];
				}
				//34% chance of nothing
				else
				{
					currencyCode = "";
				}
			}
			//10% chance of Korean Won
			else if (prn <= 70)
			{
				currencySignString = currencySignsString[4];
				currencySignChar = currencySignsChar[4];
				currencySignByte = currencySignsByte[4];
				tmp[0] = currencySignsByte[4];

				//assign an international currency
				prn = ThreadLocalRandom.current().nextInt(1, 101);
				//33% chance of KPW (North)
				if (prn <= 33)
				{
					currencyCode = currencyCodes[7];
				}
				//33% chance of KRW (South)
				else if (prn <= 67)
				{
					currencyCode = currencyCodes[8];
				}
				//34% chance of nothing
				else
				{
					currencyCode = "";
				}
			}
			//10% chance of Russian Ruple
			else if (prn <= 80)
			{
				currencySignString = currencySignsString[5];
				currencySignChar = currencySignsChar[5];
				currencySignByte = currencySignsByte[5];
				tmp[0] = currencySignsByte[5];

				//assign an international currency
				prn = ThreadLocalRandom.current().nextInt(1, 101);
				//50% chance of RUB
				if (prn <= 50)
				{
					currencyCode = currencyCodes[9];
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
				currencySignString = currencySignsString[6];
				currencySignChar = currencySignsChar[6];
				currencySignByte = currencySignsByte[6];
				tmp[0] = currencySignsByte[6];

				//assign an international currency
				prn = ThreadLocalRandom.current().nextInt(1, 101);
				//50% chance of INR
				if (prn <= 50)
				{
					currencyCode = currencyCodes[10];
				}
				//50% chance of nothing
				else
				{
					currencyCode = "";
				}
			}
			//10% chance of nothing


			stringArrayList.add(currencySignString);
			//byteArray[i] = currencySignString.getBytes(Charset.forName("UTF-8"));
			charArray[i] = currencySignChar;
			String tempStringUTF8 = new String(tmp, Charset.forName("UTF-8"));
			String tempStringUTF16BE = new String(tmp, Charset.forName("UTF-16BE"));
			String tempStringUTF16LE = new String(tmp, Charset.forName("UTF-16LE"));

			System.out.println("String: " + currencySignString + " " + " number of code points: " + currencySignString.codePointCount(0, currencySignString.length()) + " string legnth: " + currencySignString.length());
			System.out.println("char: " + currencySignChar + " is a valid uncide code point? " + Character.isValidCodePoint(currencySignChar)
					+ ". unicode block: " + Character.UnicodeBlock.of(currencySignChar)
					+ ". unicode script: " + Character.UnicodeScript.of(currencySignChar)
					+ ". char count: " + Character.charCount(currencySignChar)
					+ ". char casted to UTF-8 " + tempStringUTF8
					+ ". char casted to UTF-16 BE" + tempStringUTF16BE
					+ ". char casted to UTF-16 LE" + tempStringUTF16LE);
			System.out.println();

		}
	}

	private void writeArray()
	{
		try
		{
			stdoutFile = Files.newBufferedWriter(outputPath, Charset.forName("UTF-8"));

			for(String currentLine : stringArrayList)
			{
				stdoutFile.write(currentLine);
			}
			stdoutFile.newLine();
			stdoutFile.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}
}
