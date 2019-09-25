package config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ReadConfig {

	public String[] GetConfigParams()
	{
		String file = "config.csv";
		InputStream s = this.getClass().getResourceAsStream(file);
		BufferedReader buffReader = new BufferedReader(new InputStreamReader(s));
		
		try 
		{
			String line = buffReader.readLine();
			String[] params;
			params = line.split(";");
			if(params.length != 3)
			{
				throw new IOException("Illegal number of parameters");
			}
			return params;
		} 
		catch (IOException e) 
		{
			System.err.println("Error while reading config file: " + e.getMessage());
			return null;
		}
	}
}
