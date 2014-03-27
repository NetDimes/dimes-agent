package dimes.util;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import dimes.platform.PlatformDependencies;

/*
 * Created on 22/07/2004
 */

/**
 * @author anat
 */
public class JarExtractor
{
	static String MANIFEST = "META-INF/MANIFEST.MF";

	private ExtractedFileFilter filter = new ExtractedFileFilter();

	public Vector extract(String jarName, String dest)
	{
		return extract(jarName, dest, true);
	}

	/*
	 * don't overwrite - if file already exists, rename new version to "later" name.
	 * For example, if, when extracting a file named bla, there exists another file bla, rename
	 * new file to bla_currentTime. This way, the names are unique and always progress with time. 
	 */
	public Vector extract(String jarName, String dest, boolean addTimeStampExtension)
	{
		Vector extractedFiles = new Vector();
		// DONT LOG USING LOGGERS : in this stage propertiesBean isn't
		// loaded by the class loader yet...
		System.out.println("Extracting " + jarName + " to " + dest);//debug
		File currentArchive = new File(jarName);
		File outputDir = new File(dest);
		if (!outputDir.exists())
			outputDir.mkdirs();

		byte[] buf = new byte[1024];

		JarFile jar = null;
		FileOutputStream out = null;
		InputStream in = null;

		try
		{
			jar = new JarFile(currentArchive);
			int size = jar.size();

			Enumeration entries = jar.entries();
			for (int i = 0; i < size; i++)
			{
				JarEntry entry = (JarEntry) entries.nextElement();

				String pathname = entry.getName();

				if (!this.filter.accept(null, pathname))
					continue; //entry was filtered

				File outFile;
				if (entry.isDirectory())
				{
					outFile = new File(outputDir, pathname);
					if (!outFile.exists())
						outFile.mkdirs();
					continue;
				}
				else
				{
					int extIndex = pathname.lastIndexOf('.');
					String name = pathname;
					String extension = "";
					if (extIndex >= 0)
					{
						name = pathname.substring(0, extIndex);
						extension = pathname.substring(extIndex);
					}
					if (addTimeStampExtension)
						outFile = new File(outputDir, name + "_" + String.valueOf(System.currentTimeMillis()) + extension);
					else
						outFile = new File(outputDir, name + extension);
				}

				in = jar.getInputStream(entry);
				File parent = new File(outFile.getParent());
				if (parent != null && !parent.exists())
					parent.mkdirs();

				out = new FileOutputStream(outFile);

				for (int nRead = in.read(buf, 0, buf.length); nRead > 0; nRead = in.read(buf, 0, buf.length))
					out.write(buf, 0, nRead);

				out.close();
				outFile.setLastModified(entry.getTime());
				extractedFiles.add(outFile);
			}

			if (in != null)
				in.close();
			jar.close();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (jar != null)
					jar.close();
				if (out != null)
					out.close();
				if (in != null)
					in.close();
			}
			catch (IOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		return extractedFiles;
	}

	/**
	 * @param theExcludeFilters The excludeFilters to set.
	 */
	public void setExcludeFilters(String[] theExcludeFilters)
	{
		this.filter.setExcludeFilters(theExcludeFilters);
	}

	/**
	 * @param theIncludeFilters The includeFilters to set.
	 */
	public void setIncludeFilters(String[] theIncludeFilters)
	{
		this.filter.setIncludeFilters(theIncludeFilters);
	}

	public boolean addExcludeFilter(String anExcludeFilter)
	{
		return this.filter.addExcludeFilter(anExcludeFilter);
	}

	public boolean addAllExcludeFilter(String[] theExcludeFilters)
	{
		return this.filter.addAllExcludeFilter(theExcludeFilters);
	}

	public boolean removeExcludeFilter(String anExcludeFilter)
	{
		return this.filter.removeExcludeFilter(anExcludeFilter);
	}

	public boolean addIncludeFilter(String anIncludeFilter)
	{
		return this.filter.addIncludeFilter(anIncludeFilter);
	}

	public boolean addAllIncludeFilter(String[] theIncludeFilters)
	{
		return this.filter.addAllIncludeFilter(theIncludeFilters);
	}

	public boolean removeIncludeFilter(String anIncludeFilter)
	{
		return this.filter.removeIncludeFilter(anIncludeFilter);
	}

	public String getContainingJarName()
	{
		String myClassName = this.getClass().getName();
		String myClassURL = myClassName.replaceAll("\\Q.\\E", "/") + ".class";
		URL urlJar = this.getClass().getClassLoader().getResource(myClassURL);
		String urlStr = urlJar.toString();
		int from = "jar:file:".length() + 1;
		int to = urlStr.indexOf("!/");
		String jarName = urlStr.substring(from, to);
		if (PlatformDependencies.os != PlatformDependencies.WINDOWS)
		{
			jarName = "/"+jarName;
		}
		return jarName;
	}

	/*
	 * ignores <dir>. treates <name> as if it were the complete path. 
	 * @author anat
	 */
	private class ExtractedFileFilter implements FilenameFilter
	{
		private HashSet includeFilters = new HashSet();
		private HashSet excludeFilters = new HashSet();

		/**
		 *  
		 */
		public ExtractedFileFilter()
		{
			this(new String[]{}, new String[]{"\\Q" + MANIFEST + "\\E"});
		}

		public ExtractedFileFilter(String[] include, String[] exclude)
		{
			this.includeFilters.clear();
			this.includeFilters.addAll(Arrays.asList(include));
			this.excludeFilters.clear();
			this.excludeFilters.addAll(Arrays.asList(exclude));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
		 */
		public boolean accept(File dir, String name)
		{
			boolean matched = false;
			Iterator iter = this.includeFilters.iterator();
			while (iter.hasNext())
			{
				String pattern = (String) iter.next();
				if (name.matches(pattern))
				{
					matched = true;
					break;
				}
			}
			if (!matched)//no need to see if excluded
				return false;
			iter = this.excludeFilters.iterator();
			while (iter.hasNext())
			{
				String pattern = (String) iter.next();
				if (name.matches(pattern))
					return false;
			}

			return true;
		}

		/**
		 * @param theExcludeFilters The excludeFilters to set.
		 */
		public void setExcludeFilters(String[] theExcludeFilters)
		{
			this.excludeFilters.clear();
			this.excludeFilters.addAll(Arrays.asList(theExcludeFilters));
		}

		/**
		 * @param theIncludeFilters The includeFilters to set.
		 */
		public void setIncludeFilters(String[] theIncludeFilters)
		{
			this.includeFilters.clear();
			this.includeFilters.addAll(Arrays.asList(theIncludeFilters));
		}

		public boolean addExcludeFilter(String anExcludeFilter)
		{
			return this.excludeFilters.add(anExcludeFilter);
		}

		public boolean addAllExcludeFilter(String[] theExcludeFilters)
		{
			return this.excludeFilters.addAll(Arrays.asList(theExcludeFilters));
		}

		public boolean removeExcludeFilter(String anExcludeFilter)
		{
			return this.excludeFilters.remove(anExcludeFilter);
		}

		public boolean addIncludeFilter(String anIncludeFilter)
		{
			return this.includeFilters.add(anIncludeFilter);
		}

		public boolean addAllIncludeFilter(String[] theIncludeFilters)
		{
			return this.includeFilters.addAll(Arrays.asList(theIncludeFilters));
		}

		public boolean removeIncludeFilter(String anIncludeFilter)
		{
			return this.includeFilters.remove(anIncludeFilter);
		}

	}
}