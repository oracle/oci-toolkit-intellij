package com.oracle.oci.intellij.appstack;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.oracle.oci.intellij.ui.appstack.command.SetCommand;
import com.oracle.oci.intellij.ui.appstack.command.SetCommand.SetCommandResult;
import com.oracle.oci.intellij.appStackGroup.models.Application;
import com.oracle.oci.intellij.appStackGroup.models.Application_Performance_Monitoring;
import com.oracle.oci.intellij.appStackGroup.models.Application_URL;
import com.oracle.oci.intellij.appStackGroup.models.Application_configuration_SSL_communication_between_backends_and_load_balancer;
import com.oracle.oci.intellij.appStackGroup.models.Container_instance_configuration;
import com.oracle.oci.intellij.appStackGroup.models.Database;
import com.oracle.oci.intellij.appStackGroup.models.General_Configuration;
import com.oracle.oci.intellij.appStackGroup.models.Hide_constants_and_internal_variables;
import com.oracle.oci.intellij.appStackGroup.models.Network;
import com.oracle.oci.intellij.appStackGroup.models.Other_parameters;
import com.oracle.oci.intellij.appStackGroup.models.Stack_authentication;
import com.oracle.oci.intellij.appStackGroup.models.VariableGroup;
import com.oracle.oci.intellij.common.Utils;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand.CommandFailedException;

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

		ModelLoader loader = new ModelLoader();
		loader.loadAndIndex(loader);
	}

	private void loadAndIndex(ModelLoader loader) throws Exception {
		Map<String, VariableGroup> variableGroupByVarName = loader.init().build();
		Map<String, String> variables = new HashMap<>();
		loader.variablesByName.forEach((name, pd) -> {
			try {
				VariableGroup target = variableGroupByVarName.get(name);
				if (target != null) {
					Object propertyValue = Utils.getPropertyValue(target, pd);
					Optional.ofNullable(propertyValue).ifPresent(p -> variables.put(name, p.toString()));
				} else {
					System.out.println("Missing property: "+name);
				}
			} catch (CommandFailedException e) {
				throw new RuntimeException(e);
			}
		});

		variables.forEach((key, value) -> System.out.printf("%s = %s\n", key, value));
	}

	public ModelLoader() {

	}

	public ModelLoader init() throws Exception {
		loadModel("com.oracle.appstack.test.model");
		loadTestDataFromCP("/com/oracle/appstack/test/resources/testdata.json");
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
					varGroup = varGroupClass.newInstance();
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
				System.out.println("FALSE");
				break;
			case TRUE:
				System.out.println("FALSE");
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
				System.out.println(result.toString());
			}
		}
		return populatedGroups;
	}

	private void loadModel(String string) throws IntrospectionException {
		vgClasses = new ArrayList<>();
		vgClasses.add(Application_configuration_SSL_communication_between_backends_and_load_balancer.class);
		vgClasses.add(Application_Performance_Monitoring.class);
		vgClasses.add(Application_URL.class);
		vgClasses.add(Application.class);
		vgClasses.add(Container_instance_configuration.class);
		vgClasses.add(Database.class);
		vgClasses.add(General_Configuration.class);
		vgClasses.add(Hide_constants_and_internal_variables.class);
		vgClasses.add(Network.class);
		vgClasses.add(Other_parameters.class);
		vgClasses.add(Stack_authentication.class);

		variableGroupsByVarName = new HashMap<>();
		variablesByName = new HashMap<>();

		for (Class<? extends VariableGroup> vgClass : this.vgClasses) {
			BeanInfo beanInfo = Introspector.getBeanInfo(vgClass);
			PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
			for (PropertyDescriptor pd : propertyDescriptors) {
				variablesByName.put(pd.getName(), pd);
				variableGroupsByVarName.put(pd.getName(), vgClass);
			}
		}
	}

	private void loadTestDataFromCP(String resourcePath) throws Exception {
		InputStream jsonStream = ModelLoader.class.getResourceAsStream(resourcePath);
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
