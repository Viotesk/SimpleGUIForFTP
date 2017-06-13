package ru.trein.gui.util;

import ru.trein.gui.nodes.Node;


public class ResponseParser {

    public static Node parseMLSD(String line, String parentPath) {
        boolean type;
        long modify;
        int size;
        String name;
        String separator = "/";


        String[] attributes = line.split(";");

        type = attributes[0].replace("type=", "").equals("dir");
        modify = Long.parseLong(attributes[1].replace("modify=", ""));

        if (attributes.length == 3) {
            name = attributes[2].trim();
            return new Node(type, modify, 0, name, parentPath + name + separator, null);
        } else if(attributes.length == 4) {
            size = Integer.parseInt(attributes[2].replace("size=", ""));
            name = attributes[3].trim();
//            return new Node(type, modify, size, name, parentPath + name, null);
            return new Node(type, modify, size, name, parentPath, null);
        } else {
            throw new RuntimeException("So many attributes;");
        }
    }
}
