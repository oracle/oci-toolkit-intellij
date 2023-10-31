package com.oracle.oci.intellij.common;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import com.oracle.oci.intellij.common.command.AbstractBasicCommand.CommandFailedException;

public class Utils {

	public static String GetBase64EncodingForAFile(String filePath) throws IOException {
		byte[] fileData = Files.readAllBytes(Paths.get(filePath));
		byte[] fileDataBase64Encoded = Base64.getEncoder().encode(fileData);
		return new String(fileDataBase64Encoded, StandardCharsets.UTF_8);
	}

	public static Object getPropertyValue(Object target, PropertyDescriptor pd) throws CommandFailedException {
		Method readMethod = pd.getReadMethod();
		try {
			Object curValue = readMethod.invoke(target);
			return curValue;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			System.out.printf("%s, %s\n", target.getClass(), pd.getPropertyType());
			throw new CommandFailedException(e);
		}
	}
	
	public static Object setPropertyValue(Object target, PropertyDescriptor pd, Object newValue) throws CommandFailedException {
		Method writeMethod = pd.getWriteMethod();
		Object oldValue;
		try {
			oldValue = getPropertyValue(target, pd);
			writeMethod.invoke(target, newValue);
			return oldValue;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new CommandFailedException(e);
		}
	}
}
