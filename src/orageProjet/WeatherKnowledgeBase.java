package orageProjet;



import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.update.GraphStore;
import org.apache.jena.update.GraphStoreFactory;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.util.FileManager;
/**The JenaFamily class is for manipulating and doing inference the data in TDB dataset.
 * The Tbox (schema, reasoner, rules, etc.) is set up in the local memory
 * The Abox (instances of data) is set up in the Jena TDB dataset
 * @author Chao CHEN
 */


public class WeatherKnowledgeBase {
	private String directory;
	private List<Rule> rules;
	private Model model, data, deductionData;
	private Reasoner boundReasoner, rulesReasoner;
	private InfModel infmodel;
	private Dataset dataset;
	private static Scanner sc;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WeatherKnowledgeBase weatherKnowledgeBaseTDBTest = new WeatherKnowledgeBase("weatherTest");
		//weatherKnowledgeBaseTDBTest.initFamily(); 
		DateFormat df = new SimpleDateFormat("MM-dd-yyyy");

		// Get the date today using Calendar object.
		Date today = Calendar.getInstance().getTime();        
		// Using DateFormat format method we can create a string 
		// representation of a date with the defined format.
		String reportDate = df.format(today);
		String id_Temperature = "Temperature"+reportDate;
		String id_Wind = "Wind"+reportDate;
		String id_Precipitation = "Precipitation"+reportDate;
		weatherKnowledgeBaseTDBTest.addTemperature(id_Temperature, "20");
		weatherKnowledgeBaseTDBTest.addWind(id_Wind, "20", "339.5");
		weatherKnowledgeBaseTDBTest.addPrecipitation(id_Precipitation, "5.0");
		weatherKnowledgeBaseTDBTest.addWeatherstate("Weatherstate"+reportDate, id_Temperature, id_Wind, id_Precipitation);
		//Manipulate data in TDB dataset:
		Model resource = null;
		weatherKnowledgeBaseTDBTest.createInferenceInLocalModel();
		/*try { // chose the data in the Abox 
			int option = readAboxOption();
			if (option == 1) {
				resource = weatherKnowledgeBaseTDBTest.getOriginalData();
				
			} else if (option == 2) {
				resource = weatherKnowledgeBaseTDBTest.getDeductionData();
				
			} else if(option == 3) {
				resource = weatherKnowledgeBaseTDBTest.getAllData();		
			} else {
				System.out.println("Option is wrong! ");
			}
			
		} catch (IOException e){
			e.printStackTrace();
		}*/
	
		System.out.println(weatherKnowledgeBaseTDBTest.searchThunderStrom(weatherKnowledgeBaseTDBTest.getAllData()));
}
	public WeatherKnowledgeBase(String datasetName) {
		this.directory = datasetName;
		//dataset = TDBFactory.createDataset(directory);
		data = ModelFactory.createDefaultModel();
		this.model = ModelFactory.createDefaultModel();
		this.model.read("https://www.auto.tuwien.ac.at/downloads/thinkhome/ontology/WeatherOntology.owl");
		addRulesFile("newRules.rules");
	}
	
	public void addModel(String url) throws FileNotFoundException {
		//Add new ontology file to local model (create Tbox)		
		System.out.println(url);
		model.read(url);
		
		//write model into a file
		//FileOutputStream out = new FileOutputStream("result/model.owl");
		//model.write(out); 
	}
	
	public void usingSPARQLFromTDBbyModel(String s, Model resource) {
		String queryString = s;
		if(queryString.equals("")) {
			//queryString = "SELECT ?x WHERE {?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://www.auto.tuwien.ac.at/downloads/thinkhome/ontology/WeatherOntology.owl#Thunderstorm>}";
			queryString = "SELECT ?x ?y ?z WHERE {?x ?y ?z}";
		}
		System.out.println("SPARQL query is: " + queryString);
		
		Query query = QueryFactory.create(queryString);
		//dataset.begin(ReadWrite.READ);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, resource)) {
			long startTime = System.currentTimeMillis();
			ResultSet results = qexec.execSelect();
			long endTime = System.currentTimeMillis();
			System.out.println("time of excuting query is : " + (endTime-startTime) + "ms");
			System.out.println("After Quering by SPARQL, result is: ");
			
			//write result into a file
			//FileOutputStream outResult = new FileOutputStream("result/testQueryResultOfTDBWithInfModelDataset.owl");
			//ResultSetFormatter.out(outResult, results, query);
			
			//print result 
			ResultSetFormatter.out(results);
			//System.out.println(ResultSetFormatter.toList(results));
			
			qexec.close();
			System.out.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>query ended<<<<<<<<<<<<<<<<<<<<<");
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			//dataset.end();
		}
		
	}
	
	public Boolean searchThunderStrom(Model resource) {
		Boolean isThunderStorm = false;
		String queryString = "SELECT ?x WHERE {?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://www.auto.tuwien.ac.at/downloads/thinkhome/ontology/WeatherOntology.owl#Thunderstorm>}";

		System.out.println("SPARQL query is: " + queryString);
		
		Query query = QueryFactory.create(queryString);
		//dataset.begin(ReadWrite.READ);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, resource)) {
			long startTime = System.currentTimeMillis();
			ResultSet results = qexec.execSelect();
			long endTime = System.currentTimeMillis();
			System.out.println("time of excuting query is : " + (endTime-startTime) + "ms");
			System.out.println("After Quering by SPARQL, result is: ");
			
			//write result into a file
			//FileOutputStream outResult = new FileOutputStream("result/testQueryResultOfTDBWithInfModelDataset.owl");
			//ResultSetFormatter.out(outResult, results, query);
			
			//print result
			if(results.hasNext()) {
				ResultSetFormatter.out(results);
				isThunderStorm = true;
			}
			//System.out.println(ResultSetFormatter.toList(results));
			
			qexec.close();
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>query ended<<<<<<<<<<<<<<<<<<<<<");
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
			return isThunderStorm;
		
	}
	
	public void addRulesFile(String rulesFile) {
		//Add new rules from file		
		String newWeatherRules = WeatherKnowledgeBase.class.getClassLoader().getResource(rulesFile).getPath();//path of Weather Rules
		List<Rule> newRules = Rule.rulesFromURL(newWeatherRules);
		if (rules == null) {
			rules = newRules;
		} else {
			rules.addAll(newRules);
		}
	}
	
	public void addRulesReasoner() {	
		//create a reasoner by rules and add the reasoner into infmodel
		//this.infmodel.getDeductionsModel().write(System.out,"RDF/XML");
		System.out.println(rules);
		
		GenericRuleReasoner rulesReasoner = new GenericRuleReasoner(rules);
		//System.out.print(rulesReasoner.toString());
		rulesReasoner.setOWLTranslation(true);
		rulesReasoner.setTransitiveClosureCaching(true);
		rulesReasoner.bindSchema(model);
		
		this.infmodel = ModelFactory.createInfModel(rulesReasoner, this.data); //create new inference by adding new rules
		//this.infmodel.getDeductionsModel().write(System.out,"RDF/XML");
	}
	
	public void createInferenceInLocalModel() {
		try { 
			long startTime = System.currentTimeMillis();
			addRulesReasoner(); // add rules reasoner
			deductionData = this.infmodel.getDeductionsModel();
			long endTime = System.currentTimeMillis();
			System.out.println("time of creating infmodel is : " + (endTime-startTime) +"ms");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			//dataset.end();
		}
	}

	
	public InfModel getInfModel() {
		return this.infmodel;
	}
	
	public Model getOriginalData() { //get data created by users
		return this.data;
	}
	
	public Model getDeductionData() { //get data created by inference
		return deductionData;
	}
	
	public Model getAllData() { // get all data in the TDB dataset
		Model allData = ModelFactory.createDefaultModel();
		allData.add(data);
		allData.add(deductionData);
		return allData;
	}
	
/*
 * functions for oprating the dataset by SPARQL
 * */
	public void insertData(String insertString) { // operation for inserting data into model 
		try {
			UpdateRequest request = UpdateFactory.create(insertString) ;
		    UpdateProcessor proc = UpdateExecutionFactory.create(request, GraphStoreFactory.create(this.data));
		    proc.execute();
		    
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			//dataset.commit();
			//dataset.end(); 
		}
	}
	
	
	public void addTemperature(String name, String value) { //added a male member of family
		String insertString = "";
		try {
			insertString = FileUtil.readFile("add_temperature.sparql");
			insertString = insertString.replaceAll("%%id_Temperature%%", name);
			insertString = insertString.replaceAll("%%value%%", value); 
			insertData(insertString);
			System.out.println("added Temperature " + name + ": " + value);
		    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addWind(String name, String speed, String direction) { //added a male member of family
		String insertString = "";
		try {
			insertString = FileUtil.readFile("add_wind.sparql");
			insertString = insertString.replaceAll("%%id_Wind%%", name);
			insertString = insertString.replaceAll("%%speed%%", speed);
			insertString = insertString.replaceAll("%%direction%%", direction); 
			insertData(insertString);
			System.out.println("added Wind " + name + " speed : " + speed + " direction : " + direction);
		    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addPrecipitation(String name, String intensity) { //added precipitation
		String insertString = "";
		try {
			insertString = FileUtil.readFile("add_precipitation.sparql");
			insertString = insertString.replaceAll("%%id_Precipitation%%", name);
			insertString = insertString.replaceAll("%%intensity%%", intensity);
			insertData(insertString);
			System.out.println("added Precipitation " + name + " speed : " + intensity );
		    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addWeatherstate(String name, String idTemperature, String idWind, String idPrecipitation) { //added weatherstate
		String insertString = "";
		try {
			insertString = FileUtil.readFile("add_weatherstate.sparql");
			insertString = insertString.replaceAll("%%id_weatherstate%%", name);
			insertString = insertString.replaceAll("%%id_Precipitation%%", idPrecipitation);
			insertString = insertString.replaceAll("%%id_Temperature%%", idTemperature);
			insertString = insertString.replaceAll("%%id_Wind%%", idWind);
			System.out.println(insertString);
			insertData(insertString);
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	public static int readAboxOption() throws IOException { //chose the query resource
        int option;  
        sc = new Scanner(System.in);  
        System.out.println("If you want to query in original data please input \'1\', input \'2\' to query in deduction data, input \'3\' to query in original data and deduction data:");  
        option=sc.nextInt();  
        return option;
    }

}