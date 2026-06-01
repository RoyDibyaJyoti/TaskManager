package com.taskmanager;

public class Launcher {
    public static void main(String[] args) {
        System.setProperty("apple.awt.application.name", "TaskManager");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "TaskManager");
        App.main(args);
    }
}
