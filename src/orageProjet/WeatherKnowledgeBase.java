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
		weatherKnowledgeBaseTDBTest.addTemperature("Temperature"+reportDate, "20");
		weatherKnowledgeBaseTDBTest.addWind("Wind"+reportDate, "20", "339.5");
		//Manipulate data in TDB dataset:
		Model resource = null;
		weatherKnowledgeBaseTDBTest.createInferenceInLocalModel();
		//weatherKnowledgeBaseTDBTest.addInfResultToTDB(weatherKnowledgeBaseTDBTest.getInfModel(),"deductionData");
		try { // chose the data in the Abox 
			System.out.println("here??????????");
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
		}
		//familyTDBTest.listFamilyMembers(resource); 
		//familyTDBTest.findChild("John", resource);
		//familyTDBTest.findHusband("Lisa",resource);
		weatherKnowledgeBaseTDBTest.usingSPARQLFromTDBbyModel("",weatherKnowledgeBaseTDBTest.getInfModel());
		//weatherKnowledgeBaseTDBTest.listAllData(resource);
}
	public WeatherKnowledgeBase(String datasetName) {
		this.directory = datasetName;
		dataset = TDBFactory.createDataset(directory);
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
			queryString = "SELECT ?y ?z WHERE {<https://www.auto.tuwien.ac.at/downloads/thinkhome/ontology/WeatherOntology.owl#Wind02-27-2018>   ?y ?z}";
		}
		System.out.println("SPARQL query is: " + queryString);
		
		Query query = QueryFactory.create(queryString);
		dataset.begin(ReadWrite.READ);
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
			dataset.end();
		}
		
	}
	
	public void addRulesFile(String rulesFile) {
		//Add new rules from file		
		String newFamilyRules = WeatherKnowledgeBase.class.getClassLoader().getResource(rulesFile).getPath();//path of family rules
		List<Rule> newRules = Rule.rulesFromURL(newFamilyRules);
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
		System.out.print(rulesReasoner.toString());
		rulesReasoner.setOWLTranslation(true);
		rulesReasoner.setTransitiveClosureCaching(true);
		rulesReasoner.bindSchema(model);
		
		this.infmodel = ModelFactory.createInfModel(rulesReasoner, this.data); //create new inference by adding new rules
		this.infmodel.getDeductionsModel().write(System.out,"RDF/XML");
	}
	
	public void createInferenceInLocalModel() {
		//create inference model by adding OWL reasoner and rules reasoner
		Reasoner reasoner = ReasonerRegistry.getOWLReasoner(); //using OWL Reasoner
		dataset.begin(ReadWrite.READ);
		data = dataset.getNamedModel("data");
		try { 
			long startTime = System.currentTimeMillis();
			//boundReasoner = reasoner.bindSchema(model);
			//this.infmodel = ModelFactory.createInfModel(boundReasoner,data);
			addRulesReasoner(); // add rules reasoner
			long endTime = System.currentTimeMillis();
			System.out.println("time of creating infmodel is : " + (endTime-startTime) +"ms");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			dataset.end();
		}
	}
	
	
	
	public void addInfResultToTDB(InfModel inf, String dataName) {
		//add deduction result to a model of TDB dataset 
		dataset.begin(ReadWrite.WRITE) ;
		deductionData = dataset.getNamedModel(dataName);
		if(!deductionData.isEmpty()) { // if the model is not empty, clean it up
			deductionData.removeAll();
		}
		try {
			deductionData.add(inf.getDeductionsModel());		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			dataset.commit();
			dataset.end(); 
		}
	}
	
	public InfModel getInfModel() {
		return this.infmodel;
	}
	
	public Model getOriginalData() { //get data created by users
		return this.data;
	}
	
	public Model getDeductionData() { //get data created by inference
		return this.deductionData;
	}
	
	public Model getAllData() { // get all data in the TDB dataset
		dataset.begin(ReadWrite.READ);
		Model allData = dataset.getUnionModel();
		dataset.end();
		return allData;
	}
	
/*
 * functions for oprating the dataset by SPARQL
 * */
	public void insertData(String insertString) { // operation for inserting data into model of TDB dataset
		dataset.begin(ReadWrite.WRITE) ;
		data = dataset.getNamedModel("data");
		
		try {
			GraphStore graphStore = GraphStoreFactory.create(data);
			UpdateRequest request = UpdateFactory.create(insertString) ;
		    UpdateProcessor proc = UpdateExecutionFactory.create(request, graphStore);
		    proc.execute();
		    
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			dataset.commit();
			dataset.end(); 
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
	
	
	
	
	public static int readAboxOption() throws IOException { //chose the query resource
        int option;  
        sc = new Scanner(System.in);  
        System.out.println("If you want to query in original data please input \'1\', input \'2\' to query in deduction data, input \'3\' to query in original data and deduction data:");  
        option=sc.nextInt();  
        return option;
    }

}