package com.oracle.oci.intellij.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.junit.jupiter.api.Assertions.*;

class SystemPropertiesTest {
  @BeforeEach
  void setUp(){
    // Nothing to setup.
  }

  @AfterEach
  void tearDown(){
    // Nothing to clean.
  }

  @Test
  void lineSeparator(){
    // TODO:
    assertTrue(false);
  }

  @Test
  void userHome(){
    // TODO:
    assertTrue(false);
  }

  @EnabledOnOs({OS.WINDOWS})
  @Test
  void isWindows(){
    assertAll(
            () -> assertTrue(SystemProperties.isWindows()),
            () -> assertFalse(SystemProperties.isMac()),
            () -> assertFalse(SystemProperties.isLinux())
    );
  }

  @EnabledOnOs({OS.MAC})
  @Test
  void isMac(){
    assertAll(
            () -> assertTrue(SystemProperties.isMac()),
            () -> assertFalse(SystemProperties.isWindows()),
            () -> assertFalse(SystemProperties.isLinux())
    );
  }

  @EnabledOnOs({OS.LINUX})
  @Test
  void isLinux(){
    assertAll(
            () -> assertTrue(SystemProperties.isLinux()),
            () -> assertFalse(SystemProperties.isMac()),
            () -> assertFalse(SystemProperties.isWindows())
    );
  }
}