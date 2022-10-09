package is_lab1;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
   

public class Main {

	
	static HashMap<Integer, Color> colorByRow = new HashMap<Integer, Color>();
    static HashMap<Integer, Boolean> wasModified = new HashMap<Integer, Boolean>();

    
	static JTable table = new JTable() {
        public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
           Component comp = super.prepareRenderer(renderer, row, column);

           if(wasModified.get(row) != null && wasModified.get(row)) {
        	   comp.setBackground(Color.LIGHT_GRAY);
           }else if(colorByRow.get(row) != null) {
        	   comp.setBackground(colorByRow.get(row));
           }else {
        	   comp.setBackground(Color.YELLOW);
           }

           return comp;
        }
     };
     
     private static void setTableModelListener() {
    	 table.getModel().addTableModelListener(new TableModelListener() {
     		public void tableChanged(TableModelEvent e) {
     			Integer row = e.getFirstRow();
     			Integer column = e.getColumn();
     			System.out.println("row+column");
     			System.out.println(String.valueOf(row) + String.valueOf(column));
     			System.out.println("valueByCoordinate");

     			System.out.println(valueByCoordinate.get(String.valueOf(row) + String.valueOf(column)));
     			System.out.println("tableGetValueAt");
     			System.out.println(table.getValueAt(row, column));
     			if(!valueByCoordinate.get(String.valueOf(row) + String.valueOf(column)).equals(table.getValueAt(row, column))) {
     				wasModified.put(row,  true);
     			}else {
     				wasModified.put(row,  false);
     			}
     		}
   		});
     }
    
	
    static List < String > headers = Arrays.asList(
        "Producent",
        "wielkość matrycy",
        "typ matrycy",
        "czy dotykowy ekran",
        "procesor",
        "liczba rdzeni fizyczynch",
        "taktowanie",
        "RAM",
        "pojemność dysku",
        "typ dysku",
        "karta graficzna",
        "pamięć karty graficznej",
        "system operacyjny",
        "Napęd optyczny");
    static HashMap <String, String> headerTranslations = new HashMap<String,String>();
    static HashMap <String, JTextArea> textAreaByHeader = new HashMap<String, JTextArea>();

    private static void clearTable() throws SQLException{
    	connectToDb();
    	try {
    		Statement statement = con.createStatement();
            statement.executeUpdate("DELETE FROM jdbcdb.laptops");
   
        } catch (Exception ex) {
    		System.out.println("error");
            System.out.println(ex.getMessage());
        }
    }
    
    private static void saveAllLaptops() throws SQLException{
    	connectToDb();
    	clearTable();
    	try {

            TableModel model = table.getModel();
            
            Statement statement = con.createStatement();
            String query = "INSERT INTO jdbcdb.laptops (";
            for(String column : dbColumns) {
            	query += "" + column + ",";
            }
            query = query.substring(0, query.length() - 1);
            query += ") VALUES ";
            
            for (int i = 0; i < model.getRowCount(); i++) {
            	query += "\n(";
                for (int j = 0; j < model.getColumnCount(); j++) {
                	String value = (String) model.getValueAt(i,  j);
                	if(value == null) {
                		value = "";
                	}
                	query += "'" + value + "',";
                }
                query = query.substring(0, query.length() - 1);
                query += "),";
                
            }
            query = query.substring(0, query.length() - 1);
            query += ';';
            System.out.println(query);
            statement.executeUpdate(query);
   
        } catch (Exception ex) {
    		System.out.println("error");
            System.out.println(ex.getMessage());
        }
    }
    
    public static HashMap<String, String> valueByCoordinate = new HashMap<String, String>();
    private static void getAllLaptops() throws SQLException {
    	Integer howManyRecordsFound = 0;
    	Integer howManyDuplicatesFound = 0;
    	HashMap<String, Integer> duplicatesByKey = new HashMap<String, Integer>();
    	String query = "SELECT * FROM jdbcdb.laptops;";
    	connectToDb();
    	DefaultTableModel model = new DefaultTableModel();
    	
    	try {
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(query);
			System.out.println("Executing query all...");
			System.out.println(rs);
			for (String header: headers) {
                model.addColumn(header);
            }
			List<Vector<String>> rows = new ArrayList<Vector<String>>();
			Integer k = 0;
			while(rs.next()) {
				Vector<String> row = new Vector<String>();
				String key = "";
				howManyRecordsFound ++;
				Integer p = 0;
				for(String columnName : dbColumns) {
					System.out.println(columnName + ": " + rs.getString(columnName));
					row.add(rs.getString(columnName));
					key += rs.getString(columnName);
					valueByCoordinate.put(String.valueOf(k)+String.valueOf(p), rs.getString(columnName));
					p++;
				}
				if(duplicatesByKey.get(key) == null) {
					duplicatesByKey.put(key,  1);
				}else {
					duplicatesByKey.put(key, duplicatesByKey.get(key) + 1);
				}
				rows.add(row);
				//model.addRow(row);
				k++;
			}
			System.out.println("testing map");
			System.out.println(duplicatesByKey);
			
			colorByRow = new HashMap<Integer, Color>();
			Integer i = 0;
			for(Vector<String> row : rows) {
				System.out.println(row);
				String key = "";
				for(String rowString : row) {
					key += rowString;
				}
				System.out.println(key);

				
				if(duplicatesByKey.get(key) != null && duplicatesByKey.get(key) > 1) {
					colorByRow.put(i, Color.RED);
				}else {
					colorByRow.put(i, null);
				}
				i++;
				model.addRow(row);
			}
			
			Integer allDuplicates = 0;
			for(Integer value : duplicatesByKey.values()) {
				if(value > 1) {
					allDuplicates += value;
				}
			}
			duplicatesLabel.setText(duplicatesLabel.getText().replace("0", String.valueOf(allDuplicates)));
			allRecordsLabel.setText(allRecordsLabel.getText().replace("0", String.valueOf(howManyRecordsFound)));

			table.setModel(model);
			st.close();
			con.close();
		} catch (SQLException e1) {
			System.out.println(e1.getMessage());
			con.close();
		}
		
	}
	private static Connection con;
    private static void connectToDb() {
		try {
			System.out.println("Loading driver...");
			//Class.forName("com.mysql.jc.jdbc.Driver");
			System.out.println("Driver loaded.");
			try {
				con = DriverManager.getConnection("jdbc:mysql://localhost:3306/jdbcdb", "root", "root");
				System.out.println("connection checking.");
				System.out.println(con);
			} catch (SQLException e) {
				System.out.println(e.getMessage());
				System.out.println("SQLException");
			}
		} catch (Exception e1) {
			System.out.println("Class not found");
			System.out.println(e1.getMessage());
		}
	}
    
    
    static JLabel duplicatesLabel = new JLabel("Duplicates found in database: 0");
    static JLabel allRecordsLabel = new JLabel("All records found in database: 0");
    public static void main(String args[]) {
    	fillHeaderTranslations();
        JFrame frame = new JFrame("Integracja systemów - Ryszard Rogalski");

        JLabel selected_file = new JLabel();

        frame.setSize(1350, 800);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        //frame.setLayout(new GridLayout(2,2));

        JButton btn_textFile = new JButton("Select File (*.txt)");
        btn_textFile.setSize(100, 100);
        JButton btn_xmlFile = new JButton("Select File (*.xml)");
        btn_xmlFile.setSize(100, 100);

        JButton btn_save_textFile = new JButton("Save File (*.txt)");
        btn_save_textFile.setSize(100, 100);

        JButton btn_save_xmlFile = new JButton("Save File (*.xml)");
        btn_save_xmlFile.setSize(100, 100);
        
        JButton db_readFile = new JButton("Read File (JDBC)");
        db_readFile.setSize(100,100);
        
        JButton db_saveFile = new JButton("Save File (JDBC");
        db_saveFile.setSize(100,100);
        
        JButton clearDb = new JButton("Clear database");
        clearDb.setSize(100,100);


        
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setPreferredSize(new Dimension(1000, 50));
        buttonPanel.setMaximumSize(buttonPanel.getPreferredSize());
        buttonPanel.setMinimumSize(buttonPanel.getPreferredSize());

        buttonPanel.setLayout(new GridLayout(1, 4));
        buttonPanel.setSize(1920, 150);
        buttonPanel.setBorder(BorderFactory.createTitledBorder("Actions"));
        buttonPanel.add(btn_textFile);
        buttonPanel.add(btn_xmlFile);
        buttonPanel.add(btn_save_textFile);
        buttonPanel.add(btn_save_xmlFile);
        buttonPanel.add(db_saveFile);
        buttonPanel.add(db_readFile);
        buttonPanel.add(clearDb);

        frame.add(buttonPanel);
        

        
        

        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new GridLayout(5,3));
        crudPanel.setBorder(BorderFactory.createTitledBorder("Adding data"));
        
        for(Integer i = 0; i < headers.size() ; i++) {
        	String header = headers.get(i);
        	
        	JPanel tmpPanel = new JPanel();
        	tmpPanel.setLayout(new GridLayout(0, 1));
        	tmpPanel.setBorder(BorderFactory.createTitledBorder(header));
        	JTextArea tmpArea = new JTextArea();
        	textAreaByHeader.put(header,  tmpArea);
        	tmpPanel.add(tmpArea);
        	crudPanel.add(tmpPanel);
        }
        JButton crudButton = new JButton("Add data");
        crudButton.setBackground(Color.BLUE);
        crudButton.setOpaque(true);
        crudPanel.add(crudButton);
        frame.add(crudPanel);
        
        JPanel labelsPanel = new JPanel();
        labelsPanel.setLayout(new GridLayout(1,2));
        labelsPanel.setSize(1920, 150);
        labelsPanel.add(duplicatesLabel);
        labelsPanel.add(allRecordsLabel);
        frame.add(labelsPanel);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1));

        panel.setBorder(BorderFactory.createTitledBorder("Data"));

        JScrollPane sp = new JScrollPane(table);

        panel.add(sp);
        frame.add(panel);

        DefaultTableModel initModel = new DefaultTableModel();

        for (String header: headers) {
            initModel.addColumn(header);
        }
        table.setModel(initModel);


        panel.add(table.getTableHeader(), BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        
        
        
        db_saveFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	try {
					saveAllLaptops();
				} catch (SQLException e1) {
					System.out.println(e1.getMessage());
				}
            }
        });
        
        db_readFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	try {
					getAllLaptops();
					setTableModelListener();
				} catch (SQLException e1) {
					System.out.println(e1.getMessage());
				}
            }
        });
        
        clearDb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	try {
					clearTable();
				} catch (SQLException e1) {
					System.out.println(e1.getMessage());
				}
            }
        });
        
        crudButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	Vector<String> row = new Vector<String>();
            	System.out.println("crudButtonListener");
            	Boolean atLeastOneData = false;
            	for(String header : headers) {
            		JTextArea area = textAreaByHeader.get(header);
            		if(area.getText() != null && area.getText().trim() != "" && !area.getText().isEmpty()) {
            		
            			atLeastOneData = true;
            		}
            		System.out.println(area.getText());
            		row.add(area.getText());
            	}
            	if(atLeastOneData) {
            		DefaultTableModel model = (DefaultTableModel) table.getModel();
            		model.addRow(row);
                	
                	table.setModel(model);
                	for(String header : headers) {
                		JTextArea area = textAreaByHeader.get(header);
                		area.setText("");
                	}
            	}
            }
        });
        
		btn_save_xmlFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	try {
                    TableModel model = table.getModel();
                    File f = new File("savedDane.xml");
                    f.createNewFile();
                    System.out.println(f.getAbsolutePath());
                    
                    FileWriter xml = new FileWriter(f);
                    xml.write("<laptops>" + "\n");

                    HashMap<String, Integer> howManyChildren = new HashMap<String, Integer>();
                    howManyChildren.put("screen", 3);
                    howManyChildren.put("processor", 3);
                    howManyChildren.put("disc", 2);
                    howManyChildren.put("graphic_card", 2);
                    
                    HashMap<String, String> parentAfterMarkdown = new HashMap<String, String>();
                    parentAfterMarkdown.put("manufacturer", "screen");
                    parentAfterMarkdown.put("screen", "processor");
                    parentAfterMarkdown.put("ram", "disc");
                    parentAfterMarkdown.put("disc", "graphic_card");

                    for (int i = 0; i < model.getRowCount(); i++) {
                    	xml.write("\t<laptop>" + "\n");
                    	String previousColumnName = "";
                    	
                        HashMap<String, String> valueByKey = new HashMap<String, String>();
                        List<Integer> whichValuesToSkip = new ArrayList<Integer>();
                        
                        String previousColumn = "";
                        for (int j = 0; j < model.getColumnCount(); j++) {
                        	if(whichValuesToSkip.contains(j)) {
                        		continue;
                        	}
                        	
                        	String columnName = headerTranslations.get(model.getColumnName(j));
                        
							if(parentAfterMarkdown.get(previousColumn) != null) {
							    xml.write("\t\t<" + parentAfterMarkdown.get(previousColumn) + ">"); 
							    System.out.println("previousColumn = " + previousColumn);
							    System.out.println(howManyChildren);
							    Integer howManyChildrenForThisParent = howManyChildren.get(parentAfterMarkdown.get(previousColumn));
	                        	if(howManyChildrenForThisParent != null) {
	                        		for(int o = 0 ; o < howManyChildrenForThisParent; o++) {
	                        			System.out.println(j+o);
	                        			whichValuesToSkip.add(j+o);
	                        			String childrenColumnName = headerTranslations.get(model.getColumnName(j+o));
	                        			xml.write("\n\t\t\t<" + childrenColumnName + ">");
	                        			String childrenValue = (String) model.getValueAt(i, j+o);
	                        			if(childrenValue == null) {
	                        				childrenValue = "";
	                        			}
	                        			xml.write(childrenValue + "</" + childrenColumnName + ">");
	                        		}
	                        	}
	                        	xml.write("\n\t\t</" + parentAfterMarkdown.get(previousColumn) + ">\n");   
	                        	previousColumn = parentAfterMarkdown.get(previousColumn);
							}else {
								xml.write("\t\t<" + columnName + ">");
								String value = (String) model.getValueAt(i, j);
	                        	if(value == null) {
	                        		value = "";
	                        	}
	                            xml.write(value + "</" + columnName + ">" + "\n");
	                            previousColumn = columnName;
							}

                        	

                        }
                        xml.write("\t</laptop>\n");
                    }

                    xml.write("</laptops>");
                    xml.close();
                    
                   
                } catch (IOException ex) {
            		System.out.println("error");
                    System.out.println(ex.getMessage());
                }
            }
        });
		
        btn_save_textFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	try {

                    TableModel model = table.getModel();
                    File f = new File("savedDane.txt");
                    f.createNewFile();
                    System.out.println(f.getAbsolutePath());
                    
                    FileWriter csv = new FileWriter(f);

//                    for (int i = 0; i < model.getColumnCount(); i++) {
//                        csv.write(model.getColumnName(i) + ";");
////                    }
//
//                    csv.write("\n");

                    for (int i = 0; i < model.getRowCount(); i++) {
                        for (int j = 0; j < model.getColumnCount(); j++) {
                        	String value = (String) model.getValueAt(i,  j);
                        	if(value == null) {
                        		value = "";
                        	}
                            if(j==2) {
                            	csv.write(";");
                            }
                        	csv.write(value + ";");

                        }
                        csv.write("\n");
                    }

                    csv.close();
                    
                   
                } catch (IOException ex) {
            		System.out.println("error");
                    System.out.println(ex.getMessage());
                }
            }
        });
        
        btn_textFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	
            	colorByRow = new HashMap<Integer, Color>();
                wasModified = new HashMap<Integer, Boolean>();
                JFileChooser filechooser = new JFileChooser();

                int i = filechooser.showOpenDialog(null);
                if (i == JFileChooser.APPROVE_OPTION) {
                    File f = filechooser.getSelectedFile();
                    String filepath = f.getPath();
                    String fi = f.getName();
                    //Parsing CSV Data
                    System.out.print(filepath);
                    selected_file.setText(fi);
                    DefaultTableModel csv_data = new DefaultTableModel();

                    try {

                        int start = 0;
                        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(filepath));
                        CSVParser csvParser = CSVFormat.EXCEL.withDelimiter(';').parse(inputStreamReader);

                        for (String header: headers) {
                            csv_data.addColumn(header);
                        }

                        for (CSVRecord csvRecord: csvParser) {
                            Vector < String > row = new Vector < String > ();
                            for (Integer r = 0; r < 15; r++) {
                                if(r != 2) {
                                	row.add(csvRecord.get(r));
                                }
                            }
                            csv_data.addRow(row);
                        }
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                        System.out.println("Error in Parsing CSV File");
                    }
                    System.out.println(csv_data);
                    table.setModel(csv_data);

                }
            }
        });

        btn_xmlFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	
            	colorByRow = new HashMap<Integer, Color>();
                wasModified = new HashMap<Integer, Boolean>();
                
                JFileChooser filechooser = new JFileChooser();

                int i = filechooser.showOpenDialog(null);
                if (i == JFileChooser.APPROVE_OPTION) {
                    File f = filechooser.getSelectedFile();
                    String filepath = f.getPath();
                    String fi = f.getName();

                    selected_file.setText(fi);
                    DefaultTableModel xml_data = new DefaultTableModel();

                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

                    for (String header: headers) {
                        xml_data.addColumn(header);
                    }

                    try {
                      

                        DocumentBuilder db = dbf.newDocumentBuilder();

                        Document doc = db.parse(f);


                        if (doc.hasChildNodes()) {
                            printNote(doc.getChildNodes(), xml_data);
                        }

                        
                        table.setModel(xml_data);
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }

                }
            }
        });
    }
    
    static List < String > dbColumns = Arrays.asList(
            "manufacturer",
            "screen_size",
            "screen_type",
            "screen_touchscreen",
            "processor_name",
            "processor_physical_cores",
            "processor_clock_speed",
            "ram",
            "disc_storage",
            "disc_type",
            "graphic_card_name",
            "graphic_card_memory",
            "os",
            "disc_reader"
            );
    
    
	private static Vector<String> row = new Vector<String>();
    private static void printNote(NodeList nodeList, DefaultTableModel xml_data) {

    	for (int count = 0; count < nodeList.getLength(); count++) {

            Node tempNode = nodeList.item(count);

            // make sure it's element node.
            if (tempNode.getNodeType() == Node.ELEMENT_NODE) {

                // get node name and value
                if(!tempNode.getTextContent().contains("\n")) {
//                	System.out.println("\nNode Name =" + tempNode.getNodeName());
//                    System.out.println("Node Value =" + tempNode.getTextContent());
                    row.add(tempNode.getTextContent());
                    if(tempNode.getNodeName() == "disc_reader") {
                    	xml_data.addRow(row);
                    	System.out.println(row);
                    	row = new Vector<String>();
                    }
                }

                if (tempNode.hasAttributes()) {

                    // get attributes names and values
                    NamedNodeMap nodeMap = tempNode.getAttributes();
                    for (int i = 0; i < nodeMap.getLength(); i++) {
                        Node node = nodeMap.item(i);
                        System.out.println("attr name : " + node.getNodeName());
                        System.out.println("attr value : " + node.getNodeValue());
                    }

                }

                if (tempNode.hasChildNodes()) {
                    // loop again if has child nodes
                    printNote(tempNode.getChildNodes(), xml_data);
                }

            }

    	}

    }

	private static void fillHeaderTranslations() {
		headerTranslations.put("Producent", "manufacturer");
		headerTranslations.put("wielkość matrycy", "size");
		headerTranslations.put("typ matrycy", "type");
		headerTranslations.put("czy dotykowy ekran", "touchscreen");
		headerTranslations.put("procesor", "name");
		headerTranslations.put("liczba rdzeni fizyczynch", "physical_cores");
		headerTranslations.put("taktowanie", "clock_speed");
		headerTranslations.put("RAM", "ram");
		headerTranslations.put("pojemność dysku", "storage");
		headerTranslations.put("typ dysku", "type");
		headerTranslations.put("karta graficzna", "name");
		headerTranslations.put("pamięć karty graficznej", "memory");
		headerTranslations.put("system operacyjny", "os");
		headerTranslations.put("Napęd optyczny", "disc_reader");
	}
}
