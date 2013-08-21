package coza.mambo.migraine.core;

/**
 * User: Daniel
 */
public class _ {
	static int offset = 0 ;
	static int d()
	{
		return Thread.currentThread().getStackTrace().length  - offset;
	}

	static void setOffset()
	{
		_.offset = d();
	}

	static StringBuffer buffer = new StringBuffer("                             ");
	static String t()
	{

		boolean efficient = false ;
		if (efficient) //prevents copying in intellij console
		{
		//offset by d() length
		buffer.setLength(d());
		String s = new String(buffer.toString());
		return s;
		}
		else
		{
			String s = "";
			for(int i = 0, d = d(); i < d ; i++)
			{
				s = s + " ";
			}
			return s ;
		}
	}
}
