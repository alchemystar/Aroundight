/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.lancelot.loader;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @Author lizhuyang
 */
public class XmlLoader {
    public final static String DEFAULT_XML = "/Users/alchemystar/mycode/Lancelot/src/resource/rule.xml";

    private static Map<String, TableRuleConfig> tableRules;
    private static Set<String> hitColumns = new HashSet<String>();

    public static void load() {
        load(DEFAULT_XML);
    }

    public static void load(String xmlFile) {
        InputStream xml = null;
        try {
            xml = new FileInputStream(xmlFile);
            Element root = ConfigUtil.getDocument(xml).getDocumentElement();
            loadTableRules(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadTableRules(Element root) {
        NodeList list = root.getElementsByTagName("tableRule");
        tableRules = new HashMap<String, TableRuleConfig>();
        for (int i = 0, n = list.getLength(); i < n; ++i) {
            Node node = list.item(i);
            if (node instanceof Element) {
                Element e = (Element) node;
                String name = e.getAttribute("name");
                if (tableRules.containsKey(name)) {
                    throw new RuntimeException("table rule " + name + " duplicated!");
                }
                String cols = e.getElementsByTagName("columns").item(0).getTextContent();
                String[] columns = cols.split(",");
                for(int k=0;k<columns.length;k++){
                    hitColumns.add(columns[k]);
                }
                String tableName = e.getElementsByTagName("tableName").item(0).getTextContent();
                String dbRuleList = e.getElementsByTagName("dbRender").item(0).getTextContent();
                String tbRuleList = e.getElementsByTagName("tbRender").item(0).getTextContent();
                TableRuleConfig tableRuleConfig = new TableRuleConfig(columns, tableName, dbRuleList, tbRuleList);
                tableRules.put(name, tableRuleConfig);
            }

        }
    }

    public static Map<String, TableRuleConfig> getTableRules() {
        return tableRules;
    }

    public static void setTableRules(
            Map<String, TableRuleConfig> tableRules) {
        XmlLoader.tableRules = tableRules;
    }

    public static Set<String> getHitColumns() {
        return hitColumns;
    }

    public static void setHitColumns(Set<String> hitColumns) {
        XmlLoader.hitColumns = hitColumns;
    }

    public static void main(String[] args) {
        XmlLoader loader = new XmlLoader();
        loader.load();
        System.out.println(loader.tableRules);
    }
}
