package ru.trein.gui.util;

import ru.trein.gui.nodes.RemoteRepoTreeItem;


public class ResponseParser {

    public static RemoteRepoTreeItem parseMLSD(String line) {
        String type;
        long modify;
        int size;
        String name;

        String[] attributes = line.split(";");

        type = attributes[0].replace("type=", "");
        modify = Long.parseLong(attributes[1].replace("modify=", ""));

        if (attributes.length == 3) {
            name = attributes[2].trim();
            return new RemoteRepoTreeItem(type, modify, name);
        } else if(attributes.length == 4) {
            size = Integer.parseInt(attributes[2].replace("size=", ""));
            name = attributes[3].trim();
            return new RemoteRepoTreeItem(type, modify, size, name);
        } else {
            throw new RuntimeException("So many attributes;");
        }
    }
}
