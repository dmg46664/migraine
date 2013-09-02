package coza.mambo.migraine.core;

import java.util.HashMap;
import java.util.Map;

/**
 * All the names in this class are small, because the logging in the IDE has limited space
 * User: Daniel
 */
public class _ {
	static int offset = 0 ;
	static int d(int offset2)
	{
		return Thread.currentThread().getStackTrace().length  - offset - offset2;
	}

	static Map<String, Integer> map = new HashMap<String, Integer>();

	/** print stacktrace line with metadata, offset by depth*/
	static String p(Integer setting, Object thisObj, String message)
	{
		StackTraceElement trace = Thread.currentThread().getStackTrace()[2] ;
		String fullClassName = trace.getClassName();
		int last = fullClassName.lastIndexOf('.');
		String packageName = fullClassName.substring(0, last);
		String className = fullClassName.substring(last+1, fullClassName.length()) ;
		String place = fullClassName+"."+trace.getMethodName()
				+"("+trace.getFileName()+":"+trace.getLineNumber()+")";



		int passcount = 1;
		if (!map.containsKey(place))
			map.put(place, passcount);
		else
		{
			passcount = map.get(place);
			map.put(place, ++passcount);
		}

		return padLeft(place, PAD_COUNT)+_.t(1)+
				className+"."+trace.getMethodName()+"() ["+thisObj.getClass()+"] Pass count: "+ passcount +" "
			+message;
	}

	public static final int PAD_COUNT = 80;

	public static String padLeft(String s, int n) {
		return String.format("%1$" + n + "s", s);
	}

	static void initialize()
	{
		_.offset = d(0);
		map = new HashMap<String, Integer>();
	}

	static StringBuffer buffer = new StringBuffer("                             ");

	/** for tabbing out the stacktrace */
	static String t()
	{
		return padLeft("", PAD_COUNT)+t(0);
	}

	static String t(int offset2)
	{

		boolean efficient = false ;
		if (efficient) //prevents copying in intellij console
		{
		//offset by d() length
		buffer.setLength(d(0));
		String s = new String(buffer.toString());
		return s;
		}
		else
		{
			String s = "";
			for(int i = 0, d = d(0); i < d ; i++)
			{
				s = s + " ";
			}
			return s ;
		}
	}
}
