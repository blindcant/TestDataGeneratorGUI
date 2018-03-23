/**
 * Created by blindcant on 10/07/17.
 */
public class TestFactorClass
{
	//# INSTANCE VARIABLES #
	
	
	//# MAIN METHOD #
	public static void main(String[] args)
	{
		TestFactorClass testFactorClass = new TestFactorClass();
	}
	
	//# CONSTRUCTOR(S) #
	public TestFactorClass()
	{
/*		System.out.println(calculateBlockSizeEven(16));
		System.out.println(calculateBlockSizeEven(32));
		System.out.println(calculateBlockSizeEven(64));
		System.out.println(calculateBlockSizeEven(65536));
		System.out.println(calculateBlockSizeEven(1073741824));
		
		System.out.println(calculateBlockSizeOdd(9));
		System.out.println(calculateBlockSizeOdd(15));
		System.out.println(calculateBlockSizeOdd(63));
		System.out.println(calculateBlockSizeOdd(65535));
		System.out.println(calculateBlockSizeOdd(1073741823));
		System.out.println(calculateBlockSizeOdd(2147483647));*/
		printExponentsAsArrayInitialiser(2, 15);
	}
	
	//# METHODS #
	private int calculateBlockSizeEven(int amountToGenerate)
	{
		int largestFactor = 0;
		for (int i = 0; i < 30; i++)
		{
			int result = (int) Math.pow(2, i);
			if (amountToGenerate % result == 0 && result > largestFactor && result < amountToGenerate)
			{
				largestFactor = result;
			}
			if (result > amountToGenerate - 1)
			{
				return largestFactor;
			}
		}
		return 1;
	}
	
	private int calculateBlockSizeOdd(int amountToGenerate)
	{
		int factor = 1;
		for (int i = 3; i < (amountToGenerate - 1); i+=2)
		{
			if (amountToGenerate % i == 0 && i > factor)
			{
				factor = i;
			}
		}
		return factor;
	}

	private void printExponentsAsArrayInitialiser(int number, int amount)
	{
		for (int i = 0; i < amount; i++)
		{
			int result = (int) Math.pow(number, i);
			if (i == 0)
			{
				System.out.print("{ " + result + ", ");
			}
			else if (i > 0 && i < amount - 1)
			{
				System.out.print(result + ", ");
			}
			else
			{
				System.out.print(result + " }");
			}
		}
	}
}
