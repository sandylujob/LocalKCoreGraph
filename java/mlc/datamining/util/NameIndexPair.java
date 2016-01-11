package mlc.datamining.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;


import org.apache.commons.lang.StringUtils;

/**
 * @author chen_lu
 *
 */
public class NameIndexPair {
	//"select index, name from table"
	private Hashtable htName = new Hashtable<Integer, String>();
	private Hashtable htIndex = new Hashtable<String, Integer>();
	
	public NameIndexPair() {
		super();
	}
	
	public void add(int index, String name) {
		htName.put(index, name);
		htIndex.put(name, index);		
	}
	
	public void add(String name, int index) {
		add(index, name);		
	}
	
	
	public NameIndexPair(String path, int indexColumn, int nameColumn, String seperator) throws NumberFormatException, IOException {
		super();
		readAllFromFile(path, indexColumn, nameColumn, seperator);
	}
	
	public void print() throws IOException {
		print(null);
	}
	
	public void print(String path) throws IOException {
		Writer  out = null;
		
		if(path==null) {
			out = new PrintWriter(System.out);
		} else {
			File file = new File(path);
			out = new FileWriter(file);
		}
		
		ArrayList<Integer> allIndex = new ArrayList<Integer>(htName.keySet());
		Collections.sort(allIndex);
		for(Integer i : allIndex) {
			StringBuilder sb = new StringBuilder();
			sb.append(i).append("\t").append(htName.get(i)).append("\n");					
			out.write(sb.toString());
		}
		out.close();
	}
	
	private void readAllFromFile(String path, int indexColumn, int nameColumn, String seperator) throws NumberFormatException, IOException {		
		BufferedReader input = new BufferedReader(new FileReader(new File(path)));
		String line = null; 
		
		for (int i=0; (line = input.readLine())!= null; i++){
			if(StringUtils.isNotEmpty(line)) {
				String[] strArr = line.split(seperator);
				if(strArr.length>indexColumn && strArr.length>nameColumn) {
					int index = i;
					if(indexColumn>=0) {
						index = Integer.parseInt(strArr[indexColumn]);
					}
					String name = strArr[nameColumn];
					htName.put(index, name);
					htIndex.put(name, index);
				}
	        }
		}
		input.close();
	}
	
	private void readAllFromTable(Connection conn, String sql) throws SQLException {		
		if(conn!=null) {
			Statement statement = null;
	        try {
	            statement = conn.createStatement();
				ResultSet rs = statement.executeQuery(sql);
				while(rs.next()) {
					int index = rs.getInt("index");
					String name = rs.getString("name");
					htName.put(index, name);
					htIndex.put(name, index);
				}
	        }catch (Exception e) {
	            System.out.println("Error: " + e);
	        } finally {
	        	if(statement!=null) {
		        	statement.close();	        		
	        	}
	        	conn.close();
	        }
		}
	}
	
	public String getNameByIndex(String index) {
		return (String) htName.get(new Integer(index));
	}
	
	public String getNameByIndex(Integer index) {
		return (String) htName.get(index);
	}
	
	public Integer getIndexByName(String name) {
		return (Integer) htIndex.get(name);
	}
	
	public int size() {
		return htIndex.size();
	}
	
	public static void main(String[] args) throws SQLException, IOException {
/*		NameIndexPair yaoNameIndex = new NameIndexPair("select index, name from yao");
		yaoNameIndex.print(BaseDB.TEXT_FILE_PATH + "yao_index.txt");
*/		
/*		NameIndexPair fangNameIndex = new NameIndexPair("select index, name from fang");
		fangNameIndex.print(Base.TEXT_FILE_PATH + "fang_index.txt");
*/
//		NameIndexPair yaoChemIndex = new NameIndexPair("select index, name from yao_unique_chem");
//		yaoChemIndex.print(Base.TEXT_FILE_PATH + "chem_index.txt");

		NameIndexPair yaoIndex = new NameIndexPair("c:/data/dev/java/tcm/data/text/fangji/YaoCode.txt", 0, 1, "\t");
		yaoIndex.print("c:/data/dev/java/tcm/data/text/fangji/YaoCode_pair.txt");
	}
}
