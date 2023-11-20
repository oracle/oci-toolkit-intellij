package com.oracle.oci.intellij.ui.appstack.test;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;

public class Appstack extends AnAction {

        public Appstack() {
            super("APP STACK");
        }

        @Override
        public void actionPerformed(AnActionEvent e) {

            try {
                com.oracle.oci.intellij.ui.appstack.test.YamlLoader.load();
            } catch (IntrospectionException | InvocationTargetException | IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            System.out.println("i am update ");

            super.update(e);
        }

}
