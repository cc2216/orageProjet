package orageProjet;

import java.io.*;
import java.net.*;

import java.awt.*;


public class Weather {
	public Weather() {
		
	}
	
	public String getInformation() {
		try {
		URL meteo = new URL("http://dataservice.accuweather.com/forecasts/v1/daily/1day/132536?apikey=8EUiQ2quASfX4IRz4gxkCximgZHd7zdY&details=true");
        URLConnection meteoCo = meteo.openConnection();
        BufferedReader in = new BufferedReader(
                                new InputStreamReader(
                                meteoCo.getInputStream()));
        String inputLine = "";
        String result = "";
        while ((inputLine = in.readLine()) != null) {
            result = result + inputLine;
        }
        in.close();
        return result;
	}
		catch(Exception e) {
			System.out.println("failed");
			return "";
		}
		
	}
	
	public String stormRisk(String s) {
		System.out.println(s);
		String s2 = s.substring(s.indexOf("ThunderstormProbability")+25, s.length());
		String res = s2.substring(0, s2.indexOf(","));
		System.out.println("Probability of storm : " + res);
		return "Probability of storm : " + res;
	}
	
	
	public String windDirectionDegrees(String s){
		String s2 = s.substring(s.indexOf("Degrees")+9, s.length());
		String res = s2.substring(0, s2.indexOf(","));
		System.out.println( "Direction of wind : " + res);
		return "Direction of wind : " + res;
	}
	
	public String windSpeedMiPerHour(String s){
		String s2 = s.substring(s.indexOf("Speed")+16, s.length());
		String res = s2.substring(0, s2.indexOf(","));
		System.out.println( "Speed of wind : " + res);
		return "Speed of wind : " + res;
	}
	
	
	public double GetMinimumTemp(String s) {
		String s2 = s.substring(s.indexOf("Minimum")+18, s.length());
		String res = s2.substring(0, s2.indexOf(","));
		System.out.println( "Minimum temp (�F) : " + res);
		return Double.parseDouble(res);
	}
	
	public double GetMaximumTemp(String s) {
		String s2 = s.substring(s.indexOf("Maximum")+18, s.length());
		String res = s2.substring(0, s2.indexOf(","));
		System.out.println( "Maximum temp (�F) : " + res);
		return Double.parseDouble(res);
	}
	
	
	public String GetMeanTemperature(String s) {
		double min = this.GetMinimumTemp(s);
		double max = this.GetMaximumTemp(s);
		double mean = (min + max)/2;
		System.out.println( "Mean temp (�F) : " + mean);
		return "Mean temp (�F) : " + Double.toString(mean);
	}
	
	public String GetRainInch(String s) {
		String s2 = s.substring(s.indexOf("Rain\":")+15, s.length());
		String res = s2.substring(0, s2.indexOf(","));
		System.out.println( "Rain (inches) : " + res);
		return "Rain (inches) : " + res;
	}
	
	public static void main(String args[]) {
		try{
			Weather met = new Weather();
			String storm = met.getInformation();
			met.stormRisk(storm);
			met.windDirectionDegrees(storm);
			met.windSpeedMiPerHour(storm);
			met.GetMeanTemperature(storm);
			met.GetRainInch(storm);
		}
		catch(Exception e) {
		}
	}
	
}
