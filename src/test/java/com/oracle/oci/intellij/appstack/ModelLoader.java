package com.oracle.oci.intellij.appstack;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

//import com.oracle.oci.intellij.ui.appstack.models.;
import com.oracle.oci.intellij.common.Utils;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand.CommandFailedException;
import com.oracle.oci.intellij.ui.appstack.command.SetCommand;
import com.oracle.oci.intellij.ui.appstack.command.SetCommand.SetCommandResult;
import com.oracle.oci.intellij.ui.appstack.models.Application;
import com.oracle.oci.intellij.ui.appstack.models.Application_Configuration_SSL_Communication;
import com.oracle.oci.intellij.ui.appstack.models.Application_Performance_Monitoring;
import com.oracle.oci.intellij.ui.appstack.models.Application_URL;
import com.oracle.oci.intellij.ui.appstack.models.Container_Instance_Configuration;
import com.oracle.oci.intellij.ui.appstack.models.Database;
import com.oracle.oci.intellij.ui.appstack.models.General_Configuration;
import com.oracle.oci.intellij.ui.appstack.models.Hide_constants_and_internal_variables;
import com.oracle.oci.intellij.ui.appstack.models.Network;
import com.oracle.oci.intellij.ui.appstack.models.Other_Parameters;
import com.oracle.oci.intellij.ui.appstack.models.Stack_Authentication;
import com.oracle.oci.intellij.ui.appstack.models.VariableGroup;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

public class ModelLoader {

	private JsonObject testData;
	private List<Class<? extends VariableGroup>> vgClasses;
	private Map<String, Class<? extends VariableGroup>> variableGroupsByVarName;
	private Map<String, PropertyDescriptor> variablesByName;

	public static void main(String[] args) throws Exception {

	  new ModelLoader().loadTestVariables();
	}
	
	public Map<String, String> loadTestVariables() throws Exception {
	   ModelLoader loader = new ModelLoader();
	   return loader.loadAndIndex(loader);

	}

	private Map<String, String> loadAndIndex(ModelLoader loader) throws Exception {
		Map<String, VariableGroup> variableGroupByVarName = loader.init().build();
		Map<String, String> variables = new HashMap<>();
		
		for (Map.Entry<String, VariableGroup> entry : variableGroupByVarName.entrySet()) {
			try {
			  String name = entry.getKey();
			  VariableGroup vg = entry.getValue();

 			if (vg != null) {
 			    PropertyDescriptor pd = loader.variablesByName.get(name);
					Object propertyValue = Utils.getPropertyValue(vg, pd);
					Optional.ofNullable(propertyValue).ifPresent(p -> variables.put(name, p.toString()));
				} else {
					System.out.println("Missing target: "+name);
				}
			} catch (CommandFailedException e) {
				throw new RuntimeException(e);
			}
		}

		variables.forEach((key, value) -> System.out.printf("%s = %s\n", key, value));
		return variables;
	}

	public ModelLoader() {

	}

	public ModelLoader init() throws Exception {
		loadModel("com.oracle.appstack.test.model");
		loadTestDataFromCP("testdata2.json");
		return this;
	}

	public Map<String, VariableGroup> build() throws Exception {
		Map<String, VariableGroup> populatedGroups = new LinkedHashMap<String, VariableGroup>();

		JsonObject variables = this.testData.getJsonObject("variables");
		for (Map.Entry<String, JsonValue> testDataEntry : variables.entrySet()) {
			String varName = testDataEntry.getKey();
			Class<? extends VariableGroup> varGroupClass = variableGroupsByVarName.get(varName);
			if (varGroupClass == null) {
				System.out.println("No vargroup for: " + varName);
				continue;
			}
			VariableGroup varGroup = populatedGroups.get(varName);
			if (varGroup == null) {
				try {
				  
					Constructor<? extends VariableGroup> constructor = varGroupClass.getConstructor();
					varGroup = constructor.newInstance(new Object[0]);
					assert varGroup != null && varName != null;
					populatedGroups.put(varName, varGroup);
					
				} catch (InstantiationException | IllegalAccessException e) {
					throw new Exception("TODO");
				}
			}
			JsonValue value = testDataEntry.getValue();
			PropertyDescriptor pd = this.variablesByName.get(varName);
			ValueType valueType = value.getValueType();
			SetCommand<VariableGroup, ?> command = null;
			switch (valueType) {
			case ARRAY:
			case OBJECT:
				System.out.println("Array/Object");
				break;
			case FALSE:
			  command = new SetCommand<VariableGroup, Boolean>(varGroup, pd, Boolean.FALSE);
				break;
			case TRUE:
        command = new SetCommand<VariableGroup, Boolean>(varGroup, pd, Boolean.TRUE);
				break;
			case NULL:
				System.out.println("NULL");
				break;
			case NUMBER: {
				JsonNumber jsonNumber = (JsonNumber) value;
				int val = jsonNumber.intValue();
				command = new SetCommand<VariableGroup, Integer>(varGroup, pd, Integer.valueOf(val));
			}
				break;
			case STRING: {
				JsonString jsonString = (JsonString) value;
				String val = jsonString.getString();
				command = new SetCommand<VariableGroup, String>(varGroup, pd, val);

			}
				break;
			}
			if (command != null) {
				SetCommandResult<?, ?> result = (SetCommandResult<?, ?>) command.execute();
				System.out.println("Result for "+varName+ " "+result.toString());
			}
			else {
			  System.out.println("Missing command:" +varName);
			}
		}
		return populatedGroups;
	}

	private void loadModel(String string) throws IntrospectionException {
		vgClasses = new ArrayList<>();
    vgClasses.add(Application_Configuration_SSL_Communication.class);
		vgClasses.add(Application_Performance_Monitoring.class);
		vgClasses.add(Application_URL.class);
		vgClasses.add(Application.class);
		vgClasses.add(Container_Instance_Configuration.class);
		vgClasses.add(Database.class);
		vgClasses.add(General_Configuration.class);
		vgClasses.add(Hide_constants_and_internal_variables.class);
		vgClasses.add(Network.class);
		vgClasses.add(Other_Parameters.class);
		vgClasses.add(Stack_Authentication.class);

		variableGroupsByVarName = new HashMap<>();
		variablesByName = new HashMap<>();

		for (Class<? extends VariableGroup> vgClass : this.vgClasses) {
			BeanInfo beanInfo = Introspector.getBeanInfo(vgClass);
			PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
			for (PropertyDescriptor pd : propertyDescriptors) {
			  String name = pd.getName();
        if ("class".equals(name)) {
			    continue; // ignore the built-in class variable.
			  }
        variablesByName.put(name, pd);
				variableGroupsByVarName.put(name, vgClass);
				System.out.printf("varName %s, property %s, varGroup %s\n", 
				   name, pd.getReadMethod(), vgClass.getName());
			}
		}
	}

	private void loadTestDataFromCP(String resourcePath) throws Exception {
		InputStream jsonStream = ModelLoader.class.getClassLoader().getResourceAsStream(resourcePath);
		testData = loadJson(jsonStream);
		// jsonObject.getJsonObject("variables");
		// System.out.println(variables);

	}

	public JsonObject loadJson(InputStream jsonStream) throws Exception {
		JsonArray read = Json.createReader(jsonStream).readArray();
		JsonValue jsonValue = read.get(0);
		if (jsonValue instanceof JsonObject) {
			JsonObject jsonObject = jsonValue.asJsonObject();
			return jsonObject;
		}
		throw new Exception("Failed to load json data");
	}
}
