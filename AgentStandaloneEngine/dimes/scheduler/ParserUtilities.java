package dimes.scheduler;

//import com.l2fprod.gui.plaf.skin.impl.gtk.parser.ParseException;


public class ParserUtilities {

		public static String[] parseStringArray(String parameter) {
			String[] parts = parameter.split(",");
			String[] result = new String[parts.length];
			if (result.length == 0)
				return result;
//			parts[0] = parts[0].substring(1);
//			parts[result.length-1] = parts[result.length-1].substring(0 ,parts[result.length-1].length()-1 );
			for (int i=0; i<parts.length; i++)
				result[i] = parts[i].trim();
			return result;
		}

		public static int[] parseIntArray(String parameter) {
			String[] parts = parameter.split(",");
			int[] result = new int[parts.length];
//			if (result.length == 0)
//				return result;
////			parts[0] = parts[0].substring(1);
//			parts[result.length-1] = parts[result.length-1].trim();//substring(0 ,parts[result.length-1].length()-1 );
			for (int i=0; i<parts.length; i++)
				result[i] = Integer.parseInt(parts[i].trim());
			return result;
		}

		public static void verifyArraySizes(int[][] intArraysToVerify, int[] possibleArraySizes) throws ParseException {
			for (int i=0; i<intArraysToVerify.length; i++)
			{
				boolean verified = false;
				for (int j=0; j<possibleArraySizes.length; j++)
					if (possibleArraySizes[j] == intArraysToVerify[i].length)
						verified = true;
				if (!verified)
					throw new ParseException("int array " +intArraysToVerify[i] + " doesn't have enough parameters.");						
			}
			
		}

		public static void verifyArraySizes(String[][] stringArraysToVerify, int[] possibleArraySizes)
		throws ParseException {
			for (int i=0; i<stringArraysToVerify.length; i++)
			{
				boolean verified = false;
				for (int j=0; j<possibleArraySizes.length; j++)
					if (possibleArraySizes[j] == stringArraysToVerify[i].length)
						verified = true;
				if (!verified)
					throw new ParseException("String array " +stringArraysToVerify[i] + " doesn't have enough parameters.");						
			}
		}
		
}
